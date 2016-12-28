package sbe_tests

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"reflect"
	"testing"
)

func TestEncodeDecodeEnum(t *testing.T) {

	// var e ENUMEnum = Value1;
	var in ENUMEnum = ENUM.Value10
	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out ENUMEnum = *new(ENUMEnum)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out:\n", in, out)
		t.Fail()
	}

	return

	// xmain()
}

func xmain() {
	// var e ENUMEnum = ENUME.Value1;
	var e ENUMEnum = 9
	var buf = new(bytes.Buffer)
	if err := e.Encode(buf, binary.LittleEndian); err != nil {
		fmt.Println("Encoding Error", err)
		return
	}

	var out ENUMEnum = *new(ENUMEnum)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		fmt.Println("Decoding Error", err)
		return
	}

	fmt.Println(e, out, reflect.TypeOf(out))
	return

}
