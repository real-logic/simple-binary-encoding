/*
 * Copyright 2015 Real Logic Ltd.
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
#ifndef _OTF_MESSAGEDECODER_H
#define _OTF_MESSAGEDECODER_H

#include <functional>
#include <vector>

#include "Token.h"

namespace sbe {
namespace otf {
namespace OtfMessageDecoder {

typedef std::function<void(Token& token)> on_begin_message_t;

typedef std::function<void(Token& token)> on_end_message_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    Token& typeToken,
    std::uint64_t actingVersion)> on_encoding_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    std::vector<Token>& tokens,
    int fromIndex,
    int toIndex,
    std::uint64_t actingVersion)> on_enum_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    std::vector<Token>& tokens,
    int fromIndex,
    int toIndex,
    std::uint64_t actingVersion)> on_bit_set_t;

typedef std::function<void(
    Token& fieldToken,
    std::vector<Token>& tokens,
    int fromIndex,
    int toIndex)> on_begin_composite_t;

typedef std::function<void(
    Token& fieldToken,
    std::vector<Token>& tokens,
    int fromIndex,
    int toIndex)> on_end_composite_t;

typedef std::function<void(
    Token& token,
    int numInGroup)> on_group_header_t;

typedef std::function<void(
    Token& token,
    int groupIndex,
    int numInGroup)> on_begin_group_t;

typedef std::function<void(
    Token& token,
    int groupIndex,
    int numInGroup)> on_end_group_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    size_t length,
    Token& typeToken)> on_var_data_t;

// Builder allows us to avoid vtable overhead and leverage some ease of use
class TokenListenerBuilder
{
public:

    inline TokenListenerBuilder& onBeginMessage(on_begin_message_t onBeginMessage)
    {
        m_onBeginMessage = onBeginMessage;
        return *this;
    }

    inline TokenListenerBuilder& onEndMessage(on_end_message_t onEndMessage)
    {
        m_onEndMessage = onEndMessage;
        return *this;
    }

    inline TokenListenerBuilder& onEncoding(on_encoding_t onEncoding)
    {
        m_onEncoding = onEncoding;
        return *this;
    }

    inline TokenListenerBuilder& onEnum(on_enum_t onEnum)
    {
        m_onEnum = onEnum;
        return *this;
    }

    inline TokenListenerBuilder& onBitSet(on_bit_set_t onBitSet)
    {
        m_onBitSet = onBitSet;
        return *this;
    }

    inline TokenListenerBuilder& onBeginComposite(on_begin_composite_t onBeginComposite)
    {
        m_onBeginComposite = onBeginComposite;
        return *this;
    }

    inline TokenListenerBuilder& onEndComposite(on_begin_composite_t onEndComposite)
    {
        m_onEndComposite = onEndComposite;
        return *this;
    }

    inline TokenListenerBuilder& onGroupHeader(on_group_header_t onGroupHeader)
    {
        m_onGroupHeader = onGroupHeader;
        return *this;
    }

    inline TokenListenerBuilder& onBeginGroup(on_begin_group_t onBeginGroup)
    {
        m_onBeginGroup = onBeginGroup;
        return *this;
    }

    inline TokenListenerBuilder& onEndGroup(on_begin_group_t onEndGroup)
    {
        m_onEndGroup = onEndGroup;
        return *this;
    }

    inline TokenListenerBuilder& onVarData(on_var_data_t onVarData)
    {
        m_onVarData = onVarData;
        return *this;
    }

    inline on_begin_message_t onBeginMessage() const
    {
        return m_onBeginMessage;
    }

    inline on_end_message_t onEndMessage() const
    {
        return m_onEndMessage;
    }

    inline on_encoding_t onEncoding() const
    {
        return m_onEncoding;
    }

    inline on_enum_t onEnum() const
    {
        return m_onEnum;
    }

    inline on_bit_set_t onBitSet() const
    {
        return m_onBitSet;
    }

    inline on_begin_composite_t onBeginComposite() const
    {
        return m_onBeginComposite;
    }

    inline on_end_composite_t onEndComposite() const
    {
        return m_onEndComposite;
    }

    inline on_group_header_t onGroupHeader() const
    {
        return m_onGroupHeader;
    }

    inline on_begin_group_t onBeginGroup() const
    {
        return m_onBeginGroup;
    }

    inline on_end_group_t onEndGroup() const
    {
        return m_onEndGroup;
    }

    inline on_var_data_t onVarData() const
    {
        return m_onVarData;
    }

private:
    on_begin_message_t m_onBeginMessage = [](Token&) { /* no op */ };
    on_end_message_t m_onEndMessage = [](Token&) { /* no op */ };
    on_encoding_t m_onEncoding = [](Token&, const char *, Token&, std::uint64_t) { /* no op */ };
    on_enum_t m_onEnum = [](Token&, const char *, std::vector<Token>&, int, int, std::uint64_t) { /* no op */ };
    on_bit_set_t m_onBitSet = [](Token&, const char *, std::vector<Token>&, int, int, std::uint64_t) { /* no op */ };
    on_begin_composite_t m_onBeginComposite = [](Token&, std::vector<Token>&, int, int) { /* no op */ };
    on_end_composite_t m_onEndComposite = [](Token&, std::vector<Token>&, int, int) { /* no op */ };
    on_group_header_t m_onGroupHeader = [](Token&, int) { /* no op */ };
    on_begin_group_t m_onBeginGroup = [](Token&, int, int) { /* no op */ };
    on_end_group_t m_onEndGroup = [](Token&, int, int) { /* no op */ };
    on_var_data_t m_onVarData = [](Token&, const char *, size_t, Token&) { /* no op */ };
};

