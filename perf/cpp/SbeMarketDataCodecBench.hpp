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
#ifndef _SBE_MARKET_DATA_CODEC_BENCH_HPP
#define _SBE_MARKET_DATA_CODEC_BENCH_HPP

#include "CodecBench.hpp"
#include "uk_co_real_logic_sbe_samples_fix/MessageHeader.hpp"
#include "uk_co_real_logic_sbe_samples_fix/MarketDataIncrementalRefreshTrades.hpp"

using namespace uk_co_real_logic_sbe_samples_fix;

class SbeMarketDataCodecBench : public CodecBench<SbeMarketDataCodecBench>
{
public:
    virtual int encode(char *buffer, const int bufferLength)
    {
        messageHeader_.wrap(buffer, 0, 0, bufferLength);
        messageHeader_.blockLength(marketData_.sbeBlockLength());
        messageHeader_.templateId(marketData_.sbeTemplateId());
        messageHeader_.schemaId(marketData_.sbeSchemaId());
        messageHeader_.version(marketData_.sbeSchemaVersion());

        marketData_.wrapForEncode(buffer + messageHeader_.size(), 0, bufferLength);
        marketData_.transactTime(1234L);
        marketData_.eventTimeDelta(987);
        marketData_.matchEventIndicator(MatchEventIndicator::END_EVENT);

        MarketDataIncrementalRefreshTrades::MdIncGrp &mdIncGrp = marketData_.mdIncGrpCount(2);

        mdIncGrp.next();
        mdIncGrp.tradeId(1234L);
        mdIncGrp.securityId(56789L);
        mdIncGrp.mdEntryPx().mantissa(50);
        mdIncGrp.mdEntrySize().mantissa(10);
        mdIncGrp.numberOfOrders(1);
        mdIncGrp.mdUpdateAction(MDUpdateAction::NEW);
        mdIncGrp.rptSeq((short)1);
        mdIncGrp.aggressorSide(Side::BUY);
        mdIncGrp.mdEntryType(MDEntryType::BID);

        mdIncGrp.next();
        mdIncGrp.tradeId(1234L);
        mdIncGrp.securityId(56789L);
        mdIncGrp.mdEntryPx().mantissa(50);
        mdIncGrp.mdEntrySize().mantissa(10);
        mdIncGrp.numberOfOrders(1);
        mdIncGrp.mdUpdateAction(MDUpdateAction::NEW);
        mdIncGrp.rptSeq((short)1);
        mdIncGrp.aggressorSide(Side::SELL);
        mdIncGrp.mdEntryType(MDEntryType::OFFER);

        return MessageHeader::size() + marketData_.size();
    };

    virtual int decode(const char *buffer, const int bufferLength)
    {
        int64_t actingVersion;
        int64_t actingBlockLength;

        messageHeader_.wrap((char *)buffer, 0, 0, bufferLength);

        actingBlockLength = messageHeader_.blockLength();
        actingVersion = messageHeader_.version();


        marketData_.wrapForDecode((char *)buffer, messageHeader_.size(), actingBlockLength, actingVersion, bufferLength);

        marketData_.transactTime();
        marketData_.eventTimeDelta();
        marketData_.matchEventIndicator();

        MarketDataIncrementalRefreshTrades::MdIncGrp &mdIncGrp = marketData_.mdIncGrp();
        while (mdIncGrp.hasNext())
        {
            mdIncGrp.next();
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

        return MessageHeader::size() + marketData_.size();
    };

private:
    MessageHeader messageHeader_;
    MarketDataIncrementalRefreshTrades marketData_;
};

#endif /* _SBE_MARKET_DATA_CODEC_BENCH_HPP */
