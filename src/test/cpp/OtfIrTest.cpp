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
#define __STDC_LIMIT_MACROS 1
#include <stdint.h>
#include <iostream>

#include "gtest/gtest.h"
#include "otf_api/Ir.h"
#include "otf_api/IrCollection.h"

using namespace sbe::on_the_fly;
using ::std::cout;
using ::std::endl;

class OtfIrTestWrapper : public Ir
{
public:
    OtfIrTestWrapper() : Ir(NULL, 0, -1, -1, -1) {};
};

/*
 * Fixture around IR for testing that it works correctly
 */
class OtfIrTest : public testing::Test
{
protected:
    // virtual void SetUp()
    // virtual void TearDown() {};

    void addMessageHeader()
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageHeaderStr = std::string("messageHeader");
        int32_t schemaId = Ir::INVALID_ID;

        // messageHeader
        ir_.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
        ir_.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("blockLength"));
        ir_.addToken(2, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("templateId"));
        ir_.addToken(4, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("version"));
        ir_.addToken(5, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("reserved"));
        ir_.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
    };

    OtfIrTestWrapper ir_;
};

TEST_F(OtfIrTest, shouldBeAbleToAddTokens)
{
    addMessageHeader();
}

TEST_F(OtfIrTest, shouldBeAbleToIterate)
{
    addMessageHeader();
    ir_.begin();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 0);
    EXPECT_EQ(ir_.size(), 0);
    EXPECT_EQ(ir_.signal(), Ir::BEGIN_COMPOSITE);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::NONE);
    EXPECT_EQ(ir_.name(), std::string("messageHeader"));
    ir_.next();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 0);
    EXPECT_EQ(ir_.size(), 2);
    EXPECT_EQ(ir_.signal(), Ir::ENCODING);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::UINT16);
    EXPECT_EQ(ir_.name(), std::string("blockLength"));
    ir_.next();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 2);
    EXPECT_EQ(ir_.size(), 2);
    EXPECT_EQ(ir_.signal(), Ir::ENCODING);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::UINT16);
    EXPECT_EQ(ir_.name(), std::string("templateId"));
    ir_.next();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 4);
    EXPECT_EQ(ir_.size(), 1);
    EXPECT_EQ(ir_.signal(), Ir::ENCODING);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::UINT8);
    EXPECT_EQ(ir_.name(), std::string("version"));
    ir_.next();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 5);
    EXPECT_EQ(ir_.size(), 1);
    EXPECT_EQ(ir_.signal(), Ir::ENCODING);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::UINT8);
    EXPECT_EQ(ir_.name(), std::string("reserved"));
    ir_.next();
    EXPECT_EQ(ir_.end(), false);
    EXPECT_EQ(ir_.offset(), 0);
    EXPECT_EQ(ir_.size(), 0);
    EXPECT_EQ(ir_.signal(), Ir::END_COMPOSITE);
    EXPECT_EQ(ir_.byteOrder(), Ir::SBE_LITTLE_ENDIAN);
    EXPECT_EQ(ir_.primitiveType(), Ir::NONE);
    EXPECT_EQ(ir_.name(), std::string("messageHeader"));
    ir_.next();
    EXPECT_EQ(ir_.end(), true);
}

TEST(OtfIrCollectionTest, shouldHandleEmptyIrCollection)
{
    IrCollection irCollection;
}
