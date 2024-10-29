package otf

import (
	"os"
	"sort"
	"strings"

	ir "github.com/real-logic/simple-binary-encoding/uk_co_real_logic_sbe_ir_generated"
)

type IrDecoder struct {
	headerTokens []Token
	messages     [][]Token
	buffer       []byte
	length       uint64
	id           int32
	typesByName  map[string][]Token
}

func NewIrDecoder() *IrDecoder {
	return &IrDecoder{
		typesByName: make(map[string][]Token),
	}
}

func (decoder *IrDecoder) Decode(irBuffer []byte) int {
	length := uint64(len(irBuffer))
	decoder.length = length
	if length == 0 {
		return -1
	}
	decoder.buffer = make([]byte, length)
	copy(decoder.buffer, irBuffer)
	return decoder.decodeIr()
}

func (decoder *IrDecoder) DecodeFile(filename string) int {
	fileStat, err := os.Stat(filename)
	if err != nil {
		return -1
	}

	decoder.length = uint64(fileStat.Size())
	if decoder.length == 0 {
		return -1
	}
	decoder.buffer = make([]byte, decoder.length)

	if err := readFileIntoBuffer(decoder.buffer, filename, decoder.length); err != nil {
		return -1
	}

	return decoder.decodeIr()
}

func (decoder *IrDecoder) Header() []Token {
	return decoder.headerTokens
}

func (decoder *IrDecoder) Messages() [][]Token {
	return decoder.messages
}

func (decoder *IrDecoder) Message(id int32, version int32) []Token {
	for _, tokens := range decoder.messages {
		token := &tokens[0]
		if token.Signal() == SignalBeginMessage && token.FieldId() == id && token.TokenVersion() == version {
			return tokens
		}
	}
	return nil
}

func (decoder *IrDecoder) MessageByID(id int32) []Token {
	for _, tokens := range decoder.messages {
		token := &tokens[0]
		if token.Signal() == SignalBeginMessage && token.FieldId() == id {
			return tokens
		}
	}
	return nil
}

// TypeByName gets the type representation for a given type name.
func (decoder *IrDecoder) TypeByName(name string) []Token {
	return decoder.typesByName[name]
}

func (decoder *IrDecoder) decodeIr() int {
	var frame ir.FrameCodec
	offset := uint64(0)
	var tmp [256]byte

	frame.WrapForDecode(
		decoder.buffer,
		offset,
		uint64(frame.SbeBlockLength()),
		uint64(frame.SbeSchemaVersion()),
		decoder.length,
	)

	frame.GetPackageName(tmp[:])

	if frame.IrVersion() != 0 {
		return -1
	}

	frame.GetNamespaceName(tmp[:])
	frame.GetSemanticVersion(tmp[:])

	offset += frame.EncodedLength()

	decoder.headerTokens = make([]Token, 0)

	headerLength := decoder.readHeader(offset)

	decoder.id = frame.IrId()

	offset += headerLength

	for offset < decoder.length {
		offset += decoder.readMessage(offset)
	}

	decoder.captureTypes(decoder.headerTokens, 0, len(decoder.headerTokens)-1)

	return 0
}
func (decoder *IrDecoder) decodeAndAddToken(tokens *[]Token, offset uint64) uint64 {
	var tokenCodec ir.TokenCodec
	tokenCodec.WrapForDecode(
		decoder.buffer,
		offset,
		uint64(tokenCodec.SbeBlockLength()),
		uint64(tokenCodec.SbeSchemaVersion()),
		decoder.length,
	)

	signal := Signal(tokenCodec.Signal())
	primitiveType := PrimitiveType(tokenCodec.PrimitiveType())
	presence := Presence(tokenCodec.Presence())
	byteOrder := ByteOrder(tokenCodec.ByteOrder())
	tokenOffset := tokenCodec.TokenOffset()
	tokenSize := tokenCodec.TokenSize()
	id := tokenCodec.FieldId()
	version := tokenCodec.TokenVersion()
	componentTokenCount := tokenCodec.ComponentTokenCount()

	name := tokenCodec.GetNameAsString()

	tmpBuffer := [256]byte{}
	tmpLength := tokenCodec.GetConstValue(tmpBuffer[:])
	constValue := NewPrimitiveValue(primitiveType, tmpBuffer[:tmpLength])
	tmpLength = tokenCodec.GetMinValue(tmpBuffer[:])
	minValue := NewPrimitiveValue(primitiveType, tmpBuffer[:tmpLength])
	tmpLength = tokenCodec.GetMaxValue(tmpBuffer[:])
	maxValue := NewPrimitiveValue(primitiveType, tmpBuffer[:tmpLength])
	tmpLength = tokenCodec.GetNullValue(tmpBuffer[:])
	nullValue := NewPrimitiveValue(primitiveType, tmpBuffer[:tmpLength])

	characterEncoding := tokenCodec.GetCharacterEncodingAsString()
	epoch := tokenCodec.GetEpochAsString()
	timeUnit := tokenCodec.GetTimeUnitAsString()
	semanticType := tokenCodec.GetSemanticTypeAsString()
	description := tokenCodec.GetDescriptionAsString()
	referencedName := tokenCodec.GetReferencedNameAsString()

	encoding := NewEncoding(
		primitiveType,
		presence,
		byteOrder,
		minValue,
		maxValue,
		nullValue,
		constValue,
		characterEncoding,
		epoch,
		timeUnit,
		semanticType,
	)

	token := Token{
		offset:              tokenOffset,
		fieldId:             id,
		version:             version,
		encodedLength:       tokenSize,
		componentTokenCount: componentTokenCount,
		signal:              signal,
		name:                name,
		referencedName:      referencedName,
		description:         description,
		encoding:            encoding,
	}

	*tokens = append(*tokens, token)

	return tokenCodec.EncodedLength()
}

