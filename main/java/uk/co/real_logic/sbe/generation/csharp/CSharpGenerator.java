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
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
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
    /** Class name to be used for visitor pattern that accesses the message header. */
    public static final String MESSAGE_HEADER_TYPE = "MessageHeader";
    public static final String FIXED_FLYWEIGHT_TYPE = "IFixedFlyweight";
    public static final String MESSAGE_FLYWEIGHT_TYPE = "IMessageFlyweight";

    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final IntermediateRepresentation ir;
    private final OutputManager outputManager;

    public CSharpGenerator(final IntermediateRepresentation ir, final OutputManager outputManager)
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
            final List<Token> tokens = ir.messageHeader().tokens();
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateClassDeclaration(MESSAGE_HEADER_TYPE, FIXED_FLYWEIGHT_TYPE));
            out.append(generateFixedFlyweightCode(tokens.get(0).size()));
            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("    }\n");
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
                out.append(generateFileHeader(ir.packageName()));
                out.append(generateClassDeclaration(className, MESSAGE_FLYWEIGHT_TYPE));
                out.append(generateMessageFlyweightCode(className, msgToken.size(), msgToken.version(), msgToken.schemaId()));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, groups, 0, BASE_INDENT);
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

    private int generateGroups(final StringBuilder sb, final List<Token> tokens, int index, final String indent)
    {
        for (int size = tokens.size(); index < size; index++)
        {
            if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
            {
                final Token groupToken = tokens.get(index);
                final String groupName = groupToken.name();
                sb.append(generateGroupProperty(groupName, groupToken, indent));

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
            indent + "public class %1$sGroup : IGroupFlyweight<%1$sGroup>\n" +
            indent + "{\n" +
            indent + "    private readonly %2$s _dimensions = new %2$s();\n" +
            indent + "    private IMessageFlyweight _parentMessage;\n" +
            indent + "    private DirectBuffer _buffer;\n" +
            indent + "    private int _blockLength;\n" +
            indent + "    private int _actingVersion;\n" +
            indent + "    private int _count;\n" +
            indent + "    private int _index;\n" +
            indent + "    private int _offset;\n\n",
            formatClassName(groupName),
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    public void WrapForDecode(IMessageFlyweight parentMessage, DirectBuffer buffer, int actingVersion)\n" +
            indent + "    {\n" +
            indent + "        _parentMessage = parentMessage;\n" +
            indent + "        _buffer = buffer;\n" +
            indent + "        _dimensions.Wrap(buffer, parentMessage.Position, actingVersion);\n" +
            indent + "        _count = _dimensions.NumInGroup;\n" +
            indent + "        _blockLength = _dimensions.BlockLength;\n" +
            indent + "        _actingVersion = actingVersion;\n" +
            indent + "        _index = -1;\n" +
            indent + "        const int dimensionsHeaderSize = %1$d;\n" +
            indent + "        _parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;\n" +
            indent + "    }\n\n",
            dimensionHeaderSize
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String typeForBlockLength = cSharpTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String typeForNumInGroup = cSharpTypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    public void WrapForEncode(IMessageFlyweight parentMessage, DirectBuffer buffer, int count)\n" +
            indent + "    {\n" +
            indent + "        _parentMessage = parentMessage;\n" +
            indent + "        _buffer = buffer;\n" +
            indent + "        _dimensions.Wrap(buffer, parentMessage.Position, _actingVersion);\n" +
            indent + "        _dimensions.NumInGroup = (%1$s)count;\n" +
            indent + "        _dimensions.BlockLength = (%2$s)%3$d;\n" +
            indent + "        _index = -1;\n" +
            indent + "        _count = count;\n" +
            indent + "        _blockLength = %3$d;\n" +
            indent + "        const int dimensionsHeaderSize = %4$d;\n" +
            indent + "        parentMessage.Position = parentMessage.Position + dimensionsHeaderSize;\n" +
            indent + "    }\n\n",
            typeForNumInGroup,
            typeForBlockLength,
            blockLength,
            dimensionHeaderSize
        ));

        sb.append(
            indent + "    public int Count { get { return _count; } }\n\n" +
            indent + "    public bool HasNext { get { return _index + 1 < _count; } }\n\n"
        );

        sb.append(String.format(
            indent + "    public %sGroup Next()\n" +
            indent + "    {\n" +
            indent + "        if (_index + 1 >= _count)\n" +
            indent + "        {\n" +
            indent + "            throw new InvalidOperationException();\n" +
            indent + "        }\n\n" +
            indent + "        _offset = _parentMessage.Position;\n" +
            indent + "        _parentMessage.Position = _offset + _blockLength;\n" +
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
            indent + "    public const long %sSchemaId = %d;\n\n",
            toUpperFirstChar(groupName),
            Integer.valueOf(token.schemaId())
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

                final String propertyName = toUpperFirstChar(token.name());
                final Token lengthToken = tokens.get(i + 2);
                final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
                final Encoding lengthEncoding = lengthToken.encoding();
                final String lengthJavaType = cSharpTypeName(lengthEncoding.primitiveType());
                final String lengthTypePrefix = toUpperFirstChar(lengthEncoding.primitiveType().primitiveName());
                final ByteOrder byteOrder = lengthEncoding.byteOrder();
                final String byteOrderStr = generateByteOrder(byteOrder, lengthEncoding.primitiveType().size());

                sb.append(String.format(
                    "    public int Get%1$s(byte[] dst, int dstOffset, int length)\n" +
                    "    {\n" +
                    "%2$s" +
                    "        const int sizeOfLengthField = %3$d;\n" +
                    "        int lengthPosition = Position;\n" +
                    "        Position = lengthPosition + sizeOfLengthField;\n" +
                    "        int dataLength = _buffer.%4$sGet(lengthPosition%5$s);\n" +
                    "        int bytesCopied = Math.Min(length, dataLength);\n" +
                    "        _buffer.GetBytes(Position, dst, dstOffset, bytesCopied);\n" +
                    "        Position = Position + dataLength;\n\n" +
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
                    "        int lengthPosition = Position;\n" +
                    "        _buffer.%3$sPut(lengthPosition, (%4$s)length%5$s);\n" +
                    "        Position = lengthPosition + sizeOfLengthField;\n" +
                    "        _buffer.SetBytes(Position, src, srcOffset, length);\n" +
                    "        Position = Position + length;\n\n" +
                    "        return length;\n" +
                    "    }\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
                    lengthJavaType,
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
            out.append(generateFileHeader(ir.packageName()));
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
            out.append(generateFileHeader(ir.packageName()));
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
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateClassDeclaration(compositeName, FIXED_FLYWEIGHT_TYPE));
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
                final String choiceBitPosition = token.encoding().constVal().toString();
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
            sb.append(INDENT).append(INDENT).append(token.name()).append(" = ").append(token.encoding().constVal()).append(",\n");
        }

        final PrimitiveValue nullVal = encoding.applicableNullVal();

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
            "using System;\n" +
            "using Adaptive.SimpleBinaryEncoding;\n\n" +
            "namespace %s\n" +
            "{\n",
            sb
        );
    }

    private CharSequence generateClassDeclaration(final String className, final String implementedInterface)
    {
        return String.format(
            "    public class %s : %s\n" +
                "    {\n",
            className,
            implementedInterface
        );
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

    private CharSequence generatePrimitivePropertyEncodings(final List<Token> tokens,
                                                            final String indent)
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

    private CharSequence generatePrimitiveProperty(final String propertyName,
                                                   final Token token,
                                                   final String indent)
    {
        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            return generateConstPropertyMethods(propertyName, token, indent);
        }
        else
        {
            return generatePrimitivePropertyMethods(propertyName, token, indent);
        }
    }

    private CharSequence generatePrimitivePropertyMethods(final String propertyName,
                                                          final Token token,
                                                          final String indent)
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

    private CharSequence generateSingleValueProperty(final String propertyName,
                                                     final Token token,
                                                     final String indent)
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
            indent + "            return _buffer.%4$sGet(_offset + %5$d%6$s);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%4$sPut(_offset + %5$d, value%6$s);\n" +
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
            indent + "        if (actingVersion < %1$d) return %2$s;\n\n",
            Integer.valueOf(sinceVersion),
            sinceVersion > 0 ? generateLiteral(encoding.primitiveType(), encoding.nullVal().toString()) : "(byte)0"
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

    private CharSequence generateArrayProperty(final String propertyName,
                                               final Token token,
                                               final String indent)
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
            indent + "        return _buffer.%5$sGet(_offset + %6$d + (index * %7$d)%8$s);\n" +
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
            indent + "        _buffer.%4$sPut(_offset + %5$d + (index * %6$d), value%7$s);\n" +
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
                indent + "            throw new IndexOutOfRangeException(\"dstOffset out of range for copy: offset=\" + dstOffset);\n" +
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
                indent + "            throw new IndexOutOfRangeException(\"srcOffset out of range for copy: offset=\" + srcOffset);\n" +
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
                generateLiteral(token.encoding().primitiveType(), token.encoding().constVal().toString())
            );
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = cSharpTypeName(token.encoding().primitiveType());
        final byte[] constantValue = token.encoding().constVal().byteArrayValue();
        final CharSequence values = generateByteLiteralList(token.encoding().constVal().byteArrayValue());

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

    private CharSequence generateMessageFlyweightCode(final String className,
                                                      final int blockLength,
                                                      final int version,
                                                      final int schemaId)
    {
        final String blockLengthType = cSharpTypeName(ir.messageHeader().blockLengthType());
        final String templateIdType = cSharpTypeName(ir.messageHeader().templateIdType());
        final String templateVersionType = cSharpTypeName(ir.messageHeader().templateVersionType());

        return String.format(
            "    public const %s TemplateId = %s;\n" +
            "    public const %s TemplateVersion = %s;\n" +
            "    public const %s BlockLength = %s;\n\n" +
            "    private readonly IMessageFlyweight _parentMessage;\n" +
            "    private DirectBuffer _buffer;\n" +
            "    private int _offset;\n" +
            "    private int _position;\n" +
            "    private int _actingBlockLength;\n" +
            "    private int _actingVersion;\n" +
            "\n" +
            "    public int Offset { get { return _offset; } }\n\n" +
            "    public %s()\n" +
            "    {\n" +
            "        _parentMessage = this;\n" +
            "    }\n\n" +
            "    public void WrapForEncode(DirectBuffer buffer, int offset)\n" +
            "    {\n" +
            "        _buffer = buffer;\n" +
            "        _offset = offset;\n" +
            "        _actingBlockLength = BlockLength;\n" +
            "        _actingVersion = TemplateVersion;\n" +
            "        Position = offset + _actingBlockLength;\n" +
            "    }\n\n" +
            "    public void WrapForDecode(DirectBuffer buffer, int offset,\n" +
            "                              int actingBlockLength, int actingVersion)\n" +
            "    {\n" +
            "        _buffer = buffer;\n" +
            "        _offset = offset;\n" +
            "        _actingBlockLength = actingBlockLength;\n" +
            "        _actingVersion = actingVersion;\n" +
            "        Position = offset + _actingBlockLength;\n" +
            "    }\n\n" +
            "    public int Size\n" +
            "    {\n" +
            "        get\n" +
            "        {\n" +
            "            return _position - _offset;\n" +
            "        }\n" +
            "    }\n\n" +
            "    public int Position\n" +
            "    {\n" +
            "        get\n" +
            "        {\n" +
            "            return _position;\n" +
            "        }\n" +
            "        set\n" +
            "        {\n" +
            "            _buffer.CheckPosition(_position);\n" +
            "            _position = value;\n" +
            "        }\n" +
            "    }\n\n",
            templateIdType,
            generateLiteral(ir.messageHeader().templateIdType(), Integer.toString(schemaId)),
            templateVersionType,
            generateLiteral(ir.messageHeader().templateVersionType(), Integer.toString(version)),
            blockLengthType,
            generateLiteral(ir.messageHeader().blockLengthType(), Integer.toString(blockLength)),
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
            indent + "    public const int %sSchemaId = %d;\n",
            CSharpUtil.formatPropertyName(token.name()),
            Integer.valueOf(token.schemaId())
        ));
    }

    private CharSequence generateEnumFieldNotPresentCondition(final int sinceVersion, final String enumName, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %d) return %s.NULL_VALUE;\n\n",
            Integer.valueOf(sinceVersion),
            enumName
        );
    }

    private CharSequence generateEnumProperty(final String propertyName,
                                              final Token token,
                                              final String indent)
    {
        final String enumName = token.name();
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
            indent + "            return (%4$s)_buffer.%5$sGet(_offset + %6$d%7$s);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%5$sPut(_offset + %6$d, (%8$s)value%7$s);\n" +
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
            indent + "            return (%4$s)_buffer.%5$sGet(_offset + %6$d%7$s);\n" +
            indent + "        }\n" +
            indent + "        set\n" +
            indent + "        {\n" +
            indent + "            _buffer.%5$sPut(_offset + %6$d, (%8$s)value%7$s);\n" +
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
                return ", ByteOrder.BigEndian";

            default:
                return ", ByteOrder.LittleEndian";
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
                literal = value + "f";
                break;

            case UINT64:
                literal = value + "UL";
                break;

            case INT64:
                literal = value + "L";
                break;

            case DOUBLE:
                literal = value + "d";
        }

        return literal;
    }
}
