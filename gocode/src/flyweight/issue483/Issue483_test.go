package issue483

import (
	"testing"
)

func TestPresence(t *testing.T) {
	var issue483 Issue483

	if issue483.UnsetMetaAttribute("PRESENCE") != "required" {
		t.Log("Unset attribute's presence should be 'required'")
		t.Fail()
	}

	if issue483.RequiredMetaAttribute("PRESENCE") != "required" {
		t.Log("Required attribute's presence should be 'required'")
		t.Fail()
	}

	if issue483.ConstantMetaAttribute("PRESENCE") != "constant" {
		t.Log("Constant attribute's presence should be 'constant'")
		t.Fail()
	}

	// Check contant value is set by init func
	if issue483.Constant() != 1 {
		t.Log("Constant's value should be 1")
		t.Fail()
	}

	if issue483.OptionalMetaAttribute("PRESENCE") != "optional" {
		t.Log("Optional attribute's presence should be 'optional'")
		t.Fail()
	}
}
