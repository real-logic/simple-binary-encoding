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
package uk.co.real_logic.sbe.generation.rust;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.NamedToken;
import uk.co.real_logic.sbe.ir.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.*;
import static uk.co.real_logic.sbe.ir.Signal.*;

public class RustGenerator implements CodeGenerator
{
    private final Ir ir;
    private final OutputManager outputManager;
    static final String SCRATCH_ENCODER_TYPE = "ScratchEncoderData";
    static final String SCRATCH_ENCODER_PROPERTY = "scratch";
    static final String SCRATCH_DECODER_PROPERTY = SCRATCH_ENCODER_PROPERTY;
    static final String SCRATCH_DECODER_TYPE = "ScratchDecoderData";
    static final String DATA_LIFETIME = "'d";

    public RustGenerator(final Ir ir, final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generate() throws IOException
    {
        final int headerSize = totalByteSize(ir.headerStructure());

        generateSharedImports(outputManager);
        generateResultEnums(outputManager);
        generateDecoderScratchStruct(outputManager);
        generateEncoderScratchStruct(outputManager);
        generateEitherEnum(outputManager);
        generateEnums(ir, outputManager);
        generateComposites(ir, outputManager);
        generateBitSets(ir, outputManager);
        generateMessageHeaderDecoder(outputManager, headerSize);

        for (final List<Token> tokens : ir.messages())
        {
            final MessageComponents components = MessageComponents.collectMessageComponents(tokens);
            final String messageTypeName = formatTypeName(components.messageToken.name());

            final RustStruct fieldStruct = generateMessageFieldStruct(messageTypeName, components, outputManager);
            generateMessageHeaderDefault(ir, outputManager, components.messageToken);

            // Avoid the work of recomputing the group tree twice per message
            final List<GroupTreeNode> groupTree = buildGroupTrees(messageTypeName, components.groups);
            generateGroupFieldRepresentations(outputManager, groupTree);

            generateMessageDecoder(outputManager, components, groupTree, fieldStruct, headerSize);
            generateMessageEncoder(outputManager, components, groupTree, fieldStruct, headerSize);
        }
    }

    private static int totalByteSize(final HeaderStructure headerStructure)
    {
        return headerStructure
            .tokens()
            .stream()
            .filter((t) -> t.signal() == ENCODING || t.signal() == BEGIN_ENUM || t.signal() == BEGIN_SET)
            .mapToInt(Token::encodedLength)
            .sum();
    }

    private void generateGroupFieldRepresentations(
        final OutputManager outputManager, final List<GroupTreeNode> groupTree) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Group fixed-field member representations"))
        {
            generateGroupFieldRepresentations(writer, groupTree);
        }
    }

    private void generateGroupFieldRepresentations(
        final Appendable appendable, final List<GroupTreeNode> groupTree) throws IOException
    {
        for (final GroupTreeNode node : groupTree)
        {
            final RustStruct struct = RustStruct.fromTokens(node.contextualName + "Member",
                node.simpleNamedFields,
                EnumSet.of(RustStruct.Modifier.PACKED, RustStruct.Modifier.DEFAULT));
            struct.appendDefinitionTo(appendable);

            generateConstantAccessorImpl(appendable, node.contextualName + "Member", node.rawFields);

            generateGroupFieldRepresentations(appendable, node.groups);
        }
    }

    private static RustStruct generateMessageFieldStruct(
        final String messageTypeName,
        final MessageComponents components,
        final OutputManager outputManager) throws IOException
    {
        final List<NamedToken> namedFieldTokens = NamedToken.gatherNamedNonConstantFieldTokens(components.fields);

        final String representationStruct = messageTypeName + "Fields";
        final RustStruct struct = RustStruct.fromTokens(representationStruct, namedFieldTokens,
            EnumSet.of(RustStruct.Modifier.PACKED, RustStruct.Modifier.DEFAULT));

        try (Writer writer = outputManager.createOutput(
            messageTypeName + " Fixed-size Fields (" + struct.sizeBytes() + " bytes)"))
        {
            struct.appendDefinitionTo(writer);
            writer.append("\n");
            generateConstantAccessorImpl(writer, representationStruct, components.fields);
        }

        return struct;
    }

    private static void generateBitSets(final Ir ir, final OutputManager outputManager) throws IOException
    {
        for (final List<Token> tokens : ir.types())
        {
            if (!tokens.isEmpty() && tokens.get(0).signal() == BEGIN_SET)
            {
                generateSingleBitSet(tokens, outputManager);
            }
        }
    }

    private static void generateSingleBitSet(final List<Token> tokens, final OutputManager outputManager)
        throws IOException
    {
        final Token beginToken = tokens.get(0);
        final String setType = formatTypeName(beginToken.applicableTypeName());
        try (Writer writer = outputManager.createOutput(setType + " bit set"))
        {
            writer.append("#[derive(Default)]\n");
            writer.append("#[repr(C,packed)]\n");
            final String rustPrimitiveType = rustTypeName(beginToken.encoding().primitiveType());
            writer.append(format("pub struct %s(pub %s);\n", setType, rustPrimitiveType));
            writer.append(format("impl %s {\n", setType));
            indent(writer, 1, "pub fn new() -> Self {\n");
            indent(writer, 2, "%s(0)\n", setType);
            indent(writer, 1, "}\n");

            indent(writer, 1, "pub fn clear(&mut self) -> &mut Self {\n");
            indent(writer, 2, "self.0 = 0;\n");
            indent(writer, 2, "self\n");
            indent(writer, 1, "}\n");

            for (final Token token : tokens)
            {
                if (Signal.CHOICE != token.signal())
                {
                    continue;
                }

                final String choiceName = formatMethodName(token.name());
                final Encoding encoding = token.encoding();
                final String choiceBitIndex = encoding.constValue().toString();

                indent(writer, 1, "pub fn get_%s(&self) -> bool {\n", choiceName);
                indent(writer, 2, "0 != self.0 & (1 << %s)\n", choiceBitIndex);
                indent(writer, 1, "}\n");

                indent(writer, 1, "pub fn set_%s(&mut self, value: bool) -> &mut Self {\n", choiceName);
                indent(writer, 2, "self.0 = if value {\n");
                indent(writer, 3, "self.0 | (1 << %s)\n", choiceBitIndex);
                indent(writer, 2, "} else {\n");
                indent(writer, 3, "self.0 & !(1 << %s)\n", choiceBitIndex);
                indent(writer, 2, "};\n");
                indent(writer, 2, "self\n");
                indent(writer, 1, "}\n");
            }

            writer.append("}\n");
        }

        try (Writer writer = outputManager.createOutput(setType + " bit set debug"))
        {
            indent(writer, 0, "impl core::fmt::Debug for %s {\n", setType);
            indent(writer, 1, "fn fmt(&self, fmt: &mut core::fmt::Formatter) -> core::fmt::Result {\n");
            indent(writer, 2, "write!(fmt, \"%s[", setType);

            final StringBuilder builder = new StringBuilder();
            final StringBuilder arguments = new StringBuilder();
            for (final Token token : tokens)
            {
                if (Signal.CHOICE != token.signal())
                {
                    continue;
                }

                final String choiceName = formatMethodName(token.name());
                final String choiceBitIndex = token.encoding().constValue().toString();

                builder.append(choiceName).append("(").append(choiceBitIndex).append(")={},");
                arguments.append("self.get_").append(choiceName).append("(),");
            }

            writer.append(builder.toString()).append("]\",\n");
            indent(writer, 3, arguments.toString() + ")\n");
            indent(writer, 1, "}\n");
            writer.append("}\n");
        }
    }

    private static void generateMessageEncoder(
        final OutputManager outputManager,
        final MessageComponents components,
        final List<GroupTreeNode> groupTree,
        final RustStruct fieldStruct,
        final int headerSize)
        throws IOException
    {
        final Token msgToken = components.messageToken;
        final String messageTypeName = formatTypeName(msgToken.name());
        final int msgLen = msgToken.encodedLength();
        final RustCodecType codecType = RustCodecType.Encoder;
        String topType = codecType.generateDoneCoderType(outputManager, messageTypeName);
        topType = generateTopVarDataCoders(messageTypeName, components.varData, outputManager, topType, codecType);
        topType = generateGroupsCoders(groupTree, outputManager, topType, codecType);
        topType = generateFixedFieldCoder(messageTypeName, msgLen, outputManager, topType, fieldStruct, codecType);
        topType = codecType.generateMessageHeaderCoder(messageTypeName, outputManager, topType, headerSize);
        generateEntryPoint(messageTypeName, outputManager, topType, codecType);
    }

    private static void generateMessageDecoder(
        final OutputManager outputManager,
        final MessageComponents components,
        final List<GroupTreeNode> groupTree,
        final RustStruct fieldStruct,
        final int headerSize)
        throws IOException
    {
        final Token msgToken = components.messageToken;
        final String messageTypeName = formatTypeName(msgToken.name());
        final int msgLen = msgToken.encodedLength();
        final RustCodecType codecType = RustCodecType.Decoder;
        String topType = codecType.generateDoneCoderType(outputManager, messageTypeName);
        topType = generateTopVarDataCoders(messageTypeName, components.varData, outputManager, topType, codecType);
        topType = generateGroupsCoders(groupTree, outputManager, topType, codecType);
        topType = generateFixedFieldCoder(messageTypeName, msgLen, outputManager, topType, fieldStruct, codecType);
        topType = codecType.generateMessageHeaderCoder(messageTypeName, outputManager, topType, headerSize);
        generateEntryPoint(messageTypeName, outputManager, topType, codecType);
    }

