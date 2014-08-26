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
package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.util.Verify;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.csharp.CSharpUtil.*;

public class CSharpGenerator implements CodeGenerator
{
    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";

    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public CSharpGenerator(final Ir ir, final OutputManager outputManager)
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
            out.append(generateFixedFlyweightCode(tokens.get(0).size()));
            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("    }\n");
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
                out.append(generateFields(rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, className, groups, 0, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = messageBody.subList(offset, messageBody.size());
                out.append(generateVarData(varData));

                out.append("    }\n");
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
        final StringBuilder sb, final String parentMessageClassName, final List<Token> tokens, int index, final String indent)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
            {
                final Token groupToken = tokens.get(index);
                final String groupName = groupToken.name();
                sb.append(generateGroupProperty(groupName, groupToken, indent));

                generateGroupClassHeader(sb, groupName, parentMessageClassName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateFields(rootFields, indent + INDENT));

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
            indent + "public sealed partial class %1$sGroup\n" +
            indent + "{\n" +
            indent + "    private readonly %2$s _dimensions = new %2$s();\n" +
            indent + "    private %3$s _parentMessage;\n" +
            indent + "    private DirectBuffer _buffer;\n" +
            indent + "    private int _blockLength;\n" +
            indent + "    private int _actingVersion;\n" +
            indent + "    private int _count;\n" +
            indent + "    private int _index;\n" +
            indent + "    private int _offset;\n\n",
            formatClassName(groupName),
            dimensionsClassName,
            parentMessageClassName
        ));

        sb.append(String.format(
            indent + "    public void WrapForDecode(%s parentMessage, DirectBuffer buffer, int actingVersion)\n" +
            indent + "    {\n" +
            indent + "        _parentMessage = parentMessage;\n" +
            indent + "        _buffer = buffer;\n" +
            indent + "        _dimensions.Wrap(buffer, parentMessage.Limit, actingVersion);\n" +
            indent + "        _blockLength = _dimensions.BlockLength;\n" +
            indent + "        _count = _dimensions.NumInGroup;\n" +
            indent + "        _actingVersion = actingVersion;\n" +
            indent + "        _index = -1;\n" +
            indent + "        _parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;\n" +
            indent + "    }\n\n",
            parentMessageClassName
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String typeForBlockLength = cSharpTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String typeForNumInGroup = cSharpTypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    public void WrapForEncode(%1$s parentMessage, DirectBuffer buffer, int count)\n" +
            indent + "    {\n" +
            indent + "        _parentMessage = parentMessage;\n" +
            indent + "        _buffer = buffer;\n" +
            indent + "        _dimensions.Wrap(buffer, parentMessage.Limit, _actingVersion);\n" +
            indent + "        _dimensions.BlockLength = (%2$s)%3$d;\n" +
            indent + "        _dimensions.NumInGroup = (%4$s)count;\n" +
            indent + "        _index = -1;\n" +
            indent + "        _count = count;\n" +
            indent + "        _blockLength = %3$d;\n" +
            indent + "        parentMessage.Limit = parentMessage.Limit + SbeHeaderSize;\n" +
            indent + "    }\n\n",
            parentMessageClassName,
            typeForBlockLength,
            blockLength,
            typeForNumInGroup
        ));

        sb.append(String.format(
                indent + "    public const int SbeBlockLength = %d;\n" +
                indent + "    public const int SbeHeaderSize = %d;\n",
                blockLength,
                dimensionHeaderSize
        ));

        sb.append(String.format(
            indent + "    public int ActingBlockLength { get { return _blockLength; } }\n\n" +
            indent + "    public int Count { get { return _count; } }\n\n" +
            indent + "    public bool HasNext { get { return (_index + 1) < _count; } }\n\n"
        ));

