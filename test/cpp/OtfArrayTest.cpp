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

class OtfCharArrayTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define STR_FIELD_VALUE "a static length field"
#define STR_FIELD_VALUE_SIZE (sizeof(STR_FIELD_VALUE) - 1)

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithCharArray");
        std::string charFieldStr = std::string("CharArrayField");

        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::ENCODING, byteOrder, Ir::CHAR, Ir::INVALID_ID, std::string("char"));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        ::memcpy(buffer_ + bufferLen_, STR_FIELD_VALUE, STR_FIELD_VALUE_SIZE);
        bufferLen_ += STR_FIELD_VALUE_SIZE;
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            char fieldValue[STR_FIELD_VALUE_SIZE + 1];

            EXPECT_EQ(f.type(), Field::ENCODING);
            EXPECT_EQ(f.numEncodings(), 1);
            EXPECT_EQ(f.fieldName(), "CharArrayField");
            EXPECT_EQ(f.primitiveType(), Ir::CHAR);
            EXPECT_EQ(f.length(), STR_FIELD_VALUE_SIZE);  // works for char, uint8, and int8, but not for others
            f.getArray(0, fieldValue, 0, STR_FIELD_VALUE_SIZE);
            EXPECT_EQ(std::string(fieldValue, STR_FIELD_VALUE_SIZE), STR_FIELD_VALUE);
        }
        return 0;
    };
};

TEST_F(OtfCharArrayTest, shouldHandleCharArray)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}

class OtfUInt32ArrayTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define UINT32_ARRAY_LENGTH 20
#define UINT32_ARRAY_SIZE (sizeof(uint32_t) * UINT32_ARRAY_LENGTH)
#define SEED 0xFEEDBEEF

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithUInt32Array");
        std::string charFieldStr = std::string("UInt32ArrayField");

        ir.addToken(0, UINT32_ARRAY_SIZE, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, UINT32_ARRAY_SIZE, Ir::ENCODING, byteOrder, Ir::UINT32, Ir::INVALID_ID, std::string("uint32"));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, UINT32_ARRAY_SIZE, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    void constructUInt32Array(char *dst, const int length, const uint32_t seed)
    {
        uint32_t *array = (uint32_t *)dst;
        for (int i = 0; i < length; i++)
        {
            uint32_t value = seed ^ i;
            ::memcpy(array, &value, sizeof(uint32_t));
            array++;
        }
    }

    virtual void constructMessage()
    {
        constructUInt32Array(buffer_ + bufferLen_, UINT32_ARRAY_LENGTH, SEED);
        bufferLen_ += UINT32_ARRAY_SIZE;
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            char controlValue[UINT32_ARRAY_SIZE];
            char fieldValue[UINT32_ARRAY_SIZE];

            constructUInt32Array(controlValue, UINT32_ARRAY_LENGTH, SEED);

            EXPECT_EQ(f.type(), Field::ENCODING);
            EXPECT_EQ(f.numEncodings(), 1);
            EXPECT_EQ(f.fieldName(), "UInt32ArrayField");
            EXPECT_EQ(f.primitiveType(), Ir::UINT32);
            EXPECT_EQ(f.length(), UINT32_ARRAY_LENGTH);
            f.getArray(0, fieldValue, 0, UINT32_ARRAY_LENGTH);
            EXPECT_EQ(::memcmp(fieldValue, controlValue, UINT32_ARRAY_SIZE), 0);
        }
        return 0;
    };
};

TEST_F(OtfUInt32ArrayTest, shouldHandleUInt32Array)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}

class OtfConstantCharArrayTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("MessageWithCharArray");
        std::string charFieldStr = std::string("CharArrayField");
        const char constStr[] = STR_FIELD_VALUE;

        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::ENCODING, byteOrder, Ir::CHAR, Ir::INVALID_ID, std::string("char"), constStr, strlen(constStr));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, charFieldStr);
        ir.addToken(0, STR_FIELD_VALUE_SIZE, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        // nothing here. It's all constant.
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            char fieldValue[STR_FIELD_VALUE_SIZE + 1];

            EXPECT_EQ(f.type(), Field::ENCODING);
            EXPECT_EQ(f.numEncodings(), 1);
            EXPECT_EQ(f.fieldName(), "CharArrayField");
            EXPECT_EQ(f.primitiveType(), Ir::CHAR);
            EXPECT_EQ(f.length(), STR_FIELD_VALUE_SIZE);
            f.getArray(0, fieldValue, 0, STR_FIELD_VALUE_SIZE);
            EXPECT_EQ(std::string(fieldValue, STR_FIELD_VALUE_SIZE), STR_FIELD_VALUE);
        }
        return 0;
    };
};

TEST_F(OtfConstantCharArrayTest, shouldHandleCharArray)
{
    listener_.dispatchMessageByHeader(messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}
