/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.java;

import org.agrona.*;
import org.agrona.generation.OutputManager;
import org.agrona.sbe.*;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;

import static uk.co.real_logic.sbe.SbeTool.JAVA_INTERFACE_PACKAGE;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.CodecType.DECODER;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.CodecType.ENCODER;
import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

/**
 * Generate codecs for the Java 8 programming language.
 */
@SuppressWarnings("MethodLength")
public class JavaGenerator implements CodeGenerator
{
    enum CodecType
    {
        DECODER,
        ENCODER
    }

    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String PACKAGE_INFO = "package-info";
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

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
            throw new IllegalArgumentException("Unable to find " + fullyQualifiedBufferImplementation, ex);
        }
    }

    private String encoderName(final String className)
    {
        return formatClassName(className) + "Encoder";
    }

    private String decoderName(final String className)
    {
        return formatClassName(className) + "Decoder";
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
        generatePackageInfo();
        generateTypeStubs();
        generateMessageHeaderStub();

        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final List<Token> messageBody = getMessageBody(tokens);
            final boolean hasVarData = -1 != findSignal(messageBody, Signal.BEGIN_VAR_DATA);

            int i = 0;
            final List<Token> fields = new ArrayList<>();
            i = collectFields(messageBody, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(messageBody, i, groups);

            final List<Token> varData = new ArrayList<>();
            collectVarData(messageBody, i, varData);

            generateDecoder(msgToken, fields, groups, varData, hasVarData);
            generateEncoder(msgToken, fields, groups, varData, hasVarData);
        }
    }

    private void generateEncoder(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final boolean hasVarData)
        throws IOException
    {
        final String className = formatClassName(encoderName(msgToken.name()));
        final String implementsString = implementsInterface(MessageEncoderFlyweight.class.getSimpleName());

        try (Writer out = outputManager.createOutput(className))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), ENCODER, hasVarData));

            if (shouldGenerateGroupOrderAnnotation)
            {
                generateAnnotations(BASE_INDENT, className, groups, out, this::encoderName);
            }
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateEncoderFlyweightCode(className, msgToken));

            final StringBuilder sb = new StringBuilder();
            generateEncoderFields(sb, className, fields, BASE_INDENT);
            generateEncoderGroups(sb, className, groups, BASE_INDENT, false);
            generateEncoderVarData(sb, className, varData, BASE_INDENT);

            generateEncoderDisplay(sb, decoderName(msgToken.name()));

            out.append(sb);
            out.append("}\n");
        }
    }

    private void generateDecoder(
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final boolean hasVarData)
        throws IOException
    {
        final String className = formatClassName(decoderName(msgToken.name()));
        final String implementsString = implementsInterface(MessageDecoderFlyweight.class.getSimpleName());

        try (Writer out = outputManager.createOutput(className))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), DECODER, hasVarData));

            if (shouldGenerateGroupOrderAnnotation)
            {
                generateAnnotations(BASE_INDENT, className, groups, out, this::decoderName);
            }
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateDecoderFlyweightCode(className, msgToken));

            final StringBuilder sb = new StringBuilder();
            generateDecoderFields(sb, fields, BASE_INDENT);
            generateDecoderGroups(sb, className, groups, BASE_INDENT, false);
            generateDecoderVarData(sb, varData, BASE_INDENT);

            generateDecoderDisplay(sb, msgToken.name(), fields, groups, varData);

            out.append(sb);
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

            final int index = i;
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

            generateGroupDecoderProperty(sb, groupName, groupToken, indent, isSubGroup);
            generateTypeJavadoc(sb, indent + INDENT, groupToken);

            if (shouldGenerateGroupOrderAnnotation)
            {
                generateAnnotations(indent + INDENT, groupName, groups, sb, this::decoderName);
            }
            generateGroupDecoderClassHeader(sb, groupName, outerClassName, tokens, groups, index, indent + INDENT);

            generateDecoderFields(sb, fields, indent + INDENT);
            generateDecoderGroups(sb, outerClassName, groups, indent + INDENT, true);
            generateDecoderVarData(sb, varData, indent + INDENT);

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

            final int index = i;
            final String groupName = groupToken.name();
            final String groupClassName = encoderName(groupName);

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);

            generateGroupEncoderProperty(sb, groupName, groupToken, indent, isSubGroup);
            generateTypeJavadoc(sb, indent + INDENT, groupToken);

            if (shouldGenerateGroupOrderAnnotation)
            {
                generateAnnotations(indent + INDENT, groupClassName, groups, sb, this::encoderName);
            }
            generateGroupEncoderClassHeader(sb, groupName, outerClassName, tokens, groups, index, indent + INDENT);

            generateEncoderFields(sb, groupClassName, fields, indent + INDENT);
            generateEncoderGroups(sb, outerClassName, groups, indent + INDENT, true);
            generateEncoderVarData(sb, groupClassName, varData, indent + INDENT);

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
        final String className = formatClassName(groupName);
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
            groupName,
            parentMessageClassName,
            findSubGroupNames(subGroupTokens),
            indent,
            dimensionHeaderLen);

        final String blockLenCast = PrimitiveType.UINT32 == blockLengthType ? "(int)" : "";
        final String numInGroupCast = PrimitiveType.UINT32 == numInGroupType ? "(int)" : "";

        sb.append("\n")
            .append(indent).append("    public void wrap(final ").append(readOnlyBuffer).append(" buffer)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        if (buffer != this.buffer)\n")
            .append(indent).append("        {\n")
            .append(indent).append("            this.buffer = buffer;\n")
            .append(indent).append("        }\n\n")
            .append(indent).append("        index = 0;\n")
            .append(indent).append("        final int limit = parentMessage.limit();\n")
            .append(indent).append("        parentMessage.limit(limit + HEADER_SIZE);\n")
            .append(indent).append("        blockLength = ").append(blockLenCast).append(blockLengthGet).append(";\n")
            .append(indent).append("        count = ").append(numInGroupCast).append(numInGroupGet).append(";\n")
            .append(indent).append("    }\n");

        sb.append("\n")
            .append(indent).append("    public ").append(className).append(" next()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        if (index >= count)\n")
            .append(indent).append("        {\n")
            .append(indent).append("            throw new java.util.NoSuchElementException();\n")
            .append(indent).append("        }\n\n")
            .append(indent).append("        offset = parentMessage.limit();\n")
            .append(indent).append("        parentMessage.limit(offset + blockLength);\n")
            .append(indent).append("        ++index;\n\n")
            .append(indent).append("        return this;\n")
            .append(indent).append("    }\n");

        sb.append("\n")
            .append(indent).append("    public static int sbeHeaderSize()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return HEADER_SIZE;\n")
            .append(indent).append("    }\n");

        sb.append("\n")
            .append(indent).append("    public static int sbeBlockLength()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(tokens.get(index).encodedLength()).append(";\n")
            .append(indent).append("    }\n");

        sb.append("\n")
            .append(indent).append("    public int actingBlockLength()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return blockLength;\n")
            .append(indent).append("    }\n\n")
            .append(indent).append("    public int count()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return count;\n")
            .append(indent).append("    }\n\n")
            .append(indent).append("    public java.util.Iterator<").append(className).append("> iterator()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return this;\n")
            .append(indent).append("    }\n\n")
            .append(indent).append("    public void remove()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        throw new UnsupportedOperationException();\n")
            .append(indent).append("    }\n\n")
            .append(indent).append("    public boolean hasNext()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return index < count;\n")
            .append(indent).append("    }\n");
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
        final int dimensionHeaderSize = tokens.get(index + 1).encodedLength();

        generateGroupEncoderClassDeclaration(
            sb,
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
        final String blockLengthValue = Integer.toString(blockLength);
        final String blockLengthPut = generatePut(
            blockLengthType, blockLengthOffset, blockLengthValue, byteOrderString(blockLengthToken.encoding()));

        final PrimitiveType numInGroupType = numInGroupToken.encoding().primitiveType();
        final PrimitiveType newInGroupTypeCast = PrimitiveType.UINT32 == numInGroupType ?
            PrimitiveType.INT32 : numInGroupType;
        final String numInGroupOffset = "limit + " + numInGroupToken.offset();
        final String numInGroupValue = "count";
        final String numInGroupPut = generatePut(
            newInGroupTypeCast, numInGroupOffset, numInGroupValue, byteOrderString(numInGroupToken.encoding()));

        new Formatter(sb).format("\n" +
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
            ind + "        index = 0;\n" +
            ind + "        this.count = count;\n" +
            ind + "        final int limit = parentMessage.limit();\n" +
            ind + "        initialLimit = limit;\n" +
            ind + "        parentMessage.limit(limit + HEADER_SIZE);\n" +
            ind + "        %5$s;\n" +
            ind + "        %6$s;\n" +
            ind + "    }\n",
            parentMessageClassName,
            mutableBuffer,
            numInGroupToken.encoding().applicableMinValue().longValue(),
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            blockLengthPut,
            numInGroupPut);

        sb.append("\n")
            .append(ind).append("    public ").append(encoderName(groupName)).append(" next()\n")
            .append(ind).append("    {\n")
            .append(ind).append("        if (index >= count)\n")
            .append(ind).append("        {\n")
            .append(ind).append("            throw new java.util.NoSuchElementException();\n")
            .append(ind).append("        }\n\n")
            .append(ind).append("        offset = parentMessage.limit();\n")
            .append(ind).append("        parentMessage.limit(offset + sbeBlockLength());\n")
            .append(ind).append("        ++index;\n\n")
            .append(ind).append("        return this;\n")
            .append(ind).append("    }\n");

        final String countOffset = "initialLimit + " + numInGroupToken.offset();
        final String resetCountPut = generatePut(
            newInGroupTypeCast, countOffset, numInGroupValue, byteOrderString(numInGroupToken.encoding()));

        sb.append("\n")
            .append(ind).append("    public int resetCountToIndex()\n")
            .append(ind).append("    {\n")
            .append(ind).append("        count = index;\n")
            .append(ind).append("        ").append(resetCountPut).append(";\n\n")
            .append(ind).append("        return count;\n")
            .append(ind).append("    }\n");

        sb.append("\n")
            .append(ind).append("    public static int sbeHeaderSize()\n")
            .append(ind).append("    {\n")
            .append(ind).append("        return HEADER_SIZE;\n")
            .append(ind).append("    }\n");

        sb.append("\n")
            .append(ind).append("    public static int sbeBlockLength()\n")
            .append(ind).append("    {\n")
            .append(ind).append("        return ").append(blockLength).append(";\n")
            .append(ind).append("    }\n");
    }

    private static String primitiveTypeName(final Token token)
    {
        return javaTypeName(token.encoding().primitiveType());
    }

    private void generateGroupDecoderClassDeclaration(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<String> subGroupNames,
        final String indent,
        final int dimensionHeaderSize)
    {
        final String className = formatClassName(groupName);

        new Formatter(sb).format("\n" +
            indent + "public static class %1$s\n" +
            indent + "    implements Iterable<%1$s>, java.util.Iterator<%1$s>\n" +
            indent + "{\n" +
            indent + "    public static final int HEADER_SIZE = %2$d;\n" +
            indent + "    private final %3$s parentMessage;\n" +
            indent + "    private %4$s buffer;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n" +
            indent + "    private int blockLength;\n",
            className,
            dimensionHeaderSize,
            parentMessageClassName,
            readOnlyBuffer);

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

        sb.append(indent).append("    }\n");
    }

    private void generateGroupEncoderClassDeclaration(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final List<String> subGroupNames,
        final String indent,
        final int dimensionHeaderSize)
    {
        final String className = encoderName(groupName);

        new Formatter(sb).format("\n" +
            indent + "public static class %1$s\n" +
            indent + "{\n" +
            indent + "    public static final int HEADER_SIZE = %2$d;\n" +
            indent + "    private final %3$s parentMessage;\n" +
            indent + "    private %4$s buffer;\n" +
            indent + "    private int count;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n" +
            indent + "    private int initialLimit;\n",
            className,
            dimensionHeaderSize,
            parentMessageClassName,
            mutableBuffer);

        for (final String subGroupName : subGroupNames)
        {
            final String type = encoderName(subGroupName);
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
            final String type = encoderName(subGroupName);
            final String field = formatPropertyName(subGroupName);
            sb
                .append(indent).append("        ")
                .append(field).append(" = new ").append(type).append("(parentMessage);\n");
        }

        sb.append(indent).append("    }\n");
    }

    private static void generateGroupDecoderProperty(
        final StringBuilder sb,
        final String groupName,
        final Token token,
        final String indent,
        final boolean isSubGroup)
    {
        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(token.name());

        if (!isSubGroup)
        {
            new Formatter(sb).format("\n" +
                indent + "    private final %s %s = new %s(this);\n",
                className,
                propertyName,
                className);
        }

        new Formatter(sb).format("\n" +
            indent + "    public static long %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.id());

        new Formatter(sb).format("\n" +
            indent + "    public static int %sSinceVersion()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.version());

        final String actingVersionGuard = token.version() == 0 ?
            "" :
            indent + "        if (parentMessage.actingVersion < " + token.version() + ")\n" +
            indent + "        {\n" +
            indent + "            " + propertyName + ".count = 0;\n" +
            indent + "            " + propertyName + ".index = 0;\n" +
            indent + "            return " + propertyName + ";\n" +
            indent + "        }\n\n";

        generateFlyweightPropertyJavadoc(sb, indent + INDENT, token, className);
        new Formatter(sb).format("\n" +
            indent + "    public %1$s %2$s()\n" +
            indent + "    {\n" +
            "%3$s" +
            indent + "        %2$s.wrap(buffer);\n" +
            indent + "        return %2$s;\n" +
            indent + "    }\n",
            className,
            propertyName,
            actingVersionGuard);
    }

    private void generateGroupEncoderProperty(
        final StringBuilder sb,
        final String groupName,
        final Token token,
        final String indent,
        final boolean isSubGroup)
    {
        final String className = formatClassName(encoderName(groupName));
        final String propertyName = formatPropertyName(groupName);

        if (!isSubGroup)
        {
            new Formatter(sb).format("\n" +
                indent + "    private final %s %s = new %s(this);\n",
                className,
                propertyName,
                className);
        }

        new Formatter(sb).format("\n" +
            indent + "    public static long %sId()\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            formatPropertyName(groupName),
            token.id());

        generateGroupEncodePropertyJavadoc(sb, indent + INDENT, token, className);
        new Formatter(sb).format("\n" +
            indent + "    public %1$s %2$sCount(final int count)\n" +
            indent + "    {\n" +
            indent + "        %2$s.wrap(buffer, count);\n" +
            indent + "        return %2$s;\n" +
            indent + "    }\n",
            className,
            propertyName);
    }

    private void generateDecoderVarData(
        final StringBuilder sb, final List<Token> tokens, final String indent)
    {
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
            final String methodPropName = Generators.toLowerFirstChar(propertyName);

            sb.append("\n")
                .append(indent).append("    public static int ").append(methodPropName).append("HeaderLength()\n")
                .append(indent).append("    {\n")
                .append(indent).append("        return ").append(sizeOfLengthField).append(";\n")
                .append(indent).append("    }\n");

            sb.append("\n")
                .append(indent).append("    public int ").append(methodPropName).append("Length()\n")
                .append(indent).append("    {\n")
                .append(generateArrayFieldNotPresentCondition(token.version(), indent))
                .append(indent).append("        final int limit = parentMessage.limit();\n")
                .append(indent).append("        return ").append(PrimitiveType.UINT32 == lengthType ? "(int)" : "")
                .append(generateGet(lengthType, "limit", byteOrderStr)).append(";\n")
                .append(indent).append("    }\n");

            generateDataDecodeMethods(
                sb, token, propertyName, sizeOfLengthField, lengthType, byteOrderStr, characterEncoding, indent);

            i += token.componentTokenCount();
        }
    }

    private void generateEncoderVarData(
        final StringBuilder sb, final String className, final List<Token> tokens, final String indent)
    {
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

            final String methodPropName = Generators.toLowerFirstChar(propertyName);
            sb.append("\n")
                .append(indent).append("    public static int ").append(methodPropName).append("HeaderLength()\n")
                .append(indent).append("    {\n")
                .append(indent).append("        return ")
                .append(sizeOfLengthField).append(";\n")
                .append(indent).append("    }\n");

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
        new Formatter(sb).format("\n" +
            indent + "    public int skip%1$s()\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        final int headerLength = %3$d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = %4$s%5$s;\n" +
            indent + "        final int dataOffset = limit + headerLength;\n" +
            indent + "        parentMessage.limit(dataOffset + dataLength);\n\n" +
            indent + "        return dataLength;\n" +
            indent + "    }\n",
            Generators.toUpperFirstChar(propertyName),
            generateStringNotPresentConditionForAppendable(token.version(), indent),
            sizeOfLengthField,
            PrimitiveType.UINT32 == lengthType ? "(int)" : "",
            generateGet(lengthType, "limit", byteOrderStr));

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
            new Formatter(sb).format("\n" +
                indent + "    public String %1$s()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        final int headerLength = %3$d;\n" +
                indent + "        final int limit = parentMessage.limit();\n" +
                indent + "        final int dataLength = %4$s%5$s;\n" +
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
                indent + "            value = new String(tmp, \"%6$s\");\n" +
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
                PrimitiveType.UINT32 == lengthType ? "(int)" : "",
                generateGet(lengthType, "limit", byteOrderStr),
                characterEncoding);

            if (characterEncoding.contains("ASCII"))
            {
                new Formatter(sb).format("\n" +
                    indent + "    public int get%1$s(final Appendable appendable)\n" +
                    indent + "    {\n" +
                    "%2$s" +
                    indent + "        final int headerLength = %3$d;\n" +
                    indent + "        final int limit = parentMessage.limit();\n" +
                    indent + "        final int dataLength = %4$s%5$s;\n" +
                    indent + "        final int dataOffset = limit + headerLength;\n\n" +
                    indent + "        parentMessage.limit(dataOffset + dataLength);\n" +
                    indent + "        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);\n\n" +
                    indent + "        return dataLength;\n" +
                    indent + "    }\n",
                    Generators.toUpperFirstChar(propertyName),
                    generateStringNotPresentConditionForAppendable(token.version(), indent),
                    sizeOfLengthField,
                    PrimitiveType.UINT32 == lengthType ? "(int)" : "",
                    generateGet(lengthType, "limit", byteOrderStr));
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
        new Formatter(sb).format("\n" +
            indent + "    public void wrap%s(final %s wrapBuffer)\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        final int headerLength = %d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = %s%s;\n" +
            indent + "        parentMessage.limit(limit + headerLength + dataLength);\n" +
            indent + "        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);\n" +
            indent + "    }\n",
            propertyName,
            readOnlyBuffer,
            generateWrapFieldNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            PrimitiveType.UINT32 == lengthType ? "(int)" : "",
            generateGet(lengthType, "limit", byteOrderStr));
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
        final PrimitiveType lengthPutType = PrimitiveType.UINT32 == lengthType ? PrimitiveType.INT32 : lengthType;

        if (characterEncoding.contains("ASCII"))
        {
            new Formatter(sb).format("\n" +
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
                generatePut(lengthPutType, "limit", "length", byteOrderStr));

            new Formatter(sb).format("\n" +
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
                generatePut(lengthPutType, "limit", "length", byteOrderStr));
        }
        else
        {
            new Formatter(sb).format("\n" +
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
                generatePut(lengthPutType, "limit", "length", byteOrderStr));
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
        new Formatter(sb).format("\n" +
            indent + "    public int get%s(final %s dst, final int dstOffset, final int length)\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        final int headerLength = %d;\n" +
            indent + "        final int limit = parentMessage.limit();\n" +
            indent + "        final int dataLength = %s%s;\n" +
            indent + "        final int bytesCopied = Math.min(length, dataLength);\n" +
            indent + "        parentMessage.limit(limit + headerLength + dataLength);\n" +
            indent + "        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);\n\n" +
            indent + "        return bytesCopied;\n" +
            indent + "    }\n",
            propertyName,
            exchangeType,
            generateArrayFieldNotPresentCondition(token.version(), indent),
            sizeOfLengthField,
            PrimitiveType.UINT32 == lengthType ? "(int)" : "",
            generateGet(lengthType, "limit", byteOrderStr));
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
        final PrimitiveType lengthPutType = PrimitiveType.UINT32 == lengthType ? PrimitiveType.INT32 : lengthType;

        new Formatter(sb).format("\n" +
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
            generatePut(lengthPutType, "limit", "length", byteOrderStr));
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String bitSetName = token.applicableTypeName();
        final String decoderName = decoderName(bitSetName);
        final String encoderName = encoderName(bitSetName);
        final List<Token> messageBody = getMessageBody(tokens);
        final String implementsString = implementsInterface(Flyweight.class.getSimpleName());

        try (Writer out = outputManager.createOutput(decoderName))
        {
            generateFixedFlyweightHeader(out, token, decoderName, implementsString, readOnlyBuffer, fqReadOnlyBuffer);
            out.append(generateChoiceIsEmpty(token.encoding().primitiveType()));
            generateChoiceDecoders(out, messageBody);
            out.append(generateChoiceDisplay(messageBody));
            out.append("}\n");
        }

        try (Writer out = outputManager.createOutput(encoderName))
        {
            generateFixedFlyweightHeader(out, token, encoderName, implementsString, mutableBuffer, fqMutableBuffer);
            generateChoiceClear(out, encoderName, token);
            generateChoiceEncoders(out, encoderName, messageBody);
            out.append("}\n");
        }
    }

    private void generateFixedFlyweightHeader(
        final Writer out,
        final Token token,
        final String typeName,
        final String implementsString,
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
        final String compositeName = token.applicableTypeName();
        final String decoderName = decoderName(compositeName);
        final String encoderName = encoderName(compositeName);

        try (Writer out = outputManager.createOutput(decoderName))
        {
            final String implementsString = implementsInterface(CompositeDecoderFlyweight.class.getSimpleName());
            generateCompositeFlyweightHeader(
                token, decoderName, out, readOnlyBuffer, fqReadOnlyBuffer, implementsString);

            for (int i = 1, end = tokens.size() - 1; i < end;)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = decoderName(encodingToken.applicableTypeName());

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

            out.append(generateCompositeDecoderDisplay(tokens));

            out.append("}\n");
        }

        try (Writer out = outputManager.createOutput(encoderName))
        {
            final String implementsString = implementsInterface(CompositeEncoderFlyweight.class.getSimpleName());
            generateCompositeFlyweightHeader(token, encoderName, out, mutableBuffer, fqMutableBuffer, implementsString);

            for (int i = 1, end = tokens.size() - 1; i < end;)
            {
                final Token encodingToken = tokens.get(i);
                final String propertyName = formatPropertyName(encodingToken.name());
                final String typeName = encoderName(encodingToken.applicableTypeName());

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

            out.append(generateCompositeEncoderDisplay(decoderName));
            out.append("}\n");
        }
    }

    private void generateChoiceClear(final Appendable out, final String bitSetClassName, final Token token)
        throws IOException
    {
        final Encoding encoding = token.encoding();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final String byteOrderStr = byteOrderString(encoding);

        final String clearStr = generatePut(encoding.primitiveType(), "offset", literalValue, byteOrderStr);
        out.append("\n")
            .append("    public ").append(bitSetClassName).append(" clear()\n")
            .append("    {\n")
            .append("        ").append(clearStr).append(";\n")
            .append("        return this;\n")
            .append("    }\n");
    }

    private void generateChoiceDecoders(final Appendable out, final List<Token> tokens)
        throws IOException
    {
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = formatPropertyName(token.name());
                final Encoding encoding = token.encoding();
                final String choiceBitIndex = encoding.constValue().toString();
                final String byteOrderStr = byteOrderString(encoding);
                final PrimitiveType primitiveType = encoding.primitiveType();
                final String argType = bitsetArgType(primitiveType);

                generateOptionDecodeJavadoc(out, INDENT, token);
                final String choiceGet = generateChoiceGet(primitiveType, choiceBitIndex, byteOrderStr);
                final String staticChoiceGet = generateStaticChoiceGet(primitiveType, choiceBitIndex);
                out.append("\n")
                    .append("    public boolean ").append(choiceName).append("()\n")
                    .append("    {\n")
                    .append("        return ").append(choiceGet).append(";\n")
                    .append("    }\n\n")
                    .append("    public static boolean ").append(choiceName)
                    .append("(final ").append(argType).append(" value)\n")
                    .append("    {\n").append("        return ").append(staticChoiceGet).append(";\n")
                    .append("    }\n");
            }
        }
    }

    private void generateChoiceEncoders(final Appendable out, final String bitSetClassName, final List<Token> tokens)
        throws IOException
    {
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = formatPropertyName(token.name());
                final Encoding encoding = token.encoding();
                final String choiceBitIndex = encoding.constValue().toString();
                final String byteOrderStr = byteOrderString(encoding);
                final PrimitiveType primitiveType = encoding.primitiveType();
                final String argType = bitsetArgType(primitiveType);

                generateOptionEncodeJavadoc(out, INDENT, token);
                final String choicePut = generateChoicePut(encoding.primitiveType(), choiceBitIndex, byteOrderStr);
                final String staticChoicePut = generateStaticChoicePut(encoding.primitiveType(), choiceBitIndex);
                out.append("\n")
                    .append("    public ").append(bitSetClassName).append(" ").append(choiceName)
                    .append("(final boolean value)\n")
                    .append("    {\n")
                    .append(choicePut).append("\n")
                    .append("        return this;\n")
                    .append("    }\n\n")
                    .append("    public static ").append(argType).append(" ").append(choiceName)
                    .append("(final ").append(argType).append(" bits, final boolean value)\n")
                    .append("    {\n")
                    .append(staticChoicePut)
                    .append("    }\n");
            }
        }
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
            generateTypeJavadoc(sb, INDENT, token);
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

        return
            "    private final " + javaEncodingType + " value;\n\n" +
            "    " + enumName + "(final " + javaEncodingType + " value)\n" +
            "    {\n" +
            "        this.value = value;\n" +
            "    }\n\n" +
            "    public " + javaEncodingType + " value()\n" +
            "    {\n" +
            "        return value;\n" +
            "    }\n";
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final String enumName)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = tokens.get(0).encoding().primitiveType();
        sb.append("\n").append("    public static ").append(enumName)
            .append(" get(final ").append(javaTypeName(primitiveType)).append(" value)\n").append("    {\n")
            .append("        switch (value)\n").append("        {\n");

        for (final Token token : tokens)
        {
            final String constStr = token.encoding().constValue().toString();
            final String name = token.name();
            sb.append("            case ").append(constStr).append(": return ").append(name).append(";\n");
        }

        final String nullValue = tokens.get(0).encoding().applicableNullValue().toString();
        sb.append("            case ").append(nullValue).append(": return NULL_VAL").append(";\n");

        final String handleUnknownLogic = shouldDecodeUnknownEnumValues ?
            INDENT + INDENT + "return SBE_UNKNOWN;\n" :
            INDENT + INDENT + "throw new IllegalArgumentException(\"Unknown value: \" + value);\n";

        sb.append("        }\n\n")
            .append(handleUnknownLogic)
            .append("    }\n");

        return sb;
    }

    private String interfaceImportLine()
    {
        if (!shouldGenerateInterfaces)
        {
            return "\n";
        }

        return "import " + JAVA_INTERFACE_PACKAGE + ".*;\n\n";
    }

    private CharSequence generateFileHeader(final String packageName, final String fqBuffer)
    {
        return
            "/* Generated SBE (Simple Binary Encoding) message codec. */\n" +
            "package " + packageName + ";\n\n" +
            "import " + fqBuffer + ";\n" +
            interfaceImportLine();
    }

    private CharSequence generateMainHeader(
        final String packageName, final CodecType codecType, final boolean hasVarData)
    {
        if (fqMutableBuffer.equals(fqReadOnlyBuffer))
        {
            return
                "/* Generated SBE (Simple Binary Encoding) message codec. */\n" +
                "package " + packageName + ";\n\n" +
                "import " + fqMutableBuffer + ";\n" +
                interfaceImportLine();
        }
        else
        {
            final boolean hasMutableBuffer = ENCODER == codecType || hasVarData;
            final boolean hasReadOnlyBuffer = DECODER == codecType || hasVarData;

            return
                "/* Generated SBE (Simple Binary Encoding) message codec. */\n" +
                "package " + packageName + ";\n\n" +
                (hasMutableBuffer ? "import " + fqMutableBuffer + ";\n" : "") +
                (hasReadOnlyBuffer ? "import " + fqReadOnlyBuffer + ";\n" : "") +
                interfaceImportLine();
        }
    }

    private static CharSequence generateEnumFileHeader(final String packageName)
    {
        return
            "/* Generated SBE (Simple Binary Encoding) message codec. */\n" +
            "package " + packageName + ";\n\n";
    }

    private void generateAnnotations(
        final String indent,
        final String className,
        final List<Token> tokens,
        final Appendable out,
        final Function<String, String> nameMapping) throws IOException
    {
        final List<String> groupClassNames = new ArrayList<>();
        int level = 0;

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.BEGIN_GROUP)
            {
                if (1 == ++level)
                {
                    groupClassNames.add(formatClassName(nameMapping.apply(token.name())));
                }
            }
            else if (token.signal() == Signal.END_GROUP)
            {
                --level;
            }
        }

        if (!groupClassNames.isEmpty())
        {
            out.append(indent).append("@uk.co.real_logic.sbe.codec.java.GroupOrder({\n");
            int i = 0;
            for (final String name : groupClassNames)
            {
                out.append(indent).append(INDENT).append(className).append('.').append(name).append(".class");
                if (++i < groupClassNames.size())
                {
                    out.append(",\n");
                }
            }

            out.append("})");
        }
    }

    private static CharSequence generateDeclaration(
        final String className, final String implementsString, final Token typeToken)
    {
        final StringBuilder sb = new StringBuilder();

        generateTypeJavadoc(sb, BASE_INDENT, typeToken);
        sb.append("@SuppressWarnings(\"all\")\n")
            .append("public class ").append(className).append(implementsString).append('\n')
            .append("{\n");

        return sb;
    }

    private void generatePackageInfo() throws IOException
    {
        try (Writer out = outputManager.createOutput(PACKAGE_INFO))
        {
            out.append(
                "/* Generated SBE (Simple Binary Encoding) message codecs.*/\n" +
                "/**\n" +
                " * ").append(ir.description()).append("\n")
                .append(
                " */\n" +
                "package ").append(ir.applicableNamespace()).append(";\n");
        }
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (Writer out = outputManager.createOutput(META_ATTRIBUTE_ENUM))
        {
            out.append(
                "/* Generated SBE (Simple Binary Encoding) message codec. */\n" +
                "package ").append(ir.applicableNamespace()).append(";\n\n")
                .append(
                "/**\n" +
                " * Meta attribute enum for selecting a particular meta attribute value.\n" +
                " */\n" +
                "public enum MetaAttribute\n" +
                "{\n" +
                "    /**\n" +
                "     * The epoch or start of time. Default is 'UNIX' which is midnight January 1, 1970 UTC\n" +
                "     */\n" +
                "    EPOCH,\n\n" +
                "    /**\n" +
                "     * Time unit applied to the epoch. Can be second, millisecond, microsecond, or nanosecond.\n" +
                "     */\n" +
                "    TIME_UNIT,\n\n" +
                "    /**\n" +
                "     * The type relationship to a FIX tag value encoded type. For reference only.\n" +
                "     */\n" +
                "    SEMANTIC_TYPE,\n\n" +
                "    /**\n" +
                "     * Field presence indication. Can be optional, required, or constant.\n" +
                "     */\n" +
                "    PRESENCE\n" +
                "}\n");
        }
    }

    private static CharSequence generateEnumDeclaration(final String name, final Token typeToken)
    {
        final StringBuilder sb = new StringBuilder();

        generateTypeJavadoc(sb, BASE_INDENT, typeToken);
        sb.append("public enum ").append(name).append("\n{\n");

        return sb;
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

        generatePrimitiveFieldMetaMethod(sb, formattedPropertyName, encodingToken, indent);

        if (encodingToken.isConstantEncoding())
        {
            generateConstPropertyMethods(sb, formattedPropertyName, encodingToken, indent);
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

        generatePrimitiveFieldMetaMethod(sb, formattedPropertyName, token, indent);

        if (!token.isConstantEncoding())
        {
            sb.append(generatePrimitivePropertyEncodeMethods(
                containingClassName, formattedPropertyName, token, indent));
        }
        else
        {
            generateConstPropertyMethods(sb, formattedPropertyName, token, indent);
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

    private void generatePrimitiveFieldMetaMethod(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String javaTypeName = javaTypeName(primitiveType);
        final String formattedPropertyName = formatPropertyName(propertyName);

        final String nullValue = generateLiteral(primitiveType, token.encoding().applicableNullValue().toString());
        generatePrimitiveFieldMetaMethod(sb, indent, javaTypeName, formattedPropertyName, "Null", nullValue);

        final String minValue = generateLiteral(primitiveType, token.encoding().applicableMinValue().toString());
        generatePrimitiveFieldMetaMethod(sb, indent, javaTypeName, formattedPropertyName, "Min", minValue);

        final String maxValue = generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString());
        generatePrimitiveFieldMetaMethod(sb, indent, javaTypeName, formattedPropertyName, "Max", maxValue);
    }

    private void generatePrimitiveFieldMetaMethod(
        final StringBuilder sb,
        final String indent,
        final String javaTypeName,
        final String formattedPropertyName,
        final String metaType,
        final String retValue)
    {
        sb.append("\n")
            .append(indent).append("    public static ")
            .append(javaTypeName).append(" ").append(formattedPropertyName).append(metaType).append("Value()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(retValue).append(";\n")
            .append(indent).append("    }\n");
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

    private CharSequence generateWrapFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return
            indent + "        if (parentMessage.actingVersion < " + sinceVersion + ")\n" +
            indent + "        {\n" +
            indent + "            wrapBuffer.wrap(buffer, offset, 0);\n" +
            indent + "            return;\n" +
            indent + "        }\n\n";
    }

    private CharSequence generateFieldNotPresentCondition(
        final boolean inComposite, final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (inComposite || 0 == sinceVersion)
        {
            return "";
        }

        final String nullValue = generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
        return
            indent + "        if (parentMessage.actingVersion < " + sinceVersion + ")\n" +
            indent + "        {\n" +
            indent + "            return " + nullValue + ";\n" +
            indent + "        }\n\n";
    }

    private static CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return
            indent + "        if (parentMessage.actingVersion < " + sinceVersion + ")\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
            indent + "        }\n\n";
    }

    private static CharSequence generateStringNotPresentConditionForAppendable(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return
            indent + "        if (parentMessage.actingVersion < " + sinceVersion + ")\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
            indent + "        }\n\n";
    }

    private static CharSequence generateStringNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return
            indent + "        if (parentMessage.actingVersion < " + sinceVersion + ")\n" +
            indent + "        {\n" +
            indent + "            return \"\";\n" +
            indent + "        }\n\n";
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

        final String nullValue = enumName == null ? "null" : (enumName + ".NULL_VAL");
        return
            indent + "        if (parentMessage.actingVersion < " + propertyToken.version() + ")\n" +
            indent + "        {\n" +
            indent + "            return " + nullValue + ";\n" +
            indent + "        }\n\n";
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

        new Formatter(sb).format("\n" +
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            "%s" +
            indent + "        final int pos = offset + %d + (index * %d);\n\n" +
            indent + "        return %s;\n" +
            indent + "    }\n\n",
            javaTypeName,
            propertyName,
            fieldLength,
            generateFieldNotPresentCondition(inComposite, propertyToken.version(), encoding, indent),
            offset,
            typeSize,
            generateGet(encoding.primitiveType(), "pos", byteOrderStr));

        if (encoding.primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, encoding.characterEncoding(), indent);

            new Formatter(sb).format("\n" +
                indent + "    public int get%s(final byte[] dst, final int dstOffset)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        if (dstOffset < 0 || dstOffset > (dst.length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"Copy will go out of range: offset=\" + dstOffset);\n" +
                indent + "        }\n\n" +
                "%s" +
                indent + "        buffer.getBytes(offset + %d, dst, dstOffset, length);\n\n" +
                indent + "        return length;\n" +
                indent + "    }\n",
                Generators.toUpperFirstChar(propertyName),
                fieldLength,
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                offset);

            new Formatter(sb).format("\n" +
                indent + "    public String %s()\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        final byte[] dst = new byte[%d];\n" +
                indent + "        buffer.getBytes(offset + %d, dst, 0, %d);\n\n" +
                indent + "        int end = 0;\n" +
                indent + "        for (; end < %d && dst[end] != 0; ++end);\n\n" +
                indent + "        return new String(dst, 0, end, %s);\n" +
                indent + "    }\n\n",
                propertyName,
                generateStringNotPresentCondition(propertyToken.version(), indent),
                fieldLength,
                offset,
                fieldLength,
                fieldLength,
                charset(encoding.characterEncoding()));

            if (encoding.characterEncoding().contains("ASCII"))
            {
                new Formatter(sb).format("\n" +
                    indent + "    public int get%1$s(final Appendable value)\n" +
                    indent + "    {\n" +
                    "%2$s" +
                    indent + "        for (int i = 0; i < %3$d; ++i)\n" +
                    indent + "        {\n" +
                    indent + "            final int c = buffer.getByte(offset + %4$d + i) & 0xFF;\n" +
                    indent + "            if (c == 0)\n" +
                    indent + "            {\n" +
                    indent + "                return i;\n" +
                    indent + "            }\n\n" +
                    indent + "            try\n" +
                    indent + "            {\n" +
                    indent + "                value.append(c > 127 ? '?' : (char)c);\n" +
                    indent + "            }\n" +
                    indent + "            catch (final java.io.IOException ex)\n" +
                    indent + "            {\n" +
                    indent + "                throw new java.io.UncheckedIOException(ex);\n" +
                    indent + "            }\n" +
                    indent + "        }\n\n" +
                    indent + "        return %3$d;\n" +
                    indent + "    }\n\n",
                    Generators.toUpperFirstChar(propertyName),
                    generateStringNotPresentConditionForAppendable(propertyToken.version(), indent),
                    fieldLength,
                    offset);
            }
        }
        else if (encoding.primitiveType() == PrimitiveType.UINT8)
        {
            new Formatter(sb).format("\n" +
                indent + "    public int get%s(final byte[] dst, final int dstOffset, final int length)\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        final int bytesCopied = Math.min(length, %d);\n" +
                indent + "        buffer.getBytes(offset + %d, dst, dstOffset, bytesCopied);\n\n" +
                indent + "        return bytesCopied;\n" +
                indent + "    }\n",
                Generators.toUpperFirstChar(propertyName),
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                fieldLength,
                offset);

            new Formatter(sb).format("\n" +
                indent + "    public int get%s(final %s dst, final int dstOffset, final int length)\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        final int bytesCopied = Math.min(length, %d);\n" +
                indent + "        buffer.getBytes(offset + %d, dst, dstOffset, bytesCopied);\n\n" +
                indent + "        return bytesCopied;\n" +
                indent + "    }\n",
                Generators.toUpperFirstChar(propertyName),
                fqMutableBuffer,
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                fieldLength,
                offset);

            new Formatter(sb).format("\n" +
                indent + "    public void wrap%s(final %s wrapBuffer)\n" +
                indent + "    {\n" +
                "%s" +
                indent + "        wrapBuffer.wrap(buffer, offset + %d, %d);\n" +
                indent + "    }\n",
                Generators.toUpperFirstChar(propertyName),
                readOnlyBuffer,
                generateWrapFieldNotPresentCondition(propertyToken.version(), indent),
                offset,
                fieldLength);
        }

        return sb;
    }

    private static void generateArrayLengthMethod(
        final String propertyName, final String indent, final int fieldLength, final StringBuilder sb)
    {
        final String formatPropertyName = formatPropertyName(propertyName);
        sb.append("\n")
            .append(indent).append("    public static int ").append(formatPropertyName).append("Length()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(fieldLength).append(";\n")
            .append(indent).append("    }\n\n");
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

        new Formatter(sb).format("\n" +
            indent + "    public %s %s(final int index, final %s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        final int pos = offset + %d + (index * %d);\n" +
            indent + "        %s;\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            className,
            propertyName,
            javaTypeName,
            arrayLength,
            offset,
            typeSize,
            generatePut(primitiveType, "pos", "value", byteOrderStr));

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
                final String indexStr = "offset + " + (offset + (typeSize * i));

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
        else if (primitiveType == PrimitiveType.UINT8)
        {
            generateByteArrayEncodeMethods(
                containingClassName, propertyName, indent, offset, arrayLength, sb);
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

        new Formatter(sb).format("\n" +
            indent + "    public %s put%s(final byte[] src, final int srcOffset)\n" +
            indent + "    {\n" +
            indent + "        final int length = %d;\n" +
            indent + "        if (srcOffset < 0 || srcOffset > (src.length - length))\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfBoundsException(" +
            "\"Copy will go out of range: offset=\" + srcOffset);\n" +
            indent + "        }\n\n" +
            indent + "        buffer.putBytes(offset + %d, src, srcOffset, length);\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            Generators.toUpperFirstChar(propertyName),
            fieldLength,
            offset);

        if (encoding.characterEncoding().contains("ASCII"))
        {
            new Formatter(sb).format("\n" +
                indent + "    public %1$s %2$s(final String src)\n" +
                indent + "    {\n" +
                indent + "        final int length = %3$d;\n" +
                indent + "        final int srcLength = null == src ? 0 : src.length();\n" +
                indent + "        if (srcLength > length)\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"String too large for copy: byte length=\" + srcLength);\n" +
                indent + "        }\n\n" +
                indent + "        buffer.putStringWithoutLengthAscii(offset + %4$d, src);\n\n" +
                indent + "        for (int start = srcLength; start < length; ++start)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(offset + %4$d + start, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                offset);

            new Formatter(sb).format("\n" +
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
                indent + "            buffer.putByte(offset + %4$d + i, byteValue);\n" +
                indent + "        }\n\n" +
                indent + "        for (int i = srcLength; i < length; ++i)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(offset + %4$d + i, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                offset);
        }
        else
        {
            new Formatter(sb).format("\n" +
                indent + "    public %s %s(final String src)\n" +
                indent + "    {\n" +
                indent + "        final int length = %d;\n" +
                indent + "        final byte[] bytes = null == src ? new byte[0] : src.getBytes(%s);\n" +
                indent + "        if (bytes.length > length)\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfBoundsException(" +
                "\"String too large for copy: byte length=\" + bytes.length);\n" +
                indent + "        }\n\n" +
                indent + "        buffer.putBytes(offset + %d, bytes, 0, bytes.length);\n\n" +
                indent + "        for (int start = bytes.length; start < length; ++start)\n" +
                indent + "        {\n" +
                indent + "            buffer.putByte(offset + %d + start, (byte)0);\n" +
                indent + "        }\n\n" +
                indent + "        return this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                fieldLength,
                charset(encoding.characterEncoding()),
                offset,
                offset);
        }
    }

    private void generateByteArrayEncodeMethods(
        final String containingClassName,
        final String propertyName,
        final String indent,
        final int offset,
        final int fieldLength,
        final StringBuilder sb)
    {
        new Formatter(sb).format("\n" +
            indent + "    public %s put%s(final byte[] src, final int srcOffset, final int length)\n" +
            indent + "    {\n" +
            indent + "        if (length > %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IllegalStateException(" +
            "\"length > maxValue for type: \" + length);\n" +
            indent + "        }\n\n" +
            indent + "        buffer.putBytes(offset + %d, src, srcOffset, length);\n" +
            indent + "        for (int i = length; i < %d; i++)\n" +
            indent + "        {\n" +
            indent + "            buffer.putByte(offset + %d + i, (byte)0);\n" +
            indent + "        }\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            Generators.toUpperFirstChar(propertyName),
            fieldLength,
            offset,
            fieldLength,
            offset);

        new Formatter(sb).format("\n" +
            indent + "    public %s put%s(final %s src, final int srcOffset, final int length)\n" +
            indent + "    {\n" +
            indent + "        if (length > %d)\n" +
            indent + "        {\n" +
            indent + "            throw new IllegalStateException(" +
            "\"length > maxValue for type: \" + length);\n" +
            indent + "        }\n\n" +
            indent + "        buffer.putBytes(offset + %d, src, srcOffset, length);\n" +
            indent + "        for (int i = length; i < %d; i++)\n" +
            indent + "        {\n" +
            indent + "            buffer.putByte(offset + %d + i, (byte)0);\n" +
            indent + "        }\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            Generators.toUpperFirstChar(propertyName),
            fqReadOnlyBuffer,
            fieldLength,
            offset,
            fieldLength,
            offset);
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
            final String propName = formatPropertyName(propertyName);
            sb.append("\n")
                .append(indent).append("    public static String ").append(propName).append("CharacterEncoding()\n")
                .append(indent).append("    {\n")
                .append(indent).append("        return \"").append(characterEncoding).append("\";\n")
                .append(indent).append("    }\n");
        }
    }

    private void generateConstPropertyMethods(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final String formattedPropertyName = formatPropertyName(propertyName);
        final Encoding encoding = token.encoding();
        if (encoding.primitiveType() != PrimitiveType.CHAR)
        {
            new Formatter(sb).format("\n" +
                indent + "    public %s %s()\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
                javaTypeName(encoding.primitiveType()),
                formattedPropertyName,
                generateLiteral(encoding.primitiveType(), encoding.constValue().toString()));

            return;
        }

        final String javaTypeName = javaTypeName(encoding.primitiveType());
        final byte[] constBytes = encoding.constValue().byteArrayValue(encoding.primitiveType());
        final CharSequence values = generateByteLiteralList(
            encoding.constValue().byteArrayValue(encoding.primitiveType()));

        new Formatter(sb).format("\n" +
            "\n" +
            indent + "    private static final byte[] %s_VALUE = { %s };\n",
            propertyName.toUpperCase(),
            values);

        generateArrayLengthMethod(formattedPropertyName, indent, constBytes.length, sb);

        new Formatter(sb).format("\n" +
            indent + "    public %s %s(final int index)\n" +
            indent + "    {\n" +
            indent + "        return %s_VALUE[index];\n" +
            indent + "    }\n\n",
            javaTypeName,
            formattedPropertyName,
            propertyName.toUpperCase());

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
            new Formatter(sb).format("\n" +
                indent + "    public String %s()\n" +
                indent + "    {\n" +
                indent + "        return \"%s\";\n" +
                indent + "    }\n\n",
                formattedPropertyName,
                encoding.constValue());
        }
        else
        {
            new Formatter(sb).format("\n" +
                indent + "    public byte %s()\n" +
                indent + "    {\n" +
                indent + "        return (byte)%s;\n" +
                indent + "    }\n\n",
                formattedPropertyName,
                encoding.constValue());
        }
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
        final String wrapMethod =
            "    public " + className + " wrap(\n" +
            "        final " + readOnlyBuffer + " buffer,\n" +
            "        final int offset,\n" +
            "        final int actingBlockLength,\n" +
            "        final int actingVersion)\n" +
            "    {\n" +
            "        if (buffer != this.buffer)\n" +
            "        {\n" +
            "            this.buffer = buffer;\n" +
            "        }\n" +
            "        this.initialOffset = offset;\n" +
            "        this.offset = offset;\n" +
            "        this.actingBlockLength = actingBlockLength;\n" +
            "        this.actingVersion = actingVersion;\n" +
            "        limit(offset + actingBlockLength);\n\n" +
            "        return this;\n" +
            "    }\n\n";

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
            "    int actingBlockLength;\n" +
            "    int actingVersion;\n";

        return String.format(
            "    public static final %1$s BLOCK_LENGTH = %2$s;\n" +
            "    public static final %3$s TEMPLATE_ID = %4$s;\n" +
            "    public static final %5$s SCHEMA_ID = %6$s;\n" +
            "    public static final %7$s SCHEMA_VERSION = %8$s;\n" +
            "    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.%14$s;\n\n" +
            "    private final %9$s parentMessage = this;\n" +
            "    private %11$s buffer;\n" +
            "    private int initialOffset;\n" +
            "    private int offset;\n" +
            "    private int limit;\n" +
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
            "    public int initialOffset()\n" +
            "    {\n" +
            "        return initialOffset;\n" +
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
        final String wrapMethod =
            "    public " + className + " wrap(final " + mutableBuffer + " buffer, final int offset)\n" +
            "    {\n" +
            "        if (buffer != this.buffer)\n" +
            "        {\n" +
            "            this.buffer = buffer;\n" +
            "        }\n" +
            "        this.initialOffset = offset;\n" +
            "        this.offset = offset;\n" +
            "        limit(offset + BLOCK_LENGTH);\n\n" +
            "        return this;\n" +
            "    }\n\n";

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

    private void generateEncoderFields(
        final StringBuilder sb, final String containingClassName, final List<Token> tokens, final String indent)
    {
        Generators.forEachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = encoderName(typeToken.name());

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
    }

    private void generateDecoderFields(final StringBuilder sb, final List<Token> tokens, final String indent)
    {
        Generators.forEachField(
            tokens,
            (fieldToken, typeToken) ->
            {
                final String propertyName = formatPropertyName(fieldToken.name());
                final String typeName = decoderName(typeToken.name());

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
    }

    private static void generateFieldIdMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final String propertyName = formatPropertyName(token.name());
        sb.append("\n")
            .append(indent).append("    public static int ").append(propertyName).append("Id()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(token.id()).append(";\n")
            .append(indent).append("    }\n");
    }

    private static void generateEncodingOffsetMethod(
        final StringBuilder sb, final String name, final int offset, final String indent)
    {
        final String propertyName = formatPropertyName(name);
        sb.append("\n")
            .append(indent).append("    public static int ").append(propertyName).append("EncodingOffset()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(offset).append(";\n")
            .append(indent).append("    }\n");
    }

    private static void generateEncodingLengthMethod(
        final StringBuilder sb, final String name, final int length, final String indent)
    {
        final String propertyName = formatPropertyName(name);
        sb.append("\n")
            .append(indent).append("    public static int ").append(propertyName).append("EncodingLength()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(length).append(";\n")
            .append(indent).append("    }\n");
    }

    private static void generateFieldSinceVersionMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final String propertyName = formatPropertyName(token.name());
        sb.append("\n")
            .append(indent).append("    public static int ").append(propertyName).append("SinceVersion()\n")
            .append(indent).append("    {\n")
            .append(indent).append("        return ").append(token.version()).append(";\n")
            .append(indent).append("    }\n");
    }

    private static void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();
        final String presence = encoding.presence().toString().toLowerCase();
        final String propertyName = formatPropertyName(token.name());

        sb.append("\n")
            .append(indent).append("    public static String ")
            .append(propertyName).append("MetaAttribute(final MetaAttribute metaAttribute)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        if (MetaAttribute.PRESENCE == metaAttribute)\n")
            .append(indent).append("        {\n")
            .append(indent).append("            return \"").append(presence).append("\";\n")
            .append(indent).append("        }\n");

        if (!Strings.isEmpty(epoch))
        {
            sb.append(indent).append("        if (MetaAttribute.EPOCH == metaAttribute)\n")
                .append(indent).append("        {\n")
                .append(indent).append("            return \"").append(epoch).append("\";\n")
                .append(indent).append("        }\n");
        }

        if (!Strings.isEmpty(timeUnit))
        {
            sb.append(indent).append("        if (MetaAttribute.TIME_UNIT == metaAttribute)\n")
                .append(indent).append("        {\n")
                .append(indent).append("            return \"").append(timeUnit).append("\";\n")
                .append(indent).append("        }\n");
        }

        if (!Strings.isEmpty(semanticType))
        {
            sb.append(indent).append("        if (MetaAttribute.SEMANTIC_TYPE == metaAttribute)\n")
                .append(indent).append("        {\n")
                .append(indent).append("            return \"").append(semanticType).append("\";\n")
                .append(indent).append("        }\n");
        }

        sb.append("\n")
            .append(indent).append("        return \"\";\n")
            .append(indent).append("    }\n");
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

        new Formatter(sb).format("\n" +
            indent + "    private final %s %s = new %s();\n",
            bitSetName,
            propertyName,
            bitSetName);

        generateFlyweightPropertyJavadoc(sb, indent + INDENT, propertyToken, bitSetName);
        new Formatter(sb).format("\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            bitSetName,
            propertyName,
            generatePropertyNotPresentCondition(inComposite, codecType, propertyToken, null, indent),
            propertyName,
            bitsetToken.offset(),
            propertyName);

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

        new Formatter(sb).format("\n" +
            indent + "    private final %s %s = new %s();\n",
            compositeName,
            propertyName,
            compositeName);

        generateFlyweightPropertyJavadoc(sb, indent + INDENT, propertyToken, compositeName);
        new Formatter(sb).format("\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            "%s" +
            indent + "        %s.wrap(buffer, offset + %d);\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            generatePropertyNotPresentCondition(inComposite, codecType, propertyToken, null, indent),
            propertyName,
            compositeToken.offset(),
            propertyName);

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
            case UINT16:
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

    private void generateEncoderDisplay(final StringBuilder sb, final String decoderName)
    {
        appendToString(sb, INDENT);

        sb.append('\n');
        append(sb, INDENT, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, INDENT, "{");
        append(sb, INDENT, "    if (null == buffer)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        return builder;");
        append(sb, INDENT, "    }");
        sb.append('\n');
        append(sb, INDENT, "    final " + decoderName + " decoder = new " + decoderName + "();");
        append(sb, INDENT, "    decoder.wrap(buffer, initialOffset, BLOCK_LENGTH, SCHEMA_VERSION);");
        sb.append('\n');
        append(sb, INDENT, "    return decoder.appendTo(builder);");
        append(sb, INDENT, "}");
    }

    private CharSequence generateCompositeEncoderDisplay(final String decoderName)
    {
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, INDENT);
        sb.append('\n');
        append(sb, INDENT, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, INDENT, "{");
        append(sb, INDENT, "    if (null == buffer)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        return builder;");
        append(sb, INDENT, "    }");
        sb.append('\n');
        append(sb, INDENT, "    final " + decoderName + " decoder = new " + decoderName + "();");
        append(sb, INDENT, "    decoder.wrap(buffer, offset);");
        sb.append('\n');
        append(sb, INDENT, "    return decoder.appendTo(builder);");
        append(sb, INDENT, "}");

        return sb;
    }

    private CharSequence generateCompositeDecoderDisplay(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, INDENT);
        sb.append('\n');
        append(sb, INDENT, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, INDENT, "{");
        append(sb, INDENT, "    if (null == buffer)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        return builder;");
        append(sb, INDENT, "    }");
        sb.append('\n');
        Separator.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, INDENT + INDENT, "builder");

        int lengthBeforeLastGeneratedSeparator = -1;

        for (int i = 1, end = tokens.size() - 1; i < end;)
        {
            final Token encodingToken = tokens.get(i);
            final String propertyName = formatPropertyName(encodingToken.name());
            lengthBeforeLastGeneratedSeparator = writeTokenDisplay(propertyName, encodingToken, sb, INDENT + INDENT);
            i += encodingToken.componentTokenCount();
        }

        if (-1 != lengthBeforeLastGeneratedSeparator)
        {
            sb.setLength(lengthBeforeLastGeneratedSeparator);
        }

        Separator.END_COMPOSITE.appendToGeneratedBuilder(sb, INDENT + INDENT, "builder");
        sb.append('\n');
        append(sb, INDENT, "    return builder;");
        append(sb, INDENT, "}");

        return sb;
    }

    private CharSequence generateChoiceDisplay(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, INDENT);
        sb.append('\n');
        append(sb, INDENT, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, INDENT, "{");
        Separator.BEGIN_SET.appendToGeneratedBuilder(sb, INDENT + INDENT, "builder");
        append(sb, INDENT, "    boolean atLeastOne = false;");

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = formatPropertyName(token.name());
                append(sb, INDENT, "    if (" + choiceName + "())");
                append(sb, INDENT, "    {");
                append(sb, INDENT, "        if (atLeastOne)");
                append(sb, INDENT, "        {");
                Separator.ENTRY.appendToGeneratedBuilder(sb, INDENT + INDENT + INDENT + INDENT, "builder");
                append(sb, INDENT, "        }");
                append(sb, INDENT, "        builder.append(\"" + choiceName + "\");");
                append(sb, INDENT, "        atLeastOne = true;");
                append(sb, INDENT, "    }");
            }
        }

        Separator.END_SET.appendToGeneratedBuilder(sb, INDENT + INDENT, "builder");
        sb.append('\n');
        append(sb, INDENT, "    return builder;");
        append(sb, INDENT, "}");

        return sb;
    }

    private void generateDecoderDisplay(
        final StringBuilder sb,
        final String name,
        final List<Token> tokens,
        final List<Token> groups,
        final List<Token> varData)
    {
        appendMessageToString(sb, decoderName(name));
        sb.append('\n');
        append(sb, INDENT, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, INDENT, "{");
        append(sb, INDENT, "    if (null == buffer)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        return builder;");
        append(sb, INDENT, "    }");
        sb.append('\n');
        append(sb, INDENT, "    final int originalLimit = limit();");
        append(sb, INDENT, "    limit(initialOffset + actingBlockLength);");
        append(sb, INDENT, "    builder.append(\"[" + name + "](sbeTemplateId=\");");
        append(sb, INDENT, "    builder.append(TEMPLATE_ID);");
        append(sb, INDENT, "    builder.append(\"|sbeSchemaId=\");");
        append(sb, INDENT, "    builder.append(SCHEMA_ID);");
        append(sb, INDENT, "    builder.append(\"|sbeSchemaVersion=\");");
        append(sb, INDENT, "    if (parentMessage.actingVersion != SCHEMA_VERSION)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        builder.append(parentMessage.actingVersion);");
        append(sb, INDENT, "        builder.append('/');");
        append(sb, INDENT, "    }");
        append(sb, INDENT, "    builder.append(SCHEMA_VERSION);");
        append(sb, INDENT, "    builder.append(\"|sbeBlockLength=\");");
        append(sb, INDENT, "    if (actingBlockLength != BLOCK_LENGTH)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        builder.append(actingBlockLength);");
        append(sb, INDENT, "        builder.append('/');");
        append(sb, INDENT, "    }");
        append(sb, INDENT, "    builder.append(BLOCK_LENGTH);");
        append(sb, INDENT, "    builder.append(\"):\");");
        appendDecoderDisplay(sb, tokens, groups, varData, INDENT + INDENT);
        sb.append('\n');
        append(sb, INDENT, "    limit(originalLimit);");
        sb.append('\n');
        append(sb, INDENT, "    return builder;");
        append(sb, INDENT, "}");
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
        append(sb, indent, "public StringBuilder appendTo(final StringBuilder builder)");
        append(sb, indent, "{");
        append(sb, indent, "    if (null == buffer)");
        append(sb, indent, "    {");
        append(sb, indent, "        return builder;");
        append(sb, indent, "    }");
        sb.append('\n');
        Separator.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        appendDecoderDisplay(sb, fields, groups, varData, indent + INDENT);
        Separator.END_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        sb.append('\n');
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

            final String groupName = formatPropertyName(groupToken.name());
            final String groupDecoderName = decoderName(groupToken.name());

            append(
                sb, indent, "builder.append(\"" + groupName + Separator.KEY_VALUE + Separator.BEGIN_GROUP + "\");");
            append(sb, indent, groupDecoderName + " " + groupName + " = " + groupName + "();");
            append(sb, indent, "if (" + groupName + ".count() > 0)");
            append(sb, indent, "{");
            append(sb, indent, "    while (" + groupName + ".hasNext())");
            append(sb, indent, "    {");
            append(sb, indent, "        " + groupName + ".next().appendTo(builder);");
            Separator.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
            append(sb, indent, "    }");
            append(sb, indent, "    builder.setLength(builder.length() - 1);");
            append(sb, indent, "}");
            Separator.END_GROUP.appendToGeneratedBuilder(sb, indent, "builder");

            lengthBeforeLastGeneratedSeparator = sb.length();
            Separator.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

            i = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
        }

        for (int i = 0, size = varData.size(); i < size;)
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            final String characterEncoding = varData.get(i + 3).encoding().characterEncoding();
            final String varDataName = formatPropertyName(varDataToken.name());
            append(sb, indent, "builder.append(\"" + varDataName + Separator.KEY_VALUE + "\");");
            if (null == characterEncoding)
            {
                final String name = Generators.toUpperFirstChar(varDataToken.name());
                append(sb, indent, "builder.append(skip" + name + "()).append(\" bytes of raw data\");");
            }
            else
            {
                if (characterEncoding.contains("ASCII") || characterEncoding.contains("ascii"))
                {
                    append(sb, indent, "builder.append('\\'');");
                    append(sb, indent, formatGetterName(varDataToken.name()) + "(builder);");
                    append(sb, indent, "builder.append('\\'');");
                }
                else
                {
                    append(sb, indent, "builder.append('\\'').append(" + varDataName + "()).append('\\'');");
                }
            }

            lengthBeforeLastGeneratedSeparator = sb.length();
            Separator.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

            i += varDataToken.componentTokenCount();
        }

        if (-1 != lengthBeforeLastGeneratedSeparator)
        {
            sb.setLength(lengthBeforeLastGeneratedSeparator);
        }
    }

    private int writeTokenDisplay(
        final String fieldName, final Token typeToken, final StringBuilder sb, final String indent)
    {
        if (typeToken.encodedLength() <= 0 || typeToken.isConstantEncoding())
        {
            return -1;
        }

        append(sb, indent, "builder.append(\"" + fieldName + Separator.KEY_VALUE + "\");");

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
                        Separator.BEGIN_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                        append(sb, indent, "if (" + fieldName + "Length() > 0)");
                        append(sb, indent, "{");
                        append(sb, indent, "    for (int i = 0; i < " + fieldName + "Length(); i++)");
                        append(sb, indent, "    {");
                        append(sb, indent, "        builder.append(" + fieldName + "(i));");
                        Separator.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
                        append(sb, indent, "    }");
                        append(sb, indent, "    builder.setLength(builder.length() - 1);");
                        append(sb, indent, "}");
                        Separator.END_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                    }
                }
                else
                {
                    // have to duplicate because of checkstyle :/
                    append(sb, indent, "builder.append(" + fieldName + "());");
                }
                break;

            case BEGIN_ENUM:
                append(sb, indent, "builder.append(" + fieldName + "());");
                break;

            case BEGIN_SET:
                append(sb, indent, fieldName + "().appendTo(builder);");
                break;

            case BEGIN_COMPOSITE:
            {
                final String typeName = formatClassName(decoderName(typeToken.applicableTypeName()));
                append(sb, indent, "final " + typeName + " " + fieldName + " = " + fieldName + "();");
                append(sb, indent, "if (" + fieldName + " != null)");
                append(sb, indent, "{");
                append(sb, indent, "    " + fieldName + ".appendTo(builder);");
                append(sb, indent, "}");
                append(sb, indent, "else");
                append(sb, indent, "{");
                append(sb, indent, "    builder.append(\"null\");");
                append(sb, indent, "}");
                break;
            }
        }

        final int lengthBeforeFieldSeparator = sb.length();
        Separator.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

        return lengthBeforeFieldSeparator;
    }

    private void appendToString(final StringBuilder sb, final String indent)
    {
        sb.append('\n');
        append(sb, indent, "public String toString()");
        append(sb, indent, "{");
        append(sb, indent, "    if (null == buffer)");
        append(sb, indent, "    {");
        append(sb, indent, "        return \"\";");
        append(sb, indent, "    }");
        sb.append('\n');
        append(sb, indent, "    return appendTo(new StringBuilder()).toString();");
        append(sb, indent, "}");
    }

    private void appendMessageToString(final StringBuilder sb, final String decoderName)
    {
        sb.append('\n');
        append(sb, INDENT, "public String toString()");
        append(sb, INDENT, "{");
        append(sb, INDENT, "    if (null == buffer)");
        append(sb, INDENT, "    {");
        append(sb, INDENT, "        return \"\";");
        append(sb, INDENT, "    }");
        sb.append('\n');
        append(sb, INDENT, "    final " + decoderName + " decoder = new " + decoderName + "();");
        append(sb, INDENT, "    decoder.wrap(buffer, initialOffset, actingBlockLength, actingVersion);");
        sb.append('\n');
        append(sb, INDENT, "    return decoder.appendTo(new StringBuilder()).toString();");
        append(sb, INDENT, "}");
    }
}
