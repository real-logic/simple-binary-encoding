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
package uk.co.real_logic.sbe.otf;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;
import java.util.List;

/**
 * Used to decode the var data field.
 */
public class OtfVarDataDecoder
{
    public static final String VAR_DATA_ENCODING_NAME = "varDataEncoding";
    public static final String LENGTH_NAME = "length";
    public static final String VAR_DATA_NAME = "varData";

    private int size;
    private final int tokenCount;
    private int lengthOffset = -1;
    private int varDataTokenOffset = -1;
    private PrimitiveType lengthType;
    private ByteOrder lengthByteOrder;

    /**
     * Scan the tokens and cache meta data to make decoding more efficient.
     *
     * @param tokens describing the var data header.
     */
    public OtfVarDataDecoder(final List<Token> tokens)
    {
        final Token varDataToken = tokens.get(0);
        if (!VAR_DATA_ENCODING_NAME.equals(varDataToken.name()) ||
            Signal.BEGIN_COMPOSITE != varDataToken.signal())
        {
            throw new IllegalArgumentException("Invalid Var Data: " + varDataToken);
        }

        tokenCount = tokens.size();

        for (int i = 0, limit = tokens.size(); i < limit; i++)
        {
            final Token token = tokens.get(i);

            switch (token.name())
            {
                case LENGTH_NAME:
                    lengthOffset = token.offset();
                    lengthType = token.encoding().primitiveType();
                    lengthByteOrder = token.encoding().byteOrder();
                    size = token.encoding().primitiveType().size();
                    break;

                case VAR_DATA_NAME:
                    varDataTokenOffset = i;
                    break;
            }
        }

        if (-1 == lengthOffset)
        {
            throw new IllegalStateException(LENGTH_NAME + " is missing");
        }

        if (-1 == varDataTokenOffset)
        {
            throw new IllegalStateException(VAR_DATA_NAME + " is missing");
        }
    }

    /**
     * Get the value of the length field from the underlying buffer.
     *
     * @param buffer to read from.
     * @param bufferIndex at which field starts.
     * @return the integer value of the length field.
     */
    public int getLength(final DirectBuffer buffer, final int bufferIndex)
    {
        return Util.getInt(buffer, bufferIndex + lengthOffset, lengthType, lengthByteOrder);
    }

    /**
     * Get the offset at which the var data encoding starts.
     *
     * @return the offset at which the var data encoding starts.
     */
    public int varDataTokenOffset()
    {
        return varDataTokenOffset;
    }

    /**
     * Count of tokens in the composite.
     *
     * @return count of tokens in the composite.
     */
    public int tokenCount()
    {
        return tokenCount;
    }

    /**
     * Size of the var data header before the actual data starts.
     *
     * @return size of the var data header before the actual data starts.
     */
    public int size()
    {
        return size;
    }
}
