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

import java.nio.ByteOrder;

public class CodecUtil
{
    /**
     * Put a character to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void charPut(final DirectBuffer buffer, final int index, final byte value)
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
    public static void charsPut(
        final DirectBuffer buffer, final int index, final byte[] src, final int offset, final int length)
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
    public static void int8Put(final DirectBuffer buffer, final int index, final byte value)
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
    public static void int8sPut(
        final DirectBuffer buffer, final int index, final byte[] src, final int offset, final int length)
    {
        buffer.putBytes(index, src, offset, length);
    }

    /**
     * Put a 16-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void int16Put(final DirectBuffer buffer, final int index, final short value, final ByteOrder byteOrder)
    {
        buffer.putShort(index, value, byteOrder);
    }

    /**
     * Put a 32-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void int32Put(final DirectBuffer buffer, final int index, final int value, final ByteOrder byteOrder)
    {
        buffer.putInt(index, value, byteOrder);
    }

    /**
     * Put a 64-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void int64Put(final DirectBuffer buffer, final int index, final long value, final ByteOrder byteOrder)
    {
        buffer.putLong(index, value, byteOrder);
    }

    /**
     * Put a 8-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @throws IllegalArgumentException if the number is negative
     */
    public static void uint8Put(final DirectBuffer buffer, final int index, final short value)
    {
        buffer.putByte(index, (byte)value);
    }

    /**
     * Put a 16-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void uint16Put(final DirectBuffer buffer, final int index, final int value, final ByteOrder byteOrder)
    {
        buffer.putShort(index, (short)value, byteOrder);
    }

    /**
     * Put a 32-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void uint32Put(final DirectBuffer buffer, final int index, final long value, final ByteOrder byteOrder)
    {
        buffer.putInt(index, (int)value, byteOrder);
    }

    /**
     * Put a 64-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void uint64Put(final DirectBuffer buffer, final int index, final long value, final ByteOrder byteOrder)
    {
        buffer.putLong(index, value, byteOrder);
    }

    /**
     * Put a float to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void floatPut(final DirectBuffer buffer, final int index, final float value, final ByteOrder byteOrder)
    {
        buffer.putFloat(index, value, byteOrder);
    }

    /**
     * Put a double to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @param byteOrder for the buffer encoding
     */
    public static void doublePut(final DirectBuffer buffer, final int index, final double value, final ByteOrder byteOrder)
    {
        buffer.putDouble(index, value, byteOrder);
    }

    /**
     * Get a char from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the byte representation of the value
     */
    public static byte charGet(final DirectBuffer buffer, final int index)
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
    public static void charsGet(final DirectBuffer buffer, final int index, final byte[] dst, final int offset, final int length)
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
    public static byte int8Get(final DirectBuffer buffer, final int index)
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
    public static void int8sGet(final DirectBuffer buffer, final int index, final byte[] dst, final int offset, final int length)
    {
        buffer.getBytes(index, dst, offset, length);
    }

    /**
     * Get a 16-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the short representation of the value
     */
    public static short int16Get(final DirectBuffer buffer, final int index,  ByteOrder byteOrder)
    {
        return buffer.getShort(index, byteOrder);
    }

    /**
     * Get a 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the int representation of the value
     */
    public static int int32Get(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getInt(index, byteOrder);
    }

    /**
     * Get a 64-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the long representation of the value
     */
    public static long int64Get(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getLong(index, byteOrder);
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
     * Get a unsigned 16-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the int representation of the value
     */
    public static int uint16Get(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getShort(index, byteOrder) & 0xFFFF;
    }

    /**
     * Get a unsigned 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the long representation of the value
     */
    public static long uint32Get(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getInt(index, byteOrder) & 0xFFFF_FFFFL;
    }

    /**
     * Get a unsigned 64-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the long representation of the value
     */
    public static long uint64Get(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getLong(index, byteOrder);
    }

    /**
     * Get a 32-bit float from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the float representation of the value
     */
    public static float floatGet(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getFloat(index, byteOrder);
    }

    /**
     * Get a 64-bit double from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @param byteOrder for the buffer encoding
     * @return the double representation of the value
     */
    public static double doubleGet(final DirectBuffer buffer, final int index, final ByteOrder byteOrder)
    {
        return buffer.getDouble(index, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint8GetChoice(final DirectBuffer buffer, final int index, final int bitIndex)
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
    public static void uint8PutChoice(final DirectBuffer buffer, final int index, final int bitIndex, boolean switchOn)
    {
        byte bits = buffer.getByte(index);
        bits = (byte)(switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex));
        buffer.putByte(index, bits);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @param byteOrder for the buffer encoding
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint16GetChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final ByteOrder byteOrder)
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
     * @param byteOrder for the buffer encoding
     */
    public static void uint16PutChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
    {
        short bits = buffer.getShort(index, byteOrder);
        bits = (short)(switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex));
        buffer.putShort(index, bits, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @param byteOrder for the buffer encoding
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint32GetChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final ByteOrder byteOrder)
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
     * @param byteOrder for the buffer encoding
     */
    public static void uint32PutChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
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
     * @param byteOrder for the buffer encoding
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint64GetChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final ByteOrder byteOrder)
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
     * @param byteOrder for the buffer encoding
     */
    public static void uint64PutChoice(
        final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
    {
        long bits = buffer.getLong(index, byteOrder);
        bits = switchOn ? bits | (1L << bitIndex) : bits & ~(1L << bitIndex);
        buffer.putLong(index, bits, byteOrder);
    }
}
