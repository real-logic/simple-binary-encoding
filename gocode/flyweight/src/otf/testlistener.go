package otf

import (
	"fmt"
	"github.com/real-logic/simple-binary-encoding/otf"
	"strconv"
	"strings"
)

type TestListener struct {
	out            strings.Builder
	compositeLevel int
	namedScope     []string
}

func (t *TestListener) OnBeginMessage(token otf.Token) {
	t.namedScope = append(t.namedScope, token.Name()+".")
}

func (t *TestListener) OnEndMessage(token otf.Token) {
	t.namedScope = t.namedScope[:len(t.namedScope)-1]
}

func (t *TestListener) OnEncoding(fieldToken otf.Token, buffer []byte, typeToken otf.Token, actingVersion uint64) {
	value := t.readEncodingAsString(buffer, fieldToken, typeToken, uint64(fieldToken.TokenVersion()), actingVersion)

	t.printScope()
	if t.compositeLevel > 0 {
		t.out.WriteString(typeToken.Name())
	} else {
		t.out.WriteString(fieldToken.Name())
	}
	t.out.WriteString("=")
	t.out.WriteString(value)
	t.out.WriteString("\n")

}

func (t *TestListener) printScope() {
	for i := range t.namedScope {
		t.out.WriteString(t.namedScope[i])
	}
}

func (t *TestListener) determineName(
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

func (t *TestListener) readEncodingAsString(
	buffer []byte,
	fieldToken otf.Token,
	typeToken otf.Token,
	fieldVersion uint64,
	actingVersion uint64,
) string {
	constOrNotPresentValue := t.constOrNotPresentValue(typeToken, fieldVersion, actingVersion)
	if constOrNotPresentValue.PrimitiveType != otf.NONE {
		encoding := typeToken.Encoding()
		characterEncoding := encoding.CharacterEncoding()
		if constOrNotPresentValue.Size() == 1 && characterEncoding == "" {
			// TODO java uses this
			// final byte[] bytes = { (byte)constOrNotPresentValue.longValue() };
			// return new String(bytes, characterEncoding);
			return constOrNotPresentValue.String()
		} else {
			value := constOrNotPresentValue.String()
			size := typeToken.ArrayLength()
			if size < 2 {
				return value
			}
			sb := strings.Builder{}
			more := false
			for i := int32(0); i < size; i++ {
				if more {
					sb.WriteString(", ")
					more = true
				}
				sb.WriteString(value)
			}
			return sb.String()
		}
	}

	sb := strings.Builder{}
	encoding := typeToken.Encoding()
	elementSize := int32(encoding.PrimitiveType().Size())

	more := false
	for i := int32(0); i < typeToken.ArrayLength(); i++ {
		if more {
			sb.WriteString(", ")
			more = true
		}
		arrayValue := otf.NewPrimitiveValue(encoding.PrimitiveType(), buffer[i*elementSize:(i+1)*elementSize])
		sb.WriteString(arrayValue.String())
	}
	return sb.String()
}

func (t *TestListener) readEncodingAsInt(
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

func (t *TestListener) readEncodingAsUInt(
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

func (t *TestListener) constOrNotPresentValue(
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

func (t *TestListener) OnEnum(
	fieldToken otf.Token,
	buffer []byte,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
	actingVersion uint64,
) {
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

	t.printScope()
	t.out.WriteString(t.determineName(0, fieldToken, tokens, fromIndex))
	t.out.WriteString("=")
	t.out.WriteString(value)
	t.out.WriteString("\n")
}

func (t *TestListener) OnBitSet(
	fieldToken otf.Token,
	buffer []byte,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
	actingVersion uint64,
) {
	typeToken := tokens[fromIndex+1]
	encodedValue := t.readEncodingAsInt(
		buffer,
		typeToken,
		uint64(fieldToken.TokenVersion()),
		actingVersion,
	)
	t.printScope()
	t.out.WriteString(t.determineName(0, fieldToken, tokens, fromIndex))
	t.out.WriteString(":")
	for i := fromIndex + 1; i < toIndex; i++ {
		t.out.WriteString(" ")
		t.out.WriteString(tokens[i].Name())
		t.out.WriteString("=")
		encoding := tokens[i].Encoding()
		constValue := encoding.ConstValue()
		bitPosition := constValue.AsInt()
		flag := (encodedValue & (1 << uint64(bitPosition))) != 0
		t.out.WriteString(strconv.FormatBool(flag))
	}
	t.out.WriteString("\n")
}

func (t *TestListener) OnBeginComposite(
	fieldToken otf.Token,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
) {
	t.compositeLevel++
	t.namedScope = append(t.namedScope, t.determineName(1, fieldToken, tokens, fromIndex)+".")
}

func (t *TestListener) OnEndComposite(
	token otf.Token,
	tokens []otf.Token,
	fromIndex int,
	toIndex int,
) {
	t.compositeLevel--
	t.namedScope = t.namedScope[:len(t.namedScope)-1]
}

func (t *TestListener) OnGroupHeader(
	token otf.Token,
	numInGroup uint64,
) {
	t.printScope()
	t.out.WriteString(token.Name())
	t.out.WriteString(" Group Header : numInGroup=")
	t.out.WriteString(strconv.FormatUint(numInGroup, 10))
	t.out.WriteString("\n")
}

func (t *TestListener) OnBeginGroup(
	token otf.Token,
	groupIndex uint64,
	numInGroup uint64,
) {
	t.namedScope = append(t.namedScope, token.Name()+".")
}

func (t *TestListener) OnEndGroup(
	token otf.Token,
	groupIndex uint64,
	numInGroup uint64,
) {
	t.namedScope = t.namedScope[:len(t.namedScope)-1]
}

func (t *TestListener) OnVarData(
	fieldToken otf.Token,
	buffer []byte,
	length uint64,
	typeToken otf.Token,
) {
	value := ""
	typeTokenEncoding := typeToken.Encoding()
	if characterEncoding := typeTokenEncoding.CharacterEncoding(); characterEncoding == "" {
		value = fmt.Sprintf("%d bytes of raw data", length)
	} else {
		value = string(buffer[:length])
	}

	t.printScope()
	t.out.WriteString(fieldToken.Name())
	t.out.WriteString("=")
	t.out.WriteString(value)
	t.out.WriteString("\n")
}
