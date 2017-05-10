using System;
using System.Diagnostics;
using Org.SbeTool.Sbe.Dll;
using Uk.Co.Real_logic.Sbe.Benchmarks.Fix;

namespace Org.SbeTool.Sbe.Benchmarks
{
    public static class SbePerfTestRunner
    {
        static readonly double TicksToNanos = 1000 * 1000 * 1000 / (double)Stopwatch.Frequency;

        public static long PerfTestEncode(int runNumber)
        {
            const int reps = 10 * 1000 * 1000;
            var state = new BenchmarkState();
            var gcCount = GC.CollectionCount(0);

            var sw = Stopwatch.StartNew();
            var size = 0;
            for (int i = 0; i < reps; i++)
            {
                size = MarketDataBenchmark.Encode(state.MessageHeader, state.MarketData, state.EncodeBuffer, state.BufferIndex);
            }

            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long)((elapsedTicks / (double)reps) * TicksToNanos);

            Console.WriteLine("[{0}/Encode/SBE] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }

        public static long PerfTestDecode(int runNumber)
        {
            const int reps = 10 * 1000 * 1000;
            var state = new BenchmarkState();
            var marketDataSize = 0;

            var gcCount = GC.CollectionCount(0);

            var sw = Stopwatch.StartNew();
            for (int i = 0; i < reps; i++)
            {
                marketDataSize = MarketDataBenchmark.Decode(state.MessageHeader, state.MarketData, state.DecodeBuffer, state.BufferIndex);
            }

            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long)((elapsedTicks / (double)reps) * TicksToNanos);

            Console.WriteLine("[{0}/Decode/SBE] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                marketDataSize + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }
    }
}