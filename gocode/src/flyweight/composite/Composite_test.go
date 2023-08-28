package composite

import (
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	var data [256]byte
	var in Composite
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	if in.SbeBlockAndHeaderLength() != 50 {
		t.Logf("Failed to encode, expected %d, got %d",
			50,
			in.SbeBlockAndHeaderLength(),
		)
		t.Fail()
	}

	in.Start().
		SetName("start").
		SetD(3.14).
		SetI(1).
		SetUIndex(0, 66).
		SetUIndex(1, 77).
		SetTruthval1(BooleanEnum_NULL_VALUE).
		SetTruthval2(BooleanEnum_T)

	in.End().
		SetName("end").
		SetD(0.31).
		SetI(2).
		SetUIndex(0, 77).
		SetUIndex(1, 88).
		SetTruthval1(BooleanEnum_T).
		SetTruthval2(BooleanEnum_F)

	var out Composite
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(in.SbeBlockLength()),
		uint64(len(data)),
	)

	expected := `{"Name": "Composite", "sbeTemplateId": 1, "start": {"name": "start", "d": "3.14", "i": "1", "u": [66,77], "truthval1": "NULL_VALUE", "truthval2": "T"}, "end": {"name": "end", "d": "0.31", "i": "2", "u": [77,88], "truthval1": "T", "truthval2": "F"}}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
