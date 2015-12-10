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
package uk.co.real_logic.sbe.ir;

import java.util.List;
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

    public static int collectRootFields(final List<Token> tokens, int index, final List<Token> rootFields)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            final Token token = tokens.get(index);
            if (Signal.BEGIN_GROUP == token.signal() ||
                Signal.END_GROUP == token.signal() ||
                Signal.BEGIN_VAR_DATA == token.signal())
            {
                return index;
            }

            rootFields.add(token);
        }

        return index;
    }

    public static int collectGroups(final List<Token> tokens, final int index, final List<Token> groups)
    {
        int groupStart = -1;
        int groupEnd = -1;
        for (int i = index, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (Signal.BEGIN_GROUP == token.signal() && -1 == groupStart)
            {
                groupStart = i;
            }

            if (Signal.END_GROUP == token.signal())
            {
                groupEnd = i;
            }
        }

        if (groupStart > -1)
        {
            for (int i = groupStart; i <= groupEnd; i++)
            {
                groups.add(tokens.get(i));
            }
        }

        return groupEnd > -1 ? groupEnd + 1 : index;
    }

    public static int collectDataFields(final List<Token> tokens, final int index, final List<Token> dataFields)
    {
        int dataEnd = index;
        while (dataEnd < tokens.size())
        {
            if (Signal.BEGIN_VAR_DATA != tokens.get(dataEnd).signal())
            {
                break;
            }

            for (int p = 0; p < 6; p++)
            {
                dataFields.add(tokens.get(dataEnd + p));
            }

            dataEnd += 6;
        }

        return dataEnd;
    }

    public static List<Token> getMessageBody(final List<Token> tokens)
    {
        return tokens.subList(1, tokens.size() - 1);
    }

    public static CharSequence concatEncodingTokens(final List<Token> tokens, final Function<Token, CharSequence> mapper)
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
}
