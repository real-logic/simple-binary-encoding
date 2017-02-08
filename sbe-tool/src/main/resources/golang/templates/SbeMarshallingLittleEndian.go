// Copyright (C) 2017 MarketFactory, Inc
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This file provides a simple bespoke marhalling layer for the
// standard binary encoding golang backend and is part of:
//
// https://github.com/real-logic/simple-binary-encoding

package %1$s

import (
	"io"
	"math"
)

// Allocate via NewSbeGoMarshaller to initialize
type SbeGoMarshaller struct {
	b8 []byte // statically allocated tmp space to avoid alloc
	b1 []byte // previously created slice into b to save time
	b2 []byte // previously created slice into b to save time
	b4 []byte // previously created slice into b to save time
}

func NewSbeGoMarshaller() *SbeGoMarshaller {
	var m SbeGoMarshaller
	m.b8 = make([]byte, 8)
	m.b1 = m.b8[:1]
	m.b2 = m.b8[:2]
	m.b4 = m.b8[:4]
	return &m
}

func (m *SbeGoMarshaller) WriteUint8(w io.Writer, v uint8) error {
	m.b1[0] = byte(v)
	_, err := w.Write(m.b1)
	return err
}

func (m *SbeGoMarshaller) WriteUint16(w io.Writer, v uint16) error {
	m.b2[0] = byte(v)
	m.b2[1] = byte(v >> 8)
	_, err := w.Write(m.b2)
	return err
}

func (m *SbeGoMarshaller) WriteUint32(w io.Writer, v uint32) error {
	m.b4[0] = byte(v)
	m.b4[1] = byte(v >> 8)
	m.b4[2] = byte(v >> 16)
	m.b4[3] = byte(v >> 24)
	_, err := w.Write(m.b4)
	return err
}

func (m *SbeGoMarshaller) WriteUint64(w io.Writer, v uint64) error {
	m.b8[0] = byte(v)
	m.b8[1] = byte(v >> 8)
	m.b8[2] = byte(v >> 16)
	m.b8[3] = byte(v >> 24)
	m.b8[4] = byte(v >> 32)
	m.b8[5] = byte(v >> 40)
	m.b8[6] = byte(v >> 48)
	m.b8[7] = byte(v >> 56)
	_, err := w.Write(m.b8)
	return err
}

func (m *SbeGoMarshaller) WriteInt8(w io.Writer, v int8) error {
	m.b1[0] = byte(v)
	_, err := w.Write(m.b1)
	return err
}

func (m *SbeGoMarshaller) WriteInt16(w io.Writer, v int16) error {
	m.b2[0] = byte(v)
	m.b2[1] = byte(v >> 8)
	_, err := w.Write(m.b2)
	return err
}

func (m *SbeGoMarshaller) WriteInt32(w io.Writer, v int32) error {
	m.b4[0] = byte(v)
	m.b4[1] = byte(v >> 8)
	m.b4[2] = byte(v >> 16)
	m.b4[3] = byte(v >> 24)
	_, err := w.Write(m.b4)
	return err
}

func (m *SbeGoMarshaller) WriteInt64(w io.Writer, v int64) error {
	m.b8[0] = byte(v)
	m.b8[1] = byte(v >> 8)
	m.b8[2] = byte(v >> 16)
	m.b8[3] = byte(v >> 24)
	m.b8[4] = byte(v >> 32)
	m.b8[5] = byte(v >> 40)
	m.b8[6] = byte(v >> 48)
	m.b8[7] = byte(v >> 56)
	_, err := w.Write(m.b8)
	return err
}

func (m *SbeGoMarshaller) WriteFloat32(w io.Writer, v float32) error {
	u32 := math.Float32bits(v)
	m.b4[0] = byte(u32)
	m.b4[1] = byte(u32 >> 8)
	m.b4[2] = byte(u32 >> 16)
	m.b4[3] = byte(u32 >> 24)
	_, err := w.Write(m.b4)
	return err
}

func (m *SbeGoMarshaller) WriteFloat64(w io.Writer, v float64) error {
	u64 := math.Float64bits(v)
	m.b8[0] = byte(u64)
	m.b8[1] = byte(u64 >> 8)
	m.b8[2] = byte(u64 >> 16)
	m.b8[3] = byte(u64 >> 24)
	m.b8[4] = byte(u64 >> 32)
	m.b8[5] = byte(u64 >> 40)
	m.b8[6] = byte(u64 >> 48)
	m.b8[7] = byte(u64 >> 56)
	_, err := w.Write(m.b8)
	return err
}

