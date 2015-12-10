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
static const char *VAR_DATA_FIELD_IDX_0 = "neg idx 0";
static const int VAR_DATA_FIELD_IDX_0_LENGTH = 9;
static const char *VAR_DATA_FIELD_IDX_1 = "idx 1 positive";
static const int VAR_DATA_FIELD_IDX_1_LENGTH = 14;

static const int expectedTestMessage1Size = 78;

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
    		.tagGroup2(TAG_GROUP_2_IDX_0)
    		.putVarDataField(VAR_DATA_FIELD_IDX_0, strlen(VAR_DATA_FIELD_IDX_0));

    	entries.next()
    		.putTagGroup1(TAG_GROUP_1_IDX_1)
    		.tagGroup2(TAG_GROUP_2_IDX_1)
    		.putVarDataField(VAR_DATA_FIELD_IDX_1, strlen(VAR_DATA_FIELD_IDX_1));

        return msg1_.size();
    }

    TestMessage1 msg1_;
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
