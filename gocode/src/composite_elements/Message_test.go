package composite_elements

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecodeMsg(t *testing.T) {

	var set SetOne
	var in Msg

	set[SetOneChoice.Bit0] = true
	in.Structure = Outer{EnumOne.Value10, 0, set, OuterInner{1, 2}}

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out Msg = *new(Msg)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out\n", in, out)
		t.Fail()
	}

	return

}

func TestEncodeDecodeMsg2(t *testing.T) {

	var set SetOne
	var in Msg2

	set[SetOneChoice.Bit16] = true
	in.Structure = OuterWithOffsets{EnumOne.Value10, 0, set, OuterWithOffsetsInner{1, 2}}

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}
	if int(in.SbeBlockLength()) != buf.Len() {
		t.Logf("in.SbeBlockLength(%d) != buf.len(%d)", in.SbeBlockLength(), buf.Len())
		t.Fail()
	}

	var out Msg2 = *new(Msg2)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out\n", in, out)
		t.Fail()
	}

	return

}

func TestEncodeDecodeMsg3(t *testing.T) {

	var in Msg3

	in.Structure = FuturesPrice{65, 3, IsSettlement.T}

	var buf = new(bytes.Buffer)
	if err := in.Encode(buf, binary.LittleEndian); err != nil {
		t.Logf("Encoding Error", err)
		t.Fail()
	}

	var out Msg3 = *new(Msg3)
	if err := out.Decode(buf, binary.LittleEndian, 0, true); err != nil {
		t.Logf("Decoding Error", err)
		t.Fail()
	}

	if in != out {
		t.Logf("in != out\n", in, out)
		t.Fail()
	}

	return

}
