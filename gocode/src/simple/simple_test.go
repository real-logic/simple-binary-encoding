package simple

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	in := Simple0{2863311530, 123456, 7890, 63, -8, -16, -32, -64, 3.14, -3.14e7, [6]byte{'a', 'b', 'c', 'd', 'e', 'f'}, 'A', [2]int32{254, 255}}

	var sbuf = new(bytes.Buffer)
	if err := in.Encode(sbuf, binary.LittleEndian, true); err != nil {
		t.Log("Simple0 Encoding Error", err)
		t.Fail()
	}
	t.Log(in, " -> ", sbuf.Bytes())
	t.Log("Cap() = ", sbuf.Cap(), "Len() = ", sbuf.Len())

	m := MessageHeader{in.SbeBlockLength(), in.SbeTemplateId(), in.SbeSchemaId(), in.SbeSchemaVersion()}
	var mbuf = new(bytes.Buffer)
	if err := m.Encode(mbuf, binary.LittleEndian); err != nil {
		t.Log("MessageHeader Encoding Error", err)
		t.Fail()
	}
	t.Log(m, " -> ", mbuf.Bytes())
	t.Log("Cap() = ", mbuf.Cap(), "Len() = ", mbuf.Len())

	// Create a new empty MessageHeader and Simple0
	m = *new(MessageHeader)
	var out Simple0 = *new(Simple0)

	if err := m.Decode(mbuf, binary.LittleEndian, 0); err != nil {
		t.Log("MessageHeader Decoding Error", err)
		t.Fail()
	}
	t.Log("MessageHeader Decodes as: ", m)
	t.Log("Cap() = ", mbuf.Cap(), "Len() = ", mbuf.Len())

	if err := out.Decode(sbuf, binary.LittleEndian, in.SbeSchemaVersion(), in.SbeBlockLength(), true); err != nil {
		t.Log("Simple0 Decoding Error", err)
		t.Fail()
	}
	t.Log("Simple0 decodes as: ", out)
	t.Log("Cap() = ", sbuf.Cap(), "Len() = ", sbuf.Len())

	if in != out {
		t.Logf("in != out\n%s\n%s", in, out)
		t.Fail()
	}

	// SinceVersion and Deprecated checkeds
	if in.U64SinceVersion() != 1 {
		t.Logf("in.U64Deprecated() should be 1 and is", in.U64SinceVersion())
		t.Fail()
	}
	if in.U64Deprecated() != 2 {
		t.Logf("in.U64Deprecated() should be 2 and is", in.U64Deprecated())
		t.Fail()
	}
}
