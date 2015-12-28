/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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
package uk.co.real_logic.sbe.otf;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.sbe.ir.Token;

import java.util.List;

import static uk.co.real_logic.sbe.ir.Signal.BEGIN_FIELD;
import static uk.co.real_logic.sbe.ir.Signal.BEGIN_GROUP;
import static uk.co.real_logic.sbe.ir.Signal.BEGIN_VAR_DATA;

/**
 * On-the-fly decoder that dynamically decodes messages based on the IR for a schema.
 *
 * The contents of the messages are structurally decomposed and passed to a {@link TokenListener} for decoding the
 * primitive values.
 *
 * The design keeps all state on the stack to maximise performance and avoid object allocation. The message decoder can
 * be reused repeatably by calling {@link OtfMessageDecoder#decode(DirectBuffer, int, int, int, List, TokenListener)}
 * which is thread safe to be used across multiple threads.
 */
public class OtfMessageDecoder
{
    /**
     * Decode a message from the provided buffer based on the message schema described with IR {@link Token}s.
     *
     * @param buffer        containing the encoded message.
     * @param bufferIdx     at which the message encoding starts in the buffer.
     * @param actingVersion of the encoded message for dealing with extension fields.
     * @param blockLength   of the root message fields.
     * @param msgTokens     in IR format describing the message structure.
     * @param listener      to callback for decoding the primitive values as discovered in the structure.
     * @return the index in the underlying buffer after decoding.
     */
    public static int decode(
        final DirectBuffer buffer,
        int bufferIdx,
        final int actingVersion,
        final int blockLength,
        final List<Token> msgTokens,
        final TokenListener listener)
    {
        listener.onBeginMessage(msgTokens.get(0));

        final int numTokens = msgTokens.size();
        final int tokenIdx = decodeFields(buffer, bufferIdx, actingVersion, msgTokens, 1, numTokens, listener);
        bufferIdx += blockLength;

        final long packedValues = decodeGroups(buffer, bufferIdx, actingVersion, msgTokens, tokenIdx, numTokens, listener);

        bufferIdx = decodeData(buffer, bufferIndex(packedValues), msgTokens, tokenIndex(packedValues), numTokens, listener);

        listener.onEndMessage(msgTokens.get(numTokens - 1));

        return bufferIdx;
    }

    private static int decodeFields(
        final DirectBuffer buffer,
        final int bufferIdx,
        final int actingVersion,
        final List<Token> tokens,
        int tokenIdx,
        final int numTokens,
        final TokenListener listener)
    {
        while (tokenIdx < numTokens)
        {
            final Token fieldToken = tokens.get(tokenIdx);
            if (BEGIN_FIELD != fieldToken.signal())
            {
                break;
            }

            final int nextFieldIdx = tokenIdx + fieldToken.componentTokenCount();
            tokenIdx++;

            final Token typeToken = tokens.get(tokenIdx);
            final int offset = typeToken.offset();

            switch (typeToken.signal())
            {
                case BEGIN_COMPOSITE:
                    decodeComposite(
                        fieldToken, buffer, bufferIdx + offset, tokens, tokenIdx, nextFieldIdx - 2, actingVersion, listener);
                    break;

                case BEGIN_ENUM:
                    listener.onEnum(
                        fieldToken, buffer, bufferIdx + offset, tokens, tokenIdx, nextFieldIdx - 2, actingVersion);
                    break;

                case BEGIN_SET:
                    listener.onBitSet(
                        fieldToken, buffer, bufferIdx + offset, tokens, tokenIdx, nextFieldIdx - 2, actingVersion);
                    break;

                case ENCODING:
                    listener.onEncoding(fieldToken, buffer, bufferIdx + offset, typeToken, actingVersion);
                    break;
            }

            tokenIdx = nextFieldIdx;
        }

        return tokenIdx;
    }

