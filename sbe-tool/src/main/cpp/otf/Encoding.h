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

#if defined(WIN32) || defined(_WIN32)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_16(v) _byteswap_ushort(v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_32(v) _byteswap_ulong(v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_64(v) _byteswap_uint64(v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_16(v) (v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_32(v) (v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_64(v) (v)
#elif __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__
    #define SBE_OTF_BIG_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_16(v) (v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_32(v) (v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_64(v) (v)
#elif __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)
    #define SBE_OTF_LITTLE_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_16(v) (v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_32(v) (v)
    #define SBE_OTF_BIG_ENDIAN_ENCODE_64(v) (v)
#else
    #error "Byte Ordering of platform not determined. Set __BYTE_ORDER__ manually before including this file."
#endif

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
        m_presence(presence), m_primitiveType(type), m_byteOrder(byteOrder), m_minValue(minValue), m_maxValue(maxValue),
        m_nullValue(nullValue), m_constValue(constValue), m_characterEncoding(characterEncoding), m_epoch(epoch),
        m_timeUnit(timeUnit), m_semanticType(semanticType)
    {
    }

    // TODO: finish these with appropriate ordering

    static inline char getChar(const char *buffer)
    {
        return *(char *)buffer;
    }

    static inline std::int8_t getInt8(const char *buffer)
    {
        return *(std::int8_t *)buffer;
    }

    static inline std::int16_t getInt16(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::int16_t *)buffer;
    }

    static inline std::int32_t getInt32(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::int32_t *)buffer;
    }

    static inline std::int64_t getInt64(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::int64_t *)buffer;
    }

    static inline std::uint8_t getUInt8(const char *buffer)
    {
        return *(std::uint8_t *)buffer;
    }

    static inline std::uint16_t getUInt16(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::uint16_t *)buffer;
    }

    static inline std::uint32_t getUInt32(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::uint32_t *)buffer;
    }

    static inline std::uint64_t getUInt64(const char *buffer, const ByteOrder byteOrder)
    {
        return *(std::uint64_t *)buffer;
    }

    static inline float getFloat(const char *buffer, const ByteOrder byteOrder)
    {
        return *(float *)buffer;
    }

    static inline double getDouble(const char *buffer, const ByteOrder byteOrder)
    {
        return *(double *)buffer;
    }

    static inline std::int64_t getInt(const PrimitiveType type, const ByteOrder byteOrder, const char *buffer)
    {
        switch (type)
        {
            case PrimitiveType::CHAR:
                return getChar(buffer);
            case PrimitiveType::INT8:
                return getInt8(buffer);
            case PrimitiveType::INT16:
                return getInt16(buffer, byteOrder);
            case PrimitiveType::INT32:
                return getInt32(buffer, byteOrder);
            case PrimitiveType::INT64:
                return getInt64(buffer, byteOrder);
            default:
                throw std::runtime_error("incorrect type for Encoding::getInt");
        }
    }

    static inline std::uint64_t getUInt(const PrimitiveType type, const ByteOrder byteOrder, const char *buffer)
    {
        switch (type)
        {
            case PrimitiveType::UINT8:
                return getUInt8(buffer);
            case PrimitiveType::UINT16:
                return getUInt16(buffer, byteOrder);
            case PrimitiveType::UINT32:
                return getUInt32(buffer, byteOrder);
            case PrimitiveType::UINT64:
                return getUInt64(buffer, byteOrder);
            default:
                throw std::runtime_error("incorrect type for Encoding::getUInt");
        }
    }

    static inline double getDouble(const PrimitiveType type, const ByteOrder byteOrder, const char *buffer)
    {
        if (type == PrimitiveType::FLOAT)
        {
            return getFloat(buffer, byteOrder);
        }
        else if (type == PrimitiveType::DOUBLE)
        {
            return getDouble(buffer, byteOrder);
        }
        else
        {
            throw std::runtime_error("incorrect type for Encoding::getDouble");
        }
    }

    inline ByteOrder byteOrder() const
    {
        return m_byteOrder;
    }

    inline PrimitiveType primitiveType() const
    {
        return m_primitiveType;
    }

    inline std::int64_t getAsInt(const char *buffer)
    {
        return getInt(m_primitiveType, m_byteOrder, buffer);
    }

    inline std::int64_t getAsUInt(const char *buffer)
    {
        return getUInt(m_primitiveType, m_byteOrder, buffer);
    }

    inline double getAsDouble(const char *buffer)
    {
        return getDouble(m_primitiveType, m_byteOrder, buffer);
    }

private:
    Presence m_presence;
    PrimitiveType m_primitiveType;
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
