// Copyright 2013-2025 Real Logic Limited.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package otf

import (
	"encoding/binary"
	"errors"
	"fmt"
	"math"
	"strconv"
	"strings"
)

type ByteOrder int

const (
	SbeLittleEndian ByteOrder = iota
	SbeBigEndian
)

// String returns the string representation of the ByteOrder.
func (o ByteOrder) String() string {
	switch o {
	case SbeLittleEndian:
		return "SbeLittleEndian"
	case SbeBigEndian:
		return "SbeBigEndian"
	default:
		return "Unknown"
	}
}

type PrimitiveType int

const (
	NONE PrimitiveType = iota
	CHAR
	INT8
	INT16
	INT32
	INT64
	UINT8
	UINT16
	UINT32
	UINT64
	FLOAT
	DOUBLE
)

func (p PrimitiveType) String() string {
	switch p {
	case NONE:
		return "NONE"
	case CHAR:
		return "CHAR"
	case INT8:
		return "INT8"
	case INT16:
		return "INT16"
	case INT32:
		return "INT32"
	case INT64:
		return "INT64"
	case UINT8:
		return "UINT8"
	case UINT16:
		return "UINT16"
	case UINT32:
		return "UINT32"
	case UINT64:
		return "UINT64"
	case FLOAT:
		return "FLOAT"
	case DOUBLE:
		return "DOUBLE"
	default:
		return "Unknown"
	}
}

func (p PrimitiveType) Size() int {
	switch p {
	case CHAR, INT8, UINT8:
		return 1
	case INT16, UINT16:
		return 2
	case INT32, UINT32, FLOAT:
		return 4
	case INT64, UINT64, DOUBLE:
		return 8
	default:
		return 0
	}
}

// IsUnsigned returns true if the type is unsigned.
func (p PrimitiveType) IsUnsigned() bool {
	switch p {
	case UINT8, UINT16, UINT32, UINT64:
		return true
	default:
		return false
	}
}

type Presence int

const (
	SbeRequired Presence = iota
	SbeOptional
	SbeConstant
)

func (p Presence) String() string {
	switch p {
	case SbeRequired:
		return "SbeRequired"
	case SbeOptional:
		return "SbeOptional"
	case SbeConstant:
		return "SbeConstant"
	default:
		return "Unknown"
	}
}

type PrimitiveValue struct {
	PrimitiveType
	size       int
	asInt      int64
	asUInt     uint64
	asDouble   float64
	arrayValue string
}

// String returns the string representation of the value.
func (p *PrimitiveValue) String() string {
	switch p.PrimitiveType {
	case CHAR:
		if p.size > 1 {
			return p.arrayValue
		}
		return string(byte(p.asInt))
	case INT8, INT16, INT32, INT64:
		return strconv.FormatInt(p.asInt, 10)
	case UINT8, UINT16, UINT32, UINT64:
		return strconv.FormatUint(p.asUInt, 10)
	case FLOAT, DOUBLE:
		return strconv.FormatFloat(p.asDouble, 'f', -1, 64)
	default:
		return ""
	}
}

func AppendPrimitiveValue(sb *strings.Builder, p PrimitiveValue) {
	switch p.PrimitiveType {
	case CHAR:
		if p.size > 1 {
			sb.WriteString(p.arrayValue)
			return
		}
		sb.WriteByte(byte(p.asInt))
	case INT8, INT16, INT32, INT64:
		sb.WriteString(strconv.FormatInt(p.asInt, 10))
	case UINT8, UINT16, UINT32, UINT64:
		sb.WriteString(strconv.FormatUint(p.asUInt, 10))
	case FLOAT, DOUBLE:
		sb.WriteString(strconv.FormatFloat(p.asDouble, 'f', -1, 64))
	}
}

func AppendPrimitiveValueJson(sb *strings.Builder, p PrimitiveValue) {
	switch p.PrimitiveType {
	case CHAR:
		if p.size > 1 {
			sb.WriteString(p.arrayValue)
			return
		}
		sb.WriteRune('"')
		sb.WriteByte(byte(p.asInt))
		sb.WriteRune('"')
	case INT8, INT16, INT32, INT64:
		sb.WriteString(strconv.FormatInt(p.asInt, 10))
	case UINT8:
		sb.WriteString(strconv.FormatUint(p.asUInt&0xFF, 10))
	case UINT16:
		sb.WriteString(strconv.FormatUint(p.asUInt&0xFFFF, 10))
	case UINT32:
		sb.WriteString(strconv.FormatUint(p.asUInt&0xFFFFFFFF, 10))
	case UINT64:
		sb.WriteString(strconv.FormatUint(p.asUInt, 10))
	case FLOAT, DOUBLE:
		sb.WriteString(strconv.FormatFloat(p.asDouble, 'f', -1, 64))
	}
}

