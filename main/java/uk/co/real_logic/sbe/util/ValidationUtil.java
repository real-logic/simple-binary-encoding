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
     * http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9
     *
     * @see javax.lang.model.SourceVersion#isName(CharSequence)
     *
     * @param string to check
     * @return true for validity as a Java name. false if not.
     */
    public static boolean isSbeJavaName(final String string)
    {
        String id = string.toString();

        for(String token : id.split("\\.", -1))
        {
            if (possibleJavaKeyword(token))
            {
                switch (token)
                {
                    case "abstract":
                    case "assert":
                    case "boolean":
                    case "break":
                    case "byte":
                    case "case":
                    case "catch":
                    case "char":
                    case "class":
                    case "const":
                    case "continue":
                    case "default":
                    case "do":
                    case "double":
                    case "else":
                    case "enum":
                    case "extends":
                    case "final":
                    case "finally":
                    case "float":
                    case "for":
                    case "goto":
                    case "if":
                    case "implements":
                    case "import":
                    case "instanceof":
                    case "int":
                    case "interface":
                    case "long":
                    case "native":
                    case "new":
                    case "package":
                    case "private":
                    case "protected":
                    case "public":
                    case "return":
                    case "short":
                    case "static":
                    case "strictfp":
                    case "super":
                    case "switch":
                    case "synchronized":
                    case "this":
                    case "throw":
                    case "throws":
                    case "transient":
                    case "try":
                    case "void":
                    case "volatile":
                    case "while":
                    // literals
                    case "null":
                    case "true":
                    case "false":
                        return false;
                }
            }
        }
        return true;
    }

    private static boolean possibleJavaKeyword(final String stringVal)
    {
        if (stringVal.length() == 0)
        {
            return false;
        }

        for (int i = 0, size = stringVal.length(); i < size; i++)
        {
            char c = stringVal.charAt(i);

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
