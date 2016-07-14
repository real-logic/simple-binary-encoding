/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_TOKENCODEC_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_TOKENCODEC_H_

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

#if __cplusplus >= 201103L
#  define SBE_CONST_KIND constexpr
#else
#  define SBE_CONST_KIND const
#endif

#include <sbe/sbe.h>

#include "ByteOrderCodec.h"
#include "SignalCodec.h"
#include "PresenceCodec.h"
#include "PrimitiveTypeCodec.h"
#include "VarDataEncoding.h"

using namespace sbe;

namespace uk {
namespace co {
namespace real_logic {
namespace sbe {
namespace ir {
namespace generated {

class TokenCodec
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

    TokenCodec(void) : m_buffer(nullptr), m_bufferLength(0), m_offset(0) {}

    TokenCodec(char *buffer, const std::uint64_t bufferLength)
    {
        reset(buffer, 0, bufferLength, sbeBlockLength(), sbeSchemaVersion());
    }

    TokenCodec(char *buffer, const std::uint64_t bufferLength, const std::uint64_t actingBlockLength, const std::uint64_t actingVersion)
    {
        reset(buffer, 0, bufferLength, actingBlockLength, actingVersion);
    }

    TokenCodec(const TokenCodec& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
    }

#if __cplusplus >= 201103L
    TokenCodec(TokenCodec&& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
    }

    TokenCodec& operator=(TokenCodec&& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
        return *this;
    }

#endif

    TokenCodec& operator=(const TokenCodec& codec)
    {
        reset(codec.m_buffer, codec.m_offset, codec.m_bufferLength, codec.m_actingBlockLength, codec.m_actingVersion);
        return *this;
    }

    static SBE_CONST_KIND std::uint16_t SbeBlockLength{(std::uint16_t)24};
    static const std::uint16_t sbeBlockLength(void)
    {
        return SbeBlockLength;
    }

    static SBE_CONST_KIND std::uint16_t SbeTemplateId{(std::uint16_t)2};
    static const std::uint16_t sbeTemplateId(void)
    {
        return SbeTemplateId;
    }

    static SBE_CONST_KIND std::uint16_t SbeSchemaId{(std::uint16_t)1};
    static const std::uint16_t sbeSchemaId(void)
    {
        return SbeSchemaId;
    }

    static SBE_CONST_KIND std::uint16_t SbeSchemaVersion{(std::uint16_t)0};
    static const std::uint16_t sbeSchemaVersion(void)
    {
        return SbeSchemaVersion;
    }

    static SBE_CONST_KIND char SbeSemanticType[] = "";
    static const char *sbeSemanticType(void)
    {
        return SbeSemanticType;
    }

    std::uint64_t offset(void) const
    {
        return m_offset;
    }

    TokenCodec &wrapForEncode(char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength)
    {
        reset(buffer, offset, bufferLength, sbeBlockLength(), sbeSchemaVersion());
        return *this;
    }

    TokenCodec &wrapForDecode(
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

    static const std::uint16_t tokenOffsetId(void)
    {
        return 11;
    }

    static const std::uint64_t tokenOffsetSinceVersion(void)
    {
         return 0;
    }

    bool tokenOffsetInActingVersion(void)
    {
        return (m_actingVersion >= tokenOffsetSinceVersion()) ? true : false;
    }


    static const char *tokenOffsetMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t tokenOffsetNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t tokenOffsetMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t tokenOffsetMaxValue()
    {
        return 2147483647;
    }

    std::int32_t tokenOffset(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 0)));
    }

    TokenCodec &tokenOffset(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t tokenSizeId(void)
    {
        return 12;
    }

    static const std::uint64_t tokenSizeSinceVersion(void)
    {
         return 0;
    }

    bool tokenSizeInActingVersion(void)
    {
        return (m_actingVersion >= tokenSizeSinceVersion()) ? true : false;
    }


    static const char *tokenSizeMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t tokenSizeNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t tokenSizeMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t tokenSizeMaxValue()
    {
        return 2147483647;
    }

    std::int32_t tokenSize(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 4)));
    }

