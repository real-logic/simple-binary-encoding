package test973

import (
	"bytes"
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	message := new(SomeMessage)
	SomeMessageInit(message)

	m := NewSbeGoMarshaller()
	var buf = new(bytes.Buffer)

	message.MyEvent[EventTypeChoice.A] = true
	message.MyEvent[EventTypeChoice.Ccc] = true

	if err := message.Encode(m, buf, true); err != nil {
		t.Log("Encoding Error", err)
		t.Fail()
	}

	var out = *new(SomeMessage)
	if err := out.Decode(m, buf, message.SbeSchemaVersion(), message.SbeBlockLength(), true); err != nil {
		t.Log("Decoding Error", err)
		t.Fail()
	}

	if out.MyEvent != message.MyEvent {
		t.Log("Value mismatch: ", out.MyEvent, message.MyEvent)
		t.Fail()
	}

	if !out.MyEvent[EventTypeChoice.A] || !out.MyEvent[EventTypeChoice.Ccc] || out.MyEvent[EventTypeChoice.EeEee] {
		t.Log("Sanity check failed: ", out.MyEvent)
		t.Fail()
	}
}
