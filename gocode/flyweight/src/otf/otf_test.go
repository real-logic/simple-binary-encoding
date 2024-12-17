package otf

import (
	"github.com/real-logic/simple-binary-encoding/otf"
	"github.com/real-logic/simple-binary-encoding/otf/test/code_generation_test"
	"github.com/stretchr/testify/assert"
	"testing"
)

//go:generate java --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dsbe.output.dir=. -Dsbe.target.language=golang -Dsbe.go.generate.generate.flyweights=true -Dsbe.generate.ir=true -jar ../../../${SBE_JAR} ../../../../sbe-tool/src/test/resources/code-generation-schema.xml

const SchemaFilename = "code-generation-schema.sbeir"
const MsgBufferCapacity = 4 * 1024

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
