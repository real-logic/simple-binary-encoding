/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

/**
 * Common code generation utility functions to be used by the different language specific backends.
 */
public final class GenerationUtil
{
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

    public static List<String> findSubGroupNames(final List<Token> tokens)
    {
        final ArrayList<String> groupNames = new ArrayList<>();
        int level = 0;

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.BEGIN_GROUP)
            {
                if (level++ == 0)
                {
                    groupNames.add(token.name());
                }
            }

            if (token.signal() == Signal.END_GROUP)
            {
                level--;
            }
        }

        return groupNames;
    }

    public static int findSignal(final List<Token> tokens, final Signal signal)
    {
        for (int i = 0, endIndex = tokens.size() - 1; i < endIndex; i++)
        {
            if (signal == tokens.get(i).signal())
            {
                return i;
            }
        }

        return -1;
    }
}
