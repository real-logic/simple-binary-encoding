/*
 * Copyright 2015 Real Logic Ltd.
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
#include "group_with_data/TestMessage1.hpp"
#include "group_with_data/TestMessage2.hpp"
#include "group_with_data/TestMessage3.hpp"
#include "group_with_data/TestMessage4.hpp"

using namespace std;
using namespace group_with_data;

static const sbe_uint32_t TAG_1 = 32;
static const int ENTRIES_COUNT = 2;

static const char TAG_GROUP_1_IDX_0[] = { 'T', 'a', 'g', 'G', 'r', 'o', 'u', 'p', '0' };
static const char TAG_GROUP_1_IDX_1[] = { 'T', 'a', 'g', 'G', 'r', 'o', 'u', 'p', '1' };
static const int TAG_GROUP_1_IDX_0_LENGTH = sizeof(TAG_GROUP_1_IDX_0);
static const int TAG_GROUP_1_IDX_1_LENGTH = sizeof(TAG_GROUP_1_IDX_1);

static const sbe_int64_t TAG_GROUP_2_IDX_0 = -120;
static const sbe_int64_t TAG_GROUP_2_IDX_1 = 120;
static const sbe_int64_t TAG_GROUP_2_IDX_2 = 75;

static const sbe_int64_t TAG_GROUP_2_IDX_3 = 76;
static const sbe_int64_t TAG_GROUP_2_IDX_4 = 77;
static const sbe_int64_t TAG_GROUP_2_IDX_5 = 78;

static const char *VAR_DATA_FIELD_IDX_0 = "neg idx 0";
static const int VAR_DATA_FIELD_IDX_0_LENGTH = 9;
static const char *VAR_DATA_FIELD_IDX_1 = "idx 1 positive";
static const int VAR_DATA_FIELD_IDX_1_LENGTH = 14;

static const int NESTED_ENTRIES_COUNT = 3;

static const char *VAR_DATA_FIELD_NESTED_IDX_0 = "zero";
static const int VAR_DATA_FIELD_NESTED_IDX_0_LENGTH = 4;
static const char *VAR_DATA_FIELD_NESTED_IDX_1 = "one";
static const int VAR_DATA_FIELD_NESTED_IDX_1_LENGTH = 3;
static const char *VAR_DATA_FIELD_NESTED_IDX_2 = "two";
static const int VAR_DATA_FIELD_NESTED_IDX_2_LENGTH = 3;

static const char *VAR_DATA_FIELD_NESTED_IDX_3 = "three";
static const int VAR_DATA_FIELD_NESTED_IDX_3_LENGTH = 5;
static const char *VAR_DATA_FIELD_NESTED_IDX_4 = "four";
static const int VAR_DATA_FIELD_NESTED_IDX_4_LENGTH = 4;
static const char *VAR_DATA_FIELD_NESTED_IDX_5 = "five";
static const int VAR_DATA_FIELD_NESTED_IDX_5_LENGTH = 4;

static const char *VAR_DATA_FIELD_1_IDX_0 = "neg idx 0";
static const int VAR_DATA_FIELD_1_IDX_0_LENGTH = 9;
static const char *VAR_DATA_FIELD_1_IDX_1 = "idx 1 positive";
static const int VAR_DATA_FIELD_1_IDX_1_LENGTH = 14;

static const char *VAR_DATA_FIELD_2_IDX_0 = "negative index 0";
static const int VAR_DATA_FIELD_2_IDX_0_LENGTH = 16;
static const char *VAR_DATA_FIELD_2_IDX_1 = "index 1 pos";
static const int VAR_DATA_FIELD_2_IDX_1_LENGTH = 11;

static const int expectedTestMessage1Size = 78;
static const int expectedTestMessage2Size = 107;
static const int expectedTestMessage3Size = 145;
static const int expectedTestMessage4Size = 73;

class GroupWithDataTest : public testing::Test
{
public:

    virtual int encodeTestMessage1(char *buffer, int offset, int bufferLength)
    {
        msg1_.wrapForEncode(buffer, offset, bufferLength);

    	msg1_.tag1(TAG_1);

    	TestMessage1::Entries &entries = msg1_.entriesCount(ENTRIES_COUNT);

    	entries.next()
    		.putTagGroup1(TAG_GROUP_1_IDX_0)
    		.tagGroup2(TAG_GROUP_2_IDX_0);

    	entries.putVarDataField(VAR_DATA_FIELD_IDX_0, VAR_DATA_FIELD_IDX_0_LENGTH);

    	entries.next()
    		.putTagGroup1(TAG_GROUP_1_IDX_1)
    		.tagGroup2(TAG_GROUP_2_IDX_1);

    	entries.putVarDataField(VAR_DATA_FIELD_IDX_1, VAR_DATA_FIELD_IDX_1_LENGTH);

        return msg1_.size();
    }

    virtual int encodeTestMessage2(char *buffer, int offset, int bufferLength)
    {
        msg2_.wrapForEncode(buffer, offset, bufferLength);

    	msg2_.tag1(TAG_1);

    	TestMessage2::Entries &entries = msg2_.entriesCount(ENTRIES_COUNT);

    	entries.next()
    		.putTagGroup1(TAG_GROUP_1_IDX_0)
    		.tagGroup2(TAG_GROUP_2_IDX_0);

        entries.putVarDataField1(VAR_DATA_FIELD_1_IDX_0, VAR_DATA_FIELD_1_IDX_0_LENGTH);
    	entries.putVarDataField2(VAR_DATA_FIELD_2_IDX_0, VAR_DATA_FIELD_2_IDX_0_LENGTH);

    	entries.next()
    		.putTagGroup1(TAG_GROUP_1_IDX_1)
    		.tagGroup2(TAG_GROUP_2_IDX_1);

    	entries.putVarDataField1(VAR_DATA_FIELD_1_IDX_1, VAR_DATA_FIELD_1_IDX_1_LENGTH);
    	entries.putVarDataField2(VAR_DATA_FIELD_2_IDX_1, VAR_DATA_FIELD_2_IDX_1_LENGTH);

        return msg2_.size();
    }

    virtual int encodeTestMessage3(char *buffer, int offset, int bufferLength)
    {
        msg3_.wrapForEncode(buffer, offset, bufferLength);

        msg3_.tag1(TAG_1);

        TestMessage3::Entries &entries = msg3_.entriesCount(ENTRIES_COUNT);

        entries.next()
            .putTagGroup1(TAG_GROUP_1_IDX_0);

        TestMessage3::Entries::NestedEntries &nestedEntries0 = entries.nestedEntriesCount(NESTED_ENTRIES_COUNT);

        nestedEntries0.next()
            .tagGroup2(TAG_GROUP_2_IDX_0);

        nestedEntries0.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_0, VAR_DATA_FIELD_NESTED_IDX_0_LENGTH);

        nestedEntries0.next()
            .tagGroup2(TAG_GROUP_2_IDX_1);

        nestedEntries0.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_1, VAR_DATA_FIELD_NESTED_IDX_1_LENGTH);

        nestedEntries0.next()
            .tagGroup2(TAG_GROUP_2_IDX_2);

        nestedEntries0.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_2, VAR_DATA_FIELD_NESTED_IDX_2_LENGTH);

        entries.putVarDataField(VAR_DATA_FIELD_IDX_0, VAR_DATA_FIELD_IDX_0_LENGTH);

        entries.next()
            .putTagGroup1(TAG_GROUP_1_IDX_1);

        TestMessage3::Entries::NestedEntries &nestedEntries1 = entries.nestedEntriesCount(NESTED_ENTRIES_COUNT);

        nestedEntries1.next()
            .tagGroup2(TAG_GROUP_2_IDX_3);

        nestedEntries1.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_3, VAR_DATA_FIELD_NESTED_IDX_3_LENGTH);

        nestedEntries1.next()
            .tagGroup2(TAG_GROUP_2_IDX_4);

        nestedEntries1.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_4, VAR_DATA_FIELD_NESTED_IDX_4_LENGTH);

        nestedEntries1.next()
            .tagGroup2(TAG_GROUP_2_IDX_5);

        nestedEntries1.putVarDataFieldNested(VAR_DATA_FIELD_NESTED_IDX_5, VAR_DATA_FIELD_NESTED_IDX_5_LENGTH);

        entries.putVarDataField(VAR_DATA_FIELD_IDX_1, VAR_DATA_FIELD_IDX_1_LENGTH);

        return msg3_.size();
    }

    virtual int encodeTestMessage4(char *buffer, int offset, int bufferLength)
    {
        msg4_.wrapForEncode(buffer, offset, bufferLength);

    	msg4_.tag1(TAG_1);

    	TestMessage4::Entries &entries = msg4_.entriesCount(ENTRIES_COUNT);

    	entries.next();

        entries.putVarDataField1(VAR_DATA_FIELD_1_IDX_0, VAR_DATA_FIELD_1_IDX_0_LENGTH);
    	entries.putVarDataField2(VAR_DATA_FIELD_2_IDX_0, VAR_DATA_FIELD_2_IDX_0_LENGTH);

    	entries.next();

    	entries.putVarDataField1(VAR_DATA_FIELD_1_IDX_1, VAR_DATA_FIELD_1_IDX_1_LENGTH);
    	entries.putVarDataField2(VAR_DATA_FIELD_2_IDX_1, VAR_DATA_FIELD_2_IDX_1_LENGTH);

        return msg4_.size();
    }

    TestMessage1 msg1_;
    TestMessage2 msg2_;
    TestMessage3 msg3_;
    TestMessage4 msg4_;
};

TEST_F(GroupWithDataTest, shouldBeAbleToEncodeTestMessage1Correctly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeTestMessage1(buffer, 0, sizeof(buffer));

    int offset = 0;
    EXPECT_EQ(*(sbe_uint32_t *)(bp + offset), TAG_1);
    EXPECT_EQ(TestMessage1::sbeBlockLength(), 16);
    offset += 16;  // root blockLength of 16

    // entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), TAG_GROUP_1_IDX_0_LENGTH + sizeof(sbe_int64_t));
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_0_LENGTH), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));
    offset += TAG_GROUP_1_IDX_0_LENGTH;
    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_0);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_IDX_0_LENGTH), VAR_DATA_FIELD_IDX_0);
    offset += VAR_DATA_FIELD_IDX_0_LENGTH;

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_1_LENGTH), std::string(TAG_GROUP_1_IDX_1, TAG_GROUP_1_IDX_1_LENGTH));
    offset += TAG_GROUP_1_IDX_1_LENGTH;
    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_1);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_IDX_1_LENGTH), VAR_DATA_FIELD_IDX_1);
    offset += VAR_DATA_FIELD_IDX_1_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(GroupWithDataTest, shouldbeAbleToEncodeAndDecodeTestMessage1Correctly)
{
    char buffer[2048];
    int sz = encodeTestMessage1(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, expectedTestMessage1Size);

    TestMessage1 msg1Decoder(buffer, sizeof(buffer), TestMessage1::sbeBlockLength(), TestMessage1::sbeSchemaVersion());

    EXPECT_EQ(msg1Decoder.tag1(), TAG_1);

    TestMessage1::Entries &entries = msg1Decoder.entries();
    EXPECT_EQ(entries.count(), ENTRIES_COUNT);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.tagGroup1Length(), TAG_GROUP_1_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.tagGroup1(), entries.tagGroup1Length()), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));
    EXPECT_EQ(entries.tagGroup2(), TAG_GROUP_2_IDX_0);
    EXPECT_EQ(entries.varDataFieldLength(), VAR_DATA_FIELD_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField(), VAR_DATA_FIELD_IDX_0_LENGTH), VAR_DATA_FIELD_IDX_0);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.tagGroup1Length(), TAG_GROUP_1_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.tagGroup1(), entries.tagGroup1Length()), std::string(TAG_GROUP_1_IDX_1, TAG_GROUP_1_IDX_1_LENGTH));
    EXPECT_EQ(entries.tagGroup2(), TAG_GROUP_2_IDX_1);
    EXPECT_EQ(entries.varDataFieldLength(), VAR_DATA_FIELD_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField(), VAR_DATA_FIELD_IDX_1_LENGTH), VAR_DATA_FIELD_IDX_1);

    EXPECT_EQ(msg1Decoder.size(), expectedTestMessage1Size);
}

TEST_F(GroupWithDataTest, shouldBeAbleToEncodeTestMessage2Correctly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeTestMessage2(buffer, 0, sizeof(buffer));

    int offset = 0;
    EXPECT_EQ(*(sbe_uint32_t *)(bp + offset), TAG_1);
    EXPECT_EQ(TestMessage2::sbeBlockLength(), 16);
    offset += 16;  // root blockLength of 16

    // entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), TAG_GROUP_1_IDX_0_LENGTH + sizeof(sbe_int64_t));
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_0_LENGTH), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));
    offset += TAG_GROUP_1_IDX_0_LENGTH;
    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_0);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_1_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_1_IDX_0_LENGTH), VAR_DATA_FIELD_1_IDX_0);
    offset += VAR_DATA_FIELD_1_IDX_0_LENGTH;
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_2_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_2_IDX_0_LENGTH), VAR_DATA_FIELD_2_IDX_0);
    offset += VAR_DATA_FIELD_2_IDX_0_LENGTH;

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_1_LENGTH), std::string(TAG_GROUP_1_IDX_1, TAG_GROUP_1_IDX_1_LENGTH));
    offset += TAG_GROUP_1_IDX_1_LENGTH;
    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_1);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_1_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_1_IDX_1_LENGTH), VAR_DATA_FIELD_1_IDX_1);
    offset += VAR_DATA_FIELD_1_IDX_1_LENGTH;
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_2_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_2_IDX_1_LENGTH), VAR_DATA_FIELD_2_IDX_1);
    offset += VAR_DATA_FIELD_2_IDX_1_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(GroupWithDataTest, shouldbeAbleToEncodeAndDecodeTestMessage2Correctly)
{
    char buffer[2048];
    int sz = encodeTestMessage2(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, expectedTestMessage2Size);

    TestMessage2 msg2Decoder(buffer, sizeof(buffer), TestMessage2::sbeBlockLength(), TestMessage2::sbeSchemaVersion());

    EXPECT_EQ(msg2Decoder.tag1(), TAG_1);

    TestMessage2::Entries &entries = msg2Decoder.entries();
    EXPECT_EQ(entries.count(), ENTRIES_COUNT);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.tagGroup1Length(), TAG_GROUP_1_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.tagGroup1(), entries.tagGroup1Length()), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));
    EXPECT_EQ(entries.tagGroup2(), TAG_GROUP_2_IDX_0);
    EXPECT_EQ(entries.varDataField1Length(), VAR_DATA_FIELD_1_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField1(), VAR_DATA_FIELD_1_IDX_0_LENGTH), VAR_DATA_FIELD_1_IDX_0);
    EXPECT_EQ(entries.varDataField2Length(), VAR_DATA_FIELD_2_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField2(), VAR_DATA_FIELD_2_IDX_0_LENGTH), VAR_DATA_FIELD_2_IDX_0);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.tagGroup1Length(), TAG_GROUP_1_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.tagGroup1(), entries.tagGroup1Length()), std::string(TAG_GROUP_1_IDX_1, TAG_GROUP_1_IDX_1_LENGTH));
    EXPECT_EQ(entries.tagGroup2(), TAG_GROUP_2_IDX_1);
    EXPECT_EQ(entries.varDataField1Length(), VAR_DATA_FIELD_1_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField1(), VAR_DATA_FIELD_1_IDX_1_LENGTH), VAR_DATA_FIELD_1_IDX_1);
    EXPECT_EQ(entries.varDataField2Length(), VAR_DATA_FIELD_2_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField2(), VAR_DATA_FIELD_2_IDX_1_LENGTH), VAR_DATA_FIELD_2_IDX_1);

    EXPECT_EQ(msg2Decoder.size(), expectedTestMessage2Size);
}

TEST_F(GroupWithDataTest, shouldBeAbleToEncodeTestMessage3Correctly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeTestMessage3(buffer, 0, sizeof(buffer));

    int offset = 0;
    EXPECT_EQ(*(sbe_uint32_t *)(bp + offset), TAG_1);
    EXPECT_EQ(TestMessage1::sbeBlockLength(), 16);
    offset += 16;  // root blockLength of 16

    // entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), TAG_GROUP_1_IDX_0_LENGTH);
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_0_LENGTH), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));
    offset += TAG_GROUP_1_IDX_0_LENGTH;

    // nested entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), sizeof(sbe_int64_t));
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), NESTED_ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_0);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_0_LENGTH), VAR_DATA_FIELD_NESTED_IDX_0);
    offset += VAR_DATA_FIELD_NESTED_IDX_0_LENGTH;

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_1);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_1_LENGTH), VAR_DATA_FIELD_NESTED_IDX_1);
    offset += VAR_DATA_FIELD_NESTED_IDX_1_LENGTH;

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_2);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_2_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_2_LENGTH), VAR_DATA_FIELD_NESTED_IDX_2);
    offset += VAR_DATA_FIELD_NESTED_IDX_2_LENGTH;

    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_IDX_0_LENGTH), VAR_DATA_FIELD_IDX_0);
    offset += VAR_DATA_FIELD_IDX_0_LENGTH;

    EXPECT_EQ(std::string(bp + offset, TAG_GROUP_1_IDX_1_LENGTH), std::string(TAG_GROUP_1_IDX_1, TAG_GROUP_1_IDX_1_LENGTH));
    offset += TAG_GROUP_1_IDX_1_LENGTH;

    // nested entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), sizeof(sbe_int64_t));
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), NESTED_ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_3);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_3_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_3_LENGTH), VAR_DATA_FIELD_NESTED_IDX_3);
    offset += VAR_DATA_FIELD_NESTED_IDX_3_LENGTH;

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_4);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_4_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_4_LENGTH), VAR_DATA_FIELD_NESTED_IDX_4);
    offset += VAR_DATA_FIELD_NESTED_IDX_4_LENGTH;

    EXPECT_EQ(*(sbe_int64_t *)(bp + offset), TAG_GROUP_2_IDX_5);
    offset += sizeof(sbe_int64_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_NESTED_IDX_5_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_NESTED_IDX_5_LENGTH), VAR_DATA_FIELD_NESTED_IDX_5);
    offset += VAR_DATA_FIELD_NESTED_IDX_5_LENGTH;

    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_IDX_1_LENGTH), VAR_DATA_FIELD_IDX_1);
    offset += VAR_DATA_FIELD_IDX_1_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(GroupWithDataTest, shouldbeAbleToEncodeAndDecodeTestMessage3Correctly)
{
    char buffer[2048];
    int sz = encodeTestMessage3(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, expectedTestMessage3Size);

    TestMessage3 msg3Decoder(buffer, sizeof(buffer), TestMessage3::sbeBlockLength(), TestMessage3::sbeSchemaVersion());

    EXPECT_EQ(msg3Decoder.tag1(), TAG_1);

    TestMessage3::Entries &entries = msg3Decoder.entries();
    EXPECT_EQ(entries.count(), ENTRIES_COUNT);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.tagGroup1Length(), TAG_GROUP_1_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.tagGroup1(), entries.tagGroup1Length()), std::string(TAG_GROUP_1_IDX_0, TAG_GROUP_1_IDX_0_LENGTH));

    TestMessage3::Entries::NestedEntries &nestedEntries0 = entries.nestedEntries();
    EXPECT_EQ(nestedEntries0.count(), NESTED_ENTRIES_COUNT);

    ASSERT_TRUE(nestedEntries0.hasNext());
    nestedEntries0.next();

    EXPECT_EQ(nestedEntries0.tagGroup2(), TAG_GROUP_2_IDX_0);
    EXPECT_EQ(nestedEntries0.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_0_LENGTH);
    EXPECT_EQ(std::string(nestedEntries0.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_0_LENGTH), VAR_DATA_FIELD_NESTED_IDX_0);

    ASSERT_TRUE(nestedEntries0.hasNext());
    nestedEntries0.next();

    EXPECT_EQ(nestedEntries0.tagGroup2(), TAG_GROUP_2_IDX_1);
    EXPECT_EQ(nestedEntries0.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_1_LENGTH);
    EXPECT_EQ(std::string(nestedEntries0.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_1_LENGTH), VAR_DATA_FIELD_NESTED_IDX_1);

    ASSERT_TRUE(nestedEntries0.hasNext());
    nestedEntries0.next();

    EXPECT_EQ(nestedEntries0.tagGroup2(), TAG_GROUP_2_IDX_2);
    EXPECT_EQ(nestedEntries0.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_2_LENGTH);
    EXPECT_EQ(std::string(nestedEntries0.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_2_LENGTH), VAR_DATA_FIELD_NESTED_IDX_2);

    EXPECT_EQ(entries.varDataFieldLength(), VAR_DATA_FIELD_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField(), VAR_DATA_FIELD_IDX_0_LENGTH), VAR_DATA_FIELD_IDX_0);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    TestMessage3::Entries::NestedEntries &nestedEntries1 = entries.nestedEntries();
    EXPECT_EQ(nestedEntries1.count(), NESTED_ENTRIES_COUNT);

    ASSERT_TRUE(nestedEntries1.hasNext());
    nestedEntries1.next();

    EXPECT_EQ(nestedEntries1.tagGroup2(), TAG_GROUP_2_IDX_3);
    EXPECT_EQ(nestedEntries1.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_3_LENGTH);
    EXPECT_EQ(std::string(nestedEntries1.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_3_LENGTH), VAR_DATA_FIELD_NESTED_IDX_3);

    ASSERT_TRUE(nestedEntries1.hasNext());
    nestedEntries1.next();

    EXPECT_EQ(nestedEntries1.tagGroup2(), TAG_GROUP_2_IDX_4);
    EXPECT_EQ(nestedEntries1.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_4_LENGTH);
    EXPECT_EQ(std::string(nestedEntries1.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_4_LENGTH), VAR_DATA_FIELD_NESTED_IDX_4);

    ASSERT_TRUE(nestedEntries1.hasNext());
    nestedEntries1.next();

    EXPECT_EQ(nestedEntries1.tagGroup2(), TAG_GROUP_2_IDX_5);
    EXPECT_EQ(nestedEntries1.varDataFieldNestedLength(), VAR_DATA_FIELD_NESTED_IDX_5_LENGTH);
    EXPECT_EQ(std::string(nestedEntries1.varDataFieldNested(), VAR_DATA_FIELD_NESTED_IDX_5_LENGTH), VAR_DATA_FIELD_NESTED_IDX_5);

    EXPECT_EQ(entries.varDataFieldLength(), VAR_DATA_FIELD_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField(), VAR_DATA_FIELD_IDX_1_LENGTH), VAR_DATA_FIELD_IDX_1);

    EXPECT_EQ(msg3Decoder.size(), expectedTestMessage3Size);
}

TEST_F(GroupWithDataTest, shouldBeAbleToEncodeTestMessage4Correctly)
{
    char buffer[2048];
    const char *bp = buffer;
    int sz = encodeTestMessage4(buffer, 0, sizeof(buffer));

    int offset = 0;
    EXPECT_EQ(*(sbe_uint32_t *)(bp + offset), TAG_1);
    EXPECT_EQ(TestMessage4::sbeBlockLength(), 16);
    offset += 16;  // root blockLength of 16

    // entries
    EXPECT_EQ(*(sbe_uint16_t *)(bp + offset), 0);
    offset += sizeof(sbe_uint16_t);
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), ENTRIES_COUNT);
    offset += sizeof(sbe_uint8_t);

    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_1_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_1_IDX_0_LENGTH), VAR_DATA_FIELD_1_IDX_0);
    offset += VAR_DATA_FIELD_1_IDX_0_LENGTH;
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_2_IDX_0_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_2_IDX_0_LENGTH), VAR_DATA_FIELD_2_IDX_0);
    offset += VAR_DATA_FIELD_2_IDX_0_LENGTH;

    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_1_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_1_IDX_1_LENGTH), VAR_DATA_FIELD_1_IDX_1);
    offset += VAR_DATA_FIELD_1_IDX_1_LENGTH;
    EXPECT_EQ(*(sbe_uint8_t *)(bp + offset), VAR_DATA_FIELD_2_IDX_1_LENGTH);
    offset += sizeof(sbe_uint8_t);
    EXPECT_EQ(std::string(bp + offset, VAR_DATA_FIELD_2_IDX_1_LENGTH), VAR_DATA_FIELD_2_IDX_1);
    offset += VAR_DATA_FIELD_2_IDX_1_LENGTH;

    EXPECT_EQ(sz, offset);
}

TEST_F(GroupWithDataTest, shouldbeAbleToEncodeAndDecodeTestMessage4Correctly)
{
    char buffer[2048];
    int sz = encodeTestMessage4(buffer, 0, sizeof(buffer));

    EXPECT_EQ(sz, expectedTestMessage4Size);

    TestMessage4 msg4Decoder(buffer, sizeof(buffer), TestMessage4::sbeBlockLength(), TestMessage4::sbeSchemaVersion());

    EXPECT_EQ(msg4Decoder.tag1(), TAG_1);

    TestMessage4::Entries &entries = msg4Decoder.entries();
    EXPECT_EQ(entries.count(), ENTRIES_COUNT);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.varDataField1Length(), VAR_DATA_FIELD_1_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField1(), VAR_DATA_FIELD_1_IDX_0_LENGTH), VAR_DATA_FIELD_1_IDX_0);
    EXPECT_EQ(entries.varDataField2Length(), VAR_DATA_FIELD_2_IDX_0_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField2(), VAR_DATA_FIELD_2_IDX_0_LENGTH), VAR_DATA_FIELD_2_IDX_0);

    ASSERT_TRUE(entries.hasNext());
    entries.next();

    EXPECT_EQ(entries.varDataField1Length(), VAR_DATA_FIELD_1_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField1(), VAR_DATA_FIELD_1_IDX_1_LENGTH), VAR_DATA_FIELD_1_IDX_1);
    EXPECT_EQ(entries.varDataField2Length(), VAR_DATA_FIELD_2_IDX_1_LENGTH);
    EXPECT_EQ(std::string(entries.varDataField2(), VAR_DATA_FIELD_2_IDX_1_LENGTH), VAR_DATA_FIELD_2_IDX_1);

    EXPECT_EQ(msg4Decoder.size(), expectedTestMessage4Size);
}
