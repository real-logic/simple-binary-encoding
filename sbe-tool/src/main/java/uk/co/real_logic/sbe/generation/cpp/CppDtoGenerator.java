/*
 * Copyright 2013-2024 Real Logic Limited.
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

package uk.co.real_logic.sbe.generation.cpp;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import org.agrona.LangUtil;
import org.agrona.Verify;
import org.agrona.generation.OutputManager;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static uk.co.real_logic.sbe.generation.Generators.toLowerFirstChar;
import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;
import static uk.co.real_logic.sbe.generation.cpp.CppUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

/**
 * DTO generator for the CSharp programming language.
 */
public class CppDtoGenerator implements CodeGenerator
{
    private static final String INDENT = "    ";
    private static final String BASE_INDENT = INDENT;

    private final Ir ir;
    private final OutputManager outputManager;

    /**
     * Create a new C# DTO {@link CodeGenerator}.
     *
     * @param ir            for the messages and types.
     * @param outputManager for generating the DTOs to.
     */
    public CppDtoGenerator(final Ir ir, final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    /**
     * {@inheritDoc}
     */
    public void generate() throws IOException
    {
        generateDtosForTypes();

        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final String codecClassName = formatClassName(msgToken.name());
            final String className = formatDtoClassName(msgToken.name());

            final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
            int offset = 0;

            final ClassBuilder classBuilder = new ClassBuilder(className, BASE_INDENT);

            final List<Token> fields = new ArrayList<>();
            offset = collectFields(messageBody, offset, fields);
            generateFields(classBuilder, codecClassName, fields, BASE_INDENT + INDENT);

            final List<Token> groups = new ArrayList<>();
            offset = collectGroups(messageBody, offset, groups);
            generateGroups(classBuilder, className, codecClassName, groups,
                BASE_INDENT + INDENT);

            final List<Token> varData = new ArrayList<>();
            collectVarData(messageBody, offset, varData);
            generateVarData(classBuilder, varData, BASE_INDENT + INDENT);

            generateDecodeWith(classBuilder, className, codecClassName, fields,
                groups, varData, BASE_INDENT + INDENT);
            generateDecodeFrom(classBuilder, className, codecClassName, BASE_INDENT + INDENT);
            generateEncodeWith(classBuilder, className, codecClassName, fields, groups, varData,
                BASE_INDENT + INDENT);
            generateEncodeInto(classBuilder, className, codecClassName, BASE_INDENT + INDENT);
            generateComputeEncodedLength(classBuilder, codecClassName, groups, varData, BASE_INDENT + INDENT);
            generateDisplay(classBuilder, className, codecClassName, "dto.computeEncodedLength()",
                "wrapForEncode", null, BASE_INDENT + INDENT);

            try (Writer out = outputManager.createOutput(className))
            {
                final List<Token> beginTypeTokensInSchema = ir.types().stream()
                    .map(t -> t.get(0))
                    .collect(Collectors.toList());

                final Set<String> referencedTypes = generateTypesToIncludes(beginTypeTokensInSchema);
                referencedTypes.add(codecClassName);

                out.append(generateDtoFileHeader(
                    ir.namespaces(),
                    className,
                    referencedTypes));
                out.append(generateDocumentation(BASE_INDENT, msgToken));
                classBuilder.appendTo(out);
                out.append(CppUtil.closingBraces(ir.namespaces().length));
                out.append("#endif\n");
            }
        }
    }

    private static final class ClassBuilder
    {
        private final StringBuilder publicSb = new StringBuilder();
        private final StringBuilder privateSb = new StringBuilder();
        private final StringBuilder fieldSb = new StringBuilder();
        private final String className;
        private final String indent;

        private ClassBuilder(final String className, final String indent)
        {
            this.className = className;
            this.indent = indent;
        }

        public StringBuilder appendPublic()
        {
            return publicSb;
        }

        public StringBuilder appendPrivate()
        {
            return privateSb;
        }

        public StringBuilder appendField()
        {
            return fieldSb;
        }

        public void appendTo(final Appendable out)
        {
            try
            {
                out.append(indent).append("class ").append(className).append("\n")
                    .append(indent).append("{\n")
                    .append(indent).append("private:\n")
                    .append(privateSb)
                    .append("\n")
                    .append(indent).append("public:\n")
                    .append(publicSb)
                    .append("\n")
                    .append(indent).append("private:\n")
                    .append(fieldSb)
                    .append(indent).append("};\n");
            }
            catch (final IOException exception)
            {
                LangUtil.rethrowUnchecked(exception);
            }
        }
    }

