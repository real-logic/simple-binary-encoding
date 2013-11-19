/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _SERIALIZEDSIGNAL_HPP_
#define _SERIALIZEDSIGNAL_HPP_

#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class SerializedSignal
{
public:

    enum Value 
    {
        END_SET = (sbe_uint8_t)14,
        BEGIN_VAR_DATA = (sbe_uint8_t)15,
        END_MESSAGE = (sbe_uint8_t)2,
        BEGIN_FIELD = (sbe_uint8_t)5,
        END_FIELD = (sbe_uint8_t)6,
        END_COMPOSITE = (sbe_uint8_t)4,
        ENCODING = (sbe_uint8_t)17,
        VALID_VALUE = (sbe_uint8_t)10,
        END_GROUP = (sbe_uint8_t)8,
        BEGIN_COMPOSITE = (sbe_uint8_t)3,
        END_VAR_DATA = (sbe_uint8_t)16,
        BEGIN_GROUP = (sbe_uint8_t)7,
        BEGIN_ENUM = (sbe_uint8_t)9,
        END_ENUM = (sbe_uint8_t)11,
        BEGIN_SET = (sbe_uint8_t)12,
        CHOICE = (sbe_uint8_t)13,
        BEGIN_MESSAGE = (sbe_uint8_t)1
    };

    static SerializedSignal::Value get(const sbe_uint8_t value)
    {
        switch (value)
        {
            case 14: return END_SET;
            case 15: return BEGIN_VAR_DATA;
            case 2: return END_MESSAGE;
            case 5: return BEGIN_FIELD;
            case 6: return END_FIELD;
            case 4: return END_COMPOSITE;
            case 17: return ENCODING;
            case 10: return VALID_VALUE;
            case 8: return END_GROUP;
            case 3: return BEGIN_COMPOSITE;
            case 16: return END_VAR_DATA;
            case 7: return BEGIN_GROUP;
            case 9: return BEGIN_ENUM;
            case 11: return END_ENUM;
            case 12: return BEGIN_SET;
            case 13: return CHOICE;
            case 1: return BEGIN_MESSAGE;
        }

        throw "unknown value for enum SerializedSignal";
    };
};
}
#endif
