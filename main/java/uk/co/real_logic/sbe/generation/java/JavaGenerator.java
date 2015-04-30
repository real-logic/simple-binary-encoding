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

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.Verify;
import uk.co.real_logic.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

public class JavaGenerator implements CodeGenerator
{
    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;
    private final String fullMutableBufferImplementation;
    private final String mutableBufferImplementation;
    private final String fullReadOnlyBufferImplementation;
    private final String readOnlyBufferImplementation;

    public JavaGenerator(
        final Ir ir,
        final String mutableBufferImplementation,
        final String readOnlyBufferImplementation,
        final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;

        this.mutableBufferImplementation = validateBufferImplementation(
            mutableBufferImplementation, MutableDirectBuffer.class);
        this.fullMutableBufferImplementation = mutableBufferImplementation;

        this.readOnlyBufferImplementation = validateBufferImplementation(
            readOnlyBufferImplementation, DirectBuffer.class);
        this.fullReadOnlyBufferImplementation = readOnlyBufferImplementation;
    }

    private String validateBufferImplementation(
        final String fullyQualifiedBufferImplementation, final Class<?> bufferClass)
    {
        Verify.notNull(fullyQualifiedBufferImplementation, "fullyQualifiedBufferImplementation");

        try
        {
            final Class<?> cls = Class.forName(fullyQualifiedBufferImplementation);
            if (!bufferClass.isAssignableFrom(cls))
            {
                throw new IllegalArgumentException(
                    fullyQualifiedBufferImplementation + " doesn't implement " + bufferClass.getName());
            }

            return cls.getSimpleName();
        }
        catch (final ClassNotFoundException ex)
        {
            throw new IllegalArgumentException(
                "Unable to validate " + fullyQualifiedBufferImplementation + " because it can't be found", ex);
        }
    }

    private String encoderName(final String className)
    {
        return className + "Encoder";
    }

    private String decoderName(final String className)
    {
        return className + "Decoder";
    }

