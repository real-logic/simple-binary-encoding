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
package uk.co.real_logic.sbe.generation.java;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static uk.co.real_logic.sbe.SbeTool.JAVA_INTERFACE_PACKAGE;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.CodecType.DECODER;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.CodecType.ENCODER;
import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

public class JavaGenerator implements CodeGenerator
{
    enum CodecType
    {
        DECODER,
        ENCODER
    }

    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";
    private static final String FLYWEIGHT = "Flyweight";
    private static final String COMPOSITE_DECODER_FLYWEIGHT = "CompositeDecoderFlyweight";
    private static final String COMPOSITE_ENCODER_FLYWEIGHT = "CompositeEncoderFlyweight";
    private static final String MESSAGE_DECODER_FLYWEIGHT = "MessageDecoderFlyweight";
    private static final String MESSAGE_ENCODER_FLYWEIGHT = "MessageEncoderFlyweight";

    private final Ir ir;
    private final OutputManager outputManager;
    private final String fqMutableBuffer;
    private final String mutableBuffer;
    private final String fqReadOnlyBuffer;
    private final String readOnlyBuffer;
    private final boolean shouldGenerateGroupOrderAnnotation;
    private final boolean shouldGenerateInterfaces;
    private final boolean shouldDecodeUnknownEnumValues;

    public JavaGenerator(
        final Ir ir,
        final String mutableBuffer,
        final String readOnlyBuffer,
        final boolean shouldGenerateGroupOrderAnnotation,
        final boolean shouldGenerateInterfaces,
        final boolean shouldDecodeUnknownEnumValues,
        final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;

        this.mutableBuffer = validateBufferImplementation(mutableBuffer, MutableDirectBuffer.class);
        this.fqMutableBuffer = mutableBuffer;

        this.readOnlyBuffer = validateBufferImplementation(readOnlyBuffer, DirectBuffer.class);
        this.fqReadOnlyBuffer = readOnlyBuffer;

        this.shouldGenerateGroupOrderAnnotation = shouldGenerateGroupOrderAnnotation;
        this.shouldGenerateInterfaces = shouldGenerateInterfaces;
        this.shouldDecodeUnknownEnumValues = shouldDecodeUnknownEnumValues;
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

    private String implementsInterface(final String interfaceName)
    {
        if (!shouldGenerateInterfaces)
        {
            return "";
        }
        else
        {
            return " implements " + interfaceName;
        }
    }

    public void generateMessageHeaderStub() throws IOException
    {
        generateComposite(ir.headerStructure().tokens());
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
        generateTypeStubs();
        generateMessageHeaderStub();

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
        final String implementsString = implementsInterface(MESSAGE_ENCODER_FLYWEIGHT);

        try (Writer out = outputManager.createOutput(className))
        {
            out.append(generateMainHeader(ir.applicableNamespace()));

            generateAnnotations(indent, className, groups, out, 0, this::encoderName);
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateEncoderFlyweightCode(className, msgToken));
            out.append(generateEncoderFields(className, fields, indent));

            final StringBuilder sb = new StringBuilder();
            generateEncoderGroups(sb, className, groups, indent, false);
            out.append(sb);

            out.append(generateEncoderVarData(className, varData, indent));

            out.append(generateEncoderDisplay(formatClassName(decoderName(msgToken.name())), indent));

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
        final String implementsString = implementsInterface(MESSAGE_DECODER_FLYWEIGHT);

        try (Writer out = outputManager.createOutput(className))
        {
            out.append(generateMainHeader(ir.applicableNamespace()));

            generateAnnotations(indent, className, groups, out, 0, this::decoderName);
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateDecoderFlyweightCode(className, msgToken));
            out.append(generateDecoderFields(fields, indent));

            final StringBuilder sb = new StringBuilder();
            generateDecoderGroups(sb, className, groups, indent, false);
            out.append(sb);

            out.append(generateDecoderVarData(varData, indent));

            out.append(generateDecoderDisplay(msgToken.name(), fields, groups, varData, indent));

            out.append("}\n");
        }
    }

