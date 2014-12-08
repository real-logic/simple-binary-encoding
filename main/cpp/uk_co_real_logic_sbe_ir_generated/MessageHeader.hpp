/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _MESSAGEHEADER_HPP_
#define _MESSAGEHEADER_HPP_

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

#include <sbe/sbe.hpp>

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class MessageHeader
{
private:
    char *buffer_;
    int offset_;
    int actingVersion_;

public:
    MessageHeader &wrap(char *buffer, const int offset, const int actingVersion, const int bufferLength)
    {
        if (SBE_BOUNDS_CHECK_EXPECT((offset > (bufferLength - 8)), 0))
        {
            throw std::runtime_error("buffer too short for flyweight [E107]");
        }
        buffer_ = buffer;
        offset_ = offset;
        actingVersion_ = actingVersion;
        return *this;
    }

    static const int size(void)
    {
        return 8;
    }


    static const sbe_uint16_t blockLengthNullValue()
    {
        return USHRT_MAX;
    }

    static const sbe_uint16_t blockLengthMinValue()
    {
        return (sbe_uint16_t)0;
    }

    static const sbe_uint16_t blockLengthMaxValue()
    {
        return (sbe_uint16_t)65534;
    }

    sbe_uint16_t blockLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 0)));
    }

    MessageHeader &blockLength(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    }

    static const sbe_uint16_t templateIdNullValue()
    {
        return USHRT_MAX;
    }

    static const sbe_uint16_t templateIdMinValue()
    {
        return (sbe_uint16_t)0;
    }

    static const sbe_uint16_t templateIdMaxValue()
    {
        return (sbe_uint16_t)65534;
    }

    sbe_uint16_t templateId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 2)));
    }

    MessageHeader &templateId(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 2)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    }

    static const sbe_uint16_t schemaIdNullValue()
    {
        return USHRT_MAX;
    }

    static const sbe_uint16_t schemaIdMinValue()
    {
        return (sbe_uint16_t)0;
    }

    static const sbe_uint16_t schemaIdMaxValue()
    {
        return (sbe_uint16_t)65534;
    }

    sbe_uint16_t schemaId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 4)));
    }

    MessageHeader &schemaId(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 4)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    }

    static const sbe_uint16_t versionNullValue()
    {
        return USHRT_MAX;
    }

    static const sbe_uint16_t versionMinValue()
    {
        return (sbe_uint16_t)0;
    }

    static const sbe_uint16_t versionMaxValue()
    {
        return (sbe_uint16_t)65534;
    }

    sbe_uint16_t version(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 6)));
    }

    MessageHeader &version(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 6)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    }
};
}
#endif
