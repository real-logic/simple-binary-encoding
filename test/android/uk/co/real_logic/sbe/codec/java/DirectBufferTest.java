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

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import android.os.MemoryFile;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.Integer.valueOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@SmallTest
@RunWith(Theories.class)
public class DirectBufferTest
{
    private static final int BUFFER_CAPACITY = 4096;
    private static final int INDEX = 8;

    private static final byte BYTE_VALUE = 1;
    private static final short SHORT_VALUE = 22345;
    private static final int INT_VALUE = 41244325;
    private static final float FLOAT_VALUE = 5.0f;
    private static final long LONG_VALUE = 1234567890123L;
    private static final double DOUBLE_VALUE = 7.0d;

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();

    @DataPoint
    public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

    @DataPoint
    public static final ByteOrder NONNATIVE_BYTE_ORDER =
        (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

    @DataPoint
    public static final DirectBuffer BYTE_ARRAY_BACKED = new DirectBuffer(new byte[BUFFER_CAPACITY]);

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocate(BUFFER_CAPACITY));

    @DataPoint
    public static final DirectBuffer DIRECT_BYTE_BUFFER = new DirectBuffer(ByteBuffer.allocateDirect(BUFFER_CAPACITY));

    @DataPoint
    public static final DirectBuffer HEAP_BYTE_BUFFER_SLICE =
        new DirectBuffer(((ByteBuffer)(ByteBuffer.allocate(BUFFER_CAPACITY * 2).position(BUFFER_CAPACITY))).slice());

    @DataPoint
    public static final DirectBuffer MEMORY_MAPPED_BUFFER = new DirectBuffer(createMemoryMappedBuffer());

    private static ByteBuffer offHeapDummyBuffer = ByteBuffer.allocateDirect(BUFFER_CAPACITY);
    private static long memoryBlockAddress = BitUtil.getEffectiveDirectAddress(offHeapDummyBuffer);

     @DataPoint
     public static final DirectBuffer OFF_HEAP_BUFFER = new DirectBuffer(memoryBlockAddress, BUFFER_CAPACITY);

     @DataPoint //not valid for jdk
     public static final DirectBuffer MEMORY_FILE_BUFFER =
         new DirectBuffer(createMemoryFile());

     @Theory
    public void shouldThrowExceptionForLimitAboveCapacity(final DirectBuffer buffer)
    {
        exceptionRule.expect(IndexOutOfBoundsException.class);
        final int position = BUFFER_CAPACITY + 1;

        buffer.checkLimit(position);
    }

    @Theory
    public void shouldNotThrowExceptionForLimitAtCapacity(final DirectBuffer buffer)
    {
        final int position = BUFFER_CAPACITY;

        buffer.checkLimit(position);
    }

    @Theory
    public void shouldGetLongFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.putLong(INDEX, LONG_VALUE);