template<typename TokenListener>
static void decodeComposite(
    Token& fieldToken,
    const char *buffer,
    std::shared_ptr<std::vector<Token>> tokens,
    size_t tokenIndex,
    size_t toIndex,
    std::uint64_t actingVersion,
    TokenListener& listener)
{
    listener.onBeginComposite(fieldToken, *tokens.get(), tokenIndex, toIndex);

    for (size_t i = tokenIndex + 1; i < toIndex; i++)
    {
        Token &token = tokens->at(i);
        listener.onEncoding(token, buffer + token.offset(), token, actingVersion);
    }

    listener.onEndComposite(fieldToken, *tokens.get(), tokenIndex, toIndex);
}

template<typename TokenListener>
static size_t decodeFields(
    const char *buffer,
    size_t length,
    std::uint64_t actingVersion,
    std::shared_ptr<std::vector<Token>> tokens,
    size_t tokenIndex,
    const size_t numTokens,
    TokenListener& listener)
{
    while (tokenIndex < numTokens)
    {
        Token& fieldToken = tokens->at(tokenIndex);
        if (Signal::BEGIN_FIELD != fieldToken.signal())
        {
            break;
        }

        const size_t nextFieldIndex = tokenIndex + fieldToken.componentTokenCount();
        tokenIndex++;

        Token& typeToken = tokens->at(tokenIndex);
        const int offset = typeToken.offset();

        switch (typeToken.signal())
        {
            case Signal::BEGIN_COMPOSITE:
                decodeComposite<TokenListener>(
                    fieldToken, buffer + offset, tokens, tokenIndex, nextFieldIndex - 2, actingVersion, listener);
                break;
            case Signal::BEGIN_ENUM:
                listener.onEnum(fieldToken, buffer + offset, *tokens.get(), tokenIndex, nextFieldIndex - 2, actingVersion);
                break;
            case Signal::BEGIN_SET:
                listener.onBitSet(fieldToken, buffer + offset, *tokens.get(), tokenIndex, nextFieldIndex - 2, actingVersion);
                break;
            case Signal::ENCODING:
                listener.onEncoding(fieldToken, buffer + offset, typeToken, actingVersion);
                break;
            default:
                throw std::runtime_error("incorrect signal type in decodeFields");
        }

        tokenIndex = nextFieldIndex;
    }

    return tokenIndex;
}