    private static void generateEntryPoint(
        final String messageTypeName,
        final OutputManager outputManager,
        final String topType,
        final RustCodecType codecType) throws IOException
    {
        try (Writer writer = outputManager.createOutput(messageTypeName + format(" %s entry point", codecType.name())))
        {
            final String gerund = codecType.gerund();
            writer.append(format("pub fn start_%s_%s<%s>(data: &%s%s [u8]) -> %s {\n", gerund,
                formatMethodName(messageTypeName), DATA_LIFETIME, DATA_LIFETIME,
                codecType == RustCodecType.Encoder ? " mut" : "",
                withLifetime(topType)));
            indent(writer, 1, "%s::wrap(%s { data: data, pos: 0 })\n",
                topType, codecType.scratchType());
            writer.append("}\n");
        }
    }

    static void generateMessageHeaderDecoder(
        final OutputManager outputManager,
        final int headerSize) throws IOException
    {
        final String messageTypeName = "MessageHeader";
        final RustCodecType codecType = RustCodecType.Decoder;
        try (Writer writer = outputManager.createOutput(messageTypeName + format(" %s entry point", codecType.name())))
        {
            final String gerund = codecType.gerund();
            writer.append(format("pub fn start_%s_%s<%s>(data: &%s%s [u8]) -> CodecResult<(&%s %s, %s)> {\n", gerund,
                formatMethodName(messageTypeName), DATA_LIFETIME, DATA_LIFETIME,
                codecType == RustCodecType.Encoder ? " mut" : "",
                DATA_LIFETIME, messageTypeName,
                withLifetime(codecType.scratchType())));
            indent(writer, 1, format("let mut scratch = %s { data: data, pos: 0 };\n", codecType.scratchType()));
            indent(writer, 1, format("let v = scratch.read_type::<%s>(%s)?;\n", messageTypeName, headerSize));
            indent(writer, 1, "Ok((v, scratch))\n");
            writer.append("}\n");
        }
    }

    static String withLifetime(final String typeName)
    {
        return format("%s<%s>", typeName, DATA_LIFETIME);
    }

    private static String generateFixedFieldCoder(
        final String messageTypeName,
        final int messageEncodedLength,
        final OutputManager outputManager,
        final String topType,
        final RustStruct fieldStruct,
        final RustCodecType codecType) throws IOException
    {
        try (Writer writer = outputManager.createOutput(messageTypeName + " Fixed fields " + codecType.name()))
        {
            final String decoderName = fieldStruct.name + codecType.name();
            codecType.appendScratchWrappingStruct(writer, decoderName);
            appendImplWithLifetimeHeader(writer, decoderName);
            codecType.appendWrapMethod(writer, decoderName);
            codecType.appendDirectCodeMethods(writer, formatMethodName(messageTypeName) + "_fields",
                fieldStruct.name, topType, fieldStruct.sizeBytes(),
                messageEncodedLength - fieldStruct.sizeBytes());
            writer.append("}\n");

            return decoderName;
        }
    }

    private static String generateGroupsCoders(
        final List<GroupTreeNode> groupTreeNodes,
        final OutputManager outputManager,
        final String initialNextCoderType,
        final RustCodecType codecType) throws IOException
    {
        String nextCoderType = initialNextCoderType;
        for (int i = groupTreeNodes.size() - 1; i >= 0; i--)
        {
            if (codecType == RustCodecType.Decoder)
            {
                nextCoderType = generateGroupNodeDecoders(outputManager, nextCoderType, groupTreeNodes.get(i), false);
            }
            else if (codecType == RustCodecType.Encoder)
            {
                nextCoderType = generateGroupNodeEncoders(outputManager, nextCoderType, groupTreeNodes.get(i), false);
            }
            else
            {
                throw new IllegalArgumentException(format("Unknown CodecType %s", codecType));
            }
        }

        return nextCoderType;
    }

    private static String generateGroupNodeEncoders(
        final OutputManager outputManager,
        final String afterGroupCoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent) throws IOException
    {
        final boolean hasParent = node.parent.isPresent();
        if (!hasParent && atEndOfParent)
        {
            throw new IllegalArgumentException("Group cannot both lack a parent and be at the end of a parent group");
        }

        boolean atEndOfCurrentLevel = true;
        // TODO - either make this CodecType a param or collapse it
        final RustCodecType codecType = RustCodecType.Encoder;
        final String memberCoderType = node.contextualName + "Member" + codecType.name();
        String nextCoderType = memberCoderType;

        if (!node.varData.isEmpty())
        {
            for (final VarDataSummary varDataSummary : reversedList(node.varData))
            {
                nextCoderType = varDataSummary.generateVarDataEncoder(
                    node.contextualName, memberCoderType, node.depth() + 1,
                    atEndOfCurrentLevel, outputManager, nextCoderType);
                atEndOfCurrentLevel = false;
            }
        }

        for (final GroupTreeNode childNode : reversedList(node.groups))
        {
            nextCoderType = generateGroupNodeEncoders(outputManager, nextCoderType, childNode, atEndOfCurrentLevel);
            atEndOfCurrentLevel = false;
        }

        return writeGroupEncoderTopTypes(
            outputManager,
            afterGroupCoderType,
            node,
            atEndOfParent,
            atEndOfCurrentLevel,
            memberCoderType,
            nextCoderType);
    }

    private static String generateGroupNodeDecoders(
        final OutputManager outputManager,
        final String initialNextDecoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent) throws IOException
    {
        final boolean hasParent = node.parent.isPresent();
        if (!hasParent && atEndOfParent)
        {
            throw new IllegalArgumentException("Group cannot both lack a parent and be at the end of a parent group");
        }

        boolean atEndOfCurrentLevel = true;
        final String memberDecoderType = node.contextualName + "MemberDecoder";
        final String headerDecoderType = node.contextualName + "HeaderDecoder";
        final String groupLevelNextDecoderType =
            format("Either<%s, %s>", withLifetime(memberDecoderType), initialNextDecoderType.startsWith("Either") ?
            initialNextDecoderType : withLifetime(initialNextDecoderType));
        String nextDecoderType = groupLevelNextDecoderType;

        if (!node.varData.isEmpty())
        {
            for (final VarDataSummary varDataSummary : reversedList(node.varData))
            {
                nextDecoderType = varDataSummary.generateVarDataDecoder(
                    node.contextualName, memberDecoderType, node.depth() + 1,
                    atEndOfCurrentLevel, outputManager, nextDecoderType);
                atEndOfCurrentLevel = false;
            }
        }

        for (final GroupTreeNode childNode : reversedList(node.groups))
        {
            nextDecoderType = generateGroupNodeDecoders(outputManager, nextDecoderType, childNode, atEndOfCurrentLevel);
            atEndOfCurrentLevel = false;
        }

        writeGroupDecoderTopTypes(outputManager, initialNextDecoderType, node, atEndOfParent,
            atEndOfCurrentLevel, memberDecoderType, headerDecoderType, groupLevelNextDecoderType, nextDecoderType);

        return headerDecoderType;
    }

