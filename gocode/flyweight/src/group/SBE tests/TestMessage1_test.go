package sbe_tests

import (
	"testing"
)

func TestEncodeDecodeTestMessage1(t *testing.T) {

	var data [256]byte
	var in TestMessage1
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.SetTag1(44)
	in.EntriesCount(1).Next().
		SetTagGroup1("abcdefghijklmnopqrst").
		SetTagGroup2(7)

	in.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	var out TestMessage1
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if in.Tag1() != out.Tag1() {
		t.Log("in != out:\n", in.Tag1(), out.Tag1())
		t.Fail()
	}
	inEntries, outEntries := in.Entries(), out.Entries()
	if inEntries.Count() != outEntries.Count() {
		t.Logf("len(in.Entries)(%d) != len(out.Entries)(%d):\n", inEntries.Count(), outEntries.Count())
		t.Fail()
	}
	for i := uint64(0); i < inEntries.Count(); i++ {
		outEntries = outEntries.Next()
		inEntries = inEntries.Next()
		if inEntries.String() != outEntries.String() {
			t.Logf("in.Entries[%d] != out.Entries[%d]: %s %s\n", i, i, inEntries.String(), outEntries.String())
			t.Fail()
		}
	}

	return

}