    private static long decodeGroups(
        final DirectBuffer buffer,
        int bufferIdx,
        final int actingVersion,
        final List<Token> tokens,
        int tokenIdx,
        final int numTokens,
        final TokenListener listener)
    {
        while (tokenIdx < numTokens)
        {
            final Token token = tokens.get(tokenIdx);
            if (BEGIN_GROUP != token.signal())
            {
                break;
            }

            final Token blockLengthToken = tokens.get(tokenIdx + 2);
            final int blockLength = Types.getInt(
                buffer,
                bufferIdx + blockLengthToken.offset(),
                blockLengthToken.encoding().primitiveType(),
                blockLengthToken.encoding().byteOrder());

            final Token numInGroupToken = tokens.get(tokenIdx + 3);
            final int numInGroup = Types.getInt(
                buffer,
                bufferIdx + numInGroupToken.offset(),
                numInGroupToken.encoding().primitiveType(),
                numInGroupToken.encoding().byteOrder());

            final Token dimensionTypeComposite = tokens.get(tokenIdx + 1);
            bufferIdx += dimensionTypeComposite.encodedLength();

            final int beginFieldsIdx = tokenIdx + dimensionTypeComposite.componentTokenCount() + 1;

            listener.onGroupHeader(token, numInGroup);

            for (int i = 0; i < numInGroup; i++)
            {
                listener.onBeginGroup(token, i, numInGroup);

                final int afterFieldsIdx = decodeFields(
                    buffer, bufferIdx, actingVersion, tokens, beginFieldsIdx, numTokens, listener);
                bufferIdx += blockLength;

                final long packedValues = decodeGroups(
                    buffer, bufferIdx, actingVersion, tokens, afterFieldsIdx, numTokens, listener);

                bufferIdx = decodeData(
                    buffer, bufferIndex(packedValues), tokens, tokenIndex(packedValues), numTokens, listener);

                listener.onEndGroup(token, i, numInGroup);
            }

            tokenIdx += token.componentTokenCount();
        }

        return pack(bufferIdx, tokenIdx);
    }

    private static void decodeComposite(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIdx,
        final List<Token> tokens,
        final int tokenIdx,
        final int toIndex,
        final int actingVersion,
        final TokenListener listener)
    {
        listener.onBeginComposite(fieldToken, tokens, tokenIdx, toIndex);

        for (int i = tokenIdx + 1; i < toIndex;)
        {
            final Token typeToken = tokens.get(i);
            final int nextFieldIdx = i + typeToken.componentTokenCount();

            final int offset = typeToken.offset();

            switch (typeToken.signal())
            {
                case BEGIN_COMPOSITE:
                    decodeComposite(
                        fieldToken, buffer, bufferIdx + offset, tokens, i, nextFieldIdx - 1, actingVersion, listener);
                    break;

                case BEGIN_ENUM:
                    listener.onEnum(
                        fieldToken, buffer, bufferIdx + offset, tokens, i, nextFieldIdx - 1, actingVersion);
                    break;

                case BEGIN_SET:
                    listener.onBitSet(
                        fieldToken, buffer, bufferIdx + offset, tokens, i, nextFieldIdx - 1, actingVersion);
                    break;

                case ENCODING:
                    listener.onEncoding(typeToken, buffer, bufferIdx + offset, typeToken, actingVersion);
                    break;
            }

            i += typeToken.componentTokenCount();
        }

        listener.onEndComposite(fieldToken, tokens, tokenIdx, toIndex);
    }

    private static int decodeData(
        final DirectBuffer buffer,
        int bufferIdx,
        final List<Token> tokens,
        int tokenIdx,
        final int numTokens,
        final TokenListener listener)
    {
        while (tokenIdx < numTokens)
        {
            final Token token = tokens.get(tokenIdx);
            if (BEGIN_VAR_DATA != token.signal())
            {
                break;
            }

            final Token lengthToken = tokens.get(tokenIdx + 2);
            final int length = Types.getInt(
                buffer,
                bufferIdx + lengthToken.offset(),
                lengthToken.encoding().primitiveType(),
                lengthToken.encoding().byteOrder());

            final Token dataToken = tokens.get(tokenIdx + 3);
            bufferIdx += dataToken.offset();

            listener.onVarData(token, buffer, bufferIdx, length, dataToken);

            bufferIdx += length;
            tokenIdx += token.componentTokenCount();
        }

        return bufferIdx;
    }

    private static long pack(final int bufferIndex, final int tokenIndex)
    {
        return ((long)bufferIndex << 32) | tokenIndex;
    }

    private static int bufferIndex(final long packedValues)
    {
        return (int)(packedValues >>> 32);
    }

    private static int tokenIndex(final long packedValues)
    {
        return (int)packedValues;
    }
}
