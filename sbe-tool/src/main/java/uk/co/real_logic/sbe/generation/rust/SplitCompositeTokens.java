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
package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.generation.NamedToken;
import uk.co.real_logic.sbe.ir.Token;

import java.util.ArrayList;
import java.util.List;

final class SplitCompositeTokens
{
    private final List<Token> constantEncodingTokens;
    private final List<NamedToken> nonConstantEncodingTokens;

    private SplitCompositeTokens(
        final List<Token> constantEncodingTokens, final List<NamedToken> nonConstantEncodingTokens)
    {
        this.constantEncodingTokens = constantEncodingTokens;
        this.nonConstantEncodingTokens = nonConstantEncodingTokens;
    }

    public List<Token> constantEncodingTokens()
    {
        return constantEncodingTokens;
    }

    public List<NamedToken> nonConstantEncodingTokens()
    {
        return nonConstantEncodingTokens;
    }

    public static SplitCompositeTokens splitInnerTokens(final List<Token> tokens)
    {
        final List<Token> constantTokens = new ArrayList<>();
        final List<NamedToken> namedNonConstantTokens = new ArrayList<>();

        for (int i = 1, end = tokens.size() - 1; i < end; )
        {
            final Token encodingToken = tokens.get(i);
            if (encodingToken.isConstantEncoding())
            {
                constantTokens.add(encodingToken);
            }
            else
            {
                namedNonConstantTokens.add(new NamedToken(encodingToken.name(), encodingToken));
            }

            i += encodingToken.componentTokenCount();
        }

        return new SplitCompositeTokens(constantTokens, namedNonConstantTokens);
    }
}
