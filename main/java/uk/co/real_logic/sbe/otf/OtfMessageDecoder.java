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
import uk.co.real_logic.sbe.util.Verify;

import java.util.List;

public class OtfMessageDecoder
{
    private final OtfGroupSizeDecoder groupSizeDecoder;

    public OtfMessageDecoder(final OtfGroupSizeDecoder groupSizeDecoder)
    {
        Verify.notNull(groupSizeDecoder, "groupSizeDecoder");

        this.groupSizeDecoder = groupSizeDecoder;
    }

    public int decode(final DirectBuffer buffer,
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

    private static void decodeFields(final DirectBuffer buffer,
                                     final int bufferIndex,
                                     final int actingVersion,
                                     final List<Token> tokens,
                                     final int fromIndex,
                                     final int toIndex,
                                     final TokenListener listener)
    {
        for (int i = fromIndex; i < toIndex; i++)
        {
            if (tokens.get(i).signal() == Signal.BEGIN_FIELD)
            {
                i = decodeField(buffer, bufferIndex, tokens, i, actingVersion, listener);
            }
        }
    }

    private int decodeGroups(final DirectBuffer buffer,
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
                final int beginFieldsIndex = i + groupSizeDecoder.groupHeaderTokenCount() + 1;
                final int endGroupIndex = findNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.END_GROUP);
                final int nextGroupIndex = findNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.BEGIN_GROUP);
                final int endOfFieldsIndex = Math.min(endGroupIndex, nextGroupIndex) - 1;

                final int blockLength = groupSizeDecoder.getBlockLength(buffer, bufferIndex);
                final int numInGroup = groupSizeDecoder.getNumInGroup(buffer, bufferIndex);

                bufferIndex += groupSizeDecoder.size();

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

    private int decodeVarData(final DirectBuffer buffer,
                              int bufferIndex,
                              final List<Token> tokens,
                              final int fromIndex,
                              final int toIndex,
                              final TokenListener listener)
    {
        return bufferIndex;
    }

    private static int decodeField(final DirectBuffer buffer,
                                   final int bufferIndex,
                                   final List<Token> tokens,
                                   final int fromIndex,
                                   final int actingVersion,
                                   final TokenListener listener)
    {
        final int toIndex = findNextOrLimit(tokens, fromIndex + 1, tokens.size(), Signal.END_FIELD);
        final Token fieldToken = tokens.get(fromIndex);
        final Token typeToken = tokens.get(fromIndex + 1);

        switch (typeToken.signal())
        {
            case ENCODING:
                listener.onEncoding(fieldToken, buffer, bufferIndex + typeToken.offset(), typeToken, actingVersion);
                break;

            case BEGIN_ENUM:
                listener.onEnum(fieldToken, buffer, bufferIndex + typeToken.offset(), tokens, fromIndex + 1, toIndex - 1, actingVersion, listener);
                break;

            case BEGIN_SET:
                listener.onBitSet(fieldToken, buffer, bufferIndex + typeToken.offset(), tokens, fromIndex + 1, toIndex - 1, actingVersion, listener);
                break;

            case BEGIN_COMPOSITE:
                decodeComposite(fieldToken, buffer, bufferIndex + typeToken.offset(), tokens, fromIndex + 1, toIndex - 1, actingVersion, listener);
                break;
        }

        return toIndex;
    }

    private static void decodeComposite(final Token fieldToken,
                                        final DirectBuffer buffer, final int bufferIndex,
                                        final List<Token> tokens, final int fromIndex, final int toIndex,
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
