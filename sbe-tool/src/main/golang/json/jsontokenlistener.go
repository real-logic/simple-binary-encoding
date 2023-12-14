package json

import (
	"encoding/hex"
	"strconv"
	"strings"

	"github.com/real-logic/simple-binary-encoding/otf"
)

// JsonTokenListener dynamically decodes to convert them to JSON for output.
type JsonTokenListener struct {
	output         *strings.Builder
	indentation    int
	compositeLevel int
	more           bool
}

func newJsonTokenListener(output *strings.Builder) *JsonTokenListener {
	return &JsonTokenListener{
		output: output,
	}
}

func (t *JsonTokenListener) printScope() {
	for i := 0; i < t.compositeLevel; i++ {
		t.output.WriteString("  ")
	}
}

func isLastGroup(groupIndex uint64, numInGroup uint64) bool {
	return groupIndex == numInGroup-1
}

func (t *JsonTokenListener) startObject() {
	t.indent()
	t.output.WriteString("{\n")
	t.indentation++
	t.more = false
}

func (t *JsonTokenListener) endObject() {
	t.output.WriteString("\n")
	t.indentation--
	t.indent()
	t.output.WriteString("}")
	t.more = true
}

func (t *JsonTokenListener) property(name string) {
	t.indent()
	t.doubleQuote()
	t.output.WriteString(name)
	t.output.WriteString("\": ")
}

func (t *JsonTokenListener) indent() {
	for i := 0; i < t.indentation; i++ {
		t.output.WriteString("    ")
	}
}

func (t *JsonTokenListener) doubleQuote() {
	t.output.WriteString("\"")
}

func (t *JsonTokenListener) prev() {
	if t.more {
		t.output.WriteString(",\n")
	}
}

func (t *JsonTokenListener) next() {
	t.more = true
}

func (t *JsonTokenListener) escapePrintableChar(buffer []byte, index int, size int, elementSize int) {
	for i := 0; i < size; i++ {
		c := buffer[index+i*elementSize]
		if c > 0 {
			t.escape(rune(c))
		} else {
			break
		}
	}
}

func (t *JsonTokenListener) escapeString(str string) {
	for _, c := range str {
		t.escape(c)
	}
}

func (t *JsonTokenListener) escape(c rune) {
	if c == '"' || c == '\\' || c == '\b' || c == '\f' || c == '\n' || c == '\r' || c == '\t' {
		t.output.WriteString("\\")
	}

	t.output.WriteRune(c)
}

func (t *JsonTokenListener) determineName(
	thresholdLevel int,
	fieldToken otf.Token,
	tokens []otf.Token,
	fromIndex int,
) string {
	if t.compositeLevel > thresholdLevel {
		return tokens[fromIndex].Name()
	} else {
		return fieldToken.Name()
	}

}

func (t *JsonTokenListener) OnBeginMessage(token otf.Token) {
	t.startObject()
}

func (t *JsonTokenListener) OnEndMessage(token otf.Token) {
	t.endObject()
}

func (t *JsonTokenListener) OnEncoding(fieldToken otf.Token, buffer []byte, typeToken otf.Token, actingVersion uint64) {
	t.prev()
	name := fieldToken.Name()
	if t.compositeLevel > 0 {
		name = fieldToken.Name()
	}
	t.property(name)
	t.appendEncodingAsString(buffer, typeToken, uint64(fieldToken.TokenVersion()), actingVersion)
	t.next()
}

