/*
 * Copyright 2013-2025 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation.golang.flyweight;

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
import java.util.*;

import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;
import static uk.co.real_logic.sbe.generation.golang.flyweight.GolangFlyweightUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;

/**
 * Codec generator for the Go programming language using flyweights.
 */
@SuppressWarnings("MethodLength")
public class GolangFlyweightGenerator implements CodeGenerator
{
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;
    private final boolean shouldDecodeUnknownEnumValues;

    private final Set<String> includes = new TreeSet<>();

    /**
     * Create a new Golang language {@link CodeGenerator}.
     *
     * @param ir                            for the messages and types.
     * @param shouldDecodeUnknownEnumValues generate support for unknown enum values when decoding.
     * @param outputManager                 for generating the codecs to.
     */
    public GolangFlyweightGenerator(
        final Ir ir,
        final boolean shouldDecodeUnknownEnumValues,
        final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.shouldDecodeUnknownEnumValues = shouldDecodeUnknownEnumValues;
        this.outputManager = outputManager;
    }

    private static void generateGroupClassHeader(
        final StringBuilder sb,
        final String groupClassName,
        final List<Token> tokens,
        final int index,
        final String indent,
        final List<Token> fields,
        final List<Token> groups)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final int dimensionHeaderLength = tokens.get(index + 1).encodedLength();
        final int blockLength = tokens.get(index).encodedLength();
        final Token blockLengthToken = Generators.findFirst("blockLength", tokens, index);
        final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, index);
        final String golangTypeBlockLength = goTypeName(blockLengthToken.encoding().primitiveType());
        final String golangTypeNumInGroup = goTypeName(numInGroupToken.encoding().primitiveType());

        new Formatter(sb).format("\n" +
            indent + "type %1$s struct {\n" +
            indent + "    buffer         []byte\n" +
            indent + "    bufferLength   uint64\n" +
            indent + "    initialPosition uint64\n" +
            indent + "    positionPtr    *uint64\n" +
            indent + "    blockLength    uint64\n" +
            indent + "    count          uint64\n" +
            indent + "    index          uint64\n" +
            indent + "    offset         uint64\n" +
            indent + "    actingVersion  uint64\n",
            groupClassName);

        for (int i = 0, size = fields.size(); i < size; i++)
        {
            final Token signalToken = fields.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = fields.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());
                final String typeName = formatClassName(encodingToken.applicableTypeName());

                switch (encodingToken.signal())
                {
                    case BEGIN_SET:
                    case BEGIN_COMPOSITE:
                        new Formatter(sb).format(indent + "    _%1$s %2$s\n",
                            propertyName,
                            typeName);
                        break;

                    default:
                        break;
                }
            }
        }

        for (int i = 0, size = groups.size(); i < size; )
        {
            final Token token = groups.get(i);
            if (token.signal() == Signal.BEGIN_GROUP)
            {
                final String propertyName = formatPropertyName(token.name());
                final String groupName = groupClassName + formatClassName(token.name());

                new Formatter(sb).format(indent + "    _%1$s %2$s\n",
                    propertyName,
                    groupName);
                i += token.componentTokenCount();
            }
        }

        new Formatter(sb).format(
            indent + "}\n\n" +
            indent + "func (g *%1$s) sbePositionPtr() *uint64 {\n" +
            indent + "    return g.positionPtr\n" +
            indent + "}\n\n",
            groupClassName);

        new Formatter(sb).format(
            indent + "func (g *%3$s) WrapForDecode(\n" +
            indent + "    buffer []byte,\n" +
            indent + "    pos *uint64,\n" +
            indent + "    actingVersion uint64,\n" +
            indent + "    bufferLength uint64) {\n" +
            indent + "    dimensions := %2$s{}\n" +
            indent + "    dimensions.Wrap(buffer, *pos, actingVersion, bufferLength)\n" +
            indent + "    g.buffer = buffer\n" +
            indent + "    g.bufferLength = bufferLength\n" +
            indent + "    g.blockLength = uint64(dimensions.BlockLength())\n" +
            indent + "    g.count = uint64(dimensions.NumInGroup())\n" +
            indent + "    g.index = 0\n" +
            indent + "    g.actingVersion = actingVersion\n" +
            indent + "    g.initialPosition = *pos\n" +
            indent + "    g.positionPtr = pos\n" +
            indent + "    *g.positionPtr = *g.positionPtr + %1$d\n" +
            indent + "}\n",
            dimensionHeaderLength, dimensionsClassName, groupClassName);

        final long minCount = numInGroupToken.encoding().applicableMinValue().longValue();
        final String minCheck = minCount > 0 ? "count < " + minCount + " || " : "";

        new Formatter(sb).format("\n" +
            indent + "func (g *%8$s) WrapForEncode(\n" +
            indent + "    buffer []byte,\n" +
            indent + "    count %3$s,\n" +
            indent + "    pos *uint64,\n" +
            indent + "    actingVersion uint64,\n" +
            indent + "    bufferLength uint64) {\n" +
            indent + "    if %5$scount > %6$d {\n" +
            indent + "        panic(\"count outside of allowed range [E110]\")\n" +
            indent + "    }\n" +
            indent + "    g.buffer = buffer\n" +
            indent + "    g.bufferLength = bufferLength\n" +
            indent + "    dimensions := %7$s{}\n" +
            indent + "    dimensions.Wrap(buffer, *pos, actingVersion, bufferLength)\n" +
            indent + "    dimensions.SetBlockLength(%2$d)\n" +
            indent + "    dimensions.SetNumInGroup(count)\n" +
            indent + "    g.index = 0\n" +
            indent + "    g.count = uint64(count)\n" +
            indent + "    g.blockLength = %2$d\n" +
            indent + "    g.actingVersion = actingVersion\n" +
            indent + "    g.initialPosition = *pos\n" +
            indent + "    g.positionPtr = pos\n" +
            indent + "    *g.positionPtr = *g.positionPtr + %4$d\n" +
            indent + "}\n",
            golangTypeBlockLength,
            blockLength,
            golangTypeNumInGroup,
            dimensionHeaderLength,
            minCheck,
            numInGroupToken.encoding().applicableMaxValue().longValue(),
            dimensionsClassName,
            groupClassName);

        new Formatter(sb).format("\n" +
            indent + "func (g *%3$s) SbeHeaderSize() uint64 {\n" +
            indent + "    return %1$d\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) SbeBlockLength() uint64 {\n" +
            indent + "    return %2$d\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) SbePosition() uint64 {\n" +
            indent + "    return *g.positionPtr\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) SbeCheckPosition(position uint64) uint64 {\n" +
            indent + "    if position > g.bufferLength {\n" +
            indent + "        panic(\"buffer too short [E100]\")\n" +
            indent + "    }\n" +
            indent + "    return position\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) SetSbePosition(position uint64) {\n" +
            indent + "    *g.positionPtr = g.SbeCheckPosition(position)\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) Count() uint64 {\n" +
            indent + "    return g.count\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) HasNext() bool {\n" +
            indent + "    return g.index < g.count\n" +
            indent + "}\n\n" +

            indent + "func (g *%3$s) Next() *%3$s {\n" +
            indent + "    if g.index >= g.count {\n" +
            indent + "        panic(\"index >= count [E108]\")\n" +
            indent + "    }\n" +
            indent + "    g.offset = *g.positionPtr\n" +
            indent + "    if g.offset+g.blockLength > g.bufferLength {\n" +
            indent + "        panic(\"buffer too short for next group index [E108]\")\n" +
            indent + "    }\n" +
            indent + "    *g.positionPtr = g.offset + g.blockLength\n" +
            indent + "    g.index++\n\n" +

            indent + "    return g\n" +
            indent + "}\n",
            dimensionHeaderLength,
            blockLength,
            groupClassName);
        sb.append("\n")
            .append(indent).append("func (g *").append(groupClassName)
            .append(") ResetCountToIndex() uint64 {\n")
            .append(indent).append("    g.count = g.index\n")
            .append(indent).append("    dimensions := ").append(dimensionsClassName).append("{}\n")
            .append(indent).append("    dimensions.Wrap")
            .append("(g.buffer, g.initialPosition, g.actingVersion, g.bufferLength)\n")
            .append(indent)
            .append("    dimensions.SetNumInGroup(").append(golangTypeNumInGroup).append("(g.count))\n")
            .append(indent).append("    return g.count\n")
            .append(indent).append("}\n\n");

        sb.append("\n")
            .append(indent).append("func (g *").append(groupClassName)
            .append(") ForEach(fn func(group *").append(groupClassName).append(")) {\n")
            .append(indent).append("    for g.HasNext() {\n")
            .append(indent).append("        g.Next()\n")
            .append(indent).append("        fn(g)\n")
            .append(indent).append("    }\n")
            .append(indent).append("}\n\n");
    }

    private static void generateGroupProperty(
        final StringBuilder sb,
        final String groupName,
        final String groupClassName,
        final String outerClassName,
        final Token token,
        final String goTypeForNumInGroup,
        final String indent)
    {
        final String propertyName = formatPropertyName(groupName);

        new Formatter(sb).format(
            indent + "    func (m *%3$s) %1$sId() int {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            token.id(),
            outerClassName);


        new Formatter(sb).format("\n" +
            indent + "func (m *%1$s) %2$s() *%3$s {\n" +
            indent + "    m._%2$s.WrapForDecode(m.buffer, m.sbePositionPtr(), m.actingVersion, m.bufferLength)\n" +
            indent + "    return &m._%2$s\n" +
            indent + "}\n",
            outerClassName,
            propertyName,
            groupClassName);

        new Formatter(sb).format("\n" +
            indent + "func (m *%1$s) %2$sCount(count %3$s) *%4$s {\n" +
            indent +
            "    m._%2$s.WrapForEncode(m.buffer, count, m.sbePositionPtr(), m.actingVersion, m.bufferLength)\n" +
            indent + "    return &m._%2$s\n" +
            indent + "}\n",
            outerClassName,
            propertyName,
            goTypeForNumInGroup,
            groupClassName);

        final int version = token.version();
        final String versionCheck = 0 == version ?
            "    return true\n" : "    return m.actingVersion >= m.%1$sSinceVersion()\n";
        new Formatter(sb).format("\n" +
            indent + "func (m *%3$s) %1$sSinceVersion() uint64 {\n" +
            indent + "    return %2$d\n" +
            indent + "}\n\n" +

            indent + "func (m *%3$s) %1$sInActingVersion() bool {\n" +
            indent + versionCheck +
            indent + "}\n",
            propertyName,
            version,
            outerClassName);
    }

    private static CharSequence generateChoiceNotPresentCondition(final int sinceVersion)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            "        if m.actingVersion < %1$d {\n" +
                "            return false\n" +
                "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateArrayFieldNotPresentCondition(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return nil\n" +
                indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateArrayLengthNotPresentCondition(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return 0\n" +
                indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateArrayFieldNotPresentConditionWithErr(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return 0, nil\n" +
                indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateStringNotPresentCondition(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return \"\"\n" +
                indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateTypeFieldNotPresentCondition(
        final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return \"\"\n" +
                indent + "        }\n\n",
            sinceVersion);
    }

    private static CharSequence generateClassDeclaration(final String className)
    {
        return
            "type " + className + " struct {\n";
    }

    private static CharSequence generateEnumDeclaration(final String name, final Token encodingToken)
    {
        final Encoding encoding = encodingToken.encoding();
        return "type " + name + " " + goTypeName(encoding.primitiveType()) + "\n";
    }

    private static CharSequence generateConstructorsAndOperators(final String className)
    {
        return String.format(
            "    func %1$s() {\n\n" +

                "    func %1$s(\n" +
                "        buffer []byte,\n" +
                "        offset uint64,\n" +
                "        bufferLength uint64,\n" +
                "        actingBlockLength uint64,\n" +
                "        actingVersion uint64) {\n" +
                "        m.buffer := buffer\n" +
                "        m.bufferLength := bufferLength\n" +
                "        m.offset := offset\n" +
                "        m.position := sbeCheckPosition(offset + actingBlockLength)\n" +
                "        m.actingBlockLength := actingBlockLength\n" +
                "        m.actingVersion := actingVersion\n" +
                "    }\n\n",
            className);
    }

    private static void generateFieldMetaAttributeMethod(
        final StringBuilder sb, final Token token, final String indent, final String className)
    {
        final Encoding encoding = token.encoding();
        final String propertyName = toUpperFirstChar(token.name());
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append("\n")
            .append(indent).append("func (m *").append(className)
            .append(") ").append(propertyName).append("MetaAttribute(metaAttribute string) string {\n")
            .append(indent).append("    switch metaAttribute {\n");

        if (!Strings.isEmpty(epoch))
        {
            sb.append(indent)
                .append("        case \"EPOCH\": return \"").append(epoch).append("\"\n");
        }

        if (!Strings.isEmpty(timeUnit))
        {
            sb.append(indent)
                .append("        case \"TIME_UNIT\": return \"").append(timeUnit).append("\"\n");
        }

        if (!Strings.isEmpty(semanticType))
        {
            sb.append(indent)
                .append("        case \"SEMANTIC_TYPE\": return \"").append(semanticType)
                .append("\"\n");
        }

        sb
            .append(indent).append("        case \"PRESENCE\": return \"")
            .append(encoding.presence().toString().toLowerCase()).append("\"\n")
            .append(indent).append("        default: return \"\"\n")
            .append(indent).append("    }\n")
            .append(indent).append("}\n");
    }

    private static CharSequence generateEnumFieldNotPresentCondition(
        final int sinceVersion, final String enumName, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return %2$s_NULL_VALUE\n" +
                indent + "        }\n\n",
            sinceVersion,
            enumName);
    }

    private static void generateBitsetProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token token,
        final String indent,
        final String className)
    {
        final String bitsetName = formatClassName(token.applicableTypeName());
        final int offset = token.offset();

        new Formatter(sb).format(
            indent + "    func (m *%4$s) %2$s() *%1$s {\n" +
            indent + "        m._%2$s.Wrap(m.buffer, m.offset + %3$d, m.actingVersion, m.bufferLength)\n" +
            indent + "        return &m._%2$s\n" +
            indent + "    }\n",
            bitsetName,
            propertyName,
            offset,
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sEncodingLength() int {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size(),
            className);
    }

    private static void generateCompositeProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token token,
        final String indent,
        final String className)
    {
        final String compositeName = formatClassName(token.applicableTypeName());

        new Formatter(sb).format(
            indent + "    func (m *%4$s) %2$s() *%1$s {\n" +
            indent + "        m._%2$s.Wrap(m.buffer, m.offset + %3$d, m.actingVersion, m.bufferLength)\n" +
            indent + "        return &m._%2$s\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            token.offset(),
            className);
    }

    /**
     * Generate the composites for dealing with the message header.
     *
     * @throws IOException if an error is encountered when writing the output.
     */
    public void generateMessageHeaderStub() throws IOException
    {
        generateComposite(ir.headerStructure().tokens());
    }

    private void generateTypeStubs() throws IOException
    {
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

                default:
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void generate() throws IOException
    {

        generateUtils(ir.namespaces());

        generateMessageHeaderStub();
        generateTypeStubs();

        for (final List<Token> tokens : ir.messages())
        {
            includes.clear();

            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());

            try (Writer fileOut = outputManager.createOutput(className))
            {
                final StringBuilder out = new StringBuilder();
                out.append(generateClassDeclaration(className));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int i = 0;

                final List<Token> fields = new ArrayList<>();
                i = collectFields(messageBody, i, fields);

                final List<Token> groups = new ArrayList<>();
                i = collectGroups(messageBody, i, groups);

                final List<Token> varData = new ArrayList<>();
                collectVarData(messageBody, i, varData);

                out.append(generateMessageFlyweightCode(className, msgToken, fields, groups));

                final StringBuilder sb = new StringBuilder();
                generateFields(sb, className, fields, BASE_INDENT);
                generateGroups(sb, groups, BASE_INDENT, className);
                generateVarData(sb, className, varData, BASE_INDENT);
                generateDisplay(sb, msgToken.name(), className, fields, groups, varData);
                sb.append(generateMessageLength(groups, varData, BASE_INDENT, className));
                out.append(sb);

                fileOut.append(generateFileHeader(ir.namespaces()));
                fileOut.append(out);
            }
        }
    }

    private void generateUtils(final String[] namespaces) throws IOException
    {

        try (Writer out = outputManager.createOutput("Utils"))
        {
            out.append("/* Generated SBE (Simple Binary Encoding) message codec */\n");

            out.append(String.format("// Code generated by SBE. DO NOT EDIT.\n\n" +

                "package %1$s\n\n" +

                "import (\n" +
                "\t\"math\"\n" +
                "\t\"math/bits\"\n" +
                ")\n\n" +

                "// SbeBigEndianEncode16 encodes a 16-bit value into big endian byte order\n" +
                "func SbeBigEndianEncode16(v uint16) uint16 {\n" +
                "\treturn bits.ReverseBytes16(v)\n" +
                "}\n\n" +

                "// SbeBigEndianEncode32 encodes a 32-bit value into big endian byte order\n" +
                "func SbeBigEndianEncode32(v uint32) uint32 {\n" +
                "\treturn bits.ReverseBytes32(v)\n" +
                "}\n\n" +

                "// SbeBigEndianEncode64 encodes a 64-bit value into big endian byte order\n" +
                "func SbeBigEndianEncode64(v uint64) uint64 {\n" +
                "\treturn bits.ReverseBytes64(v)\n" +
                "}\n\n" +

                "// SbeLittleEndianEncode16 encodes a 16-bit value into little endian byte order\n" +
                "func SbeLittleEndianEncode16(v uint16) uint16 {\n" +
                "\treturn v\n" +
                "}\n\n" +

                "// SbeLittleEndianEncode32 encodes a 32-bit value into little endian byte order\n" +
                "func SbeLittleEndianEncode32(v uint32) uint32 {\n" +
                "\treturn v\n" +
                "}\n\n" +

                "// SbeLittleEndianEncode64 encodes a 64-bit value into little endian byte order\n" +
                "func SbeLittleEndianEncode64(v uint64) uint64 {\n" +
                "\treturn v\n" +
                "}\n\n" +

                "// SbeNullValueFloat returns the null value for a float\n" +
                "func SbeNullValueFloat() float32 {\n" +
                "\treturn float32(math.NaN())\n" +
                "}\n\n" +

                "// SbeNullValueDouble returns the null value for a double\n" +
                "func SbeNullValueDouble() float64 {\n" +
                "\treturn math.NaN()\n" +
                "}\n\n" +

                "// SbeNullValueByte returns the null value for a byte\n" +
                "func SbeNullValueByte() byte {\n" +
                "\treturn byte(math.MaxUint8)\n" +
                "}\n\n" +

                "// SbeNullValueInt8 returns the null value for an 8-bit integer\n" +
                "func SbeNullValueInt8() int8 {\n" +
                "\treturn math.MinInt8\n" +
                "}\n\n" +

                "// SbeNullValueInt16 returns the null value for a 16-bit integer\n" +
                "func SbeNullValueInt16() int16 {\n" +
                "\treturn math.MinInt16\n" +
                "}\n\n" +

                "// SbeNullValueInt32 returns the null value for a 32-bit integer\n" +
                "func SbeNullValueInt32() int32 {\n" +
                "\treturn math.MinInt32\n" +
                "}\n\n" +

                "// SbeNullValueInt64 returns the null value for a 64-bit integer\n" +
                "func SbeNullValueInt64() int64 {\n" +
                "\treturn math.MinInt64\n" +
                "}\n\n" +

                "// SbeNullValueUint8 returns the null value for an 8-bit unsigned integer\n" +
                "func SbeNullValueUint8() uint8 {\n" +
                "\treturn math.MaxUint8\n" +
                "}\n\n" +

                "// SbeNullValueUint16 returns the null value for a 16-bit unsigned integer\n" +
                "func SbeNullValueUint16() uint16 {\n" +
                "\treturn math.MaxUint16\n" +
                "}\n\n" +

                "// SbeNullValueUint32 returns the null value for a 32-bit unsigned integer\n" +
                "func SbeNullValueUint32() uint32 {\n" +
                "\treturn math.MaxUint32\n" +
                "}\n\n" +

                "// SbeNullValueUint64 returns the null value for a 64-bit unsigned integer\n" +
                "func SbeNullValueUint64() uint64 {\n" +
                "\treturn math.MaxUint64\n" +
                "}\n\n" +

                "type MetaAttribute int\n" +
                "const (\n" +
                "    EPOCH MetaAttribute = iota\n" +
                "    TIME_UNIT\n" +
                "    SEMANTIC_TYPE\n" +
                "    PRESENCE\n" +
                ")\n\n" +

                "var SbeNoBoundsCheck = false\n\n" +
                "// LittleEndian\n" +
                "\n" +
                "func ByteLittleEndian(b []byte) byte {\n" +
                "\treturn b[0]\n" +
                "}\n" +
                "\n" +
                "func PutByteLittleEndian(b []byte, v byte) {\n" +
                "\tb[0] = v\n" +
                "}\n" +
                "\n" +
                "func Uint8LittleEndian(b []byte) uint8 {\n" +
                "\treturn b[0]\n" +
                "}\n" +
                "\n" +
                "func PutUint8LittleEndian(b []byte, v uint8) {\n" +
                "\tb[0] = v\n" +
                "}\n" +
                "\n" +
                "func Int8LittleEndian(b []byte) int8 {\n" +
                "\treturn int8(Uint8LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt8LittleEndian(b []byte, v int8) {\n" +
                "\tPutUint8LittleEndian(b, uint8(v))\n" +
                "}\n" +
                "\n" +
                "func Uint16LittleEndian(b []byte) uint16 {\n" +
                "\t_ = b[1] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint16(b[0]) | uint16(b[1])<<8\n" +
                "}\n" +
                "\n" +
                "func PutUint16LittleEndian(b []byte, v uint16) {\n" +
                "\t_ = b[1] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v)\n" +
                "\tb[1] = byte(v >> 8)\n" +
                "}\n" +
                "\n" +
                "func Int16LittleEndian(b []byte) int16 {\n" +
                "\treturn int16(Uint16LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt16LittleEndian(b []byte, v int16) {\n" +
                "\tPutUint16LittleEndian(b, uint16(v))\n" +
                "}\n" +
                "\n" +
                "func Uint32LittleEndian(b []byte) uint32 {\n" +
                "\t_ = b[3] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint32(b[0]) | uint32(b[1])<<8 | uint32(b[2])<<16 | uint32(b[3])<<24\n" +
                "}\n" +
                "\n" +
                "func PutUint32LittleEndian(b []byte, v uint32) {\n" +
                "\t_ = b[3] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v)\n" +
                "\tb[1] = byte(v >> 8)\n" +
                "\tb[2] = byte(v >> 16)\n" +
                "\tb[3] = byte(v >> 24)\n" +
                "}\n" +
                "\n" +
                "func Int32LittleEndian(b []byte) int32 {\n" +
                "\treturn int32(Uint32LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt32LittleEndian(b []byte, v int32) {\n" +
                "\tPutUint32LittleEndian(b, uint32(v))\n" +
                "}\n" +
                "\n" +
                "func Uint64LittleEndian(b []byte) uint64 {\n" +
                "\t_ = b[7] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint64(b[0]) | uint64(b[1])<<8 | uint64(b[2])<<16 | uint64(b[3])<<24 |\n" +
                "\t\tuint64(b[4])<<32 | uint64(b[5])<<40 | uint64(b[6])<<48 | uint64(b[7])<<56\n" +
                "}\n" +
                "\n" +
                "func PutUint64LittleEndian(b []byte, v uint64) {\n" +
                "\t_ = b[7] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v)\n" +
                "\tb[1] = byte(v >> 8)\n" +
                "\tb[2] = byte(v >> 16)\n" +
                "\tb[3] = byte(v >> 24)\n" +
                "\tb[4] = byte(v >> 32)\n" +
                "\tb[5] = byte(v >> 40)\n" +
                "\tb[6] = byte(v >> 48)\n" +
                "\tb[7] = byte(v >> 56)\n" +
                "}\n" +
                "\n" +
                "func Int64LittleEndian(b []byte) int64 {\n" +
                "\treturn int64(Uint64LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt64LittleEndian(b []byte, v int64) {\n" +
                "\tPutUint64LittleEndian(b, uint64(v))\n" +
                "}\n" +
                "\n" +
                "func FloatLittleEndian(b []byte) float32 {\n" +
                "\treturn math.Float32frombits(Uint32LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutFloatLittleEndian(b []byte, v float32) {\n" +
                "\tPutUint32LittleEndian(b, math.Float32bits(v))\n" +
                "}\n" +
                "\n" +
                "func DoubleLittleEndian(b []byte) float64 {\n" +
                "\treturn math.Float64frombits(Uint64LittleEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutDoubleLittleEndian(b []byte, v float64) {\n" +
                "\tPutUint64LittleEndian(b, math.Float64bits(v))\n" +
                "}\n" +
                "\n" +
                "// BigEndian\n" +
                "\n" +
                "func ByteBigEndian(b []byte) byte {\n" +
                "\treturn b[0]\n" +
                "}\n" +
                "\n" +
                "func PutByteBigEndian(b []byte, v byte) {\n" +
                "\tb[0] = v\n" +
                "}\n" +
                "\n" +
                "func Uint8BigEndian(b []byte) uint8 {\n" +
                "\treturn b[0]\n" +
                "}\n" +
                "\n" +
                "func PutUint8BigEndian(b []byte, v uint8) {\n" +
                "\tb[0] = byte(v)\n" +
                "}\n" +
                "\n" +
                "func Int8BigEndian(b []byte) int8 {\n" +
                "\treturn int8(Uint8BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt8BigEndian(b []byte, v int8) {\n" +
                "\tPutUint8BigEndian(b, uint8(v))\n" +
                "}\n" +
                "\n" +
                "func Uint16BigEndian(b []byte) uint16 {\n" +
                "\t_ = b[1] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint16(b[1]) | uint16(b[0])<<8\n" +
                "}\n" +
                "\n" +
                "func PutUint16BigEndian(b []byte, v uint16) {\n" +
                "\t_ = b[1] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v >> 8)\n" +
                "\tb[1] = byte(v)\n" +
                "}\n" +
                "\n" +
                "func Int16BigEndian(b []byte) int16 {\n" +
                "\treturn int16(Uint16BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt16BigEndian(b []byte, v int16) {\n" +
                "\tPutUint16BigEndian(b, uint16(v))\n" +
                "}\n" +
                "\n" +
                "func Uint32BigEndian(b []byte) uint32 {\n" +
                "\t_ = b[3] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint32(b[3]) | uint32(b[2])<<8 | uint32(b[1])<<16 | uint32(b[0])<<24\n" +
                "}\n" +
                "\n" +
                "func PutUint32BigEndian(b []byte, v uint32) {\n" +
                "\t_ = b[3] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v >> 24)\n" +
                "\tb[1] = byte(v >> 16)\n" +
                "\tb[2] = byte(v >> 8)\n" +
                "\tb[3] = byte(v)\n" +
                "}\n" +
                "\n" +
                "func Int32BigEndian(b []byte) int32 {\n" +
                "\treturn int32(Uint32BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt32BigEndian(b []byte, v int32) {\n" +
                "\tPutUint32BigEndian(b, uint32(v))\n" +
                "}\n" +
                "\n" +
                "func Uint64BigEndian(b []byte) uint64 {\n" +
                "\t_ = b[7] // bounds check hint to compiler; see golang.org/issue/14808\n" +
                "\treturn uint64(b[7]) | uint64(b[6])<<8 | uint64(b[5])<<16 | uint64(b[4])<<24 |\n" +
                "\t\tuint64(b[3])<<32 | uint64(b[2])<<40 | uint64(b[1])<<48 | uint64(b[0])<<56\n" +
                "}\n" +
                "\n" +
                "func PutUint64BigEndian(b []byte, v uint64) {\n" +
                "\t_ = b[7] // early bounds check to guarantee safety of writes below\n" +
                "\tb[0] = byte(v >> 56)\n" +
                "\tb[1] = byte(v >> 48)\n" +
                "\tb[2] = byte(v >> 40)\n" +
                "\tb[3] = byte(v >> 32)\n" +
                "\tb[4] = byte(v >> 24)\n" +
                "\tb[5] = byte(v >> 16)\n" +
                "\tb[6] = byte(v >> 8)\n" +
                "\tb[7] = byte(v)\n" +
                "}\n" +
                "\n" +
                "func Int64BigEndian(b []byte) int64 {\n" +
                "\treturn int64(Uint64BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutInt64BigEndian(b []byte, v int64) {\n" +
                "\tPutUint64BigEndian(b, uint64(v))\n" +
                "}\n" +
                "\n" +
                "func FloatBigEndian(b []byte) float32 {\n" +
                "\treturn math.Float32frombits(Uint32BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutFloatBigEndian(b []byte, v float32) {\n" +
                "\tPutUint32BigEndian(b, math.Float32bits(v))\n" +
                "}\n" +
                "\n" +
                "func DoubleBigEndian(b []byte) float64 {\n" +
                "\treturn math.Float64frombits(Uint64BigEndian(b))\n" +
                "}\n" +
                "\n" +
                "func PutDoubleBigEndian(b []byte, v float64) {\n" +
                "\tPutUint64BigEndian(b, math.Float64bits(v))\n" +
                "}\n",

                namespacesToPackageName(namespaces)));
        }
    }

    private String formatIncludes(final Set<String> includes)
    {
        final StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (final String s : includes)
        {
            if (!first)
            {
                sb.append("\n");
            }
            sb.append('"').append(s).append('"');
            first = false;
        }
        return sb.toString();
    }

    private void generateGroups(
        final StringBuilder sb,
        final List<Token> tokens,
        final String indent,
        final String outerClassName)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final String groupName = groupToken.name();
            final String groupClassName = formatClassName(outerClassName + formatClassName(groupToken.name()));
            final Token numInGroupToken = Generators.findFirst("numInGroup", tokens, i);
            final String goTypeForNumInGroup = goTypeName(numInGroupToken.encoding().primitiveType());

            final int groupStart = i;

            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            generateGroupClassHeader(sb, groupClassName, tokens, groupStart,
                indent + INDENT, fields, groups);

            generateFields(sb, groupClassName, fields, indent + INDENT);
            generateGroups(sb, groups, indent + INDENT, groupClassName);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(sb, groupClassName, varData, indent + INDENT);

            sb.append(generateGroupDisplay(groupClassName, fields, groups, varData, indent + INDENT + INDENT));
            sb.append(generateMessageLength(groups, varData, indent + INDENT + INDENT, groupClassName));

            generateGroupProperty(sb, groupName, groupClassName, outerClassName, groupToken, goTypeForNumInGroup,
                indent);
        }
    }

    private void generateVarData(
        final StringBuilder sb,
        final String className,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; )
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
            final String lengthGoType = goTypeName(lengthToken.encoding().primitiveType());
            final String lengthByteOrderReadStr = formatReadBytes(
                lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType());

            generateFieldMetaAttributeMethod(sb, token, indent, className);

            generateVarDataDescriptors(
                sb, token, propertyName, characterEncoding, lengthToken,
                lengthOfLengthField, lengthGoType, indent, className);

            new Formatter(sb).format("\n" +
                indent + "    func (m *%5$s) Skip%1$s() uint64 {\n" +
                "%2$s" +
                indent + "        lengthOfLengthField := uint64(%3$d)\n" +
                indent + "        lengthPosition := m.SbePosition()\n" +
                indent +
                "        dataLength := uint64(%4$s(m.buffer[lengthPosition : lengthPosition+lengthOfLengthField]))\n" +
                indent + "        m.SetSbePosition(lengthPosition + lengthOfLengthField + dataLength)\n" +
                indent + "        return dataLength\n" +
                indent + "    }\n",
                propertyName,
                generateArrayLengthNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderReadStr,
                className);


            new Formatter(sb).format("\n" +
                indent + "func (m *%6$s) %1$s() string {\n" +
                "%2$s" +
                indent + "    lengthFieldValue := uint64(%4$s(m.buffer[m.SbePosition():m.SbePosition()+%3$d]))\n" +
                indent + "    m.SetSbePosition(m.SbePosition() + uint64(%3$d))\n" +
                indent + "    pos := m.SbePosition()\n" +
                indent + "    m.SetSbePosition(pos + lengthFieldValue)\n" +
                indent + "    return string(m.buffer[pos:pos+lengthFieldValue])\n" +
                indent + "}\n",
                formatPropertyName(propertyName),
                generateTypeFieldNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderReadStr,
                lengthGoType,
                className);

            new Formatter(sb).format("\n" +
                indent + "func (m *%5$s) Get%1$s(dst []byte) int {\n" +
                "%2$s" +
                indent + "    lengthOfLengthField := uint64(%3$d)\n" +
                indent + "    lengthPosition := m.SbePosition()\n" +
                indent + "    m.SetSbePosition(m.SbePosition() + lengthOfLengthField)\n" +
                indent +
                "    dataLength := uint64(%4$s(m.buffer[lengthPosition:lengthPosition+lengthOfLengthField]))\n" +
                indent + "    bytesToCopy := dataLength\n" +
                indent + "    if uint64(len(dst)) < dataLength {\n" +
                indent + "        bytesToCopy = uint64(len(dst))\n" +
                indent + "    }\n" +
                indent + "    pos := m.SbePosition()\n" +
                indent + "    m.SetSbePosition(pos + dataLength)\n" +
                indent + "    copy(dst, m.buffer[pos:pos+bytesToCopy])\n" +
                indent + "    return int(bytesToCopy)\n" +
                indent + "}\n",
                propertyName,
                generateArrayLengthNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderReadStr,
                className);

            final String lengthByteOrderWriteStr = formatWriteBytes(
                lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType());
            new Formatter(sb).format("\n" +
                indent + "func (m *%5$s) Put%1$sLen(src string, length %3$s) *%5$s {\n" +
                indent + "    lengthOfLengthField := uint64(%2$d)\n" +
                indent + "    lengthPosition := m.SbePosition()\n" +
                indent + "    m.SetSbePosition(lengthPosition + lengthOfLengthField)\n" +
                indent + "    %4$s(m.buffer[lengthPosition:], length)\n" +
                indent + "    if length != %3$s(0) {\n" +
                indent + "        pos := m.SbePosition()\n" +
                indent + "        m.SetSbePosition(pos + uint64(length))\n" +
                indent + "        copy(m.buffer[pos:], src)\n" +
                indent + "    }\n" +
                indent + "    return m\n" +
                indent + "}\n",
                propertyName,
                lengthOfLengthField,
                lengthGoType,
                lengthByteOrderWriteStr,
                className,
                lengthByteOrderReadStr);

            new Formatter(sb).format("\n" +
                indent + "func (m *%6$s) Get%1$sAsString() string {\n" +
                "%2$s" +
                indent + "    lengthOfLengthField := uint64(%3$d)\n" +
                indent + "    lengthPosition := m.SbePosition()\n" +
                indent +
                "    dataLength := uint64(%4$s(m.buffer[lengthPosition:lengthPosition+lengthOfLengthField]))\n" +
                indent + "    m.SetSbePosition(lengthPosition + lengthOfLengthField)\n" +
                indent + "    pos := m.SbePosition()\n" +
                indent + "    m.SetSbePosition(pos + dataLength)\n" +
                indent + "    return string(m.buffer[pos:pos+dataLength])\n" +
                indent + "}\n",
                propertyName,
                generateStringNotPresentCondition(token.version(), indent),
                lengthOfLengthField,
                lengthByteOrderReadStr,
                lengthGoType,
                className);

            generateJsonEscapedStringGetter(sb, token, indent, propertyName, className);

            new Formatter(sb).format("\n" +
                indent + "func (m *%1$s) Put%2$s(str string) *%1$s {\n" +
                indent + "    if len(str) > %4$d {\n" +
                indent + "        panic(\"string too long for length type [E109]\")\n" +
                indent + "    }\n" +
                indent + "    return m.Put%2$sLen(str, %3$s(len(str)))\n" +
                indent + "}\n",
                className,
                propertyName,
                lengthGoType,
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
        final String lengthGoType,
        final String indent,
        final String className)
    {
        new Formatter(sb).format("\n" +
            indent + "func (m *%3$s) %1$sCharacterEncoding() string {\n" +
            indent + "        return \"%2$s\"\n" +
            indent + "    }\n",
            formatPropertyName(propertyName),
            characterEncoding,
            className);

        final int version = token.version();
        final String versionCheck = 0 == version ?
            "        return true\n" : "        return m.actingVersion >= m.%1$sSinceVersion()\n";
        new Formatter(sb).format("\n" +
            indent + "func (m *%4$s) %1$sSinceVersion() uint64 {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n\n" +

            indent + "func (m *%4$s) %1$sInActingVersion() bool {\n" +
            indent + versionCheck +
            indent + "    }\n\n" +

            indent + "func (m *%4$s) %1$sId() uint16 {\n" +
            indent + "        return %3$d\n" +
            indent + "    }\n",
            formatPropertyName(propertyName),
            version,
            token.id(),
            className);


        new Formatter(sb).format("\n" +
            indent + "func (m *%3$s) %1$sHeaderLength() uint64 {\n" +
            indent + "        return %2$d\n" +
            indent + "}\n",
            formatPropertyName(propertyName),
            sizeOfLengthField,
            className);

        new Formatter(sb).format("\n" +
            indent + "func (m *%5$s) %1$sLength() %4$s {\n" +
            "%2$s" +
            indent + "        return %3$s(m.buffer[m.SbePosition():])\n" +
            indent + "}\n",
            formatPropertyName(propertyName),
            generateArrayLengthNotPresentCondition(version, BASE_INDENT),
            formatReadBytes(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
            lengthGoType,
            className);
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String bitSetName = formatClassName(token.applicableTypeName());

        try (Writer fileOut = outputManager.createOutput(bitSetName))
        {
            includes.clear();
            final StringBuilder out = new StringBuilder();

            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, token.encodedLength(), tokens));

            final Encoding encoding = token.encoding();
            new Formatter(out).format("\n" +
                "    func (bs *%1$s) Clear() *%1$s {\n" +
                "        %2$s(bs.buffer[bs.offset:], 0)\n" +
                "        return bs\n" +
                "    }\n",
                bitSetName,
                formatWriteBytes(token.encoding().byteOrder(), encoding.primitiveType()));

            new Formatter(out).format("\n" +
                "    func (bs *%1$s) IsEmpty() bool {\n" +
                "        return %2$s(bs.buffer[bs.offset:]) == 0\n" +
                "    }\n",
                bitSetName,
                formatReadBytes(token.encoding().byteOrder(), encoding.primitiveType()));

            new Formatter(out).format("\n" +
                "    func (bs *%1$s) RawValue() %2$s {\n" +
                "        return %2$s(bs.buffer[bs.offset])\n" +
                "    }\n",
                bitSetName,
                goTypeName(encoding.primitiveType()));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));
            out.append(generateChoicesDisplay(bitSetName, tokens.subList(1, tokens.size() - 1)));
            out.append("\n");

            fileOut.append(generateFileHeader(ir.namespaces()));
            fileOut.append(out);
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer fileOut = outputManager.createOutput(enumName))
        {
            includes.clear();
            final StringBuilder out = new StringBuilder();

            out.append(generateEnumDeclaration(enumName, enumToken));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumName, enumToken));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append(generateEnumDisplay(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append("\n");

            fileOut.append(generateEnumFileHeader(ir.namespaces()));
            fileOut.append(out);
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).applicableTypeName());

        try (Writer fileOut = outputManager.createOutput(compositeName))
        {
            includes.clear();
            final StringBuilder out = new StringBuilder();

            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).encodedLength(), tokens));

            out.append(generateCompositePropertyElements(
                compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append(generateCompositeDisplay(
                tokens.get(0).applicableTypeName(), tokens.subList(1, tokens.size() - 1)));

            fileOut.append(generateFileHeader(ir.namespaces()));
            fileOut.append(out);
        }
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
                final PrimitiveType type = token.encoding().primitiveType();
                final String typeName = goTypeName(type);
                final String choiceBitPosition = token.encoding().constValue().toString();
                final CharSequence constantOne = generateLiteral(type, "1");

                new Formatter(sb).format("\n" +
                    "    func (m *%5$s) %1$sValue(bits %2$s) bool {\n" +
                    "        return (bits & (%4$s << %3$s)) != 0\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition,
                    constantOne,
                    bitsetClassName);

                new Formatter(sb).format("\n" +
                    "    func (m *%5$s) Set%1$sValue(bits %2$s, value bool) %2$s {\n" +
                    "        if value {\n" +
                    "            return bits | (%4$s << %3$s)\n" +
                    "        }\n" +
                    "        return bits & ^(%4$s << %3$s)\n" +
                    "    }\n",
                    choiceName,
                    typeName,
                    choiceBitPosition,
                    constantOne,
                    bitsetClassName);


                new Formatter(sb).format("\n" +
                    "func (m *%1$s) %2$s() bool {\n" +
                    "%3$s" +
                    "    return m.%2$sValue(%4$s(m.buffer[m.offset:]))\n" +
                    "}\n",
                    bitsetClassName,
                    choiceName,
                    generateChoiceNotPresentCondition(token.version()),
                    formatReadBytes(token.encoding().byteOrder(), type));

                new Formatter(sb).format("\n" +
                    "func (m *%1$s) Set%2$s(value bool) *%1$s{\n" +
                    "    bits := %7$s(m.buffer[m.offset:])\n" +
                    "    if value {\n" +
                    "        bits = %3$s(%3$s(bits) | (%6$s << %5$s))\n" +
                    "    } else {\n" +
                    "        bits = %3$s(%3$s(bits) & ^(%6$s << %5$s))\n" +
                    "    }\n" +
                    "    %4$s(m.buffer[m.offset:], bits)\n" +
                    "    return m\n" +
                    "}\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
                    formatWriteBytes(token.encoding().byteOrder(), type),
                    choiceBitPosition,
                    constantOne,
                    formatReadBytes(token.encoding().byteOrder(), type));
            });

        return sb;
    }

    private CharSequence generateEnumValues(
        final List<Token> tokens,
        final String enumName,
        final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        sb.append("    const (\n");

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());
            sb.append("    ").append(enumName).append("_").append(token.name()).append(" ").append(enumName)
            .append(" = ").append(enumName).append("(").append(constVal).append(")\n");
        }

        final CharSequence nullLiteral = generateLiteral(
            encoding.primitiveType(), encoding.applicableNullValue().toString());
        if (shouldDecodeUnknownEnumValues)
        {
            sb.append("    ").append(enumName).append("_").append("UNKNOWN")
                .append(" ").append(enumName).append(" = ").append(enumName)
                .append("(").append(nullLiteral).append(")\n");
        }

        sb.append("    ").append(enumName).append("_").append("NULL_VALUE")
            .append(" ").append(enumName)
            .append(" = ").append(enumName).append("(")
            .append(nullLiteral).append(")\n");
        sb.append("\n    )\n\n");

        return sb;
    }


    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.applicableTypeName());
        final StringBuilder sb = new StringBuilder();
        final String goTypeName = goTypeName(encodingToken.encoding().primitiveType());

        addInclude("errors");
        new Formatter(sb).format(
            "    var %1$sLookupErr = errors.New(\"unknown value for enum %1$s [E103]\")\n" +
            "    func Lookup%1$s(value %2$s) (%1$s, error) {\n" +
            "        switch value {\n",
            enumName,
            goTypeName);

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());

            sb.append("            case ").append(constVal)
                .append(": return ").append(enumName).append("_")
                .append(token.name()).append(", nil\n");
        }

        final CharSequence nullVal = generateLiteral(
            encodingToken.encoding().primitiveType(), encodingToken.encoding().applicableNullValue().toString());

        sb.append("            case ").append(nullVal).append(": return ")
            .append(enumName).append("_").append("NULL_VALUE, nil\n")
            .append("        }\n\n");

        if (shouldDecodeUnknownEnumValues)
        {

            sb.append("    return ").append(enumName).append("_").append("UNKNOWN").append(", nil\n}\n");
        }
        else
        {
            new Formatter(sb).format("        return 0, %1$sLookupErr\n}\n",
                enumName);
        }

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
            indent + "        if m.actingVersion < %1$d {\n" +
                indent + "            return %2$s\n" +
                indent + "        }\n\n",
            sinceVersion,
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString()));
    }


    private String namespacesToPackageName(final CharSequence[] namespaces)
    {
        return String.join("_", namespaces).toLowerCase().replace('.', '_').replace(' ', '_').replace('-', '_');
    }

    private CharSequence generateFileHeader(final CharSequence[] namespaces)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message codec */\n");


        sb.append(String.format(
            "// Code generated by SBE. DO NOT EDIT.\n\n" +

            "package %1$s\n\n" +

            "import (\n" +
            "%2$s\n" +
            ")\n\n",
            namespacesToPackageName(namespaces),
            formatIncludes(includes)));

        return sb;
    }

    private CharSequence generateEnumFileHeader(final CharSequence[] namespaces)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message codec */\n");

        sb.append(String.format(
            "// Code generated by SBE. DO NOT EDIT.\n\n" +

            "package %1$s\n\n" +

            "import (\n" +
            "%2$s\n" +
            ")\n\n",
            namespacesToPackageName(namespaces),
            formatIncludes(includes)));

        return sb;
    }

    private CharSequence generateCompositePropertyElements(
        final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size(); )
        {
            final Token fieldToken = tokens.get(i);
            final String propertyName = formatPropertyName(fieldToken.name());

            generateFieldMetaAttributeMethod(sb, fieldToken, indent, containingClassName);
            generateFieldCommonMethods(indent, sb, fieldToken, fieldToken, propertyName, containingClassName);

            switch (fieldToken.signal())
            {
                case ENCODING:
                    generatePrimitiveProperty(sb, containingClassName, propertyName, fieldToken, fieldToken, indent);
                    break;

                case BEGIN_ENUM:
                    generateEnumProperty(sb, containingClassName, fieldToken, propertyName, fieldToken, indent);
                    break;

                case BEGIN_SET:
                    generateBitsetProperty(sb, propertyName, fieldToken, indent, containingClassName);
                    break;

                case BEGIN_COMPOSITE:
                    generateCompositeProperty(sb, propertyName, fieldToken, indent, containingClassName);
                    break;

                default:
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
        generatePrimitiveFieldMetaData(sb, propertyName, encodingToken, indent, containingClassName);

        if (encodingToken.isConstantEncoding())
        {
            generateConstPropertyMethods(sb, containingClassName, propertyName, encodingToken, indent);
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
        final StringBuilder sb,
        final String propertyName,
        final Token token,
        final String indent,
        final String className)
    {
        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String goTypeName = goTypeName(primitiveType);
        final CharSequence nullValueString = generateNullValueLiteral(primitiveType, encoding);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%4$s) %1$sNullValue() %2$s {\n" +
            indent + "        return %3$s\n" +
            indent + "    }\n",
            propertyName,
            goTypeName,
            nullValueString,
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%4$s) %1$sMinValue() %2$s {\n" +
            indent + "        return %3$s\n" +
            indent + "    }\n",
            propertyName,
            goTypeName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString()),
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%4$s) %1$sMaxValue() %2$s {\n" +
            indent + "        return %3$s\n" +
            indent + "    }\n",
            propertyName,
            goTypeName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString()),
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sEncodingLength() int {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            token.encoding().primitiveType().size() * token.arrayLength(),
            className);
    }

    private CharSequence generateLoadValue(
        final PrimitiveType primitiveType,
        final String offsetStr,
        final ByteOrder byteOrder,
        final String indent)
    {
        final String goTypeName = goTypeName(primitiveType);
        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format(
            indent + "        return %3$s(m.buffer[(m.offset + %1$s):])\n",
            offsetStr,
            goTypeName,
            formatReadBytes(byteOrder, primitiveType));

        return sb;
    }

    private CharSequence generateStoreValue(
        final PrimitiveType primitiveType,
        final String valueSuffix,
        final String offsetStr,
        final ByteOrder byteOrder,
        final String indent)
    {
        final String goTypeName = goTypeName(primitiveType);
        final String goMarshalType = golangMarshalType(primitiveType);
        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format(
            indent + "        %3$s(m.buffer[(m.offset + %4$s):], value%2$s)\n",
            goTypeName,
            valueSuffix,
            formatWriteBytes(byteOrder, primitiveType),
            offsetStr,
            goMarshalType);

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
        final String goTypeName = goTypeName(primitiveType);
        final int offset = encodingToken.offset();

        new Formatter(sb).format("\n" +
            indent + "    func (m *%1$s) %2$s() %3$s {\n" +
            "%4$s" +
            "%5$s" +
            indent + "    }\n",
            containingClassName,
            propertyName,
            goTypeName,
            generateFieldNotPresentCondition(propertyToken.version(), encodingToken.encoding(), indent),
            generateLoadValue(primitiveType, Integer.toString(offset), encodingToken.encoding().byteOrder(), indent));

        final CharSequence storeValue = generateStoreValue(
            primitiveType, "", Integer.toString(offset), encodingToken.encoding().byteOrder(), indent);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%1$s) Set%2$s(value %3$s) *%1$s {\n" +
            "%4$s" +
            indent + "        return m\n" +
            indent + "    }\n",
            containingClassName,
            propertyName,
            goTypeName,
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
        final String goTypeName = goTypeName(primitiveType);
        final int offset = encodingToken.offset();

        final int arrayLength = encodingToken.arrayLength();
        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sLength() int {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            arrayLength,
            formatClassName(containingClassName));

        new Formatter(sb).format("\n" +
            indent + "func (m *%6$s) %2$s() []byte {\n" +
            "%4$s" +
            indent + "    return m.buffer[m.offset+%5$d:m.offset+%5$d+%3$d]\n" +
            indent + "}\n",
            goTypeName,
            toUpperFirstChar(propertyName),
            arrayLength,
            generateArrayFieldNotPresentCondition(propertyToken.version(), indent),
            offset,
            formatClassName(containingClassName));

        final CharSequence loadValue = generateLoadValue(
            primitiveType,
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            encodingToken.encoding().byteOrder(),
            indent);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%6$s) %2$sIndex(index uint64) %1$s {\n" +
            indent + "        if (index >= %3$d) {\n" +
            indent + "            panic(\"index out of range for %2$s [E104]\")\n" +
            indent + "        }\n\n" +
            "%4$s" +
            "%5$s" +
            indent + "    }\n",
            goTypeName,
            propertyName,
            arrayLength,
            generateFieldNotPresentCondition(propertyToken.version(), encodingToken.encoding(), indent),
            loadValue,
            formatClassName(containingClassName));

        final CharSequence storeValue = generateStoreValue(
            primitiveType,
            "",
            String.format("%d + (index * %d)", offset, primitiveType.size()),
            encodingToken.encoding().byteOrder(),
            indent);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%6$s) Set%2$sIndex(index uint64, value %3$s) *%6$s {\n" +
            indent + "        if (index >= %4$d) {\n" +
            indent + "            panic(\"index out of range for %2$s [E105]\")\n" +
            indent + "        }\n\n" +

            "%5$s" +
            indent + "        return m\n" +
            indent + "    }\n",
            containingClassName,
            propertyName,
            goTypeName,
            arrayLength,
            storeValue,
            formatClassName(containingClassName));


        addInclude("fmt");
        new Formatter(sb).format("\n" +
            indent + "func (m *%6$s) Get%2$s(dst []byte) (uint64, error) {\n" +
            indent + "    if len(dst) > %3$d {\n" +
            indent + "        return 0, fmt.Errorf(\"length too large for get%2$s [E106]\")\n" +
            indent + "    }\n\n" +

            "%4$s" +
            indent + "    copy(dst, m.buffer[m.offset+%5$d:])\n" +
            indent + "    return %3$d, nil\n" +
            indent + "}\n",
            goTypeName,
            toUpperFirstChar(propertyName),
            arrayLength,
            generateArrayFieldNotPresentConditionWithErr(propertyToken.version(), indent),
            offset,
            formatClassName(containingClassName));

        new Formatter(sb).format("\n" +
            indent + "func (m *%1$s) Put%2$s(src []byte) *%1$s {\n" +
            indent + "    copy(m.buffer[(m.offset+%3$d):], src)\n" +
            indent + "    return m\n" +
            indent + "}\n",
            formatClassName(containingClassName),
            formatPropertyName(propertyName),
            offset);

        if (arrayLength > 1 && arrayLength <= 4)
        {
            sb.append("\n").append(indent).append("func (m *")
                .append(formatClassName(containingClassName)).append(") Put")
                .append(formatPropertyName(propertyName))
                .append("Values (\n");

            for (int i = 0; i < arrayLength; i++)
            {
                sb.append(indent).append("    value").append(i)
                    .append(" ")
                    .append(goTypeName);

                if (i < (arrayLength - 1))
                {
                    sb.append(",\n");
                }
            }

            sb.append(")")
                .append(" *")
                .append(formatClassName(containingClassName))
                .append(" {\n");

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
            sb.append(indent).append("    return m\n");
            sb.append(indent).append("}\n");
        }


        if (encodingToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            new Formatter(sb).format("\n" +
                indent + "    func (m *%4$s) Get%1$sAsString() string {\n" +
                indent + "        offset := m.offset + %2$d\n" +
                indent + "        length := uint64(0)\n\n" +
                indent + "        for length < %3$d && m.buffer[offset + length] != '\\x00' {\n" +
                indent + "            length++\n" +
                indent + "        }\n" +
                indent + "        return string(m.buffer[offset:offset + length])\n" +
                indent + "    }\n",
                toUpperFirstChar(propertyName),
                offset,
                arrayLength,
                formatClassName(containingClassName));

            generateJsonEscapedStringGetter(sb, encodingToken, indent, propertyName, containingClassName);

            new Formatter(sb).format("\n" +
                indent + "func (m *%1$s) Set%2$s(src string) *%1$s {\n" +
                indent + "    copy(m.buffer[(m.offset+%3$d):], []byte(src))\n" +
                indent + "    return m\n" +
                indent + "}\n",
                formatClassName(containingClassName),
                formatPropertyName(propertyName),
                offset);
        }
    }

    private void generateJsonEscapedStringGetter(
        final StringBuilder sb,
        final Token token,
        final String indent,
        final String propertyName,
        final String className)
    {
        addInclude("strings");
        addInclude("fmt");
        new Formatter(sb).format("\n" +
            indent + "func (m *%3$s) Get%1$sAsJsonEscapedString() string {\n" +
            "%2$s" +
            indent + "    oss := strings.Builder{}\n" +
            indent + "    s := m.Get%1$sAsString()\n\n" +
            indent + "    for _, c := range s {\n" +
            indent + "        switch c {\n" +
            indent + "            case '\"': oss.WriteString(\"\\\\\\\"\")\n" +
            indent + "            case '\\\\': oss.WriteString(\"\\\\\\\\\")\n" +
            indent + "            case '\\b': oss.WriteString(\"\\\\b\")\n" +
            indent + "            case '\\f': oss.WriteString(\"\\\\f\")\n" +
            indent + "            case '\\n': oss.WriteString(\"\\\\n\")\n" +
            indent + "            case '\\r': oss.WriteString(\"\\\\r\")\n" +
            indent + "            case '\\t': oss.WriteString(\"\\\\t\")\n\n" +
            indent + "            default:\n" +
            indent + "                if '\\x00' <= c && c <= '\\x1f' {\n" +
            indent + "                    fmt.Fprintf(&oss, \"%%x\", c)\n" +
            indent + "                } else {\n" +
            indent + "                    oss.WriteString(string(c))\n" +
            indent + "                }\n" +
            indent + "        }\n" +
            indent + "    }\n\n" +
            indent + "    return oss.String()\n" +
            indent + "}\n",
            toUpperFirstChar(propertyName),
            generateStringNotPresentCondition(token.version(), indent),
            className);
    }

    private void generateConstPropertyMethods(
        final StringBuilder sb,
        final String className,
        final String propertyName,
        final Token token,
        final String indent)
    {
        final String goTypeName = goTypeName(token.encoding().primitiveType());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            new Formatter(sb).format("\n" +
                indent + "    func (m *%4$s) %2$s() %1$s {\n" +
                indent + "        return %3$s\n" +
                indent + "    }\n",
                goTypeName,
                propertyName,
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()),
                className);

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
            indent + "    func (m *%3$s) %1$sLength() uint64 {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            constantValue.length,
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$s() string {\n" +
            indent + "        %1$sValues := []uint8{ %2$s, 0 }\n\n" +
            indent + "        return string(%1$sValues)\n" +
            indent + "    }\n",
            propertyName,
            values,
            className);

        sb.append(String.format("\n" +
            indent + "    func (m *%4$s) %2$sIndex(index uint64) %1$s {\n" +
            indent + "        %2$sValues := []uint8{ %3$s, 0 }\n\n" +
            indent + "        return byte(%2$sValues[index])\n" +
            indent + "    }\n",
            goTypeName,
            propertyName,
            values,
            className));

        new Formatter(sb).format("\n" +
            indent + "    func (m *%4$s) Get%1$s(dst []byte, length uint64) uint64 {\n" +
            indent + "        %2$sValues := []uint8{ %3$s }\n" +
            indent + "        bytesToCopy := uint64(len(%2$sValues))\n" +
            indent + "        if length < uint64(len(%2$sValues)) {\n" +
            indent + "            bytesToCopy = length\n" +
            indent + "        }\n" +
            indent + "        copy(dst, %2$sValues[:bytesToCopy])\n" +
            indent + "        return bytesToCopy\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            propertyName,
            values,
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) Get%1$sAsString() string {\n" +
            indent + "        %1$sValues := []uint8{ %2$s, 0 }\n\n" +
            indent + "        return string(%1$sValues)\n" +
            indent + "    }\n",
            propertyName,
            values,
            className);

        generateJsonEscapedStringGetter(sb, token, indent, propertyName, className);
    }

    private CharSequence generateFixedFlyweightCode(
        final String className,
        final int size,
        final List<Token> fields)
    {
        final String schemaIdType = goTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = goTypeName(ir.headerStructure().schemaVersionType());

        final StringBuilder sb = new StringBuilder();
        for (int i = 1, fieldSize = fields.size(); i < fieldSize; )
        {
            final Token signalToken = fields.get(i);
            final String propertyName = formatPropertyName(signalToken.name());
            final String typeName = formatClassName(signalToken.applicableTypeName());

            switch (signalToken.signal())
            {
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    new Formatter(sb).format("    _%1$s %2$s\n",
                        propertyName,
                        typeName);
                    break;

                default:
                    break;
            }

            i += fields.get(i).componentTokenCount();
        }

        String sizeValue = String.format("%1$d", size);
        if (size == -1)
        {
            addInclude("math");
            sizeValue = "math.MaxUint64";
        }

        return String.format(
            "    buffer []byte\n" +
                "    bufferLength uint64\n" +
                "    offset uint64\n" +
                "    actingVersion uint64\n" +
                "%7$s\n" +
                "}\n\n" +

                "    const (\n" +
                "        %1$sEncodedLength    uint64 = %2$s\n" +
                "        %1$sSbeSchemaID        %3$s = %4$s\n" +
                "        %1$sSbeSchemaVersion   %5$s = %6$s\n" +
                "    )\n\n" +

                "    func (m *%1$s) Wrap(\n" +
                "        buffer []byte,\n" +
                "        offset uint64,\n" +
                "        actingVersion uint64,\n" +
                "        bufferLength uint64) {\n" +
                "        m.buffer = buffer\n" +
                "        m.bufferLength = bufferLength\n" +
                "        m.offset = offset\n" +
                "        m.actingVersion = actingVersion\n" +
                "        if !SbeNoBoundsCheck && ((m.offset + %2$s) > m.bufferLength) {\n" +
                "            panic(\"buffer too short for flyweight [E107]\")\n" +
                "        }\n" +
                "    }\n\n" +

                "    func (m *%1$s) EncodedLength() uint64 {\n" +
                "        return %2$s\n" +
                "    }\n\n" +

                "    func (m *%1$s) Offset() uint64 {\n" +
                "        return m.offset\n" +
                "    }\n\n" +

                "    func (m *%1$s) Buffer() []byte {\n" +
                "        return m.buffer\n" +
                "    }\n\n" +

                "    func (m *%1$s) BufferLength() uint64 {\n" +
                "        return m.bufferLength\n" +
                "    }\n\n" +

                "    func (m *%1$s) ActingVersion() uint64 {\n" +
                "        return m.actingVersion\n" +
                "    }\n\n" +

                "    func (m *%1$s) SbeSchemaId() %3$s {\n" +
                "        return %4$s\n" +
                "    }\n\n" +

                "    func (m *%1$s) SbeSchemaVersion() %5$s {\n" +
                "        return %6$s\n" +
                "    }\n",

            className,
            sizeValue,
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(ir.version())),
            sb);
    }

    private CharSequence generateMessageFlyweightCode(
        final String className,
        final Token token,
        final List<Token> fields,
        final List<Token> groups)
    {
        final StringBuilder sb = new StringBuilder();
        final String blockLengthType = goTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = goTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = goTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = goTypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();
        final String semanticVersion = ir.semanticVersion() == null ? "" : ir.semanticVersion();

        sb.append("    buffer []byte\n" +
            "    bufferLength uint64\n" +
            "    offset uint64\n" +
            "    position uint64\n" +
            "    actingBlockLength uint64\n" +
            "    actingVersion uint64\n");

        for (int i = 0, size = fields.size(); i < size; i++)
        {
            final Token signalToken = fields.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = fields.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());
                final String typeName = formatClassName(encodingToken.applicableTypeName());

                switch (encodingToken.signal())
                {
                    case BEGIN_SET:
                    case BEGIN_COMPOSITE:
                        new Formatter(sb).format("    _%1$s %2$s\n",
                            propertyName,
                            typeName);
                        break;

                    default:
                        break;
                }
            }
        }

        for (int i = 0, size = groups.size(); i < size; )
        {
            final Token groupToken = groups.get(i);
            if (groupToken.signal() == Signal.BEGIN_GROUP)
            {
                final String propertyName = formatPropertyName(groupToken.name());
                final String groupName = className + formatClassName(groupToken.name());

                new Formatter(sb).format("    _%1$s %2$s\n",
                    propertyName,
                    groupName);

                i += groupToken.componentTokenCount();
            }
        }

        sb.append("}\n\n");

        sb.append(String.format(
            "    func (m *%10$s) sbePositionPtr() *uint64 {\n" +
            "        return &m.position\n" +
            "    }\n\n" +

            "    const (\n" +
            "        %10$sSbeBlockLength     %1$s = %2$s\n" +
            "        %10$sSbeTemplateID      %3$s = %4$s\n" +
            "        %10$sSbeSchemaID        %5$s = %6$s\n" +
            "        %10$sSbeSchemaVersion   %7$s = %8$s\n" +
            "        %10$sSbeSemanticVersion string = \"%11$s\"\n" +
            "    )\n\n" +

            "    func (m *%10$s) SbeBlockLength() %1$s {\n" +
            "        return %2$s\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeBlockAndHeaderLength() uint64 {\n" +
            "        return MessageHeaderEncodedLength + uint64(m.SbeBlockLength())\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeTemplateId() %3$s {\n" +
            "        return %4$s\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeSchemaId() %5$s {\n" +
            "        return %6$s\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeSchemaVersion() %7$s {\n" +
            "        return %8$s\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeSemanticVersion() string {\n" +
            "        return \"%11$s\"\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeSemanticType() string {\n" +
            "        return \"%9$s\"\n" +
            "    }\n\n" +

            "    func (m *%10$s) Offset() uint64 {\n" +
            "        return m.offset\n" +
            "    }\n\n" +

            "    func (m *%10$s) Wrap(\n" +
            "        buffer []byte,\n" +
            "        offset uint64,\n" +
            "        bufferLength uint64,\n" +
            "        actingBlockLength uint64,\n" +
            "        actingVersion uint64) *%10$s {\n" +
            "        m.buffer = buffer\n" +
            "        m.bufferLength = bufferLength\n" +
            "        m.offset = offset\n" +
            "        m.position = m.SbeCheckPosition(offset + actingBlockLength)\n" +
            "        m.actingBlockLength = actingBlockLength\n" +
            "        m.actingVersion = actingVersion\n" +
            "        return m\n" +
            "    }\n\n" +

            "    func (m *%10$s) WrapForEncode(buffer []byte, offset uint64, bufferLength uint64) *%10$s {\n" +
            "        return m.Wrap(buffer, offset, bufferLength,\n" +
            "            uint64(m.SbeBlockLength()), uint64(m.SbeSchemaVersion()))\n" +
            "    }\n\n" +

            "    func (m *%10$s) WrapAndApplyHeader(buffer []byte, offset uint64, bufferLength uint64) *%10$s {\n" +
            "        var hdr MessageHeader\n" +
            "        hdr.Wrap(buffer, offset, uint64(m.SbeSchemaVersion()), bufferLength)\n\n" +

            "        hdr.SetBlockLength(m.SbeBlockLength()).\n" +
            "           SetTemplateId(m.SbeTemplateId()).\n" +
            "           SetSchemaId(m.SbeSchemaId()).\n" +
            "           SetVersion(m.SbeSchemaVersion())\n\n" +

            "        return m.Wrap(\n" +
            "            buffer,\n" +
            "            offset + hdr.EncodedLength(),\n" +
            "            bufferLength,\n" +
            "            uint64(m.SbeBlockLength()),\n" +
            "            uint64(m.SbeSchemaVersion()))\n" +
            "    }\n\n" +

            "    func (m *%10$s) WrapForDecode(\n" +
            "        buffer []byte,\n" +
            "        offset uint64,\n" +
            "        actingBlockLength uint64,\n" +
            "        actingVersion uint64,\n" +
            "        bufferLength uint64) *%10$s {\n" +
            "        return m.Wrap(\n" +
            "            buffer,\n" +
            "            offset,\n" +
            "            bufferLength,\n" +
            "            actingBlockLength,\n" +
            "            actingVersion)\n" +
            "    }\n\n" +

            "    func (m *%10$s) sbeRewind() *%10$s {\n" +
            "        return m.WrapForDecode(" +
            "m.buffer, m.offset, m.actingBlockLength, m.actingVersion, m.bufferLength)\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbePosition() uint64 {\n" +
            "        return m.position\n" +
            "    }\n\n" +

            "    func (m *%10$s) SbeCheckPosition(position uint64) uint64 {\n" +
            "        if !SbeNoBoundsCheck && (position > m.bufferLength) {\n" +
            "            panic(\"buffer too short [E100]\")\n" +
            "        }\n" +
            "        return position\n" +
            "    }\n\n" +

            "    func (m *%10$s) SetSbePosition(position uint64) {\n" +
            "        m.position = m.SbeCheckPosition(position)\n" +
            "    }\n\n" +

            "    func (m *%10$s) EncodedLength() uint64 {\n" +
            "        return m.SbePosition() - m.offset\n" +
            "    }\n\n" +

            "    func (m *%10$s) DecodeLength() uint64 {\n" +
            "        var skipper %10$s\n" +
            "        skipper.WrapForDecode(m.buffer, m.offset, uint64(m.SbeBlockLength()),\n" +
            "            uint64(m.ActingVersion()), m.bufferLength)\n" +
            "        skipper.Skip()\n" +
            "        return skipper.EncodedLength()\n" +
            "    }\n\n" +

            "    func (m *%10$s) Buffer() []byte {\n" +
            "        return m.buffer\n" +
            "    }\n\n" +

            "    func (m *%10$s) BufferLength() uint64 {\n" +
            "        return m.bufferLength\n" +
            "    }\n\n" +

            "    func (m *%10$s) ActingVersion() uint64 {\n" +
            "        return m.actingVersion\n" +
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
            semanticVersion));

        return sb.toString();
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

                generateFieldMetaAttributeMethod(sb, signalToken, indent, containingClassName);
                generateFieldCommonMethods(indent, sb, signalToken, encodingToken, propertyName, containingClassName);

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
                        generateBitsetProperty(sb, propertyName, encodingToken, indent, containingClassName);
                        break;

                    case BEGIN_COMPOSITE:
                        generateCompositeProperty(sb, propertyName, encodingToken, indent, containingClassName);
                        break;

                    default:
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
        final String propertyName,
        final String className)
    {
        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sId() int {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            fieldToken.id(),
            className);

        final int version = fieldToken.version();
        final String versionCheck = 0 == version ?
            "        return true\n" : "        return m.actingVersion >= m.%1$sSinceVersion()\n";
        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sSinceVersion() uint64 {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n\n" +

            indent + "    func (m *%3$s) %1$sInActingVersion() bool {\n" +
            indent + versionCheck +
            indent + "    }\n",
            propertyName,
            version,
            className);

        new Formatter(sb).format("\n" +
            indent + "    func (m *%3$s) %1$sEncodingOffset() uint {\n" +
            indent + "        return %2$d\n" +
            indent + "    }\n",
            propertyName,
            encodingToken.offset(),
            className);
    }

    private void generateEnumProperty(
        final StringBuilder sb,
        final String containingClassName,
        final Token fieldToken,
        final String propertyName,
        final Token encodingToken,
        final String indent)
    {
        final String enumName = formatClassName(encodingToken.applicableTypeName());
        final PrimitiveType primitiveType = encodingToken.encoding().primitiveType();
        final String typeName = goTypeName(primitiveType);
        final int offset = encodingToken.offset();

        new Formatter(sb).format("\n" +
            indent + "    func (m *%1$s) %2$sEncodingLength() int {\n" +
            indent + "        return %3$d\n" +
            indent + "    }\n",
            containingClassName,
            propertyName,
            fieldToken.encodedLength());

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();

            new Formatter(sb).format("\n" +
                indent + "    func (m *%1$s) %2$sConstValue() %4$s {\n" +
                indent + "        return %4$s_%3$s\n" +
                indent + "    }\n",
                containingClassName,
                propertyName,
                constValue.substring(constValue.indexOf(".") + 1),
                enumName);

            new Formatter(sb).format("\n" +
                indent + "    func (m *%1$s) %2$s() %5$s {\n" +
                "%3$s" +
                indent + "        return %5$s_%4$s\n" +
                indent + "    }\n",
                containingClassName,
                propertyName,
                generateEnumFieldNotPresentCondition(fieldToken.version(), enumName, indent),
                constValue.substring(constValue.indexOf(".") + 1),
                enumName);

            new Formatter(sb).format("\n" +
                indent + "    func (m *%3$s) %2$sRaw() %1$s {\n" +
                indent + "        return %1$s(%5$s_%4$s)\n" +
                indent + "    }\n",
                typeName,
                propertyName,
                containingClassName,
                constValue.substring(constValue.indexOf(".") + 1),
                enumName);

        }
        else
        {
            final String offsetStr = Integer.toString(offset);
            new Formatter(sb).format("\n" +
                indent + "    func (m *%5$s) %2$sRaw() %1$s {\n" +
                "%3$s" +
                "%4$s" +
                indent + "    }\n",
                typeName,
                propertyName,
                generateFieldNotPresentCondition(fieldToken.version(), encodingToken.encoding(), indent),
                generateLoadValue(primitiveType, offsetStr, encodingToken.encoding().byteOrder(), indent),
                containingClassName);

            new Formatter(sb).format("\n" +
                indent + "    func (m *%1$s) %2$s() %7$s {\n" +
                "%3$s" +
                indent + "        return %7$s(%4$s(m.buffer[(m.offset + %6$d):]))\n" +
                indent + "    }\n",
                containingClassName,
                propertyName,
                generateEnumFieldNotPresentCondition(fieldToken.version(), enumName, indent),
                formatReadBytes(encodingToken.encoding().byteOrder(), primitiveType),
                typeName,
                offset,
                enumName);

            new Formatter(sb).format("\n" +
                indent + "    func (m *%1$s) Set%2$s(value %3$s) *%1$s {\n" +
                indent + "        val := %4$s(value)\n" +
                indent + "        %6$s(m.buffer[(m.offset + %5$d):], val)\n" +
                indent + "        return m\n" +
                indent + "    }\n",
                formatClassName(containingClassName),
                propertyName,
                enumName,
                typeName,
                offset,
                formatWriteBytes(encodingToken.encoding().byteOrder(), primitiveType));
        }
    }

    private CharSequence generateNullValueLiteral(
        final PrimitiveType primitiveType,
        final Encoding encoding)
    {
        // Go does not handle minimum integer values properly
        // So null values get special handling
        if (null == encoding.nullValue())
        {
            switch (primitiveType)
            {
                case CHAR:
                    return "SbeNullValueByte()";
                case FLOAT:
                    return "SbeNullValueFloat()";
                case DOUBLE:
                    return "SbeNullValueDouble()";
                case INT8:
                    return "SbeNullValueInt8()";
                case INT16:
                    return "SbeNullValueInt16()";
                case INT32:
                    return "SbeNullValueInt32()";
                case INT64:
                    return "SbeNullValueInt64()";
                case UINT8:
                    return "SbeNullValueUint8()";
                case UINT16:
                    return "SbeNullValueUint16()";
                case UINT32:
                    return "SbeNullValueUint32()";
                case UINT64:
                    return "SbeNullValueUint64()";
            }
        }

        return generateLiteral(primitiveType, encoding.applicableNullValue().toString());
    }


    private CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        switch (type)
        {
            case CHAR:
                literal = "byte(" + value + ")";
                break;
            case UINT8:
                literal = "uint8(" + value + ")";
                break;
            case UINT16:
                literal = "uint16(" + value + ")";
                break;
            case INT8:
                literal = "int8(" + value + ")";
                break;
            case INT16:
                literal = "int16(" + value + ")";
                break;

            case UINT32:
                literal = "uint32(0x" + Integer.toHexString((int)Long.parseLong(value)) + ")";
                break;

            case INT32:
                final long intValue = Long.parseLong(value);
                if (intValue == Integer.MIN_VALUE)
                {
                    literal = "math.MinInt32";
                    addInclude("math");
                }
                else
                {
                    literal = "int32(" + value + ")";
                }
                break;

            case FLOAT:
                if (value.endsWith("NaN"))
                {
                    literal = "math.NaN()";
                }
                else
                {
                    literal = value;
                }
                break;

            case INT64:
                final long longValue = Long.parseLong(value);
                if (longValue == Long.MIN_VALUE)
                {
                    literal = "math.MinInt64";
                    addInclude("math");
                }
                else
                {
                    literal = "int64(" + value + ")";
                }
                break;

            case UINT64:
                literal = "uint64(0x" + Long.toHexString(Long.parseLong(value)) + ")";
                break;

            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "math.NaN()";
                    addInclude("math");
                }
                else
                {
                    literal = value;
                }
                break;
        }

        return literal;
    }

    private void addInclude(final String name)
    {
        includes.add(name);
    }


    private void generateDisplay(
        final StringBuilder sb,
        final String containingClassName,
        final String name,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData)
    {
        addInclude("strings");
        addInclude("fmt");
        new Formatter(sb).format("\n" +
            "func (writer *%1$s) String() string {\n" +
            "    var builder strings.Builder\n" +
            "    builder.WriteString(\"{\\\"Name\\\": \\\"%1$s\\\", \")\n" +
            "    builder.WriteString(fmt.Sprintf(\"\\\"sbeTemplateId\\\": %%d\", writer.SbeTemplateId()))\n" +
            "    builder.WriteString(\", \")\n\n" +
            "%2$s" +
            "    builder.WriteString(\"}\")\n\n" +
            "    return builder.String()\n" +
            "}\n",
            formatClassName(name),
            appendDisplay(containingClassName, fields, groups, varData, INDENT));
    }


    private CharSequence generateGroupDisplay(
        final String groupClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        addInclude("strings");
        return String.format("\n" +
                indent + "func (writer *%1$s) String() string {\n" +
                indent + "    builder := strings.Builder{}\n" +
                indent + "    builder.WriteString(\"{\")\n" +
                "%2$s" +
                indent + "    builder.WriteString(\"}\")\n\n" +
                indent + "    return builder.String()\n" +
                indent + "}\n",
            groupClassName,
            appendDisplay(groupClassName, fields, groups, varData, indent + INDENT));
    }


    private CharSequence generateCompositeDisplay(final String name, final List<Token> tokens)
    {
        addInclude("strings");
        return String.format("\n" +
                "func (writer *%1$s) String() string {\n" +
                "    builder := strings.Builder{}\n" +
                "    builder.WriteString(\"{\")\n" +
                "%2$s" +
                "    builder.WriteString(\"}\")\n\n" +
                "    return builder.String()\n" +
                "}\n\n",
            formatClassName(name),
            appendDisplay(formatClassName(name), tokens, new ArrayList<>(), new ArrayList<>(), INDENT));
    }


    private CharSequence appendDisplay(
        final String containingClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        final StringBuilder sb = new StringBuilder();
        final boolean[] atLeastOne = {false};

        for (int i = 0, size = fields.size(); i < size; )
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
                sb.append(indent).append("builder.WriteString(\", \")\n");
            }
            atLeastOne[0] = true;

            new Formatter(sb).format(
                indent + "{\n" +
                indent + "    atLeastOne := false\n" +
                indent + "    builder.WriteString(`\"%3$s\": [`)\n" +
                indent + "    writer.%2$s().ForEach(\n" +
                indent + "        func(value *%1$s) {\n" +
                indent + "            if atLeastOne {\n" +
                indent + "                builder.WriteString(\", \")\n" +
                indent + "            }\n" +
                indent + "            atLeastOne = true\n" +
                indent + "            builder.WriteString(value.String())\n" +
                indent + "        })\n" +
                indent + "    builder.WriteString(\"]\")\n" +
                indent + "}\n\n",
                containingClassName + formatClassName(groupToken.name()),
                formatPropertyName(groupToken.name()),
                groupToken.name());

            i = findEndSignal(groups, i, Signal.END_GROUP, groupToken.name());
        }

        for (int i = 0, size = varData.size(); i < size; )
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            if (atLeastOne[0])
            {
                sb.append(indent).append("builder.WriteString(\", \")\n");
            }
            atLeastOne[0] = true;

            final String characterEncoding = varData.get(i + 3).encoding().characterEncoding();
            sb.append(indent).append("builder.WriteString(`\"").append(varDataToken.name()).append("\": `)\n");

            if (null == characterEncoding)
            {
                final String skipFunction = "writer.Skip" + toUpperFirstChar(varDataToken.name()) + "()";

                sb.append(indent).append("builder.WriteString(`\"`)\n")
                    .append(indent).append(INDENT).append(skipFunction).append("\n")
                    .append(INDENT).append("builder.WriteString(\" bytes of raw data\")\n");
            }
            else
            {
                final String getAsStringFunction =
                    "writer.Get" + toUpperFirstChar(varDataToken.name()) + "AsJsonEscapedString()";

                sb.append(indent).append("builder.WriteString(`\"` + ")
                    .append(getAsStringFunction).append(" + `\"`);\n\n");
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
            sb.append(indent).append("builder.WriteString(\", \")\n");
        }
        else
        {
            atLeastOne[0] = true;
        }

        sb.append(indent).append("builder.WriteString(`\"").append(fieldTokenName).append("\": `)\n");
        final String fieldName = "writer." + formatPropertyName(fieldTokenName);

        switch (typeToken.signal())
        {
            case ENCODING:
                if (typeToken.arrayLength() > 1)
                {
                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        final String getAsStringFunction =
                            "writer.Get" + toUpperFirstChar(fieldTokenName) + "AsJsonEscapedString()";

                        sb.append(indent).append("builder.WriteString(`\"` +\n").append(indent).append(INDENT)
                            .append(getAsStringFunction).append(" + `\"`)\n");
                    }
                    else
                    {
                        addInclude("fmt");
                        sb.append(
                            indent + "builder.WriteString(\"[\")\n" +
                            indent + "for i := 0; i < " + fieldName + "Length(); i++ {\n" +
                            indent + "    if i > 0 {\n" +
                            indent + "        builder.WriteString(\",\")\n" +
                            indent + "    }\n" +
                            indent + "    builder.WriteString(fmt.Sprintf(\"%v\", " + fieldName +
                            "Index(uint64(i))))\n" +
                            indent + "}\n" +
                            indent + "builder.WriteString(\"]\")\n");
                    }
                }
                else
                {
                    addInclude("fmt");

                    if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        sb.append(indent).append("builder.WriteString(fmt.Sprintf(`\"%c\"`, ").append(fieldName)
                            .append("()))\n");
                    }
                    else
                    {
                        sb.append(indent).append("builder.WriteString(fmt.Sprintf(`\"%v\"`, ").append(fieldName)
                            .append("()))\n");
                    }
                }
                break;

            case BEGIN_ENUM:
                addInclude("fmt");
                sb.append(indent).append("builder.WriteString(fmt.Sprintf(`\"%v\"`, ").append(fieldName)
                    .append("()))\n");
                break;

            case BEGIN_SET:
            case BEGIN_COMPOSITE:
                if (0 == typeToken.version())
                {
                    sb.append(indent).append("builder.WriteString(").append(fieldName).append("().String())\n");
                }
                else
                {
                    new Formatter(sb).format(
                        indent + "if (%1$sInActingVersion()) {\n" +
                        indent + "    builder.WriteString(%1$s().String())\n" +
                        indent + "} else {\n" +
                        indent + "    builder.WriteString(%2$s)\n" +
                        indent + "}\n",
                        fieldName,
                        typeToken.signal() == Signal.BEGIN_SET ? "\"[]\"" : "\"{}\"");
                }
                break;

            default:
                break;
        }

        sb.append('\n');
    }


    private CharSequence generateChoicesDisplay(
        final String name,
        final List<Token> tokens)
    {
        final String indent = INDENT;
        final StringBuilder sb = new StringBuilder();
        final List<Token> choiceTokens = new ArrayList<>();

        collect(Signal.CHOICE, tokens, 0, choiceTokens);

        addInclude("strings");
        new Formatter(sb).format("\n" +
            indent + "func (writer %1$s) String() string {\n" +
            indent + "    builder := strings.Builder{}\n" +
            indent + "    builder.WriteString(\"[\")\n",
            name);

        if (choiceTokens.size() > 1)
        {
            sb.append(indent + "    atLeastOne := false\n");
        }

        for (int i = 0, size = choiceTokens.size(); i < size; i++)
        {
            final Token token = choiceTokens.get(i);
            final String choiceName = "writer." + formatPropertyName(token.name());

            sb.append(indent + "    if ").append(choiceName).append("() {\n");

            if (i > 0)
            {
                sb.append(
                    indent + "        if (atLeastOne) {\n" +
                    indent + "            builder.WriteString(\",\")\n" +
                    indent + "        }\n");
            }
            sb.append(indent + "        builder.WriteString(`\"").append(formatPropertyName(token.name()))
                .append("\"`)\n");

            if (i < (size - 1))
            {
                sb.append(indent + "        atLeastOne = true\n");
            }

            sb.append(indent + "    }\n");
        }

        sb.append(
            indent + "    builder.WriteString(\"]\")\n" +
            indent + "    return builder.String()\n" +
            indent + "}\n");

        return sb;
    }


    private CharSequence generateEnumDisplay(
        final List<Token> tokens,
        final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.applicableTypeName());
        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format("\n" +
            "    func (value %1$s) String() string {\n" +
            "        switch value {\n",
            enumName);

        for (final Token token : tokens)
        {
            new Formatter(sb).format(
                "            case %2$s_%1$s: return \"%1$s\"\n",
                token.name(),
                enumName);
        }

        sb.append("            case ").append(enumName)
            .append("_NULL_VALUE: return \"NULL_VALUE\"\n").append("        }\n\n");

        if (shouldDecodeUnknownEnumValues)
        {
            sb.append("        return \"SBE_UNKNOWN\"\n").append("    }\n\n");
        }
        else
        {
            addInclude("fmt");
            new Formatter(sb).format(
                "        panic(fmt.Sprintf(\"unknown value for enum %1$s [E103]: %%d\", value))\n" +
                "    }\n\n",
                enumName);
        }

        return sb;
    }

    private Object[] generateMessageLengthArgs(
        final List<Token> groups,
        final List<Token> varData,
        final String indent,
        final boolean inStruct)
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
                if (inStruct)
                {
                    sb.append("; ").append(indent);
                }
                else
                {
                    sb.append(",\n").append(indent);
                }
            }

            final List<Token> thisGroup = groups.subList(i, endSignal + 1);

            if (isMessageConstLength(thisGroup))
            {
                sb.append(" ").append(groupName).append("Length ").append("int");
            }
            else
            {
                sb.append(groupName).append("ItemLengths ").append("[]struct{");
                sb.append(generateMessageLengthArgs(thisGroup, indent + INDENT, true)[0]);
                sb.append("}");
            }

            count += 1;

            i = endSignal;
        }

        for (int i = 0, size = varData.size(); i < size; )
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            if (count > 0)
            {
                if (inStruct)
                {
                    sb.append("; ").append(indent);
                }
                else
                {
                    sb.append(",\n").append(indent);
                }
            }

            sb.append(" ").append(formatPropertyName(varDataToken.name())).append("Length").append(" int");

            count += 1;

            i += varDataToken.componentTokenCount();
        }

        CharSequence result = sb;
        if (count > 1)
        {
            result = "\n" + indent + result;
        }

        return new Object[] {result, count};
    }


    private Object[] generateMessageLengthArgs(final List<Token> tokens, final String indent, final boolean inStruct)
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

        return generateMessageLengthArgs(groups, varData, indent, inStruct);
    }

    private boolean isMessageConstLength(final List<Token> tokens)
    {
        final Integer count = (Integer)generateMessageLengthArgs(tokens, BASE_INDENT, false)[1];

        return count == 0;
    }

    private String generateMessageLengthCallHelper(
        final List<Token> groups,
        final List<Token> varData)
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
                sb.append(", ");
            }

            final List<Token> thisGroup = groups.subList(i, endSignal + 1);

            if (isMessageConstLength(thisGroup))
            {
                sb.append("e.").append(groupName).append("Length");
            }
            else
            {
                sb.append("e.").append(groupName).append("ItemLengths");
            }

            count += 1;

            i = endSignal;
        }

        for (int i = 0, size = varData.size(); i < size; )
        {
            final Token varDataToken = varData.get(i);
            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            if (count > 0)
            {
                sb.append(", ");
            }

            sb.append("e.").append(formatPropertyName(varDataToken.name())).append("Length");

            count += 1;

            i += varDataToken.componentTokenCount();
        }

        return sb.toString();
    }

    private CharSequence generateMessageLengthCallHelper(final List<Token> tokens)
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

        return generateMessageLengthCallHelper(groups, varData);
    }

    private CharSequence generateMessageLength(
        final List<Token> groups,
        final List<Token> varData,
        final String indent,
        final String className)
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

            final String countName = isMessageConstLength(thisGroup) ?
                formatPropertyName(groupToken.name()) + "Length" :
                "len(" + formatPropertyName(groupToken.name()) + "ItemLengths)";

            final String minCheck = minCount > 0 ? countName + " < " + minCount + " || " : "";
            final String maxCheck = countName + " > " + maxCount;

            new Formatter(sbEncode).format("\n" +
                indent + "    length += m.%1$s().SbeHeaderSize()\n",
                formatPropertyName(groupToken.name()));

            if (isMessageConstLength(thisGroup))
            {
                addInclude("fmt");
                new Formatter(sbEncode).format(
                    indent + "    if (%3$s%4$s) {\n" +
                    indent + "        panic(fmt.Sprintf(\"%5$s outside of allowed range [E110]\"))\n" +
                    indent + "    }\n" +
                    indent + "    length += uint64(%1$sLength) * m.%1$s().SbeBlockLength()\n",
                    formatPropertyName(groupToken.name()),
                    formatClassName(groupToken.name()),
                    minCheck,
                    maxCheck,
                    countName);
            }
            else
            {
                new Formatter(sbEncode).format(
                    indent + "    if (%3$s%4$s) {\n" +
                    indent + "        panic(\"%5$s outside of allowed range [E110]\")\n" +
                    indent + "    }\n\n" +
                    indent + "    for _, e := range %1$sItemLengths {\n" +
                    indent + "        l, err := m.%1$s().ComputeLength(%6$s)\n" +
                    indent + "        if err != nil {\n" +
                    indent + "            return 0, err\n" +
                    indent + "        }\n" +
                    indent + "        length += uint64(l)\n" +
                    indent + "    }\n",
                    formatPropertyName(groupToken.name()),
                    formatClassName(groupToken.name()),
                    minCheck,
                    maxCheck,
                    countName,
                    generateMessageLengthCallHelper(thisGroup));
            }

            new Formatter(sbSkip).format(
                indent + ("    for %1$sGroup := m.%1$s(); %1$sGroup.HasNext(); {\n") +
                indent + ("        %1$sGroup.Next().Skip()\n") +
                indent + ("    }\n"),
                formatPropertyName(groupToken.name()));

            i = endSignal;
        }
        for (int i = 0, size = varData.size(); i < size; )
        {
            final Token varDataToken = varData.get(i);

            if (varDataToken.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + varDataToken);
            }

            final Token lengthToken = Generators.findFirst("length", varData, i);

            addInclude("fmt");
            new Formatter(sbEncode).format("\n" +
                indent + "    length += m.%1$sHeaderLength()\n" +
                indent + "    if m.%1$sLength() > %2$d {\n" +
                indent + "        return 0, fmt.Errorf(\"%1$sLength too long for length type [E109]\")\n" +
                indent + "    }\n" +
                indent + "    length += uint64(m.%1$sLength())\n",
                formatPropertyName(varDataToken.name()),
                lengthToken.encoding().applicableMaxValue().longValue());

            new Formatter(sbSkip).format(
                indent + "    m.Skip%1$s()\n",
                toUpperFirstChar(varDataToken.name()));

            i += varDataToken.componentTokenCount();
        }

        final StringBuilder sb = new StringBuilder();

        new Formatter(sb).format("\n" +
            indent + "func (m *%2$s) Skip() {\n" +
            sbSkip +
            indent + "}\n\n" +

            indent + "func (m *%2$s) IsConstLength() bool {\n" +
            indent + "    return " + (groups.isEmpty() && varData.isEmpty()) + "\n" +
            indent + "}\n\n" +

            indent + "func (m *%2$s) ComputeLength(%1$s) (int, error) {\n" +
            indent + "    length := uint64(m.SbeBlockLength())\n" +
            sbEncode + "\n" +
            indent + "    return int(length), nil\n" +
            indent + "}\n",
            generateMessageLengthArgs(groups, varData, indent + INDENT, false)[0],
            className);

        return sb;
    }
}
