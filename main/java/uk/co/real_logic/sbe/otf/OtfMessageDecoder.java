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
package uk.co.real_logic.sbe.otf;

import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.util.List;

import static uk.co.real_logic.sbe.otf.Util.getInt;

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
    private static final int GROUP_DIM_TYPE_TOKENS = 5;
    private static final int VAR_DATA_TOKENS = 5;

    /**
     * Decode a message from the provided buffer based on the message schema described with IR {@link Token}s.
     *
     * @param buffer        containing the encoded message.
     * @param bufferIndex   at which the message encoding starts in the buffer.
     * @param actingVersion of the encoded message for dealing with extension fields.
     * @param blockLength   of the root message fields.
     * @param msgTokens     in IR format describing the message structure.
     * @param listener      to callback for decoding the primitive values as discovered in the structure.
     * @return the index in the underlying buffer after decoding.
     */
    public static int decode(
        final DirectBuffer buffer,
        int bufferIndex,
        final int actingVersion,
        final int blockLength,
        final List<Token> msgTokens,
        final TokenListener listener)
    {
        final int groupsBeginIndex = findNextOrLimit(msgTokens, 1, msgTokens.size(), Signal.BEGIN_GROUP);
        final int varDataSearchStart = groupsBeginIndex != msgTokens.size() ? groupsBeginIndex : 1;
        final int varDataBeginIndex = findNextOrLimit(msgTokens, varDataSearchStart, msgTokens.size(), Signal.BEGIN_VAR_DATA);

        listener.onBeginMessage(msgTokens.get(0));

        decodeFields(buffer, bufferIndex, actingVersion, msgTokens, 0, groupsBeginIndex, listener);
        bufferIndex += blockLength;

        bufferIndex = decodeGroups(buffer, bufferIndex, actingVersion, msgTokens, groupsBeginIndex, varDataBeginIndex, listener);

        bufferIndex = decodeVarData(buffer, bufferIndex, msgTokens, varDataBeginIndex, msgTokens.size(), listener);

        listener.onEndMessage(msgTokens.get(msgTokens.size() - 1));

        return bufferIndex;
    }

    private static void decodeFields(
        final DirectBuffer buffer,
        final int bufferIndex,
        final int actingVersion,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final TokenListener listener)
    {
        for (int i = fromIndex; i < toIndex; i++)
        {
            if (Signal.BEGIN_FIELD == tokens.get(i).signal())
            {
                i = decodeField(buffer, bufferIndex, tokens, i, actingVersion, listener);
            }
        }
    }

    private static int decodeGroups(
        final DirectBuffer buffer,
        int bufferIndex,
        final int actingVersion,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final TokenListener listener)
    {
        for (int i = fromIndex; i < toIndex; i++)
        {
            final Token token = tokens.get(i);

            if (Signal.BEGIN_GROUP == token.signal())
            {
                final Token blockLengthToken = tokens.get(i + 2);
                final int blockLength = getInt(
                    buffer, bufferIndex + blockLengthToken.offset(),
                    blockLengthToken.encoding().primitiveType(),
                    blockLengthToken.encoding().byteOrder());

                final Token numInGroupToken = tokens.get(i + 3);
                final int numInGroup = getInt(
                    buffer, bufferIndex + numInGroupToken.offset(),
                    numInGroupToken.encoding().primitiveType(),
                    numInGroupToken.encoding().byteOrder());

                final Token dimensionTypeComposite = tokens.get(i + 1);
                bufferIndex += dimensionTypeComposite.size();

                final int beginFieldsIndex = i + GROUP_DIM_TYPE_TOKENS;
                final int endGroupIndex = findNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.END_GROUP);
                final int nextGroupIndex = findNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.BEGIN_GROUP);
                final int endOfFieldsIndex = Math.min(endGroupIndex, nextGroupIndex) - 1;

                listener.onGroupHeader(token, numInGroup);

                for (int g = 0; g < numInGroup; g++)
                {
                    listener.onBeginGroup(token, g, numInGroup);

                    decodeFields(buffer, bufferIndex, actingVersion, tokens, beginFieldsIndex, endOfFieldsIndex, listener);
                    bufferIndex += blockLength;

                    if (nextGroupIndex < endGroupIndex)
                    {
                        bufferIndex = decodeGroups(buffer, bufferIndex, actingVersion, tokens, nextGroupIndex, toIndex, listener);
                    }

                    listener.onEndGroup(token, g, numInGroup);
                }

                i = endGroupIndex;
            }
        }

        return bufferIndex;
    }

    private static int decodeVarData(
        final DirectBuffer buffer,
        int bufferIndex,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final TokenListener listener)
    {
        for (int i = fromIndex; i < toIndex; i++)
        {
            final Token token = tokens.get(i);

            if (Signal.BEGIN_VAR_DATA == token.signal())
            {
                final Token lengthToken = tokens.get(i + 2);
                final int length = getInt(buffer, bufferIndex + lengthToken.offset(),
                    lengthToken.encoding().primitiveType(), lengthToken.encoding().byteOrder());

                final Token varDataToken = tokens.get(i + 3);
                bufferIndex += varDataToken.offset();

                listener.onVarData(token, buffer, bufferIndex, length, varDataToken);

                bufferIndex += length;
                i += VAR_DATA_TOKENS;
            }
        }

        return bufferIndex;
    }

    private static int decodeField(
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int fromIndex,
        final int actingVersion,
        final TokenListener listener)
    {
        final int toIndex = findNextOrLimit(tokens, fromIndex + 1, tokens.size(), Signal.END_FIELD);
        final Token fieldToken = tokens.get(fromIndex);
        final Token typeToken = tokens.get(fromIndex + 1);
        final int offset = typeToken.offset();

        switch (typeToken.signal())
        {
            case BEGIN_COMPOSITE:
                decodeComposite(
                    fieldToken, buffer, bufferIndex + offset, tokens, fromIndex + 1, toIndex - 1, actingVersion, listener);
                break;

            case BEGIN_ENUM:
                listener.onEnum(fieldToken, buffer, bufferIndex + offset, tokens, fromIndex + 1, toIndex - 1, actingVersion);
                break;

            case BEGIN_SET:
                listener.onBitSet(fieldToken, buffer, bufferIndex + offset, tokens, fromIndex + 1, toIndex - 1, actingVersion);
                break;

            case ENCODING:
                listener.onEncoding(fieldToken, buffer, bufferIndex + offset, typeToken, actingVersion);
                break;
        }

        return toIndex;
    }

    private static void decodeComposite(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final int actingVersion,
        final TokenListener listener)
    {
        listener.onBeginComposite(fieldToken, tokens, fromIndex, toIndex);

        for (int i = fromIndex + 1; i < toIndex; i++)
        {
            final Token token = tokens.get(i);
            listener.onEncoding(token, buffer, bufferIndex + token.offset(), token, actingVersion);
        }

        listener.onEndComposite(fieldToken, tokens, fromIndex, toIndex);
    }

    private static int findNextOrLimit(final List<Token> tokens, final int fromIndex, final int limitIndex, final Signal signal)
    {
        int i = fromIndex;
        for (; i < limitIndex; i++)
        {
            if (tokens.get(i).signal() == signal)
            {
                break;
            }
        }

        return i;
    }
}
