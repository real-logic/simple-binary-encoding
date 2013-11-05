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
package uk.co.real_logic.sbe.generation.cpp99;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.util.Verify;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.cpp99.Cpp99Util.*;

public class Cpp99Generator implements CodeGenerator
{
    /** Class name to be used for visitor pattern that accesses the message header. */
    public static final String MESSAGE_HEADER_VISITOR = "MessageHeader";

    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final IntermediateRepresentation ir;
    private final OutputManager outputManager;

    public Cpp99Generator(final IntermediateRepresentation ir, final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir)");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generateMessageHeaderStub() throws IOException
    {
        try (final Writer out = outputManager.createOutput(MESSAGE_HEADER_VISITOR))
        {
            out.append(generateFileHeader(ir.namespaceName().replace('.', '_'), MESSAGE_HEADER_VISITOR, null));
            out.append(generateClassDeclaration(MESSAGE_HEADER_VISITOR, "FixedFlyweight"));
            out.append(generateFixedFlyweightCode(MESSAGE_HEADER_VISITOR, ir.header().get(0).size()));

            final List<Token> tokens = ir.header();
            out.append(generatePrimitivePropertyEncodings(MESSAGE_HEADER_VISITOR, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("};\n}\n#endif\n");
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
            final String className = formatClassName(tokens.get(0).name());

            try (final Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.namespaceName().replace('.', '_'), className, typesToInclude));
                out.append(generateClassDeclaration(className, "MessageFlyweight"));
                out.append(generateMessageFlyweightCode(tokens.get(0).size(), className, tokens.get(0).schemaId()));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(className, rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                StringBuilder sb = new StringBuilder();
                generateGroups(sb, groups, 0, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = messageBody.subList(offset, messageBody.size());
                out.append(generateVarData(varData));

                out.append("};\n}\n#endif\n");
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

                sb.append(indent).append("    };\n");
                sb.append(generateGroupProperty(groupName, groupToken, indent));
            }
        }

        return index;
    }

    private void generateGroupClassHeader(final StringBuilder sb,
                                          final String groupName,
                                          final List<Token> tokens,
                                          final int index,
                                          final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final Integer dimensionHeaderSize = Integer.valueOf(tokens.get(index + 1).size());

        sb.append(String.format(
            "\n" +
            indent + "class %s : public GroupFlyweight\n" +
            indent + "{\n" +
            indent + "private:\n" +
            indent + "    %s dimensions_;\n" +
            indent + "    int blockLength_;\n" +
            indent + "    int count_;\n" +
            indent + "    int index_;\n" +
            indent + "    int offset_;\n" +
            indent + "    MessageFlyweight *message_;\n" +
            indent + "    char *buffer_;\n\n" +
            indent + "public:\n\n",
            formatClassName(groupName),
            dimensionsClassName,
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    void resetForDecode(MessageFlyweight *message)\n" +
            indent + "    {\n" +
            indent + "        message_ = message;\n" +
            indent + "        buffer_ = message_->buffer();\n" +
            indent + "        dimensions_.reset(buffer_, message_->position());\n" +
            indent + "        count_ = dimensions_.numInGroup();\n" +
            indent + "        blockLength_ = dimensions_.blockLength();\n" +
            indent + "        index_ = -1;\n" +
            indent + "        int dimensionsHeaderSize = %d;\n" +
            indent + "        message_->position(message_->position() + dimensionsHeaderSize);\n" +
            indent + "    };\n\n",
            dimensionHeaderSize
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String cpp99TypeForBlockLength = cpp99TypeName(tokens.get(index + 2).encoding().primitiveType());
        final String cpp99TypeForNumInGroup = cpp99TypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    void resetForEncode(MessageFlyweight *message, const int count)\n" +
            indent + "    {\n" +
            indent + "        message_ = message;\n" +
            indent + "        buffer_ = message_->buffer();\n" +
            indent + "        dimensions_.reset(buffer_, message_->position());\n" +
            indent + "        dimensions_.numInGroup((%s)count);\n" +
            indent + "        dimensions_.blockLength((%s)%d);\n" +
            indent + "        index_ = -1;\n" +
            indent + "        count_ = count;\n" +
            indent + "        blockLength_ = %d;\n" +
            indent + "        int dimensionsHeaderSize = %d;\n" +
            indent + "        message_->position(message_->position() + dimensionsHeaderSize);\n" +
            indent + "    };\n\n",
            cpp99TypeForNumInGroup,
            cpp99TypeForBlockLength,
            blockLength,
            blockLength,
            dimensionHeaderSize
        ));

        sb.append(
            indent + "    int count(void) const\n" +
            indent + "    {\n" +
            indent + "        return count_;\n" +
            indent + "    };\n\n" +
            indent + "    bool hasNext(void) const\n" +
            indent + "    {\n" +
            indent + "        return index_ + 1 < count_;\n" +
            indent + "    };\n\n"
        );

        sb.append(String.format(
            indent + "    %s &next(void)\n" +
            indent + "    {\n" +
            indent + "        offset_ = message_->position();\n" +
            indent + "        message_->position(offset_ + blockLength_);\n" +
            indent + "        ++index_;\n\n" +
            indent + "        return *this;\n" +
            indent + "    };\n\n",
            formatClassName(groupName)
        ));

        sb.append(
            indent + "    MessageFlyweight *message(void)\n" +
            indent + "    {\n" +
            indent + "        return message_;\n" +
            indent + "    };\n\n"
        );
    }

    private CharSequence generateGroupProperty(final String groupName,
                                               final Token token,
                                               final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        sb.append(String.format(
            "\n" +
            "private:\n" +
            indent + "    %s %s_;\n\n" +
            "public:\n",
            className,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    int %sId(void) const\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    };\n\n",
            groupName,
            Long.valueOf(token.schemaId())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %s &%s(void)\n" +
            indent + "    {\n" +
            indent + "        %s_.resetForDecode(message());\n" +
            indent + "        return %s_;\n" +
            indent + "    };\n",
            className,
            propertyName,
            propertyName,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %s &%sCount(const int count)\n" +
            indent + "    {\n" +
            indent + "        %s_.resetForEncode(message(), count);\n" +
            indent + "        return %s_;\n" +
            indent + "    };\n",
            className,
            propertyName,
            propertyName,
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

                sb.append(String.format(
                    "\n"  +
                    "    const char *%sCharacterEncoding()\n" +
                    "    {\n" +
                    "        return \"%s\";\n" +
                    "    };\n\n",
                    formatPropertyName(propertyName),
                    characterEncoding
                ));

                sb.append(String.format(
                    "    int %sId(void) const\n" +
                    "    {\n" +
                    "        return %d;\n" +
                    "    };\n\n",
                    formatPropertyName(propertyName),
                    Long.valueOf(token.schemaId())
                ));

                final Token lengthToken = tokens.get(i + 2);
                final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
                final String lengthCpp99Type = cpp99TypeName(lengthToken.encoding().primitiveType());

                sb.append(String.format(
                    "    int get%s(char *dst, const int length)\n" +
                    "    {\n" +
                    "        sbe_uint64_t sizeOfLengthField = %d;\n" +
                    "        sbe_uint64_t lengthPosition = position();\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        sbe_uint64_t dataLength = %s(*((%s *)(buffer_ + lengthPosition)));\n" +
                    "        int bytesToCopy = (length < dataLength) ? length : dataLength;\n" +
                    "        ::memcpy(dst, buffer_ + position(), bytesToCopy);\n" +
                    "        position(position() + (sbe_uint64_t)dataLength);\n" +
                    "        return bytesToCopy;\n" +
                    "    };\n\n",
                    propertyName,
                    sizeOfLengthField,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
                    lengthCpp99Type
                ));

                sb.append(String.format(
                    "    int put%s(const char *src, const int length)\n" +
                    "    {\n" +
                    "        sbe_uint64_t sizeOfLengthField = %d;\n" +
                    "        sbe_uint64_t lengthPosition = position();\n" +
                    "        *((%s *)(buffer_ + lengthPosition)) = %s((%s)length);\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        ::memcpy(buffer_ + position(), src, length);\n" +
                    "        position(position() + (sbe_uint64_t)length);\n" +
                    "        return length;\n" +
                    "    };\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthCpp99Type,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
                    lengthCpp99Type
                ));
            }
        }

        return sb;
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.namespaceName().replace('.', '_'), bitSetName, null));
            out.append(generateClassDeclaration(bitSetName, "FixedFlyweight"));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).size()));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.namespaceName().replace('.', '_'), enumName, null));
            out.append(generateEnumDeclaration(enumName));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1)));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumName));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.namespaceName().replace('.', '_'), compositeName, null));
            out.append(generateClassDeclaration(compositeName, "FixedFlyweight"));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).size()));

            out.append(generatePrimitivePropertyEncodings(compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("};\n}\n#endif\n");
        }
    }

    private CharSequence generateChoices(final String bitsetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final String typeName = cpp99TypeName(token.encoding().primitiveType());
                final String choiceBitPosition = token.encoding().constVal().toString();

                sb.append(String.format(
                    "\n" +
                    "    bool %s(void) const\n" +
                    "    {\n" +
                    "        return (%s(*((%s *)(buffer_ + offset_))) & ((uint64_t)0x1 << %s)) ? true : false;\n" +
                    "    };\n\n",
                    choiceName,
                    formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
                    typeName,
                    choiceBitPosition
                ));

                sb.append(String.format(
                    "    %s &%s(const bool value)\n" +
                    "    {\n" +
                    "        *((%s *)(buffer_ + offset_)) |= %s((uint64_t)value << %s);\n" +
                    "        return *this;\n" +
                    "    };\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
                    formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
                    choiceBitPosition
                ));
            }
        }

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(
            "    enum Value \n" +
            "    {\n"
        );

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(token);
            sb.append("        ").append(token.name()).append(" = ").append(constVal).append(",\n");
        }

        sb.setLength(sb.length() - 2);
        sb.append("\n    };\n\n");

        return sb;
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final String enumName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
           "    static %s::Value get(const %s value)\n" +
           "    {\n" +
           "        switch (value)\n" +
           "        {\n",
           enumName,
           cpp99TypeName(tokens.get(0).encoding().primitiveType())
        ));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %s: return %s;\n",
                token.encoding().constVal().toString(),
                token.name())
            );
        }

        sb.append(String.format(
            "        }\n\n" +
            "        throw \"unknown value for enum %s\";\n" +
            "    };\n",
            enumName
        ));

        return sb;
    }

    private CharSequence generateFileHeader(final String namespaceName,
                                            final String className,
                                            final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "/* Generated class message */\n"
        ));

        sb.append(String.format(
            "#ifndef _%s_HPP_\n" +
            "#define _%s_HPP_\n\n" +
            "#include \"sbe/sbe.hpp\"\n\n",
            className.toUpperCase(),
            className.toUpperCase()
        ));

        if (typesToInclude != null)
        {
            for (final String incName : typesToInclude)
            {
                sb.append(String.format(
                        "#include \"%s/%s.hpp\"\n",
                        namespaceName,
                        toUpperFirstChar(incName)
                ));
            }
            sb.append("\n");
        }

        sb.append(String.format(
            "using namespace sbe;\n\n" +
            "namespace %s {\n\n",
            namespaceName
        ));

        return sb;
    }

    private CharSequence generateClassDeclaration(final String className, final String implementedInterface)
    {
        return String.format(
            "class %s : public %s\n" +
            "{\n",
            className,
            implementedInterface
        );
    }

    private CharSequence generateEnumDeclaration(final String name)
    {
        return "class " + name + "\n{\npublic:\n\n";
    }

    private CharSequence generatePrimitivePropertyEncodings(final String containingClassName,
                                                            final List<Token> tokens,
                                                            final String indent)
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

    private CharSequence generatePrimitiveProperty(final String containingClassName,
                                                   final String propertyName,
                                                   final Token token,
                                                   final String indent)
    {
        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            return generateConstPropertyMethods(propertyName, token, indent);
        }
        else
        {
            return generatePrimitivePropertyMethods(containingClassName, propertyName, token, indent);
        }
    }

    private CharSequence generatePrimitivePropertyMethods(final String containingClassName,
                                                          final String propertyName,
                                                          final Token token,
                                                          final String indent)
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

    private CharSequence generateSingleValueProperty(final String containingClassName,
                                                     final String propertyName,
                                                     final Token token,
                                                     final String indent)
    {
        final String cpp99TypeName = cpp99TypeName(token.encoding().primitiveType());
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %s %s(void) const\n" +
            indent + "    {\n" +
            indent + "        return %s(*((%s *)(buffer_ + offset_ + %d)));\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            cpp99TypeName,
            offset
        ));

        sb.append(String.format(
            indent + "    %s &%s(const %s value)\n" +
            indent + "    {\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d)) = %s(value);\n" +
            indent + "        return *this;\n" +
            indent + "    };\n",
            formatClassName(containingClassName),
            propertyName,
            cpp99TypeName,
            cpp99TypeName,
            offset,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        return sb;
    }

    private CharSequence generateArrayProperty(final String containingClassName,
                                               final String propertyName,
                                               final Token token,
                                               final String indent)
    {
        final String cpp99TypeName = cpp99TypeName(token.encoding().primitiveType());
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    int %sLength(void) const\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    };\n\n",
            propertyName,
            Integer.valueOf(token.arrayLength())
        ));

        sb.append(String.format(
            indent + "    %s %s(const int index) const\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw \"index out of range for %s\";\n" +
            indent + "        }\n\n" +
            indent + "        return %s(*((%s *)(buffer_ + offset_ + %d + (index * %d))));\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            Integer.valueOf(token.arrayLength()),
            propertyName,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            cpp99TypeName,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size())
        ));

        sb.append(String.format(
            indent + "    void %s(const int index, const %s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw \"index out of range for %s\";\n" +
            indent + "        }\n\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d + (index * %d))) = %s(value);\n" +
            indent + "    };\n\n",
            propertyName,
            cpp99TypeName,
            Integer.valueOf(token.arrayLength()),
            propertyName,
            cpp99TypeName,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size()),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        sb.append(String.format(
            indent + "    int get%s(char *dst, const int length) const\n" +
            indent + "    {\n" +
            indent + "        if (length > %d)\n" +
            indent + "        {\n" +
            indent + "             throw \"length too large for get%s\";\n" +
            indent + "        }\n\n" +
            indent + "        ::memcpy(dst, buffer_ + offset_ + %d, length);\n" +
            indent + "        return length;\n" +
            indent + "    };\n\n",
            toUpperFirstChar(propertyName),
            Integer.valueOf(token.arrayLength()),
            toUpperFirstChar(propertyName),
            offset
        ));

        sb.append(String.format(
            indent + "    %s &put%s(const char *src)\n" +
            indent + "    {\n" +
            indent + "        ::memcpy(buffer_ + offset_ + %d, src, %d);\n" +
            indent + "        return *this;\n" +
            indent + "    };\n",
            containingClassName,
            toUpperFirstChar(propertyName),
            offset,
            Integer.valueOf(token.arrayLength())
        ));

        return sb;
    }

    private CharSequence generateConstPropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final String cpp99TypeName = cpp99TypeName(token.encoding().primitiveType());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            return String.format(
                "\n" +
                indent + "    %s %s(void) const\n" +
                indent + "    {\n" +
                indent + "        return %s;\n" +
                indent + "    };\n",
                cpp99TypeName,
                propertyName,
                generateLiteral(token)
            );
        }

        final StringBuilder sb = new StringBuilder();

        final byte[] constantValue = token.encoding().constVal().byteArrayValue();
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
            indent + "    int %sLength(void) const\n" +
            indent + "    {\n" +
            indent + "        return %d;\n" +
            indent + "    };\n\n",
            propertyName,
            Integer.valueOf(constantValue.length)
        ));

        sb.append(String.format(
            indent + "    %s %s(const int index) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %sValues[] = {%s};\n\n" +
            indent + "        return %sValues[index];\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            propertyName,
            values,
            propertyName
        ));

        sb.append(String.format(
            indent + "    int get%s(char *dst, const int length) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %sValues[] = {%s};\n" +
            indent + "        int bytesToCopy = (length < sizeof(%sValues)) ? length : sizeof(%sValues);\n\n" +
            indent + "        ::memcpy(dst, %sValues, bytesToCopy);\n" +
            indent + "        return bytesToCopy;\n" +
            indent + "    };\n",
            toUpperFirstChar(propertyName),
            propertyName,
            values,
            propertyName,
            propertyName,
            propertyName,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        return String.format(
            "private:\n" +
            "    char *buffer_;\n" +
            "    int offset_;\n\n" +
            "public:\n" +
            "    %s &reset(char *buffer, const int offset)\n" +
            "    {\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "        return *this;\n" +
            "    };\n\n" +
            "    int size(void) const\n" +
            "    {\n" +
            "        return %s;\n" +
            "    };\n\n",
            className,
            Integer.valueOf(size)
        );
    }

    private CharSequence generateMessageFlyweightCode(final int blockLength,
                                                      final String className,
                                                      final long schemaId)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "private:\n" +
            "    char *buffer_;\n" +
            "    int offset_;\n" +
            "    int position_;\n\n"
        ));

        sb.append(String.format(
            "public:\n\n" +
            "    sbe_uint64_t blockLength(void) const\n" +
            "    {\n" +
            "        return %d;\n" +
            "    };\n\n" +
            "    sbe_uint64_t offset(void) const\n" +
            "    {\n" +
            "        return offset_;\n" +
            "    };\n\n" +
            "    %s &reset(char *buffer, const int offset)\n" +
            "    {\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "        position(offset + blockLength());\n" +
            "        return *this;\n" +
            "    };\n\n" +
            "    sbe_uint64_t position(void) const\n" +
            "    {\n" +
            "        return position_;\n" +
            "    };\n\n" +
            "    void position(const sbe_uint64_t position)\n" +
            "    {\n" +
            "        position_ = position;\n" +
            "    };\n\n" +
            "    int size(void) const\n" +
            "    {\n" +
            "        return position() - offset_;\n" +
            "    };\n\n" +
            "    int templateId(void) const\n" +
            "    {\n" +
            "        return %d;\n" +
            "    };\n\n" +
            "    char *buffer(void)\n" +
            "    {\n" +
            "        return buffer_;\n" +
            "    };\n\n" +
            "    MessageFlyweight *message(void)\n" +
            "    {\n" +
            "        return this;\n" +
            "    };\n",
            Integer.valueOf(blockLength),
            className,
            Long.valueOf(schemaId)
        ));

        return sb;
    }

    private CharSequence generateFields(final String containingClassName,
                                        final List<Token> tokens,
                                        final String indent)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = signalToken.name();

                sb.append(String.format(
                    "\n" +
                    indent + "    int %sId(void) const\n" +
                    indent + "    {\n" +
                    indent + "        return %d;\n" +
                    indent + "    };\n\n",
                    propertyName,
                    Long.valueOf(signalToken.schemaId())
                ));

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

    private CharSequence generateEnumProperty(final String containingClassName,
                                              final String propertyName,
                                              final Token token,
                                              final String indent)
    {
        final String enumName = token.name();
        final String typeName = cpp99TypeName(token.encoding().primitiveType());
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %s::Value %s(void) const\n" +
            indent + "    {\n" +
            indent + "        return %s::get(%s(*((%s *)(buffer_ + offset_ + %d))));\n" +
            indent + "    };\n\n",
            enumName,
            propertyName,
            enumName,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            typeName,
            offset
        ));

        sb.append(String.format(
            indent + "    %s &%s(const %s::Value value)\n" +
            indent + "    {\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d)) = %s(value);\n" +
            indent + "        return *this;\n" +
            indent + "    };\n",
            formatClassName(containingClassName),
            propertyName,
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
        final Integer offset = Integer.valueOf(token.offset());

        sb.append(String.format(
            "\n" +
            indent + "private:\n" +
            indent + "    %s %s_;\n\n" +
            indent + "public:\n",
            bitsetName,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %s &%s()\n" +
            indent + "    {\n" +
            indent + "        %s_.reset(buffer_, offset_ + %d);\n" +
            indent + "        return %s_;\n" +
            indent + "    };\n",
            bitsetName,
            propertyName,
            propertyName,
            offset,
            propertyName
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = token.name();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                "\n" +
                "private:\n" +
                indent + "    %s %s_;\n\n" +
                "public:\n",
                compositeName,
                propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %s &%s(void)\n" +
            indent + "    {\n" +
            indent + "        %s_.reset(buffer_, offset_ + %d);\n" +
            indent + "        return %s_;\n" +
            indent + "    };\n",
            compositeName,
            propertyName,
            propertyName,
            offset,
            propertyName
        ));

        return sb;
    }

    private CharSequence generateLiteral(final Token token)
    {
        String literal = "";

        final String castType = cpp99TypeName(token.encoding().primitiveType());
        switch (token.encoding().primitiveType())
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + token.encoding().constVal();
                break;

            case UINT32:
            case INT32:
                literal = token.encoding().constVal().toString();
                break;

            case FLOAT:
                literal = token.encoding().constVal() + "f";
                break;

            case UINT64:
            case INT64:
                literal = token.encoding().constVal() + "L";
                break;

            case DOUBLE:
                literal = token.encoding().constVal() + "d";
        }

        return literal;
    }
}
