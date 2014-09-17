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

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.*;
import uk.co.real_logic.sbe.util.Verify;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;

public class JavaGenerator implements CodeGenerator
{
    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public JavaGenerator(final Ir ir, final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generateMessageHeaderStub() throws IOException
    {
        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_TYPE))
        {
            final List<Token> tokens = ir.headerStructure().tokens();
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateClassDeclaration(MESSAGE_HEADER_TYPE));
            out.append(generateFixedFlyweightCode(MESSAGE_HEADER_TYPE, tokens.get(0).size()));
            out.append(generatePrimitivePropertyEncodings(
                MESSAGE_HEADER_TYPE, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("}\n");
        }
    }

    public void generateTypeStubs() throws IOException
    {
        generateMetaAttributeEnum();

        for (final List<Token> tokens : ir.types())
        {
            switch (tokens.get(0).signal())
            {
                case BEGIN_ENUM:
                    generateEnum(tokens);
                    break;

                case BEGIN_SET:
                    generateBitSet(tokens);
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
            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());

            try (final Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.applicableNamespace()));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(className, msgToken));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(className, rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, className, groups, 0, BASE_INDENT);
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

    private int generateGroups(
        final StringBuilder sb,
        final String parentMessageClassName,
        final List<Token> tokens,
        int index,
        final String indent)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
            {
                final Token groupToken = tokens.get(index);
                final String groupName = groupToken.name();
                final String groupClassName = formatClassName(groupName);
                sb.append(generateGroupProperty(groupName, groupToken, indent));

                generateGroupClassHeader(sb, groupName, parentMessageClassName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateFields(groupClassName, rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateGroups(sb, parentMessageClassName, tokens, index, indent + INDENT);
                }

                sb.append(indent).append("    }\n");
            }
        }

