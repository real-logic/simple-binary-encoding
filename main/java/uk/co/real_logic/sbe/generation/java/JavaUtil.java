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

import uk.co.real_logic.sbe.PrimitiveType;

import java.util.EnumMap;
import java.util.Map;

public class JavaUtil
{
    private static Map<PrimitiveType, String> typeNameByPrimitiveTypeMap = new EnumMap<>(PrimitiveType.class);

    static
    {
        typeNameByPrimitiveTypeMap.put(PrimitiveType.CHAR, "byte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT8, "byte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT16, "short");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT32, "int");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT64, "long");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT8, "int");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT16, "int");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT32, "long");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT64, "long");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.FLOAT, "float");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.DOUBLE, "double");
    }

    /**
     * Map the name of a {@link PrimitiveType} to a Java primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String javaTypeFor(final PrimitiveType primitiveType)
    {
        return typeNameByPrimitiveTypeMap.get(primitiveType);
    }

    /**
     * Uppercase the first character of a given String.
     *
     * @param str to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toUpperFirstChar(final String str)
    {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);

    }

    public static void charPut(final DirectBuffer buffer, final int index, final byte value)
    {
        buffer.putByte(index, value);
    }

    public static void int8Put(final DirectBuffer buffer, final int index, final byte value)
    {
        buffer.putByte(index, value);
    }

    public static void int16Put(final DirectBuffer buffer, final int index, final short value)
    {
        buffer.putShort(index, value);
    }

    public static void int32Put(final DirectBuffer buffer, final int index, final int value)
    {
        buffer.putInt(index, value);
    }

    public static void int64Put(final DirectBuffer buffer, final int index, final long value)
    {
        buffer.putLong(index, value);
    }

    public static void uint8Put(final DirectBuffer buffer, final int index, final short value)
    {
        buffer.putByte(index, (byte)(value & 0xFF));
    }

    public static void uint16Put(final DirectBuffer buffer, final int index, final int value)
    {
        buffer.putShort(index, (short)(value & 0xFFFF));
    }

    public static void uint32Put(final DirectBuffer buffer, final int index, final long value)
    {
        buffer.putInt(index, (int)(value & 0xFFFFFFFFL));
    }

    public static void uint64Put(final DirectBuffer buffer, final int index, final long value)
    {
        buffer.putLong(index, value);
    }

    public static void floatPut(final DirectBuffer buffer, final int index, final float value)
    {
        buffer.putFloat(index, value);
    }

    public static void doublePut(final DirectBuffer buffer, final int index, final double value)
    {
        buffer.putDouble(index, value);
    }

    public static byte charGet(final DirectBuffer buffer, final int index)
    {
        return buffer.getByte(index);
    }

    public static byte int8Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getByte(index);
    }

    public static short int16Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getShort(index);
    }

    public static int int32Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getInt(index);
    }

    public static long int64Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getLong(index);
    }

    public static short uint8Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getByte(index);
    }

    public static int uint16Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getShort(index);
    }

    public static long uint32Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getInt(index);
    }

    public static long uint64Get(final DirectBuffer buffer, final int index)
    {
        return buffer.getLong(index);
    }

    public static float floatGet(final DirectBuffer buffer, final int index)
    {
        return buffer.getFloat(index);
    }

    public static double doubleGet(final DirectBuffer buffer, final int index)
    {
        return buffer.getDouble(index);
    }
}
