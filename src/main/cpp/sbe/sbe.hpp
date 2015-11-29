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

#if !defined(__STDC_LIMIT_MACROS)
    #define __STDC_LIMIT_MACROS 1
#endif
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
#elif defined(_MSC_VER)
    #define SBE_BOUNDS_CHECK_EXPECT(exp,c) (exp)
#else
    #define SBE_BOUNDS_CHECK_EXPECT(exp,c) (__builtin_expect(exp,c))
#endif

#if defined(__GNUC__)
    #define SBE_NULLVALUE_INT8 (INT8_MIN)
    #define SBE_NULLVALUE_INT16 (INT16_MIN)
    #define SBE_NULLVALUE_INT32 (INT32_MIN)
    #define SBE_NULLVALUE_INT64 (INT64_MIN)
    #define SBE_NULLVALUE_UINT8 (UINT8_MAX)
    #define SBE_NULLVALUE_UINT16 (UINT16_MAX)
    #define SBE_NULLVALUE_UINT32 (UINT32_MAX)
    #define SBE_NULLVALUE_UINT64 (UINT64_MAX)
#elif defined(_MSC_VER)
    // Visual C++ does not handle minimum integer values properly
    // See: http://msdn.microsoft.com/en-us/library/4kh09110.aspx
    #define SBE_NULLVALUE_INT8 (SCHAR_MIN)
    #define SBE_NULLVALUE_INT16 (SHRT_MIN)
    #define SBE_NULLVALUE_INT32 (LONG_MIN)
    #define SBE_NULLVALUE_INT64 (LLONG_MIN)
    #define SBE_NULLVALUE_UINT8 (UCHAR_MAX)
    #define SBE_NULLVALUE_UINT16 (USHRT_MAX)
    #define SBE_NULLVALUE_UINT32 (ULONG_MAX)
    #define SBE_NULLVALUE_UINT64 (ULLONG_MAX)
#else
    #define SBE_NULLVALUE_INT8 (INT8_MIN)
    #define SBE_NULLVALUE_INT16 (INT16_MIN)
    #define SBE_NULLVALUE_INT32 (INT32_MIN)
    #define SBE_NULLVALUE_INT64 (INT64_MIN)
    #define SBE_NULLVALUE_UINT8 (UINT8_MAX)
    #define SBE_NULLVALUE_UINT16 (UINT16_MAX)
    #define SBE_NULLVALUE_UINT32 (UINT32_MAX)
    #define SBE_NULLVALUE_UINT64 (UINT64_MAX)
#endif

namespace MetaAttribute {

enum Attribute
{
    EPOCH,
    TIME_UNIT,
    SEMANTIC_TYPE
};

}

}

#endif /* _SBE_HPP_ */
