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
package uk.co.real_logic.sbe.generation.java;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static java.lang.Integer.valueOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.util.BitUtil.SIZE_OF_BYTE;

@RunWith(Theories.class)
public class DirectBufferTest
{
    private static final int BUFFER_SIZE = 4096;
    private static final int INDEX = 8;

    private static final byte BYTE_VALUE = 1;
    private static final short SHORT_VALUE = 2;
    private static final int INT_VALUE = 4;
    private static final float FLOAT_VALUE = 5.0f;
    private static final long LONG_VALUE = 6;
    private static final double DOUBLE_VALUE = 7.0d;

    @DataPoint
    public static final DirectBuffer BYTE_ARRAY_BACKED = new DirectBuffer(new byte[BUFFER_SIZE]);

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocate(BUFFER_SIZE));

    @DataPoint
    public static final DirectBuffer DIRECT_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocateDirect(BUFFER_SIZE));

    @Theory
    public void shouldGetLongFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(buffer.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldPutLongToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(duplicateBuffer.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldGetIntFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(buffer.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldPutIntToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(duplicateBuffer.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldGetShortFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(buffer.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldPutShortToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(duplicateBuffer.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldGetDoubleFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(buffer.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldPutDoubleToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(duplicateBuffer.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldGetFloatFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(buffer.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldPutFloatToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(duplicateBuffer.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldGetByteFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        duplicateBuffer.put(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(buffer.getByte(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldPutByteToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();

        buffer.putByte(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(duplicateBuffer.get(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldGetByteArrayFromBuffer(final DirectBuffer buffer)
    {
        final byte[] testArray = {'H', 'e', 'l', 'l', 'o'};

        int i = INDEX;
        for (final byte v : testArray)
        {
            buffer.putByte(i, v);
            i += SIZE_OF_BYTE;
        }

        final byte[] result = new byte[testArray.length];
        buffer.getBytes(INDEX, result);

        assertThat(result, is(testArray));
    }

    @Theory
    public void shouldGetBytesFromBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final byte[] buff = new byte[testBytes.length];
        buffer.getBytes(INDEX, buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldGetBytesFromBufferToBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer = ByteBuffer.allocate(testBytes.length);
        buffer.getBytes(INDEX, dstBuffer, testBytes.length);

        assertThat(dstBuffer.array(), is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();
        buffer.putBytes(INDEX, testBytes);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ByteBuffer.wrap(testBytes);

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }
}
