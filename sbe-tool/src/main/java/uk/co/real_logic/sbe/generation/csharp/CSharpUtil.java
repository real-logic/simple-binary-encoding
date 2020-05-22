/*
 * Copyright 2013-2020 Real Logic Limited.
 * Copyright (C) 2017 MarketFactory, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;

/**
 * Utilities for mapping between IR and the C# language.
 */
public class CSharpUtil
{
    private static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRING_ENUM_MAP = new EnumMap<>(PrimitiveType.class);

    /*
     * http://msdn.microsoft.com/en-us/library/ms228360(v=vs.90).aspx
     */
    static
    {
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.CHAR, "byte");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT8, "sbyte");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT16, "short");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT32, "int");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT64, "long");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT8, "byte");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT16, "ushort");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT32, "uint");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT64, "ulong");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.FLOAT, "float");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.DOUBLE, "double");
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a C# primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String cSharpTypeName(final PrimitiveType primitiveType)
    {
        return PRIMITIVE_TYPE_STRING_ENUM_MAP.get(primitiveType);
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
