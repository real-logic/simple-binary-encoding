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
package uk.co.real_logic.sbe.generation.cpp98;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.ir.*;
import uk.co.real_logic.agrona.Verify;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static uk.co.real_logic.sbe.generation.cpp98.Cpp98Util.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectRootFields;

public class Cpp98Generator implements CodeGenerator
{
    private static final String BASE_INDENT = "";
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public Cpp98Generator(final Ir ir, final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    public void generateMessageHeaderStub() throws IOException
    {
        final String messageHeader = "MessageHeader";

        try (final Writer out = outputManager.createOutput(messageHeader))
        {
            final List<Token> tokens = ir.headerStructure().tokens();
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), messageHeader, null));
            out.append(generateClassDeclaration(messageHeader));
            out.append(generateFixedFlyweightCode(messageHeader, tokens.get(0).encodedLength()));
            out.append(generatePrimitivePropertyEncodings(
                messageHeader, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

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
            final Token msgToken = tokens.get(0);
            final String className = formatClassName(msgToken.name());

            try (final Writer out = outputManager.createOutput(className))
            {
                out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), className, typesToInclude));
                out.append(generateClassDeclaration(className));
                out.append(generateMessageFlyweightCode(className, msgToken));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int offset = 0;

                final List<Token> rootFields = new ArrayList<>();
                offset = collectRootFields(messageBody, offset, rootFields);
                out.append(generateFields(className, rootFields, BASE_INDENT));

                final List<Token> groups = new ArrayList<>();
                offset = collectGroups(messageBody, offset, groups);
                final StringBuilder sb = new StringBuilder();
                generateGroups(sb, groups, 0, BASE_INDENT);
                out.append(sb);

                final List<Token> varData = messageBody.subList(offset, messageBody.size());
                out.append(generateVarData(varData));

