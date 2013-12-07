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
package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.PrimitiveType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utilities for mapping between IR and the C# language.
 */
public class CSharpUtil
{
    static Map<PrimitiveType, String> typeNameByPrimitiveTypeMap = new EnumMap<>(PrimitiveType.class);

    /**
     * http://msdn.microsoft.com/en-us/library/ms228360(v=vs.90).aspx
     */
    static
    {
        typeNameByPrimitiveTypeMap.put(PrimitiveType.CHAR, "byte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT8, "sbyte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT16, "short");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT32, "int");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT64, "long");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT8, "byte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT16, "ushort");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT32, "uint");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT64, "ulong");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.FLOAT, "float");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.DOUBLE, "double");
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a C# primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String cSharpTypeName(final PrimitiveType primitiveType)
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
}
