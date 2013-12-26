/*
 * Copyright 2012 Real Logic Ltd.
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
 * Used to decode the group size header.
 */
public class OtfGroupSizeDecoder
{
    public static final String GROUP_SIZE_ENCODING_NAME = "groupSizeEncoding";
    public static final String BLOCK_LENGTH_NAME = "blockLength";
    public static final String NUM_IN_GROUP_NAME = "numInGroup";

    private final int size;
    private final int groupHeaderTokenCount;
    private int blockLengthOffset = -1;
    private int numInGroupOffset = -1;
    private PrimitiveType blockLengthType;
    private PrimitiveType numInGroupType;
    private ByteOrder blockLengthByteOrder;
    private ByteOrder numInGroupByteOrder;

    /**
     * Scan the tokens and cache meta data to make decoding more efficient.
     *
     * @param tokens describing the group header.
     */
    public OtfGroupSizeDecoder(final List<Token> tokens)
    {
        final Token compositeToken = tokens.get(0);
        if (!GROUP_SIZE_ENCODING_NAME.equals(compositeToken.name()) ||
            Signal.BEGIN_COMPOSITE != compositeToken.signal())
        {
            throw new IllegalArgumentException("Invalid Group: " + compositeToken);
        }

        size = compositeToken.size();
        groupHeaderTokenCount = tokens.size();

        for (final Token token : tokens)
        {
            switch (token.name())
            {
                case BLOCK_LENGTH_NAME:
                    blockLengthOffset = token.offset();
                    blockLengthType = token.encoding().primitiveType();
                    blockLengthByteOrder = token.encoding().byteOrder();
                    break;

                case NUM_IN_GROUP_NAME:
                    numInGroupOffset = token.offset();
                    numInGroupType = token.encoding().primitiveType();
                    numInGroupByteOrder = token.encoding().byteOrder();
                    break;
            }
        }

        if (-1 == blockLengthOffset)
        {
            throw new IllegalStateException(BLOCK_LENGTH_NAME + " is missing");
        }

        if (-1 == numInGroupOffset)
        {
            throw new IllegalStateException(NUM_IN_GROUP_NAME + " is missing");
        }
    }

    /**
     * Get the block length that is used for each iteration of the group fields.
     *
     * @param buffer to be read.
     * @param bufferIndex at which he group header begins.
     * @return the blockLength for an iteration of the group fields.
     */
    public int getBlockLength(final DirectBuffer buffer, final int bufferIndex)
    {
        return Util.getInt(buffer, bufferIndex + blockLengthOffset, blockLengthType, blockLengthByteOrder);
    }

    /**
     * Get the number of times the group fields will be repeated.
     *
     * @param buffer to be read.
     * @param bufferIndex at which he group header begins.
     * @return the number of times the group fields will be repeated.
     */
    public int getNumInGroup(final DirectBuffer buffer, final int bufferIndex)
    {
        return Util.getInt(buffer, bufferIndex + numInGroupOffset, numInGroupType, numInGroupByteOrder);
    }

    /**
     * Get the size of the group header in bytes.
     *
     * @return the size of the group header in bytes.
     */
    public int size()
    {
        return size;
    }

    /**
     * The number of tokens in the group header.
     *
     * @return the number of tokens in the group header.
     */
    public int groupHeaderTokenCount()
    {
        return groupHeaderTokenCount;
    }
}
