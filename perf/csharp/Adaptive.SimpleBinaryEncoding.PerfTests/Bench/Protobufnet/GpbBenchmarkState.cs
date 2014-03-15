using System;
using System.IO;
using ProtoBuf;
using ProtoBuf.Meta;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Protobufnet
{
    public class GpbBenchmarkState
    {
        public GpbBenchmarkState()
        {
            Marketdata = CreateSampleData();
            var input = new MemoryStream();
            Out = input;
            In = input;

            var model = TypeModel.Create();
            Type type = typeof(MarketDataIncrementalRefreshTrades);
            model.Deserialize(Out, null, type);
            Out.Position = 0;
            Compiled = model.Compile();

            Writer = new ProtoWriter(Out, model, null);
            Reader = new ProtoReader(In, model, null);

            GpbMarketDataBenchmark.Encode(Marketdata, input, Compiled, Writer);
        }

        public TypeModel Compiled { get; set; }
        public MarketDataIncrementalRefreshTrades Marketdata { get; set; }
        public Stream In { get; private set; }
        public MemoryStream Out { get; private set; }
        public ProtoWriter Writer { get; private set; }
        public ProtoReader Reader { get; private set; }

        private static MarketDataIncrementalRefreshTrades CreateSampleData()
        {
            MarketDataIncrementalRefreshTrades trades = new MarketDataIncrementalRefreshTrades();
            trades.transactTime = 1234L;
            trades.eventTimeDelta = 987;
            trades.matchEventIndicator = MarketDataIncrementalRefreshTrades.MatchEventIndicator.END_EVENT;

            MdIncGrp groupBuilder = new MdIncGrp();
            groupBuilder.tradeId = 1234L;
            groupBuilder.securityId = 56789L;
            groupBuilder.mdEntryPx = new Decimal64();
            groupBuilder.mdEntryPx.mantissa = 50;
            groupBuilder.mdEntrySize = new IntQty32();
            groupBuilder.mdEntrySize.mantissa = 10;
            groupBuilder.numberOfOrders = 1;
            groupBuilder.mdUpdateAction = MdIncGrp.MdUpdateAction.NEW;
            groupBuilder.repSeq = 1;
            groupBuilder.aggressorSide = MdIncGrp.Side.BUY;
            groupBuilder.mdEntryType = MdIncGrp.MdEntryType.BID;
            trades.mdIncGroup.Add(groupBuilder);

            groupBuilder = new MdIncGrp();
            groupBuilder.tradeId = 1234L;
            groupBuilder.securityId = 56789L;
            groupBuilder.mdEntryPx = new Decimal64();
            groupBuilder.mdEntryPx.mantissa = 50;
            groupBuilder.mdEntrySize = new IntQty32();
            groupBuilder.mdEntrySize.mantissa = 10;
            groupBuilder.numberOfOrders = 1;
            groupBuilder.mdUpdateAction = MdIncGrp.MdUpdateAction.NEW;
            groupBuilder.repSeq = 1;
            groupBuilder.aggressorSide = MdIncGrp.Side.SELL;
            groupBuilder.mdEntryType = MdIncGrp.MdEntryType.OFFER;
            trades.mdIncGroup.Add(groupBuilder);
            return trades;
        }
    }
}