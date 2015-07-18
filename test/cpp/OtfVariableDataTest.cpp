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
#define VAR_DATA_CHAR_STR "this is a variable length data string"
#define VAR_DATA_CHAR_SIZE (sizeof(VAR_DATA_CHAR_STR) - 1)
#define VAR_DATA_UINT8_SIZE 27
#define VAR_DATA_UINT8_SEED 0x9A

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithVarData");
        std::string varDataStr1 = std::string("VarDataField1");
        std::string varDataStr2 = std::string("VarDataField2");
        std::string compositeStr1 = std::string("varDataEncodingChar");
        std::string compositeStr2 = std::string("varDataEncodingUInt8");

        ir.addToken(0, 0xFFFFFFFF, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID, varDataStr1);
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, Field::INVALID_ID, compositeStr1);
        ir.addToken(0, 1, Ir::ENCODING, byteOrder, Ir::UINT8, Field::INVALID_ID, std::string("length"));
        ir.addToken(1, 0xFFFFFFFF, Ir::ENCODING, byteOrder, Ir::CHAR, Field::INVALID_ID, std::string("varData"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, Field::INVALID_ID, compositeStr1);
        ir.addToken(0, 0, Ir::END_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID, varDataStr1);
        ir.addToken(0, 0, Ir::BEGIN_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID + 1, varDataStr2);
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, Field::INVALID_ID, compositeStr2);
        ir.addToken(0xFFFFFFFF, 1, Ir::ENCODING, byteOrder, Ir::UINT8, Field::INVALID_ID, std::string("length"));
        ir.addToken(0xFFFFFFFF, 0xFFFFFFFF, Ir::ENCODING, byteOrder, Ir::UINT8, Field::INVALID_ID, std::string("varData"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, Field::INVALID_ID, compositeStr2);
        ir.addToken(0, 0, Ir::END_VAR_DATA, byteOrder, Ir::NONE, FIELD_ID + 1, varDataStr2);
        ir.addToken(0, 0xFFFFFFFF, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    void constructUInt8Array(char *dst, const int length, const uint8_t seed)
    {
        uint8_t *array = (uint8_t *)dst;
        for (uint8_t i = 0; i < length; i++)
        {
            array[i] = seed ^ i;
        }
    }

    virtual void constructMessage()
    {
        buffer_[bufferLen_] = VAR_DATA_CHAR_SIZE;
        ::memcpy(buffer_ + bufferLen_ + 1, VAR_DATA_CHAR_STR, VAR_DATA_CHAR_SIZE);
        bufferLen_ += 1 + VAR_DATA_CHAR_SIZE;
        buffer_[bufferLen_] = VAR_DATA_UINT8_SIZE;
        constructUInt8Array(buffer_ + bufferLen_ + 1, VAR_DATA_UINT8_SIZE, VAR_DATA_UINT8_SEED);
        bufferLen_ += 1+ VAR_DATA_UINT8_SIZE;
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            char fieldValue[VAR_DATA_CHAR_SIZE + 1];

            EXPECT_EQ(f.type(), Field::VAR_DATA);
            EXPECT_EQ(f.numEncodings(), 2);
            EXPECT_EQ(f.fieldName(), "VarDataField1");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT8);
            EXPECT_EQ(f.primitiveType(1), Ir::CHAR);
            EXPECT_EQ(f.length(0), 1);
            EXPECT_EQ(f.length(1), static_cast<int>(VAR_DATA_CHAR_SIZE));
            EXPECT_EQ(f.getUInt(0), static_cast< ::uint64_t>(VAR_DATA_CHAR_SIZE));
            f.getArray(1, fieldValue, 0, VAR_DATA_CHAR_SIZE);
            EXPECT_EQ(std::string(fieldValue, VAR_DATA_CHAR_SIZE), VAR_DATA_CHAR_STR);
        }
        else if (numFieldsSeen_ == 3)
        {
            char fieldValue[VAR_DATA_UINT8_SIZE + 1];
            char controlValue[VAR_DATA_UINT8_SIZE + 1];

            constructUInt8Array(controlValue, VAR_DATA_UINT8_SIZE, VAR_DATA_UINT8_SEED);
            EXPECT_EQ(f.type(), Field::VAR_DATA);
            EXPECT_EQ(f.numEncodings(), 2);
            EXPECT_EQ(f.fieldName(), "VarDataField2");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT8);
            EXPECT_EQ(f.primitiveType(1), Ir::UINT8);
            EXPECT_EQ(f.length(0), 1);
            EXPECT_EQ(f.length(1), static_cast<int>(VAR_DATA_UINT8_SIZE));
            EXPECT_EQ(f.getUInt(0), static_cast< ::uint64_t>(VAR_DATA_UINT8_SIZE));
            f.getArray(1, fieldValue, 0, VAR_DATA_UINT8_SIZE);
            EXPECT_EQ(::memcmp(fieldValue, controlValue, VAR_DATA_UINT8_SIZE), 0);
        }
        return 0;
    };

    virtual int onError(const Error &e)
    {
        return OtfMessageTestCBs::onError(e);
    };
};

TEST_F(OtfVariableDataTest, shouldHandleVariableData)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 3);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}
