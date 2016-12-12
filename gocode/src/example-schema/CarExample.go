// golang example code for SBE's example-schema.xml

package main

import (
	"baseline"
	"bytes"
	"encoding/binary"
	"fmt"
	"io"
	"net"
	"os"
	"time"
)

func main() {

	fmt.Println("Example encode and decode")
	ExampleEncodeDecode()

	fmt.Println("Example decode using bytes.buffer")
	ExampleDecodeBuffer()

	fmt.Println("Example decode using io.Pipe")
	ExampleDecodePipe()

	fmt.Println("Example decode using socket")
	ExampleDecodeSocket()

	return

}

func ExampleEncodeDecode() bool {
	in := makeCar()

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		fmt.Println("Encoding Error", err)
		os.Exit(1)
	}

	var out baseline.Car = *new(baseline.Car)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		fmt.Println("Decoding Error", err)
		os.Exit(1)
	}

	if in.SerialNumber != out.SerialNumber {
		fmt.Println("in.SerialNumber != out.SerialNumber:\n", in.SerialNumber, out.SerialNumber)
		os.Exit(1)
	}
	if in.ModelYear != out.ModelYear {
		fmt.Println("in.ModelYear != out.ModelYear:\n", in.ModelYear, out.ModelYear)
		os.Exit(1)
	}
	if in.Available != out.Available {
		fmt.Println("in.Available != out.Available:\n", in.Available, out.Available)
		os.Exit(1)
	}
	if in.Code != out.Code {
		fmt.Println("in.Code != out.Code:\n", in.Code, out.Code)
		os.Exit(1)
	}
	if in.SomeNumbers != out.SomeNumbers {
		fmt.Println("in.SomeNumbers != out.SomeNumbers:\n", in.SomeNumbers, out.SomeNumbers)
		os.Exit(1)
	}
	if in.VehicleCode != out.VehicleCode {
		fmt.Println("in.VehicleCode != out.VehicleCode:\n", in.VehicleCode, out.VehicleCode)
		os.Exit(1)
	}
	if in.Extras != out.Extras {
		fmt.Println("in.Extras != out.Extras:\n", in.Extras, out.Extras)
		os.Exit(1)
	}

	// DiscountedModel is constant
	if baseline.Model.C != out.DiscountedModel {
		fmt.Println("in.DiscountedModel != out.DiscountedModel:\n", in.DiscountedModel, out.DiscountedModel)
		os.Exit(1)
	}

	// Engine has two constant values which should come back filled in
	if in.Engine == out.Engine {
		fmt.Println("in.Engine == out.Engine (and they should be different):\n", in.Engine, out.Engine)
		os.Exit(1)
	}

	// Engine has constant elements so We should have used our the
	// EngineInit() function to fill those in when we created the
	// object, and then they will correctly compare
	baseline.EngineInit(&in.Engine)
	if in.Engine != out.Engine {
		fmt.Println("in.Engine != out.Engine:\n", in.Engine, out.Engine)
		os.Exit(1)
	}

	return true
}

func ExampleDecodeBuffer() bool {
	buf := bytes.NewBuffer(data)
	var m baseline.MessageHeader
	if err := m.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		fmt.Println("Failed to decode message header", err)
		os.Exit(1)
	}

	// fmt.Println("\tbuffer is length:", buf.Len())
	var c baseline.Car
	if err := c.Decode(buf, binary.LittleEndian, m.Version, true); err != nil {
		fmt.Println("Failed to decode car", err)
		os.Exit(1)
	}
	return true
}

func ExampleDecodePipe() bool {
	var r,w = io.Pipe()

	go func() {
		defer w.Close()

		// By way of test, stream the bytes into the pipe 32 at a time
		msg := data[0:]
		for ;len(msg) > 0; {
			min := MinInt(len(msg), 64)
			// fmt.Println("writing: ", msg[0:min])
			n, err := w.Write(msg[0:min])
			if err != nil {
				fmt.Println("write error is", err)
				os.Exit(1)
			}
			if (n < 8) {
				fmt.Println("short write of", n, "bytes")
				os.Exit(1)
			}
			msg = msg[n:]
			time.Sleep(time.Second)
		}
	}()

	var m baseline.MessageHeader
	m.Decode(r, binary.LittleEndian, 0, true);

	var c baseline.Car
	if err := c.Decode(r, binary.LittleEndian, m.Version, true); err != nil {
		fmt.Println("Failed to decode car", err)
		os.Exit(1)
	}
	r.Close()
	return true
}


