/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _SERIALIZEDPRIMITIVETYPE_HPP_
#define _SERIALIZEDPRIMITIVETYPE_HPP_

/* math.h needed for NAN */
#include <math.h>
#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class SerializedPrimitiveType
{
public:

    enum Value 
    {
        INT32 = (sbe_uint8_t)4,
        INT8 = (sbe_uint8_t)2,
        CHAR = (sbe_uint8_t)1,
        DOUBLE = (sbe_uint8_t)11,
        FLOAT = (sbe_uint8_t)10,
        UINT32 = (sbe_uint8_t)8,
        UINT8 = (sbe_uint8_t)6,
        INT64 = (sbe_uint8_t)5,
        UINT64 = (sbe_uint8_t)9,
        NONE = (sbe_uint8_t)0,
        UINT16 = (sbe_uint8_t)7,
        INT16 = (sbe_uint8_t)3,
        NULL_VALUE = (sbe_uint8_t)255
    };

    static SerializedPrimitiveType::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 4: return INT32;
            case 2: return INT8;
            case 1: return CHAR;
            case 11: return DOUBLE;
            case 10: return FLOAT;
            case 8: return UINT32;
            case 6: return UINT8;
            case 5: return INT64;
            case 9: return UINT64;
            case 0: return NONE;
            case 7: return UINT16;
            case 3: return INT16;
            case 255: return NULL_VALUE;
        }

        throw "unknown value for enum SerializedPrimitiveType";
    }
};
}
#endif
