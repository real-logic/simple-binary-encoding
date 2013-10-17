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

class OtfMessageTestMessage : public OtfMessageTestCBs
{
public:
    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 1)
        {
            EXPECT_EQ(f.type(), Field::COMPOSITE);
            EXPECT_EQ(f.name(Field::FIELD_INDEX), "");
            EXPECT_EQ(f.name(Field::COMPOSITE_INDEX), "messageHeader");
            EXPECT_EQ(f.schemaId(), Field::INVALID_ID);
            EXPECT_EQ(f.numEncodings(), 4);
            EXPECT_EQ(f.name(0), "blockLength");
            EXPECT_EQ(f.name(1), "templateId");
            EXPECT_EQ(f.name(2), "version");
            EXPECT_EQ(f.name(3), "reserved");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
            EXPECT_EQ(f.primitiveType(1), Ir::UINT16);
            EXPECT_EQ(f.primitiveType(2), Ir::UINT8);
            EXPECT_EQ(f.primitiveType(3), Ir::UINT8);
            EXPECT_EQ(f.valueUInt(0), BLOCKLENGTH);
            EXPECT_EQ(f.valueUInt(1), TEMPLATE_ID);
            EXPECT_EQ(f.valueUInt(2), VERSION);
            EXPECT_EQ(f.valueUInt(3), 0);
        }
        else if (numFieldsSeen_ == 2)
        {
            EXPECT_EQ(f.type(), Field::ENCODING);
            EXPECT_EQ(f.name(Field::FIELD_INDEX), "Field1");
            EXPECT_EQ(f.schemaId(), FIELD_ID);
            EXPECT_EQ(f.numEncodings(), 1);
            EXPECT_EQ(f.name(0), "uint32");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT32);
            EXPECT_EQ(f.valueUInt(0), FIELD_VALUE);
        }
        return 0;
    };
};

