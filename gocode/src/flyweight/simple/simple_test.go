package simple

import (
	"testing"
)

func TestEncodeDecode(t *testing.T) {
	var data [256]byte
	var in Simple0
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.SetU64nv(2863311530)
	in.SetU64(2863311530)
	in.SetU32(123456)
	in.SetU16(7890)
	in.SetU8(63)
	in.SetS8(-8)
	in.SetS16(-16)
	in.SetS32(-32)
	in.SetS64(-64)
	in.SetF32(3.14)
	in.SetD64(-3.14e7)
	in.SetString6ASCII("abcdef")
	in.SetString1ASCII('A')
	in.PutInt2Values(254, 255)

	var out Simple0
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	expected := `{"Name": "Simple0", "sbeTemplateId": 11, "U64nv": "2863311530", "U64": "2863311530", "U32": "123456", "U16": "7890", "U8": "63", "S8": "-8", "S16": "-16", "S32": "-32", "S64": "-64", "F32": "3.14", "D64": "-3.14e+07", "String6ASCII": "abcdef", "String1ASCII": "A", "Int2": [254,255]}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}

	if in.U64SinceVersion() != 1 {
		t.Log("in.U64SinceVersion() should be 1 and is", in.U64SinceVersion())
		t.Fail()
	}
}
