/*
 * Copyright 2013-2025 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation.golang.flyweight;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.ValidationUtil;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;

/**
 * Utilities for mapping between IR and the Golang language.
 */
public class GolangFlyweightUtil
{
    private static final Map<PrimitiveType, String> PRIMITIVE_TYPE_STRING_ENUM_MAP = new EnumMap<>(PrimitiveType.class);
    private static final Map<PrimitiveType, String> MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP =
        new EnumMap<>(PrimitiveType.class);

    static
    {
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.CHAR, "byte");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT8, "int8");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT16, "int16");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT32, "int32");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.INT64, "int64");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT8, "uint8");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT16, "uint16");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT32, "uint32");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.UINT64, "uint64");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.FLOAT, "float32");
        PRIMITIVE_TYPE_STRING_ENUM_MAP.put(PrimitiveType.DOUBLE, "float64");
    }

    static
    {
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.CHAR, "Bytes");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT8, "Int8");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT16, "Int16");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT32, "Int32");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT64, "Int64");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT8, "Uint8");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT16, "Uint16");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT32, "Uint32");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT64, "Uint64");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.FLOAT, "Float32");
        MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.DOUBLE, "Float64");
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a Golang primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String goTypeName(final PrimitiveType primitiveType)
    {
        return PRIMITIVE_TYPE_STRING_ENUM_MAP.get(primitiveType);
    }

    /**
     * Format a String as a class name.
     *
     * @param value to be formatted.
     * @return the string formatted as a class name.
     */
    public static String formatClassName(final String value)
    {
        return toUpperFirstChar(value);
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a Golang marshalling function name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String golangMarshalType(final PrimitiveType primitiveType)
    {
        return MARSHAL_TYPE_BY_PRIMITIVE_TYPE_MAP.get(primitiveType);
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
                    "' please correct the schema or consider setting system property: " +
                    SbeTool.KEYWORD_APPEND_TOKEN);
            }

            formattedValue += keywordAppendToken;
        }

        return formattedValue;
    }

    /**
     * Generate a count of closing braces, one on each line.
     *
     * @param count of closing braces.
     * @return A string with count of closing braces.
     */
    public static String closingBraces(final int count)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            sb.append("}\n");
        }

        return sb.toString();
    }

    /**
     * Return the Golang formatted byte order encoding string to use to read for a given byte order and primitiveType.
     *
     * @param byteOrder     of the {@link uk.co.real_logic.sbe.ir.Token}.
     * @param primitiveType of the {@link uk.co.real_logic.sbe.ir.Token}.
     * @return the string formatted as the byte ordering encoding.
     */
    public static String formatReadBytes(final ByteOrder byteOrder, final PrimitiveType primitiveType)
    {
        final String suffix = byteOrder == ByteOrder.BIG_ENDIAN ? "BigEndian" : "LittleEndian";

        switch (primitiveType)
        {
            case CHAR:
                return "Byte" + suffix;
            case FLOAT:
                return "Float" + suffix;
            case DOUBLE:
                return "Double" + suffix;
            case INT8:
                return "Int8" + suffix;
            case INT16:
                return "Int16" + suffix;
            case INT32:
                return "Int32" + suffix;
            case INT64:
                return "Int64" + suffix;
            case UINT8:
                return "Uint8" + suffix;
            case UINT16:
                return "Uint16" + suffix;
            case UINT32:
                return "Uint32" + suffix;
            case UINT64:
                return "Uint64" + suffix;
        }
        return "";
    }

    /**
     * Return the Golang formatted byte order encoding string to use to write for a given byte order and primitiveType.
     *
     * @param byteOrder     of the {@link uk.co.real_logic.sbe.ir.Token}.
     * @param primitiveType of the {@link uk.co.real_logic.sbe.ir.Token}.
     * @return the string formatted as the byte ordering encoding.
     */
    public static String formatWriteBytes(final ByteOrder byteOrder, final PrimitiveType primitiveType)
    {
        final String suffix = byteOrder == ByteOrder.BIG_ENDIAN ? "BigEndian" : "LittleEndian";

        switch (primitiveType)
        {
            case CHAR:
                return "PutByte" + suffix;
            case FLOAT:
                return "PutFloat" + suffix;
            case DOUBLE:
                return "PutDouble" + suffix;
            case INT8:
                return "PutInt8" + suffix;
            case INT16:
                return "PutInt16" + suffix;
            case INT32:
                return "PutInt32" + suffix;
            case INT64:
                return "PutInt64" + suffix;
            case UINT8:
                return "PutUint8" + suffix;
            case UINT16:
                return "PutUint16" + suffix;
            case UINT32:
                return "PutUint32" + suffix;
            case UINT64:
                return "PutUint64" + suffix;
        }
        return "";
    }
}
