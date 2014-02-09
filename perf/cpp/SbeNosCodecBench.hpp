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
#ifndef _SBE_NOS_CODEC_BENCH_HPP
#define _SBE_NOS_CODEC_BENCH_HPP

#include "CodecBench.hpp"
#include "uk_co_real_logic_sbe_samples_fix/NewOrder.hpp"

using namespace uk_co_real_logic_sbe_samples_fix;

// 12
char ACCOUNT[] = "NewJackHawt1";
// 20
char CLORDID[] = "NewJackHawtClOrdId01";
// 6
char SYMBOL[] = "SBEOPT";
// 10
char ALLOCACCOUNT[] = "NewJackets";
// 20
char SECURITYDESC[] = "DataLayoutHasOptions";
// 3
char SECURITYTYPE[] = "UHH";
// 12
char SELFMATCHPREVENTIONID[] = "DONTMATCHID1";
// 3
char GIVEUPFIRM[] = "OHH";
// 2
char CMTAGIVEUPCD[] = "OH";
// 20
char CORRELATIONCLORDID[] = "NewJackHawtClOrdId01";

class SbeNewOrderSingleCodecBench : public CodecBench<SbeNewOrderSingleCodecBench>
{
public:
    virtual int encode(char *buffer)
    {
        nos_.wrapForEncode(buffer, 0)
            .putAccount(ACCOUNT)
            .putClOrdID(CLORDID)
            .handInst(HandInst::AUTOMATED_EXECUTION)
            .custOrderHandlingInst(CustOrderHandlingInst::ALGO_ENGINE);

        nos_.orderQty().mantissa(10);

        nos_.ordType(OrdType::MARKET_ORDER);

        nos_.price()
            .mantissa(3509)
            .exponent(-2);

        nos_.side(Side::BUY)
            .putSymbol(SYMBOL)
            .timeInForce(TimeInForce::GOOD_TILL_CANCEL)
            .transactTime(0xFFFFFFFFFEFE)
            .manualOrderIndicator(BooleanType::FIX_FALSE)
            .putAllocAccount(ALLOCACCOUNT);

        nos_.stopPx()
            .mantissa(3510)
            .exponent(-2);

        nos_.putSecurityDesc(SECURITYDESC);

        nos_.minQty().mantissa(9);

        nos_.putSecurityType(SECURITYTYPE)
            .customerOrFirm(CustomerOrFirm::CUSTOMER);

        nos_.maxShow().mantissa(5);

        nos_.expireDate(1210)
            .putSelfMatchPreventionID(SELFMATCHPREVENTIONID)
            .ctiCode(CtiCode::OWN)
            .putGiveUpFirm(GIVEUPFIRM)
            .putCmtaGiveupCD(CMTAGIVEUPCD)
            .putCorrelationClOrdID(CORRELATIONCLORDID);

        return nos_.size();
    };

    virtual int decode(const char *buffer)
    {
        nos_.wrapForDecode((char *)buffer, 0, nos_.blockLength(), nos_.templateVersion());

        nos_.account();
        nos_.clOrdID();
        nos_.handInst();
        nos_.custOrderHandlingInst();
        nos_.orderQty().mantissa();
        nos_.ordType();
        nos_.price().mantissa();
        nos_.price().exponent();
        nos_.side();
        nos_.symbol();
        nos_.timeInForce();
        nos_.transactTime();
        nos_.manualOrderIndicator();
        nos_.allocAccount();
        nos_.stopPx().mantissa();
        nos_.stopPx().exponent();
        nos_.securityDesc();
        nos_.minQty().mantissa();
        nos_.securityType();
        nos_.customerOrFirm();
        nos_.maxShow().mantissa();
        nos_.expireDate();
        nos_.selfMatchPreventionID();
        nos_.ctiCode();
        nos_.giveUpFirm();
        nos_.cmtaGiveupCD();
        nos_.correlationClOrdID();

        return nos_.size();
    };

private:
    NewOrder nos_;
};

#endif /* _SBE_NOS_CODEC_BENCH_HPP */