func (t *JsonTokenListener) appendEncodingAsString(
	buffer []byte,
	typeToken otf.Token,
	fieldVersion uint64,
	actingVersion uint64,
) {
	arrayLength := typeToken.ArrayLength()
	encoding := typeToken.Encoding()
	constOrNotPresentValue := t.constOrNotPresentValue(typeToken, fieldVersion, actingVersion)
	if constOrNotPresentValue.PrimitiveType != otf.NONE {
		characterEncoding := encoding.CharacterEncoding()
		if characterEncoding == "" {
			t.doubleQuote()
			t.escapeString(constOrNotPresentValue.String())
			t.doubleQuote()
		} else {
			if arrayLength < 2 {
				t.doubleQuote()
				t.escapeString(constOrNotPresentValue.String())
				t.doubleQuote()
			} else {
				t.output.WriteString("[")
				for i := int32(0); i < arrayLength; i++ {
					if i > 0 {
						t.output.WriteString(", ")
					}
					t.doubleQuote()
					t.escapeString(constOrNotPresentValue.String())
					t.doubleQuote()
				}
				t.output.WriteString("]")
			}
		}
	} else {
		elementSize := encoding.PrimitiveType().Size()
		if arrayLength < 1 && encoding.PrimitiveType() == otf.CHAR {
			t.doubleQuote()
			t.escapePrintableChar(buffer, 0, int(arrayLength), elementSize)
		} else {
			encoding := typeToken.Encoding()
			elementSize := int32(encoding.PrimitiveType().Size())
			if arrayLength == 1 {
				otf.AppendPrimitiveValueJson(t.output, otf.NewPrimitiveValue(encoding.PrimitiveType(), buffer))
			} else {
				t.output.WriteString("[")
				for i := int32(0); i < typeToken.ArrayLength(); i++ {
					if i > 0 {
						t.output.WriteString(", ")
					}
					otf.AppendPrimitiveValueJson(t.output, otf.NewPrimitiveValue(encoding.PrimitiveType(),
						buffer[i*elementSize:(i+1)*elementSize]))
				}
				t.output.WriteString("]")
			}
		}
	}
}

func (t *JsonTokenListener) readEncodingAsInt(
	buffer []byte,
	typeToken otf.Token,
	fieldVersion uint64,
	actingVersion uint64,
) int64 {
	constOrNotPresentValue := t.constOrNotPresentValue(typeToken, fieldVersion, actingVersion)
	if constOrNotPresentValue.PrimitiveType != otf.NONE {
		return constOrNotPresentValue.AsInt()
	}
	typeTokenEncoding := typeToken.Encoding()
	v, _ := otf.GetAsInt(typeTokenEncoding.PrimitiveType(), typeTokenEncoding.ByteOrder(), buffer)
	return v
}

func (t *JsonTokenListener) readEncodingAsUInt(
	buffer []byte,
	typeToken otf.Token,
	fieldVersion uint64,
	actingVersion uint64,
) uint64 {
	constOrNotPresentValue := t.constOrNotPresentValue(typeToken, fieldVersion, actingVersion)
	if constOrNotPresentValue.PrimitiveType != otf.NONE {
		return constOrNotPresentValue.AsUInt()
	}
	typeTokenEncoding := typeToken.Encoding()
	v, _ := otf.GetAsUInt(typeTokenEncoding.PrimitiveType(), typeTokenEncoding.ByteOrder(), buffer)
	return v
}

func (t *JsonTokenListener) constOrNotPresentValue(
	typeToken otf.Token,
	fieldVersion uint64,
	actingVersion uint64,
) otf.PrimitiveValue {
	typeTokenEncoding := typeToken.Encoding()
	if typeToken.IsConstantEncoding() {
		return typeTokenEncoding.ConstValue()
	} else if typeToken.IsOptionalEncoding() && actingVersion < fieldVersion {
		return typeTokenEncoding.ApplicableNullValue()
	} else {
		return otf.PrimitiveValue{}
	}
}

