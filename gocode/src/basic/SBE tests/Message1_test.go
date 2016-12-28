package sbe_tests

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecodeMessage1(t *testing.T) {

	var in Message1
	copy(in.EDTField[:], "abcdefghijklmnopqrst")
	in.Header = MessageHeader{in.SbeBlockLength(), in.SbeTemplateId(), in.SbeSchemaId(), in.SbeSchemaVersion()}
	in.ENUMField = ENUM.Value10

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out Message1 = *new(Message1)
	if err := out.Decode(buf, binary.LittleEndian, in.SbeSchemaVersion(), true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out:\n", in, out)
		t.Fail()
	}

	// Now let's see that if we pass in an unknown enum value
	// we get a Nullvalue if strict and the unknown value if lax.
	in.ENUMField = 77
	in.Encode(buf, binary.LittleEndian)
	if err := out.Decode(buf, binary.LittleEndian, in.SbeSchemaVersion(), true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}
	if out.ENUMField != ENUM.NullValue {
		t.Logf("out.ENUMField != ENUM.NullValue\n", in, out)
		t.Fail()
	}

	in.Encode(buf, binary.LittleEndian)
	if err := out.Decode(buf, binary.LittleEndian, in.SbeSchemaVersion(), false); err != nil {
		t.Logf("Decoding Error:", err)
		t.Fail()
	}
	if out.ENUMField != in.ENUMField {
		t.Logf("in out.ENUMField != in.ENUMField")
		t.Fail()
	}

	return
}