TEST_F(OtfMessageTest, shouldHandleMessageDispatch)
{
    OtfMessageTestMessage cbs;

    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 2);
    EXPECT_EQ(cbs.numErrorsSeen_, 0);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithOnCompleted)
{
    OtfMessageTestMessage cbs;

    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 2);
    EXPECT_EQ(cbs.numErrorsSeen_, 0);
    EXPECT_EQ(cbs.numCompletedsSeen_, 1);
}

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithTooShortMessage)
{
    OtfMessageTestMessage cbs;

    bufferLen_ -= 2;
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 1);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithTooShortMessageHeader)
{
    OtfMessageTestMessage cbs;

    bufferLen_ -= 5;
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 0);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithNoRoomForMessage)
{
    OtfMessageTestMessage cbs;

    bufferLen_ -= sizeof(uint32_t);
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 1);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithNoTemplateIDEncodingName)
{
    OtfMessageTestMessage cbs;

    listener_.dispatchMessageByHeader(std::string("notGoingToBeFound!"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 1);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

class OtfMessageTestIrCallbackNULL : public Ir::Callback
{
public:
    virtual Ir *irForTemplateId(const int templateId)
    {
        return NULL;
    };
};

TEST_F(OtfMessageTest, shouldHandleMessageDispatchWithIrCallbackReturningNULL)
{
    OtfMessageTestMessage cbs;
    OtfMessageTestIrCallbackNULL ircb;

    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, &ircb)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 1);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

class OtfMessageAllPrimitiveTypesTest : public OtfMessageTest, public OtfMessageTestCBs
{
protected:
#define FIELD_CHAR_VALUE 0xB
#define FIELD_INT8_VALUE -0xB
#define FIELD_INT16_VALUE -0x0EED
#define FIELD_INT32_VALUE -0x0EEDBEEF
#define FIELD_INT64_VALUE -0x0EEDFEEDBEEFL
#define FIELD_UINT8_VALUE 0xB
#define FIELD_UINT16_VALUE 0xDEED
#define FIELD_UINT32_VALUE 0xFEEDBEEF
#define FIELD_UINT64_VALUE 0xDEEDFEEDBEEFL
#define FIELD_FLOAT_VALUE 7.11f
#define FIELD_DOUBLE_VALUE 711.711f

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("Message1");
        std::string fieldStr = std::string("Field1");
        std::string compositeStr = std::string("AllTypes");

        ir.addToken(0, 43, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, 0xFFFF, compositeStr);
        ir.addToken(0, 1, Ir::ENCODING, byteOrder, Ir::CHAR, 0xFFFF, std::string("char"));
        ir.addToken(1, 1, Ir::ENCODING, byteOrder, Ir::INT8, 0xFFFF, std::string("int8"));
        ir.addToken(2, 2, Ir::ENCODING, byteOrder, Ir::INT16, 0xFFFF, std::string("int16"));
        ir.addToken(4, 4, Ir::ENCODING, byteOrder, Ir::INT32, 0xFFFF, std::string("int32"));
        ir.addToken(8, 8, Ir::ENCODING, byteOrder, Ir::INT64, 0xFFFF, std::string("int64"));
        ir.addToken(16, 1, Ir::ENCODING, byteOrder, Ir::UINT8, 0xFFFF, std::string("uint8"));
        ir.addToken(17, 2, Ir::ENCODING, byteOrder, Ir::UINT16, 0xFFFF, std::string("uint16"));
        ir.addToken(19, 4, Ir::ENCODING, byteOrder, Ir::UINT32, 0xFFFF, std::string("uint32"));
        ir.addToken(23, 8, Ir::ENCODING, byteOrder, Ir::UINT64, 0xFFFF, std::string("uint64"));
        ir.addToken(31, 4, Ir::ENCODING, byteOrder, Ir::FLOAT, 0xFFFF, std::string("float"));
        ir.addToken(35, 8, Ir::ENCODING, byteOrder, Ir::DOUBLE, 0xFFFF, std::string("double"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, 0xFFFF, compositeStr);
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 43, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    virtual void constructMessage()
    {
        *((char *)(buffer_ + bufferLen_)) = FIELD_CHAR_VALUE;
        bufferLen_ += sizeof(char);

        *((int8_t *)(buffer_ + bufferLen_)) = FIELD_INT8_VALUE;
        bufferLen_ += sizeof(int8_t);

        *((int16_t *)(buffer_ + bufferLen_)) = FIELD_INT16_VALUE;
        bufferLen_ += sizeof(int16_t);

        *((int32_t *)(buffer_ + bufferLen_)) = FIELD_INT32_VALUE;
        bufferLen_ += sizeof(int32_t);

        *((int64_t *)(buffer_ + bufferLen_)) = FIELD_INT64_VALUE;
        bufferLen_ += sizeof(int64_t);

        *((uint8_t *)(buffer_ + bufferLen_)) = FIELD_UINT8_VALUE;
        bufferLen_ += sizeof(uint8_t);

        *((uint16_t *)(buffer_ + bufferLen_)) = FIELD_UINT16_VALUE;
        bufferLen_ += sizeof(uint16_t);

        *((uint32_t *)(buffer_ + bufferLen_)) = FIELD_UINT32_VALUE;
        bufferLen_ += sizeof(uint32_t);

        *((uint64_t *)(buffer_ + bufferLen_)) = FIELD_UINT64_VALUE;
        bufferLen_ += sizeof(uint64_t);

        *((float *)(buffer_ + bufferLen_)) = FIELD_FLOAT_VALUE;
        bufferLen_ += sizeof(float);

        *((double *)(buffer_ + bufferLen_)) = FIELD_DOUBLE_VALUE;
        bufferLen_ += sizeof(double);
    };

    virtual int onNext(const Field &f)
    {
        OtfMessageTestCBs::onNext(f);

        if (numFieldsSeen_ == 2)
        {
            EXPECT_EQ(f.numEncodings(), 11);
            EXPECT_EQ(f.primitiveType(0), Ir::CHAR);
            EXPECT_EQ(f.primitiveType(1), Ir::INT8);
            EXPECT_EQ(f.primitiveType(2), Ir::INT16);
            EXPECT_EQ(f.primitiveType(3), Ir::INT32);
            EXPECT_EQ(f.primitiveType(4), Ir::INT64);
            EXPECT_EQ(f.primitiveType(5), Ir::UINT8);
            EXPECT_EQ(f.primitiveType(6), Ir::UINT16);
            EXPECT_EQ(f.primitiveType(7), Ir::UINT32);
            EXPECT_EQ(f.primitiveType(8), Ir::UINT64);
            EXPECT_EQ(f.primitiveType(9), Ir::FLOAT);
            EXPECT_EQ(f.primitiveType(10), Ir::DOUBLE);
            EXPECT_EQ(f.valueInt(0), FIELD_CHAR_VALUE);
            EXPECT_EQ(f.valueInt(1), FIELD_INT8_VALUE);
            EXPECT_EQ(f.valueInt(2), FIELD_INT16_VALUE);
            EXPECT_EQ(f.valueInt(3), FIELD_INT32_VALUE);
            EXPECT_EQ(f.valueInt(4), FIELD_INT64_VALUE);
            EXPECT_EQ(f.valueUInt(5), FIELD_UINT8_VALUE);
            EXPECT_EQ(f.valueUInt(6), FIELD_UINT16_VALUE);
            EXPECT_EQ(f.valueUInt(7), FIELD_UINT32_VALUE);
            EXPECT_EQ(f.valueUInt(8), FIELD_UINT64_VALUE);
            EXPECT_EQ(f.valueDouble(9), FIELD_FLOAT_VALUE);
            EXPECT_EQ(f.valueDouble(10), FIELD_DOUBLE_VALUE);
        }
        return 0;
    };
};

TEST_F(OtfMessageAllPrimitiveTypesTest, shouldHandleAllTypes)
{
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, this)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 2);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}

/*
 * TODO: test reuse of listener
 * TODO: test offset values on fields
 * TODO: test every type (encoded data type, composite, enum, and set) in a single message
 * TODO: single repeating group
 * TODO: nested repeating group - MassQuote
 * TODO: variable length fields
 */