func (t *JsonTokenListener) OnEnum(
	fieldToken otf.Token,
	buffer []byte,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
	actingVersion uint64,
) {
	t.prev()
	typeToken := tokens[fromIndex+1]
	encoding := typeToken.Encoding()

	value := ""
	if fieldToken.IsConstantEncoding() {
		encoding := fieldToken.Encoding()
		refValue := encoding.ConstValue()
		indexOfDot := strings.LastIndex(refValue.String(), ".")
		if indexOfDot == -1 {
			value = refValue.String()
		} else {
			value = refValue.String()[indexOfDot+1:]
		}
	} else if encoding.PrimitiveType().IsUnsigned() {
		encodedValue := t.readEncodingAsUInt(
			buffer,
			typeToken,
			uint64(fieldToken.TokenVersion()),
			actingVersion,
		)
		for i := fromIndex + 1; i < toIndex; i++ {
			encoding := tokens[i].Encoding()
			constValue := encoding.ConstValue()
			if constValue.AsUInt() == encodedValue {
				value = tokens[i].Name()
				break
			}
		}
	} else {
		encodedValue := t.readEncodingAsInt(
			buffer,
			typeToken,
			uint64(fieldToken.TokenVersion()),
			actingVersion,
		)
		for i := fromIndex + 1; i < toIndex; i++ {
			encoding := tokens[i].Encoding()
			constValue := encoding.ConstValue()
			if constValue.AsInt() == encodedValue {
				value = tokens[i].Name()
				break
			}
		}
	}

	t.property(t.determineName(0, fieldToken, tokens, fromIndex))
	t.doubleQuote()
	t.output.WriteString(value)
	t.doubleQuote()
	t.next()
}

func (t *JsonTokenListener) OnBitSet(
	fieldToken otf.Token,
	buffer []byte,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
	actingVersion uint64,
) {
	t.prev()
	typeToken := tokens[fromIndex+1]
	encodedValue := t.readEncodingAsInt(
		buffer,
		typeToken,
		uint64(fieldToken.TokenVersion()),
		actingVersion,
	)
	t.property(t.determineName(0, fieldToken, tokens, fromIndex))

	t.output.WriteString("{ ")
	for i := fromIndex + 1; i < toIndex; i++ {
		t.output.WriteString(`"`)
		t.output.WriteString(tokens[i].Name())
		t.output.WriteString(`": `)
		encoding := tokens[i].Encoding()
		constValue := encoding.ConstValue()
		bitPosition := constValue.AsInt()
		flag := (encodedValue & (1 << uint64(bitPosition))) != 0
		t.output.WriteString(strconv.FormatBool(flag))
		if i < toIndex-1 {
			t.output.WriteString(", ")
		}
	}
	t.output.WriteString(` }`)
	t.next()
}

func (t *JsonTokenListener) OnBeginComposite(
	fieldToken otf.Token,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
) {
	t.prev()
	t.compositeLevel++
	t.property(t.determineName(0, fieldToken, tokens, fromIndex))
	t.output.WriteString("\n")
	t.startObject()
}

func (t *JsonTokenListener) OnEndComposite(
	token otf.Token,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
) {
	t.compositeLevel--
	t.endObject()
}

func (t *JsonTokenListener) OnGroupHeader(
	token otf.Token,
	numInGroup uint64,
) {
	t.prev()
	t.property(token.Name())
	if numInGroup > 0 {
		t.output.WriteString("[\n")
		t.more = false
	} else {
		t.output.WriteString("[]")
	}
}

func (t *JsonTokenListener) OnBeginGroup(
	token otf.Token,
	groupIndex uint64,
	numInGroup uint64,
) {
	t.prev()
	t.startObject()
}

func (t *JsonTokenListener) OnEndGroup(
	token otf.Token,
	groupIndex uint64,
	numInGroup uint64,
) {
	t.endObject()
	if isLastGroup(groupIndex, numInGroup) {
		t.output.WriteString("]")
	}
}

func (t *JsonTokenListener) OnVarData(
	fieldToken otf.Token,
	buffer []byte,
	length uint64,
	typeToken otf.Token,
) {
	t.prev()
	t.property(fieldToken.Name())
	t.doubleQuote()

	typeTokenEncoding := typeToken.Encoding()
	if characterEncoding := typeTokenEncoding.CharacterEncoding(); characterEncoding == "" {
		t.escapeString(hex.EncodeToString(buffer[:length]))
	} else {
		t.escapeString(string(buffer[:length]))
	}
	t.doubleQuote()
	t.next()
}
