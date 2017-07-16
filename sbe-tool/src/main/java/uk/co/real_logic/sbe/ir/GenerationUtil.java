/*
 * Copyright 2013-2017 Real Logic Ltd.
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
package uk.co.real_logic.sbe.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * Common code generation utility functions to be used by the different language specific backends.
 */
public final class GenerationUtil
{
    private GenerationUtil()
    {
    }

    public static int collectFields(final List<Token> tokens, final int index, final List<Token> fields)
    {
        return collect(Signal.BEGIN_FIELD, tokens, index, fields);
    }

    public static int collectGroups(final List<Token> tokens, final int index, final List<Token> groups)
    {
        return collect(Signal.BEGIN_GROUP, tokens, index, groups);
    }

    public static int collectVarData(final List<Token> tokens, final int index, final List<Token> varData)
    {
        return collect(Signal.BEGIN_VAR_DATA, tokens, index, varData);
    }

    public static MessageComponents collectMessageComponents(final List<Token> tokens)
    {
        final Token msgToken = tokens.get(0);
        final List<Token> messageBody = getMessageBody(tokens);

        int i = 0;
        final List<Token> fields = new ArrayList<>();
        i = collectFields(messageBody, i, fields);

        final List<Token> groups = new ArrayList<>();
        i = collectGroups(messageBody, i, groups);

        final List<Token> varData = new ArrayList<>();
        collectVarData(messageBody, i, varData);
        return new MessageComponents(msgToken, fields, groups, varData);
    }


    public static int collect(
            final Signal signal, final List<Token> tokens, final int index, final List<Token> collected)
    {
        int i = index;
        while (i < tokens.size())
        {
            final Token token = tokens.get(i);
            if (signal != token.signal())
            {
                break;
            }

            final int tokenCount = token.componentTokenCount();
            for (final int limit = i + tokenCount; i < limit; i++)
            {
                collected.add(tokens.get(i));
            }
        }

        return i;
    }

    public static List<Token> getMessageBody(final List<Token> tokens)
    {
        return tokens.subList(1, tokens.size() - 1);
    }

    public static CharSequence concatEncodingTokens(
            final List<Token> tokens, final Function<Token, CharSequence> mapper)
    {
        return concatTokens(tokens, Signal.ENCODING, mapper);
    }

    public static CharSequence concatTokens(
            final List<Token> tokens, final Signal signal, final Function<Token, CharSequence> mapper)
    {
        return tokens
                .stream()
                .filter((token) -> token.signal() == signal)
                .map(mapper)
                .collect(joining());
    }

    public static int findEndSignal(
            final List<Token> tokens, final int startIndex, final Signal signal, final String name)
    {
        int result = tokens.size() - 1;

        for (int i = startIndex, endIndex = tokens.size() - 1; i < endIndex; i++)
        {
            final Token token = tokens.get(i);

            if (signal == token.signal() && name.equals(token.name()))
            {
                result = i;
                break;
            }
        }

        return result;
    }

    public static void eachField(final List<Token> tokens, final BiConsumer<Token, Token> consumer)
    {
        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token fieldToken = tokens.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                consumer.accept(fieldToken, encodingToken);
                i += fieldToken.componentTokenCount();
            }
            else
            {
                ++i;
            }
        }
    }
}
