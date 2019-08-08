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
package uk.co.real_logic.sbe.generation.python;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Function;

import static uk.co.real_logic.sbe.generation.python.PyUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

public class PythonGenerator implements CodeGenerator
{
    enum CodecType
    {
        DECODER,
        ENCODER,
        NONE
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

    public PythonGenerator(
        final Ir ir,
        final boolean shouldGenerateGroupOrderAnnotation,
        final boolean shouldGenerateInterfaces,
        final boolean shouldDecodeUnknownEnumValues,
        final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;

        this.fqMutableBuffer = "bytearray";
        this.mutableBuffer = "bytearray";

        this.readOnlyBuffer = "bytes";
        this.fqReadOnlyBuffer = "bytes";

        this.shouldGenerateGroupOrderAnnotation = shouldGenerateGroupOrderAnnotation;
        this.shouldGenerateInterfaces = shouldGenerateInterfaces;
        this.shouldDecodeUnknownEnumValues = shouldDecodeUnknownEnumValues;
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
        if (shouldGenerateInterfaces)
        {
            throw new RuntimeException("Python codec doesn't support interfaces");
        }
        return "";
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
        generateStructParsers();
        generateTypeStubs();
        generateMessageHeaderStub();
        final List<String> modules = new ArrayList<>(ir.messages().size());
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

            generateDecoder(BASE_INDENT, fields, groups, varData, msgToken, messageBody);
            generateEncoder(BASE_INDENT, fields, groups, varData, msgToken, messageBody);

            final String className = formatClassName(msgToken.name());
            final String pyModuleName = camToSnake(className);
            modules.add(pyModuleName);
        }
        generatePackageDefinition(modules);
    }

