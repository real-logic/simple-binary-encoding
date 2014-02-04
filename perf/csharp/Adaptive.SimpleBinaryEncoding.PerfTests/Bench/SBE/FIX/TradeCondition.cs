/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    [Flags]
    public enum TradeCondition : byte
    {
        OpeningTrade = 1,
        CmeGlobexPrice = 2,
    }
}
