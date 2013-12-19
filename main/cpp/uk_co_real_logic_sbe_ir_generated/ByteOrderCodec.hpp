/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _BYTEORDERCODEC_HPP_
#define _BYTEORDERCODEC_HPP_

/* math.h needed for NAN */
#include <math.h>
#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class ByteOrderCodec
{
public:

    enum Value 
    {
        SBE_LITTLE_ENDIAN = (sbe_uint8_t)0,
        SBE_BIG_ENDIAN = (sbe_uint8_t)1,
        NULL_VALUE = (sbe_uint8_t)255
    };

    static ByteOrderCodec::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 0: return SBE_LITTLE_ENDIAN;
            case 1: return SBE_BIG_ENDIAN;
            case 255: return NULL_VALUE;
        }

        throw "unknown value for enum ByteOrderCodec";
    }
};
}
#endif
