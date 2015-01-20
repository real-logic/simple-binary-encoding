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
#include "composite_offsets_test/MessageHeader.hpp"
#include "composite_offsets_test/TestMessage1.hpp"
#include "otf_api/Ir.h"
#include "otf_api/IrCollection.h"
#include "otf_api/Listener.h"

using namespace std;
using namespace composite_offsets_test;

class CompositeOffsetsIrTest : public testing::Test, public IrCollection, public Ir::Callback,
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
        TestMessage1 msg_;

        hdr_.wrap(buffer, 0, 0, sizeof(buffer))
            .blockLength(TestMessage1::sbeBlockLength())
            .templateId(TestMessage1::sbeTemplateId())
            .schemaId(TestMessage1::sbeSchemaId())
            .version(TestMessage1::sbeSchemaVersion());

        msg_.wrapForEncode(buffer, hdr_.size(), sizeof(buffer));

        TestMessage1::Entries &entries = msg_.entriesCount(2);

        entries.next()
            .tagGroup1(10)
            .tagGroup2(20);

        entries.next()
            .tagGroup1(30)
            .tagGroup2(40);

        return hdr_.size() + msg_.size();
    }

    virtual Ir *irForTemplateId(const int templateId, const int schemaVersion)
    {
        EXPECT_EQ(templateId, TestMessage1::sbeTemplateId());
        EXPECT_EQ(schemaVersion, TestMessage1::sbeSchemaVersion());

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
            EXPECT_EQ(f.getUInt(0), TestMessage1::sbeBlockLength());
            EXPECT_EQ(f.primitiveType(1), Ir::UINT16);
            EXPECT_EQ(f.getUInt(1), TestMessage1::sbeTemplateId());
            EXPECT_EQ(f.primitiveType(2), Ir::UINT16);
            EXPECT_EQ(f.getUInt(2), TestMessage1::sbeSchemaId());
            EXPECT_EQ(f.primitiveType(3), Ir::UINT16);
            EXPECT_EQ(f.getUInt(3), TestMessage1::sbeSchemaVersion());
        }
        else if (eventNumber_ == 1)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 2);
        }
        else if (eventNumber_ == 2)
        {
            EXPECT_EQ(f.schemaId(), 3);
            EXPECT_EQ(f.primitiveType(), Ir::UINT64);
            EXPECT_EQ(f.getUInt(), 10);
        }
        else if (eventNumber_ == 3)
        {
            EXPECT_EQ(f.schemaId(), 4);
            EXPECT_EQ(f.primitiveType(), Ir::INT64);
            EXPECT_EQ(f.getInt(), 20);
        }
        else if (eventNumber_ == 4)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 2);            
        }
        else if (eventNumber_ == 5)
        {
            EXPECT_EQ(g.event(), Group::START);
            EXPECT_EQ(g.schemaId(), 2);
        }
        else if (eventNumber_ == 6)
        {
            EXPECT_EQ(f.schemaId(), 3);
            EXPECT_EQ(f.primitiveType(), Ir::UINT64);
            EXPECT_EQ(f.getUInt(), 30);
        }
        else if (eventNumber_ == 7)
        {
            EXPECT_EQ(f.schemaId(), 4);
            EXPECT_EQ(f.primitiveType(), Ir::INT64);
            EXPECT_EQ(f.getInt(), 40);
        }
        else if (eventNumber_ == 8)
        {
            EXPECT_EQ(g.event(), Group::END);
            EXPECT_EQ(g.schemaId(), 2);            
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
        EXPECT_EQ(eventNumber_, 9);
        return 0;
    }

};

TEST_F(CompositeOffsetsIrTest, shouldHandleAllEventsCorrectltInOrder)
{
    ASSERT_EQ(encodeHdrAndMsg(), 52);

    ASSERT_GE(IrCollection::loadFromFile("composite-offsets-schema.sbeir"), 0);

    listener.dispatchMessageByHeader(IrCollection::header(), this)
            .resetForDecode(buffer, 52)
            .subscribe(this, this, this);

    ASSERT_EQ(listener.bufferOffset(), 52);
}