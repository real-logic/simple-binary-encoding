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
     * Put a 16-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int16Put(final DirectBuffer buffer, final int index, final short value)
    {
        buffer.putShort(index, value);
    }

    /**
     * Put a 32-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int32Put(final DirectBuffer buffer, final int index, final int value)
    {
        buffer.putInt(index, value);
    }

    /**
     * Put a 64-bit integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void int64Put(final DirectBuffer buffer, final int index, final long value)
    {
        buffer.putLong(index, value);
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
        if (value < 0)
        {
            throw new IllegalArgumentException("Negative values are not allowed value=" + value);
        }

        buffer.putByte(index, (byte)(value & 0xFF));
    }

    /**
     * Put a 16-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @throws IllegalArgumentException if the number is negative
     */
    public static void uint16Put(final DirectBuffer buffer, final int index, final int value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("Negative values are not allowed value=" + value);
        }

        buffer.putShort(index, (short)(value & 0xFFFF));
    }

    /**
     * Put a 32-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @throws IllegalArgumentException if the number is negative
     */
    public static void uint32Put(final DirectBuffer buffer, final int index, final long value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("Negative values are not allowed value=" + value);
        }

        buffer.putInt(index, (int)(value & 0xFFFFFFFFL));
    }

    /**
     * Put a 64-bit signed integer to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     * @throws IllegalArgumentException if the number is negative
     */
    public static void uint64Put(final DirectBuffer buffer, final int index, final long value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("Negative values are not allowed value=" + value);
        }

        buffer.putLong(index, value);
    }

    /**
     * Put a float to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void floatPut(final DirectBuffer buffer, final int index, final float value)
    {
        buffer.putFloat(index, value);
    }

    /**
     * Put a double to a {@link DirectBuffer} at the given index.
     *
     * @param buffer to which the value should be written.
     * @param index from which to begin writing.
     * @param value to be be written.
     */
    public static void doublePut(final DirectBuffer buffer, final int index, final double value)
    {
        buffer.putDouble(index, value);
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
     * Get a 16-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the short representation of the value
     */
    public static short int16Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getShort(index);
    }

    /**
     * Get a 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the int representation of the value
     */
    public static int int32Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getInt(index);
    }

    /**
     * Get a 74-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long int64Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getLong(index);
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
     * @return the int representation of the value
     */
    public static int uint16Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getShort(index) & 0xFFFF;
    }

    /**
     * Get a unsigned 32-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long uint32Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getInt(index) & 0xFFFFFFFFL;
    }

    /**
     * Get a unsigned 64-bit integer from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the long representation of the value
     */
    public static long uint64Get(final DirectBuffer buffer, final int index)
    {
        final long value = buffer.getLong(index);
        if (value < 0)
        {
            throw new IllegalStateException("Value out of range: " + value);
        }

        return value;
    }

    /**
     * Get a 32-bit float from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the float representation of the value
     */
    public static float floatGet(final DirectBuffer buffer, final int index)
    {
        return buffer.getFloat(index);
    }

    /**
     * Get a 64-bit double from a {@link DirectBuffer} at a given index.
     *
     * @param buffer from which the value should be read.
     * @param index from which to begin reading.
     * @return the double representation of the value
     */
    public static double doubleGet(final DirectBuffer buffer, final int index)
    {
        return buffer.getDouble(index);
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
        return 0 != (buffer.getByte(index) | (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint8PutChoice(final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn)
    {
        byte bits = buffer.getByte(index);
        bits = (byte)(0xFF & (switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex)));
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
    public static boolean uint16GetChoice(final DirectBuffer buffer, final int index, final int bitIndex)
    {
        return 0 != (buffer.getShort(index) | (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint16PutChoice(final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn)
    {
        short bits = buffer.getShort(index);
        bits = (short)(0xFFFF & (switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex)));
        buffer.putShort(index, bits);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint32GetChoice(final DirectBuffer buffer, final int index, final int bitIndex)
    {
        return 0 != (buffer.getInt(index) | (1 << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint32PutChoice(final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn)
    {
        int bits = buffer.getInt(index);
        bits = switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex);
        buffer.putInt(index, bits);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer to read from.
     * @param index of the beginning byte
     * @param bitIndex bit index to read
     * @return true if the bit is set otherwise false.
     */
    public static boolean uint64GetChoice(final DirectBuffer buffer, final int index, final int bitIndex)
    {
        return 0 != (buffer.getLong(index) | (1L << bitIndex));
    }

    /**
     * Set a bit on or off at a given index.
     *
     * @param buffer to write the bit too.
     * @param index of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint64PutChoice(final DirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn)
    {
        long bits = buffer.getLong(index);
        bits = switchOn ? bits | (1L << bitIndex) : bits & ~(1L << bitIndex);
        buffer.putLong(index, bits);
    }
}
