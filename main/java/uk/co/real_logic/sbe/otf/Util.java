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
import uk.co.real_logic.sbe.codec.java.CodecUtil;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;

import java.nio.ByteOrder;

/**
 * Utility functions to help with on-the-fly decoding.
 */
public class Util
{
    /**
     * Get an integer value from a buffer at a given index.
     *
     * @param buffer      from which to read.
     * @param bufferIndex at which he integer should be read.
     * @param type        of the integer encoded in the buffer.
     * @param byteOrder   of the integer in the buffer.
     * @return the value of the encoded integer.
     */
    static int getInt(final DirectBuffer buffer, final int bufferIndex, final PrimitiveType type, final ByteOrder byteOrder)
    {
        switch (type)
        {
            case INT8:
                return CodecUtil.int8Get(buffer, bufferIndex);

            case UINT8:
                return CodecUtil.uint8Get(buffer, bufferIndex);

            case INT16:
                return CodecUtil.int16Get(buffer, bufferIndex, byteOrder);

            case UINT16:
                return CodecUtil.uint16Get(buffer, bufferIndex, byteOrder);

            case INT32:
                return CodecUtil.int32Get(buffer, bufferIndex, byteOrder);

            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
