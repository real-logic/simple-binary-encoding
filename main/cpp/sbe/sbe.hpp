/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef _SBE_HPP_
#define _SBE_HPP_

#include <string.h>
#include <stdint.h>
#include <limits.h>
#include <stdexcept>

/*
 * Types used by C++ codec. Might have to be platform specific at some stage.
 */
typedef char sbe_char_t;
typedef ::int8_t sbe_int8_t;
typedef ::int16_t sbe_int16_t;
typedef ::int32_t sbe_int32_t;
typedef ::int64_t sbe_int64_t;
typedef ::uint8_t sbe_uint8_t;
typedef ::uint16_t sbe_uint16_t;
typedef ::uint32_t sbe_uint32_t;
typedef ::uint64_t sbe_uint64_t;
typedef float sbe_float_t;
typedef double sbe_double_t;

namespace sbe {

/*
 * Define some byte ordering macros
 */
#if defined(WIN32) || defined(_WIN32)
    #define SBE_BIG_ENDIAN_ENCODE_16(v) _byteswap_ushort(v)
    #define SBE_BIG_ENDIAN_ENCODE_32(v) _byteswap_ulong(v)
    #define SBE_BIG_ENDIAN_ENCODE_64(v) _byteswap_uint64(v)
    #define SBE_LITTLE_ENDIAN_ENCODE_16(v) (v)
    #define SBE_LITTLE_ENDIAN_ENCODE_32(v) (v)
    #define SBE_LITTLE_ENDIAN_ENCODE_64(v) (v)
#elif __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__
    #define SBE_BIG_ENDIAN_ENCODE_16(v) __builtin_bswap16(v) 
    #define SBE_BIG_ENDIAN_ENCODE_32(v) __builtin_bswap32(v) 
    #define SBE_BIG_ENDIAN_ENCODE_64(v) __builtin_bswap64(v) 
    #define SBE_LITTLE_ENDIAN_ENCODE_16(v) (v)
    #define SBE_LITTLE_ENDIAN_ENCODE_32(v) (v)
    #define SBE_LITTLE_ENDIAN_ENCODE_64(v) (v)
#elif __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #define SBE_LITTLE_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)
    #define SBE_LITTLE_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)
    #define SBE_LITTLE_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)
    #define SBE_BIG_ENDIAN_ENCODE_16(v) (v)
    #define SBE_BIG_ENDIAN_ENCODE_32(v) (v)
    #define SBE_BIG_ENDIAN_ENCODE_64(v) (v)
#else
    #error "Byte Ordering of platform not determined. Set __BYTE_ORDER__ manually before including this file."
#endif

#if defined(SBE_NO_BOUNDS_CHECK)
    #define SBE_BOUNDS_CHECK_EXPECT(exp,c) (false)
#elif defined(WIN32) || defined(_WIN32)
    #define SBE_BOUNDS_CHECK_EXPECT(exp,c) (exp)
#else
    #define SBE_BOUNDS_CHECK_EXPECT(exp,c) (__builtin_expect(exp,c))
#endif

class MetaAttribute
{
public:
    enum Attribute
    {
        EPOCH,
        TIME_UNIT,
        SEMANTIC_TYPE
    };
};

}

#endif /* _SBE_HPP_ */
