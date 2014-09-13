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
package uk.co.real_logic.protobuf;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import uk.co.real_logic.protobuf.fix.Fix;

public class MarketDataBenchmark
{
    @State(Scope.Benchmark)
    public static class MyState
    {
        final byte[] decodeBuffer;

        {
            try
            {
                decodeBuffer = encode();
            }
            catch (final Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    @Benchmark
    public byte[] testEncode(final MyState state) throws Exception
    {
        return encode();
    }

    @Benchmark
    public Fix.MarketDataIncrementalRefreshTrades testDecode(final MyState state) throws Exception
    {
        final byte[] buffer = state.decodeBuffer;

        return decode(buffer);
    }

    private static byte[] encode() throws Exception
    {
        final Fix.MarketDataIncrementalRefreshTrades.Builder marketData = Fix.MarketDataIncrementalRefreshTrades.newBuilder();

        marketData.clear()
                  .setTransactTime(1234L)
                  .setEventTimeDelta(987)
                  .setMatchEventIndicator(Fix.MarketDataIncrementalRefreshTrades.MatchEventIndicator.END_EVENT);

        final Fix.MdIncGrp.Builder mdIncGroupBuilder1 = Fix.MdIncGrp.newBuilder();
        mdIncGroupBuilder1.setTradeId(1234L);
        mdIncGroupBuilder1.setSecurityId(56789L);
        mdIncGroupBuilder1.getMdEntryPxBuilder().setMantissa(50);
        mdIncGroupBuilder1.getMdEntrySizeBuilder().setMantissa(10);
        mdIncGroupBuilder1.setNumberOfOrders(1);
        mdIncGroupBuilder1.setMdUpdateAction(Fix.MdIncGrp.MdUpdateAction.NEW);
        mdIncGroupBuilder1.setRepSeq(1);
        mdIncGroupBuilder1.setAggressorSide(Fix.MdIncGrp.Side.BUY);
        mdIncGroupBuilder1.setMdEntryType(Fix.MdIncGrp.MdEntryType.BID);
        marketData.addMdIncGroup(mdIncGroupBuilder1);

        final Fix.MdIncGrp.Builder mdIncGroupBuilder2 = Fix.MdIncGrp.newBuilder();
        mdIncGroupBuilder2.setTradeId(1234L);
        mdIncGroupBuilder2.setSecurityId(56789L);
        mdIncGroupBuilder2.getMdEntryPxBuilder().setMantissa(50);
        mdIncGroupBuilder2.getMdEntrySizeBuilder().setMantissa(10);
        mdIncGroupBuilder2.setNumberOfOrders(1);
        mdIncGroupBuilder2.setMdUpdateAction(Fix.MdIncGrp.MdUpdateAction.NEW);
        mdIncGroupBuilder2.setRepSeq(1);
        mdIncGroupBuilder2.setAggressorSide(Fix.MdIncGrp.Side.BUY);
        mdIncGroupBuilder2.setMdEntryType(Fix.MdIncGrp.MdEntryType.BID);
        marketData.addMdIncGroup(mdIncGroupBuilder2);

        return marketData.build().toByteArray();
    }

    private static Fix.MarketDataIncrementalRefreshTrades decode(final byte[] buffer) throws Exception
    {
        final Fix.MarketDataIncrementalRefreshTrades marketData = Fix.MarketDataIncrementalRefreshTrades.parseFrom(buffer);

        marketData.getTransactTime();
        marketData.getEventTimeDelta();
        marketData.getMatchEventIndicator();

        for (final Fix.MdIncGrp mdIncGrp : marketData.getMdIncGroupList())
        {
            mdIncGrp.getTradeId();
            mdIncGrp.getSecurityId();
            mdIncGrp.getMdEntryPx().getMantissa();
            mdIncGrp.getMdEntrySize().getMantissa();
            mdIncGrp.getNumberOfOrders();
            mdIncGrp.getMdUpdateAction();
            mdIncGrp.getRepSeq();
            mdIncGrp.getAggressorSide();
            mdIncGrp.getMdEntryType();
        }

        return marketData;
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args) throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            perfTestEncode(i);
            perfTestDecode(i);
        }
    }

    private static void perfTestEncode(final int runNumber) throws Exception
    {
        final int reps = 1 * 1000 * 1000;
        final MyState state = new MyState();
        final MarketDataBenchmark benchmark = new MarketDataBenchmark();

        byte[] marketData = null;
        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            marketData = benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testEncode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(marketData.length));
    }

    private static void perfTestDecode(final int runNumber) throws Exception
    {
        final int reps = 1 * 1000 * 1000;
        final MyState state = new MyState();
        final MarketDataBenchmark benchmark = new MarketDataBenchmark();

        Fix.MarketDataIncrementalRefreshTrades marketData = null;
        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            marketData = benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testDecode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(marketData.getMdIncGroupCount()));
    }
}
