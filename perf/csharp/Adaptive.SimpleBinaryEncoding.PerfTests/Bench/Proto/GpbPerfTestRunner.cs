using System;
using System.Diagnostics;
using Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Proto
{
    public static class GpbPerfTestRunner
    {
        private static readonly double TicksToNanos = 1000*1000*1000/(double) Stopwatch.Frequency;

        public static long PerfTestEncode(int runNumber)
        {
            const int reps = 1*1000*1000;
            var state = new GpbBenchmarkState();
            int gcCount = GC.CollectionCount(0);

            Stopwatch sw = Stopwatch.StartNew();
            long size = 0;
            for (int i = 0; i < reps; i++)
            {
                size = GpbMarketDataBenchmark.Encode(state.Marketdata, state.Out);
            }
            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long) ((elapsedTicks/(double) reps)*TicksToNanos);

            Console.WriteLine("[{0}/Encode/Protobuf] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }

        public static long PerfTestDecode(int runNumber)
        {
            const int reps = 1*1000*1000;
            var state = new GpbBenchmarkState();
            int gcCount = GC.CollectionCount(0);

            Stopwatch sw = Stopwatch.StartNew();
            long size = 0;
            for (int i = 0; i < reps; i++)
            {
                size = GpbMarketDataBenchmark.Decode(state.Marketdata, state.In);
            }
            var elapsedTicks = sw.ElapsedTicks;
            var avgOpLatency = (long) ((elapsedTicks/(double) reps)*TicksToNanos);

            Console.WriteLine("[{0}/Decode/Protobuf] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);

            return avgOpLatency;
        }
    }
}