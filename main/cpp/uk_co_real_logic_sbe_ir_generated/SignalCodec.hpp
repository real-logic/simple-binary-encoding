/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _SIGNALCODEC_HPP_
#define _SIGNALCODEC_HPP_

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

class SignalCodec
{
public:

    enum Value 
    {
        BEGIN_MESSAGE = (sbe_uint8_t)1,
        END_MESSAGE = (sbe_uint8_t)2,
        BEGIN_COMPOSITE = (sbe_uint8_t)3,
        END_COMPOSITE = (sbe_uint8_t)4,
        BEGIN_FIELD = (sbe_uint8_t)5,
        END_FIELD = (sbe_uint8_t)6,
        BEGIN_GROUP = (sbe_uint8_t)7,
        END_GROUP = (sbe_uint8_t)8,
        BEGIN_ENUM = (sbe_uint8_t)9,
        VALID_VALUE = (sbe_uint8_t)10,
        END_ENUM = (sbe_uint8_t)11,
        BEGIN_SET = (sbe_uint8_t)12,
        CHOICE = (sbe_uint8_t)13,
        END_SET = (sbe_uint8_t)14,
        BEGIN_VAR_DATA = (sbe_uint8_t)15,
        END_VAR_DATA = (sbe_uint8_t)16,
        ENCODING = (sbe_uint8_t)17,
        NULL_VALUE = (sbe_uint8_t)255
    };

    static SignalCodec::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 1: return BEGIN_MESSAGE;
            case 2: return END_MESSAGE;
            case 3: return BEGIN_COMPOSITE;
            case 4: return END_COMPOSITE;
            case 5: return BEGIN_FIELD;
            case 6: return END_FIELD;
            case 7: return BEGIN_GROUP;
            case 8: return END_GROUP;
            case 9: return BEGIN_ENUM;
            case 10: return VALID_VALUE;
            case 11: return END_ENUM;
            case 12: return BEGIN_SET;
            case 13: return CHOICE;
            case 14: return END_SET;
            case 15: return BEGIN_VAR_DATA;
            case 16: return END_VAR_DATA;
            case 17: return ENCODING;
            case 255: return NULL_VALUE;
        }

        throw std::runtime_error("unknown value for enum SignalCodec [E103]");
    }
};
}
#endif
