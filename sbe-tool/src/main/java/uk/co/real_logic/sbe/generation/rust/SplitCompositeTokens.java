package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.ir.Token;

import java.util.ArrayList;
import java.util.List;

final class SplitCompositeTokens
{
    final List<Token> constantEncodingTokens;
    final List<NamedToken> nonConstantEncodingTokens;

    private SplitCompositeTokens(final List<Token> constantEncodingTokens, final List<NamedToken>
        nonConstantEncodingTokens)
    {
        this.constantEncodingTokens = constantEncodingTokens;
        this.nonConstantEncodingTokens = nonConstantEncodingTokens;
    }

    static SplitCompositeTokens splitInnerTokens(final List<Token> tokens)
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
