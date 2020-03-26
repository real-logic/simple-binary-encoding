#ifndef _SBE_SBEC_H_
#define _SBE_SBEC_H_

#include <errno.h>
#if !defined(__STDC_LIMIT_MACROS)
#define __STDC_LIMIT_MACROS 1
#endif
#include <limits.h>
#include <math.h>

#ifdef NAN

#define SBE_FLOAT_NAN NAN
#define SBE_DOUBLE_NAN NAN
#else

#define SBE_HUGE_ENUF  1e+300  /* SBE_HUGE_ENUF*SBE_HUGE_ENUF must overflow */
#define SBE_INFINITY ((float)(SBE_HUGE_ENUF * SBE_HUGE_ENUF))
#define SBE_FLOAT_NAN ((float)(SBE_INFINITY  * 0.0F))
#define SBE_DOUBLE_NAN ((double)(SBE_INFINITY  * 0.0))

#endif

#if (defined(_MSC_VER) && _MSC_VER < 1800)
#ifndef __bool_true_false_are_defined
#define __bool_true_false_are_defined 1

#ifndef __cplusplus

typedef unsigned char _Bool;

#define bool _Bool
#define false 0
#define true 1

#endif /* __cplusplus */

#endif /* __bool_true_false_are_defined*/

#else
#include <stdbool.h>
#endif

#include <stdint.h>
#include <string.h>

#ifdef __cplusplus
#define SBE_ONE_DEF inline
#else

#if defined(__GNUC__)
#define SBE_ONE_DEF __attribute__((always_inline))
#else

#if (defined(_MSC_VER) && _MSC_VER < 1800)
#define SBE_ONE_DEF static __inline
#else
#define SBE_ONE_DEF static inline
#endif

#endif

#endif

union sbec_float_as_uint
{
    float fp_value;
    uint32_t uint_value;
};

union sbec_double_as_uint
{
    double fp_value;
    uint64_t uint_value;
};

struct sbec_string_view
{
    const char* data;
    size_t length;
};


enum sbec_meta_attribute
{
    sbec_meta_attribute_EPOCH,
    sbec_meta_attribute_TIME_UNIT,
    sbec_meta_attribute_SEMANTIC_TYPE,
    sbec_meta_attribute_PRESENCE
};

#ifdef __vxworks
#define __ORDER_LITTLE_ENDIAN__ 0
#define __ORDER_BIG_ENDIAN__ 1
#if defined(__BIG_ENDIAN__) && __BIG_ENDIAN__
#define __BYTE_ORDER__ __ORDER_BIG_ENDIAN__
#else
#define __BYTE_ORDER__ __ORDER_LITTLE_ENDIAN__
#endif

/* Swap bytes in 16-bit value.  */
#ifndef __builtin_bswap16
#define __builtin_bswap16(x)                    \
  ((uint16_t) ((((x) >> 8) & 0xff) | (((x) & 0xff) << 8)))
#endif

/* Swap bytes in 32-bit value.  */
#ifndef __builtin_bswap32
#define __builtin_bswap32(x)                    \
  ((((x) & 0xff000000u) >> 24) | (((x) & 0x00ff0000u) >> 8)	\
   | (((x) & 0x0000ff00u) << 8) | (((x) & 0x000000ffu) << 24))
#endif

/* Swap bytes in 64-bit value.  */
#ifndef __builtin_bswap64
#define __builtin_bswap64(x)                   \
  ((((x) & 0xff00000000000000ull) >> 56)       \
   | (((x) & 0x00ff000000000000ull) >> 40)     \
   | (((x) & 0x0000ff0000000000ull) >> 24)     \
   | (((x) & 0x000000ff00000000ull) >> 8)      \
   | (((x) & 0x00000000ff000000ull) << 8)      \
   | (((x) & 0x0000000000ff0000ull) << 24)     \
   | (((x) & 0x000000000000ff00ull) << 40)     \
   | (((x) & 0x00000000000000ffull) << 56))
#endif

#endif /* __vxworks */

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
    #define SBE_BIG_ENDIAN_ENCODE_16(v) (v)
    #define SBE_BIG_ENDIAN_ENCODE_32(v) (v)
    #define SBE_BIG_ENDIAN_ENCODE_64(v) (v)
    #define SBE_LITTLE_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)
    #define SBE_LITTLE_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)
    #define SBE_LITTLE_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)
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

#define SBE_NULLVALUE_INT8 INT8_MIN
#define SBE_NULLVALUE_INT16 INT16_MIN
#define SBE_NULLVALUE_INT32 INT32_MIN
#define SBE_NULLVALUE_INT64 INT64_MIN
#define SBE_NULLVALUE_UINT8 UINT8_MAX
#define SBE_NULLVALUE_UINT16 UINT16_MAX
#define SBE_NULLVALUE_UINT32 UINT32_MAX
#define SBE_NULLVALUE_UINT64 UINT64_MAX

#define E100 -50100 /* E_BUF_SHORT */
#define E103 -50103 /* VAL_UNKNOWN_ENUM */
#define E104 -50104 /* I_OUT_RANGE_NUM */
#define E105 -50105 /* I_OUT_RANGE_NUM */
#define E106 -50106 /* I_OUT_RANGE_NUM */
#define E107 -50107 /* BUF_SHORT_FLYWEIGHT */
#define E108 -50108 /* BUF_SHORT_NXT_GRP_IND */
#define E109 -50109 /* STR_TOO_LONG_FOR_LEN_TYP */
#define E110 -50110 /* CNT_OUT_RANGE */

SBE_ONE_DEF const char *sbe_strerror(const int errnum)
{
    switch (errnum)
    {
        case E100:
            return "buffer too short";
        case E103:
            return "unknown value for enum";
        case E104:
            return "index out of range";
        case E105:
            return "index out of range";
        case E106:
            return "length too large";
        case E107:
            return "buffer too short for flyweight";
        case E108:
            return "buffer too short to support next group index";
        case E109:
            return "std::string too long for length type";
        case E110:
            return "count outside of allowed range";
        default:
            return "unknown error";
    }
}

#endif
