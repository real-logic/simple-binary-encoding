package group_with_data

import (
	_ "fmt"
	"testing"
)

func TestEncodeDecodeTestMessage1(t *testing.T) {
	var data [256]byte
	var in TestMessage1
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	in.SetTag1(1234)
	in.EntriesCount(1).Next().
		SetTagGroup1("123456789").
		SetTagGroup2(123456789).
		PutVarDataField("abcdef")

	var out TestMessage1
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	expected := `{"Name": "TestMessage1", "sbeTemplateId": 1, "Tag1": "1234", "Entries": [{"TagGroup1": "123456789", "TagGroup2": "123456789", "varDataField": "abcdef"}]}`
	if actual := out.String(); actual != expected {
		t.Logf("Failed to decode, expected %s, got %s", expected, actual)
		t.Fail()
	}
}

func TestEncodeDecodeTestMessage2(t *testing.T) {
	var data [256]byte
	var in TestMessage2
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	tag1 := uint32(1234)
	tagGroup1 := "123456789"
	tagGroup2 := int64(123456789)
	varDataField1 := "abcdef"
	varDataField2 := "ghij"

	in.SetTag1(tag1)
	entries := in.EntriesCount(2)
	for i := 0; i < 2; i++ {
		entry := entries.Next()
		entry.SetTagGroup1(tagGroup1).
			SetTagGroup2(tagGroup2).
			PutVarDataField1(varDataField1).
			PutVarDataField2(varDataField2)
	}

	var out TestMessage2
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if out.Tag1() != tag1 {
		t.Logf("Tag1 failed, expected %d, got %d", tag1, out.Tag1())
		t.Fail()
	}
	outEntries := out.Entries()
	for i := 0; i < 2; i++ {
		entry := outEntries.Next()
		if entry.GetTagGroup1AsString() != tagGroup1 {
			t.Logf("TagGroup1 failed at index %d, expected %s, got %s", i, tagGroup1, entry.GetTagGroup1AsString())
			t.Fail()
		}
		if entry.TagGroup2() != tagGroup2 {
			t.Logf("TagGroup2 failed at index %d, expected %d, got %d", i, tagGroup2, entry.TagGroup2())
			t.Fail()
		}
		if entry.VarDataField1() != varDataField1 {
			t.Logf("VarDataField1 failed at index %d, expected %s, got %s", i, varDataField1, entry.VarDataField1())
			t.Fail()
		}
		if entry.VarDataField2() != varDataField2 {
			t.Logf("VarDataField2 failed at index %d, expected %s, got %s", i, varDataField2, entry.VarDataField2())
			t.Fail()
		}
	}
}

func TestEncodeDecodeTestMessage3(t *testing.T) {
	var data [256]byte
	var in TestMessage3
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	tag1 := uint32(1234)
	tagGroup1 := "123456789"
	varDataField := "middle"
	tagGroup2Nested := int64(99887766)
	varDataFieldNested := "nested"

	in.SetTag1(tag1)
	entries := in.EntriesCount(2)
	for i := 0; i < 2; i++ {
		entry := entries.Next()
		entry.SetTagGroup1(tagGroup1).
			PutVarDataField(varDataField).
			NestedEntriesCount(1).Next().
			SetTagGroup2(tagGroup2Nested).
			PutVarDataFieldNested(varDataFieldNested)
	}

	in.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	var out TestMessage3
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if out.Tag1() != tag1 {
		t.Logf("Tag1 failed, expected %d, got %d", tag1, out.Tag1())
		t.Fail()
	}
	outEntries := out.Entries()
	for i := 0; i < 2; i++ {
		entry := outEntries.Next()
		if entry.GetTagGroup1AsString() != tagGroup1 {
			t.Logf("TagGroup1 failed at index %d, expected %s, got %s", i, tagGroup1, entry.GetTagGroup1AsString())
			t.Fail()
		}
		if entry.VarDataField() != varDataField {
			t.Logf("VarDataField failed at index %d, expected %s, got %s", i, varDataField, entry.VarDataField())
			t.Fail()
		}
		nestedEntry := entry.NestedEntries().Next()
		if nestedEntry.TagGroup2() != tagGroup2Nested {
			t.Logf("TagGroup2Nested failed at index %d, expected %d, got %d", i, tagGroup2Nested, nestedEntry.TagGroup2())
			t.Fail()
		}
		if nestedEntry.VarDataFieldNested() != varDataFieldNested {
			t.Logf("VarDataFieldNested failed at index %d, expected %s, got %s", i, varDataFieldNested, nestedEntry.VarDataFieldNested())
			t.Fail()
		}
	}
}

func TestEncodeDecodeTestMessage4(t *testing.T) {
	var data [256]byte
	var in TestMessage4
	in.WrapForEncode(data[:], 0, uint64(len(data)))

	tag1 := uint32(9876)
	varDataField1 := "abcdef"
	varDataField2 := "ghij"

	in.SetTag1(tag1)
	entries := in.EntriesCount(3)
	for i := 0; i < 3; i++ {
		entry := entries.Next()
		entry.PutVarDataField1(varDataField1).
			PutVarDataField2(varDataField2)
	}

	var out TestMessage4
	out.WrapForDecode(
		data[:],
		0,
		uint64(in.SbeBlockLength()),
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if out.Tag1() != tag1 {
		t.Logf("Tag1 failed, expected %d, got %d", tag1, out.Tag1())
		t.Fail()
	}

	outEntries := out.Entries()
	for i := 0; i < 3; i++ {
		entry := outEntries.Next()
		if entry.VarDataField1() != varDataField1 {
			t.Logf("VarDataField1 failed at index %d, expected %s, got %s", i, varDataField1, entry.VarDataField1())
			t.Fail()
		}
		if entry.VarDataField2() != varDataField2 {
			t.Logf("VarDataField2 failed at index %d, expected %s, got %s", i, varDataField2, entry.VarDataField2())
			t.Fail()
		}
	}
}
