/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public enum PresenceCodec : byte
    {
        SBE_REQUIRED = 0,
        SBE_OPTIONAL = 1,
        SBE_CONSTANT = 2,
        NULL_VALUE = 255
    }
}
