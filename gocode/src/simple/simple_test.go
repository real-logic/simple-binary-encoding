package simple

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	s := Simple0{2863311530, 123456, 7890, 63, -8, -16, -32, -64, 3.14, -3.14e7, [6]byte{'a', 'b', 'c', 'd', 'e', 'f'}, 'A', [2]int32{254, 255}}

	var sbuf = new(bytes.Buffer)
	if err := s.Encode(sbuf, binary.LittleEndian); err != nil {
		t.Log("Simple0 Encoding Error", err)
		t.Fail()
	}
	t.Log(s, " -> ", sbuf.Bytes())
	t.Log("Cap() = ", sbuf.Cap(), "Len() = ", sbuf.Len())

	m := MessageHeader{s.SbeBlockLength(), s.SbeTemplateId(), s.SbeSchemaId(), s.SbeSchemaVersion()}
	var mbuf = new(bytes.Buffer)
	if err := m.Encode(mbuf, binary.LittleEndian); err != nil {
		t.Log("MessageHeader Encoding Error", err)
		t.Fail()
	}
	t.Log(m, " -> ", mbuf.Bytes())
	t.Log("Cap() = ", mbuf.Cap(), "Len() = ", mbuf.Len())

	// Create a new empty MessageHeader and Simple0
	m = *new(MessageHeader)
	var s2 Simple0 = *new(Simple0)

	if err := m.Decode(mbuf, binary.LittleEndian, 0, true); err != nil {
		t.Log("MessageHeader Decoding Error", err)
		t.Fail()
	}
	t.Log("MessageHeader Decodes as: ", m)
	t.Log("Cap() = ", mbuf.Cap(), "Len() = ", mbuf.Len())

	if err := s2.Decode(sbuf, binary.LittleEndian, 0, true); err != nil {
		t.Log("Simple0 Decoding Error", err)
		t.Fail()
	}
	t.Log("Simple0 decodes as: ", s2)
	t.Log("Cap() = ", sbuf.Cap(), "Len() = ", sbuf.Len())

	// s2.Int2[0] = 18
	if s != s2 {
		t.Logf("s != s2\n%s\n%s", s, s2)
		t.Fail()
	}
}
