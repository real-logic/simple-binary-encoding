using System;
using System.Diagnostics;
using Uk.Co.Real_logic.Sbe.Samples.Fix;

namespace Adaptive.SimpleBinaryEncoding.PerfTests
{
    public class Program
    {
        static readonly double TicksToNanos = 1000 * 1000 * 1000 / (double)Stopwatch.Frequency;

        public static void Main()
        {
            for (int i = 0; i < 10; i++)
            {
                PerfTestEncode(i);
                PerfTestDecode(i);
            }
        }

        private static void PerfTestEncode(int runNumber)
        {
            const int reps = 10*1000*1000;
            var state = new BenchmarkState();

            var sw = Stopwatch.StartNew();
            var gcCount = GC.CollectionCount(0);
            var size = 0;
            for (int i = 0; i < reps; i++)
            {
                size = MarketDataBenchmark.Encode(state.MessageHeader, state.MarketData, state.EncodeBuffer, state.BufferIndex);
            }
            
            var avgOpLatency = (long)(((sw.ElapsedTicks) / (double)reps) * TicksToNanos);

            Console.WriteLine("[{0}/Encode] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                size + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);
        }

        private static void PerfTestDecode(int runNumber)
        {
            const int reps = 10*1000*1000;
            var state = new BenchmarkState();
            var marketDataSize = 0;

            var sw = Stopwatch.StartNew();
            var gcCount = GC.CollectionCount(0);
            for (int i = 0; i < reps; i++)
            {
                marketDataSize = MarketDataBenchmark.Decode(state.MessageHeader, state.MarketData, state.DecodeBuffer, state.BufferIndex);
            }

            var avgOpLatency = (long)(((sw.ElapsedTicks) / (double)reps) * TicksToNanos);

            Console.WriteLine("[{0}/Decode] - {1}(ns) average latency - message size: {2} - GC count: {3}",
                runNumber,
                avgOpLatency,
                marketDataSize + MessageHeader.Size,
                GC.CollectionCount(0) - gcCount);
        }
    }
}