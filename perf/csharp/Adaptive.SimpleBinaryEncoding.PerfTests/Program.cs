using System;
using Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE;

namespace Adaptive.SimpleBinaryEncoding.PerfTests
{
    public class Program
    {
        public static void Main()
        {
            Console.WriteLine("WARM UP");
            SbePerfTestRunner.PerfTestEncode(-1);
            SbePerfTestRunner.PerfTestDecode(-1);
            Bench.Proto.GpbPerfTestRunner.PerfTestEncode(-1);
            Bench.Proto.GpbPerfTestRunner.PerfTestDecode(-1);
            Bench.Protobufnet.GpbPerfTestRunner.PerfTestEncode(-1);
            Bench.Protobufnet.GpbPerfTestRunner.PerfTestDecode(-1);

            long sbeDecodeLatency = 0L;
            long sbeEncodeLatency = 0L;
            long gpbDecodeLatency = 0L;
            long gpbEncodeLatency = 0L;
            long gpbnDecodeLatency = 0L;
            long gpbnEncodeLatency = 0L;

            Console.WriteLine();
            Console.WriteLine("Running ...");

            const int runsCount = 5;

            for (int i = 0; i < runsCount; i++)
            {
                sbeEncodeLatency += SbePerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                sbeDecodeLatency += SbePerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);

                gpbEncodeLatency += Bench.Proto.GpbPerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                gpbDecodeLatency += Bench.Proto.GpbPerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);

                gpbnEncodeLatency += Bench.Protobufnet.GpbPerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                gpbnDecodeLatency += Bench.Protobufnet.GpbPerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);
            }

            Console.WriteLine("Latency ratio Google Protobuf / SBE - Decode: {0}", Math.Truncate((double)gpbDecodeLatency / sbeDecodeLatency *100) / 100);
            Console.WriteLine("Latency ratio Google Protobuf / SBE - Encode: {0}", Math.Truncate((double)gpbEncodeLatency / sbeEncodeLatency *100) / 100);
            Console.WriteLine("Latency ratio Google Protobuf-net / SBE - Decode: {0}", Math.Truncate((double)gpbnDecodeLatency / sbeDecodeLatency *100) / 100);
            Console.WriteLine("Latency ratio Google Protobuf-net / SBE - Encode: {0}", Math.Truncate((double)gpbnEncodeLatency / sbeEncodeLatency *100) / 100);

            Console.WriteLine("##teamcity[buildStatisticValue key='AverageEncodeLatencyNanos' value='{0:0.0}']", (double) sbeEncodeLatency / runsCount);
            Console.WriteLine("##teamcity[buildStatisticValue key='AverageDecodeLatencyNanos' value='{0:0.0}']", (double) sbeDecodeLatency / runsCount);
        }
    }
}