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

import org.junit.Test;
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

    static
    {
        System.setProperty(DirectBuffer.BOUNDS_CHECK_PROPERTY, "TRUE");
    }

    @DataPoint
    public static final DirectBuffer BYTE_ARRAY_BACKED = new DirectBuffer(new byte[BUFFER_SIZE]);

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocate(BUFFER_SIZE));

    @DataPoint
    public static final DirectBuffer DIRECT_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocateDirect(BUFFER_SIZE));

    @Theory
    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldCheckUpperBound(final DirectBuffer view)
    {
        view.putInt(view.getCapacity() - 1, INT_VALUE);
    }

    @Theory
    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldCheckLowerBound(final DirectBuffer view)
    {
        view.putInt(-1, INT_VALUE);
    }

    @Theory
    public void shouldGetLongFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(view.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldPutLongToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(duplicateBuffer.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldGetIntFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(view.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldPutIntToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(duplicateBuffer.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldGetShortFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(view.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldPutShortToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(duplicateBuffer.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldGetDoubleFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(view.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldPutDoubleToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(duplicateBuffer.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldGetFloatFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(view.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldPutFloatToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(duplicateBuffer.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldGetByteFromView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        duplicateBuffer.put(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(view.getByte(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldPutByteToView(final DirectBuffer view)
    {
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();

        view.putByte(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(duplicateBuffer.get(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldGetByteArrayFromView(final DirectBuffer view)
    {
        final byte[] testArray = {'H', 'e', 'l', 'l', 'o'};

        int i = INDEX;
        for (final byte v : testArray)
        {
            view.putByte(i, v);
            i += SIZE_OF_BYTE;
        }

        final byte[] result = new byte[testArray.length];
        view.getBytes(INDEX, result);

        assertThat(result, is(testArray));
    }

    @Theory
    public void shouldGetBytesFromView(final DirectBuffer view)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final byte[] buffer = new byte[testBytes.length];
        view.getBytes(INDEX, buffer);

        assertThat(buffer, is(testBytes));
    }

    @Theory
    public void shouldGetBytesFromViewToBuffer(final DirectBuffer view)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer = ByteBuffer.allocate(testBytes.length);
        view.getBytes(INDEX, dstBuffer, testBytes.length);

        assertThat(dstBuffer.array(), is(testBytes));
    }

    @Theory
    public void shouldPutBytesToView(final DirectBuffer view)
    {
        final byte[] testBytes = "Hello World".getBytes();
        view.putBytes(INDEX, testBytes);

        final byte[] buffer = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buffer);

        assertThat(buffer, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToViewFromBuffer(final DirectBuffer view)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ByteBuffer.wrap(testBytes);

        view.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buffer = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = view.duplicateByteBuffer();
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buffer);

        assertThat(buffer, is(testBytes));
    }
}
