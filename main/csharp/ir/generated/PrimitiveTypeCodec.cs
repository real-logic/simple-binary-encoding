/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public enum PrimitiveTypeCodec : byte
    {
        NONE = 0,
        CHAR = 1,
        INT8 = 2,
        INT16 = 3,
        INT32 = 4,
        INT64 = 5,
        UINT8 = 6,
        UINT16 = 7,
        UINT32 = 8,
        UINT64 = 9,
        FLOAT = 10,
        DOUBLE = 11,
        NULL_VALUE = 255
    }
}
