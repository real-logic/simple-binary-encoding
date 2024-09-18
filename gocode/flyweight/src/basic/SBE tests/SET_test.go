package sbe_tests

import (
	"testing"
)

func TestEncodeDecodeSet(t *testing.T) {
	var data [256]byte
	var in SET
	in.Wrap(data[:], 0, 0, uint64(len(data)))
	in.SetBit16(true)

	var out SET
	out.Wrap(
		data[:],
		0,
		uint64(in.SbeSchemaVersion()),
		uint64(len(data)),
	)

	if out.Bit0() {
		t.Fail()
	}

	if !out.Bit16() {
		t.Fail()
	}
}
