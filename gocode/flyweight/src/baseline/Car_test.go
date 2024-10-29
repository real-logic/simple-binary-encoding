package baseline

import (
	_ "fmt"
	"testing"
)

func TestEncodeDecodeCar(t *testing.T) {
	var data [256]byte
	var in Car
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.SetSerialNumber(1234).
		SetModelYear(2000).
		SetAvailable(BooleanType_T).
		SetCode(Model_A).
		PutSomeNumbersValues(0, 1, 2, 3).
		SetVehicleCode("abcdef")

	in.Extras().
		SetCruiseControl(true).
		SetSportsPack(true)

	in.Engine().
		SetCapacity(2000).
		SetNumCylinders(4).
		SetManufacturerCode("123").
		SetEfficiency(42).
		SetBoosterEnabled(BooleanType_T).
		Booster().
		SetBoostType(BoostType_NITROUS).
		SetHorsePower(200)

	in.FuelFiguresCount(3).Next().
		SetSpeed(30).
		SetMpg(35.9).
		PutUsageDescription("Urban Cycle").Next().
		SetSpeed(55).
		SetMpg(49.0).
		PutUsageDescription("Combined Cycle").Next().
		SetSpeed(75).
		SetMpg(40.0).
		PutUsageDescription("Highway Cycle")

	performanceFigures := in.PerformanceFiguresCount(2)
	performanceFigures.SetOctaneRating(95).Next().
		AccelerationCount(3).
		SetMph(30).
		SetSeconds(3.8).Next().
		SetMph(60).
		SetSeconds(7.5).Next().
		SetMph(100).
		SetSeconds(12.2)
	performanceFigures.Next().
		SetOctaneRating(99).
		AccelerationCount(3).
		SetMph(30).
		SetSeconds(3.8).Next().
		SetMph(60).
		SetSeconds(7.5).Next().
		SetMph(100).
		SetSeconds(12.2)

	in.PutManufacturer("123").
		PutModel("Civic VTi").
		PutActivationCode("deadbeef")

	var out Car
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(in.SbeBlockLength()),
		uint64(len(data)),
	)

	if in.SerialNumber() != out.SerialNumber() {
		t.Log("in.SerialNumber != out.SerialNumber:\n", in.SerialNumber(), out.SerialNumber())
		t.Fail()
	}
	if in.ModelYear() != out.ModelYear() {
		t.Log("in.ModelYear != out.ModelYear:\n", in.ModelYear(), out.ModelYear())
		t.Fail()
	}
	if in.Available() != out.Available() {
		t.Log("in.Available != out.Available:\n", in.Available(), out.Available())
		t.Fail()
	}
	if in.Code() != out.Code() {
		t.Log("in.Code != out.Code:\n", in.Code(), out.Code())
		t.Fail()
	}
	for i, l := uint64(0), uint64(in.SomeNumbersLength()); i < l; i++ {
		if in.SomeNumbersIndex(i) != out.SomeNumbersIndex(i) {
			t.Log("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersIndex(i), out.SomeNumbersIndex(i))
			t.Fail()
		}
	}
	for i, l := uint64(0), uint64(in.VehicleCodeLength()); i < l; i++ {
		if in.VehicleCodeIndex(i) != out.VehicleCodeIndex(i) {
			t.Log("in.VehicleCode != out.VehicleCode:\n", in.VehicleCodeIndex(i), out.VehicleCodeIndex(i))
			t.Fail()
		}
	}
	if in.Extras().String() != out.Extras().String() {
		t.Log("in.Extras != out.Extras:\n", in.Extras(), out.Extras())
		t.Fail()
	}

	// DiscountedModel is constant
	if Model_C != out.DiscountedModel() {
		t.Log("in.DiscountedModel != out.DiscountedModel:\n", in.DiscountedModel(), out.DiscountedModel())
		t.Fail()
	}

	if in.Engine().String() != out.Engine().String() {
		t.Log("in.Engine != out.Engine:\n", in.Engine().String(), out.Engine().String())
		t.Fail()
	}

	return

}

func TestDecodeJavaBuffer(t *testing.T) {
	// See ~gocode/src/example-schema/CarExample.go for how this is generated
	data := []byte{45, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 35, 1, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102}

	var c Car
	c.WrapAndApplyHeader(data, 0, uint64(len(data)))

	expected := `{"Name": "Car", "sbeTemplateId": 1, "serialNumber": "1234", "modelYear": "2013", "available": "T", "code": "A", "someNumbers": [1,2,3,4], "vehicleCode": "abcdef", "extras": ["SportsPack","CruiseControl"], "discountedModel": "C", "engine": {"capacity": "2000", "numCylinders": "4", "manufacturerCode": "123", "efficiency": "35", "boosterEnabled": "T", "booster": {"BoostType": "NITROUS", "horsePower": "200"}}, "fuelFigures": [{"speed": "30", "mpg": "35.9", "usageDescription": "Urban Cycle"}, {"speed": "55", "mpg": "49", "usageDescription": "Combined Cycle"}, {"speed": "75", "mpg": "40", "usageDescription": "Highway Cycle"}], "performanceFigures": [{"octaneRating": "95", "acceleration": [{"mph": "30", "seconds": "4"}, {"mph": "60", "seconds": "7.5"}, {"mph": "100", "seconds": "12.2"}]}, {"octaneRating": "99", "acceleration": [{"mph": "30", "seconds": "3.8"}, {"mph": "60", "seconds": "7.1"}, {"mph": "100", "seconds": "11.8"}]}], "manufacturer": "Honda", "model": "Civic VTi", "activationCode": "abcdef"}`
	if actual := c.String(); actual != expected {
		t.Logf("Failed to decode car, expected %s, got %s", expected, actual)
		t.Fail()
	}
	return
}