    private static String writeGroupEncoderTopTypes(
        final OutputManager outputManager,
        final String afterGroupCoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent,
        final boolean atEndOfCurrentLevel,
        final String memberCoderType,
        final String nextCoderType) throws IOException
    {
        final String headerCoderType = node.contextualName + "HeaderEncoder";
        try (Writer out = outputManager.createOutput(node.contextualName + " Encoder for fields and header"))
        {
            appendStructHeader(out, withLifetime(memberCoderType));
            final String rustCountType = rustTypeName(node.dimensionsNumInGroupType());
            final String contentProperty;
            final String contentBearingType;
            if (node.parent.isPresent())
            {
                contentProperty = "parent";
                contentBearingType = withLifetime(node.parent.get().contextualName + "MemberEncoder");
            }
            else
            {
                contentProperty = SCRATCH_ENCODER_PROPERTY;
                contentBearingType = withLifetime(SCRATCH_ENCODER_TYPE);
            }
            indent(out, 1, "%s: %s,\n", contentProperty, contentBearingType);
            indent(out).append("count_write_pos: usize,\n");
            indent(out, 1, "count: %s,\n", rustCountType);
            out.append("}\n\n");

            appendImplWithLifetimeHeader(out, memberCoderType);

            indent(out).append("#[inline]\n");
            indent(out, 1, "fn new(%s: %s, count_write_pos: usize) -> Self {\n", contentProperty, contentBearingType);
            indent(out, 2).append(memberCoderType).append(" {\n");
            indent(out, 3, "%s: %s,\n", contentProperty, contentProperty);
            indent(out, 3).append("count_write_pos: count_write_pos,\n");
            indent(out, 3).append("count: 0,\n");
            indent(out, 2).append("}\n").append(INDENT).append("}\n\n");

            indent(out).append("#[inline]\n");
            final String fieldsType = node.contextualName + "Member";
            indent(out, 1, "pub fn next_%s_member(mut self, fields: &%s)",
                formatMethodName(node.originalName), fieldsType);
            out.append(format(" -> CodecResult<%s> {\n", withLifetime(nextCoderType)));
            final String scratchChain = toScratchChain(node);
            indent(out, 2, "%s.write_type::<%s>(fields, %s)?; // block length\n",
                scratchChain, fieldsType, node.blockLength);
            indent(out, 2).append("self.count += 1;\n");
            indent(out, 2, "Ok(%s)\n", atEndOfCurrentLevel ? "self" : format("%s::wrap(self)", nextCoderType));
            indent(out).append("}\n");

            indent(out).append("#[inline]\n");
            indent(out, 1, "pub fn done_with_%s(mut self) -> CodecResult<%s> {\n",
                formatMethodName(node.originalName), withLifetime(afterGroupCoderType));
            indent(out, 2, "%s.write_at_position::<%s>(self.count_write_pos, &self.count, %s)?;\n",
                scratchChain, rustCountType, node.dimensionsNumInGroupType().size());
            indent(out, 2, "Ok(%s)\n", atEndOfParent ? "self.parent" :
                format("%s::wrap(self.%s)", afterGroupCoderType, contentProperty));
            indent(out).append("}\n").append("}\n");

            appendStructHeader(out, withLifetime(headerCoderType));
            indent(out, 1, "%s: %s,\n", contentProperty, contentBearingType);
            out.append("}\n");

            // Header impl
            appendImplWithLifetimeHeader(out, headerCoderType);
            indent(out).append("#[inline]\n");
            indent(out, 1, "fn wrap(%s: %s) -> Self {\n", contentProperty, contentBearingType);
            indent(out, 2, "%s { %s: %s }\n", headerCoderType, contentProperty, contentProperty)
                .append(INDENT).append("}\n");

            indent(out).append("#[inline]\n");
            indent(out, 1, "pub fn %s_individually(mut self) -> CodecResult<%s> {\n",
                formatMethodName(node.originalName), withLifetime(memberCoderType));
            indent(out, 2, "%s.write_type::<%s>(&%s, %s)?; // block length\n",
                scratchChain, rustTypeName(node.dimensionsBlockLengthType()),
                generateRustLiteral(node.dimensionsBlockLengthType(), Integer.toString(node.blockLength)),
                node.dimensionsBlockLengthType().size());
            indent(out, 2, "let count_pos = %s.pos;\n", scratchChain);
            indent(out, 2, "%s.write_type::<%s>(&0, %s)?; // preliminary group member count\n",
                scratchChain, rustCountType, node.dimensionsNumInGroupType().size());
            indent(out, 2, "Ok(%s::new(self.%s, count_pos))\n", memberCoderType, contentProperty);
            indent(out, 1).append("}\n");

            if (node.hasFixedSizeMembers())
            {
                appendFixedSizeMemberGroupEncoderMethods(
                    afterGroupCoderType, node, atEndOfParent, out,
                    rustCountType, contentProperty, fieldsType, scratchChain);
            }

            out.append("}\n");
        }

        return headerCoderType;
    }

    private static void appendFixedSizeMemberGroupEncoderMethods(
        final String afterGroupCoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent,
        final Writer out,
        final String rustCountType,
        final String contentProperty,
        final String fieldsType,
        final String scratchChain) throws IOException
    {
        final String s = atEndOfParent ?
            "self.parent" : format("%s::wrap(self.%s)", afterGroupCoderType, contentProperty);

        indent(out).append("#[inline]\n");
        indent(out, 1, "pub fn %s_as_slice(mut self, count: %s) -> CodecResult<(&%s mut [%s], %s)> {\n",
            formatMethodName(node.originalName), rustCountType, DATA_LIFETIME, fieldsType,
            withLifetime(afterGroupCoderType));
        indent(out, 2, "%s.write_type::<%s>(&%s, %s)?; // block length\n",
            scratchChain, rustTypeName(node.dimensionsBlockLengthType()),
            generateRustLiteral(node.dimensionsBlockLengthType(), Integer.toString(node.blockLength)),
            node.dimensionsBlockLengthType().size());
        indent(out, 2, "%s.write_type::<%s>(&count, %s)?; // group count\n",
            scratchChain, rustCountType, node.dimensionsNumInGroupType().size());
        indent(out, 2, "let c = count as usize;\n");
        indent(out, 2, "let group_slice = %s.writable_slice::<%s>(c, %s)?;\n",
            scratchChain, fieldsType, node.blockLength);

        indent(out, 2, "Ok((group_slice, %s))\n", s);
        indent(out, 1).append("}\n");

        indent(out).append("#[inline]\n");
        indent(out, 1, "pub fn %s_from_slice(mut self, s: &[%s]) -> CodecResult<%s> {\n",
            formatMethodName(node.originalName), fieldsType,
            withLifetime(afterGroupCoderType));
        indent(out, 2, "%s.write_type::<%s>(&%s, %s)?; // block length\n",
            scratchChain, rustTypeName(node.dimensionsBlockLengthType()),
            generateRustLiteral(node.dimensionsBlockLengthType(), Integer.toString(node.blockLength)),
            node.dimensionsBlockLengthType().size());
        indent(out, 2, "let count = s.len();\n");
        indent(out, 2, "if count > %s {\n", node.dimensionsNumInGroupType().maxValue());
        indent(out, 3).append("return Err(CodecErr::SliceIsLongerThanAllowedBySchema)\n");
        indent(out, 2).append("}\n");
        indent(out, 2, "%s.write_type::<%s>(&(count as %s), %s)?; // group count\n",
            scratchChain, rustCountType, rustCountType, node.dimensionsNumInGroupType().size());
        indent(out, 2, "%s.write_slice_without_count::<%s>(s, %s)?;\n",
            scratchChain, fieldsType, node.blockLength);
        indent(out, 2, "Ok(%s)\n", s);
        indent(out, 1).append("}\n");
    }

    private static void writeGroupDecoderTopTypes(
        final OutputManager outputManager,
        final String initialNextDecoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent,
        final boolean atEndOfCurrentLevel,
        final String memberDecoderType,
        final String headerDecoderType,
        final String groupLevelNextDecoderType,
        final String nextDecoderType) throws IOException
    {
        try (Writer out = outputManager.createOutput(node.contextualName + " Decoder for fields and header"))
        {
            appendStructHeader(out, withLifetime(memberDecoderType));
            final String rustCountType = rustTypeName(node.dimensionsNumInGroupType());
            final String contentProperty;
            final String contentBearingType;
            if (node.parent.isPresent())
            {
                contentProperty = "parent";
                contentBearingType = withLifetime(node.parent.get().contextualName + "MemberDecoder");
            }
            else
            {
                contentProperty = SCRATCH_DECODER_PROPERTY;
                contentBearingType = withLifetime(SCRATCH_DECODER_TYPE);
            }
            indent(out, 1, "%s: %s,\n", contentProperty, contentBearingType);
            indent(out, 1, "max_index: %s,\n", rustCountType);
            indent(out, 1, "index: %s,\n", rustCountType);
            out.append("}\n\n");

            appendImplWithLifetimeHeader(out, memberDecoderType);

            indent(out, 1, "fn new(%s: %s, count: %s) -> Self {\n",
                contentProperty, contentBearingType, rustCountType);
            indent(out, 2, "assert!(count > 0%s);\n", rustCountType); // TODO - elide assert
            indent(out, 2).append(memberDecoderType).append(" {\n");
            indent(out, 3, "%s: %s,\n", contentProperty, contentProperty);
            indent(out, 3).append("max_index: count - 1,\n");
            indent(out, 3).append("index: 0,\n");
            indent(out, 2).append("}\n").append(INDENT).append("}\n\n");

            indent(out, 1, "pub fn next_%s_member(mut self)", formatMethodName(node.originalName));
            out.append(format(" -> CodecResult<(&%s %s, %s)> {\n", DATA_LIFETIME, node.contextualName + "Member",
                nextDecoderType.startsWith("Either") ? nextDecoderType : withLifetime(nextDecoderType)));
            // TODO - account for version mismatch by making use of previously read in-message blockLength
            indent(out, 2, "let v = %s.read_type::<%s>(%s)?;\n",
                toScratchChain(node), node.contextualName + "Member", node.blockLength);
            indent(out, 2).append("self.index += 1;\n");

            indent(out, 2, "Ok((v, %s))\n", atEndOfCurrentLevel ? "self.after_member()" :
                format("%s::wrap(self)", nextDecoderType));
            indent(out).append("}\n");

            indent(out).append("#[inline]\n");
            indent(out, 1, "fn after_member(self) -> %s {\n", groupLevelNextDecoderType);
            indent(out, 2).append("if self.index <= self.max_index {\n");
            indent(out, 3).append("Either::Left(self)\n");
            indent(out, 2).append("} else {\n").append(INDENT).append(INDENT).append(INDENT)
                .append(format("Either::Right(%s)\n", atEndOfParent ? "self.parent.after_member()" :
                format("%s::wrap(self.%s)", initialNextDecoderType, contentProperty)));
            indent(out, 2).append("}\n").append(INDENT).append("}\n").append("}\n");

            appendStructHeader(out, withLifetime(headerDecoderType));
            indent(out, 1, "%s: %s,\n", contentProperty, contentBearingType).append("}\n");

            appendImplWithLifetimeHeader(out, headerDecoderType);
            indent(out, 1, "fn wrap(%s: %s) -> Self {\n", contentProperty, contentBearingType);
            indent(out, 2, "%s { %s: %s }\n", headerDecoderType, contentProperty, contentProperty)
                .append(INDENT).append("}\n");

            indent(out, 1, "pub fn %s_individually(mut self) -> CodecResult<%s> {\n",
                formatMethodName(node.originalName), groupLevelNextDecoderType);
            indent(out, 2, "let dim = %s.read_type::<%s>(%s)?;\n",
                toScratchChain(node),
                formatTypeName(node.dimensionsTypeName()),
                node.dimensionsTypeSize());
            indent(out, 2).append("if dim.num_in_group > 0 {\n");
            indent(out, 3, "Ok(Either::Left(%s::new(self.%s, dim.num_in_group)))\n",
                memberDecoderType, contentProperty).append(INDENT).append(INDENT).append("} else {\n");

            if (atEndOfParent)
            {
                indent(out, 3).append("Ok(Either::Right(self.parent.after_member()))\n");
            }
            else
            {
                indent(out, 3, "Ok(Either::Right(%s::wrap(self.%s)))\n",
                    initialNextDecoderType, contentProperty);
            }
            indent(out, 2).append("}\n");
            indent(out, 1, "}\n");

            if (node.hasFixedSizeMembers())
            {
                appendFixedSizeMemberGroupDecoderMethods(
                    initialNextDecoderType, node, atEndOfParent, out, contentProperty);
            }

            out.append("}\n");
        }
    }

