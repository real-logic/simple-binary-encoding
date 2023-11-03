package otf

import (
	"fmt"
)

type Signal int

const (
	SignalBeginMessage   Signal = 1
	SignalEndMessage     Signal = 2
	SignalBeginComposite Signal = 3
	SignalEndComposite   Signal = 4
	SignalBeginField     Signal = 5
	SignalEndField       Signal = 6
	SignalBeginGroup     Signal = 7
	SignalEndGroup       Signal = 8
	SignalBeginEnum      Signal = 9
	SignalValidValue     Signal = 10
	SignalEndEnum        Signal = 11
	SignalBeginSet       Signal = 12
	SignalChoice         Signal = 13
	SignalEndSet         Signal = 14
	SignalBeginVarData   Signal = 15
	SignalEndVarData     Signal = 16
	SignalEncoding       Signal = 17
)

func (s Signal) String() string {
	switch s {
	case SignalBeginMessage:
		return "BeginMessage"
	case SignalEndMessage:
		return "EndMessage"
	case SignalBeginComposite:
		return "BeginComposite"
	case SignalEndComposite:
		return "EndComposite"
	case SignalBeginField:
		return "BeginField"
	case SignalEndField:
		return "EndField"
	case SignalBeginGroup:
		return "BeginGroup"
	case SignalEndGroup:
		return "EndGroup"
	case SignalBeginEnum:
		return "BeginEnum"
	case SignalValidValue:
		return "ValidValue"
	case SignalEndEnum:
		return "EndEnum"
	case SignalBeginSet:
		return "BeginSet"
	case SignalChoice:
		return "Choice"
	case SignalEndSet:
		return "EndSet"
	case SignalBeginVarData:
		return "BeginVarData"
	case SignalEndVarData:
		return "EndVarData"
	case SignalEncoding:
		return "Encoding"
	default:
		return "Unknown"
	}
}

type Token struct {
	offset              int32
	fieldId             int32
	version             int32
	encodedLength       int32
	componentTokenCount int32
	signal              Signal
	name                string
	referencedName      string
	description         string
	encoding            Encoding
}

func (token Token) ReferencedName() string {
	return token.referencedName
}

func (token Token) Signal() Signal {
	return token.signal
}

func (token Token) Name() string {
	return token.name
}

func (token Token) Description() string {
	return token.description
}

func (token Token) FieldId() int32 {
	return token.fieldId
}

func (token Token) TokenVersion() int32 {
	return token.version
}

// ApplicableTypeName returns the name of the type that should be applied in context.
func (token Token) ApplicableTypeName() string {
	if token.referencedName == "" {
		return token.name
	}
	return token.referencedName
}

func (token Token) Encoding() Encoding {
	return token.encoding
}

// EncodedLength returns the length of the encoded primitive in bytes.
func (token Token) EncodedLength() int32 {
	return token.encodedLength
}

// ArrayLength returns the number of encoded primitives in this type.
func (token Token) ArrayLength() int32 {
	if token.encoding.PrimitiveType() == NONE || token.encodedLength == 0 {
		return 0
	}
	return token.encodedLength / int32(token.encoding.PrimitiveType().Size())
}

func (token Token) Offset() int32 {
	return token.offset
}

func (token Token) ComponentTokenCount() int32 {
	return token.componentTokenCount
}

// IsConstantEncoding returns true if the encoding presence is constant or false if not.
func (token Token) IsConstantEncoding() bool {
	return token.encoding.presence == SbeConstant
}

// IsOptionalEncoding returns true if the encoding presence is optional or false if not.
func (token Token) IsOptionalEncoding() bool {
	return token.encoding.presence == SbeOptional
}

func (token Token) String() string {
	return fmt.Sprintf(
		"Token{signal: %s, name: %s, description: %s, fieldId: %d, tokenVersion: %d, encoding: %s, encodedLength: %d, offset: %d, componentTokenCount: %d}",
		token.signal,
		token.name,
		token.description,
		token.fieldId,
		token.version,
		&token.encoding,
		token.encodedLength,
		token.offset,
		token.componentTokenCount)
}
