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

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;

import libcore.io.Memory;

/**
 * Direct buffer which can wrap a byte[] or a {@link ByteBuffer} that is heap or direct allocated
 * for direct access with native types.
 */
public class DirectBuffer
{
    private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

    private byte[] byteArray;
    private ByteBuffer byteBuffer;
    private long effectiveDirectAddress;
    private int offset;
    private int capacity;
    private Object theBuffer;

    //
    //"effectiveDirectAddress" type changed from int to long in changeset: 0121106d9dc1ba713b53822886355e4d9339e852 (Android 4.3 - api 18)
    //at the same time, methods inside libcore.io.Memory where changed to use long addresses instead of ints 
    //https://android.googlesource.com/platform/libcore/+/0121106d9dc1ba713b53822886355e4d9339e852%5E%21/luni/src/main/java/java/nio/Buffer.java
    //
    //Added to Buffer in changeset: bd8ecd863aa83df50d7ce8f5950d8645ab6356af (Android 2.3 - api 9)
    //https://android.googlesource.com/platform/libcore/+/bd8ecd863aa83df50d7ce8f5950d8645ab6356af%5E%21/nio/src/main/java/java/nio/Buffer.java

    private static final Field EFFECTIVE_DIRECT_ADDRESS_FIELD;
    // private static final boolean USE_LONG_ADDRESS;