    private static void appendFixedSizeMemberGroupDecoderMethods(
        final String initialNextDecoderType,
        final GroupTreeNode node,
        final boolean atEndOfParent,
        final Writer out,
        final String contentProperty) throws IOException
    {
        indent(out, 1, "pub fn %s_as_slice(mut self) -> CodecResult<(&%s [%s], %s)> {\n",
            formatMethodName(node.originalName), DATA_LIFETIME, node.contextualName + "Member",
            initialNextDecoderType.startsWith("Either") ?
            initialNextDecoderType : withLifetime(initialNextDecoderType));
        indent(out, 2, "let dim = %s.read_type::<%s>(%s)?;\n",
            toScratchChain(node),
            formatTypeName(node.dimensionsTypeName()),
            node.dimensionsTypeSize());
        indent(out, 2, "let s = %s.read_slice::<%s>(dim.num_in_group as usize, %s)?;\n",
            toScratchChain(node), node.contextualName + "Member", node.blockLength);
        indent(out, 2, "Ok((s,%s))\n", atEndOfParent ? "self.parent.after_member()" :
            format("%s::wrap(self.%s)", initialNextDecoderType, contentProperty));
        indent(out, 1, "}\n");
    }

    private static String toScratchChain(final GroupTreeNode node)
    {
        final StringBuilder builder = new StringBuilder("self");
        Optional<GroupTreeNode> currentParent = node.parent;
        while (currentParent.isPresent())
        {
            builder.append(".parent");
            currentParent = currentParent.get().parent;
        }
        builder.append(".scratch");

        return builder.toString();
    }

    private static String toScratchChain(final int depth)
    {
        final StringBuilder builder = new StringBuilder("self");
        for (int i = 0; i < depth; i++)
        {
            builder.append(".parent");
        }
        builder.append(format(".%s", SCRATCH_DECODER_PROPERTY));

        return builder.toString();
    }

    private static <T> Iterable<T> reversedList(final List<T> list)
    {
        return
            () ->
            {
                if (list.isEmpty())
                {
                    return list.stream().iterator();
                }

                final int maxIndex = list.size() - 1;

                return IntStream.rangeClosed(0, maxIndex).mapToObj((i) -> list.get(maxIndex - i)).iterator();
            };
    }

    private static List<GroupTreeNode> buildGroupTrees(final String parentTypeName, final List<Token> groupsTokens)
    {
        return buildGroupTrees(parentTypeName, groupsTokens, Optional.empty());
    }

    private static List<GroupTreeNode> buildGroupTrees(
        final String parentTypeName,
        final List<Token> groupsTokens,
        final Optional<GroupTreeNode> parent)
    {
        final int size = groupsTokens.size();
        final List<GroupTreeNode> groups = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            final Token groupToken = groupsTokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            final String originalName = groupToken.name();
            final String contextualName = parentTypeName + formatTypeName(originalName);
            ++i;

            final Token dimensionsToken = groupsTokens.get(i);
            final int groupHeaderTokenCount = dimensionsToken.componentTokenCount();
            final List<Token> dimensionsTokens = groupsTokens.subList(i, i + groupHeaderTokenCount);
            final GroupDimensions dimensions = GroupDimensions.ofTokens(dimensionsTokens);
            final int blockLength = groupToken.encodedLength();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(groupsTokens, i, fields);

            final List<Token> childGroups = new ArrayList<>();
            i = collectGroups(groupsTokens, i, childGroups);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(groupsTokens, i, varData);
            final List<VarDataSummary> varDataSummaries = VarDataSummary.gatherVarDataSummaries(varData);

            final GroupTreeNode node = new GroupTreeNode(
                parent,
                originalName,
                contextualName,
                dimensions,
                blockLength,
                fields,
                varDataSummaries);

            groups.add(node);
            buildGroupTrees(contextualName, childGroups, Optional.of(node));
        }