func NewPrimitiveValue(t PrimitiveType, value []byte) PrimitiveValue {
	p := PrimitiveValue{PrimitiveType: t}

	if len(value) == 0 {
		return p
	}

	switch t {
	case CHAR:
		if len(value) > 1 {
			p.arrayValue = string(value)
			p.size = len(value)
		} else {
			p.asInt = int64(value[0])
			p.size = 1
		}
	case INT8:
		p.asInt = int64(value[0])
		p.size = 1
	case INT16:
		p.asInt = int64(binary.LittleEndian.Uint16(value))
		p.size = 2
	case INT32:
		p.asInt = int64(binary.LittleEndian.Uint32(value))
		p.size = 4
	case INT64:
		p.asInt = int64(binary.LittleEndian.Uint64(value))
		p.size = 8
	case UINT8:
		p.asUInt = uint64(value[0])
		p.size = 1
	case UINT16:
		p.asUInt = uint64(binary.LittleEndian.Uint16(value))
		p.size = 2
	case UINT32:
		p.asUInt = uint64(binary.LittleEndian.Uint32(value))
		p.size = 4
	case UINT64:
		p.asUInt = binary.LittleEndian.Uint64(value)
		p.size = 8
	case FLOAT:
		p.asDouble = float64(math.Float32frombits(binary.LittleEndian.Uint32(value)))
		p.size = 4
	case DOUBLE:
		p.asDouble = math.Float64frombits(binary.LittleEndian.Uint64(value))
		p.size = 8
	default:
		p.PrimitiveType = NONE
		p.size = 0
	}

	return p
}

func ParsePrimitiveValue(t PrimitiveType, value []byte) (PrimitiveValue, error) {
	p := PrimitiveValue{PrimitiveType: t}

	var err error

	switch t {
	case CHAR:
		if len(value) > 1 {
			p.arrayValue = string(value)
			p.size = len(value)
		} else {
			p.asInt = int64(value[0])
			p.size = 1
		}
	case INT8:
		p.asInt, err = strconv.ParseInt(string(value), 10, 8)
		p.size = 1
	case INT16:
		p.asInt, err = strconv.ParseInt(string(value), 10, 16)
		p.size = 2
	case INT32:
		p.asInt, err = strconv.ParseInt(string(value), 10, 32)
		p.size = 4
	case INT64:
		p.asInt, err = strconv.ParseInt(string(value), 10, 64)
		p.size = 8
	case UINT8:
		p.asUInt, err = strconv.ParseUint(string(value), 10, 8)
		p.size = 1
	case UINT16:
		p.asUInt, err = strconv.ParseUint(string(value), 10, 16)
		p.size = 2
	case UINT32:
		p.asUInt, err = strconv.ParseUint(string(value), 10, 32)
		p.size = 4
	case UINT64:
		p.asUInt, err = strconv.ParseUint(string(value), 10, 64)
		p.size = 8
	case FLOAT:
		p.asDouble, err = strconv.ParseFloat(string(value), 32)
		p.size = 4
	case DOUBLE:
		p.asDouble, err = strconv.ParseFloat(string(value), 64)
		p.size = 8
	default:
		p.PrimitiveType = NONE
		p.size = 0
	}

	return p, err
}

func NewNoneValue() PrimitiveValue {
	return PrimitiveValue{}
}

func NewCharValue(value byte) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: CHAR,
		size:          1,
		asInt:         int64(value),
	}
}

func NewStringValue(value string) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: CHAR,
		size:          len(value),
		arrayValue:    value,
	}
}

func NewInt8Value(value int8) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: INT8,
		size:          1,
		asInt:         int64(value),
	}
}

func NewInt16Value(value int16) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: INT16,
		size:          2,
		asInt:         int64(value),
	}
}

func NewInt32Value(value int32) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: INT32,
		size:          4,
		asInt:         int64(value),
	}
}