    TokenCodec &tokenSize(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t fieldIdId(void)
    {
        return 13;
    }

    static const std::uint64_t fieldIdSinceVersion(void)
    {
         return 0;
    }

    bool fieldIdInActingVersion(void)
    {
        return (m_actingVersion >= fieldIdSinceVersion()) ? true : false;
    }


    static const char *fieldIdMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t fieldIdNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t fieldIdMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t fieldIdMaxValue()
    {
        return 2147483647;
    }

    std::int32_t fieldId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 8)));
    }

    TokenCodec &fieldId(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t tokenVersionId(void)
    {
        return 14;
    }

    static const std::uint64_t tokenVersionSinceVersion(void)
    {
         return 0;
    }

    bool tokenVersionInActingVersion(void)
    {
        return (m_actingVersion >= tokenVersionSinceVersion()) ? true : false;
    }


    static const char *tokenVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t tokenVersionNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t tokenVersionMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t tokenVersionMaxValue()
    {
        return 2147483647;
    }

    std::int32_t tokenVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 12)));
    }

    TokenCodec &tokenVersion(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 12)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t componentTokenCountId(void)
    {
        return 15;
    }

    static const std::uint64_t componentTokenCountSinceVersion(void)
    {
         return 0;
    }

    bool componentTokenCountInActingVersion(void)
    {
        return (m_actingVersion >= componentTokenCountSinceVersion()) ? true : false;
    }


    static const char *componentTokenCountMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const std::int32_t componentTokenCountNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const std::int32_t componentTokenCountMinValue()
    {
        return -2147483647;
    }

    static const std::int32_t componentTokenCountMaxValue()
    {
        return 2147483647;
    }

    std::int32_t componentTokenCount(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((std::int32_t *)(m_buffer + m_offset + 16)));
    }

    TokenCodec &componentTokenCount(const std::int32_t value)
    {
        *((std::int32_t *)(m_buffer + m_offset + 16)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const std::uint16_t signalId(void)
    {
        return 16;
    }

    static const std::uint64_t signalSinceVersion(void)
    {
         return 0;
    }

    bool signalInActingVersion(void)
    {
        return (m_actingVersion >= signalSinceVersion()) ? true : false;
    }


    static const char *signalMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    SignalCodec::Value signal(void) const
    {
        return SignalCodec::get((*((std::uint8_t *)(m_buffer + m_offset + 20))));
    }

    TokenCodec &signal(const SignalCodec::Value value)
    {
        *((std::uint8_t *)(m_buffer + m_offset + 20)) = (value);
        return *this;
    }

    static const std::uint16_t primitiveTypeId(void)
    {
        return 17;
    }

    static const std::uint64_t primitiveTypeSinceVersion(void)
    {
         return 0;
    }

    bool primitiveTypeInActingVersion(void)
    {
        return (m_actingVersion >= primitiveTypeSinceVersion()) ? true : false;
    }


    static const char *primitiveTypeMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    PrimitiveTypeCodec::Value primitiveType(void) const
    {
        return PrimitiveTypeCodec::get((*((std::uint8_t *)(m_buffer + m_offset + 21))));
    }

    TokenCodec &primitiveType(const PrimitiveTypeCodec::Value value)
    {
        *((std::uint8_t *)(m_buffer + m_offset + 21)) = (value);
        return *this;
    }

    static const std::uint16_t byteOrderId(void)
    {
        return 18;
    }

    static const std::uint64_t byteOrderSinceVersion(void)
    {
         return 0;
    }

    bool byteOrderInActingVersion(void)
    {
        return (m_actingVersion >= byteOrderSinceVersion()) ? true : false;
    }


    static const char *byteOrderMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    ByteOrderCodec::Value byteOrder(void) const
    {
        return ByteOrderCodec::get((*((std::uint8_t *)(m_buffer + m_offset + 22))));
    }

    TokenCodec &byteOrder(const ByteOrderCodec::Value value)
    {
        *((std::uint8_t *)(m_buffer + m_offset + 22)) = (value);
        return *this;
    }

    static const std::uint16_t presenceId(void)
    {
        return 19;
    }

    static const std::uint64_t presenceSinceVersion(void)
    {
         return 0;
    }

    bool presenceInActingVersion(void)
    {
        return (m_actingVersion >= presenceSinceVersion()) ? true : false;
    }


    static const char *presenceMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    PresenceCodec::Value presence(void) const
    {
        return PresenceCodec::get((*((std::uint8_t *)(m_buffer + m_offset + 23))));
    }

    TokenCodec &presence(const PresenceCodec::Value value)
    {
        *((std::uint8_t *)(m_buffer + m_offset + 23)) = (value);
        return *this;
    }

    static const char *nameMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *nameCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t nameSinceVersion(void)
    {
         return 0;
    }

    bool nameInActingVersion(void)
    {
        return (m_actingVersion >= nameSinceVersion()) ? true : false;
    }

    static const std::uint16_t nameId(void)
    {
        return 20;
    }


    static const std::uint64_t nameHeaderLength()
    {
        return 2;
    }

    std::uint16_t nameLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *name(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getName(char *dst, const std::uint64_t length)
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

    TokenCodec &putName(const char *src, const std::uint16_t length)
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

    const std::string getNameAsString()
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

    TokenCodec &putName(const std::string& str)
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

    static const char *constValueMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *constValueCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t constValueSinceVersion(void)
    {
         return 0;
    }

    bool constValueInActingVersion(void)
    {
        return (m_actingVersion >= constValueSinceVersion()) ? true : false;
    }

    static const std::uint16_t constValueId(void)
    {
        return 21;
    }


    static const std::uint64_t constValueHeaderLength()
    {
        return 2;
    }

    std::uint16_t constValueLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *constValue(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getConstValue(char *dst, const std::uint64_t length)
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

    TokenCodec &putConstValue(const char *src, const std::uint16_t length)
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

    const std::string getConstValueAsString()
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

    TokenCodec &putConstValue(const std::string& str)
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

    static const char *minValueMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *minValueCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t minValueSinceVersion(void)
    {
         return 0;
    }

    bool minValueInActingVersion(void)
    {
        return (m_actingVersion >= minValueSinceVersion()) ? true : false;
    }

    static const std::uint16_t minValueId(void)
    {
        return 22;
    }


    static const std::uint64_t minValueHeaderLength()
    {
        return 2;
    }

    std::uint16_t minValueLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *minValue(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getMinValue(char *dst, const std::uint64_t length)
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

    TokenCodec &putMinValue(const char *src, const std::uint16_t length)
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

    const std::string getMinValueAsString()
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

    TokenCodec &putMinValue(const std::string& str)
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

    static const char *maxValueMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *maxValueCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t maxValueSinceVersion(void)
    {
         return 0;
    }

    bool maxValueInActingVersion(void)
    {
        return (m_actingVersion >= maxValueSinceVersion()) ? true : false;
    }

    static const std::uint16_t maxValueId(void)
    {
        return 23;
    }


    static const std::uint64_t maxValueHeaderLength()
    {
        return 2;
    }

    std::uint16_t maxValueLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *maxValue(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getMaxValue(char *dst, const std::uint64_t length)
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

    TokenCodec &putMaxValue(const char *src, const std::uint16_t length)
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

    const std::string getMaxValueAsString()
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

    TokenCodec &putMaxValue(const std::string& str)
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

    static const char *nullValueMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *nullValueCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t nullValueSinceVersion(void)
    {
         return 0;
    }

    bool nullValueInActingVersion(void)
    {
        return (m_actingVersion >= nullValueSinceVersion()) ? true : false;
    }

    static const std::uint16_t nullValueId(void)
    {
        return 24;
    }


    static const std::uint64_t nullValueHeaderLength()
    {
        return 2;
    }

    std::uint16_t nullValueLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *nullValue(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getNullValue(char *dst, const std::uint64_t length)
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

    TokenCodec &putNullValue(const char *src, const std::uint16_t length)
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

    const std::string getNullValueAsString()
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

    TokenCodec &putNullValue(const std::string& str)
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

    static const char *characterEncodingMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *characterEncodingCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t characterEncodingSinceVersion(void)
    {
         return 0;
    }

    bool characterEncodingInActingVersion(void)
    {
        return (m_actingVersion >= characterEncodingSinceVersion()) ? true : false;
    }

    static const std::uint16_t characterEncodingId(void)
    {
        return 25;
    }


    static const std::uint64_t characterEncodingHeaderLength()
    {
        return 2;
    }

    std::uint16_t characterEncodingLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *characterEncoding(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getCharacterEncoding(char *dst, const std::uint64_t length)
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

    TokenCodec &putCharacterEncoding(const char *src, const std::uint16_t length)
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

    const std::string getCharacterEncodingAsString()
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

    TokenCodec &putCharacterEncoding(const std::string& str)
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

    static const char *epochMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *epochCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t epochSinceVersion(void)
    {
         return 0;
    }

    bool epochInActingVersion(void)
    {
        return (m_actingVersion >= epochSinceVersion()) ? true : false;
    }

    static const std::uint16_t epochId(void)
    {
        return 26;
    }


    static const std::uint64_t epochHeaderLength()
    {
        return 2;
    }

    std::uint16_t epochLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *epoch(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getEpoch(char *dst, const std::uint64_t length)
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

    TokenCodec &putEpoch(const char *src, const std::uint16_t length)
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

    const std::string getEpochAsString()
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

    TokenCodec &putEpoch(const std::string& str)
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

    static const char *timeUnitMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *timeUnitCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t timeUnitSinceVersion(void)
    {
         return 0;
    }

    bool timeUnitInActingVersion(void)
    {
        return (m_actingVersion >= timeUnitSinceVersion()) ? true : false;
    }

    static const std::uint16_t timeUnitId(void)
    {
        return 27;
    }


    static const std::uint64_t timeUnitHeaderLength()
    {
        return 2;
    }

    std::uint16_t timeUnitLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *timeUnit(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getTimeUnit(char *dst, const std::uint64_t length)
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

    TokenCodec &putTimeUnit(const char *src, const std::uint16_t length)
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

    const std::string getTimeUnitAsString()
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

    TokenCodec &putTimeUnit(const std::string& str)
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

    static const char *semanticTypeMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *semanticTypeCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t semanticTypeSinceVersion(void)
    {
         return 0;
    }

    bool semanticTypeInActingVersion(void)
    {
        return (m_actingVersion >= semanticTypeSinceVersion()) ? true : false;
    }

    static const std::uint16_t semanticTypeId(void)
    {
        return 28;
    }


    static const std::uint64_t semanticTypeHeaderLength()
    {
        return 2;
    }

    std::uint16_t semanticTypeLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *semanticType(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getSemanticType(char *dst, const std::uint64_t length)
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

    TokenCodec &putSemanticType(const char *src, const std::uint16_t length)
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

    const std::string getSemanticTypeAsString()
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

    TokenCodec &putSemanticType(const std::string& str)
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

    static const char *descriptionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static const char *descriptionCharacterEncoding()
    {
        return "UTF-8";
    }

    static const std::uint64_t descriptionSinceVersion(void)
    {
         return 0;
    }

    bool descriptionInActingVersion(void)
    {
        return (m_actingVersion >= descriptionSinceVersion()) ? true : false;
    }

    static const std::uint16_t descriptionId(void)
    {
        return 29;
    }


    static const std::uint64_t descriptionHeaderLength()
    {
        return 2;
    }

    std::uint16_t descriptionLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((std::uint16_t *)(m_buffer + position())));
    }

    const char *description(void)
    {
         const char *fieldPtr = (m_buffer + position() + 2);
         position(position() + 2 + *((std::uint16_t *)(m_buffer + position())));
         return fieldPtr;
    }

    std::uint64_t getDescription(char *dst, const std::uint64_t length)
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

    TokenCodec &putDescription(const char *src, const std::uint16_t length)
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

    const std::string getDescriptionAsString()
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

    TokenCodec &putDescription(const std::string& str)
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
SBE_CONST_KIND std::uint16_t TokenCodec::SbeBlockLength;
SBE_CONST_KIND std::uint16_t TokenCodec::SbeTemplateId;
SBE_CONST_KIND std::uint16_t TokenCodec::SbeSchemaId;
SBE_CONST_KIND std::uint16_t TokenCodec::SbeSchemaVersion;
SBE_CONST_KIND char TokenCodec::SbeSemanticType[];
};
};
};
};
};
};
#endif
