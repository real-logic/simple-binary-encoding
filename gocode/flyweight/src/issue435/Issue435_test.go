package issue435

import (
	"testing"
)

func TestEncodeDecode(t *testing.T) {

	var data [256]byte

	var in Issue435

	// Non-standard header so we use the generated one
	var hdr MessageHeader
	hdr.Wrap(data[:], 0, 0, uint64(len(data)))
	hdr.S().SetTwo(true)

	in.WrapForEncode(data[:], hdr.EncodedLength(), uint64(len(data))-hdr.EncodedLength())
	in.Example().SetE(EnumRef_Two)

	var outHdr MessageHeader
	outHdr.Wrap(data[:], 0, 0, uint64(len(data)))

	if !outHdr.S().Two() {
		t.Logf("Failed to decode, expected Two to be true")
		t.Fail()
	}

	var out Issue435

	out.WrapForDecode(
		data[:],
		hdr.EncodedLength(),
		uint64(outHdr.BlockLength()),
		outHdr.ActingVersion(),
		uint64(len(data)))

	if out.Example().E() != EnumRef_Two {
		t.Logf("Failed to decode, expected EnumRef_Two, got %d", out.Example().E())
		t.Fail()
	}

	expected := `{"Name": "Issue435", "sbeTemplateId": 1, "example": {"e": "Two"}}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
