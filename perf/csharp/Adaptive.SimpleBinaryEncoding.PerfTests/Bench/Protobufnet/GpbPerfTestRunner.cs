using System;
using System.Diagnostics;
using Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Protobufnet
{
    public static class GpbPerfTestRunner
    {
        private const int Reps = 1*1000 * 1000;
        private static readonly double TicksToNanos = 1000 * 1000 * 1000 / (double)Stopwatch.Frequency;

        public static long PerfTestEncode(int runNumber)
        {
            var state = new GpbBenchmarkState();
            int gcCount = GC.CollectionCount(0);

            Stopwatch sw = Stopwatch.StartNew();
            long size = 0;
            for (int i = 0; i < Reps; i++)
            {
                state.Out.Position = 0; // Re-write over same memory (avoid re-allocating internal buffers)
                size = GpbMarketDataBenchmark.Encode(state.Marketdata, state.Out, state.Compiled, state.Writer);
            }
            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long) ((elapsedTicks/(double) Reps)*TicksToNanos);

            Console.WriteLine("[{0}/Encode/Protobuf-net] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }

        public static long PerfTestDecode(int runNumber)
        {
            var state = new GpbBenchmarkState();
            state.Marketdata = new MarketDataIncrementalRefreshTrades();
            int gcCount = GC.CollectionCount(0);

            Stopwatch sw = Stopwatch.StartNew();
            long size = 0;
            for (int i = 0; i < Reps; i++)
            {
                state.In.Position = 0; // Reset to start
                size = GpbMarketDataBenchmark.Decode(state.Marketdata, state.In, state.Compiled, state.Reader);
            }
            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long) ((elapsedTicks/(double) Reps)*TicksToNanos);

            Console.WriteLine("[{0}/Decode/Protobuf-net] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }
    }
}