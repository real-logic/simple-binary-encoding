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

class OtfIrMessageHeaderTestWrapper : public Ir
{
public:
    OtfIrMessageHeaderTestWrapper() : Ir(NULL, 0, -1, -1, -1) {};
};

/*
 * Fixture around listener that also is an OnNext and OnError
 */
class OtfMessageHeaderTest : public testing::Test, public OnNext, public OnError, public OnCompleted
{
protected:
    static const unsigned int BLOCKLENGTH = 64;
    static const unsigned int TEMPLATE_ID = 100;
    static const unsigned int VERSION = 1;
    static const unsigned int CORRECT_MESSAGEHEADER_SIZE = 6;

    void addMessageHeaderIr()
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

    char *constructMessageHeader(const uint16_t blockLength, const uint16_t templateId, const uint8_t version)
    {
        // do not expose outside this function, so we keep ourselves honest
        struct MessageHeader
        {
            uint16_t blockLength;
            uint16_t templateId;
            uint8_t version;
            uint8_t reserved;
        };

        char *buffer = new char[sizeof(struct MessageHeader)];
        struct MessageHeader *msgHdr = (struct MessageHeader *)buffer;
        msgHdr->blockLength = blockLength;
        msgHdr->templateId = templateId;
        msgHdr->version = version;
        msgHdr->reserved = 0;
        return buffer;
    };

    virtual void SetUp()
    {
        addMessageHeaderIr();
        buffer_ = constructMessageHeader(BLOCKLENGTH, TEMPLATE_ID, VERSION);
        bufferLen_ = CORRECT_MESSAGEHEADER_SIZE;
        numFieldsSeen_ = 0;
        numErrorsSeen_ = 0;
        numCompletedsSeen_ = 0;
    };

    virtual void TearDown()
    {
        delete[] buffer_;
    };

    virtual int onNext(const Field &f)
    {
        EXPECT_EQ(f.type(), Field::COMPOSITE);
        EXPECT_EQ(f.fieldName(), "");
        EXPECT_EQ(f.compositeName(), "messageHeader");
        EXPECT_EQ(f.schemaId(), Field::INVALID_ID);
        EXPECT_EQ(f.numEncodings(), 4);
        EXPECT_EQ(f.encodingName(0), "blockLength");
        EXPECT_EQ(f.encodingName(1), "templateId");
        EXPECT_EQ(f.encodingName(2), "version");
        EXPECT_EQ(f.encodingName(3), "reserved");
        EXPECT_EQ(f.primitiveType(0), Ir::UINT16);
        EXPECT_EQ(f.primitiveType(1), Ir::UINT16);
        EXPECT_EQ(f.primitiveType(2), Ir::UINT8);
        EXPECT_EQ(f.primitiveType(3), Ir::UINT8);
        EXPECT_EQ(f.getUInt(0), BLOCKLENGTH);
        EXPECT_EQ(f.getUInt(1), TEMPLATE_ID);
        EXPECT_EQ(f.getUInt(2), VERSION);
        EXPECT_EQ(f.getUInt(3), 0u);
        numFieldsSeen_++;
        return 0;
    };

    virtual int onNext(const Group &p)
    {
        return 0;
    };

    virtual int onError(const Error &e)
    {
        EXPECT_EQ(e.message(), "buffer too short");
        numErrorsSeen_++;
        return 0;
    };

    virtual int onCompleted()
    {
        numCompletedsSeen_++;
        return 0;
    }

    Listener listener_;
    OtfIrMessageHeaderTestWrapper ir_;
    char *buffer_;
    int bufferLen_;
    int numFieldsSeen_;
    int numErrorsSeen_;
    int numCompletedsSeen_;
};

const unsigned int OtfMessageHeaderTest::BLOCKLENGTH;
const unsigned int OtfMessageHeaderTest::TEMPLATE_ID;
const unsigned int OtfMessageHeaderTest::VERSION;
const unsigned int OtfMessageHeaderTest::CORRECT_MESSAGEHEADER_SIZE;

TEST_F(OtfMessageHeaderTest, shouldHandleMessageHeader)
{
    listener_.ir(ir_)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this);
    EXPECT_EQ(numFieldsSeen_, 1);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 0);
}

TEST_F(OtfMessageHeaderTest, shouldHandleMessageHeaderWithOnCompleted)
{
    listener_.ir(ir_)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 1);
    EXPECT_EQ(numErrorsSeen_, 0);
    EXPECT_EQ(numCompletedsSeen_, 1);
}

TEST_F(OtfMessageHeaderTest, shouldHandleTooShortMessageHeader)
{
    bufferLen_ -= 2; // make the data too short. Should generate OnError
    listener_.ir(ir_)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this);
    EXPECT_EQ(numFieldsSeen_, 0);
    EXPECT_EQ(numErrorsSeen_, 1);
    EXPECT_EQ(numCompletedsSeen_, 0);
}

TEST_F(OtfMessageHeaderTest, shouldHandleTooShortMessageHeaderWithOnCompleted)
{
    bufferLen_ -= 2; // make the data too short. Should generate OnError, and no OnCompleted
    listener_.ir(ir_)
        .resetForDecode(buffer_, bufferLen_)
        .subscribe(this, this, this);
    EXPECT_EQ(numFieldsSeen_, 0);
    EXPECT_EQ(numErrorsSeen_, 1);
    EXPECT_EQ(numCompletedsSeen_, 0);
}
