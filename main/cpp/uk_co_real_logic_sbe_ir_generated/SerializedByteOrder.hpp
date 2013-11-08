/* Generated class message */
#ifndef _SERIALIZEDBYTEORDER_HPP_
#define _SERIALIZEDBYTEORDER_HPP_

#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class SerializedByteOrder
{
public:

    enum Value 
    {
        SBE_BIG_ENDIAN = (sbe_uint8_t)1,
        SBE_LITTLE_ENDIAN = (sbe_uint8_t)0
    };

    static SerializedByteOrder::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 1: return SBE_BIG_ENDIAN;
            case 0: return SBE_LITTLE_ENDIAN;
        }

        throw "unknown value for enum SerializedByteOrder";
    };
};
}
#endif
