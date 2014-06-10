/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public enum CustOrderHandlingInst : byte
    {
        PHONE_SIMPLE = 65,
        PHONE_COMPLEX = 66,
        FCM_PROVIDED_SCREEN = 67,
        OTHER_PROVIDED_SCREEN = 68,
        CLIENT_PROVIDED_PLATFORM_CONTROLLED_BY_FCM = 69,
        CLIENT_PROVIDED_PLATFORM_DIRECT_TO_EXCHANGE = 70,
        FCM_API_OR_FIX = 71,
        ALGO_ENGINE = 72,
        PRICE_AT_EXECUTION = 74,
        DESK_ELECTRONIC = 87,
        DESK_PIT = 88,
        CLIENT_ELECTRONIC = 89,
        CLIENT_PIT = 90,
        NULL_VALUE = 0
    }
}
