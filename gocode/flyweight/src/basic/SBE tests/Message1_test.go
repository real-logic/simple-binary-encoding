package sbe_tests

import (
	"testing"
)

func TestEncodeDecodeMessage1(t *testing.T) {
	var data [256]byte
	var in Message1
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.SetEDTField("abcdefghijklmnopqrst")
	in.SetENUMField(ENUM_Value10)

	var out Message1
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	expected := `{"Name": "Message1", "sbeTemplateId": 1, "header": {"blockLength": "0", "templateId": "0", "schemaId": "0", "version": "0"}, "EDTField": "abcdefghijklmnopqrst", "ENUMField": "Value10", "SETField": [], "int64Field": "0"}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
