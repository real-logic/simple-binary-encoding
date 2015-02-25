/*
 * Copyright 2014 Real Logic Ltd.
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
#include <iostream>

#include "gtest/gtest.h"
#include "message_block_length_test/MessageHeader.hpp"
#include "message_block_length_test/MsgName.hpp"
#include "otf_api/Ir.h"
#include "otf_api/IrCollection.h"
#include "otf_api/Listener.h"

using namespace std;
using namespace message_block_length_test;

class MessageBlockLengthIrTest : public testing::Test, public IrCollection, public Ir::Callback,
                                 public OnNext, public OnError, public OnCompleted
{
public:
    char buffer[2048];
    Listener listener;
    int eventNumber_;

    virtual void SetUp()
    {
        eventNumber_ = 0;
    }

    virtual int encodeHdrAndMsg()
    {
        MessageHeader hdr_;
        MsgName msg_;

        hdr_.wrap(buffer, 0, 0, sizeof(buffer))
            .blockLength(MsgName::sbeBlockLength())
            .templateId(MsgName::sbeTemplateId())
            .schemaId(MsgName::sbeSchemaId())
            .version(MsgName::sbeSchemaVersion());

        msg_.wrapForEncode(buffer, hdr_.size(), sizeof(buffer));

        msg_.field1(187);
        msg_.field2().clear()
            .choice1(true);

        MsgName::GrName &grp = msg_.grNameCount(2);

        grp.next()
           .grField1(10)
           .grField2(20);

        grp.next()
           .grField1(30)
           .grField2(40);

        return hdr_.size() + msg_.size();
    }

    virtual Ir *irForTemplateId(const int templateId, const int schemaVersion)
    {
        EXPECT_EQ(templateId, MsgName::sbeTemplateId());
        EXPECT_EQ(schemaVersion, MsgName::sbeSchemaVersion());

        Ir *tmplt = (Ir *)IrCollection::message(templateId, schemaVersion);
        return tmplt;
    }

    void checkEvent(const Field &f, const Group &g)
    {
        if (eventNumber_ == 0)
        {
            EXPECT_EQ(f.isComposite(), true);
            EXPECT_EQ(f.numEncodings(), 4);
            EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
            EXPECT_EQ(f.getUInt(0), MsgName::sbeBlockLength());
            EXPECT_EQ(f.primitiveType(1), Ir::UINT16);
            EXPECT_EQ(f.getUInt(1), MsgName::sbeTemplateId());
            EXPECT_EQ(f.primitiveType(2), Ir::UINT16);
            EXPECT_EQ(f.getUInt(2), MsgName::sbeSchemaId());
            EXPECT_EQ(f.primitiveType(3), Ir::UINT16);
            EXPECT_EQ(f.getUInt(3), MsgName::sbeSchemaVersion());
        }
        else if (eventNumber_ == 1)
        {
            EXPECT_EQ(f.schemaId(), 11);
            EXPECT_EQ(f.primitiveType(), Ir::UINT64);
            EXPECT_EQ(f.getUInt(), 187);
        }
        else if (eventNumber_ == 2)
        {
            EXPECT_EQ(f.isSet(), true);
            EXPECT_EQ(f.schemaId(), 12);
            EXPECT_EQ(f.primitiveType(), Ir::UINT8);
            EXPECT_EQ(f.getUInt(), 0x2);
        }
        else if (eventNumber_ == 3)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 20);
            EXPECT_EQ(g.numInGroup(), 2);
            EXPECT_EQ(g.iteration(), 0);
        }
        else if (eventNumber_ == 4)
        {
            EXPECT_EQ(f.schemaId(), 21);
            EXPECT_EQ(f.primitiveType(), Ir::UINT64);
            EXPECT_EQ(f.getUInt(), 10);
        }
        else if (eventNumber_ == 5)
        {
            EXPECT_EQ(f.schemaId(), 22);
            EXPECT_EQ(f.primitiveType(), Ir::INT64);
            EXPECT_EQ(f.getInt(), 20);
        }
        else if (eventNumber_ == 6)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 20);            
            EXPECT_EQ(g.numInGroup(), 2);
            EXPECT_EQ(g.iteration(), 0);
        }
        else if (eventNumber_ == 7)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 20);
            EXPECT_EQ(g.numInGroup(), 2);
            EXPECT_EQ(g.iteration(), 1);
        }
        else if (eventNumber_ == 8)
        {
            EXPECT_EQ(f.schemaId(), 21);
            EXPECT_EQ(f.primitiveType(), Ir::UINT64);
            EXPECT_EQ(f.getUInt(), 30);
        }
        else if (eventNumber_ == 9)
        {
            EXPECT_EQ(f.schemaId(), 22);
            EXPECT_EQ(f.primitiveType(), Ir::INT64);
            EXPECT_EQ(f.getInt(), 40);
        }
        else if (eventNumber_ == 10)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 20);            
            EXPECT_EQ(g.numInGroup(), 2);
            EXPECT_EQ(g.iteration(), 1);
        }
        else
        {
            exit(1);
        }
    }

    virtual int onNext(const Field &f)
    {
        Group dummy;

        checkEvent(f, dummy);
        eventNumber_++;
        return 0;
    }

    virtual int onNext(const Group &g)
    {
        Field dummy;

        checkEvent(dummy, g);
        eventNumber_++;
        return 0;
    }

    virtual int onError(const Error &e)
    {
        std::cout << "Error " << e.message() << "\n";
        exit(1);
        return 0;
    }

    virtual int onCompleted()
    {
        EXPECT_EQ(eventNumber_, 11);
        return 0;
    }

};

TEST_F(MessageBlockLengthIrTest, shouldHandleAllEventsCorrectltInOrder)
{

    int sz = encodeHdrAndMsg();

    ASSERT_EQ(sz, 54);
    EXPECT_EQ(*((::uint16_t *)buffer), MsgName::sbeBlockLength());
    EXPECT_EQ(*((::uint16_t *)(buffer + 2)), MsgName::sbeTemplateId());
    EXPECT_EQ(*((::uint16_t *)(buffer + 4)), MsgName::sbeSchemaId());
    EXPECT_EQ(*((::uint16_t *)(buffer + 6)), MsgName::sbeSchemaVersion());

    EXPECT_EQ(*((::uint64_t *)(buffer + 8)), 187); // field 1
    EXPECT_EQ(*((::uint8_t *)(buffer + 16)), 0x2); // field 2

    EXPECT_EQ(*((::uint16_t *)(buffer + 19)), 16); // groupSizeEncoding blockLength
    EXPECT_EQ(*((::uint8_t *)(buffer + 21)), 2);   // groupSizeEncoding numInGroup

    ASSERT_GE(IrCollection::loadFromFile("message-block-length-test.sbeir"), 0);

    listener.dispatchMessageByHeader(IrCollection::header(), this)
            .resetForDecode(buffer, 54)
            .subscribe(this, this, this);

    ASSERT_EQ(listener.bufferOffset(), 54);
}