        return groups;
    }

    private static PrimitiveType findPrimitiveByTokenName(final List<Token> tokens, final String targetName)
    {
        return findPrimitiveTokenByTokenName(tokens, targetName).encoding().primitiveType();
    }

    private static Token findPrimitiveTokenByTokenName(final List<Token> tokens, final String targetName)
    {
        for (final Token token : tokens)
        {
            if (targetName.equalsIgnoreCase(token.name()) &&
                token.encoding() != null &&
                token.encoding().primitiveType() != null)
            {
                return token;
            }
        }

        throw new IllegalStateException(format("%s not specified for group", targetName));
    }

    private static final class GroupDimensions
    {
        final String typeName;
        final int typeSize;
        final PrimitiveType numInGroupType;
        final PrimitiveType blockLengthType;

        private GroupDimensions(final String typeName, final int typeSize,
            final PrimitiveType numInGroupType, final PrimitiveType blockLengthType)
        {
            this.typeName = typeName;
            this.typeSize = typeSize;
            this.numInGroupType = numInGroupType;
            this.blockLengthType = blockLengthType;
        }

        public static GroupDimensions ofTokens(final List<Token> dimensionsTokens)
        {
            final PrimitiveType numInGroupType = findPrimitiveByTokenName(dimensionsTokens, "numInGroup");
            final PrimitiveType blockLengthType = findPrimitiveByTokenName(dimensionsTokens, "blockLength");
            return new GroupDimensions(dimensionsTokens.get(0).name(), dimensionsTokens.get(0).encodedLength(),
                    numInGroupType, blockLengthType);
        }
    }

    static class GroupTreeNode
    {
        final Optional<GroupTreeNode> parent;
        final String originalName;
        final String contextualName;
        final GroupDimensions dimensions;
        final int blockLength;
        final List<Token> rawFields;
        final List<NamedToken> simpleNamedFields;
        final List<GroupTreeNode> groups = new ArrayList<>();
        final List<VarDataSummary> varData;

        GroupTreeNode(
            final Optional<GroupTreeNode> parent,
            final String originalName,
            final String contextualName,
            final GroupDimensions dimensions,
            final int blockLength,
            final List<Token> fields,
            final List<VarDataSummary> varData)
        {
            this.parent = parent;
            this.originalName = originalName;
            this.contextualName = contextualName;
            this.dimensions = dimensions;
            this.blockLength = blockLength;
            this.rawFields = fields;
            this.simpleNamedFields = NamedToken.gatherNamedNonConstantFieldTokens(fields);
            this.varData = varData;

            parent.ifPresent((p) -> p.addChild(this));
        }

        void addChild(final GroupTreeNode child)
        {
            groups.add(child);
        }

        int depth()
        {
            Optional<GroupTreeNode> currentParent = this.parent;
            int d = 0;
            while (currentParent.isPresent())
            {
                d += 1;
                currentParent = currentParent.get().parent;
            }

            return d;
        }

        boolean hasFixedSizeMembers()
        {
            return groups.isEmpty() && varData.isEmpty();
        }

        public PrimitiveType dimensionsNumInGroupType()
        {
            return dimensions.numInGroupType;
        }

        public PrimitiveType dimensionsBlockLengthType()
        {
            return dimensions.blockLengthType;
        }

        public String dimensionsTypeName()
        {
            return dimensions.typeName;
        }

        public int dimensionsTypeSize()
        {
            return dimensions.typeSize;
        }
    }

    static class VarDataSummary
    {
        final String name;
        final PrimitiveType lengthType;
        final PrimitiveType dataType;

        VarDataSummary(final String name, final PrimitiveType lengthType, final PrimitiveType dataType)
        {
            this.name = name;
            this.lengthType = lengthType;
            this.dataType = dataType;
        }

        String generateVarDataEncoder(
            final String parentContextualName,
            final String contentType,
            final int groupDepth,
            final boolean atEndOfGroup,
            final OutputManager outputManager,
            final String nextCoderType) throws IOException
        {
            if (groupDepth <= 0 && atEndOfGroup)
            {
                throw new IllegalStateException("Cannot be both outside of any group and at the end of a group");
            }

            final RustCodecType codecType = RustCodecType.Encoder;
            final String decoderType = parentContextualName + formatTypeName(name) + codecType.name();
            try (Writer writer = outputManager.createOutput(name + " variable-length data"))
            {
                appendStructHeader(writer, withLifetime(decoderType));
                final String contentPropertyName = groupDepth > 0 ? "parent" : codecType.scratchProperty();
                indent(writer, 1, "%s: %s,\n", contentPropertyName, withLifetime(contentType));
                writer.append("}\n");

                appendImplWithLifetimeHeader(writer, decoderType);
                indent(writer, 1, "fn wrap(%s: %s) -> Self {\n",
                    contentPropertyName, withLifetime(contentType));
                indent(writer, 2, "%s { %s: %s }\n",
                    decoderType, contentPropertyName, contentPropertyName).append(INDENT).append("}\n");

                indent(writer, 1, "pub fn %s(mut self, s: &%s [%s]) -> CodecResult<%s> {\n",
                    formatMethodName(name), DATA_LIFETIME, rustTypeName(this.dataType),
                    atEndOfGroup ? nextCoderType : withLifetime(nextCoderType));

                indent(writer, 2).append("let l = s.len();\n");
                indent(writer, 2, "if l > %s {\n", this.lengthType.maxValue());
                indent(writer, 3).append("return Err(CodecErr::SliceIsLongerThanAllowedBySchema)\n");
                indent(writer, 2).append("}\n");
                indent(writer, 2).append("// Write data length\n");
                indent(writer, 2, "%s.write_type::<%s>(&(l as %s), %s)?; // group length\n",
                    toScratchChain(groupDepth), rustTypeName(this.lengthType), rustTypeName(this.lengthType),
                    this.lengthType.size());
                indent(writer, 2).append(format("%s.write_slice_without_count::<%s>(s, %s)?;\n",
                    toScratchChain(groupDepth), rustTypeName(this.dataType), this.dataType.size()));
                indent(writer, 2, "Ok(%s)\n", atEndOfGroup ? "self.parent" :
                    format("%s::wrap(self.%s)", nextCoderType, contentPropertyName));

                indent(writer).append("}\n}\n");
            }

            return decoderType;
        }

        String generateVarDataDecoder(
            final String parentContextualName,
            final String contentType,
            final int groupDepth,
            final boolean atEndOfGroup,
            final OutputManager outputManager,
            final String nextDecoderType) throws IOException
        {
            if (groupDepth <= 0 && atEndOfGroup)
            {
                throw new IllegalStateException("Cannot be both outside of any group and at the end of a group");
            }

            final String name = this.name;
            final String decoderType = parentContextualName + formatTypeName(name) + "Decoder";
            try (Writer writer = outputManager.createOutput(name + " variable-length data"))
            {
                appendStructHeader(writer, withLifetime(decoderType));
                final String contentPropertyName = groupDepth > 0 ? "parent" : SCRATCH_DECODER_PROPERTY;
                indent(writer, 1, "%s: %s,\n", contentPropertyName, withLifetime(contentType));
                writer.append("}\n");

                appendImplWithLifetimeHeader(writer, decoderType);
                indent(writer, 1, "fn wrap(%s: %s) -> Self {\n", contentPropertyName, withLifetime(contentType));
                indent(writer, 2, "%s { %s: %s }\n",
                    decoderType, contentPropertyName, contentPropertyName).append(INDENT).append("}\n");

                indent(writer, 1, "pub fn %s(mut self) -> CodecResult<(&%s [%s], %s)> {\n",
                    formatMethodName(name), DATA_LIFETIME, rustTypeName(this.dataType),
                    atEndOfGroup ? nextDecoderType : withLifetime(nextDecoderType));
                indent(writer, 2, "let count = *%s.read_type::<%s>(%s)?;\n",
                    toScratchChain(groupDepth), rustTypeName(this.lengthType), this.lengthType.size());

                final String goToNext;
                if (atEndOfGroup)
                {
                    goToNext = "self.parent.after_member()";
                }
                else
                {
                    goToNext = format("%s::wrap(self.%s)", nextDecoderType, contentPropertyName);
                }

                indent(writer, 2,
                    "Ok((%s.read_slice::<%s>(count as usize, %s)?, %s))\n", toScratchChain(groupDepth),
                    rustTypeName(this.dataType), this.dataType.size(), goToNext);
                indent(writer).append("}\n");

                writer.append("}\n");
            }

            return decoderType;
        }

        static List<VarDataSummary> gatherVarDataSummaries(final List<Token> tokens)
        {
            final List<VarDataSummary> summaries = new ArrayList<>();
            for (int i = 0; i < tokens.size(); i++)
            {
                final Token beginToken = tokens.get(i);
                if (beginToken.signal() != Signal.BEGIN_VAR_DATA)
                {
                    throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + beginToken);
                }

                ++i;
                final Token dimensionsToken = tokens.get(i);
                final int headerTokenCount = dimensionsToken.componentTokenCount();
                final List<Token> currentEncodingTokens = tokens.subList(i, i + headerTokenCount);
                final PrimitiveType lengthType = findPrimitiveByTokenName(currentEncodingTokens, "length");
                final PrimitiveType dataType = findPrimitiveByTokenName(currentEncodingTokens, "varData");
                summaries.add(new VarDataSummary(beginToken.name(), lengthType, dataType));
                i += headerTokenCount;
            }

            return summaries;
        }
    }

    static String generateTopVarDataCoders(
        final String messageTypeName,
        final List<Token> tokens,
        final OutputManager outputManager,
        final String initialNextType,
        final RustCodecType codecType)
        throws IOException
    {
        final List<VarDataSummary> summaries = VarDataSummary.gatherVarDataSummaries(tokens);

        String nextCoderType = initialNextType;
        for (final VarDataSummary summary : reversedList(summaries))
        {
            if (codecType == RustCodecType.Decoder)
            {
                nextCoderType = summary.generateVarDataDecoder(
                    messageTypeName, SCRATCH_DECODER_TYPE, 0, false, outputManager, nextCoderType);
            }
            else if (codecType == RustCodecType.Encoder)
            {
                nextCoderType = summary.generateVarDataEncoder(
                    messageTypeName, SCRATCH_ENCODER_TYPE, 0, false, outputManager, nextCoderType);
            }
            else
            {
                throw new IllegalArgumentException(format("Unknown RustCodecType %s", codecType));
            }
        }

        return nextCoderType;
    }

    static void appendImplWithLifetimeHeader(final Appendable appendable, final String typeName)
        throws IOException
    {
        appendable.append(format("impl<%s> %s<%s> {\n", DATA_LIFETIME, typeName, DATA_LIFETIME));
    }

    private static void generateEnums(final Ir ir, final OutputManager outputManager)
        throws IOException
    {
        final Set<String> enumTypeNames = new HashSet<>();
        for (final List<Token> tokens : ir.types())
        {
            if (tokens.isEmpty())
            {
                continue;
            }

            final Token beginToken = tokens.get(0);
            if (beginToken.signal() != BEGIN_ENUM)
            {
                continue;
            }

            final String typeName = beginToken.applicableTypeName();
            if (enumTypeNames.contains(typeName))
            {
                continue;
            }

            generateEnum(tokens, outputManager);
            enumTypeNames.add(typeName);
        }
    }

    static void generateSharedImports(final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Imports core rather than std to broaden usable environments."))
        {
            writer.append("extern crate core;\n");
        }
    }

    static void generateResultEnums(final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Result types for error handling"))
        {
            writer.append("\n/// Errors that may occur during the course of encoding or decoding.\n");
            writer.append("#[derive(Debug)]\n");
            writer.append("pub enum CodecErr {\n");
            indent(writer, 1, "/// Too few bytes in the byte-slice to read or write the data structure relevant\n");
            indent(writer, 1, "/// to the current state of the codec\n");
            indent(writer, 1, "NotEnoughBytes,\n\n");
            indent(writer, 1, "/// Groups and vardata are constrained by the numeric type chosen to represent their\n");
            indent(writer, 1, "/// length as well as optional maxima imposed by the schema\n");
            indent(writer, 1, "SliceIsLongerThanAllowedBySchema,\n");
            writer.append("}\n\n");
            writer.append("pub type CodecResult<T> = core::result::Result<T, CodecErr>;\n");
        }
    }

    static void generateEncoderScratchStruct(final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Scratch Encoder Data Wrapper - codec internal use only"))
        {
            writer.append("#[derive(Debug)]\n");
            writer.append(format("pub struct %s<%s> {\n", SCRATCH_ENCODER_TYPE, DATA_LIFETIME));
            indent(writer, 1, "data: &%s mut [u8],\n", DATA_LIFETIME);
            indent(writer).append("pos: usize,\n");
            writer.append("}\n");

            writer.append(format("%nimpl<%s> %s<%s> {\n", DATA_LIFETIME, SCRATCH_ENCODER_TYPE, DATA_LIFETIME));

            indent(writer, 1, "/// Copy the bytes of a value into the data buffer\n");
            indent(writer, 1, "/// Advances the `pos` index to after the newly-written bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer).append("fn write_type<T>(&mut self, t: & T, num_bytes: usize) -> " +
                "CodecResult<()> {\n");
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3).append("let source_bytes: &[u8] = unsafe {\n");
            indent(writer, 4).append("core::slice::from_raw_parts(t as *const T as *const u8, num_bytes)\n");
            indent(writer, 3).append("};\n");
            indent(writer, 3).append("(&mut self.data[self.pos..end]).copy_from_slice(source_bytes);\n");
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(())\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n\n");

            indent(writer, 1, "/// Advances the `pos` index by a set number of bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer).append("fn skip_bytes(&mut self, num_bytes: usize) -> CodecResult<()> {\n");
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(())\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n\n");

            indent(writer, 1, "/// Create a struct reference overlaid atop the data buffer\n");
            indent(writer, 1, "/// such that changes to the struct directly edit the buffer. \n");
            indent(writer, 1, "/// Note that the initial content of the struct's fields may be garbage.\n");
            indent(writer, 1, "/// Advances the `pos` index to after the newly-written bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer, 1, "fn writable_overlay<T>(&mut self, num_bytes: usize) " +
                "-> CodecResult<&%s mut T> {\n", DATA_LIFETIME);
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3, "let v: &%s mut T = unsafe {\n", DATA_LIFETIME);
            indent(writer, 4).append("let s = self.data.as_ptr().offset(self.pos as isize) as *mut T;\n");
            indent(writer, 4).append("&mut *s\n");
            indent(writer, 3).append("};\n");
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(v)\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n\n");

            indent(writer, 1, "/// Copy the bytes of a value into the data buffer at a specific position\n");
            indent(writer, 1, "/// Does **not** alter the `pos` index.\n");
            indent(writer).append("#[inline]\n");
            indent(writer).append("fn write_at_position<T>(&mut self, position: usize, t: & T, num_bytes: usize) -> " +
                "CodecResult<()> {\n");
            indent(writer, 2).append("let end = position + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3).append("let source_bytes: &[u8] = unsafe {\n");
            indent(writer, 4).append("core::slice::from_raw_parts(t as *const T as *const u8, num_bytes)\n");
            indent(writer, 3).append("};\n");
            indent(writer, 3).append("(&mut self.data[position..end]).copy_from_slice(source_bytes);\n");
            indent(writer, 3).append("Ok(())\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n");

            generateEncoderScratchSliceMethods(writer);

            writer.append("}\n");
        }
    }

    static void generateEncoderScratchSliceMethods(final Appendable writer) throws IOException
    {
        indent(writer, 1, "/// Create a mutable slice overlaid atop the data buffer directly\n");
        indent(writer, 1, "/// such that changes to the slice contents directly edit the buffer\n");
        indent(writer, 1, "/// Note that the initial content of the slice's members' fields may be garbage.\n");
        indent(writer, 1, "/// Advances the `pos` index to after the region representing the slice.\n");
        indent(writer).append("#[inline]\n");
        indent(writer, 1, "fn writable_slice<T>(&mut self, count: usize, bytes_per_item: usize) " +
            "-> CodecResult<&%s mut [T]> {\n", DATA_LIFETIME);
        indent(writer, 2).append("let end = self.pos + (count * bytes_per_item);\n");
        indent(writer, 2).append("if end <= self.data.len() {\n");
        indent(writer, 3, "let v: &%s mut [T] = unsafe {\n", DATA_LIFETIME);
        indent(writer, 4).append("core::slice::from_raw_parts_mut(" +
            "self.data[self.pos..end].as_mut_ptr() as *mut T, count)\n");
        indent(writer, 3).append("};\n");
        indent(writer, 3).append("self.pos = end;\n");
        indent(writer, 3).append("Ok(v)\n");
        indent(writer, 2).append("} else {\n");
        indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
        indent(writer, 2).append("}\n");
        indent(writer).append("}\n\n");

        indent(writer, 1, "/// Copy the raw bytes of a slice's contents into the data buffer\n");
        indent(writer, 1, "/// Does **not** encode the length of the slice explicitly into the buffer.\n");
        indent(writer, 1, "/// Advances the `pos` index to after the newly-written slice bytes.\n");
        indent(writer).append("#[inline]\n");
        indent(writer).append("fn write_slice_without_count<T>(&mut self, t: &[T], bytes_per_item: usize) -> " +
            "CodecResult<()> {\n");
        indent(writer, 2).append("let content_bytes_size = bytes_per_item * t.len();\n");
        indent(writer, 2).append("let end = self.pos + content_bytes_size;\n");
        indent(writer, 2).append("if end <= self.data.len() {\n");
        indent(writer, 3).append("let source_bytes: &[u8] = unsafe {\n");
        indent(writer, 4).append("core::slice::from_raw_parts(t.as_ptr() as *const u8, content_bytes_size)\n");
        indent(writer, 3).append("};\n");
        indent(writer, 3).append("(&mut self.data[self.pos..end]).copy_from_slice(source_bytes);\n");
        indent(writer, 3).append("self.pos = end;\n");
        indent(writer, 3).append("Ok(())\n");
        indent(writer, 2).append("} else {\n");
        indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
        indent(writer, 2).append("}\n");
        indent(writer).append("}\n");
    }

    private static void generateDecoderScratchStruct(final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Scratch Decoder Data Wrapper - codec internal use only"))
        {
            writer.append("#[derive(Debug)]\n");
            writer.append(format("pub struct %s<%s> {\n", SCRATCH_DECODER_TYPE, DATA_LIFETIME));
            indent(writer, 1, "data: &%s [u8],\n", DATA_LIFETIME);
            indent(writer).append("pos: usize,\n");
            writer.append("}\n");

            writer.append(format("%nimpl<%s> %s<%s> {\n", DATA_LIFETIME, SCRATCH_DECODER_TYPE, DATA_LIFETIME));

            indent(writer, 1, "/// Create a struct reference overlaid atop the data buffer\n");
            indent(writer, 1, "/// such that the struct's contents directly reflect the buffer. \n");
            indent(writer, 1, "/// Advances the `pos` index by the size of the struct in bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer, 1, "fn read_type<T>(&mut self, num_bytes: usize) -> CodecResult<&%s T> {\n", DATA_LIFETIME);
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3).append("let s = self.data[self.pos..end].as_ptr() as *mut T;\n");
            indent(writer, 3, "let v: &%s T = unsafe { &*s };\n", DATA_LIFETIME);
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(v)\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n\n");

            indent(writer, 1, "/// Advances the `pos` index by a set number of bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer).append("fn skip_bytes(&mut self, num_bytes: usize) -> CodecResult<()> {\n");
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(())\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n\n");

            indent(writer, 1, "/// Create a slice reference overlaid atop the data buffer\n");
            indent(writer, 1, "/// such that the slice's members' contents directly reflect the buffer.\n");
            indent(writer, 1, "/// Advances the `pos` index by the size of the slice contents in bytes.\n");
            indent(writer).append("#[inline]\n");
            indent(writer, 1, "fn read_slice<T>(&mut self, count: usize, bytes_per_item: usize) " +
                "-> CodecResult<&%s [T]> {\n", DATA_LIFETIME);
            indent(writer, 2).append("let num_bytes = bytes_per_item * count;\n");
            indent(writer, 2).append("let end = self.pos + num_bytes;\n");
            indent(writer, 2).append("if end <= self.data.len() {\n");
            indent(writer, 3, "let v: &%s [T] = unsafe {\n", DATA_LIFETIME);
            indent(writer, 4).append("core::slice::from_raw_parts(self.data[self.pos..end].as_ptr() as *const T, " +
                "count)\n");
            indent(writer, 3).append("};\n");
            indent(writer, 3).append("self.pos = end;\n");
            indent(writer, 3).append("Ok(v)\n");
            indent(writer, 2).append("} else {\n");
            indent(writer, 3).append("Err(CodecErr::NotEnoughBytes)\n");
            indent(writer, 2).append("}\n");
            indent(writer).append("}\n");

            writer.append("}\n");
        }
    }

    private static void generateEitherEnum(final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Convenience Either enum"))
        {
            writer.append("#[derive(Copy, Clone, PartialEq, Eq, PartialOrd, Ord, Hash, Debug)]\n");
            writer.append("pub enum Either<L, R> {\n");
            indent(writer).append("Left(L),\n");
            indent(writer).append("Right(R)\n");
            writer.append("}\n");
        }
    }

    private static void generateEnum(final List<Token> enumTokens, final OutputManager outputManager)
        throws IOException
    {
        final String originalEnumName = enumTokens.get(0).applicableTypeName();
        final String enumRustName = formatTypeName(originalEnumName);
        try (Writer writer = outputManager.createOutput("Enum " + enumRustName))
        {
            final List<Token> messageBody = getMessageBody(enumTokens);
            if (messageBody.isEmpty())
            {
                throw new IllegalArgumentException("No valid values provided for enum " + originalEnumName);
            }

            writer.append("#[derive(Clone,Copy,Debug,PartialEq,Eq,PartialOrd,Ord,Hash)]").append("\n");
            final String rustReprTypeName = rustTypeName(messageBody.get(0).encoding().primitiveType());
            writer.append(format("#[repr(%s)]",
                rustReprTypeName)).append("\n");
            writer.append("pub enum ").append(enumRustName).append(" {\n");

            for (final Token token : messageBody)
            {
                final Encoding encoding = token.encoding();
                final String literal = generateRustLiteral(encoding.primitiveType(), encoding.constValue().toString());
                indent(writer, 1).append(token.name())
                    .append(" = ")
                    .append(literal)
                    .append(",\n");
            }

            // null value
            final Token token = messageBody.get(0);
            final Encoding encoding = token.encoding();
            final CharSequence nullVal = generateRustLiteral(
                encoding.primitiveType(), encoding.applicableNullValue().toString());
            indent(writer, 1).append("NullVal")
                .append(" = ")
                .append(nullVal)
                .append(",\n");

            writer.append("}\n");

            // Default implementation to support Default in other structs
            indent(writer, 0, "impl Default for %s {\n", enumRustName);
            indent(writer, 1, "fn default() -> Self { %s::%s }\n", enumRustName, "NullVal");
            indent(writer, 0, "}\n");
        }
    }

    private static void generateComposites(final Ir ir, final OutputManager outputManager) throws IOException
    {
        for (final List<Token> tokens : ir.types())
        {
            if (!tokens.isEmpty() && tokens.get(0).signal() == Signal.BEGIN_COMPOSITE)
            {
                generateSingleComposite(tokens, outputManager);
            }
        }
    }

    private static void generateSingleComposite(final List<Token> tokens, final OutputManager outputManager)
        throws IOException
    {
        final Token beginToken = tokens.get(0);
        final String originalTypeName = beginToken.applicableTypeName();
        final String formattedTypeName = formatTypeName(originalTypeName);
        final SplitCompositeTokens splitTokens = SplitCompositeTokens.splitInnerTokens(tokens);

        try (Writer writer = outputManager.createOutput(formattedTypeName))
        {
            final RustStruct struct = RustStruct.fromTokens(formattedTypeName,
                splitTokens.nonConstantEncodingTokens(),
                EnumSet.of(RustStruct.Modifier.PACKED, RustStruct.Modifier.DEFAULT));
            struct.appendDefinitionTo(writer);

            generateConstantAccessorImpl(writer, formattedTypeName, getMessageBody(tokens));
        }
    }

    private interface RustTypeDescriptor
    {
        String DEFAULT_VALUE = "Default::default()";

        String name();

        String literalValue(String valueRep);

        int sizeBytes();

        default String defaultValue()
        {
            return DEFAULT_VALUE;
        }
    }

    private static final class RustArrayType implements RustTypeDescriptor
    {
        private final RustTypeDescriptor componentType;
        private final int length;

        private RustArrayType(final RustTypeDescriptor component, final int length)
        {
            this.componentType = component;
            this.length = length;
        }

        public String name()
        {
            return getRustStaticArrayString(componentType.name(), length);
        }

        public String literalValue(final String valueRep)
        {
            return getRustStaticArrayString(valueRep + componentType.name(), length);
        }

        public int sizeBytes()
        {
            return componentType.sizeBytes() * length;
        }

        public String defaultValue()
        {
            final String defaultValue = RustTypeDescriptor.super.defaultValue();
            if (length <= 32)
            {
                return defaultValue;
            }
            else
            {
                final StringBuilder result = new StringBuilder();
                result.append('[');
                for (int i = 0; i < length; i++)
                {
                    result.append(defaultValue);
                    result.append(", ");
                    if (i % 4 == 0) // ~80 char lines
                    {
                        result.append('\n');
                    }
                }
                result.append(']');

                return result.toString();
            }
        }
    }

    private static final class RustPrimitiveType implements RustTypeDescriptor
    {
        private final String name;
        private final int sizeBytes;

        private RustPrimitiveType(final String name, final int sizeBytes)
        {
            this.name = name;
            this.sizeBytes = sizeBytes;
        }

        public String name()
        {
            return name;
        }

        public String literalValue(final String valueRep)
        {
            return valueRep + name;
        }

        public int sizeBytes()
        {
            return sizeBytes;
        }
    }

    private static final class AnyRustType implements RustTypeDescriptor
    {
        private final String name;
        private final int sizeBytes;

        private AnyRustType(final String name, final int sizeBytes)
        {
            this.name = name;
            this.sizeBytes = sizeBytes;
        }

        public String name()
        {
            return name;
        }

        public String literalValue(final String valueRep)
        {
            final String msg = String.format("Cannot produce a literal value %s of type %s!", valueRep, name);
            throw new UnsupportedOperationException(msg);
        }

        public int sizeBytes()
        {
            return sizeBytes;
        }
    }

    private static final class RustTypes
    {
        static final RustTypeDescriptor U_8 = new RustPrimitiveType("u8", 1);

        static RustTypeDescriptor ofPrimitiveToken(final Token token)
        {
            final PrimitiveType primitiveType = token.encoding().primitiveType();
            final String rustPrimitiveType = RustUtil.rustTypeName(primitiveType);
            final RustPrimitiveType type = new RustPrimitiveType(rustPrimitiveType, primitiveType.size());
            if (token.arrayLength() > 1)
            {
                return new RustArrayType(type, token.arrayLength());
            }

            return type;
        }

        static RustTypeDescriptor ofGeneratedToken(final Token token)
        {
            return new AnyRustType(formatTypeName(token.applicableTypeName()), token.encodedLength());
        }

        static RustTypeDescriptor arrayOf(final RustTypeDescriptor type, final int len)
        {
            return new RustArrayType(type, len);
        }
    }

    private static final class RustStruct
    {
        enum Modifier
        {
            PACKED, DEFAULT
        }

        final String name;
        final List<RustStructField> fields;
        final EnumSet<Modifier> modifiers;

        private RustStruct(final String name, final List<RustStructField> fields, final EnumSet<Modifier> modifiers)
        {
            this.name = name;
            this.fields = fields;
            this.modifiers = modifiers;
        }

        public int sizeBytes()
        {
            return fields.stream().mapToInt((v) -> v.type.sizeBytes()).sum();
        }

        static RustStruct fromHeader(final HeaderStructure header)
        {
            final List<Token> tokens = header.tokens();
            final String originalTypeName = tokens.get(0).applicableTypeName();
            final String formattedTypeName = formatTypeName(originalTypeName);
            final SplitCompositeTokens splitTokens = SplitCompositeTokens.splitInnerTokens(tokens);

            return RustStruct.fromTokens(
                formattedTypeName,
                splitTokens.nonConstantEncodingTokens(),
                EnumSet.of(Modifier.PACKED, Modifier.DEFAULT));
        }

        static RustStruct fromTokens(
            final String name, final List<NamedToken> tokens, final EnumSet<Modifier> modifiers)
        {
            return new RustStruct(name, collectStructFields(tokens), modifiers);
        }

        // No way to create struct with default values.
        // Rust RFC: https://github.com/Centril/rfcs/pull/19
        // Used when struct contains a field which doesn't have a Default impl
        void appendDefaultConstructorTo(final Appendable appendable) throws IOException
        {
            indent(appendable, 0, "impl Default for %s {\n", name);
            indent(appendable, 1, "fn default() -> Self {\n");

            appendInstanceTo(appendable, 2, Collections.emptyMap());

            indent(appendable, 1, "}\n");

            appendable.append("}\n");
        }

        void appendDefinitionTo(final Appendable appendable) throws IOException
        {
            final boolean needsDefault = modifiers.contains(Modifier.DEFAULT);
            final boolean canDeriveDefault = fields.stream()
                .allMatch((v) -> v.type.defaultValue() == RustTypeDescriptor.DEFAULT_VALUE);

            final Set<Modifier> modifiers = this.modifiers.clone();
            if (needsDefault && !canDeriveDefault)
            {
                modifiers.remove(Modifier.DEFAULT);
            }

            appendStructHeader(appendable, name, modifiers);
            for (final RustStructField field: fields)
            {
                indent(appendable);
                if (field.modifiers.contains(RustStructField.Modifier.PUBLIC))
                {
                    appendable.append("pub ");
                }
                appendable.append(field.name).append(":").append(field.type.name()).append(",\n");
            }
            appendable.append("}\n");

            if (needsDefault && !canDeriveDefault)
            {
                appendDefaultConstructorTo(appendable);
            }
        }

        void appendInstanceTo(final Appendable appendable, final int indent, final Map<String, String> values)
            throws IOException
        {
            indent(appendable, indent, "%s {\n", name);
            for (final RustStructField field: fields)
            {
                final String value;
                if (values.containsKey(field.name))
                {
                    value = field.type.literalValue(values.get(field.name));
                }
                else
                {
                    value = field.type.defaultValue();
                }

                indent(appendable, indent + 1, "%s: %s,\n", formatMethodName(field.name), value);
            }
            indent(appendable, indent, "}\n");
        }
    }

    private static final class RustStructField
    {
        enum Modifier
        {
            PUBLIC
        }

        final String name;
        final RustTypeDescriptor type;
        final EnumSet<Modifier> modifiers;

        private RustStructField(final String name, final RustTypeDescriptor type, final EnumSet<Modifier> modifiers)
        {
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
        }

        private RustStructField(final String name, final RustTypeDescriptor type)
        {
            this(name, type, EnumSet.noneOf(Modifier.class));
        }
    }

    private static List<RustStructField> collectStructFields(final List<NamedToken> namedTokens)
    {
        final List<RustStructField> fields = new ArrayList<>();
        int totalSize = 0;
        for (final NamedToken namedToken : namedTokens)
        {
            final Token typeToken = namedToken.typeToken();
            if (typeToken.isConstantEncoding())
            {
                continue;
            }

            final String propertyName = formatMethodName(namedToken.name());

            // need padding when field offsets imply gaps
            final int offset = typeToken.offset();
            if (offset != totalSize)
            {
                int rem = offset - totalSize;
                int idx = 1;
                while (rem > 0)
                {
                    // split padding arrays to 32 as larger arrays do not have an `impl Default`
                    final int padding = Math.min(rem, 32);
                    final RustTypeDescriptor type = RustTypes.arrayOf(RustTypes.U_8, padding);
                    fields.add(new RustStructField(propertyName + "_padding_" + idx, type));

                    idx += 1;
                    rem -= padding;
                }
            }
            totalSize = offset + typeToken.encodedLength();

            final RustTypeDescriptor type;
            switch (typeToken.signal())
            {
                case ENCODING:
                    type = RustTypes.ofPrimitiveToken(typeToken);
                    fields.add(new RustStructField(propertyName, type, EnumSet.of(RustStructField.Modifier.PUBLIC)));
                    break;

                case BEGIN_ENUM:
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    type = RustTypes.ofGeneratedToken(typeToken);
                    fields.add(new RustStructField(propertyName, type, EnumSet.of(RustStructField.Modifier.PUBLIC)));
                    break;

                default:
                    throw new IllegalStateException(format("Unsupported struct property from %s", typeToken));
            }
        }
        return fields;
    }

    private void generateMessageHeaderDefault(
        final Ir ir,
        final OutputManager outputManager,
        final Token messageToken)
        throws IOException
    {
        final HeaderStructure header = ir.headerStructure();
        final RustStruct rustHeader = RustStruct.fromHeader(header);

        final String messageTypeName = formatTypeName(messageToken.name());
        final String wrapperName = messageTypeName + "MessageHeader";

        try (Writer writer = outputManager.createOutput(messageTypeName + " specific Message Header "))
        {
            appendStructHeader(writer, wrapperName, EnumSet.of(RustStruct.Modifier.PACKED));
            indent(writer, 1, "pub message_header: MessageHeader\n");
            writer.append("}\n");

            final String blockLength = Integer.toString(messageToken.encodedLength());
            final String templateId = Integer.toString(messageToken.id());
            final String schemaId = Integer.toString(ir.id());
            final String version = Integer.toString(ir.version());

            indent(writer, 0, "impl %s {\n", wrapperName);
            indent(writer, 1, "pub const BLOCK_LENGTH : u16 = " + blockLength + ";\n");
            indent(writer, 1, "pub const TEMPLATE_ID : u16 = " + templateId + ";\n");
            indent(writer, 1, "pub const SCHEMA_ID : u16 = " + schemaId + ";\n");
            indent(writer, 1, "pub const VERSION : u16 = " + version + ";\n");
            indent(writer, 0, "}\n");

            indent(writer, 0, "impl Default for %s {\n", wrapperName);
            indent(writer, 1, "fn default() -> %s {\n", wrapperName);
            indent(writer, 2, "%s {\n", wrapperName);

            indent(writer, 3, "message_header: ");
            rustHeader.appendInstanceTo(writer, 3,
                new HashMap<String, String>()
                {
                    {
                        put("block_length", blockLength);
                        put("template_id", templateId);
                        put("schema_id", schemaId);
                        put("version", version);
                    }
                });

            indent(writer, 2, "}\n");
            indent(writer, 1, "}\n");

            writer.append("}\n");
        }
    }

    private static void appendStructHeader(final Appendable appendable, final String structName) throws IOException
    {
        appendStructHeader(appendable, structName, EnumSet.noneOf(RustStruct.Modifier.class));
    }

    private static void appendStructHeader(
        final Appendable appendable, final String structName, final Set<RustStruct.Modifier> modifiers)
        throws IOException
    {
        if (!modifiers.isEmpty())
        {
            if (modifiers.contains(RustStruct.Modifier.PACKED))
            {
                appendable.append("#[repr(C,packed)]\n");
            }
            if (modifiers.contains(RustStruct.Modifier.DEFAULT))
            {
                appendable.append("#[derive(Default)]\n");
            }
        }

        appendable.append(format("pub struct %s {\n", structName));
    }

    private static String getRustStaticArrayString(final String rustPrimitiveType, final int length)
    {
        return format("[%s;%s]", rustPrimitiveType, length);
    }

    private static String getRustTypeForPrimitivePossiblyArray(
        final Token encodingToken, final String rustPrimitiveType)
    {
        final String rustType;
        if (encodingToken.arrayLength() > 1)
        {
            rustType = getRustStaticArrayString(rustPrimitiveType, encodingToken.arrayLength());
        }
        else
        {
            rustType = rustPrimitiveType;
        }

        return rustType;
    }

    private static void generateConstantAccessorImpl(
        final Appendable writer, final String formattedTypeName, final List<Token> unfilteredFields) throws IOException
    {
        writer.append(format("%nimpl %s {\n", formattedTypeName));

        for (int i = 0; i < unfilteredFields.size(); )
        {
            final Token fieldToken = unfilteredFields.get(i);
            final String name = fieldToken.name();
            final int componentTokenCount = fieldToken.componentTokenCount();
            final Token signalToken;
            if (fieldToken.signal() == BEGIN_FIELD)
            {
                if (i > unfilteredFields.size() - 1)
                {
                    throw new ArrayIndexOutOfBoundsException("BEGIN_FIELD token should be followed by content tokens");
                }
                signalToken = unfilteredFields.get(i + 1);
            }
            else
            {
                signalToken = fieldToken;
            }

            // Either the field must be marked directly as constant
            // or it must wrap something that is fully constant
            if (!(fieldToken.isConstantEncoding() || signalToken.isConstantEncoding()))
            {
                i += componentTokenCount;
                continue;
            }

            final String constantRustTypeName;
            final String constantRustExpression;
            switch (signalToken.signal())
            {
                case ENCODING:
                    final String rawValue = signalToken.encoding().constValue().toString();
                    if (signalToken.encoding().primitiveType() == PrimitiveType.CHAR)
                    {
                        // Special case string handling
                        constantRustTypeName = "&'static str";
                        // TODO - proper string escaping
                        constantRustExpression = "\"" + rawValue + "\"";
                    }
                    else
                    {
                        final String constantRustPrimitiveType = RustUtil.rustTypeName(
                            signalToken.encoding().primitiveType());
                        constantRustTypeName = getRustTypeForPrimitivePossiblyArray(
                            signalToken, constantRustPrimitiveType);
                        constantRustExpression = generateRustLiteral(
                            signalToken.encoding().primitiveType(), rawValue);
                    }
                    break;

                case BEGIN_ENUM:
                    final String enumType = formatTypeName(signalToken.applicableTypeName());
                    final String rawConstValueName = fieldToken.encoding().constValue().toString();
                    final int indexOfDot = rawConstValueName.indexOf('.');
                    final String constValueName = -1 == indexOfDot ?
                        rawConstValueName : rawConstValueName.substring(indexOfDot + 1);
                    boolean foundMatchingValueName = false;
                    for (int j = i; j < unfilteredFields.size(); j++)
                    {
                        final Token searchAhead = unfilteredFields.get(j);
                        if (searchAhead.signal() == VALID_VALUE && searchAhead.name().equals(constValueName))
                        {
                            foundMatchingValueName = true;
                            break;
                        }
                    }
                    if (!foundMatchingValueName)
                    {
                        throw new IllegalStateException(format("Found a constant enum field that requested value %s, " +
                                "which is not an available enum option.", rawConstValueName));
                    }
                    constantRustTypeName = enumType;
                    constantRustExpression = enumType + "::" + constValueName;
                    break;

                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                default:
                    throw new IllegalStateException("Unsupported constant presence property " + fieldToken);
            }

            appendConstAccessor(writer, name, constantRustTypeName, constantRustExpression);
            i += componentTokenCount;
        }

        writer.append("}\n");
    }

    private static void appendConstAccessor(
        final Appendable writer,
        final String name,
        final String rustTypeName,
        final String rustExpression) throws IOException
    {
        writer.append("\n").append(INDENT).append("#[inline]\n").append(INDENT);
        writer.append(format("pub fn %s() -> %s {\n", formatMethodName(name), rustTypeName));
        indent(writer, 2).append(rustExpression).append("\n");
        indent(writer).append("}\n");
    }
}
