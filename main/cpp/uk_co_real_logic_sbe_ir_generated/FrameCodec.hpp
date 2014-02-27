/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _FRAMECODEC_HPP_
#define _FRAMECODEC_HPP_

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

class FrameCodec
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
        return (sbe_uint16_t)12;
    }

    static sbe_uint16_t templateId(void)
    {
        return (sbe_uint16_t)1;
    }

    static sbe_uint8_t templateVersion(void)
    {
        return (sbe_uint8_t)0;
    }

    static const char *semanticType(void)
    {
        return "";
    }

    sbe_uint64_t offset(void) const
    {
        return offset_;
    }

    FrameCodec &wrapForEncode(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingBlockLength_ = blockLength();
        actingVersion_ = templateVersion();
        position(offset + actingBlockLength_);
        positionPtr_ = &position_;
        return *this;
    }

    FrameCodec &wrapForDecode(char *buffer, const int offset, const int actingBlockLength, const int actingVersion)
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

    static int sbeIrIdId(void)
    {
        return 1;
    }

    static int sbeIrIdSinceVersion(void)
    {
         return 0;
    }

    bool sbeIrIdInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }


    static const char *sbeIrIdMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static sbe_int32_t sbeIrIdNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t sbeIrIdMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t sbeIrIdMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t sbeIrId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 0)));
    }

    FrameCodec &sbeIrId(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static int sbeIrVersionId(void)
    {
        return 2;
    }

    static int sbeIrVersionSinceVersion(void)
    {
         return 0;
    }

    bool sbeIrVersionInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }


    static const char *sbeIrVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static sbe_int32_t sbeIrVersionNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t sbeIrVersionMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t sbeIrVersionMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t sbeIrVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 4)));
    }

    FrameCodec &sbeIrVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 4)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static int sbeSchemaVersionId(void)
    {
        return 3;
    }

    static int sbeSchemaVersionSinceVersion(void)
    {
         return 0;
    }

    bool sbeSchemaVersionInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }


    static const char *sbeSchemaVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    static sbe_int32_t sbeSchemaVersionNullValue()
    {
        return -2147483648;
    }

    static sbe_int32_t sbeSchemaVersionMinValue()
    {
        return -2147483647;
    }

    static sbe_int32_t sbeSchemaVersionMaxValue()
    {
        return 2147483647;
    }

    sbe_int32_t sbeSchemaVersion(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_32(*((sbe_int32_t *)(buffer_ + offset_ + 8)));
    }

    FrameCodec &sbeSchemaVersion(const sbe_int32_t value)
    {
        *((sbe_int32_t *)(buffer_ + offset_ + 8)) = SBE_LITTLE_ENDIAN_ENCODE_32(value);
        return *this;
    }

    static const char *sbePackageNameCharacterEncoding()
    {
        return "UTF-8";
    }

    static int sbePackageNameSinceVersion(void)
    {
         return 0;
    }

    bool sbePackageNameInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int sbePackageNameId(void)
    {
        return 4;
    }


    static const char *sbePackageNameMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t sbePackageNameLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *sbePackageName(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getSbePackageName(char *dst, const int length)
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

    int putSbePackageName(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }

    static const char *sbeNamespaceNameCharacterEncoding()
    {
        return "UTF-8";
    }

    static int sbeNamespaceNameSinceVersion(void)
    {
         return 0;
    }

    bool sbeNamespaceNameInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int sbeNamespaceNameId(void)
    {
        return 5;
    }


    static const char *sbeNamespaceNameMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t sbeNamespaceNameLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *sbeNamespaceName(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getSbeNamespaceName(char *dst, const int length)
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

    int putSbeNamespaceName(const char *src, const int length)
    {
        sbe_uint64_t sizeOfLengthField = 1;
        sbe_uint64_t lengthPosition = position();
        *((sbe_uint8_t *)(buffer_ + lengthPosition)) = ((sbe_uint8_t)length);
        position(lengthPosition + sizeOfLengthField);
        ::memcpy(buffer_ + position(), src, length);
        position(position() + (sbe_uint64_t)length);
        return length;
    }

    static const char *sbeSemanticVersionCharacterEncoding()
    {
        return "UTF-8";
    }

    static int sbeSemanticVersionSinceVersion(void)
    {
         return 0;
    }

    bool sbeSemanticVersionInActingVersion(void)
    {
        return (actingVersion_ >= 0) ? true : false;
    }

    static int sbeSemanticVersionId(void)
    {
        return 6;
    }


    static const char *sbeSemanticVersionMetaAttribute(const MetaAttribute::Attribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case MetaAttribute::EPOCH: return "unix";
            case MetaAttribute::TIME_UNIT: return "nanosecond";
            case MetaAttribute::SEMANTIC_TYPE: return "";
        }

        return "";
    }

    sbe_int64_t sbeSemanticVersionLength(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + position())));
    }

    const char *sbeSemanticVersion(void)
    {
         const char *fieldPtr = (buffer_ + position() + 1);
         position(position() + 1 + *((sbe_uint8_t *)(buffer_ + position())));
         return fieldPtr;
    }

    int getSbeSemanticVersion(char *dst, const int length)
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

    int putSbeSemanticVersion(const char *src, const int length)
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
