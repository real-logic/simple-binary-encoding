/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    [Flags]
    public enum SettlPriceType : byte
    {
        Final = 1,
        Actual = 2,
        Rounded = 4,
        ReservedBits = 8,
        NullValue = 128,
    }
}
