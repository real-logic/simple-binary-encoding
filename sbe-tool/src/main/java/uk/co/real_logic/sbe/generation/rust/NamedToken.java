package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.ir.Token;

import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.ir.GenerationUtil.eachField;

final class NamedToken
{
    final String name;
    final Token typeToken;

    NamedToken(final String name, final Token typeToken)
    {
        this.name = name;
        this.typeToken = typeToken;
    }

    static List<NamedToken> gatherNamedFieldTokens(final List<Token> fields)
    {
        final List<NamedToken> namedTokens = new ArrayList<>();
        eachField(fields, (f, t) -> namedTokens.add(new NamedToken(f.name(), t)));
        return namedTokens;
    }
}