    public void generateMessageHeaderStub() throws IOException
    {
        final List<Token> tokens = ir.headerStructure().tokens();

        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_TYPE))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullMutableBufferImplementation));
            out.append(generateClassDeclaration(MESSAGE_HEADER_TYPE));
            out.append(generateFixedFlyweightCode(
                MESSAGE_HEADER_TYPE, tokens.get(0).size(), false, mutableBufferImplementation));
            out.append(concatEncodingTokens(tokens, token ->
                generatePrimitiveEncoder(MESSAGE_HEADER_TYPE, token.name(), token, BASE_INDENT)));
            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(READ_ONLY_MESSAGE_HEADER_TYPE))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullReadOnlyBufferImplementation));
            out.append(generateClassDeclaration(READ_ONLY_MESSAGE_HEADER_TYPE));
            out.append(generateFixedFlyweightCode(
                READ_ONLY_MESSAGE_HEADER_TYPE, tokens.get(0).size(), false, readOnlyBufferImplementation));
            out.append(concatEncodingTokens(tokens, token ->
                generatePrimitiveDecoder(token.name(), token, BASE_INDENT)));
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
            final List<Token> messageBody = getMessageBody(tokens);

            int offset = 0;
            final List<Token> rootFields = new ArrayList<>();
            offset = collectRootFields(messageBody, offset, rootFields);
            final List<Token> groups = new ArrayList<>();
            offset = collectGroups(messageBody, offset, groups);
            final List<Token> varData = messageBody.subList(offset, messageBody.size());

            generateDecoder(groups, rootFields, varData, msgToken);
            generateEncoder(groups, rootFields, varData, msgToken);
        }
    }

    private void generateEncoder(final List<Token> groups,
                                 final List<Token> rootFields,
                                 final List<Token> varData,
                                 final Token msgToken) throws IOException
    {
        final String className = formatClassName(encoderName(msgToken.name()));

        try (final Writer out = outputManager.createOutput(className))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullMutableBufferImplementation));

            generateAnnotations(className, groups, out, 0, this::encoderName);
            out.append(generateClassDeclaration(className));
            out.append(generateEncoderFlyweightCode(className, msgToken));
            out.append(generateEncoderFields(className, rootFields, BASE_INDENT));

            final StringBuilder sb = new StringBuilder();
            generateEncoderGroups(sb, className, groups, 0, BASE_INDENT);
            out.append(sb);

            out.append(generateVarDataEncoders(varData));
            out.append("}\n");
        }
    }

    private void generateDecoder(final List<Token> groups,
                                 final List<Token> rootFields,
                                 final List<Token> varData,
                                 final Token msgToken) throws IOException
    {
        final String className = formatClassName(decoderName(msgToken.name()));

        try (final Writer out = outputManager.createOutput(className))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullReadOnlyBufferImplementation));

            generateAnnotations(className, groups, out, 0, this::decoderName);
            out.append(generateClassDeclaration(className));
            out.append(generateDecoderFlyweightCode(className, msgToken));
            out.append(generateDecoderFields(rootFields, BASE_INDENT));

            final StringBuilder sb = new StringBuilder();
            generateDecoderGroups(sb, className, groups, 0, BASE_INDENT);
            out.append(sb);

            out.append(generateVarDataDecoders(varData));
            out.append("}\n");
        }
    }

    private int generateDecoderGroups(
        final StringBuilder sb,
        final String parentMessageClassName,
        final List<Token> tokens,
        int index,
        final String indent)
        throws IOException
    {
        for (int size = tokens.size(); index < size; index++)
        {
            final Token groupToken = tokens.get(index);
            if (groupToken.signal() == Signal.BEGIN_GROUP)
            {
                final String groupName = decoderName(formatClassName(groupToken.name()));
                sb.append(generateGroupDecoderProperty(groupName, groupToken, indent));

                generateAnnotations(groupName, tokens, sb, index + 1, this::decoderName);
                generateGroupDecoderClassHeader(sb, groupName, parentMessageClassName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateDecoderFields(rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateDecoderGroups(sb, parentMessageClassName, tokens, index, indent + INDENT);
                }

                sb.append(indent).append("    }\n");
            }
        }

        return index;
    }

    private int generateEncoderGroups(
        final StringBuilder sb,
        final String parentMessageClassName,
        final List<Token> tokens,
        int index,
        final String indent)
        throws IOException
    {
        for (int size = tokens.size(); index < size; index++)
        {
            final Token groupToken = tokens.get(index);
            if (groupToken.signal() == Signal.BEGIN_GROUP)
            {
                final String groupName = groupToken.name();
                final String groupClassName = formatClassName(encoderName(groupName));
                sb.append(generateGroupEncoderProperty(groupName, groupToken, indent));

                generateAnnotations(groupClassName, tokens, sb, index + 1, this::encoderName);
                generateGroupEncoderClassHeader(sb, groupName, parentMessageClassName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateEncoderFields(groupClassName, rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateEncoderGroups(sb, parentMessageClassName, tokens, index, indent + INDENT);
                }

                sb.append(indent).append("    }\n");
            }
        }

        return index;
    }

    private void generateGroupDecoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<Token> tokens,
        final int index,
        final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final int dimensionHeaderSize = tokens.get(index + 1).size();

        generateDecoderClassDeclaration(sb, groupName, parentMessageClassName, indent, dimensionsClassName, dimensionHeaderSize);

        sb.append(String.format(
            indent + "    public void wrap(\n" +
            indent + "        final %s parentMessage, final %s buffer, final int actingVersion)\n" +
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
            parentMessageClassName,
            readOnlyBufferImplementation
        ));

        final int blockLength = tokens.get(index).size();

        sb.append(indent).append("    public static int sbeHeaderSize()\n")
          .append(indent).append("    {\n")
          .append(indent).append("        return HEADER_SIZE;\n")
          .append(indent).append("    }\n\n");

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

    private void generateGroupEncoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<Token> tokens,
        final int index,
        final String indent)
    {
        final String dimensionsClassName = formatClassName(encoderName(tokens.get(index + 1).name()));
        final int dimensionHeaderSize = tokens.get(index + 1).size();

        generateEncoderClassDeclaration(sb, groupName, parentMessageClassName, indent, dimensionsClassName, dimensionHeaderSize);

        final int blockLength = tokens.get(index).size();
        final String javaTypeForBlockLength = primitiveTypeName(tokens.get(index + 2));
        final String javaTypeForNumInGroup = primitiveTypeName(tokens.get(index + 3));

        sb.append(String.format(
            indent + "    public void wrap(final %1$s parentMessage, final %5$s buffer, final int count)\n" +
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
            javaTypeForNumInGroup,
            mutableBufferImplementation
        ));

        sb.append(indent).append("    public static int sbeHeaderSize()\n")
          .append(indent).append("    {\n")
          .append(indent).append("        return HEADER_SIZE;\n")
          .append(indent).append("    }\n\n");

        sb.append(String.format(
            indent + "    public static int sbeBlockLength()\n" +
                indent + "    {\n" +
                indent + "        return %d;\n" +
                indent + "    }\n\n",
            blockLength
        ));

        sb.append(String.format(
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
            formatClassName(encoderName(groupName))
        ));
    }

    private String primitiveTypeName(final Token token)
    {
        return javaTypeName(token.encoding().primitiveType());
    }

    private void generateDecoderClassDeclaration(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final String indent,
        final String dimensionsClassName,
        final int dimensionHeaderSize)
    {
        sb.append(String.format(
            "\n" +
            indent + "public static class %1$s\n" +
            indent + "implements Iterable<%1$s>, java.util.Iterator<%1$s>\n" +
            indent + "{\n" +
            indent + "    private static final int HEADER_SIZE = %2$d;\n" +
            indent + "    private final %3$s dimensions = new %3$s();\n" +
            indent + "    private %4$s parentMessage;\n" +
            indent + "    private %5$s buffer;\n" +
            indent + "    private int blockLength;\n" +
            indent + "    private int actingVersion;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n\n",
            formatClassName(groupName),
            dimensionHeaderSize,
            decoderName(dimensionsClassName),
            parentMessageClassName,
            readOnlyBufferImplementation
        ));
    }

    private void generateEncoderClassDeclaration(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final String indent,
        final String dimensionsClassName,
        final int dimensionHeaderSize)
    {
        sb.append(String.format(
            "\n" +
            indent + "public static class %1$s\n" +
            indent + "{\n" +
            indent + "    private static final int HEADER_SIZE = %2$d;\n" +
            indent + "    private final %3$s dimensions = new %3$s();\n" +
            indent + "    private %4$s parentMessage;\n" +
            indent + "    private %5$s buffer;\n" +
            indent + "    private int blockLength;\n" +
            indent + "    private int actingVersion;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n\n",
            formatClassName(encoderName(groupName)),
            dimensionHeaderSize,
            dimensionsClassName,
            parentMessageClassName,
            mutableBufferImplementation
        ));
    }

    private CharSequence generateGroupDecoderProperty(final String groupName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(token.name());

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
            formatPropertyName(groupName),
            token.id()
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s()\n" +
            indent + "    {\n" +
            indent + "        %2$s.wrap(parentMessage, buffer, actingVersion);\n" +
            indent + "        return %2$s;\n" +
            indent + "    }\n",
            className,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateGroupEncoderProperty(final String groupName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(encoderName(groupName));
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
            formatPropertyName(groupName),
            token.id()
        ));

        sb.append(String.format(
            "\n" +
                indent + "    public %1$s %2$sCount(final int count)\n" +
                indent + "    {\n" +
                indent + "        %2$s.wrap(parentMessage, buffer, count);\n" +
                indent + "        return %2$s;\n" +
                indent + "    }\n",
            className,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateVarDataDecoders(final List<Token> tokens)
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
            final int sizeOfLengthField = lengthToken.size();
            final Encoding lengthEncoding = lengthToken.encoding();
            final String lengthTypePrefix = lengthEncoding.primitiveType().primitiveName();
            final String byteOrderStr = byteOrderString(lengthEncoding);

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
                "    public int %sLength()\n" +
                "    {\n" +
                "%s" +
                "        final int sizeOfLengthField = %d;\n" +
                "        final int limit = limit();\n" +
                "        buffer.checkLimit(limit + sizeOfLengthField);\n\n" +
                "        return CodecUtil.%sGet(buffer, limit%s);\n" +
                "    }\n",
                toLowerFirstChar(propertyName),
                generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
                sizeOfLengthField,
                lengthTypePrefix,
                byteOrderStr
            ));

            generateVarDataDecodeMethods(
                sb, token, propertyName, sizeOfLengthField, lengthTypePrefix, byteOrderStr, characterEncoding);
        }

        return sb;
    }

    private CharSequence generateVarDataEncoders(final List<Token> tokens)
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
            final int sizeOfLengthField = lengthToken.size();
            final Encoding lengthEncoding = lengthToken.encoding();
            final String lengthJavaType = javaTypeName(lengthEncoding.primitiveType());
            final String lengthTypePrefix = lengthEncoding.primitiveType().primitiveName();
            final String byteOrderStr = byteOrderString(lengthEncoding);

            generateVarDataEncodeMethods(
                sb, propertyName, sizeOfLengthField, lengthJavaType, lengthTypePrefix, byteOrderStr, characterEncoding);
        }

        return sb;
    }

    private void generateVarDataDecodeMethods(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final String lengthTypePrefix,
        final String byteOrderStr,
        final String characterEncoding)
    {
        generateVarDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            fullMutableBufferImplementation,
            lengthTypePrefix,
            byteOrderStr);

        generateVarDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            "byte[]",
            lengthTypePrefix,
            byteOrderStr);

        sb.append(String.format(
            "\n" +
                "    public String %1$s()\n" +
            "    {\n" +
            "%2$s" +
            "        final int sizeOfLengthField = %3$d;\n" +
            "        final int limit = limit();\n" +
            "        buffer.checkLimit(limit + sizeOfLengthField);\n" +
            "        final int dataLength = CodecUtil.%4$sGet(buffer, limit%5$s);\n" +
            "        limit(limit + sizeOfLengthField + dataLength);\n" +
            "        final byte[] tmp = new byte[dataLength];\n" +
            "        buffer.getBytes(limit + sizeOfLengthField, tmp, 0, dataLength);\n\n" +
            "        final String value;\n" +
            "        try\n" +
            "        {\n" +
            "            value = new String(tmp, \"%6$s\");\n" +
            "        }\n" +
            "        catch (final java.io.UnsupportedEncodingException ex)\n" +
            "        {\n" +
            "            throw new RuntimeException(ex);\n" +
            "        }\n\n" +
            "        return value;\n" +
            "    }\n",
            toLowerFirstChar(propertyName),
            generateStringNotPresentCondition(token.version(), BASE_INDENT),
            sizeOfLengthField,
            lengthTypePrefix,
            byteOrderStr,
            characterEncoding
        ));
    }

    private void generateVarDataEncodeMethods(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final String lengthJavaType,
        final String lengthTypePrefix,
        final String byteOrderStr,
        final String characterEncoding)
    {
        generateVarDataTypedEncoder(
            sb,
            propertyName,
            sizeOfLengthField,
            fullMutableBufferImplementation,
            lengthJavaType,
            lengthTypePrefix,
            byteOrderStr);

        generateVarDataTypedEncoder(
            sb,
            propertyName,
            sizeOfLengthField,
            "byte[]",
            lengthJavaType,
            lengthTypePrefix,
            byteOrderStr);

        sb.append(String.format(
            "\n" +
            "    public void %1$s(final String value)\n" +
            "    {\n" +
            "        final byte[] bytes;\n" +
            "        try\n" +
            "        {\n" +
            "            bytes = value.getBytes(\"%2$s\");\n" +
            "        }\n" +
            "        catch (final java.io.UnsupportedEncodingException ex)\n" +
            "        {\n" +
            "            throw new RuntimeException(ex);\n" +
            "        }\n\n" +
            "        final int length = bytes.length;\n" +
            "        final int sizeOfLengthField = %3$d;\n" +
            "        final int limit = limit();\n" +
            "        limit(limit + sizeOfLengthField + length);\n" +
            "        CodecUtil.%4$sPut(buffer, limit, (%5$s)length%6$s);\n" +
            "        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);\n" +
            "    }\n",
            toLowerFirstChar(propertyName),
            characterEncoding,
            sizeOfLengthField,
            lengthTypePrefix,
            lengthJavaType,
            byteOrderStr
        ));
    }

    private void generateVarDataTypedDecoder(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final String exchangeType,
        final String lengthTypePrefix,
        final String byteOrderStr)
    {
        sb.append(String.format(
            "\n" +
            "    public int get%s(final %s dst, final int dstOffset, final int length)\n" +
            "    {\n" +
            "%s" +
            "        final int sizeOfLengthField = %d;\n" +
            "        final int limit = limit();\n" +
            "        buffer.checkLimit(limit + sizeOfLengthField);\n" +
            "        final int dataLength = CodecUtil.%sGet(buffer, limit%s);\n" +
            "        final int bytesCopied = Math.min(length, dataLength);\n" +
            "        limit(limit + sizeOfLengthField + dataLength);\n" +
            "        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);\n\n" +
            "        return bytesCopied;\n" +
            "    }\n",
            propertyName,
            exchangeType,
            generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
            sizeOfLengthField,
            lengthTypePrefix,
            byteOrderStr
        ));
    }

    private void generateVarDataTypedEncoder(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final String exchangeType,
        final String lengthJavaType,
        final String lengthTypePrefix,
        final String byteOrderStr)
    {
        sb.append(String.format(
            "\n" +
            "    public int put%s(final %s src, final int srcOffset, final int length)\n" +
            "    {\n" +
            "        final int sizeOfLengthField = %d;\n" +
            "        final int limit = limit();\n" +
            "        limit(limit + sizeOfLengthField + length);\n" +
            "        CodecUtil.%sPut(buffer, limit, (%s)length%s);\n" +
            "        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);\n\n" +
            "        return length;\n" +
            "    }\n",
            propertyName,
            exchangeType,
            sizeOfLengthField,
            lengthTypePrefix,
            lengthJavaType,
            byteOrderStr
        ));
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String bitSetName = formatClassName(token.name());
        final String decoderName = decoderName(bitSetName);
        final String encoderName = encoderName(bitSetName);
        final List<Token> messageBody = getMessageBody(tokens);

        try (final Writer out = outputManager.createOutput(decoderName))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullReadOnlyBufferImplementation));
            out.append(generateClassDeclaration(decoderName));
            out.append(generateFixedFlyweightCode(decoderName, token.size(), false, readOnlyBufferImplementation));
            out.append(generateChoiceDecoders(messageBody));

            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(encoderName))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullMutableBufferImplementation));
            out.append(generateClassDeclaration(encoderName));
            out.append(generateFixedFlyweightCode(encoderName, token.size(), false, mutableBufferImplementation));
            out.append(generateChoiceClear(encoderName, token));
            out.append(generateChoiceEncoders(encoderName, messageBody));

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

            out.append(generateEnumValues(getMessageBody(tokens)));
            out.append(generateEnumBody(tokens.get(0), enumName));

            out.append(generateEnumLookupMethod(getMessageBody(tokens), enumName));

            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String compositeName = formatClassName(token.name());
        final String decoderName = decoderName(compositeName);
        final String encoderName = encoderName(compositeName);

        final List<Token> messageBody = getMessageBody(tokens);

        try (final Writer out = outputManager.createOutput(decoderName))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullReadOnlyBufferImplementation));
            out.append(generateClassDeclaration(decoderName));
            out.append(generateFixedFlyweightCode(decoderName, token.size(), false, readOnlyBufferImplementation));

            out.append(concatEncodingTokens(messageBody,
                tok -> generatePrimitiveDecoder(tok.name(), tok, BASE_INDENT)));

            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(encoderName))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), fullMutableBufferImplementation));
            out.append(generateClassDeclaration(encoderName));
            out.append(generateFixedFlyweightCode(encoderName, token.size(), false, mutableBufferImplementation));

            out.append(concatEncodingTokens(messageBody,
                tok -> generatePrimitiveEncoder(encoderName, tok.name(), tok, BASE_INDENT)));

            out.append("}\n");
        }
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String typePrefix = encoding.primitiveType().primitiveName();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final String byteOrderStr = byteOrderString(encoding);

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

    private CharSequence generateChoiceDecoders(final List<Token> tokens)
    {
        return concatTokens(tokens, Signal.CHOICE, (token) ->
        {
            final String choiceName = token.name();
            final Encoding encoding = token.encoding();
            final String typePrefix = encoding.primitiveType().primitiveName();
            final String choiceBitPosition = encoding.constValue().toString();
            final String byteOrderStr = byteOrderString(encoding);

            return String.format(
                "\n" +
                "    public boolean %s()\n" +
                "    {\n" +
                "        return CodecUtil.%sGetChoice(buffer, offset, %s%s);\n" +
                "    }\n\n",
                choiceName,
                typePrefix,
                choiceBitPosition,
                byteOrderStr
            );
        });
    }

    private CharSequence generateChoiceEncoders(final String bitSetClassName, final List<Token> tokens)
    {
        return concatTokens(tokens, Signal.CHOICE, (token) ->
        {
            final String choiceName = token.name();
            final Encoding encoding = token.encoding();
            final String typePrefix = encoding.primitiveType().primitiveName();
            final String choiceBitPosition = encoding.constValue().toString();
            final String byteOrderStr = byteOrderString(encoding);

            return String.format(
                "\n" +
                "    public %s %s(final boolean value)\n" +
                "    {\n" +
                "        CodecUtil.%sPutChoice(buffer, offset, %s, value%s);\n" +
                "        return this;\n" +
                "    }\n",
                bitSetClassName,
                choiceName,
                typePrefix,
                choiceBitPosition,
                byteOrderStr
            );
        });
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
        final String javaEncodingType = primitiveTypeName(token);

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

    private CharSequence generateFileHeader(final String packageName, final String fqBuffer)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n" +
            "import uk.co.real_logic.sbe.codec.java.*;\n" +
            "import %s;\n\n",
            packageName,
            fqBuffer
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

    private void generateAnnotations(
        final String className,
        final List<Token> tokens,
        final Appendable out,
        int index,
        final Function<String, String> nameMapping)
        throws IOException
    {
        final List<String> groupClassNames = new ArrayList<>();
        int level = 0;

        for (int size = tokens.size(); index < size; index++)
        {
            if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
            {
                if (++level == 1)
                {
                    final Token groupToken = tokens.get(index);
                    final String groupName = groupToken.name();
                    groupClassNames.add(formatClassName(nameMapping.apply(groupName)));
                }
            }
            else if (tokens.get(index).signal() == Signal.END_GROUP)
            {
                if (--level < 0)
                {
                    break;
                }
            }
        }

        if (groupClassNames.isEmpty())
        {
            return;
        }

        out.append("@GroupOrder({");
        index = 0;
        for (final String name : groupClassNames)
        {
            out.append(className).append('.').append(name).append(".class");
            if (++index < groupClassNames.size())
            {
                out.append(", ");
            }
        }

        out.append("})\n");
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

    private CharSequence generatePrimitiveDecoder(
        final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (token.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyDecodeMethods(propertyName, token, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitiveEncoder(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (token.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyEncodeMethods(containingClassName, propertyName, token, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyDecodeMethods(
        final String propertyName, final Token token, final String indent)
    {
        return token.switchArray(
            () -> generateSingleValuePropertyDecode(propertyName, token, indent),
            () -> generateArrayPropertyDecode(propertyName, token, indent));
    }

    private CharSequence generatePrimitivePropertyEncodeMethods(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        return token.switchArray(
            () -> generateSingleValuePropertyEncode(containingClassName, propertyName, token, indent),
            () -> generateArrayPropertyEncode(containingClassName, propertyName, token, indent));
    }

    private CharSequence generatePrimitiveFieldMetaData(final String propertyName, final Token token, final String indent)
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

    private CharSequence generateSingleValuePropertyDecode(
        final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
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
        );
    }

    private CharSequence generateSingleValuePropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
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
        );
    }

    private CharSequence generateFieldNotPresentCondition(final int sinceVersion, final Encoding encoding, final String indent)
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
            sinceVersion,
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
            sinceVersion
        );
    }

    private CharSequence generateStringNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return \"\";\n" +
            indent + "        }\n\n",
            sinceVersion
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
            sinceVersion
        );
    }

    private CharSequence generateArrayPropertyDecode(final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);
        final int fieldLength = token.arrayLength();
        final int typeSize = sizeOfPrimitive(encoding);

        final StringBuilder sb = new StringBuilder();

        generateArrayLengthMethod(propertyName, indent, fieldLength, sb);

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
                indent +               "\"dstOffset out of range for copy: offset=\" + dstOffset);\n" +
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
        }

        return sb;
    }

    private void generateArrayLengthMethod(String propertyName, String indent, int fieldLength, StringBuilder sb)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            propertyName,
            fieldLength
        ));
    }

    private String byteOrderString(final Encoding encoding)
    {
        final ByteOrder byteOrder = encoding.byteOrder();
        return sizeOfPrimitive(encoding) == 1 ? "" : ", java.nio.ByteOrder." + byteOrder;
    }

    private CharSequence generateArrayPropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);
        final int fieldLength = token.arrayLength();
        final int typeSize = sizeOfPrimitive(encoding);

        final StringBuilder sb = new StringBuilder();

        generateArrayLengthMethod(propertyName, indent, fieldLength, sb);

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
                indent + "    public %s put%s(final byte[] src, final int srcOffset)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        if (srcOffset < 0 || srcOffset > (src.length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                indent +               "\"srcOffset out of range for copy: offset=\" + srcOffset);\n" +
                indent + "        }\n\n" +
                indent + "        CodecUtil.charsPut(buffer, this.offset + %d, src, srcOffset, length);\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                toUpperFirstChar(propertyName),
                fieldLength,
                offset
            ));
        }

        return sb;
    }

    private int sizeOfPrimitive(final Encoding encoding)
    {
        return encoding.primitiveType().size();
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
            indent + "    private static final byte[] %s_VALUE = {%s};\n",
            propertyName.toUpperCase(),
            values
        ));

        generateArrayLengthMethod(propertyName, indent, constantValue.length, sb);

        sb.append(String.format(
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        return %s_VALUE[index];\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            propertyName.toUpperCase()
        ));

        sb.append(String.format(
            indent + "    public int get%s(final byte[] dst, final int offset, final int length)\n" +
            indent + "    {\n" +
            indent + "        final int bytesCopied = Math.min(length, %d);\n" +
            indent + "        System.arraycopy(%s_VALUE, 0, dst, offset, bytesCopied);\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            constantValue.length,
            propertyName.toUpperCase()
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

    private CharSequence generateFixedFlyweightCode(
        final String className,
        final int size,
        final boolean callsSuper,
        final String bufferImplementation)
    {
        final String body = callsSuper ? "        super.wrap(buffer, offset, actingVersion);\n" : "";

        return String.format(
            "    private %3$s buffer;\n" +
            "    private int offset;\n" +
            "    private int actingVersion;\n\n" +
            "    public %1$s wrap(final %3$s buffer, final int offset, final int actingVersion)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "%4$s" +
            "        this.offset = offset;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public int size()\n" +
            "    {\n" +
            "        return %2$d;\n" +
            "    }\n",
            className,
            size,
            bufferImplementation,
            body
        );
    }

    private CharSequence generateDecoderFlyweightCode(final String className, final Token token)
    {
        final String wrapMethod = String.format(
            "    public %1$s wrap(\n" +
            "        final %2$s buffer, final int offset, final int actingBlockLength, final int actingVersion)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        this.actingBlockLength = actingBlockLength;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        limit(offset + actingBlockLength);\n\n" +
            "        return this;\n" +
            "    }\n\n",
            className,
            readOnlyBufferImplementation);

        return generateFlyweightCode(className, token, wrapMethod, readOnlyBufferImplementation);
    }

    private CharSequence generateFlyweightCode(
        final String className, final Token token, final String wrapMethod, final String bufferImplementation)
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
            "    private %11$s buffer;\n" +
            "    protected int offset;\n" +
            "    protected int limit;\n" +
            "    protected int actingBlockLength;\n" +
            "    protected int actingVersion;\n" +
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
            "%12$s" +
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
            semanticType,
            bufferImplementation,
            wrapMethod
        );
    }

    private CharSequence generateEncoderFlyweightCode(final String className, final Token token)
    {
        final String wrapMethod = String.format(
            "    public %1$s wrap(final %2$s buffer, final int offset)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "        this.offset = offset;\n" +
            "        limit(offset + BLOCK_LENGTH);\n" +
            "        return this;\n" +
            "    }\n\n",
            className,
            mutableBufferImplementation);

        return generateFlyweightCode(className, token, wrapMethod, mutableBufferImplementation);
    }

    private CharSequence generateEncoderFields(final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        eachField(tokens, (signalToken, encodingToken) ->
        {
            final String propertyName = formatPropertyName(signalToken.name());
            final String typeName = formatClassName(encoderName(encodingToken.name()));

            switch (encodingToken.signal())
            {
                case ENCODING:
                    sb.append(generatePrimitiveEncoder(containingClassName, propertyName, encodingToken, indent));
                    break;

                case BEGIN_ENUM:
                    sb.append(generateEnumEncoder(containingClassName, propertyName, encodingToken, indent));
                    break;

                case BEGIN_SET:
                    sb.append(generateBitSetProperty(propertyName, encodingToken, indent, typeName));
                    break;

                case BEGIN_COMPOSITE:
                    sb.append(generateCompositeProperty(propertyName, encodingToken, indent, typeName));
                    break;
            }
        });

        return sb;
    }

    private CharSequence generateDecoderFields(
        final List<Token> tokens,
        final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        eachField(tokens, (signalToken, encodingToken) ->
        {
            final String propertyName = formatPropertyName(signalToken.name());
            final String typeName = decoderName(formatClassName(encodingToken.name()));

            generateFieldIdMethod(sb, signalToken, indent);
            generateFieldMetaAttributeMethod(sb, signalToken, indent);

            switch (encodingToken.signal())
            {
                case ENCODING:
                    sb.append(generatePrimitiveDecoder(propertyName, encodingToken, indent));
                    break;

                case BEGIN_ENUM:
                    sb.append(generateEnumDecoder(propertyName, encodingToken, indent));
                    break;

                case BEGIN_SET:
                    sb.append(generateBitSetProperty(propertyName, encodingToken, indent, typeName));
                    break;

                case BEGIN_COMPOSITE:
                    sb.append(generateCompositeProperty(propertyName, encodingToken, indent, typeName));
                    break;
            }
        });

        return sb;
    }

    private void eachField(final List<Token> tokens, BiConsumer<Token, Token> consumer)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);

                consumer.accept(signalToken, encodingToken);
            }
        }
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
            token.id()
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

    private CharSequence generateEnumDecoder(
        final String propertyName, final Token token, final String indent)
    {
        final String enumName = formatClassName(token.name());
        final Encoding encoding = token.encoding();
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
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
        );
    }

    private CharSequence generateEnumEncoder(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String enumName = formatClassName(token.name());
        final Encoding encoding = token.encoding();
        final String typePrefix = encoding.primitiveType().primitiveName();
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
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
        );
    }

    private Object generateBitSetProperty(
        final String propertyName,
        final Token token,
        final String indent,
        final String bitSetName)
    {
        final StringBuilder sb = new StringBuilder();
        final int offset = token.offset();

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

    private CharSequence generateCompositeProperty(
        final String propertyName,
        final Token token,
        final String indent,
        final String compositeName)
    {
        final int offset = token.offset();

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
