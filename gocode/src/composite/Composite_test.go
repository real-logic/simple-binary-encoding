package composite

import (
	"bytes"
	"encoding/binary"
	"testing"
)

func TestEncodeDecode(t *testing.T) {

	var s1, s2 [5]byte
	copy(s1[:], "start")
	copy(s2[:], "  end")
	p1 := Point{s1, 3.14, 1, [2]uint8{66, 77}, Truthval1.NullValue, Truthval2.T}
	p2 := Point{s2, 0.31, 2, [2]uint8{77, 88}, Truthval1.T, Truthval2.F}
	c := Composite{p1, p2}

	var cbuf = new(bytes.Buffer)
	if err := c.Encode(cbuf, binary.LittleEndian); err != nil {
		t.Log("Composite Encoding Error", err)
		t.Fail()
	}
	t.Log(c, " -> ", cbuf.Bytes())
	t.Log("Cap() = ", cbuf.Cap(), "Len() = \n", cbuf.Len())

	m := MessageHeader{c.SbeBlockLength(), c.SbeTemplateId(), c.SbeSchemaId(), c.SbeSchemaVersion()}
	var mbuf = new(bytes.Buffer)
	if err := m.Encode(mbuf, binary.LittleEndian); err != nil {
		t.Log("MessageHeader Encoding Error", err)
		t.Fail()
	}
	t.Log(m, " -> ", mbuf.Bytes())
	t.Log("Cap() = ", mbuf.Cap(), "Len() = \n", mbuf.Len())

	// Create a new empty MessageHeader and Composite
	m = *new(MessageHeader)
	var c2 Composite = *new(Composite)

	if err := m.Decode(mbuf, binary.LittleEndian, c.SbeSchemaVersion(), true); err != nil {
		t.Log("MessageHeader Decoding Error", err)
		t.Fail()
	}
	t.Log("MessageHeader Decodes as: ", m)
	t.Log("Cap() = ", mbuf.Cap(), "Len() = \n", mbuf.Len())

	if err := c2.Decode(cbuf, binary.LittleEndian, c.SbeSchemaVersion(), true); err != nil {
		t.Log("Composite Decoding Error", err)
		t.Fail()
	}
	t.Log("Composite decodes as: ", c2)
	t.Log("Cap() = ", cbuf.Cap(), "Len() = \n", cbuf.Len())

	// c2.End.I = 18
	if c != c2 {
		t.Logf("c != c2\n%s\n%s", c, c2)
		t.Fail()
	}
}
