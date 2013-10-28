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

import java.nio.ByteOrder;

import static uk.co.real_logic.sbe.util.BitUtil.*;

public class CodecUtil
{
    /**
     * Check that a given position is within the capacity of a buffer from a given offset.
     *
     * @param position access is required to.
     * @param offset from which the position is added.
     * @param capacity of the underlying buffer.
     */
    public static void checkPosition(final int position,
                                     final int offset,
                                     final int capacity)
    {
        if ((offset + position) >= capacity)
        {
            final String msg = String.format("position=%d is beyond capacity=%d with offset=%d",
                                             Integer.valueOf(position),
                                             Integer.valueOf(capacity),
                                             Integer.valueOf(offset));

            throw new IllegalStateException(msg);
        }
    }

    /**
     * Put a character to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void charPut(final DirectBuffer buffer,
                               final int index,
                               final byte value)
    {
        buffer.putByte(index, value);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void charsPut(final DirectBuffer buffer,
                                final int index,
                                final byte[] src,
                                final int offset,
                                final int length)
    {
        buffer.putBytes(index, src, offset, length);
    }

    /**
     * Put a 8-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int8sPut(final DirectBuffer buffer,
                                final int index,
                                final byte value)
    {
        buffer.putByte(index, value);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void int8sPut(final DirectBuffer buffer,
                                final int index,
                                final byte[] src,
                                final int offset,
                                final int length)
    {
        buffer.putBytes(index, src, offset, length);
    }

    /**
     * Put a 16-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int16Put(final DirectBuffer buffer,
                                final int index,
                                final short value,
                                final ByteOrder byteOrder)
    {
        buffer.putShort(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void int16sPut(final DirectBuffer buffer,
                                 final int index,
                                 final short[] src,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putShort(index + (i * SIZE_OF_SHORT), src[offset + i], byteOrder);
        }
    }

    /**
     * Put a 32-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int32Put(final DirectBuffer buffer,
                                final int index,
                                final int value,
                                final ByteOrder byteOrder)
    {
        buffer.putInt(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void int32sPut(final DirectBuffer buffer,
                                 final int index,
                                 final int[] src,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putInt(index + (i * SIZE_OF_INT), src[offset + i], byteOrder);
        }
    }

    /**
     * Put a 64-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int64Put(final DirectBuffer buffer,
                                final int index,
                                final long value,
                                final ByteOrder byteOrder)
    {
        buffer.putLong(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void int64sPut(final DirectBuffer buffer,
                                 final int index,
                                 final long[] src,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putLong(index + (i * SIZE_OF_LONG), src[offset + i], byteOrder);
        }
    }

    /**
     * Put a 8-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @throws IllegalArgumentException if the number is negative
     */
    public static void uint8Put(final DirectBuffer buffer,
                                final int index,
                                final short value)
    {
        buffer.putByte(index, (byte)value);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void uint8sPut(final DirectBuffer buffer,
                                 final int index,
                                 final short[] src,
                                 final int offset,
                                 final int length)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putByte(index + i, (byte)src[offset + i]);
        }
    }

    /**
     * Put a 16-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void uint16Put(final DirectBuffer buffer,
                                 final int index,
                                 final int value,
                                 final ByteOrder byteOrder)
    {
        buffer.putShort(index, (short)value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void uint16sPut(final DirectBuffer buffer,
                                  final int index,
                                  final int[] src,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putShort(index + (i * SIZE_OF_SHORT), (short)src[offset + i], byteOrder);
        }
    }

    /**
     * Put a 32-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void uint32Put(final DirectBuffer buffer,
                                 final int index,
                                 final long value,
                                 final ByteOrder byteOrder)
    {
        buffer.putInt(index, (int)value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void uint32sPut(final DirectBuffer buffer,
                                  final int index,
                                  final long[] src,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putInt(index + (i * SIZE_OF_INT), (int)src[offset + i], byteOrder);
        }
    }

    /**
     * Put a 64-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void uint64Put(final DirectBuffer buffer,
                                 final int index,
                                 final long value,
                                 final ByteOrder byteOrder)
    {
        buffer.putLong(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void uint64sPut(final DirectBuffer buffer,
                                  final int index,
                                  final long[] src,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putLong(index + (i * SIZE_OF_LONG), src[offset + i], byteOrder);
        }
    }

    /**
     * Put a float to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void floatPut(final DirectBuffer buffer,
                                final int index,
                                final float value,
                                final ByteOrder byteOrder)
    {
        buffer.putFloat(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void floatsPut(final DirectBuffer buffer,
                                 final int index,
                                 final float[] src,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putFloat(index + (i * SIZE_OF_FLOAT), src[offset + i], byteOrder);
        }
    }

    /**
     * Put a double to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void doublePut(final DirectBuffer buffer,
                                 final int index,
                                 final double value,
                                 final ByteOrder byteOrder)
    {
        buffer.putDouble(index, value, byteOrder);
    }

    /**
     * Put an array into a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param src to be be written
     * @param offset in the src buffer to write from
     * @param length of the src buffer to copy.
     */
    public static void doublesPut(final DirectBuffer buffer,
                                  final int index,
                                  final double[] src,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            buffer.putDouble(index + (i * SIZE_OF_DOUBLE), src[offset + i], byteOrder);
        }
    }

    /**
     * Get a char from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the byte representation of the value
     */
    public static byte charGet(final DirectBuffer buffer,
                               final int index)
    {
        return buffer.getByte(index);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void charsGet(final DirectBuffer buffer,
                                final int index,
                                final byte[] dst,
                                final int offset,
                                final int length)
    {
        buffer.getBytes(index, dst, offset, length);
    }

    /**
     * Get a 8-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the byte representation of the value
     */
    public static byte int8Get(final DirectBuffer buffer,
                               final int index)
    {
        return buffer.getByte(index);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void int8sGet(final DirectBuffer buffer,
                                final int index,
                                final byte[] dst,
                                final int offset,
                                final int length)
    {
        buffer.getBytes(index, dst, offset, length);
    }

    /**
     * Get a 16-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the short representation of the value
     */
    public static short int16Get(final DirectBuffer buffer,
                                 final int index,
                                 final ByteOrder byteOrder)
    {
        return buffer.getShort(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void int16sGet(final DirectBuffer buffer,
                                 final int index,
                                 final short[] dst,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getShort(index + (i * SIZE_OF_SHORT), byteOrder);
        }
    }

    /**
     * Get a 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the int representation of the value
     */
    public static int int32Get(final DirectBuffer buffer,
                               final int index,
                               final ByteOrder byteOrder)
    {
        return buffer.getInt(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void int32sGet(final DirectBuffer buffer,
                                 final int index,
                                 final int[] dst,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getInt(index + (i * SIZE_OF_INT), byteOrder);
        }
    }

    /**
     * Get a 64-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long int64Get(final DirectBuffer buffer,
                                final int index,
                                final ByteOrder byteOrder)
    {
        return buffer.getLong(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void int64sGet(final DirectBuffer buffer,
                                 final int index,
                                 final long[] dst,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getLong(index + (i * SIZE_OF_LONG), byteOrder);
        }
    }

    /**
     * Get a unsigned 8-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the short representation of the value
     */
    public static short uint8Get(final DirectBuffer buffer, final int index)
    {
        return (short)(buffer.getByte(index) & 0xFF);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void uint8sGet(final DirectBuffer buffer,
                                 final int index,
                                 final short[] dst,
                                 final int offset,
                                 final int length)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = (short)(buffer.getByte(index + i) & 0xFF);
        }
    }

    /**
     * Get a unsigned 16-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the int representation of the value
     */
    public static int uint16Get(final DirectBuffer buffer,
                                final int index,
                                final ByteOrder byteOrder)
    {
        return buffer.getShort(index, byteOrder) & 0xFFFF;
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void uint16sGet(final DirectBuffer buffer,
                                  final int index,
                                  final int[] dst,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = (short)(buffer.getShort(index + (i * SIZE_OF_SHORT), byteOrder) & 0xFFFF);
        }
    }

    /**
     * Get a unsigned 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long uint32Get(final DirectBuffer buffer,
                                 final int index,
                                 final ByteOrder byteOrder)
    {
        return buffer.getInt(index, byteOrder) & 0xFFFF_FFFFL;
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void uint32sGet(final DirectBuffer buffer,
                                  final int index,
                                  final long[] dst,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getInt(index + (i * SIZE_OF_INT), byteOrder) & 0xFFFF_FFFFL;
        }
    }

    /**
     * Get a unsigned 64-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long uint64Get(final DirectBuffer buffer,
                                 final int index,
                                 final ByteOrder byteOrder)
    {
        return buffer.getLong(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void uint64sGet(final DirectBuffer buffer,
                                  final int index,
                                  final long[] dst,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getLong(index + (i * SIZE_OF_LONG), byteOrder);
        }
    }

    /**
     * Get a 32-bit float from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the float representation of the value
     */
    public static float floatGet(final DirectBuffer buffer,
                                 final int index,
                                 final ByteOrder byteOrder)
    {
        return buffer.getFloat(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void floatsGet(final DirectBuffer buffer,
                                 final int index,
                                 final float[] dst,
                                 final int offset,
                                 final int length,
                                 final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getFloat(index + (i * SIZE_OF_FLOAT), byteOrder);
        }
    }

    /**
     * Get a 64-bit double from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the double representation of the value
     */
    public static double doubleGet(final DirectBuffer buffer,
                                   final int index,
                                   final ByteOrder byteOrder)
    {
        return buffer.getDouble(index, byteOrder);
    }

    /**
     * Get from a {@link DirectBuffer} at a given index into an array.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param dst into which the copy will occur
     * @param offset at which to start in the destination
     * @param length of the array to copy
     */
    public static void doublesGet(final DirectBuffer buffer,
                                  final int index,
                                  final double[] dst,
                                  final int offset,
                                  final int length,
                                  final ByteOrder byteOrder)
    {
        for (int i = 0; i < length; i++)
        {
            dst[offset + i] = buffer.getDouble(index + (i * SIZE_OF_DOUBLE), byteOrder);
        }
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint8GetChoice(final DirectBuffer buffer,
                                         final int index,
                                         final int bitIndex)
    {
        return 0 != (buffer.getByte(index) & (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint8PutChoice(final DirectBuffer buffer,
                                      final int index,
                                      final int bitIndex,
                                      final boolean switchOn)
    {
        byte bits = buffer.getByte(index);
        bits = (byte)((switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex)));
        buffer.putByte(index, bits);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint16GetChoice(final DirectBuffer buffer,
                                          final int index,
                                          final int bitIndex,
                                          final ByteOrder byteOrder)
    {
        return 0 != (buffer.getShort(index, byteOrder) & (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint16PutChoice(final DirectBuffer buffer,
                                       final int index,
                                       final int bitIndex,
                                       final boolean switchOn,
                                       final ByteOrder byteOrder)
    {
        short bits = buffer.getShort(index, byteOrder);
        bits = (short)((switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex)));
        buffer.putShort(index, bits, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint32GetChoice(final DirectBuffer buffer,
                                          final int index,
                                          final int bitIndex,
                                          final ByteOrder byteOrder)
    {
        return 0 != (buffer.getInt(index, byteOrder) & (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint32PutChoice(final DirectBuffer buffer,
                                       final int index,
                                       final int bitIndex,
                                       final boolean switchOn,
                                       final ByteOrder byteOrder)
    {
        int bits = buffer.getInt(index, byteOrder);
        bits = switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex);
        buffer.putInt(index, bits, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint64GetChoice(final DirectBuffer buffer,
                                          final int index,
                                          final int bitIndex,
                                          final ByteOrder byteOrder)
    {
        return 0 != (buffer.getLong(index, byteOrder) & (1L << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint64PutChoice(final DirectBuffer buffer,
                                       final int index,
                                       final int bitIndex,
                                       final boolean switchOn,
                                       final ByteOrder byteOrder)
    {
        long bits = buffer.getLong(index, byteOrder);
        bits = switchOn ? bits | (1L << bitIndex) : bits & ~(1L << bitIndex);
        buffer.putLong(index, bits, byteOrder);
    }
}
