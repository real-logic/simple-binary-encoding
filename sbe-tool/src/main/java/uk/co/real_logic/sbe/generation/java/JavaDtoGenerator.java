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
package uk.co.real_logic.sbe.generation.java;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import org.agrona.LangUtil;
import org.agrona.Verify;
import org.agrona.generation.OutputManager;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static uk.co.real_logic.sbe.generation.Generators.toLowerFirstChar;
import static uk.co.real_logic.sbe.generation.Generators.toUpperFirstChar;
import static uk.co.real_logic.sbe.generation.java.JavaUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

/**
 * DTO generator for the Java programming language.
 */
public class JavaDtoGenerator implements CodeGenerator
{
    private static final Predicate<Token> ALWAYS_FALSE_PREDICATE = ignored -> false;
    private static final String INDENT = "    ";
    private static final String BASE_INDENT = "";

    private final Ir ir;
    private final OutputManager outputManager;

    /**
     * Create a new C# DTO {@link CodeGenerator}.
     *
     * @param ir            for the messages and types.
     * @param outputManager for generating the DTOs to.
     */
    public JavaDtoGenerator(final Ir ir, final OutputManager outputManager)
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
            final String encoderClassName = encoderName(msgToken.name());
            final String decoderClassName = decoderName(msgToken.name());
            final String dtoClassName = formatDtoClassName(msgToken.name());

            final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
            int offset = 0;

            final ClassBuilder classBuilder = new ClassBuilder(dtoClassName, BASE_INDENT, "public final");

            final List<Token> fields = new ArrayList<>();
            offset = collectFields(messageBody, offset, fields);
            generateFields(classBuilder, decoderClassName, fields, BASE_INDENT + INDENT);

            final List<Token> groups = new ArrayList<>();
            offset = collectGroups(messageBody, offset, groups);
            generateGroups(classBuilder, dtoClassName, encoderClassName, decoderClassName, groups,
                BASE_INDENT + INDENT);

            final List<Token> varData = new ArrayList<>();
            collectVarData(messageBody, offset, varData);
            generateVarData(classBuilder, varData, BASE_INDENT + INDENT);

            generateDecodeWith(classBuilder, dtoClassName, decoderClassName, fields,
                groups, varData, BASE_INDENT + INDENT, fieldToken -> fieldToken.version() > msgToken.version());
            generateDecodeFrom(classBuilder, dtoClassName, decoderClassName, BASE_INDENT + INDENT);
            generateEncodeWith(classBuilder, dtoClassName, encoderClassName, fields, groups, varData,
                BASE_INDENT + INDENT);
            generateEncodeWithOverloads(classBuilder, dtoClassName, encoderClassName, BASE_INDENT + INDENT);
            generateComputeEncodedLength(classBuilder, decoderClassName,
                decoderClassName + ".BLOCK_LENGTH",
                groups, varData, BASE_INDENT + INDENT);
            generateDisplay(classBuilder, encoderClassName, "computeEncodedLength()",
                BASE_INDENT + INDENT);

