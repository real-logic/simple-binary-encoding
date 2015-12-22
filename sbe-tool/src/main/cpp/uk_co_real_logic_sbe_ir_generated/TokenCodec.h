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
#endif

#include <sbe/sbe.h>

#include "ByteOrderCodec.h"
#include "SignalCodec.h"
#include "PresenceCodec.h"
#include "PrimitiveTypeCodec.h"
#include "VarDataEncoding.h"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class TokenCodec
{
private:
    char *buffer_;
    int bufferLength_;
    int *positionPtr_;
    int offset_;
    int position_;
    int actingBlockLength_;
    int actingVersion_;

    inline void reset(char *buffer, const int offset, const int bufferLength, const int actingBlockLength, const int actingVersion)
    {
        buffer_ = buffer;
        offset_ = offset;
        bufferLength_ = bufferLength;
        actingBlockLength_ = actingBlockLength;
        actingVersion_ = actingVersion;
        positionPtr_ = &position_;
        position(offset + actingBlockLength_);
    }

public:

    TokenCodec(void) : buffer_(NULL), bufferLength_(0), offset_(0) {}

    TokenCodec(char *buffer, const int bufferLength)
    {
        reset(buffer, 0, bufferLength, sbeBlockLength(), sbeSchemaVersion());
    }

    TokenCodec(char *buffer, const int bufferLength, const int actingBlockLength, const int actingVersion)
    {
        reset(buffer, 0, bufferLength, actingBlockLength, actingVersion);
    }

    TokenCodec(const TokenCodec& codec)
    {
        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_, codec.actingVersion_);
    }

#if __cplusplus >= 201103L
    TokenCodec(TokenCodec&& codec)
    {
        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_, codec.actingVersion_);
    }

    TokenCodec& operator=(TokenCodec&& codec)
    {
        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_, codec.actingVersion_);
        return *this;
    }