func (m *SbeGoMarshaller) WriteBytes(w io.Writer, v []byte) error {
	_, err := w.Write(v)
	return err
}

func (m *SbeGoMarshaller) ReadUint8(r io.Reader, v *uint8) error {
	if _, err := io.ReadFull(r, m.b1); err != nil {
		return err
	}
	*v = uint8(m.b1[0])
	return nil
}

func (m *SbeGoMarshaller) ReadUint16(r io.Reader, v *uint16) error {
	if _, err := io.ReadFull(r, m.b2); err != nil {
		return err
	}
	*v = (uint16(m.b2[0]) | uint16(m.b2[1])<<8)
	return nil
}

func (m *SbeGoMarshaller) ReadUint32(r io.Reader, v *uint32) error {
	if _, err := io.ReadFull(r, m.b4); err != nil {
		return err
	}
	*v = (uint32(m.b4[0]) | uint32(m.b4[1])<<8 |
		uint32(m.b4[2])<<16 | uint32(m.b4[3])<<24)
	return nil
}

func (m *SbeGoMarshaller) ReadUint64(r io.Reader, v *uint64) error {
	if _, err := io.ReadFull(r, m.b8); err != nil {
		return err
	}
	*v = (uint64(m.b8[0]) | uint64(m.b8[1])<<8 |
		uint64(m.b8[2])<<16 | uint64(m.b8[3])<<24 |
		uint64(m.b8[4])<<32 | uint64(m.b8[5])<<40 |
		uint64(m.b8[6])<<48 | uint64(m.b8[7])<<56)
	return nil
}

func (m *SbeGoMarshaller) ReadInt8(r io.Reader, v *int8) error {
	if _, err := io.ReadFull(r, m.b1); err != nil {
		return err
	}
	*v = int8(m.b1[0])
	return nil
}

func (m *SbeGoMarshaller) ReadInt16(r io.Reader, v *int16) error {
	if _, err := io.ReadFull(r, m.b2); err != nil {
		return err
	}
	*v = (int16(m.b2[0]) | int16(m.b2[1])<<8)
	return nil
}

func (m *SbeGoMarshaller) ReadInt32(r io.Reader, v *int32) error {
	if _, err := io.ReadFull(r, m.b4); err != nil {
		return err
	}
	*v = (int32(m.b4[0]) | int32(m.b4[1])<<8 |
		int32(m.b4[2])<<16 | int32(m.b4[3])<<24)
	return nil
}

func (m *SbeGoMarshaller) ReadInt64(r io.Reader, v *int64) error {
	if _, err := io.ReadFull(r, m.b8); err != nil {
		return err
	}
	*v = (int64(m.b8[0]) | int64(m.b8[1])<<8 |
		int64(m.b8[2])<<16 | int64(m.b8[3])<<24 |
		int64(m.b8[4])<<32 | int64(m.b8[5])<<40 |
		int64(m.b8[6])<<48 | int64(m.b8[7])<<56)
	return nil
}

func (m *SbeGoMarshaller) ReadFloat32(r io.Reader, v *float32) error {
	if _, err := io.ReadFull(r, m.b4); err != nil {
		return err
	}
	*v = math.Float32frombits(uint32(m.b4[0]) | uint32(m.b4[1])<<8 |
		uint32(m.b4[2])<<16 | uint32(m.b4[3])<<24)
	return nil
}

func (m *SbeGoMarshaller) ReadFloat64(r io.Reader, v *float64) error {
	if _, err := io.ReadFull(r, m.b8); err != nil {
		return err
	}
	*v = math.Float64frombits(uint64(m.b8[0]) | uint64(m.b8[1])<<8 |
		uint64(m.b8[2])<<16 | uint64(m.b8[3])<<24 |
		uint64(m.b8[4])<<32 | uint64(m.b8[5])<<40 |
		uint64(m.b8[6])<<48 | uint64(m.b8[7])<<56)
	return nil
}

func (m *SbeGoMarshaller) ReadBytes(r io.Reader, b []byte) error {
	if _, err := io.ReadFull(r, b); err != nil {
		return err
	}
	return nil
}
