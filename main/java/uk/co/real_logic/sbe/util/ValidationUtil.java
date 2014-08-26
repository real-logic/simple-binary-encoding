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
package uk.co.real_logic.sbe.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Various validation utilities used across parser, IR, and generator
 */
public class ValidationUtil
{
    private static boolean isSbeCppIdentifierStart(final char c)
    {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isSbeCppIdentifierPart(final char c)
    {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static final Set<String> CPP_KEYWORDS = new HashSet<>(Arrays.asList(new String[]
        {"alignas", "and", "and_eq", "asm", "auto",
         "bitand", "bitor", "bool", "break", "case",
         "catch", "char", "class", "compl", "const",
         "const_cast", "continue", "char16_t", "char32_t", "default",
         "delete", "do", "double", "dynamic_cast", "else",
         "enum", "explicit", "export", "extern", "false",
         "float", "for", "friend", "goto", "if",
         "inline", "int", "long", "mutable", "namespace",
         "new", "not", "not_eq", "noexcept", "operator",
         "or", "or_eq", "private", "protected", "public",
         "register", "reinterpret_cast", "return", "short", "signed",
         "sizeof", "static", "static_cast", "struct", "switch",
         "template", "this", "throw", "true", "try",
         "typedef", "typeid", "typename", "union", "unsigned",
         "using", "virtual", "void", "volatile", "wchar_t",
         "while", "xor", "xor_eq", "override",
         // since C++11
         "alignof", "constexpr", "decltype", "nullptr", "static_assert", "thread_local",
         // since C++11 have special meaning, so avoid
         "final"}));

    /**
     * Check value for validity of usage as a C++ identifier. A programmatic variable
     * must have all elements be a letter or digit or '_'. The first character must not be a digit.
     * And must not be a C++ keyword.
     *
     * http://en.cppreference.com/w/cpp/keyword
     *
     * @param value to check
     * @return true for validity as a C++ name. false if not.
     */
    public static boolean isSbeCppName(final String value)
    {
        if (possibleCppKeyword(value))
        {
            if (CPP_KEYWORDS.contains(value))
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    private static boolean possibleCppKeyword(final String stringVal)
    {
        for (int i = 0, size = stringVal.length(); i < size; i++)
        {
            char c = stringVal.charAt(i);

            if (i == 0 && isSbeCppIdentifierStart(c))
            {
                continue;
            }

            if (isSbeCppIdentifierPart(c))
            {
                continue;
            }

            return false;
        }

        return true;
    }

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(new String[]
        {"abstract", "assert", "boolean", "break", "byte",
         "case", "catch", "char", "class", "const",
         "continue", "default", "do", "double", "else",
         "enum", "extends", "final", "finally", "float",
         "for", "goto", "if", "implements", "import",
         "instanceof", "int", "interface", "long", "native",
         "new", "package", "private", "protected", "public",
         "return", "short", "static", "strictfp", "super",
         "switch", "synchronized", "this", "throw", "throws",
         "transient", "try", "void", "volatile", "while",
         // literals
         "null", "true", "false"}));

    /**
     * Check string for validity of usage as a Java identifier. Avoiding keywords.
     *
     * http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9
     *
     * @param value to check
     * @return true for validity as a Java name. false if not.
     * @see javax.lang.model.SourceVersion#isName(CharSequence)
     */
    public static boolean isSbeJavaName(final String value)
    {
        for (final String token : value.split("\\.", -1))
        {
            if (possibleJavaKeyword(token))
            {
                if (JAVA_KEYWORDS.contains(token))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private static boolean possibleJavaKeyword(final String value)
    {
        if (value.length() == 0)
        {
            return false;
        }

        for (int i = 0, size = value.length(); i < size; i++)
        {
            char c = value.charAt(i);

            if (i == 0 && Character.isJavaIdentifierStart(c))
            {
                continue;
            }

            if (Character.isJavaIdentifierPart(c))
            {
                continue;
            }

            return false;
        }

        return true;
    }
}
