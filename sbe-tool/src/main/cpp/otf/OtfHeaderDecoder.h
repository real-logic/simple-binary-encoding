/*
 * Copyright 2015 - 2016 Real Logic Ltd.
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
#ifndef _OTF_HEADERDECODER_H
#define _OTF_HEADERDECODER_H

#include <cstdint>
#include <memory>
#include <vector>
#include <string>
#include <algorithm>

#include "Token.h"

namespace sbe {
namespace otf {

class OtfHeaderDecoder
{
public:
    OtfHeaderDecoder(std::shared_ptr<std::vector<Token>> tokens) :
        m_tokens(tokens)
    {
        m_encodedLength = tokens->at(0).encodedLength();

        std::for_each(tokens->begin(), tokens->end(), [&](Token& token)
        {
            const std::string& name = token.name();
            const Encoding& encoding = token.encoding();

            if (name == "blockLength")
            {
                m_blockLengthOffset = token.offset();
                m_blockLengthType = encoding.primitiveType();
                m_blockLengthByteOrder = encoding.byteOrder();
            }
            else if (name == "templateId")
            {
                m_templateIdOffset = token.offset();
                m_templateIdType = encoding.primitiveType();
                m_templateIdByteOrder = encoding.byteOrder();
            }
            else if (name == "schemaId")
            {
                m_schemaIdOffset = token.offset();
                m_schemaIdType = encoding.primitiveType();
                m_schemaIdByteOrder = encoding.byteOrder();
            }
            else if (name == "version")
            {
                m_schemaVersionOffset = token.offset();
                m_schemaVersionType = encoding.primitiveType();
                m_schemaVersionByteOrder = encoding.byteOrder();
            }

        });
    }

    inline std::uint32_t encodedLength() const
    {
        return static_cast<std::uint32_t>(m_encodedLength);
    }

    /*
     * All elements must be unsigned integers according to RC3
     */

    std::uint64_t getTemplateId(const char *headerBuffer)
    {
        return Encoding::getUInt(m_templateIdType, m_templateIdByteOrder, headerBuffer + m_templateIdOffset);
    }

    std::uint64_t getSchemaId(const char *headerBuffer)
    {
        return Encoding::getUInt(m_schemaIdType, m_schemaIdByteOrder, headerBuffer + m_schemaIdOffset);
    }

    std::uint64_t getSchemaVersion(const char *headerBuffer)
    {
        return Encoding::getUInt(m_schemaVersionType, m_schemaVersionByteOrder, headerBuffer + m_schemaVersionOffset);
    }

    std::uint64_t getBlockLength(const char *headerBuffer)
    {
        return Encoding::getUInt(m_blockLengthType, m_blockLengthByteOrder, headerBuffer + m_blockLengthOffset);
    }

private:
    std::shared_ptr<std::vector<Token>> m_tokens;
    std::int32_t m_encodedLength;
    std::int32_t m_blockLengthOffset;
    std::int32_t m_templateIdOffset;
    std::int32_t m_schemaIdOffset;
    std::int32_t m_schemaVersionOffset;
    PrimitiveType m_blockLengthType;
    PrimitiveType m_templateIdType;
    PrimitiveType m_schemaIdType;
    PrimitiveType m_schemaVersionType;
    ByteOrder m_blockLengthByteOrder;
    ByteOrder m_templateIdByteOrder;
    ByteOrder m_schemaIdByteOrder;
    ByteOrder m_schemaVersionByteOrder;
};

}}

#endif
