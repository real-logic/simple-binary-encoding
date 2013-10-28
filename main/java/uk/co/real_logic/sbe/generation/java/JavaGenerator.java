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

import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.util.Verify;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;

public class JavaGenerator implements CodeGenerator
{
    /** Class name to be used for visitor pattern that accesses the message header. */
    public static final String MESSAGE_HEADER_VISITOR = "MessageHeader";

    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

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
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateClassDeclaration(MESSAGE_HEADER_VISITOR, FixedFlyweight.class.getSimpleName()));
            out.append(generateFixedFlyweightCode());

            final List<Token> tokens = ir.header();
            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

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

    public void generate() throws IOException
    {
        generateMessageHeaderStub();
        generateTypeStubs();

        for (final List<Token> tokens : ir.messages())
        {
            final String className = formatClassName(tokens.get(0).name());

            try (final Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.packageName()));
                out.append(generateClassDeclaration(className, MessageFlyweight.class.getSimpleName()));
                out.append(generateMessageFlyweightCode(tokens.get(0).size()));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                StringBuilder sb = new StringBuilder();
                generateGroups(sb, groups, 0, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = messageBody.subList(offset, messageBody.size());
                out.append(generateVarData(varData));

                out.append("}\n");
            }
        }
    }

    private int collectRootFields(final List<Token> tokens, int index, final List<Token> rootFields)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            final Token token = tokens.get(index);
            if (Signal.BEGIN_GROUP == token.signal() ||
                Signal.END_GROUP == token.signal() ||
                Signal.BEGIN_VAR_DATA == token.signal())
            {
                return index;
            }

            rootFields.add(token);
        }

        return index;
    }

    private int collectGroups(final List<Token> tokens, int index, final List<Token> groups)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            final Token token = tokens.get(index);
            if (Signal.BEGIN_VAR_DATA == token.signal())
            {
                return index;
            }

            groups.add(token);
        }

        return index;
    }

    private int generateGroups(final StringBuilder sb, final List<Token> tokens, int index, final String indent)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
            {
                final String groupName = tokens.get(index).name();
                sb.append(generateGroupProperty(groupName, indent));

                generateGroupClassHeader(sb, groupName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateFields(rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateGroups(sb, tokens, index, indent + INDENT);
                }

                sb.append(indent).append("    }\n");
            }
        }

        return index;
    }

    private void generateGroupClassHeader(final StringBuilder sb,
                                          final String groupName,
                                          final List<Token> tokens,
                                          final int index,
                                          final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final Integer dimensionHeaderSize = Integer.valueOf(tokens.get(index + 1).size());

        sb.append(String.format(
            "\n" +
            indent + "public class %s implements GroupFlyweight\n" +
            indent + "{\n" +
            indent + "    private final %s dimensions = new %s();\n" +
            indent + "    private int blockLength;\n" +
            indent + "    private int size;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n\n",
            formatClassName(groupName),
            dimensionsClassName,
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    public void resetForDecode()\n" +
            indent + "    {\n" +
            indent + "        dimensions.reset(buffer, position());\n" +
            indent + "        size = dimensions.numInGroup();\n" +
            indent + "        blockLength = dimensions.blockLength();\n" +
            indent + "        index = -1;\n" +
            indent + "        final int dimensionsHeaderSize = %d;\n" +
            indent + "        position(position() + dimensionsHeaderSize);\n" +
            indent + "    }\n\n",
            dimensionHeaderSize
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String javaTypeForBlockLength = javaTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String javaTypeForNumInGroup = javaTypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    public void resetForEncode(final int size)\n" +
            indent + "    {\n" +
            indent + "        dimensions.reset(buffer, position());\n" +
            indent + "        dimensions.numInGroup((%s)size);\n" +
            indent + "        dimensions.blockLength((%s)%d);\n" +
            indent + "        index = -1;\n" +
            indent + "        this.size = size;\n" +
            indent + "        blockLength = %d;\n" +
            indent + "        final int dimensionsHeaderSize = %d;\n" +
            indent + "        position(position() + dimensionsHeaderSize);\n" +
            indent + "    }\n\n",
            javaTypeForNumInGroup,
            javaTypeForBlockLength,
            blockLength,
            blockLength,
            dimensionHeaderSize
        ));

        sb.append(
            indent + "    public int size()\n" +
            indent + "    {\n" +
            indent + "        return size;\n" +
            indent + "    }\n\n"
        );

        sb.append(
            indent + "    public boolean next()\n" +
            indent + "    {\n" +
            indent + "        if (index + 1 >= size)\n" +
            indent + "        {\n" +
            indent + "            return false;\n" +
            indent + "        }\n\n" +
            indent + "        offset = position();\n" +
            indent + "        position(offset + blockLength);\n" +
            indent + "        ++index;\n\n" +
            indent + "        return true;\n" +
            indent + "    }\n"
        );
    }

    private CharSequence generateGroupProperty(final String groupName, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        sb.append(String.format(
            "\n" +
            indent + "    private final %s %s = new %s();\n",
            className,
            propertyName,
            className
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        %s.resetForDecode();\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            className,
            propertyName,
            propertyName,
            propertyName
        ));


        sb.append(String.format(
            "\n" +
                indent + "    public %s %sSize(final int size)\n" +
                indent + "    {\n" +
                indent + "        %s.resetForEncode(size);\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
            className,
            propertyName,
            propertyName,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateVarData(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = toUpperFirstChar(token.name());
                final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();

                sb.append(String.format(
                    "\n"  +
                    "    public String %sCharacterEncoding()\n" +
                    "    {\n" +
                    "        return \"%s\";\n" +
                    "    }\n\n",
                    formatPropertyName(propertyName),
                    characterEncoding
                ));

                final Token lengthToken = tokens.get(i + 2);
                final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
                final String lengthJavaType = javaTypeName(lengthToken.encoding().primitiveType());
                final String lengthTypePrefix = lengthToken.encoding().primitiveType().primitiveName();

                sb.append(String.format(
                    "    public int get%s(final byte[] dst, final int offset, final int length)\n" +
                    "    {\n" +
                    "        final int sizeOfLengthField = %d;\n" +
                    "        final int lengthPosition = position();\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        final int dataLength = CodecUtil.%sGet(buffer, lengthPosition);\n" +
                    "        final int bytesCopied = Math.min(length, dataLength);\n" +
                    "        CodecUtil.int8sGet(buffer, position(), dst, offset, bytesCopied);\n" +
                    "        position(position() + dataLength);\n" +
                    "        return bytesCopied;\n" +
                    "    }\n\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix
                ));

                sb.append(String.format(
                    "    public int put%s(final byte[] src, final int offset, final int length)\n" +
                    "    {\n" +
                    "        final int sizeOfLengthField = %d;\n" +
                    "        final int lengthPosition = position();\n" +
                    "        CodecUtil.%sPut(buffer, lengthPosition, (%s)length);\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        CodecUtil.int8sPut(buffer, position(), src, offset, length);\n" +
                    "        position(position() + length);\n" +
                    "        return length;" +
                    "    }\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    lengthJavaType
                ));
            }
        }

        return sb;
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateClassDeclaration(bitSetName, FixedFlyweight.class.getSimpleName()));
            out.append(generateFixedFlyweightCode());

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));

            out.append("}\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateEnumDeclaration(enumName));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1)));
            out.append(generateEnumBody(tokens.get(0), enumName));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumName));

            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateClassDeclaration(compositeName, FixedFlyweight.class.getSimpleName()));
            out.append(generateFixedFlyweightCode());

            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("}\n");
        }
    }

    private CharSequence generateChoices(final String bitsetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final String typePrefix = token.encoding().primitiveType().primitiveName();
                final String choiceBitPosition = token.encoding().constVal().toString();

                sb.append(String.format(
                    "\n" +
                    "    public boolean %s()\n" +
                    "    {\n" +
                    "        return CodecUtil.%sGetChoice(buffer, offset, %s);\n" +
                    "    }\n\n" +
                    "    public %s %s(final boolean value)\n" +
                    "    {\n" +
                    "        CodecUtil.%sPutChoice(buffer, offset, %s, value);\n" +
                    "        return this;" +
                    "    }\n",
                    choiceName,
                    typePrefix,
                    choiceBitPosition,
                    bitsetClassName,
                    choiceName,
                    typePrefix,
                    choiceBitPosition
                ));
            }
        }

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(token);
            sb.append("    ").append(token.name()).append('(').append(constVal).append("),\n");
        }

        sb.setLength(sb.length() - 2);
        sb.append(";\n\n");

        return sb;
    }

    private CharSequence generateEnumBody(final Token token, final String enumName)
    {
        final String javaEncodingType = javaTypeName(token.encoding().primitiveType());

        return String.format(
            "    private final %s value;\n\n"+
            "    %s(final %s value)\n" +
            "    {\n" +
            "        this.value = value;\n" +
            "    }\n\n" +
            "    public %s value()\n" +
            "    {\n" +
            "        return value;\n" +
            "    }\n\n",
            javaEncodingType,
            enumName,
            javaEncodingType,
            javaEncodingType
        );
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final String enumName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
           "    public static %s get(final %s value)\n" +
           "    {\n" +
           "        switch (value)\n" +
           "        {\n",
           enumName,
           javaTypeName(tokens.get(0).encoding().primitiveType())
        ));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %s: return %s;\n",
                token.encoding().constVal().toString(),
                token.name())
            );
        }

        sb.append(
            "        }\n\n" +
            "        throw new IllegalArgumentException(\"Unknown value: \" + value);\n" +
            "    }\n"
        );

        return sb;
    }

    private CharSequence generateFileHeader(final String packageName)
    {
        return String.format(
            "/* Generated class message */\n" +
            "package %s;\n\n" +
            "import uk.co.real_logic.sbe.generation.java.*;\n\n",
            packageName
        );
    }

    private CharSequence generateClassDeclaration(final String className, final String implementedInterface)
    {
        return String.format(
            "public class %s implements %s\n" +
            "{\n",
            className,
            implementedInterface
        );
    }

    private CharSequence generateEnumDeclaration(final String name)
    {
        return "public enum " + name + "\n{\n";
    }

    private CharSequence generatePrimitivePropertyEncodings(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                sb.append(generatePrimitiveProperty(token.name(), token, indent));
            }
        }

       return sb;
    }

    private CharSequence generatePrimitiveProperty(final String propertyName, final Token token, final String indent)
    {
        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            return generateConstPropertyMethod(propertyName, token, indent);
        }
        else
        {
            return generatePrimitivePropertyMethods(propertyName, token, indent);
        }
    }

    private CharSequence generatePrimitivePropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final int arrayLength = token.arrayLength();

        if (arrayLength == 1)
        {
            return generateSingleValueProperty(propertyName, token, indent);
        }
        else if (arrayLength > 1)
        {
            return generateArrayProperty(propertyName, token, indent);
        }

        return "";
    }

    private CharSequence generateSingleValueProperty(final String propertyName, final Token token, final String indent)
    {
        final String javaTypeName = javaTypeName(token.encoding().primitiveType());
        final String typePrefix = token.encoding().primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        return CodecUtil.%sGet(buffer, offset + %d);\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            typePrefix,
            offset
        ));

        sb.append(String.format(
            indent + "    public void %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        CodecUtil.%sPut(buffer, offset + %d, value);\n" +
            indent + "    }\n",
            propertyName,
            javaTypeName,
            typePrefix,
            offset
        ));

        return sb;
    }

    private CharSequence generateArrayProperty(final String propertyName, final Token token, final String indent)
    {
        final String javaTypeName = javaTypeName(token.encoding().primitiveType());
        final String typePrefix = token.encoding().primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public int %sLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            propertyName,
            Integer.valueOf(token.arrayLength())
        ));

        sb.append(String.format(
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        return CodecUtil.%sGet(buffer, this.offset + %d + (index * %d));\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            Integer.valueOf(token.arrayLength()),
            typePrefix,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size())
        ));

        sb.append(String.format(
            indent + "    public void %s(final int index, final %s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        CodecUtil.%sPut(buffer, this.offset + %d + (index * %d), value);\n" +
            indent + "    }\n\n",
            propertyName,
            javaTypeName,
            Integer.valueOf(token.arrayLength()),
            typePrefix,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size())
        ));

        sb.append(String.format(
            indent + "    public int get%s(final %s[] dst, final int offset, final int length)\n" +
            indent + "    {\n" +
            indent + "        if (offset < 0 || offset > (dst.length - length))\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"offset out of range: offset=\" + offset);\n" +
            indent + "        }\n\n" +
            indent + "        final int elementsCopied = Math.min(length, %d);\n" +
            indent + "        CodecUtil.%ssGet(buffer, this.offset + %d, dst, offset, elementsCopied);\n" +
            indent + "        return elementsCopied;\n" +
            indent + "    }\n\n",
            toUpperFirstChar(propertyName),
            javaTypeName,
            Integer.valueOf(token.arrayLength()),
            typePrefix,
            offset
        ));

        sb.append(String.format(
            indent + "    public int put%s(final %s[] src, final int offset, final int length)\n" +
            indent + "    {\n" +
            indent + "        if (offset < 0 || offset > (src.length - length))\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"offset out of range: offset=\" + offset);\n" +
            indent + "        }\n\n" +
            indent + "        final int elementsCopied = Math.min(length, %d);\n" +
            indent + "        CodecUtil.%ssPut(buffer, this.offset + %d, src, offset, elementsCopied);\n" +
            indent + "        return elementsCopied;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            javaTypeName,
            Integer.valueOf(token.arrayLength()),
            typePrefix,
            offset
        ));

        return sb;
    }

    private CharSequence generateConstPropertyMethod(final String propertyName, final Token token, final String indent)
    {
        return String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName(token.encoding().primitiveType()),
            propertyName,
            generateLiteral(token)
        );
    }

    private CharSequence generateFixedFlyweightCode()
    {
        return
            "    private DirectBuffer buffer;\n" +
            "    private int offset;\n\n" +
            "    public void reset(final DirectBuffer buffer, final int offset)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "    }\n";
    }

    private CharSequence generateMessageFlyweightCode(final int blockLength)
    {
        return String.format(
            "    private static final int blockLength = %d;\n\n" +
            "    private DirectBuffer buffer;\n" +
            "    private int offset;\n" +
            "    private int position;\n" +
            "\n" +
            "    public int blockLength()\n" +
            "    {\n" +
            "        return blockLength;\n" +
            "    }\n\n" +
            "    public void reset(final DirectBuffer buffer, final int offset)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        position(blockLength);\n" +
            "    }\n\n" +
            "    public int position()\n" +
            "    {\n" +
            "        return position;\n" +
            "    }\n\n" +
            "    public void position(final int position)\n" +
            "    {\n" +
            "        CodecUtil.checkPosition(position, offset, buffer.capacity());\n" +
            "        this.position = position;\n" +
            "    }\n",
            Integer.valueOf(blockLength)
        );
    }

    private CharSequence generateFields(final List<Token> tokens, final String indent)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = signalToken.name();

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitsetProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, encodingToken, indent));
                        break;
                }
            }
        }

        return sb;
    }

    private CharSequence generateEnumProperty(final String propertyName, final Token token, final String indent)
    {
        final String enumName = token.name();
        final String typePrefix = token.encoding().primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        return %s.get(CodecUtil.%sGet(buffer, offset + %d));\n" +
            indent + "    }\n\n",
            enumName,
            propertyName,
            enumName,
            typePrefix,
            offset
        ));

        sb.append(String.format(
            indent + "    public void %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        CodecUtil.%sPut(buffer, offset + %d, value.value());\n" +
            indent + "    }\n",
            propertyName,
            enumName,
            typePrefix,
            offset
        ));

        return sb;
    }

    private Object generateBitsetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String bitsetName = formatClassName(token.name());
        final String formattedPropertyName = formatPropertyName(propertyName);
        final Integer offset = Integer.valueOf(token.offset());

        sb.append(String.format(
            "\n" +
            indent + "    private final %s %s = new %s();\n",
            bitsetName,
            formattedPropertyName,
            bitsetName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        %s.reset(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            bitsetName,
            formattedPropertyName,
            formattedPropertyName,
            offset,
            formattedPropertyName
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = token.name();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    private final %s %s = new %s();\n",
            compositeName,
            propertyName,
            compositeName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        %s.reset(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            propertyName,
            offset,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateLiteral(final Token token)
    {
        String literal = "";

        final String castType = javaTypeName(token.encoding().primitiveType());
        switch (token.encoding().primitiveType())
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + token.encoding().constVal();
                break;

            case UINT32:
            case INT32:
                literal = token.encoding().constVal().toString();
                break;

            case FLOAT:
                literal = token.encoding().constVal() + "f";
                break;

            case UINT64:
            case INT64:
                literal = token.encoding().constVal() + "L";
                break;

            case DOUBLE:
                literal = token.encoding().constVal() + "d";
        }

        return literal;
    }
}
