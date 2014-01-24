/*
 * Copyright 2013 Real Logic Ltd.
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

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.util.Verify;

import java.util.List;

/**
 * Metadata description for a message headerStructure
 */
public class HeaderStructure
{
    public static final String TEMPLATE_ID = "templateId";
    public static final String TEMPLATE_VERSION = "version";
    public static final String BLOCK_LENGTH = "blockLength";

    private final List<Token> tokens;
    private PrimitiveType templateIdType;
    private PrimitiveType templateVersionType;
    private PrimitiveType blockLengthType;

    public HeaderStructure(final List<Token> tokens)
    {
        Verify.notNull(tokens, "tokens");
        this.tokens = tokens;

        captureEncodings(tokens);

        Verify.notNull(templateIdType, "templateIdType");
        Verify.notNull(templateVersionType, "templateVersionType");
        Verify.notNull(blockLengthType, "blockLengthType");
    }

    private void captureEncodings(final List<Token> tokens)
    {
        for (final Token token : tokens)
        {
            switch (token.name())
            {
                case TEMPLATE_ID:
                    templateIdType = token.encoding().primitiveType();
                    break;

                case TEMPLATE_VERSION:
                    templateVersionType = token.encoding().primitiveType();
                    break;

                case BLOCK_LENGTH:
                    blockLengthType = token.encoding().primitiveType();
                    break;
            }
        }
    }

    public List<Token> tokens()
    {
        return tokens;
    }

    public PrimitiveType templateIdType()
    {
        return templateIdType;
    }

    public PrimitiveType templateVersionType()
    {
        return templateVersionType;
    }

    public PrimitiveType blockLengthType()
    {
        return blockLengthType;
    }
}
