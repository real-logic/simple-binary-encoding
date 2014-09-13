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
package uk.co.real_logic.sbe;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.samples.fix.*;

import java.nio.ByteBuffer;

public class MarketDataBenchmark
{
    @State(Scope.Benchmark)
    public static class MyState
    {
        final int bufferIndex = 0;
        final MarketDataIncrementalRefreshTrades marketData = new MarketDataIncrementalRefreshTrades();
        final MessageHeader messageHeader = new MessageHeader();
        final DirectBuffer encodeBuffer = new DirectBuffer(ByteBuffer.allocateDirect(1024));

        final DirectBuffer decodeBuffer = new DirectBuffer(ByteBuffer.allocateDirect(1024));

        {
            MarketDataBenchmark.encode(messageHeader, marketData, decodeBuffer, bufferIndex);
        }
    }

    @Benchmark
    public int testEncode(final MyState state)
    {
        final MarketDataIncrementalRefreshTrades marketData = state.marketData;
        final MessageHeader messageHeader = state.messageHeader;
        final DirectBuffer buffer = state.encodeBuffer;
        final int bufferIndex = state.bufferIndex;

        encode(messageHeader, marketData, buffer, bufferIndex);

        return marketData.size();
    }

    @Benchmark
    public int testDecode(final MyState state)
    {
        final MarketDataIncrementalRefreshTrades marketData = state.marketData;
        final MessageHeader messageHeader = state.messageHeader;
        final DirectBuffer buffer = state.decodeBuffer;
        final int bufferIndex = state.bufferIndex;

        decode(messageHeader, marketData, buffer, bufferIndex);

        return marketData.size();
    }

    public static void encode(
        final MessageHeader messageHeader,
        final MarketDataIncrementalRefreshTrades marketData,
        final DirectBuffer buffer,
        final int bufferIndex)
    {
        messageHeader.wrap(buffer, bufferIndex, 0)
                     .blockLength(marketData.sbeBlockLength())
                     .templateId(marketData.sbeTemplateId())
                     .schemaId(marketData.sbeSchemaId())
                     .version(marketData.sbeSchemaVersion());

        marketData.wrapForEncode(buffer, bufferIndex + messageHeader.size())
                  .transactTime(1234L)
                  .eventTimeDelta(987)
                  .matchEventIndicator(MatchEventIndicator.END_EVENT);

        final MarketDataIncrementalRefreshTrades.MdIncGrp mdIncGrp = marketData.mdIncGrpCount(2);

        mdIncGrp.next();
        mdIncGrp.tradeId(1234L);
        mdIncGrp.securityId(56789L);
        mdIncGrp.mdEntryPx().mantissa(50);
        mdIncGrp.mdEntrySize().mantissa(10);
        mdIncGrp.numberOfOrders(1);
        mdIncGrp.mdUpdateAction(MDUpdateAction.NEW);
        mdIncGrp.rptSeq((short)1);
        mdIncGrp.aggressorSide(Side.BUY);
        mdIncGrp.mdEntryType(MDEntryType.BID);

        mdIncGrp.next();
        mdIncGrp.tradeId(1234L);
        mdIncGrp.securityId(56789L);
        mdIncGrp.mdEntryPx().mantissa(50);
        mdIncGrp.mdEntrySize().mantissa(10);
        mdIncGrp.numberOfOrders(1);
        mdIncGrp.mdUpdateAction(MDUpdateAction.NEW);
        mdIncGrp.rptSeq((short)1);
        mdIncGrp.aggressorSide(Side.SELL);
        mdIncGrp.mdEntryType(MDEntryType.OFFER);
    }


    private static void decode(
        final MessageHeader messageHeader,
        final MarketDataIncrementalRefreshTrades marketData,
        final DirectBuffer buffer,
        final int bufferIndex)
    {
        messageHeader.wrap(buffer, bufferIndex, 0);

        final int actingVersion = messageHeader.version();
        final int actingBlockLength = messageHeader.blockLength();

        marketData.wrapForDecode(buffer, bufferIndex + messageHeader.size(), actingBlockLength, actingVersion);

        marketData.transactTime();
        marketData.eventTimeDelta();
        marketData.matchEventIndicator();

        for (final MarketDataIncrementalRefreshTrades.MdIncGrp mdIncGrp : marketData.mdIncGrp())
        {
            mdIncGrp.tradeId();
            mdIncGrp.securityId();
            mdIncGrp.mdEntryPx().mantissa();
            mdIncGrp.mdEntrySize().mantissa();
            mdIncGrp.numberOfOrders();
            mdIncGrp.mdUpdateAction();
            mdIncGrp.rptSeq();
            mdIncGrp.aggressorSide();
            mdIncGrp.mdEntryType();
        }
    }

    /*
     * Benchmarks to allow execution outside of JMH.
     */

    public static void main(final String[] args)
    {
        for (int i = 0; i < 10; i++)
        {
            perfTestEncode(i);
            perfTestDecode(i);
        }
    }

    private static void perfTestEncode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final MarketDataBenchmark benchmark = new MarketDataBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testEncode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testEncode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(state.marketData.size() + state.messageHeader.size()));
    }

    private static void perfTestDecode(final int runNumber)
    {
        final int reps = 10 * 1000 * 1000;
        final MyState state = new MyState();
        final MarketDataBenchmark benchmark = new MarketDataBenchmark();

        final long start = System.nanoTime();
        for (int i = 0; i < reps; i++)
        {
            benchmark.testDecode(state);
        }

        final long totalDuration = System.nanoTime() - start;

        System.out.printf(
            "%d - %d(ns) average duration for %s.testDecode() - message size %d\n",
            Integer.valueOf(runNumber),
            Long.valueOf(totalDuration / reps),
            benchmark.getClass().getName(),
            Integer.valueOf(state.marketData.size() + state.messageHeader.size()));
    }
}