    private void generateGroups(
        final ClassBuilder classBuilder,
        final String qualifiedParentDtoClassName,
        final String qualifiedParentCodecClassName,
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
            final String groupClassName = formatDtoClassName(groupName);
            final String qualifiedDtoClassName = qualifiedParentDtoClassName + "::" + groupClassName;

            final String fieldName = "m_" + toLowerFirstChar(groupName);
            final String formattedPropertyName = formatPropertyName(groupName);

            classBuilder.appendField().append(indent).append("std::vector<")
                .append(qualifiedDtoClassName).append("> ")
                .append(fieldName).append(";\n");

            final ClassBuilder groupClassBuilder = new ClassBuilder(groupClassName, indent);

            i++;
            i += tokens.get(i).componentTokenCount();

            final String qualifiedCodecClassName =
                qualifiedParentCodecClassName + "::" + formatClassName(groupName);

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            generateFields(groupClassBuilder, qualifiedCodecClassName, fields, indent + INDENT);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(groupClassBuilder, qualifiedDtoClassName,
                qualifiedCodecClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(groupClassBuilder, varData, indent + INDENT);

            generateDecodeListFrom(
                groupClassBuilder, groupClassName, qualifiedCodecClassName, indent + INDENT);
            generateDecodeWith(groupClassBuilder, groupClassName, qualifiedCodecClassName,
                fields, groups, varData, indent + INDENT);
            generateEncodeWith(
                groupClassBuilder, groupClassName, qualifiedCodecClassName, fields, groups, varData, indent + INDENT);
            generateComputeEncodedLength(groupClassBuilder, qualifiedCodecClassName, groups, varData,
                indent + INDENT);

            groupClassBuilder.appendTo(
                classBuilder.appendPublic().append("\n").append(generateDocumentation(indent, groupToken))
            );

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("[[nodiscard]] const std::vector<").append(qualifiedDtoClassName).append(">& ")
                .append(formattedPropertyName).append("() const\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("[[nodiscard]] std::vector<").append(qualifiedDtoClassName).append(">& ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("void ").append(formattedPropertyName).append("(")
                .append("const std::vector<").append(qualifiedDtoClassName).append(">& values)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = values;\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("void ").append(formattedPropertyName).append("(")
                .append("std::vector<").append(qualifiedDtoClassName).append(">&& values)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = std::move(values);\n")
                .append(indent).append("}\n");
        }
    }

    private void generateComputeEncodedLength(
        final ClassBuilder classBuilder,
        final String qualifiedCodecClassName,
        final List<Token> groupTokens,
        final List<Token> varDataTokens,
        final String indent)
    {
        final StringBuilder lengthBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("[[nodiscard]] std::size_t computeEncodedLength() const\n")
            .append(indent).append("{\n");

        lengthBuilder
            .append(indent).append(INDENT).append("std::size_t encodedLength = 0;\n");

        lengthBuilder.append(indent).append(INDENT).append("encodedLength += ").append(qualifiedCodecClassName)
            .append("::sbeBlockLength();\n\n");

        for (int i = 0, size = groupTokens.size(); i < size; i++)
        {
            final Token groupToken = groupTokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            i++;
            i += groupTokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(groupTokens, i, fields);
            final List<Token> subGroups = new ArrayList<>();
            i = collectGroups(groupTokens, i, subGroups);
            final List<Token> subVarData = new ArrayList<>();
            i = collectVarData(groupTokens, i, subVarData);

            final String groupName = groupToken.name();
            final String fieldName = "m_" + toLowerFirstChar(groupName);
            final String groupCodecClassName = qualifiedCodecClassName + "::" + formatClassName(groupName);

            lengthBuilder
                .append(indent).append(INDENT).append("encodedLength += ")
                .append(groupCodecClassName).append("::sbeHeaderSize();\n\n")
                .append(indent).append(INDENT).append("for (auto& group : ")
                .append(fieldName).append(")\n")
                .append(indent).append(INDENT).append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("encodedLength += group.computeEncodedLength();\n")
                .append(indent).append(INDENT).append("}\n\n");
        }

        for (int i = 0, size = varDataTokens.size(); i < size; i++)
        {
            final Token token = varDataTokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();
                final Token varDataToken = Generators.findFirst("varData", varDataTokens, i);
                final String fieldName = "m_" + toLowerFirstChar(propertyName);

                lengthBuilder.append(indent).append(INDENT).append("encodedLength += ")
                    .append(qualifiedCodecClassName).append("::")
                    .append(formatPropertyName(propertyName)).append("HeaderLength();\n");

                lengthBuilder.append(indent).append(INDENT).append("encodedLength += ")
                    .append(fieldName).append(".size() * sizeof(")
                    .append(cppTypeName(varDataToken.encoding().primitiveType())).append(");\n\n");

            }
        }

        lengthBuilder.append(indent).append(INDENT).append("return encodedLength;\n")
            .append(indent).append("}\n");
    }

    private void generateCompositeDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("static void decodeWith(").append(codecClassName).append("& codec, ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldDecodeWith(
                decodeBuilder, token, token, codecClassName, indent + INDENT);

            i += tokens.get(i).componentTokenCount();
        }

        decodeBuilder.append(indent).append("}\n");
    }

    private void generateCompositeEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("static void encodeWith(").append(codecClassName).append("& codec,")
            .append("const ").append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldEncodeWith(encodeBuilder, codecClassName, token, token, indent + INDENT);

