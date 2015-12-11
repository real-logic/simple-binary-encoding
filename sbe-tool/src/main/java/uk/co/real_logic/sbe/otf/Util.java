/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.ir.Encoding;

import java.nio.ByteOrder;

/**
 * Utility functions to help with on-the-fly decoding.
 */
public class Util
{
    /**
     * Get an integer value from a buffer at a given index for a {@link PrimitiveType}.
     *
     * @param buffer    from which to read.
     * @param index     at which he integer should be read.
     * @param type      of the integer encoded in the buffer.
     * @param byteOrder of the integer in the buffer.
     * @return the value of the encoded integer.
     */
    public static int getInt(final DirectBuffer buffer, final int index, final PrimitiveType type, final ByteOrder byteOrder)
    {
        switch (type)
        {
            case INT8:
                return buffer.getByte(index);

            case UINT8:
                return (short)(buffer.getByte(index) & 0xFF);

            case INT16:
                return buffer.getShort(index, byteOrder);

            case UINT16:
                return buffer.getShort(index, byteOrder) & 0xFFFF;

            case INT32:
                return buffer.getInt(index, byteOrder);

            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /**
     * Get a long value from a buffer at a given index for a given {@link Encoding}.
     *
     * @param buffer   from which to read.
     * @param index    at which he integer should be read.
     * @param encoding of the value.
     * @return the value of the encoded long.
     */
    public static long getLong(final DirectBuffer buffer, final int index, final Encoding encoding)
    {
        switch (encoding.primitiveType())
        {
            case CHAR:
                return buffer.getByte(index);

            case INT8:
                return buffer.getByte(index);

            case INT16:
                return buffer.getShort(index, encoding.byteOrder());

            case INT32:
                return buffer.getInt(index, encoding.byteOrder());

            case INT64:
                return buffer.getLong(index, encoding.byteOrder());

            case UINT8:
                return (short)(buffer.getByte(index) & 0xFF);

            case UINT16:
                return buffer.getShort(index, encoding.byteOrder()) & 0xFFFF;

            case UINT32:
                return buffer.getInt(index, encoding.byteOrder()) & 0xFFFF_FFFFL;

            case UINT64:
                return buffer.getLong(index, encoding.byteOrder());

            default:
                throw new IllegalArgumentException("Unsupported type for long: " + encoding.primitiveType());
        }
    }
}
