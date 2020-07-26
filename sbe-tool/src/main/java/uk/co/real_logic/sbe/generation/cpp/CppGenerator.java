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
package uk.co.real_logic.sbe.generation.cpp;

import org.agrona.Strings;
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
import java.util.Formatter;
import java.util.List;

import static uk.co.real_logic.sbe.generation.Generators.toLowerFirstChar;
import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;
import static uk.co.real_logic.sbe.generation.cpp.CppUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

/**
 * Codec generator for the C++11 programming language with conditional compilation for additional C++14 and C++17
 * features.
 */
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
                generateFields(sb, className, fields, BASE_INDENT);
                generateGroups(sb, groups, BASE_INDENT);
                generateVarData(sb, className, varData, BASE_INDENT);
                generateDisplay(sb, msgToken.name(), fields, groups, varData);
                sb.append(generateMessageLength(groups, varData, BASE_INDENT));
                sb.append("};\n");
                sb.append(CppUtil.closingBraces(ir.namespaces().length)).append("#endif\n");
                out.append(sb);
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
            generateFields(sb, formatClassName(groupName), fields, indent + INDENT);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(sb, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(sb, formatClassName(groupName), varData, indent + INDENT);

            sb.append(generateGroupDisplay(groupName, fields, groups, varData, indent + INDENT + INDENT));
            sb.append(generateMessageLength(groups, varData, indent + INDENT + INDENT));

            sb.append(indent).append("    };\n");
            generateGroupProperty(sb, groupName, groupToken, cppTypeForNumInGroup, indent);
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
        final String cppTypeBlockLength = cppTypeName(blockLengthToken.encoding().primitiveType());
        final String cppTypeNumInGroup = cppTypeName(numInGroupToken.encoding().primitiveType());

        new Formatter(sb).format("\n" +
            indent + "class %1$s\n" +
            indent + "{\n" +
            indent + "private:\n" +
            indent + "    char *m_buffer = nullptr;\n" +
            indent + "    std::uint64_t m_bufferLength = 0;\n" +
            indent + "    std::uint64_t m_initialPosition = 0;\n" +
            indent + "    std::uint64_t *m_positionPtr = nullptr;\n" +
            indent + "    std::uint64_t m_blockLength = 0;\n" +
            indent + "    std::uint64_t m_count = 0;\n" +
            indent + "    std::uint64_t m_index = 0;\n" +
            indent + "    std::uint64_t m_offset = 0;\n" +
            indent + "    std::uint64_t m_actingVersion = 0;\n\n" +

            indent + "    SBE_NODISCARD std::uint64_t *sbePositionPtr() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_positionPtr;\n" +
            indent + "    }\n\n" +

            indent + "public:\n",
            formatClassName(groupName));

        new Formatter(sb).format(
            indent + "    inline void wrapForDecode(\n" +
            indent + "        char *buffer,\n" +
            indent + "        std::uint64_t *pos,\n" +
            indent + "        const std::uint64_t actingVersion,\n" +
            indent + "        const std::uint64_t bufferLength)\n" +
            indent + "    {\n" +
            indent + "        %2$s dimensions(buffer, *pos, bufferLength, actingVersion);\n" +
            indent + "        m_buffer = buffer;\n" +
            indent + "        m_bufferLength = bufferLength;\n" +
            indent + "        m_blockLength = dimensions.blockLength();\n" +
            indent + "        m_count = dimensions.numInGroup();\n" +
            indent + "        m_index = 0;\n" +
            indent + "        m_actingVersion = actingVersion;\n" +
            indent + "        m_initialPosition = *pos;\n" +
            indent + "        m_positionPtr = pos;\n" +
            indent + "        *m_positionPtr = *m_positionPtr + %1$d;\n" +
            indent + "    }\n",
            dimensionHeaderLength, dimensionsClassName);

        final long minCount = numInGroupToken.encoding().applicableMinValue().longValue();
        final String minCheck = minCount > 0 ? "count < " + minCount + " || " : "";

        new Formatter(sb).format("\n" +
            indent + "    inline void wrapForEncode(\n" +
            indent + "        char *buffer,\n" +
            indent + "        const %3$s count,\n" +
            indent + "        std::uint64_t *pos,\n" +
            indent + "        const std::uint64_t actingVersion,\n" +
            indent + "        const std::uint64_t bufferLength)\n" +
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
            indent + "        %7$s dimensions(buffer, *pos, bufferLength, actingVersion);\n" +
            indent + "        dimensions.blockLength(static_cast<%1$s>(%2$d));\n" +
            indent + "        dimensions.numInGroup(static_cast<%3$s>(count));\n" +
            indent + "        m_index = 0;\n" +
            indent + "        m_count = count;\n" +
            indent + "        m_blockLength = %2$d;\n" +
            indent + "        m_actingVersion = actingVersion;\n" +
            indent + "        m_initialPosition = *pos;\n" +
            indent + "        m_positionPtr = pos;\n" +
            indent + "        *m_positionPtr = *m_positionPtr + %4$d;\n" +
            indent + "    }\n",
            cppTypeBlockLength,
            blockLength,
            cppTypeNumInGroup,
            dimensionHeaderLength,
            minCheck,
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            dimensionsClassName);

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t sbeHeaderSize() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %1$d;\n" +
            indent + "    }\n\n" +

            indent + "    static SBE_CONSTEXPR std::uint64_t sbeBlockLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    SBE_NODISCARD std::uint64_t sbePosition() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return *m_positionPtr;\n" +
            indent + "    }\n\n" +

            indent + "    // NOLINTNEXTLINE(readability-convert-member-functions-to-static)\n" +
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

            indent + "    SBE_NODISCARD inline std::uint64_t count() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_count;\n" +
            indent + "    }\n\n" +

            indent + "    SBE_NODISCARD inline bool hasNext() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return m_index < m_count;\n" +
            indent + "    }\n\n" +

            indent + "    inline %3$s &next()\n" +
            indent + "    {\n" +
            indent + "        if (m_index >= m_count)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"index >= count [E108]\");\n" +
            indent + "        }\n" +
            indent + "        m_offset = *m_positionPtr;\n" +
            indent + "        if (SBE_BOUNDS_CHECK_EXPECT(((m_offset + m_blockLength) > m_bufferLength), false))\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"buffer too short for next group index [E108]\");\n" +
            indent + "        }\n" +
            indent + "        *m_positionPtr = m_offset + m_blockLength;\n" +
            indent + "        ++m_index;\n\n" +

            indent + "        return *this;\n" +
            indent + "    }\n",
            dimensionHeaderLength,
            blockLength,
            formatClassName(groupName));

        sb.append("\n")
            .append(indent).append("    inline std::uint64_t resetCountToIndex() SBE_NOEXCEPT\n")
            .append(indent).append("    {\n")
            .append(indent).append("        m_count = m_index;\n")
            .append(indent).append("        ").append(dimensionsClassName)
            .append(" dimensions(m_buffer, m_initialPosition, m_bufferLength, m_actingVersion);\n")
            .append(indent)
            .append("        dimensions.numInGroup(static_cast<").append(cppTypeNumInGroup).append(">(m_count));\n")
            .append(indent).append("        return m_count;\n")
            .append(indent).append("    }\n");

        sb.append("\n")
            .append(indent).append("#if __cplusplus < 201103L\n")
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

    private static void generateGroupProperty(
        final StringBuilder sb,
        final String groupName,
        final Token token,
        final String cppTypeForNumInGroup,
        final String indent)
    {
        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        new Formatter(sb).format("\n" +
            "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            "public:\n",
            className,
            propertyName);

        new Formatter(sb).format(
            indent + "    SBE_NODISCARD static SBE_CONSTEXPR std::uint16_t %1$sId() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            groupName,
            token.id());

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD inline %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrapForDecode(m_buffer, sbePositionPtr(), m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            className,
            propertyName);

        new Formatter(sb).format("\n" +
            indent + "    %1$s &%2$sCount(const %3$s count)\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrapForEncode(" +
            "m_buffer, count, sbePositionPtr(), m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            className,
            propertyName,
            cppTypeForNumInGroup);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD static SBE_CONSTEXPR std::uint64_t %1$sSinceVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    SBE_NODISCARD bool %1$sInActingVersion() const SBE_NOEXCEPT\n" +
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
            token.version());
    }

    private void generateVarData(
        final StringBuilder sb, final String className, final List<Token> tokens, final String indent)
    {
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

            new Formatter(sb).format("\n" +
                indent + "    std::uint64_t skip%1$s()\n" +
                indent + "    {\n" +
                "%2$s" +
                indent + "        std::uint64_t lengthOfLengthField = %3$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        %5$s lengthFieldValue;\n" +
                indent + "        std::memcpy(&lengthFieldValue, m_buffer + lengthPosition, sizeof(%5$s));\n" +
                indent + "        std::uint64_t dataLength = %4$s(lengthFieldValue);\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField + dataLength);\n" +
                indent + "        return dataLength;\n" +
                indent + "    }\n",
                propertyName,
                generateArrayFieldNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderStr,
                lengthCppType);

            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD const char *%1$s()\n" +
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
                lengthByteOrderStr);

            new Formatter(sb).format("\n" +
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
                indent + "        std::memcpy(dst, m_buffer + pos, static_cast<size_t>(bytesToCopy));\n" +
                indent + "        return bytesToCopy;\n" +
                indent + "    }\n",
                propertyName,
                generateArrayFieldNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderStr,
                lengthCppType);

            new Formatter(sb).format("\n" +
                indent + "    %5$s &put%1$s(const char *src, const %3$s length)\n" +
                indent + "    {\n" +
                indent + "        std::uint64_t lengthOfLengthField = %2$d;\n" +
                indent + "        std::uint64_t lengthPosition = sbePosition();\n" +
                indent + "        %3$s lengthFieldValue = %4$s(length);\n" +
                indent + "        sbePosition(lengthPosition + lengthOfLengthField);\n" +
                indent + "        std::memcpy(m_buffer + lengthPosition, &lengthFieldValue, sizeof(%3$s));\n" +
                indent + "        if (length != %3$s(0))\n" +
                indent + "        {\n" +
                indent + "            std::uint64_t pos = sbePosition();\n" +
                indent + "            sbePosition(pos + length);\n" +
                indent + "            std::memcpy(m_buffer + pos, src, length);\n" +
                indent + "        }\n" +
                indent + "        return *this;\n" +
                indent + "    }\n",
                propertyName,
                lengthOfLengthField,
                lengthCppType,
                lengthByteOrderStr,
                className);

            new Formatter(sb).format("\n" +
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
                lengthCppType);

            generateJsonEscapedStringGetter(sb, token, indent, propertyName);

            new Formatter(sb).format("\n" +
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
                lengthCppType);

            new Formatter(sb).format("\n" +
                indent + "    %1$s &put%2$s(const std::string& str)\n" +
                indent + "    {\n" +
                indent + "        if (str.length() > %4$d)\n" +
                indent + "        {\n" +
                indent + "            throw std::runtime_error(\"std::string too long for length type [E109]\");\n" +
                indent + "        }\n" +
                indent + "        return put%2$s(str.data(), static_cast<%3$s>(str.length()));\n" +
                indent + "    }\n",
                className,
                propertyName,
                lengthCppType,
                lengthToken.encoding().applicableMaxValue().longValue());

            new Formatter(sb).format("\n" +
                indent + "    #if __cplusplus >= 201703L\n" +
                indent + "    %1$s &put%2$s(const std::string_view str)\n" +
                indent + "    {\n" +
                indent + "        if (str.length() > %4$d)\n" +
                indent + "        {\n" +
                indent + "            throw std::runtime_error(\"std::string too long for length type [E109]\");\n" +
                indent + "        }\n" +
                indent + "        return put%2$s(str.data(), static_cast<%3$s>(str.length()));\n" +
                indent + "    }\n" +
                indent + "    #endif\n",
                className,
                propertyName,
                lengthCppType,
                lengthToken.encoding().applicableMaxValue().longValue());

            i += token.componentTokenCount();
        }
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
        new Formatter(sb).format("\n" +
            indent + "    static const char *%1$sCharacterEncoding() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return \"%2$s\";\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            characterEncoding);

        new Formatter(sb).format("\n" +
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
            token.id());

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %sHeaderLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            sizeOfLengthField);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD %4$s %1$sLength() const\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        %4$s length;\n" +
            indent + "        std::memcpy(&length, m_buffer + sbePosition(), sizeof(%4$s));\n" +
            indent + "        return %3$s(length);\n" +
            indent + "    }\n",
            toLowerFirstChar(propertyName),
            generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
            formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
            lengthCppType);
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.namespaces(), bitSetName, null));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).encodedLength()));

            new Formatter(out).format("\n" +
                "    %1$s &clear()\n" +
                "    {\n" +
                "        %2$s zero = 0;\n" +
                "        std::memcpy(m_buffer + m_offset, &zero, sizeof(%2$s));\n" +
                "        return *this;\n" +
                "    }\n",
                bitSetName,
                cppTypeName(tokens.get(0).encoding().primitiveType()));

            new Formatter(out).format("\n" +
                "    SBE_NODISCARD bool isEmpty() const\n" +
                "    {\n" +
                "        %1$s val;\n" +
                "        std::memcpy(&val, m_buffer + m_offset, sizeof(%1$s));\n" +
                "        return 0 == val;\n" +
                "    }\n",
                cppTypeName(tokens.get(0).encoding().primitiveType()));

            new Formatter(out).format("\n" +
                "    SBE_NODISCARD %1$s rawValue() const\n" +
                "    {\n" +
                "        %1$s val;\n" +
                "        std::memcpy(&val, m_buffer + m_offset, sizeof(%1$s));\n" +
                "        return val;\n" +
                "    }\n",
                cppTypeName(tokens.get(0).encoding().primitiveType()));

            new Formatter(out).format("\n" +
                "    %1$s &rawValue(%2$s value)\n" +
                "    {\n" +
                "        std::memcpy(m_buffer + m_offset, &value, sizeof(%2$s));\n" +
                "        return *this;\n" +
                "    }\n",
                bitSetName,
                cppTypeName(tokens.get(0).encoding().primitiveType()));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));
            out.append(generateChoicesDisplay(bitSetName, tokens.subList(1, tokens.size() - 1)));
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

            out.append(generateEnumDisplay(tokens.subList(1, tokens.size() - 1), enumToken));

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

            out.append(generateCompositeDisplay(
                tokens.get(0).applicableTypeName(), tokens.subList(1, tokens.size() - 1)));

            out.append("};\n\n");
            out.append(CppUtil.closingBraces(ir.namespaces().length)).append("\n#endif\n");
        }
    }

    private static CharSequence generateChoiceNotPresentCondition(final int sinceVersion)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            "        if (m_actingVersion < %1$d)\n" +
            "        {\n" +
            "            return false;\n" +
            "        }\n\n",
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

                new Formatter(sb).format("\n" +
                    "    static bool %1$s(const %2$s bits)\n" +
                    "    {\n" +
                    "        return (bits & (1u << %3$su)) != 0;\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition);

                new Formatter(sb).format("\n" +
                    "    static %2$s %1$s(const %2$s bits, const bool value)\n" +
                    "    {\n" +
                    "        return value ?" +
                    " static_cast<%2$s>(bits | (1u << %3$su)) : static_cast<%2$s>(bits & ~(1u << %3$su));\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition);

                new Formatter(sb).format("\n" +
                    "    SBE_NODISCARD bool %1$s() const\n" +
                    "    {\n" +
                    "%2$s" +
                    "        %4$s val;\n" +
                    "        std::memcpy(&val, m_buffer + m_offset, sizeof(%4$s));\n" +
                    "        return (%3$s(val) & (1u << %5$su)) != 0;\n" +
                    "    }\n",
                    choiceName,
                    generateChoiceNotPresentCondition(token.version()),
                    byteOrderStr,
                    typeName,
                    choiceBitPosition);

                new Formatter(sb).format("\n" +
                    "    %1$s &%2$s(const bool value)\n" +
                    "    {\n" +
                    "        %3$s bits;\n" +
                    "        std::memcpy(&bits, m_buffer + m_offset, sizeof(%3$s));\n" +
                    "        bits = %4$s(value ?" +
                    " static_cast<%3$s>(%4$s(bits) | (1u << %5$su)) " +
                    ": static_cast<%3$s>(%4$s(bits) & ~(1u << %5$su)));\n" +
                    "        std::memcpy(m_buffer + m_offset, &bits, sizeof(%3$s));\n" +
                    "        return *this;\n" +
                    "    }\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
                    byteOrderStr,
                    choiceBitPosition);
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

        new Formatter(sb).format(
            "    static %1$s::Value get(const %2$s value)\n" +
            "    {\n" +
            "        switch (value)\n" +
            "        {\n",
            enumName,
            cppTypeName(tokens.get(0).encoding().primitiveType()));

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());

            sb.append("            case ").append(constVal).append(": return ").append(token.name()).append(";\n");
        }

        final CharSequence constVal = generateLiteral(
            encodingToken.encoding().primitiveType(), encodingToken.encoding().applicableNullValue().toString());

        new Formatter(sb).format(
            "            case %1$s: return NULL_VALUE;\n" +
            "        }\n\n" +

            "        throw std::runtime_error(\"unknown value for enum %2$s [E103]\");\n" +
            "    }\n",
            constVal,
            enumName);

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
            "#  define SBE_CONSTEXPR constexpr\n" +
            "#  define SBE_NOEXCEPT noexcept\n" +
            "#else\n" +
            "#  define SBE_CONSTEXPR\n" +
            "#  define SBE_NOEXCEPT\n" +
            "#endif\n\n" +

            "#if __cplusplus >= 201703L\n" +
            "#  include <string_view>\n" +
            "#  define SBE_NODISCARD [[nodiscard]]\n" +
            "#else\n" +
            "#  define SBE_NODISCARD\n" +
            "#endif\n\n" +

            "#if !defined(__STDC_LIMIT_MACROS)\n" +
            "#  define __STDC_LIMIT_MACROS 1\n" +
            "#endif\n\n" +

            "#include <cstdint>\n" +
            "#include <cstring>\n" +
            "#include <iomanip>\n" +
            "#include <limits>\n" +
            "#include <ostream>\n" +
            "#include <stdexcept>\n" +
            "#include <sstream>\n" +
            "#include <string>\n" +
            "#include <vector>\n" +
            "#include <tuple>\n" +
            "\n" +

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
            "#define SBE_NULLVALUE_UINT64 (std::numeric_limits<std::uint64_t>::max)()\n\n",
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
        return
            "class " + className + "\n" +
            "{\n";
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
            generateFieldCommonMethods(indent, sb, fieldToken, fieldToken, propertyName);

            switch (fieldToken.signal())
            {
                case ENCODING:
                    generatePrimitiveProperty(sb, containingClassName, propertyName, fieldToken, fieldToken, indent);
                    break;

                case BEGIN_ENUM:
                    generateEnumProperty(sb, containingClassName, fieldToken, propertyName, fieldToken, indent);
                    break;

                case BEGIN_SET:
                    generateBitsetProperty(sb, propertyName, fieldToken, indent);
                    break;

                case BEGIN_COMPOSITE:
                    generateCompositeProperty(sb, propertyName, fieldToken, indent);
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }

        return sb;
    }

    private void generatePrimitiveProperty(
        final StringBuilder sb,
        final String containingClassName,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        generatePrimitiveFieldMetaData(sb, propertyName, encodingToken, indent);

        if (encodingToken.isConstantEncoding())
        {
            generateConstPropertyMethods(sb, propertyName, encodingToken, indent);
        }
        else
        {
            generatePrimitivePropertyMethods(
                sb, containingClassName, propertyName, propertyToken, encodingToken, indent);
        }
    }

    private void generatePrimitivePropertyMethods(
        final StringBuilder sb,
        final String containingClassName,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final int arrayLength = encodingToken.arrayLength();
        if (arrayLength == 1)
        {
            generateSingleValueProperty(sb, containingClassName, propertyName, propertyToken, encodingToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayProperty(sb, containingClassName, propertyName, propertyToken, encodingToken, indent);
        }
    }

    private void generatePrimitiveFieldMetaData(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final CharSequence nullValueString = generateNullValueLiteral(primitiveType, encoding);

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sNullValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            nullValueString);

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sMinValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString()));

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR %1$s %2$sMaxValue() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString()));

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size() * token.arrayLength());
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
                primitiveType == PrimitiveType.FLOAT ? "union sbe_float_as_uint_u" : "union sbe_double_as_uint_u";

            new Formatter(sb).format(
                indent + "        %1$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %2$s, sizeof(%3$s));\n" +
                indent + "        val.uint_value = %4$s(val.uint_value);\n" +
                indent + "        return val.fp_value;\n",
                stackUnion,
                offsetStr,
                cppTypeName,
                byteOrderStr);
        }
        else
        {
            new Formatter(sb).format(
                indent + "        %1$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %2$s, sizeof(%1$s));\n" +
                indent + "        return %3$s(val);\n",
                cppTypeName,
                offsetStr,
                byteOrderStr);
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

            new Formatter(sb).format(
                indent + "        %1$s val%2$s;\n" +
                indent + "        val%2$s.fp_value = value%2$s;\n" +
                indent + "        val%2$s.uint_value = %3$s(val%2$s.uint_value);\n" +
                indent + "        std::memcpy(m_buffer + m_offset + %4$s, &val%2$s, sizeof(%5$s));\n",
                stackUnion,
                valueSuffix,
                byteOrderStr,
                offsetStr,
                cppTypeName);
        }
        else
        {
            new Formatter(sb).format(
                indent + "        %1$s val%2$s = %3$s(value%2$s);\n" +
                indent + "        std::memcpy(m_buffer + m_offset + %4$s, &val%2$s, sizeof(%1$s));\n",
                cppTypeName,
                valueSuffix,
                byteOrderStr,
                offsetStr);
        }

        return sb;
    }

    private void generateSingleValueProperty(
        final StringBuilder sb,
        final String containingClassName,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final PrimitiveType primitiveType = encodingToken.encoding().primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final int offset = encodingToken.offset();

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD %1$s %2$s() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%3$s" +
            "%4$s" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            generateFieldNotPresentCondition(propertyToken.version(), encodingToken.encoding(), indent),
            generateLoadValue(primitiveType, Integer.toString(offset), encodingToken.encoding().byteOrder(), indent));

        final CharSequence storeValue = generateStoreValue(
            primitiveType, "", Integer.toString(offset), encodingToken.encoding().byteOrder(), indent);

        new Formatter(sb).format("\n" +
            indent + "    %1$s &%2$s(const %3$s value) SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%4$s" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            cppTypeName,
            storeValue);
    }

    private void generateArrayProperty(
        final StringBuilder sb,
        final String containingClassName,
        final String propertyName,
        final Token propertyToken,
        final Token encodingToken,
        final String indent)
    {
        final PrimitiveType primitiveType = encodingToken.encoding().primitiveType();
        final String cppTypeName = cppTypeName(primitiveType);
        final int offset = encodingToken.offset();

        final int arrayLength = encodingToken.arrayLength();
        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            arrayLength);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD const char *%1$s() const SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        return m_buffer + m_offset + %3$d;\n" +
            indent + "    }\n",
            propertyName,
            generateTypeFieldNotPresentCondition(propertyToken.version(), indent),
            offset);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD char *%1$s() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        return m_buffer + m_offset + %3$d;\n" +
            indent + "    }\n",
            propertyName,
            generateTypeFieldNotPresentCondition(propertyToken.version(), indent),
            offset);

        final CharSequence loadValue = generateLoadValue(
            primitiveType,
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            encodingToken.encoding().byteOrder(),
            indent);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD %1$s %2$s(const std::uint64_t index) const\n" +
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
            generateFieldNotPresentCondition(propertyToken.version(), encodingToken.encoding(), indent),
            loadValue);

        final CharSequence storeValue = generateStoreValue(
            primitiveType,
            "",
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            encodingToken.encoding().byteOrder(),
            indent);

        new Formatter(sb).format("\n" +
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
            storeValue);

        new Formatter(sb).format("\n" +
            indent + "    std::uint64_t get%1$s(char *const dst, const std::uint64_t length) const\n" +
            indent + "    {\n" +
            indent + "        if (length > %2$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"length too large for get%1$s [E106]\");\n" +
            indent + "        }\n\n" +

            "%3$s" +
            indent + "        std::memcpy(dst, m_buffer + m_offset + %4$d, " +
            "sizeof(%5$s) * static_cast<size_t>(length));\n" +
            indent + "        return length;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            arrayLength,
            generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
            offset,
            cppTypeName);

        new Formatter(sb).format("\n" +
            indent + "    %1$s &put%2$s(const char *const src) SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        std::memcpy(m_buffer + m_offset + %3$d, src, sizeof(%4$s) * %5$d);\n" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            containingClassName,
            toUpperFirstChar(propertyName),
            offset,
            cppTypeName,
            arrayLength);

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
                    encodingToken.encoding().byteOrder(),
                    indent));
            }

            sb.append("\n");
            sb.append(indent).append("        return *this;\n");
            sb.append(indent).append("    }\n");
        }

        if (encodingToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD std::string get%1$sAsString() const\n" +
                indent + "    {\n" +
                indent + "        const char *buffer = m_buffer + m_offset + %2$d;\n" +
                indent + "        size_t length = 0;\n\n" +

                indent + "        for (; length < %3$d && *(buffer + length) != '\\0'; ++length);\n" +
                indent + "        std::string result(buffer, length);\n\n" +

                indent + "        return result;\n" +
                indent + "    }\n",
                toUpperFirstChar(propertyName),
                offset,
                arrayLength);

            generateJsonEscapedStringGetter(sb, encodingToken, indent, propertyName);

            new Formatter(sb).format("\n" +
                indent + "    #if __cplusplus >= 201703L\n" +
                indent + "    SBE_NODISCARD std::string_view get%1$sAsStringView() const SBE_NOEXCEPT\n" +
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
                arrayLength);

            new Formatter(sb).format("\n" +
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
                arrayLength);
        }
    }

    private void generateJsonEscapedStringGetter(
        final StringBuilder sb, final Token token, final String indent, final String propertyName)
    {
        new Formatter(sb).format("\n" +
            indent + "    std::string get%1$sAsJsonEscapedString()\n" +
            indent + "    {\n" +
            "%2$s" +
            indent + "        std::ostringstream oss;\n" +
            indent + "        std::string s = get%1$sAsString();\n\n" +
            indent + "        for (const auto c : s)\n" +
            indent + "        {\n" +
            indent + "            switch (c)\n" +
            indent + "            {\n" +
            indent + "                case '\"': oss << \"\\\\\\\"\"; break;\n" +
            indent + "                case '\\\\': oss << \"\\\\\\\\\"; break;\n" +
            indent + "                case '\\b': oss << \"\\\\b\"; break;\n" +
            indent + "                case '\\f': oss << \"\\\\f\"; break;\n" +
            indent + "                case '\\n': oss << \"\\\\n\"; break;\n" +
            indent + "                case '\\r': oss << \"\\\\r\"; break;\n" +
            indent + "                case '\\t': oss << \"\\\\t\"; break;\n\n" +
            indent + "                default:\n" +
            indent + "                    if ('\\x00' <= c && c <= '\\x1f')\n" +
            indent + "                    {\n" +
            indent + "                        oss << \"\\\\u\"" + " << std::hex << std::setw(4)\n" +
            indent + "                            << std::setfill('0') << (int)(c);\n" +
            indent + "                    }\n" +
            indent + "                    else\n" +
            indent + "                    {\n" +
            indent + "                        oss << c;\n" +
            indent + "                    }\n" +
            indent + "            }\n" +
            indent + "        }\n\n" +
            indent + "        return oss.str();\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            generateStringNotPresentCondition(token.version(), indent));
    }

    private void generateConstPropertyMethods(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final String cppTypeName = cppTypeName(token.encoding().primitiveType());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD static SBE_CONSTEXPR %1$s %2$s() SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        return %3$s;\n" +
                indent + "    }\n",
                cppTypeName,
                propertyName,
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));

            return;
        }

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

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint64_t %1$sLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            constantValue.length);

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD const char *%1$s() const\n" +
            indent + "    {\n" +
            indent + "        static const std::uint8_t %1$sValues[] = { %2$s };\n\n" +

            indent + "        return (const char *)%1$sValues;\n" +
            indent + "    }\n",
            propertyName,
            values);

        sb.append(String.format("\n" +
            indent + "    SBE_NODISCARD %1$s %2$s(const std::uint64_t index) const\n" +
            indent + "    {\n" +
            indent + "        static const std::uint8_t %2$sValues[] = { %3$s };\n\n" +

            indent + "        return (char)%2$sValues[index];\n" +
            indent + "    }\n",
            cppTypeName,
            propertyName,
            values));

        new Formatter(sb).format("\n" +
            indent + "    std::uint64_t get%1$s(char *dst, const std::uint64_t length) const\n" +
            indent + "    {\n" +
            indent + "        static std::uint8_t %2$sValues[] = { %3$s };\n" +
            indent + "        std::uint64_t bytesToCopy = " +
            "length < sizeof(%2$sValues) ? length : sizeof(%2$sValues);\n\n" +

            indent + "        std::memcpy(dst, %2$sValues, static_cast<size_t>(bytesToCopy));\n" +
            indent + "        return bytesToCopy;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            propertyName,
            values);

        new Formatter(sb).format("\n" +
            indent + "    std::string get%1$sAsString() const\n" +
            indent + "    {\n" +
            indent + "        static const std::uint8_t %1$sValues[] = { %2$s };\n\n" +
            indent + "        return std::string((const char *)%1$sValues, %3$s);\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            values,
            constantValue.length);

        generateJsonEscapedStringGetter(sb, token, indent, propertyName);
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        final String schemaIdType = cppTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cppTypeName(ir.headerStructure().schemaVersionType());

        return String.format(
            "private:\n" +
            "    char *m_buffer = nullptr;\n" +
            "    std::uint64_t m_bufferLength = 0;\n" +
            "    std::uint64_t m_offset = 0;\n" +
            "    std::uint64_t m_actingVersion = 0;\n\n" +

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

            "    %1$s &wrap(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t offset,\n" +
            "        const std::uint64_t actingVersion,\n" +
            "        const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        return *this = %1$s(buffer, offset, bufferLength, actingVersion);\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR std::uint64_t encodedLength() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t offset() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_offset;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD const char *buffer() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD char *buffer() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t bufferLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_bufferLength;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t actingVersion() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_actingVersion;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR %3$s sbeSchemaId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %4$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR %5$s sbeSchemaVersion() SBE_NOEXCEPT\n" +
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

            "    %1$s(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t bufferLength,\n" +
            "        const std::uint64_t actingBlockLength,\n" +
            "        const std::uint64_t actingVersion) :\n" +
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
        final String headerType = ir.headerStructure().tokens().get(0).name();

        return String.format(
            "private:\n" +
            "    char *m_buffer = nullptr;\n" +
            "    std::uint64_t m_bufferLength = 0;\n" +
            "    std::uint64_t m_offset = 0;\n" +
            "    std::uint64_t m_position = 0;\n" +
            "    std::uint64_t m_actingVersion = 0;\n\n" +

            "    inline std::uint64_t *sbePositionPtr() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return &m_position;\n" +
            "    }\n\n" +

            "public:\n" +
            "    static const %1$s SBE_BLOCK_LENGTH = %2$s;\n" +
            "    static const %3$s SBE_TEMPLATE_ID = %4$s;\n" +
            "    static const %5$s SBE_SCHEMA_ID = %6$s;\n" +
            "    static const %7$s SBE_SCHEMA_VERSION = %8$s;\n\n" +

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

            "    using messageHeader = %12$s;\n\n" +

            "%11$s" +
            "    SBE_NODISCARD static SBE_CONSTEXPR %1$s sbeBlockLength() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR std::uint64_t sbeBlockAndHeaderLength() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return messageHeader::encodedLength() + sbeBlockLength();\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR %3$s sbeTemplateId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %4$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR %5$s sbeSchemaId() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %6$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR %7$s sbeSchemaVersion() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return %8$s;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD static SBE_CONSTEXPR const char *sbeSemanticType() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return \"%9$s\";\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t offset() const SBE_NOEXCEPT\n" +
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
            "        messageHeader hdr(buffer, offset, bufferLength, sbeSchemaVersion());\n\n" +

            "        hdr\n" +
            "            .blockLength(sbeBlockLength())\n" +
            "            .templateId(sbeTemplateId())\n" +
            "            .schemaId(sbeSchemaId())\n" +
            "            .version(sbeSchemaVersion());\n\n" +

            "        return *this = %10$s(\n" +
            "            buffer,\n" +
            "            offset + messageHeader::encodedLength(),\n" +
            "            bufferLength,\n" +
            "            sbeBlockLength(),\n" +
            "            sbeSchemaVersion());\n" +
            "    }\n\n" +

            "    %10$s &wrapForDecode(\n" +
            "        char *buffer,\n" +
            "        const std::uint64_t offset,\n" +
            "        const std::uint64_t actingBlockLength,\n" +
            "        const std::uint64_t actingVersion,\n" +
            "        const std::uint64_t bufferLength)\n" +
            "    {\n" +
            "        return *this = %10$s(buffer, offset, bufferLength, actingBlockLength, actingVersion);\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t sbePosition() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_position;\n" +
            "    }\n\n" +

            "    // NOLINTNEXTLINE(readability-convert-member-functions-to-static)\n" +
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

            "    SBE_NODISCARD std::uint64_t encodedLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return sbePosition() - m_offset;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t decodeLength() const\n" +
            "    {\n" +
            "        %10$s skipper(m_buffer, m_offset, m_bufferLength, sbeBlockLength(), m_actingVersion);\n" +
            "        skipper.skip();\n" +
            "        return skipper.encodedLength();\n" +
            "    }\n\n" +

            "    SBE_NODISCARD const char *buffer() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD char *buffer() SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_buffer;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t bufferLength() const SBE_NOEXCEPT\n" +
            "    {\n" +
            "        return m_bufferLength;\n" +
            "    }\n\n" +

            "    SBE_NODISCARD std::uint64_t actingVersion() const SBE_NOEXCEPT\n" +
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
            generateConstructorsAndOperators(className),
            formatClassName(headerType));
    }

    private void generateFields(
        final StringBuilder sb,
        final String containingClassName,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());

                generateFieldMetaAttributeMethod(sb, signalToken, indent);
                generateFieldCommonMethods(indent, sb, signalToken, encodingToken, propertyName);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        generatePrimitiveProperty(
                            sb, containingClassName, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_ENUM:
                        generateEnumProperty(sb, containingClassName, signalToken, propertyName, encodingToken, indent);
                        break;

                    case BEGIN_SET:
                        generateBitsetProperty(sb, propertyName, encodingToken, indent);
                        break;

                    case BEGIN_COMPOSITE:
                        generateCompositeProperty(sb, propertyName, encodingToken, indent);
                        break;
                }
            }
        }
    }

    private void generateFieldCommonMethods(
        final String indent,
        final StringBuilder sb,
        final Token fieldToken,
        final Token encodingToken,
        final String propertyName)
    {
        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::uint16_t %1$sId() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            fieldToken.id());

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD static SBE_CONSTEXPR std::uint64_t %1$sSinceVersion() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +

            indent + "    SBE_NODISCARD bool %1$sInActingVersion() SBE_NOEXCEPT\n" +
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
            fieldToken.version());

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD static SBE_CONSTEXPR std::size_t %1$sEncodingOffset() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            encodingToken.offset());
    }

    private static void generateFieldMetaAttributeMethod(
        final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append("\n")
            .append(indent).append("    SBE_NODISCARD static const char *")
            .append(token.name()).append("MetaAttribute(const MetaAttribute metaAttribute) SBE_NOEXCEPT\n")
            .append(indent).append("    {\n")
            .append(indent).append("        switch (metaAttribute)\n")
            .append(indent).append("        {\n");

        if (!Strings.isEmpty(epoch))
        {
            sb.append(indent)
                .append("            case MetaAttribute::EPOCH: return \"").append(epoch).append("\";\n");
        }

        if (!Strings.isEmpty(timeUnit))
        {
            sb.append(indent)
                .append("            case MetaAttribute::TIME_UNIT: return \"").append(timeUnit).append("\";\n");
        }

        if (!Strings.isEmpty(semanticType))
        {
            sb.append(indent)
                .append("            case MetaAttribute::SEMANTIC_TYPE: return \"").append(semanticType)
                .append("\";\n");
        }

        sb
            .append(indent).append("            case MetaAttribute::PRESENCE: return \"")
            .append(encoding.presence().toString().toLowerCase()).append("\";\n")
            .append(indent).append("            default: return \"\";\n")
            .append(indent).append("        }\n")
            .append(indent).append("    }\n");
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

    private void generateEnumProperty(
        final StringBuilder sb,
        final String containingClassName,
        final Token fieldToken,
        final String propertyName,
        final Token token,
        final String indent)
    {
        final String enumName = formatClassName(token.applicableTypeName());
        final String typeName = cppTypeName(token.encoding().primitiveType());
        final int offset = token.offset();

        new Formatter(sb).format("\n" +
            indent + "    SBE_NODISCARD static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            fieldToken.encodedLength());

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();

            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD static SBE_CONSTEXPR %1$s::Value %2$sConstValue() SBE_NOEXCEPT\n" +
                indent + "    {\n" +
                indent + "        return %1$s::Value::%3$s;\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                constValue.substring(constValue.indexOf(".") + 1));

            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD %1$s::Value %2$s() const\n" +
                indent + "    {\n" +
                "%3$s" +
                indent + "        return %1$s::Value::%4$s;\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                generateEnumFieldNotPresentCondition(fieldToken.version(), enumName, indent),
                constValue.substring(constValue.indexOf(".") + 1));
        }
        else
        {
            new Formatter(sb).format("\n" +
                indent + "    SBE_NODISCARD %1$s::Value %2$s() const\n" +
                indent + "    {\n" +
                "%3$s" +
                indent + "        %5$s val;\n" +
                indent + "        std::memcpy(&val, m_buffer + m_offset + %6$d, sizeof(%5$s));\n" +
                indent + "        return %1$s::get(%4$s(val));\n" +
                indent + "    }\n",
                enumName,
                propertyName,
                generateEnumFieldNotPresentCondition(fieldToken.version(), enumName, indent),
                formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
                typeName,
                offset);

            new Formatter(sb).format("\n" +
                indent + "    %1$s &%2$s(const %3$s::Value value) SBE_NOEXCEPT\n" +
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
                formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()));
        }
    }

    private static void generateBitsetProperty(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final String bitsetName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();

        new Formatter(sb).format("\n" +
            indent + "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            indent + "public:\n",
            bitsetName,
            propertyName);

        new Formatter(sb).format(
            indent + "    SBE_NODISCARD %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrap(m_buffer, m_offset + %3$d, m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            bitsetName,
            propertyName,
            offset);

        new Formatter(sb).format("\n" +
            indent + "    static SBE_CONSTEXPR std::size_t %1$sEncodingLength() SBE_NOEXCEPT\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size());
    }

    private static void generateCompositeProperty(
        final StringBuilder sb, final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.applicableTypeName());

        new Formatter(sb).format("\n" +
            "private:\n" +
            indent + "    %1$s m_%2$s;\n\n" +

            "public:\n",
            compositeName,
            propertyName);

        new Formatter(sb).format(
            indent + "    SBE_NODISCARD %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        m_%2$s.wrap(m_buffer, m_offset + %3$d, m_actingVersion, m_bufferLength);\n" +
            indent + "        return m_%2$s;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            token.offset());
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

    private static CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        switch (type)
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "static_cast<" + cppTypeName(type) + ">(" + value + ")";
                break;

            case UINT32:
                literal = "UINT32_C(0x" + Integer.toHexString((int)Long.parseLong(value)) + ")";
                break;

            case INT32:
                final long intValue = Long.parseLong(value);
                if (intValue == Integer.MIN_VALUE)
                {
                    literal = "INT32_MIN";
                }
                else
                {
                    literal = "INT32_C(" + value + ")";
                }
                break;

            case FLOAT:
                literal = value.endsWith("NaN") ? "SBE_FLOAT_NAN" : value + "f";
                break;

            case INT64:
                final long longValue = Long.parseLong(value);
                if (longValue == Long.MIN_VALUE)
                {
                    literal = "INT64_MIN";
                }
                else
                {
                    literal = "INT64_C(" + value + ")";
                }
                break;

            case UINT64:
                literal = "UINT64_C(0x" + Long.toHexString(Long.parseLong(value)) + ")";
                break;

            case DOUBLE:
                literal = value.endsWith("NaN") ? "SBE_DOUBLE_NAN" : value;
                break;
        }

        return literal;
    }

    private void generateDisplay(
        final StringBuilder sb,
        final String name,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        new Formatter(sb).format("\n" +
            "template<typename CharT, typename Traits>\n" +
            "friend std::basic_ostream<CharT, Traits>& operator << (\n" +
            "    std::basic_ostream<CharT, Traits>& builder, %1$s _writer)\n" +
            "{\n" +
            "    %1$s writer(_writer.m_buffer, _writer.m_offset,\n" +
            "        _writer.m_bufferLength, _writer.sbeBlockLength(), _writer.m_actingVersion);\n" +
            "    builder << '{';\n" +
            "    builder << R\"(\"Name\": \"%1$s\", )\";\n" +
            "    builder << R\"(\"sbeTemplateId\": )\";\n" +
            "    builder << writer.sbeTemplateId();\n" +
            "    builder << \", \";\n\n" +
            "%2$s" +
            "    builder << '}';\n\n" +
            "    return builder;\n" +
            "}\n",
            formatClassName(name),
            appendDisplay(fields, groups, varData, INDENT));
    }

    private CharSequence generateGroupDisplay(
        final String name,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        return String.format("\n" +
            indent + "template<typename CharT, typename Traits>\n" +
            indent + "friend std::basic_ostream<CharT, Traits>& operator << (\n" +
            indent + "    std::basic_ostream<CharT, Traits>& builder, %1$s writer)\n" +
            indent + "{\n" +
            indent + "    builder << '{';\n" +
            "%2$s" +
            indent + "    builder << '}';\n\n" +
            indent + "    return builder;\n" +
            indent + "}\n",
            formatClassName(name),
            appendDisplay(fields, groups, varData, indent + INDENT));
    }

    private CharSequence generateCompositeDisplay(final String name, final List<Token> tokens)
    {
        return String.format("\n" +
            "template<typename CharT, typename Traits>\n" +
            "friend std::basic_ostream<CharT, Traits>& operator << (\n" +
            "    std::basic_ostream<CharT, Traits>& builder, %1$s writer)\n" +
            "{\n" +
            "    builder << '{';\n" +
            "%2$s" +
            "    builder << '}';\n\n" +
            "    return builder;\n" +
            "}\n\n",
            formatClassName(name),
            appendDisplay(tokens, new ArrayList<>(), new ArrayList<>(), INDENT));
    }

    private CharSequence appendDisplay(
        final List<Token> fields, final List<Token> groups, final List<Token> varData, final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final boolean[] atLeastOne = { false };

        for (int i = 0, size = fields.size(); i < size;)
        {
            final Token fieldToken = fields.get(i);
            final Token encodingToken = fields.get(fieldToken.signal() == Signal.BEGIN_FIELD ? i + 1 : i);

            writeTokenDisplay(sb, fieldToken.name(), encodingToken, atLeastOne, indent);
            i += fieldToken.componentTokenCount();
        }

        for (int i = 0, size = groups.size(); i < size; i++)
        {
            final Token groupToken = groups.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            if (atLeastOne[0])
            {
                sb.append(indent).append("builder << \", \";\n");
            }
            atLeastOne[0] = true;

            new Formatter(sb).format(
                indent + "{\n" +
                indent + "    bool atLeastOne = false;\n" +
                indent + "    builder << R\"(\"%3$s\": [)\";\n" +
                indent + "    writer.%2$s().forEach([&](%1$s& %2$s)\n" +
                indent + "    {\n" +
                indent + "        if (atLeastOne)\n" +
                indent + "        {\n" +
                indent + "            builder << \", \";\n" +
                indent + "        }\n" +
                indent + "        atLeastOne = true;\n" +
                indent + "        builder << %2$s;\n" +
                indent + "    });\n" +
                indent + "    builder << ']';\n" +
                indent + "}\n\n",
                formatClassName(groupToken.name()),
                formatPropertyName(groupToken.name()),
                groupToken.name());

            i = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
        }

        for (int i = 0, size = varData.size(); i < size;)
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            if (atLeastOne[0])
            {
                sb.append(indent).append("builder << \", \";\n");
            }
            atLeastOne[0] = true;

            final String characterEncoding = varData.get(i + 3).encoding().characterEncoding();
            sb.append(indent).append("builder << R\"(\"").append(varDataToken.name()).append("\": )\";\n");

            if (null == characterEncoding)
            {
                final String skipFunction = "writer.skip" + toUpperFirstChar(varDataToken.name()) + "()";

                sb.append(indent).append("builder << '\"' <<\n").append(indent).append(INDENT).append(skipFunction)
                    .append(" << \" bytes of raw data\\\"\";\n");
            }
            else
            {
                final String getAsStringFunction =
                    "writer.get" + toUpperFirstChar(varDataToken.name()) + "AsJsonEscapedString().c_str()";

                sb.append(indent).append("builder << '\"' <<\n").append(indent).append(INDENT)
                    .append(getAsStringFunction).append(" << '\"';\n\n");
            }

            i += varDataToken.componentTokenCount();
        }

        return sb;
    }

    private void writeTokenDisplay(
        final StringBuilder sb,
        final String fieldTokenName,
        final Token typeToken,
        final boolean[] atLeastOne,
        final String indent)
    {
        if (typeToken.encodedLength() <= 0 || typeToken.isConstantEncoding())
        {
            return;
        }

        if (atLeastOne[0])
        {
            sb.append(indent).append("builder << \", \";\n");
        }
        else
        {
            atLeastOne[0] = true;
        }

        sb.append(indent).append("builder << R\"(\"").append(fieldTokenName).append("\": )\";\n");
        final String fieldName = "writer." + formatPropertyName(fieldTokenName);

        switch (typeToken.signal())
        {
            case ENCODING:
                if (typeToken.arrayLength() > 1)
                {
                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        final String getAsStringFunction =
                            "writer.get" + toUpperFirstChar(fieldTokenName) + "AsJsonEscapedString().c_str()";

                        sb.append(indent).append("builder << '\"' <<\n").append(indent).append(INDENT)
                            .append(getAsStringFunction).append(" << '\"';\n");
                    }
                    else
                    {
                        sb.append(
                            indent + "builder << '[';\n" +
                            indent + "if (" + fieldName + "Length() > 0)\n" +
                            indent + "{\n" +
                            indent + "    for (size_t i = 0, length = " + fieldName + "Length(); i < length; i++)\n" +
                            indent + "    {\n" +
                            indent + "        if (i)\n" +
                            indent + "        {\n" +
                            indent + "            builder << ',';\n" +
                            indent + "        }\n" +
                            indent + "        builder << +" + fieldName + "(i);\n" +
                            indent + "    }\n" +
                            indent + "}\n" +
                            indent + "builder << ']';\n");
                    }
                }
                else
                {
                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        sb.append(
                            indent + "if (std::isprint(" + fieldName + "()))\n" +
                            indent + "{\n" +
                            indent + "    builder << '\"' << (char)" + fieldName + "() << '\"';\n" +
                            indent + "}\n" +
                            indent + "else\n" +
                            indent + "{\n" +
                            indent + "    builder << (int)" + fieldName + "();\n" +
                            indent + "}\n");
                    }
                    else
                    {
                        sb.append(indent).append("builder << +").append(fieldName).append("();\n");
                    }
                }
                break;

            case BEGIN_ENUM:
                sb.append(indent).append("builder << '\"' << ").append(fieldName).append("() << '\"';\n");
                break;

            case BEGIN_SET:
            case BEGIN_COMPOSITE:
                sb.append(indent).append("builder << ").append(fieldName).append("();\n");
                break;
        }

        sb.append('\n');
    }

    private CharSequence generateChoicesDisplay(final String name, final List<Token> tokens)
    {
        final String indent = INDENT;
        final StringBuilder sb = new StringBuilder();
        final List<Token> choiceTokens = new ArrayList<>();

        collect(Signal.CHOICE, tokens, 0, choiceTokens);

        new Formatter(sb).format("\n" +
            indent + "template<typename CharT, typename Traits>\n" +
            indent + "friend std::basic_ostream<CharT, Traits>& operator << (\n" +
            indent + "    std::basic_ostream<CharT, Traits>& builder, %1$s writer)\n" +
            indent + "{\n" +
            indent + "    builder << '[';\n",
            name);

        if (choiceTokens.size() > 1)
        {
            sb.append(indent + "    bool atLeastOne = false;\n");
        }

        for (int i = 0, size = choiceTokens.size(); i < size; i++)
        {
            final Token token = choiceTokens.get(i);
            final String choiceName = "writer." + formatPropertyName(token.name());

            sb.append(indent + "    if (").append(choiceName).append("())\n")
                .append(indent).append("    {\n");

            if (i > 0)
            {
                sb.append(
                    indent + "        if (atLeastOne)\n" +
                    indent + "        {\n" +
                    indent + "            builder << \",\";\n" +
                    indent + "        }\n");
            }
            sb.append(indent + "        builder << R\"(\"").append(formatPropertyName(token.name())).append("\")\";\n");

            if (i < (size - 1))
            {
                sb.append(indent + "        atLeastOne = true;\n");
            }

            sb.append(indent + "    }\n");
        }

        sb.append(
            indent + "    builder << ']';\n" +
            indent + "    return builder;\n" +
            indent + "}\n");

        return sb;
    }

    private CharSequence generateEnumDisplay(final List<Token> tokens, final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.applicableTypeName());
        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format("\n" +
            "    static const char *c_str(const %1$s::Value value)\n" +
            "    {\n" +
            "        switch (value)\n" +
            "        {\n",
            enumName);

        for (final Token token : tokens)
        {
            new Formatter(sb).format(
                "            case %1$s: return \"%1$s\";\n",
                token.name());
        }

        new Formatter(sb).format(
            "            case NULL_VALUE: return \"NULL_VALUE\";\n" +
            "        }\n\n" +
            "        throw std::runtime_error(\"unknown value for enum %1$s [E103]:\");\n" +
            "    }\n\n" +

            "    template<typename CharT, typename Traits>\n" +
            "    friend std::basic_ostream<CharT, Traits>& operator << (\n" +
            "        std::basic_ostream<CharT, Traits>& os, %1$s::Value m)\n" +
            "    {\n" +
            "        return os << %1$s::c_str(m);\n" +
            "    }\n",
            enumName);

        return sb;
    }

    private Object[] generateMessageLengthArgs(
        final List<Token> groups,
        final List<Token> varData,
        final String indent,
        final boolean withName)
    {
        final StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0, size = groups.size(); i < size; i++)
        {
            final Token groupToken = groups.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final int endSignal = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
            final String groupName = formatPropertyName(groupToken.name());

            if (count > 0)
            {
                sb.append(",\n").append(indent);
            }

            final List<Token> thisGroup = groups.subList(i, endSignal + 1);

            if (isMessageConstLength(thisGroup))
            {
                sb.append("std::size_t");
                if (withName)
                {
                    sb.append(" ").append(groupName).append("Length = 0");
                }
            }
            else
            {
                sb.append("const std::vector<std::tuple<");
                sb.append(generateMessageLengthArgs(thisGroup, indent + INDENT, false)[0]);
                sb.append(">> &");

                if (withName)
                {
                    sb.append(groupName).append("ItemLengths = {}");
                }
            }

            count += 1;

            i = endSignal;
        }

        for (int i = 0, size = varData.size(); i < size;)
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            if (count > 0)
            {
                sb.append(",\n").append(indent);
            }

            sb.append("std::size_t");
            if (withName)
            {
                sb.append(" ").append(formatPropertyName(varDataToken.name())).append("Length = 0");
            }

            count += 1;

            i += varDataToken.componentTokenCount();
        }

        CharSequence result = sb;
        if (count > 1)
        {
            result = "\n" + indent + result;
        }

        return new Object[]{ result, count };
    }

    private Object[] generateMessageLengthArgs(final List<Token> tokens, final String indent, final boolean withName)
    {
        int i = 0;

        final Token groupToken = tokens.get(i);
        if (groupToken.signal() != Signal.BEGIN_GROUP)
        {
            throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
        }

        ++i;
        final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
        i += groupHeaderTokenCount;

        final List<Token> fields = new ArrayList<>();
        i = collectFields(tokens, i, fields);

        final List<Token> groups = new ArrayList<>();
        i = collectGroups(tokens, i, groups);

        final List<Token> varData = new ArrayList<>();
        collectVarData(tokens, i, varData);

        return generateMessageLengthArgs(groups, varData, indent, withName);
    }

    private boolean isMessageConstLength(final List<Token> tokens)
    {
        final Integer count = (Integer)generateMessageLengthArgs(tokens, BASE_INDENT, false)[1];

        return count == 0;
    }

    private CharSequence generateMessageLengthCallPre17Helper(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();
        final Integer count = (Integer)generateMessageLengthArgs(tokens, BASE_INDENT, false)[1];

        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }

            sb.append("std::get<").append(i).append(">(e)");
        }

        return sb;
    }

    private CharSequence generateMessageLength(final List<Token> groups, final List<Token> varData, final String indent)
    {
        final StringBuilder sbEncode = new StringBuilder();
        final StringBuilder sbSkip = new StringBuilder();

        for (int i = 0, size = groups.size(); i < size; i++)
        {
            final Token groupToken = groups.get(i);

            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final int endSignal = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
            final List<Token> thisGroup = groups.subList(i, endSignal + 1);

            final Token numInGroupToken = Generators.findFirst("numInGroup", groups, i);
            final long minCount = numInGroupToken.encoding().applicableMinValue().longValue();
            final long maxCount = numInGroupToken.encoding().applicableMaxValue().longValue();

            final String countName = formatPropertyName(groupToken.name()) +
                (isMessageConstLength(thisGroup) ? "Length" : "ItemLengths.size()");

            final String minCheck = minCount > 0 ? countName + " < " + minCount + "LL || " : "";
            final String maxCheck = countName + " > " + maxCount + "LL";

            new Formatter(sbEncode).format("\n" +
                indent + "    length += %1$s::sbeHeaderSize();\n",
                formatClassName(groupToken.name()));

            if (isMessageConstLength(thisGroup))
            {
                new Formatter(sbEncode).format(
                    indent + "    if (%3$s%4$s)\n" +
                    indent + "    {\n" +
                    indent + "        throw std::runtime_error(\"%5$s outside of allowed range [E110]\");\n" +
                    indent + "    }\n" +
                    indent + "    length += %1$sLength *%2$s::sbeBlockLength();\n",
                    formatPropertyName(groupToken.name()),
                    formatClassName(groupToken.name()),
                    minCheck,
                    maxCheck,
                    countName);
            }
            else
            {
                new Formatter(sbEncode).format(
                    indent + "    if (%3$s%4$s)\n" +
                    indent + "    {\n" +
                    indent + "        throw std::runtime_error(\"%5$s outside of allowed range [E110]\");\n" +
                    indent + "    }\n\n" +
                    indent + "    for (const auto &e: %1$sItemLengths)\n" +
                    indent + "    {\n" +
                    indent + "        #if __cpluplus >= 201703L\n" +
                    indent + "        length += std::apply(%2$s::computeLength, e);\n" +
                    indent + "        #else\n" +
                    indent + "        length += %2$s::computeLength(%6$s);\n" +
                    indent + "        #endif\n" +
                    indent + "    }\n",
                    formatPropertyName(groupToken.name()),
                    formatClassName(groupToken.name()),
                    minCheck,
                    maxCheck,
                    countName,
                    generateMessageLengthCallPre17Helper(thisGroup));
            }

            new Formatter(sbSkip).format(
                indent + "    %2$s().forEach([](%1$s e)\n" +
                indent + "    {\n" +
                indent + "        e.skip();\n" +
                indent + "    });\n",
                formatClassName(groupToken.name()),
                formatPropertyName(groupToken.name()));

            i = endSignal;
        }

        for (int i = 0, size = varData.size(); i < size;)
        {
            final Token varDataToken = varData.get(i);

            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            final Token lengthToken = Generators.findFirst("length", varData, i);

            new Formatter(sbEncode).format("\n" +
                indent + "    length += %1$sHeaderLength();\n" +
                indent + "    if (%1$sLength > %2$dLL)\n" +
                indent + "    {\n" +
                indent + "        throw std::runtime_error(\"%1$sLength too long for length type [E109]\");\n" +
                indent + "    }\n" +
                indent + "    length += %1$sLength;\n",
                formatPropertyName(varDataToken.name()),
                lengthToken.encoding().applicableMaxValue().longValue());

            new Formatter(sbSkip).format(
                indent + "    skip%1$s();\n",
                toUpperFirstChar(varDataToken.name()));

            i += varDataToken.componentTokenCount();
        }

        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format("\n" +
            indent + "void skip()\n" +
            indent + "{\n" +
            sbSkip +
            indent + "}\n\n" +

            indent + "SBE_NODISCARD static SBE_CONSTEXPR bool isConstLength() SBE_NOEXCEPT\n" +
            indent + "{\n" +
            indent + "    return " + (groups.isEmpty() && varData.isEmpty()) + ";\n" +
            indent + "}\n\n" +

            indent + "SBE_NODISCARD static std::size_t computeLength(%1$s)\n" +
            indent + "{\n" +
            "#if defined(__GNUG__) && !defined(__clang__)\n" +
            "#pragma GCC diagnostic push\n" +
            "#pragma GCC diagnostic ignored \"-Wtype-limits\"\n" +
            "#endif\n" +
            indent + "    std::size_t length = sbeBlockLength();\n" +
            sbEncode + "\n" +
            indent + "    return length;\n" +
            "#if defined(__GNUG__) && !defined(__clang__)\n" +
            "#pragma GCC diagnostic pop\n" +
            "#endif\n" +
            indent + "}\n",
            generateMessageLengthArgs(groups, varData, indent + INDENT, true)[0]);

        return sb;
    }
}
