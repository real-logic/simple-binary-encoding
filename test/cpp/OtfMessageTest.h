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
#ifndef _OTFMESSAGETEST_H_
#define _OTFMESSAGETEST_H_

class OtfIrMessageTestWrapper : public Ir
{
public:
    OtfIrMessageTestWrapper() : Ir(NULL, 0, -1, -1, -1) {};
};

/*
 * Fixture around listener
 */
class OtfMessageTest : public testing::Test, public Ir::Callback
{
public:
#define BLOCKLENGTH 64
#define TEMPLATE_ID 100
#define VERSION 0
#define CORRECT_MESSAGEHEADER_SIZE 6
#define FIELD_ID 1001
#define FIELD_VALUE 0xFEEDBEEF

protected:
    void constructMessageHeaderIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageHeaderStr = std::string("messageHeader");
        int32_t schemaId = Field::INVALID_ID;

        // messageHeader
        ir.addToken(0, 0, Ir::BEGIN_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
        ir.addToken(0, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("blockLength"));
        ir.addToken(2, 2, Ir::ENCODING, byteOrder, Ir::UINT16, schemaId, std::string("templateId"));
        ir.addToken(4, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("version"));
        ir.addToken(5, 1, Ir::ENCODING, byteOrder, Ir::UINT8, schemaId, std::string("reserved"));
        ir.addToken(0, 0, Ir::END_COMPOSITE, byteOrder, Ir::NONE, schemaId, messageHeaderStr);
    };

    virtual void constructMessageIr(Ir &ir)
    {
        Ir::TokenByteOrder byteOrder = Ir::SBE_LITTLE_ENDIAN;
        std::string messageStr = std::string("Message1");
        std::string fieldStr = std::string("Field1");

        ir.addToken(0, 4, Ir::BEGIN_MESSAGE, byteOrder, Ir::NONE, TEMPLATE_ID, messageStr);
        ir.addToken(0, 0, Ir::BEGIN_FIELD, byteOrder, Ir::NONE, FIELD_ID, fieldStr);
        ir.addToken(0, 4, Ir::ENCODING, byteOrder, Ir::UINT32, Field::INVALID_ID, std::string("uint32"));
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
        bufferLen_ = CORRECT_MESSAGEHEADER_SIZE;
        return buffer;
    };

    virtual void constructMessage()
    {
        uint32_t *value = (uint32_t *)(buffer_ + CORRECT_MESSAGEHEADER_SIZE);
        *value = FIELD_VALUE;
        bufferLen_ += sizeof(uint32_t);
    }

    virtual void SetUp()
    {
        constructMessageHeaderIr(messageHeaderIr_);
        constructMessageIr(messageIr_);
        buffer_ = constructMessageHeader(BLOCKLENGTH, TEMPLATE_ID, VERSION, 1024); // 1024 should be enough room
        constructMessage();
    };

    virtual void TearDown()
    {
        delete[] buffer_;
    };

    virtual Ir *irForTemplateId(const int templateId, const int version)
    {
        EXPECT_EQ(templateId, TEMPLATE_ID);
        EXPECT_EQ(version, VERSION);
        return &messageIr_;
    };

    OtfIrMessageTestWrapper messageHeaderIr_;
    OtfIrMessageTestWrapper messageIr_;
    Listener listener_;
    char *buffer_;
    int bufferLen_;
};

#endif /* _OTFMESSAGETEST_H_ */
