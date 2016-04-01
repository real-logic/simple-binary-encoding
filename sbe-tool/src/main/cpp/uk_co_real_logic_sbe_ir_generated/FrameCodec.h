/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_FRAMECODEC_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_FRAMECODEC_H_

#if defined(SBE_HAVE_CMATH)
/* cmath needed for std::numeric_limits<double>::quiet_NaN() */
#  include <cmath>
#  define SBE_FLOAT_NAN std::numeric_limits<float>::quiet_NaN()
#  define SBE_DOUBLE_NAN std::numeric_limits<double>::quiet_NaN()
#else
/* math.h needed for NAN */
#  include <math.h>
#  define SBE_FLOAT_NAN NAN
#  define SBE_DOUBLE_NAN NAN
#endif

#if __cplusplus >= 201103L
#  include <cstdint>
#  include <functional>
#  include <string>
#  include <cstring>
#endif

#include <sbe/sbe.h>

#include "ByteOrderCodec.h"
#include "SignalCodec.h"
#include "PresenceCodec.h"
#include "PrimitiveTypeCodec.h"
#include "VarDataEncoding.h"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class FrameCodec
{
private:
    char *m_buffer;
    std::uint64_t m_bufferLength;
    std::uint64_t *m_positionPtr;
    std::uint64_t m_offset;
    std::uint64_t m_position;
    std::uint64_t m_actingBlockLength;
    std::uint64_t m_actingVersion;

    inline void reset(
        char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength,
        const std::uint64_t actingBlockLength, const std::uint64_t actingVersion)
    {
        m_buffer = buffer;
        m_offset = offset;
        m_bufferLength = bufferLength;
        m_actingBlockLength = actingBlockLength;
        m_actingVersion = actingVersion;
        m_positionPtr = &m_position;
        position(offset + m_actingBlockLength);
    }

public:

    FrameCodec(void) : m_buffer(nullptr), m_bufferLength(0), m_offset(0) {}

    FrameCodec(char *buffer, const std::uint64_t bufferLength)
    {
        reset(buffer, 0, bufferLength, sbeBlockLength(), sbeSchemaVersion());
    }

    FrameCodec(char *buffer, const std::uint64_t bufferLength, const std::uint64_t actingBlockLength, const std::uint64_t actingVersion)
    {
        reset(buffer, 0, bufferLength, actingBlockLength, actingVersion);
    }

    FrameCodec(const FrameCodec& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
    }

#if __cplusplus >= 201103L
    FrameCodec(FrameCodec&& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
    }

    FrameCodec& operator=(FrameCodec&& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
        return *this;
    }

#endif

    FrameCodec& operator=(const FrameCodec& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
        return *this;
    }

    static const std::uint16_t sbeBlockLength(void)
    {
        return (std::uint16_t)12;
    }

    static const std::uint16_t sbeTemplateId(void)
    {
        return (std::uint16_t)1;
    }

    static const std::uint16_t sbeSchemaId(void)
    {
        return (std::uint16_t)1;
    }

    static const std::uint16_t sbeSchemaVersion(void)
    {
        return (std::uint16_t)0;
    }

    static const char *sbeSemanticType(void)
    {
        return "";
    }

    std::uint64_t offset(void) const
    {
        return m_offset;
    }

    FrameCodec &wrapForEncode(char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength)
    {
        reset(buffer, offset, bufferLength, sbeBlockLength(), sbeSchemaVersion());
        return *this;
    }

    FrameCodec &wrapForDecode(
         char *buffer, const std::uint64_t offset, const std::uint64_t actingBlockLength,
         const std::uint64_t actingVersion, const std::uint64_t bufferLength)
    {
        reset(buffer, offset, bufferLength, actingBlockLength, actingVersion);
        return *this;
    }

    std::uint64_t position(void) const
    {
        return m_position;
    }

    void position(const std::uint64_t position)
    {
        if (SBE_BOUNDS_CHECK_EXPECT((position > m_bufferLength), false))
        {
            throw std::runtime_error("buffer too short [E100]");
        }
        m_position = position;
    }

    std::uint64_t encodedLength(void) const
    {
        return position() - m_offset;
    }

    char *buffer(void)
    {
        return m_buffer;
    }

    std::uint64_t actingVersion(void) const
    {
        return m_actingVersion;
    }

    static const std::uint16_t irIdId(void)
    {
        return 1;
    }

    static const std::uint64_t irIdSinceVersion(void)
    {
         return 0;
    }

    bool irIdInActingVersion(void)
    {
        return (m_actingVersion >= irIdSinceVersion()) ? true : false;
    }


    static const char *irIdMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t irIdNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t irIdMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t irIdMaxValue()
    {
        return 2147483647;
    }

    std::int32_t irId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 0)));
    }

    FrameCodec &irId(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t irVersionId(void)
    {
        return 2;
    }

    static const std::uint64_t irVersionSinceVersion(void)
    {
         return 0;
    }

    bool irVersionInActingVersion(void)
    {
        return (m_actingVersion >= irVersionSinceVersion()) ? true : false;
    }


    static const char *irVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t irVersionNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t irVersionMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t irVersionMaxValue()
    {
        return 2147483647;
    }

    std::int32_t irVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 4)));
    }

    FrameCodec &irVersion(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t schemaVersionId(void)
    {
        return 3;
    }

    static const std::uint64_t schemaVersionSinceVersion(void)
    {
         return 0;
    }

    bool schemaVersionInActingVersion(void)
    {
        return (m_actingVersion >= schemaVersionSinceVersion()) ? true : false;
    }


    static const char *schemaVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t schemaVersionNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t schemaVersionMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t schemaVersionMaxValue()
    {
        return 2147483647;
    }

    std::int32_t schemaVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 8)));
    }

    FrameCodec &schemaVersion(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const char *packageNameMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *packageNameCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t packageNameSinceVersion(void)
    {
         return 0;
    }

    bool packageNameInActingVersion(void)
    {
        return (m_actingVersion >= packageNameSinceVersion()) ? true : false;
    }

    static const std::uint16_t packageNameId(void)
    {
        return 4;
    }


    static const std::uint64_t packageNameHeaderLength()
    {
        return 2;
    }

    std::uint16_t packageNameLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *packageName(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getPackageName(char *dst, const std::uint64_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t bytesToCopy = (length < dataLength) ? length : dataLength;
        std::uint64_t pos = position();
        position(position() + dataLength);
        std::memcpy(dst, m_buffer + pos, bytesToCopy);
        return bytesToCopy;
    }

    FrameCodec &putPackageName(const char *src, const std::uint16_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16(length);
        std::uint64_t pos = position();
        position(position() + length);
        std::memcpy(m_buffer + pos, src, length);
        return *this;
    }

    const std::string getPackageNameAsString()
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t pos = position();
        const std::string result(m_buffer + pos, dataLength);
        position(position() + dataLength);
        return result;
    }

    FrameCodec &putPackageName(const std::string& str)
    {
        if (str.length() > 65534)
        {
             throw std::runtime_error("std::string length too long for length type [E109]");
        }
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16((std::uint16_t)str.length());
        std::uint64_t pos = position();
        position(position() + str.length());
        std::memcpy(m_buffer + pos, str.c_str(), str.length());
        return *this;
    }

    static const char *namespaceNameMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *namespaceNameCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t namespaceNameSinceVersion(void)
    {
         return 0;
    }

    bool namespaceNameInActingVersion(void)
    {
        return (m_actingVersion >= namespaceNameSinceVersion()) ? true : false;
    }

    static const std::uint16_t namespaceNameId(void)
    {
        return 5;
    }


    static const std::uint64_t namespaceNameHeaderLength()
    {
        return 2;
    }

    std::uint16_t namespaceNameLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *namespaceName(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getNamespaceName(char *dst, const std::uint64_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t bytesToCopy = (length < dataLength) ? length : dataLength;
        std::uint64_t pos = position();
        position(position() + dataLength);
        std::memcpy(dst, m_buffer + pos, bytesToCopy);
        return bytesToCopy;
    }

    FrameCodec &putNamespaceName(const char *src, const std::uint16_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16(length);
        std::uint64_t pos = position();
        position(position() + length);
        std::memcpy(m_buffer + pos, src, length);
        return *this;
    }

    const std::string getNamespaceNameAsString()
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t pos = position();
        const std::string result(m_buffer + pos, dataLength);
        position(position() + dataLength);
        return result;
    }

    FrameCodec &putNamespaceName(const std::string& str)
    {
        if (str.length() > 65534)
        {
             throw std::runtime_error("std::string length too long for length type [E109]");
        }
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16((std::uint16_t)str.length());
        std::uint64_t pos = position();
        position(position() + str.length());
        std::memcpy(m_buffer + pos, str.c_str(), str.length());
        return *this;
    }

    static const char *semanticVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *semanticVersionCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t semanticVersionSinceVersion(void)
    {
         return 0;
    }

    bool semanticVersionInActingVersion(void)
    {
        return (m_actingVersion >= semanticVersionSinceVersion()) ? true : false;
    }

    static const std::uint16_t semanticVersionId(void)
    {
        return 6;
    }


    static const std::uint64_t semanticVersionHeaderLength()
    {
        return 2;
    }

    std::uint16_t semanticVersionLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *semanticVersion(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getSemanticVersion(char *dst, const std::uint64_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t bytesToCopy = (length < dataLength) ? length : dataLength;
        std::uint64_t pos = position();
        position(position() + dataLength);
        std::memcpy(dst, m_buffer + pos, bytesToCopy);
        return bytesToCopy;
    }

    FrameCodec &putSemanticVersion(const char *src, const std::uint16_t length)
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16(length);
        std::uint64_t pos = position();
        position(position() + length);
        std::memcpy(m_buffer + pos, src, length);
        return *this;
    }

    const std::string getSemanticVersionAsString()
    {
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        std::uint64_t dataLength = SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + lengthPosition)));
        std::uint64_t pos = position();
        const std::string result(m_buffer + pos, dataLength);
        position(position() + dataLength);
        return result;
    }

    FrameCodec &putSemanticVersion(const std::string& str)
    {
        if (str.length() > 65534)
        {
             throw std::runtime_error("std::string length too long for length type [E109]");
        }
        std::uint64_t lengthOfLengthField = 2;
        std::uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((std::uint16_t *)(m_buffer + lengthPosition)) = SBE_LITTLE_ENDIAN_ENCODE_16((std::uint16_t)str.length());
        std::uint64_t pos = position();
        position(position() + str.length());
        std::memcpy(m_buffer + pos, str.c_str(), str.length());
        return *this;
    }
};
}
#endif
