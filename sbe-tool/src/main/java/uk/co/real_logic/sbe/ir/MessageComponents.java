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
package uk.co.real_logic.sbe.ir;

import java.util.ArrayList;
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

    public static MessageComponents collectMessageComponents(final List<Token> tokens)
    {
        final Token msgToken = tokens.get(0);
        final List<Token> messageBody = GenerationUtil.getMessageBody(tokens);

        int i = 0;
        final List<Token> fields = new ArrayList<>();
        i = GenerationUtil.collectFields(messageBody, i, fields);

        final List<Token> groups = new ArrayList<>();
        i = GenerationUtil.collectGroups(messageBody, i, groups);

        final List<Token> varData = new ArrayList<>();
        GenerationUtil.collectVarData(messageBody, i, varData);

        return new MessageComponents(msgToken, fields, groups, varData);
    }
}
