package uk.co.real_logic.sbe.ir;

import java.util.Collections;
import java.util.List;

public class MessageComponents
{
    public final Token messageToken;
    public final List<Token> fields;
    public final List<Token> groups;
    public final List<Token> varData;

    public MessageComponents(
            final Token messageToken, final List<Token> fields, final List<Token> groups, final List<Token> varData)
    {
        this.messageToken = messageToken;
        this.fields = Collections.unmodifiableList(fields);
        this.groups = Collections.unmodifiableList(groups);
        this.varData = Collections.unmodifiableList(varData);
    }
}
