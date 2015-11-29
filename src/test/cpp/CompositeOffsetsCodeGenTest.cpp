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

using namespace std;
using namespace composite_offsets_test;

class CompositeOffsetsCodeGenTest : public testing::Test
{
public:

    virtual int encodeHdr(char *buffer, int offset, int bufferLength)
    {
        hdr_.wrap(buffer, offset, 0, bufferLength)
            .blockLength(TestMessage1::sbeBlockLength())
            .templateId(TestMessage1::sbeTemplateId())
            .schemaId(TestMessage1::sbeSchemaId())
            .version(TestMessage1::sbeSchemaVersion());

        return hdr_.size();
    }

    virtual int encodeMsg(char *buffer, int offset, int bufferLength)
    {
        msg_.wrapForEncode(buffer, offset, bufferLength);

        TestMessage1::Entries &entries = msg_.entriesCount(2);

        entries.next()
            .tagGroup1(10)
            .tagGroup2(20);

        entries.next()
            .tagGroup1(30)
            .tagGroup2(40);

        return msg_.size();
    }

    MessageHeader hdr_;
    MessageHeader hdrDecoder_;
    TestMessage1 msg_;
    TestMessage1 msgDecoder_;
};

TEST_F(CompositeOffsetsCodeGenTest, shouldReturnCorrectValuesForMessageHeaderStaticFields)
{
    EXPECT_EQ(MessageHeader::size(), 12);
    // only checking the block length field
    EXPECT_EQ(MessageHeader::blockLengthNullValue(), 65535);
    EXPECT_EQ(MessageHeader::blockLengthMinValue(), 0);
    EXPECT_EQ(MessageHeader::blockLengthMaxValue(), 65534);
}

TEST_F(CompositeOffsetsCodeGenTest, shouldReturnCorrectValuesForTestMessage1StaticFields)
{
    EXPECT_EQ(TestMessage1::sbeBlockLength(), 0);
    EXPECT_EQ(TestMessage1::sbeTemplateId(), 1);
    EXPECT_EQ(TestMessage1::sbeSchemaId(), 15);
    EXPECT_EQ(TestMessage1::sbeSchemaVersion(), 0);
    EXPECT_EQ(std::string(TestMessage1::sbeSemanticType()), std::string(""));
    EXPECT_EQ(TestMessage1::Entries::sbeBlockLength(), 16);
    EXPECT_EQ(TestMessage1::Entries::sbeHeaderSize(), 8);
}

TEST_F(CompositeOffsetsCodeGenTest, shouldBeAbleToEncodeMessageHeaderCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;

    int sz = encodeHdr(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, 12);
    EXPECT_EQ(*((::uint16_t *)bp), TestMessage1::sbeBlockLength());
    EXPECT_EQ(*((::uint16_t *)(bp + 4)), TestMessage1::sbeTemplateId());
    EXPECT_EQ(*((::uint16_t *)(bp + 8)), TestMessage1::sbeSchemaId());
    EXPECT_EQ(*((::uint16_t *)(bp + 10)), TestMessage1::sbeSchemaVersion());
}

TEST_F(CompositeOffsetsCodeGenTest, shouldBeAbleToEncodeAndDecodeMessageHeaderCorrectly)
{
    char buffer[2048];

    encodeHdr(buffer, 0, sizeof(buffer));

    hdrDecoder_.wrap(buffer, 0, 0, sizeof(buffer));
    EXPECT_EQ(hdrDecoder_.blockLength(), TestMessage1::sbeBlockLength());
    EXPECT_EQ(hdrDecoder_.templateId(), TestMessage1::sbeTemplateId());
    EXPECT_EQ(hdrDecoder_.schemaId(), TestMessage1::sbeSchemaId());
    EXPECT_EQ(hdrDecoder_.version(), TestMessage1::sbeSchemaVersion());
}

TEST_F(CompositeOffsetsCodeGenTest, shouldBeAbleToEncodeMessageCorrectly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeMsg(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, 40);

    EXPECT_EQ(*(::uint16_t *)bp, TestMessage1::Entries::sbeBlockLength());
    EXPECT_EQ(*(::uint8_t *)(bp + 7), 2u);
    EXPECT_EQ(*(::uint64_t *)(bp + 8), 10u);
    EXPECT_EQ(*(::int64_t *)(bp + 16), 20u);
    EXPECT_EQ(*(::uint64_t *)(bp + 24), 30u);
    EXPECT_EQ(*(::int64_t *)(bp + 32), 40u);
}

TEST_F(CompositeOffsetsCodeGenTest, shouldBeAbleToDecodeHeaderAndMsgCorrectly)
{
    char buffer[2048];
    int hdrSz = encodeHdr(buffer, 0, sizeof(buffer));
    int sz = encodeMsg(buffer, hdrSz, sizeof(buffer));

    EXPECT_EQ(hdrSz, 12);
    EXPECT_EQ(sz, 40);

    hdrDecoder_.wrap(buffer, 0, 0, hdrSz + sz);

    EXPECT_EQ(hdrDecoder_.blockLength(), TestMessage1::sbeBlockLength());
    EXPECT_EQ(hdrDecoder_.templateId(), TestMessage1::sbeTemplateId());
    EXPECT_EQ(hdrDecoder_.schemaId(), TestMessage1::sbeSchemaId());
    EXPECT_EQ(hdrDecoder_.version(), TestMessage1::sbeSchemaVersion());

    msgDecoder_.wrapForDecode(buffer, hdrSz, TestMessage1::sbeBlockLength(), TestMessage1::sbeSchemaVersion(), hdrSz + sz);

    TestMessage1::Entries entries = msgDecoder_.entries();
    EXPECT_EQ(entries.count(), 2);

    ASSERT_TRUE(entries.hasNext());
    entries.next();
    EXPECT_EQ(entries.tagGroup1(), 10u);
    EXPECT_EQ(entries.tagGroup2(), 20u);

    ASSERT_TRUE(entries.hasNext());
    entries.next();
    EXPECT_EQ(entries.tagGroup1(), 30u);
    EXPECT_EQ(entries.tagGroup2(), 40u);

    EXPECT_EQ(msgDecoder_.size(), 40);
}

