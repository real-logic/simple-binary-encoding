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
        ENCODING = 15
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
        UINT64 = 9
    };

    Ir(const char *buffer, const int len);

    virtual ~Ir() {
        delete [] buffer_;
    };

    void begin();
    void next();
    bool end();

    /// access methods for current IR Token

    uint32_t offset();
    uint32_t size();
    TokenSignal signal();
    TokenByteOrder byteOrder();
    TokenPrimitiveType primitiveType();
    uint16_t schemaId();
    uint8_t nameLen();
    std::string name();

    /**
     * Interface for returning an IR for a given templateId value
     */
    class Callback
    {
    public:
        virtual Ir *irForTemplateId(const int templateId) = 0;
    };

protected:
    // TODO: used by test fitures to generate IR for tests - initial call allocates max sized buffer
    //void addToken(offset, size, signal, byteOrder, primitiveType, schemaId, nameLen, name);

private:
    const char *buffer_;
    const int len_;
    int cursorOffset_;
};

} // namespace on_the_fly
} // namespace sbe

#endif /* _IR_H_ */
