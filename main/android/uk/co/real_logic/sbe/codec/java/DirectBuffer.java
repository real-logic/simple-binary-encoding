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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import android.os.MemoryFile;
import libcore.io.Memory;

/**
 * Direct buffer which can wrap a byte[] or a {@link ByteBuffer} that is heap or direct allocated
 * for direct access with native types.
 */
public final class DirectBuffer
{
    private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();
    private static final MemoryAccess MEMORY_ACCESS = BitUtil.getMemoryAccess();

    private byte[] byteArray;
    private long effectiveDirectAddress;
    private int offset;
    private int capacity;

    private ByteBuffer byteBuffer;
    //we keep this reference to avoid being cleaned by GC
    @SuppressWarnings("unused")
    private MemoryFile memoryFile;

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
     * Attach a view to an off-heap memory region by address.
     *
     * <b>This constructor is not available under Android.</b>
     *
     * @param address  where the memory begins off-heap
     * @param capacity of the buffer from the given address
     */
    public DirectBuffer(final long address, final int capacity)
    {
        wrap(address, capacity);
    }

    /**
     * Attach a view to a {@link MemoryFile} for providing direct access.
     *
     * @param memoryFile to which the view is attached.
     */
    public DirectBuffer(final MemoryFile memoryFile)
    {
        wrap(memoryFile);
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
        memoryFile = null;
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

        if (buffer.hasArray())
        {
            byteArray = buffer.array();
            offset = buffer.arrayOffset();
            effectiveDirectAddress = 0;
        }
        else
        {
            // ByteArray is null only for memory mapped buffers
            // and buffers allocated inside JNI.
            // Performance seems to be lower for this situation
            byteArray = null;
            offset = 0;
            effectiveDirectAddress = BitUtil.getEffectiveDirectAddress(buffer);
        }
        memoryFile = null;
        capacity = buffer.capacity();
    }

    /**
     * Attach a view to an off-heap memory region by address.
     *
     * <b>This method is not available under Android.</b>
     *
     * @param address  where the memory begins off-heap
     * @param capacity of the buffer from the given address
     */
    public void wrap(final long address, final int capacity)
    {
        effectiveDirectAddress = address;
        this.capacity = capacity;
        byteArray = null;
        //Memory.memmove needs either a bytebuffer or a bytearray
        //it could only work with memory addresses, but it doesn't
        byteBuffer = BitUtil.newDirectByteBuffer(effectiveDirectAddress, this.capacity);
        memoryFile = null;
    }

