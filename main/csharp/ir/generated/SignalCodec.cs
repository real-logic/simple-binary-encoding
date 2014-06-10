/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public enum SignalCodec : byte
    {
        BEGIN_MESSAGE = 1,
        END_MESSAGE = 2,
        BEGIN_COMPOSITE = 3,
        END_COMPOSITE = 4,
        BEGIN_FIELD = 5,
        END_FIELD = 6,
        BEGIN_GROUP = 7,
        END_GROUP = 8,
        BEGIN_ENUM = 9,
        VALID_VALUE = 10,
        END_ENUM = 11,
        BEGIN_SET = 12,
        CHOICE = 13,
        END_SET = 14,
        BEGIN_VAR_DATA = 15,
        END_VAR_DATA = 16,
        ENCODING = 17,
        NULL_VALUE = 255
    }
}