        assertThat(Long.valueOf(buffer.getLong(INDEX, byteOrder)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldPutLongToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        buffer.putLong(INDEX, LONG_VALUE, byteOrder);

        assertThat(Long.valueOf(duplicateBuffer.getLong(INDEX)), is(Long.valueOf(LONG_VALUE)));
    }

    @Theory
    public void shouldGetIntFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.putInt(INDEX, INT_VALUE);

        assertThat(valueOf(buffer.getInt(INDEX, byteOrder)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldPutIntToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        buffer.putInt(INDEX, INT_VALUE, byteOrder);

        assertThat(valueOf(duplicateBuffer.getInt(INDEX)), is(valueOf(INT_VALUE)));
    }

    @Theory
    public void shouldGetShortFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.putShort(INDEX, SHORT_VALUE);

        assertThat(Short.valueOf(buffer.getShort(INDEX, byteOrder)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldPutShortToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        buffer.putShort(INDEX, SHORT_VALUE, byteOrder);

        assertThat(Short.valueOf(duplicateBuffer.getShort(INDEX)), is(Short.valueOf(SHORT_VALUE)));
    }

    @Theory
    public void shouldGetDoubleFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.putDouble(INDEX, DOUBLE_VALUE);

        assertThat(Double.valueOf(buffer.getDouble(INDEX, byteOrder)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldPutDoubleToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        buffer.putDouble(INDEX, DOUBLE_VALUE, byteOrder);

        assertThat(Double.valueOf(duplicateBuffer.getDouble(INDEX)), is(Double.valueOf(DOUBLE_VALUE)));
    }

    @Theory
    public void shouldGetFloatFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.putFloat(INDEX, FLOAT_VALUE);

        assertThat(Float.valueOf(buffer.getFloat(INDEX, byteOrder)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldPutFloatToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        buffer.putFloat(INDEX, FLOAT_VALUE, byteOrder);

        assertThat(Float.valueOf(duplicateBuffer.getFloat(INDEX)), is(Float.valueOf(FLOAT_VALUE)));
    }

    @Theory
    public void shouldGetByteFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

        duplicateBuffer.put(INDEX, BYTE_VALUE);

        assertThat(Byte.valueOf(buffer.getByte(INDEX)), is(Byte.valueOf(BYTE_VALUE)));
    }

    @Theory
    public void shouldPutByteToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);

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
            i += Byte.SIZE / 8;
        }

        final byte[] result = new byte[testArray.length];
        buffer.getBytes(INDEX, result);

        assertThat(result, is(testArray));
    }

    @Theory
    public void shouldGetBytesFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final byte[] buff = new byte[testBytes.length];
        buffer.getBytes(INDEX, buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldGetBytesFromBufferToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer = ByteBuffer.allocate(testBytes.length);
        buffer.getBytes(INDEX, dstBuffer, testBytes.length);

        assertThat(dstBuffer.array(), is(testBytes));
    }

    @Theory
    public void shouldGetBytesFromBufferToDirectBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
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
    public void shouldGetBytesFromBufferToSlice(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();

        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.put(testBytes);

        final ByteBuffer dstBuffer =
            ((ByteBuffer)ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice();

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
        final ByteBuffer testBuff = (ByteBuffer)ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length);
        final DirectBuffer dstBuffer = new DirectBuffer(testBuff.slice());

        buffer.putBytes(INDEX, testBytes);
        buffer.getBytes(INDEX, dstBuffer, 0, testBytes.length);

        byte[] result = new byte[testBytes.length];
        dstBuffer.getBytes(0, result);
        assertThat(result, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();
        buffer.putBytes(INDEX, testBytes);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ByteBuffer.wrap(testBytes);

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromDirectBuffer(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer = ByteBuffer.allocateDirect(testBytes.length);
        srcBuffer.put(testBytes);
        srcBuffer.flip();

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToBufferFromSlice(final DirectBuffer buffer, final ByteOrder byteOrder)
    {
        final byte[] testBytes = "Hello World".getBytes();
        final ByteBuffer srcBuffer =
            ((ByteBuffer)ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice();

        srcBuffer.put(testBytes);
        srcBuffer.flip();

        buffer.putBytes(INDEX, srcBuffer, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        final ByteBuffer duplicateBuffer = buffer.duplicateByteBuffer().order(byteOrder);
        duplicateBuffer.position(INDEX);
        duplicateBuffer.get(buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldPutBytesToDirectBufferFromDirectBuffer(final DirectBuffer buffer)
    {
        final byte[] testBytes = "Hello World!".getBytes();
        final DirectBuffer srcBuffer =
            new DirectBuffer(((ByteBuffer)ByteBuffer.allocate(testBytes.length * 2).position(testBytes.length)).slice());

        srcBuffer.putBytes(0, testBytes);
        buffer.putBytes(INDEX, srcBuffer, 0, testBytes.length);

        final byte[] buff = new byte[testBytes.length];
        buffer.getBytes(INDEX, buff);

        assertThat(buff, is(testBytes));
    }

    @Theory
    public void shouldGetByteArrayFromBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final byte[] testBytes = null;

        buffer.getBytes(INDEX, testBytes, 0, 10);
    }

    @Theory
    public void shouldPutByteArrayToBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final byte[] testBytes = null;

        buffer.putBytes(INDEX, testBytes, 0, 10);
    }

    @Theory
    public void shouldGetBytesFromBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final ByteBuffer testBuffer = null;

        buffer.getBytes(INDEX, testBuffer, 10);
    }

    @Theory
    public void shouldPutBytesToBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final ByteBuffer testBuffer = null;

        buffer.putBytes(INDEX, testBuffer, 10);
    }

    @Theory
    public void shouldGetDirectBytesFromBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final DirectBuffer testBuffer = null;

        buffer.getBytes(INDEX, testBuffer, 0, 10);
    }

    @Theory
    public void shouldPutDirectBytesToBufferNpe(final DirectBuffer buffer)
    {
        exceptionRule.expect(NullPointerException.class);
        final DirectBuffer testBuffer = null;

        buffer.putBytes(INDEX, testBuffer, 0, 10);
    }

    @Theory
    public void shouldGetByteArrayFromBufferTruncate(final DirectBuffer buffer)
    {
        final byte[] testBytes = new byte[10];

        final int result = buffer.getBytes(INDEX, testBytes, 0, 21);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(10)));
    }

    @Theory
    public void shouldPutByteArrayToBufferTruncate(final DirectBuffer buffer)
    {
        final byte[] testBytes = new byte[11];

        final int result = buffer.putBytes(INDEX, testBytes, 0, 20);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(11)));
    }

    @Theory
    public void shouldGetBytesFromBufferTruncate(final DirectBuffer buffer)
    {
        final ByteBuffer testBytes = ByteBuffer.allocate(12);

        final int result = buffer.getBytes(INDEX, testBytes, 20);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(12)));
    }

    @Theory
    public void shouldPutBytesToBufferTruncate(final DirectBuffer buffer)
    {
        final ByteBuffer testBytes = ByteBuffer.allocate(13);

        final int result = buffer.putBytes(INDEX, testBytes, 20);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(13)));
    }

    @Theory
    public void shouldGetDirectBytesFromBufferTruncate(final DirectBuffer buffer)
    {
        final DirectBuffer testBytes = new DirectBuffer(new byte[14]);

        final int result = buffer.getBytes(INDEX, testBytes, 0, 20);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(14)));
    }

    @Theory
    public void shouldPutDirectBytesToBufferTruncate(final DirectBuffer buffer)
    {
        final DirectBuffer testBytes = new DirectBuffer(new byte[15]);

        final int result = buffer.putBytes(INDEX, testBytes, 0, 20);

        assertThat(Integer.valueOf(result), is(Integer.valueOf(15)));
    }

    @Theory
    public void shouldTestHelloMemoryFile() throws IOException
    {
        final byte[] testBytes = "Hello World!".getBytes();
        MEMORY_FILE_BUFFER.putBytes(10, testBytes, 6, 6);
        byte[] result = new byte[6];
        memoryFile.readBytes(result, 10, 0, 6);
        String s = new String(result);
        assertThat(s, is("World!"));
    }

    private static RandomAccessFile memoryMappedFile;
    private static MappedByteBuffer buffer;
    private static MemoryFile memoryFile;

    private static MemoryFile createMemoryFile()
    {
        try
        {
            memoryFile = new MemoryFile("gigica", BUFFER_CAPACITY);
        }
        catch (IOException e)
        {
            memoryFile = null;
        }
        return memoryFile;
    }

    private static ByteBuffer createMemoryMappedBuffer()
    {
        try
        {
            File tempFile = new File("/dev/zero");
            memoryMappedFile = new RandomAccessFile(tempFile, "rw");
            buffer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, BUFFER_CAPACITY);
            return buffer;
        }
        catch (IOException e)
        {
            Log.e(DirectBufferTest.class.getName(), "Exception encountered", e);
            e.printStackTrace();
        }
        return null;
    }

    @AfterClass
    public static void cleanup()
    {
        try
        {
            //if we do not unmap the buffer, the temporary file is not deleted on exit
            //forcibly free the buffer
            Method cleanMethod = buffer.getClass().getMethod("free");
            cleanMethod.setAccessible(true);
            cleanMethod.invoke(buffer);

            memoryMappedFile.close();
            memoryFile.close();
        }
        catch (Exception e)
        {
            Log.e(DirectBufferTest.class.getName(), "Exception encountered", e);
            e.printStackTrace();
        }
    }
}
