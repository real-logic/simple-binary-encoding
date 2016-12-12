/*
 * Copyright (C) 2016 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.golang;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.util.ValidationUtil;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utilities for mapping between IR and the Golang language.
 */
public class GolangUtil
{
    private static Map<PrimitiveType, String> typeNameByPrimitiveTypeMap = new EnumMap<>(PrimitiveType.class);

    static
    {
        typeNameByPrimitiveTypeMap.put(PrimitiveType.CHAR, "byte");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT8, "int8");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT16, "int16");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT32, "int32");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.INT64, "int64");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT8, "uint8");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT16, "uint16");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT32, "uint32");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.UINT64, "uint64");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.FLOAT, "float32");
        typeNameByPrimitiveTypeMap.put(PrimitiveType.DOUBLE, "float64");
    }


    /* FIXME: Share across languages */
    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a Golang primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String golangTypeName(final PrimitiveType primitiveType)
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
     * @param value to be formatted.
     * @return the string formatted as a property name.
     */
    public static String formatPropertyName(final String value)
    {
        String formattedValue = toUpperFirstChar(value);

        if (ValidationUtil.isGolangKeyword(formattedValue))
        {
            final String keywordAppendToken = System.getProperty(SbeTool.KEYWORD_APPEND_TOKEN);
            if (null == keywordAppendToken)
            {
                throw new IllegalStateException(
                    "Invalid property name='" + formattedValue +
                    "' please correct the schema or consider setting system property: " + SbeTool.KEYWORD_APPEND_TOKEN);
            }

            formattedValue += keywordAppendToken;
        }

        return formattedValue;
    }

    /**
     * Format a String as a type name.
     *
     * @param value to be formatted.
     * @return the string formatted as an exported type name.
     */
    public static String formatTypeName(final String value)
    {
        return toUpperFirstChar(value);
    }

    /**
     * Return the Golang formatted byte order encoding string to use for a given byte order and primitiveType
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
                return "binary.Write(buf, order, obj)";

            case 4:
                return "binary.Write(buf, order, obj)";

            case 8:
                return "binary.Write(buf, order, obj)";

            default:
                return "";
        }
    }

    public static String closingBraces(final int count)
    {
        return new String(new char[count]).replace("\0", "}\n");
    }
}
