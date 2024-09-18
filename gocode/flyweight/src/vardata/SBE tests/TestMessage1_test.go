package sbe_tests

import (
	"testing"
)

func TestEncodeDecodeTestMessage1(t *testing.T) {
	var data [256]byte
	var in TestMessage1
	in.WrapForEncode(data[:], 0, uint64(len(data)))
	in.PutEncryptedNewPassword("abcdefghijklmnopqrst")

	var out TestMessage1
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	expected := `{"Name": "TestMessage1", "sbeTemplateId": 1, "encryptedNewPassword": "abcdefghijklmnopqrst"}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
