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
#include "gtest/gtest.h"
#include "otf_api/Listener.h"

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

#include "OtfMessageTest.h"
#include "OtfMessageTestCBs.h"

class OtfGroupTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define NUM_ITERATIONS 3
#define GROUP_ID 10

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithRepeatingGroup");
        std::string groupDimensionStr = std::string("groupSizeEncoding");
        std::string groupStr = std::string("GroupName");
        std::string fieldStr = std::string("FieldName");

        ir.addToken(0, 0xFFFFFFFF, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_GROUP, byteOrder, Ir::NONE, GROUP_ID, groupStr);
        ir.addToken(0, 3, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, Ir::INVALID_ID, std::string("blockLength"));
        ir.addToken(2, 1, Ir::ENCODING, byteOrder, Ir::UINT8, Ir::INVALID_ID, std::string("numInGroup"));
        ir.addToken(0, 3, Ir::END_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 4, Ir::ENCODING, byteOrder, Ir::UINT32, Ir::INVALID_ID, std::string("uint32"));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 0, Ir::END_GROUP, byteOrder, Ir::NONE, GROUP_ID, groupStr);
        ir.addToken(0, 0xFFFFFFFF, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        *((uint16_t *)(buffer_ + bufferLen_)) = 4;
        bufferLen_ += 2;
        buffer_[bufferLen_++] = NUM_ITERATIONS;
        for (int i = 0; i < NUM_ITERATIONS; i++)
        {
            *((uint32_t *)(buffer_ + bufferLen_)) = i;
            bufferLen_ += 4;
        }
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            EXPECT_EQ(numGroupsSeen_, 1);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 0u);
        }
        else if (numFieldsSeen_ == 3)
        {
            EXPECT_EQ(numGroupsSeen_, 3);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 1u);
        }
        else if (numFieldsSeen_ == 4)
        {
            EXPECT_EQ(numGroupsSeen_, 5);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 2u);
        }
        return 0;
    };

    virtual int onNext(const Group &g)
    {
        OtfMessageTestCBs::onNext(g);

        EXPECT_EQ(g.name(), "GroupName");
        EXPECT_EQ(g.numInGroup(), NUM_ITERATIONS);
        if (numGroupsSeen_ == 1)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 1);
            EXPECT_EQ(g.iteration(), 0);
        }
        else if (numGroupsSeen_ == 2)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 2);
            EXPECT_EQ(g.iteration(), 0);
        }
        else if (numGroupsSeen_ == 3)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 2);
            EXPECT_EQ(g.iteration(), 1);
        }
        else if (numGroupsSeen_ == 4)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 1);
        }
        else if (numGroupsSeen_ == 5)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 2);
        }
        else if (numGroupsSeen_ == 6)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 4);
            EXPECT_EQ(g.iteration(), 2);
        }
        return 0;
    };

    virtual int onError(const Error &e)
    {
        return OtfMessageTestCBs::onError(e);
    };
};

TEST_F(OtfGroupTest, shouldHandleRepeatingGroup)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 4);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
    EXPECT_EQ(numGroupsSeen_, 6);
}

class OtfEmptyGroupTest : public OtfGroupTest
{
    virtual void constructMessage()
    {
        *((uint16_t *)(buffer_ + bufferLen_)) = 4;
        bufferLen_ += 2;
        buffer_[bufferLen_++] = 0;
    };

    virtual int onNext(const Field &f)
    {
        return OtfMessageTestCBs::onNext(f);
    };

    virtual int onNext(const Group &g)
    {
        return OtfMessageTestCBs::onNext(g);
    };

    virtual int onError(const Error &e)
    {
        std::cout << "ERROR: " << e.message() << std::endl;
        return OtfMessageTestCBs::onError(e);
    };
};

TEST_F(OtfEmptyGroupTest, shouldHandleEmptyRepeatingGroup)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 1);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
    EXPECT_EQ(numGroupsSeen_, 0);
}

TEST_F(OtfEmptyGroupTest, shouldHandleEmptyRepeatingGroupWithListenerReuse)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 1);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
    EXPECT_EQ(numGroupsSeen_, 0);

    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 2);
    EXPECT_EQ(numGroupsSeen_, 0);
}

class OtfNestedGroupTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define NUM_OUTER_ITERATIONS 3
#define NUM_INNER_ITERATIONS 2
#define GROUP_ID 10

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithNestedRepeatingGroup");
        std::string groupDimensionStr = std::string("groupSizeEncoding");
        std::string groupStr1 = std::string("OuterGroupName");
        std::string groupStr2 = std::string("InnerGroupName");
        std::string fieldStr = std::string("FieldName");

        ir.addToken(0, 0xFFFFFFFF, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_GROUP, byteOrder, Ir::NONE, GROUP_ID, groupStr1);
        ir.addToken(0, 3, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, Ir::INVALID_ID, std::string("blockLength"));
        ir.addToken(2, 1, Ir::ENCODING, byteOrder, Ir::UINT8, Ir::INVALID_ID, std::string("numInGroup"));
        ir.addToken(0, 3, Ir::END_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 0, Ir::BEGIN_GROUP, byteOrder, Ir::NONE, GROUP_ID + 1, groupStr2);
        ir.addToken(0, 3, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, Ir::INVALID_ID, std::string("blockLength"));
        ir.addToken(2, 1, Ir::ENCODING, byteOrder, Ir::UINT8, Ir::INVALID_ID, std::string("numInGroup"));
        ir.addToken(0, 3, Ir::END_COMPOSITE, byteOrder, Ir::NONE, Ir::INVALID_ID, groupDimensionStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 4, Ir::ENCODING, byteOrder, Ir::UINT32, Ir::INVALID_ID, std::string("uint32"));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 0, Ir::END_GROUP, byteOrder, Ir::NONE, GROUP_ID + 1, groupStr2);
        ir.addToken(0, 0, Ir::END_GROUP, byteOrder, Ir::NONE, GROUP_ID, groupStr1);
        ir.addToken(0, 0xFFFFFFFF, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        *((uint16_t *)(buffer_ + bufferLen_)) = 4;
        bufferLen_ += 2;
        buffer_[bufferLen_++] = NUM_OUTER_ITERATIONS;
        for (int i = 0; i < NUM_OUTER_ITERATIONS; i++)
        {
            *((uint16_t *)(buffer_ + bufferLen_)) = 4;
            bufferLen_ += 2;
            buffer_[bufferLen_++] = NUM_INNER_ITERATIONS;
            for (int j = 0; j < NUM_INNER_ITERATIONS; j++)
            {
                *((uint32_t *)(buffer_ + bufferLen_)) = i + j;
                bufferLen_ += 4;
            }
        }
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            EXPECT_EQ(numGroupsSeen_, 2);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 0u);
        }
        else if (numFieldsSeen_ == 3)
        {
            EXPECT_EQ(numGroupsSeen_, 4);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 1u);
        }
        else if (numFieldsSeen_ == 4)
        {
            EXPECT_EQ(numGroupsSeen_, 8);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 1u);
        }
        else if (numFieldsSeen_ == 5)
        {
            EXPECT_EQ(numGroupsSeen_, 10);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 2u);
        }
        else if (numFieldsSeen_ == 6)
        {
            EXPECT_EQ(numGroupsSeen_, 14);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 2u);
        }
        else if (numFieldsSeen_ == 7)
        {
            EXPECT_EQ(numGroupsSeen_, 16);
            EXPECT_EQ(f.fieldName(), "FieldName");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.getUInt(), 3u);
        }
        return 0;
    };

    virtual int onNext(const Group &g)
    {
        OtfMessageTestCBs::onNext(g);

        if (numGroupsSeen_ == 1)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 1);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 2)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 1);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 3)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 2);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 4)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 2);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 5)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 6)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 7)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 8)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 3);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 9)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 4);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 10)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 4);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 11)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 5);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 12)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 5);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 13)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 5);
            EXPECT_EQ(g.iteration(), 2);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 14)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 5);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 15)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 6);
            EXPECT_EQ(g.iteration(), 0);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 16)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(numFieldsSeen_, 6);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 17)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 7);
            EXPECT_EQ(g.iteration(), 1);
            EXPECT_EQ(g.name(), "InnerGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_INNER_ITERATIONS);
        }
        else if (numGroupsSeen_ == 18)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(numFieldsSeen_, 7);
            EXPECT_EQ(g.iteration(), 2);
            EXPECT_EQ(g.name(), "OuterGroupName");
            EXPECT_EQ(g.numInGroup(), NUM_OUTER_ITERATIONS);
        }
        return 0;
    };

    virtual int onError(const Error &e)
    {
        return OtfMessageTestCBs::onError(e);
    };
};

TEST_F(OtfNestedGroupTest, shouldHandleNestedRepeatingGroup)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 7);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
    EXPECT_EQ(numGroupsSeen_, 18);
}

TEST_F(OtfNestedGroupTest, shouldHandleNestedRepeatingGroupWithListenerReuse)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 7);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
    EXPECT_EQ(numGroupsSeen_, 18);

    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 14);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 2);
    EXPECT_EQ(numGroupsSeen_, 36);
}