    static
    {
        try
        {
            final PrivilegedExceptionAction<Field> action = new PrivilegedExceptionAction<Field>()
            {
                public Field run() throws Exception
                {
                    Field field = Buffer.class.getDeclaredField("effectiveDirectAddress");
                    field.setAccessible(true);
                    return field;
                }
            };

            EFFECTIVE_DIRECT_ADDRESS_FIELD = AccessController.doPrivileged(action);
            // USE_LONG_ADDRESS = EFFECTIVE_DIRECT_ADDRESS_FIELD.getType() == long.class;
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Attach a view to a byte[] for providing direct access.
     *
     * @param buffer to which the view is attached.
     */
    public DirectBuffer(final byte[] buffer)
    {
        wrap(buffer);
    }

    /**
     * Attach a view to a {@link ByteBuffer} for providing direct access, the {@link ByteBuffer} can be
     * heap based or direct.
     *
     * @param buffer to which the view is attached.
     */
    public DirectBuffer(final ByteBuffer buffer)
    {
        wrap(buffer);
    }

    /**
     * Attach a view to a byte[] for providing direct access.
     *
     * @param buffer to which the view is attached.
     */
    public void wrap(final byte[] buffer)
    {
        offset = 0;
        effectiveDirectAddress = 0;
        capacity = buffer.length;
        byteArray = buffer;
        byteBuffer = null;
        theBuffer = byteArray;
    }

    /**
     * Attach a view to a {@link ByteBuffer} for providing direct access, the {@link ByteBuffer} can be
     * heap based or direct.
     *
     * @param buffer to which the view is attached.
     */
    public void wrap(final ByteBuffer buffer)
    {
        byteBuffer = buffer;

        if (buffer.isDirect())
        {
            byteArray = null;
            theBuffer = byteBuffer;
            offset = 0;
            try
            {
                effectiveDirectAddress = EFFECTIVE_DIRECT_ADDRESS_FIELD.getLong(buffer);
                // effectiveDirectAddress = (USE_LONG_ADDRESS) ? EFFECTIVE_DIRECT_ADDRESS_FIELD.getLong(buffer)
                //         : EFFECTIVE_DIRECT_ADDRESS_FIELD.getInt(buffer);
            }
            catch (IllegalAccessException | IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            byteArray = buffer.array();
            theBuffer = byteArray;
            offset = buffer.arrayOffset();
            effectiveDirectAddress = 0;
        }

        capacity = buffer.capacity();
    }

    /**
     * Return the underlying byte[] for this buffer or null if none is attached.
     *
     * @return the underlying byte[] for this buffer.
     */
    public byte[] array()
    {
        return byteArray;
    }

    /**
     * Return the underlying {@link ByteBuffer} if one is attached.
     *
     * @return the underlying {@link ByteBuffer} if one is attached.
     */
    public ByteBuffer byteBuffer()
    {
        return byteBuffer;
    }

    /**
     * Get the capacity of the underlying buffer.
     *
     * @return the capacity of the underlying buffer in bytes.
     */
    public int capacity()
    {
        return capacity;
    }

    /**
     * Check that a given limit is not greater than the capacity of a buffer from a given offset.
     *
     * Can be overridden in a DirectBuffer subclass to enable an extensible buffer or handle retry after a flush.
     *
     * @param limit access is required to.
     * @throws IndexOutOfBoundsException if limit is beyond buffer capacity.
     */
    public void checkLimit(final int limit)
    {
        if (limit > capacity)
        {
            final String msg = String.format(Locale.US,
                                             "limit=%d is beyond capacity=%d",
                                             Integer.valueOf(limit),
                                             Integer.valueOf(capacity));

            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * Create a duplicate {@link ByteBuffer} for the view in native byte order.
     * The duplicate {@link ByteBuffer} shares the underlying memory so all changes are reflected.
     * If no {@link ByteBuffer} is attached then one will be created.
     *
     * @return a duplicate of the underlying {@link ByteBuffer}
     */
    public ByteBuffer duplicateByteBuffer()
    {
        final ByteBuffer duplicate;

        if (null == byteBuffer)
        {
            duplicate = ByteBuffer.wrap(byteArray);
        }
        else
        {
            duplicate = byteBuffer.duplicate();
        }

        duplicate.clear();

        return duplicate;
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value for at a given index
     */
    public long getLong(final int index, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            return Memory.peekLong(byteArray, offset + index, byteOrder);
        }
        else
        {
            return Memory.peekLong(effectiveDirectAddress + index, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value for at a given index
     * @param byteOrder of the value when written
     */
    public void putLong(final int index, final long value, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            Memory.pokeLong(byteArray, offset + index, value, byteOrder);
        }
        else
        {
            Memory.pokeLong(effectiveDirectAddress + index, value, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    public int getInt(final int index, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            return Memory.peekInt(byteArray, offset + index, byteOrder);
        }
        else
        {
            return Memory.peekInt(effectiveDirectAddress + index, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     * @param byteOrder of the value when written
     */
    public void putInt(final int index, final int value, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            Memory.pokeInt(byteArray, offset + index, value, byteOrder);
        }
        else
        {
            Memory.pokeInt(effectiveDirectAddress + index, value, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    public double getDouble(final int index, final ByteOrder byteOrder)
    {
        return Double.longBitsToDouble(getLong(index, byteOrder));
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     * @param byteOrder of the value when written.
     */
    public void putDouble(final int index, final double value, final ByteOrder byteOrder)
    {
        putLong(index, Double.doubleToRawLongBits(value), byteOrder);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    public float getFloat(final int index, final ByteOrder byteOrder)
    {
        return Float.intBitsToFloat(getInt(index, byteOrder));
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     * @param byteOrder of the value when written.
     */
    public void putFloat(final int index, final float value, final ByteOrder byteOrder)
    {
        putInt(index, Float.floatToIntBits(value), byteOrder);
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    public short getShort(final int index, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            return Memory.peekShort(byteArray, offset + index, byteOrder);
        }
        else
        {
            return Memory.peekShort(effectiveDirectAddress + index, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     * @param byteOrder of the value when written.
     */
    public void putShort(final int index, final short value, final ByteOrder byteOrder)
    {
        if (byteArray != null)
        {
            Memory.pokeShort(byteArray, offset + index, value, byteOrder);
        }
        else
        {
            Memory.pokeShort(effectiveDirectAddress + index, value, NATIVE_BYTE_ORDER != byteOrder);
        }
    }

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    public byte getByte(final int index)
    {
        if (byteArray != null)
        {
            return byteArray[offset + index];
        }
        else
        {
            return Memory.peekByte(effectiveDirectAddress + index);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    public void putByte(final int index, final byte value)
    {
        if (byteArray != null)
        {
            byteArray[offset + index] = value;
        }
        else
        {
            Memory.pokeByte(effectiveDirectAddress + index, value);
        }
    }

    /**
     * Get from the underlying buffer into a supplied byte array.
     * This method will try to fill the supplied byte array.
     *
     * @param index in the underlying buffer to start from.
     * @param dst   into which the dst will be copied.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final byte[] dst)
    {
        return getBytes(index, dst, 0, dst.length);
    }

    /**
     * Get bytes from the underlying buffer into a supplied byte array.
     *
     * @param index  in the underlying buffer to start from.
     * @param dst    into which the bytes will be copied.
     * @param offset in the supplied buffer to start the copy
     * @param length of the supplied buffer to use.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final byte[] dst, final int offset, final int length)
    {
        int count = Math.min(length, capacity - index);
        count = Math.min(count, dst.length - offset);
        Memory.memmove(dst, offset, theBuffer, this.offset + index, count);

        return count;
    }

    /**
     * Get bytes from the underlying buffer into a supplied DirectBuffer
     *
     * @param index  in the underlying buffer to start from.
     * @param dst    into which the bytes will be copied.
     * @param offset in the supplied buffer to start the copy
     * @param length of the supplied buffer to use.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final DirectBuffer dst, final int offset, final int length)
    {
        int count = Math.min(length, capacity - index);
        count = Math.min(count, dst.capacity - offset);
        Memory.memmove(dst.theBuffer, dst.offset + offset, theBuffer, this.offset + index, count);

        return count;
    }

    /**
     * Get from the underlying buffer into a supplied {@link ByteBuffer}.
     *
     * @param index     in the underlying buffer to start from.
     * @param dstBuffer into which the bytes will be copied.
     * @param length    of the supplied buffer to use.
     * @return count of bytes copied.
     */
    public int getBytes(final int index, final ByteBuffer dstBuffer, final int length)
    {
        int count = Math.min(dstBuffer.remaining(), capacity - index);
        count = Math.min(count, length);

        final int dstOffset;
        final Object theDstBuffer;
        if (dstBuffer.isDirect())
        {
            theDstBuffer = dstBuffer;
            dstOffset = dstBuffer.position();
        }
        else
        {
            theDstBuffer = dstBuffer.array();
            dstOffset = dstBuffer.arrayOffset() + dstBuffer.position();
        }

        Memory.memmove(theDstBuffer, dstOffset, theBuffer, this.offset + index, count);
        dstBuffer.position(dstBuffer.position() + count);

        return count;
    }

    /**
     * Put an array of src into the underlying buffer.
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
     * Put an array into the underlying buffer.
     *
     * @param index  in the underlying buffer to start from.
     * @param src    to be copied to the underlying buffer.
     * @param offset in the supplied buffer to begin the copy.
     * @param length of the supplied buffer to copy.
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final byte[] src, final int offset, final int length)
    {
        int count = Math.min(length, capacity - index);
        count = Math.min(count, src.length - offset);
        Memory.memmove(theBuffer, this.offset + index, src, offset, count);

        return count;
    }

    /**
     * Put bytes from a DirectBuffer into the underlying buffer.
     *
     * @param index  in the underlying buffer to start from.
     * @param src    to be copied to the underlying buffer.
     * @param offset in the supplied buffer to begin the copy.
     * @param length of the supplied buffer to copy.
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final DirectBuffer src, final int offset, final int length)
    {
        return src.getBytes(offset, this, index, length);
    }

    /**
     * Put an bytes into the underlying buffer for the view.  Bytes will be copied from current
     * {@link java.nio.ByteBuffer#position()} to {@link java.nio.ByteBuffer#limit()}.
     *
     * @param index     in the underlying buffer to start from.
     * @param srcBuffer to copy the bytes from.
     * @param length    of the source buffer in bytes to copy
     * @return count of bytes copied.
     */
    public int putBytes(final int index, final ByteBuffer srcBuffer, final int length)
    {
        int count = Math.min(srcBuffer.remaining(), capacity - index);
        count = Math.min(count, length);

        final int srcOffset;
        final Object theSrcBuffer;
        if (srcBuffer.isDirect())
        {
            theSrcBuffer = srcBuffer;
            srcOffset = srcBuffer.position();
        }
        else
        {
            theSrcBuffer = srcBuffer.array();
            srcOffset = srcBuffer.arrayOffset() + srcBuffer.position();
        }

        Memory.memmove(theBuffer, this.offset + index, theSrcBuffer, srcOffset, count);
        srcBuffer.position(srcBuffer.position() + count);

        return count;
    }
}
