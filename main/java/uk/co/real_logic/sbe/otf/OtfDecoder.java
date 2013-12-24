/*
 * Copyright 2012 Real Logic Ltd.
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

public class OtfDecoder
{
    private int bufferIndex = 0;

    public int decode(final DirectBuffer buffer,
                      final int bufferIndex,
                      final int actingVersion,
                      final int blockLength,
                      final List<Token> msgTokens,
                      final TokenListener listener)
    {
        this.bufferIndex = bufferIndex;

        listener.onBeginMessage(msgTokens.get(0));

        final int beginGroups = findNextOrEnd(msgTokens, 0, Signal.BEGIN_GROUP);
        final int beginVarData = findNextOrEnd(msgTokens, beginGroups, Signal.BEGIN_VAR_DATA);

        decodeFields(buffer, bufferIndex, actingVersion, msgTokens, 0, beginGroups, listener);
        this.bufferIndex += blockLength;

        decodeGroups(buffer, actingVersion, msgTokens, beginGroups, beginVarData, listener);
        decodeVarData(buffer, msgTokens, beginVarData, msgTokens.size(), listener);

        listener.onEndMessage(msgTokens.get(msgTokens.size() - 1));

        return this.bufferIndex;
    }

    private void decodeFields(final DirectBuffer buffer,
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

    private void decodeGroups(final DirectBuffer buffer,
                              final int actingVersion,
                              final List<Token> tokens,
                              final int fromIndex,
                              final int toIndex,
                              final TokenListener listener)
    {

    }

    private void decodeVarData(final DirectBuffer buffer,
                               final List<Token> tokens,
                               final int fromIndex,
                               final int toIndex,
                               final TokenListener listener)
    {

    }

    private static  int decodeField(final DirectBuffer buffer,
                                    final int bufferIndex,
                                    final List<Token> tokens,
                                    final int fromIndex,
                                    final int actingVersion,
                                    final TokenListener listener)
    {
        final int toIndex = findNextOrEnd(tokens, fromIndex + 1, Signal.END_FIELD);
        final Token fieldToken = tokens.get(fromIndex);
        final Token typeToken = tokens.get(fromIndex + 1);
        final String fieldName = fieldToken.name();

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

    private static int findNextOrEnd(final List<Token> tokens, final int fromIndex, final Signal signal)
    {
        int i = fromIndex;
        for (final int limit = tokens.size(); i < limit; i++)
        {
            if (tokens.get(i).signal() == signal)
            {
                break;
            }
        }

        return i;
    }
}
