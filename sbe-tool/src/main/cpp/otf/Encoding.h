/*
 * Copyright 2015 - 2016 Real Logic Ltd.
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
#    define SBE_OTF_BSWAP_16(v) _byteswap_ushort(v)
#    define SBE_OTF_BSWAP_32(v) _byteswap_ulong(v)
#    define SBE_OTF_BSWAP_64(v) _byteswap_uint64(v)
#else // assuming gcc/clang
#    define SBE_OTF_BSWAP_16(v) __builtin_bswap16(v)
#    define SBE_OTF_BSWAP_32(v) __builtin_bswap32(v)
#    define SBE_OTF_BSWAP_64(v) __builtin_bswap64(v)
#endif

#if defined(WIN32) || defined(_WIN32)
#    define SBE_OTF_BYTE_ORDER_16(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_16(v))
#    define SBE_OTF_BYTE_ORDER_32(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_32(v))
#    define SBE_OTF_BYTE_ORDER_64(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_64(v))
#elif __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__
#    define SBE_OTF_BYTE_ORDER_16(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_16(v))
#    define SBE_OTF_BYTE_ORDER_32(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_32(v))
#    define SBE_OTF_BYTE_ORDER_64(o,v) ((o == ByteOrder::SBE_LITTLE_ENDIAN) ? (v) : SBE_OTF_BSWAP_64(v))
#elif __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
#    define SBE_OTF_BYTE_ORDER_16(o,v) ((o == ByteOrder::SBE_BIG_ENDIAN) ? (v) : SBE_OTF_BSWAP_16(v))
#    define SBE_OTF_BYTE_ORDER_32(o,v) ((o == ByteOrder::SBE_BIG_ENDIAN) ? (v) : SBE_OTF_BSWAP_32(v))
#    define SBE_OTF_BYTE_ORDER_64(o,v) ((o == ByteOrder::SBE_BIG_ENDIAN) ? (v) : SBE_OTF_BSWAP_64(v))
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

std::size_t lengthOfType(PrimitiveType type)
{
    switch (type)
    {
        case PrimitiveType::CHAR:
        {
            return 1;
        }
        case PrimitiveType::INT8:
        {
            return 1;
        }
        case PrimitiveType::INT16:
        {
            return 2;
        }
        case PrimitiveType::INT32:
        {
            return 4;
        }
        case PrimitiveType::INT64:
        {
            return 8;
        }
        case PrimitiveType::UINT8:
        {
            return 1;
        }
        case PrimitiveType::UINT16:
        {
            return 2;
        }
        case PrimitiveType::UINT32:
        {
            return 4;
        }
        case PrimitiveType::UINT64:
        {
            return 8;
        }
        case PrimitiveType::FLOAT:
        {
            return 4;
        }
        case PrimitiveType::DOUBLE:
        {
            return 8;
        }
        default:
        {
            return 0;
        }
    }
}

class PrimitiveValue
{
public:
    PrimitiveValue(PrimitiveType type, std::size_t valueLength, const char *value)
    {
        m_type = type;
        if (0 == valueLength)
        {
            type = PrimitiveType::NONE;
            return;
        }

        switch (type)
        {
            case PrimitiveType::CHAR:
                if (valueLength > 1)
                {
                    m_arrayValue = std::string(value, valueLength);
                    m_size = valueLength;
                }
                else
                {
                    m_value.asInt = *(char *) value;
                    m_size = 1;
                }
                break;

            case PrimitiveType::INT8:
                m_value.asInt = *(std::int8_t *)value;
                m_size = 1;
                break;

            case PrimitiveType::INT16:
                m_value.asInt = *(std::int16_t *)value;
                m_size = 2;
                break;

            case PrimitiveType::INT32:
                m_value.asInt = *(std::int32_t *)value;
                m_size = 4;
                break;

            case PrimitiveType::INT64:
                m_value.asInt = *(std::int64_t *)value;
                m_size = 8;
                break;

            case PrimitiveType::UINT8:
                m_value.asUInt = *(std::uint8_t *)value;
                m_size = 1;
                break;

            case PrimitiveType::UINT16:
                m_value.asUInt = *(std::uint16_t *)value;
                m_size = 2;
                break;

            case PrimitiveType::UINT32:
                m_value.asUInt = *(std::uint32_t *)value;
                m_size = 4;
                break;

            case PrimitiveType::UINT64:
                m_value.asUInt = *(std::uint64_t *)value;
                m_size = 8;
                break;

            case PrimitiveType::FLOAT:
                m_value.asDouble = *(float *)value;
                m_size = 4;
                break;

            case PrimitiveType::DOUBLE:
                m_value.asDouble = *(double *)value;
                m_size = 8;
                break;

            default:
                m_type = PrimitiveType::NONE;
                break;
        }
    }

    inline std::int64_t getAsInt() const
    {
        return m_value.asInt;
    }

    inline std::uint64_t getAsUInt() const
    {
        return m_value.asUInt;
    }

    inline double getAsDouble() const
    {
        return m_value.asDouble;
    }

    inline const char *getArray() const
    {
        return m_arrayValue.c_str(); // in C++11 data() and c_str() are equivalent and are null terminated after length
    }

    inline std::size_t size() const
    {
        return m_size;
    }

    inline PrimitiveType primitiveType() const
    {
        return m_type;
    }

private:
    PrimitiveType m_type;
    std::size_t m_size;
    union
    {
        std::int64_t asInt;
        std::uint64_t asUInt;
        double asDouble;
    } m_value;
    std::string m_arrayValue;  // use this to store all the types, not just char arrays
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
        return SBE_OTF_BYTE_ORDER_16(byteOrder, *(std::int16_t *)buffer);
    }

    static inline std::int32_t getInt32(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_32(byteOrder, *(std::int32_t *)buffer);
    }

    static inline std::int64_t getInt64(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_64(byteOrder, *(std::int64_t *)buffer);
    }

    static inline std::uint8_t getUInt8(const char *buffer)
    {
        return *(std::uint8_t *)buffer;
    }

    static inline std::uint16_t getUInt16(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_16(byteOrder, *(std::uint16_t *)buffer);
    }

    static inline std::uint32_t getUInt32(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_32(byteOrder, *(std::uint32_t *)buffer);
    }

    static inline std::uint64_t getUInt64(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_64(byteOrder, *(std::uint64_t *)buffer);
    }

    static inline float getFloat(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_32(byteOrder, *(float *)buffer);
    }

    static inline double getDouble(const char *buffer, const ByteOrder byteOrder)
    {
        return SBE_OTF_BYTE_ORDER_64(byteOrder, *(double *)buffer);
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

    inline Presence presence() const
    {
        return m_presence;
    }

    inline ByteOrder byteOrder() const
    {
        return m_byteOrder;
    }

    inline PrimitiveType primitiveType() const
    {
        return m_primitiveType;
    }

    inline std::int64_t getAsInt(const char *buffer) const
    {
        return getInt(m_primitiveType, m_byteOrder, buffer);
    }

    inline std::uint64_t getAsUInt(const char *buffer) const
    {
        return getUInt(m_primitiveType, m_byteOrder, buffer);
    }

    inline double getAsDouble(const char *buffer) const
    {
        return getDouble(m_primitiveType, m_byteOrder, buffer);
    }

    inline const PrimitiveValue& constValue() const
    {
        return m_constValue;
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
