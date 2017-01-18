package sbe_tests

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecodeSet(t *testing.T) {

	// var e SET = Value1;
	var in SET
	in[SETChoice.Bit16] = true

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out SET = *new(SET)
	if err := out.Decode(buf, binary.LittleEndian, 0); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out:\n", in, out)
		t.Fail()
	}

	return

}
