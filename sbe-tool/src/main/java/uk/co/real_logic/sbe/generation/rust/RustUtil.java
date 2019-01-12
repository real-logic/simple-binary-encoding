/*
 * Copyright 2013-2019 Real Logic Ltd.
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
package uk.co.real_logic.sbe.generation.rust;

import org.agrona.Verify;
import uk.co.real_logic.sbe.PrimitiveType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;
import static uk.co.real_logic.sbe.generation.Generators.toLowerFirstChar;
import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;

public class RustUtil
{
    static final String INDENT = "  ";
    private static final Map<PrimitiveType, String> TYPE_NAME_BY_PRIMITIVE_TYPE_MAP =
        new EnumMap<>(PrimitiveType.class);

    static
    {
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.CHAR, "i8");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT8, "i8");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT16, "i16");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT32, "i32");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.INT64, "i64");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT8, "u8");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT16, "u16");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT32, "u32");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.UINT64, "u64");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.FLOAT, "f32");
        TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.put(PrimitiveType.DOUBLE, "f64");
    }

    /**
     * Map the name of a {@link uk.co.real_logic.sbe.PrimitiveType} to a Rust primitive type name.
     *
     * @param primitiveType to map.
     * @return the name of the Rust primitive that most closely maps.
     */
    static String rustTypeName(final PrimitiveType primitiveType)
    {
        return TYPE_NAME_BY_PRIMITIVE_TYPE_MAP.get(primitiveType);
    }

    static String generateRustLiteral(final PrimitiveType type, final String value)
    {
        Verify.notNull(type, "type");
        Verify.notNull(value, "value");
        final String typeName = rustTypeName(type);
        if (typeName == null)
        {
            throw new IllegalArgumentException("Unknown Rust type name found for primitive " + type.primitiveName());
        }

        switch (type)
        {
            case CHAR:
            case UINT8:
            case INT8:
            case INT16:
            case UINT16:
            case INT32:
            case INT64:
            case UINT32:
            case UINT64:
                return value + typeName;

            case FLOAT:
            case DOUBLE:
                return value.endsWith("NaN") ? typeName + "::NAN" : value + typeName;

            default:
                throw new IllegalArgumentException("Unsupported literal generation for type: " + type.primitiveName());
        }
    }

    static byte eightBitCharacter(final String asciiCharacter)
    {
        Verify.notNull(asciiCharacter, "asciiCharacter");
        final byte[] bytes = asciiCharacter.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != 1)
        {
            throw new IllegalArgumentException(
                format("String value %s did not fit into a single 8-bit " + "character", asciiCharacter));
        }

        return bytes[0];
    }

    static String formatTypeName(final String value)
    {
        return toUpperFirstChar(value);
    }

    static String formatMethodName(final String value)
    {
        if (value.isEmpty())
        {
            return value;
        }
        return sanitizeMethodOrProperty(toLowerUnderscoreFromCamel(value));
    }

    // Adapted from Guava, Apache License Version 2.0
    private static String toLowerUnderscoreFromCamel(final String value)
    {
        if (value.isEmpty())
        {
            return value;
        }
        final String s = toLowerFirstChar(value);

        // include some extra space for separators
        final StringBuilder out = new StringBuilder(s.length() + 4);
        int i = 0;
        int j = -1;
        while ((j = indexInUpperAlphaRange(s, ++j)) != -1)
        {
            final String word = s.substring(i, j).toLowerCase();
            out.append(word);
            if (!word.endsWith("_"))
            {
                out.append("_");
            }
            i = j;
        }

        return (i == 0) ? s.toLowerCase() : out.append(s.substring(i).toLowerCase()).toString();
    }

    // Adapted from Guava, Apache License Version 2.0
    private static int indexInUpperAlphaRange(final CharSequence sequence, final int start)
    {
        final int length = sequence.length();
        if (start < 0 || start > length)
        {
            throw new IndexOutOfBoundsException();
        }

        for (int i = start; i < length; i++)
        {
            final char c = sequence.charAt(i);
            if ('A' <= c && c <= 'Z')
            {
                return i;
            }
        }

        return -1;
    }

    private static String sanitizeMethodOrProperty(final String name)
    {
        if (shadowsKeyword(name))
        {
            return name + "_";
        }
        else
        {
            return name;
        }
    }

    private static boolean shadowsKeyword(final String name)
    {
        return ReservedKeyword.anyMatch(name);
    }

    static Appendable indent(final Appendable appendable) throws IOException
    {
        return indent(appendable, 1);
    }

    static Appendable indent(final Appendable appendable, final int level) throws IOException
    {
        Appendable out = appendable;
        for (int i = 0; i < level; i++)
        {
            out = out.append(INDENT);
        }

        return out;
    }

    static Appendable indent(final Appendable appendable, final int level, final String f, final Object... args)
        throws IOException
    {
        return indent(appendable, level).append(format(f, args));
    }

    private enum ReservedKeyword
    {
        Abstract, Alignof, As, Become, Box, Break, Const, Continue, Crate, Do,
        Else, Enum, Extern, False, Final, Fn, For, If, Impl, In, Let, Loop,
        Macro, Match, Mod, Move, Mut, Offsetof, Override, Priv, Proc, Pub,
        Pure, Ref, Return, Self, Sizeof, Static, Struct, Super, Trait, True,
        Type, Typeof, Unsafe, Unsized, Use, Virtual, Where, While, Yield;

        private static final Set<String> LOWER_CASE_NAMES = new HashSet<>();

        static
        {
            Arrays.stream(ReservedKeyword.values())
                .map(java.lang.Enum::name)
                .map(String::toLowerCase)
                .forEach(LOWER_CASE_NAMES::add);
        }

        private static boolean anyMatch(final String v)
        {
            return LOWER_CASE_NAMES.contains(v.toLowerCase());
        }
    }
}
