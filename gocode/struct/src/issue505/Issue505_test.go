package issue505

import (
	"bytes"
	"testing"
)

func TestConstant(t *testing.T) {
	message := new(SomeMessage)
	SomeMessageInit(message)

	if message.EngineType != EngineType.Gas {
		t.Log("message.EngineType != EngineType.Gas:\n", message.EngineType, EngineType.Gas)
		t.Fail()
	}

	m := NewSbeGoMarshaller()
	var buf = new(bytes.Buffer)
	var out = *new(SomeMessage)
	if err := out.Decode(m, buf, message.SbeSchemaVersion(), message.SbeBlockLength(), true); err != nil {
		t.Log("Decoding Error", err)
		t.Fail()
	}

	if out.EngineType != EngineType.Gas {
		t.Log("out.EngineType != EngineType.Gas:\n", out.EngineType, EngineType.Gas)
		t.Fail()
	}
}
