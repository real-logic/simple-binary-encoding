/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public enum MDUpdateAction : byte
    {
        NEW = 0,
        CHANGE = 1,
        DELETE = 2,
        OVERLAY = 5,
        NULL_VALUE = 255
    }
}
