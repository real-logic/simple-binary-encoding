/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_PRIMITIVETYPECODEC_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_PRIMITIVETYPECODEC_H_

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

class PrimitiveTypeCodec
{
public:

    enum Value 
    {
        NONE = (std::uint8_t)0,
        CHAR = (std::uint8_t)1,
        INT8 = (std::uint8_t)2,
        INT16 = (std::uint8_t)3,
        INT32 = (std::uint8_t)4,
        INT64 = (std::uint8_t)5,
        UINT8 = (std::uint8_t)6,
        UINT16 = (std::uint8_t)7,
        UINT32 = (std::uint8_t)8,
        UINT64 = (std::uint8_t)9,
        FLOAT = (std::uint8_t)10,
        DOUBLE = (std::uint8_t)11,
        NULL_VALUE = (std::uint8_t)255
    };

    static PrimitiveTypeCodec::Value get(const std::uint8_t value)
    {
        switch (value)
        {
            case 0: return NONE;
            case 1: return CHAR;
            case 2: return INT8;
            case 3: return INT16;
            case 4: return INT32;
            case 5: return INT64;
            case 6: return UINT8;
            case 7: return UINT16;
            case 8: return UINT32;
            case 9: return UINT64;
            case 10: return FLOAT;
            case 11: return DOUBLE;
            case 255: return NULL_VALUE;
        }

        throw std::runtime_error("unknown value for enum PrimitiveTypeCodec [E103]");
    }
};
}
#endif
