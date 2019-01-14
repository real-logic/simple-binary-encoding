/*
 * Copyright 2013-2019 Real Logic Ltd.
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

import uk.co.real_logic.sbe.ir.Token;

import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.Generators.forEachField;

public class NamedToken
{
    private final String name;
    private final Token typeToken;

    public NamedToken(final String name, final Token typeToken)
    {
        this.name = name;
        this.typeToken = typeToken;
    }

    public String name()
    {
        return name;
    }

    public Token typeToken()
    {
        return typeToken;
    }

    public static List<NamedToken> gatherNamedNonConstantFieldTokens(final List<Token> fields)
    {
        final List<NamedToken> namedTokens = new ArrayList<>();
        forEachField(fields, (f, t) ->
        {
            if (!f.isConstantEncoding())
            {
                namedTokens.add(new NamedToken(f.name(), t));
            }
        });

        return namedTokens;
    }
}
