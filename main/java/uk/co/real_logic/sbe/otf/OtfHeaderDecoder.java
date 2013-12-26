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
import uk.co.real_logic.sbe.ir.HeaderStructure;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;

/**
 * Used to decode a message header while doing on-the-fly decoding of a message stream.
 * <p/>
 * Meta data is cached to improve the performance of decoding headers.
 */
public class OtfHeaderDecoder
{
    private final int size;
    private int templateIdOffset;
    private int templateVersionOffset;
    private int blockLengthOffset;
    private PrimitiveType templateIdType;
    private PrimitiveType templateVersionType;
    private PrimitiveType blockLengthType;
    private ByteOrder templateIdByteOrder;
    private ByteOrder templateVersionByteOrder;
    private ByteOrder blockLengthByteOrder;

    /**
     * Read the message header structure and cache the meta data for finding the key fields for decoding messages.
     *
     * @param headerStructure for the meta data describing the message header.
     */
    public OtfHeaderDecoder(final HeaderStructure headerStructure)
    {
        size = headerStructure.tokens().get(0).size();

        for (final Token token : headerStructure.tokens())
        {
            switch (token.name())
            {
                case HeaderStructure.TEMPLATE_ID:
                    templateIdOffset = token.offset();
                    templateIdType = token.encoding().primitiveType();
                    templateIdByteOrder = token.encoding().byteOrder();
                    break;

                case HeaderStructure.TEMPLATE_VERSION:
                    templateVersionOffset = token.offset();
                    templateVersionType = token.encoding().primitiveType();
                    templateVersionByteOrder = token.encoding().byteOrder();
                    break;

                case HeaderStructure.BLOCK_LENGTH:
                    blockLengthOffset = token.offset();
                    blockLengthType = token.encoding().primitiveType();
                    blockLengthByteOrder = token.encoding().byteOrder();
                    break;
            }
        }
    }

    /**
     * The size of the message header in bytes.
     *
     * @return the size of the message header in bytes.
     */
    public int size()
    {
        return size;
    }

    /**
     * Get the template id from the message header.
     *
     * @param buffer from which to read the value.
     * @param bufferOffset in the buffer at which the message header begins.
     * @return the value of the template id.
     */
    public int getTemplateId(final DirectBuffer buffer, final int bufferOffset)
    {
        return Util.getInt(buffer, bufferOffset + templateIdOffset, templateIdType, templateIdByteOrder);
    }

    /**
     * Get the template version number from the message header.
     *
     * @param buffer from which to read the value.
     * @param bufferOffset in the buffer at which the message header begins.
     * @return the value of the template version number.
     */
    public int getTemplateVersion(final DirectBuffer buffer, final int bufferOffset)
    {
        return Util.getInt(buffer, bufferOffset + templateVersionOffset, templateVersionType, templateVersionByteOrder);
    }

    /**
     * Get the block length of the root block in the message.
     *
     * @param buffer from which to read the value.
     * @param bufferOffset in the buffer at which the message header begins.
     * @return the length of the root block in the coming message.
     */
    public int getBlockLength(final DirectBuffer buffer, final int bufferOffset)
    {
        return Util.getInt(buffer, bufferOffset + blockLengthOffset, blockLengthType, blockLengthByteOrder);
    }
}