func ExampleDecodeSocket() bool {
	addr := "127.0.0.1:15678"
	writerDone := make(chan bool)
	readerDone := make(chan bool)

	// Reader
	go func() {
		// fmt.Println("resolve")
		tcpAddr, err := net.ResolveTCPAddr("tcp", addr)
		if err != nil {
			fmt.Println("Resolve failed", err.Error())
			os.Exit(1)
		}

		// fmt.Println("create listener")
		listener, err := net.ListenTCP("tcp", tcpAddr)
		if err != nil {
			fmt.Println("Listen failed", err.Error())
			os.Exit(1)
		}
		defer listener.Close()

		// fmt.Println("listen")
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println("Accept failed", err.Error())
			os.Exit(1)
		}
		defer conn.Close()


		// fmt.Println("reading messageheader")
		var m baseline.MessageHeader
		m.Decode(conn, binary.LittleEndian, 0, true);

		// fmt.Println("reading car")
		var c baseline.Car
		if err := c.Decode(conn, binary.LittleEndian, m.Version, true); err != nil {
			fmt.Println("Failed to decode car", err)
			os.Exit(1)
		}

		// fmt.Printf("%+v\n", c)
		readerDone <- true
	}()

	// Let that get started
	time.Sleep(time.Second)

	// Writer
	go func() {
		//fmt.Println("dial")
		conn, err := net.Dial("tcp", addr)
		if err != nil {
			fmt.Println("Dial failed", err.Error())
			os.Exit(1)
		}
		defer conn.Close()

		// By way of test, stream the bytes into the pipe 32 at a time
		msg := data[0:]
		for ;len(msg) > 0; {
			min := MinInt(len(msg), 64)
			// fmt.Println("writing: ", msg[0:min])
			n, err := conn.Write(msg[0:min])
			if err != nil {
				fmt.Println("write error is", err)
				os.Exit(1)
			}
			if (n < 8) {
				fmt.Println("short write of", n, "bytes")
				os.Exit(1)
			}
			// fmt.Println("wrote", n, "bytes")
			msg = msg[n:]
			time.Sleep(time.Second)
		}
		<- readerDone
		writerDone <- true
	}()

	<- writerDone

	return true
}


// Helper to make a Car object as per the Java example
func makeCar() baseline.Car {
	var vehicleCode [6]byte
	copy(vehicleCode[:], "abcdef")

	var manufacturerCode [3]byte
	copy(manufacturerCode[:], "123")

	var optionalExtras [8]bool
	optionalExtras[baseline.OptionalExtrasChoice.CruiseControl] = true
	optionalExtras[baseline.OptionalExtrasChoice.SportsPack] = true

	var engine baseline.Engine
	engine = baseline.Engine{2000, 4, 0, manufacturerCode, [6]byte{}, baseline.EngineBooster{baseline.BoostType.NITROUS, 200}}

	brand := []uint8("Honda")
	model := []uint8("Civic VTi")
	activationCode := []uint8("deadbeef")

	var fuel []baseline.CarFuelFigures
	fuel = append(fuel, baseline.CarFuelFigures{30, 35.9, []uint8("Urban Cycle")})
	fuel = append(fuel, baseline.CarFuelFigures{55, 49.0, []uint8("Combined Cycle")})
	fuel = append(fuel, baseline.CarFuelFigures{75, 40.0, []uint8("Highway Cycle")})

	var acc1 []baseline.CarPerformanceFiguresAcceleration
	acc1 = append(acc1, baseline.CarPerformanceFiguresAcceleration{30, 3.8})
	acc1 = append(acc1, baseline.CarPerformanceFiguresAcceleration{60, 7.5})
	acc1 = append(acc1, baseline.CarPerformanceFiguresAcceleration{100, 12.2})

	var acc2 []baseline.CarPerformanceFiguresAcceleration
	acc2 = append(acc2, baseline.CarPerformanceFiguresAcceleration{30, 3.8})
	acc2 = append(acc2, baseline.CarPerformanceFiguresAcceleration{60, 7.5})
	acc2 = append(acc2, baseline.CarPerformanceFiguresAcceleration{100, 12.2})

	var pf []baseline.CarPerformanceFigures
	pf = append(pf, baseline.CarPerformanceFigures{95, acc1})
	pf = append(pf, baseline.CarPerformanceFigures{99, acc2})


	car := baseline.Car{1234, 2013, baseline.BooleanType.T, baseline.Model.A, [5]uint32{0, 1, 2, 3, 4}, vehicleCode, optionalExtras, baseline.Model.A, engine, fuel, pf, brand, model, activationCode}

	return car
}

// MaxInt returns the larger of two ints.
func MaxInt(a, b int) int {
	if a > b {
		return a
	}
	return b
}
// MinInt returns the larger of two ints.
func MinInt(a, b int) int {
	if a < b {
		return a
	}
	return b
}

// The byte array is from the java example for interop test
var data []byte = []byte{47, 0, 1, 0, 1, 0, 0, 0, 210, 4, 0, 0, 0, 0, 0, 0, 221, 7, 1, 65, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 97, 98, 99, 100, 101, 102, 6, 208, 7, 4, 49, 50, 51, 78, 200, 6, 0, 3, 0, 30, 0, 154, 153, 15, 66, 11, 0, 0, 0, 85, 114, 98, 97, 110, 32, 67, 121, 99, 108, 101, 55, 0, 0, 0, 68, 66, 14, 0, 0, 0, 67, 111, 109, 98, 105, 110, 101, 100, 32, 67, 121, 99, 108, 101, 75, 0, 0, 0, 32, 66, 13, 0, 0, 0, 72, 105, 103, 104, 119, 97, 121, 32, 67, 121, 99, 108, 101, 1, 0, 2, 0, 95, 6, 0, 3, 0, 30, 0, 0, 0, 128, 64, 60, 0, 0, 0, 240, 64, 100, 0, 51, 51, 67, 65, 99, 6, 0, 3, 0, 30, 0, 51, 51, 115, 64, 60, 0, 51, 51, 227, 64, 100, 0, 205, 204, 60, 65, 5, 0, 0, 0, 72, 111, 110, 100, 97, 9, 0, 0, 0, 67, 105, 118, 105, 99, 32, 86, 84, 105, 6, 0, 0, 0, 97, 98, 99, 100, 101, 102}
