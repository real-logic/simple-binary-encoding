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

import javax.lang.model.SourceVersion;

/** Various validation utilities used across parser, IR, and generator */
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
            switch (value)
            {
                case "alignas":
                case "alignof": // since C++11
                case "and":
                case "and_eq":
                case "asm":
                case "auto":
                case "bitand":
                case "bitor":
                case "bool":
                case "break":
                case "case":
                case "catch":
                case "char":
                case "class":
                case "compl":
                case "const":
                case "const_cast":
                case "continue":
                case "char16_t":
                case "char32_t":
                case "constexpr": // since C++11
                case "default":
                case "delete":
                case "do":
                case "double":
                case "dynamic_cast":
                case "decltype": // since C++11
                case "else":
                case "enum":
                case "explicit":
                case "export":
                case "extern":
                case "false":
                case "float":
                case "for":
                case "friend":
                case "goto":
                case "if":
                case "inline":
                case "int":
                case "long":
                case "mutable":
                case "namespace":
                case "new":
                case "not":
                case "not_eq":
                case "noexcept":
                case "nullptr": // since C++11
                case "operator":
                case "or":
                case "or_eq":
                case "private":
                case "protected":
                case "public":
                case "register":
                case "reinterpret_cast":
                case "return":
                case "short":
                case "signed":
                case "sizeof":
                case "static":
                case "static_cast":
                case "struct":
                case "switch":
                case "static_assert": // since C++11
                case "template":
                case "this":
                case "throw":
                case "true":
                case "try":
                case "typedef":
                case "typeid":
                case "typename":
                case "thread_local": // since C++11
                case "union":
                case "unsigned":
                case "using":
                case "virtual":
                case "void":
                case "volatile":
                case "wchar_t":
                case "while":
                case "xor":
                case "xor_eq":
                case "override":
                case "final": // since C++11 have special meaning, so avoid
                    return false;
            }
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

    /**
     * Check string for validity of usage as a Java identifier. Avoiding keywords.
     *
     * @param string to check
     * @return true for validity as a Java name. false if not.
     */
    public static boolean isSbeJavaName(final String string)
    {
        return SourceVersion.isName(string);
    }
}
