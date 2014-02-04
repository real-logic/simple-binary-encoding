/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public enum HaltReason : byte
    {
        GroupSchedule = 0,
        SurveillanceIntervention = 1,
        MarketEvent = 2,
        InstrumentActivation = 3,
        InstrumentExpiration = 4,
        Unknown = 5,
        NULL_VALUE = 255
    }
}
