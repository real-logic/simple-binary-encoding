/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public enum MDEntryType : byte
    {
        BID = 48,
        OFFER = 49,
        TRADE = 50,
        OPENING_PRICE = 52,
        SETTLEMENT_PRICE = 54,
        TRADING_SESSION_HIGH_PRICE = 55,
        TRADING_SESSION_LOW_PRICE = 56,
        TRADE_VOLUME = 66,
        OPEN_INTEREST = 67,
        SIMULATED_SELL = 69,
        SIMULATED_BUY = 70,
        EMPTY_THE_BOOK = 74,
        SESSION_HIGH_BID = 78,
        SESSION_LOW_OFFER = 79,
        FIXING_PRICE = 87,
        CASH_NOTE = 88,
        NULL_VALUE = 0
    }
}
