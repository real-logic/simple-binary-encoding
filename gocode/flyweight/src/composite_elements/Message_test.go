package composite_elements

import (
	"testing"
)

func TestEncodeDecodeMsg(t *testing.T) {
	var data [256]byte
	var in Msg
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.Structure().
		SetEnumOne(EnumOne_Value10).
		SetZeroth(0)
	in.Structure().
		SetOne().SetBit0(true)
	in.Structure().Inner().
		SetFirst(1).
		SetSecond(2)

	var out Msg
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(in.SbeBlockLength()),
		uint64(len(data)),
	)

	expected := `{"Name": "Msg", "sbeTemplateId": 1, "structure": {"enumOne": "Value10", "zeroth": "0", "setOne": ["Bit0"], "inner": {"first": "1", "second": "2"}}}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}

func TestEncodeDecodeMsg2(t *testing.T) {
	var data [256]byte
	var in Msg2
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.Structure().
		SetEnumOne(EnumOne_Value10).
		SetZeroth(0)
	in.Structure().
		SetOne().SetBit16(true)
	in.Structure().Inner().
		SetFirst(1).
		SetSecond(2)

	var out Msg2
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(in.SbeBlockLength()),
		uint64(len(data)),
	)

	expected := `{"Name": "Msg2", "sbeTemplateId": 2, "structure": {"enumOne": "Value10", "zeroth": "0", "setOne": ["Bit16"], "inner": {"first": "1", "second": "2"}}}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}

func TestEncodeDecodeMsg3(t *testing.T) {
	var data [256]byte
	var in Msg3
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.Structure().
		SetMantissa(65).
		SetExponent(3).
		SetIsSettlement(BooleanEnum_T)

	var out Msg3
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(in.SbeBlockLength()),
		uint64(len(data)),
	)

	expected := `{"Name": "Msg3", "sbeTemplateId": 3, "structure": {"mantissa": "65", "exponent": "3", "isSettlement": "T"}}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}
