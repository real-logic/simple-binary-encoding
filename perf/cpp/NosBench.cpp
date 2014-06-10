/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "benchlet.hpp"
#include "SbeNosCodecBench.hpp"

#define MAX_NOS_BUFFER (1000*1000)
#define MAX_N 10

class SbeNewOrderSingleBench : public Benchmark
{
public:
    virtual void setUp(void)
    {
        buffer_ = new char[MAX_NOS_BUFFER];
        bench_.runEncode(buffer_, MAX_N);  // set buffer up for decoding runs
        std::cout << "MAX N = " << MAX_N << std::endl;
    };

    virtual void tearDown(void)
    {
        delete[] buffer_;
    };

    SbeNewOrderSingleCodecBench bench_;
    char *buffer_;
};

static struct Benchmark::Config cfg[] = {
    { Benchmark::ITERATIONS, "1000000" },
    { Benchmark::BATCHES, "20" }
};

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunSingleEncode, cfg)
{
    bench_.runEncode(buffer_);
}

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunSingleDecode, cfg)
{
    bench_.runDecode(buffer_);
}

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunSingleEncodeAndDecode, cfg)
{
    bench_.runEncodeAndDecode(buffer_);
}

static struct Benchmark::Config cfgMulti[] = {
    { Benchmark::ITERATIONS, "100000" },
    { Benchmark::BATCHES, "20" }
};

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunMultipleEncode, cfgMulti)
{
    bench_.runEncode(buffer_, MAX_N);
}

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunMultipleDecode, cfgMulti)
{
    bench_.runDecode(buffer_, MAX_N);
}

BENCHMARK_CONFIG(SbeNewOrderSingleBench, RunMultipleEncodeAndDecode, cfgMulti)
{
    bench_.runEncodeAndDecode(buffer_, MAX_N);
}
