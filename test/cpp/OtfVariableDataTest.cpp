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

class OtfVariableDataTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define VAR_DATA_STR "this is a variable length data string"
#define VAR_DATA_SIZE (sizeof(VAR_DATA_STR) - 1)

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithVarData");
        std::string varDataStr = std::string("VarDataField");
        std::string compositeStr = std::string("varDataEncoding");

        ir.addToken(0, 0xFFFFFFFF, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID, varDataStr);
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, 0xFFFF, compositeStr);
        ir.addToken(0, 1, Ir::ENCODING, byteOrder, Ir::UINT8, 0xFFFF, std::string("length"));
        ir.addToken(1, 0xFFFFFFFF, Ir::ENCODING, byteOrder, Ir::CHAR, 0xFFFF, std::string("varData"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, 0xFFFF, compositeStr);
        ir.addToken(0, 0, Ir::END_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID, varDataStr);
        ir.addToken(0, 0xFFFFFFFF, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        buffer_[bufferLen_] = VAR_DATA_SIZE;
        ::memcpy(buffer_ + bufferLen_ + 1, VAR_DATA_STR, VAR_DATA_SIZE);
        bufferLen_ += 1 + VAR_DATA_SIZE;
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            char fieldValue[VAR_DATA_SIZE + 1];

            EXPECT_EQ(f.type(), Field::VAR_DATA);
            EXPECT_EQ(f.numEncodings(), 2);
            EXPECT_EQ(f.fieldName(), "VarDataField");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT8);
            EXPECT_EQ(f.primitiveType(1), Ir::CHAR);
            EXPECT_EQ(f.length(0), 1);
            EXPECT_EQ(f.length(1), VAR_DATA_SIZE);
            EXPECT_EQ(f.getUInt(0), VAR_DATA_SIZE);
            f.getArray(1, fieldValue, 0, VAR_DATA_SIZE);
            EXPECT_EQ(std::string(fieldValue, VAR_DATA_SIZE), VAR_DATA_STR);
        }
        return 0;
    };
};

TEST_F(OtfVariableDataTest, shouldHandleVariableDataWithCharType)
{
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}
