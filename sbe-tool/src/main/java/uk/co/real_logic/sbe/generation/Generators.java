/*
 * Copyright 2013-2018 Real Logic Ltd.
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
package uk.co.real_logic.sbe.generation;

import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.util.List;
import java.util.function.BiConsumer;

public class Generators
{
    /**
     * For each field found in a list of field {@link Token}s take the field token and following type token to
     * a {@link BiConsumer}.
     *
     * @param tokens   to be iterated over.
     * @param consumer to for the field and encoding token pair.
     */
    public static void forEachField(final List<Token> tokens, final BiConsumer<Token, Token> consumer)
    {
        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token fieldToken = tokens.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token typeToken = tokens.get(i + 1);
                consumer.accept(fieldToken, typeToken);
                i += fieldToken.componentTokenCount();
            }
            else
            {
                ++i;
            }
        }
    }

    /**
     * Uppercase the first character of a given String.
     *
     * @param s to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toUpperFirstChar(final String s)
    {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Lowercase the first character of a given String.
     *
     * @param s to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toLowerFirstChar(final String s)
    {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
