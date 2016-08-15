/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_BYTEORDERCODEC_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_BYTEORDERCODEC_H_

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

#if __cplusplus >= 201103L
#  define SBE_CONSTEXPR constexpr
#else
#  define SBE_CONSTEXPR
#endif

#include <sbe/sbe.h>

using namespace sbe;

namespace uk {
namespace co {
namespace real_logic {
namespace sbe {
namespace ir {
namespace generated {

class ByteOrderCodec
{
public:

    enum Value 
    {
        SBE_LITTLE_ENDIAN = (std::uint8_t)0,
        SBE_BIG_ENDIAN = (std::uint8_t)1,
        NULL_VALUE = (std::uint8_t)255
    };

    static ByteOrderCodec::Value get(const std::uint8_t value)
    {
        switch (value)
        {
            case 0: return SBE_LITTLE_ENDIAN;
            case 1: return SBE_BIG_ENDIAN;
            case 255: return NULL_VALUE;
        }

        throw std::runtime_error("unknown value for enum ByteOrderCodec [E103]");
    }
};
};
};
};
};
};
}
#endif
