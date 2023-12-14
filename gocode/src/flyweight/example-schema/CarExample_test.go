package main

import (
	"fmt"
	"github.com/real-logic/simple-binary-encoding/src/baseline"
	"github.com/real-logic/simple-binary-encoding/src/extension"
	"io"
	"testing"
)

func TestNoop(t *testing.T) {
}

func BenchmarkInstantiateCar(b *testing.B) {
	var data [256]byte
	for i := 0; i < b.N; i++ {
		var hdr baseline.MessageHeader
		var car baseline.Car
		makeCar(&hdr, &car, data[:], 0)
	}
}

func BenchmarkDecodeStrict(b *testing.B) {
	var data [256]byte
	var hdr baseline.MessageHeader
	var car baseline.Car
	makeCar(&hdr, &car, data[:], 0)

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		var out baseline.Car
		out.WrapForDecode(
			data[:],
			0,
			uint64(car.SbeBlockLength()),
			uint64(car.SbeSchemaVersion()),
			uint64(len(data)),
		)
	}
}

func BenchmarkPipe(b *testing.B) {
	var r, w = io.Pipe()
	data := []byte{49, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 35, 1, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102}
	writerReady := make(chan bool)

	go func() {
		defer w.Close()
		writerReady <- true
		// By way of test, stream the bytes into the pipe a
		// chunk at a time
		for looping := true; looping; {
			if _, err := w.Write(data); err != nil {
				looping = false
			}
		}
	}()

	<-writerReady
	b.ResetTimer()

	var buf [1024]byte
	for i := 0; i < b.N; i++ {
		var hdr baseline.MessageHeader
		hdrSize := int(hdr.EncodedLength())
		_, err := io.ReadAtLeast(r, buf[:], hdrSize)
		if err != nil {
			b.Log("Failed to read", err)
			b.Fail()
		}
		hdr.Wrap(buf[:], 0, 0, uint64(hdrSize))

		n, err := io.ReadAtLeast(r, buf[:], hdrSize)
		if err != nil {
			b.Log("Failed to read", err)
			b.Fail()
		}

		var out baseline.Car
		out.WrapForDecode(
			buf[hdrSize:],
			0,
			uint64(hdr.BlockLength()),
			hdr.ActingVersion(),
			uint64(n-hdrSize),
		)
	}
	r.Close()
}

func BenchmarkPipeBufio(b *testing.B) {
	var r, w = io.Pipe()
	data := []byte{49, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 35, 1, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102}

	writerReady := make(chan bool)

	go func() {
		defer w.Close()
		writerReady <- true
		// By way of test, stream the bytes into the pipe a
		// chunk at a time
		for looping := true; looping; {
			if _, err := w.Write(data); err != nil {
				looping = false
			}
		}
	}()

	<-writerReady
	b.ResetTimer()

	var buf [1024]byte
	for i := 0; i < b.N; i++ {
		var hdr baseline.MessageHeader
		hdrSize := int(hdr.EncodedLength())
		_, err := io.ReadAtLeast(r, buf[:], hdrSize)
		if err != nil {
			b.Log("Failed to read", err)
			b.Fail()
		}
		hdr.Wrap(buf[:], 0, 0, uint64(hdrSize))

		n, err := io.ReadAtLeast(r, buf[:], hdrSize)
		if err != nil {
			b.Log("Failed to read", err)
			b.Fail()
		}

		var out baseline.Car
		out.WrapForDecode(
			buf[:],
			uint64(hdrSize),
			uint64(hdr.BlockLength()),
			hdr.ActingVersion(),
			uint64(n-hdrSize),
		)
	}
	r.Close()
}

// String Preallocations which matches what the Java and C++ benchmarks do
var vehicleCode = [6]byte{'a', 'b', 'c', 'd', 'e', 'f'}
var manufacturerCode = [3]byte{'1', '2', '3'}
var manufacturer = []uint8("Honda")
var model = []uint8("Civic VTi")
var activationCode = []uint8("abcdef")

