/*
 * Copyright 2013-2023 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation.python;

import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.co.real_logic.sbe.generation.python.RustPythonGenerator.CodecType.Decoder;
import static uk.co.real_logic.sbe.generation.python.RustPythonGenerator.CodecType.Encoder;
import static uk.co.real_logic.sbe.generation.python.RustPythonGenerator.*;
import static uk.co.real_logic.sbe.generation.python.RustUtil.*;

class MessageCoderDef implements RustPythonGenerator.ParentDef
{
    private final StringBuilder sb = new StringBuilder();
    private final ArrayList<SubGroup> subGroups = new ArrayList<>();

    private final Ir ir;
    private final Token msgToken;
    final String name;
    final CodecType codecType;

    MessageCoderDef(final Ir ir, final Token msgToken, final CodecType codecType)
    {
        this.ir = ir;
        this.msgToken = msgToken;
        this.name = formatStructName(msgToken.name());
        this.codecType = codecType;
    }

    public static void generateStruct(
        final List<Token> members,
        String structName,
        Writer out
    ) throws IOException {
        // define struct...
        indent(out, 0, "#[derive(Debug, Default, Clone)]\n");
        indent(out, 0, "#[pyclass]\n");
        indent(out, 0, "pub struct %s {\n", structName);
        for (int i = 1, end = members.size() - 1; i < end; ) {
            final Token encodingToken = members.get(i);
            final StringBuilder sb = new StringBuilder();

//            System.out.println(format("Token '%s': Signal = %s", encodingToken.name(), encodingToken.signal().name()));
            switch (encodingToken.signal()) {
                case ENCODING -> generatePrimitiveField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_ENUM -> generateEnumField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_SET -> generateBitSetField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_COMPOSITE -> generateCompositeField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_GROUP -> generateGroupField(sb, 1, encodingToken, encodingToken.name());
                default -> {
                }
            }

            out.append(sb);
            i += encodingToken.componentTokenCount();
        }
        indent(out, 0, "}\n\n");

    }

    String blockLengthType()
    {
        return rustTypeName(ir.headerStructure().blockLengthType());
    }

    String schemaVersionType()
    {
        return rustTypeName(ir.headerStructure().schemaVersionType());
    }

    void generate(
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData) throws IOException
    {
        indent(sb, 0, "pub mod %s {\n", codecType.toString().toLowerCase());
        indent(sb, 1, "use super::*;\n\n");

        // i.e. <name>Decoder or <name>Encoder
        final String msgTypeName = formatStructName(msgToken.name()) + codecType.name();
        appendMessageStruct(sb, msgTypeName);

        if (codecType == Encoder)
        {
            RustPythonGenerator.appendImplEncoderTrait(sb, msgTypeName);
        }
        else
        {
            RustPythonGenerator.appendImplDecoderTrait(sb, msgTypeName);
        }

        RustPythonGenerator.appendImplWithLifetimeHeader(sb, msgTypeName); // impl start
        appendWrapFn(sb);

        indent(sb, 2, "#[inline]\n");
        indent(sb, 2, "pub fn encoded_length(&self) -> usize {\n");
        indent(sb, 3, "self.limit - self.offset\n");
        indent(sb, 2, "}\n\n");

        if (codecType == Decoder)
        {
            appendMessageHeaderDecoderFn(sb);

            RustPythonGenerator.generateDecoderFields(sb, fields, 2);
            RustPythonGenerator.generateDecoderGroups(sb, groups, 2, this);
            RustPythonGenerator.generateDecoderVarData(sb, varData, 2, false);
        }
        else
        {
            appendMessageHeaderEncoderFn(sb);

            RustPythonGenerator.generateEncoderFields(sb, fields, 2);
            RustPythonGenerator.generateEncoderGroups(sb, groups, 2, this);
            RustPythonGenerator.generateEncoderVarData(sb, varData, 2);
        }

        indent(sb, 1, "}\n\n"); // impl end

        // append all subGroup generated code
        for (final SubGroup subGroup : subGroups)
        {
            subGroup.appendTo(sb);
        }

        indent(sb, 0, "} // end %s\n\n", codecType.toString().toLowerCase()); // mod end
    }

    void appendTo(final Appendable dest) throws IOException
    {
        dest.append(sb);
    }

    public SubGroup addSubGroup(final String name, final int level, final Token groupToken)
    {
        final SubGroup subGroup = new SubGroup(name, level, groupToken);
        subGroups.add(subGroup);
        return subGroup;
    }

    void appendMessageHeaderEncoderFn(final Appendable out) throws IOException
    {
        indent(out, 2, "pub fn header(self, offset: usize) -> MessageHeaderEncoder<Self> {\n");
        indent(out, 3, "let mut header = MessageHeaderEncoder::default().wrap(self, offset);\n");
        indent(out, 3, "header.block_length(SBE_BLOCK_LENGTH);\n");
        indent(out, 3, "header.template_id(SBE_TEMPLATE_ID);\n");
        indent(out, 3, "header.schema_id(SBE_SCHEMA_ID);\n");
        indent(out, 3, "header.version(SBE_SCHEMA_VERSION);\n");
        indent(out, 3, "header\n");
        indent(out, 2, "}\n\n");
    }

    void appendMessageHeaderDecoderFn(final Appendable out) throws IOException
    {
        indent(out, 2, "pub fn header(self, mut header: MessageHeaderDecoder<ReadBuf<'a>>) -> Self {\n");
        indent(out, 3, "debug_assert_eq!(SBE_TEMPLATE_ID, header.template_id());\n");
        indent(out, 3, "let acting_block_length = header.block_length();\n");
        indent(out, 3, "let acting_version = header.version();\n\n");
        indent(out, 3, "self.wrap(\n");
        indent(out, 4, "header.parent().unwrap(),\n");
        indent(out, 4, "message_header_codec::ENCODED_LENGTH,\n");
        indent(out, 4, "acting_block_length,\n");
        indent(out, 4, "acting_version,\n");
        indent(out, 3, ")\n");
        indent(out, 2, "}\n\n");
    }

    void appendMessageStruct(final Appendable out, final String structName) throws IOException
    {
        indent(out, 1, "#[derive(Debug, Default)]\n");
        indent(out, 1, "pub struct %s {\n", withLifetime(structName));
        indent(out, 2, "buf: %s,\n", withLifetime(this.codecType.bufType()));
        indent(out, 2, "initial_offset: usize,\n");
        indent(out, 2, "offset: usize,\n");
        indent(out, 2, "limit: usize,\n");
        if (this.codecType == Decoder)
        {
            indent(out, 2, "pub acting_block_length: %s,\n", blockLengthType());
            indent(out, 2, "pub acting_version: %s,\n", schemaVersionType());
        }
        indent(out, 1, "}\n\n");
    }

    void appendWrapFn(final Appendable out) throws IOException
    {
        if (this.codecType == Decoder)
        {
            indent(out, 2, "pub fn wrap(\n");
            indent(out, 3, "mut self,\n");
            indent(out, 3, "buf: %s,\n", withLifetime(this.codecType.bufType()));
            indent(out, 3, "offset: usize,\n");
            indent(out, 3, "acting_block_length: %s,\n", blockLengthType());
            indent(out, 3, "acting_version: %s,\n", schemaVersionType());
            indent(out, 2, ") -> Self {\n");
            indent(out, 3, "let limit = offset + acting_block_length as usize;\n");
        }
        else
        {
            indent(out, 2, "pub fn wrap(mut self, buf: %s, offset: usize) -> Self {\n",
                withLifetime(this.codecType.bufType()));
            indent(out, 3, "let limit = offset + SBE_BLOCK_LENGTH as usize;\n");
        }

        indent(out, 3, "self.buf = buf;\n");
        indent(out, 3, "self.initial_offset = offset;\n");
        indent(out, 3, "self.offset = offset;\n");
        indent(out, 3, "self.limit = limit;\n");
        if (this.codecType == Decoder)
        {
            indent(out, 3, "self.acting_block_length = acting_block_length;\n");
            indent(out, 3, "self.acting_version = acting_version;\n");
        }
        indent(out, 3, "self\n");
        indent(out, 2, "}\n\n");
    }

    static void generateEncoder(
        final Ir ir,
        final Writer out,
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData) throws IOException
    {
        final MessageCoderDef coderDef = new MessageCoderDef(ir, msgToken, Encoder);
        coderDef.generate(fields, groups, varData);
        coderDef.appendTo(out);
    }

    static void generateDecoder(
        final Ir ir,
        final Writer out,
        final Token msgToken,
        final List<Token> fields,
        final List<Token> groups,
        final List<Token> varData) throws IOException
    {
        final MessageCoderDef coderDef = new MessageCoderDef(ir, msgToken, Decoder);
        coderDef.generate(fields, groups, varData);
        coderDef.appendTo(out);
    }

    static void generateCompositeStruct(
            final List<Token> tokens,
            final String structName,
            final Writer out
    ) throws IOException
    {
        // define struct...
        indent(out, 0, "#[derive(Debug, Default)]\n");
        indent(out, 0, "#[pyclass]\n");
        indent(out, 0, "pub struct %s {\n", structName);
        for (int i = 1, end = tokens.size() - 1; i < end; ) {
            final Token encodingToken = tokens.get(i);
            final StringBuilder sb = new StringBuilder();

//            System.out.println(format("Token '%s': Signal = %s", encodingToken.name(), encodingToken.signal().name()));
            switch (encodingToken.signal()) {
                case ENCODING -> generatePrimitiveField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_ENUM -> generateEnumField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_SET -> generateBitSetField(sb, 1, encodingToken, encodingToken.name());
                case BEGIN_COMPOSITE -> generateCompositeField(sb, 1, encodingToken, encodingToken.name());
                default -> {
                }
            }

            out.append(sb);
            i += encodingToken.componentTokenCount();
        }
        indent(out, 0, "}\n\n");

        // IMPL BLOCK
        indent(out, 0, "#[pymethods]\n");
        indent(out, 0, "impl %s {\n", structName);

        // new
        indent(out, 1, "#[new]\n");
        indent(out, 1, "pub fn new(\n");
        for (int i = 1, end = tokens.size() - 1; i < end; ) {
            final Token encodingToken = tokens.get(i);
            switch (encodingToken.signal()) {
                case ENCODING -> {
                    if (encodingToken.arrayLength() > 1)
                        indent(out, 2, "%s: [%s; %d],\n", toLowerSnakeCase(encodingToken.name()), rustTypeName(encodingToken.encoding().primitiveType()), encodingToken.arrayLength());
                    else if (encodingToken.isConstantEncoding());
                    else
                        indent(out, 2, "%s: %s,\n", toLowerSnakeCase(encodingToken.name()), rustTypeName(encodingToken.encoding().primitiveType()));
                }
                case BEGIN_ENUM -> {
                    final String referencedName = encodingToken.referencedName();
                    final String enumType = formatStructName(referencedName == null ? encodingToken.name() : referencedName);
                    indent(out, 2, "%s: %s,\n", formatFunctionName(encodingToken.name()), enumType);
                }
                case BEGIN_SET -> {
                    final String structTypeName = formatStructName(encodingToken.applicableTypeName());
                    indent(out, 2, "%s: %s,\n", toLowerSnakeCase(encodingToken.name()), structTypeName);
                }
                case BEGIN_COMPOSITE -> {
                    final String structTypeName = formatStructName(encodingToken.name());
                    indent(out, 2, "%s: %s,\n", toLowerSnakeCase(encodingToken.name()), structTypeName);
                }
                default -> {}
            }
            i += encodingToken.componentTokenCount();
        }
        indent(out, 1, ") -> Self {\n");
        indent(out, 2, "Self {\n");
        for (int i = 1, end = tokens.size() - 1; i < end; ) {
            final Token encodingToken = tokens.get(i);
            if (!encodingToken.isConstantEncoding())
                indent(out, 3, "%s,\n", toLowerSnakeCase(encodingToken.name()));
            i += encodingToken.componentTokenCount();
        }
        indent(out, 2, "}\n");
        indent(out, 1, "}\n\n");

        // from_buf
        indent(out, 1, "#[classmethod]\n");
        indent(out, 1, "pub fn from_buf(_cls: &PyType, buf: &[u8], offset: usize) -> Self {\n", structName);
        indent(out, 2, "let read_buf = ReadBuf::new(buf);\n");
        indent(out, 2, "let decoder = %s::default().wrap(read_buf, offset);\n\n", decoderName(structName));
        indent(out, 2, "Self {\n");
        for (int i = 1, end = tokens.size() - 1; i < end; ) {
            final Token encodingToken = tokens.get(i);
            if (!encodingToken.isConstantEncoding())
                indent(out, 3, "%s: decoder.%1$s(),\n", formatFunctionName(encodingToken.name()));
            i += encodingToken.componentTokenCount();
        }
        indent(out, 2, "}\n");
        indent(out, 1, "}\n\n");

        // to_buf
        indent(out, 1, "pub fn to_buf(&self) -> Vec<u8> {\n", structName);
        indent(out, 2, "let mut buf = vec![0u8; ENCODED_LENGTH];\n");
        indent(out, 2, "let write_buf = WriteBuf::new(&mut buf);\n");
        indent(out, 2, "let mut encoder = %s::default().wrap(write_buf, 0);\n", encoderName(structName));
        indent(out, 2, "self.write_to_encoder(encoder);\n\n", encoderName(structName));
        indent(out, 2, "buf\n");
        indent(out, 1, "}\n");
        indent(out, 0, "}\n\n");


        // RUST IMPL
        indent(out, 0, "impl %s {\n", structName);

        // write_to_encoder
        indent(out, 1, "pub fn write_to_encoder<P>(&self, mut encoder: %s<P>) {\n", encoderName(structName));

        for (int i = 1, end = tokens.size() - 1; i < end; ) {
            final Token encodingToken = tokens.get(i);
            if (encodingToken.isConstantEncoding())
            {
                i += encodingToken.componentTokenCount();
                continue;
            }

            if (Objects.requireNonNull(encodingToken.signal()) == Signal.BEGIN_COMPOSITE)
                indent(out, 2, "self.%s.write_to_encoder(encoder.%1$s_encoder());\n", formatFunctionName(encodingToken.name()));
            else
                indent(out, 2, "encoder.%s(self.%1$s);\n", formatFunctionName(encodingToken.name()));

            i += encodingToken.componentTokenCount();
        }
        indent(out, 1, "}\n");
        indent(out, 0, "}\n\n");
    }
}