#endif

    TokenCodec& operator=(const TokenCodec& codec)
    {
        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_, codec.actingVersion_);
        return *this;
    }

    static const sbe_uint16_t sbeBlockLength(void)
    {
        return (sbe_uint16_t)24;
    }

    static const sbe_uint16_t sbeTemplateId(void)
    {
        return (sbe_uint16_t)2;
    }

    static const sbe_uint16_t sbeSchemaId(void)
    {
        return (sbe_uint16_t)1;
    }

    static const sbe_uint16_t sbeSchemaVersion(void)
    {
        return (sbe_uint16_t)0;
    }

    static const char *sbeSemanticType(void)
    {
        return "";
    }

    sbe_uint64_t offset(void) const
    {
        return offset_;
    }

    TokenCodec &wrapForEncode(char *buffer, const int offset, const int bufferLength)
    {
        reset(buffer, offset, bufferLength, sbeBlockLength(), sbeSchemaVersion());
        return *this;
    }

    TokenCodec &wrapForDecode(char *buffer, const int offset, const int actingBlockLength, const int actingVersion, const int bufferLength)
    {
        reset(buffer, offset, bufferLength, actingBlockLength, actingVersion);
        return *this;
    }

    sbe_uint64_t position(void) const
    {
        return position_;
    }

    void position(const int position)
    {
        if (SBE_BOUNDS_CHECK_EXPECT((position > bufferLength_), false))
        {
            throw std::runtime_error("buffer too short [E100]");
        }
        position_ = position;
    }

    int size(void) const
    {
        return position() - offset_;
    }

    char *buffer(void)
    {
        return buffer_;
    }

    int actingVersion(void) const
    {
        return actingVersion_;
    }

    static const int tokenOffsetId(void)
    {
        return 11;
    }

    static const int tokenOffsetSinceVersion(void)
    {
         return 0;
    }

    bool tokenOffsetInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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

    static const sbe_int32_t tokenOffsetNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const sbe_int32_t tokenOffsetMinValue()
    {
        return -2147483647;
    }

    static const sbe_int32_t tokenOffsetMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t tokenOffset(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 0)));
    }

    TokenCodec &tokenOffset(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const int tokenSizeId(void)
    {
        return 12;
    }

    static const int tokenSizeSinceVersion(void)
    {
         return 0;
    }

    bool tokenSizeInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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

    static const sbe_int32_t tokenSizeNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const sbe_int32_t tokenSizeMinValue()
    {
        return -2147483647;
    }

    static const sbe_int32_t tokenSizeMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t tokenSize(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 4)));
    }

    TokenCodec &tokenSize(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const int fieldIdId(void)
    {
        return 13;
    }

    static const int fieldIdSinceVersion(void)
    {
         return 0;
    }

    bool fieldIdInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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

    static const sbe_int32_t fieldIdNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const sbe_int32_t fieldIdMinValue()
    {
        return -2147483647;
    }

    static const sbe_int32_t fieldIdMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t fieldId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 8)));
    }

    TokenCodec &fieldId(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const int tokenVersionId(void)
    {
        return 14;
    }

    static const int tokenVersionSinceVersion(void)
    {
         return 0;
    }

    bool tokenVersionInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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

    static const sbe_int32_t tokenVersionNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const sbe_int32_t tokenVersionMinValue()
    {
        return -2147483647;
    }

    static const sbe_int32_t tokenVersionMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t tokenVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 12)));
    }

    TokenCodec &tokenVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 12)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const int componentTokenCountId(void)
    {
        return 15;
    }

    static const int componentTokenCountSinceVersion(void)
    {
         return 0;
    }

    bool componentTokenCountInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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

    static const sbe_int32_t componentTokenCountNullValue()
    {
        return SBE_NULLVALUE_INT32;
    }

    static const sbe_int32_t componentTokenCountMinValue()
    {
        return -2147483647;
    }

    static const sbe_int32_t componentTokenCountMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t componentTokenCount(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 16)));
    }

    TokenCodec &componentTokenCount(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 16)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const int signalId(void)
    {
        return 16;
    }

    static const int signalSinceVersion(void)
    {
         return 0;
    }

    bool signalInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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
        return SignalCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 20))));
    }

    TokenCodec &signal(const SignalCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 20)) = (value);
        return *this;
    }

    static const int primitiveTypeId(void)
    {
        return 17;
    }

    static const int primitiveTypeSinceVersion(void)
    {
         return 0;
    }

    bool primitiveTypeInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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
        return PrimitiveTypeCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 21))));
    }

    TokenCodec &primitiveType(const PrimitiveTypeCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 21)) = (value);
        return *this;
    }

    static const int byteOrderId(void)
    {
        return 18;
    }

    static const int byteOrderSinceVersion(void)
    {
         return 0;
    }

    bool byteOrderInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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
        return ByteOrderCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 22))));
    }

    TokenCodec &byteOrder(const ByteOrderCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 22)) = (value);
        return *this;
    }

    static const int presenceId(void)
    {
        return 19;
    }

    static const int presenceSinceVersion(void)
    {
         return 0;
    }

    bool presenceInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
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
        return PresenceCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 23))));
    }

    TokenCodec &presence(const PresenceCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 23)) = (value);
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

    static const int nameSinceVersion(void)
    {
         return 0;
    }

    bool nameInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int nameId(void)
    {
        return 20;
    }


    static const int nameHeaderSize()
    {
        return 1;
    }

    sbe_int64_t nameLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *name(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getName(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putName(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getNameAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putName(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int constValueSinceVersion(void)
    {
         return 0;
    }

    bool constValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int constValueId(void)
    {
        return 21;
    }


    static const int constValueHeaderSize()
    {
        return 1;
    }

    sbe_int64_t constValueLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *constValue(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getConstValue(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putConstValue(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getConstValueAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putConstValue(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int minValueSinceVersion(void)
    {
         return 0;
    }

    bool minValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int minValueId(void)
    {
        return 22;
    }


    static const int minValueHeaderSize()
    {
        return 1;
    }

    sbe_int64_t minValueLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *minValue(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getMinValue(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putMinValue(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getMinValueAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putMinValue(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int maxValueSinceVersion(void)
    {
         return 0;
    }

    bool maxValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int maxValueId(void)
    {
        return 23;
    }


    static const int maxValueHeaderSize()
    {
        return 1;
    }

    sbe_int64_t maxValueLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *maxValue(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getMaxValue(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putMaxValue(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getMaxValueAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putMaxValue(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int nullValueSinceVersion(void)
    {
         return 0;
    }

    bool nullValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int nullValueId(void)
    {
        return 24;
    }


    static const int nullValueHeaderSize()
    {
        return 1;
    }

    sbe_int64_t nullValueLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *nullValue(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getNullValue(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putNullValue(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getNullValueAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putNullValue(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int characterEncodingSinceVersion(void)
    {
         return 0;
    }

    bool characterEncodingInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int characterEncodingId(void)
    {
        return 25;
    }


    static const int characterEncodingHeaderSize()
    {
        return 1;
    }

    sbe_int64_t characterEncodingLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *characterEncoding(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getCharacterEncoding(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putCharacterEncoding(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getCharacterEncodingAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putCharacterEncoding(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int epochSinceVersion(void)
    {
         return 0;
    }

    bool epochInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int epochId(void)
    {
        return 26;
    }


    static const int epochHeaderSize()
    {
        return 1;
    }

    sbe_int64_t epochLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *epoch(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getEpoch(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putEpoch(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getEpochAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putEpoch(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int timeUnitSinceVersion(void)
    {
         return 0;
    }

    bool timeUnitInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int timeUnitId(void)
    {
        return 27;
    }


    static const int timeUnitHeaderSize()
    {
        return 1;
    }

    sbe_int64_t timeUnitLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *timeUnit(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getTimeUnit(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putTimeUnit(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getTimeUnitAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putTimeUnit(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int semanticTypeSinceVersion(void)
    {
         return 0;
    }

    bool semanticTypeInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int semanticTypeId(void)
    {
        return 28;
    }


    static const int semanticTypeHeaderSize()
    {
        return 1;
    }

    sbe_int64_t semanticTypeLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *semanticType(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getSemanticType(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putSemanticType(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getSemanticTypeAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putSemanticType(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
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

    static const int descriptionSinceVersion(void)
    {
         return 0;
    }

    bool descriptionInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static const int descriptionId(void)
    {
        return 29;
    }


    static const int descriptionHeaderSize()
    {
        return 1;
    }

    sbe_int64_t descriptionLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *description(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getDescription(char *dst, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)dataLength);
        ::memcpy(dst, buffer_ + pos, bytesToCopy);
        return bytesToCopy;
    }

    int putDescription(const char *src, const int length)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)length);
        ::memcpy(buffer_ + pos, src, length);
        return length;
    }

    const std::string getDescriptionAsString()
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        sbe_uint64_t pos = position();
        const std::string result(buffer_ + pos, dataLength);
        position(position() + (sbe_uint64_t)dataLength);
        return std::move(result);
    }

    TokenCodec &putDescription(const std::string& str)
    {
        sbe_uint64_t lengthOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + lengthOfLengthField);
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)str.length());
        sbe_uint64_t pos = position();
        position(position() + (sbe_uint64_t)str.length());
        ::memcpy(buffer_ + pos, str.c_str(), str.length());
        return *this;
    }
};
}
#endif
