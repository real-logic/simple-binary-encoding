package issue472

import (
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	var data [256]byte
	// in contains a single optional field which is not initialized
	var in Issue472
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	var out Issue472
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	expected := `{"Name": "Issue472", "sbeTemplateId": 1, "optional": "0"}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
