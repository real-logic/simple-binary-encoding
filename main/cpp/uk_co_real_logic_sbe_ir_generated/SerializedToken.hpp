/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _SERIALIZEDTOKEN_HPP_
#define _SERIALIZEDTOKEN_HPP_

#include "sbe/sbe.hpp"

#include "uk_co_real_logic_sbe_ir_generated/VarDataEncoding.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedSignal.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedPrimitiveType.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedByteOrder.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class SerializedToken : public MessageFlyweight
{
private:
    char *buffer_;
    int offset_;
    int position_;
    int actingBlockLength_;
    int actingVersion_;

public:

    sbe_uint64_t blockLength(void) const
    {
        return 19;
    };

    sbe_uint64_t offset(void) const
    {
        return offset_;
    };

    SerializedToken &resetForEncode(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = blockLength();
        actingVersion_ = templateVersion();
        position(offset + actingBlockLength_);
        return *this;
    };

    SerializedToken &resetForDecode(char *buffer, const int offset,
                       const int actingBlockLength, const int actingVersion)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = actingBlockLength;
        actingVersion_ = actingVersion;
        position(offset + actingBlockLength_);
        return *this;
    };

    sbe_uint64_t position(void) const
    {
        return position_;
    };

    void position(const sbe_uint64_t position)
    {
        position_ = position;
    };

    int size(void) const
    {
        return position() - offset_;
    };

    int templateId(void) const
    {
        return 2;
    };

    int templateVersion(void) const
    {
        return 0;
    };

    char *buffer(void)
    {
        return buffer_;
    };

    MessageFlyweight *message(void)
    {
        return this;
    };

    int actingVersion(void) const
    {
        return actingVersion_;
    };

    int tokenOffsetSinceVersion(void) const
    {
         return 0;
    };


    int tokenOffsetId(void) const
    {
        return 11;
    };


    sbe_int32_t tokenOffset(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 0)));
    };

    SerializedToken &tokenOffset(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    int tokenSizeSinceVersion(void) const
    {
         return 0;
    };


    int tokenSizeId(void) const
    {
        return 12;
    };


    sbe_int32_t tokenSize(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 4)));
    };

    SerializedToken &tokenSize(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    int schemaIDSinceVersion(void) const
    {
         return 0;
    };


    int schemaIDId(void) const
    {
        return 13;
    };


    sbe_int32_t schemaID(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 8)));
    };

    SerializedToken &schemaID(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    int tokenVersionSinceVersion(void) const
    {
         return 0;
    };


    int tokenVersionId(void) const
    {
        return 17;
    };


    sbe_int32_t tokenVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 12)));
    };

    SerializedToken &tokenVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 12)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    int signalSinceVersion(void) const
    {
         return 0;
    };


    int signalId(void) const
    {
        return 14;
    };


    SerializedSignal::Value signal(void) const
    {
        return SerializedSignal::get((*((sbe_uint8_t *)(buffer_ + offset_ + 16))));
    };

    SerializedToken &signal(const SerializedSignal::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 16)) = (value);
        return *this;
    };

    int primitiveTypeSinceVersion(void) const
    {
         return 0;
    };


    int primitiveTypeId(void) const
    {
        return 15;
    };


    SerializedPrimitiveType::Value primitiveType(void) const
    {
        return SerializedPrimitiveType::get((*((sbe_uint8_t *)(buffer_ + offset_ + 17))));
    };

    SerializedToken &primitiveType(const SerializedPrimitiveType::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 17)) = (value);
        return *this;
    };

    int byteOrderSinceVersion(void) const
    {
         return 0;
    };


    int byteOrderId(void) const
    {
        return 16;
    };


    SerializedByteOrder::Value byteOrder(void) const
    {
        return SerializedByteOrder::get((*((sbe_uint8_t *)(buffer_ + offset_ + 18))));
    };

    SerializedToken &byteOrder(const SerializedByteOrder::Value value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 18)) = (value);
        return *this;
    };

    const char *nameCharacterEncoding()
    {
        return "UTF-8";
    };

    int nameId(void) const
    {
        return 18;
    };

    sbe_int64_t nameLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *name(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getName(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putName(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };

    const char *constValCharacterEncoding()
    {
        return "UTF-8";
    };

    int constValId(void) const
    {
        return 19;
    };

    sbe_int64_t constValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *constVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getConstVal(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putConstVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };

    const char *minValCharacterEncoding()
    {
        return "UTF-8";
    };

    int minValId(void) const
    {
        return 20;
    };

    sbe_int64_t minValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *minVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getMinVal(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putMinVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };

    const char *maxValCharacterEncoding()
    {
        return "UTF-8";
    };

    int maxValId(void) const
    {
        return 21;
    };

    sbe_int64_t maxValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *maxVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getMaxVal(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putMaxVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };

    const char *nullValCharacterEncoding()
    {
        return "UTF-8";
    };

    int nullValId(void) const
    {
        return 22;
    };

    sbe_int64_t nullValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *nullVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getNullVal(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putNullVal(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };

    const char *characterEncodingCharacterEncoding()
    {
        return "UTF-8";
    };

    int characterEncodingId(void) const
    {
        return 23;
    };

    sbe_int64_t characterEncodingLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *characterEncoding(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getCharacterEncoding(char *dst, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        position(lengthPosition + sizeOfLengthField);
        sbe_uint64_t dataLength = (*((sbe_uint8_t *)(buffer_ + lengthPosition)));
        int bytesToCopy = (length < dataLength) ? length : dataLength;
        ::memcpy(dst, buffer_ + position(), bytesToCopy);
        position(position() + (sbe_uint64_t)dataLength);
        return bytesToCopy;
    };

    int putCharacterEncoding(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    };
};
}
#endif
