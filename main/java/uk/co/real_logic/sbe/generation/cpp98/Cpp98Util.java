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
package uk.co.real_logic.sbe.generation.cpp98;

import uk.co.real_logic.sbe.PrimitiveType;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utilities for mapping between IR and the C++ language.
 */
public class Cpp98Util
{
    private static Map<PrimitiveType, String> typeNameByPrimitiveTypeMap = new EnumMap<>(PrimitiveType.class);

    static
    {
        typeNameByPrimitiveTypeMap.put(PrimitiveType.CHAR, "sbe_char_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT8, "sbe_int8_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT16, "sbe_int16_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT32, "sbe_int32_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT64, "sbe_int64_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT8, "sbe_uint8_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT16, "sbe_uint16_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT32, "sbe_uint32_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT64, "sbe_uint64_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.FLOAT, "sbe_float_t");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.DOUBLE, "sbe_double_t");
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a C++98 primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String cpp98TypeName(final PrimitiveType primitiveType)
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

    /**
     * Lowercase the first character of a given String.
     *
     * @param str to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toLowerFirstChar(final String str)
    {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Format a String as a property name.
     *
     * @param str to be formatted.
     * @return the string formatted as a property name.
     */
    public static String formatPropertyName(final String str)
    {
        return toLowerFirstChar(str);
    }

    /**
     * Format a String as a class name.
     *
     * @param str to be formatted.
     * @return the string formatted as a class name.
     */
    public static String formatClassName(final String str)
    {
        return toUpperFirstChar(str);
    }

    /**
     * Return the Cpp98 formatted byte order encoding string to use for a given byte order and primitiveType
     *
     * @param byteOrder of the {@link uk.co.real_logic.sbe.ir.Token}
     * @param primitiveType of the {@link uk.co.real_logic.sbe.ir.Token}
     * @return the string formatted as the byte ordering encoding
     */
    public static String formatByteOrderEncoding(final ByteOrder byteOrder, final PrimitiveType primitiveType)
    {
        switch (primitiveType.size())
        {
            case 2:
                return "SBE_" + byteOrder + "_ENCODE_16";

            case 4:
                return "SBE_" + byteOrder + "_ENCODE_32";

            case 8:
                return "SBE_" + byteOrder + "_ENCODE_64";

            default:
                return "";
        }
    }
}
