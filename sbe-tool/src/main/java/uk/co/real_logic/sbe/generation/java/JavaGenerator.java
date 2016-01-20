/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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
import uk.co.real_logic.sbe.ir.*;

import java.io.IOException;
import java.io.Writer;
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
    private final String fullMutableBuffer;
    private final String mutableBuffer;
    private final String fullReadOnlyBuffer;
    private final String readOnlyBuffer;
    private final boolean shouldGenerateGroupOrderAnnotation;

    public JavaGenerator(
        final Ir ir,
        final String mutableBuffer,
        final String readOnlyBuffer,
        final boolean shouldGenerateGroupOrderAnnotation,
        final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;

        this.mutableBuffer = validateBufferImplementation(mutableBuffer, MutableDirectBuffer.class);
        this.fullMutableBuffer = mutableBuffer;

        this.readOnlyBuffer = validateBufferImplementation(readOnlyBuffer, DirectBuffer.class);
        this.fullReadOnlyBuffer = readOnlyBuffer;

        this.shouldGenerateGroupOrderAnnotation = shouldGenerateGroupOrderAnnotation;
    }

    private static String validateBufferImplementation(
        final String fullyQualifiedBufferImplementation, final Class<?> bufferClass)
    {
        Verify.notNull(fullyQualifiedBufferImplementation, "fullyQualifiedBufferImplementation");

        try
        {
            final Class<?> clazz = Class.forName(fullyQualifiedBufferImplementation);
            if (!bufferClass.isAssignableFrom(clazz))
            {
                throw new IllegalArgumentException(
                    fullyQualifiedBufferImplementation + " doesn't implement " + bufferClass.getName());
            }

            return clazz.getSimpleName();
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
        final Token firstToken = tokens.get(0);
        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_ENCODER_TYPE))
        {
            generateFixedFlyweightHeader(firstToken, MESSAGE_HEADER_ENCODER_TYPE, out, mutableBuffer, fullMutableBuffer);
            out.append(concatEncodingTokens(
                tokens, (token) -> generatePrimitiveEncoder(MESSAGE_HEADER_ENCODER_TYPE, token.name(), token, BASE_INDENT)));
            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_DECODER_TYPE))
        {
            generateFixedFlyweightHeader(firstToken, MESSAGE_HEADER_DECODER_TYPE, out, readOnlyBuffer, fullReadOnlyBuffer);
            out.append(concatEncodingTokens(
                tokens, (token) -> generatePrimitiveDecoder(token.name(), token, BASE_INDENT)));
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

            int i = 0;
            final List<Token> fields = new ArrayList<>();
            i = collectFields(messageBody, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(messageBody, i, groups);

            final List<Token> varData = new ArrayList<>();
            collectVarData(messageBody, i, varData);

            generateDecoder(BASE_INDENT, fields, groups, varData, msgToken);
            generateEncoder(BASE_INDENT, fields, groups, varData, msgToken);
        }
    }

    private void generateEncoder(
        final String indent,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Token msgToken) throws IOException
    {
        final String className = formatClassName(encoderName(msgToken.name()));

        try (final Writer out = outputManager.createOutput(className))
        {
            out.append(generateFileHeader(className, ir.applicableNamespace(), fullMutableBuffer));

            generateAnnotations(indent, className, groups, out, 0, this::encoderName);
            out.append(generateClassDeclaration(className));
            out.append(generateEncoderFlyweightCode(className, msgToken));
            out.append(generateEncoderFields(className, fields, indent));

            final StringBuilder sb = new StringBuilder();
            generateEncoderGroups(sb, className, groups, indent);
            out.append(sb);

            out.append(generateEncoderVarData(className, varData, indent));
            out.append("}\n");
        }
    }

    private void generateDecoder(
        final String indent,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Token msgToken) throws IOException
    {
        final String className = formatClassName(decoderName(msgToken.name()));

        try (final Writer out = outputManager.createOutput(className))
        {
            out.append(generateFileHeader(className, ir.applicableNamespace(), fullReadOnlyBuffer));

            generateAnnotations(indent, className, groups, out, 0, this::decoderName);
            out.append(generateClassDeclaration(className));
            out.append(generateDecoderFlyweightCode(className, msgToken));
            out.append(generateDecoderFields(fields, BASE_INDENT));

            final StringBuilder sb = new StringBuilder();
            generateDecoderGroups(sb, className, groups, BASE_INDENT);
            out.append(sb);

            out.append(generateDecoderVarData(varData, BASE_INDENT));

            out.append("}\n");
        }
    }

    private void generateDecoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final List<Token> tokens,
        final String indent) throws IOException
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final String groupName = decoderName(formatClassName(groupToken.name()));
            sb.append(generateGroupDecoderProperty(groupName, groupToken, indent));

            generateAnnotations(indent + INDENT, groupName, tokens, sb, i + 1, this::decoderName);
            generateGroupDecoderClassHeader(sb, groupName, outerClassName, tokens, i, indent + INDENT);

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateDecoderFields(fields, indent + INDENT));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateDecoderGroups(sb, outerClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            sb.append(generateDecoderVarData(varData, indent + INDENT));

            sb.append(indent).append("    }\n");
        }
    }

    private void generateEncoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final List<Token> tokens,
        final String indent) throws IOException
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final String groupName = groupToken.name();
            final String groupClassName = formatClassName(encoderName(groupName));
            sb.append(generateGroupEncoderProperty(groupName, groupToken, indent));

            generateAnnotations(indent + INDENT, groupClassName, tokens, sb, i + 1, this::encoderName);
            generateGroupEncoderClassHeader(sb, groupName, outerClassName, tokens, i, indent + INDENT);

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateEncoderFields(groupClassName, fields, indent + INDENT));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateEncoderGroups(sb, outerClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            sb.append(generateEncoderVarData(groupClassName, varData, indent + INDENT));

            sb.append(indent).append("    }\n");
        }
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
        final int dimensionHeaderLen = tokens.get(index + 1).encodedLength();

        generateDecoderClassDeclaration(sb, groupName, parentMessageClassName, indent, dimensionsClassName, dimensionHeaderLen);

        sb.append(String.format(
            indent + "    public void wrap(\n" +
            indent + "        final %s parentMessage, final %s buffer)\n" +
            indent + "    {\n" +
            indent + "        this.parentMessage = parentMessage;\n" +
            indent + "        this.buffer = buffer;\n" +
            indent + "        dimensions.wrap(buffer, parentMessage.limit());\n" +
            indent + "        blockLength = dimensions.blockLength();\n" +
            indent + "        count = dimensions.numInGroup();\n" +
            indent + "        index = -1;\n" +
            indent + "        parentMessage.limit(parentMessage.limit() + HEADER_SIZE);\n" +
            indent + "    }\n\n",
            parentMessageClassName,
            readOnlyBuffer
        ));

        final int blockLength = tokens.get(index).encodedLength();

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
            indent + "    public java.util.Iterator<%s> iterator()\n" +
            indent + "    {\n" +
            indent + "        return this;\n" +
            indent + "    }\n\n" +
            indent + "    public void remove()\n" +
            indent + "    {\n" +
            indent + "        throw new UnsupportedOperationException();\n" +
            indent + "    }\n\n" +
            indent + "    public boolean hasNext()\n" +
            indent + "    {\n" +
            indent + "        return (index + 1) < count;\n" +
            indent + "    }\n\n",
            formatClassName(groupName)
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
        final int dimensionHeaderSize = tokens.get(index + 1).encodedLength();

        generateEncoderClassDeclaration(sb, groupName, parentMessageClassName, indent, dimensionsClassName, dimensionHeaderSize);

        final int blockLength = tokens.get(index).encodedLength();
        final String javaTypeForBlockLength = primitiveTypeName(tokens.get(index + 2));
        final String javaTypeForNumInGroup = primitiveTypeName(tokens.get(index + 3));

        sb.append(String.format(
            indent + "    public void wrap(\n" +
            indent + "        final %1$s parentMessage, final %5$s buffer, final int count)\n" +
            indent + "    {\n" +
            indent + "        this.parentMessage = parentMessage;\n" +
            indent + "        this.buffer = buffer;\n" +
            indent + "        actingVersion = SCHEMA_VERSION;\n" +
            indent + "        dimensions.wrap(buffer, parentMessage.limit());\n" +
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
            mutableBuffer
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

    private static String primitiveTypeName(final Token token)
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
            indent + "    implements Iterable<%1$s>, java.util.Iterator<%1$s>\n" +
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
            readOnlyBuffer
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
            mutableBuffer
        ));
    }

    private static CharSequence generateGroupDecoderProperty(final String groupName, final Token token, final String indent)
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
            indent + "        %2$s.wrap(parentMessage, buffer);\n" +
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

    private CharSequence generateDecoderVarData(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token token = tokens.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }

            generateFieldIdMethod(sb, token, indent);
            final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
            generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
            generateFieldMetaAttributeMethod(sb, token, indent);

            final String propertyName = toUpperFirstChar(token.name());
            final Token lengthToken = tokens.get(i + 2);
            final int sizeOfLengthField = lengthToken.encodedLength();
            final Encoding lengthEncoding = lengthToken.encoding();
            final PrimitiveType lengthType = lengthEncoding.primitiveType();
            final String byteOrderStr = byteOrderString(lengthEncoding);

            sb.append(String.format(
                "\n" +
                indent + "    public static int %sHeaderLength()\n" +
                indent + "    {\n" +
                indent + "        return %d;\n" +
                indent + "    }\n",
                toLowerFirstChar(propertyName),
                sizeOfLengthField
            ));

            sb.append(String.format(
                "\n" +
                indent + "    public int %sLength()\n" +
                indent + "    {\n" +
                         "%s" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
                toLowerFirstChar(propertyName),
                generateArrayFieldNotPresentCondition(token.version(), indent),
                generateGet(lengthType, "limit", byteOrderStr)
            ));

            generateDataDecodeMethods(
                sb, token, propertyName, sizeOfLengthField, lengthType, byteOrderStr, characterEncoding, indent);

            i += token.componentTokenCount();
        }

        return sb;
    }

    private CharSequence generateEncoderVarData(final String className, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token token = tokens.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }

            generateFieldIdMethod(sb, token, indent);
            final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
            generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
            generateFieldMetaAttributeMethod(sb, token, indent);

            final String propertyName = toUpperFirstChar(token.name());
            final Token lengthToken = tokens.get(i + 2);
            final int sizeOfLengthField = lengthToken.encodedLength();
            final Encoding lengthEncoding = lengthToken.encoding();
            final String lengthJavaType = javaTypeName(lengthEncoding.primitiveType());
            final String byteOrderStr = byteOrderString(lengthEncoding);

            generateDataEncodeMethods(
                sb,
                propertyName,
                sizeOfLengthField,
                lengthEncoding.primitiveType(),
                lengthJavaType,
                byteOrderStr,
                characterEncoding,
                className,
                indent);

            i += token.componentTokenCount();
        }

        return sb;
    }

    private void generateDataDecodeMethods(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String characterEncoding,
        final String indent)
    {
        generateDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            fullMutableBuffer,
            lengthType,
            byteOrderStr,
            indent);

        generateDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            "byte[]",
            lengthType,
            byteOrderStr,
            indent);

        sb.append(String.format(
            "\n" +
            indent + "    public String %1$s()\n" +
            indent + "    {\n" +
                     "%2$s" +
            indent + "        final int sizeOfLengthField = %3$d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = %4$s;\n" +
            indent + "        parentMessage.limit(limit + sizeOfLengthField + dataLength);\n" +
            indent + "        final byte[] tmp = new byte[dataLength];\n" +
            indent + "        buffer.getBytes(limit + sizeOfLengthField, tmp, 0, dataLength);\n\n" +
            indent + "        final String value;\n" +
            indent + "        try\n" +
            indent + "        {\n" +
            indent + "            value = new String(tmp, \"%5$s\");\n" +
            indent + "        }\n" +
            indent + "        catch (final java.io.UnsupportedEncodingException ex)\n" +
            indent + "        {\n" +
            indent + "            throw new RuntimeException(ex);\n" +
            indent + "        }\n\n" +
            indent + "        return value;\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            generateStringNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            generateGet(lengthType, "limit", byteOrderStr),
            characterEncoding
        ));
    }

    private void generateDataEncodeMethods(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final PrimitiveType lengthType,
        final String lengthJavaType,
        final String byteOrderStr,
        final String characterEncoding,
        final String className,
        final String indent)
    {
        generateDataTypedEncoder(
            sb,
            propertyName,
            sizeOfLengthField,
            fullReadOnlyBuffer,
            lengthJavaType,
            lengthType,
            byteOrderStr,
            indent);

        generateDataTypedEncoder(
            sb,
            propertyName,
            sizeOfLengthField,
            "byte[]",
            lengthJavaType,
            lengthType,
            byteOrderStr,
            indent);

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s(final String value)\n" +
            indent + "    {\n" +
            indent + "        final byte[] bytes;\n" +
            indent + "        try\n" +
            indent + "        {\n" +
            indent + "            bytes = value.getBytes(\"%3$s\");\n" +
            indent + "        }\n" +
            indent + "        catch (final java.io.UnsupportedEncodingException ex)\n" +
            indent + "        {\n" +
            indent + "            throw new RuntimeException(ex);\n" +
            indent + "        }\n\n" +
            indent + "        final int length = bytes.length;\n" +
            indent + "        final int sizeOfLengthField = %4$d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        parentMessage.limit(limit + sizeOfLengthField + length);\n" +
            indent + "        final %5$s l = (%5$s)length;\n" +
            indent + "        %6$s;\n" +
            indent + "        buffer.putBytes(limit + sizeOfLengthField, bytes, 0, length);\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            className,
            toLowerFirstChar(propertyName),
            characterEncoding,
            sizeOfLengthField,
            lengthJavaType,
            generatePut(lengthType, "limit", "l", byteOrderStr)
        ));
    }

    private void generateDataTypedDecoder(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final String exchangeType,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public int get%s(\n" +
            indent + "        final %s dst, final int dstOffset, final int length)\n" +
            indent + "    {\n" +
                     "%s" +
            indent + "        final int sizeOfLengthField = %d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = %s;\n" +
            indent + "        final int bytesCopied = Math.min(length, dataLength);\n" +
            indent + "        parentMessage.limit(limit + sizeOfLengthField + dataLength);\n" +
            indent + "        buffer.getBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);\n\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            propertyName,
            exchangeType,
            generateArrayFieldNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            generateGet(lengthType, "limit", byteOrderStr)
        ));
    }

    private void generateDataTypedEncoder(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final String exchangeType,
        final String lengthJavaType,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public int put%1$s(\n" +
            indent + "        final %2$s src, final int srcOffset, final int length)\n" +
            indent + "    {\n" +
            indent + "        final int sizeOfLengthField = %3$d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        parentMessage.limit(limit + sizeOfLengthField + length);\n" +
            indent + "        final %4$s l = (%4$s)length;\n" +
            indent + "        %5$s;\n" +
            indent + "        buffer.putBytes(limit + sizeOfLengthField, src, srcOffset, length);\n\n" +
            indent + "        return length;\n" +
            indent + "    }\n",
            propertyName,
            exchangeType,
            sizeOfLengthField,
            lengthJavaType,
            generatePut(lengthType, "limit", "l", byteOrderStr)
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
            generateFixedFlyweightHeader(token, decoderName, out, readOnlyBuffer, fullReadOnlyBuffer);
            out.append(generateChoiceDecoders(messageBody));

            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(encoderName))
        {
            generateFixedFlyweightHeader(token, encoderName, out, mutableBuffer, fullMutableBuffer);
            out.append(generateChoiceClear(encoderName, token));
            out.append(generateChoiceEncoders(encoderName, messageBody));
            out.append("}\n");
        }
    }

    private void generateFixedFlyweightHeader(
        final Token token, final String encoderName, final Writer out, final String buffer, final String fullBuffer)
        throws IOException
    {
        out.append(generateFileHeader(encoderName, ir.applicableNamespace(), fullBuffer));
        out.append(generateClassDeclaration(encoderName));
        out.append(generateFixedFlyweightCode(encoderName, token.encodedLength(), false, buffer));
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateEnumFileHeader(enumName, ir.applicableNamespace()));
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

        try (final Writer out = outputManager.createOutput(decoderName))
        {
            generateFixedFlyweightHeader(token, decoderName, out, readOnlyBuffer, fullReadOnlyBuffer);

            for (int i = 1, end = tokens.size() - 1; i < end; i++)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = formatClassName(decoderName(encodingToken.name()));

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        out.append(generatePrimitiveDecoder(encodingToken.name(), encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_ENUM:
                        out.append(generateEnumDecoder(encodingToken, propertyName, encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_SET:
                        out.append(generateBitSetProperty(propertyName, encodingToken, BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(generateCompositeProperty(propertyName, encodingToken, BASE_INDENT, typeName));
                        i = findEndSignal(tokens, i, Signal.END_COMPOSITE, encodingToken.name());
                        break;
                }
            }

            out.append("}\n");
        }

        try (final Writer out = outputManager.createOutput(encoderName))
        {
            generateFixedFlyweightHeader(token, encoderName, out, mutableBuffer, fullMutableBuffer);

            for (int i = 1, end = tokens.size() - 1; i < end; i++)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = formatClassName(encoderName(encodingToken.name()));

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        out.append(generatePrimitiveEncoder(encoderName, encodingToken.name(), encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_ENUM:
                        out.append(generateEnumEncoder(encoderName, propertyName, encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_SET:
                        out.append(generateBitSetProperty(propertyName, encodingToken, BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(generateCompositeProperty(propertyName, encodingToken, BASE_INDENT, typeName));
                        i = findEndSignal(tokens, i, Signal.END_COMPOSITE, encodingToken.name());
                        break;
                }
            }

            out.append("}\n");
        }
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final String byteOrderStr = byteOrderString(encoding);

        sb.append(String.format(
            "\n" +
            "    public %s clear()\n" +
            "    {\n" +
            "        %s;\n" +
            "        return this;\n" +
            "    }\n",
            bitSetClassName,
            generatePut(encoding.primitiveType(), "offset", literalValue, byteOrderStr)
        ));

        return sb;
    }

    private CharSequence generateChoiceDecoders(final List<Token> tokens)
    {
        return concatTokens(
            tokens,
            Signal.CHOICE,
            (token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                final Encoding encoding = token.encoding();
                final String choiceBitIndex = encoding.constValue().toString();
                final String byteOrderStr = byteOrderString(encoding);

                return String.format(
                    "\n" +
                    "    public boolean %s()\n" +
                    "    {\n" +
                    "        return %s;\n" +
                    "    }\n",
                    choiceName,
                    generateChoiceGet(encoding.primitiveType(), "offset", choiceBitIndex, byteOrderStr)
                );
            });
    }

    private CharSequence generateChoiceEncoders(final String bitSetClassName, final List<Token> tokens)
    {
        return concatTokens(
            tokens,
            Signal.CHOICE,
            (token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                final Encoding encoding = token.encoding();
                final String choiceBitIndex = encoding.constValue().toString();
                final String byteOrderStr = byteOrderString(encoding);

                return String.format(
                    "\n" +
                    "    public %s %s(final boolean value)\n" +
                    "    {\n" +
                    "%s\n" +
                    "        return this;\n" +
                    "    }\n",
                    bitSetClassName,
                    choiceName,
                    generateChoicePut(encoding.primitiveType(), "offset", choiceBitIndex, byteOrderStr)
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

    private static CharSequence generateFileHeader(final String className, final String packageName, final String fqBuffer)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n" +
            "import %s;\n\n" +
            "@javax.annotation.Generated(value = {\"%s.%s\"})\n",
            packageName,
            fqBuffer,
            packageName,
            className
        );
    }

    private static CharSequence generateEnumFileHeader(final String className, final String packageName)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n" +
            "@javax.annotation.Generated(value = {\"%s.%s\"})\n",
            packageName,
            packageName,
            className
        );
    }

    private void generateAnnotations(
        final String indent,
        final String className,
        final List<Token> tokens,
        final Appendable out,
        int index,
        final Function<String, String> nameMapping) throws IOException
    {
        if (shouldGenerateGroupOrderAnnotation)
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

            if (!groupClassNames.isEmpty())
            {
                out.append(indent).append("@uk.co.real_logic.sbe.codec.java.GroupOrder({");
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
        }
    }

    private static CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            "@SuppressWarnings(\"all\")\n" +
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
                "@javax.annotation.Generated(value = {\"%s.MetaAttribute\"})\n" +
                "public enum MetaAttribute\n" +
                "{\n" +
                "    EPOCH,\n" +
                "    TIME_UNIT,\n" +
                "    SEMANTIC_TYPE\n" +
                "}\n",
                ir.applicableNamespace(),
                ir.applicableNamespace()
            ));
        }
    }

    private static CharSequence generateEnumDeclaration(final String name)
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
        return token.matchOnLength(
            () -> generatePrimitivePropertyDecode(propertyName, token, indent),
            () -> generatePrimitiveArrayPropertyDecode(propertyName, token, indent));
    }

    private CharSequence generatePrimitivePropertyEncodeMethods(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        return token.matchOnLength(
            () -> generatePrimitivePropertyEncode(containingClassName, propertyName, token, indent),
            () -> generatePrimitiveArrayPropertyEncode(containingClassName, propertyName, token, indent));
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
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())
        ));

        return sb;
    }

    private CharSequence generatePrimitivePropertyDecode(
        final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());

        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        return %s;\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            generateFieldNotPresentCondition(token.version(), encoding, indent),
            generateGet(encoding.primitiveType(), "offset + " + offset, byteOrderStr)
        );
    }

    private CharSequence generatePrimitivePropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        %s;\n" +
            indent + "        return this;\n" +
            indent + "    }\n\n",
            formatClassName(containingClassName),
            propertyName,
            javaTypeName,
            generatePut(encoding.primitiveType(), "offset + " + offset, "value", byteOrderStr)
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

    private static CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
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

    private static CharSequence generateStringNotPresentCondition(final int sinceVersion, final String indent)
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

    private static CharSequence generateTypeFieldNotPresentCondition(final int sinceVersion, final String indent)
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

    private CharSequence generatePrimitiveArrayPropertyDecode(final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
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
            indent + "        final int pos = this.offset + %d + (index * %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            fieldLength,
            generateFieldNotPresentCondition(token.version(), encoding, indent),
            offset,
            typeSize,
            generateGet(encoding.primitiveType(), "pos", byteOrderStr)
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
                                          "\"dstOffset out of range for copy: offset=\" + dstOffset);\n" +
                indent + "        }\n\n" +
                "%s" +
                indent + "        buffer.getBytes(this.offset + %d, dst, dstOffset, length);\n\n" +
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

    private static void generateArrayLengthMethod(
        final String propertyName, final String indent, final int fieldLength, final StringBuilder sb)
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
        return sizeOfPrimitive(encoding) == 1 ? "" : ", java.nio.ByteOrder." + encoding.byteOrder();
    }

    private CharSequence generatePrimitiveArrayPropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
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
            indent + "        final int pos = this.offset + %d + (index * %d);\n" +
            indent + "        %s;\n" +
            indent + "    }\n",
            propertyName,
            javaTypeName,
            fieldLength,
            offset,
            typeSize,
            generatePut(encoding.primitiveType(), "pos", "value", byteOrderStr)
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
                                          "\"srcOffset out of range for copy: offset=\" + srcOffset);\n" +
                indent + "        }\n\n" +
                indent + "        buffer.putBytes(this.offset + %d, src, srcOffset, length);\n\n" +
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

    private static int sizeOfPrimitive(final Encoding encoding)
    {
        return encoding.primitiveType().size();
    }

    private static void generateCharacterEncodingMethod(
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

    private static CharSequence generateByteLiteralList(final byte[] bytes)
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

    private static CharSequence generateFixedFlyweightCode(
        final String className, final int size, final boolean callsSuper, final String bufferImplementation)
    {
        final String body = callsSuper ?
            "        super.wrap(buffer, offset);\n" : "";

        return String.format(
            "    public static final int ENCODED_LENGTH = %2$d;\n" +
            "    private %3$s buffer;\n" +
            "    private int offset;\n\n" +
            "    public %1$s wrap(final %3$s buffer, final int offset)\n" +
            "    {\n" +
            "        this.buffer = buffer;\n" +
            "%4$s" +
            "        this.offset = offset;\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public int encodedLength()\n" +
            "    {\n" +
            "        return ENCODED_LENGTH;\n" +
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
            readOnlyBuffer);

        return generateFlyweightCode(className, token, wrapMethod, readOnlyBuffer);
    }

    private CharSequence generateFlyweightCode(
        final String className, final Token token, final String wrapMethod, final String bufferImplementation)
    {
        final HeaderStructure headerStructure = ir.headerStructure();
        final String blockLengthType = javaTypeName(headerStructure.blockLengthType());
        final String templateIdType = javaTypeName(headerStructure.templateIdType());
        final String schemaIdType = javaTypeName(headerStructure.schemaIdType());
        final String schemaVersionType = javaTypeName(headerStructure.schemaVersionType());
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
            "    public int encodedLength()\n" +
            "    {\n" +
            "        return limit - offset;\n" +
            "    }\n\n" +
            "    public int limit()\n" +
            "    {\n" +
            "        return limit;\n" +
            "    }\n\n" +
            "    public void limit(final int limit)\n" +
            "    {\n" +
            "        this.limit = limit;\n" +
            "    }\n",
            blockLengthType,
            generateLiteral(headerStructure.blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(headerStructure.templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(headerStructure.schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(headerStructure.schemaVersionType(), Integer.toString(token.version())),
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
            mutableBuffer);

        return generateFlyweightCode(className, token, wrapMethod, mutableBuffer);
    }

    private CharSequence generateEncoderFields(final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        eachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = formatClassName(encoderName(typeToken.name()));

                switch (typeToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveEncoder(containingClassName, propertyName, typeToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumEncoder(containingClassName, propertyName, typeToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(propertyName, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, typeToken, indent, typeName));
                        break;
                }
            });

        return sb;
    }

    private CharSequence generateDecoderFields(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        eachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = decoderName(formatClassName(typeToken.name()));

                generateFieldIdMethod(sb, fieldToken, indent);
                generateFieldMetaAttributeMethod(sb, fieldToken, indent);

                switch (typeToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveDecoder(propertyName, typeToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumDecoder(fieldToken, propertyName, typeToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(propertyName, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, typeToken, indent, typeName));
                        break;
                }
            });

        return sb;
    }

    private static void eachField(final List<Token> tokens, final BiConsumer<Token, Token> consumer)
    {
        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token fieldToken = tokens.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                consumer.accept(fieldToken, encodingToken);
                i += fieldToken.componentTokenCount();
            }
            else
            {
                ++i;
            }
        }
    }

    private static void generateFieldIdMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(token.name()),
            token.id()
        ));
    }

    private static void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
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
            formatPropertyName(token.name()),
            epoch,
            timeUnit,
            semanticType
        ));
    }

    private CharSequence generateEnumDecoder(
        final Token signalToken, final String propertyName, final Token token, final String indent)
    {
        final String enumName = formatClassName(token.name());
        final Encoding encoding = token.encoding();

        if (token.isConstantEncoding())
        {
            return String.format(
                "\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    }\n\n",
                enumName,
                propertyName,
                signalToken.encoding().constValue().toString()
            );
        }
        else
        {
            return String.format(
                "\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        return %s.get(%s);\n" +
                indent + "    }\n\n",
                enumName,
                propertyName,
                generateTypeFieldNotPresentCondition(token.version(), indent),
                enumName,
                generateGet(encoding.primitiveType(), "offset + " + token.offset(), byteOrderString(encoding))
            );
        }
    }

    private CharSequence generateEnumEncoder(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        if (token.isConstantEncoding())
        {
            return "";
        }

        final String enumName = formatClassName(token.name());
        final Encoding encoding = token.encoding();
        final int offset = token.offset();

        return String.format(
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        %s;\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            enumName,
            generatePut(encoding.primitiveType(), "offset + " + offset, "value.value()", byteOrderString(encoding))
        );
    }

    private CharSequence generateBitSetProperty(
        final String propertyName, final Token token, final String indent, final String bitSetName)
    {
        final StringBuilder sb = new StringBuilder();

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
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            bitSetName,
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            propertyName,
            token.offset(),
            propertyName
        ));

        return sb;
    }

    private CharSequence generateCompositeProperty(
        final String propertyName, final Token token, final String indent, final String compositeName)
    {
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
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            propertyName,
            token.offset(),
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
                literal = value.endsWith("NaN") ? "Float.NaN" : value + "f";
                break;

            case INT64:
                literal = value + "L";
                break;

            case UINT64:
                literal = "0x" + Long.toHexString(Long.parseLong(value)) + "L";
                break;

            case DOUBLE:
                literal = value.endsWith("NaN") ? "Double.NaN" : value + "d";
                break;
        }

        return literal;
    }

    private String generateGet(final PrimitiveType type, final String index, final String byteOrder)
    {
        switch (type)
        {
            case CHAR:
            case INT8:
                return "buffer.getByte(" + index + ")";

            case UINT8:
                return "(short)(buffer.getByte(" + index + ") & 0xFF)";

            case INT16:
                return "buffer.getShort(" + index + byteOrder + ")";

            case UINT16:
                return "(buffer.getShort(" + index + byteOrder + ") & 0xFFFF)";

            case INT32:
                return "buffer.getInt(" + index + byteOrder + ")";

            case UINT32:
                return "(buffer.getInt(" + index + byteOrder + ") & 0xFFFF_FFFF)";

            case FLOAT:
                return "buffer.getFloat(" + index + byteOrder + ")";

            case INT64:
            case UINT64:
                return "buffer.getLong(" + index + byteOrder + ")";

            case DOUBLE:
                return "buffer.getDouble(" + index + byteOrder + ")";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generatePut(final PrimitiveType type, final String index, final String value, final String byteOrder)
    {
        switch (type)
        {
            case CHAR:
            case INT8:
                return "buffer.putByte(" + index + ", " + value + ")";

            case UINT8:
                return "buffer.putByte(" + index + ", (byte)" + value + ")";

            case INT16:
                return "buffer.putShort(" + index + ", " + value + byteOrder + ")";

            case UINT16:
                return "buffer.putShort(" + index + ", (short)" + value + byteOrder + ")";

            case INT32:
                return "buffer.putInt(" + index + ", " + value + byteOrder + ")";

            case UINT32:
                return "buffer.putInt(" + index + ", (int)" + value + byteOrder + ")";

            case FLOAT:
                return "buffer.putFloat(" + index + ", " + value + byteOrder + ")";

            case INT64:
            case UINT64:
                return "buffer.putLong(" + index + ", " + value + byteOrder + ")";

            case DOUBLE:
                return "buffer.putDouble(" + index + ", " + value + byteOrder + ")";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoiceGet(final PrimitiveType type, final String index, final String bitIndex, final String byteOrder)
    {
        switch (type)
        {
            case UINT8:
                return "0 != (buffer.getByte(" + index + ") & (1 << " + bitIndex + "))";

            case UINT16:
                return "0 != (buffer.getShort(" + index + byteOrder + ") & (1 << " + bitIndex + "))";

            case UINT32:
                return "0 != (buffer.getInt(" + index + byteOrder + ") & (1 << " + bitIndex + "))";

            case UINT64:
                return "0 != (buffer.getLong(" + index + byteOrder + ") & (1L << " + bitIndex + "))";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoicePut(
        final PrimitiveType type, final String index, final String bitIndex, final String byteOrder)
    {
        switch (type)
        {
            case UINT8:
                return
                    "        byte bits = buffer.getByte(" + index + ");\n" +
                    "        bits = (byte)(value ? bits | (1 << " + bitIndex + ") : bits & ~(1 << " + bitIndex + "));\n" +
                    "        buffer.putByte(" + index + ", bits);";

            case UINT16:
                return
                    "        short bits = buffer.getShort(" + index + byteOrder + ");\n" +
                    "        bits = (short)(value ? bits | (1 << " + bitIndex + ") : bits & ~(1 << " + bitIndex + "));\n" +
                    "        buffer.putShort(" + index + ", bits" + byteOrder + ");";

            case UINT32:
                return
                    "        int bits = buffer.getInt(" + index + byteOrder + ");\n" +
                    "        bits = value ? bits | (1 << " + bitIndex + ") : bits & ~(1 << " + bitIndex + ");\n" +
                    "        buffer.putInt(" + index + ", bits" + byteOrder + ");";

            case UINT64:
                return
                    "        long bits = buffer.getLong(" + index + byteOrder + ");\n" +
                    "        bits = value ? bits | (1L << " + bitIndex + ") : bits & ~(1L << " + bitIndex + ");\n" +
                    "        buffer.putLong(" + index + ", bits" + byteOrder + ");";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }
}
