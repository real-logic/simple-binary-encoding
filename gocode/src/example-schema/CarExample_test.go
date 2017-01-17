package main

import (
	"baseline"
	"bytes"
	"encoding/binary"
	"testing"
)

func TestNoop(t *testing.T) {
}

func BenchmarkInstantiateCar(b *testing.B) {
	var car baseline.Car
	for i := 0; i < b.N; i++ {
		car = makeCar()
	}

	// Compiler insists that car is used
	car.Available = baseline.BooleanType.T
}

func BenchmarkEncodeStrict(b *testing.B) {
	car := makeCar()
	var buf = new(bytes.Buffer)

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		if err := car.Encode(buf, binary.LittleEndian, true); err != nil {
			b.Logf("Encoding Error", err)
			b.Fail()
		}
		buf.Reset()
	}

}

func BenchmarkEncodeLax(b *testing.B) {
	car := makeCar()
	var buf = new(bytes.Buffer)
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		if err := car.Encode(buf, binary.LittleEndian, false); err != nil {
			b.Logf("Encoding Error", err)
			b.Fail()
		}
		buf.Reset()
	}
}

func BenchmarkEncodeLaxNotest(b *testing.B) {
	car := makeCar()
	var buf = new(bytes.Buffer)
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		car.Encode(buf, binary.LittleEndian, false)
		buf.Reset()
	}
}

func BenchmarkDecodeStrict(b *testing.B) {
	car := makeCar()
	var buf = new(bytes.Buffer)
	for i := 0; i < b.N; i++ {
		if err := car.Encode(buf, binary.LittleEndian, true); err != nil {
			b.Logf("Encoding Error", err)
			b.Fail()
		}
	}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		if err := car.Decode(buf, binary.LittleEndian, car.SbeSchemaVersion(), car.SbeBlockLength(), true); err != nil {
			b.Logf("Decoding Error", err)
			b.Fail()
		}
	}
}

func BenchmarkDecodeLax(b *testing.B) {
	car := makeCar()
	var buf = new(bytes.Buffer)
	for i := 0; i < b.N; i++ {
		if err := car.Encode(buf, binary.LittleEndian, false); err != nil {
			b.Logf("Encoding Error", err)
			b.Fail()
		}
	}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		if err := car.Decode(buf, binary.LittleEndian, car.SbeSchemaVersion(), car.SbeBlockLength(), false); err != nil {
			b.Logf("Decoding Error", err)
			b.Fail()
		}
	}
}
