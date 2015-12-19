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

using namespace sbe::otf;

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
    std::size_t fromIndex,
    std::size_t toIndex,
    std::uint64_t actingVersion)> on_enum_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    std::vector<Token>& tokens,
    std::size_t fromIndex,
    std::size_t toIndex,
    std::uint64_t actingVersion)> on_bit_set_t;

typedef std::function<void(
    Token& fieldToken,
    std::vector<Token>& tokens,
    std::size_t fromIndex,
    std::size_t toIndex)> on_begin_composite_t;

typedef std::function<void(
    Token& fieldToken,
    std::vector<Token>& tokens,
    std::size_t fromIndex,
    std::size_t toIndex)> on_end_composite_t;

typedef std::function<void(
    Token& token,
    std::uint64_t numInGroup)> on_group_header_t;

typedef std::function<void(
    Token& token,
    std::uint64_t groupIndex,
    std::uint64_t numInGroup)> on_begin_group_t;

typedef std::function<void(
    Token& token,
    std::uint64_t groupIndex,
    std::uint64_t numInGroup)> on_end_group_t;

typedef std::function<void(
    Token& fieldToken,
    const char *buffer,
    std::uint64_t length,
    Token& typeToken)> on_var_data_t;

class BasicTokenListener
{
public:
    virtual void onBeginMessage(Token& token) {}

    virtual void onEndMessage(Token& token) {}

    virtual void onEncoding(
        Token& fieldToken,
        const char *buffer,
        Token& typeToken,
        std::uint64_t actingVersion) {}

    virtual void onEnum(
        Token& fieldToken,
        const char *buffer,
        std::vector<Token>& tokens,
        std::size_t fromIndex,
        std::size_t toIndex,
        std::uint64_t actingVersion) {}

    virtual void onBitSet(
        Token& fieldToken,
        const char *buffer,
        std::vector<Token>& tokens,
        std::size_t fromIndex,
        std::size_t toIndex,
        std::uint64_t actingVersion) {}

    virtual void onBeginComposite(
        Token& fieldToken,
        std::vector<Token>& tokens,
        std::size_t fromIndex,
        std::size_t toIndex) {}

    virtual void onEndComposite(
        Token& fieldToken,
        std::vector<Token>& tokens,
        std::size_t fromIndex,
        std::size_t toIndex) {}

    virtual void onGroupHeader(
        Token& token,
        std::uint64_t numInGroup) {}

    virtual void onBeginGroup(
        Token& token,
        std::uint64_t groupIndex,
        std::uint64_t numInGroup) {}

    virtual void onEndGroup(
        Token& token,
        std::uint64_t groupIndex,
        std::uint64_t numInGroup) {}

    virtual void onVarData(
        Token& fieldToken,
        const char *buffer,
        std::uint64_t length,
        Token& typeToken) {}
};

template<typename TokenListener>
static void decodeComposite(
    Token& fieldToken,
    const char *buffer,
    std::size_t bufferIndex,
    std::size_t length,
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
        listener.onEncoding(token, buffer + bufferIndex + token.offset(), token, actingVersion);
    }

    listener.onEndComposite(fieldToken, *tokens.get(), tokenIndex, toIndex);
}

template<typename TokenListener>
static size_t decodeFields(
    const char *buffer,
    std::size_t bufferIndex,
    std::size_t length,
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
        const std::size_t offset = bufferIndex + typeToken.offset();

        switch (typeToken.signal())
        {
            case Signal::BEGIN_COMPOSITE:
                decodeComposite<TokenListener>(
                    fieldToken, buffer, offset, length, tokens, tokenIndex, nextFieldIndex - 2, actingVersion, listener);
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
std::size_t decodeData(
    const char *buffer,
    std::size_t bufferIndex,
    std::size_t length,
    std::shared_ptr<std::vector<Token>> tokens,
    std::size_t tokenIndex,
    const std::size_t numTokens,
    TokenListener& listener)
{
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

    return bufferIndex;
}

template<typename TokenListener>
std::pair<size_t, size_t> decodeGroups(
    const char *buffer,
    std::size_t bufferIndex,
    std::size_t length,
    std::uint64_t actingVersion,
    std::shared_ptr<std::vector<Token>> tokens,
    size_t tokenIndex,
    const size_t numTokens,
    TokenListener& listener)
{
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
                decodeFields(buffer, bufferIndex, length, actingVersion, tokens, beginFieldsIndex, numTokens, listener);
            bufferIndex += blockLength;

            std::pair<size_t, size_t> groupsResult =
                decodeGroups(buffer, bufferIndex, length, actingVersion, tokens, afterFieldsIndex, numTokens, listener);

            bufferIndex = decodeData(buffer, groupsResult.first, length, tokens, groupsResult.second, numTokens, listener);

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
std::size_t decode(
    const char *buffer,
    std::size_t length,
    std::uint64_t actingVersion,
    size_t blockLength,
    std::shared_ptr<std::vector<Token>> msgTokens,
    TokenListener& listener)
{
    listener.onBeginMessage(msgTokens->at(0));

    size_t numTokens = msgTokens->size();
    const size_t tokenIndex = decodeFields(buffer, 0, length, actingVersion, msgTokens, 1, numTokens, listener);

    size_t bufferIndex = blockLength;

    std::pair<size_t, size_t> groupResult =
        decodeGroups(buffer, bufferIndex, length, actingVersion, msgTokens, tokenIndex, numTokens, listener);

    bufferIndex =
        decodeData(buffer, groupResult.first, length, msgTokens, groupResult.second, numTokens, listener);

    listener.onEndMessage(msgTokens->at(numTokens - 1));

    return bufferIndex;
}


}}}

#endif