func NewInt64Value(value int64) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: INT64,
		size:          8,
		asInt:         value,
	}
}

func NewUInt8Value(value uint8) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: UINT8,
		size:          1,
		asUInt:        uint64(value),
	}
}

func NewUInt16Value(value uint16) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: UINT16,
		size:          2,
		asUInt:        uint64(value),
	}
}

func NewUInt32Value(value uint32) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: UINT32,
		size:          4,
		asUInt:        uint64(value),
	}
}

func NewUInt64Value(value uint64) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: UINT64,
		size:          8,
		asUInt:        value,
	}
}

func NewFloatValue(value float32) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: FLOAT,
		size:          4,
		asDouble:      float64(value),
	}
}

func NewDoubleValue(value float64) PrimitiveValue {
	return PrimitiveValue{
		PrimitiveType: DOUBLE,
		size:          8,
		asDouble:      value,
	}
}

func (p *PrimitiveValue) AsInt() int64 {
	return p.asInt
}

func (p *PrimitiveValue) AsUInt() uint64 {
	return p.asUInt
}

func (p *PrimitiveValue) AsDouble() float64 {
	return p.asDouble
}

func (p *PrimitiveValue) GetArray() string {
	return p.arrayValue
}

func (p *PrimitiveValue) AsString() string {
	if p.size <= 1 {
		return string(byte(p.asInt))
	}
	for i := 0; i < len(p.arrayValue); i++ {
		if p.arrayValue[i] == 0 {
			return p.arrayValue[:i]
		}
	}
	return p.arrayValue
}

func (p *PrimitiveValue) Size() int {
	return p.size
}

type Encoding struct {
	presence          Presence
	primitiveType     PrimitiveType
	byteOrder         ByteOrder
	minValue          PrimitiveValue
	maxValue          PrimitiveValue
	nullValue         PrimitiveValue
	constValue        PrimitiveValue
	characterEncoding string
	epoch             string
	timeUnit          string
	semanticType      string
}

func (e *Encoding) String() string {
	return fmt.Sprintf(
		"Encoding{presence: %s primitiveType: %s, byteOrder: %s, minValue: %s, maxValue: %s, nullValue: %s, constValue: %s, characterEncoding: %s, epoch: %s, timeUnit: %s, semanticType: %s}",
		e.presence,
		e.primitiveType,
		e.byteOrder,
		&e.minValue,
		&e.maxValue,
		&e.nullValue,
		&e.constValue,
		e.characterEncoding,
		e.epoch,
		e.timeUnit,
		e.semanticType,
	)
}

func NewEncoding(
	primitiveType PrimitiveType,
	presence Presence,
	byteOrder ByteOrder,
	minValue, maxValue, nullValue, constValue PrimitiveValue,
	characterEncoding, epoch, timeUnit, semanticType string) Encoding {
	return Encoding{
		presence:          presence,
		primitiveType:     primitiveType,
		byteOrder:         byteOrder,
		minValue:          minValue,
		maxValue:          maxValue,
		nullValue:         nullValue,
		constValue:        constValue,
		characterEncoding: characterEncoding,
		epoch:             epoch,
		timeUnit:          timeUnit,
		semanticType:      semanticType,
	}
}

func GetChar(buffer []byte) byte {
	return buffer[0]
}

func GetInt8(buffer []byte) int8 {
	return int8(buffer[0])
}

func GetInt16(buffer []byte, order ByteOrder) int16 {
	if order == SbeLittleEndian {
		return int16(binary.LittleEndian.Uint16(buffer))
	}
	return int16(binary.BigEndian.Uint16(buffer))
}

func GetInt32(buffer []byte, order ByteOrder) int32 {
	if order == SbeLittleEndian {
		return int32(binary.LittleEndian.Uint32(buffer))
	}
	return int32(binary.BigEndian.Uint32(buffer))
}

func GetInt64(buffer []byte, order ByteOrder) int64 {
	if order == SbeLittleEndian {
		return int64(binary.LittleEndian.Uint64(buffer))
	}
	return int64(binary.BigEndian.Uint64(buffer))
}

func GetUInt8(buffer []byte) uint8 {
	return buffer[0]
}

func GetUInt16(buffer []byte, order ByteOrder) uint16 {
	if order == SbeLittleEndian {
		return binary.LittleEndian.Uint16(buffer)
	}
	return binary.BigEndian.Uint16(buffer)
}