// Both Java and C++ benchmarks ignore CarFuelFigures.UsageDescription
var urban = []uint8("Urban Cycle")
var combined = []uint8("Combined Cycle")
var highway = []uint8("Highway Cycle")

func ExampleEncodeDecode() {
	var data [256]byte
	var hdr baseline.MessageHeader
	var in baseline.Car

	makeCar(&hdr, &in, data[:], 0)
	in.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	var out baseline.Car
	out.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	if in.SerialNumber() != out.SerialNumber() {
		fmt.Println("in.SerialNumber() != out.SerialNumber():\n", in.SerialNumber(), out.SerialNumber())
		return
	}
	if in.ModelYear() != out.ModelYear() {
		fmt.Println("in.ModelYear() != out.ModelYear():\n", in.ModelYear(), out.ModelYear())
		return
	}
	if in.Available() != out.Available() {
		fmt.Println("in.Available() != out.Available():\n", in.Available(), out.Available())
		return
	}
	if in.Code() != out.Code() {
		fmt.Println("in.Code() != out.Code():\n", in.Code(), out.Code())
		return
	}

	if in.SomeNumbersLength() != out.SomeNumbersLength() {
		fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersLength(), out.SomeNumbersLength())
		return
	}
	for i := uint64(0); i < uint64(in.SomeNumbersLength()); i++ {
		if in.SomeNumbersIndex(i) != out.SomeNumbersIndex(i) {
			fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersIndex(i), out.SomeNumbersIndex(i))
			return
		}
	}

	if in.GetVehicleCodeAsString() != out.GetVehicleCodeAsString() {
		fmt.Println("in.VehicleCode != out.VehicleCode:\n", in.GetVehicleCodeAsString(), out.GetVehicleCodeAsString())
		return
	}
	if in.Extras().String() != out.Extras().String() {
		fmt.Println("in.Extras != out.Extras:\n", in.Extras().String(), out.Extras().String())
		return
	}
	if baseline.Model_C != out.DiscountedModel() {
		fmt.Println("in.DiscountedModel != out.DiscountedModel:\n", in.DiscountedModel(), out.DiscountedModel())
		return
	}
	if in.Engine().String() != out.Engine().String() {
		fmt.Println("in.Engine != out.Engine:\n", in.Engine().String(), "\n", out.Engine().String())
		return
	}
	// Output:
}

