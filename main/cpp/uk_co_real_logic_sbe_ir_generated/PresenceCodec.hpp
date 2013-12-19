/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _PRESENCECODEC_HPP_
#define _PRESENCECODEC_HPP_

/* math.h needed for NAN */
#include <math.h>
#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class PresenceCodec
{
public:

    enum Value 
    {
        REQUIRED = (sbe_uint8_t)0,
        OPTIONAL = (sbe_uint8_t)1,
        CONSTANT = (sbe_uint8_t)2,
        NULL_VALUE = (sbe_uint8_t)255
    };

    static PresenceCodec::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 0: return REQUIRED;
            case 1: return OPTIONAL;
            case 2: return CONSTANT;
            case 255: return NULL_VALUE;
        }

        throw "unknown value for enum PresenceCodec";
    }
};
}
#endif
