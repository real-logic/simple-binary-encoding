/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public enum SecurityTradingStatus : byte
    {
        TradingHalt = 2,
        Close = 4,
        NewPriceIndication = 15,
        ReadyToTrade = 17,
        NotAvailableForTrading = 18,
        UnknownorInvalid = 20,
        PreOpen = 21,
        PreCross = 24,
        Cross = 25,
        PostClose = 26,
        NoChange = 103,
        NULL_VALUE = 255
    }
}
