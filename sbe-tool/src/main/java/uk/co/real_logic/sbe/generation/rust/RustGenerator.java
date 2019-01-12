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
import java.util.stream.Collectors;
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
        generateSharedImports(ir, outputManager);
        generateResultEnums(outputManager);
        generateDecoderScratchStruct(outputManager);
        generateEncoderScratchStruct(ir, outputManager);
        generateEitherEnum(outputManager);
        generateEnums(ir, outputManager);
        generateComposites(ir, outputManager);
        generateBitSets(ir, outputManager);
        final int headerSize = totalByteSize(ir.headerStructure());

        for (final List<Token> tokens : ir.messages())
        {
            final MessageComponents components = MessageComponents.collectMessageComponents(tokens);
            final String messageTypeName = formatTypeName(components.messageToken.name());

            final Optional<FieldsRepresentationSummary> fieldsRepresentation =
                generateFieldsRepresentation(messageTypeName, components, outputManager);
            generateMessageHeaderDefault(ir, outputManager, components.messageToken);

            // Avoid the work of recomputing the group tree twice per message
            final List<GroupTreeNode> groupTree = buildGroupTrees(messageTypeName, components.groups);
            generateGroupFieldRepresentations(outputManager, groupTree);

            generateMessageDecoder(outputManager, components, groupTree, fieldsRepresentation, headerSize);
            generateMessageEncoder(outputManager, components, groupTree, fieldsRepresentation, headerSize);
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
            appendStructHeader(appendable, node.contextualName + "Member", true);
            appendStructFields(appendable, node.simpleNamedFields);
            appendable.append("}\n");

            generateConstantAccessorImpl(appendable, node.contextualName + "Member", node.rawFields);

            generateGroupFieldRepresentations(appendable, node.groups);
        }
    }

    private static final class FieldsRepresentationSummary
    {
        final String typeName;
        final int numBytes;

        private FieldsRepresentationSummary(final String typeName, final int numBytes)
        {
            this.typeName = typeName;
            this.numBytes = numBytes;
        }
    }

    private static Optional<FieldsRepresentationSummary> generateFieldsRepresentation(
        final String messageTypeName,
        final MessageComponents components,
        final OutputManager outputManager) throws IOException
    {
        final List<NamedToken> namedFieldTokens = NamedToken.gatherNamedNonConstantFieldTokens(components.fields);

        final String representationStruct = messageTypeName + "Fields";
        try (Writer writer = outputManager.createOutput(messageTypeName + " Fixed-size Fields"))
        {
            appendStructHeader(writer, representationStruct, true);
            appendStructFields(writer, namedFieldTokens);
            writer.append("}\n");

            generateConstantAccessorImpl(writer, representationStruct, components.fields);
        }

        // Compute the total static size in bytes of the fields representation
        int numBytes = 0;
        for (int i = 0, size = components.fields.size(); i < size;)
        {
            final Token fieldToken = components.fields.get(i);
            if (fieldToken.signal() == Signal.BEGIN_FIELD)
            {
                final int fieldEnd = i + fieldToken.componentTokenCount();
                if (!fieldToken.isConstantEncoding())
                {
                    for (int j = i; j < fieldEnd; j++)
                    {
                        final Token t = components.fields.get(j);
                        if (t.isConstantEncoding())
                        {
                            continue;
                        }
                        if (t.signal() == ENCODING || t.signal() == BEGIN_ENUM || t.signal() == BEGIN_SET)
                        {
                            numBytes += t.encodedLength();
                        }
                    }
                }
                i += fieldToken.componentTokenCount();
            }
            else
            {
                throw new IllegalStateException("field tokens must include bounding BEGIN_FIELD and END_FIELD tokens");
            }
        }

        return Optional.of(new FieldsRepresentationSummary(representationStruct, numBytes));
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
            writer.append("#[derive(Debug,Default)]\n");
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
    }

    private static void generateMessageEncoder(
        final OutputManager outputManager,
        final MessageComponents components,
        final List<GroupTreeNode> groupTree,
        final Optional<FieldsRepresentationSummary> fieldsRepresentation,
        final int headerSize)
        throws IOException
    {
        final Token msgToken = components.messageToken;
        final String messageTypeName = formatTypeName(msgToken.name());
        final RustCodecType codecType = RustCodecType.Encoder;
        String topType = codecType.generateDoneCoderType(outputManager, messageTypeName);
        topType = generateTopVarDataCoders(messageTypeName, components.varData, outputManager, topType, codecType);
        topType = generateGroupsCoders(groupTree, outputManager, topType, codecType);
        topType = generateFixedFieldCoder(messageTypeName, outputManager, topType, fieldsRepresentation, codecType);
        topType = codecType.generateMessageHeaderCoder(messageTypeName, outputManager, topType, headerSize);
        generateEntryPoint(messageTypeName, outputManager, topType, codecType);
    }

    private static void generateMessageDecoder(
        final OutputManager outputManager,
        final MessageComponents components,
        final List<GroupTreeNode> groupTree,
        final Optional<FieldsRepresentationSummary> fieldsRepresentation,
        final int headerSize)
        throws IOException
    {
        final Token msgToken = components.messageToken;
        final String messageTypeName = formatTypeName(msgToken.name());
        final RustCodecType codecType = RustCodecType.Decoder;
        String topType = codecType.generateDoneCoderType(outputManager, messageTypeName);
        topType = generateTopVarDataCoders(messageTypeName, components.varData, outputManager, topType, codecType);
        topType = generateGroupsCoders(groupTree, outputManager, topType, codecType);
        topType = generateFixedFieldCoder(messageTypeName, outputManager, topType, fieldsRepresentation, codecType);
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

    static String withLifetime(final String typeName)
    {
        return format("%s<%s>", typeName, DATA_LIFETIME);
    }

    private static String generateFixedFieldCoder(
        final String messageTypeName,
        final OutputManager outputManager,
        final String topType,
        final Optional<FieldsRepresentationSummary> fieldsRepresentationOptional,
        final RustCodecType codecType) throws IOException
    {
        if (!fieldsRepresentationOptional.isPresent())
        {
            return topType;
        }

        final FieldsRepresentationSummary fieldsRepresentation = fieldsRepresentationOptional.get();
        try (Writer writer = outputManager.createOutput(messageTypeName + " Fixed fields " + codecType.name()))
        {
            final String representationStruct = fieldsRepresentation.typeName;
            final String decoderName = representationStruct + codecType.name();
            codecType.appendScratchWrappingStruct(writer, decoderName);
            appendImplWithLifetimeHeader(writer, decoderName);
            codecType.appendWrapMethod(writer, decoderName);
            codecType.appendDirectCodeMethods(writer, formatMethodName(messageTypeName) + "_fields",
                representationStruct, topType, fieldsRepresentation.numBytes);
            writer.append("}\n");
            // TODO - Move read position further if in-message blockLength exceeds fixed fields representation size
            // will require piping some data from the previously-read message header
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
            appendStructHeader(out, withLifetime(memberCoderType), false);
            final String rustCountType = rustTypeName(node.numInGroupType);
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
                scratchChain, rustCountType, node.numInGroupType.size());
            indent(out, 2, "Ok(%s)\n", atEndOfParent ? "self.parent" :
                format("%s::wrap(self.%s)", afterGroupCoderType, contentProperty));
            indent(out).append("}\n").append("}\n");

            appendStructHeader(out, withLifetime(headerCoderType), false);
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
                scratchChain, rustTypeName(node.blockLengthType),
                generateRustLiteral(node.blockLengthType, Integer.toString(node.blockLength)),
                node.blockLengthType.size());
            indent(out, 2, "let count_pos = %s.pos;\n", scratchChain);
            indent(out, 2, "%s.write_type::<%s>(&0, %s)?; // preliminary group member count\n",
                scratchChain, rustCountType, node.numInGroupType.size());
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
        indent(out).append("#[inline]\n");
        indent(out, 1, "pub fn %s_as_slice(mut self, count: %s) -> CodecResult<(&%s mut [%s], %s)> {\n",
            formatMethodName(node.originalName), rustCountType, DATA_LIFETIME, fieldsType,
            withLifetime(afterGroupCoderType));
        indent(out, 2, "%s.write_type::<%s>(&%s, %s)?; // block length\n",
            scratchChain, rustTypeName(node.blockLengthType),
            generateRustLiteral(node.blockLengthType, Integer.toString(node.blockLength)),
            node.blockLengthType.size());
        indent(out, 2, "%s.write_type::<%s>(&count, %s)?; // group count\n",
            scratchChain, rustCountType, node.numInGroupType.size());
        indent(out, 2, "let c = count as usize;\n");
        indent(out, 2, "let group_slice = %s.writable_slice::<%s>(c, %s)?;\n",
            scratchChain, fieldsType, node.blockLength);
        indent(out, 2, "Ok((group_slice, %s))\n", atEndOfParent ?
            "self.parent" : format("%s::wrap(self.%s)", afterGroupCoderType, contentProperty));
        indent(out, 1).append("}\n");

        indent(out).append("#[inline]\n");
        indent(out, 1, "pub fn %s_from_slice(mut self, s: &[%s]) -> CodecResult<%s> {\n",
            formatMethodName(node.originalName), fieldsType,
            withLifetime(afterGroupCoderType));
        indent(out, 2, "%s.write_type::<%s>(&%s, %s)?; // block length\n",
            scratchChain, rustTypeName(node.blockLengthType),
            generateRustLiteral(node.blockLengthType, Integer.toString(node.blockLength)),
            node.blockLengthType.size());
        indent(out, 2, "let count = s.len();\n");
        indent(out, 2, "if count > %s {\n", node.numInGroupType.maxValue());
        indent(out, 3).append("return Err(CodecErr::SliceIsLongerThanAllowedBySchema)\n");
        indent(out, 2).append("}\n");
        indent(out, 2, "%s.write_type::<%s>(&(count as %s), %s)?; // group count\n",
            scratchChain, rustCountType, rustCountType, node.numInGroupType.size());
        indent(out, 2, "%s.write_slice_without_count::<%s>(s, %s)?;\n",
            scratchChain, fieldsType, node.blockLength);
        indent(out, 2, "Ok(%s)\n", atEndOfParent ? "self.parent" :
            format("%s::wrap(self.%s)", afterGroupCoderType, contentProperty));
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
            appendStructHeader(out, withLifetime(memberDecoderType), false);
            final String rustCountType = rustTypeName(node.numInGroupType);
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
            indent(out, 1, "fn after_member(mut self) -> %s {\n", groupLevelNextDecoderType);
            indent(out, 2).append("if self.index <= self.max_index {\n");
            indent(out, 3).append("Either::Left(self)\n");
            indent(out, 2).append("} else {\n").append(INDENT).append(INDENT).append(INDENT)
                .append(format("Either::Right(%s)\n", atEndOfParent ? "self.parent.after_member()" :
                format("%s::wrap(self.%s)", initialNextDecoderType, contentProperty)));
            indent(out, 2).append("}\n").append(INDENT).append("}\n").append("}\n");

            appendStructHeader(out, withLifetime(headerDecoderType), false);
            indent(out, 1, "%s: %s,\n", contentProperty, contentBearingType).append("}\n");

            appendImplWithLifetimeHeader(out, headerDecoderType);
            indent(out, 1, "fn wrap(%s: %s) -> Self {\n", contentProperty, contentBearingType);
            indent(out, 2, "%s { %s: %s }\n", headerDecoderType, contentProperty, contentProperty)
                .append(INDENT).append("}\n");

            indent(out, 1, "pub fn %s_individually(mut self) -> CodecResult<%s> {\n",
                formatMethodName(node.originalName), groupLevelNextDecoderType);
            indent(out, 2, "%s.skip_bytes(%s)?; // Skip reading block length for now\n",
                toScratchChain(node), node.blockLengthType.size());
            indent(out, 2, "let count = *%s.read_type::<%s>(%s)?;\n",
                toScratchChain(node), rustTypeName(node.numInGroupType), node.numInGroupType.size());
            indent(out, 2).append("if count > 0 {\n");
            indent(out, 3, "Ok(Either::Left(%s::new(self.%s, count)))\n",
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
        indent(out, 2, "%s.skip_bytes(%s)?; // Skip reading block length for now\n", toScratchChain(node),
            node.blockLengthType.size());
        indent(out, 2, "let count = *%s.read_type::<%s>(%s)?;\n",
            toScratchChain(node), rustTypeName(node.numInGroupType), node.numInGroupType.size());
        indent(out, 2, "let s = %s.read_slice::<%s>(count as usize, %s)?;\n",
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
            final PrimitiveType numInGroupType = findPrimitiveByTokenName(dimensionsTokens, "numInGroup");
            final Token blockLengthToken = findPrimitiveTokenByTokenName(dimensionsTokens, "blockLength");
            final int blockLength = groupToken.encodedLength();
            final PrimitiveType blockLengthType = blockLengthToken.encoding().primitiveType();
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
                numInGroupType,
                blockLengthType,
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

    static class GroupTreeNode
    {
        final Optional<GroupTreeNode> parent;
        final String originalName;
        final String contextualName;
        final PrimitiveType numInGroupType;
        final PrimitiveType blockLengthType;
        final int blockLength;
        final List<Token> rawFields;
        final List<NamedToken> simpleNamedFields;
        final List<GroupTreeNode> groups = new ArrayList<>();
        final List<VarDataSummary> varData;

        GroupTreeNode(
            final Optional<GroupTreeNode> parent,
            final String originalName,
            final String contextualName,
            final PrimitiveType numInGroupType,
            final PrimitiveType blockLengthType,
            final int blockLength,
            final List<Token> fields,
            final List<VarDataSummary> varData)
        {
            this.parent = parent;
            this.originalName = originalName;
            this.contextualName = contextualName;
            this.numInGroupType = numInGroupType;
            this.blockLengthType = blockLengthType;
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
                appendStructHeader(writer, withLifetime(decoderType), false);
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
                appendStructHeader(writer, withLifetime(decoderType), false);
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

    static void generateSharedImports(final Ir ir, final OutputManager outputManager) throws IOException
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

    static void generateEncoderScratchStruct(final Ir ir, final OutputManager outputManager) throws IOException
    {
        try (Writer writer = outputManager.createOutput("Scratch Encoder Data Wrapper - codec internal use only"))
        {
            writer.append("#[derive(Debug)]\n");
            writer.append(format("struct %s<%s> {\n", SCRATCH_ENCODER_TYPE, DATA_LIFETIME));
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
            writer.append(format("struct %s<%s> {\n", SCRATCH_DECODER_TYPE, DATA_LIFETIME));
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

            writer.append("}\n");
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
            appendStructHeader(writer, formattedTypeName, true);
            appendStructFields(writer, splitTokens.nonConstantEncodingTokens());
            writer.append("}\n");

            generateConstantAccessorImpl(writer, formattedTypeName, getMessageBody(tokens));
        }
    }

    private static void appendStructFields(final Appendable appendable, final List<NamedToken> namedTokens)
        throws IOException
    {
        for (final NamedToken namedToken : namedTokens)
        {
            final Token typeToken = namedToken.typeToken();
            if (typeToken.isConstantEncoding())
            {
                continue;
            }

            final String propertyName = formatMethodName(namedToken.name());
            indent(appendable).append("pub ").append(propertyName).append(":");

            switch (typeToken.signal())
            {
                case ENCODING:
                    final String rustPrimitiveType = RustUtil.rustTypeName(typeToken.encoding().primitiveType());
                    final String rustFieldType = getRustTypeForPrimitivePossiblyArray(typeToken, rustPrimitiveType);
                    appendable.append(rustFieldType);
                    break;

                case BEGIN_ENUM:
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    appendable.append(formatTypeName(typeToken.applicableTypeName()));
                    break;

                default:
                    throw new IllegalStateException(
                        format("Unsupported struct property from %s", typeToken.toString()));
            }

            appendable.append(",\n");
        }
    }

    private void generateMessageHeaderDefault(
        final Ir ir,
        final OutputManager outputManager,
        final Token messageToken)
        throws IOException
    {
        final HeaderStructure header = ir.headerStructure();
        final String messageTypeName = formatTypeName(messageToken.name());
        final String wrapperName = messageTypeName + "MessageHeader";

        try (Writer writer = outputManager.createOutput(messageTypeName + " specific Message Header "))
        {
            appendStructHeader(writer, wrapperName, true);
            indent(writer, 1, "pub message_header: MessageHeader\n");
            writer.append("}\n");

            indent(writer, 1, "impl Default for %s {\n", wrapperName);
            indent(writer, 1, "fn default() -> %s {\n", wrapperName);
            indent(writer, 2, "%s {\n", wrapperName);

            indent(writer, 3, "message_header: MessageHeader {\n");
            indent(writer, 4, "%s: %s,\n", formatMethodName("blockLength"),
                generateRustLiteral(header.blockLengthType(), Integer.toString(messageToken.encodedLength())));
            indent(writer, 4, "%s: %s,\n", formatMethodName("templateId"),
                generateRustLiteral(header.templateIdType(), Integer.toString(messageToken.id())));
            indent(writer, 4, "%s: %s,\n", formatMethodName("schemaId"),
                generateRustLiteral(header.schemaIdType(), Integer.toString(ir.id())));
            indent(writer, 4, "%s: %s,\n", formatMethodName("version"),
                generateRustLiteral(header.schemaVersionType(), Integer.toString(ir.version())));

            // Technically the spec seems to allow non-standard fields in the message header, so we attempt
            // to provide some sort of default for them
            final Set<String> reserved = new HashSet<>(Arrays.asList("blockLength", "templateId", "schemaId",
                "version"));

            final List<NamedToken> nonReservedNamedTokens = SplitCompositeTokens.splitInnerTokens(header.tokens())
                .nonConstantEncodingTokens()
                .stream()
                .filter((namedToken) -> !reserved.contains(namedToken.name()))
                .collect(Collectors.toList());

            for (final NamedToken namedToken : nonReservedNamedTokens)
            {
                indent(writer, 4, "%s: Default::default(),\n", formatMethodName(namedToken.name()));
            }

            indent(writer, 3, "}\n");

            indent(writer, 2, "}\n");
            indent(writer, 1, "}\n");

            writer.append("}\n");
        }
    }

    private static void appendStructHeader(
        final Appendable appendable,
        final String structName,
        final boolean packedCRepresentation) throws IOException
    {
        if (packedCRepresentation)
        {
            appendable.append("#[repr(C,packed)]\n");
        }

        appendable.append(format("pub struct %s {\n", structName));
    }

    private static String getRustTypeForPrimitivePossiblyArray(
        final Token encodingToken, final String rustPrimitiveType)
    {
        final String rustType;
        if (encodingToken.arrayLength() > 1)
        {
            rustType = format("[%s;%s]", rustPrimitiveType, encodingToken.arrayLength());
        }
        else
        {
            rustType = rustPrimitiveType;
        }

        return rustType;
    }

    private static void generateConstantAccessorImpl(
        final Appendable writer,
        final String formattedTypeName,
        final List<Token> unfilteredFields) throws IOException
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