func ExampleCarToExtension() {
	var data [256]byte
	var hdr baseline.MessageHeader
	var in baseline.Car

	makeCar(&hdr, &in, data[:], 0)

	in.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	var out extension.Car
	out.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	if in.SerialNumber() != out.SerialNumber() {
		fmt.Println("in.SerialNumber() != out.SerialNumber():\n", in.SerialNumber(), out.SerialNumber())
		return
	}
	if in.ModelYear() != out.ModelYear() {
		fmt.Println("in.ModelYear() != out.ModelYear():\n", in.ModelYear(), out.ModelYear())
		return
	}

	// Note casts so we can compare
	if in.Available() != baseline.BooleanType(out.Available()) {
		fmt.Println("in.Available != out.Available:\n", in.Available(), out.Available())
		return
	}
	if in.Code() != baseline.Model(out.Code()) {
		fmt.Println("in.Code != out.Code:\n", in.Code(), out.Code())
		return
	}

	if in.SomeNumbersLength() != out.SomeNumbersLength() {
		fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersLength(), out.SomeNumbersLength())
		return
	}
	for i := uint64(0); i < uint64(in.SomeNumbersLength()); i++ {
		if in.SomeNumbersIndex(i) != out.SomeNumbersIndex(i) {
			fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersIndex(i), out.SomeNumbersIndex(i))
			return
		}
	}

	if in.GetVehicleCodeAsString() != out.GetVehicleCodeAsString() {
		fmt.Println("in.VehicleCode != out.VehicleCode:\n", in.GetVehicleCodeAsString(), out.GetVehicleCodeAsString())
		return
	}
	if in.Extras().String() != out.Extras().String() {
		fmt.Println("in.Extras != out.Extras:\n", in.Extras().String(), out.Extras().String())
		return
	}
	if extension.Model_C != out.DiscountedModel() {
		fmt.Println("in.DiscountedModel != out.DiscountedModel:\n", in.DiscountedModel(), out.DiscountedModel())
		return
	}
	if in.Engine().String() != out.Engine().String() {
		fmt.Println("in.Engine != out.Engine:\n", in.Engine().String(), "\n", out.Engine().String())
		return
	}

	// Engine has two constant values which should come back filled in
	if in.Engine().MaxRpm() != out.Engine().MaxRpm() {
		fmt.Println("in.Engine.MaxRpm != out.Engine/MaxRpm:\n",
			in.Engine().MaxRpm(), out.Engine().MaxRpm())
		return
	}

	inFuelFigures, outFuelFigures := in.FuelFigures(), out.FuelFigures()
	if inFuelFigures.Count() != outFuelFigures.Count() {
		fmt.Println("in.FuelFiguresCount() != out.FuelFiguresCount():\n",
			inFuelFigures.Count(), outFuelFigures.Count())
		return
	}
	for i := uint64(0); i < inFuelFigures.Count(); i++ {
		inFuelFigures, outFuelFigures = inFuelFigures.Next(), outFuelFigures.Next()
		if e, a := inFuelFigures.String(), outFuelFigures.String(); e != a {
			fmt.Println("in.FuelFigures != out.FuelFigures:\n", e, a)
			return
		}
	}

	inPerformanceFigures, outPerformanceFigures := in.PerformanceFigures(), out.PerformanceFigures()
	if inPerformanceFigures.Count() != outPerformanceFigures.Count() {
		fmt.Println("in.PerformanceFiguresCount() != out.PerformanceFiguresCount():\n",
			inPerformanceFigures.Count(), outPerformanceFigures.Count())
		return
	}
	for i := uint64(0); i < inPerformanceFigures.Count(); i++ {
		inPerformanceFigures, outPerformanceFigures = inPerformanceFigures.Next(), outPerformanceFigures.Next()
		if e, a := inPerformanceFigures.String(), outPerformanceFigures.String(); e != a {
			fmt.Println("in.PerformanceFigures != out.PerformanceFigures:\n", e, a)
			return
		}
	}

	if e, a := in.Manufacturer(), out.Manufacturer(); e != a {
		fmt.Println("in.Manufacturer != out.Manufacturer:\n", e, a)
		return
	}

	if e, a := in.Model(), out.Model(); e != a {
		fmt.Println("in.Model != out.Model:\n", e, a)
		return
	}

	if e, a := in.ActivationCode(), out.ActivationCode(); e != a {
		fmt.Println("in.ActivationCode != out.ActivationCode:\n", e, a)
		return
	}

	// Cupholder is not in example-schema and was introduced in
	// extension-schema, so it should be NullValue
	if out.CupHolderCount() != out.CupHolderCountNullValue() {
		fmt.Println("out.cupholderCount not successfully nulled:\n", out.CupHolderCount())
		return
	}
	// Output:
}

