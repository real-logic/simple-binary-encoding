/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public enum MDEntryType : byte
    {
        Bid = 48,
        Offer = 49,
        Trade = 50,
        OpeningPrice = 52,
        SettlementPrice = 54,
        TradingSessionHighPrice = 55,
        TradingSessionLowPrice = 56,
        TradeVolume = 66,
        OpenInterest = 67,
        ImpliedBid = 69,
        ImpliedOffer = 70,
        EmptyBook = 74,
        SessionHighBid = 78,
        SessionLowOffer = 79,
        FixingPrice = 87,
        ElectronicVolume = 101,
        ThresholdLimitsandPriceBandVariation = 103,
        NULL_VALUE = 0
    }
}
