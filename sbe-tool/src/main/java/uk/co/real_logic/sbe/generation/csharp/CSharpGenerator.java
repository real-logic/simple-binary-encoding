/*
 * Copyright 2013-2020 Real Logic Limited.
 * Copyright (C) 2017 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.*;
import org.agrona.Verify;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.Generators.toLowerFirstChar;
import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;
import static uk.co.real_logic.sbe.generation.csharp.CSharpUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;

/**
 * Codec generator for the CSharp programming language.
 */
@SuppressWarnings("MethodLength")
public class CSharpGenerator implements CodeGenerator
{
    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String INDENT = "    ";
    private static final String BASE_INDENT = INDENT;

    private final Ir ir;
    private final OutputManager outputManager;

    public CSharpGenerator(final Ir ir, final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
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
        generateMessageHeaderStub();
        generateTypeStubs();

        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());

            try (Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.applicableNamespace()));
                out.append(generateDocumentation(BASE_INDENT, msgToken));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(className, msgToken, BASE_INDENT));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> fields = new ArrayList<>();
                offset = collectFields(messageBody, offset, fields);
                out.append(generateFields(fields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, className, groups, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = new ArrayList<>();
                collectVarData(messageBody, offset, varData);
                out.append(generateVarData(varData, BASE_INDENT + INDENT));

                out.append(INDENT + "}\n");
                out.append("}\n");
            }
        }
    }

    private void generateGroups(
        final StringBuilder sb,
        final String parentMessageClassName,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }
            final String groupName = groupToken.name();
            sb.append(generateGroupProperty(groupName, groupToken, indent + INDENT));

            generateGroupClassHeader(sb, groupName, parentMessageClassName, tokens, i, indent + INDENT);
            i++;
            i += tokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateFields(fields, indent + INDENT));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(sb, parentMessageClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            sb.append(generateVarData(varData, indent + INDENT + INDENT));

            sb.append(indent).append(INDENT + "}\n");
        }
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
        final int dimensionHeaderLength = tokens.get(index + 1).encodedLength();

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public sealed partial class %2$sGroup\n" +
            indent + "{\n" +
            indent + INDENT + "private readonly %3$s _dimensions = new %3$s();\n" +
            indent + INDENT + "private %4$s _parentMessage;\n" +
            indent + INDENT + "private DirectBuffer _buffer;\n" +
            indent + INDENT + "private int _blockLength;\n" +
            indent + INDENT + "private int _actingVersion;\n" +
            indent + INDENT + "private int _count;\n" +
            indent + INDENT + "private int _index;\n" +
            indent + INDENT + "private int _offset;\n",
            generateDocumentation(indent, tokens.get(index)),
            formatClassName(groupName),
            dimensionsClassName,
            parentMessageClassName));

        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        final boolean isIntCastSafe = isRepresentableByInt32(numInGroupToken.encoding());

        if (!isIntCastSafe)
        {
            throw new IllegalArgumentException(String.format(
                "%s.numInGroup - cannot be represented safely by an int. Please constrain the maxValue.",
                groupName));
        }

        sb.append(String.format("\n" +
            indent + INDENT + "public void WrapForDecode(%s parentMessage, DirectBuffer buffer, int actingVersion)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_parentMessage = parentMessage;\n" +
            indent + INDENT + INDENT + "_buffer = buffer;\n" +
            indent + INDENT + INDENT + "_dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);\n" +
            indent + INDENT + INDENT + "_blockLength = _dimensions.BlockLength;\n" +
            indent + INDENT + INDENT + "_count = (int) _dimensions.NumInGroup;\n" + // cast safety checked above
            indent + INDENT + INDENT + "_actingVersion = actingVersion;\n" +
            indent + INDENT + INDENT + "_index = 0;\n" +
            indent + INDENT + INDENT + "_parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;\n" +
            indent + INDENT + "}\n",
            parentMessageClassName));

        final int blockLength = tokens.get(index).encodedLength();
        final String typeForBlockLength = cSharpTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String typeForNumInGroup = cSharpTypeName(numInGroupToken.encoding().primitiveType());

        final String throwCondition = numInGroupToken.encoding().applicableMinValue().longValue() == 0 ?
            "if ((uint) count > %3$d)\n" :
            "if (count < %2$d || count > %3$d)\n";

        sb.append(String.format("\n" +
            indent + INDENT + "public void WrapForEncode(%1$s parentMessage, DirectBuffer buffer, int count)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + throwCondition +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "ThrowHelper.ThrowCountOutOfRangeException(count);\n" +
            indent + INDENT + INDENT + "}\n\n" +
            indent + INDENT + INDENT + "_parentMessage = parentMessage;\n" +
            indent + INDENT + INDENT + "_buffer = buffer;\n" +
            indent + INDENT + INDENT + "_dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);\n" +
            indent + INDENT + INDENT + "_dimensions.BlockLength = SbeBlockLength;\n" +
            indent + INDENT + INDENT + "_dimensions.NumInGroup = (%5$s) count;\n" +
            indent + INDENT + INDENT + "_index = 0;\n" +
            indent + INDENT + INDENT + "_count = count;\n" +
            indent + INDENT + INDENT + "_blockLength = SbeBlockLength;\n" +
            indent + INDENT + INDENT + "_actingVersion = SchemaVersion;\n" +
            indent + INDENT + INDENT + "parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;\n" +
            indent + INDENT + "}\n",
            parentMessageClassName,
            numInGroupToken.encoding().applicableMinValue().longValue(),
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            typeForBlockLength,
            typeForNumInGroup));

        sb.append(String.format("\n" +
            indent + INDENT + "public const int SbeBlockLength = %d;\n" +
            indent + INDENT + "public const int SbeHeaderSize = %d;\n",
            blockLength,
            dimensionHeaderLength));

        generateGroupEnumerator(sb, groupName, typeForNumInGroup, indent);
    }

    private void generateGroupEnumerator(
        final StringBuilder sb,
        final String groupName,
        final String typeForNumInGroup,
        final String indent)
    {
        sb.append(
            indent + INDENT + "public int ActingBlockLength { get { return _blockLength; } }\n\n" +
            indent + INDENT + "public int Count { get { return _count; } }\n\n" +
            indent + INDENT + "public bool HasNext { get { return _index < _count; } }\n");

        sb.append(String.format("\n" +
            indent + INDENT + "public int ResetCountToIndex()\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_count = _index;\n" +
            indent + INDENT + INDENT + "_dimensions.NumInGroup = (%s) _count;\n\n" +
            indent + INDENT + INDENT + "return _count;\n" +
            indent + INDENT + "}\n",
            typeForNumInGroup));

        sb.append(String.format("\n" +
            indent + INDENT + "public %sGroup Next()\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "if (_index >= _count)\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "ThrowHelper.ThrowInvalidOperationException();\n" +
            indent + INDENT + INDENT + "}\n\n" +
            indent + INDENT + INDENT + "_offset = _parentMessage.Limit;\n" +
            indent + INDENT + INDENT + "_parentMessage.Limit = _offset + _blockLength;\n" +
            indent + INDENT + INDENT + "++_index;\n\n" +
            indent + INDENT + INDENT + "return this;\n" +
            indent + INDENT + "}\n",
            formatClassName(groupName)));

        sb.append("\n" +
            indent + INDENT + "public System.Collections.IEnumerator GetEnumerator()\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "while (this.HasNext)\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "yield return this.Next();\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + "}\n");
    }

    private boolean isRepresentableByInt32(final Encoding encoding)
    {
        // These min and max values are the same in .NET
        return encoding.applicableMinValue().longValue() >= Integer.MIN_VALUE &&
            encoding.applicableMaxValue().longValue() <= Integer.MAX_VALUE;
    }

    private CharSequence generateGroupProperty(final String groupName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = CSharpUtil.formatClassName(groupName);

        sb.append(String.format("\n" +
            indent + "private readonly %sGroup _%s = new %sGroup();\n",
            className,
            toLowerFirstChar(groupName),
            className));

        sb.append(String.format("\n" +
            indent + "public const long %sId = %d;\n",
            toUpperFirstChar(groupName),
            token.id()));

        generateSinceActingDeprecated(sb, indent, toUpperFirstChar(groupName), token);

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public %2$sGroup %3$s\n" +
            indent + "{\n" +
            indent + INDENT + "get\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_%4$s.WrapForDecode(_parentMessage, _buffer, _actingVersion);\n" +
            indent + INDENT + INDENT + "return _%4$s;\n" +
            indent + INDENT + "}\n" +
            indent + "}\n",
            generateDocumentation(indent, token),
            className,
            toUpperFirstChar(groupName),
            toLowerFirstChar(groupName)));

        sb.append(String.format("\n" +
            indent + "public %1$sGroup %2$sCount(int count)\n" +
            indent + "{\n" +
            indent + INDENT + "_%3$s.WrapForEncode(_parentMessage, _buffer, count);\n" +
            indent + INDENT + "return _%3$s;\n" +
            indent + "}\n",
            className,
            toUpperFirstChar(groupName),
            toLowerFirstChar(groupName)));

        return sb;
    }

    private CharSequence generateVarData(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                generateFieldIdMethod(sb, token, indent);
                generateSinceActingDeprecated(sb, indent, CSharpUtil.formatPropertyName(token.name()), token);
                generateOffsetMethod(sb, token, indent);

                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();
                generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
                generateFieldMetaAttributeMethod(sb, token, indent);

                final String propertyName = toUpperFirstChar(token.name());
                final Token lengthToken = Generators.findFirst("length", tokens, i);
                final int sizeOfLengthField = lengthToken.encodedLength();
                final Encoding lengthEncoding = lengthToken.encoding();
                final String lengthCSharpType = cSharpTypeName(lengthEncoding.primitiveType());
                final String lengthTypePrefix = toUpperFirstChar(lengthEncoding.primitiveType().primitiveName());
                final ByteOrder byteOrder = lengthEncoding.byteOrder();
                final String byteOrderStr = generateByteOrder(byteOrder, lengthEncoding.primitiveType().size());

                sb.append(String.format("\n" +
                    indent + "public const int %sHeaderSize = %d;\n",
                    propertyName,
                    sizeOfLengthField));

                sb.append(String.format(indent + "\n" +
                    indent + "public int %1$sLength()\n" +
                    indent + "{\n" +
                    indent + INDENT + "_buffer.CheckLimit(_parentMessage.Limit + %2$d);\n" +
                    indent + INDENT + "return (int)_buffer.%3$sGet%4$s(_parentMessage.Limit);\n" +
                    indent + "}\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    byteOrderStr));

                sb.append(String.format("\n" +
                    indent + "public int Get%1$s(byte[] dst, int dstOffset, int length) =>\n" +
                    indent + INDENT + "Get%1$s(new Span<byte>(dst, dstOffset, length));\n",
                    propertyName));

                sb.append(String.format("\n" +
                    indent + "public int Get%1$s(Span<byte> dst)\n" +
                    indent + "{\n" +
                    "%2$s" +
                    indent + INDENT + "const int sizeOfLengthField = %3$d;\n" +
                    indent + INDENT + "int limit = _parentMessage.Limit;\n" +
                    indent + INDENT + "_buffer.CheckLimit(limit + sizeOfLengthField);\n" +
                    indent + INDENT + "int dataLength = (int)_buffer.%4$sGet%5$s(limit);\n" +
                    indent + INDENT + "int bytesCopied = Math.Min(dst.Length, dataLength);\n" +
                    indent + INDENT + "_parentMessage.Limit = limit + sizeOfLengthField + dataLength;\n" +
                    indent + INDENT + "_buffer.GetBytes(limit + sizeOfLengthField, dst.Slice(0, bytesCopied));\n\n" +
                    indent + INDENT + "return bytesCopied;\n" +
                    indent + "}\n",
                    propertyName,
                    generateArrayFieldNotPresentCondition(token.version(), indent),
                    sizeOfLengthField,
                    lengthTypePrefix,
                    byteOrderStr));

                sb.append(String.format(indent + "\n" +
                    indent + "// Allocates and returns a new byte array\n" +
                    indent + "public byte[] Get%1$sBytes()\n" +
                    indent + "{\n" +
                    indent + INDENT + "const int sizeOfLengthField = %2$d;\n" +
                    indent + INDENT + "int limit = _parentMessage.Limit;\n" +
                    indent + INDENT + "_buffer.CheckLimit(limit + sizeOfLengthField);\n" +
                    indent + INDENT + "int dataLength = (int)_buffer.%3$sGet%4$s(limit);\n" +
                    indent + INDENT + "byte[] data = new byte[dataLength];\n" +
                    indent + INDENT + "_parentMessage.Limit = limit + sizeOfLengthField + dataLength;\n" +
                    indent + INDENT + "_buffer.GetBytes(limit + sizeOfLengthField, data);\n\n" +
                    indent + INDENT + "return data;\n" +
                    indent + "}\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    byteOrderStr));

                sb.append(String.format("\n" +
                    indent + "public int Set%1$s(byte[] src, int srcOffset, int length) =>\n" +
                    indent + INDENT + "Set%1$s(new ReadOnlySpan<byte>(src, srcOffset, length));\n",
                    propertyName));

                sb.append(String.format("\n" +
                    indent + "public int Set%1$s(ReadOnlySpan<byte> src)\n" +
                    indent + "{\n" +
                    indent + INDENT + "const int sizeOfLengthField = %2$d;\n" +
                    indent + INDENT + "int limit = _parentMessage.Limit;\n" +
                    indent + INDENT + "_parentMessage.Limit = limit + sizeOfLengthField + src.Length;\n" +
                    indent + INDENT + "_buffer.%3$sPut%5$s(limit, (%4$s)src.Length);\n" +
                    indent + INDENT + "_buffer.SetBytes(limit + sizeOfLengthField, src);\n\n" +
                    indent + INDENT + "return src.Length;\n" +
                    indent + "}\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    lengthCSharpType,
                    byteOrderStr));
            }
        }

        return sb;
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = CSharpUtil.formatClassName(enumToken.applicableTypeName());

        try (Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateDocumentation(INDENT, enumToken));
            final String enumPrimitiveType = cSharpTypeName(enumToken.encoding().primitiveType());
            out.append(generateEnumDeclaration(enumName, enumPrimitiveType, true));

            out.append(generateChoices(tokens.subList(1, tokens.size() - 1)));

            out.append(INDENT + "}\n");
            out.append("}\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = CSharpUtil.formatClassName(enumToken.applicableTypeName());

        try (Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateDocumentation(INDENT, enumToken));
            final String enumPrimitiveType = cSharpTypeName(enumToken.encoding().primitiveType());
            out.append(generateEnumDeclaration(enumName, enumPrimitiveType, false));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append(INDENT + "}\n");
            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = CSharpUtil.formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateDocumentation(INDENT, tokens.get(0)));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(tokens.get(0).encodedLength()));
            out.append(generateCompositePropertyElements(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append(INDENT + "}\n");
            out.append("}\n");
        }
    }

    private CharSequence generateCompositePropertyElements(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size();)
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            // FIXME: do I need to pass classname down here for disambiguation
            switch (token.signal())
            {
                case ENCODING:
                    sb.append(generatePrimitiveProperty(propertyName, token, token, indent));
                    break;

                case BEGIN_ENUM:
                    sb.append(generateEnumProperty(propertyName, token, token, indent));
                    break;

                case BEGIN_SET:
                    sb.append(generateBitSetProperty(propertyName, token, token, indent));
                    break;

                case BEGIN_COMPOSITE:
                    sb.append(generateCompositeProperty(propertyName, token, token, indent));
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }

        return sb;
    }

    private CharSequence generateChoices(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = toUpperFirstChar(token.applicableTypeName());
                final String choiceBitPosition = token.encoding().constValue().toString();
                final int choiceValue = (int)Math.pow(2, Integer.parseInt(choiceBitPosition));
                sb.append(String.format(INDENT + INDENT + "%s = %s,\n", choiceName, choiceValue));
            }
        }

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens, final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        for (final Token token : tokens)
        {
            sb.append(generateDocumentation(INDENT + INDENT, token))
              .append(INDENT).append(INDENT).append(token.name()).append(" = ")
              .append(token.encoding().constValue()).append(",\n");
        }

        final PrimitiveValue nullVal = encoding.applicableNullValue();

        sb.append(INDENT).append(INDENT).append("NULL_VALUE = ").append(nullVal).append("\n");

        return sb;
    }

    private CharSequence generateFileHeader(final String packageName)
    {
        String[] tokens = packageName.split("\\.");
        final StringBuilder sb = new StringBuilder();
        for (final String t : tokens)
        {
            sb.append(toUpperFirstChar(t)).append(".");
        }
        if (sb.length() > 0)
        {
            sb.setLength(sb.length() - 1);
        }

        tokens = sb.toString().split("-");
        sb.setLength(0);

        for (final String t : tokens)
        {
            sb.append(toUpperFirstChar(t));
        }

        return String.format(
            "// <auto-generated>\n" +
            "//     Generated SBE (Simple Binary Encoding) message codec\n" +
            "// </auto-generated>\n\n" +
            "#pragma warning disable 1591 // disable warning on missing comments\n" +
            "using System;\n" +
            "using Org.SbeTool.Sbe.Dll;\n\n" +
            "namespace %s\n" +
            "{\n",
            sb);
    }

    private CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            INDENT + "public sealed partial class %s\n" +
            INDENT + "{\n",
            className);
    }

    public static String generateDocumentation(final String indent, final Token token)
    {
        final String description = token.description();
        if (null == description || description.isEmpty())
        {
            return "";
        }

        return String.format(
            indent + "/// <summary>\n" +
            indent + "/// %s\n" +
            indent + "/// </summary>\n",
            description);
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (Writer out = outputManager.createOutput(META_ATTRIBUTE_ENUM))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));

            out.append(
                INDENT + "public enum MetaAttribute\n" +
                INDENT + "{\n" +
                INDENT + INDENT + "Epoch,\n" +
                INDENT + INDENT + "TimeUnit,\n" +
                INDENT + INDENT + "SemanticType,\n" +
                INDENT + INDENT + "Presence\n" +
                INDENT + "}\n" +
                "}\n");
        }
    }

    private CharSequence generateEnumDeclaration(
        final String name,
        final String primitiveType,
        final boolean addFlagsAttribute)
    {
        String result = "";
        if (addFlagsAttribute)
        {
            result += INDENT + "[Flags]\n";
        }

        result +=
            INDENT + "public enum " + name + " : " + primitiveType + "\n" +
            INDENT + "{\n";

        return result;
    }

    private CharSequence generatePrimitiveProperty(
        final String propertyName, final Token fieldToken, final Token typeToken, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, typeToken, indent + INDENT));

        if (typeToken.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(propertyName, typeToken, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyMethods(propertyName, fieldToken, typeToken, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyMethods(
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            return generateSingleValueProperty(propertyName, fieldToken, typeToken, indent + INDENT);
        }
        else if (arrayLength > 1)
        {
            return generateArrayProperty(propertyName, fieldToken, typeToken, indent + INDENT);
        }

        return "";
    }

    private CharSequence generatePrimitiveFieldMetaData(
        final String propertyName,
        final Token token,
        final String indent)
    {
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String typeName = cSharpTypeName(primitiveType);

        return String.format(
            "\n" +
            indent + "public const %1$s %2$sNullValue = %3$s;\n" +
            indent + "public const %1$s %2$sMinValue = %4$s;\n" +
            indent + "public const %1$s %2$sMaxValue = %5$s;\n",
            typeName,
            toUpperFirstChar(propertyName),
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString()),
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString()),
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString()));
    }

    private CharSequence generateSingleValueProperty(
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(typeToken.encoding().primitiveType().primitiveName());
        final int offset = typeToken.offset();
        final ByteOrder byteOrder = typeToken.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, typeToken.encoding().primitiveType().size());

        return String.format("\n" +
            "%1$s" +
            indent + "public %2$s %3$s\n" +
            indent + "{\n" +
            indent + INDENT + "get\n" +
            indent + INDENT + "{\n" +
            "%4$s" +
            indent + INDENT + INDENT + "return _buffer.%5$sGet%7$s(_offset + %6$d);\n" +
            indent + INDENT + "}\n" +
            indent + INDENT + "set\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_buffer.%5$sPut%7$s(_offset + %6$d, value);\n" +
            indent + INDENT + "}\n" +
            indent + "}\n\n",
            generateDocumentation(indent, fieldToken),
            typeName,
            toUpperFirstChar(propertyName),
            generateFieldNotPresentCondition(fieldToken.version(), typeToken.encoding(), indent),
            typePrefix,
            offset,
            byteOrderStr);
    }

    private CharSequence generateFieldNotPresentCondition(
        final int sinceVersion,
        final Encoding encoding,
        final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        final String literal;
        if (sinceVersion > 0)
        {
            literal = generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString());
        }
        else
        {
            literal = "(byte)0";
        }

        return String.format(
            indent + INDENT + INDENT + "if (_actingVersion < %1$d) return %2$s;\n\n",
            sinceVersion,
            literal);
    }

    private CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + INDENT + INDENT + "if (_actingVersion < %d) return 0;\n\n",
            sinceVersion);
    }

    private CharSequence generateBitSetNotPresentCondition(
        final int sinceVersion,
        final String indent,
        final String bitSetName)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + INDENT + INDENT + INDENT + "if (_actingVersion < %1$d) return (%2$s)0;\n\n",
            sinceVersion,
            bitSetName);
    }

    private CharSequence generateTypeFieldNotPresentCondition(
        final int sinceVersion,
        final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + INDENT + INDENT + "if (_actingVersion < %d) return null;\n\n",
            sinceVersion);
    }

    private CharSequence generateArrayProperty(
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(typeToken.encoding().primitiveType().primitiveName());
        final int offset = typeToken.offset();
        final ByteOrder byteOrder = typeToken.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, typeToken.encoding().primitiveType().size());
        final int fieldLength = typeToken.arrayLength();
        final int typeSize = typeToken.encoding().primitiveType().size();
        final String propName = toUpperFirstChar(propertyName);

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "public const int %sLength = %d;\n",
            propName, fieldLength));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public %2$s Get%3$s(int index)\n" +
            indent + "{\n" +
            indent + INDENT + "if ((uint) index >= %4$d)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "ThrowHelper.ThrowIndexOutOfRangeException(index);\n" +
            indent + INDENT + "}\n\n" +
            "%5$s" +
            indent + INDENT + "return _buffer.%6$sGet%9$s(_offset + %7$d + (index * %8$d));\n" +
            indent + "}\n",
            generateDocumentation(indent, fieldToken),
            typeName, propName, fieldLength,
            generateFieldNotPresentCondition(fieldToken.version(), typeToken.encoding(), indent),
            typePrefix, offset, typeSize, byteOrderStr));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public void Set%2$s(int index, %3$s value)\n" +
            indent + "{\n" +
            indent + INDENT + "if ((uint) index >= %4$d)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "ThrowHelper.ThrowIndexOutOfRangeException(index);\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "_buffer.%5$sPut%8$s(_offset + %6$d + (index * %7$d), value);\n" +
            indent + "}\n",
            generateDocumentation(indent, fieldToken),
            propName, typeName, fieldLength, typePrefix, offset, typeSize, byteOrderStr));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public ReadOnlySpan<%2$s> %3$s\n" +
            indent + "{\n" +
            indent + INDENT + "get => _buffer.AsReadOnlySpan<%2$s>(_offset + %4$s, %3$sLength);\n" +
            indent + INDENT + "set => value.CopyTo(_buffer.AsSpan<%2$s>(_offset + %4$s, %3$sLength));\n" +
            indent + "}\n",
            generateDocumentation(indent, fieldToken),
            typeName, propName, offset));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "public Span<%2$s> %3$sAsSpan()\n" +
            indent + "{\n" +
            indent + INDENT + "return _buffer.AsSpan<%2$s>(_offset + %4$s, %3$sLength);\n" +
            indent + "}\n",
            generateDocumentation(indent, fieldToken),
            typeName, propName, offset));

        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, typeToken.encoding().characterEncoding(), indent);

            sb.append(String.format("\n" +
                indent + "public int Get%1$s(byte[] dst, int dstOffset)\n" +
                indent + "{\n" +
                indent + INDENT + "const int length = %2$d;\n" +
                "%3$s" +
                indent + INDENT + "return Get%1$s(new Span<byte>(dst, dstOffset, length));\n" +
                indent + "}\n",
                propName, fieldLength, generateArrayFieldNotPresentCondition(fieldToken.version(), indent), offset));

            sb.append(String.format("\n" +
                indent + "public int Get%1$s(Span<byte> dst)\n" +
                indent + "{\n" +
                indent + INDENT + "const int length = %2$d;\n" +
                indent + INDENT + "if (dst.Length < length)\n" +
                indent + INDENT + "{\n" +
                indent + INDENT + INDENT + "ThrowHelper.ThrowWhenSpanLengthTooSmall(dst.Length);\n" +
                indent + INDENT + "}\n\n" +
                "%3$s" +
                indent + INDENT + "_buffer.GetBytes(_offset + %4$d, dst);\n" +
                indent + INDENT + "return length;\n" +
                indent + "}\n",
                propName, fieldLength, generateArrayFieldNotPresentCondition(fieldToken.version(), indent), offset));

            sb.append(String.format("\n" +
                indent + "public void Set%1$s(byte[] src, int srcOffset)\n" +
                indent + "{\n" +
                indent + INDENT + "Set%1$s(new ReadOnlySpan<byte>(src, srcOffset, src.Length - srcOffset));\n" +
                indent + "}\n",
                propName, fieldLength, offset));

            sb.append(String.format("\n" +
                indent + "public void Set%1$s(ReadOnlySpan<byte> src)\n" +
                indent + "{\n" +
                indent + INDENT + "const int length = %2$d;\n" +
                indent + INDENT + "if (src.Length > length)\n" +
                indent + INDENT + "{\n" +
                indent + INDENT + INDENT + "ThrowHelper.ThrowWhenSpanLengthTooLarge(src.Length);\n" +
                indent + INDENT + "}\n\n" +
                indent + INDENT + "_buffer.SetBytes(_offset + %3$d, src);\n" +
                indent + "}\n",
                propName, fieldLength, offset));
        }

        return sb;
    }

    private void generateCharacterEncodingMethod(
        final StringBuilder sb,
        final String propertyName,
        final String encoding,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "public const string %sCharacterEncoding = \"%s\";\n\n",
            formatPropertyName(propertyName),
            encoding));
    }

    private CharSequence generateConstPropertyMethods(
        final String propertyName,
        final Token token,
        final String indent)
    {
        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            // ODE: we generate a property here because the constant could
            // become a field in a newer version of the protocol
            return String.format("\n" +
                "%1s" +
                indent + INDENT + "public %2$s %3$s { get { return %4$s; } }\n",
                generateDocumentation(indent + INDENT, token),
                cSharpTypeName(token.encoding().primitiveType()),
                toUpperFirstChar(propertyName),
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = cSharpTypeName(token.encoding().primitiveType());
        final byte[] constantValue = token.encoding().constValue().byteArrayValue(token.encoding().primitiveType());
        final CharSequence values = generateByteLiteralList(
            token.encoding().constValue().byteArrayValue(token.encoding().primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + INDENT + "private static readonly byte[] _%1$sValue = { %2$s };\n",
            propertyName,
            values));

        sb.append(String.format(
            "\n" +
            indent + INDENT + "public const int %1$sLength = %2$d;\n",
            toUpperFirstChar(propertyName),
            constantValue.length));

        sb.append(String.format(
            indent + INDENT + "public %1$s %2$s(int index)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "return _%3$sValue[index];\n" +
            indent + INDENT + "}\n\n",
            javaTypeName,
            toUpperFirstChar(propertyName),
            propertyName));

        sb.append(String.format(
            indent + INDENT + "public int Get%1$s(byte[] dst, int offset, int length)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "int bytesCopied = Math.Min(length, %2$d);\n" +
            indent + INDENT + INDENT + "Array.Copy(_%3$sValue, 0, dst, offset, bytesCopied);\n" +
            indent + INDENT + INDENT + "return bytesCopied;\n" +
            indent + INDENT + "}\n",
            toUpperFirstChar(propertyName),
            constantValue.length,
            propertyName));

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

    private CharSequence generateFixedFlyweightCode(final int size)
    {
        return String.format(
            INDENT + INDENT + "public const %1$s SbeSchemaId = %2$s;\n" +
            INDENT + INDENT + "public const %3$s SbeSchemaVersion = %4$s;\n" +
            INDENT + INDENT + "public const int Size = %5$d;\n\n" +

            INDENT + INDENT + "private DirectBuffer _buffer;\n" +
            INDENT + INDENT + "private int _offset;\n" +
            INDENT + INDENT + "private int _actingVersion;\n\n" +

            INDENT + INDENT + "public void Wrap(DirectBuffer buffer, int offset, int actingVersion)\n" +
            INDENT + INDENT + "{\n" +
            INDENT + INDENT + INDENT + "_offset = offset;\n" +
            INDENT + INDENT + INDENT + "_actingVersion = actingVersion;\n" +
            INDENT + INDENT + INDENT + "_buffer = buffer;\n" +
            INDENT + INDENT + "}\n\n",
            cSharpTypeName(ir.headerStructure().schemaIdType()),
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            cSharpTypeName(ir.headerStructure().schemaVersionType()),
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            size);
    }

    private CharSequence generateMessageFlyweightCode(final String className, final Token token, final String indent)
    {
        final String blockLengthType = cSharpTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = cSharpTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = cSharpTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cSharpTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            indent + INDENT + "public const %1$s BlockLength = %2$s;\n" +
            indent + INDENT + "public const %3$s TemplateId = %4$s;\n" +
            indent + INDENT + "public const %5$s SchemaId = %6$s;\n" +
            indent + INDENT + "public const %7$s SchemaVersion = %8$s;\n" +
            indent + INDENT + "public const string SemanticType = \"%9$s\";\n\n" +
            indent + INDENT + "private readonly %10$s _parentMessage;\n" +
            indent + INDENT + "private DirectBuffer _buffer;\n" +
            indent + INDENT + "private int _offset;\n" +
            indent + INDENT + "private int _limit;\n" +
            indent + INDENT + "private int _actingBlockLength;\n" +
            indent + INDENT + "private int _actingVersion;\n" +
            "\n" +
            indent + INDENT + "public int Offset { get { return _offset; } }\n\n" +
            indent + INDENT + "public %10$s()\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_parentMessage = this;\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "public void WrapForEncode(DirectBuffer buffer, int offset)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_buffer = buffer;\n" +
            indent + INDENT + INDENT + "_offset = offset;\n" +
            indent + INDENT + INDENT + "_actingBlockLength = BlockLength;\n" +
            indent + INDENT + INDENT + "_actingVersion = SchemaVersion;\n" +
            indent + INDENT + INDENT + "Limit = offset + _actingBlockLength;\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "public void WrapForEncodeAndApplyHeader(DirectBuffer buffer, int offset, " +
                " MessageHeader headerEncoder)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "headerEncoder.Wrap(buffer, offset, SchemaVersion);\n" +
            indent + INDENT + INDENT + "headerEncoder.BlockLength = BlockLength;\n" +
            indent + INDENT + INDENT + "headerEncoder.TemplateId = TemplateId;\n" +
            indent + INDENT + INDENT + "headerEncoder.SchemaId = SchemaId;\n" +
            indent + INDENT + INDENT + "headerEncoder.Version = SchemaVersion;\n" +
            indent + INDENT + INDENT + "\n" +
            indent + INDENT + INDENT + "WrapForEncode(buffer, offset + MessageHeader.Size);\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "public void WrapForDecode(DirectBuffer buffer, int offset, " +
                "int actingBlockLength, int actingVersion)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "_buffer = buffer;\n" +
            indent + INDENT + INDENT + "_offset = offset;\n" +
            indent + INDENT + INDENT + "_actingBlockLength = actingBlockLength;\n" +
            indent + INDENT + INDENT + "_actingVersion = actingVersion;\n" +
            indent + INDENT + INDENT + "Limit = offset + _actingBlockLength;\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "public int Size\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "get\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "return _limit - _offset;\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "public int Limit\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "get\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "return _limit;\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + INDENT + "set\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "_buffer.CheckLimit(value);\n" +
            indent + INDENT + INDENT + INDENT + "_limit = value;\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + "}\n\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            semanticType,
            className);
    }

    private CharSequence generateFields(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = signalToken.name();

                generateFieldIdMethod(sb, signalToken, indent + INDENT);
                generateSinceActingDeprecated(
                    sb, indent, CSharpUtil.formatPropertyName(signalToken.name()), signalToken);
                generateOffsetMethod(sb, signalToken, indent + INDENT);
                generateFieldMetaAttributeMethod(sb, signalToken, indent + INDENT);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveProperty(propertyName, signalToken, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(propertyName, signalToken, encodingToken, indent));
                        break;

                    case BEGIN_SET:
                        sb.append(generateBitSetProperty(propertyName, signalToken, encodingToken, indent));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, signalToken, encodingToken, indent));
                        break;
                }
            }
        }

        return sb;
    }

    private void generateFieldIdMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format("\n" +
            indent + "public const int %sId = %d;\n",
            CSharpUtil.formatPropertyName(token.name()),
            token.id()));
    }

    private void generateOffsetMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format("\n" +
            indent + "public const int %sOffset = %d;\n",
            CSharpUtil.formatPropertyName(token.name()),
            token.offset()));
    }

    private void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();
        final String presence = encoding.presence() == null ? "" : encoding.presence().toString().toLowerCase();

        sb.append(String.format("\n" +
            indent + "public static string %sMetaAttribute(MetaAttribute metaAttribute)\n" +
            indent + "{\n" +
            indent + INDENT + "switch (metaAttribute)\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "case MetaAttribute.Epoch: return \"%s\";\n" +
            indent + INDENT + INDENT + "case MetaAttribute.TimeUnit: return \"%s\";\n" +
            indent + INDENT + INDENT + "case MetaAttribute.SemanticType: return \"%s\";\n" +
            indent + INDENT + INDENT + "case MetaAttribute.Presence: return \"%s\";\n" +
            indent + INDENT + "}\n\n" +
            indent + INDENT + "return \"\";\n" +
            indent + "}\n",
            toUpperFirstChar(token.name()),
            epoch,
            timeUnit,
            semanticType,
            presence));
    }

    private CharSequence generateEnumFieldNotPresentCondition(
        final int sinceVersion,
        final String enumName,
        final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + INDENT + INDENT + "if (_actingVersion < %d) return %s.NULL_VALUE;\n\n",
            sinceVersion,
            enumName);
    }

    private CharSequence generateEnumProperty(
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName());
        final String typePrefix = toUpperFirstChar(typeToken.encoding().primitiveType().primitiveName());
        final String enumUnderlyingType = cSharpTypeName(typeToken.encoding().primitiveType());
        final int offset = typeToken.offset();
        final ByteOrder byteOrder = typeToken.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, typeToken.encoding().primitiveType().size());

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();

            return String.format("\n" +
                "%1$s" +
                indent + INDENT + "public %2$s %3$s\n" +
                indent + INDENT + "{\n" +
                indent + INDENT + INDENT + "get\n" +
                indent + INDENT + INDENT + "{\n" +
                indent + INDENT + INDENT + INDENT + "return %4$s;\n" +
                indent + INDENT + INDENT + "}\n" +
                indent + INDENT + "}\n\n",
                generateDocumentation(indent + INDENT, fieldToken),
                enumName,
                toUpperFirstChar(propertyName),
                constValue);
        }
        else
        {
            return String.format("\n" +
                "%1$s" +
                indent + INDENT + "public %2$s %3$s\n" +
                indent + INDENT + "{\n" +
                indent + INDENT + INDENT + "get\n" +
                indent + INDENT + INDENT + "{\n" +
                "%4$s" +
                indent + INDENT + INDENT + INDENT + "return (%5$s)_buffer.%6$sGet%8$s(_offset + %7$d);\n" +
                indent + INDENT + INDENT + "}\n" +
                indent + INDENT + INDENT + "set\n" +
                indent + INDENT + INDENT + "{\n" +
                indent + INDENT + INDENT + INDENT + "_buffer.%6$sPut%8$s(_offset + %7$d, (%9$s)value);\n" +
                indent + INDENT + INDENT + "}\n" +
                indent + INDENT + "}\n\n",
                generateDocumentation(indent + INDENT, fieldToken),
                enumName,
                toUpperFirstChar(propertyName),
                generateEnumFieldNotPresentCondition(fieldToken.version(), enumName, indent),
                enumName,
                typePrefix,
                offset,
                byteOrderStr,
                enumUnderlyingType);
        }
    }

    private String generateBitSetProperty(
        final String propertyName, final Token fieldToken, final Token typeToken, final String indent)
    {
        final String bitSetName = formatClassName(typeToken.applicableTypeName());
        final int offset = typeToken.offset();
        final String typePrefix = toUpperFirstChar(typeToken.encoding().primitiveType().primitiveName());
        final ByteOrder byteOrder = typeToken.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, typeToken.encoding().primitiveType().size());
        final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());

        return String.format("\n" +
            "%1$s" +
            indent + INDENT + "public %2$s %3$s\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "get\n" +
            indent + INDENT + INDENT + "{\n" +
            "%4$s" +
            indent + INDENT + INDENT + INDENT + "return (%5$s)_buffer.%6$sGet%8$s(_offset + %7$d);\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + INDENT + "set\n" +
            indent + INDENT + INDENT + "{\n" +
            indent + INDENT + INDENT + INDENT + "_buffer.%6$sPut%8$s(_offset + %7$d, (%9$s)value);\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + "}\n",
            generateDocumentation(indent + INDENT, fieldToken),
            bitSetName,
            toUpperFirstChar(propertyName),
            generateBitSetNotPresentCondition(fieldToken.version(), indent, bitSetName),
            bitSetName,
            typePrefix,
            offset,
            byteOrderStr,
            typeName);
    }

    private Object generateCompositeProperty(
        final String propertyName, final Token fieldToken, final Token typeToken, final String indent)
    {
        final String compositeName = CSharpUtil.formatClassName(typeToken.applicableTypeName());
        final int offset = typeToken.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + INDENT + "private readonly %1$s _%2$s = new %3$s();\n",
            compositeName,
            toLowerFirstChar(propertyName),
            compositeName));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + INDENT + "public %2$s %3$s\n" +
            indent + INDENT + "{\n" +
            indent + INDENT + INDENT + "get\n" +
            indent + INDENT + INDENT + "{\n" +
            "%4$s" +
            indent + INDENT + INDENT + INDENT + "_%5$s.Wrap(_buffer, _offset + %6$d, _actingVersion);\n" +
            indent + INDENT + INDENT + INDENT + "return _%5$s;\n" +
            indent + INDENT + INDENT + "}\n" +
            indent + INDENT + "}\n",
            generateDocumentation(indent + INDENT, fieldToken),
            compositeName,
            toUpperFirstChar(propertyName),
            generateTypeFieldNotPresentCondition(fieldToken.version(), indent),
            toLowerFirstChar(propertyName),
            offset));

        return sb;
    }

    private void generateSinceActingDeprecated(
        final StringBuilder sb,
        final String indent,
        final String propertyName,
        final Token token)
    {
        sb.append(String.format(
            indent + "public const int %1$sSinceVersion = %2$d;\n" +
            indent + "public const int %1$sDeprecated = %3$d;\n" +
            indent + "public bool %1$sInActingVersion()\n" +
            indent + "{\n" +
            indent + INDENT + "return _actingVersion >= %1$sSinceVersion;\n" +
            indent + "}\n",
            propertyName,
            token.version(),
            token.deprecated()));
    }

    private String generateByteOrder(final ByteOrder byteOrder, final int primitiveTypeSize)
    {
        if (primitiveTypeSize == 1)
        {
            return "";
        }

        if ("BIG_ENDIAN".equals(byteOrder.toString()))
        {
            return "BigEndian";
        }

        return "LittleEndian";
    }

    private String generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        final String castType = cSharpTypeName(type);
        switch (type)
        {
            case CHAR:
            case UINT8:
            case INT8:
            case INT16:
            case UINT16:
                literal = "(" + castType + ")" + value;
                break;

            case INT32:
                literal = value;
                break;

            case UINT32:
                literal = value + "U";
                break;

            case FLOAT:
                if (value.endsWith("NaN"))
                {
                    literal = "float.NaN";
                }
                else
                {
                    literal = value + "f";
                }
                break;

            case UINT64:
                literal = "0x" + Long.toHexString(Long.parseLong(value)) + "UL";
                break;

            case INT64:
                literal = value + "L";
                break;

            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "double.NaN";
                }
                else
                {
                    literal = value + "d";
                }
                break;
        }

        return literal;
    }
}
