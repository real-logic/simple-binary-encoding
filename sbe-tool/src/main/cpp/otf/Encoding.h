/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef _OTF_ENCODING_H
#define _OTF_ENCODING_H

#include <cstdint>
#include <string>

namespace sbe {
namespace otf {

/// Constants used for representing byte order
enum class ByteOrder : int
{
    /// little endian byte order
        SBE_LITTLE_ENDIAN = 0,
    /// big endian byte order
        SBE_BIG_ENDIAN = 1
};

/// Constants used for representing primitive types
enum class PrimitiveType : int
{
    /// Type is undefined or unknown
        NONE = 0,
    /// Type is a signed character
        CHAR = 1,
    /// Type is a signed 8-bit value
        INT8 = 2,
    /// Type is a signed 16-bit value
        INT16 = 3,
    /// Type is a signed 32-bit value
        INT32 = 4,
    /// Type is a signed 64-bit value
        INT64 = 5,
    /// Type is a unsigned 8-bit value
        UINT8 = 6,
    /// Type is a unsigned 16-bit value
        UINT16 = 7,
    /// Type is a unsigned 32-bit value
        UINT32 = 8,
    /// Type is a unsigned 64-bit value
        UINT64 = 9,
    /// Type is a 32-bit floating point value
        FLOAT = 10,
    /// Type is a 64-bit double floating point value
        DOUBLE = 11
};

/// Constants used for representing Presence
enum class Presence : int
{
    /// Field or encoding presence is required
        SBE_REQUIRED = 0,
    /// Field or encoding presence is optional
        SBE_OPTIONAL = 1,
    /// Field or encoding presence is constant and not encoded
        SBE_CONSTANT = 2
};

class PrimitiveValue
{
public:
    PrimitiveValue(PrimitiveType type, long valueLength, const char *value)
    {
        if (0 == valueLength)
        {
            type = PrimitiveType::NONE;
            return;
        }

        switch (type)
        {
            case PrimitiveType::CHAR:

                break;
            case PrimitiveType::INT8:
                m_value.m_int8 = *(sbe_int8_t *)value;
                m_size = 1;
                break;
            default:
                type = PrimitiveType::NONE;
                break;
        }
    }

private:
    PrimitiveType m_type;
    size_t m_size;
    union
    {
        char m_charValue;
        sbe_int8_t m_int8;
        std::int16_t m_int16;
        std::int32_t m_int32;
        std::int64_t m_int64;
        std::uint8_t m_uint8;
        std::uint16_t m_uint16;
        std::uint32_t m_uint32;
        std::uint64_t m_uint64;
        float m_float;
        double m_double;
    } m_value;
};

class Encoding
{
public:
    Encoding(
        PrimitiveType type,
        Presence presence,
        ByteOrder byteOrder,
        PrimitiveValue minValue,
        PrimitiveValue maxValue,
        PrimitiveValue nullValue,
        PrimitiveValue constValue,
        std::string characterEncoding,
        std::string epoch,
        std::string timeUnit,
        std::string semanticType) :
        m_presence(presence), m_type(type), m_byteOrder(byteOrder), m_minValue(minValue), m_maxValue(maxValue),
        m_nullValue(nullValue), m_constValue(constValue), m_characterEncoding(characterEncoding), m_epoch(epoch),
        m_timeUnit(timeUnit), m_semanticType(semanticType)
    {
    }

private:
    Presence m_presence;
    PrimitiveType m_type;
    ByteOrder m_byteOrder;

    PrimitiveValue m_minValue;
    PrimitiveValue m_maxValue;
    PrimitiveValue m_nullValue;
    PrimitiveValue m_constValue;

    std::string m_characterEncoding;
    std::string m_epoch;
    std::string m_timeUnit;
    std::string m_semanticType;
};

}}

#endif
