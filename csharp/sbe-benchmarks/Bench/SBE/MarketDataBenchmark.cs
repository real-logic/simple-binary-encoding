using Org.SbeTool.Sbe.Dll;
using Uk.Co.Real_logic.Sbe.Benchmarks.Fix;

namespace Org.SbeTool.Sbe.Benchmarks
{
    public static class MarketDataBenchmark
    {
        public static int Encode(MessageHeader messageHeader,
            MarketDataIncrementalRefreshTrades marketData,
            DirectBuffer buffer,
            int bufferIndex)
        {
            messageHeader.Wrap(buffer, bufferIndex, 0);
            messageHeader.BlockLength = MarketDataIncrementalRefreshTrades.BlockLength;
            messageHeader.TemplateId = MarketDataIncrementalRefreshTrades.TemplateId;
            messageHeader.SchemaId = MarketDataIncrementalRefreshTrades.SchemaId;
            messageHeader.Version = MarketDataIncrementalRefreshTrades.SchemaVersion;

            marketData.WrapForEncode(buffer, bufferIndex + MessageHeader.Size);
            marketData.TransactTime = 1234L;
            marketData.EventTimeDelta = 987;
            marketData.MatchEventIndicator = MatchEventIndicator.END_EVENT;

            var mdIncGrp = marketData.MdIncGrpCount(2);

            mdIncGrp.Next();
            mdIncGrp.TradeId = 1234L;
            mdIncGrp.SecurityId = 56789L;
            mdIncGrp.MdEntryPx.Mantissa = 50;
            mdIncGrp.MdEntrySize.Mantissa = 10;
            mdIncGrp.NumberOfOrders = 1;
            mdIncGrp.MdUpdateAction = MDUpdateAction.NEW;
            mdIncGrp.RptSeq = 1;
            mdIncGrp.AggressorSide = Side.BUY;
            
            mdIncGrp.Next();
            mdIncGrp.TradeId = 1234L;
            mdIncGrp.SecurityId = 56789L;
            mdIncGrp.MdEntryPx.Mantissa = 50;
            mdIncGrp.MdEntrySize.Mantissa = 10;
            mdIncGrp.NumberOfOrders = 1;
            mdIncGrp.MdUpdateAction = MDUpdateAction.NEW;
            mdIncGrp.RptSeq = 1;
            mdIncGrp.AggressorSide = Side.SELL;
            
            return marketData.Size;
        }

        public static int Decode(MessageHeader messageHeader,
            MarketDataIncrementalRefreshTrades marketData,
            DirectBuffer buffer,
            int bufferIndex)
        {
            messageHeader.Wrap(buffer, bufferIndex, 0);

            int actingVersion = messageHeader.Version;
            int actingBlockLength = messageHeader.BlockLength;

            marketData.WrapForDecode(buffer, bufferIndex + MessageHeader.Size, actingBlockLength, actingVersion);

            var transactTime = marketData.TransactTime;
            var matchEventIndicator = marketData.MatchEventIndicator;

            var mdIncGrpGroup = marketData.MdIncGrp;
            while (mdIncGrpGroup.HasNext)
            {
                mdIncGrpGroup.Next();
                var tradeId = mdIncGrpGroup.TradeId;
                var securityId = mdIncGrpGroup.SecurityId;
                var mantissa = mdIncGrpGroup.MdEntryPx.Mantissa;
                var i = mdIncGrpGroup.MdEntrySize.Mantissa;
                var numberOfOrders = mdIncGrpGroup.NumberOfOrders;
                var mdUpdateAction = mdIncGrpGroup.MdUpdateAction;
                var rptSeq = mdIncGrpGroup.RptSeq;
                var aggressorSide = mdIncGrpGroup.AggressorSide;
                var mdEntryType = mdIncGrpGroup.MdEntryType;
            }

            return marketData.Size;
        }
    }
}