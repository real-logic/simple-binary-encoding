package sbe_tests

import (
	"bytes"
	"testing"
)

func TestEncodeDecodeSet(t *testing.T) {

	m := NewSbeGoMarshaller()
	// var e SET = Value1;
	var in SET
	in[SETChoice.Bit16] = true

	var buf = new(bytes.Buffer)
	if err := in.Encode(m, buf); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out SET = *new(SET)
	if err := out.Decode(m, buf, 0); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out:\n", in, out)
		t.Fail()
	}

	return

}
