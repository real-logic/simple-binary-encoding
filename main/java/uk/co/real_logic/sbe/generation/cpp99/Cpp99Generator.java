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
            out.append(generateFixedFlyweightCode());

            final List<Token> tokens = ir.header();
            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("};\n}\n#endif\n"); // close class, namespace, and ifndef
        }
    }

    public List<String> generateTypeStubs() throws IOException
    {
        List<String> typesToInclude = new ArrayList<>();

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
                out.append(generateFileHeader(ir.namespaceName(), className, typesToInclude));
                out.append(generateClassDeclaration(className, "MessageFlyweight"));
                out.append(generateMessageFlyweightCode(tokens.get(0).size(), typesToInclude));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(rootFields, BASE_INDENT));

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
                final String groupName = tokens.get(index).name();
                sb.append(generateGroupProperty(groupName, indent));

                generateGroupClassHeader(sb, groupName, tokens, index, indent + INDENT);

                final List<Token> rootFields = new ArrayList<>();
                index = collectRootFields(tokens, ++index, rootFields);
                sb.append(generateFields(rootFields, indent + INDENT));

                if (tokens.get(index).signal() == Signal.BEGIN_GROUP)
                {
                    index = generateGroups(sb, tokens, index, indent + INDENT);
                }

                sb.append(indent).append("    }\n");
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
            indent + "class %s : pubic GroupFlyweight\n" +
            indent + "{\n" +
            indent + "    private final %s dimensions = new %s();\n" +
            indent + "    private int blockLength;\n" +
            indent + "    private int size;\n" +
            indent + "    private int index;\n" +
            indent + "    private int offset;\n\n",
            formatClassName(groupName),
            dimensionsClassName,
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    public void resetForDecode()\n" +
            indent + "    {\n" +
            indent + "        dimensions.reset(buffer, position());\n" +
            indent + "        size = dimensions.numInGroup();\n" +
            indent + "        blockLength = dimensions.blockLength();\n" +
            indent + "        index = -1;\n" +
            indent + "        final int dimensionsHeaderSize = %d;\n" +
            indent + "        position(position() + dimensionsHeaderSize);\n" +
            indent + "    }\n\n",
            dimensionHeaderSize
        ));

        final Integer blockLength = Integer.valueOf(tokens.get(index).size());
        final String cpp99TypeForBlockLength = cpp99TypeName(tokens.get(index + 2).encoding().primitiveType());
        final String cpp99TypeForNumInGroup = cpp99TypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    public void resetForEncode(final int size)\n" +
            indent + "    {\n" +
            indent + "        dimensions.reset(buffer, position());\n" +
            indent + "        dimensions.numInGroup((%s)size);\n" +
            indent + "        dimensions.blockLength((%s)%d);\n" +
            indent + "        index = -1;\n" +
            indent + "        this.size = size;\n" +
            indent + "        blockLength = %d;\n" +
            indent + "        final int dimensionsHeaderSize = %d;\n" +
            indent + "        position(position() + dimensionsHeaderSize);\n" +
            indent + "    }\n\n",
            cpp99TypeForNumInGroup,
            cpp99TypeForBlockLength,
            blockLength,
            blockLength,
            dimensionHeaderSize
        ));

        sb.append(
            indent + "    public int size()\n" +
            indent + "    {\n" +
            indent + "        return size;\n" +
            indent + "    }\n\n"
        );

        sb.append(
            indent + "    public boolean next()\n" +
            indent + "    {\n" +
            indent + "        if (index + 1 >= size)\n" +
            indent + "        {\n" +
            indent + "            return false;\n" +
            indent + "        }\n\n" +
            indent + "        offset = position();\n" +
            indent + "        position(offset + blockLength);\n" +
            indent + "        ++index;\n\n" +
            indent + "        return true;\n" +
            indent + "    }\n"
        );
    }

    private CharSequence generateGroupProperty(final String groupName, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        sb.append(String.format(
            "\n" +
            indent + "    private final %s %s = new %s();\n",
            className,
            propertyName,
            className
        ));

        sb.append(String.format(
            "\n" +
            indent + "    public %s %s()\n" +
            indent + "    {\n" +
            indent + "        %s.resetForDecode();\n" +
            indent + "        return %s;\n" +
            indent + "    }\n",
            className,
            propertyName,
            propertyName,
            propertyName
        ));


        sb.append(String.format(
            "\n" +
                indent + "    public %s %sSize(final int size)\n" +
                indent + "    {\n" +
                indent + "        %s.resetForEncode(size);\n" +
                indent + "        return %s;\n" +
                indent + "    }\n",
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
                    "    public String %sCharacterEncoding()\n" +
                    "    {\n" +
                    "        return \"%s\";\n" +
                    "    }\n\n",
                    formatPropertyName(propertyName),
                    characterEncoding
                ));

                final Token lengthToken = tokens.get(i + 2);
                final Integer sizeOfLengthField = Integer.valueOf(lengthToken.size());
                final String lengthCpp99Type = cpp99TypeName(lengthToken.encoding().primitiveType());
                final String lengthTypePrefix = lengthToken.encoding().primitiveType().primitiveName();

                sb.append(String.format(
                    "    public int get%s(final byte[] dst, final int offset, final int length)\n" +
                    "    {\n" +
                    "        final int sizeOfLengthField = %d;\n" +
                    "        final int lengthPosition = position();\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        final int dataLength = CodecUtil.%sGet(buffer, lengthPosition);\n" +
                    "        final int bytesCopied = Math.min(length, dataLength);\n" +
                    "        CodecUtil.int8sGet(buffer, position(), dst, offset, bytesCopied);\n" +
                    "        position(position() + dataLength);\n" +
                    "        return bytesCopied;\n" +
                    "    }\n\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix
                ));

                sb.append(String.format(
                    "    public int put%s(final byte[] src, final int offset, final int length)\n" +
                    "    {\n" +
                    "        final int sizeOfLengthField = %d;\n" +
                    "        final int lengthPosition = position();\n" +
                    "        CodecUtil.%sPut(buffer, lengthPosition, (%s)length);\n" +
                    "        position(lengthPosition + sizeOfLengthField);\n" +
                    "        CodecUtil.int8sPut(buffer, position(), src, offset, length);\n" +
                    "        position(position() + length);\n" +
                    "        return length;" +
                    "    }\n",
                    propertyName,
                    sizeOfLengthField,
                    lengthTypePrefix,
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
            out.append(generateFileHeader(ir.namespaceName(), bitSetName, null));
            out.append(generateClassDeclaration(bitSetName, "FixedFlyweight"));
            out.append(generateFixedFlyweightCode());

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.namespaceName(), enumName, null));
            out.append(generateEnumDeclaration(enumName));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1)));
            // out.append(generateEnumBody(tokens.get(0), enumName));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumName));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.namespaceName(), compositeName, null));
            out.append(generateClassDeclaration(compositeName, "FixedFlyweight"));
            out.append(generateFixedFlyweightCode());

            out.append(generatePrimitivePropertyEncodings(tokens.subList(1, tokens.size() - 1), BASE_INDENT));

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
                    "        return (*((%s *)(buffer_ + offset_)) & (uint64_t)0x1 << %s)) ? true : false;\n" +
                    "    };\n\n",
                    choiceName,
                    typeName,
                    choiceBitPosition
                ));

                sb.append(String.format(
                    "    %s &%s(const bool value)\n" +
                    "    {\n" +
                    "        *((%s *)(buffer_ + offset_)) |= (uint64_t)value << %s);\n" +
                    "        return *this;\n" +
                    "    };\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
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

    private CharSequence generateEnumBody(final Token token, final String enumName)
    {
        final String cpp99EncodingType = cpp99TypeName(token.encoding().primitiveType());

        return String.format(
            "    %s value_;\n\n"+
            "    void value(const %s value)\n" +
            "    {\n" +
            "        value_ = value;\n" +
            "    };\n\n" +
            "    %s value(void) const\n" +
            "    {\n" +
            "        return value_;\n" +
            "    };\n\n",
            cpp99EncodingType,
            cpp99EncodingType,
            cpp99EncodingType
        );
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

        sb.append(
            "        }\n\n" +
            "        throw \"unknown value enum value\";\n" +
            "    };\n"
        );

        return sb;
    }

    private CharSequence generateFileHeader(final String namespaceName,
                                            final String className,
                                            final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "/* Generated class message */\n" +
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
                        incName
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
        return "class " + name + "\n{\n";
    }

    private CharSequence generatePrimitivePropertyEncodings(final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                sb.append(generatePrimitiveProperty(token.name(), token, indent));
            }
        }

       return sb;
    }

    private CharSequence generatePrimitiveProperty(final String propertyName, final Token token, final String indent)
    {
        if (Encoding.Presence.CONSTANT == token.encoding().presence())
        {
            return generateConstPropertyMethods(propertyName, token, indent);
        }
        else
        {
            return generatePrimitivePropertyMethods(propertyName, token, indent);
        }
    }

    private CharSequence generatePrimitivePropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final int arrayLength = token.arrayLength();

        if (arrayLength == 1)
        {
            return generateSingleValueProperty(propertyName, token, indent);
        }
        else if (arrayLength > 1)
        {
            return generateArrayProperty(propertyName, token, indent);
        }

        return "";
    }

    private CharSequence generateSingleValueProperty(final String propertyName, final Token token, final String indent)
    {
        final String cpp99TypeName = cpp99TypeName(token.encoding().primitiveType());
        final String typePrefix = token.encoding().primitiveType().primitiveName();
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %s %s(void) const\n" +
            indent + "    {\n" +
            indent + "        return *((%s *)(buffer_ + offset_ + %d));\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            cpp99TypeName,
            offset
        ));

        sb.append(String.format(
            indent + "    void %s(const %s value)\n" +
            indent + "    {\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d)) = value;\n" +
            indent + "    };\n",
            propertyName,
            cpp99TypeName,
            cpp99TypeName,
            offset
        ));

        return sb;
    }

    private CharSequence generateArrayProperty(final String propertyName, final Token token, final String indent)
    {
        final String cpp99TypeName = cpp99TypeName(token.encoding().primitiveType());
        final String typePrefix = token.encoding().primitiveType().primitiveName();
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
            indent + "    %s %s(const int index) const throw\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw \"index out of range for %s\";\n" +
            indent + "        }\n\n" +
            indent + "        return *((%s *)(buffer_ + offset_ + %d + (index * %d)));\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            Integer.valueOf(token.arrayLength()),
            propertyName,
            cpp99TypeName,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size())
        ));

        sb.append(String.format(
            indent + "    void %s(const int index, const %s value) throw\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %d)\n" +
            indent + "        {\n" +
            indent + "            throw \"index out of range for %s\";\n" +
            indent + "        }\n\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d + (index * %d))) = value;\n" +
            indent + "    };\n\n",
            propertyName,
            cpp99TypeName,
            Integer.valueOf(token.arrayLength()),
            propertyName,
            typePrefix,
            offset,
            Integer.valueOf(token.encoding().primitiveType().size())
        ));

        sb.append(String.format(
            indent + "    int get%s(char *dst, const int offset, const int length) const\n" +
            indent + "    {\n" +
            indent + "        if (offset < 0)\n" +
            indent + "        {\n" +
            indent + "             throw \"offset out of range for get%s\";\n" +
            indent + "        }\n\n" +
            indent + "        ::memcpy(dst + offset, buffer_ + offset_ + %d, length);\n" +
            indent + "        return length;\n" +
            indent + "    };\n\n",
            toUpperFirstChar(propertyName),
            toUpperFirstChar(propertyName),
            offset
        ));

        sb.append(String.format(
            indent + "    int put%s(const char *src, const int offset, const int length)\n" +
            indent + "    {\n" +
            indent + "        if (offset < 0)\n" +
            indent + "        {\n" +
            indent + "            throw \"offset out of range for put%s\";\n" +
            indent + "        }\n\n" +
            indent + "        ::memcpy(buffer_ + offset_ + %d, src + offset, length);\n" +
            indent + "        return length;\n" +
            indent + "    };\n",
            toUpperFirstChar(propertyName),
            toUpperFirstChar(propertyName),
            offset
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
            indent + "        return %sValue[index];\n" +
            indent + "    };\n\n",
            cpp99TypeName,
            propertyName,
            propertyName,
            values,
            propertyName
        ));

        sb.append(String.format(
            indent + "    int get%s(char *dst, const int offset, const int length) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %sValues[] = {%s};\n" +
            indent + "        int bytesToCopy = (length < sizeof(%sValues)) ? length : sizeof(%sValues);\n\n" +
            indent + "        if (offset < 0)\n" +
            indent + "        {\n" +
            indent + "            throw \"offset out of range for get%s\";\n" +
            indent + "        }\n\n" +
            indent + "        ::memcpy(dst + offset, %sValues, bytesToCopy);\n" +
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

    private CharSequence generateFixedFlyweightCode()
    {
        return
            "private:\n" +
            "    char *buffer_;\n" +
            "    int offset_;\n\n" +
            "public:\n" +
            "    void reset(char *buffer, const int offset)\n" +
            "    {\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "    };\n";
    }

    private CharSequence generateMessageFlyweightCode(final int blockLength, final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "private:\n" +
            "    char *buffer_;\n" +
            "    int offset_;\n" +
            "    int position_;\n"
        ));

        if (typesToInclude != null)
        {
            for (final String incName : typesToInclude)
            {
                sb.append(String.format(
                   "    %s %s_;\n",
                   formatClassName(incName),
                   toLowerFirstChar(formatClassName(incName))
                ));
            }
            sb.append("\n");
        }

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
            "    void reset(char *buffer, const int offset)\n" +
            "    {\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "        position(blockLength());\n" +
            "    };\n\n" +
            "    sbe_uint64_t position(void) const\n" +
            "    {\n" +
            "        return position_;\n" +
            "    };\n\n" +
            "    void position(const sbe_uint64_t position)\n" +
            "    {\n" +
            "        position_ = position;\n" +
            "    };\n\n",
            Integer.valueOf(blockLength)
        ));

        return sb;
    }

    private CharSequence generateFields(final List<Token> tokens, final String indent)
    {
        StringBuilder sb = new StringBuilder();

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
                        sb.append(generatePrimitiveProperty(propertyName, encodingToken, indent));
                        break;

                    case BEGIN_ENUM:
                        sb.append(generateEnumProperty(propertyName, encodingToken, indent));
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

    private CharSequence generateEnumProperty(final String propertyName, final Token token, final String indent)
    {
        final String enumName = token.name();
        final String typeName = cpp99TypeName(token.encoding().primitiveType());
        final Integer offset = Integer.valueOf(token.offset());

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %s::Value %s(void) const\n" +
            indent + "    {\n" +
            indent + "        return %s::get(*((%s *)(buffer_ + offset_ + %d)));\n" +
            indent + "    };\n\n",
            enumName,
            propertyName,
            enumName,
            typeName,
            offset
        ));

        sb.append(String.format(
            indent + "    void %s(const %s::Value value)\n" +
            indent + "    {\n" +
            indent + "        *((%s *)(buffer_ + offset_ + %d)) = value;\n" +
            indent + "    };\n",
            propertyName,
            enumName,
            typeName,
            offset
        ));

        return sb;
    }

    private Object generateBitsetProperty(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String bitsetName = formatClassName(token.name());
        final String formattedPropertyName = formatPropertyName(propertyName);
        final Integer offset = Integer.valueOf(token.offset());

        sb.append(String.format(
            "\n" +
            indent + "    %s &%s()\n" +
            indent + "    {\n" +
            indent + "        %s_.reset(buffer_, offset_ + %d);\n" +
            indent + "        return %s_;\n" +
            indent + "    }\n",
            bitsetName,
            formattedPropertyName,
            formattedPropertyName,
            offset,
            formattedPropertyName
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