                out.append("};\n}\n#endif\n");
            }
        }
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

    private void generateGroupClassHeader(
        final StringBuilder sb, final String groupName, final List<Token> tokens, final int index, final String indent)
    {
        final String dimensionsClassName = formatClassName(tokens.get(index + 1).name());
        final int dimensionHeaderLength = tokens.get(index + 1).encodedLength();

        sb.append(String.format(
            "\n" +
            indent + "class %1$s\n" +
            indent + "{\n" +
            indent + "private:\n" +
            indent + "    char *buffer_;\n" +
            indent + "    int bufferLength_;\n" +
            indent + "    int *positionPtr_;\n" +
            indent + "    int blockLength_;\n" +
            indent + "    int count_;\n" +
            indent + "    int index_;\n" +
            indent + "    int offset_;\n" +
            indent + "    int actingVersion_;\n" +
            indent + "    %2$s dimensions_;\n\n" +
            indent + "public:\n\n",
            formatClassName(groupName),
            dimensionsClassName
        ));

        sb.append(String.format(
            indent + "    inline void wrapForDecode(char *buffer, int *pos, const int actingVersion," +
            indent + " const int bufferLength)\n" +
            indent + "    {\n" +
            indent + "        buffer_ = buffer;\n" +
            indent + "        bufferLength_ = bufferLength;\n" +
            indent + "        dimensions_.wrap(buffer_, *pos, actingVersion, bufferLength);\n" +
            indent + "        blockLength_ = dimensions_.blockLength();\n" +
            indent + "        count_ = dimensions_.numInGroup();\n" +
            indent + "        index_ = -1;\n" +
            indent + "        actingVersion_ = actingVersion;\n" +
            indent + "        positionPtr_ = pos;\n" +
            indent + "        *positionPtr_ = *positionPtr_ + %1$d;\n" +
            indent + "    }\n\n",
            dimensionHeaderLength
        ));

        final int blockLength = tokens.get(index).encodedLength();
        final String cpp98TypeForBlockLength = cpp98TypeName(tokens.get(index + 2).encoding().primitiveType());
        final String cpp98TypeForNumInGroup = cpp98TypeName(tokens.get(index + 3).encoding().primitiveType());

        sb.append(String.format(
            indent + "    inline void wrapForEncode(char *buffer, const int count," +
            indent + " int *pos, const int actingVersion, const int bufferLength)\n" +
            indent + "    {\n" +
            indent + "        buffer_ = buffer;\n" +
            indent + "        bufferLength_ = bufferLength;\n" +
            indent + "        dimensions_.wrap(buffer_, *pos, actingVersion, bufferLength);\n" +
            indent + "        dimensions_.blockLength((%1$s)%2$d);\n" +
            indent + "        dimensions_.numInGroup((%3$s)count);\n" +
            indent + "        index_ = -1;\n" +
            indent + "        count_ = count;\n" +
            indent + "        blockLength_ = %2$d;\n" +
            indent + "        actingVersion_ = actingVersion;\n" +
            indent + "        positionPtr_ = pos;\n" +
            indent + "        *positionPtr_ = *positionPtr_ + %4$d;\n" +
            indent + "    }\n\n",
            cpp98TypeForBlockLength, blockLength, cpp98TypeForNumInGroup, dimensionHeaderLength
        ));

        sb.append(String.format(
            indent + "    static const int sbeHeaderSize()\n" +
            indent + "    {\n" +
            indent + "        return %1$d;\n" +
            indent + "    }\n\n" +
            indent + "    static const int sbeBlockLength()\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n" +
            indent + "    inline int count(void) const\n" +
            indent + "    {\n" +
            indent + "        return count_;\n" +
            indent + "    }\n\n" +
            indent + "    inline bool hasNext(void) const\n" +
            indent + "    {\n" +
            indent + "        return index_ + 1 < count_;\n" +
            indent + "    }\n\n" +
            indent + "    inline %3$s &next(void)\n" +
            indent + "    {\n" +
            indent + "        offset_ = *positionPtr_;\n" +
            indent + "        if (SBE_BOUNDS_CHECK_EXPECT(( (offset_ + blockLength_) > bufferLength_ ), false))\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"buffer too short to support next group index [E108]\");\n" +
            indent + "        }\n" +
            indent + "        *positionPtr_ = offset_ + blockLength_;\n" +
            indent + "        ++index_;\n\n" +
            indent + "        return *this;\n" +
            indent + "    }\n\n",
            dimensionHeaderLength, blockLength, formatClassName(groupName)
        ));

        sb.append(String.format(
            indent + "#if __cplusplus < 201103L\n" +
            indent + "    template<class Func>\n" +
            indent + "    inline void forEach(Func& func)\n" +
            indent + "    {\n" +
            indent + "        while(hasNext())\n" +
            indent + "        {\n" +
            indent + "            next(); func(*this);\n" +
            indent + "        }\n" +
            indent + "    }\n\n" +
            indent + "#else\n" +
            indent + "    inline void forEach(std::function<void(%1$s&)> func)\n" +
            indent + "    {\n" +
            indent + "        while(hasNext())\n" +
            indent + "        {\n" +
            indent + "            next(); func(*this);\n" +
            indent + "        }\n" +
            indent + "    }\n\n" +
            indent + "#endif\n\n",
            formatClassName(groupName)
        ));
    }

    private CharSequence generateGroupProperty(final String groupName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final String className = formatClassName(groupName);
        final String propertyName = formatPropertyName(groupName);

        sb.append(String.format(
            "\n" +
            "private:\n" +
            indent + "    %1$s %2$s_;\n\n" +
            "public:\n",
            className,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    static const int %1$sId(void)\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n",
            groupName,
            (long)token.id()
        ));

        sb.append(String.format(
            "\n" +
            indent + "    inline %1$s &%2$s(void)\n" +
            indent + "    {\n" +
            indent + "        %2$s_.wrapForDecode(buffer_, positionPtr_, actingVersion_, bufferLength_);\n" +
            indent + "        return %2$s_;\n" +
            indent + "    }\n",
            className,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %1$s &%2$sCount(const int count)\n" +
            indent + "    {\n" +
            indent + "        %2$s_.wrapForEncode(buffer_, count, positionPtr_, actingVersion_, bufferLength_);\n" +
            indent + "        return %2$s_;\n" +
            indent + "    }\n",
            className,
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
                final Token lengthToken = tokens.get(i + 2);
                final int lengthOfLengthField = lengthToken.encodedLength();
                final String lengthCpp98Type = cpp98TypeName(lengthToken.encoding().primitiveType());

                generateFieldMetaAttributeMethod(sb, token, BASE_INDENT);

                generateVarDataDescriptors(
                    sb, token, propertyName, characterEncoding, lengthToken, lengthOfLengthField, lengthCpp98Type);

                sb.append(String.format(
                    "    const char *%1$s(void)\n" +
                    "    {\n" +
                             "%2$s" +
                    "         const char *fieldPtr = (buffer_ + position() + %3$d);\n" +
                    "         position(position() + %3$d + *((%4$s *)(buffer_ + position())));\n" +
                    "         return fieldPtr;\n" +
                    "    }\n\n",
                    formatPropertyName(propertyName),
                    generateTypeFieldNotPresentCondition(token.version(), BASE_INDENT),
                    lengthOfLengthField,
                    lengthCpp98Type
                ));

                sb.append(String.format(
                    "    int get%1$s(char *dst, const int length)\n" +
                    "    {\n" +
                            "%2$s" +
                    "        sbe_uint64_t lengthOfLengthField = %3$d;\n" +
                    "        sbe_uint64_t lengthPosition = position();\n" +
                    "        position(lengthPosition + lengthOfLengthField);\n" +
                    "        sbe_int64_t dataLength = %4$s(*((%5$s *)(buffer_ + lengthPosition)));\n" +
                    "        int bytesToCopy = (length < dataLength) ? length : dataLength;\n" +
                    "        sbe_uint64_t pos = position();\n" +
                    "        position(position() + (sbe_uint64_t)dataLength);\n" +
                    "        ::memcpy(dst, buffer_ + pos, bytesToCopy);\n" +
                    "        return bytesToCopy;\n" +
                    "    }\n\n",
                    propertyName,
                    generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
                    lengthOfLengthField,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
                    lengthCpp98Type
                ));

                sb.append(String.format(
                    "    int put%1$s(const char *src, const int length)\n" +
                    "    {\n" +
                    "        sbe_uint64_t lengthOfLengthField = %2$d;\n" +
                    "        sbe_uint64_t lengthPosition = position();\n" +
                    "        *((%3$s *)(buffer_ + lengthPosition)) = %4$s((%3$s)length);\n" +
                    "        position(lengthPosition + lengthOfLengthField);\n" +
                    "        sbe_uint64_t pos = position();\n" +
                    "        position(position() + (sbe_uint64_t)length);\n" +
                    "        ::memcpy(buffer_ + pos, src, length);\n" +
                    "        return length;\n" +
                    "    }\n",
                    propertyName,
                    lengthOfLengthField,
                    lengthCpp98Type,
                    formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType())
                ));
            }
        }

        return sb;
    }

    private void generateVarDataDescriptors(
        final StringBuilder sb,
        final Token token,
        final String propertyName,
        final String characterEncoding,
        final Token lengthToken,
        final Integer sizeOfLengthField,
        final String lengthCpp98Type)
    {
        sb.append(String.format(
            "\n"  +
            "    static const char *%1$sCharacterEncoding()\n" +
            "    {\n" +
            "        return \"%2$s\";\n" +
            "    }\n\n",
            formatPropertyName(propertyName),
            characterEncoding
        ));

        sb.append(String.format(
            "    static const int %1$sSinceVersion(void)\n" +
            "    {\n" +
            "         return %2$d;\n" +
            "    }\n\n" +
            "    bool %1$sInActingVersion(void)\n" +
            "    {\n" +
            "        return (actingVersion_ >= %2$s) ? true : false;\n" +
            "    }\n\n" +
            "    static const int %1$sId(void)\n" +
            "    {\n" +
            "        return %3$d;\n" +
            "    }\n\n",
            formatPropertyName(propertyName),
            (long)token.version(),
            token.id()
        ));

        sb.append(String.format(
            "\n" +
            "    static const int %sHeaderSize()\n" +
            "    {\n" +
            "        return %d;\n" +
            "    }\n",
            toLowerFirstChar(propertyName),
            sizeOfLengthField
        ));

        sb.append(String.format(
            "\n" +
            "    sbe_int64_t %1$sLength(void) const\n" +
            "    {\n" +
                    "%2$s" +
            "        return %3$s(*((%4$s *)(buffer_ + position())));\n" +
            "    }\n\n",
            formatPropertyName(propertyName),
            generateArrayFieldNotPresentCondition(token.version(), BASE_INDENT),
            formatByteOrderEncoding(lengthToken.encoding().byteOrder(), lengthToken.encoding().primitiveType()),
            lengthCpp98Type
        ));
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final String bitSetName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(bitSetName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), bitSetName, null));
            out.append(generateClassDeclaration(bitSetName));
            out.append(generateFixedFlyweightCode(bitSetName, tokens.get(0).encodedLength()));

            out.append(String.format(
                "\n" +
                "    %1$s &clear(void)\n" +
                "    {\n" +
                "        *((%2$s *)(buffer_ + offset_)) = 0;\n" +
                "        return *this;\n" +
                "    }\n\n",
                bitSetName,
                cpp98TypeName(tokens.get(0).encoding().primitiveType())
            ));

            out.append(generateChoices(bitSetName, tokens.subList(1, tokens.size() - 1)));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(enumName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), enumName, null));
            out.append(generateEnumDeclaration(enumName));

            out.append(generateEnumValues(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append(generateEnumLookupMethod(tokens.subList(1, tokens.size() - 1), enumToken));

            out.append("};\n}\n#endif\n");
        }
    }

    private void generateComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = formatClassName(tokens.get(0).name());

        try (final Writer out = outputManager.createOutput(compositeName))
        {
            out.append(generateFileHeader(ir.applicableNamespace().replace('.', '_'), compositeName, null));
            out.append(generateClassDeclaration(compositeName));
            out.append(generateFixedFlyweightCode(compositeName, tokens.get(0).encodedLength()));

            out.append(generatePrimitivePropertyEncodings(compositeName, tokens.subList(1, tokens.size() - 1), BASE_INDENT));

            out.append("};\n}\n#endif\n");
        }
    }

    private CharSequence generateChoiceNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return false;\n" +
            indent + "        }\n\n",
            sinceVersion
        );
    }

    private CharSequence generateChoices(final String bitsetClassName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.CHOICE)
            {
                final String choiceName = token.name();
                final String typeName = cpp98TypeName(token.encoding().primitiveType());
                final String choiceBitPosition = token.encoding().constValue().toString();
                final String byteOrderStr = formatByteOrderEncoding(
                    token.encoding().byteOrder(), token.encoding().primitiveType());

                sb.append(String.format(
                    "\n" +
                    "    bool %1$s(void) const\n" +
                    "    {\n" +
                            "%2$s" +
                    "        return (%3$s(*((%4$s *)(buffer_ + offset_))) & (0x1L << %5$s)) ? true : false;\n" +
                    "    }\n\n",
                    choiceName,
                    generateChoiceNotPresentCondition(token.version(), BASE_INDENT),
                    byteOrderStr,
                    typeName,
                    choiceBitPosition
                ));

                sb.append(String.format(
                    "    %1$s &%2$s(const bool value)\n" +
                    "    {\n" +
                    "        %3$s bits = %4$s(*((%3$s *)(buffer_ + offset_)));\n" +
                    "        bits = value ? (bits | (0x1L << %5$s)) : (bits & ~(0x1L << %5$s));\n" +
                    "        *((%3$s *)(buffer_ + offset_)) = %4$s(bits);\n" +
                    "        return *this;\n" +
                    "    }\n",
                    bitsetClassName,
                    choiceName,
                    typeName,
                    byteOrderStr,
                    choiceBitPosition
                ));
            }
        }

        return sb;
    }

    private CharSequence generateEnumValues(final List<Token> tokens, final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        sb.append(
            "    enum Value \n" +
            "    {\n"
        );

        for (final Token token : tokens)
        {
            final CharSequence constVal = generateLiteral(
                token.encoding().primitiveType(), token.encoding().constValue().toString());
            sb.append("        ").append(token.name()).append(" = ").append(constVal).append(",\n");
        }

        sb.append(String.format(
            "        NULL_VALUE = %1$s",
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString())
        ));

        sb.append("\n    };\n\n");

        return sb;
    }

    private CharSequence generateEnumLookupMethod(final List<Token> tokens, final Token encodingToken)
    {
        final String enumName = formatClassName(encodingToken.name());
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
           "    static %1$s::Value get(const %2$s value)\n" +
           "    {\n" +
           "        switch (value)\n" +
           "        {\n",
           enumName,
           cpp98TypeName(tokens.get(0).encoding().primitiveType())
        ));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "            case %1$s: return %2$s;\n",
                token.encoding().constValue().toString(),
                token.name())
            );
        }

        sb.append(String.format(
            "            case %1$s: return NULL_VALUE;\n" +
            "        }\n\n" +
            "        throw std::runtime_error(\"unknown value for enum %2$s [E103]\");\n" +
            "    }\n",
            encodingToken.encoding().applicableNullValue().toString(),
            enumName
        ));

        return sb;
    }

    private CharSequence generateFieldNotPresentCondition(final int sinceVersion, final Encoding encoding, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return %2$s;\n" +
            indent + "        }\n\n",
            sinceVersion,
            generateLiteral(encoding.primitiveType(), encoding.applicableNullValue().toString())
        );
    }

    private CharSequence generateArrayFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return 0;\n" +
            indent + "        }\n\n",
            sinceVersion
        );
    }

    private CharSequence generateTypeFieldNotPresentCondition(final int sinceVersion, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return NULL;\n" +
            indent + "        }\n\n",
            sinceVersion
        );
    }

    private CharSequence generateFileHeader(final String namespaceName, final String className, final List<String> typesToInclude)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("/* Generated SBE (Simple Binary Encoding) message codec */\n");

        sb.append(String.format(
            "#ifndef _%1$s_%2$s_HPP_\n" +
            "#define _%1$s_%2$s_HPP_\n\n" +
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
            "#include <sbe/sbe.hpp>\n\n",
            namespaceName.toUpperCase(),
            className.toUpperCase()
        ));

        if (typesToInclude != null)
        {
            for (final String incName : typesToInclude)
            {
                sb.append(String.format(
                    "#include \"%1$s.hpp\"\n",
                    toUpperFirstChar(incName)
                ));
            }
            sb.append("\n");
        }

        sb.append(String.format(
            "using namespace sbe;\n\n" +
            "namespace %1$s {\n\n",
            namespaceName
        ));

        return sb;
    }

    private CharSequence generateClassDeclaration(final String className)
    {
        return String.format(
            "class %s\n" +
            "{\n",
            className
        );
    }

    private CharSequence generateEnumDeclaration(final String name)
    {
        return "class " + name + "\n{\npublic:\n\n";
    }

    private CharSequence generatePrimitivePropertyEncodings(
        final String containingClassName, final List<Token> tokens, final String indent)
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

    private CharSequence generatePrimitiveProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(generatePrimitiveFieldMetaData(propertyName, token, indent));

        if (token.isConstantEncoding())
        {
            sb.append(generateConstPropertyMethods(propertyName, token, indent));
        }
        else
        {
            sb.append(generatePrimitivePropertyMethods(containingClassName, propertyName, token, indent));
        }

        return sb;
    }

    private CharSequence generatePrimitivePropertyMethods(
        final String containingClassName, final String propertyName, final Token token, final String indent)
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

    private CharSequence generatePrimitiveFieldMetaData(final String propertyName, final Token token, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String cpp98TypeName = cpp98TypeName(primitiveType);
        final CharSequence nullValueString = generateNullValueLiteral(primitiveType, encoding);

        sb.append(String.format(
            "\n" +
            indent + "    static const %1$s %2$sNullValue()\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cpp98TypeName,
            propertyName,
            nullValueString
        ));

        sb.append(String.format(
            "\n" +
            indent + "    static const %1$s %2$sMinValue()\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cpp98TypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMinValue().toString())
        ));

        sb.append(String.format(
            "\n" +
            indent + "    static const %1$s %2$sMaxValue()\n" +
            indent + "    {\n" +
            indent + "        return %3$s;\n" +
            indent + "    }\n",
            cpp98TypeName,
            propertyName,
            generateLiteral(primitiveType, token.encoding().applicableMaxValue().toString())
        ));

        return sb;
    }

    private CharSequence generateSingleValueProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String cpp98TypeName = cpp98TypeName(token.encoding().primitiveType());
        final int offset = token.offset();
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %1$s %2$s(void) const\n" +
            indent + "    {\n" +
                              "%3$s" +
            indent + "        return %4$s(*((%1$s *)(buffer_ + offset_ + %5$d)));\n" +
            indent + "    }\n\n",
            cpp98TypeName,
            propertyName,
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            offset
        ));

        sb.append(String.format(
            indent + "    %1$s &%2$s(const %3$s value)\n" +
            indent + "    {\n" +
            indent + "        *((%3$s *)(buffer_ + offset_ + %4$d)) = %5$s(value);\n" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            formatClassName(containingClassName),
            propertyName,
            cpp98TypeName,
            offset,
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        return sb;
    }

    private CharSequence generateArrayProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String cpp98TypeName = cpp98TypeName(token.encoding().primitiveType());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    static const int %1$sLength(void)\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n",
            propertyName,
            token.arrayLength()
        ));

        sb.append(String.format(
            indent + "    const char *%1$s(void) const\n" +
            indent + "    {\n" +
                              "%2$s" +
            indent + "        return (buffer_ + offset_ + %3$d);\n" +
            indent + "    }\n\n",
            propertyName,
            generateTypeFieldNotPresentCondition(token.version(), indent),
            offset
        ));

        sb.append(String.format(
            indent + "    %1$s %2$s(const int index) const\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %3$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"index out of range for %2$s [E104]\");\n" +
            indent + "        }\n\n" +
                             "%4$s" +
            indent + "        return %5$s(*((%1$s *)(buffer_ + offset_ + %6$d + (index * %7$d))));\n" +
            indent + "    }\n\n",
            cpp98TypeName,
            propertyName,
            token.arrayLength(),
            generateFieldNotPresentCondition(token.version(), token.encoding(), indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            offset,
            token.encoding().primitiveType().size()
        ));

        sb.append(String.format(
            indent + "    void %1$s(const int index, const %2$s value)\n" +
            indent + "    {\n" +
            indent + "        if (index < 0 || index >= %3$d)\n" +
            indent + "        {\n" +
            indent + "            throw std::runtime_error(\"index out of range for %1$s [E105]\");\n" +
            indent + "        }\n\n" +
            indent + "        *((%2$s *)(buffer_ + offset_ + %4$d + (index * %5$d))) = %6$s(value);\n" +
            indent + "    }\n\n",
            propertyName,
            cpp98TypeName,
            token.arrayLength(),
            offset,
            token.encoding().primitiveType().size(),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType())
        ));

        sb.append(String.format(
            indent + "    int get%1$s(char *dst, const int length) const\n" +
            indent + "    {\n" +
            indent + "        if (length > %2$d)\n" +
            indent + "        {\n" +
            indent + "             throw std::runtime_error(\"length too large for get%1$s [E106]\");\n" +
            indent + "        }\n\n" +
                             "%3$s" +
            indent + "        ::memcpy(dst, buffer_ + offset_ + %4$d, length);\n" +
            indent + "        return length;\n" +
            indent + "    }\n\n",
            toUpperFirstChar(propertyName),
            token.arrayLength(),
            generateArrayFieldNotPresentCondition(token.version(), indent),
            offset
        ));

        sb.append(String.format(
            indent + "    %1$s &put%2$s(const char *src)\n" +
            indent + "    {\n" +
            indent + "        ::memcpy(buffer_ + offset_ + %3$d, src, %4$d);\n" +
            indent + "        return *this;\n" +
            indent + "    }\n",
            containingClassName,
            toUpperFirstChar(propertyName),
            offset,
            token.arrayLength()
        ));

        return sb;
    }

    private CharSequence generateConstPropertyMethods(final String propertyName, final Token token, final String indent)
    {
        final String cpp98TypeName = cpp98TypeName(token.encoding().primitiveType());

        if (token.encoding().primitiveType() != PrimitiveType.CHAR)
        {
            return String.format(
                "\n" +
                indent + "    %1$s %2$s(void) const\n" +
                indent + "    {\n" +
                indent + "        return %3$s;\n" +
                indent + "    }\n",
                cpp98TypeName,
                propertyName,
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString())
            );
        }

        final StringBuilder sb = new StringBuilder();

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

        sb.append(String.format(
            "\n" +
            indent + "    static const int %1$sLength(void)\n" +
            indent + "    {\n" +
            indent + "        return %2$d;\n" +
            indent + "    }\n\n",
            propertyName,
            constantValue.length
        ));

        sb.append(String.format(
            indent + "    const char *%1$s(void) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %1$sValues[] = {%2$s};\n\n" +
            indent + "        return (const char *)%1$sValues;\n" +
            indent + "    }\n\n",
            propertyName,
            values
        ));

        sb.append(String.format(
            indent + "    %1$s %2$s(const int index) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %2$sValues[] = {%3$s};\n\n" +
            indent + "        return %2$sValues[index];\n" +
            indent + "    }\n\n",
            cpp98TypeName,
            propertyName,
            values
        ));

        sb.append(String.format(
            indent + "    int get%1$s(char *dst, const int length) const\n" +
            indent + "    {\n" +
            indent + "        static sbe_uint8_t %2$sValues[] = {%3$s};\n" +
            indent + "        int bytesToCopy = (length < (int)sizeof(%2$sValues)) ? length : (int)sizeof(%2$sValues);\n\n" +
            indent + "        ::memcpy(dst, %2$sValues, bytesToCopy);\n" +
            indent + "        return bytesToCopy;\n" +
            indent + "    }\n",
            toUpperFirstChar(propertyName),
            propertyName,
            values
        ));

        return sb;
    }

    private CharSequence generateFixedFlyweightCode(final String className, final int size)
    {
        return String.format(
            "private:\n" +
            "    char *buffer_;\n" +
            "    int offset_;\n" +
            "    int actingVersion_;\n\n" +
            "    inline void reset(char *buffer, const int offset, const int bufferLength, const int actingVersion)\n" +
            "    {\n" +
            "        if (SBE_BOUNDS_CHECK_EXPECT((offset > (bufferLength - %2$s)), false))\n" +
            "        {\n" +
            "            throw std::runtime_error(\"buffer too short for flyweight [E107]\");\n" +
            "        }\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "        actingVersion_ = actingVersion;\n" +
            "    }\n\n" +
            "public:\n" +
            "    %1$s(void) : buffer_(NULL), offset_(0) {}\n\n" +
            "    %1$s(char *buffer, const int bufferLength, const int actingVersion)\n" +
            "    {\n" +
            "        reset(buffer, 0, bufferLength, actingVersion);\n" +
            "    }\n\n" +
            "    %1$s(const %1$s& codec) :\n" +
            "        buffer_(codec.buffer_), offset_(codec.offset_), actingVersion_(codec.actingVersion_) {}\n\n" +
            "#if __cplusplus >= 201103L\n" +
            "    %1$s(%1$s&& codec) :\n" +
            "        buffer_(codec.buffer_), offset_(codec.offset_), actingVersion_(codec.actingVersion_) {}\n\n" +
            "    %1$s& operator=(%1$s&& codec)\n" +
            "    {\n" +
            "        buffer_ = codec.buffer_;\n" +
            "        offset_ = codec.offset_;\n" +
            "        actingVersion_ = codec.actingVersion_;\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "#endif\n\n" +
            "    %1$s& operator=(const %1$s& codec)\n" +
            "    {\n" +
            "        buffer_ = codec.buffer_;\n" +
            "        offset_ = codec.offset_;\n" +
            "        actingVersion_ = codec.actingVersion_;\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "    %1$s &wrap(char *buffer, const int offset, const int actingVersion, const int bufferLength)\n" +
            "    {\n" +
            "        reset(buffer, offset, bufferLength, actingVersion);\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "    static const int size(void)\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n",
            className,
            size
        );
    }

    private CharSequence generateConstructorsAndOperators(final String className)
    {
        return String.format(
            "    %1$s(void) : buffer_(NULL), bufferLength_(0), offset_(0) {}\n\n" +
            "    %1$s(char *buffer, const int bufferLength, const int actingBlockLength, const int actingVersion)\n" +
            "    {\n" +
            "        reset(buffer, 0, bufferLength, actingBlockLength, actingVersion);\n" +
            "    }\n\n" +
            "    %1$s(const %1$s& codec)\n" +
            "    {\n" +
            "        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_," +
            " codec.actingVersion_);\n" +
            "    }\n\n" +
            "#if __cplusplus >= 201103L\n" +
            "    %1$s(%1$s&& codec)\n" +
            "    {\n" +
            "        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_," +
            " codec.actingVersion_);\n" +
            "    }\n\n" +
            "    %1$s& operator=(%1$s&& codec)\n" +
            "    {\n" +
            "        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_," +
            " codec.actingVersion_);\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "#endif\n\n" +
            "    %1$s& operator=(const %1$s& codec)\n" +
            "    {\n" +
            "        reset(codec.buffer_, codec.offset_, codec.bufferLength_, codec.actingBlockLength_," +
            " codec.actingVersion_);\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "",
            className
        );
    }

    private CharSequence generateMessageFlyweightCode(final String className, final Token token)
    {
        final String blockLengthType = cpp98TypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = cpp98TypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = cpp98TypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = cpp98TypeName(ir.headerStructure().schemaVersionType());
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();

        return String.format(
            "private:\n" +
            "    char *buffer_;\n" +
            "    int bufferLength_;\n" +
            "    int *positionPtr_;\n" +
            "    int offset_;\n" +
            "    int position_;\n" +
            "    int actingBlockLength_;\n" +
            "    int actingVersion_;\n\n" +
            "    inline void reset(char *buffer, const int offset, const int bufferLength, const int actingBlockLength," +
            " const int actingVersion)\n" +
            "    {\n" +
            "        buffer_ = buffer;\n" +
            "        offset_ = offset;\n" +
            "        bufferLength_ = bufferLength;\n" +
            "        actingBlockLength_ = actingBlockLength;\n" +
            "        actingVersion_ = actingVersion;\n" +
            "        positionPtr_ = &position_;\n" +
            "        position(offset + actingBlockLength_);\n" +
            "    }\n\n" +
            "public:\n\n" +
            "%11$s" +
            "    static const %1$s sbeBlockLength(void)\n" +
            "    {\n" +
            "        return %2$s;\n" +
            "    }\n\n" +
            "    static const %3$s sbeTemplateId(void)\n" +
            "    {\n" +
            "        return %4$s;\n" +
            "    }\n\n" +
            "    static const %5$s sbeSchemaId(void)\n" +
            "    {\n" +
            "        return %6$s;\n" +
            "    }\n\n" +
            "    static const %7$s sbeSchemaVersion(void)\n" +
            "    {\n" +
            "        return %8$s;\n" +
            "    }\n\n" +
            "    static const char *sbeSemanticType(void)\n" +
            "    {\n" +
            "        return \"%9$s\";\n" +
            "    }\n\n" +
            "    sbe_uint64_t offset(void) const\n" +
            "    {\n" +
            "        return offset_;\n" +
            "    }\n\n" +
            "    %10$s &wrapForEncode(char *buffer, const int offset, const int bufferLength)\n" +
            "    {\n" +
            "        reset(buffer, offset, bufferLength, sbeBlockLength(), sbeSchemaVersion());\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "    %10$s &wrapForDecode(char *buffer, const int offset, const int actingBlockLength, const int actingVersion," +
            " const int bufferLength)\n" +
            "    {\n" +
            "        reset(buffer, offset, bufferLength, actingBlockLength, actingVersion);\n" +
            "        return *this;\n" +
            "    }\n\n" +
            "    sbe_uint64_t position(void) const\n" +
            "    {\n" +
            "        return position_;\n" +
            "    }\n\n" +
            "    void position(const int position)\n" +
            "    {\n" +
            "        if (SBE_BOUNDS_CHECK_EXPECT((position > bufferLength_), false))\n" +
            "        {\n" +
            "            throw std::runtime_error(\"buffer too short [E100]\");\n" +
            "        }\n" +
            "        position_ = position;\n" +
            "    }\n\n" +
            "    int size(void) const\n" +
            "    {\n" +
            "        return position() - offset_;\n" +
            "    }\n\n" +
            "    char *buffer(void)\n" +
            "    {\n" +
            "        return buffer_;\n" +
            "    }\n\n" +
            "    int actingVersion(void) const\n" +
            "    {\n" +
            "        return actingVersion_;\n" +
            "    }\n",
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(token.version())),
            semanticType,
            className,
            generateConstructorsAndOperators(className)
        );
    }

    private CharSequence generateFields(final String containingClassName, final List<Token> tokens, final String indent)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token signalToken = tokens.get(i);
            if (signalToken.signal() == Signal.BEGIN_FIELD)
            {
                final Token encodingToken = tokens.get(i + 1);
                final String propertyName = formatPropertyName(signalToken.name());

                sb.append(String.format(
                    "\n" +
                    indent + "    static const int %1$sId(void)\n" +
                    indent + "    {\n" +
                    indent + "        return %2$d;\n" +
                    indent + "    }\n\n",
                    propertyName,
                    signalToken.id()
                ));

                sb.append(String.format(
                    indent + "    static const int %1$sSinceVersion(void)\n" +
                    indent + "    {\n" +
                    indent + "         return %2$d;\n" +
                    indent + "    }\n\n" +
                    indent + "    bool %1$sInActingVersion(void)\n" +
                    indent + "    {\n" +
                    indent + "        return (actingVersion_ >= %2$d) ? true : false;\n" +
                    indent + "    }\n\n",
                    propertyName,
                    (long)signalToken.version()
                ));

                generateFieldMetaAttributeMethod(sb, signalToken, indent);

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

    private void generateFieldMetaAttributeMethod(final StringBuilder sb, final Token token, final String indent)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();

        sb.append(String.format(
            "\n" +
            indent + "    static const char *%sMetaAttribute(const MetaAttribute::Attribute metaAttribute)\n" +
            indent + "    {\n" +
            indent + "        switch (metaAttribute)\n" +
            indent + "        {\n" +
            indent + "            case MetaAttribute::EPOCH: return \"%s\";\n" +
            indent + "            case MetaAttribute::TIME_UNIT: return \"%s\";\n" +
            indent + "            case MetaAttribute::SEMANTIC_TYPE: return \"%s\";\n" +
            indent + "        }\n\n" +
            indent + "        return \"\";\n" +
            indent + "    }\n",
            token.name(),
            epoch,
            timeUnit,
            semanticType
        ));
    }

    private CharSequence generateEnumFieldNotPresentCondition(final int sinceVersion, final String enumName, final String indent)
    {
        if (0 == sinceVersion)
        {
            return "";
        }

        return String.format(
            indent + "        if (actingVersion_ < %1$d)\n" +
            indent + "        {\n" +
            indent + "            return %2$s::NULL_VALUE;\n" +
            indent + "        }\n\n",
            sinceVersion,
            enumName
        );
    }

    private CharSequence generateEnumProperty(
        final String containingClassName, final String propertyName, final Token token, final String indent)
    {
        final String enumName = token.name();
        final String typeName = cpp98TypeName(token.encoding().primitiveType());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            indent + "    %1$s::Value %2$s(void) const\n" +
            indent + "    {\n" +
                             "%3$s" +
            indent + "        return %1$s::get(%4$s(*((%5$s *)(buffer_ + offset_ + %6$d))));\n" +
            indent + "    }\n\n",
            enumName,
            propertyName,
            generateEnumFieldNotPresentCondition(token.version(), enumName, indent),
            formatByteOrderEncoding(token.encoding().byteOrder(), token.encoding().primitiveType()),
            typeName,
            offset
        ));

        sb.append(String.format(
            indent + "    %1$s &%2$s(const %3$s::Value value)\n" +
            indent + "    {\n" +
            indent + "        *((%4$s *)(buffer_ + offset_ + %5$d)) = %6$s(value);\n" +
            indent + "        return *this;\n" +
            indent + "    }\n",
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
        final int offset = token.offset();

        sb.append(String.format(
            "\n" +
            indent + "private:\n" +
            indent + "    %1$s %2$s_;\n\n" +
            indent + "public:\n",
            bitsetName,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %1$s &%2$s()\n" +
            indent + "    {\n" +
            indent + "        %2$s_.wrap(buffer_, offset_ + %3$d, actingVersion_, bufferLength_);\n" +
            indent + "        return %2$s_;\n" +
            indent + "    }\n",
            bitsetName,
            propertyName,
            offset
        ));

        return sb;
    }

    private Object generateCompositeProperty(final String propertyName, final Token token, final String indent)
    {
        final String compositeName = formatClassName(token.name());
        final int offset = token.offset();

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
            "\n" +
            "private:\n" +
            indent + "    %1$s %2$s_;\n\n" +
            "public:\n",
            compositeName,
            propertyName
        ));

        sb.append(String.format(
            "\n" +
            indent + "    %1$s &%2$s(void)\n" +
            indent + "    {\n" +
            indent + "        %2$s_.wrap(buffer_, offset_ + %3$d, actingVersion_, bufferLength_);\n" +
            indent + "        return %2$s_;\n" +
            indent + "    }\n",
            compositeName,
            propertyName,
            offset
        ));

        return sb;
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

    private CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        final String castType = cpp98TypeName(type);
        switch (type)
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = "(" + castType + ")" + value;
                break;

            case UINT32:
            case INT32:
                literal = value;
                break;

            case FLOAT:
                if (value.endsWith("NaN"))
                {
                    literal = "SBE_FLOAT_NAN";
                }
                else
                {
                    literal = value + "f";
                }
                break;

            case INT64:
                literal = value + "L";
                break;

            case UINT64:
                literal = "0x" + Long.toHexString(Long.parseLong(value)) + "L";
                break;

            case DOUBLE:
                if (value.endsWith("NaN"))
                {
                    literal = "SBE_DOUBLE_NAN";
                }
                else
                {
                    literal = value;
                }
                break;
        }

        return literal;
    }
}
