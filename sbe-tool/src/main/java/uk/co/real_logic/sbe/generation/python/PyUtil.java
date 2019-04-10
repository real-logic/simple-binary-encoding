/*
 * Copyright 2013-2019 Real Logic Ltd.
 * Copyright (C) 2017 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.python;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.ir.Token;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for mapping between IR and Python
 */
public class PyUtil
{
    public static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRING_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRUCT_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<PrimitiveType, Integer> PRIMITIVE_TYPE_SIZE_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    public static final Map<ByteOrder, String> BYTE_ORDER_STRING_MAP = new HashMap<>();
    public static final Pattern CAM_SNAKE_PATTERN = Pattern.compile("([a-z])([A-Z]+)");

    // Ref: https://docs.python.org/3.7/library/struct.html#format-characters
    static
    {
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.CHAR, "bytes");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.CHAR, "s");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.CHAR, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT8, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT8, "b");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT8, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT16, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT16, "h");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT16, 2);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT32, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT32, "i");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT32, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT64, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.INT64, "q");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.INT64, 8);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT8, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT8, "B");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT8, 1);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT16, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT16, "H");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT16, 2);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT32, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT32, "I");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT32, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT64, "int");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.UINT64, "Q");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.UINT64, 8);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.FLOAT, "float");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.FLOAT, "f");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.FLOAT, 4);
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.DOUBLE, "float");
        PRIMITIVE_TYPE_STRUCT_ENUM_MAP.put(PrimitiveType.DOUBLE, "d");
        PRIMITIVE_TYPE_SIZE_ENUM_MAP.put(PrimitiveType.DOUBLE, 8);
        BYTE_ORDER_STRING_MAP.put(ByteOrder.LITTLE_ENDIAN, "<");
        BYTE_ORDER_STRING_MAP.put(ByteOrder.BIG_ENDIAN, ">");
    }

    /**
     * Map the name of a {@link PrimitiveType} to a Python C primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Python struct primitive that most closely maps.
     */
    public static String pythonTypeName(final PrimitiveType primitiveType)
    {
        return PRIMITIVE_TYPE_STRING_ENUM_MAP.get(primitiveType);
    }

    /**
     * https://docs.python.org/3/library/struct.html
     * @param primitiveType SBE primitive type
     * @return A string representation for python c struct unpacking
     */
    public static String pythonTypeCode(final PrimitiveType primitiveType){
        return PRIMITIVE_TYPE_STRUCT_ENUM_MAP.get(primitiveType);
    }

    /**
     * https://docs.python.org/3/library/struct.html
     * @param byteOrder byte order for convertion from Java enum to python string
     * @return  python struct unpacking code for endianess
     */
    public static String pythonEndianCode(final ByteOrder byteOrder){
        return BYTE_ORDER_STRING_MAP.get(byteOrder);
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
        return toUpperFirstChar(str);
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
     * Converts CamelCase to snake_case, which is the recommended python case
     * @param str string to transform
     * @param toUpperFirst whether to make the first char upper case first
     * @return transformed string
     */
    public static String camToSnake(final String str, boolean toUpperFirst){
        if(toUpperFirst){
            return CAM_SNAKE_PATTERN.matcher(toUpperFirstChar(str)).replaceAll("$1_$2").toLowerCase();
        }
        return CAM_SNAKE_PATTERN.matcher(str).replaceAll("$1_$2").toLowerCase();
    }

    /**
     * @param str string to  transform
     * @return transformed strings  with toUpper applied
     */
    public static String camToSnake(final String str){
        return camToSnake(str, true);
    }

    /**
     * Generate the Pydoc comment header for a type.
     *
     * @param indent    level for the comment.
     * @param typeToken for the type.
     * @return a string representation of the Javadoc comment.
     */
    public static String generateTypePydoc(final String indent, final Token typeToken)
    {
        final String description = typeToken.description();
        if (null == description || description.isEmpty())
        {
            return "";
        }

        return
                indent + "\"\"\"\n" +
                        indent + " " + description + '\n' +
                        indent + "\"\"\"\n";
    }

    /**
     * Generate the Pydoc comment header for flyweight property.
     *
     * @param indent        level for the comment.
     * @param propertyToken for the property name.
     * @param typeName      for the property type.
     * @return a string representation of the Javadoc comment.
     */
    public static String generateFlyweightPropertyPydoc(
            final String indent, final Token propertyToken, final String typeName)
    {
        final String description = propertyToken.description();
        if (null == description || description.isEmpty())
        {
            return "";
        }

        return
                indent + "\"\"\"\n" +
                        indent + description + '\n' +
                        indent + "@return " + typeName + " : " + description + "\n" +
                        indent + " \"\"\"\n";
    }

    /**
     * Generate the Javadoc comment header for group encode property.
     *
     * @param indent        level for the comment.
     * @param propertyToken for the property name.
     * @param typeName      for the property type.
     * @return a string representation of the Javadoc comment.
     */
    public static String generateGroupEncodePropertyPydoc(
            final String indent, final Token propertyToken, final String typeName)
    {
        final String description = propertyToken.description();
        if (null == description || description.isEmpty())
        {
            return "";
        }

        return
                indent + "/**\n" +
                        indent + " * " + description + "\n" +
                        indent + " *\n" +
                        indent + " * @param count of times the group will be encoded\n" +
                        indent + " * @return " + typeName + " : encoder for the group\n" +
                        indent + " */\n";
    }
}
