/*
 * Copyright 2013-2019 Real Logic Ltd.
 * Copyright (C) 2017 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.python;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.generation.csharp.CSharpUtil;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.*;

import static uk.co.real_logic.sbe.generation.python.PyUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

public class PyGenerator implements CodeGenerator
{
    private static final String META_ATTRIBUTE_ENUM = "MetaAttribute";
    private static final String INDENT = "    ";
    private static final String BASE_INDENT = INDENT;

    private final Ir ir;
    private final OutputManager outputManager;

    public PyGenerator(final Ir ir, final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generateMessageHeaderStub(final List<String> modules) throws IOException
    {
        generateComposite(ir.headerStructure().tokens(), modules);
    }

    public void generateTypeStubs(final List<String> modules) throws IOException
    {
        generateMetaAttributeEnum();

        for (final List<Token> tokens : ir.types())
        {
            switch (tokens.get(0).signal())
            {
                case BEGIN_ENUM:
                    generateEnum(tokens, modules);
                    break;

                case BEGIN_SET:
                    generateBitSet(tokens, modules);
                    break;

                case BEGIN_COMPOSITE:
                    generateComposite(tokens, modules);
                    break;
            }
        }
    }

    public void generate() throws IOException
    {
        final List<String> modules = new ArrayList<>();
        generateMessageHeaderStub(modules);
        generateTypeStubs(modules);
        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());
            final String pyModuleName = camToSnake(className);
            try (Writer out = outputManager.createOutput(pyModuleName))
            {
                modules.add(pyModuleName);
                final List<Token> messageBody = getMessageBody(tokens);
                int offset = 0;

                final List<Token> fields = new ArrayList<>();
                offset = collectFields(messageBody, offset, fields);

                out.append(generateFileHeader(ir.applicableNamespace(), messageBody, true));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(className, msgToken, BASE_INDENT, fields));

                out.append(generateFields(fields, BASE_INDENT, className));

                final StringBuilder sb = new StringBuilder();
                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                generateGroups(sb, className, groups, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = new ArrayList<>();
                collectVarData(messageBody, offset, varData);
                out.append(generateVarData(varData, BASE_INDENT, className));
            }
        }
        generateStructParsers();
        generatePackageDefinition(modules);
    }

    public void generatePackageDefinition(final List<String> modules) throws IOException
    {
        try (Writer out = outputManager.createOutput("__init__"))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), new ArrayList<>(), false));
            for (final String m : modules)
            {
                out.append("from ." + m + " import *\n");
            }
            out.append("from .meta_attribute import *\n");
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

            generateGroupClassHeader(sb, groupName, parentMessageClassName, tokens, i, indent);
            i++;
            i += tokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateFields(fields, indent + INDENT, parentMessageClassName + "." +
                formatClassName(groupName) + "Group"));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(sb, parentMessageClassName + "." + formatClassName(groupName) +
                "Group", groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            sb.append(generateVarData(varData, indent + INDENT, parentMessageClassName + "." +
                formatClassName(groupName) + "Group"));

            sb.append(generateGroupProperty(groupName, groupToken, indent, parentMessageClassName));

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
        final String topLevelParentMessageClassName = parentMessageClassName.split("\\.")[0];
        sb.append(String.format("\n" +
            indent + "class %1$sGroup:\n" +
            indent + INDENT + "__slots__ = '_dimensions', '_parent_message', '_buffer', '_block_length', " +
            "'_acting_version', '_count', '_index', '_offset'\n\n" +
            indent + INDENT + "def __init__(self):\n" +
            indent + INDENT + INDENT + "self._dimensions: '%2$s' = %2$s()\n" +
            indent + INDENT + INDENT + "self._parent_message: '%3$s'\n" +
            indent + INDENT + INDENT + "self._buffer: Union[bytes, bytearray, memoryview]\n" +
            indent + INDENT + INDENT + "self._block_length: int\n" +
            indent + INDENT + INDENT + "self._acting_version: int\n" +
            indent + INDENT + INDENT + "self._count: int\n" +
            indent + INDENT + INDENT + "self._index: int\n" +
            indent + INDENT + INDENT + "self._offset: int\n",
            formatClassName(groupName),
            dimensionsClassName,
            topLevelParentMessageClassName));

        final int blockLength = tokens.get(index).encodedLength();
        sb.append(String.format("\n" +
            indent + INDENT + "SBE_BLOCK_LENGTH: int = %d\n" +
            indent + INDENT + "SBE_HEADER_SIZE: int = %d\n",
            blockLength,
            dimensionHeaderLength));

        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);

        sb.append(String.format("\n" +
            indent + "    def wrap_decode(self, parent_message: '%1$s' , buffer: Union[bytes, bytearray," +
            " memoryview" + "], acting_version: int):\n" +
            indent + "        self._parent_message = parent_message\n" +
            indent + "        self._buffer = buffer\n" +
            indent + "        self._dimensions.wrap(buffer, parent_message.limit)\n" +
            indent + "        self._block_length = self._dimensions.get_block_length()\n" +
            indent + "        self._count = self._dimensions.get_num_in_group()\n" +
            indent + "        self._acting_version = acting_version\n" +
            indent + "        self._index = -1\n" +
            indent + "        self._parent_message.set_limit(self._parent_message.limit + self.SBE_HEADER_SIZE)\n" +
            indent + "        return self\n",
            topLevelParentMessageClassName,
            formatClassName(groupName)));

        final String typeForBlockLength = pythonTypeName(tokens.get(index + 2).encoding().primitiveType());
        final String typeForNumInGroup = pythonTypeName(numInGroupToken.encoding().primitiveType());

        sb.append(String.format("\n" +
            indent + "    def wrap_encode(self, parent_message: '%1$s', buffer: Union[bytes, bytearray, memoryview]," +
            " count: int):\n" +
            indent + "        if count < %2$d or count > %3$d:\n" +
            indent + "            raise IndexError('Count Outside allowed range: count='+ str(count) +', min=%2$d, " +
            "max=%3$d')\n" +
            indent + "        self._parent_message = parent_message\n" +
            indent + "        self._buffer = buffer\n" +
            indent + "        self._dimensions.wrap(buffer, parent_message.limit)\n" +
            indent + "        self._dimensions.set_block_length(%4$s(%5$d))\n" +
            indent + "        self._dimensions.set_num_in_group(%6$s(count))\n" +
            indent + "        self._index = -1\n" +
            indent + "        self._count = count\n" +
            indent + "        self._block_length = %5$d\n" +
            indent + "        self._acting_version = self._parent_message.SCHEMA_VERSION\n" +
            indent + "        self._parent_message.set_limit(parent_message.limit + self.SBE_HEADER_SIZE)\n" +
            indent + "        return self\n",
            topLevelParentMessageClassName,
            numInGroupToken.encoding().applicableMinValue().longValue(),
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            typeForBlockLength,
            blockLength,
            typeForNumInGroup));


        sb.append("\n");
        generateGroupEnumerator(sb, groupName, indent + INDENT, parentMessageClassName);
    }

    private void generateGroupEnumerator(final StringBuilder sb, final String groupName, final String indent,
        final String parentClassName)
    {
        sb.append(indent + "@property\n" +
            indent + "def acting_block_length(self) -> int:\n" +
            indent + "    return self._block_length\n\n" +
            indent + "@property\n" +
            indent + "def count(self) -> int:\n" +
            indent + "    return self._count\n\n" +
            indent + "@property\n" +
            indent + "def has_next(self) -> bool:\n" +
            indent + "    return (self._index + 1) < self._count\n");

        sb.append(String.format("\n" +
            indent + "def next(self) -> '%2$s.%1$sGroup':\n" +
            indent + "    if self._index + 1 >= self._count:\n" +
            indent + "        raise IndexError()\n" +
            indent + "    self._offset = self._parent_message.limit\n" +
            indent + "    self._parent_message.set_limit(self._offset + self._block_length)\n" +
            indent + "    self._index += 1\n" +
            indent + "    return self\n",
            formatClassName(groupName), parentClassName));

        sb.append(String.format("\n" +
            indent + "def generator(self) -> Generator['%2$s.%1$sGroup', None, None]:\n" +
            indent + "    while self.has_next:\n" +
            indent + "        yield self.next()\n",
            formatClassName(groupName), parentClassName
        ));
    }

    private CharSequence generateGroupProperty(final String groupName, final Token token, final String indent,
        final String parentMessageClassName)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = CSharpUtil.formatClassName(groupName);

        sb.append(String.format("\n" +
            indent + "%S_ID: int = %d\n",
            camToSnake(groupName),
            token.id()));

        sb.append(String.format("\n" +
            indent + "_%1$s_group: '%2$sGroup' = %2$sGroup()\n",
            camToSnake(groupName),
            formatClassName(groupName),
            token.id()));

        generateSinceActingDeprecated(sb, indent, toUpperFirstChar(groupName), token);

        sb.append(String.format("\n" +
            indent + "def get_%2$s(self) -> '" + parentMessageClassName + ".%1$sGroup':\n" +
            indent + "    self._%3$s_group.wrap_decode(self._parent_message, self._buffer, " +
            "self._parent_message.SCHEMA_VERSION)\n" +
            indent + "    return self._%3$s_group\n",
            className,
            camToSnake(groupName),
            camToSnake(groupName)));

        sb.append(String.format("\n" +
            indent + "def set_%2$s_count(self, count:int) -> '" + parentMessageClassName + ".%1$sGroup':\n" +
            indent + "    self._%3$s_group.wrap_encode(self._parent_message, self._buffer, count)\n" +
            indent + "    return self._%3$s_group\n",
            className,
            camToSnake(groupName),
            camToSnake(groupName)));

        return sb;
    }

    private CharSequence generateVarData(final List<Token> tokens, final String indent, final String parentClassName)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                generateFieldIdMethod(sb, token, indent);

                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();
                generateCharacterEncodingMethod(sb, token.name(), characterEncoding, indent);
                generateFieldMetaAttributeMethod(sb, token, indent);

                final String propertyName = toUpperFirstChar(token.name());
                final Token lengthToken = Generators.findFirst("length", tokens, i);
                final int sizeOfLengthField = lengthToken.encodedLength();
                final Encoding lengthEncoding = lengthToken.encoding();
                final String lengthCSharpType = pythonTypeName(lengthEncoding.primitiveType());
                final String lengthTypePrefix = toUpperFirstChar(lengthEncoding.primitiveType().primitiveName());
                final ByteOrder byteOrder = lengthEncoding.byteOrder();
                final String byteOrderStr = byteOrder.toString();

                sb.append(String.format("\n" +
                    indent + "%1$S_HEADER_SIZE: int = %2$d\n",
                    camToSnake(propertyName),
                    sizeOfLengthField));

                sb.append(String.format("\n" +
                    indent + "def get_%1$s(self) -> memoryview:\n" +
                    "%2$s" +
                    indent + " size_of_len_field: int = %3$d\n" +
                    indent + " limit:int = self._parent_message.limit\n" +
                    indent + " #self._buffer.CheckLimit(limit + size_of_len_field);\n" +
                    indent + " data_length: int = %4$S_%5$S.unpack_from(self._buffer, limit)[0]\n" +
                    indent + " self._parent_message.set_limit(limit + size_of_len_field + data_length)\n" +
                    indent + " return memoryview(self._buffer)[limit + size_of_len_field:limit + " +
                    "size_of_len_field+data_length]\n",
                    camToSnake(propertyName),
                    generateArrayFieldNotPresentCondition(token.version(), indent),
                    sizeOfLengthField,
                    lengthTypePrefix,
                    byteOrderStr));

                sb.append(String.format("\n" +
                    indent + "def set_%1$s(self, data: Union[bytes, bytearray, memoryview]) -> '%7$s':\n" +
                    indent + " size_of_len_field: int = %2$d\n" +
                    indent + " limit:int = self._parent_message.limit\n" +
                    indent + " self._parent_message.set_limit(limit + size_of_len_field + len(data))\n" +
                    indent + " data_len = len(data)\n" +
                    indent + " %3$S_%5$S.pack_into(self._buffer, limit, %4$s(data_len))\n" +
                    indent + " struct.pack_into('%6$s%%ds' %% (data_len,), self._buffer, limit + size_of_len_field, " +
                    "data)\n" +
                    indent + " return self\n\n",
                    camToSnake(propertyName),
                    sizeOfLengthField,
                    lengthTypePrefix,
                    lengthCSharpType,
                    byteOrderStr,
                    pythonEndianCode(byteOrder),
                    parentClassName));
            }
        }

        return sb;
    }

    private void generateBitSet(final List<Token> tokens, final List<String> modules) throws IOException
    {
        final Token bitSetToken = tokens.get(0);
        final String bitSetName = formatClassName(bitSetToken.applicableTypeName());
        final List<Token> messageBody = getMessageBody(tokens);
        try (Writer out = outputManager.createOutput(camToSnake(bitSetName)))
        {
            modules.add(camToSnake(bitSetName));
            out.append(generateFileHeader(ir.applicableNamespace(), messageBody, true));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateMessageFlyweightCode(bitSetName, bitSetToken, BASE_INDENT, new ArrayList<>()));
            out.append(generateChoiceIsEmpty(bitSetToken.encoding().primitiveType(),
                bitSetToken.encoding().byteOrder()));
            out.append(generateChoiceDecoders(messageBody));
            out.append(generateChoiceDisplay(messageBody));
            out.append(generateChoiceClear(bitSetName, bitSetToken));
            out.append(generateChoiceEncoders(bitSetName, messageBody));

        }
    }

    private void generateEnum(final List<Token> tokens, final List<String> modules) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(enumToken.applicableTypeName());
        final List<Token> messageBody = getMessageBody(tokens);
        try (Writer out = outputManager.createOutput(camToSnake(enumName)))
        {
            modules.add(camToSnake(enumName));
            out.append(generateFileHeader(ir.applicableNamespace(), messageBody, true));
            final String enumPrimitiveType = pythonTypeName(enumToken.encoding().primitiveType());
            out.append(generateEnumDeclaration(enumName, enumPrimitiveType));
            out.append(generateEnumValues(messageBody, enumToken));
        }
    }

    private void generateComposite(final List<Token> tokens, final List<String> modules) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(camToSnake(compositeName)))
        {
            modules.add(camToSnake(compositeName));
            out.append(generateFileHeader(ir.applicableNamespace(), getMessageBody(tokens), true));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(tokens.get(0).encodedLength(), getMessageBody(tokens)));
            out.append(generateCompositePropertyElements(getMessageBody(tokens), BASE_INDENT, compositeName));

        }
    }

    private CharSequence generateCompositePropertyElements(final List<Token> tokens, final String indent,
        final String className)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            // FIXME: do I need to pass classname down here for disambiguation
            switch (token.signal())
            {
                case ENCODING:
                    sb.append(generatePrimitiveProperty(propertyName, token, indent, className));
                    break;

                case BEGIN_ENUM:
                    sb.append(generateEnumProperty(propertyName, token, null, indent, className));
                    break;

                case BEGIN_SET:
                    sb.append(generateBitSetProperty(propertyName, token, indent));
                    break;

                case BEGIN_COMPOSITE:
                    sb.append(generateCompositeProperty(propertyName, token, indent));
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }

        return sb;
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
                final PrimitiveType primitiveType = encoding.primitiveType();

                return String.format("\n" +
                        INDENT + "def set_%2$s(self, value: bool) ->  '%1$s':\n" +
                        "%3$s\n\n" +
                        INDENT + "@staticmethod\n" +
                        INDENT + "def apply_%2$s(bits: '%4$s', value: bool) -> '%4$s':\n" +
                        "%5$s\n",
                    bitSetClassName,
                    camToSnake(choiceName),
                    generateChoicePut(primitiveType, choiceBitIndex, encoding.byteOrder()),
                    "int",
                    generateStaticChoicePut(primitiveType, choiceBitIndex));
            });
    }

    private CharSequence generateChoiceDisplay(final List<Token> tokens)
    {
        final String indent = INDENT;
        final StringBuilder sb = new StringBuilder();

        sb.append('\n');
        sb.append(indent + "def __repr__(self):\n");
        sb.append(indent + indent + "at_least_one = False\n");
        sb.append(indent + indent + "out = \"{\"\n");
        tokens
            .stream()
            .filter((token) -> token.signal() == Signal.CHOICE)
            .forEach((token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                sb.append(indent + indent + "if self.has_" + camToSnake(choiceName) + "():\n");
                sb.append(indent + indent + indent + "if at_least_one:\n");
                sb.append(indent + indent + indent + indent + "out += \",\"\n");
                sb.append(indent + indent + indent + "out += \"" + choiceName + "\"\n");
                sb.append(indent + indent + indent + "at_least_one = True\n");
            });
        sb.append(indent + indent + "out += \"}\"\n");
        sb.append('\n');
        sb.append(indent + indent + "return out\n");

        return sb.toString();
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
                final PrimitiveType primitiveType = encoding.primitiveType();

                return String.format("\n" +
                        "    def has_%1$s(self) ->  bool:\n" +
                        "        return %2$s\n\n" +
                        "    @staticmethod\n" +
                        "    def check_%1$s(value: '%3$s') -> bool:\n" +
                        "        return %4$s\n" +
                        "    \n",
                    camToSnake(choiceName),
                    generateChoiceGet(primitiveType, choiceBitIndex, encoding.byteOrder()),
                    "int",
                    generateStaticChoiceGet(primitiveType, choiceBitIndex));
            });
    }

    private String generateChoiceIsEmpty(final PrimitiveType type, final ByteOrder byteOrder)
    {
        return "\n" +
            "    @property\n" +
            "    def is_empty(self) -> bool:\n" +
            "        return " + generateChoiceIsEmptyInner(type, byteOrder) + "\n" +
            "    \n";
    }

    private String generateChoiceIsEmptyInner(final PrimitiveType type, final ByteOrder byteOrder)
    {
        switch (type)
        {
            case UINT8:
                return "0 == " + type.primitiveName().toUpperCase() + "_" + byteOrder.toString() +
                    ".unpack_from(self._buffer, self._offset)[0]";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoicePut(final PrimitiveType type, final String bitIdx, final ByteOrder byteOrder)
    {
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return INDENT + INDENT + "bits = " + type.primitiveName().toUpperCase() + "_" + byteOrder.toString() +
                       ".unpack_from(self._buffer, self._offset)[0]\n" +
                       "        bits = (bits | (1 << " + bitIdx + ")) if value else (bits & ~(1 << " + bitIdx + "))\n" +
                       INDENT + INDENT + type.primitiveName().toUpperCase() + "_" + byteOrder.toString() +
                       ".pack_into(self._buffer, self._offset,  bits)\n" +
                       INDENT + INDENT + "return  self\n";

        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateStaticChoicePut(final PrimitiveType type, final String bitIdx)
    {
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return INDENT + INDENT + "return (bits | (1 << " + bitIdx + ")) " +
                       "if value else (bits & ~(1 << " + bitIdx + "))\n";
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
            case UINT64:
                return "0 != (value & (1 << " + bitIndex + "))";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoiceGet(final PrimitiveType type, final String bitIndex, final ByteOrder byteOrder)
    {
        final String ending = byteOrder.toString().toUpperCase();
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return "0 != (" + type.primitiveName().toUpperCase() + "_" + ending +
                       ".unpack_from(self._buffer, self._offset)[0] & (1 << " + bitIndex + "))";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");

        sb.append(String.format("\n" +
            "    def clear(self) -> '%s':\n" +
            "        %2$S_%4$S.pack_into(self._buffer, self._offset, %3$s)\n" +
            "        return self\n" +
            "    \n",
            bitSetClassName,
            encoding.primitiveType().primitiveName(),
            literalValue,
            encoding.byteOrder()));

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens, final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        for (final Token token : tokens)
        {
            sb.append(INDENT).append(token.name()).append(" = ")
                .append(token.encoding().constValue()).append("\n");
        }

        final PrimitiveValue nullVal = encoding.applicableNullValue();

        sb.append(INDENT).append("NULL_VALUE = ").append(nullVal).append("\n");

        return sb;
    }

    private CharSequence generateFileHeader(final String packageName, final List<Token> tokens, final boolean imports)
    {
        final StringBuilder sb = new StringBuilder("\"\"\"\n Generated SBE (Simple Binary Encoding) message codec\n");
        sb.append("Package: ").append(packageName);
        sb.append("\"\"\"\n\n");
        if (imports)
        {
            sb.append(generateImports(tokens));
        }
        sb.append("\n");
        return sb;
    }

    private CharSequence generateImports(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();
        final Set<String> processed = new HashSet<>();
        sb.append("from typing import *\n");
        sb.append("import struct\n");
        sb.append("from ._struct_defs import *\n");
        sb.append("from .meta_attribute import MetaAttribute\n");
        for (final Token t : tokens)
        {
            if (t.signal() == Signal.BEGIN_COMPOSITE || t.signal() == Signal.BEGIN_SET ||
                t.signal() == Signal.BEGIN_ENUM)
            {
                final String importName = t.referencedName() != null ? formatClassName(t.referencedName()) :
                    formatClassName(t.name());
                if (processed.contains(importName))
                {
                    continue;
                }
                sb.append("from .").append(camToSnake(importName)).append(" import ").append(importName).append("\n");
                processed.add(importName);
            }
        }
        return sb;
    }

    private void generateStructParsers() throws IOException
    {
        try (Writer out = outputManager.createOutput("_struct_defs"))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), new ArrayList<>(), false));
            out.append("from struct import Struct\n\n");
            final StringBuilder builder = new StringBuilder();
            for (final Map.Entry<ByteOrder, String> order : BYTE_ORDER_STRING_MAP.entrySet())
            {
                for (final Map.Entry<PrimitiveType, String> type : PRIMITIVE_TYPE_STRUCT_ENUM_MAP.entrySet())
                {
                    builder.append(String.format("%1$S_%2$S: Struct = Struct('%3$s%4$s')\n",
                        type.getKey().primitiveName(),
                        order.getKey().toString(),
                        pythonEndianCode(order.getKey()),
                        pythonTypeCode(type.getKey())));
                }
                //edge case to handle char[] unpacking
                builder.append(String.format("%1$S_%2$S: Struct = Struct('%3$s%4$s')\n",
                    "CHARS",
                    order.getKey().toString(),
                    pythonEndianCode(order.getKey()),
                    "s"));
            }

            builder.append("\n");
            out.append(builder.toString());
        }
    }

    private CharSequence generateClassDeclaration(final String className)
    {
        return String.format("class %s:\n", className);
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (Writer out = outputManager.createOutput(camToSnake(META_ATTRIBUTE_ENUM)))
        {
            out.append(generateFileHeader(ir.applicableNamespace(), new ArrayList<>(), false));
            out.append(
                "from enum import IntEnum \n\n" +
                "class MetaAttribute(IntEnum):\n" +
                "    Epoch = 1\n" +
                "    TimeUnit = 2\n" +
                "    SemanticType = 3\n" +
                "    Presence = 4\n");
        }
    }

    private CharSequence generateEnumDeclaration(final String name, final String primitiveType)
    {

        return "from enum import IntEnum \n\n" +
            "class " + name + "(IntEnum):\n";
    }

    private CharSequence generatePrimitiveProperty(final String propertyName, final Token token, final String indent,
        final String parentClassName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (token.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyMethods(propertyName, token, indent, parentClassName));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyMethods(
        final String propertyName,
        final Token token,
        final String indent,
        final String parentClassName)
    {
        final int arrayLength = token.arrayLength();

        if (arrayLength == 1)
        {
            return generateSingleValueProperty(propertyName, token, indent, parentClassName);
        }
        else if (arrayLength > 1)
        {
            return generateArrayProperty(propertyName, token, indent, parentClassName);
        }

        return "";
    }

    private CharSequence generatePrimitiveFieldMetaData(
        final String propertyName,
        final Token token,
        final String indent)
    {
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String typeName = pythonTypeName(primitiveType);

        return String.format(
            "\n" +
                indent + "%2$s_NULL_VALUE: '%1$s' = %3$s\n" +
                indent + "%2$s_MIN_VALUE: '%1$s' = %4$s\n" +
                indent + "%2$s_MAX_VALUE: '%1$s' = %5$s\n",
            typeName,
            camToSnake(propertyName).toUpperCase(),
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString()),
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString()),
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString()));
    }

    private CharSequence generateSingleValueProperty(
        final String propertyName,
        final Token token,
        final String indent,
        final String parentClassName)
    {
        final String typeName = pythonTypeName(token.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final int offset = token.offset();
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = byteOrder.toString();

        final String getter = String.format("\n" +
            indent + "def get_%2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "    return %4$S_%6$S.unpack_from(self._buffer, self._offset + %5$d)[0]",
            typeName,
            camToSnake(propertyName),
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            typePrefix.toUpperCase(),
            offset,
            byteOrderStr);

        final String setter = String.format("\n" +
            indent + "def set_%2$s(self, value:%1$s) -> '%3$s':\n" +
            indent + "    %4$S_%6$s.pack_into(self._buffer, self._offset + %5$d, value)\n" +
            indent + "    return self\n",
            typeName,
            camToSnake(propertyName),
            formatClassName(parentClassName),
            typePrefix.toUpperCase(),
            offset,
            byteOrderStr);
        return getter + "\n" + setter + "\n";
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
            indent + "if self._acting_version < %1$d: return %2$s\n\n",
            sinceVersion,
            literal);
    }

    private CharSequence generateArrayFieldNotPresentCondition(
        final int sinceVersion,
        final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "if self._acting_version < %d: return 0\n\n",
            sinceVersion);
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
            indent + "if self._acting_version < %d: return None\n\n",
            sinceVersion);
    }

    private CharSequence generateArrayProperty(
        final String propertyName,
        final Token token,
        final String indent,
        final String parentClassName)
    {
        final String typeName = pythonTypeName(token.encoding().primitiveType());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final int offset = token.offset();
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = byteOrder.toString();
        final int fieldLength = token.arrayLength();
        final int typeSize = token.encoding().primitiveType().size();
        final String propName = toUpperFirstChar(propertyName);

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "%1$S_LENGTH = %2$d\n",
            camToSnake(propName), fieldLength));

        final String structParserMulti = String.format("A_%1$S_%2$S_%3$S", camToSnake(propName), typePrefix,
            byteOrderStr);

        sb.append(String.format("\n" +
            indent + structParserMulti + " = Struct('%1$s%2$d%3$s') \n",
            pythonEndianCode(byteOrder), fieldLength,
            pythonTypeCode(token.encoding().primitiveType())));

        if (token.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, token.encoding().characterEncoding(), indent);
            //Properties for multi item assignment
            sb.append(String.format("\n" +
                indent + "def get_multi_%2$s(self) -> bytes:\n" +
                "%4$s" +
                indent + "    return self." + structParserMulti + ".unpack_from(self._buffer, self._offset + %5$d)[0]" +
                "\n",
                typeName, camToSnake(propName), fieldLength,
                generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
                offset, typeSize));
        }
        else
        {
            //Properties for multi item assignment
            sb.append(String.format("\n" +
                indent + "def get_multi_%2$s(self) -> Tuple['%1$s']:\n" +
                "%4$s" +
                indent + "    return self." + structParserMulti + ".unpack_from(self._buffer, self._offset + %5$d)\n",
                typeName, camToSnake(propName), fieldLength,
                generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
                offset, typeSize));
        }


        sb.append(String.format("\n" +
            indent + "def set_%1$s(self, value:%2$s) -> '%4$s':\n" +
            indent + "    self." + structParserMulti + ".pack_into(self._buffer, self._offset + %3$d, value)\n" +
            indent + "    return self\n",
            camToSnake(propName), typeName, offset, parentClassName));

        //Properties for single assigment
        sb.append(String.format("\n" +
            indent + "def get_%2$s(self, index: int) -> '%1$s':\n" +
            indent + "    if index < 0 or index >= %3$d:\n" +
            indent + "        raise IndexError(\"index out of range: index=\" + str(index))\n\n" +
            "%4$s" +
            indent + "    return  %5$S_%8$S.unpack_from(self._buffer, self._offset + %6$d + (index * %7$d))[0]\n",
            typeName, camToSnake(propName), fieldLength,
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent), typePrefix,
            offset, typeSize, byteOrder));

        sb.append(String.format("\n" +
            indent + "def put_%1$s(self, index:int, value:%2$s) -> '%8$s':\n" +
            indent + "    if index < 0 or index >= %3$d:\n" +
            indent + "        raise IndexError(\"index out of range: index=\" + str(index))\n\n" +
            indent + "    %4$S_%7$S.pack_into(self._buffer, self._offset + %5$d + (index * %6$d), value)\n" +
            indent + "    return self\n",
            camToSnake(propName), typeName, fieldLength, typePrefix, offset, typeSize, byteOrder, parentClassName));


        return sb;
    }

    private void generateCharacterEncodingMethod(
        final StringBuilder sb,
        final String propertyName,
        final String encoding,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "%S_CHAR_ENCODING = \"%s\"\n\n",
            camToSnake(formatPropertyName(propertyName)),
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
                    indent + "@property\n" +
                    indent + "def get_%2$s(self) -> '%1$s':\n" +
                    indent + "    return %3$s\n",
                pythonTypeName(token.encoding().primitiveType()),
                camToSnake(propertyName),
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
        }

        final StringBuilder sb = new StringBuilder();

        final String pyTypeName = pythonTypeName(token.encoding().primitiveType());
        final byte[] constantValue = token.encoding().constValue().byteArrayValue(token.encoding().primitiveType());
        final CharSequence values = generateByteLiteralList(
            token.encoding().constValue().byteArrayValue(token.encoding().primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + "_%1$S_VALUE: Union[bytes, bytearray, memoryview] = bytes([ %2$s ])\n",
            camToSnake(propertyName),
            values));

        sb.append(String.format(
            "\n" +
            indent + "%1$S_LENGTH: int = %2$d\n",
            camToSnake(propertyName).toUpperCase(),
            constantValue.length));
        sb.append("\n");
        sb.append(String.format(
            indent + "def %2$s(self, index: int) -> '%1$s':\n" +
            indent + "    return self._%3$s_VALUE[index]\n",
            pyTypeName,
            camToSnake(propertyName),
            camToSnake(propertyName).toUpperCase()));
        sb.append("\n");

        sb.append(String.format(
            indent + "def get_%1$s(self, offset: int, length: int) -> memoryview:\n" +
            indent + "    last = min(length, %2$d)\n" +
            indent + "    offset = min(abs(offset), %3$d)\n" +
            indent + "    return memoryview(self._%4$S_VALUE)[offset:last]\n",
            camToSnake(propertyName),
            constantValue.length,
            constantValue.length - 1,
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

    private CharSequence generateFixedFlyweightCode(final int size, final List<Token> tokens)
    {
        //reserve __slots__ to improve performance for instance fields
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> fieldTypes = new LinkedHashMap<>();
        for (final Token t : tokens)
        {
            if (t.signal() == Signal.BEGIN_COMPOSITE || t.signal() == Signal.BEGIN_SET)
            {
                fieldTypes.put("_" + t.name(), t.referencedName());
            }
        }
        final List<String> fields = new ArrayList<>(fieldTypes.keySet());
        builder.append(INDENT + "__slots__ = '_offset', '_buffer'");
        if (fields.size() > 0)
        {
            builder.append(", '" + String.join("', '", fields) + "'");
        }
        builder.append("\n\n");
        builder.append(String.format(
            "    SIZE: int = %d \n\n" +
            "    def __init__(self):\n" +
            "        self._offset: int \n" +
            "        self._buffer: int \n", size));
        for (final Map.Entry<String, String> field : fieldTypes.entrySet())
        {
            builder.append(INDENT + INDENT + "self." + field.getKey() + ": '" + field.getValue() + "' = " +
                field.getValue() + "()\n");
        }
        builder.append("\n");

        builder.append(INDENT + "def wrap(self, buffer: Union[bytes, bytearray, memoryview], offset: int):\n" +
            INDENT + INDENT + "self._offset = offset\n" +
            INDENT + INDENT + "self._buffer = buffer\n" +
            INDENT + INDENT + "return self\n");
        return builder.toString();
    }


    private CharSequence generateMessageFlyweightCode(final String className, final Token token, final String indent,
        final List<Token> fields)
    {
        final String blockLengthType = pythonTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = pythonTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = pythonTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = pythonTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> fieldTypes = new LinkedHashMap<>();
        for (final Token t : fields)
        {
            if (t.signal() == Signal.BEGIN_COMPOSITE || t.signal() == Signal.BEGIN_SET)
            {
                fieldTypes.put("_" + camToSnake(t.name()), t.referencedName() != null ? t.referencedName() : t.name());
            }
        }
        builder.append(String.format(
            indent + "BLOCK_LENGTH: '%1$s' = %2$s\n" +
            indent + "TEMPLATE_ID: '%3$s' = %4$s\n" +
            indent + "SCHEMA_ID: '%5$s' = %6$s\n" +
            indent + "SCHEMA_VERSION: '%7$s' = %8$s\n" +
            indent + "SEMANTIC_TYPE: str = \"%9$s\"\n\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            semanticType));

        builder.append(indent + "__slots__ = '_parent_message', '_buffer', '_offset', '_limit', " +
            "'_acting_block_length', '_acting_version'");
        if (fieldTypes.size() > 0)
        {
            builder.append(", '" + String.join("', '", fieldTypes.keySet()) + "'");
        }
        builder.append("\n\n");

        builder.append(String.format(
            indent + "def __init__(self):\n" +
            indent + "    self._parent_message: '%1$s' = self\n" +
            indent + "    self._buffer: Union[bytes, bytearray, memoryview]\n" +
            indent + "    self._offset: int\n" +
            indent + "    self._limit: int\n" +
            indent + "    self._acting_block_length: int\n" +
            indent + "    self._acting_version: int\n", className));
        for (final Map.Entry<String, String> ft : fieldTypes.entrySet())
        {
            builder.append(indent + INDENT + "self." + ft.getKey() + ": '" + ft.getValue() + "' = " +
                ft.getValue() + "()\n");
        }
        builder.append("\n");

        builder.append(
            indent + "@property\n" +
            indent + "def offset(self) -> int:\n" +
            indent + "    return self._offset\n\n" +
            indent + "def wrap_encode(self, buffer: Union[bytes, bytearray, memoryview], offset: int):\n" +
            indent + "    self._buffer = buffer\n" +
            indent + "    self._offset = offset\n" +
            indent + "    self._acting_block_length = self.BLOCK_LENGTH\n" +
            indent + "    self._acting_version = self.SCHEMA_VERSION\n" +
            indent + "    self.set_limit(offset + self._acting_block_length)\n" +
            indent + "    return self\n");
        builder.append("\n");

        builder.append(
            indent + "def wrap_decode(self,buffer: Union[bytes, bytearray, memoryview], offset: int, " +
            "acting_block_length: int, acting_version:int):\n" +
            indent + "    self._buffer = buffer\n" +
            indent + "    self._offset = offset\n" +
            indent + "    self._acting_block_length = acting_block_length\n" +
            indent + "    self._acting_version = acting_version\n" +
            indent + "    self.set_limit(offset + self._acting_block_length)\n" +
            indent + "    return self\n");
        builder.append("\n");

        builder.append(
            indent + "def size(self) -> int:\n" +
            indent + "    return self._limit - self._offset\n" +
            indent + "\n\n" +
            indent + "@property\n" +
            indent + "def limit(self) -> int:\n" +
            indent + "    return self._limit\n");
        builder.append("\n");

        builder.append(
            indent + "def set_limit(self, value):\n" +
            //TODO: make sure the capacity is checked either wrap in bytearray
            indent + "    #self._buffer.CheckLimit(value);\n" +
            indent + "    self._limit = value\n" +
            indent + "    return self\n");

        return builder.toString();
    }

    private CharSequence generateFields(final List<Token> tokens, final String indent, final String parentClassName)
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
                        sb.append(generatePrimitiveProperty(propertyName, encodingToken, indent, parentClassName));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(propertyName, encodingToken, signalToken, indent,
                            parentClassName));
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
        sb.append(String.format("\n" +
            indent + "%S_ID: int = %d\n",
            camToSnake(token.name()),
            token.id()));

        generateSinceActingDeprecated(sb, indent, token.name(), token);
    }

    private void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();
        final String presence = encoding.presence() == null ? "" : encoding.presence().toString().toLowerCase();

        sb.append(String.format("\n" +
            indent + "@staticmethod\n" +
            indent + "def %s_meta_attribute(meta_attribute: 'MetaAttribute') -> str:\n" +
            indent + "    if meta_attribute == MetaAttribute.Epoch: return '%s'\n" +
            indent + "    if meta_attribute == MetaAttribute.TimeUnit: return '%s'\n" +
            indent + "    if meta_attribute == MetaAttribute.SemanticType: return '%s'\n" +
            indent + "    if meta_attribute == MetaAttribute.Presence: return '%s'\n" +
            indent + "    return ''\n",
            camToSnake(token.name()),
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
            indent + "if self._acting_version < %d: return %s.NULL_VALUE\n\n",
            sinceVersion,
            enumName);
    }

    private CharSequence generateEnumProperty(
        final String propertyName,
        final Token token,
        final Token signalToken,
        final String indent,
        final String parentClassName)
    {
        final String enumName = formatClassName(token.applicableTypeName());
        final String typePrefix = toUpperFirstChar(token.encoding().primitiveType().primitiveName());
        final String typeName = pythonTypeName(token.encoding().primitiveType());
        final String enumUnderlyingType = pythonTypeName(token.encoding().primitiveType());
        final int offset = token.offset();
        final ByteOrder byteOrder = token.encoding().byteOrder();
        final String byteOrderStr = byteOrder.toString();

        if (signalToken != null && signalToken.isConstantEncoding())
        {
            final String constValue = signalToken.encoding().constValue().toString();

            return String.format("\n" +
                    indent + "@staticmethod\n" +
                    indent + "def get_%2$s() -> '%1$s':\n" +
                    indent + "    return %3$s\n",
                enumName,
                camToSnake(propertyName),
                constValue);
        }
        else
        {
            String enumStructType = typePrefix;
            if (typePrefix.toUpperCase().equals("CHAR"))
            {
                enumStructType = "INT8";
            }
            final String getter = String.format(
                indent + "def get_%2$s(self) -> '%1$s':\n" +
                "%3$s" +
                indent + "    return %4$s(%5$S_%7$S.unpack_from(self._buffer, self._offset + %6$d)[0])\n\n",
                enumName,
                camToSnake(propertyName),
                generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
                enumName,
                enumStructType,
                offset,
                byteOrderStr,
                enumUnderlyingType);

            String cast = enumUnderlyingType + "(value)";
            if (enumUnderlyingType.equals("bytes") && enumStructType.equals("INT8"))
            {
                cast = "value";
            }
            else if (enumUnderlyingType.equals("bytes"))
            {
                cast = "bytes([value])";
            }
            final String setter = String.format("\n" +
                indent + "def set_%2$s(self, value: '%4$s') -> '%9$s':\n" +
                "%3$s" +
                indent + "    %5$S_%8$S.pack_into(self._buffer, self._offset + %7$d, %10$s)\n" +
                indent + "    return self\n\n",
                enumName,
                camToSnake(propertyName),
                generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
                enumName,
                enumStructType,
                typeName,
                offset,
                byteOrderStr,
                parentClassName,
                cast);
            return getter + setter;
        }
    }

    private String generateBitSetProperty(final String propertyName, final Token token, final String indent)
    {
        final String bitSetName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();

        final String getter = String.format("\n" +
            indent + "def get_%2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "    return self._%5$s.wrap_decode(self._buffer, self._offset + %4$d, " +
            "self._acting_block_length, self._acting_version)\n",
            bitSetName,
            camToSnake(propertyName),
            generateTypeFieldNotPresentCondition(token.version(), indent),
            offset,
            camToSnake(bitSetName));

        final String setter = String.format("\n" +
            indent + "def set_%3$s(self) -> '%1$s':\n" +
            indent + "    return self._%2$s.wrap_encode(self._buffer, self._offset + %4$d)\n",
            bitSetName,
            camToSnake(bitSetName),
            camToSnake(propertyName),
            offset);
        return getter + setter;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "def set_%2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "    self._%4$s.wrap(self._buffer, self._offset + %5$d)\n" +
            indent + "    return self._%4$s\n",
            compositeName,
            camToSnake(propertyName),
            generateTypeFieldNotPresentCondition(token.version(), indent),
            camToSnake(propertyName),
            offset));

        sb.append(String.format("\n" +
            indent + "def get_%2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "    self._%4$s.wrap(self._buffer, self._offset + %5$d)\n" +
            indent + "    return self._%4$s\n",
            compositeName,
            camToSnake(propertyName),
            generateTypeFieldNotPresentCondition(token.version(), indent),
            camToSnake(propertyName),
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
            indent + "%1$S_SINCE_VERSION: int = %2$d\n" +
            indent + "%1$S_DEPRECATED: int = %3$d\n\n" +
            indent + "@property\n" +
            indent + "def %1$s_in_acting_version(self) -> bool:\n" +
            indent + "    return self._acting_version >= self.%1$S_SINCE_VERSION\n",
            camToSnake(propertyName),
            token.version(),
            token.deprecated()));
    }

    private String generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        switch (type)
        {
            case CHAR:
                literal = "\"" + value + "\"";
                break;
            case UINT8:
            case INT8:
            case INT16:
            case UINT16:
            case UINT32:
            case INT64:
            case INT32:
                literal = value;
                break;
            case UINT64:
                literal = Long.toUnsignedString(Long.parseLong(value));
                break;
            case FLOAT:
            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "float('nan')";
                }
                else
                {
                    literal = value;
                }
                break;
        }

        return literal;
    }
}
