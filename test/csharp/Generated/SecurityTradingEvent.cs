/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public enum SecurityTradingEvent : byte
    {
        NoEvent = 0,
        NoCancel = 1,
        ResetStatistics = 4,
        ImpliedMatchingON = 5,
        ImpliedMatchingOFF = 6,
        NULL_VALUE = 255
    }
}
