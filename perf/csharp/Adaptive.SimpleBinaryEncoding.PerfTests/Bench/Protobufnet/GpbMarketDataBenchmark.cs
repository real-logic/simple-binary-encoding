using System.IO;
using ProtoBuf;
using ProtoBuf.Meta;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Protobufnet
{
    public static class GpbMarketDataBenchmark
    {
        public static long Encode(MarketDataIncrementalRefreshTrades trades, Stream output, TypeModel compiled, ProtoWriter writer)
        {
            compiled.Serialize(writer, trades);
            
            return output.Position;
        }

        public static long Decode(MarketDataIncrementalRefreshTrades marketData,
            Stream input, TypeModel compiled, ProtoReader reader)
        {
            marketData.mdIncGroup.Clear();
            compiled.Deserialize(reader, marketData, typeof(MarketDataIncrementalRefreshTrades));

            ulong transactTime = marketData.transactTime;
            uint eventTimeDelta = marketData.eventTimeDelta;
            MarketDataIncrementalRefreshTrades.MatchEventIndicator matchEventIndicator = marketData.matchEventIndicator;

            foreach (MdIncGrp mdIncGrp in marketData.mdIncGroup)
            {
                ulong tradeId = mdIncGrp.tradeId;
                ulong securityId = mdIncGrp.securityId;
                long mantissa = mdIncGrp.mdEntryPx.mantissa;
                int i = mdIncGrp.mdEntrySize.mantissa;
                uint numberOfOrders = mdIncGrp.numberOfOrders;
                MdIncGrp.MdUpdateAction mdUpdateAction = mdIncGrp.mdUpdateAction;
                uint repSeq = mdIncGrp.repSeq;
                MdIncGrp.Side aggressorSide = mdIncGrp.aggressorSide;
                MdIncGrp.MdEntryType mdEntryType = mdIncGrp.mdEntryType;
            }

            return input.Length;
        }
    }
}