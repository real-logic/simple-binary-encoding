/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _VARDATAENCODING_HPP_
#define _VARDATAENCODING_HPP_

#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class VarDataEncoding : public FixedFlyweight
{
private:
    char *buffer_;
    int offset_;

public:
    VarDataEncoding &reset(char *buffer, const int offset)
    {
        buffer_ = buffer;
        offset_ = offset;
        return *this;
    };

    int size(void) const
    {
        return -1;
    };


    sbe_uint8_t length(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + offset_ + 0)));
    };

    VarDataEncoding &length(const sbe_uint8_t value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 0)) = (value);
        return *this;
    };
};
}
#endif
