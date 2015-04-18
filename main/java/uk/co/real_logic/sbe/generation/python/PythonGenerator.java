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
package uk.co.real_logic.sbe.generation.python;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.agrona.Verify;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.python.PythonUtil.*;

public class PythonGenerator implements CodeGenerator
{
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public PythonGenerator(final Ir ir, final OutputManager outputManager)
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
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), null));
            out.append(generateClassDeclaration(MESSAGE_HEADER_TYPE));
            out.append(generateFixedFlyweightCode(MESSAGE_HEADER_TYPE, tokens.get(0).size()));
            out.append(
                generatePrimitivePropertyEncodings(MESSAGE_HEADER_TYPE, tokens.subList(1, tokens.size() - 1), BASE_INDENT));
        }
    }

    public List<String> generateTypeStubs() throws IOException
    {
        final List<String> typesToInclude = new ArrayList<>();

        for (final List<Token> tokens : ir.types())
        {
            switch (tokens.get(0).signal())
            {
                case BEGIN_ENUM:
                    generateEnum(tokens);
                    break;

                case BEGIN_SET:
                    generateChoiceSet(tokens);
                    break;

                case BEGIN_COMPOSITE:
                    generateComposite(tokens);
                    break;
            }

            typesToInclude.add(tokens.get(0).name());
        }

        return typesToInclude;
    }

    public void generate() throws IOException
    {
        generateMessageHeaderStub();
        final List<String> typesToInclude = generateTypeStubs();

        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());

            try (final Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), typesToInclude));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(msgToken));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(className, rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, groups, 0, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = messageBody.subList(offset, messageBody.size());
                out.append(generateVarData(varData));
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

                generateGroupClassHeader(sb, groupName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateFields(groupName, rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateGroups(sb, tokens, index, indent + INDENT);
                }

                sb.append(generateGroupProperty(groupName, groupToken, indent));
            }
        }

        return index;
    }

    private void generateGroupClassHeader(
        final StringBuilder sb, final String groupName, final List<Token> tokens, final int index, final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final int dimensionHeaderSize = tokens.get(index + 1).size();

        sb.append(String.format(
            "\n" +
            indent + "class %1$s:\n" +
            indent + "    buffer_ = 0\n" +
            indent + "    bufferLength_ = 0\n" +
            indent + "    blockLength_ = 0\n" +
            indent + "    count_ = 0\n" +
            indent + "    index_ = 0\n" +
            indent + "    offset_ = 0\n" +
            indent + "    actingVersion_ = 0\n" +
            indent + "    position_ = [0]\n" +
            indent + "    dimensions_ = %2$s.%2$s()\n\n",
            formatClassName(groupName),
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    def wrapForDecode(self, buffer, pos, actingVersion, bufferLength):\n" +
            indent + "        self.buffer_ = buffer\n" +
            indent + "        self.bufferLength_ = bufferLength\n" +
            indent + "        self.dimensions_.wrap(buffer, pos[0], actingVersion, bufferLength)\n" +
            indent + "        self.blockLength_ = self.dimensions_.getBlockLength()\n" +
            indent + "        self.count_ = self.dimensions_.getNumInGroup()\n" +
            indent + "        self.index_ = -1\n" +
            indent + "        self.actingVersion_ = actingVersion\n" +
            indent + "        self.position_ = pos\n" +
            indent + "        self.position_[0] += %1$d\n" +
            indent + "        return self\n\n",
            dimensionHeaderSize
        ));

        final int blockLength = tokens.get(index).size();
        final String typeForBlockLength = pythonTypeName(
            tokens.get(index + 2).encoding().primitiveType(), tokens.get(index + 2).encoding().byteOrder());
        final String typeForNumInGroup = pythonTypeName(
            tokens.get(index + 3).encoding().primitiveType(), tokens.get(index + 3).encoding().byteOrder());

        sb.append(String.format(
            indent + "    def wrapForEncode(self, buffer, count, pos, actingVersion, bufferLength):\n" +
            indent + "        self.buffer_ = buffer\n" +
            indent + "        self.bufferLength_ = bufferLength\n" +
            indent + "        self.dimensions_.wrap(self.buffer_, pos[0], actingVersion, bufferLength)\n" +
            indent + "        self.dimensions_.setBlockLength(%2$d)\n" +
            indent + "        self.dimensions_.setNumInGroup(count)\n" +
            indent + "        self.index_ = -1\n" +
            indent + "        self.count_ = count\n" +
            indent + "        self.blockLength_ = %2$d\n" +
            indent + "        self.actingVersion_ = actingVersion\n" +
            indent + "        self.position_ = pos\n" +
            indent + "        self.position_[0] += %4$d\n" +
            indent + "        return self\n\n",
            typeForBlockLength,
            blockLength,
            typeForNumInGroup,
            dimensionHeaderSize
        ));

        sb.append(String.format(
            indent + "    @staticmethod\n" +
            indent + "    def sbeHeaderSize():\n" +
            indent + "        return %d\n\n",
            dimensionHeaderSize
        ));

        sb.append(String.format(
            indent + "    @staticmethod\n" +
            indent + "    def sbeBlockLength():\n" +
            indent + "        return %d\n\n",
            blockLength
        ));

        sb.append(String.format(
            indent + "    def count(self):\n" +
            indent + "        return self.count_\n\n" +
            indent + "    def hasNext(self):\n" +
            indent + "        return self.index_ + 1 < self.count_\n\n"
        ));

        sb.append(String.format(
            indent + "    def next(self):\n" +
            indent + "        self.offset_ = self.position_[0]\n" +
            indent + "        if (self.offset_ + self.blockLength_) > self.bufferLength_:\n" +
            indent + "            raise Exception('buffer too short to support next group index')\n" +
            indent + "        self.position_[0] = self.offset_ + self.blockLength_\n" +
            indent + "        self.index_ += 1\n" +
            indent + "        return self\n\n",
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
            indent + "    @staticmethod\n" +
            indent + "    def %1$sId():\n" +
            indent + "        return %2$d;\n\n",
            groupName,
            (long)token.id()
        ));

        sb.append(String.format(
            "\n" +
            indent + "    def %2$s(self):\n" +
            indent + "        group = self.%1$s()\n" +
            indent + "        group.wrapForDecode(self.buffer_, self.position_, self.actingVersion_, self.bufferLength_)\n" +
            indent + "        return group\n\n",
            className,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    def %2$sCount(self, count):\n" +
            indent + "        group = self.%1$s()\n" +
            indent + "        group.wrapForEncode(" +
                "self.buffer_, count, self.position_, self.actingVersion_, self.bufferLength_)\n" +
            indent + "        return group\n\n",
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
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = toUpperFirstChar(token.name());
                final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
                final Token lengthToken = tokens.get(i + 2);
                final int sizeOfLengthField = lengthToken.size();
                final String lengthPythonType = pythonTypeName(
                    lengthToken.encoding().primitiveType(), lengthToken.encoding().byteOrder());

                final String byteOrder = lengthToken.encoding().byteOrder() == ByteOrder.BIG_ENDIAN ? ">" : "<";

                generateFieldMetaAttributeMethod(sb, token, BASE_INDENT);

                generateVarDataDescriptors(
                    sb, token, propertyName, characterEncoding, lengthToken, sizeOfLengthField, lengthPythonType);

                sb.append(String.format(
                    "    def get%1$s(self):\n" +
                    "        sizeOfLengthField = %3$d\n" +
                    "        lengthPosition = self.getPosition()\n" +
                    "        dataLength = struct.unpack_from('%5$s', self.buffer_, lengthPosition[0])[0]\n" +
                    "        self.setPosition(lengthPosition[0] + sizeOfLengthField)\n" +
                    "        pos = self.getPosition()\n" +
                    "        fmt = '" + byteOrder + "'+str(dataLength)+'c'\n" +
                    "        data = struct.unpack_from(fmt, self.buffer_, lengthPosition[0])\n" +
                    "        self.setPosition(pos[0] + dataLength)\n" +
                    "        return data\n\n",
                    propertyName,
                    generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
                    sizeOfLengthField,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
                    lengthPythonType
                ));

                sb.append(String.format(
                    "    def set%1$s(self, buffer):\n" +
                    "        sizeOfLengthField = %2$d\n" +
                    "        lengthPosition = self.getPosition()\n" +
                    "        struct.pack_into('%3$s', self.buffer_, lengthPosition[0], len(buffer))\n" +
                    "        self.setPosition(lengthPosition[0] + sizeOfLengthField)\n" +
                    "        pos = self.getPosition()\n" +
                    "        fmt = '" + byteOrder + "c'\n" +
                    "        for i in range(0,len(buffer)):\n" +
                    "           struct.pack_into(fmt, self.buffer_, lengthPosition[0]+i, buffer[i])\n" +
                    "        self.setPosition(pos[0] + len(buffer))\n\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthPythonType,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType())
                ));
            }
        }

        return sb;
    }

    private void generateVarDataDescriptors(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final String characterEncoding,
        final Token lengthToken,
        final Integer sizeOfLengthField,
        final String lengthCpp98Type)
    {
        sb.append(String.format(
            "\n" +
            "    @staticmethod\n" +
            "    def %1$sCharacterEncoding():\n" +
            "        return '%2$s'\n\n",
            formatPropertyName(propertyName),
            characterEncoding
        ));

        sb.append(String.format(
            "    @staticmethod\n" +
            "    def %1$sSinceVersion():\n" +
            "         return %2$d\n\n" +

            "    def %1$sInActingVersion(self):\n" +
            "        return True if self.actingVersion_ >= %2$s else False\n" +

            "    @staticmethod\n" +
            "    def %1$sId():\n" +
            "        return %3$d\n\n",
            formatPropertyName(propertyName),
            (long)token.version(),
            token.id()
        ));

        sb.append(String.format(
            "    @staticmethod\n" +
            "    def %sHeaderSize():\n" +
            "        return %d\n\n",
            toLowerFirstChar(propertyName),
            sizeOfLengthField
        ));

        sb.append(String.format(
            "    def %1$sLength(self):\n" +
            "        return struct.unpack_from('%4$s', self.buffer_, position())[0]\n\n",
            formatPropertyName(propertyName),
            generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
            formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
            lengthCpp98Type
        ));
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), null));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).size()));
            out.append(String.format(
                "\n" +
                "    def clear(self):\n" +
                "        struct.pack_into('%2$s', self.buffer_, self.offset_, 0)\n" +
                "        return self\n\n",
                bitSetName,
                pythonTypeName(tokens.get(0).encoding().primitiveType(), tokens.get(0).encoding().byteOrder())
            ));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), null));
            out.append(generateEnumDeclaration(enumName));
            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumToken));
            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumToken));
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), null));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).size()));
            out.append(generatePrimitivePropertyEncodings(compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));
        }
    }

    private CharSequence generateChoiceNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if self.actingVersion_ < %1$d:\n" +
            indent + "            return False\n\n",
            sinceVersion
        );
    }

    private CharSequence generateChoices(final String bitsetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final String typeName = pythonTypeName(token.encoding().primitiveType(), token.encoding().byteOrder());
                final String choiceBitPosition = token.encoding().constValue().toString();
                final String byteOrderStr = formatByteOrderEncoding(
                    token.encoding().byteOrder(), token.encoding().primitiveType());

                sb.append(String.format(
                    "\n" +
                    "    def get%1$s(self):\n" +
                    "        return True if struct.unpack_from(" +
                    "'%4$s', self.buffer_, self.offset_)[0] & (0x1L << %5$s) > 0 else False\n\n",
                    toUpperFirstChar(choiceName),
                    generateChoiceNotPresentCondition(token.version(), BASE_INDENT),
                    byteOrderStr,
                    typeName,
                    choiceBitPosition
                ));

                sb.append(String.format(
                    "    def set%2$s(self, value):\n" +
                    "        bits = struct.unpack_from('%3$s', self.buffer_, self.offset_)[0]\n" +
                    "        bits = (bits | ( 0x1L << %5$s)) if value > 0 else (bits & ~(0x1L << %5$s))\n" +
                    "        struct.pack_into('%3$s', self.buffer_, self.offset_, value)\n" +
                    "        return self\n",
                    bitsetClassName,
                    toUpperFirstChar(choiceName),
                    typeName,
                    byteOrderStr,
                    choiceBitPosition
                ));
            }
        }

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens, final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        sb.append("    class Value:\n");

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());
            sb.append("        ").append(token.name()).append(" = ").append(constVal).append("\n");
        }

        sb.append(String.format(
            "        NULL_VALUE = %1$s",
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString())
        ));

        sb.append("\n\n");

        return sb;
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.name());
        final StringBuilder sb = new StringBuilder();

        sb.append(
            "    @staticmethod\n" +
            "    def get(value):\n" +
            "        values = {\n");

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            %1$s : %3$s.Value.%2$s,\n",
                token.encoding().constValue().toString(),
                token.name(),
                enumName)
            );
        }

        sb.append(String.format(
            "            %1$s : %2$s.Value.NULL_VALUE\n" +
            "        }\n" +
            "        if type(value) is int:\n" +
            "            return values[value]\n" +
            "        else:\n" +
            "            return values[ord(value)]\n",
            encodingToken.encoding().applicableNullValue().toString(),
            enumName
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
            indent + "        if self.actingVersion_ < %1$d:\n" +
            indent + "            return %2$s\n\n",
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
            indent + "        if self.actingVersion_ < %1$d:\n" +
            indent + "            return False\n\n",
            sinceVersion
        );
    }

    private CharSequence generateFileHeader(final String namespaceName, final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("#\n# Generated SBE (Simple Binary Encoding) message codec\n#\n");
        sb.append("import struct\n\n");

        if (typesToInclude != null)
        {
            for (final String incName : typesToInclude)
            {
                sb.append(String.format(
                    "import %2$s\n",
                    namespaceName,
                    toUpperFirstChar(incName)
                ));
            }
            sb.append("\n");
        }

        return sb;
    }

    private CharSequence generateClassDeclaration(final String name)
    {
        return "class " + name + ":\n";
    }

    private CharSequence generateEnumDeclaration(final String name)
    {
        return "class " + name + ":\n";
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
            return generateArrayProperty(propertyName, token, indent);
        }

        return "";
    }

    private CharSequence generatePrimitiveFieldMetaData(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String pythonTypeName = pythonTypeName(primitiveType, token.encoding().byteOrder());

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sNullValue():\n" +
            indent + "        return %3$s\n",
            pythonTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableNullValue().toString())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sMinValue():\n" +
            indent + "        return %3$s\n",
            pythonTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %2$sMaxValue():\n" +
            indent + "        return %3$s\n",
            pythonTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())
        ));

        return sb;
    }

    private CharSequence generateSingleValueProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String pythonTypeName = pythonTypeName(token.encoding().primitiveType(), token.encoding().byteOrder());
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    def get%2$s(self):\n" +
            indent + "        return struct.unpack_from('%1$s', self.buffer_, self.offset_ + %5$d)[0]\n\n",
            pythonTypeName,
            toUpperFirstChar(propertyName),
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            offset
        ));

        sb.append(String.format(
            indent + "    def set%2$s(self, value):\n" +
            indent + "        struct.pack_into('%3$s', self.buffer_, self.offset_ + %4$d, value)\n" +
            indent + "        return self\n",
            formatClassName(containingClassName),
            toUpperFirstChar(propertyName),
            pythonTypeName,
            offset,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        return sb;
    }

    private CharSequence generateArrayProperty(
        final String propertyName, final Token token, final String indent)
    {
        final String pythonTypeName = pythonTypeName(token.encoding().primitiveType(), token.encoding().byteOrder());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %1$sLength():\n" +
            indent + "        return %2$d\n\n",
            propertyName,
            token.arrayLength()
        ));

        sb.append(String.format(
            indent + "    def get%2$s(self, index):\n" +
            indent + "        if index < 0 or index >= %3$d:\n" +
            indent + "            raise Exception('index out of range for %2$s')\n" +
            indent + "        return struct.unpack_from('%1$s', self.buffer_, self.offset_ + %6$d + (index * %7$d))[0]\n\n",
            pythonTypeName,
            toUpperFirstChar(propertyName),
            token.arrayLength(),
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            offset,
            token.encoding().primitiveType().size()
        ));

        sb.append(String.format(
            indent + "    def set%1$s(self, index, value):\n" +
            indent + "        if index < 0 or index >= %3$d:\n" +
            indent + "            raise Exception('index out of range for %1$s')\n" +
            indent + "        struct.pack_into('%2$s', self.buffer_, self.offset_ + %4$d + (index * %5$d), value)\n",
            propertyName,
            toUpperFirstChar(pythonTypeName),
            token.arrayLength(),
            offset,
            token.encoding().primitiveType().size(),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        return sb;
    }

    private CharSequence generateConstPropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final String pythonTypeName = pythonTypeName(token.encoding().primitiveType(), token.encoding().byteOrder());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            return String.format(
                "\n" +
                indent + "    def %2$s(self):\n" +
                indent + "        return %3$s\n",
                pythonTypeName,
                propertyName,
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString())
            );
        }

        final StringBuilder sb = new StringBuilder();
        final byte[] constantValue = token.encoding().constValue().byteArrayValue(token.encoding().primitiveType());
        final StringBuilder values = new StringBuilder();
        for (final byte b : constantValue)
        {
            values.append(b).append(", ");
        }

        if (values.length() > 0)
        {
            values.setLength(values.length() - 2);
        }

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %1$sLength():\n" +
            indent + "        return %2$d\n\n",
            propertyName,
            constantValue.length
        ));

        sb.append(String.format(
            indent + "    def %2$s(self, index):\n" +
            indent + "        %2$sValues = [%3$s]\n" +
            indent + "        return %2$sValues[index]\n",
            pythonTypeName,
            propertyName,
            values
        ));

        return sb;
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        return String.format(
            "    buffer_ = 0\n" +
            "    offset_ = 0\n" +
            "    actingVersion_ = 0\n\n" +

            "    def wrap(self, buffer, offset, actingVersion, bufferLength):\n" +
            "        if (offset > (bufferLength - %2$s)):\n" +
            "            raise Exception('buffer too short for flyweight')\n" +
            "        self.buffer_ = buffer\n" +
            "        self.offset_ = offset\n" +
            "        self.actingVersion_ = actingVersion\n" +
            "        return self\n\n" +

            "    @staticmethod\n" +
            "    def size():\n" +
            "        return %2$s\n",
            className,
            size
        );
    }

    private CharSequence generateMessageFlyweightCode(final Token token)
    {
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            "    buffer_ = 0\n" +
            "    bufferLength_ = 0\n" +
            "    offset_ = 0\n" +
            "    actingBlockLength_ = 0\n" +
            "    actingVersion_ = 0\n" +
            "    position_ = [0]\n\n" +

            "    @staticmethod\n" +
            "    def sbeBlockLength():\n" +
            "        return %1$s\n\n" +

            "    @staticmethod\n" +
            "    def sbeTemplateId():\n" +
            "        return %2$s\n\n" +

            "    @staticmethod\n" +
            "    def sbeSchemaId():\n" +
            "        return %3$s\n\n" +

            "    @staticmethod\n" +
            "    def sbeSchemaVersion():\n" +
            "        return %4$s\n\n" +

            "    @staticmethod\n" +
            "    def sbeSemanticType():\n" +
            "        return \"%5$s\"\n\n" +

            "    def offset(self):\n" +
            "        return offset_\n\n" +

            "    def wrapForEncode(self, buffer, offset, bufferLength):\n" +
            "        self.buffer_ = buffer\n" +
            "        self.offset_ = offset\n" +
            "        self.bufferLength_ = bufferLength\n" +
            "        self.actingBlockLength_ = self.sbeBlockLength()\n" +
            "        self.actingVersion_ = self.sbeSchemaVersion()\n" +
            "        self.setPosition(offset + self.actingBlockLength_)\n" +
            "        return self\n\n" +

            "    def wrapForDecode(self, buffer, offset, actingBlockLength, actingVersion, bufferLength):\n" +
            "        self.buffer_ = buffer\n" +
            "        self.offset_ = offset\n" +
            "        self.bufferLength_ = bufferLength\n" +
            "        self.actingBlockLength_ = actingBlockLength\n" +
            "        self.actingVersion_ = actingVersion\n" +
            "        self.setPosition(offset + self.actingBlockLength_)\n" +
            "        return self\n\n" +

            "    def getPosition(self):\n" +
            "        return self.position_\n\n" +

            "    def setPosition(self, position):\n" +
            "        if self.position_[0] > self.bufferLength_:\n" +
            "            raise Exception('buffer too short')\n" +
            "        self.position_[0] = position\n\n" +

            "    def size(self):\n" +
            "        return self.position() - self.offset_\n\n" +

            "    def buffer(self):\n" +
            "        return self.buffer_\n\n" +

            "    def actingVersion(self):\n" +
            "        return self.actingVersion_;\n",

            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.size())),
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(token.version())),
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

                sb.append(String.format(
                    "\n" +
                    indent + "    @staticmethod\n" +
                    indent + "    def %1$sId():\n" +
                    indent + "        return %2$d\n\n",
                    propertyName,
                    signalToken.id()
                ));

                sb.append(String.format(
                    indent + "    @staticmethod\n" +
                    indent + "    def %1$sSinceVersion():\n" +
                    indent + "         return %2$d\n\n" +
                    indent + "    def %1$sInActingVersion(self):\n" +
                    indent + "        return self.actingVersion_ >= %2$d\n",
                    propertyName,
                    (long)signalToken.version()
                ));

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
                        sb.append(generateBitsetProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_COMPOSITE:
                        sb.append(generateCompositeProperty(propertyName, encodingToken, indent));
                        break;
                }
            }
        }

        return sb;
    }

    private void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append(String.format(
            "\n" +
            indent + "    @staticmethod\n" +
            indent + "    def %sMetaAttribute(meta):\n" +
            indent + "        return \"???\"\n",
            token.name(),
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
            indent + "        if self.actingVersion_ < %1$d:\n" +
            indent + "            return %2$s.Value.NULL_VALUE\n",
            sinceVersion,
            enumName
        );
    }

    private CharSequence generateEnumProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String enumName = token.name();
        final String typeName = pythonTypeName(token.encoding().primitiveType(), token.encoding().byteOrder());
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    def get%2$s(self):\n" +
            indent + "        return %1$s.%1$s.get(struct.unpack_from( '%5$s', self.buffer_, self.offset_ + %6$d)[0])\n\n",
            enumName,
            toUpperFirstChar(propertyName),
            generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            typeName,
            offset
        ));

        sb.append(String.format(
            indent + "    def set%2$s(self, value):\n" +
            indent + "        struct.pack_into('%4$s', self.buffer_, self.offset_ + %5$d, value)\n" +
            indent + "        return self\n",
            formatClassName(containingClassName),
            toUpperFirstChar(propertyName),
            enumName,
            typeName,
            offset,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        return sb;
    }

    private Object generateBitsetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final String bitsetName = formatClassName(token.name());
        final int offset = token.offset();

        sb.append(String.format(
            "\n" +
            indent + "    def %2$s(self):\n" +
            indent + "        bitset = %1$s.%1$s()\n" +
            indent + "        bitset.wrap(self.buffer_, self.offset_ + %3$d, self.actingVersion_, self.bufferLength_)\n" +
            indent + "        return bitset;\n",
            bitsetName,
            propertyName,
            offset
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.name());
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    def %2$s(self):\n" +
            indent + "        %2$s = %1$s.%1$s()\n" +
            indent + "        %2$s.wrap(self.buffer_, self.offset_ + %3$d, self.actingVersion_, self.bufferLength_)\n" +
            indent + "        return %2$s\n",
            compositeName,
            propertyName,
            offset
        ));

        return sb;
    }

    private CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        switch (type)
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = value;
                break;

            case UINT32:
            case INT32:
                literal = value;
                break;

            case FLOAT:
                if (value.endsWith("NaN"))
                {
                    literal = "float('NaN')";
                }
                else
                {
                    literal = value;
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
                    literal = "double('NaN')";
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
