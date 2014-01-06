/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _TOKENCODEC_HPP_
#define _TOKENCODEC_HPP_

/* math.h needed for NAN */
#include <math.h>
#include "sbe/sbe.hpp"

#include "uk_co_real_logic_sbe_ir_generated/VarDataEncoding.hpp"
#include "uk_co_real_logic_sbe_ir_generated/ByteOrderCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/PresenceCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/PrimitiveTypeCodec.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SignalCodec.hpp"

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

    static sbe_uint16_t blockLength(void)
    {
        return (sbe_uint16_t)20;
    }

    static sbe_uint16_t templateId(void)
    {
        return (sbe_uint16_t)2;
    }

    static sbe_uint8_t templateVersion(void)
    {
        return (sbe_uint8_t)0;
    }

    sbe_uint64_t offset(void) const
    {
        return offset_;
    }

    TokenCodec &wrapForEncode(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = blockLength();
        actingVersion_ = templateVersion();
        position(offset + actingBlockLength_);
        positionPtr_ = &position_;
        return *this;
    }

    TokenCodec &wrapForDecode(char *buffer, const int offset,
                        const int actingBlockLength, const int actingVersion)
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

    static int tokenOffsetSchemaId(void)
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

    static sbe_int32_t tokenOffsetNullVal()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenOffsetMinVal()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenOffsetMaxVal()
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

    static int tokenSizeSchemaId(void)
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

    static sbe_int32_t tokenSizeNullVal()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenSizeMinVal()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenSizeMaxVal()
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

    static int schemaIdSchemaId(void)
    {
        return 13;
    }

    static int schemaIdSinceVersion(void)
    {
         return 0;
    }

    bool schemaIdInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }


    static const char *schemaIdMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static sbe_int32_t schemaIdNullVal()
    {
        return -2147483648;
    }

    static sbe_int32_t schemaIdMinVal()
    {
        return -2147483647;
    }

    static sbe_int32_t schemaIdMaxVal()
    {
        return 2147483647;
    }

    sbe_int32_t schemaId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 8)));
    }

    TokenCodec &schemaId(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static int tokenVersionSchemaId(void)
    {
        return 17;
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

    static sbe_int32_t tokenVersionNullVal()
    {
        return -2147483648;
    }

    static sbe_int32_t tokenVersionMinVal()
    {
        return -2147483647;
    }

    static sbe_int32_t tokenVersionMaxVal()
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

    static int signalSchemaId(void)
    {
        return 14;
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

    static int primitiveTypeSchemaId(void)
    {
        return 15;
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

    static int byteOrderSchemaId(void)
    {
        return 16;
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

    static int presenceSchemaId(void)
    {
        return 17;
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

    static int nameSchemaId(void)
    {
        return 18;
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

    static const char *constValCharacterEncoding()
    {
        return "UTF-8";
    }

    static int constValSinceVersion(void)
    {
         return 0;
    }

    bool constValInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int constValSchemaId(void)
    {
        return 19;
    }


    static const char *constValMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t constValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *constVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getConstVal(char *dst, const int length)
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

    int putConstVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }

    static const char *minValCharacterEncoding()
    {
        return "UTF-8";
    }

    static int minValSinceVersion(void)
    {
         return 0;
    }

    bool minValInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int minValSchemaId(void)
    {
        return 20;
    }


    static const char *minValMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t minValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *minVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getMinVal(char *dst, const int length)
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

    int putMinVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }

    static const char *maxValCharacterEncoding()
    {
        return "UTF-8";
    }

    static int maxValSinceVersion(void)
    {
         return 0;
    }

    bool maxValInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int maxValSchemaId(void)
    {
        return 21;
    }


    static const char *maxValMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t maxValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *maxVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getMaxVal(char *dst, const int length)
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

    int putMaxVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }

    static const char *nullValCharacterEncoding()
    {
        return "UTF-8";
    }

    static int nullValSinceVersion(void)
    {
         return 0;
    }

    bool nullValInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int nullValSchemaId(void)
    {
        return 22;
    }


    static const char *nullValMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t nullValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *nullVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getNullVal(char *dst, const int length)
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

    int putNullVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
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

    static int characterEncodingSchemaId(void)
    {
        return 23;
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

    static int epochSchemaId(void)
    {
        return 24;
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

    static int timeUnitSchemaId(void)
    {
        return 25;
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

    static int semanticTypeSchemaId(void)
    {
        return 26;
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
