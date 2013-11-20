/* Generated class message */
#ifndef _MESSAGEHEADER_HPP_
#define _MESSAGEHEADER_HPP_

#include "sbe/sbe.hpp"

using namespace sbe;

namespace uk_co_real_logic_sbe_ir_generated {

class MessageHeader : public FixedFlyweight
{
private:
    char *buffer_;
    int offset_;
    int actingVersion_;

public:
    MessageHeader &reset(char *buffer, const int offset, const int actingVersion)
    {
        buffer_ = buffer;
        offset_ = offset;
        actingVersion_ = actingVersion;
        return *this;
    };

    int size(void) const
    {
        return 6;
    };


    sbe_uint16_t blockLength(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 0)));
    };

    MessageHeader &blockLength(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 0)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    };

    sbe_uint16_t templateId(void) const
    {
        return SBE_LITTLE_ENDIAN_ENCODE_16(*((sbe_uint16_t *)(buffer_ + offset_ + 2)));
    };

    MessageHeader &templateId(const sbe_uint16_t value)
    {
        *((sbe_uint16_t *)(buffer_ + offset_ + 2)) = SBE_LITTLE_ENDIAN_ENCODE_16(value);
        return *this;
    };

    sbe_uint8_t version(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + offset_ + 4)));
    };

    MessageHeader &version(const sbe_uint8_t value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 4)) = (value);
        return *this;
    };

    sbe_uint8_t reserved(void) const
    {
        return (*((sbe_uint8_t *)(buffer_ + offset_ + 5)));
    };

    MessageHeader &reserved(const sbe_uint8_t value)
    {
        *((sbe_uint8_t *)(buffer_ + offset_ + 5)) = (value);
        return *this;
    };
};
}
#endif
