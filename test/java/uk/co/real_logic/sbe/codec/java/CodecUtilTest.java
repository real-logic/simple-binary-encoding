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
package uk.co.real_logic.sbe.codec.java;

import org.junit.Test;
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteOrder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CodecUtilTest
{
    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();
    private static final int BUFFER_CAPACITY = 64;

    private final MutableDirectBuffer buffer = new UnsafeBuffer(new byte[BUFFER_CAPACITY]);

    @Test
    public void shouldTestBitInByte()
    {
        final byte bits = (byte)0b1000_0000;
        final int bufferIndex = 8;
        final int bitIndex = 7;
        buffer.putByte(bufferIndex, bits);

        for (int i = 0; i < 8; i++)
        {
            final boolean result = CodecUtil.uint8GetChoice(buffer, bufferIndex, i);
            if (bitIndex == i)
            {
                assertTrue(result);
            }
            else
            {
                assertFalse("bit set i = " + i, result);
            }
        }
    }

    @Test
    public void shouldSetBitInByte()
    {
        final int bufferIndex = 8;

        short total = 0;
        for (int i = 0; i < 8; i++)
        {
            CodecUtil.uint8PutChoice(buffer, bufferIndex, i, true);
            total += (1 << i);
            assertThat(buffer.getByte(bufferIndex), is((byte)total));
        }
    }

    @Test
    public void shouldTestBitInShort()
    {
        final short bits = (short)0b0000_0000_0000_0100;
        final int bufferIndex = 8;
        final int bitIndex = 2;
        buffer.putShort(bufferIndex, bits, BYTE_ORDER);

        for (int i = 0; i < 16; i++)
        {
            final boolean result = CodecUtil.uint16GetChoice(buffer, bufferIndex, i, BYTE_ORDER);
            if (bitIndex == i)
            {
                assertTrue(result);
            }
            else
            {
                assertFalse("bit set i = " + i, result);
            }
        }
    }

    @Test
    public void shouldSetBitInShort()
    {
        final int bufferIndex = 8;

        int total = 0;
        for (int i = 0; i < 16; i++)
        {
            CodecUtil.uint16PutChoice(buffer, bufferIndex, i, true, BYTE_ORDER);
            total += (1 << i);
            assertThat(buffer.getShort(bufferIndex, BYTE_ORDER), is((short)total));
        }
    }

    @Test
    public void shouldTestBitInInt()
    {
        final int bits = 0b0000_0000_0000_0000_0000_0000_0000_0100;
        final int bufferIndex = 8;
        final int bitIndex = 2;
        buffer.putInt(bufferIndex, bits, BYTE_ORDER);

        for (int i = 0; i < 32; i++)
        {
            final boolean result = CodecUtil.uint32GetChoice(buffer, bufferIndex, i, BYTE_ORDER);
            if (bitIndex == i)
            {
                assertTrue(result);
            }
            else
            {
                assertFalse("bit set i = " + i, result);
            }
        }
    }

    @Test
    public void shouldSetBitInInt()
    {
        final int bufferIndex = 8;
        long total = 0;

        for (int i = 0; i < 32; i++)
        {
            CodecUtil.uint32PutChoice(buffer, bufferIndex, i, true, BYTE_ORDER);
            total += (1 << i);
            assertThat(buffer.getInt(bufferIndex, BYTE_ORDER), is((int)total));
        }
    }
}
