using System;
using Adaptive.SimpleBinaryEncoding.PerfTests.Bench.Proto;
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
            GpbPerfTestRunner.PerfTestEncode(-1);
            GpbPerfTestRunner.PerfTestDecode(-1);

            long sbeDecodeLatency = 0L;
            long sbeEncodeLatency = 0L;
            long gpbDecodeLatency = 0L;
            long gpbEncodeLatency = 0L;

            Console.WriteLine();
            Console.WriteLine("Running ...");

            for (int i = 0; i < 5; i++)
            {
                sbeEncodeLatency += SbePerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                sbeDecodeLatency += SbePerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);

                gpbEncodeLatency += GpbPerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                gpbDecodeLatency += GpbPerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);
            }

            Console.WriteLine("Latency ratio Google Protobuf / SBE - Decode: {0}", Math.Truncate((double)gpbDecodeLatency / sbeDecodeLatency *100) / 100);
            Console.WriteLine("Latency ratio Google Protobuf / SBE - Encode: {0}", Math.Truncate((double)gpbEncodeLatency / sbeEncodeLatency *100) / 100);
        }
    }
}