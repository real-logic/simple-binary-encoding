/*
 * Copyright 2013-2023 Real Logic Limited.
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

package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.Generators;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import org.agrona.Verify;
import org.agrona.generation.OutputManager;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.csharp.CSharpUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;

/**
 * DTO generator for the CSharp programming language.
 */
public class CSharpDtoGenerator implements CodeGenerator
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
    public CSharpDtoGenerator(final Ir ir, final OutputManager outputManager)
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

            final StringBuilder sb = new StringBuilder();

            final List<Token> fields = new ArrayList<>();
            offset = collectFields(messageBody, offset, fields);
            generateFields(sb, fields, BASE_INDENT + INDENT);

            final List<Token> groups = new ArrayList<>();
            offset = collectGroups(messageBody, offset, groups);
            generateGroups(sb, codecClassName, groups, BASE_INDENT + INDENT);

            final List<Token> varData = new ArrayList<>();
            collectVarData(messageBody, offset, varData);
            generateVarData(sb, varData, BASE_INDENT + INDENT);

            generateDecodeFrom(sb, className, codecClassName, fields, groups, varData, BASE_INDENT + INDENT);
            generateEncodeInto(sb, codecClassName, fields, groups, varData, BASE_INDENT + INDENT);
            generateDisplay(sb, codecClassName, "WrapForEncode", null, BASE_INDENT + INDENT);

            try (Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(
                    ir.applicableNamespace(),
                    "#nullable enable\n\n",
                    "using System.Collections.Generic;\n",
                    "using System.Linq;\n"));
                out.append(generateDocumentation(BASE_INDENT, msgToken));

                out.append(BASE_INDENT).append("public sealed partial record ").append(className).append("\n")
                    .append(BASE_INDENT).append("{")
                    .append(sb)
                    .append(BASE_INDENT).append("}\n")
                    .append("}\n");
            }
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
            final String groupClassName = formatDtoClassName(groupName);

            sb.append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("public IReadOnlyList<").append(groupClassName).append("> ")
                .append(formatPropertyName(groupName))
                .append(" { get; init; }\n");

            sb.append("\n")
                .append(generateDocumentation(indent, groupToken))
                .append(indent).append("public sealed partial record ").append(groupClassName).append("\n")
                .append(indent).append("{");

            i++;
            i += tokens.get(i).componentTokenCount();

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            generateFields(sb, fields, indent + INDENT);

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            final String codecClassName = parentMessageClassName + "." + formatClassName(groupName) + "Group";
            generateGroups(sb, codecClassName, groups, indent + INDENT);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(sb, varData, indent + INDENT);

            generateDecodeListFrom(sb, groupClassName, codecClassName, indent + INDENT);
            generateDecodeFrom(sb, groupClassName, codecClassName, fields, groups, varData, indent + INDENT);
            generateEncodeInto(sb, codecClassName, fields, groups, varData, indent + INDENT);

            sb.append(indent).append("}\n");
        }
    }

    private void generateCompositeDecodeFrom(
        final StringBuilder sb,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public static ").append(dtoClassName).append(" DecodeFrom(")
            .append(codecClassName).append(" codec)\n")
            .append(indent).append("{\n");

        sb.append(indent).append(INDENT).append("return new ").append(dtoClassName).append("()\n")
            .append(indent).append(INDENT).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldDecodeFrom(sb, token, token, codecClassName, indent + INDENT + INDENT);

            i += tokens.get(i).componentTokenCount();
        }

        sb.append(indent).append(INDENT).append("};\n");
        sb.append(indent).append("}\n");
    }

    private void generateCompositeEncodeInto(
        final StringBuilder sb,
        final String codecClassName,
        final List<Token> tokens,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public void EncodeInto(").append(codecClassName).append(" codec)\n")
            .append(indent).append("{\n");

        for (int i = 0; i < tokens.size(); )
        {
            final Token token = tokens.get(i);

            generateFieldEncodeInto(sb, codecClassName, token, token, indent + INDENT);

            i += tokens.get(i).componentTokenCount();
        }

        sb.append(indent).append("}\n");
    }

    private void generateDecodeListFrom(
        final StringBuilder sb,
        final String dtoClassName,
        final String codecClassName,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public static IReadOnlyList<").append(dtoClassName).append("> DecodeListFrom(")
            .append(codecClassName).append(" codec)\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT).append("var ").append("list = new List<").append(dtoClassName)
            .append(">(codec.Count);\n")
            .append(indent).append(INDENT)
            .append("while (codec.HasNext)\n")
            .append(indent).append(INDENT)
            .append("{\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("var element = ").append(dtoClassName).append(".DecodeFrom(codec.Next());\n")
            .append(indent).append(INDENT).append(INDENT)
            .append("list.Add(element);\n")
            .append(indent).append(INDENT)
            .append("}\n")
            .append(indent).append(INDENT)
            .append("return list.AsReadOnly();\n")
            .append(indent).append("}\n");
    }

    private void generateDecodeFrom(
        final StringBuilder sb,
        final String dtoClassName,
        final String codecClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public static ").append(dtoClassName)
            .append(" DecodeFrom(").append(codecClassName).append(" codec)\n")
            .append(indent).append("{\n");

        sb.append(indent).append(INDENT).append("return new ").append(dtoClassName).append("()\n")
            .append(indent).append(INDENT).append("{\n");
        generateFieldsDecodeFrom(sb, fields, codecClassName, indent + INDENT + INDENT);
        generateGroupsDecodeFrom(sb, groups, indent + INDENT + INDENT);
        generateVarDataDecodeFrom(sb, varData, indent + INDENT + INDENT);
        sb.append(indent).append(INDENT).append("};\n");

        sb.append(indent).append("}\n");
    }

    private void generateFieldsDecodeFrom(
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

                generateFieldDecodeFrom(sb, signalToken, encodingToken, codecClassName, indent);
            }
        }
    }

    private void generateFieldDecodeFrom(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String codecClassName, final String indent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveDecodeFrom(sb, fieldToken, typeToken, codecClassName, indent);
                break;

            case BEGIN_SET:
                generatePropertyDecodeFrom(sb, codecClassName, fieldToken, "0", indent);
                break;

            case BEGIN_ENUM:
                final String enumName = formatClassName(typeToken.applicableTypeName());
                final String nullValue = formatNamespace(ir.packageName()) + "." + enumName + ".NULL_VALUE";
                generatePropertyDecodeFrom(sb, codecClassName, fieldToken, nullValue, indent);
                break;

            case BEGIN_COMPOSITE:
                generateComplexDecodeFrom(sb, fieldToken, typeToken, indent);
                break;

            default:
                break;
        }
    }

    private void generatePrimitiveDecodeFrom(
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
            final String nullValue = codecClassName + "." + formatPropertyName(fieldToken.name()) + "NullValue";
            generatePropertyDecodeFrom(sb, codecClassName, fieldToken, nullValue, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayDecodeFrom(sb, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayDecodeFrom(
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
            wrapInActingVersionCheck(
                sb,
                fieldToken,
                indent,
                "codec.Get" + formattedPropertyName + "()",
                "null",
                null
            );
        }
        else
        {
            wrapInActingVersionCheck(
                sb,
                fieldToken,
                indent,
                "codec." + formattedPropertyName + "AsSpan().ToArray()",
                "null",
                null
            );
        }
    }

    private void generatePropertyDecodeFrom(
        final StringBuilder sb,
        final String codecClassName,
        final Token fieldToken,
        final String nullValue,
        final String indent)
    {

        if (fieldToken.isConstantEncoding())
        {
            return;
        }

        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        wrapInActingVersionCheck(
            sb,
            fieldToken,
            indent,
            "codec." + formattedPropertyName,
            nullValue,
            codecClassName + "." + formattedPropertyName + "NullValue"
        );
    }

    private void generateComplexDecodeFrom(
        final StringBuilder sb,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);
        final String dtoClassName = formatDtoClassName(typeToken.applicableTypeName());

        wrapInActingVersionCheck(
            sb,
            fieldToken,
            indent,
            dtoClassName + ".DecodeFrom(codec." + formattedPropertyName + ")",
            "null",
            "null"
        );
    }

    private void generateGroupsDecodeFrom(
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

            wrapInActingVersionCheck(
                sb,
                groupToken,
                indent,
                groupDtoClassName + ".DecodeListFrom(codec." + formattedPropertyName + ")",
                "new List<" + groupDtoClassName + ">(0).AsReadOnly()",
                null
            );

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

    private void generateVarDataDecodeFrom(
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
                final Token varDataToken = Generators.findFirst("varData", tokens, i);
                final String characterEncoding = varDataToken.encoding().characterEncoding();

                final String formattedPropertyName = formatPropertyName(propertyName);
                final String accessor = characterEncoding == null ?
                    "Get" + formattedPropertyName + "Bytes" :
                    "Get" + formattedPropertyName;

                wrapInActingVersionCheck(
                    sb,
                    token,
                    indent,
                    "codec." + accessor + "()",
                    "null",
                    null
                );
            }
        }
    }

    private void wrapInActingVersionCheck(
        final StringBuilder sb,
        final Token token,
        final String indent,
        final String presentExpression,
        final String notPresentExpression,
        final String nullCodecValueOrNull)
    {
        final String propertyName = token.name();
        final String formattedPropertyName = formatPropertyName(propertyName);

        sb.append(indent).append(formattedPropertyName).append(" = ");

        if (token.version() > 0)
        {
            sb.append("codec.").append(formattedPropertyName).append("InActingVersion()");

            if (null != nullCodecValueOrNull)
            {
                sb.append(" && codec.").append(formattedPropertyName).append(" != ").append(nullCodecValueOrNull);
            }

            sb.append(" ?\n");
            sb.append(indent).append(INDENT).append(presentExpression).append(" :\n")
                .append(indent).append(INDENT).append(notPresentExpression).append(",\n");
        }
        else
        {
            sb.append(presentExpression).append(",\n");
        }
    }

    private void generateEncodeInto(
        final StringBuilder sb,
        final String codecClassName,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public void EncodeInto(").append(codecClassName).append(" codec)\n")
            .append(indent).append("{\n");

        generateFieldsEncodeInto(sb, codecClassName, fields, indent + INDENT);
        generateGroupsEncodeInto(sb, groups, indent + INDENT);
        generateVarDataEncodeInto(sb, varData, indent + INDENT);

        sb.append(indent).append("}\n");
    }

    private void generateFieldsEncodeInto(
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
                generateFieldEncodeInto(sb, codecClassName, signalToken, encodingToken, indent);
            }
        }
    }

    private void generateFieldEncodeInto(
        final StringBuilder sb,
        final String codecClassName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        switch (typeToken.signal())
        {
            case ENCODING:
                generatePrimitiveEncodeInto(sb, codecClassName, fieldToken, typeToken, indent);
                break;

            case BEGIN_SET:
            case BEGIN_ENUM:
                generateEnumEncodeInto(sb, fieldToken, indent);
                break;

            case BEGIN_COMPOSITE:
                generateComplexEncodeInto(sb, fieldToken, indent);
                break;

            default:
                break;
        }
    }

    private void generatePrimitiveEncodeInto(
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
            generatePropertyEncodeInto(sb, codecClassName, fieldToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayEncodeInto(sb, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayEncodeInto(
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
            final String value = nullableConvertedExpression(fieldToken, formattedPropertyName, "\"\"");
            sb.append(indent).append("codec.Set").append(formattedPropertyName).append("(")
                .append(value).append(");\n");
        }
        else
        {
            final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());

            sb.append(indent).append("new Span<").append(typeName).append(">(").append(formattedPropertyName)
                .append("?.ToArray()).CopyTo(codec.").append(formattedPropertyName).append("AsSpan());\n");
        }
    }

    private String nullableConvertedExpression(
        final Token fieldToken,
        final String expression,
        final String nullValue)
    {
        return fieldToken.version() > 0 || fieldToken.isOptionalEncoding() ?
            expression + " ?? " + nullValue :
            expression;
    }

    private void generatePropertyEncodeInto(
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

        final String value = nullableConvertedExpression(
            fieldToken,
            formattedPropertyName,
            codecClassName + "." + formattedPropertyName + "NullValue");

        sb.append(indent).append("codec.").append(formattedPropertyName).append(" = ")
            .append(value).append(";\n");
    }

    private void generateEnumEncodeInto(
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

        sb.append(indent).append("codec.").append(formattedPropertyName).append(" = ")
            .append(formattedPropertyName).append(";\n");
    }

    private void generateComplexEncodeInto(
        final StringBuilder sb,
        final Token fieldToken,
        final String indent)
    {
        final String propertyName = fieldToken.name();
        final String formattedPropertyName = formatPropertyName(propertyName);
        sb.append(indent).append(formattedPropertyName).append(".EncodeInto(codec.")
            .append(formattedPropertyName).append(");\n");
    }

    private void generateGroupsEncodeInto(
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

            sb.append("\n")
                .append(indent).append("var ").append(groupCodecVarName)
                .append(" = codec.").append(formattedPropertyName)
                .append("Count(").append(formattedPropertyName).append(".Count);\n\n")
                .append(indent).append("foreach (var group in ").append(formattedPropertyName).append(")\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("group.EncodeInto(").append(groupCodecVarName)
                .append(".Next()").append(");\n")
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

    private void generateVarDataEncodeInto(
        final StringBuilder sb,
        final List<Token> tokens,
        final String indent)
    {
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.BEGIN_VAR_DATA)
            {
                final String propertyName = token.name();

                sb.append(indent).append("codec.Set").append(formatPropertyName(propertyName))
                    .append("(").append(formatPropertyName(propertyName)).append(");\n");
            }
        }
    }

    private void generateDisplay(
        final StringBuilder sb,
        final String codecClassName,
        final String wrapMethod,
        final String actingVersion,
        final String indent)
    {
        sb.append("\n")
            .append(indent).append("public string ToSbeString()\n")
            .append(indent).append("{\n")
            .append(indent).append(INDENT)
            .append("var buffer = new DirectBuffer(new byte[128], (ignored, newSize) => new byte[newSize]);\n")
            .append(indent).append(INDENT).append("var codec = new ").append(codecClassName).append("();\n")
            .append(indent).append(INDENT).append("codec.");
        sb.append(wrapMethod).append("(buffer, 0");
        if (null != actingVersion)
        {
            sb.append(", ").append(actingVersion);
        }
        sb.append(");\n");
        sb.append(indent).append(INDENT).append("EncodeInto(codec);\n")
            .append(indent).append(INDENT).append("StringBuilder sb = new StringBuilder();\n")
            .append(indent).append(INDENT).append("codec.BuildString(sb);\n")
            .append(indent).append(INDENT).append("return sb.ToString();\n")
            .append(indent).append("}\n");
    }

    private void generateFields(
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
                final String propertyName = signalToken.name();

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        generatePrimitiveProperty(sb, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_ENUM:
                        generateEnumProperty(sb, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_SET:
                        generateBitSetProperty(sb, propertyName, signalToken, encodingToken, indent);
                        break;

                    case BEGIN_COMPOSITE:
                        generateCompositeProperty(sb, propertyName, signalToken, encodingToken, indent);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private String optionality(final Token fieldToken, final Token typeToken)
    {
        return fieldToken.isOptionalEncoding() ? "?" : "";
    }

    private void generateCompositeProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String compositeName = formatDtoClassName(typeToken.applicableTypeName());
        sb.append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public ").append(compositeName)
            .append(optionality(fieldToken, typeToken)).append(" ").append(formatPropertyName(propertyName))
            .append(" { get; init; }\n");
    }

    private void generateBitSetProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName());

        sb.append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public ").append(enumName).append(" ")
            .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
    }

    private void generateEnumProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String enumName = formatClassName(typeToken.applicableTypeName());

        if (fieldToken.isConstantEncoding())
        {
            final String constValue = fieldToken.encoding().constValue().toString();

            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public static ").append(enumName).append(" ")
                .append(formatPropertyName(propertyName)).append("\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("get { return ")
                .append(formatNamespace(ir.packageName())).append(".").append(constValue)
                .append("; }\n")
                .append(indent).append("}\n");
        }
        else
        {
            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public ").append(enumName).append(" ")
                .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
        }
    }

    private void generatePrimitiveProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.isConstantEncoding())
        {
            generateConstPropertyMethods(sb, propertyName, fieldToken, typeToken, indent);
        }
        else
        {
            generatePrimitivePropertyMethods(sb, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generatePrimitivePropertyMethods(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final int arrayLength = typeToken.arrayLength();

        if (arrayLength == 1)
        {
            generateSingleValueProperty(sb, propertyName, fieldToken, typeToken, indent);
        }
        else if (arrayLength > 1)
        {
            generateArrayProperty(sb, propertyName, fieldToken, typeToken, indent);
        }
    }

    private void generateArrayProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public string ")
                .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
        }
        else
        {
            final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());

            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public IReadOnlyList<").append(typeName).append(">")
                .append(optionality(fieldToken, typeToken)).append(" ")
                .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
        }
    }

    private void generateSingleValueProperty(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        final String typeName = cSharpTypeName(typeToken.encoding().primitiveType());

        sb.append("\n")
            .append(generateDocumentation(indent, fieldToken))
            .append(indent).append("public ").append(typeName)
            .append(optionality(fieldToken, typeToken)).append(" ")
            .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
    }

    private void generateConstPropertyMethods(
        final StringBuilder sb,
        final String propertyName,
        final Token fieldToken,
        final Token typeToken,
        final String indent)
    {
        if (typeToken.encoding().primitiveType() == PrimitiveType.CHAR)
        {
            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public static string ").append(toUpperFirstChar(propertyName)).append("\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT)
                .append("get { return \"").append(typeToken.encoding().constValue().toString()).append("\"; }\n")
                .append(indent).append("}\n");
        }
        else
        {
            final String literalValue =
                generateLiteral(typeToken.encoding().primitiveType(), typeToken.encoding().constValue().toString());

            sb.append("\n")
                .append(generateDocumentation(indent, fieldToken))
                .append(indent).append("public static ").append(cSharpTypeName(typeToken.encoding().primitiveType()))
                .append(" ").append(formatPropertyName(propertyName)).append("\n")
                .append(indent).append("{\n")
                .append(indent).append(INDENT).append("get { return ").append(literalValue).append("; }\n")
                .append(indent).append("}\n");
        }
    }

    private void generateVarData(
        final StringBuilder sb,
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
                final String dtoType = characterEncoding == null ? "byte[]" : "string";

                final String optionality = token.version() > 0 ? "?" : "";

                sb.append("\n")
                    .append(indent).append("public ").append(dtoType).append(" ")
                    .append(formatPropertyName(propertyName)).append(" { get; init; }\n");
            }
        }
    }

    private String formatDtoClassName(final String name)
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
            out.append(generateFileHeader(ir.applicableNamespace(),
                "#nullable enable\n",
                "using System.Collections.Generic;\n",
                "using System.Linq;\n"));
            out.append(generateDocumentation(BASE_INDENT, tokens.get(0)));

            final StringBuilder sb = new StringBuilder();

            final List<Token> compositeTokens = tokens.subList(1, tokens.size() - 1);
            generateCompositePropertyElements(sb, compositeTokens, BASE_INDENT + INDENT);
            generateCompositeDecodeFrom(sb, className, codecClassName, compositeTokens, BASE_INDENT + INDENT);
            generateCompositeEncodeInto(sb, codecClassName, compositeTokens, BASE_INDENT + INDENT);
            generateDisplay(sb, codecClassName, "Wrap", codecClassName + ".SbeSchemaVersion", BASE_INDENT + INDENT);

            out.append(BASE_INDENT).append("public sealed partial record ").append(className).append("\n")
                .append(BASE_INDENT).append("{")
                .append(sb)
                .append(BASE_INDENT).append("}\n")
                .append("}\n");
        }
    }

    private void generateCompositePropertyElements(
        final StringBuilder sb,
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
                    generatePrimitiveProperty(sb, propertyName, token, token, indent);
                    break;

                case BEGIN_ENUM:
                    generateEnumProperty(sb, propertyName, token, token, indent);
                    break;

                case BEGIN_SET:
                    generateBitSetProperty(sb, propertyName, token, token, indent);
                    break;

                case BEGIN_COMPOSITE:
                    generateCompositeProperty(sb, propertyName, token, token, indent);
                    break;

                default:
                    break;
            }

            i += tokens.get(i).componentTokenCount();
        }
    }
}
