package group_with_data_extension

import (
	"fmt"
	"group_with_data"
	"math"
	"testing"
)

// Note, this is a copy of group-with-data that we extended to test
// message extensions within a nested group and vardata
func makeTestMessage3Extension() (data []byte, msg *TestMessage3) {
	data = make([]byte, 256)
	var t TestMessage3
	t.WrapForEncode(data[:], 0, uint64(len(data)))

	t.SetTag1(1234)

	entries := t.EntriesCount(2) // Assuming 2 entries
	for i := 0; i < 2; i++ {
		entry := entries.Next()
		entry.SetTagGroup1("123456789")

		nestedEntries := entry.NestedEntriesCount(1) // Assuming 1 nested entry
		nestedEntry := nestedEntries.Next()
		nestedEntry.SetTagGroup2(99887766)
		nestedEntry.SetInnerExtension(11112222)
		nestedEntry.PutVarDataFieldNested("nested")

		entry.PutVarDataField("middle")
	}

	return data, &t
}

func makeTestMessage3Original() (data []byte, msg *group_with_data.TestMessage3) {
	data = make([]byte, 256)
	var t group_with_data.TestMessage3
	t.WrapForEncode(data[:], 0, uint64(len(data)))

	t.SetTag1(1234)

	entries := t.EntriesCount(2) // Assuming 2 entries
	for i := 0; i < 2; i++ {
		entry := entries.Next()
		entry.SetTagGroup1("123456789")

		nestedEntries := entry.NestedEntriesCount(1) // Assuming 1 nested entry
		nestedEntry := nestedEntries.Next()
		nestedEntry.SetTagGroup2(99887766)
		nestedEntry.PutVarDataFieldNested("")

		entry.PutVarDataField("middle")
	}

	return data, &t
}

// Basic test of new message
func TestEncodeDecodeNewtoNew(t *testing.T) {
	data, in := makeTestMessage3Extension()

	var out TestMessage3
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if in.Tag1() != out.Tag1() {
		t.Logf("in.Tag1 != out.Tag1")
		t.Fail()
	}

	entries := out.Entries()
	for i := 0; i < int(entries.Count()); i++ {
		entry := entries.Next()
		if entry.GetTagGroup1AsString() != "123456789" {
			t.Logf("entry.TagGroup1 != out.Entries[0].TagGroup1")
			fmt.Printf("%+v\n%+v\n", in, out)
			t.Fail()
		}

		nestedEntry := entry.NestedEntries().Next()
		if nestedEntry.TagGroup2() != 99887766 {
			t.Logf("nestedEntry.TagGroup2 != out.Entries[0].NestedEntries[0].TagGroup2")
			fmt.Printf("%+v\n%+v\n", in, out)
			t.Fail()
		}

		if nestedEntry.InnerExtension() != 11112222 {
			t.Logf("n.Entries[0].NestedEntries[0].InnerExtension != out.Entries[0].NestedEntries[0].InnerExtension")
			fmt.Printf("%+v\n%+v\n", in, out)
			t.Fail()
		}

		if nestedEntry.VarDataFieldNested() != "nested" {
			t.Logf("in.Entries[0].NestedEntries[0].VarDataFieldNested != out.Entries[0].NestedEntries[0].VarDataFieldNested")
			fmt.Printf("%+v\n%+v\n", in, out)
			t.Fail()
		}

		if value := entry.VarDataField(); value != "middle" {
			t.Logf("in.Entries[%d].VarDataField (%v) != entry.VarDataField (%v)", i, value, "middle")
			t.Fail()
		}
	}
}

// Test of New to Old
func TestEncodeDecodeNewToOld(t *testing.T) {
	data, in := makeTestMessage3Extension()

	var out group_with_data.TestMessage3
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if in.Tag1() != out.Tag1() {
		t.Logf("in.Tag1 != out.Tag1")
		t.Fail()
	}

	entries := out.Entries()
	for i := 0; i < int(entries.Count()); i++ {
		entry := entries.Next()
		if value := entry.GetTagGroup1AsString(); value != "123456789" {
			t.Logf("entry.TagGroup1 (%s) != out.Entries[0].TagGroup1 (%s)",
				value, "123456789")
			t.Fail()
		}

		nestedEntry := entry.NestedEntries().Next()
		if nestedEntry.TagGroup2() != 99887766 {
			t.Logf("nestedEntry.TagGroup2 (%d) != out.Entries[0].NestedEntries[0].TagGroup2 (%d)",
				nestedEntry.TagGroup2(), 99887766)
			t.Fail()
		}

		if nestedEntry.VarDataFieldNested() != "nested" {
			t.Logf("in.Entries[0].NestedEntries[0].VarDataFieldNested (%s) != out.Entries[0].NestedEntries[0].VarDataFieldNested (%s)",
				nestedEntry.VarDataFieldNested(), "nested")
			t.Fail()
		}

		if value := entry.VarDataField(); value != "middle" {
			t.Logf("in.Entries[%d].VarDataField (%v) != entry.VarDataField (%v)", i, value, "middle")
			t.Fail()
		}
	}
}

// Test of Old to New
func TestEncodeDecodeOldToNew(t *testing.T) {
	data, in := makeTestMessage3Original()

	var out TestMessage3
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if in.Tag1() != out.Tag1() {
		t.Logf("in.Tag1 != out.Tag1")
		t.Fail()
	}

	entries := out.Entries()
	for i := 0; i < int(entries.Count()); i++ {
		entry := entries.Next()
		if entry.GetTagGroup1AsString() != "123456789" {
			t.Logf("entry.TagGroup1 != out.Entries[0].TagGroup1")
			fmt.Printf("%+v\n%+v\n", in, out)
			t.Fail()
		}

		nestedEntry := entry.NestedEntries().Next()
		if nestedEntry.TagGroup2() != 99887766 {
			t.Logf("nestedEntry.TagGroup2 (%d) != out.Entries[0].NestedEntries[0].TagGroup2 (%d)",
				nestedEntry.TagGroup2(), 99887766)
			t.Fail()
		}

		if nestedEntry.InnerExtension() != math.MinInt64 {
			t.Logf("n.Entries[0].NestedEntries[0].InnerExtension (%d) != out.Entries[0].NestedEntries[0].InnerExtension (%d)",
				nestedEntry.InnerExtension(), math.MinInt64)
			t.Fail()
		}

		if value := nestedEntry.VarDataFieldNested(); value != "" {
			t.Logf("in.Entries[0].NestedEntries[0].VarDataFieldNested (%s) != out.Entries[0].NestedEntries[0].VarDataFieldNested (%s)",
				value, "")
			t.Fail()
		}

		if value := entry.VarDataField(); value != "middle" {
			t.Logf("in.Entries[%d].VarDataField (%v) != entry.VarDataField (%v)", i, value, "middle")
			t.Fail()
		}
	}
}
