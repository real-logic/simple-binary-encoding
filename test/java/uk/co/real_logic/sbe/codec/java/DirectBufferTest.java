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
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import uk.co.real_logic.sbe.util.BitUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Integer.valueOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.util.BitUtil.SIZE_OF_BYTE;

@RunWith(Theories.class)
public class DirectBufferTest
{
    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();
    private static final int BUFFER_CAPACITY = 4096;
    private static final int INDEX = 8;

    private static final byte BYTE_VALUE = 1;
    private static final short SHORT_VALUE = 2;
    private static final int INT_VALUE = 4;
    private static final float FLOAT_VALUE = 5.0f;
    private static final long LONG_VALUE = 6;
    private static final double DOUBLE_VALUE = 7.0d;

    @DataPoint
    public static final DirectBuffer BYTE_ARRAY_BACKED = new DirectBuffer(new byte[BUFFER_CAPACITY]);

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocate(BUFFER_CAPACITY));

    @DataPoint
    public static final DirectBuffer DIRECT_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY));

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER_SLICE =
        new DirectBuffer(((ByteBuffer)(ByteBuffer.allocate(BUFFER_CAPACITY * 2).position(BUFFER_CAPACITY))).slice());

    // Note this will leak memory and a real world application would need to reclaim the allocated memory!!!
    @DataPoint
    public static final DirectBuffer OFF_HEAP_BUFFER = new DirectBuffer(BitUtil.getUnsafe().allocateMemory(BUFFER_CAPACITY), BUFFER_CAPACITY);

    @Theory
    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowExceptionForLimitAboveCapacity(final DirectBuffer buffer)
    {
        final int position = BUFFER_CAPACITY + 1;
        buffer.checkLimit(position);
    }

    @Theory
    public void shouldGetLongFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(buffer.getLong(INDEX, BYTE_ORDER)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldPutLongToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        buffer.putLong(INDEX, LONG_VALUE, BYTE_ORDER);

        assertThat(Long.valueOf(duplicateBuffer.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldGetIntFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(buffer.getInt(INDEX, BYTE_ORDER)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldPutIntToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        buffer.putInt(INDEX, INT_VALUE, BYTE_ORDER);

        assertThat(valueOf(duplicateBuffer.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldGetShortFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(buffer.getShort(INDEX, BYTE_ORDER)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldPutShortToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        buffer.putShort(INDEX, SHORT_VALUE, BYTE_ORDER);

        assertThat(Short.valueOf(duplicateBuffer.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldGetDoubleFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(buffer.getDouble(INDEX, BYTE_ORDER)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldPutDoubleToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        buffer.putDouble(INDEX, DOUBLE_VALUE, BYTE_ORDER);

        assertThat(Double.valueOf(duplicateBuffer.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldGetFloatFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(buffer.getFloat(INDEX, BYTE_ORDER)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldPutFloatToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        buffer.putFloat(INDEX, FLOAT_VALUE, BYTE_ORDER);

        assertThat(Float.valueOf(duplicateBuffer.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldGetByteFromBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

        duplicateBuffer.put(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(buffer.getByte(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldPutByteToBuffer(final DirectBuffer buffer)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);

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

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
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

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer = ByteBuffer.allocate(testBytes.length);
        buffer.getBytes(INDEX, dstBuffer, testBytes.length);

        assertThat(dstBuffer.array(), is(testBytes));
    }
    @Theory
    public void shouldGetBytesFromBufferToDirectBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer = ByteBuffer.allocateDirect(testBytes.length);
        buffer.getBytes(INDEX, dstBuffer, testBytes.length);

        byte[] result = new byte[testBytes.length];
        dstBuffer.flip();
        dstBuffer.get(result);
        assertThat(result, is(testBytes));
    }

    @Theory
    public void shouldGetBytesFromBufferToSlice(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer =
            ((ByteBuffer) ByteBuffer.allocate(testBytes.length*2).position(testBytes.length)).slice();

        buffer.getBytes(INDEX, dstBuffer, testBytes.length);

        byte[] result = new byte[testBytes.length];
        dstBuffer.flip();
        dstBuffer.get(result);
        assertThat(result, is(testBytes));
    }

    @Theory
    public void shouldGetBytesToDirectBufferFromDirectBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World!".getBytes();
        final DirectBuffer dstBuffer = new DirectBuffer(((ByteBuffer) ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice());

        buffer.putBytes(INDEX, testBytes);
        buffer.getBytes(INDEX, dstBuffer, 0, testBytes.length);

        byte[] result = new byte[testBytes.length];
        dstBuffer.getBytes(0, result);
        assertThat(result, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();
        buffer.putBytes(INDEX, testBytes);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
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
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromDirectBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ByteBuffer.allocateDirect(testBytes.length);
        srcBuffer.put(testBytes);
        srcBuffer.flip();

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromSlice(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ((ByteBuffer) ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice();

        srcBuffer.put(testBytes);
        srcBuffer.flip();

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(BYTE_ORDER);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToDirectBufferFromDirectBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World!".getBytes();
        final DirectBuffer srcBuffer = new DirectBuffer(((ByteBuffer) ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice());

        srcBuffer.putBytes(0, testBytes);
        buffer.putBytes(INDEX, srcBuffer, 0, testBytes.length);
        
        final byte[] buff = new byte[testBytes.length];
        buffer.getBytes(INDEX, buff);

        assertThat(buff, is(testBytes));
    }
}
