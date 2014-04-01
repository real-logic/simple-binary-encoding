/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _TOKENCODEC_HPP_
#define _TOKENCODEC_HPP_

/* math.h needed for NAN */
#include <math.h>
#include "sbe/sbe.hpp"

#include "uk_co_real_logic_sbe_ir_generated/ByteOrderCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SignalCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/PresenceCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/PrimitiveTypeCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/VarDataEncoding.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class TokenCodec
{
private:
    char *buffer_;
    int *positionPtr_;
    int offset_;
    int position_;
    int actingBlockLength_;
    int actingVersion_;

public:

    static sbe_uint16_t sbeBlockLength(void)
    {
        return (sbe_uint16_t)20;
    }

    static sbe_uint16_t sbeTemplateId(void)
    {
        return (sbe_uint16_t)2;
    }

    static sbe_uint16_t sbeSchemaId(void)
    {
        return (sbe_uint16_t)0;
    }

    static sbe_uint16_t sbeSchemaVersion(void)
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

    TokenCodec &wrapForEncode(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = sbeBlockLength();
        actingVersion_ = sbeSchemaVersion();
        position(offset + actingBlockLength_);
        positionPtr_ = &position_;
        return *this;
    }

    TokenCodec &wrapForDecode(char *buffer, const int offset, const int actingBlockLength, const int actingVersion)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = actingBlockLength;
        actingVersion_ = actingVersion;
        positionPtr_ = &position_;
        position(offset + actingBlockLength_);
        return *this;
    }

    sbe_uint64_t position(void) const
    {
        return position_;
    }

    void position(const sbe_uint64_t position)
    {
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

    static int tokenOffsetId(void)
    {
        return 11;
    }

    static int tokenOffsetSinceVersion(void)
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

    static sbe_int32_t tokenOffsetNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenOffsetMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenOffsetMaxValue()
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

    static int tokenSizeId(void)
    {
        return 12;
    }

    static int tokenSizeSinceVersion(void)
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

    static sbe_int32_t tokenSizeNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenSizeMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenSizeMaxValue()
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

    static int fieldIdId(void)
    {
        return 13;
    }

    static int fieldIdSinceVersion(void)
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

    static sbe_int32_t fieldIdNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t fieldIdMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t fieldIdMaxValue()
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

    static int tokenVersionId(void)
    {
        return 14;
    }

    static int tokenVersionSinceVersion(void)
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

    static sbe_int32_t tokenVersionNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenVersionMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenVersionMaxValue()
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

    static int signalId(void)
    {
        return 15;
    }

    static int signalSinceVersion(void)
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
        return SignalCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 16))));
    }

    TokenCodec &signal(const SignalCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 16)) = (value);
        return *this;
    }

    static int primitiveTypeId(void)
    {
        return 16;
    }

    static int primitiveTypeSinceVersion(void)
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
        return PrimitiveTypeCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 17))));
    }

    TokenCodec &primitiveType(const PrimitiveTypeCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 17)) = (value);
        return *this;
    }

    static int byteOrderId(void)
    {
        return 17;
    }

    static int byteOrderSinceVersion(void)
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
        return ByteOrderCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 18))));
    }

    TokenCodec &byteOrder(const ByteOrderCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 18)) = (value);
        return *this;
    }

    static int presenceId(void)
    {
        return 18;
    }

    static int presenceSinceVersion(void)
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
        return PresenceCodec::get((*((sbe_uint8_t *)(buffer_ + offset_ + 19))));
    }

    TokenCodec &presence(const PresenceCodec::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 19)) = (value);
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

    static int nameSinceVersion(void)
    {
         return 0;
    }

    bool nameInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int nameId(void)
    {
        return 19;
    }


    static int nameHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putName(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int constValueSinceVersion(void)
    {
         return 0;
    }

    bool constValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int constValueId(void)
    {
        return 20;
    }


    static int constValueHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putConstValue(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int minValueSinceVersion(void)
    {
         return 0;
    }

    bool minValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int minValueId(void)
    {
        return 21;
    }


    static int minValueHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putMinValue(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int maxValueSinceVersion(void)
    {
         return 0;
    }

    bool maxValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int maxValueId(void)
    {
        return 22;
    }


    static int maxValueHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putMaxValue(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int nullValueSinceVersion(void)
    {
         return 0;
    }

    bool nullValueInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int nullValueId(void)
    {
        return 23;
    }


    static int nullValueHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putNullValue(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int characterEncodingSinceVersion(void)
    {
         return 0;
    }

    bool characterEncodingInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int characterEncodingId(void)
    {
        return 24;
    }


    static int characterEncodingHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putCharacterEncoding(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int epochSinceVersion(void)
    {
         return 0;
    }

    bool epochInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int epochId(void)
    {
        return 25;
    }


    static int epochHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putEpoch(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int timeUnitSinceVersion(void)
    {
         return 0;
    }

    bool timeUnitInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int timeUnitId(void)
    {
        return 26;
    }


    static int timeUnitHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putTimeUnit(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int semanticTypeSinceVersion(void)
    {
         return 0;
    }

    bool semanticTypeInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int semanticTypeId(void)
    {
        return 27;
    }


    static int semanticTypeHeaderSize()
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
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_int64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    }

    int putSemanticType(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }
};
}
#endif