func ExampleExtensionToCar() {
	var data [256]byte
	var hdr extension.MessageHeader
	var in extension.Car

	makeExtension(&hdr, &in, data[:], 0)

	in.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	var out baseline.Car
	out.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data))-hdr.EncodedLength(),
	)

	if in.SerialNumber() != out.SerialNumber() {
		fmt.Println("in.SerialNumber() != out.SerialNumber():\n", in.SerialNumber(), out.SerialNumber())
		return
	}
	if in.ModelYear() != out.ModelYear() {
		fmt.Println("in.ModelYear() != out.ModelYear():\n", in.ModelYear(), out.ModelYear())
		return
	}

	// Note casts so we can compare
	if in.Available() != extension.BooleanType(out.Available()) {
		fmt.Println("in.Available != out.Available:\n", in.Available(), out.Available())
		return
	}
	if in.Code() != extension.Model(out.Code()) {
		fmt.Println("in.Code != out.Code:\n", in.Code(), out.Code())
		return
	}

	if in.SomeNumbersLength() != out.SomeNumbersLength() {
		fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersLength(), out.SomeNumbersLength())
		return
	}
	for i := uint64(0); i < uint64(in.SomeNumbersLength()); i++ {
		if in.SomeNumbersIndex(i) != out.SomeNumbersIndex(i) {
			fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbersIndex(i), out.SomeNumbersIndex(i))
			return
		}
	}

	if in.GetVehicleCodeAsString() != out.GetVehicleCodeAsString() {
		fmt.Println("in.VehicleCode != out.VehicleCode:\n", in.GetVehicleCodeAsString(), out.GetVehicleCodeAsString())
		return
	}
	if in.Extras().String() != out.Extras().String() {
		fmt.Println("in.Extras != out.Extras:\n", in.Extras().String(), out.Extras().String())
		return
	}
	if baseline.Model_C != out.DiscountedModel() {
		fmt.Println("in.DiscountedModel != out.DiscountedModel:\n", in.DiscountedModel(), out.DiscountedModel())
		return
	}
	if in.Engine().String() != out.Engine().String() {
		fmt.Println("in.Engine != out.Engine:\n", in.Engine().String(), "\n", out.Engine().String())
		return
	}

	inFuelFigures, outFuelFigures := in.FuelFigures(), out.FuelFigures()
	if inFuelFigures.Count() != outFuelFigures.Count() {
		fmt.Println("in.FuelFiguresCount() != out.FuelFiguresCount():\n",
			inFuelFigures.Count(), outFuelFigures.Count())
		return
	}
	for i := uint64(0); i < inFuelFigures.Count(); i++ {
		inFuelFigures, outFuelFigures = inFuelFigures.Next(), outFuelFigures.Next()
		if e, a := inFuelFigures.String(), outFuelFigures.String(); e != a {
			fmt.Println("in.FuelFigures != out.FuelFigures:\n", e, a)
			return
		}
	}

	inPerformanceFigures, outPerformanceFigures := in.PerformanceFigures(), out.PerformanceFigures()
	if inPerformanceFigures.Count() != outPerformanceFigures.Count() {
		fmt.Println("in.PerformanceFiguresCount() != out.PerformanceFiguresCount():\n",
			inPerformanceFigures.Count(), outPerformanceFigures.Count())
		return
	}
	for i := uint64(0); i < inPerformanceFigures.Count(); i++ {
		inPerformanceFigures, outPerformanceFigures = inPerformanceFigures.Next(), outPerformanceFigures.Next()
		if e, a := inPerformanceFigures.String(), outPerformanceFigures.String(); e != a {
			fmt.Println("in.PerformanceFigures != out.PerformanceFigures:\n", e, a)
			return
		}
	}

	if e, a := in.ActivationCode(), out.ActivationCode(); e != a {
		fmt.Println("in.ActivationCode != out.ActivationCode:\n", e, a)
		return
	}
	// Output:
}

