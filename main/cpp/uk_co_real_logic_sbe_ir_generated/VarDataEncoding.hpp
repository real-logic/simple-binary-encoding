/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _VARDATAENCODING_HPP_
#define _VARDATAENCODING_HPP_

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

class VarDataEncoding
{
private:
    char *buffer_;
    int offset_;
    int actingVersion_;

public:
    VarDataEncoding &wrap(char *buffer, const int offset, const int actingVersion, const int bufferLength)
    {
        if (SBE_BOUNDS_CHECK_EXPECT((offset > (bufferLength - -1)), 0))
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
        return -1;
    }


    static const sbe_uint8_t lengthNullValue()
    {
        return UCHAR_MAX;
    }

    static const sbe_uint8_t lengthMinValue()
    {
        return (sbe_uint8_t)0;
    }

    static const sbe_uint8_t lengthMaxValue()
    {
        return (sbe_uint8_t)254;
    }

    sbe_uint8_t length(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + offset_ + 0)));
    }

    VarDataEncoding &length(const sbe_uint8_t value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 0)) = (value);
        return *this;
    }

    static const sbe_uint8_t varDataNullValue()
    {
        return UCHAR_MAX;
    }

    static const sbe_uint8_t varDataMinValue()
    {
        return (sbe_uint8_t)0;
    }

    static const sbe_uint8_t varDataMaxValue()
    {
        return (sbe_uint8_t)254;
    }
};
}
#endif
