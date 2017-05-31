/*
 * Copyright 2013-2017 Real Logic Ltd.
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
import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.util.ValidationUtil;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static java.lang.reflect.Modifier.STATIC;

/**
 * Utilities for mapping between IR and the Java language.
 */
public class JavaUtil
{
    public enum Separators
    {
        BEGIN_GROUP('['),
        END_GROUP(']'),
        BEGIN_COMPOSITE('('),
        END_COMPOSITE(')'),
        BEGIN_SET('{'),
        END_SET('}'),
        BEGIN_ARRAY('['),
        END_ARRAY(']'),
        FIELD('|'),
        KEY_VALUE('='),
        ENTRY(',');

        public final char symbol;

        Separators(final char symbol)
        {
            this.symbol = symbol;
        }

        /**
         * Add separator to a generated StringBuilder
         *
         * @param builder          the code generation builder to which information should be added
         * @param indent           the current generated code indentation
         * @param generatedBuilder the name of the generated StringBuilder to which separator should be added
         */
        public void appendToGeneratedBuilder(
            final StringBuilder builder, final String indent, final String generatedBuilder)
        {
            append(builder, indent, generatedBuilder + ".append('" + symbol + "');");
        }

        public String toString()
        {
            return String.valueOf(symbol);
        }
    }

    private static final Map<PrimitiveType, String> TYPE_NAME_BY_PRIMITIVE_TYPE_MAP =
        new EnumMap<>(PrimitiveType.class);

    static
    {
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.CHAR, "byte");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT8, "byte");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT16, "short");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT32, "int");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT64, "long");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT8, "short");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT16, "int");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT32, "long");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT64, "long");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.FLOAT, "float");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.DOUBLE, "double");
    }

    /**
     * Indexes known charset aliases to the name of the instance in {@link StandardCharsets}.
     */
    private static final Map<String, String> STD_CHARSETS = new HashMap<>();

    static
    {
        try
        {
            for (final Field field : StandardCharsets.class.getDeclaredFields())
            {
                if (Charset.class.isAssignableFrom(field.getType()) && ((field.getModifiers() & STATIC) == STATIC))
                {
                    final Charset charset = (Charset)field.get(null);
                    STD_CHARSETS.put(charset.name(), field.getName());
                    charset.aliases().forEach((alias) -> STD_CHARSETS.put(alias, field.getName()));
                }
            }
        }
        catch (final IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a Java primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Java primitive that most closely maps.
     */
    public static String javaTypeName(final PrimitiveType primitiveType)
    {
        return TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.get(primitiveType);
    }

    /**
     * Uppercase the first character of a given String.
     *
     * @param s to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toUpperFirstChar(final String s)
    {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Lowercase the first character of a given String.
     *
     * @param s to have the first character upper cased.
     * @return a new String with the first character in uppercase.
     */
    public static String toLowerFirstChar(final String s)
    {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Format a property name for generated code.
     * <p>
     * If the formatted property name is a keyword then {@link SbeTool#KEYWORD_APPEND_TOKEN} is appended if set.
     *
     * @param value to be formatted.
     * @return the string formatted as a property name.
     * @throws IllegalStateException if a keyword and {@link SbeTool#KEYWORD_APPEND_TOKEN} is not set.
     */
    public static String formatPropertyName(final String value)
    {
        String formattedValue = toLowerFirstChar(value);

        if (ValidationUtil.isJavaKeyword(formattedValue))
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
     * Format a class name for the generated code.
     *
     * @param value to be formatted.
     * @return the string formatted as a class name.
     */
    public static String formatClassName(final String value)
    {
        return toUpperFirstChar(value);
    }


    /**
     * Shortcut to append a line of generated code
     *
     * @param builder string builder to which to append the line
     * @param indent  current text indentation
     * @param line    line to be appended
     */
    public static void append(final StringBuilder builder, final String indent, final String line)
    {
        builder.append(indent).append(line).append('\n');
    }

    /**
     * Code to fetch an instance of {@link java.nio.charset.Charset} corresponding to the given encoding.
     *
     * @param encoding as a string name (eg. UTF-8).
     * @return the code to fetch the associated Charset.
     */
    public static String charset(final String encoding)
    {
        final String charsetName = STD_CHARSETS.get(encoding);
        if (charsetName != null)
        {
            return "java.nio.charset.StandardCharsets." + charsetName;
        }
        else
        {
            return "java.nio.charset.Charset.forName(\"" + encoding + "\")";
        }
    }
}
