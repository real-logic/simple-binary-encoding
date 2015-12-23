/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_PRESENCECODEC_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_PRESENCECODEC_H_

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
#  include <cstring>
#endif

#include <sbe/sbe.h>

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class PresenceCodec
{
public:

    enum Value 
    {
        SBE_REQUIRED = (std::uint8_t)0,
        SBE_OPTIONAL = (std::uint8_t)1,
        SBE_CONSTANT = (std::uint8_t)2,
        NULL_VALUE = (std::uint8_t)255
    };

    static PresenceCodec::Value get(const std::uint8_t value)
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
