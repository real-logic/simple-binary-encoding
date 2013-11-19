/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _SERIALIZEDFRAME_HPP_
#define _SERIALIZEDFRAME_HPP_

#include "sbe/sbe.hpp"

#include "uk_co_real_logic_sbe_ir_generated/VarDataEncoding.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedSignal.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedPrimitiveType.hpp"
#include "uk_co_real_logic_sbe_ir_generated/SerializedByteOrder.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class SerializedFrame : public MessageFlyweight
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
        return 8;
    };

    sbe_uint64_t offset(void) const
    {
        return offset_;
    };

    SerializedFrame &resetForEncode(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = blockLength();
        actingVersion_ = templateVersion();
        position(offset + actingBlockLength_);
        return *this;
    };

    SerializedFrame &resetForDecode(char *buffer, const int offset,
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
        return 1;
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

    int sbeIrVersionSinceVersion(void) const
    {
         return 0;
    };


    int sbeIrVersionId(void) const
    {
        return 1;
    };


    sbe_int32_t sbeIrVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 0)));
    };

    SerializedFrame &sbeIrVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    int schemaVersionSinceVersion(void) const
    {
         return 0;
    };


    int schemaVersionId(void) const
    {
        return 2;
    };


    sbe_int32_t schemaVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 4)));
    };

    SerializedFrame &schemaVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    };

    const char *packageValCharacterEncoding()
    {
        return "UTF-8";
    };

    int packageValId(void) const
    {
        return 4;
    };

    sbe_int64_t packageValLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    };

    const char *packageVal(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    };

    int getPackageVal(char *dst, const int length)
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

    int putPackageVal(const char *src, const int length)
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