            i += tokens.get(i).componentTokenCount();
        }

        encodeBuilder.append(indent).append("}\n");
    }

    private void generateDecodeListFrom(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final String indent)
    {
        classBuilder.appendPublic().append("\n")
            .append(indent).append("static std::vector<").append(dtoClassName).append("> decodeManyWith(")
            .append(codecClassName).append("& codec)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("std::vector<").append(dtoClassName)
            .append("> dtos(codec.count());\n")
            .append(indent).append(INDENT)
            .append("for (std::size_t i = 0; i < dtos.size(); i++)\n")
            .append(indent).append(INDENT)
            .append("{\n")
            .append(indent).append(INDENT).append(INDENT)
            .append(dtoClassName).append(" dto;\n")
            .append(indent).append(INDENT).append(INDENT)
            .append(dtoClassName).append("::decodeWith(codec.next(), dto);\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("dtos[i] = dto;\n")
            .append(indent).append(INDENT)
            .append("}\n")
            .append(indent).append(INDENT)
            .append("return dtos;\n")
            .append(indent).append("}\n");
    }

    private void generateDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("static void decodeWith(").append(codecClassName).append("& codec, ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        generateMessageFieldsDecodeWith(decodeBuilder, fields, codecClassName, indent + INDENT);
        generateGroupsDecodeWith(decodeBuilder, groups, indent + INDENT);
        generateVarDataDecodeWith(decodeBuilder, varData, indent + INDENT);
        decodeBuilder.append(indent).append("}\n");
    }

    private static void generateDecodeFrom(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final String indent)
    {
        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("static ").append(dtoClassName).append(" decodeFrom(")
            .append("char* buffer, std::uint64_t offset, ")
            .append("std::uint64_t actingBlockLength, std::uint64_t actingVersion, ")
            .append("std::uint64_t bufferLength)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(codecClassName).append(" codec;\n")
            .append(indent).append(INDENT)
            .append("codec.wrapForDecode(buffer, offset, actingBlockLength, actingVersion, bufferLength);\n")
            .append(indent).append(INDENT).append(dtoClassName).append(" dto;\n")
            .append(indent).append(INDENT).append(dtoClassName).append("::decodeWith(codec, dto);\n")
            .append(indent).append(INDENT).append("return dto;\n")
            .append(indent).append("}\n");
    }

    private void generateMessageFieldsDecodeWith(
        final StringBuilder sb,
        final List<Token> tokens,
        final String codecClassName,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);

                generateFieldDecodeWith(sb, signalToken, encodingToken, codecClassName, indent);
            }
        }
    }

    private void generateFieldDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String codecClassName,
        final String indent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveDecodeWith(sb, fieldToken, typeToken, codecClassName, indent);
                break;

            case BEGIN_SET:
                final String bitSetName = formatDtoClassName(typeToken.applicableTypeName());
                generateBitSetDecodeWith(sb, fieldToken, bitSetName, indent);
                break;

            case BEGIN_ENUM:
                generateEnumDecodeWith(sb, fieldToken, indent);
                break;

            case BEGIN_COMPOSITE:
                generateCompositePropertyDecodeWith(sb, fieldToken, typeToken, indent);
                break;

            default:
                break;
        }
    }

    private void generatePrimitiveDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String codecClassName,
        final String indent)
    {
        if (typeToken.isConstantEncoding())
        {
            return;
        }

        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            final String typeName = cppTypeName(typeToken.encoding().primitiveType());
            final String codecNullValue = codecClassName + "::" + formatPropertyName(fieldToken.name()) + "NullValue()";
            if (fieldToken.isConstantEncoding())
            {
                return;
            }

            final String propertyName = fieldToken.name();
            final String formattedPropertyName = formatPropertyName(propertyName);

            generateRecordPropertyAssignment(
                sb,
                fieldToken,
                indent,
                "codec." + formattedPropertyName + "()",
                codecNullValue,
                typeName
            );
        }
        else if (arrayLength > 1)
        {
            generateArrayDecodeWith(sb, fieldToken, typeToken, codecClassName, indent);
        }
    }

    private void generateArrayDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String codecClassName,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            generateRecordPropertyAssignment(
                sb,
                fieldToken,
                indent,
                "std::string(codec." + formattedPropertyName + "(), " +
                codecClassName + "::" + formattedPropertyName + "Length())",
                null,
                "std::string"
            );
        }
        else
        {
            final StringBuilder initializerList = new StringBuilder();
            initializerList.append("{ ");
            final int arrayLength = typeToken.arrayLength();
            for (int i = 0; i < arrayLength; i++)
            {
                initializerList.append("codec.").append(formattedPropertyName).append("(").append(i).append("),");
            }
            assert arrayLength > 0;
            initializerList.setLength(initializerList.length() - 1);
            initializerList.append(" }");

            generateRecordPropertyAssignment(
                sb,
                fieldToken,
                indent,
                initializerList,
                null,
                "std::vector<" + cppTypeName(typeToken.encoding().primitiveType()) + ">"
            );
        }
    }

    private void generateBitSetDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final String dtoTypeName,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (fieldToken.isOptionalEncoding())
        {
            sb.append(indent).append("if (codec.").append(formattedPropertyName).append("InActingVersion()");

            sb.append(")\n")
                .append(indent).append("{\n");

            sb.append(indent).append(INDENT).append(dtoTypeName).append("::decodeWith(codec.")
                .append(formattedPropertyName).append("(), ")
                .append("dto.").append(formattedPropertyName).append("());\n");

            sb.append(indent).append("}\n")
                .append(indent).append("else\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("().clear();\n")
                .append(indent).append("}\n");
        }
        else
        {
            sb.append(indent).append(dtoTypeName).append("::decodeWith(codec.")
                .append(formattedPropertyName).append("(), ")
                .append("dto.").append(formattedPropertyName).append("());\n");
        }
    }

    private void generateEnumDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
            .append("codec.").append(formattedPropertyName).append("());\n");
    }

    private void generateCompositePropertyDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String dtoClassName = formatDtoClassName(typeToken.applicableTypeName());

        sb.append(indent).append(dtoClassName).append("::decodeWith(codec.")
            .append(formattedPropertyName).append("(), ")
            .append("dto.").append(formattedPropertyName).append("());\n");
    }

    private void generateGroupsDecodeWith(
        final StringBuilder sb,
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
            final String formattedPropertyName = formatPropertyName(groupName);
            final String groupDtoClassName = formatDtoClassName(groupName);

            sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
                .append(groupDtoClassName).append("::decodeManyWith(codec.")
                .append(formattedPropertyName).append("()));\n");

            i++;
            i += tokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
        }
    }

    private void generateVarDataDecodeWith(
        final StringBuilder sb,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();
                final String formattedPropertyName = formatPropertyName(propertyName);

                final boolean isOptional = token.version() > 0;

                final String dataVar = toLowerFirstChar(propertyName) + "Data";
                final String lengthVar = toLowerFirstChar(propertyName) + "Length";
                final String blockIndent = isOptional ? indent + INDENT : indent;
                final StringBuilder codecValueExtraction = new StringBuilder()
                    .append(blockIndent).append("std::size_t ").append(lengthVar)
                    .append(" = codec.").append(formattedPropertyName).append("Length();\n")
                    .append(blockIndent).append("const char* ").append(dataVar)
                    .append(" = codec.").append(formattedPropertyName).append("();\n");

                final String dtoValue = "std::string(" + dataVar + ", " + lengthVar + ")";
                final String nullDtoValue = "\"\"";

                if (isOptional)
                {
                    sb.append(indent).append("if (codec.").append(formattedPropertyName).append("InActingVersion()");

                    sb.append(")\n")
                        .append(indent).append("{\n");

                    sb.append(codecValueExtraction);

                    sb.append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(")
                        .append(dtoValue).append(");\n");

                    sb.append(indent).append("}\n")
                        .append(indent).append("else\n")
                        .append(indent).append("{\n")
                        .append(indent).append(INDENT).append("dto.")
                        .append(formattedPropertyName).append("(").append(nullDtoValue).append(");\n")
                        .append(indent).append("}\n");
                }
                else
                {
                    sb.append(codecValueExtraction);

                    sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
                        .append(dtoValue).append(");\n");
                }
            }
        }
    }

    private void generateRecordPropertyAssignment(
        final StringBuilder sb,
        final Token token,
        final String indent,
        final CharSequence presentExpression,
        final String nullCodecValueOrNull,
        final String dtoTypeName)
    {
        final String propertyName = token.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (token.isOptionalEncoding())
        {
            sb.append(indent).append("if (codec.").append(formattedPropertyName).append("InActingVersion()");

            if (null != nullCodecValueOrNull)
            {
                sb.append(" && codec.").append(formattedPropertyName).append("() != ").append(nullCodecValueOrNull);
            }

            sb.append(")\n")
                .append(indent).append("{\n");

            sb.append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(std::make_optional<")
                .append(dtoTypeName).append(">(").append(presentExpression).append("));\n");

            sb.append(indent).append("}\n")
                .append(indent).append("else\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(std::nullopt);\n")
                .append(indent).append("}\n");
        }
        else
        {
            sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
                .append(presentExpression).append(");\n");
        }
    }

    private void generateEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("static void encodeWith(").append(codecClassName).append("& codec, const ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        generateFieldsEncodeWith(encodeBuilder, codecClassName, fields, indent + INDENT);
        generateGroupsEncodeWith(encodeBuilder, groups, indent + INDENT);
        generateVarDataEncodeWith(encodeBuilder, varData, indent + INDENT);

        encodeBuilder.append(indent).append("}\n");
    }

    private static void generateEncodeInto(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final String indent)
    {
        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("static std::size_t encodeInto(const ").append(dtoClassName).append("& dto, ")
            .append("char *buffer, std::uint64_t offset, std::uint64_t bufferLength)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(codecClassName).append(" codec;\n")
            .append(indent).append(INDENT).append("codec.wrapForEncode(buffer, offset, bufferLength);\n")
            .append(indent).append(INDENT).append(dtoClassName).append("::encodeWith(codec, dto);\n")
            .append(indent).append(INDENT).append("return codec.encodedLength();\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("static std::size_t encodeWithHeaderInto(const ")
            .append(dtoClassName).append("& dto, ")
            .append("char *buffer, std::uint64_t offset, std::uint64_t bufferLength)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(codecClassName).append(" codec;\n")
            .append(indent).append(INDENT).append("codec.wrapAndApplyHeader(buffer, offset, bufferLength);\n")
            .append(indent).append(INDENT).append(dtoClassName).append("::encodeWith(codec, dto);\n")
            .append(indent).append(INDENT).append("return codec.sbePosition() - offset;\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("[[nodiscard]] static std::vector<std::uint8_t> bytes(const ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("std::vector<std::uint8_t> bytes(dto.computeEncodedLength());\n")
            .append(indent).append(INDENT).append(dtoClassName)
            .append("::encodeInto(dto, reinterpret_cast<char *>(bytes.data()), 0, bytes.size());\n")
            .append(indent).append(INDENT).append("return bytes;\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("[[nodiscard]] static std::vector<std::uint8_t> bytesWithHeader(const ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("std::vector<std::uint8_t> bytes(dto.computeEncodedLength() + ")
            .append("MessageHeader::encodedLength());\n")
            .append(indent).append(INDENT).append(dtoClassName)
            .append("::encodeWithHeaderInto(dto, reinterpret_cast<char *>(bytes.data()), 0, bytes.size());\n")
            .append(indent).append(INDENT).append("return bytes;\n")
            .append(indent).append("}\n");
    }

    private void generateFieldsEncodeWith(
        final StringBuilder sb,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                generateFieldEncodeWith(sb, codecClassName, signalToken, encodingToken, indent);
            }
        }
    }

    private void generateFieldEncodeWith(
        final StringBuilder sb,
        final String codecClassName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveEncodeWith(sb, codecClassName, fieldToken, typeToken, indent);
                break;

            case BEGIN_ENUM:
                generateEnumEncodeWith(sb, fieldToken, indent);
                break;

            case BEGIN_SET:
            case BEGIN_COMPOSITE:
                generateComplexPropertyEncodeWith(sb, fieldToken, typeToken, indent);
                break;

            default:
                break;
        }
    }

    private void generatePrimitiveEncodeWith(
        final StringBuilder sb,
        final String codecClassName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.isConstantEncoding())
        {
            return;
        }

        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            generatePrimitiveValueEncodeWith(sb, codecClassName, fieldToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayEncodeWith(sb, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayEncodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            final String accessor = "dto." + formattedPropertyName + "()";
            final String value = fieldToken.isOptionalEncoding() ?
                accessor + ".value_or(" + "\"\"" + ")" :
                accessor;
            sb.append(indent).append("codec.put").append(toUpperFirstChar(propertyName)).append("(")
                .append(value).append(".c_str());\n");
        }
        else
        {
            final String typeName = cppTypeName(typeToken.encoding().primitiveType());
            final String vectorVar = toLowerFirstChar(propertyName) + "Vector";

            final String accessor = "dto." + formattedPropertyName + "()";
            final String value = fieldToken.isOptionalEncoding() ?
                accessor + ".value_or(std::vector<" + typeName + ">())" :
                accessor;

            sb.append(indent).append("std::vector<").append(typeName).append("> ").append(vectorVar)
                .append(" = ").append(value).append(";\n\n");

            sb.append(indent).append("if (").append(vectorVar).append(".size() != ")
                .append(typeToken.arrayLength()).append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("throw std::invalid_argument(\"")
                .append(propertyName)
                .append(": array length != ")
                .append(typeToken.arrayLength())
                .append("\");\n")
                .append(indent).append("}\n\n");

            sb.append(indent).append("for (std::uint64_t i = 0; i < ").append(typeToken.arrayLength())
                .append("; i++)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("codec.").append(formattedPropertyName).append("(i, ")
                .append(vectorVar).append("[i]);\n")
                .append(indent).append("}\n");
        }
    }

    private void generatePrimitiveValueEncodeWith(
        final StringBuilder sb,
        final String codecClassName,
        final Token fieldToken,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        final String nullValue = codecClassName + "::" + formattedPropertyName + "NullValue()";
        final String accessor = "dto." + formattedPropertyName + "()";
        final String value = fieldToken.isOptionalEncoding() ?
            accessor + ".value_or(" + nullValue + ")" :
            accessor;

        sb.append(indent).append("codec.").append(formattedPropertyName).append("(")
            .append(value).append(");\n");
    }

    private void generateEnumEncodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final String indent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(indent).append("codec.").append(formattedPropertyName).append("(dto.")
            .append(formattedPropertyName).append("());\n");
    }

    private void generateComplexPropertyEncodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String typeName = formatDtoClassName(typeToken.applicableTypeName());

        sb.append(indent).append(typeName).append("::encodeWith(codec.")
            .append(formattedPropertyName).append("(), dto.")
            .append(formattedPropertyName).append("());\n");
    }

    private void generateGroupsEncodeWith(
        final StringBuilder sb,
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
            final String formattedPropertyName = formatPropertyName(groupName);
            final String groupCodecVarName = groupName + "Codec";
            final String groupDtoTypeName = formatDtoClassName(groupName);

            sb.append("\n")
                .append(indent).append("const std::vector<").append(groupDtoTypeName).append(">& ")
                .append(formattedPropertyName).append(" = dto.").append(formattedPropertyName).append("();\n\n")
                .append(indent).append("auto&").append(" ").append(groupCodecVarName)
                .append(" = codec.").append(formattedPropertyName)
                .append("Count(").append(formattedPropertyName).append(".size());\n\n")
                .append(indent).append("for (const auto& group: ").append(formattedPropertyName).append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(groupDtoTypeName)
                .append("::encodeWith(").append(groupCodecVarName).append(".next(), group);\n")
                .append(indent).append("}\n\n");

            i++;
            i += tokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
        }
    }

    private void generateVarDataEncodeWith(
        final StringBuilder sb,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();
                final String formattedPropertyName = formatPropertyName(propertyName);
                final String varName = toLowerFirstChar(propertyName) + "Vector";

                sb.append(indent).append("auto& ").append(varName).append(" = dto.")
                    .append(formattedPropertyName).append("();\n")
                    .append(indent).append("codec.put").append(toUpperFirstChar(propertyName))
                    .append("(").append(varName).append(");\n");
            }
        }
    }

    private void generateDisplay(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final String lengthExpression,
        final String wrapMethod,
        final String actingVersion,
        final String indent)
    {
        final StringBuilder streamBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("friend std::ostream& operator << (std::ostream& stream, const ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(codecClassName).append(" codec;\n")
            .append(indent).append(INDENT).append("const std::size_t length = ")
            .append(lengthExpression).append(";\n")
            .append(indent).append(INDENT).append("std::vector<char> buffer(length);\n")
            .append(indent).append(INDENT).append("codec.").append(wrapMethod)
            .append("(buffer.data(), 0");

        if (null != actingVersion)
        {
            streamBuilder.append(", ").append(actingVersion);
        }

        streamBuilder.append(", ").append("length);\n");

        streamBuilder.append(indent).append(INDENT).append("encodeWith(codec, dto);\n")
            .append(indent).append(INDENT).append("stream << codec;\n")
            .append(indent).append(INDENT).append("return stream;\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("[[nodiscard]] std::string string() const\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("std::ostringstream stream;\n")
            .append(indent).append(INDENT).append("stream << *this;\n")
            .append(indent).append(INDENT).append("return stream.str();\n")
            .append(indent).append("}\n");
    }

    private void generateFields(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = signalToken.name();

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        generatePrimitiveProperty(
                            classBuilder, codecClassName, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_ENUM:
                        generateEnumProperty(classBuilder, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_SET:
                    case BEGIN_COMPOSITE:
                        generateComplexProperty(classBuilder, propertyName, signalToken, encodingToken, indent);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private void generateComplexProperty(
        final ClassBuilder classBuilder,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String typeName = formatDtoClassName(typeToken.applicableTypeName());
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String fieldName = "m_" + toLowerFirstChar(propertyName);

        classBuilder.appendField()
            .append(indent).append(typeName).append(" ").append(fieldName).append(";\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("[[nodiscard]] const ").append(typeName).append("& ")
            .append(formattedPropertyName).append("() const\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("[[nodiscard]] ").append(typeName).append("& ")
            .append(formattedPropertyName).append("()\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
            .append(indent).append("}\n");
    }

    private void generateEnumProperty(
        final ClassBuilder classBuilder,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName()) + "::Value";

        final String formattedPropertyName = formatPropertyName(propertyName);

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();
            final String caseName = constValue.substring(constValue.indexOf(".") + 1);

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("[[nodiscard]] static ").append(enumName).append(" ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(enumName).append("::")
                .append(caseName).append(";\n")
                .append(indent).append("}\n");
        }
        else
        {
            final String fieldName = "m_" + toLowerFirstChar(propertyName);

            classBuilder.appendField()
                .append(indent).append(enumName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("[[nodiscard]] ").append(enumName).append(" ")
                .append(formattedPropertyName).append("() const\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName)
                .append("(").append(enumName).append(" value)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                .append(indent).append("}\n");
        }
    }

    private void generatePrimitiveProperty(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.isConstantEncoding())
        {
            generateConstPropertyMethods(classBuilder, propertyName, fieldToken, typeToken, indent);
        }
        else
        {
            generatePrimitivePropertyMethods(classBuilder, codecClassName, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generatePrimitivePropertyMethods(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            generateSingleValueProperty(classBuilder, codecClassName, propertyName, fieldToken, typeToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayProperty(classBuilder, codecClassName, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayProperty(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String fieldName = "m_" + toLowerFirstChar(propertyName);
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String validateMethod = "validate" + toUpperFirstChar(propertyName);

        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            final CharSequence typeName = typeWithFieldOptionality(
                fieldToken,
                "std::string"
            );

            classBuilder.appendField()
                .append(indent).append(typeName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("[[nodiscard]] const ").append(typeName).append("& ")
                .append(formattedPropertyName).append("() const\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName)
                .append("(const ").append(typeName).append("& borrowedValue)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = borrowedValue;\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName).append("(")
                .append(typeName).append("&& ownedValue)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = std::move(ownedValue);\n")
                .append(indent).append("}\n");

            generateArrayValidateMethod(
                classBuilder,
                codecClassName,
                fieldToken,
                indent,
                validateMethod,
                typeName,
                "std::string",
                formattedPropertyName);
        }
        else
        {
            final String elementTypeName = cppTypeName(typeToken.encoding().primitiveType());
            final String vectorTypeName = "std::vector<" + elementTypeName + ">";
            final CharSequence typeName = typeWithFieldOptionality(
                fieldToken,
                vectorTypeName
            );

            classBuilder.appendField()
                .append(indent).append(typeName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("[[nodiscard]] ").append(typeName).append(" ")
                .append(formattedPropertyName).append("() const\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName).append("(")
                .append(typeName).append("& borrowedValue").append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(validateMethod).append("(borrowedValue);\n")
                .append(indent).append(INDENT).append(fieldName).append(" = borrowedValue;\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName).append("(")
                .append(typeName).append("&& ownedValue").append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(validateMethod).append("(ownedValue);\n")
                .append(indent).append(INDENT).append(fieldName).append(" = std::move(ownedValue);\n")
                .append(indent).append("}\n");

            generateArrayValidateMethod(
                classBuilder,
                codecClassName,
                fieldToken,
                indent,
                validateMethod,
                typeName,
                vectorTypeName,
                formattedPropertyName);
        }
    }

    private static void generateArrayValidateMethod(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final Token fieldToken,
        final String indent,
        final String validateMethod,
        final CharSequence typeName,
        final String vectorTypeName,
        final String formattedPropertyName)
    {
        final StringBuilder validateBuilder = classBuilder.appendPrivate().append("\n")
            .append(indent).append("static void ").append(validateMethod).append("(")
            .append(typeName).append(" value)\n")
            .append(indent).append("{\n");

        String value = "value";

        if (fieldToken.isOptionalEncoding())
        {
            validateBuilder.append(indent).append(INDENT)
                .append("if (!value.has_value())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("return;\n")
                .append(indent).append(INDENT)
                .append("}\n");

            validateBuilder.append(indent).append(INDENT)
                .append(vectorTypeName).append(" actualValue = value.value();\n");

            value = "actualValue";
        }

        validateBuilder.append(indent).append(INDENT)
            .append("if (").append(value).append(".size() > ").append(codecClassName).append("::")
            .append(formattedPropertyName).append("Length())\n")
            .append(indent).append(INDENT)
            .append("{\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("throw std::invalid_argument(\"")
            .append(formattedPropertyName)
            .append(": too many elements: \" + std::to_string(")
            .append(value).append(".size()));\n")
            .append(indent).append(INDENT)
            .append("}\n")
            .append(indent).append("}\n");
    }

    private void generateSingleValueProperty(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final Encoding encoding = typeToken.encoding();
        final String elementTypeName = cppTypeName(encoding.primitiveType());
        final CharSequence typeName = typeWithFieldOptionality(
            fieldToken,
            elementTypeName
        );
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String fieldName = "m_" + toLowerFirstChar(propertyName);
        final String validateMethod = "validate" + toUpperFirstChar(propertyName);

        classBuilder.appendField()
            .append(indent).append(typeName).append(" ").append(fieldName).append(";\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("[[nodiscard]] ").append(typeName).append(" ")
            .append(formattedPropertyName).append("() const\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("void ").append(formattedPropertyName).append("(")
            .append(typeName).append(" value)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(validateMethod).append("(value);\n")
            .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
            .append(indent).append("}\n");

        generateSingleValuePropertyValidateMethod(
            classBuilder,
            codecClassName,
            propertyName,
            fieldToken,
            indent,
            validateMethod,
            typeName,
            formattedPropertyName,
            elementTypeName,
            encoding);
    }

    private static void generateSingleValuePropertyValidateMethod(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final String propertyName,
        final Token fieldToken,
        final String indent,
        final String validateMethod,
        final CharSequence typeName,
        final String formattedPropertyName,
        final String elementTypeName,
        final Encoding encoding)
    {
        final StringBuilder validateBuilder = classBuilder.appendPrivate().append("\n")
            .append(indent).append("static void ").append(validateMethod).append("(")
            .append(typeName).append(" value)\n")
            .append(indent).append("{\n");

        String value = "value";

        final boolean mustPreventLesser = !encoding.applicableMinValue().equals(encoding.primitiveType().minValue());
        final boolean mustPreventGreater = !encoding.applicableMaxValue().equals(encoding.primitiveType().maxValue());

        if (fieldToken.isOptionalEncoding())
        {
            validateBuilder.append(indent).append(INDENT)
                .append("if (!value.has_value())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("return;\n")
                .append(indent).append(INDENT)
                .append("}\n");

            validateBuilder.append(indent).append(INDENT)
                .append("if (value.value() == ").append(codecClassName).append("::")
                .append(formattedPropertyName).append("NullValue())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("throw std::invalid_argument(\"")
                .append(propertyName)
                .append(": null value is reserved: \" + std::to_string(value.value()));\n")
                .append(indent).append(INDENT)
                .append("}\n");

            if (mustPreventLesser || mustPreventGreater)
            {
                validateBuilder.append(indent).append(INDENT)
                    .append(elementTypeName).append(" actualValue = value.value();\n");

                value = "actualValue";
            }
        }

        if (mustPreventLesser)
        {
            validateBuilder.append(indent).append(INDENT)
                .append("if (").append(value).append(" < ")
                .append(codecClassName).append("::").append(formattedPropertyName).append("MinValue())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("throw std::invalid_argument(\"")
                .append(propertyName)
                .append(": value is less than allowed minimum: \" + std::to_string(")
                .append(value).append("));\n")
                .append(indent).append(INDENT)
                .append("}\n");
        }

        if (mustPreventGreater)
        {
            validateBuilder.append(indent).append(INDENT)
                .append("if (").append(value).append(" > ")
                .append(codecClassName).append("::").append(formattedPropertyName).append("MaxValue())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("throw std::invalid_argument(\"")
                .append(propertyName)
                .append(": value is greater than allowed maximum: \" + std::to_string(")
                .append(value).append("));\n")
                .append(indent).append(INDENT)
                .append("}\n");
        }

        validateBuilder.append(indent).append("}\n");
    }

    private void generateConstPropertyMethods(
        final ClassBuilder classBuilder,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("static std::string ").append(toLowerFirstChar(propertyName)).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT)
                .append("return \"").append(typeToken.encoding().constValue().toString()).append("\";\n")
                .append(indent).append("}\n");
        }
        else
        {
            final CharSequence literalValue =
                generateLiteral(typeToken.encoding().primitiveType(), typeToken.encoding().constValue().toString());

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("[[nodiscard]] static ")
                .append(cppTypeName(typeToken.encoding().primitiveType()))
                .append(" ").append(formatPropertyName(propertyName)).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(literalValue).append(";\n")
                .append(indent).append("}\n");
        }
    }

    private void generateVarData(
        final ClassBuilder classBuilder,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();
                final String dtoType = "std::string";

                final String fieldName = "m_" + toLowerFirstChar(propertyName);
                final String formattedPropertyName = formatPropertyName(propertyName);

                classBuilder.appendField()
                    .append(indent).append(dtoType).append(" ").append(fieldName).append(";\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("[[nodiscard]] const ").append(dtoType).append("& ")
                    .append(formattedPropertyName).append("() const\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("[[nodiscard]] ").append(dtoType).append("& ")
                    .append(formattedPropertyName).append("()\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("void ").append(formattedPropertyName)
                    .append("(const ").append(dtoType).append("& borrowedValue)\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append(fieldName).append(" = borrowedValue;\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("void ").append(formattedPropertyName)
                    .append("(").append(dtoType).append("&& ownedValue)\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append(fieldName).append(" = std::move(ownedValue);\n")
                    .append(indent).append("}\n");
            }
        }
    }

    private static String formatDtoClassName(final String name)
    {
        return formatClassName(name + "Dto");
    }

    private void generateDtosForTypes() throws IOException
    {
        for (final List<Token> tokens : ir.types())
        {
            switch (tokens.get(0).signal())
            {
                case BEGIN_COMPOSITE:
                    generateComposite(tokens);
                    break;

                case BEGIN_SET:
                    generateChoiceSet(tokens);
                    break;

                default:
                    break;
            }
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String name = tokens.get(0).applicableTypeName();
        final String className = formatDtoClassName(name);
        final String codecClassName = formatClassName(name);

        try (Writer out = outputManager.createOutput(className))
        {
            final List<Token> compositeTokens = tokens.subList(1, tokens.size() - 1);
            final Set<String> referencedTypes = generateTypesToIncludes(compositeTokens);
            referencedTypes.add(codecClassName);
            out.append(generateDtoFileHeader(ir.namespaces(), className, referencedTypes));
            out.append(generateDocumentation(BASE_INDENT, tokens.get(0)));

            final ClassBuilder classBuilder = new ClassBuilder(className, BASE_INDENT);

            generateCompositePropertyElements(classBuilder, codecClassName, compositeTokens, BASE_INDENT + INDENT);
            generateCompositeDecodeWith(classBuilder, className, codecClassName, compositeTokens,
                BASE_INDENT + INDENT);
            generateCompositeEncodeWith(classBuilder, className, codecClassName, compositeTokens, BASE_INDENT + INDENT);
            generateDisplay(classBuilder, className, codecClassName, codecClassName + "::encodedLength()", "wrap",
                codecClassName + "::sbeSchemaVersion()", BASE_INDENT + INDENT);

            classBuilder.appendTo(out);
            out.append(CppUtil.closingBraces(ir.namespaces().length));
            out.append("#endif\n");
        }
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String name = tokens.get(0).applicableTypeName();
        final String className = formatDtoClassName(name);
        final String codecClassName = formatClassName(name);

        try (Writer out = outputManager.createOutput(className))
        {
            final List<Token> setTokens = tokens.subList(1, tokens.size() - 1);
            final Set<String> referencedTypes = generateTypesToIncludes(setTokens);
            referencedTypes.add(codecClassName);
            out.append(generateDtoFileHeader(ir.namespaces(), className, referencedTypes));
            out.append(generateDocumentation(BASE_INDENT, tokens.get(0)));

            final ClassBuilder classBuilder = new ClassBuilder(className, BASE_INDENT);

            generateChoices(classBuilder, className, setTokens, BASE_INDENT + INDENT);
            generateChoiceSetDecodeWith(classBuilder, className, codecClassName, setTokens, BASE_INDENT + INDENT);
            generateChoiceSetEncodeWith(classBuilder, className, codecClassName, setTokens, BASE_INDENT + INDENT);

            classBuilder.appendTo(out);
            out.append(CppUtil.closingBraces(ir.namespaces().length));
            out.append("#endif\n");
        }
    }

    private void generateChoiceSetEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> setTokens,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("static void encodeWith(\n")
            .append(indent).append(INDENT).append(codecClassName).append("& codec, ")
            .append("const ").append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        encodeBuilder.append(indent).append(INDENT).append("codec.clear();\n");

        for (final Token token : setTokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String formattedPropertyName = formatPropertyName(token.name());
                encodeBuilder.append(indent).append(INDENT).append("codec.").append(formattedPropertyName)
                    .append("(dto.").append(formattedPropertyName).append("());\n");
            }
        }

        encodeBuilder.append(indent).append("}\n");
    }

    private void generateChoiceSetDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> setTokens,
        final String indent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("static void decodeWith(\n")
            .append(indent).append(INDENT).append("const ").append(codecClassName).append("& codec, ")
            .append(dtoClassName).append("& dto)\n")
            .append(indent).append("{\n");

        for (final Token token : setTokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String formattedPropertyName = formatPropertyName(token.name());
                decodeBuilder.append(indent).append(INDENT).append("dto.").append(formattedPropertyName)
                    .append("(codec.").append(formattedPropertyName).append("());\n");
            }
        }

        decodeBuilder.append(indent).append("}\n");
    }

    private void generateChoices(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final List<Token> setTokens,
        final String indent)
    {
        final List<String> fields = new ArrayList<>();

        for (final Token token : setTokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String fieldName = "m_" + toLowerFirstChar(token.name());
                final String formattedPropertyName = formatPropertyName(token.name());

                fields.add(fieldName);

                classBuilder.appendField()
                    .append(indent).append("bool ").append(fieldName).append(";\n");

                classBuilder.appendPublic()
                    .append("\n")
                    .append(indent).append("[[nodiscard]] bool ").append(formattedPropertyName).append("() const\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic()
                    .append("\n")
                    .append(indent).append(dtoClassName).append("& ")
                    .append(formattedPropertyName).append("(bool value)\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                    .append(indent).append(INDENT).append("return *this;\n")
                    .append(indent).append("}\n");
            }
        }

        final StringBuilder clearBuilder = classBuilder.appendPublic()
            .append(indent).append(dtoClassName).append("& clear()\n")
            .append(indent).append("{\n");

        for (final String field : fields)
        {
            clearBuilder.append(indent).append(INDENT).append(field).append(" = false;\n");
        }

        clearBuilder.append(indent).append(INDENT).append("return *this;\n")
            .append(indent).append("}\n");
    }

    private void generateCompositePropertyElements(
        final ClassBuilder classBuilder,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            switch (token.signal())
            {
                case ENCODING:
                    generatePrimitiveProperty(classBuilder, codecClassName, propertyName, token, token, indent);
                    break;

                case BEGIN_ENUM:
                    generateEnumProperty(classBuilder, propertyName, token, token, indent);
                    break;

                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    generateComplexProperty(classBuilder, propertyName, token, token, indent);
                    break;

                default:
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }
    }

    private static Set<String> generateTypesToIncludes(final List<Token> tokens)
    {
        final Set<String> typesToInclude = new HashSet<>();

        for (final Token token : tokens)
        {
            switch (token.signal())
            {
                case BEGIN_ENUM:
                    typesToInclude.add(formatClassName(token.applicableTypeName()));
                    break;

                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    typesToInclude.add(formatDtoClassName(token.applicableTypeName()));
                    break;

                default:
                    break;
            }
        }

        return typesToInclude;
    }

    private static CharSequence typeWithFieldOptionality(
        final Token fieldToken,
        final String typeName)
    {
        if (fieldToken.isOptionalEncoding())
        {
            return "std::optional<" + typeName + ">";
        }
        else
        {
            return typeName;
        }
    }

    private static CharSequence generateDtoFileHeader(
        final CharSequence[] namespaces,
        final String className,
        final Collection<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message DTO */\n");

        sb.append(String.format(
            "#ifndef _%1$s_%2$s_CXX_H_\n" +
            "#define _%1$s_%2$s_CXX_H_\n\n",
            String.join("_", namespaces).toUpperCase(),
            className.toUpperCase()));

        sb.append("#if (defined(_MSVC_LANG) && _MSVC_LANG < 201703L) || ")
            .append("(!defined(_MSVC_LANG) && defined(__cplusplus) && __cplusplus < 201703L)\n")
            .append("#error DTO code requires at least C++17.\n")
            .append("#endif\n\n");

        sb.append("#include <cstdint>\n")
            .append("#include <limits>\n")
            .append("#include <cstring>\n")
            .append("#include <iomanip>\n")
            .append("#include <ostream>\n")
            .append("#include <stdexcept>\n")
            .append("#include <sstream>\n")
            .append("#include <string>\n")
            .append("#include <vector>\n")
            .append("#include <tuple>\n")
            .append("#include <optional>\n");

        if (typesToInclude != null && !typesToInclude.isEmpty())
        {
            sb.append("\n");
            for (final String incName : typesToInclude)
            {
                sb.append("#include \"").append(incName).append(".h\"\n");
            }
        }

        sb.append("\nnamespace ");
        sb.append(String.join(" {\nnamespace ", namespaces));
        sb.append(" {\n\n");

        return sb;
    }

    private static String generateDocumentation(final String indent, final Token token)
    {
        final String description = token.description();
        if (null == description || description.isEmpty())
        {
            return "";
        }

        return
            indent + "/**\n" +
                indent + " * " + description + "\n" +
                indent + " */\n";
    }
}