// Helper to make a Car object as per the Java example
func makeCar(messageHeader *baseline.MessageHeader, car *baseline.Car, buffer []byte, bufferIndex uint64) {
	messageHeader.Wrap(buffer, bufferIndex, uint64(messageHeader.SbeSchemaVersion()), uint64(len(buffer)))

	car.WrapAndApplyHeader(buffer, bufferIndex, uint64(len(buffer))-uint64(messageHeader.BlockLength())).
		SetSerialNumber(1234).
		SetModelYear(2013).
		SetAvailable(baseline.BooleanType_T).
		SetCode(baseline.Model_A).
		PutVehicleCode(vehicleCode[:])

	size := car.SomeNumbersLength()
	for i := 0; i < size; i++ {
		car.SetSomeNumbersIndex(uint64(i), uint32(i))
	}

	car.Extras().Clear().
		SetSportsPack(true).
		SetSunRoof(true)

	car.Engine().
		SetCapacity(2000).
		SetNumCylinders(4).
		SetManufacturerCode(string(manufacturerCode[:])).
		SetEfficiency(35).
		SetBoosterEnabled(baseline.BooleanType_T).
		Booster().
		SetBoostType(baseline.BoostType_NITROUS).
		SetHorsePower(200)

	car.FuelFiguresCount(3).Next().
		SetSpeed(30).SetMpg(35.9).PutUsageDescription(string(urban[:])).Next().
		SetSpeed(55).SetMpg(49.0).PutUsageDescription(string(combined[:])).Next().
		SetSpeed(75).SetMpg(40.0).PutUsageDescription(string(highway[:]))

	perfFigures := car.PerformanceFiguresCount(2)
	perfFigures.Next().
		SetOctaneRating(95).
		AccelerationCount(3).Next().
		SetMph(30).SetSeconds(4.0).Next().
		SetMph(60).SetSeconds(7.5).Next().
		SetMph(100).SetSeconds(12.2)
	perfFigures.Next().
		SetOctaneRating(99).
		AccelerationCount(3).Next().
		SetMph(30).SetSeconds(3.8).Next().
		SetMph(60).SetSeconds(7.1).Next().
		SetMph(100).SetSeconds(11.8)

	car.PutManufacturer(string(manufacturer))
	car.PutModel(string(model))
	car.PutActivationCode(string(activationCode))
}

func makeExtension(messageHeader *extension.MessageHeader, car *extension.Car, buffer []byte, bufferIndex uint64) {
	// Helper to make an Extension (car with cupholder) object
	messageHeader.Wrap(buffer, bufferIndex, uint64(messageHeader.SbeSchemaVersion()), uint64(len(buffer)))

	car.WrapAndApplyHeader(buffer, bufferIndex, uint64(len(buffer))-uint64(messageHeader.BlockLength())).
		SetSerialNumber(1234).
		SetModelYear(2013).
		SetAvailable(extension.BooleanType_T).
		SetCode(extension.Model_A).
		PutVehicleCode(vehicleCode[:]).
		SetUuidIndex(0, 119).
		SetUuidIndex(1, 120).
		SetCupHolderCount(121)

	size := car.SomeNumbersLength()
	for i := 0; i < size; i++ {
		car.SetSomeNumbersIndex(uint64(i), uint32(i))
	}

	car.Extras().Clear().
		SetSportsPack(true).
		SetSunRoof(true)

	car.Engine().
		SetCapacity(2000).
		SetNumCylinders(4).
		SetManufacturerCode(string(manufacturerCode[:])).
		SetEfficiency(35).
		SetBoosterEnabled(extension.BooleanType_T).
		Booster().
		SetBoostType(extension.BoostType_NITROUS).
		SetHorsePower(200)

	car.FuelFiguresCount(3).Next().
		SetSpeed(30).SetMpg(35.9).PutUsageDescription(string(urban[:])).Next().
		SetSpeed(55).SetMpg(49.0).PutUsageDescription(string(combined[:])).Next().
		SetSpeed(75).SetMpg(40.0).PutUsageDescription(string(highway[:]))

	perfFigures := car.PerformanceFiguresCount(2)
	perfFigures.Next().
		SetOctaneRating(95).
		AccelerationCount(3).Next().
		SetMph(30).SetSeconds(4.0).Next().
		SetMph(60).SetSeconds(7.5).Next().
		SetMph(100).SetSeconds(12.2)
	perfFigures.Next().
		SetOctaneRating(99).
		AccelerationCount(3).Next().
		SetMph(30).SetSeconds(3.8).Next().
		SetMph(60).SetSeconds(7.1).Next().
		SetMph(100).SetSeconds(11.8)

	car.PutManufacturer(string(manufacturer))
	car.PutModel(string(model))
	car.PutActivationCode(string(activationCode))
}

// The byte array can be made at ~rust/car_example/car_example_data.sbe by running gradlew generateCarExampleDataFile
// This can then be decoded using od -tu1
var data = []byte{45, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 35, 1, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102}