    private void generateDecoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final List<Token> tokens,
        final String indent,
        final boolean isSubGroup) throws IOException
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final int groupIndex = i;
            final String groupName = decoderName(formatClassName(groupToken.name()));

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);

            sb.append(generateGroupDecoderProperty(groupName, groupToken, indent, isSubGroup));
            generateAnnotations(indent + INDENT, groupName, tokens, sb, groupIndex + 1, this::decoderName);
            generateGroupDecoderClassHeader(sb, groupName, outerClassName, tokens, groups, groupIndex, indent + INDENT);

            sb.append(generateDecoderFields(fields, indent + INDENT));
            generateDecoderGroups(sb, outerClassName, groups, indent + INDENT, true);
            sb.append(generateDecoderVarData(varData, indent + INDENT));

            appendGroupInstanceDecoderDisplay(sb, fields, groups, varData, indent + INDENT);

            sb.append(indent).append("    }\n");
        }
    }

    private void generateEncoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final List<Token> tokens,
        final String indent,
        final boolean isSubGroup) throws IOException
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final int groupIndex = i;
            final String groupName = groupToken.name();
            final String groupClassName = formatClassName(encoderName(groupName));

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);

            sb.append(generateGroupEncoderProperty(groupName, groupToken, indent, isSubGroup));
            generateAnnotations(indent + INDENT, groupClassName, tokens, sb, groupIndex + 1, this::encoderName);
            generateGroupEncoderClassHeader(sb, groupName, outerClassName, tokens, groups, groupIndex, indent + INDENT);

            sb.append(generateEncoderFields(groupClassName, fields, indent + INDENT));
            generateEncoderGroups(sb, outerClassName, groups, indent + INDENT, true);
            sb.append(generateEncoderVarData(groupClassName, varData, indent + INDENT));

            sb.append(indent).append("    }\n");
        }
    }

    private void generateGroupDecoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<Token> tokens,
        final List<Token> subGroupTokens,
        final int index,
        final String indent)
    {
        final Token groupToken = tokens.get(index);
        final int dimensionHeaderLen = tokens.get(index + 1).encodedLength();

        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);

        final PrimitiveType blockLengthType = blockLengthToken.encoding().primitiveType();
        final String blockLengthOffset = "limit + " + blockLengthToken.offset();
        final String blockLengthGet = generateGet(
            blockLengthType, blockLengthOffset, byteOrderString(blockLengthToken.encoding()));

        final PrimitiveType numInGroupType = numInGroupToken.encoding().primitiveType();
        final String numInGroupOffset = "limit + " + numInGroupToken.offset();
        final String numInGroupGet = generateGet(
            numInGroupType, numInGroupOffset, byteOrderString(numInGroupToken.encoding()));

        generateGroupDecoderClassDeclaration(
            sb,
            groupToken,
            groupName,
            parentMessageClassName,
            findSubGroupNames(subGroupTokens),
            indent,
            dimensionHeaderLen);

        sb.append(String.format(
            indent + "    public void wrap(final %s buffer)\n" +
            indent + "    {\n" +
            indent + "        if (buffer != this.buffer)\n" +
            indent + "        {\n" +
            indent + "            this.buffer = buffer;\n" +
            indent + "        }\n" +
            indent + "        index = -1;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        parentMessage.limit(limit + HEADER_SIZE);\n" +
            indent + "        blockLength = (int)%s;\n" +
            indent + "        count = (int)%s;\n" +
            indent + "    }\n\n",
            readOnlyBuffer,
            blockLengthGet,
            numInGroupGet));

        final int blockLength = tokens.get(index).encodedLength();

        sb.append(indent).append("    public static int sbeHeaderSize()\n")
          .append(indent).append("    {\n")
          .append(indent).append("        return HEADER_SIZE;\n")
          .append(indent).append("    }\n");

        sb.append(String.format("\n" +
            indent + "    public static int sbeBlockLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            blockLength));

        sb.append(String.format("\n" +
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
            indent + "    }\n",
            formatClassName(groupName)));

        sb.append(String.format("\n" +
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
            formatClassName(groupName)));
    }

    private void generateGroupEncoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<Token> tokens,
        final List<Token> subGroupTokens,
        final int index,
        final String ind)
    {
        final Token groupToken = tokens.get(index);
        final int dimensionHeaderSize = tokens.get(index + 1).encodedLength();

        generateGroupEncoderClassDeclaration(
            sb,
            groupToken,
            groupName,
            parentMessageClassName,
            findSubGroupNames(subGroupTokens),
            ind,
            dimensionHeaderSize);

        final int blockLength = tokens.get(index).encodedLength();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);

        final PrimitiveType blockLengthType = blockLengthToken.encoding().primitiveType();
        final String blockLengthOffset = "limit + " + blockLengthToken.offset();
        final String blockLengthValue = "(" + primitiveTypeName(blockLengthToken) + ")" + blockLength;
        final String blockLengthPut = generatePut(
            blockLengthType, blockLengthOffset, blockLengthValue, byteOrderString(blockLengthToken.encoding()));

        final PrimitiveType numInGroupType = numInGroupToken.encoding().primitiveType();
        final String numInGroupOffset = "limit + " + numInGroupToken.offset();
        final String numInGroupValue = "(" + primitiveTypeName(numInGroupToken) + ")count";
        final String numInGroupPut = generatePut(
            numInGroupType, numInGroupOffset, numInGroupValue, byteOrderString(numInGroupToken.encoding()));

        sb.append(String.format(
            ind + "    public void wrap(final %2$s buffer, final int count)\n" +
            ind + "    {\n" +
            ind + "        if (count < %3$d || count > %4$d)\n" +
            ind + "        {\n" +
            ind + "            throw new IllegalArgumentException(\"count outside allowed range: count=\" + count);\n" +
            ind + "        }\n\n" +
            ind + "        if (buffer != this.buffer)\n" +
            ind + "        {\n" +
            ind + "            this.buffer = buffer;\n" +
            ind + "        }\n\n" +
            ind + "        index = -1;\n" +
            ind + "        this.count = count;\n" +
            ind + "        final int limit = parentMessage.limit();\n" +
            ind + "        parentMessage.limit(limit + HEADER_SIZE);\n" +
            ind + "        %5$s;\n" +
            ind + "        %6$s;\n" +
            ind + "    }\n\n",
            parentMessageClassName,
            mutableBuffer,
            numInGroupToken.encoding().applicableMinValue().longValue(),
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            blockLengthPut,
            numInGroupPut));

        sb.append(ind).append("    public static int sbeHeaderSize()\n")
          .append(ind).append("    {\n")
          .append(ind).append("        return HEADER_SIZE;\n")
          .append(ind).append("    }\n");

        sb.append(String.format("\n" +
            ind + "    public static int sbeBlockLength()\n" +
            ind + "    {\n" +
            ind + "        return %d;\n" +
            ind + "    }\n",
            blockLength));

        sb.append(String.format("\n" +
            ind + "    public %s next()\n" +
            ind + "    {\n" +
            ind + "        if (index + 1 >= count)\n" +
            ind + "        {\n" +
            ind + "            throw new java.util.NoSuchElementException();\n" +
            ind + "        }\n\n" +
            ind + "        offset = parentMessage.limit();\n" +
            ind + "        parentMessage.limit(offset + sbeBlockLength());\n" +
            ind + "        ++index;\n\n" +
            ind + "        return this;\n" +
            ind + "    }\n",
            formatClassName(encoderName(groupName))));
    }

    private static String primitiveTypeName(final Token token)
    {
        return javaTypeName(token.encoding().primitiveType());
    }

    private void generateGroupDecoderClassDeclaration(
        final StringBuilder sb,
        final Token groupToken,
        final String groupName,
        final String parentMessageClassName,
        final List<String> subGroupNames,
        final String indent,
        final int dimensionHeaderSize)
    {
        final String className = formatClassName(groupName);

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public static class %2$s\n" +
            indent + "    implements Iterable<%2$s>, java.util.Iterator<%2$s>\n" +
            indent + "{\n" +
            indent + "    public static final int HEADER_SIZE = %3$d;\n" +
            indent + "    private final %4$s parentMessage;\n" +
            indent + "    private %5$s buffer;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n" +
            indent + "    private int blockLength;\n",
            generateTypeJavadoc(indent, groupToken),
            className,
            dimensionHeaderSize,
            parentMessageClassName,
            readOnlyBuffer));

        for (final String subGroupName : subGroupNames)
        {
            final String type = formatClassName(decoderName(subGroupName));
            final String field = formatPropertyName(subGroupName);
            sb.append(indent).append("    private final ").append(type).append(" ").append(field).append(";\n");
        }

        sb
            .append("\n")
            .append(indent).append("    ")
            .append(className).append("(final ").append(parentMessageClassName).append(" parentMessage)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        this.parentMessage = parentMessage;\n");

        for (final String subGroupName : subGroupNames)
        {
            final String type = formatClassName(decoderName(subGroupName));
            final String field = formatPropertyName(subGroupName);
            sb
                .append(indent).append("        ")
                .append(field).append(" = new ").append(type).append("(parentMessage);\n");
        }

        sb.append(indent).append("    }\n\n");
    }

    private void generateGroupEncoderClassDeclaration(
        final StringBuilder sb,
        final Token groupToken,
        final String groupName,
        final String parentMessageClassName,
        final List<String> subGroupNames,
        final String indent,
        final int dimensionHeaderSize)
    {
        final String className = formatClassName(encoderName(groupName));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public static class %2$s\n" +
            indent + "{\n" +
            indent + "    public static final int HEADER_SIZE = %3$d;\n" +
            indent + "    private final %4$s parentMessage;\n" +
            indent + "    private %5$s buffer;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n",
            generateTypeJavadoc(indent, groupToken),
            className,
            dimensionHeaderSize,
            parentMessageClassName,
            mutableBuffer));

        for (final String subGroupName : subGroupNames)
        {
            final String type = formatClassName(encoderName(subGroupName));
            final String field = formatPropertyName(subGroupName);
            sb.append(indent).append("    private final ").append(type).append(" ").append(field).append(";\n");
        }

        sb
            .append("\n")
            .append(indent).append("    ")
            .append(className).append("(final ").append(parentMessageClassName).append(" parentMessage)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        this.parentMessage = parentMessage;\n");

        for (final String subGroupName : subGroupNames)
        {
            final String type = formatClassName(encoderName(subGroupName));
            final String field = formatPropertyName(subGroupName);
            sb
                .append(indent).append("        ")
                .append(field).append(" = new ").append(type).append("(parentMessage);\n");
        }

        sb.append(indent).append("    }\n\n");
    }

    private static CharSequence generateGroupDecoderProperty(
        final String groupName, final Token token, final String indent, final boolean isSubGroup)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(token.name());

        if (!isSubGroup)
        {
            sb.append(String.format("\n" +
                indent + "    private final %s %s = new %s(this);\n",
                className,
                propertyName,
                className));
        }

        sb.append(String.format("\n" +
            indent + "    public static long %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.id()));

        sb.append(String.format("\n" +
            indent + "    public static int %sSinceVersion()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.version()));

        final String actingVersionGuard = token.version() == 0 ?
            "" :
            indent + "        if (parentMessage.actingVersion < " + token.version() + ")\n" +
            indent + "        {\n" +
            indent + "            " + propertyName + ".count = 0;\n" +
            indent + "            " + propertyName + ".index = -1;\n" +
            indent + "            return " + propertyName + ";\n" +
            indent + "        }\n\n";

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "    public %2$s %3$s()\n" +
            indent + "    {\n" +
            "%4$s" +
            indent + "        %3$s.wrap(buffer);\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            generateFlyweightPropertyJavadoc(indent + INDENT, token, className),
            className,
            propertyName,
            actingVersionGuard));

        return sb;
    }

    private CharSequence generateGroupEncoderProperty(
        final String groupName, final Token token, final String indent, final boolean isSubGroup)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(encoderName(groupName));
        final String propertyName = formatPropertyName(groupName);

        if (!isSubGroup)
        {
            sb.append(String.format("\n" +
                indent + "    private final %s %s = new %s(this);\n",
                className,
                propertyName,
                className));
        }

        sb.append(String.format("\n" +
            indent + "    public static long %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.id()));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "    public %2$s %3$sCount(final int count)\n" +
            indent + "    {\n" +
            indent + "        %3$s.wrap(buffer, count);\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            generateGroupEncodePropertyJavadoc(indent + INDENT, token, className),
            className,
            propertyName));

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
            generateFieldSinceVersionMethod(sb, token, indent);

            final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
            generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
            generateFieldMetaAttributeMethod(sb, token, indent);

            final String propertyName = Generators.toUpperFirstChar(token.name());
            final Token lengthToken = tokens.get(i + 2);
            final int sizeOfLengthField = lengthToken.encodedLength();
            final Encoding lengthEncoding = lengthToken.encoding();
            final PrimitiveType lengthType = lengthEncoding.primitiveType();
            final String byteOrderStr = byteOrderString(lengthEncoding);

            sb.append(String.format("\n" +
                indent + "    public static int %sHeaderLength()\n" +
                indent + "    {\n" +
                indent + "        return %d;\n" +
                indent + "    }\n",
                Generators.toLowerFirstChar(propertyName),
                sizeOfLengthField));

            sb.append(String.format("\n" +
                indent + "    public int %sLength()\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        return (int)%s;\n" +
                indent + "    }\n",
                Generators.toLowerFirstChar(propertyName),
                generateArrayFieldNotPresentCondition(token.version(), indent),
                generateGet(lengthType, "limit", byteOrderStr)));

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
            final Token varDataToken = Generators.findFirst("varData", tokens, i);
            final String characterEncoding = varDataToken.encoding().characterEncoding();
            generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
            generateFieldMetaAttributeMethod(sb, token, indent);

            final String propertyName = Generators.toUpperFirstChar(token.name());
            final Token lengthToken = Generators.findFirst("length", tokens, i);
            final int sizeOfLengthField = lengthToken.encodedLength();
            final Encoding lengthEncoding = lengthToken.encoding();
            final int maxLengthValue = (int)lengthEncoding.applicableMaxValue().longValue();
            final String byteOrderStr = byteOrderString(lengthEncoding);

            sb.append(String.format("\n" +
                indent + "    public static int %sHeaderLength()\n" +
                indent + "    {\n" +
                indent + "        return %d;\n" +
                indent + "    }\n",
                Generators.toLowerFirstChar(propertyName),
                sizeOfLengthField));

            generateDataEncodeMethods(
                sb,
                propertyName,
                sizeOfLengthField,
                maxLengthValue,
                lengthEncoding.primitiveType(),
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
        generateVarDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            mutableBuffer,
            lengthType,
            byteOrderStr,
            indent);

        generateVarDataTypedDecoder(
            sb,
            token,
            propertyName,
            sizeOfLengthField,
            "byte[]",
            lengthType,
            byteOrderStr,
            indent);

        generateVarDataWrapDecoder(sb, token, propertyName, sizeOfLengthField, lengthType, byteOrderStr, indent);

        if (null != characterEncoding)
        {
            sb.append(String.format("\n" +
                indent + "    public String %1$s()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        final int headerLength = %3$d;\n" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        final int dataLength = (int)%4$s;\n" +
                indent + "        parentMessage.limit(limit + headerLength + dataLength);\n\n" +
                indent + "        if (0 == dataLength)\n" +
                indent + "        {\n" +
                indent + "            return \"\";\n" +
                indent + "        }\n\n" +
                indent + "        final byte[] tmp = new byte[dataLength];\n" +
                indent + "        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);\n\n" +
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
                formatPropertyName(propertyName),
                generateStringNotPresentCondition(token.version(), indent),
                sizeOfLengthField,
                generateGet(lengthType, "limit", byteOrderStr),
                characterEncoding));

            if (characterEncoding.contains("ASCII"))
            {
                sb.append(String.format("\n" +
                    indent + "    public void get%1$s(final Appendable value)\n" +
                    indent + "    {\n" +
                    "%2$s" +
                    indent + "        final int headerLength = %3$d;\n" +
                    indent + "        final int limit = parentMessage.limit();\n" +
                    indent + "        final int dataLength = (int)%4$s;\n" +
                    indent + "        final int dataOffset = limit + headerLength;\n" +
                    indent + "        parentMessage.limit(dataOffset + dataLength);\n" +
                    indent + "        for (int i = 0; i < dataLength; ++i)\n" +
                    indent + "        {\n" +
                    indent + "            try\n" +
                    indent + "            {\n" +
                    indent + "                final int c = buffer.getByte(dataOffset + i) & 0xFF;\n" +
                    indent + "                value.append(c > 127 ? '?' : (char)c);\n" +
                    indent + "            }\n" +
                    indent + "            catch (final java.io.IOException e)\n" +
                    indent + "            {\n" +
                    indent + "                throw new java.io.UncheckedIOException(e);\n" +
                    indent + "            }\n" +
                    indent + "        }\n" +
                    indent + "    }\n",
                    Generators.toUpperFirstChar(propertyName),
                    generateStringNotPresentConditionForAppendable(token.version(), indent),
                    sizeOfLengthField,
                    generateGet(lengthType, "limit", byteOrderStr),
                    byteOrderStr));
            }
        }
    }

    private void generateVarDataWrapDecoder(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "    public void wrap%s(final %s wrapBuffer)\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        final int headerLength = %d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = (int)%s;\n" +
            indent + "        parentMessage.limit(limit + headerLength + dataLength);\n" +
            indent + "        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);\n" +
            indent + "    }\n",
            propertyName,
            readOnlyBuffer,
            generateVarWrapFieldNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            generateGet(lengthType, "limit", byteOrderStr)));
    }

    private void generateDataEncodeMethods(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final int maxLengthValue,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String characterEncoding,
        final String className,
        final String indent)
    {
        generateDataTypedEncoder(
            sb,
            className,
            propertyName,
            sizeOfLengthField,
            maxLengthValue,
            readOnlyBuffer,
            lengthType,
            byteOrderStr,
            indent);

        generateDataTypedEncoder(
            sb,
            className,
            propertyName,
            sizeOfLengthField,
            maxLengthValue,
            "byte[]",
            lengthType,
            byteOrderStr,
            indent);

        if (null != characterEncoding)
        {
            generateCharArrayEncodeMethods(
                sb,
                propertyName,
                sizeOfLengthField,
                maxLengthValue,
                lengthType,
                byteOrderStr,
                characterEncoding,
                className,
                indent);
        }
    }

    private void generateCharArrayEncodeMethods(
        final StringBuilder sb,
        final String propertyName,
        final int sizeOfLengthField,
        final int maxLengthValue,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String characterEncoding,
        final String className,
        final String indent)
    {
        if (characterEncoding.contains("ASCII"))
        {
            sb.append(String.format("\n" +
                indent + "    public %1$s %2$s(final String value)\n" +
                indent + "    {\n" +
                indent + "        final int length = null == value ? 0 : value.length();\n" +
                indent + "        if (length > %3$d)\n" +
                indent + "        {\n" +
                indent + "            throw new IllegalStateException(\"length > maxValue for type: \" + length);\n" +
                indent + "        }\n\n" +
                indent + "        final int headerLength = %4$d;\n" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        parentMessage.limit(limit + headerLength + length);\n" +
                indent + "        %5$s;\n" +
                indent + "        buffer.putStringWithoutLengthAscii(limit + headerLength, value);\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                className,
                formatPropertyName(propertyName),
                maxLengthValue,
                sizeOfLengthField,
                generatePut(lengthType, "limit", "length", byteOrderStr)));

            sb.append(String.format("\n" +
                indent + "    public %1$s %2$s(final CharSequence value)\n" +
                indent + "    {\n" +
                indent + "        final int length = null == value ? 0 : value.length();\n" +
                indent + "        if (length > %3$d)\n" +
                indent + "        {\n" +
                indent + "            throw new IllegalStateException(\"length > maxValue for type: \" + length);\n" +
                indent + "        }\n\n" +
                indent + "        final int headerLength = %4$d;\n" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        parentMessage.limit(limit + headerLength + length);\n" +
                indent + "        %5$s;\n" +
                indent + "        for (int i = 0; i < length; ++i)\n" +
                indent + "        {\n" +
                indent + "            final char charValue = value.charAt(i);\n" +
                indent + "            final byte byteValue = charValue > 127 ? (byte)'?' : (byte)charValue;\n" +
                indent + "            buffer.putByte(limit + headerLength + i, byteValue);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                className,
                formatPropertyName(propertyName),
                maxLengthValue,
                sizeOfLengthField,
                generatePut(lengthType, "limit", "length", byteOrderStr)));
        }
        else
        {
            sb.append(String.format("\n" +
                indent + "    public %1$s %2$s(final String value)\n" +
                indent + "    {\n" +
                indent + "        final byte[] bytes;\n" +
                indent + "        try\n" +
                indent + "        {\n" +
                indent + "            bytes = null == value || value.isEmpty() ?" +
                " org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes(\"%3$s\");\n" +
                indent + "        }\n" +
                indent + "        catch (final java.io.UnsupportedEncodingException ex)\n" +
                indent + "        {\n" +
                indent + "            throw new RuntimeException(ex);\n" +
                indent + "        }\n\n" +
                indent + "        final int length = bytes.length;\n" +
                indent + "        if (length > %4$d)\n" +
                indent + "        {\n" +
                indent + "            throw new IllegalStateException(\"length > maxValue for type: \" + length);\n" +
                indent + "        }\n\n" +
                indent + "        final int headerLength = %5$d;\n" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        parentMessage.limit(limit + headerLength + length);\n" +
                indent + "        %6$s;\n" +
                indent + "        buffer.putBytes(limit + headerLength, bytes, 0, length);\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                className,
                formatPropertyName(propertyName),
                characterEncoding,
                maxLengthValue,
                sizeOfLengthField,
                generatePut(lengthType, "limit", "length", byteOrderStr)));
        }
    }

    private void generateVarDataTypedDecoder(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final int sizeOfLengthField,
        final String exchangeType,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "    public int get%s(final %s dst, final int dstOffset, final int length)\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        final int headerLength = %d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = (int)%s;\n" +
            indent + "        final int bytesCopied = Math.min(length, dataLength);\n" +
            indent + "        parentMessage.limit(limit + headerLength + dataLength);\n" +
            indent + "        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);\n\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            propertyName,
            exchangeType,
            generateArrayFieldNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            generateGet(lengthType, "limit", byteOrderStr)));
    }

    private void generateDataTypedEncoder(
        final StringBuilder sb,
        final String className,
        final String propertyName,
        final int sizeOfLengthField,
        final int maxLengthValue,
        final String exchangeType,
        final PrimitiveType lengthType,
        final String byteOrderStr,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "    public %1$s put%2$s(final %3$s src, final int srcOffset, final int length)\n" +
            indent + "    {\n" +
            indent + "        if (length > %4$d)\n" +
            indent + "        {\n" +
            indent + "            throw new IllegalStateException(\"length > maxValue for type: \" + length);\n" +
            indent + "        }\n\n" +
            indent + "        final int headerLength = %5$d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        parentMessage.limit(limit + headerLength + length);\n" +
            indent + "        %6$s;\n" +
            indent + "        buffer.putBytes(limit + headerLength, src, srcOffset, length);\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            className,
            propertyName,
            exchangeType,
            maxLengthValue,
            sizeOfLengthField,
            generatePut(lengthType, "limit", "length", byteOrderStr)));
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String bitSetName = formatClassName(token.applicableTypeName());
        final String decoderName = decoderName(bitSetName);
        final String encoderName = encoderName(bitSetName);
        final List<Token> messageBody = getMessageBody(tokens);
        final String implementsString = implementsInterface(FLYWEIGHT);

        try (Writer out = outputManager.createOutput(decoderName))
        {
            generateFixedFlyweightHeader(token, decoderName, implementsString, out, readOnlyBuffer, fqReadOnlyBuffer);
            out.append(generateChoiceIsEmpty(token.encoding().primitiveType()));
            out.append(generateChoiceDecoders(messageBody));
            out.append(generateChoiceDisplay(messageBody));
            out.append("}\n");
        }

        try (Writer out = outputManager.createOutput(encoderName))
        {
            generateFixedFlyweightHeader(token, encoderName, implementsString, out, mutableBuffer, fqMutableBuffer);
            out.append(generateChoiceClear(encoderName, token));
            out.append(generateChoiceEncoders(encoderName, messageBody));
            out.append("}\n");
        }
    }

    private void generateFixedFlyweightHeader(
        final Token token,
        final String typeName,
        final String implementsString,
        final Writer out,
        final String buffer,
        final String fqBuffer) throws IOException
    {
        out.append(generateFileHeader(ir.applicableNamespace(), fqBuffer));
        out.append(generateDeclaration(typeName, implementsString, token));
        out.append(generateFixedFlyweightCode(typeName, token.encodedLength(), buffer));
    }

    private void generateCompositeFlyweightHeader(
        final Token token,
        final String typeName,
        final Writer out,
        final String buffer,
        final String fqBuffer,
        final String implementsString) throws IOException
    {
        out.append(generateFileHeader(ir.applicableNamespace(), fqBuffer));
        out.append(generateDeclaration(typeName, implementsString, token));
        out.append(generateFixedFlyweightCode(typeName, token.encodedLength(), buffer));
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(enumToken.applicableTypeName());

        try (Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateEnumFileHeader(ir.applicableNamespace()));
            out.append(generateEnumDeclaration(enumName, enumToken));

            out.append(generateEnumValues(getMessageBody(tokens)));
            out.append(generateEnumBody(enumToken, enumName));

            out.append(generateEnumLookupMethod(getMessageBody(tokens), enumName));

            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String compositeName = formatClassName(token.applicableTypeName());
        final String decoderName = decoderName(compositeName);
        final String encoderName = encoderName(compositeName);

        try (Writer out = outputManager.createOutput(decoderName))
        {
            final String implementsString = implementsInterface(COMPOSITE_DECODER_FLYWEIGHT);
            generateCompositeFlyweightHeader(
                token, decoderName, out, readOnlyBuffer, fqReadOnlyBuffer, implementsString);

            for (int i = 1, end = tokens.size() - 1; i < end;)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = formatClassName(decoderName(encodingToken.applicableTypeName()));

                final StringBuilder sb = new StringBuilder();
                generateEncodingOffsetMethod(sb, propertyName, encodingToken.offset(), BASE_INDENT);
                generateEncodingLengthMethod(sb, propertyName, encodingToken.encodedLength(), BASE_INDENT);
                generateFieldSinceVersionMethod(sb, encodingToken, BASE_INDENT);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        out.append(sb).append(generatePrimitiveDecoder(
                            true, encodingToken.name(), encodingToken, encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_ENUM:
                        out.append(sb).append(generateEnumDecoder(
                            true, encodingToken, propertyName, encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_SET:
                        out.append(sb).append(generateBitSetProperty(
                            true, DECODER, propertyName, encodingToken, encodingToken, BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(sb).append(generateCompositeProperty(
                            true, DECODER, propertyName, encodingToken, encodingToken, BASE_INDENT, typeName));
                        break;
                }

                i += encodingToken.componentTokenCount();
            }

            out.append(generateCompositeDecoderDisplay(tokens, BASE_INDENT));

            out.append("}\n");
        }

        try (Writer out = outputManager.createOutput(encoderName))
        {
            final String implementsString = implementsInterface(COMPOSITE_ENCODER_FLYWEIGHT);
            generateCompositeFlyweightHeader(token, encoderName, out, mutableBuffer, fqMutableBuffer, implementsString);

            for (int i = 1, end = tokens.size() - 1; i < end;)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = formatClassName(encoderName(encodingToken.applicableTypeName()));

                final StringBuilder sb = new StringBuilder();
                generateEncodingOffsetMethod(sb, propertyName, encodingToken.offset(), BASE_INDENT);
                generateEncodingLengthMethod(sb, propertyName, encodingToken.encodedLength(), BASE_INDENT);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        out.append(sb).append(generatePrimitiveEncoder(
                            encoderName, encodingToken.name(), encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_ENUM:
                        out.append(sb).append(generateEnumEncoder(
                            encoderName, encodingToken, propertyName, encodingToken, BASE_INDENT));
                        break;

                    case BEGIN_SET:
                        out.append(sb).append(generateBitSetProperty(
                            true, ENCODER, propertyName, encodingToken, encodingToken, BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(sb).append(generateCompositeProperty(
                            true, ENCODER, propertyName, encodingToken, encodingToken, BASE_INDENT, typeName));
                        break;
                }

                i += encodingToken.componentTokenCount();
            }

            out.append(generateCompositeEncoderDisplay(decoderName, BASE_INDENT));
            out.append("}\n");
        }
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final String byteOrderStr = byteOrderString(encoding);

        sb.append(String.format("\n" +
            "    public %s clear()\n" +
            "    {\n" +
            "        %s;\n" +
            "        return this;\n" +
            "    }\n",
            bitSetClassName,
            generatePut(encoding.primitiveType(), "offset", literalValue, byteOrderStr)));

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
                final PrimitiveType primitiveType = encoding.primitiveType();
                final String argType = bitsetArgType(primitiveType);

                return String.format("\n" +
                    "%1$s" +
                    "    public boolean %2$s()\n" +
                    "    {\n" +
                    "        return %3$s;\n" +
                    "    }\n\n" +
                    "    public static boolean %2$s(final %4$s value)\n" +
                    "    {\n" +
                    "        return %5$s;\n" +
                    "    }\n",
                    generateOptionDecodeJavadoc(INDENT, token),
                    choiceName,
                    generateChoiceGet(primitiveType, choiceBitIndex, byteOrderStr),
                    argType,
                    generateStaticChoiceGet(primitiveType, choiceBitIndex));
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
                final PrimitiveType primitiveType = encoding.primitiveType();
                final String argType = bitsetArgType(primitiveType);

                return String.format("\n" +
                    "%1$s" +
                    "    public %2$s %3$s(final boolean value)\n" +
                    "    {\n" +
                    "%4$s\n" +
                    "        return this;\n" +
                    "    }\n\n" +
                    "    public static %5$s %3$s(final %5$s bits, final boolean value)\n" +
                    "    {\n" +
                    "%6$s" +
                    "    }\n",
                    generateOptionEncodeJavadoc(INDENT, token),
                    bitSetClassName,
                    choiceName,
                    generateChoicePut(encoding.primitiveType(), choiceBitIndex, byteOrderStr),
                    argType,
                    generateStaticChoicePut(encoding.primitiveType(), choiceBitIndex));
            });
    }

    private String bitsetArgType(final PrimitiveType primitiveType)
    {
        switch (primitiveType)
        {
            case UINT8:
                return "byte";

            case UINT16:
                return "short";

            case UINT32:
                return "int";

            case UINT64:
                return "long";

            default:
                throw new IllegalStateException("Invalid type: " + primitiveType);
        }
    }

    private CharSequence generateEnumValues(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            final Encoding encoding = token.encoding();
            final CharSequence constVal = generateLiteral(encoding.primitiveType(), encoding.constValue().toString());
            sb.append(generateTypeJavadoc(INDENT, token));
            sb.append(INDENT).append(token.name()).append('(').append(constVal).append("),\n\n");
        }

        final Token token = tokens.get(0);
        final Encoding encoding = token.encoding();
        final CharSequence nullVal = generateLiteral(
            encoding.primitiveType(), encoding.applicableNullValue().toString());

        if (shouldDecodeUnknownEnumValues)
        {
            sb.append(INDENT).append("/**\n");
            sb.append(INDENT).append(" * To be used to represent a not known value from a later version.\n");
            sb.append(INDENT).append(" */\n");
            sb.append(INDENT).append("SBE_UNKNOWN").append('(').append(nullVal).append("),\n\n");
        }

        sb.append(INDENT).append("/**\n");
        sb.append(INDENT).append(" * To be used to represent not present or null.\n");
        sb.append(INDENT).append(" */\n");
        sb.append(INDENT).append("NULL_VAL").append('(').append(nullVal).append(");\n\n");

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
            enumName);
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
            javaTypeName(primitiveType)));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %s: return %s;\n",
                token.encoding().constValue().toString(),
                token.name()));
        }

        final String handleUnknownLogic = shouldDecodeUnknownEnumValues ?
            INDENT + INDENT + "return SBE_UNKNOWN;\n" :
            INDENT + INDENT + "throw new IllegalArgumentException(\"Unknown value: \" + value);\n";

        sb.append(String.format(
            "        }\n\n" +
            "        if (%s == value)\n" +
            "        {\n" +
            "            return NULL_VAL;\n" +
            "        }\n\n" +
            "%s" +
            "    }\n",
            generateLiteral(primitiveType, tokens.get(0).encoding().applicableNullValue().toString()),
            handleUnknownLogic));

        return sb;
    }

    private CharSequence interfaceImportLine()
    {
        if (!shouldGenerateInterfaces)
        {
            return "\n";
        }

        return String.format("import %s.*;\n\n", JAVA_INTERFACE_PACKAGE);
    }

    private CharSequence generateFileHeader(final String packageName, final String fqBuffer)
    {
        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package %s;\n\n" +
            "import %s;\n" +
            "%s",
            packageName,
            fqBuffer,
            interfaceImportLine());
    }

    private CharSequence generateMainHeader(final String packageName)
    {
        if (fqMutableBuffer.equals(fqReadOnlyBuffer))
        {
            return String.format(
                "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
                "package %s;\n\n" +
                "import %s;\n" +
                "%s",
                packageName,
                fqMutableBuffer,
                interfaceImportLine());
        }
        else
        {
            return String.format(
                "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
                "package %s;\n\n" +
                "import %s;\n" +
                "import %s;\n" +
                "%s",
                packageName,
                fqMutableBuffer,
                fqReadOnlyBuffer,
                interfaceImportLine());
        }
    }

    private static CharSequence generateEnumFileHeader(final String packageName)
    {
        return
            "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
            "package " + packageName + ";\n\n";
    }

    private void generateAnnotations(
        final String indent,
        final String className,
        final List<Token> tokens,
        final Appendable out,
        final int index,
        final Function<String, String> nameMapping) throws IOException
    {
        if (shouldGenerateGroupOrderAnnotation)
        {
            final List<String> groupClassNames = new ArrayList<>();
            int level = 0;
            int i = index;

            for (int size = tokens.size(); i < size; i++)
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
                else if (tokens.get(index).signal() == Signal.END_GROUP && --level < 0)
                {
                    break;
                }
            }

            if (!groupClassNames.isEmpty())
            {
                out.append(indent).append("@uk.co.real_logic.sbe.codec.java.GroupOrder({");
                i = 0;
                for (final String name : groupClassNames)
                {
                    out.append(className).append('.').append(name).append(".class");
                    if (++i < groupClassNames.size())
                    {
                        out.append(", ");
                    }
                }

                out.append("})\n");
            }
        }
    }

    private static CharSequence generateDeclaration(
        final String className, final String implementsString, final Token typeToken)
    {
        return String.format(
            "%s" +
            "@SuppressWarnings(\"all\")\n" +
            "public class %s%s\n" +
            "{\n",
            generateTypeJavadoc(BASE_INDENT, typeToken),
            className,
            implementsString);
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (Writer out = outputManager.createOutput(META_ATTRIBUTE_ENUM))
        {
            out.append(String.format(
                "/* Generated SBE (Simple Binary Encoding) message codec */\n" +
                "package %s;\n\n" +
                "public enum MetaAttribute\n" +
                "{\n" +
                "    EPOCH,\n" +
                "    TIME_UNIT,\n" +
                "    SEMANTIC_TYPE,\n" +
                "    PRESENCE\n" +
                "}\n",
                ir.applicableNamespace()));
        }
    }

    private static CharSequence generateEnumDeclaration(final String name, final Token typeToken)
    {
        return
            generateTypeJavadoc(BASE_INDENT, typeToken) +
            "public enum " + name + "\n{\n";
    }

    private CharSequence generatePrimitiveDecoder(
        final boolean inComposite,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(generatePrimitiveFieldMetaData(formattedPropertyName, encodingToken, indent));

        if (encodingToken.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(formattedPropertyName, encodingToken, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyDecodeMethods(
                inComposite, formattedPropertyName, propertyToken, encodingToken, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitiveEncoder(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(generatePrimitiveFieldMetaData(formattedPropertyName, token, indent));

        if (!token.isConstantEncoding())
        {
            sb.append(generatePrimitivePropertyEncodeMethods(
                containingClassName, formattedPropertyName, token, indent));
        }
        else
        {
            sb.append(generateConstPropertyMethods(formattedPropertyName, token, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyDecodeMethods(
        final boolean inComposite,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        return encodingToken.matchOnLength(
            () -> generatePrimitivePropertyDecode(inComposite, propertyName, propertyToken, encodingToken, indent),
            () -> generatePrimitiveArrayPropertyDecode(
                inComposite, propertyName, propertyToken, encodingToken, indent));
    }

    private CharSequence generatePrimitivePropertyEncodeMethods(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        return token.matchOnLength(
            () -> generatePrimitivePropertyEncode(containingClassName, propertyName, token, indent),
            () -> generatePrimitiveArrayPropertyEncode(containingClassName, propertyName, token, indent));
    }

    private CharSequence generatePrimitiveFieldMetaData(
        final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String javaTypeName = javaTypeName(primitiveType);
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(String.format("\n" +
            indent + "    public static %s %sNullValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            formattedPropertyName,
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString())));

        sb.append(String.format("\n" +
            indent + "    public static %s %sMinValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            formattedPropertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())));

        sb.append(String.format(
            "\n" +
            indent + "    public static %s %sMaxValue()\n" +
            indent + "    {\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            javaTypeName,
            formattedPropertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())));

        return sb;
    }

    private CharSequence generatePrimitivePropertyDecode(
        final boolean inComposite,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final Encoding encoding = encodingToken.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());

        final int offset = encodingToken.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        return %s;\n" +
            indent + "    }\n\n",
            javaTypeName,
            formatPropertyName(propertyName),
            generateFieldNotPresentCondition(inComposite, propertyToken.version(), encoding, indent),
            generateGet(encoding.primitiveType(), "offset + " + offset, byteOrderStr));
    }

    private CharSequence generatePrimitivePropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            "\n" +
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        %s;\n" +
            indent + "        return this;\n" +
            indent + "    }\n\n",
            formatClassName(containingClassName),
            formatPropertyName(propertyName),
            javaTypeName,
            generatePut(encoding.primitiveType(), "offset + " + offset, "value", byteOrderStr));
    }

    private CharSequence generateVarWrapFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            wrapBuffer.wrap(buffer, offset, 0);\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private CharSequence generateFieldNotPresentCondition(
        final boolean inComposite, final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (inComposite || 0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return %s;\n" +
            indent + "        }\n\n",
            sinceVersion,
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString()));
    }

    private static CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateStringNotPresentConditionForAppendable(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return;\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateStringNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return \"\";\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generatePropertyNotPresentCondition(
        final boolean inComposite,
        final CodecType codecType,
        final Token propertyToken,
        final String enumName,
        final String indent)
    {
        if (inComposite || codecType == ENCODER || 0 == propertyToken.version())
        {
            return "";
        }

        return String.format(
            indent + "        if (parentMessage.actingVersion < %d)\n" +
            indent + "        {\n" +
            indent + "            return %s;\n" +
            indent + "        }\n\n",
            propertyToken.version(),
            enumName == null ? "null" : (enumName + ".NULL_VAL"));
    }

    private CharSequence generatePrimitiveArrayPropertyDecode(
        final boolean inComposite,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final Encoding encoding = encodingToken.encoding();
        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final int offset = encodingToken.offset();
        final String byteOrderStr = byteOrderString(encoding);
        final int fieldLength = encodingToken.arrayLength();
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
            indent + "        final int pos = this.offset + %d + (index * %d);\n\n" +
            indent + "        return %s;\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            fieldLength,
            generateFieldNotPresentCondition(inComposite, propertyToken.version(), encoding, indent),
            offset,
            typeSize,
            generateGet(encoding.primitiveType(), "pos", byteOrderStr)));

        if (encoding.primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, encoding.characterEncoding(), indent);

            sb.append(String.format("\n" +
                indent + "    public int get%s(final byte[] dst, final int dstOffset)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        if (dstOffset < 0 || dstOffset > (dst.length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"Copy will go out of range: offset=\" + dstOffset);\n" +
                indent + "        }\n\n" +
                "%s" +
                indent + "        buffer.getBytes(this.offset + %d, dst, dstOffset, length);\n\n" +
                indent + "        return length;\n" +
                indent + "    }\n",
                Generators.toUpperFirstChar(propertyName),
                fieldLength,
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                offset));

            sb.append(String.format("\n" +
                indent + "    public String %s()\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        final byte[] dst = new byte[%d];\n" +
                indent + "        buffer.getBytes(this.offset + %d, dst, 0, %d);\n\n" +
                indent + "        int end = 0;\n" +
                indent + "        for (; end < %d && dst[end] != 0; ++end);\n\n" +
                indent + "        return new String(dst, 0, end, %s);\n" +
                indent + "    }\n\n",
                propertyName,
                generateStringNotPresentCondition(propertyToken.version(), indent),
                fieldLength, offset,
                fieldLength, fieldLength,
                charset(encoding.characterEncoding())));

            if (encoding.characterEncoding().contains("ASCII"))
            {
                sb.append(String.format("\n" +
                    indent + "    public void get%s(final Appendable value)\n" +
                    indent + "    {\n" +
                    "%s" +
                    indent + "        for (int i = 0; i < %d ; ++i)\n" +
                    indent + "        {\n" +
                    indent + "            final int c = buffer.getByte(this.offset + %d + i) & 0xFF;\n" +
                    indent + "            if (c == 0)\n" +
                    indent + "            {\n" +
                    indent + "                break;\n" +
                    indent + "            }\n" +
                    indent + "            try\n" +
                    indent + "            {\n" +
                    indent + "                value.append(c > 127 ? '?' : (char)c);\n" +
                    indent + "            }\n" +
                    indent + "            catch (final java.io.IOException e)\n" +
                    indent + "            {\n" +
                    indent + "                throw new java.io.UncheckedIOException(e);\n" +
                    indent + "            }\n" +
                    indent + "        }\n" +
                    indent + "    }\n\n",
                    Generators.toUpperFirstChar(propertyName),
                    generateStringNotPresentConditionForAppendable(propertyToken.version(), indent),
                    fieldLength, offset));
            }
        }

        return sb;
    }

    private static void generateArrayLengthMethod(
        final String propertyName, final String indent, final int fieldLength, final StringBuilder sb)
    {
        sb.append(String.format("\n" +
            indent + "    public static int %sLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n\n",
            formatPropertyName(propertyName),
            fieldLength));
    }

    private String byteOrderString(final Encoding encoding)
    {
        return sizeOfPrimitive(encoding) == 1 ? "" : ", java.nio.ByteOrder." + encoding.byteOrder();
    }

    private CharSequence generatePrimitiveArrayPropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String javaTypeName = javaTypeName(primitiveType);
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);
        final int arrayLength = token.arrayLength();
        final int typeSize = sizeOfPrimitive(encoding);

        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(containingClassName);

        generateArrayLengthMethod(propertyName, indent, arrayLength, sb);

        sb.append(String.format(
            indent + "    public %s %s(final int index, final %s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        final int pos = this.offset + %d + (index * %d);\n" +
            indent + "        %s;\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            className,
            propertyName,
            javaTypeName,
            arrayLength,
            offset,
            typeSize,
            generatePut(primitiveType, "pos", "value", byteOrderStr)));

        if (arrayLength > 1 && arrayLength <= 4)
        {
            sb.append(indent)
                .append("    public ")
                .append(className)
                .append(" put").append(Generators.toUpperFirstChar(propertyName))
                .append("(final ").append(javaTypeName).append(" value0");

            for (int i = 1; i < arrayLength; i++)
            {
                sb.append(", final ").append(javaTypeName).append(" value").append(i);
            }

            sb.append(")\n");
            sb.append(indent).append("    {\n");

            for (int i = 0; i < arrayLength; i++)
            {
                final String indexStr = "this.offset + " + (offset + (typeSize * i));

                sb.append(indent).append("        ")
                    .append(generatePut(primitiveType, indexStr, "value" + i, byteOrderStr))
                    .append(";\n");
            }

            sb.append("\n");
            sb.append(indent).append("        return this;\n");
            sb.append(indent).append("    }\n");
        }

        if (primitiveType == PrimitiveType.CHAR)
        {
            generateCharArrayEncodeMethods(
                containingClassName, propertyName, indent, encoding, offset, arrayLength, sb);
        }

        return sb;
    }

    private void generateCharArrayEncodeMethods(
        final String containingClassName,
        final String propertyName,
        final String indent,
        final Encoding encoding,
        final int offset,
        final int fieldLength,
        final StringBuilder sb)
    {
        generateCharacterEncodingMethod(sb, propertyName, encoding.characterEncoding(), indent);

        sb.append(String.format("\n" +
            indent + "    public %s put%s(final byte[] src, final int srcOffset)\n" +
            indent + "    {\n" +
            indent + "        final int length = %d;\n" +
            indent + "        if (srcOffset < 0 || srcOffset > (src.length - length))\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(" +
            "\"Copy will go out of range: offset=\" + srcOffset);\n" +
            indent + "        }\n\n" +
            indent + "        buffer.putBytes(this.offset + %d, src, srcOffset, length);\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            Generators.toUpperFirstChar(propertyName),
            fieldLength,
            offset));

        if (encoding.characterEncoding().contains("ASCII"))
        {
            sb.append(String.format("\n" +
                indent + "    public %1$s %2$s(final String src)\n" +
                indent + "    {\n" +
                indent + "        final int length = %3$d;\n" +
                indent + "        final int srcLength = null == src ? 0 : src.length();\n" +
                indent + "        if (srcLength > length)\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"String too large for copy: byte length=\" + srcLength);\n" +
                indent + "        }\n\n" +
                indent + "        buffer.putStringWithoutLengthAscii(this.offset + %4$d, src);\n\n" +
                indent + "        for (int start = srcLength; start < length; ++start)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(this.offset + %4$d + start, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                offset));
            sb.append(String.format("\n" +
                indent + "    public %1$s %2$s(final CharSequence src)\n" +
                indent + "    {\n" +
                indent + "        final int length = %3$d;\n" +
                indent + "        final int srcLength = null == src ? 0 : src.length();\n" +
                indent + "        if (srcLength > length)\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"CharSequence too large for copy: byte length=\" + srcLength);\n" +
                indent + "        }\n\n" +
                indent + "        for (int i = 0; i < srcLength; ++i)\n" +
                indent + "        {\n" +
                indent + "            final char charValue = src.charAt(i);\n" +
                indent + "            final byte byteValue = charValue > 127 ? (byte)'?' : (byte)charValue;\n" +
                indent + "            buffer.putByte(this.offset + %4$d + i, byteValue);\n" +
                indent + "        }\n\n" +
                indent + "        for (int i = srcLength; i < length; ++i)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(this.offset + %4$d + i, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                offset));
        }
        else
        {
            sb.append(String.format("\n" +
                indent + "    public %s %s(final String src)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        final byte[] bytes = null == src ? new byte[0] : src.getBytes(%s);\n" +
                indent + "        if (bytes.length > length)\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"String too large for copy: byte length=\" + bytes.length);\n" +
                indent + "        }\n\n" +
                indent + "        buffer.putBytes(this.offset + %d, bytes, 0, bytes.length);\n\n" +
                indent + "        for (int start = bytes.length; start < length; ++start)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(this.offset + %d + start, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                charset(encoding.characterEncoding()),
                offset,
                offset));
        }
    }

    private static int sizeOfPrimitive(final Encoding encoding)
    {
        return encoding.primitiveType().size();
    }

    private static void generateCharacterEncodingMethod(
        final StringBuilder sb, final String propertyName, final String characterEncoding, final String indent)
    {
        if (null != characterEncoding)
        {
            sb.append(String.format("\n" +
                indent + "    public static String %sCharacterEncoding()\n" +
                indent + "    {\n" +
                indent + "        return \"%s\";\n" +
                indent + "    }\n",
                formatPropertyName(propertyName),
                characterEncoding));
        }
    }

    private CharSequence generateConstPropertyMethods(
        final String propertyName, final Token token, final String indent)
    {
        final String formattedPropertyName = formatPropertyName(propertyName);
        final Encoding encoding = token.encoding();
        if (encoding.primitiveType() != PrimitiveType.CHAR)
        {
            return String.format("\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
                javaTypeName(encoding.primitiveType()),
                formattedPropertyName,
                generateLiteral(encoding.primitiveType(), encoding.constValue().toString()));
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final byte[] constBytes = encoding.constValue().byteArrayValue(encoding.primitiveType());
        final CharSequence values = generateByteLiteralList(
            encoding.constValue().byteArrayValue(encoding.primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + "    private static final byte[] %s_VALUE = { %s };\n",
            propertyName.toUpperCase(),
            values));

        generateArrayLengthMethod(formattedPropertyName, indent, constBytes.length, sb);

        sb.append(String.format(
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        return %s_VALUE[index];\n" +
            indent + "    }\n\n",
            javaTypeName,
            formattedPropertyName,
            propertyName.toUpperCase()));

        sb.append(String.format(
            indent + "    public int get%s(final byte[] dst, final int offset, final int length)\n" +
            indent + "    {\n" +
            indent + "        final int bytesCopied = Math.min(length, %d);\n" +
            indent + "        System.arraycopy(%s_VALUE, 0, dst, offset, bytesCopied);\n\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            Generators.toUpperFirstChar(propertyName),
            constBytes.length,
            propertyName.toUpperCase()));

        if (constBytes.length > 1)
        {
            sb.append(String.format("\n" +
                indent + "    public String %s()\n" +
                indent + "    {\n" +
                indent + "        return \"%s\";\n" +
                indent + "    }\n\n",
                formattedPropertyName,
                encoding.constValue()));
        }
        else
        {
            sb.append(String.format("\n" +
                indent + "    public byte %s()\n" +
                indent + "    {\n" +
                indent + "        return (byte)%s;\n" +
                indent + "    }\n\n",
                formattedPropertyName,
                encoding.constValue()));
        }

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

    private CharSequence generateFixedFlyweightCode(
        final String className, final int size, final String bufferImplementation)
    {
        final String schemaIdType = javaTypeName(ir.headerStructure().schemaIdType());
        final String schemaIdAccessorType = shouldGenerateInterfaces ? "int" : schemaIdType;
        final String schemaVersionType = javaTypeName(ir.headerStructure().schemaVersionType());
        final String schemaVersionAccessorType = shouldGenerateInterfaces ? "int" : schemaVersionType;

        return String.format(
            "    public static final %5$s SCHEMA_ID = %6$s;\n" +
            "    public static final %7$s SCHEMA_VERSION = %8$s;\n" +
            "    public static final int ENCODED_LENGTH = %2$d;\n" +
            "    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.%4$s;\n\n" +
            "    private int offset;\n" +
            "    private %3$s buffer;\n\n" +
            "    public %1$s wrap(final %3$s buffer, final int offset)\n" +
            "    {\n" +
            "        if (buffer != this.buffer)\n" +
            "        {\n" +
            "            this.buffer = buffer;\n" +
            "        }\n" +
            "        this.offset = offset;\n\n" +
            "        return this;\n" +
            "    }\n\n" +
            "    public %3$s buffer()\n" +
            "    {\n" +
            "        return buffer;\n" +
            "    }\n\n" +
            "    public int offset()\n" +
            "    {\n" +
            "        return offset;\n" +
            "    }\n\n" +
            "    public int encodedLength()\n" +
            "    {\n" +
            "        return ENCODED_LENGTH;\n" +
            "    }\n\n" +
            "    public %9$s sbeSchemaId()\n" +
            "    {\n" +
            "        return SCHEMA_ID;\n" +
            "    }\n\n" +
            "    public %10$s sbeSchemaVersion()\n" +
            "    {\n" +
            "        return SCHEMA_VERSION;\n" +
            "    }\n",
            className,
            size,
            bufferImplementation,
            ir.byteOrder(),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            schemaIdAccessorType,
            schemaVersionAccessorType);


    }

    private CharSequence generateDecoderFlyweightCode(final String className, final Token token)
    {
        final String wrapMethod = String.format(
            "    public %1$s wrap(\n" +
            "        final %2$s buffer, final int offset, final int actingBlockLength, final int actingVersion)\n" +
            "    {\n" +
            "        if (buffer != this.buffer)\n" +
            "        {\n" +
            "            this.buffer = buffer;\n" +
            "        }\n" +
            "        this.offset = offset;\n" +
            "        this.actingBlockLength = actingBlockLength;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        limit(offset + actingBlockLength);\n\n" +
            "        return this;\n" +
            "    }\n\n",
            className,
            readOnlyBuffer);

        return generateFlyweightCode(DECODER, className, token, wrapMethod, readOnlyBuffer);
    }

    private CharSequence generateFlyweightCode(
        final CodecType codecType,
        final String className,
        final Token token,
        final String wrapMethod,
        final String bufferImplementation)
    {
        final HeaderStructure headerStructure = ir.headerStructure();
        final String blockLengthType = javaTypeName(headerStructure.blockLengthType());
        final String blockLengthAccessorType = shouldGenerateInterfaces ? "int" : blockLengthType;
        final String templateIdType = javaTypeName(headerStructure.templateIdType());
        final String templateIdAccessorType = shouldGenerateInterfaces ? "int" : templateIdType;
        final String schemaIdType = javaTypeName(headerStructure.schemaIdType());
        final String schemaIdAccessorType = shouldGenerateInterfaces ? "int" : schemaIdType;
        final String schemaVersionType = javaTypeName(headerStructure.schemaVersionType());
        final String schemaVersionAccessorType = shouldGenerateInterfaces ? "int" : schemaVersionType;
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();
        final String actingFields = codecType == CodecType.ENCODER ?
            "" :
            "    protected int actingBlockLength;\n" +
            "    protected int actingVersion;\n";

        return String.format(
            "    public static final %1$s BLOCK_LENGTH = %2$s;\n" +
            "    public static final %3$s TEMPLATE_ID = %4$s;\n" +
            "    public static final %5$s SCHEMA_ID = %6$s;\n" +
            "    public static final %7$s SCHEMA_VERSION = %8$s;\n" +
            "    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.%14$s;\n\n" +
            "    private final %9$s parentMessage = this;\n" +
            "    private %11$s buffer;\n" +
            "    protected int offset;\n" +
            "    protected int limit;\n" +
            "%13$s" +
            "\n" +
            "    public %15$s sbeBlockLength()\n" +
            "    {\n" +
            "        return BLOCK_LENGTH;\n" +
            "    }\n\n" +
            "    public %16$s sbeTemplateId()\n" +
            "    {\n" +
            "        return TEMPLATE_ID;\n" +
            "    }\n\n" +
            "    public %17$s sbeSchemaId()\n" +
            "    {\n" +
            "        return SCHEMA_ID;\n" +
            "    }\n\n" +
            "    public %18$s sbeSchemaVersion()\n" +
            "    {\n" +
            "        return SCHEMA_VERSION;\n" +
            "    }\n\n" +
            "    public String sbeSemanticType()\n" +
            "    {\n" +
            "        return \"%10$s\";\n" +
            "    }\n\n" +
            "    public %11$s buffer()\n" +
            "    {\n" +
            "        return buffer;\n" +
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
            generateLiteral(headerStructure.schemaVersionType(), Integer.toString(ir.version())),
            className,
            semanticType,
            bufferImplementation,
            wrapMethod,
            actingFields,
            ir.byteOrder(),
            blockLengthAccessorType,
            templateIdAccessorType,
            schemaIdAccessorType,
            schemaVersionAccessorType);
    }

    private CharSequence generateEncoderFlyweightCode(final String className, final Token token)
    {
        final String wrapMethod = String.format(
            "    public %1$s wrap(final %2$s buffer, final int offset)\n" +
            "    {\n" +
            "        if (buffer != this.buffer)\n" +
            "        {\n" +
            "            this.buffer = buffer;\n" +
            "        }\n" +
            "        this.offset = offset;\n" +
            "        limit(offset + BLOCK_LENGTH);\n\n" +
            "        return this;\n" +
            "    }\n\n",
            className,
            mutableBuffer);

        final StringBuilder builder = new StringBuilder(
            "    public %1$s wrapAndApplyHeader(\n" +
            "        final %2$s buffer, final int offset, final %3$s headerEncoder)\n" +
            "    {\n" +
            "        headerEncoder\n" +
            "            .wrap(buffer, offset)");

        for (final Token headerToken : ir.headerStructure().tokens())
        {
            if (!headerToken.isConstantEncoding())
            {
                switch (headerToken.name())
                {
                    case "blockLength":
                        builder.append("\n            .blockLength(BLOCK_LENGTH)");
                        break;

                    case "templateId":
                        builder.append("\n            .templateId(TEMPLATE_ID)");
                        break;

                    case "schemaId":
                        builder.append("\n            .schemaId(SCHEMA_ID)");
                        break;

                    case "version":
                        builder.append("\n            .version(SCHEMA_VERSION)");
                        break;
                }
            }
        }

        builder.append(";\n\n        return wrap(buffer, offset + %3$s.ENCODED_LENGTH);\n" + "    }\n\n");

        final String wrapAndApplyMethod = String.format(
            builder.toString(),
            className,
            mutableBuffer,
            formatClassName(ir.headerStructure().tokens().get(0).applicableTypeName() + "Encoder"));

        return generateFlyweightCode(
            CodecType.ENCODER, className, token, wrapMethod + wrapAndApplyMethod, mutableBuffer);
    }

    private CharSequence generateEncoderFields(
        final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        Generators.forEachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = formatClassName(encoderName(typeToken.name()));

                generateFieldIdMethod(sb, fieldToken, indent);
                generateFieldSinceVersionMethod(sb, fieldToken, indent);
                generateEncodingOffsetMethod(sb, propertyName, fieldToken.offset(), indent);
                generateEncodingLengthMethod(sb, propertyName, typeToken.encodedLength(), indent);
                generateFieldMetaAttributeMethod(sb, fieldToken, indent);

                switch (typeToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveEncoder(containingClassName, propertyName, typeToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumEncoder(
                            containingClassName, fieldToken, propertyName, typeToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(
                            false, ENCODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(
                            false, ENCODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;
                }
            });

        return sb;
    }

    private CharSequence generateDecoderFields(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        Generators.forEachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = decoderName(formatClassName(typeToken.name()));

                generateFieldIdMethod(sb, fieldToken, indent);
                generateFieldSinceVersionMethod(sb, fieldToken, indent);
                generateEncodingOffsetMethod(sb, propertyName, fieldToken.offset(), indent);
                generateEncodingLengthMethod(sb, propertyName, typeToken.encodedLength(), indent);
                generateFieldMetaAttributeMethod(sb, fieldToken, indent);

                switch (typeToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveDecoder(false, propertyName, fieldToken, typeToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumDecoder(false, fieldToken, propertyName, typeToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(
                            false, DECODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(
                            false, DECODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;
                }
            });

        return sb;
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
            token.id()));
    }

    private static void generateEncodingOffsetMethod(
        final StringBuilder sb, final String name, final int offset, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sEncodingOffset()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(name),
            offset));
    }

    private static void generateEncodingLengthMethod(
        final StringBuilder sb, final String name, final int length, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sEncodingLength()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(name),
            length));
    }

    private static void generateFieldSinceVersionMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    public static int %sSinceVersion()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(token.name()),
            token.version()));
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
            indent + "            case PRESENCE: return \"%s\";\n" +
            indent + "        }\n\n" +
            indent + "        return \"\";\n" +
            indent + "    }\n",
            formatPropertyName(token.name()),
            epoch,
            timeUnit,
            semanticType,
            encoding.presence().toString().toLowerCase()));
    }

    private CharSequence generateEnumDecoder(
        final boolean inComposite,
        final Token fieldToken,
        final String propertyName,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName());
        final Encoding encoding = typeToken.encoding();

        if (fieldToken.isConstantEncoding())
        {
            return String.format(
                "\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    }\n\n",
                enumName,
                propertyName,
                fieldToken.encoding().constValue().toString());
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
                generatePropertyNotPresentCondition(inComposite, DECODER, fieldToken, enumName, indent),
                enumName,
                generateGet(encoding.primitiveType(), "offset + " + typeToken.offset(), byteOrderString(encoding)));
        }
    }

    private CharSequence generateEnumEncoder(
        final String containingClassName,
        final Token fieldToken,
        final String propertyName,
        final Token typeToken,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return "";
        }

        final String enumName = formatClassName(typeToken.applicableTypeName());
        final Encoding encoding = typeToken.encoding();
        final int offset = typeToken.offset();

        return String.format("\n" +
            indent + "    public %s %s(final %s value)\n" +
            indent + "    {\n" +
            indent + "        %s;\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            enumName,
            generatePut(encoding.primitiveType(), "offset + " + offset, "value.value()", byteOrderString(encoding)));
    }

    private CharSequence generateBitSetProperty(
        final boolean inComposite,
        final CodecType codecType,
        final String propertyName,
        final Token propertyToken,
        final Token bitsetToken,
        final String indent,
        final String bitSetName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "    private final %s %s = new %s();\n",
            bitSetName,
            propertyName,
            bitSetName));

        sb.append(String.format("\n" +
            "%s" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            generateFlyweightPropertyJavadoc(indent + INDENT, propertyToken, bitSetName),
            bitSetName,
            propertyName,
            generatePropertyNotPresentCondition(inComposite, codecType, propertyToken, null, indent),
            propertyName,
            bitsetToken.offset(),
            propertyName));

        return sb;
    }

    private CharSequence generateCompositeProperty(
        final boolean inComposite,
        final CodecType codecType,
        final String propertyName,
        final Token propertyToken,
        final Token compositeToken,
        final String indent,
        final String compositeName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "    private final %s %s = new %s();\n",
            compositeName,
            propertyName,
            compositeName));

        sb.append(String.format("\n" +
            "%s" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            generateFlyweightPropertyJavadoc(indent + INDENT, propertyToken, compositeName),
            compositeName,
            propertyName,
            generatePropertyNotPresentCondition(inComposite, codecType, propertyToken, null, indent),
            propertyName,
            compositeToken.offset(),
            propertyName));

        return sb;
    }

    private String generateGet(final PrimitiveType type, final String index, final String byteOrder)
    {
        switch (type)
        {
            case CHAR:
            case INT8:
                return "buffer.getByte(" + index + ")";

            case UINT8:
                return "((short)(buffer.getByte(" + index + ") & 0xFF))";

            case INT16:
                return "buffer.getShort(" + index + byteOrder + ")";

            case UINT16:
                return "(buffer.getShort(" + index + byteOrder + ") & 0xFFFF)";

            case INT32:
                return "buffer.getInt(" + index + byteOrder + ")";

            case UINT32:
                return "(buffer.getInt(" + index + byteOrder + ") & 0xFFFF_FFFFL)";

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

    private String generatePut(
        final PrimitiveType type, final String index, final String value, final String byteOrder)
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

    private String generateChoiceIsEmpty(final PrimitiveType type)
    {
        return "\n" +
            "    public boolean isEmpty()\n" +
            "    {\n" +
            "        return " + generateChoiceIsEmptyInner(type) + ";\n" +
            "    }\n";
    }

    private String generateChoiceIsEmptyInner(final PrimitiveType type)
    {
        switch (type)
        {
            case UINT8:
                return "0 == buffer.getByte(offset)";

            case UINT16:
                return "0 == buffer.getShort(offset)";

            case UINT32:
                return "0 == buffer.getInt(offset)";

            case UINT64:
                return "0 == buffer.getLong(offset)";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoiceGet(final PrimitiveType type, final String bitIndex, final String byteOrder)
    {
        switch (type)
        {
            case UINT8:
                return "0 != (buffer.getByte(offset) & (1 << " + bitIndex + "))";

            case UINT16:
                return "0 != (buffer.getShort(offset" + byteOrder + ") & (1 << " + bitIndex + "))";

            case UINT32:
                return "0 != (buffer.getInt(offset" + byteOrder + ") & (1 << " + bitIndex + "))";

            case UINT64:
                return "0 != (buffer.getLong(offset" + byteOrder + ") & (1L << " + bitIndex + "))";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateStaticChoiceGet(final PrimitiveType type, final String bitIndex)
    {
        switch (type)
        {
            case UINT8:
                return "0 != (value & (1 << " + bitIndex + "))";

            case UINT16:
                return "0 != (value & (1 << " + bitIndex + "))";

            case UINT32:
                return "0 != (value & (1 << " + bitIndex + "))";

            case UINT64:
                return "0 != (value & (1L << " + bitIndex + "))";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoicePut(final PrimitiveType type, final String bitIdx, final String byteOrder)
    {
        switch (type)
        {
            case UINT8:
                return
                    "        byte bits = buffer.getByte(offset);\n" +
                    "        bits = (byte)(value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + "));\n" +
                    "        buffer.putByte(offset, bits);";

            case UINT16:
                return
                    "        short bits = buffer.getShort(offset" + byteOrder + ");\n" +
                    "        bits = (short)(value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + "));\n" +
                    "        buffer.putShort(offset, bits" + byteOrder + ");";

            case UINT32:
                return
                    "        int bits = buffer.getInt(offset" + byteOrder + ");\n" +
                    "        bits = value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + ");\n" +
                    "        buffer.putInt(offset, bits" + byteOrder + ");";

            case UINT64:
                return
                    "        long bits = buffer.getLong(offset" + byteOrder + ");\n" +
                    "        bits = value ? bits | (1L << " + bitIdx + ") : bits & ~(1L << " + bitIdx + ");\n" +
                    "        buffer.putLong(offset, bits" + byteOrder + ");";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateStaticChoicePut(final PrimitiveType type, final String bitIdx)
    {
        switch (type)
        {
            case UINT8:
                return
                    "        return (byte)(value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + "));\n";

            case UINT16:
                return
                    "        return (short)(value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + "));\n";

            case UINT32:
                return
                    "        return value ? bits | (1 << " + bitIdx + ") : bits & ~(1 << " + bitIdx + ");\n";

            case UINT64:
                return
                    "        return value ? bits | (1L << " + bitIdx + ") : bits & ~(1L << " + bitIdx + ");\n";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private CharSequence generateEncoderDisplay(final String decoderName, final String baseIndent)
    {
        final String indent = baseIndent + INDENT;
        final StringBuilder sb = new StringBuilder();

        sb.append('\n');
        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        append(sb, indent, INDENT + decoderName + " writer = new " + decoderName + "();");
        append(sb, indent, "    writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);");
        sb.append('\n');
        append(sb, indent, "    return writer.appendTo(builder);");
        append(sb, indent, "}");

        return sb.toString();
    }

    private CharSequence generateCompositeEncoderDisplay(final String decoderName, final String baseIndent)
    {
        final String indent = baseIndent + INDENT;
        final StringBuilder sb = new StringBuilder();
        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        append(sb, indent, INDENT + decoderName + " writer = new " + decoderName + "();");
        append(sb, indent, "    writer.wrap(buffer, offset);");
        sb.append('\n');
        append(sb, indent, "    return writer.appendTo(builder);");
        append(sb, indent, "}");

        return sb.toString();
    }

    private CharSequence generateCompositeDecoderDisplay(final List<Token> tokens, final String baseIndent)
    {
        final String indent = baseIndent + INDENT;
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        Separators.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");

        int lengthBeforeLastGeneratedSeparator = -1;

        for (int i = 1, end = tokens.size() - 1; i < end;)
        {
            final Token encodingToken = tokens.get(i);
            final String propertyName = formatPropertyName(encodingToken.name());
            lengthBeforeLastGeneratedSeparator = writeTokenDisplay(propertyName, encodingToken, sb, indent + INDENT);
            i += encodingToken.componentTokenCount();
        }

        if (-1 != lengthBeforeLastGeneratedSeparator)
        {
            sb.setLength(lengthBeforeLastGeneratedSeparator);
        }

        Separators.END_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        sb.append('\n');
        append(sb, indent, "    return builder;");
        append(sb, indent, "}");

        return sb.toString();
    }

    private CharSequence generateChoiceDisplay(final List<Token> tokens)
    {
        final String indent = INDENT;
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        Separators.BEGIN_SET.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        append(sb, indent, "    boolean atLeastOne = false;");

        tokens
            .stream()
            .filter((token) -> token.signal() == Signal.CHOICE)
            .forEach((token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                append(sb, indent, "    if (" + choiceName + "())");
                append(sb, indent, "    {");
                append(sb, indent, "        if (atLeastOne)");
                append(sb, indent, "        {");
                Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT + INDENT, "builder");
                append(sb, indent, "        }");
                append(sb, indent, "        builder.append(\"" + choiceName + "\");");
                append(sb, indent, "        atLeastOne = true;");
                append(sb, indent, "    }");
            });

        Separators.END_SET.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        sb.append('\n');
        append(sb, indent, "    return builder;");
        append(sb, indent, "}");

        return sb.toString();
    }

    private CharSequence generateDecoderDisplay(
        final String name,
        final List<Token> tokens,
        final List<Token> groups,
        final List<Token> varData,
        final String baseIndent)
    {
        final String indent = baseIndent + INDENT;
        final StringBuilder sb = new StringBuilder();

        sb.append('\n');
        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        append(sb, indent, "    final int originalLimit = limit();");
        append(sb, indent, "    limit(offset + actingBlockLength);");
        append(sb, indent, "    builder.append(\"[" + name + "](sbeTemplateId=\");");
        append(sb, indent, "    builder.append(TEMPLATE_ID);");
        append(sb, indent, "    builder.append(\"|sbeSchemaId=\");");
        append(sb, indent, "    builder.append(SCHEMA_ID);");
        append(sb, indent, "    builder.append(\"|sbeSchemaVersion=\");");
        append(sb, indent, "    if (parentMessage.actingVersion != SCHEMA_VERSION)");
        append(sb, indent, "    {");
        append(sb, indent, "        builder.append(parentMessage.actingVersion);");
        append(sb, indent, "        builder.append('/');");
        append(sb, indent, "    }");
        append(sb, indent, "    builder.append(SCHEMA_VERSION);");
        append(sb, indent, "    builder.append(\"|sbeBlockLength=\");");
        append(sb, indent, "    if (actingBlockLength != BLOCK_LENGTH)");
        append(sb, indent, "    {");
        append(sb, indent, "        builder.append(actingBlockLength);");
        append(sb, indent, "        builder.append('/');");
        append(sb, indent, "    }");
        append(sb, indent, "    builder.append(BLOCK_LENGTH);");
        append(sb, indent, "    builder.append(\"):\");");
        appendDecoderDisplay(sb, tokens, groups, varData, indent + INDENT);
        sb.append('\n');
        append(sb, indent, "    limit(originalLimit);");
        sb.append('\n');
        append(sb, indent, "    return builder;");
        append(sb, indent, "}");

        return sb.toString();
    }

    private void appendGroupInstanceDecoderDisplay(
        final StringBuilder sb,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String baseIndent)
    {
        final String indent = baseIndent + INDENT;

        sb.append('\n');
        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        Separators.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        appendDecoderDisplay(sb, fields, groups, varData, indent + INDENT);
        Separators.END_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        append(sb, indent, "    return builder;");
        append(sb, indent, "}");
    }

    private void appendDecoderDisplay(
        final StringBuilder sb,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        int lengthBeforeLastGeneratedSeparator = -1;

        for (int i = 0, size = fields.size(); i < size;)
        {
            final Token fieldToken = fields.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = fields.get(i + 1);

                final String fieldName = formatPropertyName(fieldToken.name());
                append(sb, indent, "//" + fieldToken);
                lengthBeforeLastGeneratedSeparator = writeTokenDisplay(fieldName, encodingToken, sb, indent);

                i += fieldToken.componentTokenCount();
            }
            else
            {
                ++i;
            }
        }

        for (int i = 0, size = groups.size(); i < size; i++)
        {
            final Token groupToken = groups.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            append(sb, indent, "//" + groupToken);

            final String groupName = formatPropertyName(groupToken.name());
            final String groupDecoderName = decoderName(formatClassName(groupToken.name()));

            append(
                sb, indent, "builder.append(\"" + groupName + Separators.KEY_VALUE + Separators.BEGIN_GROUP + "\");");
            append(sb, indent, groupDecoderName + " " + groupName + " = " + groupName + "();");
            append(sb, indent, "if (" + groupName + ".count() > 0)");
            append(sb, indent, "{");
            append(sb, indent, "    while (" + groupName + ".hasNext())");
            append(sb, indent, "    {");
            append(sb, indent, "        " + groupName + ".next().appendTo(builder);");
            Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
            append(sb, indent, "    }");
            append(sb, indent, "    builder.setLength(builder.length() - 1);");
            append(sb, indent, "}");
            Separators.END_GROUP.appendToGeneratedBuilder(sb, indent, "builder");

            lengthBeforeLastGeneratedSeparator = sb.length();
            Separators.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

            i = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
        }

        for (int i = 0, size = varData.size(); i < size;)
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            append(sb, indent, "//" + varDataToken);

            final String characterEncoding = varData.get(i + 3).encoding().characterEncoding();
            final String varDataName = formatPropertyName(varDataToken.name());
            append(sb, indent, "builder.append(\"" + varDataName + Separators.KEY_VALUE + "\");");
            if (null == characterEncoding)
            {
                append(sb, indent, "builder.append(" + varDataName + "Length() + \" bytes of raw data\");");
                append(sb, indent,
                    "parentMessage.limit(parentMessage.limit() + " + varDataName + "HeaderLength() + " +
                    varDataName + "Length());");
            }
            else
            {
                append(sb, indent, "builder.append('\\'' + " + varDataName + "() + '\\'');");
            }

            lengthBeforeLastGeneratedSeparator = sb.length();
            Separators.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

            i += varDataToken.componentTokenCount();
        }

        if (-1 != lengthBeforeLastGeneratedSeparator)
        {
            sb.setLength(lengthBeforeLastGeneratedSeparator);
        }
    }

    private int writeTokenDisplay(
        final String fieldName,
        final Token typeToken,
        final StringBuilder sb,
        final String indent)
    {
        append(sb, indent, "//" + typeToken);

        if (typeToken.encodedLength() <= 0 || typeToken.isConstantEncoding())
        {
            return -1;
        }

        append(sb, indent, "builder.append(\"" + fieldName + Separators.KEY_VALUE + "\");");

        switch (typeToken.signal())
        {
            case ENCODING:
                if (typeToken.arrayLength() > 1)
                {
                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        append(sb, indent,
                            "for (int i = 0; i < " + fieldName + "Length() && " + fieldName + "(i) > 0; i++)");
                        append(sb, indent, "{");
                        append(sb, indent, "    builder.append((char)" + fieldName + "(i));");
                        append(sb, indent, "}");
                    }
                    else
                    {
                        Separators.BEGIN_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                        append(sb, indent, "if (" + fieldName + "Length() > 0)");
                        append(sb, indent, "{");
                        append(sb, indent, "    for (int i = 0; i < " + fieldName + "Length(); i++)");
                        append(sb, indent, "    {");
                        append(sb, indent, "        builder.append(" + fieldName + "(i));");
                        Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
                        append(sb, indent, "    }");
                        append(sb, indent, "    builder.setLength(builder.length() - 1);");
                        append(sb, indent, "}");
                        Separators.END_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                    }
                }
                else
                {
                    // have to duplicate because of checkstyle :/
                    append(sb, indent, "builder.append(" + fieldName + "());");
                }
                break;

            case BEGIN_ENUM:
            case BEGIN_SET:
                append(sb, indent, "builder.append(" + fieldName + "());");
                break;

            case BEGIN_COMPOSITE:
                append(sb, indent, fieldName + "().appendTo(builder);");
                break;
        }

        final int lengthBeforeFieldSeparator = sb.length();
        Separators.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

        return lengthBeforeFieldSeparator;
    }

    private void appendToString(final StringBuilder sb, final String indent)
    {
        sb.append('\n');
        append(sb, indent, "public String toString()");
        append(sb, indent, "{");
        append(sb, indent, "    return appendTo(new StringBuilder(100)).toString();");
        append(sb, indent, "}");
    }
}