    public void generatePackageDefinition(final List<String> modules) throws IOException
    {
        try (Writer out = outputManager.createOutput("__init__"))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), new ArrayList<>(), CodecType.NONE));
            for (final String m : modules)
            {
                out.append("from .").append(m).append("_encoder import *\n");
                out.append("from .").append(m).append("_decoder import *\n");
            }
            out.append("from .meta_attribute import *\n");
        }
    }

    private void generateStructParsers() throws IOException
    {
        try (Writer out = outputManager.createOutput("_struct_defs"))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), new ArrayList<>(), CodecType.NONE));
            out.append("from struct import Struct\n\n");
            final StringBuilder builder = new StringBuilder();
            for (final Map.Entry<ByteOrder, String> order : BYTE_ORDER_STRING_MAP.entrySet())
            {
                for (final Map.Entry<PrimitiveType, String> type : PRIMITIVE_TYPE_STRUCT_ENUM_MAP.entrySet())
                {
                    builder.append(String.format("%2$S_%1$S: Struct = Struct('%3$s%4$s')\n",
                        type.getKey().primitiveName(),
                        order.getKey().toString(),
                        pythonEndianCode(order.getKey()),
                        pythonTypeCode(type.getKey())));
                }
                //edge case to handle char[] unpacking
                builder.append(String.format("%2$S_%1$S: Struct = Struct('%3$s%4$s')\n",
                    "CHARS",
                    order.getKey().toString(),
                    pythonEndianCode(order.getKey()),
                    "s"));
            }

            builder.append("\n");
            out.append(builder.toString());
        }
    }

    private void generateEncoder(
        final String indent,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Token msgToken,
        final List<Token> msgBody) throws IOException
    {
        final String className = formatClassName(encoderName(msgToken.name()));
        final String moduleName = formatModuleName(className);
        final String implementsString = implementsInterface(MESSAGE_ENCODER_FLYWEIGHT);

        try (Writer out = outputManager.createOutput(moduleName))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), msgBody, CodecType.ENCODER));

            generateAnnotations(indent, className, groups, out, 0, this::encoderName);
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateEncoderFlyweightCode(className, msgToken));
            out.append(generateEncoderFields(className, fields, indent));

            final StringBuilder sb = new StringBuilder();
            generateEncoderGroups(sb, className, className, groups, indent, false);
            out.append(sb);

            out.append(generateEncoderVarData(className, varData, indent));

        }
    }

    private void generateDecoder(
        final String indent,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final Token msgToken,
        final List<Token> msgBody) throws IOException
    {
        final String className = formatClassName(decoderName(msgToken.name()));
        final String moduleName = formatModuleName(className);
        final String implementsString = implementsInterface(MESSAGE_DECODER_FLYWEIGHT);

        try (Writer out = outputManager.createOutput(moduleName))
        {
            out.append(generateMainHeader(ir.applicableNamespace(), msgBody, CodecType.DECODER));

            generateAnnotations(indent, className, groups, out, 0, this::decoderName);
            out.append(generateDeclaration(className, implementsString, msgToken));
            out.append(generateDecoderFlyweightCode(className, msgToken));
            out.append(generateDecoderFields(fields, indent));

            final StringBuilder sb = new StringBuilder();
            generateDecoderGroups(sb, className, className, groups, indent, false);
            out.append(sb);

            out.append(generateDecoderVarData(varData, indent));

            out.append(generateDecoderDisplay(msgToken.name(), fields, groups, varData, indent));

        }
    }

    private void generateDecoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final String hierarchyName,
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
            final String fullyQualifiedName = hierarchyName + '.' + groupName;

            generateAnnotations(indent + INDENT, groupName, tokens, sb, groupIndex + 1, this::decoderName);
            generateGroupDecoderClassHeader(sb, groupName, outerClassName, fullyQualifiedName, tokens, groups,
                groupIndex, indent + INDENT);

            sb.append(generateDecoderFields(fields, indent + INDENT));
            generateDecoderGroups(sb, outerClassName, fullyQualifiedName, groups, indent + INDENT, true);
            sb.append(generateDecoderVarData(varData, indent + INDENT));

            appendGroupInstanceDecoderDisplay(sb, fields, groups, varData, indent + INDENT);

            sb.append(generateGroupDecoderProperty(groupName, fullyQualifiedName, groupToken, indent, isSubGroup));

            sb.append("\n");
        }
    }

    private void generateEncoderGroups(
        final StringBuilder sb,
        final String outerClassName,
        final String hierarchyName,
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
            final String fullyQualifiedName = hierarchyName + '.' + groupClassName;

            generateAnnotations(indent + INDENT, groupClassName, tokens, sb, groupIndex + 1, this::encoderName);
            generateGroupEncoderClassHeader(sb, groupName, outerClassName, fullyQualifiedName, tokens, groups,
                groupIndex, indent + INDENT);

            sb.append(generateEncoderFields(fullyQualifiedName, fields, indent + INDENT));
            generateEncoderGroups(sb, outerClassName, fullyQualifiedName, groups, indent + INDENT, true);
            sb.append(generateEncoderVarData(fullyQualifiedName, varData, indent + INDENT));

            sb.append(generateGroupEncoderProperty(groupName, fullyQualifiedName, groupToken, indent, isSubGroup));

            sb.append(indent).append("\n");
        }
    }

    private void generateGroupDecoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final String fullyQualifiedClassName,
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
            "\n" +
            indent + "    def wrap(self, buffer: '%1$s', parentMessage: '%4$s'):\n" +
            indent + "        if buffer != self._buffer:\n" +
            indent + "            self._buffer = buffer\n" +
            indent + "        self._index: int = -1\n" +
            indent + "        self._parentMessage = parentMessage\n" +
            indent + "        limit: int = self._parentMessage.limit\n" +
            indent + "        self._parentMessage.set_limit(limit + self.HEADER_SIZE)\n" +
            indent + "        self._blockLength: int = %2$s\n" +
            indent + "        self._count: int = %3$s\n",
            readOnlyBuffer,
            blockLengthGet,
            numInGroupGet,
            parentMessageClassName));

        final int blockLength = tokens.get(index).encodedLength();

        sb.append(indent).append("    @property\n")
            .append(indent).append("    def sbeHeaderSize(self) -> int:\n")
            .append(indent).append("        return self.HEADER_SIZE\n");

        sb.append(String.format("\n" +
            indent + "    @property\n" +
            indent + "    def sbeBlockLength(self) -> int:\n" +
            indent + "        return %d\n",
            blockLength));

        sb.append(String.format("\n" +
            indent + "    def actingBlockLength(self) -> int:\n" +
            indent + "        return self._blockLength\n" +
            indent + "        \n\n" +
            indent + "    def count(self) -> int:\n" +
            indent + "        return self._count\n" +
            indent + "    \n\n" +
            indent + "    def remove(self):\n" +
            indent + "        raise NotImplementedError()\n" +
            indent + "    \n\n" +
            indent + "    def hasNext(self) -> bool:\n" +
            indent + "        return (self._index + 1) < self._count\n" +
            indent + "    \n",
            formatClassName(fullyQualifiedClassName)));

        sb.append(String.format("\n" +
            indent + "    def next(self) -> '%s':\n" +
            indent + "        if self._index + 1 >= self._count:\n" +
            indent + "            raise KeyError()\n" +
            indent + "        \n\n" +
            indent + "        self._offset: int = self._parentMessage.limit\n" +
            indent + "        self._parentMessage.set_limit(self._offset + self._blockLength)\n" +
            indent + "        self._index += 1\n\n" +
            indent + "        return self\n" +
            indent + "    \n",
            formatClassName(fullyQualifiedClassName)));
    }

    private void generateGroupEncoderClassHeader(
        final StringBuilder sb,
        final String groupName,
        final String parentMessageClassName,
        final String hierarchyName,
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
            hierarchyName,
            findSubGroupNames(subGroupTokens),
            ind,
            dimensionHeaderSize);

        final int blockLength = tokens.get(index).encodedLength();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);

        final PrimitiveType blockLengthType = blockLengthToken.encoding().primitiveType();
        final String blockLengthOffset = "limit + " + blockLengthToken.offset();
        final String blockLengthValue = String.valueOf(blockLength);
        final String blockLengthPut = generatePut(
            blockLengthType, blockLengthOffset, blockLengthValue, byteOrderString(blockLengthToken.encoding()));

        final PrimitiveType numInGroupType = numInGroupToken.encoding().primitiveType();
        final String numInGroupOffset = "limit + " + numInGroupToken.offset();
        final String numInGroupValue = "count";
        final String numInGroupPut = generatePut(
            numInGroupType, numInGroupOffset, numInGroupValue, byteOrderString(numInGroupToken.encoding()));

        sb.append(String.format(
            ind + "    def wrap(self, buffer: %2$s, parentMessage: '%1$s', count: int):\n" +
            ind + "        if count < %3$d or count > %4$d:\n" +
            ind + "            raise IndexError(\"count outside allowed range: count=\" + str(count))\n" +
            ind + "        \n\n" +
            ind + "        if buffer != self._buffer:\n" +
            ind + "            self._buffer = buffer\n" +
            ind + "        \n\n" +
            ind + "        self._index = -1\n" +
            ind + "        self._parentMessage = parentMessage\n" +
            ind + "        self._count = count\n" +
            ind + "        limit: int = self._parentMessage.limit\n" +
            ind + "        self._parentMessage.set_limit(limit + self.HEADER_SIZE)\n" +
            ind + "        %5$s\n" +
            ind + "        %6$s\n" +
            ind + "    \n\n",
            parentMessageClassName,
            mutableBuffer,
            numInGroupToken.encoding().applicableMinValue().longValue(),
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            blockLengthPut,
            numInGroupPut));

        sb.append(ind).append("    @property\n")
            .append(ind).append("    def sbeHeaderSize(self) -> int:\n")
            .append(ind).append("        return self.HEADER_SIZE\n");

        sb.append(String.format("\n" +
            ind + "    @property\n" +
            ind + "    def sbeBlockLength(self) -> int:\n" +
            ind + "        return %d\n",
            blockLength));

        sb.append(String.format("\n" +
            ind + "    def next(self) -> '%s':\n" +
            ind + "        if self._index + 1 >= self._count:\n" +
            ind + "            raise IndexError('Index ' + str(self._index) + 'out of bounds ' + str(self._count))\n" +
            ind + "        \n\n" +
            ind + "        self._offset= self._parentMessage.limit\n" +
            ind + "        self._parentMessage.set_limit(self._offset + self.sbeBlockLength)\n" +
            ind + "        self._index += 1\n\n" +
            ind + "        return self\n" +
            ind + "    \n",
            hierarchyName));
    }

    private static String primitiveTypeName(final Token token)
    {
        return pythonTypeName(token.encoding().primitiveType());
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
            indent + "class %2$s:\n" +
            indent + "    HEADER_SIZE: int = %3$d\n" +
            indent + "    _parentMessage: '%4$s'\n" +
            indent + "    _buffer: %5$s = None\n" +
            indent + "    _count: int\n" +
            indent + "    _index: int\n" +
            indent + "    _offset: int\n" +
            indent + "    _blockLength: int\n",
            generatePyDoc(indent, groupToken),
            className,
            dimensionHeaderSize,
            parentMessageClassName,
            readOnlyBuffer));

    }

    private void generateGroupEncoderClassDeclaration(
        final StringBuilder sb,
        final Token groupToken,
        final String groupName,
        final String parentMessageClassName,
        final String fullyQualifiedName,
        final List<String> subGroupNames,
        final String indent,
        final int dimensionHeaderSize)
    {
        final String className = formatClassName(encoderName(groupName));

        sb.append(String.format("\n" +
            "%1$s" +
            indent + "class %2$s:\n" +
            indent + "    HEADER_SIZE: int = %3$d\n",
            generatePyDoc(indent, groupToken),
            className,
            dimensionHeaderSize));


        sb
            .append("\n")
            .append(indent).append("    ")
            .append("def __init__(self):\n")
            .append(String.format(
            indent + "        self._parentMessage: '%1$s' = None\n" +
            indent + "        self._buffer: '%2$s' = None\n" +
            indent + "        self._count: int = None\n" +
            indent + "        self._index: int = None\n" +
            indent + "        self._offset: int = None\n",
            parentMessageClassName,
            mutableBuffer));

        for (final String subGroupName : subGroupNames)
        {
            final String type = formatClassName(encoderName(subGroupName));
            final String field = formatPropertyName(subGroupName);
            sb.append(indent).append("        self._").append(field).append(": '").append(fullyQualifiedName)
                .append(".").append(type).append("' = ").append("self").append('.').append(type).append("()\n");
        }

        sb.append("\n\n");
    }

    private static CharSequence generateGroupDecoderProperty(
        final String groupName, final String fullyQualifiedName, final Token token, final String indent,
        final boolean isSubGroup)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(token.name());

        sb.append(String.format("\n" +
            indent + "    _%s: %s = %s()\n",
            propertyName,
            className,
            className));

        sb.append(String.format("\n" +
            indent + "    def %sId(self) -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(groupName),
            token.id()));

        sb.append(String.format("\n" +
            indent + "    def %sSinceVersion(self) -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(groupName),
            token.version()));

        final String actingVersionGuard = token.version() == 0 ?
            "" :
            indent + "        if parentMessage.actingVersion < " + token.version() + ":\n" +
            indent + "            " + propertyName + ".count = 0\n" +
            indent + "            " + propertyName + ".index = -1\n" +
            indent + "            return " + propertyName + "\n" +
            indent + "        \n\n";

        sb.append(String.format("\n" +
            indent + "    def %2$s(self) -> %1$s:\n" +
            "%3$s" +
            indent + "        self._%2$s.wrap(self._buffer, self._parentMessage)\n" +
            indent + "        return self._%2$s\n" +
            indent + "    \n",
            className,
            propertyName,
            actingVersionGuard));

        return sb;
    }

    private CharSequence generateGroupEncoderProperty(
        final String groupName, final String fullyQualifiedName, final Token token, final String indent,
        final boolean isSubGroup)
    {
        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(encoderName(groupName));
        final String propertyName = formatPropertyName(groupName);

        if (!isSubGroup)
        {
            sb.append(String.format("\n" +
                indent + "    _%s: '%s' = %s()\n",
                propertyName,
                className,
                className));
        }

        sb.append(String.format("\n" +
            indent + "    def %sId(self) -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(groupName),
            token.id()));

        sb.append(String.format("\n" +
            indent + "    def %2$sCount(self, count: int) -> '%1$s':\n" +
            indent + "        self._%2$s.wrap(self._buffer, self._parentMessage, count)\n" +
            indent + "        return self._%2$s\n" +
            indent + "    \n",
            className,
            propertyName));

        return sb;
    }

    private CharSequence generateDecoderVarData(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; )
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
                indent + "    @staticmethod\n" +
                indent + "    def %sHeaderLength() -> int:\n" +
                indent + "        return %d\n" +
                indent + "    \n",
                Generators.toLowerFirstChar(propertyName),
                sizeOfLengthField));

            sb.append(String.format("\n" +
                indent + "    def %sLength(self) -> int:\n" +
                "%s" +
                indent + "        limit: int = self._parentMessage.limit\n" +
                indent + "        return %s\n" +
                indent + "    \n",
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

        for (int i = 0, size = tokens.size(); i < size; )
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

            sb.append(String.format("\n" +
                indent + "    def %sHeaderLength(self) -> int:\n" +
                indent + "        return %d\n" +
                indent + "    \n",
                Generators.toLowerFirstChar(propertyName),
                sizeOfLengthField));

            generateDataEncodeMethods(
                sb,
                propertyName,
                sizeOfLengthField,
                maxLengthValue,
                lengthEncoding.primitiveType(),
                lengthEncoding,
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

        generateVarDataWrapDecoder(sb, token, propertyName, sizeOfLengthField, lengthType, byteOrderStr, indent);

        if (null != characterEncoding)
        {
            sb.append(String.format("\n" +
                indent + "    def %1$s(self) -> str:\n" +
                "%2$s" +
                indent + "        headerLength: int = %3$d\n" +
                indent + "        limit: int = self._parentMessage.limit\n" +
                indent + "        dataLength: int = %4$s\n" +
                indent + "        self._parentMessage.set_limit(limit + headerLength + dataLength)\n\n" +
                indent + "        if 0 == dataLength:\n" +
                indent + "            return \"\"\n" +
                indent + "        \n\n" +
                indent + "        return memoryview(self._buffer)[limit + headerLength:limit + " +
                "headerLength + dataLength].tobytes()." +
                "decode(\"%5$s\")\n" +
                indent + "\n",
                formatPropertyName(propertyName),
                generateStringNotPresentCondition(token.version(), indent),
                sizeOfLengthField,
                generateGet(lengthType, "limit", byteOrderStr),
                characterEncoding));
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
            indent + "    def wrap%s(self) -> memoryview:\n" +
            "%s" +
            indent + "        headerLength: int = %d\n" +
            indent + "        limit: int = self._parentMessage.limit\n" +
            indent + "        dataLength: int = %s\n" +
            indent + "        self._parentMessage.set_limit(limit + headerLength + dataLength)\n" +
            indent + "        limit: int = self._parentMessage.limit\n" +
            indent + "        self._parentMessage.set_limit(limit + headerLength + dataLength)\n" +
            indent + "        return memoryview(self._buffer)[limit + headerLength:limit + headerLength + dataLength]" +
            "\n" +
            indent + "    \n",
            propertyName,
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
        final Encoding encoding,
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
            encoding,
            indent);

        if (null != characterEncoding)
        {
            generateCharArrayEncodeMethods(
                sb,
                propertyName,
                sizeOfLengthField,
                maxLengthValue,
                lengthType,
                encoding,
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
        final Encoding encoding,
        final String characterEncoding,
        final String className,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "    def %1$s(self, data: Union[bytes, bytearray, memoryview]) -> '%7$s':\n" +
            indent + "        size_of_len_field: int = %2$d\n" +
            indent + "        limit:int = self._parentMessage.limit\n" +
            indent + "        self._parentMessage.set_limit(limit + size_of_len_field + len(data))\n" +
            indent + "        data_len: int = len(data)\n" +
            indent + "        %5$s\n" +
            indent + "        struct.pack_into('%6$s%%ds' %% (data_len,), self._buffer, limit + size_of_len_field, " +
            "data)\n" +
            indent + "        return self\n\n",
            formatPropertyName(propertyName),
            sizeOfLengthField,
            pythonTypeName(lengthType),
            byteOrderString(encoding),
            generatePut(lengthType, "limit", "data_len", byteOrderString(encoding)),
            pythonEndianCode(encoding.byteOrder()),
            className));
    }

    private void generateDataTypedEncoder(
        final StringBuilder sb,
        final String className,
        final String propertyName,
        final int sizeOfLengthField,
        final int maxLengthValue,
        final String exchangeType,
        final PrimitiveType lengthType,
        final Encoding encoding,
        final String indent)
    {
        //TODO: FIX
        sb.append(String.format("\n" +
            indent + "    def put%2$s(self, src: %3$s, srcOffset: int, length: int) -> '%1$s':\n" +
            indent + "        if length > %4$d:\n" +
            indent + "            raise OverflowError(\"length > maxValue for type: \" + length)\n" +
            indent + "        \n\n" +
            indent + "        headerLength: int = %5$d\n" +
            indent + "        limit:  int = self._parentMessage.limit\n" +
            indent + "        self._parentMessage.set_limit(limit + headerLength + length)\n" +
            indent + "        %6$s\n" +
            indent + "        return self\n" +
            indent + "    \n",
            className,
            propertyName,
            exchangeType,
            maxLengthValue,
            sizeOfLengthField,
            generatePut(lengthType, "limit", "length", byteOrderString(encoding))));
    }

    private void generateBitSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final List<Token> msgBody = getMessageBody(tokens);
        final String bitSetName = formatClassName(token.applicableTypeName());
        final String decoderName = decoderName(bitSetName);
        final String encoderName = encoderName(bitSetName);
        final String encoderModuleName = formatModuleName(encoderName);
        final String decoderModuleName = formatModuleName(decoderName);

        final List<Token> messageBody = getMessageBody(tokens);
        final String implementsString = implementsInterface(FLYWEIGHT);

        try (Writer out = outputManager.createOutput(decoderModuleName))
        {
            generateFixedFlyweightHeader(token, decoderName, implementsString, out, readOnlyBuffer, fqReadOnlyBuffer,
                msgBody, CodecType.DECODER);
            out.append(generateChoiceIsEmpty(token.encoding().primitiveType(), token.encoding().byteOrder()));
            out.append(generateChoiceDecoders(messageBody));
            out.append(generateChoiceDisplay(messageBody));
        }

        try (Writer out = outputManager.createOutput(encoderModuleName))
        {
            generateFixedFlyweightHeader(token, encoderName, implementsString, out, mutableBuffer, fqMutableBuffer,
                msgBody, CodecType.ENCODER);
            out.append(generateChoiceClear(encoderName, token));
            out.append(generateChoiceEncoders(encoderName, messageBody));
        }
    }

    private void generateFixedFlyweightHeader(
        final Token token,
        final String typeName,
        final String implementsString,
        final Writer out,
        final String buffer,
        final String fqBuffer,
        final List<Token> tokens,
        final CodecType codecType) throws IOException
    {
        out.append(generateFileHeader(ir.applicableNamespace(), fqBuffer, tokens, codecType));
        out.append(generateDeclaration(typeName, implementsString, token));
        out.append(generateFixedFlyweightCode(typeName, token.encodedLength(), buffer));
    }

    private void generateCompositeFlyweightHeader(
        final Token token,
        final String typeName,
        final Writer out,
        final String buffer,
        final String fqBuffer,
        final String implementsString,
        final List<Token> tokens,
        final CodecType codecType) throws IOException
    {
        out.append(generateFileHeader(ir.applicableNamespace(), fqBuffer, tokens, codecType));
        out.append(generateDeclaration(typeName, implementsString, token));
        out.append(generateFixedFlyweightCode(typeName, token.encodedLength(), buffer));
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(enumToken.applicableTypeName());
        final String moduleName = formatModuleName(enumName);


        try (Writer out = outputManager.createOutput(moduleName))
        {
            out.append(generateEnumFileHeader(ir.applicableNamespace()));
            out.append(generateEnumDeclaration(enumName, enumToken));

            out.append(generateEnumValues(getMessageBody(tokens)));
            out.append(generateEnumBody(enumToken, enumName));

            out.append(generateEnumLookupMethod(getMessageBody(tokens), enumName));
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final List<Token> msgBody = getMessageBody(tokens);
        final String compositeName = formatClassName(token.applicableTypeName());
        final String decoderName = decoderName(compositeName);
        final String encoderName = encoderName(compositeName);
        final String encoderModuleName = formatModuleName(encoderName);
        final String decoderModuleName = formatModuleName(decoderName);

        try (Writer out = outputManager.createOutput(decoderModuleName))
        {
            final String implementsString = implementsInterface(COMPOSITE_DECODER_FLYWEIGHT);
            generateCompositeFlyweightHeader(
                token, decoderName, out, readOnlyBuffer, fqReadOnlyBuffer, implementsString, msgBody,
                CodecType.DECODER);
            for (int i = 1, end = tokens.size() - 1; i < end; )
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
                            true, CodecType.DECODER, propertyName, encodingToken, encodingToken,
                            BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(sb).append(generateCompositeProperty(
                            true, CodecType.DECODER, propertyName, encodingToken, encodingToken,
                            BASE_INDENT, typeName));
                        break;
                }
                i += encodingToken.componentTokenCount();
            }
            out.append(generateCompositeDecoderDisplay(tokens, BASE_INDENT));
        }

        try (Writer out = outputManager.createOutput(encoderModuleName))
        {
            final String implementsString = implementsInterface(COMPOSITE_ENCODER_FLYWEIGHT);
            generateCompositeFlyweightHeader(token, encoderName, out, mutableBuffer, fqMutableBuffer, implementsString,
                msgBody, CodecType.ENCODER);

            for (int i = 1, end = tokens.size() - 1; i < end; )
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
                            true, CodecType.ENCODER, propertyName, encodingToken, encodingToken,
                            BASE_INDENT, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        out.append(sb).append(generateCompositeProperty(
                            true, CodecType.ENCODER, propertyName, encodingToken, encodingToken,
                            BASE_INDENT, typeName));
                        break;
                }

                i += encodingToken.componentTokenCount();
            }

        }
    }

    private CharSequence generateChoiceClear(final String bitSetClassName, final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final String literalValue = generateLiteral(encoding.primitiveType(), "0");
        final String byteOrderStr = byteOrderString(encoding);

        sb.append(String.format("\n" +
            "    def clear(self) -> '%s':\n" +
            "        %s\n" +
            "        return self\n" +
            "    \n",
            bitSetClassName,
            generatePut(encoding.primitiveType(), "self._offset", literalValue, byteOrderStr)));

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
                        "    def %1$s(self) -> bool:\n" +
                        "        return %2$s\n" +
                        "    \n\n" +
                        "    @staticmethod\n" +
                        "    def apply_%1$s(value: %3$s) -> bool:\n" +
                        "        return %4$s\n" +
                        "    \n",
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
                        "    def %2$s(self, value: bool) -> '%1$s':\n" +
                        "%3$s\n" +
                        "        return self\n" +
                        "    \n\n" +
                        "    @staticmethod\n" +
                        "    def apply_%2$s(bits: %4$s, value: bool) -> '%4$s':\n" +
                        "%5$s" +
                        "    \n",
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
            case UINT16:
            case UINT32:
            case UINT64:
                return "int";

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
            final CharSequence constVal = generateLiteral(PrimitiveType.INT32, encoding.constValue().toString());
            sb.append(INDENT).append(token.name()).append(" = ").append(constVal).append("\n");
        }

        final Token token = tokens.get(0);
        final Encoding encoding = token.encoding();
        final CharSequence nullVal = generateLiteral(PrimitiveType.INT32, encoding.applicableNullValue().toString());

        if (shouldDecodeUnknownEnumValues)
        {
            sb.append(INDENT).append("\"\"\"\n");
            sb.append(INDENT).append(" To be used to represent a not known value from a later version.\n");
            sb.append(INDENT).append("\"\"\"\n");
            sb.append(INDENT).append("SBE_UNKNOWN").append(" = ").append(nullVal).append("\n");
        }

        sb.append(INDENT).append("\"\"\"\n");
        sb.append(INDENT).append(" To be used to represent not present or null.\n");
        sb.append(INDENT).append("\"\"\"\n");
        sb.append(INDENT).append("NULL_VAL").append(" = ").append(nullVal).append("\n\n");

        return sb;
    }

    private CharSequence generateEnumBody(final Token token, final String enumName)
    {
        return "";
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final String enumName)
    {
        final StringBuilder sb = new StringBuilder();

        final PrimitiveType primitiveType = PrimitiveType.INT32;
        sb.append(String.format(
            INDENT + "@staticmethod\n" +
            INDENT + "def get(value: %s) -> '%s':\n",
            pythonTypeName(primitiveType),
            enumName));

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(PrimitiveType.INT32,
                token.encoding().constValue().toString());
            sb.append(String.format(
                INDENT + INDENT + "if value == %1$s: return " + enumName + ".%2$s\n",
                constVal,
                token.name()));
        }

        final String handleUnknownLogic = shouldDecodeUnknownEnumValues ?
            INDENT + INDENT + "return " + enumName + ".SBE_UNKNOWN\n" :
            INDENT + INDENT + "raise KeyError(\"Unknown value: \" + str(value))\n";

        sb.append(String.format(
            INDENT + INDENT + "if %1$s == value:\n" +
            INDENT + INDENT + INDENT + "return " + enumName + ".NULL_VAL\n" +
            "%2$s",
            generateLiteral(primitiveType, tokens.get(0).encoding().applicableNullValue().toString()),
            handleUnknownLogic));

        return sb;
    }

    private CharSequence interfaceImportLine(final List<Token> tokens, final CodecType codecType,
        final boolean includeHeader)
    {
        if (shouldGenerateInterfaces)
        {
            throw new RuntimeException("Interfaced not supported in python codec");
        }
        final StringBuilder sb = new StringBuilder();
        final Set<String> processed = new HashSet<>();
        sb.append("from typing import *\n");
        sb.append("import struct\n");
        sb.append("from ._struct_defs import *\n");
        sb.append("from .meta_attribute import MetaAttribute\n");
        if (includeHeader)
        {
            switch (codecType)
            {
                case ENCODER:
                    sb.append("from .message_header_encoder import MessageHeaderEncoder\n");
                    break;
                case DECODER:
                    sb.append("from .message_header_decoder import MessageHeaderDecoder\n");
            }
        }
        for (final Token t : tokens)
        {
            if (t.signal() == Signal.BEGIN_COMPOSITE || t.signal() == Signal.BEGIN_SET ||
                t.signal() == Signal.BEGIN_ENUM)
            {
                String importName = formatClassName(t.applicableTypeName());
                if (processed.contains(importName))
                {
                    continue;
                }
                if (t.signal() == Signal.BEGIN_ENUM)
                {
                    sb.append("from .").append(camToSnake(importName))
                        .append(" import ").append(importName).append("\n");
                }
                else
                {
                    importName = codecType == CodecType.ENCODER ? encoderName(importName) : decoderName(importName);
                    sb.append("from .").append(camToSnake(importName)).append(" import ").append(importName)
                        .append("\n");
                }
                processed.add(importName);
            }
        }
        sb.append("\n\n");
        return sb;
    }

    private CharSequence generateFileHeader(final String packageName, final String fqBuffer, final List<Token> tokens,
        final CodecType codecType)
    {
        return String.format(
            "\"\"\" Generated SBE (Simple Binary Encoding) message codec \"\"\"\n" +
                "%s",
            interfaceImportLine(tokens, codecType, false));
    }

    private CharSequence generateMainHeader(final String packageName, final List<Token> tokens,
        final CodecType codecType)
    {
        if (fqMutableBuffer.equals(fqReadOnlyBuffer))
        {
            return String.format(
                "\"\"\" Generated SBE (Simple Binary Encoding) message codec \"\"\"\n" +
                    "%s",
                interfaceImportLine(tokens, codecType, true));
        }
        else
        {
            return String.format(
                "\"\"\" Generated SBE (Simple Binary Encoding) message codec \"\"\"\n" +
                    "%s",
                interfaceImportLine(tokens, codecType, true));
        }
    }

    private static CharSequence generateEnumFileHeader(final String packageName)
    {
        return "\"\"\" Generated SBE (Simple Binary Encoding) message codec \"\"\"\n\n" +
            "from enum import Enum\n\n\n";
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
            "class %s:\n",
            className);
    }

    private void generateMetaAttributeEnum() throws IOException
    {
        try (Writer out = outputManager.createOutput(formatModuleName(META_ATTRIBUTE_ENUM)))
        {
            out.append("\"\"\"Generated SBE (Simple Binary Encoding) message codec \"\"\"\n\n");
            out.append(
                "from enum import IntEnum \n\n\n" +
                "class MetaAttribute(IntEnum):\n" +
                "    Epoch = 1\n" +
                "    TimeUnit = 2\n" +
                "    SemanticType = 3\n" +
                "    Presence = 4\n");
        }
    }

    private static CharSequence generateEnumDeclaration(final String name, final Token typeToken)
    {
        return
            "class " + name + "(Enum):\n";
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
        final String pythonTypeName = pythonTypeName(primitiveType);
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(String.format("\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sNullValue() -> '%1$s':\n" +
            indent + "        return %3$s\n" +
            indent + "    \n",
            pythonTypeName,
            formattedPropertyName,
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString())));

        sb.append(String.format("\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sMinValue() -> '%1$s':\n" +
            indent + "        return %3$s\n" +
            indent + "    \n",
            pythonTypeName,
            formattedPropertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())));

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sMaxValue() -> '%1$s':\n" +
            indent + "        return %3$s\n" +
            indent + "    \n",
            pythonTypeName,
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
        final String javaTypeName = pythonTypeName(encoding.primitiveType());

        final int offset = encodingToken.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            "\n" +
                indent + "    def %s(self) -> '%s':\n" +
                "%s" +
                indent + "        return %s\n" +
                indent + "    \n\n",
            formatPropertyName(propertyName),
            javaTypeName,
            generateFieldNotPresentCondition(inComposite, propertyToken.version(), encoding, indent),
            generateGet(encoding.primitiveType(), "self._offset + " + offset, byteOrderStr));
    }

    private CharSequence generatePrimitivePropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String javaTypeName = pythonTypeName(encoding.primitiveType());
        final int offset = token.offset();
        final String byteOrderStr = byteOrderString(encoding);

        return String.format(
            "\n" +
                indent + "    def %s(self, value: %s) -> '%s':\n" +
                indent + "        %s\n" +
                indent + "        return self\n" +
                indent + "    \n\n",
            formatPropertyName(propertyName),
            javaTypeName,
            formatClassName(containingClassName),
            generatePut(encoding.primitiveType(), "self._offset + " + offset, "value", byteOrderStr));
    }

    private CharSequence generateVarWrapFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            self._wrapBuffer.wrap(buffer, offset, 0)\n" +
                indent + "        \n\n",
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
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            return %s\n" +
                indent + "        \n\n",
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
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            return 0\n" +
                indent + "        \n\n",
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
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            return\n" +
                indent + "        \n\n",
            sinceVersion);
    }

    private static CharSequence generateStringNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            return \"\"\n" +
                indent + "        \n\n",
            sinceVersion);
    }

    private static CharSequence generatePropertyNotPresentCondition(
        final boolean inComposite,
        final CodecType codecType,
        final Token propertyToken,
        final String enumName,
        final String indent)
    {
        if (inComposite || codecType == CodecType.ENCODER || 0 == propertyToken.version())
        {
            return "";
        }

        return String.format(
            indent + "        if parentMessage.actingVersion < %d:\n" +
                indent + "            return %s\n" +
                indent + "        \n\n",
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
        final String typeName = pythonTypeName(encoding.primitiveType());
        final int offset = encodingToken.offset();
        final String byteOrderStr = byteOrderString(encoding);
        final String typePrefix = toUpperFirstChar(encoding.primitiveType().primitiveName());
        final int fieldLength = encodingToken.arrayLength();
        final int typeSize = sizeOfPrimitive(encoding);

        final StringBuilder sb = new StringBuilder();

        generateArrayLengthMethod(propertyName, indent, fieldLength, sb);
        sb.append(String.format(
            indent + "    def %s(self, index: int) -> '%s':\n" +
            indent + "        if index < 0 or index >= %d:\n" +
            indent + "            raise IndexError(\"index out of range: index=\" + str(index))\n" +
            "%s" +
            indent + "        pos: int = self._offset + %d + (index * %d)\n\n" +
            indent + "        return %s\n" +
            indent + "    \n\n",
            propertyName,
            typeName,
            fieldLength,
            generateFieldNotPresentCondition(inComposite, propertyToken.version(), encoding, indent),
            offset,
            typeSize,
            generateGet(encoding.primitiveType(), "pos", byteOrderStr)));


        final String structParserMulti = String.format("A_%1$S_%2$S_%3$S", Generators.toUpperFirstChar(propertyName),
            typePrefix, byteOrderStr);

        sb.append(String.format("\n" +
            INDENT + structParserMulti + " = Struct('%1$s%2$d%3$s') \n",
            pythonEndianCode(encoding.byteOrder()), fieldLength,
            pythonTypeCode(encoding.primitiveType())));

        if (encoding.primitiveType() == PrimitiveType.CHAR)
        {
            generateCharacterEncodingMethod(sb, propertyName, encoding.characterEncoding(), indent);
            //Properties for multi item assignment
            sb.append(String.format("\n" +
                INDENT + "def getMulti%2$s(self) -> bytes:\n" +
                "%4$s" +
                INDENT + "    return self." + structParserMulti + ".unpack_from(self._buffer, self._offset + %5$d)[0]" +
                "\n",
                typeName, Generators.toUpperFirstChar(propertyName), fieldLength,
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                offset, typeSize));
        }
        else
        {
            //Properties for multi item assignment
            sb.append(String.format("\n" +
                INDENT + "def getMulti%2$s(self) -> Tuple['%1$s']:\n" +
                "%4$s" +
                INDENT + "    return self." + structParserMulti + ".unpack_from(self._buffer, self._offset + %5$d)\n",
                typeName, Generators.toUpperFirstChar(propertyName), fieldLength,
                generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
                offset, typeSize));
        }


        return sb;
    }

    private static void generateArrayLengthMethod(
        final String propertyName, final String indent, final int fieldLength, final StringBuilder sb)
    {
        sb.append(String.format("\n" +
            indent + "    def %sLength(self) -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n\n",
            formatPropertyName(propertyName),
            fieldLength));
    }


    private CharSequence generatePrimitiveArrayPropertyEncode(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String javaTypeName = pythonTypeName(primitiveType);
        final String typeName = pythonTypeName(encoding.primitiveType());
        final String byteOrderStr = byteOrderString(encoding);
        final int offset = token.offset();
        final int arrayLength = token.arrayLength();
        final int typeSize = sizeOfPrimitive(encoding);
        final String typePrefix = toUpperFirstChar(encoding.primitiveType().primitiveName());

        final StringBuilder sb = new StringBuilder();
        final String className = formatClassName(containingClassName);
        final String structParserMulti = String.format("A_%1$S_%2$S_%3$S", Generators.toUpperFirstChar(propertyName),
            typePrefix, byteOrderStr);

        sb.append(String.format("\n" +
            INDENT + structParserMulti + " = Struct('%1$s%2$d%3$s') \n",
            pythonEndianCode(encoding.byteOrder()), token.arrayLength(),
            pythonTypeCode(encoding.primitiveType())));

        generateArrayLengthMethod(propertyName, indent, arrayLength, sb);

        sb.append(String.format(
            indent + "    def %s(self, index: int, value: '%s') -> '%s':\n" +
            indent + "        if index < 0 or index >= %d:\n" +
            indent + "            raise IndexError(\"index out of range: index=\" + str(index))\n" +
            indent + "        \n" +
            indent + "        pos: int = self._offset + %d + (index * %d)\n" +
            indent + "        %s\n\n" +
            indent + "        return self\n" +
            indent + "    \n",
            propertyName,
            javaTypeName,
            className,
            arrayLength,
            offset,
            typeSize,
            generatePut(primitiveType, "pos", "value", byteOrderStr)));

        sb.append(String.format("\n" +
            INDENT + "def set_%1$s(self, value: %2$s) -> '%4$s':\n" +
            INDENT + "    self." + structParserMulti + ".pack_into(self._buffer, self._offset + %3$d, value)\n" +
            INDENT + "    return self\n",
            propertyName, typeName, offset, containingClassName));

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
            indent + "        FIX: final int length = %d;\n" +
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
                indent + "        FIX: final int length = %3$d;\n" +
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
                encoding.characterEncoding(),
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
                indent + "    @staticmethod\n" +
                indent + "    def %sCharacterEncoding()  -> str:\n" +
                indent + "        return \"%s\"\n" +
                indent + "    \n",
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
                    indent + "    @property\n" +
                    indent + "    def %s(self) -> '%s':\n" +
                    indent + "        return %s\n" +
                    indent + "    \n",
                formattedPropertyName,
                pythonTypeName(encoding.primitiveType()),
                generateLiteral(encoding.primitiveType(), encoding.constValue().toString()));
        }

        final StringBuilder sb = new StringBuilder();

        final String javaTypeName = pythonTypeName(encoding.primitiveType());
        final byte[] constBytes = encoding.constValue().byteArrayValue(encoding.primitiveType());
        final CharSequence values = generateByteLiteralList(
            encoding.constValue().byteArrayValue(encoding.primitiveType()));

        sb.append(String.format(
            "\n" +
            indent + "    %s_VALUE:  bytes = bytes([%s]) \n",
            propertyName.toUpperCase(),
            values));

        generateArrayLengthMethod(formattedPropertyName, indent, constBytes.length, sb);

        sb.append(String.format(
            indent + "    def %s(self, index: int)  -> int:\n" +
            indent + "        return self.%s_VALUE[index]\n" +
            indent + "    \n\n",
            formattedPropertyName,
            propertyName.toUpperCase()));

        sb.append(String.format(
            indent + "    def get%1$s(self, offset: int, length: int) -> memoryview:\n" +
            indent + "        last = min(length, %2$d)\n" +
            indent + "        offset = min(abs(offset), %3$d)\n" +
            indent + "        return memoryview(self.%4$S_VALUE)[offset:last]\n",
            Generators.toUpperFirstChar(propertyName),
            constBytes.length,
            constBytes.length - 1,
            propertyName));


        if (constBytes.length > 1)
        {
            sb.append(String.format("\n" +
                indent + "    @staticmethod\n" +
                indent + "    def get%sAll() -> str:\n" +
                indent + "    \n" +
                indent + "         return \"%s\"\n" +
                indent + "    \n\n",
                toUpperFirstChar(formattedPropertyName),
                encoding.constValue()));
        }
        else
        {
            sb.append(String.format("\n" +
                indent + "    @staticmethod\n" +
                indent + "    def %s() -> int:\n" +
                indent + "    \n" +
                indent + "        return %s\n" +
                indent + "    \n\n",
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
        final String schemaIdType = pythonTypeName(ir.headerStructure().schemaIdType());
        final String schemaIdAccessorType = shouldGenerateInterfaces ? "int" : schemaIdType;
        final String schemaVersionType = pythonTypeName(ir.headerStructure().schemaVersionType());
        final String schemaVersionAccessorType = shouldGenerateInterfaces ? "int" : schemaVersionType;

        return String.format(
            "    SCHEMA_ID: %5$s = %6$s\n" +
                "    SCHEMA_VERSION: %7$s = %8$s\n" +
                "    ENCODED_LENGTH: int = %2$d\n" +
                "    _offset: int = 0\n" +
                "    _buffer: %3$s = None\n\n" +
                "    def wrap(self, buffer: %3$s, offset: int) -> '%1$s':\n" +
                "        if buffer != self._buffer:\n" +
                "            self._buffer = buffer\n" +
                "        \n" +
                "        self._offset = offset\n\n" +
                "        return self\n" +
                "    \n\n" +
                "    @property\n" +
                "    def buffer(self) -> '%3$s':\n" +
                "    \n" +
                "        return self._buffer\n" +
                "    \n\n" +
                "    @property\n" +
                "    def offset(self) -> int:\n" +
                "        return self._offset\n" +
                "    \n\n" +
                "    @property\n" +
                "    def encodedLength(self) ->int:\n" +
                "        return self.ENCODED_LENGTH\n" +
                "    \n\n" +
                "    @property\n" +
                "    def sbeSchemaId(self) -> '%9$s':\n" +
                "        return self.SCHEMA_ID\n" +
                "    \n\n" +
                "    def sbeSchemaVersion(self) -> '%10$s':\n" +
                "        return self.SCHEMA_VERSION\n" +
                "    \n",
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
            "    def wrap(self, buffer: %2$s, offset: int, actingBlockLength: int, actingVersion: int) -> '%1$s':\n" +
            "        if buffer != self._buffer:\n" +
            "            self._buffer = buffer\n" +
            "        \n" +
            "        self._offset = offset\n" +
            "        self._actingBlockLength = actingBlockLength\n" +
            "        self._actingVersion = actingVersion\n" +
            "        self.set_limit(offset + actingBlockLength)\n\n" +
            "        return self\n" +
            "    \n\n",
            className,
            readOnlyBuffer);

        return generateFlyweightCode(CodecType.DECODER, className, token, wrapMethod, readOnlyBuffer);
    }

    private CharSequence generateFlyweightCode(
        final CodecType codecType,
        final String className,
        final Token token,
        final String wrapMethod,
        final String bufferImplementation)
    {
        final HeaderStructure headerStructure = ir.headerStructure();
        final String blockLengthType = pythonTypeName(headerStructure.blockLengthType());
        final String blockLengthAccessorType = shouldGenerateInterfaces ? "int" : blockLengthType;
        final String templateIdType = pythonTypeName(headerStructure.templateIdType());
        final String templateIdAccessorType = shouldGenerateInterfaces ? "int" : templateIdType;
        final String schemaIdType = pythonTypeName(headerStructure.schemaIdType());
        final String schemaIdAccessorType = shouldGenerateInterfaces ? "int" : schemaIdType;
        final String schemaVersionType = pythonTypeName(headerStructure.schemaVersionType());
        final String schemaVersionAccessorType = shouldGenerateInterfaces ? "int" : schemaVersionType;
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();
        final String actingFields = codecType == CodecType.ENCODER ?
            "" :
            "       self._actingBlockLength: int = None\n" +
            "       self._actingVersion: int = None\n";

        return String.format(
            "    BLOCK_LENGTH: %1$s = %2$s\n" +
            "    TEMPLATE_ID: %3$s = %4$s\n" +
            "    SCHEMA_ID: %5$s = %6$s\n" +
            "    SCHEMA_VERSION: %7$s = %8$s\n" +
            "    def __init__(self):\n" +
            "       self._parentMessage: '%9$s' = self\n" +
            "       self._buffer: %11$s = None\n" +
            "       self._offset: int = None\n" +
            "       self._limit: int = None\n" +
            "%13$s" +
            "\n" +
            "    @property\n" +
            "    def sbeBlockLength(self) -> %15$s:\n" +
            "        return self.BLOCK_LENGTH\n" +
            "    \n\n" +
            "    @property\n" +
            "    def sbeTemplateId(self) -> %16$s:\n" +
            "        return self.TEMPLATE_ID\n" +
            "    \n\n" +
            "    @property\n" +
            "    def sbeSchemaId(self) -> %17$s:\n" +
            "        return self.SCHEMA_ID\n" +
            "    \n\n" +
            "    @property\n" +
            "    def sbeSchemaVersion(self) -> %18$s:\n" +
            "        return self.SCHEMA_VERSION\n" +
            "    \n\n" +
            "    @property\n" +
            "    def sbeSemanticType(self) -> str:\n" +
            "        return \"%10$s\"\n" +
            "    \n\n" +
            "    @property\n" +
            "    def buffer(self) -> %11$s:\n" +
            "        return self._buffer\n" +
            "    \n\n" +
            "    @property\n" +
            "    def offset(self) -> int:\n" +
            "        return self._offset\n" +
            "    \n\n" +
            "%12$s" +
            "    @property\n" +
            "    def encodedLength(self) -> int:\n" +
            "        return self._limit - self._offset\n" +
            "    \n\n" +
            "    @property\n" +
            "    def limit(self) -> int:\n" +
            "        return self._limit\n" +
            "    \n\n" +
            "    def set_limit(self, limit: int):\n" +
            "        self._limit = limit\n" +
            "    \n",
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
            "    def wrap(self, buffer: %2$s , offset: int) -> '%1$s':\n" +
            "        if buffer != self._buffer:\n" +
            "            self._buffer = buffer\n" +
            "        \n" +
            "        self._offset = offset\n" +
            "        self.set_limit(offset + self.BLOCK_LENGTH)\n\n" +
            "        return self\n" +
            "    \n\n",
            className,
            mutableBuffer);

        final StringBuilder builder = new StringBuilder(
            "    def wrapAndApplyHeader(self, buffer: %2$s, offset: int, headerEncoder: '%3$s') -> '%1$s':\n" +
            "        headerEncoder.wrap(buffer, offset)");

        for (final Token headerToken : ir.headerStructure().tokens())
        {
            if (!headerToken.isConstantEncoding())
            {
                switch (headerToken.name())
                {
                    case "blockLength":
                        builder.append(".blockLength(self.BLOCK_LENGTH)");
                        break;

                    case "templateId":
                        builder.append(".templateId(self.TEMPLATE_ID)");
                        break;

                    case "schemaId":
                        builder.append(".schemaId(self.SCHEMA_ID)");
                        break;

                    case "version":
                        builder.append(".version(self.SCHEMA_VERSION)");
                        break;
                }
            }
        }

        builder.append("\n\n        return self.wrap(buffer, offset + %3$s.ENCODED_LENGTH)\n\n");

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
                            false, CodecType.ENCODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(
                            false, CodecType.ENCODER, propertyName, fieldToken, typeToken, indent, typeName));
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
                            false, CodecType.DECODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(
                            false, CodecType.DECODER, propertyName, fieldToken, typeToken, indent, typeName));
                        break;
                }
            });

        return sb;
    }

    private static void generateFieldIdMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %sId() -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(token.name()),
            token.id()));
    }

    private static void generateEncodingOffsetMethod(
        final StringBuilder sb, final String name, final int offset, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %sEncodingOffset() -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(name),
            offset));
    }

    private static void generateEncodingLengthMethod(
        final StringBuilder sb, final String name, final int length, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %sEncodingLength() -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
            formatPropertyName(name),
            length));
    }

    private static void generateFieldSinceVersionMethod(final StringBuilder sb, final Token token, final String indent)
    {
        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %sSinceVersion() -> int:\n" +
            indent + "        return %d\n" +
            indent + "    \n",
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
            indent + "    @staticmethod\n" +
            indent + "    def %sMetaAttribute(metaAttribute: MetaAttribute) -> str:\n" +
            indent + "        if metaAttribute ==  MetaAttribute.Epoch: return \"%s\"\n" +
            indent + "        if metaAttribute ==  MetaAttribute.TimeUnit: return \"%s\"\n" +
            indent + "        if metaAttribute ==  MetaAttribute.SemanticType: return \"%s\"\n" +
            indent + "        if metaAttribute ==  MetaAttribute.Presence: return \"%s\"\n" +
            indent + "        return \"\"\n" +
            indent + "    \n",
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
                indent + "    def %s(self) -> %s:\n" +
                indent + "        return %s\n" +
                indent + "    \n\n",
                propertyName,
                enumName,
                fieldToken.encoding().constValue().toString());
        }
        else
        {
            final String decoderProp;
            if (encoding.primitiveType().equals(PrimitiveType.CHAR))
            {
                decoderProp = "ord(" + generateGet(encoding.primitiveType(), "self._offset + " + typeToken.offset(),
                    byteOrderString(encoding)) + ")";
            }
            else
            {
                decoderProp = generateGet(encoding.primitiveType(), "self._offset + " + typeToken.offset(),
                    byteOrderString(encoding));
            }
            return String.format(
                "\n" +
                indent + "    def %s(self) -> %s:\n" +
                "%s" +
                indent + "        return %s.get(%s)\n" +
                indent + "    \n\n",
                propertyName,
                enumName,
                generatePropertyNotPresentCondition(inComposite, CodecType.DECODER, fieldToken, enumName, indent),
                enumName,
                decoderProp);
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
            indent + "    def %1$s(self, value: %2$s) -> '%3$s':\n" +
            indent + "        %4$s\n" +
            indent + "        return self\n" +
            indent + "    \n",
            propertyName,
            enumName,
            formatClassName(containingClassName),
            generatePut(encoding.primitiveType(), "self._offset + " + offset, "value.value",
                byteOrderString(encoding)));
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
            indent + "    _%s: %s = %s()\n",
            propertyName,
            bitSetName,
            bitSetName));

        sb.append(String.format("\n" +
            indent + "    def %2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "        self._%4$s.wrap(self._buffer, self._offset + %5$d)\n" +
            indent + "        return self._%6$s\n" +
            indent + "    \n",
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
            indent + "    _%2$s: %1$s = %3$s()\n",
            compositeName,
            propertyName,
            compositeName));

        sb.append(String.format("\n" +
            indent + "    def %2$s(self) -> '%1$s':\n" +
            "%3$s" +
            indent + "        self._%4$s.wrap(self._buffer, self._offset + %5$d)\n" +
            indent + "        return self._%6$s\n" +
            indent + "    \n",
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
            case UINT8:
            case INT16:
            case UINT16:
            case INT32:
            case UINT32:
            case FLOAT:
            case INT64:
            case UINT64:
            case DOUBLE:
                return byteOrder.toUpperCase() + "_" + type.name().toUpperCase() + ".unpack_from(self._buffer, " +
                    index + ")[0]";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generatePut(
        final PrimitiveType type, final String index, final String value, final String byteOrder)
    {
        switch (type)
        {
            case CHAR:
                return byteOrder.toUpperCase() + "_" + type.name().toUpperCase() + ".pack_into(self._buffer, " + index +
                    ", bytes([" + value + "]))";
            case INT8:
            case UINT8:
            case INT16:
            case UINT16:
            case INT32:
            case UINT32:
            case FLOAT:
            case INT64:
            case UINT64:
            case DOUBLE:
                return byteOrder.toUpperCase() + "_" + type.name().toUpperCase() + ".pack_into(self._buffer, " + index +
                    ", " + value + ")";

        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoiceIsEmpty(final PrimitiveType type, final ByteOrder byteOrder)
    {
        return "\n" +
            "    @property\n" +
            "    def isEmpty(self) -> bool:\n" +
            "        return " + generateChoiceIsEmptyInner(type, byteOrder) + "\n" +
            "    \n";
    }

    private String generateChoiceIsEmptyInner(final PrimitiveType type, final ByteOrder byteOrder)
    {
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return "0 == " + byteOrder.toString() + "_" + type.primitiveName().toUpperCase() +
                    ".unpack_from(self._buffer, self._offset)[0]";
        }

        throw new IllegalArgumentException("primitive type not supported: " + type);
    }

    private String generateChoiceGet(final PrimitiveType type, final String bitIndex, final String byteOrder)
    {
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return "0 != (" + byteOrder.toUpperCase() + "_" + type.primitiveName().toUpperCase() +
                    ".unpack_from(self._buffer, self._offset)[0] & (1 << " + bitIndex + "))";
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

    private String generateChoicePut(final PrimitiveType type, final String bitIdx, final String byteOrder)
    {
        final String byteOrderUpper = byteOrder.toUpperCase();
        switch (type)
        {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return "        bits = " + byteOrderUpper + "_" + type.primitiveName().toUpperCase() +
                       ".unpack_from(self._buffer, self._offset)[0]\n" +
                       "        bits = (bits | (1 << " + bitIdx + ")) if value else (bits & ~(1 << " + bitIdx + "))\n" +
                       "        " + byteOrderUpper + "_" + type.primitiveName().toUpperCase() +
                       ".pack_into(self._buffer, self._offset,  bits)\n" +
                       "        return  self\n";

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

    private CharSequence generateCompositeDecoderDisplay(final List<Token> tokens, final String baseIndent)
    {
        final String indent = baseIndent + INDENT;
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "def appendTo(self, builder: str) -> str:");
        Separators.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");

        int lengthBeforeLastGeneratedSeparator = -1;

        for (int i = 1, end = tokens.size() - 1; i < end; )
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
        append(sb, indent, "    return builder");
        return sb.toString();
    }

    private CharSequence generateChoiceDisplay(final List<Token> tokens)
    {
        final String indent = INDENT;
        final StringBuilder sb = new StringBuilder();

        appendToString(sb, indent);
        sb.append('\n');
        append(sb, indent, "def appendTo(self, builder: str) -> str:");
        Separators.BEGIN_SET.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        append(sb, indent, "    atLeastOne: bool = False");

        tokens
            .stream()
            .filter((token) -> token.signal() == Signal.CHOICE)
            .forEach((token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                append(sb, indent, "    if self." + choiceName + "():");
                append(sb, indent, "        if atLeastOne:");
                Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT + INDENT, "builder");
                append(sb, indent, "        builder += str(\"" + choiceName + "\")");
                append(sb, indent, "        atLeastOne = True");
            });

        Separators.END_SET.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        sb.append('\n');
        append(sb, indent, "    return builder");

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
        append(sb, indent, "def appendTo(self, builder: str) -> str:");
        append(sb, indent, "    originalLimit: int = self.limit");
        append(sb, indent, "    self.set_limit(self.offset + self._actingBlockLength)");
        append(sb, indent, "    builder += str(\"[" + name + "](sbeTemplateId=\")");
        append(sb, indent, "    builder += str(self.TEMPLATE_ID)");
        append(sb, indent, "    builder += (\"|sbeSchemaId=\")");
        append(sb, indent, "    builder += str(self.SCHEMA_ID)");
        append(sb, indent, "    builder += (\"|sbeSchemaVersion=\")");
        append(sb, indent, "    if self._parentMessage._actingVersion != self.SCHEMA_VERSION:");
        append(sb, indent, "        builder += str(self._parentMessage._actingVersion)");
        append(sb, indent, "        builder += str('/')");
        append(sb, indent, "    builder += str(self.SCHEMA_VERSION)");
        append(sb, indent, "    builder += (\"|sbeBlockLength=\")");
        append(sb, indent, "    if self._actingBlockLength != self.BLOCK_LENGTH:");
        append(sb, indent, "        builder += str(self._actingBlockLength)");
        append(sb, indent, "        builder += str('/')");
        append(sb, indent, "    builder += str(self.BLOCK_LENGTH)");
        append(sb, indent, "    builder += (\"):\")");
        appendDecoderDisplay(sb, tokens, groups, varData, indent + INDENT);
        sb.append('\n');
        append(sb, indent, "    self.set_limit(originalLimit)");
        sb.append('\n');
        append(sb, indent, "    return builder");
        sb.append('\n');
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
        append(sb, indent, "def appendTo(self, builder: str) -> str:");
        Separators.BEGIN_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        appendDecoderDisplay(sb, fields, groups, varData, indent + INDENT);
        Separators.END_COMPOSITE.appendToGeneratedBuilder(sb, indent + INDENT, "builder");
        append(sb, indent, "    return builder");
        append(sb, "", "\n");
    }

    private void appendDecoderDisplay(
        final StringBuilder sb,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        int lengthBeforeLastGeneratedSeparator = -1;

        for (int i = 0, size = fields.size(); i < size; )
        {
            final Token fieldToken = fields.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = fields.get(i + 1);

                final String fieldName = formatPropertyName(fieldToken.name());
                append(sb, indent, "#" + fieldToken);
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

            append(sb, indent, "#" + groupToken);

            final String groupName = formatPropertyName(groupToken.name());

            append(
                sb, indent, "builder += str(\"" + groupName + Separators.KEY_VALUE +
                Separators.BEGIN_GROUP + "\")");
            append(sb, indent, groupName + " = self." + groupName + "()");
            append(sb, indent, "if " + groupName + ".count() > 0:");
            append(sb, indent, "    while " + groupName + ".hasNext():");
            append(sb, indent, "        " + groupName + ".next().appendTo(builder)");
            Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
            Separators.END_GROUP.appendToGeneratedBuilder(sb, indent, "builder");

            lengthBeforeLastGeneratedSeparator = sb.length();
            Separators.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

            i = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
        }

        for (int i = 0, size = varData.size(); i < size; )
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            append(sb, indent, "#" + varDataToken);

            final String characterEncoding = varData.get(i + 3).encoding().characterEncoding();
            final String varDataName = formatPropertyName(varDataToken.name());
            append(sb, indent, "builder += str(\"" + varDataName + Separators.KEY_VALUE + "\")");
            if (null == characterEncoding)
            {
                append(sb, indent, "builder += str(self." + varDataName + "Length() + \" bytes of raw data\")");
                append(sb, indent,
                    "self._parentMessage.set_limit(self._parentMessage.limit + " + varDataName + "HeaderLength() + " +
                    varDataName + "Length())");
            }
            else
            {
                append(sb, indent, "builder += str('\\'' + self." + varDataName + "() + '\\'')");
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
        append(sb, indent, "#" + typeToken);

        if (typeToken.encodedLength() <= 0 || typeToken.isConstantEncoding())
        {
            return -1;
        }

        append(sb, indent, "builder += str(\"" + fieldName + Separators.KEY_VALUE + "\")");

        switch (typeToken.signal())
        {
            case ENCODING:
                if (typeToken.arrayLength() > 1)
                {
                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        append(sb, indent, "i: int = 0");
                        append(sb, indent,
                            "while i < self." + fieldName + "Length() and self." + fieldName + "(i) > 0:");
                        append(sb, indent, "    builder += str(self." + fieldName + "(i))");
                        append(sb, indent, "    i += 1");
                    }
                    else
                    {
                        Separators.BEGIN_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                        append(sb, indent, "if self." + fieldName + "Length() > 0:");
                        append(sb, indent, "    i: int = 0");
                        append(sb, indent, "    while i < self." + fieldName + "Length():");
                        append(sb, indent, "        builder += str(self." + fieldName + "(i))");
                        append(sb, indent, "        i += 1");
                        Separators.ENTRY.appendToGeneratedBuilder(sb, indent + INDENT + INDENT, "builder");
                        Separators.END_ARRAY.appendToGeneratedBuilder(sb, indent, "builder");
                    }
                }
                else
                {
                    // have to duplicate because of checkstyle :/
                    append(sb, indent, "builder += str(self." + fieldName + "())");
                }
                break;

            case BEGIN_ENUM:
            case BEGIN_SET:
                append(sb, indent, "builder += str(self." + fieldName + "())");
                break;

            case BEGIN_COMPOSITE:
                append(sb, indent, "self._" + fieldName + ".appendTo(builder)");
                break;
        }

        final int lengthBeforeFieldSeparator = sb.length();
        Separators.FIELD.appendToGeneratedBuilder(sb, indent, "builder");

        return lengthBeforeFieldSeparator;
    }

    private void appendToString(final StringBuilder sb, final String indent)
    {
        sb.append('\n');
        append(sb, indent, "def __str__(self) -> str:");
        append(sb, indent, "    return self.appendTo(\"\")");
    }

    private String byteOrderString(final Encoding encoding)
    {
        return encoding.byteOrder().toString();
    }
}
