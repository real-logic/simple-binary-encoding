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

/*
 * Fixture around listener
 */
class OtfMessageTest : public testing::Test, public Ir::Callback
{
public:
    static const int BLOCKLENGTH = 64;
    static const int TEMPLATE_ID = 100;
    static const int VERSION = 1;
    static const int CORRECT_MESSAGEHEADER_SIZE = 6;
    static const int FIELD_ID = 1001;
    static const uint32_t FIELD_VALUE = 0xFEEDDEEF;

protected:
    void constructMessageHeaderIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageHeaderStr = std::string("messageHeader");
        uint16_t schemaId = 0xFFFF;

        // messageHeader
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
        ir.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("blockLength"));
        ir.addToken(2, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("templateId"));
        ir.addToken(4, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("version"));
        ir.addToken(5, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("reserved"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
    };

    void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("Message1");
        std::string fieldStr = std::string("Field1");

        ir.addToken(0, 4, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 4, Ir::ENCODING, byteOrder, Ir::UINT32, 0xFFFF, std::string("uint32"));
        ir.addToken(0, 0, Ir::END_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 4, Ir::END_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
    };

    char *constructMessageHeader(const uint16_t blockLength, const uint16_t templateId, const uint8_t version, const int messageSize)
    {
        // do not expose outside this function, so we keep ourselves honest
        struct MessageHeader
        {
            uint16_t blockLength;
            uint16_t templateId;
            uint8_t version;
            uint8_t reserved;
        };

        char *buffer = new char[sizeof(struct MessageHeader) + messageSize];
        struct MessageHeader *msgHdr = (struct MessageHeader *)buffer;
        msgHdr->blockLength = blockLength;
        msgHdr->templateId = templateId;
        msgHdr->version = version;
        msgHdr->reserved = 0;
        return buffer;
    };

    virtual void SetUp()
    {
        constructMessageHeaderIr(messageHeaderIr_);
        constructMessageIr(messageIr_);
        buffer_ = constructMessageHeader(BLOCKLENGTH, TEMPLATE_ID, VERSION, sizeof(uint32_t));
        uint32_t *value = (uint32_t *)(buffer_ + CORRECT_MESSAGEHEADER_SIZE);
        *value = FIELD_VALUE;
        bufferLen_ = CORRECT_MESSAGEHEADER_SIZE + sizeof(uint32_t);
    };

    virtual void TearDown()
    {
        delete[] buffer_;
    };

    virtual Ir *irForTemplateId(const int templateId)
    {
        EXPECT_EQ(templateId, TEMPLATE_ID);
        return &messageIr_;
    };

    Ir messageHeaderIr_;
    Ir messageIr_;
    Listener listener_;
    char *buffer_;
    int bufferLen_;
};

const int OtfMessageTest::BLOCKLENGTH;
const int OtfMessageTest::TEMPLATE_ID;
const int OtfMessageTest::VERSION;
const int OtfMessageTest::CORRECT_MESSAGEHEADER_SIZE;
const int OtfMessageTest::FIELD_ID;
const uint32_t OtfMessageTest::FIELD_VALUE;

/*
 * Base class for CBs to track seen callbacks
 */
class OtfMessageTestCBs : public OnNext, public OnError, public OnCompleted
{
public:
    OtfMessageTestCBs() : numFieldsSeen_(0), numErrorsSeen_(0), numCompletedsSeen_(0) {};

    virtual int onNext(const Field &f)
    {
        numFieldsSeen_++;
        return 0;
    };

    virtual int onError(const Error &e)
    {
        numErrorsSeen_++;
        return 0;
    };

    virtual int onCompleted()
    {
        numCompletedsSeen_++;
        return 0;
    };

    int numFieldsSeen_;
    int numErrorsSeen_;
    int numCompletedsSeen_;
};

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
            EXPECT_EQ(f.valueUInt(0), OtfMessageTest::BLOCKLENGTH);
            EXPECT_EQ(f.valueUInt(1), OtfMessageTest::TEMPLATE_ID);
            EXPECT_EQ(f.valueUInt(2), OtfMessageTest::VERSION);
            EXPECT_EQ(f.valueUInt(3), 0);
        }
        else if (numFieldsSeen_ == 2)
        {
            EXPECT_EQ(f.type(), Field::ENCODING);
            EXPECT_EQ(f.name(Field::FIELD_INDEX), "Field1");
            EXPECT_EQ(f.schemaId(), OtfMessageTest::FIELD_ID);
            EXPECT_EQ(f.numEncodings(), 1);
            EXPECT_EQ(f.name(0), "uint32");
            EXPECT_EQ(f.primitiveType(0), Ir::UINT32);
            EXPECT_EQ(f.valueUInt(0), OtfMessageTest::FIELD_VALUE);
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

    bufferLen_ -= sizeof(uint32_t);
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

    bufferLen_ -= sizeof(uint32_t);
    listener_.dispatchMessageByHeader(std::string("templateId"), messageHeaderIr_, &ircb)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(&cbs, &cbs, &cbs);
    EXPECT_EQ(cbs.numFieldsSeen_, 1);
    EXPECT_EQ(cbs.numErrorsSeen_, 1);
    EXPECT_EQ(cbs.numCompletedsSeen_, 0);
}

/*
 * TODO: test reuse of listener
 * TODO: test offset values on fields
 * TODO: test every type (encoded data type, composite, enum, and set) in a single message
 * TODO: test every primitiveType
 * TODO: single repeating group
 * TODO: nested repeating group - MassQuote
 * TODO: variable length fields
 */
