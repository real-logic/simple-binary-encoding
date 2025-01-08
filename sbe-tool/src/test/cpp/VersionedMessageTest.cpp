/*
 * Copyright 2013-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <gtest/gtest.h>

#include "versmsg/VersionedMessageV1.h"
#include "versmsg/VersionedMessageV2.h"

using namespace versmsg;

static const std::size_t BUFFER_LEN = 2048;

class VersionedMessageTest : public testing::Test
{
public:
    VersionedMessageV2 m_versionedMessageV2 = {};
    VersionedMessageV1 m_versionedMessageV1Decoder = {};
};

TEST_F(VersionedMessageTest, shouldV1DecodeV2Message)
{
    char buffer[BUFFER_LEN] = {};

    m_versionedMessageV2.wrapForEncode(buffer, 0, sizeof(buffer));
    m_versionedMessageV2.fieldA1(1);
    m_versionedMessageV2.fieldB1(2);
    m_versionedMessageV2.fieldC2(3);
    m_versionedMessageV2.fieldD2(4);
    m_versionedMessageV2.fieldE2(5);
    m_versionedMessageV2.putString1("asdf", 4);

    m_versionedMessageV1Decoder.wrapForDecode(
        buffer,
        0,
        VersionedMessageV2::sbeBlockLength(),
        VersionedMessageV2::sbeSchemaVersion(),
        m_versionedMessageV2.encodedLength());

    EXPECT_EQ(m_versionedMessageV1Decoder.fieldA1(), (uint32_t)1);
    EXPECT_EQ(m_versionedMessageV1Decoder.fieldB1(), (uint32_t)2);
    EXPECT_STREQ(m_versionedMessageV1Decoder.string1(), "asdf");

    EXPECT_EQ(m_versionedMessageV1Decoder.decodeLength(), (uint64_t)26);
}
