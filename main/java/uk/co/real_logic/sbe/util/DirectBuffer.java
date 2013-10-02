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
package uk.co.real_logic.sbe.util;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static uk.co.real_logic.sbe.util.BitUtil.*;

/**
 * Direct buffer which can wrap a byte[] or a {@link ByteBuffer} that is heap or direct allocated for direct access
 * with native types.
 */
public class DirectBuffer
{
    /** Boolean system property to control if bounds checking is applied or not */
    public static final String BOUNDS_CHECK_PROPERTY = "atomic.buffer.bounds.check";

    /** Should access to the backing array be bounds checked or not */
    public static final boolean BOUNDS_CHECK = "TRUE".equalsIgnoreCase(System.getProperty(BOUNDS_CHECK_PROPERTY, "FALSE"));

    private static final Unsafe UNSAFE = BitUtil.getUnsafe();
    private static final long BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    private final byte[] byteArray;
    private final ByteBuffer byteBuffer;

    private final long baseOffset;
    private final int capacity;

    /**
     * Attach a view to a byte[] for providing atomic direct access.
     *
     * @param byteArray to which the view is attached.
     */
    public DirectBuffer(final byte[] byteArray)
    {
        Verify.notNull(byteArray, "byteArray");

        baseOffset = BYTE_ARRAY_OFFSET;
        capacity = byteArray.length;
        this.byteArray = byteArray;
        this.byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder());
    }

    /**
     * Attach a view to a {@link ByteBuffer} for providing atomic direct access, the {@link ByteBuffer} can be
     * heap based or direct.
     *
     * @param byteBuffer to which the view is attached.
     */
    public DirectBuffer(final ByteBuffer byteBuffer)
    {
        Verify.notNull(byteBuffer, "byteBuffer");

        this.byteBuffer = byteBuffer.duplicate().order(ByteOrder.nativeOrder());

        if (byteBuffer.hasArray())
        {
            byteArray = byteBuffer.array();
            baseOffset = BYTE_ARRAY_OFFSET;
        }
        else
        {
            byteArray = null;
            baseOffset = ((sun.nio.ch.DirectBuffer)byteBuffer).address();
        }

        capacity = byteBuffer.capacity();
    }

    /**
     * Get the capacity of the underlying buffer.
     *
     * @return the capacity of the underlying buffer in bytes.
     */
    public int getCapacity()
    {
        return capacity;
    }

    /**
     * Create a duplicate {@link ByteBuffer} for the view in native byte order.
     * The duplicate {@link ByteBuffer} shares the underlying memory so all changes are reflected.
     *
     * @return a duplicate of the underlying memory wrapped as a {@link ByteBuffer}
     */
    public ByteBuffer duplicateByteBuffer()
    {
        byteBuffer.clear();
        return byteBuffer.duplicate().order(ByteOrder.nativeOrder());
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value for at a given index
     */
    public long getLong(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_LONG, "LHS");
        return UNSAFE.getLong(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value for at a given index
     */
    public void putLong(final int index, final long value)
    {
        checkBounds(index, capacity, SIZE_OF_LONG, "LHS");
        UNSAFE.putLong(byteArray, baseOffset + index, value);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public int getInt(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_INT, "LHS");
        return UNSAFE.getInt(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putInt(final int index, final int value)
    {
        checkBounds(index, capacity, SIZE_OF_INT, "LHS");
        UNSAFE.putInt(byteArray, baseOffset + index, value);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public double getDouble(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_DOUBLE, "LHS");
        return UNSAFE.getDouble(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putDouble(final int index, final double value)
    {
        checkBounds(index, capacity, SIZE_OF_DOUBLE, "LHS");
        UNSAFE.putDouble(byteArray, baseOffset + index, value);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public float getFloat(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_FLOAT, "LHS");
        return UNSAFE.getFloat(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putFloat(final int index, final float value)
    {
        checkBounds(index, capacity, SIZE_OF_FLOAT, "LHS");
        UNSAFE.putFloat(byteArray, baseOffset + index, value);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public short getShort(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_SHORT, "LHS");
        return UNSAFE.getShort(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putShort(final int index, final short value)
    {
        checkBounds(index, capacity, SIZE_OF_SHORT, "LHS");
        UNSAFE.putShort(byteArray, baseOffset + index, value);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public byte getByte(final int index)
    {
        checkBounds(index, capacity, SIZE_OF_BYTE, "LHS");
        return UNSAFE.getByte(byteArray, baseOffset + index);
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putByte(final int index, final byte value)
    {
        checkBounds(index, capacity, SIZE_OF_BYTE, "LHS");
        UNSAFE.putByte(byteArray, baseOffset + index, value);
    }

    /**
     * Get dst from the underlying buffer and put them into a supplied byte array.
     * This method will try to fill the supplied byte array.
     *
     * @param index in the underlying buffer to start from.
     * @param dst   into which the dst will be copied.
     */
    public void getBytes(final int index, final byte[] dst)
    {
        getBytes(index, dst, 0, dst.length);
    }

    /**
     * Get bytes from the underlying buffer and put them into a supplied byte array.
     *
     * @param index  in the underlying buffer to start from.
     * @param dst    into which the bytes will be copied.
     * @param offset in the supplied buffer to start the copy
     * @param length of the supplied buffer to use.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final byte[] dst, final int offset, final int length)
    {
        final int count = Math.min(length, capacity - index);
        checkBounds(index, capacity, count, "LHS");
        checkBounds(offset, dst.length, count, "RHS");
        UNSAFE.copyMemory(byteArray, baseOffset + index, dst, BYTE_ARRAY_OFFSET + offset, count);

        return count;
    }

    /**
     * Get bytes from the underlying buffer and put them into a supplied {@link ByteBuffer}.
     *
     * @param index     in the underlying buffer to start from.
     * @param dstBuffer into which the bytes will be copied.
     * @param length    of the supplied buffer to use.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final ByteBuffer dstBuffer, final int length)
    {
        byteBuffer.clear().limit(index + length).position(index);
        dstBuffer.put(byteBuffer);

        int count = Math.min(dstBuffer.remaining(), capacity - index);
        count = Math.min(count, length);

        checkBounds(index, capacity, count, "LHS");

        final int dstOffset = dstBuffer.position();
        final byte[] dstByteArray;
        final long dstBaseOffset;
        if (dstBuffer.hasArray())
        {
            dstByteArray = dstBuffer.array();
            dstBaseOffset = BYTE_ARRAY_OFFSET;
        }
        else
        {
            dstByteArray = null;
            dstBaseOffset = ((sun.nio.ch.DirectBuffer)dstBuffer).address();
        }

        UNSAFE.copyMemory(byteArray, baseOffset + index, dstByteArray, dstBaseOffset + dstOffset, count);
        dstBuffer.position(dstBuffer.position() + count);

        return count;
    }

    /**
     * Put an array of src into the underlying buffer for the view.
     *
     * @param index in the underlying buffer to start from.
     * @param src   to be copied to the underlying buffer.
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final byte[] src)
    {
        return putBytes(index, src, 0, src.length);
    }

    /**
     * Put an array of src into the underlying buffer for the view.
     *
     * @param index  in the underlying buffer to start from.
     * @param src    to be copied to the underlying buffer.
     * @param offset in the supplied buffer to begin the copy.
     * @param length of the supplied buffer to copy.
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final byte[] src, final int offset, final int length)
    {
        final int count = Math.min(length, capacity - index);
        checkBounds(index, capacity, count, "LHS");
        checkBounds(offset, src.length, count, "RHS");
        UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + offset, byteArray, baseOffset + index,  count);

        return count;
    }

    /**
     * Put an bytes into the underlying buffer for the view.  Bytes will be copied from current
     * {@link java.nio.ByteBuffer#position()} to {@link java.nio.ByteBuffer#limit()}.
     *
     * @param index     in the underlying buffer to start from.
     * @param srcBuffer to copy the bytes from.
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final ByteBuffer srcBuffer, final int length)
    {
        int count = Math.min(srcBuffer.remaining(), capacity - index);
        count = Math.min(count, length);

        checkBounds(index, capacity, count, "LHS");

        final int srcOffset = srcBuffer.position();
        final byte[] srcByteArray;
        final long srcBaseOffset;
        if (srcBuffer.hasArray())
        {
            srcByteArray = srcBuffer.array();
            srcBaseOffset = BYTE_ARRAY_OFFSET;
        }
        else
        {
            srcByteArray = null;
            srcBaseOffset = ((sun.nio.ch.DirectBuffer)srcBuffer).address();
        }

        UNSAFE.copyMemory(srcByteArray, srcBaseOffset + srcOffset, byteArray, baseOffset + index,  count);
        srcBuffer.position(srcBuffer.position() + count);

        return count;
    }

    private void checkBounds(final int index, final int capacity, final int size, final String endpoint)
    {
        if (BOUNDS_CHECK)
        {
            if (index < 0 || (index + size) > capacity)
            {
                final String msg = String.format("Index %d out of bounds 0-%d for type of size %d at %s",
                                                 Integer.valueOf(index),
                                                 Integer.valueOf((capacity - 1)),
                                                 Integer.valueOf(size),
                                                 endpoint);

                throw new IndexOutOfBoundsException(msg);
            }
        }
    }
}