        return index;
    }

    private void generateGroupClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<Token> tokens,
        final int index,
        final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final Integer dimensionHeaderSize = Integer.valueOf(tokens.get(index + 1).size());

        sb.append(String.format(
            "\n" +
            indent + "public static class %1$s implements Iterable<%1$s>, java.util.Iterator<%1$s>\n" +
            indent + "{\n" +
            indent + "    private static final int HEADER_SIZE = %2$d;\n" +
            indent + "    private final %3$s dimensions = new %3$s();\n" +
            indent + "    private %4$s parentMessage;\n" +
            indent + "    private DirectBuffer buffer;\n" +
            indent + "    private int blockLength;\n" +
            indent + "    private int actingVersion;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n\n",
            formatClassName(groupName),
            dimensionHeaderSize,
            dimensionsClassName,
            parentMessageClassName
        ));

        sb.append(String.format(
            indent + "    public void wrapForDecode(\n" +
            indent + "        final %s parentMessage, final DirectBuffer buffer, final int actingVersion)\n" +
            indent + "    {\n" +
            indent + "        this.parentMessage = parentMessage;\n" +
            indent + "        this.buffer = buffer;\n" +
            indent + "        dimensions.wrap(buffer, parentMessage.limit(), actingVersion);\n" +
            indent + "        blockLength = dimensions.blockLength();\n" +
            indent + "        count = dimensions.numInGroup();\n" +
            indent + "        this.actingVersion = actingVersion;\n" +
            indent + "        index = -1;\n" +
            indent + "        parentMessage.limit(parentMessage.limit() + HEADER_SIZE);\n" +
            indent + "    }\n\n",
            parentMessageClassName
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String javaTypeForBlockLength = javaTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String javaTypeForNumInGroup = javaTypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    public void wrapForEncode(final %1$s parentMessage, final DirectBuffer buffer, final int count)\n" +
            indent + "    {\n" +
            indent + "        this.parentMessage = parentMessage;\n" +
            indent + "        this.buffer = buffer;\n" +
            indent + "        actingVersion = SCHEMA_VERSION;\n" +
            indent + "        dimensions.wrap(buffer, parentMessage.limit(), actingVersion);\n" +
            indent + "        dimensions.blockLength((%2$s)%3$d);\n" +
            indent + "        dimensions.numInGroup((%4$s)count);\n" +
            indent + "        index = -1;\n" +
            indent + "        this.count = count;\n" +
            indent + "        blockLength = %3$d;\n" +
            indent + "        parentMessage.limit(parentMessage.limit() + HEADER_SIZE);\n" +
            indent + "    }\n\n",
            parentMessageClassName,
            javaTypeForBlockLength,
            blockLength,
            javaTypeForNumInGroup
        ));

        sb.append(String.format(
            indent + "    public static int sbeHeaderSize()\n" +
            indent + "    {\n" +
            indent + "        return HEADER_SIZE;\n" +
            indent + "    }\n\n"
        ));
        sb.append(String.format(
            indent + "    public static int sbeBlockLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            blockLength
        ));

        sb.append(String.format(
            indent + "    public int actingBlockLength()\n" +
            indent + "    {\n" +
            indent + "        return blockLength;\n" +
            indent + "    }\n\n" +
            indent + "    public int count()\n" +
            indent + "    {\n" +
            indent + "        return count;\n" +
            indent + "    }\n\n" +
            indent + "    @Override\n" +
            indent + "    public java.util.Iterator<%s> iterator()\n" +
            indent + "    {\n" +
            indent + "        return this;\n" +
            indent + "    }\n\n" +
            indent + "    @Override\n" +
            indent + "    public void remove()\n" +
            indent + "    {\n" +
            indent + "        throw new UnsupportedOperationException();\n" +
            indent + "    }\n\n" +
            indent + "    @Override\n" +
            indent + "    public boolean hasNext()\n" +
            indent + "    {\n" +
            indent + "        return (index + 1) < count;\n" +
            indent + "    }\n\n",
            formatClassName(groupName)
        ));

        sb.append(String.format(
            indent + "    @Override\n" +
            indent + "    public %s next()\n" +
            indent + "    {\n" +
            indent + "        if (index + 1 >= count)\n" +
            indent + "        {\n" +
            indent + "            throw new java.util.NoSuchElementException();\n" +
            indent + "        }\n\n" +
            indent + "        offset = parentMessage.limit();\n" +
            indent + "        parentMessage.limit(offset + blockLength);\n" +
            indent + "        ++index;\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(groupName)
        ));
    }

    private CharSequence generateGroupProperty(final String groupName, final Token token, final String indent)
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
            indent + "    public static long %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            groupName,
            Integer.valueOf(token.id())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s()\n" +
            indent + "    {\n" +
            indent + "        %2$s.wrapForDecode(parentMessage, buffer, actingVersion);\n" +
            indent + "        return %2$s;\n" +
            indent + "    }\n",
            className,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$sCount(final int count)\n" +
            indent + "    {\n" +
            indent + "        %2$s.wrapForEncode(parentMessage, buffer, count);\n" +
            indent + "        return %2$s;\n" +
            indent + "    }\n",
            className,
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
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                continue;
            }

            generateFieldIdMethod(sb, token, BASE_INDENT);
            final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
            generateCharacterEncodingMethod(sb, token.name(), characterEncoding, BASE_INDENT);
            generateFieldMetaAttributeMethod(sb, token, BASE_INDENT);

            final String propertyName = toUpperFirstChar(token.name());
            final Token lengthToken = tokens.get(i + 2);
            final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
            final Encoding lengthEncoding = lengthToken.encoding();
            final String lengthJavaType = javaTypeName(lengthEncoding.primitiveType());
            final String lengthTypePrefix = lengthEncoding.primitiveType().primitiveName();
            final ByteOrder byteOrder = lengthEncoding.byteOrder();
            final String byteOrderStr = lengthEncoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;

            sb.append(String.format(
                "\n" +
                "    public static int %sHeaderSize()\n" +
                "    {\n" +
                "        return %d;\n" +
                "    }\n",
                toLowerFirstChar(propertyName),
                sizeOfLengthField
            ));

            sb.append(String.format(
                "\n" +
                "    public int get%s(final byte[] dst, final int dstOffset, final int length)\n" +
                "    {\n" +
                "%s" +
                "        final int sizeOfLengthField = %d;\n" +
                "        final int limit = limit();\n" +
                "        buffer.checkLimit(limit + sizeOfLengthField);\n" +
                "        final int dataLength = CodecUtil.%sGet(buffer, limit%s);\n" +
                "        final int bytesCopied = Math.min(length, dataLength);\n" +
                "        limit(limit + sizeOfLengthField + dataLength);\n" +
                "        CodecUtil.int8sGet(buffer, limit + sizeOfLengthField, dst, dstOffset, bytesCopied);\n\n" +
                "        return bytesCopied;\n" +
                "    }\n",
                propertyName,
                generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
                sizeOfLengthField,
                lengthTypePrefix,
                byteOrderStr
            ));

            sb.append(String.format(
                "\n" +
                "    public int put%s(final byte[] src, final int srcOffset, final int length)\n" +
                "    {\n" +
                "        final int sizeOfLengthField = %d;\n" +
                "        final int limit = limit();\n" +
                "        limit(limit + sizeOfLengthField + length);\n" +
                "        CodecUtil.%sPut(buffer, limit, (%s)length%s);\n" +
                "        CodecUtil.int8sPut(buffer, limit + sizeOfLengthField, src, srcOffset, length);\n\n" +
                "        return length;\n" +
                "    }\n",
                propertyName,
                sizeOfLengthField,
                lengthTypePrefix,
                lengthJavaType,
                byteOrderStr
            ));
        }

        return sb;
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).size()));
            out.append(generateChoiceClear(bitSetName, tokens.get(0)));
            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));

            out.append("}\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateEnumFileHeader(ir.applicableNamespace()));
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
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).size()));

            out.append(generatePrimitivePropertyEncodings(compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("}\n");
        }
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String typePrefix = encoding.primitiveType().primitiveName();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final ByteOrder byteOrder = encoding.byteOrder();
        final String byteOrderStr = encoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;

        sb.append(String.format(
            "\n" +
            "    public %s clear()\n" +
            "    {\n" +
            "        CodecUtil.%sPut(buffer, offset, %s%s);\n" +
            "        return this;\n" +
            "    }\n",
            bitSetClassName,
            typePrefix,
            literalValue,
            byteOrderStr
        ));

        return sb;
    }

    private CharSequence generateChoices(final String bitSetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final Encoding encoding = token.encoding();
                final String typePrefix = encoding.primitiveType().primitiveName();
                final String choiceBitPosition = encoding.constValue().toString();
                final ByteOrder byteOrder = encoding.byteOrder();
                final String byteOrderStr = encoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;

                sb.append(String.format(
                    "\n" +
                    "    public boolean %s()\n" +
                    "    {\n" +
                    "        return CodecUtil.%sGetChoice(buffer, offset, %s%s);\n" +
                    "    }\n\n" +
                    "    public %s %s(final boolean value)\n" +
                    "    {\n" +
                    "        CodecUtil.%sPutChoice(buffer, offset, %s, value%s);\n" +
                    "        return this;\n" +
                    "    }\n",
                    choiceName,
                    typePrefix,
                    choiceBitPosition,
                    byteOrderStr,
                    bitSetClassName,
                    choiceName,
                    typePrefix,
                    choiceBitPosition,
                    byteOrderStr
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
            final Encoding encoding = token.encoding();
            final CharSequence constVal = generateLiteral(encoding.primitiveType(), encoding.constValue().toString());
            sb.append("    ").append(token.name()).append('(').append(constVal).append("),\n");
        }

        final Token token = tokens.get(0);
        final Encoding encoding = token.encoding();
        final CharSequence nullVal = generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());

        sb.append("    ").append("NULL_VAL").append('(').append(nullVal).append(')');
        sb.append(";\n\n");

        return sb;
    }

    private CharSequence generateEnumBody(final Token token, final String enumName)
    {
        final String javaEncodingType = javaTypeName(token.encoding().primitiveType());

        return String.format(
            "    private final %1$s value;\n\n" +
            "    %2$s(final %1$s value)\n" +
            "    {\n" +
            "        this.value = value;\n" +
            "    }\n\n" +
            "    public %1$s value()\n" +
            "    {\n" +
            "        return value;\n" +
            "    }\n\n",
            javaEncodingType,
            enumName
        );
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final String enumName)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = tokens.get(0).encoding().primitiveType();
        sb.append(String.format(
            "    public static %s get(final %s value)\n" +
            "    {\n" +
            "        switch (value)\n" +
            "        {\n",
            enumName,
            javaTypeName(primitiveType)
        ));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %s: return %s;\n",
                token.encoding().constValue().toString(),
                token.name())
            );
        }

        sb.append(String.format(
            "        }\n\n" +
            "        if (%s == value)\n" +
            "        {\n" +
            "            return NULL_VAL;\n" +
            "        }\n\n" +
            "        throw new IllegalArgumentException(\"Unknown value: \" + value);\n" +
            "    }\n",
            generateLiteral(primitiveType, tokens.get(0).encoding().applicableNullValue().toString())
        ));

        return sb;
    }

    private CharSequence generateFileHeader(final String packageName)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n" +
            "import uk.co.real_logic.sbe.codec.java.*;\n\n",
            packageName
        );
    }

    private CharSequence generateEnumFileHeader(final String packageName)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n",
            packageName
        );
    }

    private CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            "public class %s\n" +
            "{\n",
            className
        );
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (final Writer out = outputManager.createOutput(META_ATTRIBUTE_ENUM))
        {
            out.append(String.format(
                "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
                "package %s;\n\n" +
                "public enum MetaAttribute\n" +
                "{\n" +
                "    EPOCH,\n" +
                "    TIME_UNIT,\n" +
                "    SEMANTIC_TYPE\n" +
                "}\n",
                ir.applicableNamespace()
            ));
        }
    }

    private CharSequence generateEnumDeclaration(final String name)
    {
        return "public enum " + name + "\n{\n";
    }

    private CharSequence generatePrimitivePropertyEncodings(
        final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                sb.append(generatePrimitiveProperty(containingClassName, token.name(), token, indent));
            }
        }

        return sb;
    }

    private CharSequence generatePrimitiveProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyMethods(containingClassName, propertyName, token, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyMethods(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final int arrayLength = token.arrayLength();

        if (arrayLength == 1)
        {
            return generateSingleValueProperty(containingClassName, propertyName, token, indent);
        }
        else if (arrayLength > 1)
        {
            return generateArrayProperty(containingClassName, propertyName, token, indent);
        }

        return "";
    }

    private CharSequence generatePrimitiveFieldMetaData(
        final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String javaTypeName = javaTypeName(primitiveType);

        sb.append(String.format(
            "\n" +
            indent + "    public static %s %sNullValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public static %s %sMinValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public static %s %sMaxValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())
        ));

        return sb;
    }

    private CharSequence generateSingleValueProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = encoding.byteOrder();
        final String byteOrderStr = encoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        return CodecUtil.%sGet(buffer, offset + %d%s);\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            generateFieldNotPresentCondition(token.version(), encoding, indent),
            typePrefix,
            offset,
            byteOrderStr
        ));

        sb.append(String.format(
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        CodecUtil.%sPut(buffer, offset + %d, value%s);\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            javaTypeName,
            typePrefix,
            offset,
            byteOrderStr
        ));

        return sb;
    }

    private CharSequence generateFieldNotPresentCondition(
        final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return %s;\n" +
            indent + "        }\n\n",
            Integer.valueOf(sinceVersion),
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString())
        );
    }

    private CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
            indent + "        }\n\n",
            Integer.valueOf(sinceVersion)
        );
    }

    private CharSequence generateTypeFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return null;\n" +
            indent + "        }\n\n",
            Integer.valueOf(sinceVersion)
        );
    }

    private CharSequence generateArrayProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = encoding.byteOrder();
        final String byteOrderStr = encoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;
        final Integer fieldLength = Integer.valueOf(token.arrayLength());
        final Integer typeSize = Integer.valueOf(encoding.primitiveType().size());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public static int %sLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            propertyName,
            fieldLength
        ));

        sb.append(String.format(
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            "%s" +
            indent + "        return CodecUtil.%sGet(buffer, this.offset + %d + (index * %d)%s);\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            fieldLength,
            generateFieldNotPresentCondition(token.version(), encoding, indent),
            typePrefix,
            offset,
            typeSize,
            byteOrderStr
        ));

        sb.append(String.format(
            indent + "    public void %s(final int index, final %s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        CodecUtil.%sPut(buffer, this.offset + %d + (index * %d), value%s);\n" +
            indent + "    }\n",
            propertyName,
            javaTypeName,
            fieldLength,
            typePrefix,
            offset,
            typeSize,
            byteOrderStr
        ));

        if (encoding.primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, encoding.characterEncoding(), indent);

            sb.append(String.format(
                "\n" +
                indent + "    public int get%s(final byte[] dst, final int dstOffset)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        if (dstOffset < 0 || dstOffset > (dst.length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                indent + "                \"dstOffset out of range for copy: offset=\" + dstOffset);\n" +
                indent + "        }\n\n" +
                "%s" +
                indent + "        CodecUtil.charsGet(buffer, this.offset + %d, dst, dstOffset, length);\n" +
                indent + "        return length;\n" +
                indent + "    }\n\n",
                toUpperFirstChar(propertyName),
                fieldLength,
                generateArrayFieldNotPresentCondition(token.version(), indent),
                offset
            ));

            sb.append(String.format(
                indent + "    public %s put%s(final byte[] src, final int srcOffset)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        if (srcOffset < 0 || srcOffset > (src.length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                indent + "                \"srcOffset out of range for copy: offset=\" + srcOffset);\n" +
                indent + "        }\n\n" +
                indent + "        CodecUtil.charsPut(buffer, this.offset + %d, src, srcOffset, length);\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                containingClassName,
                toUpperFirstChar(propertyName),
                fieldLength,
                offset
            ));
        }

        return sb;
    }

    private void generateCharacterEncodingMethod(
        final StringBuilder sb, final String propertyName, final String encoding, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static String %sCharacterEncoding()\n" +
            indent + "    {\n" +
            indent + "        return \"%s\";\n" +
            indent + "    }\n",
            formatPropertyName(propertyName),
            encoding
        ));
    }

    private CharSequence generateConstPropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        if (encoding.primitiveType() != PrimitiveType.CHAR)
        {
            return String.format(
                "\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
                javaTypeName(encoding.primitiveType()),
                propertyName,
                generateLiteral(encoding.primitiveType(), encoding.constValue().toString())
            );
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final byte[] constantValue = encoding.constValue().byteArrayValue(encoding.primitiveType());
        final CharSequence values = generateByteLiteralList(encoding.constValue().byteArrayValue(encoding.primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + "    private static final byte[] %sValue = {%s};\n",
            propertyName,
            values
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public static int %sLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            propertyName,
            Integer.valueOf(constantValue.length)
        ));

        sb.append(String.format(
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        return %sValue[index];\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            propertyName
        ));

        sb.append(String.format(
            indent + "    public int get%s(final byte[] dst, final int offset, final int length)\n" +
            indent + "    {\n" +
            indent + "        final int bytesCopied = Math.min(length, %d);\n" +
            indent + "        System.arraycopy(%sValue, 0, dst, offset, bytesCopied);\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            Integer.valueOf(constantValue.length),
            propertyName
        ));

        return sb;
    }

    private CharSequence generateByteLiteralList(final byte[] bytes)
    {
        final StringBuilder values = new StringBuilder();
        for (final byte b : bytes)
        {
            values.append(b).append(", ");
        }

        if (values.length() > 0)
        {
            values.setLength(values.length() - 2);
        }

        return values;
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        return String.format(
            "    private DirectBuffer buffer;\n" +
            "    private int offset;\n" +
            "    private int actingVersion;\n\n" +
            "    public %s wrap(final DirectBuffer buffer, final int offset, final int actingVersion)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public int size()\n" +
            "    {\n" +
            "        return %d;\n" +
            "    }\n",
            className,
            Integer.valueOf(size)
        );
    }

    private CharSequence generateMessageFlyweightCode(final String className, final Token token)
    {
        final String blockLengthType = javaTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = javaTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = javaTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = javaTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            "    public static final %1$s BLOCK_LENGTH = %2$s;\n" +
            "    public static final %3$s TEMPLATE_ID = %4$s;\n" +
            "    public static final %5$s SCHEMA_ID = %6$s;\n" +
            "    public static final %7$s SCHEMA_VERSION = %8$s;\n\n" +
            "    private final %9$s parentMessage = this;\n" +
            "    private DirectBuffer buffer;\n" +
            "    private int offset;\n" +
            "    private int limit;\n" +
            "    private int actingBlockLength;\n" +
            "    private int actingVersion;\n" +
            "\n" +
            "    public %1$s sbeBlockLength()\n" +
            "    {\n" +
            "        return BLOCK_LENGTH;\n" +
            "    }\n\n" +
            "    public %3$s sbeTemplateId()\n" +
            "    {\n" +
            "        return TEMPLATE_ID;\n" +
            "    }\n\n" +
            "    public %5$s sbeSchemaId()\n" +
            "    {\n" +
            "        return SCHEMA_ID;\n" +
            "    }\n\n" +
            "    public %7$s sbeSchemaVersion()\n" +
            "    {\n" +
            "        return SCHEMA_VERSION;\n" +
            "    }\n\n" +
            "    public String sbeSemanticType()\n" +
            "    {\n" +
            "        return \"%10$s\";\n" +
            "    }\n\n" +
            "    public int offset()\n" +
            "    {\n" +
            "        return offset;\n" +
            "    }\n\n" +
            "    public %9$s wrapForEncode(final DirectBuffer buffer, final int offset)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        this.actingBlockLength = BLOCK_LENGTH;\n" +
            "        this.actingVersion = SCHEMA_VERSION;\n" +
            "        limit(offset + actingBlockLength);\n\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public %9$s wrapForDecode(\n" +
            "        final DirectBuffer buffer, final int offset, final int actingBlockLength, final int actingVersion)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        this.actingBlockLength = actingBlockLength;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        limit(offset + actingBlockLength);\n\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public int size()\n" +
            "    {\n" +
            "        return limit - offset;\n" +
            "    }\n\n" +
            "    public int limit()\n" +
            "    {\n" +
            "        return limit;\n" +
            "    }\n\n" +
            "    public void limit(final int limit)\n" +
            "    {\n" +
            "        buffer.checkLimit(limit);\n" +
            "        this.limit = limit;\n" +
            "    }\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.size())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(token.version())),
            className,
            semanticType
        );
    }

    private CharSequence generateFields(final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());

                generateFieldIdMethod(sb, signalToken, indent);
                generateFieldMetaAttributeMethod(sb, signalToken, indent);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveProperty(containingClassName, propertyName, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(containingClassName, propertyName, encodingToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, encodingToken, indent));
                        break;
                }
            }
        }

        return sb;
    }

    private void generateFieldIdMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            token.name(),
            Integer.valueOf(token.id())
        ));
    }

    private void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append(String.format(
            "\n" +
            indent + "    public static String %sMetaAttribute(final MetaAttribute metaAttribute)\n" +
            indent + "    {\n" +
            indent + "        switch (metaAttribute)\n" +
            indent + "        {\n" +
            indent + "            case EPOCH: return \"%s\";\n" +
            indent + "            case TIME_UNIT: return \"%s\";\n" +
            indent + "            case SEMANTIC_TYPE: return \"%s\";\n" +
            indent + "        }\n\n" +
            indent + "        return \"\";\n" +
            indent + "    }\n",
            token.name(),
            epoch,
            timeUnit,
            semanticType
        ));
    }

    private CharSequence generateEnumProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String enumName = token.name();
        final Encoding encoding = token.encoding();
        final String typePrefix = encoding.primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = encoding.byteOrder();
        final String byteOrderStr = encoding.primitiveType().size() == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        return %s.get(CodecUtil.%sGet(buffer, offset + %d%s));\n" +
            indent + "    }\n\n",
            enumName,
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            enumName,
            typePrefix,
            offset,
            byteOrderStr
        ));

        sb.append(String.format(
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        CodecUtil.%sPut(buffer, offset + %d, value.value()%s);\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            enumName,
            typePrefix,
            offset,
            byteOrderStr
        ));

        return sb;
    }

    private Object generateBitSetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String bitSetName = formatClassName(token.name());
        final Integer offset = Integer.valueOf(token.offset());

        sb.append(String.format(
            "\n" +
            indent + "    private final %s %s = new %s();\n",
            bitSetName,
            propertyName,
            bitSetName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d, actingVersion);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            bitSetName,
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            propertyName,
            offset,
            propertyName
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.name());
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
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d, actingVersion);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            propertyName,
            offset,
            propertyName
        ));

        return sb;
    }

    private String generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        final String castType = javaTypeName(type);
        switch (type)
        {
            case CHAR:
            case UINT8:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + value;
                break;

            case UINT16:
            case INT32:
                literal = value;
                break;

            case UINT32:
                literal = value + "L";
                break;

            case FLOAT:
                if (value.endsWith("NaN"))
                {
                    literal = "Float.NaN";
                }
                else
                {
                    literal = value + "f";
                }
                break;

            case INT64:
                literal = value + "L";
                break;

            case UINT64:
                literal = "0x" + Long.toHexString(Long.parseLong(value)) + "L";
                break;

            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "Double.NaN";
                }
                else
                {
                    literal = value + "d";
                }
        }

        return literal;
    }
}
