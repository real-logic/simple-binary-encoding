/* Generated class message */
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

public:

    sbe_uint64_t blockLength(void) const
    {
        return 19;
    };

    sbe_uint64_t offset(void) const
    {
        return offset_;
    };

    SerializedToken &reset(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        position(offset + blockLength());
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

    char *buffer(void)
    {
        return buffer_;
    };

    MessageFlyweight *message(void)
    {
        return this;
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
