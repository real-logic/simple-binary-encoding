/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    [Flags]
    public enum InstAttribValue : uint
    {
        ElectronicMatchEligible = 1,
        OrderCrossEligible = 2,
        BlockTradeEligible = 4,
        EFPEligible = 8,
        EBFEligible = 16,
        EFSEligible = 32,
        EFREligible = 64,
        OTCEligible = 128,
        ILinkIndicativeMassQuotingEligible = 256,
        NegativeStrikeEligible = 512,
        NegativePriceOutrightEligible = 1024,
        IsFractional = 2048,
        VolatilityQuotedOption = 4096,
        RFQCrossEligible = 8192,
        ZeroPriceOutrightEligible = 16384,
        DecayingProductEligibility = 32768,
        VariableProductEligibility = 65536,
        DailyProductEligibility = 131072,
        GTOrdersEligibility = 262144,
        ImpliedMatchingEligibility = 524288,
    }
}
