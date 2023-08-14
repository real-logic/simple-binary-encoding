package otf

import (
	"fmt"
	"github.com/real-logic/simple-binary-encoding/otf"
	"github.com/real-logic/simple-binary-encoding/otf/test/code_generation_test"
	"github.com/stretchr/testify/assert"
	"strconv"
	"strings"
	"testing"
)

//go:generate java -Dsbe.output.dir=. -Dsbe.target.language=golang -Dsbe.go.generate.generate.flyweights=true -Dsbe.generate.ir=true -jar ../../../../sbe-all/build/libs/sbe-all-1.30.0-SNAPSHOT.jar ../../../../sbe-tool/src/test/resources/code-generation-schema.xml

const SchemaFilename = "code-generation-schema.sbeir"
const MsgBufferCapacity = 4 * 1024

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

func TestOtf(t *testing.T) {
	assert := assert.New(t)

	ir := otf.NewIrDecoder()
	if ir.DecodeFile(SchemaFilename) < 0 {
		t.Errorf("Error decoding %s", SchemaFilename)
	}

	var car code_generation_test.Car
	buffer := make([]byte, MsgBufferCapacity)
	encodedLength := encodeCar(&car, buffer)

	headerDecoder, _ := otf.NewOtfHeaderDecoder(ir.Header())
	bufferOffset := 0
	templateId, _ := headerDecoder.TemplateId(buffer[bufferOffset:])
	schemaId, _ := headerDecoder.SchemaId(buffer[bufferOffset:])
	actingVersion, _ := headerDecoder.SchemaVersion(buffer[bufferOffset:])
	blockLength, _ := headerDecoder.BlockLength(buffer[bufferOffset:])

	if uint16(schemaId) != car.SbeSchemaId() {
		t.Errorf("Invalid schema id: %d", schemaId)
	}

	bufferOffset += int(headerDecoder.EncodedLength())
	listener := &TestListener{}
	tokens := ir.MessageByID(int32(templateId))
	bufferOffset = otf.Decode(
		buffer[bufferOffset:],
		actingVersion,
		blockLength,
		tokens,
		listener,
	)

	assert.EqualValues(bufferOffset, encodedLength, "Message not fully decoded")
	expected := `Car.serialNumber=1234
Car.modelYear=2013
Car.available=T
Car.code=A
Car.someNumbers=01234
Car.vehicleCode=abcdef
Car.extras: sunRoof=false sportsPack=false cruiseControl=false
Car.discountedModel=C
Car.engine.capacity=2000
Car.engine.numCylinders=4
Car.engine.maxRpm=9000
Car.engine.manufacturerCode=123
Car.engine.fuel=Petrol
Car.engine.booster.BoostType=NITROUS
Car.engine.booster.horsePower=200
Car.fuelFigures Group Header : numInGroup=3
Car.fuelFigures.speed=30
Car.fuelFigures.mpg=35.900001525878906
Car.fuelFigures.usageDescription=Urban Cycle
Car.fuelFigures.speed=55
Car.fuelFigures.mpg=49
Car.fuelFigures.usageDescription=Combined Cycle
Car.fuelFigures.speed=75
Car.fuelFigures.mpg=40
Car.fuelFigures.usageDescription=Highway Cycle
Car.performanceFigures Group Header : numInGroup=2
Car.performanceFigures.octaneRating=95
Car.performanceFigures.acceleration Group Header : numInGroup=3
Car.performanceFigures.acceleration.mph=30
Car.performanceFigures.acceleration.seconds=4
Car.performanceFigures.acceleration.mph=60
Car.performanceFigures.acceleration.seconds=7.5
Car.performanceFigures.acceleration.mph=100
Car.performanceFigures.acceleration.seconds=12.199999809265137
Car.performanceFigures.octaneRating=99
Car.performanceFigures.acceleration Group Header : numInGroup=3
Car.performanceFigures.acceleration.mph=30
Car.performanceFigures.acceleration.seconds=3.799999952316284
Car.performanceFigures.acceleration.mph=60
Car.performanceFigures.acceleration.seconds=7.099999904632568
Car.performanceFigures.acceleration.mph=100
Car.performanceFigures.acceleration.seconds=11.800000190734863
Car.manufacturer=Honda
Car.model=Civic VTi
Car.activationCode=abcdef
Car.color=
`
	assert.EqualValues(expected, listener.out.String())
}

func encodeCar(car *code_generation_test.Car, buffer []byte) int {
	const vehicleCode = "abcdef"
	const manufacturerCode = "123"
	const activationCode = "abcdef"
	const manufacturer = "Honda"
	const model = "Civic VTi"

	car.WrapAndApplyHeader(buffer, 0, uint64(len(buffer))).
		SetSerialNumber(1234).
		SetModelYear(2013).
		SetAvailable(code_generation_test.BooleanType_T).
		SetCode(code_generation_test.Model_A).
		SetVehicleCode(vehicleCode)

	// change this to a loop
	for i := 0; i < car.SomeNumbersLength(); i++ {
		car.SetSomeNumbersIndex(uint64(i), int32(i))
	}

	car.Extras().
		Clear().
		SetCruiseControl(true).
		SetSportsPack(true).
		SetSunRoof(false)

	car.Engine().
		SetCapacity(2000).
		SetNumCylinders(4).
		SetManufacturerCode(manufacturerCode).
		Booster().
		SetBoostType(code_generation_test.BoostType_NITROUS).
		SetHorsePower(200)

	car.FuelFiguresCount(3).
		Next().SetSpeed(30).SetMpg(35.9).PutUsageDescription("Urban Cycle").
		Next().SetSpeed(55).SetMpg(49.0).PutUsageDescription("Combined Cycle").
		Next().SetSpeed(75).SetMpg(40.0).PutUsageDescription("Highway Cycle")

	figures := car.PerformanceFiguresCount(2)
	figures.Next().
		SetOctaneRating(95).
		AccelerationCount(3).
		Next().SetMph(30).SetSeconds(4.0).
		Next().SetMph(60).SetSeconds(7.5).
		Next().SetMph(100).SetSeconds(12.2)
	figures.Next().
		SetOctaneRating(99).
		AccelerationCount(3).
		Next().SetMph(30).SetSeconds(3.8).
		Next().SetMph(60).SetSeconds(7.1).
		Next().SetMph(100).SetSeconds(11.8)

	car.PutManufacturer(manufacturer).
		PutModel(model).
		PutActivationCode(activationCode).
		PutColor("")

	// var hdr code_generation_test.MessageHeader
	return int(car.EncodedLength())
}