func GetUInt32(buffer []byte, order ByteOrder) uint32 {
	if order == SbeLittleEndian {
		return binary.LittleEndian.Uint32(buffer)
	}
	return binary.BigEndian.Uint32(buffer)
}

func GetUInt64(buffer []byte, order ByteOrder) uint64 {
	if order == SbeLittleEndian {
		return binary.LittleEndian.Uint64(buffer)
	}
	return binary.BigEndian.Uint64(buffer)
}

func GetFloat(buffer []byte, order ByteOrder) float32 {
	if order == SbeLittleEndian {
		return math.Float32frombits(binary.LittleEndian.Uint32(buffer))
	}
	return math.Float32frombits(binary.BigEndian.Uint32(buffer))
}

func GetDouble(buffer []byte, order ByteOrder) float64 {
	if order == SbeLittleEndian {
		return math.Float64frombits(binary.LittleEndian.Uint64(buffer))
	}
	return math.Float64frombits(binary.BigEndian.Uint64(buffer))
}

func GetAsInt(primitiveType PrimitiveType, byteOrder ByteOrder, buffer []byte) (int64, error) {
	switch primitiveType {
	case CHAR:
		return int64(GetChar(buffer)), nil
	case INT8:
		return int64(GetInt8(buffer)), nil
	case INT16:
		return int64(GetInt16(buffer, byteOrder)), nil
	case INT32:
		return int64(GetInt32(buffer, byteOrder)), nil
	case INT64:
		return GetInt64(buffer, byteOrder), nil
	default:
		return 0, errors.New("incorrect type for Encoding.GetAsInt")
	}
}

func GetAsUInt(primitiveType PrimitiveType, byteOrder ByteOrder, buffer []byte) (uint64, error) {
	switch primitiveType {
	case UINT8:
		return uint64(GetUInt8(buffer)), nil
	case UINT16:
		return uint64(GetUInt16(buffer, byteOrder)), nil
	case UINT32:
		return uint64(GetUInt32(buffer, byteOrder)), nil
	case UINT64:
		return GetUInt64(buffer, byteOrder), nil
	default:
		return 0, errors.New("incorrect type for Encoding.GetAsUInt")
	}
}

func GetAsDouble(primitiveType PrimitiveType, byteOrder ByteOrder, buffer []byte) (float64, error) {
	switch primitiveType {
	case FLOAT:
		return float64(GetFloat(buffer, byteOrder)), nil
	case DOUBLE:
		return GetDouble(buffer, byteOrder), nil
	default:
		return 0, errors.New("incorrect type for Encoding.GetAsDouble")
	}
}

func (e *Encoding) GetAsInt(buffer []byte) (int64, error) {
	return GetAsInt(e.primitiveType, e.byteOrder, buffer)
}

func (e *Encoding) GetAsUInt(buffer []byte) (uint64, error) {
	return GetAsUInt(e.primitiveType, e.byteOrder, buffer)
}

func (e *Encoding) GetAsDouble(buffer []byte) (float64, error) {
	return GetAsDouble(e.primitiveType, e.byteOrder, buffer)
}

func (e *Encoding) Presence() Presence {
	return e.presence
}

func (e *Encoding) PrimitiveType() PrimitiveType {
	return e.primitiveType
}

func (e *Encoding) ByteOrder() ByteOrder {
	return e.byteOrder
}

func (e *Encoding) MinValue() PrimitiveValue {
	return e.minValue
}

func (e *Encoding) MaxValue() PrimitiveValue {
	return e.maxValue
}

func (e *Encoding) NullValue() PrimitiveValue {
	return e.nullValue
}

func (e *Encoding) ConstValue() PrimitiveValue {
	return e.constValue
}

func (e *Encoding) CharacterEncoding() string {
	return e.characterEncoding
}

func (e *Encoding) Epoch() string {
	return e.epoch
}

func (e *Encoding) TimeUnit() string {
	return e.timeUnit
}

func (e *Encoding) SemanticType() string {
	return e.semanticType
}

func (e *Encoding) ApplicableNullValue() PrimitiveValue {
	if e.nullValue.PrimitiveType != NONE {
		return e.nullValue
	}
	return PrimitiveValue{
		PrimitiveType: e.primitiveType,
	}
}
