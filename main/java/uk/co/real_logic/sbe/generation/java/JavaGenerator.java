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
package uk.co.real_logic.sbe.generation.java;

import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.util.Verify;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static uk.co.real_logic.sbe.generation.java.JavaUtil.javaTypeName;
import static uk.co.real_logic.sbe.generation.java.JavaUtil.toUpperFirstChar;

public class JavaGenerator implements CodeGenerator
{
    /** Class name to be used for visitor pattern that accesses the message header. */
    public static final String MESSAGE_HEADER_VISITOR = "MessageHeaderVisitor";

    private final IntermediateRepresentation ir;
    private final OutputManager outputManager;

    public JavaGenerator(final IntermediateRepresentation ir, final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir)");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generateMessageHeaderStub() throws IOException
    {
        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_VISITOR))
        {
            generateFileHeader(out, ir.packageName());
            generateClassDeclaration(out, MESSAGE_HEADER_VISITOR, FixedFlyweight.class.getSimpleName());
            generateFixedFlyweightCode(out);

            final List<Token> tokens = ir.header();
            generatePrimitiveEncodings(out, tokens.subList(1, tokens.size() - 1));

            out.append("}\n");
        }
    }

    public void generateTypeStubs() throws IOException
    {
        for (final List<Token> tokens : ir.types())
        {
            switch (tokens.get(0).signal())
            {
                case BEGIN_ENUM:
                    generateEnum(tokens);
                    break;

                case BEGIN_SET:
                    generateChoiceSet(tokens);
                    break;

                case BEGIN_COMPOSITE:
                    generateComposite(tokens);
                    break;
            }
        }
    }

    public void generateMessageStubs() throws IOException
    {
        for (final List<Token> tokens : ir.messages())
        {
            final String className = toUpperFirstChar(tokens.get(0).name());

            try (final Writer out = outputManager.createOutput(className))
            {
                generateFileHeader(out, ir.packageName());
                generateClassDeclaration(out, className, MessageFlyweight.class.getSimpleName());
                generateMessageFlyweightCode(out);

                out.append("}\n");
            }
        }
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = toUpperFirstChar(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            generateFileHeader(out, ir.packageName());
            generateClassDeclaration(out, bitSetName, FixedFlyweight.class.getSimpleName());
            generateFixedFlyweightCode(out);

            generateChoices(out, tokens.subList(1, tokens.size() - 1));

            out.append("}\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = toUpperFirstChar(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            generateFileHeader(out, ir.packageName());
            generateEnumDeclaration(out, enumName);

            generateEnumValues(out, tokens.subList(1, tokens.size() - 1));
            generateEnumBody(out, tokens.get(0), enumName);

            generateEnumLookupMethod(out, tokens.subList(1, tokens.size() - 1), enumName);

            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = toUpperFirstChar(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            generateFileHeader(out, ir.packageName());
            generateClassDeclaration(out, compositeName, FixedFlyweight.class.getSimpleName());
            generateFixedFlyweightCode(out);

            generatePrimitiveEncodings(out, tokens.subList(1, tokens.size() - 1));

            out.append("}\n");
        }
    }

    private void generateChoices(final Writer out, final List<Token> tokens) throws IOException
    {
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final String typePrefix = token.primitiveType().primitiveName();
                final String choiceBitPosition = token.options().constVal().toString();

                final String str = String.format(
                    "\n" +
                    "    public boolean %s()\n" +
                    "    {\n" +
                    "        return CodecUtil.%sGetChoice(buffer, offset, %s);\n" +
                    "    }\n\n" +
                    "    public void %s(final boolean value)\n" +
                    "    {\n" +
                    "        CodecUtil.%sPutChoice(buffer, offset, %s, value);\n" +
                    "    }\n",
                    choiceName,
                    typePrefix,
                    choiceBitPosition,
                    choiceName,
                    typePrefix,
                    choiceBitPosition
                );

                out.append(str);
            }
        }
    }

    private void generateEnumValues(final Writer out, final List<Token> tokens) throws IOException
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            final String constVal = generateLiteral(token);
            sb.append("    ").append(token.name()).append('(').append(constVal).append("),\n");
        }

        sb.setLength(sb.length() - 2);
        sb.append(";\n\n");

        out.append(sb);
    }

    private void generateEnumBody(final Writer out, final Token token, final String enumName) throws IOException
    {
        final String javaEncodingType = javaTypeName(token.primitiveType());

        out.append("    private final ").append(javaEncodingType).append(" value;\n\n")
           .append("    ").append(enumName).append("(final ").append(javaEncodingType).append(" value)\n")
           .append("    {\n")
           .append("        this.value = value;\n")
           .append("    }\n\n")
           .append("    public ").append(javaEncodingType).append(" value()\n")
           .append("    {\n")
           .append("        return value;\n")
           .append("    }\n\n");
    }

    private void generateEnumLookupMethod(final Writer out, final List<Token> tokens, final String enumName)
        throws IOException
    {
        final String javaEncodingType = javaTypeName(tokens.get(0).primitiveType());

        out.append("    public static ").append(enumName).append(" lookup(final ").append(javaEncodingType).append(" value)\n")
           .append("    {\n")
           .append("        switch (value)\n")
           .append("        {\n");

        for (final Token token : tokens)
        {
            final String constVal = token.options().constVal().toString();
            out.append("            case ").append(constVal).append(": return ").append(token.name()).append(";\n");
        }

        out.append("        }\n\n")
           .append("        throw new IllegalArgumentException(\"Unknown value: \" + value);\n")
           .append("    }\n");

    }

    private static void generateFileHeader(final Writer out, final String packageName)
        throws IOException
    {
        final String str = String.format(
            "/* Generated class message */\n" +
            "package %s;\n\n" +
            "import uk.co.real_logic.sbe.generation.java.*;\n\n",
            packageName
        );

        out.append(str);
    }

    private static void generateClassDeclaration(final Writer out,
                                                 final String className,
                                                 final String implementsName)
        throws IOException
    {
        out.append("public class ").append(className)
           .append(" implements ").append(implementsName).append("\n{\n");
    }

    private static void generateEnumDeclaration(final Writer out, final String name)
        throws IOException
    {
        out.append("public enum ").append(name).append("\n{\n");
    }

    private void generatePrimitiveEncodings(final Writer out, final List<Token> tokens)
        throws IOException
    {
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                generatePrimitiveEncoding(out, token);
            }
        }
    }

    private void generatePrimitiveEncoding(final Writer out, final Token token) throws IOException
    {
        final PrimitiveValue constVal = token.options().constVal();
        if (null == constVal)
        {
            generatePrimitiveEncodingMethods(out, token);
        }
        else
        {
            generateConstEncodingMethod(out, token);
        }
    }

    private void generatePrimitiveEncodingMethods(final Writer out, final Token token) throws IOException
    {
        final String javaTypeName = javaTypeName(token.primitiveType());
        final String typePrefix = token.primitiveType().primitiveName();
        final String propertyName = token.name();
        final Integer offset = Integer.valueOf(token.offset());

        final int arraySize = token.size() / token.primitiveType().size();
        if (arraySize == 1)
        {
            out.append(String.format(
                "\n" +
                "    public %s %s()\n" +
                "    {\n" +
                "        return CodecUtil.%sGet(buffer, offset + %d);\n" +
                "    }\n\n",
                javaTypeName,
                propertyName,
                typePrefix,
                offset
            ));

            out.append(String.format(
                "    public void %s(final %s value)\n" +
                "    {\n" +
                "        CodecUtil.%sPut(buffer, offset + %d, value);\n" +
                "    }\n",
                propertyName,
                javaTypeName,
                typePrefix,
                offset
            ));
        }
        else if (arraySize > 1)
        {
            out.append(String.format(
                "\n" +
                "    public int %sLength()\n" +
                "    {\n" +
                "        return %d;\n" +
                "    }\n\n",
                propertyName,
                Integer.valueOf(arraySize)
            ));

            out.append(String.format(
                "    public %s %s(final int index)\n" +
                "    {\n" +
                "        if (index < 0 || index > %d)\n" +
                "        {\n" +
                "            throw new IllegalArgumentException(\"index out of range: \" + %d);\n" +
                "        }\n\n" +
                "        return CodecUtil.%sGet(buffer, this.offset + %d + (index * %d));\n" +
                "    }\n\n",
                javaTypeName,
                propertyName,
                Integer.valueOf(arraySize),
                Integer.valueOf(arraySize),
                typePrefix,
                offset,
                Integer.valueOf(token.primitiveType().size())
            ));

            out.append(String.format(
                "    public void %s(final int index, final %s value)\n" +
                "    {\n" +
                "        if (index < 0 || index > %d)\n" +
                "        {\n" +
                "            throw new IllegalArgumentException(\"index out of range: \" + %d);\n" +
                "        }\n\n" +
                "        CodecUtil.%sPut(buffer, this.offset + %d + (index * %d), value);\n" +
                "    }\n",
                propertyName,
                javaTypeName,
                Integer.valueOf(arraySize),
                Integer.valueOf(arraySize),
                typePrefix,
                offset,
                Integer.valueOf(token.primitiveType().size())
            ));
        }
    }

    private void generateConstEncodingMethod(final Writer out, final Token token) throws IOException
    {
        final String javaTypeName = javaTypeName(token.primitiveType());
        final String propertyName = token.name();

        out.append(String.format(
            "\n" +
            "    public %s %s()\n" +
            "    {\n" +
            "        return %s;\n" +
            "    }\n",
            javaTypeName,
            propertyName,
            generateLiteral(token)
        ));
    }

    private void generateFixedFlyweightCode(final Writer out) throws IOException
    {
        out.append("    private DirectBuffer buffer;\n")
           .append("    private int offset;\n\n")
           .append("    public void reset(final DirectBuffer buffer, final int offset)\n")
           .append("    {\n")
           .append("        this.buffer = buffer;\n")
           .append("        this.offset = offset;\n")
           .append("    }\n");
    }


    private void generateMessageFlyweightCode(final Writer out) throws IOException
    {
        out.append("    private DirectBuffer buffer;\n")
           .append("    private int offset;\n\n")
           .append("    public void resetForDecode(final DirectBuffer buffer, final int offset)\n")
           .append("    {\n")
           .append("        this.buffer = buffer;\n")
           .append("        this.offset = offset;\n")
           .append("    }\n\n")
           .append("    public void resetForEncode(final DirectBuffer buffer, final int offset)\n")
           .append("    {\n")
           .append("        this.buffer = buffer;\n")
           .append("        this.offset = offset;\n")
           .append("    }\n");
    }

    private String generateLiteral(final Token token)
    {
        String literal = "";

        final String castType = javaTypeName(token.primitiveType());
        switch (token.primitiveType())
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + token.options().constVal();
                break;

            case UINT32:
            case INT32:
            case FLOAT:
                literal = token.options().constVal().toString();
                break;

            case UINT64:
            case INT64:
                literal = token.options().constVal() + "L";
                break;

            case DOUBLE:
                literal = token.options().constVal() + "d";
        }

        return literal;
    }
}
