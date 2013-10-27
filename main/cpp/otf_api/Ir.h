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
#ifndef _IR_H_
#define _IR_H_

#include <string>

namespace sbe {
namespace on_the_fly {

/**
 * Class that acts as an iterator and accessor over a serialized IR token list
 */
class Ir
{
public:
    /// Invalid message template ID
    static const int INVALID_ID = 0xFFFF;
    /// Value representing a variable length field (size)
    static const uint32_t VARIABLE_SIZE = 0xFFFFFFFF;

    enum TokenSignal
    {
        BEGIN_MESSAGE = 1,
        END_MESSAGE = 2,
        BEGIN_COMPOSITE = 3,
        END_COMPOSITE = 4,
        BEGIN_FIELD = 5,
        END_FIELD = 6,
        BEGIN_GROUP = 7,
        END_GROUP = 8,
        BEGIN_ENUM = 9,
        VALID_VALUE = 10,
        END_ENUM = 11,
        BEGIN_SET = 12,
        CHOICE = 13,
        END_SET = 14,
        BEGIN_VAR_DATA = 15,
        END_VAR_DATA = 16,
        ENCODING = 17
    };

    enum TokenByteOrder
    {
        SBE_LITTLE_ENDIAN = 0,
        SBE_BIG_ENDIAN = 1
    };

    enum TokenPrimitiveType
    {
        CHAR = 1,
        INT8 = 2,
        INT16 = 3,
        INT32 = 4,
        INT64 = 5,
        UINT8 = 6,
        UINT16 = 7,
        UINT32 = 8,
        UINT64 = 9,
        FLOAT = 10,
        DOUBLE = 11,
        NONE = 255
    };

    /**
     * Interface for returning an IR for a given templateId value
     */
    class Callback
    {
    public:
        virtual Ir *irForTemplateId(const int templateId) = 0;
    };

    // constructors and destructors

    Ir(const char *buffer = NULL, const int len = 0);

    virtual ~Ir()
    {
        if (buffer_ != NULL)
        {
            delete[] buffer_;
            buffer_ = NULL;
        }
    };

    /// iterator methods for IrTokens

    void begin();
    void next();
    bool end() const;

    /// access methods for current IR Token

    uint32_t offset() const;
    uint32_t size() const;
    TokenSignal signal() const;
    TokenByteOrder byteOrder() const;
    TokenPrimitiveType primitiveType() const;
    uint16_t schemaId() const;
    uint64_t validValue() const;
    uint64_t choiceValue() const;
    uint8_t nameLen() const;
    std::string name() const;
    uint64_t constLen() const;
    const char *constVal() const;
    int position() const;

    /// rewind or fast-forward IR to given position
    void position(int pos);

    // used by test fixtures to generate IR for tests - initial call allocates max sized buffer
    void addToken(uint32_t offset,
                  uint32_t size,
                  TokenSignal signal,
                  TokenByteOrder byteOrder,
                  TokenPrimitiveType primitiveType,
                  uint16_t schemaId,
                  const std::string &name,
                  const char *constVal = NULL);

    static unsigned int size(TokenPrimitiveType type)
    {
        switch (type)
        {
        case CHAR:
        case INT8:
        case UINT8:
            return 1;
            break;
        case INT16:
        case UINT16:
            return 2;
            break;
        case INT32:
        case UINT32:
        case FLOAT:
            return 4;
            break;
        case INT64:
        case UINT64:
        case DOUBLE:
            return 8;
            break;
        case NONE:
        deefault:
            return 0;
            break;
        }
    }

private:
    const char *buffer_;
    int len_;
    int cursorOffset_;
};

} // namespace on_the_fly
} // namespace sbe

#endif /* _IR_H_ */