template<typename TokenListener>
size_t decodeData(
    const char *buffer,
    size_t length,
    std::shared_ptr<std::vector<Token>> tokens,
    size_t tokenIndex,
    const size_t numTokens,
    TokenListener& listener)
{
    size_t bufferIndex = 0;

    while (tokenIndex < numTokens)
    {
        Token& token = tokens->at(tokenIndex);
        if (Signal::BEGIN_VAR_DATA != token.signal())
        {
            break;
        }

        Token& lengthToken = tokens->at(tokenIndex + 2);
        // TODO: is length always unsigned according to spec?
        std::uint64_t dataLength = lengthToken.encoding().getAsUInt(buffer + bufferIndex + lengthToken.offset());

        Token& dataToken = tokens->at(tokenIndex + 3);
        bufferIndex += dataToken.offset();

        listener.onVarData(token, buffer + bufferIndex, dataLength, dataToken);

        bufferIndex += dataLength;
        tokenIndex += token.componentTokenCount();
    }

    return tokenIndex;
}

template<typename TokenListener>
std::pair<size_t, size_t> decodeGroups(
    const char *buffer,
    size_t length,
    std::uint64_t actingVersion,
    std::shared_ptr<std::vector<Token>> tokens,
    size_t tokenIndex,
    const size_t numTokens,
    TokenListener& listener)
{
    size_t bufferIndex = 0;

    while (tokenIndex < numTokens)
    {
        Token& token = tokens->at(tokenIndex);
        if (Signal::BEGIN_GROUP != token.signal())
        {
            break;
        }

        Token& blockLengthToken = tokens->at(tokenIndex + 2);
        std::uint64_t blockLength = blockLengthToken.encoding().getAsUInt(buffer + bufferIndex + blockLengthToken.offset());

        Token& numInGroupToken = tokens->at(tokenIndex + 3);
        std::uint64_t numInGroup = numInGroupToken.encoding().getAsUInt(buffer + bufferIndex + numInGroupToken.offset());

        Token& dimensionsTypeComposite = tokens->at(tokenIndex + 1);
        bufferIndex += static_cast<size_t>(dimensionsTypeComposite.encodedLength());

        size_t beginFieldsIndex = tokenIndex + dimensionsTypeComposite.componentTokenCount() + 1;

        listener.onGroupHeader(token, numInGroup);

        for (std::uint64_t i = 0; i < numInGroup; i++)
        {
            listener.onBeginGroup(token, i, numInGroup);

            size_t afterFieldsIndex =
                decodeFields(buffer + bufferIndex, length, actingVersion, tokens, beginFieldsIndex, numTokens, listener);
            bufferIndex += blockLength;

            std::pair<size_t, size_t> groupsResult =
                decodeGroups(buffer + bufferIndex, length, actingVersion, tokens, afterFieldsIndex, numTokens, listener);

            bufferIndex += groupsResult.first;

            listener.onEndGroup(token, i, numInGroup);
        }

        tokenIndex += token.componentTokenCount();
    }

    return std::pair<size_t, size_t>(bufferIndex, tokenIndex);
};

/**
 * Entry point for decoder.
 */
template<typename TokenListener>
size_t decode(
    const char *buffer,
    size_t length,
    std::uint64_t actingVersion,
    size_t blockLength,
    std::shared_ptr<std::vector<Token>> msgTokens,
    TokenListener& listener)
{
    listener.onBeginMessage(msgTokens->at(0));

    size_t numTokens = msgTokens->size();
    const size_t tokenIndex = decodeFields(buffer, length, actingVersion, msgTokens, 1, numTokens, listener);

    size_t bufferIndex = blockLength;

    std::pair<size_t, size_t> groupResult =
        decodeGroups(buffer + bufferIndex, length, actingVersion, msgTokens, tokenIndex, numTokens, listener);

    bufferIndex =
        decodeData(buffer + bufferIndex + groupResult.first, length, msgTokens, groupResult.second, numTokens, listener);

    listener.onEndMessage(msgTokens->at(numTokens - 1));

    return bufferIndex;
}


}}}

#endif