            try (Writer out = outputManager.createOutput(dtoClassName))
            {
                out.append(generateDtoFileHeader(ir.applicableNamespace()));
                out.append("import org.agrona.DirectBuffer;\n");
                out.append("import org.agrona.MutableDirectBuffer;\n");
                out.append("import org.agrona.concurrent.UnsafeBuffer;\n\n");
                out.append("import java.util.ArrayList;\n");
                out.append("import java.util.List;\n\n");
                out.append(generateDocumentation(BASE_INDENT, msgToken));
                classBuilder.appendTo(out);
            }
        }
    }

    private static final class ClassBuilder
    {
        private final StringBuilder fieldSb = new StringBuilder();
        private final StringBuilder privateSb = new StringBuilder();
        private final StringBuilder publicSb = new StringBuilder();
        private final String className;
        private final String indent;
        private final String modifiers;

        private ClassBuilder(
            final String className,
            final String indent,
            final String modifiers)
        {
            this.className = className;
            this.indent = indent;
            this.modifiers = modifiers.length() == 0 ? modifiers : modifiers + " ";
        }

        public StringBuilder appendField()
        {
            return fieldSb;
        }

        public StringBuilder appendPrivate()
        {
            return privateSb;
        }

        public StringBuilder appendPublic()
        {
            return publicSb;
        }

        public void appendTo(final Appendable out)
        {
            try
            {
                out.append(indent).append(modifiers).append("class ").append(className).append("\n")
                    .append(indent).append("{\n")
                    .append(fieldSb)
                    .append(privateSb)
                    .append(publicSb)
                    .append(indent).append("}\n");
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
        final String qualifiedParentEncoderClassName,
        final String qualifiedParentDecoderClassName,
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
            final String qualifiedDtoClassName = qualifiedParentDtoClassName + "." + groupClassName;

            final Token dimToken = tokens.get(i + 1);
            if (dimToken.signal() != Signal.BEGIN_COMPOSITE)
            {
                throw new IllegalStateException("groups must start with BEGIN_COMPOSITE: token=" + dimToken);
            }
            final int sinceVersion = dimToken.version();

            final String fieldName = formatFieldName(groupName);
            final String formattedPropertyName = formatPropertyName(groupName);

            classBuilder.appendField().append(indent).append("private List<")
                .append(qualifiedDtoClassName).append("> ")
                .append(fieldName).append(" = new ArrayList<>();\n");

            final ClassBuilder groupClassBuilder = new ClassBuilder(groupClassName, indent, "public static final");

            i++;
            i += tokens.get(i).componentTokenCount();

            final String qualifiedEncoderClassName =
                qualifiedParentEncoderClassName + "." + encoderName(groupName);
            final String qualifiedDecoderClassName =
                qualifiedParentDecoderClassName + "." + decoderName(groupName);

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            generateFields(groupClassBuilder, qualifiedDecoderClassName, fields, indent + INDENT);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(groupClassBuilder, qualifiedDtoClassName,
                qualifiedEncoderClassName, qualifiedDecoderClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(groupClassBuilder, varData, indent + INDENT);

            final Predicate<Token> wasAddedAfterGroup = token ->
            {
                final boolean addedAfterParent = token.version() > sinceVersion;

                if (addedAfterParent && token.signal() == Signal.BEGIN_VAR_DATA)
                {
                    throw new IllegalStateException("Cannot extend var data inside a group.");
                }

                return addedAfterParent;
            };

            generateDecodeListWith(
                groupClassBuilder, groupClassName, qualifiedDecoderClassName, indent + INDENT);
            generateDecodeWith(groupClassBuilder, groupClassName, qualifiedDecoderClassName,
                fields, groups, varData, indent + INDENT, wasAddedAfterGroup);
            generateEncodeWith(
                groupClassBuilder, groupClassName, qualifiedEncoderClassName, fields, groups, varData, indent + INDENT);
            generateComputeEncodedLength(groupClassBuilder, qualifiedDecoderClassName,
                qualifiedDecoderClassName + ".sbeBlockLength()",
                groups, varData, indent + INDENT);

            groupClassBuilder.appendTo(
                classBuilder.appendPublic().append("\n").append(generateDocumentation(indent, groupToken))
            );

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("public List<").append(qualifiedDtoClassName).append("> ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("public void ").append(formattedPropertyName).append("(")
                .append("List<").append(qualifiedDtoClassName).append("> value)")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                .append(indent).append("}\n");
        }
    }

    private void generateComputeEncodedLength(
        final ClassBuilder classBuilder,
        final String qualifiedDecoderClassName,
        final String blockLengthExpression,
        final List<Token> groupTokens,
        final List<Token> varDataTokens,
        final String indent)
    {
        final StringBuilder lengthBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public int computeEncodedLength()\n")
            .append(indent).append("{\n");

        lengthBuilder
            .append(indent).append(INDENT).append("int encodedLength = 0;\n");

        lengthBuilder.append(indent).append(INDENT).append("encodedLength += ").append(blockLengthExpression)
            .append(";\n\n");

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
            final String fieldName = formatFieldName(groupName);
            final String groupDecoderClassName = qualifiedDecoderClassName + "." + decoderName(groupName);
            final String groupDtoClassName = formatDtoClassName(groupName);

            lengthBuilder
                .append(indent).append(INDENT).append("encodedLength += ")
                .append(groupDecoderClassName).append(".sbeHeaderSize();\n\n")
                .append(indent).append(INDENT).append("for (").append(groupDtoClassName).append(" group : ")
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
                final String fieldName = formatFieldName(propertyName);

                lengthBuilder.append(indent).append(INDENT).append("encodedLength += ")
                    .append(qualifiedDecoderClassName).append(".")
                    .append(formatPropertyName(propertyName)).append("HeaderLength();\n");

                final String characterEncoding = varDataToken.encoding().characterEncoding();
                final String lengthAccessor = characterEncoding == null ? ".length" : ".length()";
                lengthBuilder.append(indent).append(INDENT).append("encodedLength += ")
                    .append(fieldName).append(lengthAccessor);

                final int elementByteLength = varDataToken.encoding().primitiveType().size();
                if (elementByteLength != 1)
                {
                    lengthBuilder.append(" * ").append(elementByteLength);
                }

                lengthBuilder.append(";\n\n");
            }
        }

        lengthBuilder.append(indent).append(INDENT).append("return encodedLength;\n")
            .append(indent).append("}\n");
    }

    private void generateCompositeDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String decoderClassName,
        final List<Token> tokens,
        final String indent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("public static void decodeWith(").append(decoderClassName).append(" decoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldDecodeWith(
                decodeBuilder, token, token, decoderClassName, indent + INDENT, ALWAYS_FALSE_PREDICATE);

            i += tokens.get(i).componentTokenCount();
        }

        decodeBuilder.append(indent).append("}\n");
    }

    private void generateCompositeEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String encoderClassName,
        final List<Token> tokens,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("public static void encodeWith(").append(encoderClassName).append(" encoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldEncodeWith(encodeBuilder, token, token, indent + INDENT);

            i += tokens.get(i).componentTokenCount();
        }

        encodeBuilder.append(indent).append("}\n");
    }

    private void generateDecodeListWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String decoderClassName,
        final String indent)
    {
        classBuilder.appendPublic().append("\n")
            .append(indent).append("public static List<").append(dtoClassName).append("> decodeManyWith(")
            .append(decoderClassName).append(" decoder)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("List<").append(dtoClassName)
            .append("> dtos = new ArrayList<>(decoder.count());\n")
            .append(indent).append(INDENT)
            .append("while (decoder.hasNext())\n")
            .append(indent).append(INDENT)
            .append("{\n")
            .append(indent).append(INDENT).append(INDENT)
            .append(dtoClassName).append(" dto = new ").append(dtoClassName).append("();\n")
            .append(indent).append(INDENT).append(INDENT)
            .append(dtoClassName).append(".decodeWith(decoder.next(), dto);\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("dtos.add(dto);\n")
            .append(indent).append(INDENT)
            .append("}\n")
            .append(indent).append(INDENT)
            .append("return dtos;\n")
            .append(indent).append("}\n");
    }

    private void generateDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String decoderClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("public static void decodeWith(").append(decoderClassName).append(" decoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        generateMessageFieldsDecodeWith(decodeBuilder, fields, decoderClassName, indent + INDENT, wasAddedAfterParent);
        generateGroupsDecodeWith(decodeBuilder, groups, indent + INDENT);
        generateVarDataDecodeWith(decodeBuilder, decoderClassName, varData, indent + INDENT, wasAddedAfterParent);
        decodeBuilder.append(indent).append("}\n");
    }

    private static void generateDecodeFrom(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String decoderClassName,
        final String indent)
    {
        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static ").append(dtoClassName).append(" decodeFrom(")
            .append("DirectBuffer buffer, int offset, ")
            .append("short actingBlockLength, short actingVersion)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(decoderClassName).append(" decoder = new ")
            .append(decoderClassName).append("();\n")
            .append(indent).append(INDENT)
            .append("decoder.wrap(buffer, offset, actingBlockLength, actingVersion);\n")
            .append(indent).append(INDENT).append(dtoClassName).append(" dto = new ")
            .append(dtoClassName).append("();\n")
            .append(indent).append(INDENT).append("decodeWith(decoder, dto);\n")
            .append(indent).append(INDENT).append("return dto;\n")
            .append(indent).append("}\n");
    }

    private void generateMessageFieldsDecodeWith(
        final StringBuilder sb,
        final List<Token> tokens,
        final String decoderClassName,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);

                generateFieldDecodeWith(sb, signalToken, encodingToken, decoderClassName, indent, wasAddedAfterParent);
            }
        }
    }

    private void generateFieldDecodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String decoderClassName,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveDecodeWith(sb, fieldToken, typeToken, decoderClassName, indent, wasAddedAfterParent);
                break;

            case BEGIN_SET:
                final String bitSetName = formatDtoClassName(typeToken.applicableTypeName());
                generateBitSetDecodeWith(sb, decoderClassName, fieldToken, bitSetName, indent, wasAddedAfterParent);
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
        final String decoderClassName,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        if (typeToken.isConstantEncoding())
        {
            return;
        }

        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            final String decoderNullValue =
                decoderClassName + "." + formatPropertyName(fieldToken.name()) + "NullValue()";
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
                "decoder.actingVersion() >= " + decoderClassName + "." + formattedPropertyName + "SinceVersion()",
                "decoder." + formattedPropertyName + "()",
                decoderNullValue,
                wasAddedAfterParent
            );
        }
        else if (arrayLength > 1)
        {
            generateArrayDecodeWith(sb, decoderClassName, fieldToken, typeToken, indent, wasAddedAfterParent);
        }
    }

    private void generateArrayDecodeWith(
        final StringBuilder sb,
        final String decoderClassName,
        final Token fieldToken,
        final Token typeToken,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);
        final PrimitiveType primitiveType = typeToken.encoding().primitiveType();

        if (primitiveType == PrimitiveType.CHAR)
        {
            generateRecordPropertyAssignment(
                sb,
                fieldToken,
                indent,
                "decoder.actingVersion() >= " + decoderClassName + "." + formattedPropertyName + "SinceVersion()",
                "decoder." + formattedPropertyName + "()",
                "\"\"",
                wasAddedAfterParent
            );
        }
        else
        {
            final StringBuilder initializerList = new StringBuilder();
            final String elementType = javaTypeName(primitiveType);
            initializerList.append("new ").append(elementType).append("[] { ");
            final int arrayLength = typeToken.arrayLength();
            for (int i = 0; i < arrayLength; i++)
            {
                initializerList.append("decoder.").append(formattedPropertyName).append("(").append(i).append("),");
            }
            assert arrayLength > 0;
            initializerList.setLength(initializerList.length() - 1);
            initializerList.append(" }");

            generateRecordPropertyAssignment(
                sb,
                fieldToken,
                indent,
                "decoder.actingVersion() >= " + decoderClassName + "." + formattedPropertyName + "SinceVersion()",
                initializerList,
                "new " + elementType + "[0]",
                wasAddedAfterParent
            );
        }
    }

    private void generateBitSetDecodeWith(
        final StringBuilder sb,
        final String decoderClassName,
        final Token fieldToken,
        final String dtoTypeName,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (wasAddedAfterParent.test(fieldToken))
        {
            sb.append(indent).append("if (decoder.actingVersion() >= ")
                .append(decoderClassName).append(".")
                .append(formattedPropertyName).append("SinceVersion())\n")
                .append(indent).append("{\n");

            sb.append(indent).append(INDENT).append(dtoTypeName).append(".decodeWith(decoder.")
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
            sb.append(indent).append(dtoTypeName).append(".decodeWith(decoder.")
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
            .append("decoder.").append(formattedPropertyName).append("());\n");
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

        sb.append(indent).append(dtoClassName).append(".decodeWith(decoder.")
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
                .append(groupDtoClassName).append(".decodeManyWith(decoder.")
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
        final String decoderClassName,
        final List<Token> tokens,
        final String indent,
        final Predicate<Token> wasAddedAfterParent)
    {
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();
                final String formattedPropertyName = formatPropertyName(propertyName);
                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();

                final boolean isOptional = wasAddedAfterParent.test(token);
                final String blockIndent = isOptional ? indent + INDENT : indent;

                final String dataVar = toLowerFirstChar(propertyName) + "Data";

                final StringBuilder decoderValueExtraction = new StringBuilder();

                if (characterEncoding == null)
                {
                    decoderValueExtraction.append(blockIndent).append("byte[] ").append(dataVar)
                        .append(" = new byte[decoder.").append(formattedPropertyName).append("Length()];\n")
                        .append(blockIndent).append("decoder.get").append(toUpperFirstChar(formattedPropertyName))
                        .append("(").append(dataVar).append(", 0, decoder.").append(formattedPropertyName)
                        .append("Length());\n");
                }
                else
                {
                    decoderValueExtraction.append(blockIndent).append("String ").append(dataVar)
                        .append(" = decoder.").append(formattedPropertyName).append("();\n");
                }

                if (isOptional)
                {
                    sb.append(indent).append("if (decoder.actingVersion() >= ")
                        .append(decoderClassName).append(".")
                        .append(formattedPropertyName).append("SinceVersion())\n")
                        .append(indent).append("{\n");

                    sb.append(decoderValueExtraction);

                    sb.append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(")
                        .append(dataVar).append(");\n");

                    final String nullDtoValue = characterEncoding == null ? "new byte[0]" : "\"\"";

                    sb.append(indent).append("}\n")
                        .append(indent).append("else\n")
                        .append(indent).append("{\n")
                        .append(indent).append(INDENT).append("dto.")
                        .append(formattedPropertyName).append("(").append(nullDtoValue).append(");\n")
                        .append(indent).append("}\n");
                }
                else
                {
                    sb.append(decoderValueExtraction);

                    sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
                        .append(dataVar).append(");\n");
                }
            }
        }
    }

    private void generateRecordPropertyAssignment(
        final StringBuilder sb,
        final Token token,
        final String indent,
        final String presenceExpression,
        final CharSequence getExpression,
        final String nullDecoderValue,
        final Predicate<Token> wasAddedAfterParent)
    {
        final String propertyName = token.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        if (wasAddedAfterParent.test(token))
        {
            sb.append(indent).append("if (").append(presenceExpression).append(")\n")
                .append(indent).append("{\n");

            sb.append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(")
                .append(getExpression).append(");\n");

            sb.append(indent).append("}\n")
                .append(indent).append("else\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("dto.").append(formattedPropertyName).append("(")
                .append(nullDecoderValue).append(");\n")
                .append(indent).append("}\n");
        }
        else
        {
            sb.append(indent).append("dto.").append(formattedPropertyName).append("(")
                .append(getExpression).append(");\n");
        }
    }

    private void generateEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String encoderClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic().append("\n")
            .append(indent).append("public static void encodeWith(").append(encoderClassName).append(" encoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        generateFieldsEncodeWith(encodeBuilder, fields, indent + INDENT);
        generateGroupsEncodeWith(encodeBuilder, encoderClassName, groups, indent + INDENT);
        generateVarDataEncodeWith(encodeBuilder, varData, indent + INDENT);

        encodeBuilder.append(indent).append("}\n");
    }

    private static void generateEncodeWithOverloads(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String encoderClassName,
        final String indent)
    {
        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static int encodeWith(").append(dtoClassName).append(" dto, ")
            .append("MutableDirectBuffer buffer, int offset)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(encoderClassName).append(" encoder = new ")
            .append(encoderClassName).append("();\n")
            .append(indent).append(INDENT).append("encoder.wrap(buffer, offset);\n")
            .append(indent).append(INDENT).append("encodeWith(encoder, dto);\n")
            .append(indent).append(INDENT).append("return encoder.encodedLength();\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static int encodeWithHeaderWith(")
            .append(dtoClassName).append(" dto, ")
            .append("MutableDirectBuffer buffer, int offset)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(encoderClassName).append(" encoder = new ")
            .append(encoderClassName).append("();\n")
            .append(indent).append(INDENT)
            .append("encoder.wrapAndApplyHeader(buffer, offset, new MessageHeaderEncoder());\n")
            .append(indent).append(INDENT).append("encodeWith(encoder, dto);\n")
            .append(indent).append(INDENT).append("return encoder.limit() - offset;\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static byte[] bytes(")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("byte[] bytes = new byte[dto.computeEncodedLength()];\n")
            .append(indent).append(INDENT).append("encodeWith(dto, new UnsafeBuffer(bytes), 0);\n")
            .append(indent).append(INDENT).append("return bytes;\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static byte[] bytesWithHeader(")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("byte[] bytes = new byte[dto.computeEncodedLength() + ")
            .append("MessageHeaderEncoder.ENCODED_LENGTH];\n")
            .append(indent).append(INDENT).append("encodeWithHeaderWith(dto, new UnsafeBuffer(bytes), 0);\n")
            .append(indent).append(INDENT).append("return bytes;\n")
            .append(indent).append("}\n");
    }

    private void generateFieldsEncodeWith(
        final StringBuilder sb,
        final List<Token> tokens,
        final String indent)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                generateFieldEncodeWith(sb, signalToken, encodingToken, indent);
            }
        }
    }

    private void generateFieldEncodeWith(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveEncodeWith(sb, fieldToken, typeToken, indent);
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
            generatePrimitiveValueEncodeWith(sb, fieldToken, indent);
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

        final PrimitiveType primitiveType = typeToken.encoding().primitiveType();

        if (primitiveType == PrimitiveType.CHAR)
        {
            sb.append(indent).append("encoder.").append(toLowerFirstChar(propertyName)).append("(")
                .append("dto.").append(formattedPropertyName).append("());\n");
        }
        else
        {
            final String javaTypeName = javaTypeName(primitiveType);
            sb.append(indent).append(javaTypeName).append("[] ").append(formattedPropertyName).append(" = ")
                .append("dto.").append(formattedPropertyName).append("();\n")
                .append(indent).append("for (int i = 0; i < ").append(formattedPropertyName).append(".length; i++)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("encoder.").append(formattedPropertyName).append("(")
                .append("i, ").append(formattedPropertyName).append("[i]);\n")
                .append(indent).append("}\n");
        }
    }

    private void generatePrimitiveValueEncodeWith(
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
        final String accessor = "dto." + formattedPropertyName + "()";

        sb.append(indent).append("encoder.").append(formattedPropertyName).append("(")
            .append(accessor).append(");\n");
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

        sb.append(indent).append("encoder.").append(formattedPropertyName).append("(dto.")
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

        sb.append(indent).append(typeName).append(".encodeWith(encoder.")
            .append(formattedPropertyName).append("(), dto.")
            .append(formattedPropertyName).append("());\n");
    }

    private void generateGroupsEncodeWith(
        final StringBuilder sb,
        final String parentEncoderClassName,
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
            final String groupEncoderVarName = groupName + "Encoder";
            final String groupDtoTypeName = formatDtoClassName(groupName);
            final String groupEncoderTypeName = parentEncoderClassName + "." + encoderName(groupName);

            sb.append("\n")
                .append(indent).append("List<").append(groupDtoTypeName).append("> ")
                .append(formattedPropertyName).append(" = dto.").append(formattedPropertyName).append("();\n\n")
                .append(indent).append(groupEncoderTypeName).append(" ").append(groupEncoderVarName)
                .append(" = encoder.").append(formattedPropertyName)
                .append("Count(").append(formattedPropertyName).append(".size());\n\n")
                .append(indent).append("for (").append(groupDtoTypeName).append(" group : ")
                .append(formattedPropertyName).append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(groupDtoTypeName)
                .append(".encodeWith(").append(groupEncoderVarName).append(".next(), group);\n")
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
                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();

                if (characterEncoding == null)
                {
                    sb.append(indent).append("encoder.put").append(toUpperFirstChar(propertyName)).append("(")
                        .append("dto.").append(formattedPropertyName).append("(),")
                        .append("0,")
                        .append("dto.").append(formattedPropertyName).append("().length);\n");
                }
                else
                {
                    sb.append(indent).append("encoder.").append(formattedPropertyName).append("(")
                        .append("dto.").append(formattedPropertyName).append("());\n");
                }
            }
        }
    }

    private void generateDisplay(
        final ClassBuilder classBuilder,
        final String encoderClassName,
        final String lengthExpression,
        final String indent)
    {
        final StringBuilder sb = classBuilder.appendPublic();

        sb.append("\n")
            .append(indent).append("public String toString()\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT)
            .append("MutableDirectBuffer buffer = new UnsafeBuffer(new byte[").append(lengthExpression).append("]);\n")
            .append(indent).append(INDENT).append(encoderClassName).append(" encoder = new ")
            .append(encoderClassName).append("();\n")
            .append(indent).append(INDENT).append("encoder.");

        sb.append("wrap").append("(buffer, 0);\n");

        sb.append(indent).append(INDENT).append("encodeWith(encoder, this);\n")
            .append(indent).append(INDENT).append("StringBuilder sb = new StringBuilder();\n")
            .append(indent).append(INDENT).append("encoder.appendTo(sb);\n")
            .append(indent).append(INDENT).append("return sb.toString();\n")
            .append(indent).append("}\n");
    }

    private void generateFields(
        final ClassBuilder classBuilder,
        final String decoderClassName,
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
                            classBuilder, decoderClassName, propertyName, signalToken, encodingToken, indent);
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
        final String fieldName = formatFieldName(propertyName);

        classBuilder.appendField()
            .append(indent).append("private ").append(typeName).append(" ").append(fieldName)
            .append(" = new ").append(typeName).append("();\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public ").append(typeName).append(" ")
            .append(formattedPropertyName).append("()\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public void ")
            .append(formattedPropertyName).append("(").append(typeName).append(" value)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
            .append(indent).append("}\n");
    }

    private void generateEnumProperty(
        final ClassBuilder classBuilder,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName());

        final String formattedPropertyName = formatPropertyName(propertyName);

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();
            final String caseName = constValue.substring(constValue.indexOf(".") + 1);

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public static ").append(enumName).append(" ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(enumName).append(".")
                .append(caseName).append(";\n")
                .append(indent).append("}\n");
        }
        else
        {
            final String fieldName = formatFieldName(propertyName);

            classBuilder.appendField()
                .append(indent).append("private ").append(enumName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public ").append(enumName).append(" ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public void ").append(formattedPropertyName)
                .append("(").append(enumName).append(" value)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                .append(indent).append("}\n");
        }
    }

    private void generatePrimitiveProperty(
        final ClassBuilder classBuilder,
        final String decoderClassName,
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
            generatePrimitivePropertyMethods(
                classBuilder, decoderClassName, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generatePrimitivePropertyMethods(
        final ClassBuilder classBuilder,
        final String decoderClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            generateSingleValueProperty(classBuilder, decoderClassName, propertyName, fieldToken, typeToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayProperty(classBuilder, decoderClassName, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayProperty(
        final ClassBuilder classBuilder,
        final String decoderClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String fieldName = formatFieldName(propertyName);
        final String validateMethod = "validate" + toUpperFirstChar(propertyName);

        final PrimitiveType primitiveType = typeToken.encoding().primitiveType();

        if (primitiveType == PrimitiveType.CHAR)
        {
            final CharSequence typeName = "String";

            classBuilder.appendField()
                .append(indent).append("private ").append(typeName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public ").append(typeName).append(" ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("void ").append(formattedPropertyName)
                .append("(").append(typeName).append(" value)\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(validateMethod).append("(value);\n")
                .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                .append(indent).append("}\n");

            generateArrayValidateMethod(
                classBuilder,
                decoderClassName,
                indent,
                validateMethod,
                typeName,
                ".length()",
                formattedPropertyName);
        }
        else
        {
            final String elementTypeName = javaTypeName(primitiveType);
            final String typeName = elementTypeName + "[]";

            classBuilder.appendField()
                .append(indent).append("private ").append(typeName).append(" ").append(fieldName).append(";\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public ").append(typeName).append(" ")
                .append(formattedPropertyName).append("()\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                .append(indent).append("}\n");

            classBuilder.appendPublic().append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public void ").append(formattedPropertyName).append("(")
                .append(typeName).append(" value").append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append(validateMethod).append("(value);\n")
                .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                .append(indent).append("}\n");

            generateArrayValidateMethod(
                classBuilder,
                decoderClassName,
                indent,
                validateMethod,
                typeName,
                ".length",
                formattedPropertyName);
        }
    }

    private static void generateArrayValidateMethod(
        final ClassBuilder classBuilder,
        final String decoderClassName,
        final String indent,
        final String validateMethod,
        final CharSequence typeName,
        final String lengthAccessor,
        final String formattedPropertyName)
    {
        final StringBuilder validateBuilder = classBuilder.appendPrivate().append("\n")
            .append(indent).append("private static void ").append(validateMethod).append("(")
            .append(typeName).append(" value)\n")
            .append(indent).append("{\n");

        validateBuilder.append(indent).append(INDENT)
            .append("if (value").append(lengthAccessor).append(" > ").append(decoderClassName).append(".")
            .append(formattedPropertyName).append("Length())\n")
            .append(indent).append(INDENT)
            .append("{\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("throw new IllegalArgumentException(\"")
            .append(formattedPropertyName)
            .append(": too many elements: \" + ")
            .append("value").append(lengthAccessor).append(");\n")
            .append(indent).append(INDENT)
            .append("}\n")
            .append(indent).append("}\n");
    }

    private void generateSingleValueProperty(
        final ClassBuilder classBuilder,
        final String decoderClassName,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String typeName = javaTypeName(typeToken.encoding().primitiveType());
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String fieldName = formatFieldName(propertyName);
        final String validateMethod = "validate" + toUpperFirstChar(propertyName);

        final boolean representedWithinJavaType = typeToken.encoding().primitiveType() != PrimitiveType.UINT64;

        final StringBuilder validationCall = new StringBuilder();

        if (representedWithinJavaType)
        {
            final StringBuilder validateBuilder = classBuilder.appendPrivate().append("\n")
                .append(indent).append("private static void ").append(validateMethod).append("(")
                .append(typeName).append(" value)\n")
                .append(indent).append("{\n");

            validateBuilder.append(indent).append(INDENT)
                .append("if (value < ")
                .append(decoderClassName).append(".").append(formattedPropertyName).append("MinValue() || ")
                .append("value").append(" > ")
                .append(decoderClassName).append(".").append(formattedPropertyName).append("MaxValue())\n")
                .append(indent).append(INDENT)
                .append("{\n")
                .append(indent).append(INDENT).append(INDENT)
                .append("throw new IllegalArgumentException(\"")
                .append(propertyName)
                .append(": value is out of allowed range: \" + ")
                .append("value").append(");\n")
                .append(indent).append(INDENT)
                .append("}\n")
                .append(indent).append("}\n");

            validationCall.append(indent).append(INDENT).append(validateMethod).append("(value);\n");
        }

        classBuilder.appendField()
            .append(indent).append("private ").append(typeName).append(" ").append(fieldName).append(";\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public ").append(typeName).append(" ")
            .append(formattedPropertyName).append("()\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
            .append(indent).append("}\n");

        classBuilder.appendPublic().append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public void ").append(formattedPropertyName).append("(")
            .append(typeName).append(" value)\n")
            .append(indent).append("{\n")
            .append(validationCall)
            .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
            .append(indent).append("}\n");
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
                .append(indent).append("public static String ").append(toLowerFirstChar(propertyName)).append("()\n")
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
                .append(indent).append("public static ")
                .append(javaTypeName(typeToken.encoding().primitiveType()))
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
                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();
                final String dtoType = characterEncoding == null ? "byte[]" : "String";

                final String fieldName = formatFieldName(propertyName);
                final String formattedPropertyName = formatPropertyName(propertyName);

                classBuilder.appendField()
                    .append(indent).append("private ").append(dtoType).append(" ").append(fieldName).append(";\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("public ").append(dtoType).append(" ")
                    .append(formattedPropertyName).append("()\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic().append("\n")
                    .append(indent).append("public void ").append(formattedPropertyName)
                    .append("(").append(dtoType).append(" value)\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
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
        final String encoderClassName = encoderName(name);
        final String decoderClassName = decoderName(name);

        try (Writer out = outputManager.createOutput(className))
        {
            final List<Token> compositeTokens = tokens.subList(1, tokens.size() - 1);
            out.append(generateDtoFileHeader(ir.applicableNamespace()));
            out.append("import org.agrona.DirectBuffer;\n");
            out.append("import org.agrona.MutableDirectBuffer;\n");
            out.append("import org.agrona.concurrent.UnsafeBuffer;\n\n");
            out.append(generateDocumentation(BASE_INDENT, tokens.get(0)));

            final ClassBuilder classBuilder = new ClassBuilder(className, BASE_INDENT, "public final");

            generateCompositePropertyElements(classBuilder, decoderClassName, compositeTokens,
                BASE_INDENT + INDENT);
            generateCompositeDecodeWith(classBuilder, className, decoderClassName, compositeTokens,
                BASE_INDENT + INDENT);
            generateCompositeEncodeWith(classBuilder, className, encoderClassName, compositeTokens,
                BASE_INDENT + INDENT);
            generateDisplay(classBuilder, encoderClassName, encoderClassName + ".ENCODED_LENGTH",
                BASE_INDENT + INDENT);

            classBuilder.appendTo(out);
        }
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String name = tokens.get(0).applicableTypeName();
        final String className = formatDtoClassName(name);
        final String encoderClassName = encoderName(name);
        final String decoderClassName = decoderName(name);

        try (Writer out = outputManager.createOutput(className))
        {
            final List<Token> setTokens = tokens.subList(1, tokens.size() - 1);
            out.append(generateDtoFileHeader(ir.applicableNamespace()));
            out.append(generateDocumentation(BASE_INDENT, tokens.get(0)));

            final ClassBuilder classBuilder = new ClassBuilder(className, BASE_INDENT, "public final");

            generateChoices(classBuilder, className, setTokens, BASE_INDENT + INDENT);
            generateChoiceSetDecodeWith(classBuilder, className, decoderClassName, setTokens, BASE_INDENT + INDENT);
            generateChoiceSetEncodeWith(classBuilder, className, encoderClassName, setTokens, BASE_INDENT + INDENT);

            classBuilder.appendTo(out);
        }
    }

    private void generateChoiceSetEncodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String encoderClassName,
        final List<Token> setTokens,
        final String indent)
    {
        final StringBuilder encodeBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static void encodeWith(\n")
            .append(indent).append(INDENT).append(encoderClassName).append(" encoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        encodeBuilder.append(indent).append(INDENT).append("encoder.clear();\n");

        for (final Token token : setTokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String formattedPropertyName = formatPropertyName(token.name());
                encodeBuilder.append(indent).append(INDENT).append("encoder.").append(formattedPropertyName)
                    .append("(dto.").append(formattedPropertyName).append("());\n");
            }
        }

        encodeBuilder.append(indent).append("}\n");
    }

    private void generateChoiceSetDecodeWith(
        final ClassBuilder classBuilder,
        final String dtoClassName,
        final String decoderClassName,
        final List<Token> setTokens,
        final String indent)
    {
        final StringBuilder decodeBuilder = classBuilder.appendPublic()
            .append("\n")
            .append(indent).append("public static void decodeWith(\n")
            .append(indent).append(INDENT).append(decoderClassName).append(" decoder, ")
            .append(dtoClassName).append(" dto)\n")
            .append(indent).append("{\n");

        for (final Token token : setTokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String formattedPropertyName = formatPropertyName(token.name());
                decodeBuilder.append(indent).append(INDENT).append("dto.").append(formattedPropertyName)
                    .append("(decoder.").append(formattedPropertyName).append("());\n");
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
                final String fieldName = formatFieldName(token.name());
                final String formattedPropertyName = formatPropertyName(token.name());

                fields.add(fieldName);

                classBuilder.appendField()
                    .append(indent).append("boolean ").append(fieldName).append(";\n");

                classBuilder.appendPublic()
                    .append("\n")
                    .append(indent).append("public boolean ").append(formattedPropertyName).append("()\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append("return ").append(fieldName).append(";\n")
                    .append(indent).append("}\n");

                classBuilder.appendPublic()
                    .append("\n")
                    .append(indent).append(dtoClassName).append(" ")
                    .append(formattedPropertyName).append("(boolean value)\n")
                    .append(indent).append("{\n")
                    .append(indent).append(INDENT).append(fieldName).append(" = value;\n")
                    .append(indent).append(INDENT).append("return this;\n")
                    .append(indent).append("}\n");
            }
        }

        final StringBuilder clearBuilder = classBuilder.appendPublic()
            .append(indent).append(dtoClassName).append(" clear()\n")
            .append(indent).append("{\n");

        for (final String field : fields)
        {
            clearBuilder.append(indent).append(INDENT).append(field).append(" = false;\n");
        }

        clearBuilder.append(indent).append(INDENT).append("return this;\n")
            .append(indent).append("}\n");
    }

    private void generateCompositePropertyElements(
        final ClassBuilder classBuilder,
        final String decoderClassName,
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
                    generatePrimitiveProperty(classBuilder, decoderClassName, propertyName, token, token, indent);
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

    private static CharSequence generateDtoFileHeader(final String packageName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message DTO */\n");
        sb.append("package ").append(packageName).append(";\n\n");

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

    private static String formatFieldName(final String propertyName)
    {
        return formatPropertyName(propertyName);
    }
}
