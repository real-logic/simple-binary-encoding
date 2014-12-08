/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _PRESENCECODEC_HPP_
#define _PRESENCECODEC_HPP_

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

class PresenceCodec
{
public:

    enum Value 
    {
        SBE_REQUIRED = (sbe_uint8_t)0,
        SBE_OPTIONAL = (sbe_uint8_t)1,
        SBE_CONSTANT = (sbe_uint8_t)2,
        NULL_VALUE = (sbe_uint8_t)255
    };

    static PresenceCodec::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 0: return SBE_REQUIRED;
            case 1: return SBE_OPTIONAL;
            case 2: return SBE_CONSTANT;
            case 255: return NULL_VALUE;
        }

        throw std::runtime_error("unknown value for enum PresenceCodec [E103]");
    }
};
}
#endif