        sb.append(String.format(
            indent + "    public %sGroup Next()\n" +
            indent + "    {\n" +
            indent + "        if (_index + 1 >= _count)\n" +
            indent + "        {\n" +
            indent + "            throw new InvalidOperationException();\n" +
            indent + "        }\n\n" +
            indent + "        _offset = _parentMessage.Limit;\n" +
            indent + "        _parentMessage.Limit = _offset + _blockLength;\n" +
            indent + "        ++_index;\n\n" +
            indent + "        return this;\n" +
            indent + "    }\n",
            formatClassName(groupName)
        ));
    }

    private CharSequence generateGroupProperty(final String groupName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = CSharpUtil.formatClassName(groupName);

        sb.append(String.format(
            "\n" +
            indent + "    private readonly %sGroup _%s = new %sGroup();\n",
            className,
            toLowerFirstChar(groupName),
            className
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public const long %sId = %d;\n\n",
            toUpperFirstChar(groupName),
            Integer.valueOf(token.id())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$sGroup %2$s\n" +
            indent + "    {\n" +
            indent + "        get\n" +
            indent + "        {\n" +
            indent + "            _%3$s.WrapForDecode(_parentMessage, _buffer, _actingVersion);\n" +
            indent + "            return _%3$s;\n" +
            indent + "        }\n" +
            indent + "    }\n",
            className,
            toUpperFirstChar(groupName),
            toLowerFirstChar(groupName)
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$sGroup %2$sCount(int count)\n" +
            indent + "    {\n" +
            indent + "        _%3$s.WrapForEncode(_parentMessage, _buffer, count);\n" +
            indent + "        return _%3$s;\n" +
            indent + "    }\n",
            className,
            toUpperFirstChar(groupName),
            toLowerFirstChar(groupName)
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
                generateFieldIdMethod(sb, token, BASE_INDENT);

                final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
                generateCharacterEncodingMethod(sb, token.name(), characterEncoding);
                generateFieldMetaAttributeMethod(sb, token, BASE_INDENT);

                final String propertyName = toUpperFirstChar(token.name());
                final Token lengthToken = tokens.get(i + 2);
                final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
                final Encoding lengthEncoding = lengthToken.encoding();
                final String lengthCsharpType = cSharpTypeName(lengthEncoding.primitiveType());
                final String lengthTypePrefix = toUpperFirstChar(lengthEncoding.primitiveType().primitiveName());
                final ByteOrder byteOrder = lengthEncoding.byteOrder();
                final String byteOrderStr = generateByteOrder(byteOrder, lengthEncoding.primitiveType().size());

                sb.append(String.format(
                        "\n" +
                                "    public const int %sHeaderSize = %d;\n",
                        propertyName,
                        sizeOfLengthField
                ));

                sb.append(String.format(
                    "\n" +
                    "    public int Get%1$s(byte[] dst, int dstOffset, int length)\n" +
                    "    {\n" +
                    "%2$s" +
                    "        const int sizeOfLengthField = %3$d;\n" +
                    "        int limit = Limit;\n" +
                    "        _buffer.CheckLimit(limit + sizeOfLengthField);\n" +
                    "        int dataLength = _buffer.%4$sGet%5$s(limit);\n" +
                    "        int bytesCopied = Math.Min(length, dataLength);\n" +
                    "        Limit = limit + sizeOfLengthField + dataLength;\n" +
                    "        _buffer.GetBytes(limit + sizeOfLengthField, dst, dstOffset, bytesCopied);\n\n" +
                    "        return bytesCopied;\n" +
                    "    }\n\n",
                    propertyName,
                    generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
                    sizeOfLengthField,
                    lengthTypePrefix,
                    byteOrderStr
                ));

                sb.append(String.format(
                    "    public int Set%1$s(byte[] src, int srcOffset, int length)\n" +
                    "    {\n" +
                    "        const int sizeOfLengthField = %2$d;\n" +
                    "        int limit = Limit;\n" +
                    "        Limit = limit + sizeOfLengthField + length;\n" +
                    "        _buffer.%3$sPut%5$s(limit, (%4$s)length);\n" +
                    "        _buffer.SetBytes(limit + sizeOfLengthField, src, srcOffset, length);\n\n" +
                    "        return length;\n" +
                    "    }\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    lengthCsharpType,
                    byteOrderStr
                ));
            }
        }

        return sb;
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        Token enumToken = tokens.get(0);
        final String enumName = CSharpUtil.formatClassName(enumToken.name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            String enumPrimitiveType = cSharpTypeName(enumToken.encoding().primitiveType());
            out.append(generateEnumDeclaration(enumName, enumPrimitiveType, true));

            out.append(generateChoices(tokens.subList(1, tokens.size() - 1)));

            out.append(INDENT).append("}\n");

            out.append("}\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        Token enumToken = tokens.get(0);
        final String enumName = CSharpUtil.formatClassName(enumToken.name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            String enumPrimitiveType = cSharpTypeName(enumToken.encoding().primitiveType());
            out.append(generateEnumDeclaration(enumName, enumPrimitiveType, false));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append(INDENT).append("}\n");

            out.append("}\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = CSharpUtil.formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(tokens.get(0).size()));

            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("    }\n");
            out.append("}\n");
        }
    }

    private CharSequence generateChoices(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = toUpperFirstChar(token.name());
                final String choiceBitPosition = token.encoding().constValue().toString();
                final int choiceValue = (int)Math.pow(2, Integer.parseInt(choiceBitPosition));
                sb.append(String.format("        %s = %s,\n", choiceName, Integer.valueOf(choiceValue)));
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
            sb.append(INDENT).append(INDENT).append(token.name()).append(" = ")
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

        return String.format(
            "/* Generated SBE (Simple Binary Encoding) message codec */\n\n" +
            "#pragma warning disable 1591 // disable warning on missing comments\n" +
            "using System;\n" +
            "using Adaptive.SimpleBinaryEncoding;\n\n" +
            "namespace %s\n" +
            "{\n",
            sb
        );
    }

    private CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            "    public sealed partial class %s\n" +
            "    {\n",
            className
        );
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (final Writer out = outputManager.createOutput(META_ATTRIBUTE_ENUM))
        {
            out.append(generateFileHeader(ir.applicableNamespace()));

            out.append(String.format(
                "    public enum MetaAttribute\n" +
                "    {\n" +
                "        Epoch,\n" +
                "        TimeUnit,\n" +
                "        SemanticType\n" +
                "    }\n" +
                "}\n"
            ));
        }
    }

    private CharSequence generateEnumDeclaration(final String name, final String primitiveType, final boolean addFlagsAttribute)
    {
        String result = "";
        if (addFlagsAttribute)
        {
            result += "    [Flags]\n";
        }

        result +=
            "    public enum " + name + " : " + primitiveType + "\n" +
            "    {\n";

        return result;
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
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyMethods(propertyName, token, indent));
        }

        return sb;
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

    private CharSequence generatePrimitiveFieldMetaData(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String typeName = cSharpTypeName(primitiveType);

        sb.append(String.format(
            "\n" +
                    indent + "    public const %s %sNullValue = %s;\n",
            typeName,
            toUpperFirstChar(propertyName),
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString())
        ));

        sb.append(String.format(
            "\n" +
                    indent + "    public const %s %sMinValue = %s;\n",
            typeName,
            toUpperFirstChar(propertyName),
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())
        ));

        sb.append(String.format(
            "\n" +
                    indent + "    public const %s %sMaxValue = %s;\n",
            typeName,
            toUpperFirstChar(propertyName),
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())
        ));

        return sb;
    }

    private CharSequence generateSingleValueProperty(final String propertyName, final Token token, final String indent)
    {
        final String typeName = cSharpTypeName(token.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, token.encoding().primitiveType().size());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s\n" +
            indent + "    {\n" +
            indent + "        get\n" +
            indent + "        {\n" +
            "%3$s" +
            indent + "            return _buffer.%4$sGet%6$s(_offset + %5$d);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%4$sPut%6$s(_offset + %5$d, value);\n" +
            indent + "        }\n" +
            indent + "    }\n\n",
            typeName,
            toUpperFirstChar(propertyName),
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            typePrefix,
            offset,
            byteOrderStr
        ));

        return sb;
    }

    private CharSequence generateFieldNotPresentCondition(final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (_actingVersion < %1$d) return %2$s;\n\n",
            Integer.valueOf(sinceVersion),
            sinceVersion > 0 ? generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString()) : "(byte)0"
        );
    }

    private CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion < %d) return 0;\n\n",
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
            indent + "        if (actingVersion < %d) return null;\n\n",
            Integer.valueOf(sinceVersion)
        );
    }

    private CharSequence generateArrayProperty(final String propertyName, final Token token, final String indent)
    {
        final String typeName = cSharpTypeName(token.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, token.encoding().primitiveType().size());
        final Integer fieldLength = Integer.valueOf(token.arrayLength());
        final Integer typeSize = Integer.valueOf(token.encoding().primitiveType().size());
        final String propName = toUpperFirstChar(propertyName);

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public const int %sLength  = %d;\n\n",
            propName,
            fieldLength
        ));

        sb.append(String.format(
            indent + "    public %1$s Get%2$s(int index)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %3$d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfRangeException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            "%4$s" +
            indent + "        return _buffer.%5$sGet%8$s(_offset + %6$d + (index * %7$d));\n" +
            indent + "    }\n\n",
            typeName,
            propName,
            fieldLength,
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            typePrefix,
            offset,
            typeSize,
            byteOrderStr
        ));

        sb.append(String.format(
            indent + "    public void Set%1$s(int index, %2$s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %3$d)\n" +
            indent + "        {\n" +
            indent + "            throw new IndexOutOfRangeException(\"index out of range: index=\" + index);\n" +
            indent + "        }\n\n" +
            indent + "        _buffer.%4$sPut%7$s(_offset + %5$d + (index * %6$d), value);\n" +
            indent + "    }\n",
            propName,
            typeName,
            fieldLength,
            typePrefix,
            offset,
            typeSize,
            byteOrderStr
        ));

        if (token.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, token.encoding().characterEncoding());

            sb.append(String.format(
                indent + "    public int Get%1$s(byte[] dst, int dstOffset)\n" +
                indent + "    {\n" +
                indent + "        const int length = %2$d;\n" +
                indent + "        if (dstOffset < 0 || dstOffset > (dst.Length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfRangeException(" +
                    "\"dstOffset out of range for copy: offset=\" + dstOffset);\n" +
                indent + "        }\n\n" +
                "%3$s" +
                indent + "        _buffer.GetBytes(_offset + %4$d, dst, dstOffset, length);\n" +
                indent + "        return length;\n" +
                indent + "    }\n\n",
                propName,
                fieldLength,
                generateArrayFieldNotPresentCondition(token.version(), indent),
                offset
            ));

            sb.append(String.format(
                indent + "    public void Set%1$s(byte[] src, int srcOffset)\n" +
                indent + "    {\n" +
                indent + "        const int length = %2$d;\n" +
                indent + "        if (srcOffset < 0 || srcOffset > (src.Length - length))\n" +
                indent + "        {\n" +
                indent + "            throw new IndexOutOfRangeException(" +
                    "\"srcOffset out of range for copy: offset=\" + srcOffset);\n" +
                indent + "        }\n\n" +
                indent + "        _buffer.SetBytes(_offset + %3$d, src, srcOffset, length);\n" +
                indent + "    }\n",
                propName,
                fieldLength,
                offset
            ));
        }

        return sb;
    }

    private void generateCharacterEncodingMethod(final StringBuilder sb, final String propertyName, final String encoding)
    {
        sb.append(String.format(
            "\n" +
            "    public const string %sCharacterEncoding = \"%s\";\n\n",
            formatPropertyName(propertyName),
            encoding
        ));
    }

    private CharSequence generateConstPropertyMethods(final String propertyName, final Token token, final String indent)
    {
        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            // ODE: we generate a property here because the constant could become a field in a newer version of the protocol
            return String.format(
                "\n" +
                indent + "    public %1$s %2$s { get { return %3$s; } }\n",
                cSharpTypeName(token.encoding().primitiveType()),
                toUpperFirstChar(propertyName),
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString())
            );
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = cSharpTypeName(token.encoding().primitiveType());
        final byte[] constantValue = token.encoding().constValue().byteArrayValue(token.encoding().primitiveType());
        final CharSequence values = generateByteLiteralList(
            token.encoding().constValue().byteArrayValue(token.encoding().primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + "    private static readonly byte[] _%1$sValue = {%2$s};\n",
            propertyName,
            values
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public const int %1$sLength = %2$d;\n",
            toUpperFirstChar(propertyName),
            Integer.valueOf(constantValue.length)
        ));

        sb.append(String.format(
            indent + "    public %1$s %2$s(int index)\n" +
            indent + "    {\n" +
            indent + "        return _%3$sValue[index];\n" +
            indent + "    }\n\n",
            javaTypeName,
            toUpperFirstChar(propertyName),
            propertyName
        ));

        sb.append(String.format(
            indent + "    public int Get%1$s(byte[] dst, int offset, int length)\n" +
            indent + "    {\n" +
            indent + "        int bytesCopied = Math.Min(length, %2$d);\n" +
            indent + "        Array.Copy(_%3$sValue, 0, dst, offset, bytesCopied);\n" +
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

    private CharSequence generateFixedFlyweightCode(final int size)
    {
        return String.format(
            "        private DirectBuffer _buffer;\n" +
            "        private int _offset;\n" +
            "        private int _actingVersion;\n\n" +
            "        public void Wrap(DirectBuffer buffer, int offset, int actingVersion)\n" +
            "        {\n" +
            "            _offset = offset;\n" +
            "            _actingVersion = actingVersion;\n" +
            "            _buffer = buffer;\n" +
            "        }\n\n" +
            "        public const int Size = %d;\n",
            Integer.valueOf(size)
        );
    }

    private CharSequence generateMessageFlyweightCode(final String className, final Token token)
    {
        final String blockLengthType = cSharpTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = cSharpTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = cSharpTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cSharpTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            "    public const %1$s BlockLength = %2$s;\n" +
            "    public const %3$s TemplateId = %4$s;\n" +
            "    public const %5$s SchemaId = %6$s;\n" +
            "    public const %7$s Schema_Version = %8$s;\n" +
            "    public const string SemanticType = \"%9$s\";\n\n" +
            "    private readonly %10$s _parentMessage;\n" +
            "    private DirectBuffer _buffer;\n" +
            "    private int _offset;\n" +
            "    private int _limit;\n" +
            "    private int _actingBlockLength;\n" +
            "    private int _actingVersion;\n" +
            "\n" +
            "    public int Offset { get { return _offset; } }\n\n" +
            "    public %10$s()\n" +
            "    {\n" +
            "        _parentMessage = this;\n" +
            "    }\n\n" +
            "    public void WrapForEncode(DirectBuffer buffer, int offset)\n" +
            "    {\n" +
            "        _buffer = buffer;\n" +
            "        _offset = offset;\n" +
            "        _actingBlockLength = BlockLength;\n" +
            "        _actingVersion = Schema_Version;\n" +
            "        Limit = offset + _actingBlockLength;\n" +
            "    }\n\n" +
            "    public void WrapForDecode(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion)\n" +
            "    {\n" +
            "        _buffer = buffer;\n" +
            "        _offset = offset;\n" +
            "        _actingBlockLength = actingBlockLength;\n" +
            "        _actingVersion = actingVersion;\n" +
            "        Limit = offset + _actingBlockLength;\n" +
            "    }\n\n" +
            "    public int Size\n" +
            "    {\n" +
            "        get\n" +
            "        {\n" +
            "            return _limit - _offset;\n" +
            "        }\n" +
            "    }\n\n" +
            "    public int Limit\n" +
            "    {\n" +
            "        get\n" +
            "        {\n" +
            "            return _limit;\n" +
            "        }\n" +
            "        set\n" +
            "        {\n" +
            "            _buffer.CheckLimit(value);\n" +
            "            _limit = value;\n" +
            "        }\n" +
            "    }\n\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.size())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(token.version())),
            semanticType,
            className
        );
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

                generateFieldIdMethod(sb, signalToken, indent);
                generateFieldMetaAttributeMethod(sb, signalToken, indent);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(propertyName, encodingToken, indent));
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
            indent + "    public const int %sId = %d;\n",
            CSharpUtil.formatPropertyName(token.name()),
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
            indent + "    public static string %sMetaAttribute(MetaAttribute metaAttribute)\n" +
            indent + "    {\n" +
            indent + "        switch (metaAttribute)\n" +
            indent + "        {\n" +
            indent + "            case MetaAttribute.Epoch: return \"%s\";\n" +
            indent + "            case MetaAttribute.TimeUnit: return \"%s\";\n" +
            indent + "            case MetaAttribute.SemanticType: return \"%s\";\n" +
            indent + "        }\n\n" +
            indent + "        return \"\";\n" +
            indent + "    }\n",
            toUpperFirstChar(token.name()),
            epoch,
            timeUnit,
            semanticType
        ));
    }

    private CharSequence generateEnumFieldNotPresentCondition(final int sinceVersion, final String enumName, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (_actingVersion < %d) return %s.NULL_VALUE;\n\n",
            Integer.valueOf(sinceVersion),
            enumName
        );
    }

    private CharSequence generateEnumProperty(final String propertyName, final Token token, final String indent)
    {
        final String enumName = formatClassName(token.name());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final String enumUnderlyingType = cSharpTypeName(token.encoding().primitiveType());
        final Integer offset = Integer.valueOf(token.offset());
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, token.encoding().primitiveType().size());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s\n" +
            indent + "    {\n" +
            indent + "        get\n" +
            indent + "        {\n" +
            "%3$s" +
            indent + "            return (%4$s)_buffer.%5$sGet%7$s(_offset + %6$d);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%5$sPut%7$s(_offset + %6$d, (%8$s)value);\n" +
            indent + "        }\n" +
            indent + "    }\n\n",
            enumName,
            toUpperFirstChar(propertyName),
            generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
            enumName,
            typePrefix,
            offset,
            byteOrderStr,
            enumUnderlyingType
        ));

        return sb;
    }

    private Object generateBitSetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String bitSetName = formatClassName(token.name());
        final Integer offset = Integer.valueOf(token.offset());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = generateByteOrder(byteOrder, token.encoding().primitiveType().size());
        final String typeName = cSharpTypeName(token.encoding().primitiveType());

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s\n" +
            indent + "    {\n" +
            indent + "        get\n" +
            indent + "        {\n" +
            "%3$s" +
            indent + "            return (%4$s)_buffer.%5$sGet%7$s(_offset + %6$d);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%5$sPut%7$s(_offset + %6$d, (%8$s)value);\n" +
            indent + "        }\n" +
            indent + "    }\n",
            bitSetName,
            toUpperFirstChar(propertyName),
            generateTypeFieldNotPresentCondition(token.version(), indent),
            bitSetName,
            typePrefix,
            offset,
            byteOrderStr,
            typeName
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = CSharpUtil.formatClassName(token.name());
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    private readonly %1$s _%2$s = new %3$s();\n",
            compositeName,
            toLowerFirstChar(propertyName),
            compositeName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %1$s %2$s\n" +
            indent + "    {\n" +
            indent + "        get\n" +
            indent + "        {\n" +
            "%3$s" +
            indent + "            _%4$s.Wrap(_buffer, _offset + %5$d, _actingVersion);\n" +
            indent + "            return _%4$s;\n" +
            indent + "        }\n" +
            indent + "    }\n",
            compositeName,
            toUpperFirstChar(propertyName),
            generateTypeFieldNotPresentCondition(token.version(), indent),
            toLowerFirstChar(propertyName),
            offset
        ));

        return sb;
    }

    private String generateByteOrder(final ByteOrder byteOrder, final int primitiveTypeSize)
    {
        if (primitiveTypeSize == 1)
        {
            return "";
        }

        switch (byteOrder.toString())
        {
            case "BIG_ENDIAN":
                return "BigEndian";

            default:
                return "LittleEndian";
        }
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
        }

        return literal;
    }
}
