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
#ifndef _PB_MARKET_DATA_CODEC_BENCH_HPP
#define _PB_MARKET_DATA_CODEC_BENCH_HPP

#include <string>

#include "CodecBench.hpp"
#include "fix-messages.pb.h"

using namespace uk::co::real_logic::protobuf::examples;

class PbMarketDataCodecBench : public CodecBench<PbMarketDataCodecBench>
{
public:
    virtual int encode(char *buffer)
    {
        marketData_.Clear();
        marketData_.set_transacttime(1234L);
        marketData_.set_eventtimedelta(987);
        marketData_.set_matcheventindicator(MarketDataIncrementalRefreshTrades_MatchEventIndicator_END_EVENT);

        MdIncGrp *mdIncGrp =  marketData_.add_mdincgroup();
        mdIncGrp->set_tradeid(1234L);
        mdIncGrp->set_securityid(56789L);
        mdIncGrp->mutable_mdentrypx()->set_mantissa(50);
        mdIncGrp->mutable_mdentrysize()->set_mantissa(10);
        mdIncGrp->set_numberoforders(1);
        mdIncGrp->set_mdupdateaction(MdIncGrp_MdUpdateAction_NEW);
        mdIncGrp->set_repseq((short)1);
        mdIncGrp->set_aggressorside(MdIncGrp_Side_BUY);
        mdIncGrp->set_mdentrytype(MdIncGrp_MdEntryType_BID);

        mdIncGrp =  marketData_.add_mdincgroup();
        mdIncGrp->set_tradeid(1234L);
        mdIncGrp->set_securityid(56789L);
        mdIncGrp->mutable_mdentrypx()->set_mantissa(50);
        mdIncGrp->mutable_mdentrysize()->set_mantissa(10);
        mdIncGrp->set_numberoforders(1);
        mdIncGrp->set_mdupdateaction(MdIncGrp_MdUpdateAction_NEW);
        mdIncGrp->set_repseq((short)1);
        mdIncGrp->set_aggressorside(MdIncGrp_Side_SELL);
        mdIncGrp->set_mdentrytype(MdIncGrp_MdEntryType_OFFER);

        marketData_.SerializeToArray(buffer, marketData_.ByteSize());

        return marketData_.ByteSize();
    };

    virtual int decode(const char *buffer)
    {
        marketData_.ParseFromArray(buffer, 10000);

        marketData_.transacttime();
        marketData_.eventtimedelta();
        marketData_.matcheventindicator();

        for (int i = 0, max = marketData_.mdincgroup_size(); i < max; i++)
        {
            const MdIncGrp &mdIncGrp = marketData_.mdincgroup(i);
            mdIncGrp.tradeid();
            mdIncGrp.securityid();
            mdIncGrp.mdentrypx().mantissa();
            mdIncGrp.mdentrysize().mantissa();
            mdIncGrp.numberoforders();
            mdIncGrp.mdupdateaction();
            mdIncGrp.repseq();
            mdIncGrp.aggressorside();
            mdIncGrp.mdentrytype();
        }

        return marketData_.ByteSize();
    };

private:
    MarketDataIncrementalRefreshTrades marketData_;
};

#endif /* _PB_MARKET_DATA_CODEC_BENCH_HPP */
