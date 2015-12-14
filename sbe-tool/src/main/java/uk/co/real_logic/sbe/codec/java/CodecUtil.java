/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.MutableDirectBuffer;

import java.nio.ByteOrder;

public class CodecUtil
{
    /**
     * Is a bit set at a given index.
     *
     * @param buffer   to read from.
     * @param index    of the beginning byte
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
     * @param buffer   to write the bit too.
     * @param index    of the beginning byte.
     * @param bitIndex bit index to set.
     * @param switchOn true sets bit to 1 and false sets it to 0.
     */
    public static void uint8PutChoice(final MutableDirectBuffer buffer, final int index, final int bitIndex, boolean switchOn)
    {
        byte bits = buffer.getByte(index);
        bits = (byte)(switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex));
        buffer.putByte(index, bits);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer    to read from.
     * @param index     of the beginning byte
     * @param bitIndex  bit index to read
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
     * @param buffer    to write the bit too.
     * @param index     of the beginning byte.
     * @param bitIndex  bit index to set.
     * @param switchOn  true sets bit to 1 and false sets it to 0.
     * @param byteOrder for the buffer encoding
     */
    public static void uint16PutChoice(
        final MutableDirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
    {
        short bits = buffer.getShort(index, byteOrder);
        bits = (short)(switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex));
        buffer.putShort(index, bits, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer    to read from.
     * @param index     of the beginning byte
     * @param bitIndex  bit index to read
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
     * @param buffer    to write the bit too.
     * @param index     of the beginning byte.
     * @param bitIndex  bit index to set.
     * @param switchOn  true sets bit to 1 and false sets it to 0.
     * @param byteOrder for the buffer encoding
     */
    public static void uint32PutChoice(
        final MutableDirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
    {
        int bits = buffer.getInt(index, byteOrder);
        bits = switchOn ? bits | (1 << bitIndex) : bits & ~(1 << bitIndex);
        buffer.putInt(index, bits, byteOrder);
    }

    /**
     * Is a bit set at a given index.
     *
     * @param buffer    to read from.
     * @param index     of the beginning byte
     * @param bitIndex  bit index to read
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
     * @param buffer    to write the bit too.
     * @param index     of the beginning byte.
     * @param bitIndex  bit index to set.
     * @param switchOn  true sets bit to 1 and false sets it to 0.
     * @param byteOrder for the buffer encoding
     */
    public static void uint64PutChoice(
        final MutableDirectBuffer buffer, final int index, final int bitIndex, final boolean switchOn, final ByteOrder byteOrder)
    {
        long bits = buffer.getLong(index, byteOrder);
        bits = switchOn ? bits | (1L << bitIndex) : bits & ~(1L << bitIndex);
        buffer.putLong(index, bits, byteOrder);
    }
}