func (decoder *IrDecoder) readHeader(offset uint64) uint64 {
	size := uint64(0)
	for offset+size < decoder.length {
		size += decoder.decodeAndAddToken(&decoder.headerTokens, offset+size)
		token := &decoder.headerTokens[len(decoder.headerTokens)-1]
		if token.signal == SignalEndComposite {
			break
		}
	}
	return size
}

func (decoder *IrDecoder) readMessage(offset uint64) uint64 {
	size := uint64(0)
	tokensForMessage := make([]Token, 0)
	for offset+size < decoder.length {
		size += decoder.decodeAndAddToken(&tokensForMessage, offset+size)
		token := &tokensForMessage[len(tokensForMessage)-1]
		if token.signal == SignalEndMessage {
			break
		}
	}

	decoder.captureTypes(tokensForMessage, 0, len(tokensForMessage)-1)
	decoder.updateComponentTokenCounts(tokensForMessage)

	decoder.messages = append(decoder.messages, tokensForMessage)
	return size
}

func (decoder *IrDecoder) Id() int32 {
	return decoder.id
}

func readFileIntoBuffer(buffer []byte, filename string, length uint64) error {
	f, err := os.Open(filename)
	if err != nil {
		return err
	}
	defer f.Close()

	var remaining = length
	for remaining > 0 {
		bytes := uint(4098)
		if remaining < 4098 {
			bytes = uint(remaining)
		}
		sz, err := f.Read(buffer[length-remaining : length-remaining+uint64(bytes)])
		if err != nil {
			return err
		}
		remaining -= uint64(sz)
	}

	return nil
}

func (decoder *IrDecoder) captureTypes(tokens []Token, beginIndex, endIndex int) {
	for i := beginIndex; i < endIndex; i++ {
		token := tokens[i]
		typeBeginIndex := i
		switch token.Signal() {
		case SignalBeginComposite:
			i = decoder.captureType(
				tokens,
				i,
				SignalEndComposite,
				token.Name(),
				token.ReferencedName(),
			)
			decoder.captureTypes(tokens, typeBeginIndex+1, i-1)
		case SignalBeginEnum:
			i = decoder.captureType(
				tokens,
				i,
				SignalEndEnum,
				token.Name(),
				token.ReferencedName(),
			)
		case SignalBeginSet:
			i = decoder.captureType(
				tokens,
				i,
				SignalEndSet,
				token.Name(),
				token.ReferencedName(),
			)
		default:
		}
	}
}

func (decoder *IrDecoder) captureType(
	tokens []Token,
	index int,
	endSignal Signal,
	name, referencedName string,
) int {
	var typeTokens []Token
	i := index
	token := tokens[i]
	typeTokens = append(typeTokens, token)
	for {
		i++
		token = tokens[i]
		typeTokens = append(typeTokens, token)
		if token.Signal() == endSignal || name == token.Name() {
			break
		}
	}

	decoder.updateComponentTokenCounts(typeTokens)

	typeName := referencedName
	if typeName == "" {
		typeName = name
	}
	decoder.typesByName[typeName] = typeTokens
	return i
}

// updateComponentTokenCounts iterates over a list of Tokens and updates
// their counts of how many tokens make up each component.
func (decoder *IrDecoder) updateComponentTokenCounts(tokens []Token) {
	beginIndexMap := make(map[string][]int)
	for i, size := 0, len(tokens); i < size; i++ {
		token := tokens[i]
		signal := token.Signal()
		if strings.HasPrefix(signal.String(), "Begin") {
			componentType := signal.String()[5:]
			beginIndexMap[componentType] = append(beginIndexMap[componentType], i)
		} else if strings.HasPrefix(signal.String(), "End") {
			componentType := signal.String()[3:]
			beginIndices := beginIndexMap[componentType]
			last := len(beginIndices) - 1
			beginIndex := beginIndices[last]
			beginIndexMap[componentType] = beginIndices[:last]
			componentTokenCount := int32((i - beginIndex) + 1)
			tokens[beginIndex].componentTokenCount = componentTokenCount
			token.componentTokenCount = componentTokenCount
		}
	}
}

func (decoder *IrDecoder) Types() [][]Token {
	var typeNames []string
	for name := range decoder.typesByName {
		typeNames = append(typeNames, name)
	}
	sort.Strings(typeNames)
	var types [][]Token
	for _, name := range typeNames {
		types = append(types, decoder.typesByName[name])
	}
	return types
}
