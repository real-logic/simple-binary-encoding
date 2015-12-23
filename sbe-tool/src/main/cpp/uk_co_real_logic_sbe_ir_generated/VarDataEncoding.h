/* Generated SBE (Simple Binary Encoding) message codec */
#ifndef _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_VARDATAENCODING_H_
#define _UK_CO_REAL_LOGIC_SBE_IR_GENERATED_VARDATAENCODING_H_

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

class VarDataEncoding
{
private:
    char *m_buffer;
    std::uint64_t m_offset;
    std::uint64_t m_actingVersion;

    inline void reset(char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength, const std::uint64_t actingVersion)
    {
        if (SBE_BOUNDS_CHECK_EXPECT(((offset + -1) > bufferLength), false))
        {
            throw std::runtime_error("buffer too short for flyweight [E107]");
        }
        m_buffer = buffer;
        m_offset = offset;
        m_actingVersion = actingVersion;
    }

public:
    VarDataEncoding(void) : m_buffer(nullptr), m_offset(0) {}

    VarDataEncoding(char *buffer, const std::uint64_t bufferLength, const std::uint64_t actingVersion)
    {
        reset(buffer, 0, bufferLength, actingVersion);
    }

    VarDataEncoding(const VarDataEncoding& codec) :
        m_buffer(codec.m_buffer), m_offset(codec.m_offset), m_actingVersion(codec.m_actingVersion) {}

#if __cplusplus >= 201103L
    VarDataEncoding(VarDataEncoding&& codec) :
        m_buffer(codec.m_buffer), m_offset(codec.m_offset), m_actingVersion(codec.m_actingVersion) {}

    VarDataEncoding& operator=(VarDataEncoding&& codec)
    {
        m_buffer = codec.m_buffer;
        m_offset = codec.m_offset;
        m_actingVersion = codec.m_actingVersion;
        return *this;
    }

#endif

    VarDataEncoding& operator=(const VarDataEncoding& codec)
    {
        m_buffer = codec.m_buffer;
        m_offset = codec.m_offset;
        m_actingVersion = codec.m_actingVersion;
        return *this;
    }

    VarDataEncoding &wrap(char *buffer, const std::uint64_t offset, const std::uint64_t actingVersion, const std::uint64_t bufferLength)
    {
        reset(buffer, offset, bufferLength, actingVersion);
        return *this;
    }

    static const std::uint64_t encodedLength(void)
    {
        return -1;
    }


    static const std::uint8_t lengthNullValue()
    {
        return SBE_NULLVALUE_UINT8;
    }

    static const std::uint8_t lengthMinValue()
    {
        return (std::uint8_t)0;
    }

    static const std::uint8_t lengthMaxValue()
    {
        return (std::uint8_t)254;
    }

    std::uint8_t length(void) const
    {
        return (*((std::uint8_t *)(m_buffer + m_offset + 0)));
    }

    VarDataEncoding &length(const std::uint8_t value)
    {
        *((std::uint8_t *)(m_buffer + m_offset + 0)) = (value);
        return *this;
    }

    static const std::uint8_t varDataNullValue()
    {
        return SBE_NULLVALUE_UINT8;
    }

    static const std::uint8_t varDataMinValue()
    {
        return (std::uint8_t)0;
    }

    static const std::uint8_t varDataMaxValue()
    {
        return (std::uint8_t)254;
    }
};
}
#endif
