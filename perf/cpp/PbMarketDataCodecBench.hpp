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

/*
 * This test is deliberately setup to be unfair to SBE. SBE tests use a messageHeader for length, etc. In
 * these PB tests, we will use a static length field at the start of the message to contain the length. A full
 * comparison would use another PB with the equivalent messageHeader fields.
 */

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

        // grab and cache size. This will scan the message. Refer to Kenton Varda #128
        int len = marketData_.ByteSize();

        // add length field so decode knows length (SBE will have full messageHeader instead)
        *((int *)buffer) = len;

        // Refer to Kenton Varda #128
        marketData_.SerializeWithCachedSizesToArray((::google::protobuf::uint8*)(buffer + sizeof(int)));

        return len;
    };

    virtual int decode(const char *buffer)
    {
        int len = *((int *)buffer);

        if (marketData_.ParseFromArray(buffer + sizeof(int), len) == false)
        {
            std::cout << "error in PB parse" << std::endl;
            exit(1);
        }

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

        // just return len. No need to call ByteSize() which will scan message
        return len;
    };

private:
    MarketDataIncrementalRefreshTrades marketData_;
};

#endif /* _PB_MARKET_DATA_CODEC_BENCH_HPP */
