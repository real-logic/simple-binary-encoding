package sbe_tests

import (
	"testing"
)

func TestEnum(t *testing.T) {
	e := ENUM_Value1
	if e.String() != "Value1" {
		t.Fail()
	}
}
