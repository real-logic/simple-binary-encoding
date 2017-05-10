// Portions Copyright (C) 2017 MarketFactory, Inc
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System;

namespace Org.SbeTool.Sbe.Benchmarks
{
    public class Program
    {
        public static void Main()
        {
            Console.WriteLine("WARM UP");
            SbePerfTestRunner.PerfTestEncode(-1);
            SbePerfTestRunner.PerfTestDecode(-1);

            long sbeDecodeLatency = 0L;
            long sbeEncodeLatency = 0L;

            Console.WriteLine();
            Console.WriteLine("Running ...");

            const int runsCount = 5;

            for (int i = 0; i < runsCount; i++)
            {
                sbeEncodeLatency += SbePerfTestRunner.PerfTestEncode(i);
                GC.Collect(2);

                sbeDecodeLatency += SbePerfTestRunner.PerfTestDecode(i);
                GC.Collect(2);
            }

            Console.WriteLine("##teamcity[buildStatisticValue key='AverageEncodeLatencyNanos' value='{0:0.0}']", (double) sbeEncodeLatency / runsCount);
            Console.WriteLine("##teamcity[buildStatisticValue key='AverageDecodeLatencyNanos' value='{0:0.0}']", (double) sbeDecodeLatency / runsCount);

            Console.WriteLine("Press a key to continue...");
            Console.ReadKey();
        }
    }
}
