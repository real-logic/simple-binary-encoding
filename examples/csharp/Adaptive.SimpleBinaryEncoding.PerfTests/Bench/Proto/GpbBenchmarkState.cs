using System;
using System.IO;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Proto
{
    public class GpbBenchmarkState
    {
        public GpbBenchmarkState()
        {
            Marketdata = MarketDataIncrementalRefreshTrades.CreateBuilder();
            var input = new MemoryStream();
            GpbMarketDataBenchmark.Encode(Marketdata, input);
            In = input.ToArray();

            var outBuffer = new Byte[1024];
            Out = new MemoryStream(outBuffer);
        }

        public MarketDataIncrementalRefreshTrades.Builder Marketdata { get; private set; }
        public byte[] In { get; private set; }
        public MemoryStream Out { get; private set; }
    }
}