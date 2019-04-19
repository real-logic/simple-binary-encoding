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
package uk.co.real_logic.sbe.generation.cpp;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.cpp.CppUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

@SuppressWarnings("MethodLength")
public class CppGenerator implements CodeGenerator
{
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public CppGenerator(final Ir ir, final OutputManager outputManager)
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

            typesToInclude.add(tokens.get(0).applicableTypeName());
        }

        return typesToInclude;
    }

    public List<String> generateTypesToIncludes(final List<Token> tokens)
    {
        final List<String> typesToInclude = new ArrayList<>();

        for (final Token token : tokens)
        {
            switch (token.signal())
            {
                case BEGIN_ENUM:
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    typesToInclude.add(token.applicableTypeName());
                    break;
            }
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

            try (Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.namespaces(), className, typesToInclude));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(className, msgToken));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int i = 0;

                final List<Token> fields = new ArrayList<>();
                i = collectFields(messageBody, i, fields);

                final List<Token> groups = new ArrayList<>();
                i = collectGroups(messageBody, i, groups);

                final List<Token> varData = new ArrayList<>();
                collectVarData(messageBody, i, varData);

                final StringBuilder sb = new StringBuilder();
                out.append(generateFields(className, fields, BASE_INDENT, false));
                generateGroups(sb, groups, BASE_INDENT);
                out.append(sb);
                out.append(generateVarData(className, varData, BASE_INDENT));
                out.append("};\n");
                out.append(CppUtil.closingBraces(ir.namespaces().length)).append("#endif\n");
            }
        }
    }

    private void generateGroups(final StringBuilder sb, final List<Token> tokens, final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final String groupName = groupToken.name();
            final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, i);
            final String cppTypeForNumInGroup = cppTypeName(numInGroupToken.encoding().primitiveType());

            generateGroupClassHeader(sb, groupName, tokens, i, indent + INDENT);

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateFields(formatClassName(groupName), fields, indent + INDENT, false));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(sb, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            sb.append(generateVarData(formatClassName(groupName), varData, indent + INDENT));

            sb.append(indent).append("    };\n");
            sb.append(generateGroupProperty(groupName, groupToken, cppTypeForNumInGroup, indent));
        }
    }

    private static void generateGroupClassHeader(
        final StringBuilder sb, final String groupName, final List<Token> tokens, final int index, final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final int dimensionHeaderLength = tokens.get(index + 1).encodedLength();
        final int blockLength = tokens.get(index).encodedLength();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        final String cppTypeForBlockLength = cppTypeName(blockLengthToken.encoding().primitiveType());
        final String cppTypeForNumInGroup = cppTypeName(numInGroupToken.encoding().primitiveType());

        sb.append(String.format("\n" +
            indent + "class %1$s\n" +
            indent + "{\n" +
            indent + "private:\n" +
            indent + "    char *m_buffer;\n" +
            indent + "    std::uint64_t m_bufferLength;\n" +
            indent + "    std::uint64_t *m_positionPtr;\n" +
            indent + "    std::uint64_t m_blockLength;\n" +
            indent + "    std::uint64_t m_count;\n" +
            indent + "    std::uint64_t m_index;\n" +
            indent + "    std::uint64_t m_offset;\n" +
            indent + "    std::uint64_t m_actingVersion;\n" +
            indent + "    %2$s m_dimensions;\n\n" +

            indent + "    std::uint64_t *sbePositionPtr() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_positionPtr;\n" +
            indent + "    }\n\n" +

            indent + "public:\n",
            formatClassName(groupName), dimensionsClassName));

        sb.append(String.format(
            indent + "    inline void wrapForDecode(char *buffer, std::uint64_t *pos," +
            " const std::uint64_t actingVersion, const std::uint64_t bufferLength)\n" +
            indent + "    {\n" +
            indent + "        m_buffer = buffer;\n" +
            indent + "        m_bufferLength = bufferLength;\n" +
            indent + "        m_dimensions.wrap(m_buffer, *pos, actingVersion, bufferLength);\n" +
            indent + "        m_blockLength = m_dimensions.blockLength();\n" +
            indent + "        m_count = m_dimensions.numInGroup();\n" +
            indent + "        m_index = -1;\n" +
            indent + "        m_actingVersion = actingVersion;\n" +
            indent + "        m_positionPtr = pos;\n" +
            indent + "        *m_positionPtr = *m_positionPtr + %1$d;\n" +
            indent + "    }\n",
            dimensionHeaderLength));

        final long minCount = numInGroupToken.encoding().applicableMinValue().longValue();
        final String minCheck = minCount > 0 ? "count < " + minCount + " || " : "";

        sb.append(String.format("\n" +
            indent + "    inline void wrapForEncode(char *buffer, const %3$s count," +
            " std::uint64_t *pos, const std::uint64_t actingVersion, const std::uint64_t bufferLength)\n" +
            indent + "    {\n" +
            indent + "#if defined(__GNUG__) && !defined(__clang__)\n" +
            indent + "#pragma GCC diagnostic push\n" +
            indent + "#pragma GCC diagnostic ignored \"-Wtype-limits\"\n" +
            indent + "#endif\n" +
            indent + "        if (%5$scount > %6$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"count outside of allowed range [E110]\");\n" +
            indent + "        }\n" +
            indent + "#if defined(__GNUG__) && !defined(__clang__)\n" +
            indent + "#pragma GCC diagnostic pop\n" +
            indent + "#endif\n" +
            indent + "        m_buffer = buffer;\n" +
            indent + "        m_bufferLength = bufferLength;\n" +
            indent + "        m_dimensions.wrap(m_buffer, *pos, actingVersion, bufferLength);\n" +
            indent + "        m_dimensions.blockLength((%1$s)%2$d);\n" +
            indent + "        m_dimensions.numInGroup((%3$s)count);\n" +
            indent + "        m_index = -1;\n" +
            indent + "        m_count = count;\n" +
            indent + "        m_blockLength = %2$d;\n" +
            indent + "        m_actingVersion = actingVersion;\n" +
            indent + "        m_positionPtr = pos;\n" +
            indent + "        *m_positionPtr = *m_positionPtr + %4$d;\n" +
            indent + "    }\n",
            cppTypeForBlockLength, blockLength, cppTypeForNumInGroup, dimensionHeaderLength,
            minCheck,
            numInGroupToken.encoding().applicableMaxValue().longValue()));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t sbeHeaderSize() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %1$d;\n" +
            indent + "    }\n\n" +

            indent + "    static SBE_CONSTEXPR std::uint64_t sbeBlockLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    std::uint64_t sbePosition() const\n" +
            indent + "    {\n" +
            indent + "        return *m_positionPtr;\n" +
            indent + "    }\n\n" +

            indent + "    std::uint64_t sbeCheckPosition(const std::uint64_t position)\n" +
            indent + "    {\n" +
            indent + "        if (SBE_BOUNDS_CHECK_EXPECT((position > m_bufferLength), false))\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"buffer too short [E100]\");\n" +
            indent + "        }\n" +
            indent + "        return position;\n" +
            indent + "    }\n\n" +

            indent + "    void sbePosition(const std::uint64_t position)\n" +
            indent + "    {\n" +
            indent + "        *m_positionPtr = sbeCheckPosition(position);\n" +
            indent + "    }\n\n" +

            indent + "    inline std::uint64_t count() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_count;\n" +
            indent + "    }\n\n" +

            indent + "    inline bool hasNext() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_index + 1 < m_count;\n" +
            indent + "    }\n\n" +

            indent + "    inline %3$s &next()\n" +
            indent + "    {\n" +
            indent + "        m_offset = *m_positionPtr;\n" +
            indent + "        if (SBE_BOUNDS_CHECK_EXPECT(((m_offset + m_blockLength) > m_bufferLength), false))\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"" +
            "buffer too short to support next group index [E108]\");\n" +
            indent + "        }\n" +
            indent + "        *m_positionPtr = m_offset + m_blockLength;\n" +
            indent + "        ++m_index;\n\n" +

            indent + "        return *this;\n" +
            indent + "    }\n",
            dimensionHeaderLength, blockLength, formatClassName(groupName)));

        sb.append(indent).append("#if __cplusplus < 201103L\n")
            .append(indent).append("    template<class Func> inline void forEach(Func& func)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        while (hasNext())\n")
            .append(indent).append("        {\n")
            .append(indent).append("            next();\n")
            .append(indent).append("            func(*this);\n")
            .append(indent).append("        }\n")
            .append(indent).append("    }\n\n")

            .append(indent).append("#else\n")
            .append(indent).append("    template<class Func> inline void forEach(Func&& func)\n")
            .append(indent).append("    {\n")
            .append(indent).append("        while (hasNext())\n")
            .append(indent).append("        {\n")
            .append(indent).append("            next();\n")
            .append(indent).append("            func(*this);\n")
            .append(indent).append("        }\n")
            .append(indent).append("    }\n\n")

            .append(indent).append("#endif\n");
    }

    private static CharSequence generateGroupProperty(
        final String groupName, final Token token, final String cppTypeForNumInGroup, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        sb.append(String.format("\n" +
            "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            "public:\n",
            className,
            propertyName));

        sb.append(String.format(
            indent + "    static SBE_CONSTEXPR std::uint16_t %1$sId() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            groupName,
            token.id()));

        sb.append(String.format("\n" +
            indent + "    inline %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrapForDecode(m_buffer, sbePositionPtr(), m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            className,
            propertyName));

        sb.append(String.format("\n" +
            indent + "    %1$s &%2$sCount(const %3$s count)\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrapForEncode(" +
            "m_buffer, count, sbePositionPtr(), m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            className,
            propertyName,
            cppTypeForNumInGroup));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sSinceVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    bool %1$sInActingVersion() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic push\n" +
            indent + "#pragma clang diagnostic ignored \"-Wtautological-compare\"\n" +
            indent + "#endif\n" +
            indent + "        return m_actingVersion >= %1$sSinceVersion();\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic pop\n" +
            indent + "#endif\n" +
            indent + "    }\n",
            propertyName,
            token.version()));

        return sb;
    }

    private CharSequence generateVarData(final String className, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token token = tokens.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }

            final String propertyName = toUpperFirstChar(token.name());
            final Token lengthToken = Generators.findFirst("length", tokens, i);
            final Token varDataToken = Generators.findFirst("varData", tokens, i);
            final String characterEncoding = varDataToken.encoding().characterEncoding();
            final int lengthOfLengthField = lengthToken.encodedLength();
            final String lengthCppType = cppTypeName(lengthToken.encoding().primitiveType());
            final String lengthByteOrderStr = formatByteOrderEncoding(
                lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType());

            generateFieldMetaAttributeMethod(sb, token, indent);

            generateVarDataDescriptors(
                sb, token, propertyName, characterEncoding, lengthToken, lengthOfLengthField, lengthCppType, indent);

            sb.append(String.format("\n" +
                indent + "    const char *%1$s()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        %4$s lengthFieldValue;\n" +
                indent + "        std::memcpy(&lengthFieldValue, m_buffer + sbePosition(), sizeof(%4$s));\n" +
                indent + "        const char *fieldPtr = m_buffer + sbePosition() + %3$d;\n" +
                indent + "        sbePosition(sbePosition() + %3$d + %5$s(lengthFieldValue));\n" +
                indent + "        return fieldPtr;\n" +
                indent + "    }\n",
                formatPropertyName(propertyName),
                generateTypeFieldNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthCppType,
                lengthByteOrderStr));

            sb.append(String.format("\n" +
                indent + "    std::uint64_t get%1$s(char *dst, const std::uint64_t length)\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        std::uint64_t lengthOfLengthField = %3$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        %5$s lengthFieldValue;\n" +
                indent + "        std::memcpy(&lengthFieldValue, m_buffer + lengthPosition, sizeof(%5$s));\n" +
                indent + "        std::uint64_t dataLength = %4$s(lengthFieldValue);\n" +
                indent + "        std::uint64_t bytesToCopy = length < dataLength ? length : dataLength;\n" +
                indent + "        std::uint64_t pos = sbePosition();\n" +
                indent + "        sbePosition(pos + dataLength);\n" +
                indent + "        std::memcpy(dst, m_buffer + pos, bytesToCopy);\n" +
                indent + "        return bytesToCopy;\n" +
                indent + "    }\n",
                propertyName,
                generateArrayFieldNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderStr,
                lengthCppType));

            sb.append(String.format("\n" +
                indent + "    %5$s &put%1$s(const char *src, const %3$s length)\n" +
                indent + "    {\n" +
                indent + "        std::uint64_t lengthOfLengthField = %2$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        %3$s lengthFieldValue = %4$s(length);\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        std::memcpy(m_buffer + lengthPosition, &lengthFieldValue, sizeof(%3$s));\n" +
                indent + "        std::uint64_t pos = sbePosition();\n" +
                indent + "        sbePosition(pos + length);\n" +
                indent + "        std::memcpy(m_buffer + pos, src, length);\n" +
                indent + "        return *this;\n" +
                indent + "    }\n",
                propertyName,
                lengthOfLengthField,
                lengthCppType,
                lengthByteOrderStr,
                className));

            sb.append(String.format("\n" +
                indent + "    std::string get%1$sAsString()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        std::uint64_t lengthOfLengthField = %3$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        %5$s lengthFieldValue;\n" +
                indent + "        std::memcpy(&lengthFieldValue, m_buffer + lengthPosition, sizeof(%5$s));\n" +
                indent + "        std::uint64_t dataLength = %4$s(lengthFieldValue);\n" +
                indent + "        std::uint64_t pos = sbePosition();\n" +
                indent + "        const std::string result(m_buffer + pos, dataLength);\n" +
                indent + "        sbePosition(pos + dataLength);\n" +
                indent + "        return result;\n" +
                indent + "    }\n",
                propertyName,
                generateStringNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderStr,
                lengthCppType));

            sb.append(String.format("\n" +
                indent + "    #if __cplusplus >= 201703L\n" +
                indent + "    std::string_view get%1$sAsStringView()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        std::uint64_t lengthOfLengthField = %3$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        %5$s lengthFieldValue;\n" +
                indent + "        std::memcpy(&lengthFieldValue, m_buffer + lengthPosition, sizeof(%5$s));\n" +
                indent + "        std::uint64_t dataLength = %4$s(lengthFieldValue);\n" +
                indent + "        std::uint64_t pos = sbePosition();\n" +
                indent + "        const std::string_view result(m_buffer + pos, dataLength);\n" +
                indent + "        sbePosition(pos + dataLength);\n" +
                indent + "        return result;\n" +
                indent + "    }\n" +
                indent + "    #endif\n",
                propertyName,
                generateStringViewNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderStr,
                lengthCppType));

            sb.append(String.format("\n" +
                indent + "    %1$s &put%2$s(const std::string& str)\n" +
                indent + "    {\n" +
                indent + "        if (str.length() > %6$d)\n" +
                indent + "        {\n" +
                indent + "            throw std::runtime_error(\"std::string too long for length type [E109]\");\n" +
                indent + "        }\n" +
                indent + "        std::uint64_t lengthOfLengthField = %3$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        %4$s lengthFieldValue = %5$s(static_cast<%4$s>(str.length()));\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        std::memcpy(m_buffer + lengthPosition, &lengthFieldValue, sizeof(%4$s));\n" +
                indent + "        std::uint64_t pos = sbePosition();\n" +
                indent + "        sbePosition(pos + str.length());\n" +
                indent + "        std::memcpy(m_buffer + pos, str.c_str(), str.length());\n" +
                indent + "        return *this;\n" +
                indent + "    }\n",
                className,
                propertyName,
                lengthOfLengthField,
                lengthCppType,
                lengthByteOrderStr,
                lengthToken.encoding().applicableMaxValue().longValue()));

            i += token.componentTokenCount();
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
        final String lengthCppType,
        final String indent)
    {
        sb.append(String.format("\n" +
            indent + "    static const char *%1$sCharacterEncoding() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return \"%2$s\";\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            characterEncoding));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sSinceVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    bool %1$sInActingVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic push\n" +
            indent + "#pragma clang diagnostic ignored \"-Wtautological-compare\"\n" +
            indent + "#endif\n" +
            indent + "        return m_actingVersion >= %1$sSinceVersion();\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic pop\n" +
            indent + "#endif\n" +
            indent + "    }\n\n" +

            indent + "    static SBE_CONSTEXPR std::uint16_t %1$sId() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$d;\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            token.version(),
            token.id()));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %sHeaderLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            sizeOfLengthField));

        sb.append(String.format("\n" +
            indent + "    %4$s %1$sLength() const\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        %4$s length;\n" +
            indent + "        std::memcpy(&length, m_buffer + sbePosition(), sizeof(%4$s));\n" +
            indent + "        return %3$s(length);\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
            formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
            lengthCppType));
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.namespaces(), bitSetName, null));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).encodedLength()));

            out.append(String.format("\n" +
                "    %1$s &clear()\n" +
                "    {\n" +
                "        %2$s zero = 0;\n" +
                "        std::memcpy(m_buffer + m_offset, &zero, sizeof(%2$s));\n" +
                "        return *this;\n" +
                "    }\n",
                bitSetName,
                cppTypeName(tokens.get(0).encoding().primitiveType())));

            out.append(String.format("\n" +
                "    bool isEmpty() const\n" +
                "    {\n" +
                "        %1$s val;\n" +
                "        std::memcpy(&val, m_buffer + m_offset, sizeof(%1$s));\n" +
                "        return 0 == val;\n" +
                "    }\n",
                cppTypeName(tokens.get(0).encoding().primitiveType())));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));
            out.append("};\n");
            out.append(CppUtil.closingBraces(ir.namespaces().length)).append("#endif\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.namespaces(), enumName, null));
            out.append(generateEnumDeclaration(enumName));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append("};\n\n");
            out.append(CppUtil.closingBraces(ir.namespaces().length)).append("\n#endif\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.namespaces(), compositeName,
                generateTypesToIncludes(tokens.subList(1, tokens.size() - 1))));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).encodedLength()));

            out.append(generateCompositePropertyElements(
                compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("};\n\n");
            out.append(CppUtil.closingBraces(ir.namespaces().length)).append("\n#endif\n");
        }
    }

    private static CharSequence generateChoiceNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return false;\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private CharSequence generateChoices(final String bitsetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        tokens
            .stream()
            .filter((token) -> token.signal() == Signal.CHOICE)
            .forEach((token) ->
            {
                final String choiceName = formatPropertyName(token.name());
                final String typeName = cppTypeName(token.encoding().primitiveType());
                final String choiceBitPosition = token.encoding().constValue().toString();
                final String byteOrderStr = formatByteOrderEncoding(
                    token.encoding().byteOrder(), token.encoding().primitiveType());

                sb.append(String.format("\n" +
                    "    static bool %1$s(const %2$s bits)\n" +
                    "    {\n" +
                    "        return (bits & (static_cast<%2$s>(1) << %3$s)) != 0;\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition));

                sb.append(String.format("\n" +
                    "    static %2$s %1$s(const %2$s bits, const bool value)\n" +
                    "    {\n" +
                    "        return value ?" +
                    " (bits | (static_cast<%2$s>(1) << %3$s)) : (bits & ~(static_cast<%2$s>(1) << %3$s));\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition));

                sb.append(String.format("\n" +
                    "    bool %1$s() const\n" +
                    "    {\n" +
                    "%2$s" +
                    "        %4$s val;\n" +
                    "        std::memcpy(&val, m_buffer + m_offset, sizeof(%4$s));\n" +
                    "        return (%3$s(val) & (static_cast<%4$s>(1) << %5$s)) != 0;\n" +
                    "    }\n",
                    choiceName,
                    generateChoiceNotPresentCondition(token.version(), BASE_INDENT),
                    byteOrderStr,
                    typeName,
                    choiceBitPosition));

                sb.append(String.format("\n" +
                    "    %1$s &%2$s(const bool value)\n" +
                    "    {\n" +
                    "        %3$s bits;\n" +
                    "        std::memcpy(&bits, m_buffer + m_offset, sizeof(%3$s));\n" +
                    "        bits = %4$s(value ?" +
                    " (%4$s(bits) | (static_cast<%3$s>(1) << %5$s)) " +
                    ": (%4$s(bits) & ~(static_cast<%3$s>(1) << %5$s)));\n" +
                    "        std::memcpy(m_buffer + m_offset, &bits, sizeof(%3$s));\n" +
                    "        return *this;\n" +
                    "    }\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
                    byteOrderStr,
                    choiceBitPosition));
            });

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens, final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        sb.append(
            "    enum Value\n" +
            "    {\n");

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());
            sb.append("        ").append(token.name()).append(" = ").append(constVal).append(",\n");
        }

        sb.append(String.format(
            "        NULL_VALUE = %1$s",
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString())));

        sb.append("\n    };\n\n");

        return sb;
    }

    private static CharSequence generateEnumLookupMethod(final List<Token> tokens, final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.applicableTypeName());
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "    static %1$s::Value get(const %2$s value)\n" +
            "    {\n" +
            "        switch (value)\n" +
            "        {\n",
            enumName,
            cppTypeName(tokens.get(0).encoding().primitiveType())));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %1$s: return %2$s;\n",
                token.encoding().constValue().toString(),
                token.name()));
        }

        sb.append(String.format(
            "            case %1$s: return NULL_VALUE;\n" +
            "        }\n\n" +

            "        throw std::runtime_error(\"unknown value for enum %2$s [E103]\");\n" +
            "    }\n",
            encodingToken.encoding().applicableNullValue().toString(),
            enumName));

        return sb;
    }

    private CharSequence generateFieldNotPresentCondition(
        final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return %2$s;\n" +
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
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
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
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return std::string(\"\");\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateStringViewNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return std::string_view(\"\");\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateTypeFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return nullptr;\n" +
            indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateFileHeader(
        final CharSequence[] namespaces,
        final String className,
        final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message codec */\n");

        sb.append(String.format(
            "#ifndef _%1$s_%2$s_H_\n" +
            "#define _%1$s_%2$s_H_\n\n" +

            "#if defined(SBE_HAVE_CMATH)\n" +
            "/* cmath needed for std::numeric_limits<double>::quiet_NaN() */\n" +
            "#  include <cmath>\n" +
            "#  define SBE_FLOAT_NAN std::numeric_limits<float>::quiet_NaN()\n" +
            "#  define SBE_DOUBLE_NAN std::numeric_limits<double>::quiet_NaN()\n" +
            "#else\n" +
            "/* math.h needed for NAN */\n" +
            "#  include <math.h>\n" +
            "#  define SBE_FLOAT_NAN NAN\n" +
            "#  define SBE_DOUBLE_NAN NAN\n" +
            "#endif\n\n" +

            "#if __cplusplus >= 201103L\n" +
            "#  include <cstdint>\n" +
            "#  include <string>\n" +
            "#  include <cstring>\n" +
            "#endif\n\n" +

            "#if __cplusplus >= 201103L\n" +
            "#  define SBE_CONSTEXPR constexpr\n" +
            "#  define SBE_NOEXCEPT noexcept\n" +
            "#else\n" +
            "#  define SBE_CONSTEXPR\n" +
            "#  define SBE_NOEXCEPT\n" +
            "#endif\n\n" +

            "#if !defined(__STDC_LIMIT_MACROS)\n" +
            "#  define __STDC_LIMIT_MACROS 1\n" +
            "#endif\n" +
            "#include <cstdint>\n" +
            "#include <cstring>\n" +
            "#include <limits>\n" +
            "#include <stdexcept>\n\n" +

            "#if defined(WIN32) || defined(_WIN32)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_16(v) _byteswap_ushort(v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_32(v) _byteswap_ulong(v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_64(v) _byteswap_uint64(v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_16(v) (v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_32(v) (v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_64(v) (v)\n" +
            "#elif __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_16(v) (v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_32(v) (v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_64(v) (v)\n" +
            "#elif __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_16(v) __builtin_bswap16(v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_32(v) __builtin_bswap32(v)\n" +
            "#  define SBE_LITTLE_ENDIAN_ENCODE_64(v) __builtin_bswap64(v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_16(v) (v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_32(v) (v)\n" +
            "#  define SBE_BIG_ENDIAN_ENCODE_64(v) (v)\n" +
            "#else\n" +
            "#  error \"Byte Ordering of platform not determined. " +
            "Set __BYTE_ORDER__ manually before including this file.\"\n" +
            "#endif\n\n" +

            "#if defined(SBE_NO_BOUNDS_CHECK)\n" +
            "#  define SBE_BOUNDS_CHECK_EXPECT(exp,c) (false)\n" +
            "#elif defined(_MSC_VER)\n" +
            "#  define SBE_BOUNDS_CHECK_EXPECT(exp,c) (exp)\n" +
            "#else\n" +
            "#  define SBE_BOUNDS_CHECK_EXPECT(exp,c) (__builtin_expect(exp,c))\n" +
            "#endif\n\n" +

            "#define SBE_NULLVALUE_INT8 (std::numeric_limits<std::int8_t>::min)()\n" +
            "#define SBE_NULLVALUE_INT16 (std::numeric_limits<std::int16_t>::min)()\n" +
            "#define SBE_NULLVALUE_INT32 (std::numeric_limits<std::int32_t>::min)()\n" +
            "#define SBE_NULLVALUE_INT64 (std::numeric_limits<std::int64_t>::min)()\n" +
            "#define SBE_NULLVALUE_UINT8 (std::numeric_limits<std::uint8_t>::max)()\n" +
            "#define SBE_NULLVALUE_UINT16 (std::numeric_limits<std::uint16_t>::max)()\n" +
            "#define SBE_NULLVALUE_UINT32 (std::numeric_limits<std::uint32_t>::max)()\n" +
            "#define SBE_NULLVALUE_UINT64 (std::numeric_limits<std::uint64_t>::max)()\n",
            String.join("_", namespaces).toUpperCase(),
            className.toUpperCase()));

        if (typesToInclude != null && typesToInclude.size() != 0)
        {
            sb.append("\n");
            for (final String incName : typesToInclude)
            {
                sb.append(String.format("#include \"%1$s.h\"\n", toUpperFirstChar(incName)));
            }
        }

        sb.append("\nnamespace ");
        sb.append(String.join(" {\nnamespace ", namespaces));
        sb.append(" {\n\n");

        return sb;
    }

    private static CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            "class %s\n" +
            "{\n",
            className);
    }

    private static CharSequence generateEnumDeclaration(final String name)
    {
        return "class " + name + "\n{\npublic:\n";
    }

    private CharSequence generateCompositePropertyElements(
        final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size();)
        {
            final Token fieldToken = tokens.get(i);
            final String propertyName = formatPropertyName(fieldToken.name());

            generateFieldMetaAttributeMethod(sb, fieldToken, indent);
            generateFieldCommonMethods(indent, sb, fieldToken, fieldToken, propertyName, true);

            switch (fieldToken.signal())
            {
                case ENCODING:
                    sb.append(generatePrimitiveProperty(containingClassName, propertyName, fieldToken, indent));
                    break;

                case BEGIN_ENUM:
                    sb.append(generateEnumProperty(containingClassName, fieldToken, propertyName, fieldToken, indent));
                    break;

                case BEGIN_SET:
                    sb.append(generateBitsetProperty(propertyName, fieldToken, indent));
                    break;

                case BEGIN_COMPOSITE:
                    sb.append(generateCompositeProperty(propertyName, fieldToken, indent));
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }

        return sb;
    }

    private CharSequence generatePrimitiveProperty(
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
            return generateArrayProperty(containingClassName, propertyName, token, indent);
        }

        return "";
    }

    private CharSequence generatePrimitiveFieldMetaData(
        final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final CharSequence nullValueString = generateNullValueLiteral(primitiveType, encoding);

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sNullValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            nullValueString));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sMinValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sMaxValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size() * token.arrayLength()));

        return sb;
    }

    private CharSequence generateLoadValue(
        final PrimitiveType primitiveType,
        final String offsetStr,
        final ByteOrder byteOrder,
        final String indent)
    {
        final String cppTypeName = cppTypeName(primitiveType);
        final String byteOrderStr = formatByteOrderEncoding(byteOrder, primitiveType);
        final StringBuilder sb = new StringBuilder();

        if (primitiveType == PrimitiveType.FLOAT || primitiveType == PrimitiveType.DOUBLE)
        {
            final String stackUnion =
                (primitiveType == PrimitiveType.FLOAT) ? "union sbe_float_as_uint_u" : "union sbe_double_as_uint_u";

            sb.append(String.format(
                indent + "        %1$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %2$s, sizeof(%3$s));\n" +
                indent + "        val.uint_value = %4$s(val.uint_value);\n" +
                indent + "        return val.fp_value;\n",
                stackUnion,
                offsetStr,
                cppTypeName,
                byteOrderStr));
        }
        else
        {
            sb.append(String.format(
                indent + "        %1$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %2$s, sizeof(%1$s));\n" +
                indent + "        return %3$s(val);\n",
                cppTypeName,
                offsetStr,
                byteOrderStr));
        }

        return sb;
    }

    private CharSequence generateStoreValue(
        final PrimitiveType primitiveType,
        final String valueSuffix,
        final String offsetStr,
        final ByteOrder byteOrder,
        final String indent)
    {
        final String cppTypeName = cppTypeName(primitiveType);
        final String byteOrderStr = formatByteOrderEncoding(byteOrder, primitiveType);
        final StringBuilder sb = new StringBuilder();

        if (primitiveType == PrimitiveType.FLOAT || primitiveType == PrimitiveType.DOUBLE)
        {
            final String stackUnion = primitiveType == PrimitiveType.FLOAT ?
                "union sbe_float_as_uint_u" : "union sbe_double_as_uint_u";

            sb.append(String.format(
                indent + "        %1$s val%2$s;\n" +
                indent + "        val%2$s.fp_value = value%2$s;\n" +
                indent + "        val%2$s.uint_value = %3$s(val%2$s.uint_value);\n" +
                indent + "        std::memcpy(m_buffer + m_offset + %4$s, &val%2$s, sizeof(%5$s));\n",
                stackUnion,
                valueSuffix,
                byteOrderStr,
                offsetStr,
                cppTypeName));
        }
        else
        {
            sb.append(String.format(
                indent + "        %1$s val%2$s = %3$s(value%2$s);\n" +
                indent + "        std::memcpy(m_buffer + m_offset + %4$s, &val%2$s, sizeof(%1$s));\n",
                cppTypeName,
                valueSuffix,
                byteOrderStr,
                offsetStr));
        }

        return sb;
    }

    private CharSequence generateSingleValueProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "    %1$s %2$s() const\n" +
            indent + "    {\n" +
            "%3$s" +
            "%4$s" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            generateLoadValue(primitiveType, Integer.toString(offset), token.encoding().byteOrder(), indent)));

        final CharSequence storeValue = generateStoreValue(
            primitiveType, "", Integer.toString(offset), token.encoding().byteOrder(), indent);

        sb.append(String.format("\n" +
            indent + "    %1$s &%2$s(const %3$s value)\n" +
            indent + "    {\n" +
            "%4$s" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            cppTypeName,
            storeValue));

        return sb;
    }

    private CharSequence generateArrayProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final PrimitiveType primitiveType = token.encoding().primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        final int arrayLength = token.arrayLength();
        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            arrayLength));

        sb.append(String.format("\n" +
            indent + "    const char *%1$s() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        return m_buffer + m_offset + %3$d;\n" +
            indent + "    }\n",
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            offset));

        sb.append(String.format("\n" +
            indent + "    char *%1$s() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        return m_buffer + m_offset + %3$d;\n" +
            indent + "    }\n",
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            offset));

        final CharSequence loadValue = generateLoadValue(
            primitiveType,
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            token.encoding().byteOrder(),
            indent);

        sb.append(String.format("\n" +
            indent + "    %1$s %2$s(const std::uint64_t index) const\n" +
            indent + "    {\n" +
            indent + "        if (index >= %3$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"index out of range for %2$s [E104]\");\n" +
            indent + "        }\n\n" +
            "%4$s" +
            "%5$s" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            arrayLength,
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            loadValue));

        final CharSequence storeValue = generateStoreValue(
            primitiveType,
            "",
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            token.encoding().byteOrder(),
            indent);

        sb.append(String.format("\n" +
            indent + "    %1$s &%2$s(const std::uint64_t index, const %3$s value)\n" +
            indent + "    {\n" +
            indent + "        if (index >= %4$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"index out of range for %2$s [E105]\");\n" +
            indent + "        }\n\n" +

            "%5$s" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            containingClassName,
            propertyName,
            cppTypeName,
            arrayLength,
            storeValue));

        sb.append(String.format("\n" +
            indent + "    std::uint64_t get%1$s(char *const dst, const std::uint64_t length) const\n" +
            indent + "    {\n" +
            indent + "        if (length > %2$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"length too large for get%1$s [E106]\");\n" +
            indent + "        }\n\n" +

            "%3$s" +
            indent + "        std::memcpy(dst, m_buffer + m_offset + %4$d, sizeof(%5$s) * length);\n" +
            indent + "        return length;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            arrayLength,
            generateArrayFieldNotPresentCondition(token.version(), indent),
            offset,
            cppTypeName));

        sb.append(String.format("\n" +
            indent + "    %1$s &put%2$s(const char *const src) SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        std::memcpy(m_buffer + m_offset + %3$d, src, sizeof(%4$s) * %5$d);\n" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            containingClassName,
            toUpperFirstChar(propertyName),
            offset,
            cppTypeName,
            arrayLength));

        if (arrayLength > 1 && arrayLength <= 4)
        {
            sb.append("\n").append(indent).append("    ")
                .append(containingClassName).append(" &put").append(toUpperFirstChar(propertyName))
                .append("(\n");

            for (int i = 0; i < arrayLength; i++)
            {
                sb.append(indent).append("        ")
                    .append("const ").append(cppTypeName).append(" value").append(i);

                if (i < (arrayLength - 1))
                {
                    sb.append(",\n");
                }
            }

            sb.append(") SBE_NOEXCEPT\n");
            sb.append(indent).append("    {\n");

            for (int i = 0; i < arrayLength; i++)
            {
                sb.append(generateStoreValue(
                    primitiveType,
                    Integer.toString(i),
                    Integer.toString(offset + (i * primitiveType.size())),
                    token.encoding().byteOrder(),
                    indent));
            }

            sb.append("\n");
            sb.append(indent).append("        return *this;\n");
            sb.append(indent).append("    }\n");
        }

        if (token.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            sb.append(String.format("\n" +
                indent + "    std::string get%1$sAsString() const\n" +
                indent + "    {\n" +
                indent + "        const char *buffer = m_buffer + m_offset + %2$d;\n" +
                indent + "        size_t length = 0;\n\n" +

                indent + "        for (; length < %3$d && *(buffer + length) != '\\0'; ++length);\n" +
                indent + "        std::string result(buffer, length);\n\n" +

                indent + "        return result;\n" +
                indent + "    }\n",
                toUpperFirstChar(propertyName),
                offset,
                arrayLength));

            sb.append(String.format("\n" +
                indent + "    #if __cplusplus >= 201703L\n" +
                indent + "    std::string_view get%1$sAsStringView() const SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        const char *buffer = m_buffer + m_offset + %2$d;\n" +
                indent + "        size_t length = 0;\n\n" +

                indent + "        for (; length < %3$d && *(buffer + length) != '\\0'; ++length);\n" +
                indent + "        std::string_view result(buffer, length);\n\n" +

                indent + "        return result;\n" +
                indent + "    }\n" +
                indent + "    #endif\n",
                toUpperFirstChar(propertyName),
                offset,
                arrayLength));

            sb.append(String.format("\n" +
                indent + "    #if __cplusplus >= 201703L\n" +
                indent + "    %1$s &put%2$s(const std::string_view str)\n" +
                indent + "    {\n" +
                indent + "        const size_t srcLength = str.length();\n" +
                indent + "        if (srcLength > %4$d)\n" +
                indent + "        {\n" +
                indent + "            throw std::runtime_error(\"string too large for put%2$s [E106]\");\n" +
                indent + "        }\n\n" +

                indent + "        std::memcpy(m_buffer + m_offset + %3$d, str.data(), srcLength);\n" +
                indent + "        for (size_t start = srcLength; start < %4$d; ++start)\n" +
                indent + "        {\n" +
                indent + "            m_buffer[m_offset + %3$d + start] = 0;\n" +
                indent + "        }\n\n" +

                indent + "        return *this;\n" +
                indent + "    }\n" +
                indent + "    #else\n" +
                indent + "    %1$s &put%2$s(const std::string& str)\n" +
                indent + "    {\n" +
                indent + "        const size_t srcLength = str.length();\n" +
                indent + "        if (srcLength > %4$d)\n" +
                indent + "        {\n" +
                indent + "            throw std::runtime_error(\"string too large for put%2$s [E106]\");\n" +
                indent + "        }\n\n" +

                indent + "        std::memcpy(m_buffer + m_offset + %3$d, str.c_str(), srcLength);\n" +
                indent + "        for (size_t start = srcLength; start < %4$d; ++start)\n" +
                indent + "        {\n" +
                indent + "            m_buffer[m_offset + %3$d + start] = 0;\n" +
                indent + "        }\n\n" +

                indent + "        return *this;\n" +
                indent + "    }\n" +
                indent + "    #endif\n",
                containingClassName,
                toUpperFirstChar(propertyName),
                offset,
                arrayLength));
        }

        return sb;
    }

    private CharSequence generateConstPropertyMethods(
        final String propertyName, final Token token, final String indent)
    {
        final String cppTypeName = cppTypeName(token.encoding().primitiveType());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            return String.format("\n" +
                indent + "    static SBE_CONSTEXPR %1$s %2$s() SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        return %3$s;\n" +
                indent + "    }\n",
                cppTypeName,
                propertyName,
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
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

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            constantValue.length));

        sb.append(String.format("\n" +
            indent + "    const char *%1$s() const\n" +
            indent + "    {\n" +
            indent + "        static std::uint8_t %1$sValues[] = {%2$s};\n\n" +

            indent + "        return (const char *)%1$sValues;\n" +
            indent + "    }\n",
            propertyName,
            values));

        sb.append(String.format("\n" +
            indent + "    %1$s %2$s(const std::uint64_t index) const\n" +
            indent + "    {\n" +
            indent + "        static std::uint8_t %2$sValues[] = {%3$s};\n\n" +

            indent + "        return %2$sValues[index];\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            values));

        sb.append(String.format("\n" +
            indent + "    std::uint64_t get%1$s(char *dst, const std::uint64_t length) const\n" +
            indent + "    {\n" +
            indent + "        static std::uint8_t %2$sValues[] = {%3$s};\n" +
            indent + "        std::uint64_t bytesToCopy = " +
            "length < sizeof(%2$sValues) ? length : sizeof(%2$sValues);\n\n" +

            indent + "        std::memcpy(dst, %2$sValues, bytesToCopy);\n" +
            indent + "        return bytesToCopy;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            propertyName,
            values));

        return sb;
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        final String schemaIdType = cppTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cppTypeName(ir.headerStructure().schemaVersionType());

        return String.format(
            "private:\n" +
            "    char *m_buffer = nullptr;\n" +
            "    std::uint64_t m_bufferLength;\n" +
            "    std::uint64_t m_offset = 0;\n" +
            "    std::uint64_t m_actingVersion;\n\n" +

            "public:\n" +
            "    enum MetaAttribute\n" +
            "    {\n" +
            "        EPOCH, TIME_UNIT, SEMANTIC_TYPE, PRESENCE\n" +
            "    };\n\n" +

            "    union sbe_float_as_uint_u\n" +
            "    {\n" +
            "        float fp_value;\n" +
            "        std::uint32_t uint_value;\n" +
            "    };\n\n" +

            "    union sbe_double_as_uint_u\n" +
            "    {\n" +
            "        double fp_value;\n" +
            "        std::uint64_t uint_value;\n" +
            "    };\n\n" +

            "    %1$s() = default;\n\n" +

            "    %1$s(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t offset,\n" +
            "        const std::uint64_t bufferLength,\n" +
            "        const std::uint64_t actingVersion) :\n" +
            "        m_buffer(buffer),\n" +
            "        m_bufferLength(bufferLength),\n" +
            "        m_offset(offset),\n" +
            "        m_actingVersion(actingVersion)\n" +
            "    {\n" +
            "        if (SBE_BOUNDS_CHECK_EXPECT(((m_offset + %2$s) > m_bufferLength), false))\n" +
            "        {\n" +
            "            throw std::runtime_error(\"buffer too short for flyweight [E107]\");\n" +
            "        }\n" +
            "    }\n\n" +

            "    %1$s(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t bufferLength,\n" +
            "        const std::uint64_t actingVersion) :\n" +
            "        %1$s(buffer, 0, bufferLength, actingVersion)\n" +
            "    {\n" +
            "    }\n\n" +

            "    %1$s(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t bufferLength) :\n" +
            "        %1$s(buffer, 0, bufferLength, sbeSchemaVersion())\n" +
            "    {\n" +
            "    }\n\n" +

            "    %1$s &wrap(char *buffer, const std::uint64_t offset, const std::uint64_t actingVersion," +
            " const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        return *this = %1$s(buffer, offset, bufferLength, actingVersion);\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR std::uint64_t encodedLength() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n" +

            "    std::uint64_t offset() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_offset;\n" +
            "    }\n\n" +

            "    const char * buffer() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    char * buffer() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    std::uint64_t bufferLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_bufferLength;\n" +
            "    }\n\n" +

            "    std::uint64_t actingVersion() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_actingVersion;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR %3$s sbeSchemaId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %4$s;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR %5$s sbeSchemaVersion() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %6$s;\n" +
            "    }\n",
            className,
            size,
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())));
    }

    private static CharSequence generateConstructorsAndOperators(final String className)
    {
        return String.format(
            "    %1$s() = default;\n\n" +

            "    %1$s(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t offset,\n" +
            "        const std::uint64_t bufferLength,\n" +
            "        const std::uint64_t actingBlockLength,\n" +
            "        const std::uint64_t actingVersion) :\n" +
            "        m_buffer(buffer),\n" +
            "        m_bufferLength(bufferLength),\n" +
            "        m_offset(offset),\n" +
            "        m_position(sbeCheckPosition(offset + actingBlockLength)),\n" +
            "        m_actingVersion(actingVersion)\n" +
            "    {\n" +
            "    }\n\n" +

            "    %1$s(char *buffer, const std::uint64_t bufferLength) :\n" +
            "        %1$s(buffer, 0, bufferLength, sbeBlockLength(), sbeSchemaVersion())\n" +
            "    {\n" +
            "    }\n\n" +

            "    %1$s(char *buffer, const std::uint64_t bufferLength, const std::uint64_t actingBlockLength," +
            " const std::uint64_t actingVersion) :\n" +
            "        %1$s(buffer, 0, bufferLength, actingBlockLength, actingVersion)\n" +
            "    {\n" +
            "    }\n\n",
            className);
    }

    private CharSequence generateMessageFlyweightCode(final String className, final Token token)
    {
        final String blockLengthType = cppTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = cppTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = cppTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cppTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            "private:\n" +
            "    char *m_buffer = nullptr;\n" +
            "    std::uint64_t m_bufferLength = 0;\n" +
            "    std::uint64_t m_offset = 0;\n" +
            "    std::uint64_t m_position;\n" +
            "    std::uint64_t m_actingVersion;\n\n" +

            "    inline std::uint64_t *sbePositionPtr() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return &m_position;\n" +
            "    }\n\n" +

            "public:\n" +
            "    enum MetaAttribute\n" +
            "    {\n" +
            "        EPOCH, TIME_UNIT, SEMANTIC_TYPE, PRESENCE\n" +
            "    };\n\n" +

            "    union sbe_float_as_uint_u\n" +
            "    {\n" +
            "        float fp_value;\n" +
            "        std::uint32_t uint_value;\n" +
            "    };\n\n" +

            "    union sbe_double_as_uint_u\n" +
            "    {\n" +
            "        double fp_value;\n" +
            "        std::uint64_t uint_value;\n" +
            "    };\n\n" +

            "%11$s" +
            "    static SBE_CONSTEXPR %1$s sbeBlockLength() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR %3$s sbeTemplateId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %4$s;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR %5$s sbeSchemaId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %6$s;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR %7$s sbeSchemaVersion() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %8$s;\n" +
            "    }\n\n" +

            "    static SBE_CONSTEXPR const char * sbeSemanticType() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return \"%9$s\";\n" +
            "    }\n\n" +

            "    std::uint64_t offset() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_offset;\n" +
            "    }\n\n" +

            "    %10$s &wrapForEncode(char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        return *this = %10$s(buffer, offset, bufferLength, sbeBlockLength(), sbeSchemaVersion());\n" +
            "    }\n\n" +

            "    %10$s &wrapAndApplyHeader(" +
            "char *buffer, const std::uint64_t offset, const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        MessageHeader hdr(buffer + offset, bufferLength, sbeSchemaVersion());\n\n" +

            "        hdr\n" +
            "            .blockLength(sbeBlockLength())\n" +
            "            .templateId(sbeTemplateId())\n" +
            "            .schemaId(sbeSchemaId())\n" +
            "            .version(sbeSchemaVersion());\n\n" +

            "        return *this = %10$s(\n" +
            "            buffer + offset + MessageHeader::encodedLength(),\n" +
            "            0,\n" +
            "            bufferLength - MessageHeader::encodedLength(),\n" +
            "            sbeBlockLength(),\n" +
            "            sbeSchemaVersion());\n" +
            "    }\n\n" +

            "    %10$s &wrapForDecode(\n" +
            "        char *buffer, const std::uint64_t offset, const std::uint64_t actingBlockLength,\n" +
            "        const std::uint64_t actingVersion, const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        return *this = %10$s(buffer, offset, bufferLength, actingBlockLength, actingVersion);\n" +
            "    }\n\n" +

            "    std::uint64_t sbePosition() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_position;\n" +
            "    }\n\n" +

            "    std::uint64_t sbeCheckPosition(const std::uint64_t position)\n" +
            "    {\n" +
            "        if (SBE_BOUNDS_CHECK_EXPECT((position > m_bufferLength), false))\n" +
            "        {\n" +
            "            throw std::runtime_error(\"buffer too short [E100]\");\n" +
            "        }\n" +
            "        return position;\n" +
            "    }\n\n" +

            "    void sbePosition(const std::uint64_t position)\n" +
            "    {\n" +
            "        m_position = sbeCheckPosition(position);\n" +
            "    }\n\n" +

            "    std::uint64_t encodedLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return sbePosition() - m_offset;\n" +
            "    }\n\n" +

            "    const char * buffer() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    char * buffer() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    std::uint64_t bufferLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_bufferLength;\n" +
            "    }\n\n" +

            "    std::uint64_t actingVersion() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_actingVersion;\n" +
            "    }\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            semanticType,
            className,
            generateConstructorsAndOperators(className));
    }

    private CharSequence generateFields(
        final String containingClassName, final List<Token> tokens, final String indent, final boolean inComposite)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());

                generateFieldMetaAttributeMethod(sb, signalToken, indent);
                generateFieldCommonMethods(indent, sb, signalToken, encodingToken, propertyName, inComposite);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generatePrimitiveProperty(containingClassName, propertyName, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(
                            containingClassName, signalToken, propertyName, encodingToken, indent));
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

    private void generateFieldCommonMethods(
        final String indent,
        final StringBuilder sb,
        final Token fieldToken,
        final Token encodingToken,
        final String propertyName,
        final boolean inComposite)
    {
        if (!inComposite)
        {
            sb.append(String.format("\n" +
                indent + "    static SBE_CONSTEXPR std::uint16_t %1$sId() SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        return %2$d;\n" +
                indent + "    }\n",
                propertyName,
                fieldToken.id()));
        }

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sSinceVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    bool %1$sInActingVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic push\n" +
            indent + "#pragma clang diagnostic ignored \"-Wtautological-compare\"\n" +
            indent + "#endif\n" +
            indent + "        return m_actingVersion >= %1$sSinceVersion();\n" +
            indent + "#if defined(__clang__)\n" +
            indent + "#pragma clang diagnostic pop\n" +
            indent + "#endif\n" +
            indent + "    }\n",
            propertyName,
            fieldToken.version()));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingOffset() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            encodingToken.offset()));
    }

    private static void generateFieldMetaAttributeMethod(
        final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append(String.format("\n" +
            indent + "    static const char * %sMetaAttribute(const MetaAttribute metaAttribute)" +
            " SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        switch (metaAttribute)\n" +
            indent + "        {\n" +
            indent + "            case MetaAttribute::EPOCH: return \"%s\";\n" +
            indent + "            case MetaAttribute::TIME_UNIT: return \"%s\";\n" +
            indent + "            case MetaAttribute::SEMANTIC_TYPE: return \"%s\";\n" +
            indent + "            case MetaAttribute::PRESENCE: return \"%s\";\n" +
            indent + "        }\n\n" +

            indent + "        return \"\";\n" +
            indent + "    }\n",
            token.name(),
            epoch,
            timeUnit,
            semanticType,
            encoding.presence().toString().toLowerCase()));
    }

    private static CharSequence generateEnumFieldNotPresentCondition(
        final int sinceVersion,
        final String enumName,
        final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (m_actingVersion < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return %2$s::NULL_VALUE;\n" +
            indent + "        }\n\n",
            sinceVersion,
            enumName);
    }

    private CharSequence generateEnumProperty(
        final String containingClassName,
        final Token fieldToken,
        final String propertyName,
        final Token token,
        final String indent)
    {
        final String enumName = formatClassName(token.applicableTypeName());
        final String typeName = cppTypeName(token.encoding().primitiveType());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            fieldToken.encodedLength()));

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();

            sb.append(String.format("\n" +
                indent + "    static SBE_CONSTEXPR %1$s::Value %2$sConstValue() SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        return %1$s::Value::%3$s;\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                constValue.substring(constValue.indexOf(".") + 1)));

            sb.append(String.format("\n" +
                indent + "    %1$s::Value %2$s() const SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                "%3$s" +
                indent + "        return %1$s::Value::%4$s;\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
                constValue.substring(constValue.indexOf(".") + 1)));
        }
        else
        {
            sb.append(String.format("\n" +
                indent + "    %1$s::Value %2$s() const\n" +
                indent + "    {\n" +
                "%3$s" +
                indent + "        %5$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %6$d, sizeof(%5$s));\n" +
                indent + "        return %1$s::get(%4$s(val));\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
                formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
                typeName,
                offset));

            sb.append(String.format("\n" +
                indent + "    %1$s &%2$s(const %3$s::Value value)\n" +
                indent + "    {\n" +
                indent + "        %4$s val = %6$s(value);\n" +
                indent + "        std::memcpy(m_buffer + m_offset + %5$d, &val, sizeof(%4$s));\n" +
                indent + "        return *this;\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                enumName,
                typeName,
                offset,
                formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())));
        }

        return sb;
    }

    private static Object generateBitsetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String bitsetName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();

        sb.append(String.format("\n" +
            indent + "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            indent + "public:\n",
            bitsetName,
            propertyName));

        sb.append(String.format(
            indent + "    %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrap(m_buffer, m_offset + %3$d, m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            bitsetName,
            propertyName,
            offset));

        sb.append(String.format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size()));

        return sb;
    }

    private static Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n" +
            "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            "public:\n",
            compositeName,
            propertyName));

        sb.append(String.format(
            indent + "    %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrap(m_buffer, m_offset + %3$d, m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            offset));

        return sb;
    }

    private CharSequence generateNullValueLiteral(final PrimitiveType primitiveType, final Encoding encoding)
    {
        // Visual C++ does not handle minimum integer values properly
        // See: http://msdn.microsoft.com/en-us/library/4kh09110.aspx
        // So some of the null values get special handling
        if (null == encoding.nullValue())
        {
            switch (primitiveType)
            {
                case CHAR:
                case FLOAT:
                case DOUBLE:
                    break; // no special handling
                case INT8:
                    return "SBE_NULLVALUE_INT8";
                case INT16:
                    return "SBE_NULLVALUE_INT16";
                case INT32:
                    return "SBE_NULLVALUE_INT32";
                case INT64:
                    return "SBE_NULLVALUE_INT64";
                case UINT8:
                    return "SBE_NULLVALUE_UINT8";
                case UINT16:
                    return "SBE_NULLVALUE_UINT16";
                case UINT32:
                    return "SBE_NULLVALUE_UINT32";
                case UINT64:
                    return "SBE_NULLVALUE_UINT64";
            }
        }

        return generateLiteral(primitiveType, encoding.applicableNullValue().toString());
    }

    private CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        final String castType = cppTypeName(type);
        switch (type)
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + value;
                break;

            case UINT32:
            case INT32:
                literal = value;
                break;

            case FLOAT:
                literal = value.endsWith("NaN") ? "SBE_FLOAT_NAN" : value + "f";
                break;

            case INT64:
                literal = value + "L";
                if (value.equals("-9223372036854775808"))
                {
                    literal = "INT64_MIN";
                }
                break;

            case UINT64:
                literal = "0x" + Long.toHexString(Long.parseLong(value)) + "L";
                break;

            case DOUBLE:
                literal = value.endsWith("NaN") ? "SBE_DOUBLE_NAN" : value;
                break;
        }

        return literal;
    }
}
