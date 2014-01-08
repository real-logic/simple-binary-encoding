using System.IO;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Proto
{
    public static class GpbMarketDataBenchmark
    {
        public static long Encode(MarketDataIncrementalRefreshTrades.Builder marketData, Stream output)
        {
            output.Position = 0;

            marketData.Clear()
                .SetTransactTime(1234L)
                .SetEventTimeDelta(987)
                .SetMatchEventIndicator(MarketDataIncrementalRefreshTrades.Types.MatchEventIndicator.END_EVENT);

            MdIncGrp.Builder groupBuilder = MdIncGrp.CreateBuilder();
            groupBuilder.SetTradeId(1234L);
            groupBuilder.SetSecurityId(56789L);
            groupBuilder.SetMdEntryPx(Decimal64.CreateBuilder().SetMantissa(50));
            groupBuilder.SetMdEntrySize(IntQty32.CreateBuilder().SetMantissa(10));
            groupBuilder.SetNumberOfOrders(1);
            groupBuilder.SetMdUpdateAction(MdIncGrp.Types.MdUpdateAction.NEW);
            groupBuilder.SetRepSeq(1);
            groupBuilder.SetAggressorSide(MdIncGrp.Types.Side.BUY);
            groupBuilder.SetMdEntryType(MdIncGrp.Types.MdEntryType.BID);
            marketData.AddMdIncGroup(groupBuilder);

            groupBuilder = MdIncGrp.CreateBuilder();
            groupBuilder.SetTradeId(1234L);
            groupBuilder.SetSecurityId(56789L);
            groupBuilder.SetMdEntryPx(Decimal64.CreateBuilder().SetMantissa(50));
            groupBuilder.SetMdEntrySize(IntQty32.CreateBuilder().SetMantissa(10));
            groupBuilder.SetNumberOfOrders(1);
            groupBuilder.SetMdUpdateAction(MdIncGrp.Types.MdUpdateAction.NEW);
            groupBuilder.SetRepSeq(1);
            groupBuilder.SetAggressorSide(MdIncGrp.Types.Side.SELL);
            groupBuilder.SetMdEntryType(MdIncGrp.Types.MdEntryType.OFFER);
            marketData.AddMdIncGroup(groupBuilder);

            marketData.Build().WriteTo(output);

            return output.Position;
        }

        public static long Decode(MarketDataIncrementalRefreshTrades.Builder marketData,
            byte[] input)
        {
            var builder = MarketDataIncrementalRefreshTrades.CreateBuilder();

            marketData.Clear();
            builder.MergeFrom(input).Build();

            ulong transactTime = marketData.TransactTime;
            uint eventTimeDelta = marketData.EventTimeDelta;
            MarketDataIncrementalRefreshTrades.Types.MatchEventIndicator matchEventIndicator =
                marketData.MatchEventIndicator;

            foreach (MdIncGrp mdIncGrp in marketData.MdIncGroupList)
            {
                ulong tradeId = mdIncGrp.TradeId;
                ulong securityId = mdIncGrp.SecurityId;
                long mantissa = mdIncGrp.MdEntryPx.Mantissa;
                int i = mdIncGrp.MdEntrySize.Mantissa;
                uint numberOfOrders = mdIncGrp.NumberOfOrders;
                MdIncGrp.Types.MdUpdateAction mdUpdateAction = mdIncGrp.MdUpdateAction;
                uint repSeq = mdIncGrp.RepSeq;
                MdIncGrp.Types.Side aggressorSide = mdIncGrp.AggressorSide;
                MdIncGrp.Types.MdEntryType mdEntryType = mdIncGrp.MdEntryType;
            }

            return input.Length;
        }
    }
}