    /**
     * Attach a view to a {@link MemoryFile} for providing direct access.
     *
     * @param memoryFile to which the view is attached.
     */
    public void wrap(final MemoryFile memoryFile)
    {
        wrap(BitUtil.getMemoryFileAddress(memoryFile), memoryFile.length());
        this.memoryFile = memoryFile;
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
            final String msg = String.format(
                Locale.US,
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
     * @param index     in bytes from which to get.
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            return MEMORY_ACCESS.peekLong(address, swapBytes);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     for at a given index
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            MEMORY_ACCESS.pokeLong(address, value, swapBytes);
        }
    }

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            return MEMORY_ACCESS.peekInt(address, swapBytes);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            MEMORY_ACCESS.pokeInt(address, value, swapBytes);
        }
    }

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
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
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    public void putDouble(final int index, final double value, final ByteOrder byteOrder)
    {
        putLong(index, Double.doubleToRawLongBits(value), byteOrder);
    }

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
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
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    public void putFloat(final int index, final float value, final ByteOrder byteOrder)
    {
        putInt(index, Float.floatToIntBits(value), byteOrder);
    }

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            return MEMORY_ACCESS.peekShort(address, swapBytes);
        }
    }

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
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
            final long address = effectiveDirectAddress + index;
            final boolean swapBytes = NATIVE_BYTE_ORDER != byteOrder;
            MEMORY_ACCESS.pokeShort(address, value, swapBytes);
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
            final long address = effectiveDirectAddress + index;
            return MEMORY_ACCESS.peekByte(address);
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
            final long address = effectiveDirectAddress + index;
            MEMORY_ACCESS.pokeByte(address, value);
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

        if (byteArray != null)
        {
            System.arraycopy(byteArray, this.offset + index, dst, offset, count);
        }
        else
        {
            final long address = effectiveDirectAddress + index;
            MEMORY_ACCESS.peekByteArray(address, dst, offset, count);
        }

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

        if (byteArray != null)
        {
            if (dst.byteArray != null)
            {
                System.arraycopy(byteArray, this.offset + index, dst.byteArray, dst.offset + offset, count);
            }
            else
            {
                final long address = dst.effectiveDirectAddress + offset;
                MEMORY_ACCESS.pokeByteArray(address, byteArray, this.offset + index, count);
            }
        }
        else
        {
            if (dst.byteArray != null)
            {
                final long address = effectiveDirectAddress + index;
                MEMORY_ACCESS.peekByteArray(address, dst.byteArray, dst.offset + offset, count);
            }
            else
            {
                Memory.memmove(dst.byteBuffer, dst.offset + offset, byteBuffer, this.offset + index, count);
            }
        }

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
        int count = Math.min(length, capacity - index);
        count = Math.min(count, dstBuffer.remaining());

        if (byteArray != null)
        {
            getBytesFromByteArray(index, dstBuffer, count);
        }
        else
        {
            getBytesFromMemory(index, dstBuffer, count);
        }

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

        if (byteArray != null)
        {
            System.arraycopy(src, offset, byteArray, this.offset + index, count);
        }
        else
        {
            final long address = effectiveDirectAddress + index;
            MEMORY_ACCESS.pokeByteArray(address, src, offset, count);
        }

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
        int count = Math.min(length, capacity - index);
        count = Math.min(count, srcBuffer.remaining());

        if (byteArray != null)
        {
            putBytesToByteArray(index, srcBuffer, count);
        }
        else
        {
            putBytesToMemory(index, srcBuffer, count);
        }

        srcBuffer.position(srcBuffer.position() + count);

        return count;
    }

    private void getBytesFromMemory(final int index, final ByteBuffer dstBuffer, final int count)
    {
        if (dstBuffer.hasArray())
        {
            final byte[] dst = dstBuffer.array();
            final int dstOffset = dstBuffer.arrayOffset() + dstBuffer.position();
            final long address = effectiveDirectAddress + index;
            MEMORY_ACCESS.peekByteArray(address, dst, dstOffset, count);
        }
        else
        {
            Memory.memmove(dstBuffer, dstBuffer.position(), byteBuffer, index, count);
        }
    }

    private void putBytesToMemory(final int index, final ByteBuffer srcBuffer, final int count)
    {
        if (srcBuffer.hasArray())
        {
            final byte[] src = srcBuffer.array();
            final int srcOffset = srcBuffer.arrayOffset() + srcBuffer.position();
            final long address = effectiveDirectAddress + index;
            MEMORY_ACCESS.pokeByteArray(address, src, srcOffset, count);
        }
        else
        {
            Memory.memmove(byteBuffer, index, srcBuffer, srcBuffer.position(), count);
        }
    }

    private void getBytesFromByteArray(final int index, final ByteBuffer dstBuffer, final int count)
    {
        if (dstBuffer.hasArray())
        {
            final byte[] dst = dstBuffer.array();
            final int dstOffset = dstBuffer.arrayOffset() + dstBuffer.position();
            System.arraycopy(byteArray, this.offset + index, dst, dstOffset, count);
        }
        else
        {
            final long address = BitUtil.getEffectiveDirectAddress(dstBuffer) + dstBuffer.position();
            MEMORY_ACCESS.pokeByteArray(address, byteArray, offset + index, count);
        }
    }

    private void putBytesToByteArray(final int index, final ByteBuffer srcBuffer, final int count)
    {
        if (srcBuffer.hasArray())
        {
            final byte[] src = srcBuffer.array();
            final int srcOffset = srcBuffer.arrayOffset() + srcBuffer.position();
            System.arraycopy(src, srcOffset, byteArray, this.offset + index, count);
        }
        else
        {
            final long address = BitUtil.getEffectiveDirectAddress(srcBuffer) + srcBuffer.position();
            MEMORY_ACCESS.peekByteArray(address, byteArray, offset + index, count);
        }
    }
}
