package since_deprecated

import (
	"testing"
)

func TestSinceDeprecated(t *testing.T) {

	sv := SinceDeprecated{}

	// V1 has always been there
	if sv.V1SinceVersion() != 0 {
		t.Fail()
	}
	// V2 was introduced in version 2
	if sv.V2SinceVersion() != 2 {
		t.Fail()
	}
	// V3 was introduced in version 3 and deprecated in version 4
	if sv.V3SinceVersion() != 3 {
		t.Fail()
	}
}
