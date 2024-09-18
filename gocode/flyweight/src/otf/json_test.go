package otf

import (
	"github.com/real-logic/simple-binary-encoding/json"
	"github.com/real-logic/simple-binary-encoding/otf"
	"github.com/real-logic/simple-binary-encoding/otf/test/code_generation_test"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestJson(t *testing.T) {
	assert := assert.New(t)

	ir := otf.NewIrDecoder()
	if ir.DecodeFile(SchemaFilename) < 0 {
		t.Errorf("Error decoding %s", SchemaFilename)
		t.Fail()
	}
	printer, err := json.NewJsonPrinter(ir)
	assert.NoErrorf(err, "Error creating JsonPrinter: %v", err)
	assert.NotNil(printer)

	var car code_generation_test.Car
	buffer := make([]byte, MsgBufferCapacity)
	encodeCar(&car, buffer)

	headerDecoder, _ := otf.NewOtfHeaderDecoder(ir.Header())
	bufferOffset := 0
	schemaId, _ := headerDecoder.SchemaId(buffer[bufferOffset:])

	if uint16(schemaId) != car.SbeSchemaId() {
		t.Errorf("Invalid schema id: %d", schemaId)
	}

	str, err := printer.PrintJson(buffer)
	assert.NoErrorf(err, "Error printing JSON: %v", err)
	expected := `{
    "serialNumber": 1234,
    "modelYear": 2013,
    "available": "T",
    "code": "A",
    "someNumbers": [0, 1, 2, 3, 4],
    "vehicleCode": ["a", "b", "c", "d", "e", "f"],
    "extras": { "sunRoof": false, "sportsPack": false, "cruiseControl": false },
    "discountedModel": "C",
    "Engine": 
    {
        "capacity": 2000,
        "numCylinders": 4,
        "maxRpm": "9000",
        "manufacturerCode": ["1", "2", "3"],
        "fuel": "Petrol",
        "booster": 
        {
            "BoostType": "NITROUS",
            "horsePower": 200
        }
    },
    "fuelFigures": [
    {
        "speed": 30,
        "mpg": 35.900001525878906,
        "usageDescription": "Urban Cycle"
    },
    {
        "speed": 55,
        "mpg": 49,
        "usageDescription": "Combined Cycle"
    },
    {
        "speed": 75,
        "mpg": 40,
        "usageDescription": "Highway Cycle"
    }],
    "performanceFigures": [
    {
        "octaneRating": 95,
        "acceleration": [
        {
            "mph": 30,
            "seconds": 4
        },
        {
            "mph": 60,
            "seconds": 7.5
        },
        {
            "mph": 100,
            "seconds": 12.199999809265137
        }]
    },
    {
        "octaneRating": 99,
        "acceleration": [
        {
            "mph": 30,
            "seconds": 3.799999952316284
        },
        {
            "mph": 60,
            "seconds": 7.099999904632568
        },
        {
            "mph": 100,
            "seconds": 11.800000190734863
        }]
    }],
    "manufacturer": "Honda",
    "model": "Civic VTi",
    "activationCode": "abcdef",
    "color": ""
}`
	assert.EqualValues(expected, str, "Message printer")
}
