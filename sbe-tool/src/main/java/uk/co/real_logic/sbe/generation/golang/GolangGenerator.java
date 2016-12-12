/*
 * Copyright (C) 2016 MarketFactory, Inc
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
package uk.co.real_logic.sbe.generation.golang;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.ir.*;
import org.agrona.Verify;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static uk.co.real_logic.sbe.PrimitiveType.CHAR;
import static uk.co.real_logic.sbe.generation.golang.GolangUtil.*;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectVarData;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectGroups;
import static uk.co.real_logic.sbe.ir.GenerationUtil.collectFields;

public class GolangGenerator implements CodeGenerator
{
    private static final String BASE_INDENT = "";
    private static final String INDENT = "\t";

    private final Ir ir;
    private final OutputManager outputManager;

    private static TreeSet<String> imports;


    public GolangGenerator(final Ir ir, final OutputManager outputManager)
        throws IOException
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
        this.imports = imports;

    }

    public void generateMessageHeaderStub() throws IOException
    {
        final String messageHeader = "MessageHeader";
        try (Writer out = outputManager.createOutput(messageHeader))
        {
            // Initialize the imports
            imports = new TreeSet<String>();
            imports.add("io");
            imports.add("encoding/binary");

            final StringBuilder sb = new StringBuilder();

            final List<Token> tokens = ir.headerStructure().tokens();

            generateTypeDeclaration(sb, messageHeader);
            sb.append(generateTypeBodyComposite(messageHeader, tokens.subList(1, tokens.size() - 1)));
            generateEncodeDecode(sb, messageHeader, tokens.subList(1, tokens.size() - 1));
            generateEncodedLength(sb, messageHeader, tokens.get(0).encodedLength());
            sb.append(generateCompositePropertyElements(
                messageHeader, tokens.subList(1, tokens.size() - 1)));

            out.append(generateFileHeader(ir.namespaces(), messageHeader));
            out.append(sb);
        }
    }

    public void generateTypeStubs() throws IOException
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
                    generateComposite(tokens, "");
                    break;

                case BEGIN_MESSAGE:
                    break;

                default:
                    break;
            }
        }
    }

    public void generate() throws IOException
    {
        generateMessageHeaderStub();
        generateTypeStubs();

        for (final List<Token> tokens : ir.messages())
        {
            final Token msgToken = tokens.get(0);
            final String typeName = formatTypeName(msgToken.name());

            try (Writer out = outputManager.createOutput(typeName))
            {
                final StringBuilder sb = new StringBuilder();

                // Initialize the imports
                imports = new TreeSet<String>();
                this.imports.add("io");
                this.imports.add("encoding/binary");

                generateTypeDeclaration(sb, typeName);
                generateTypeBody(sb, typeName, tokens.subList(1, tokens.size() - 1));

                sb.append(generateMessageCode(typeName, tokens));

                final List<Token> messageBody = tokens.subList(1, tokens.size() - 1);
                int i = 0;

                final List<Token> fields = new ArrayList<>();
                i = collectFields(messageBody, i, fields);

                final List<Token> groups = new ArrayList<>();
                i = collectGroups(messageBody, i, groups);

                final List<Token> varData = new ArrayList<>();
                collectVarData(messageBody, i, varData);

                sb.append(generateFields(typeName, fields, ""));
                generateGroups(sb, groups, typeName);
                generateGroupProperties(sb, groups, typeName);
                generateVarData(sb, typeName, varData, "");

                out.append(generateFileHeader(ir.namespaces(), typeName));
                out.append(sb);

            }
        }
    }

    private String generateEncodeOffset(final int gap, final String indent)
    {
        if (gap > 0)
        {
            return String.format(
                "\n" +
                "%1$s\tfor i := 0; i < %2$d; i++ {\n" +
                "%1$s\t\tif err := binary.Write(writer, order, uint8(0)); err != nil {\n" +
                "%1$s\t\t\treturn err\n" +
                "%1$s\t\t}\n" +
                "%1$s\t}\n",
                indent,
                gap);
        }
        return "";
    }

    private String generateDecodeOffset(final int gap, final String indent)
    {
        if (gap > 0)
        {
            this.imports.add("io");
            this.imports.add("io/ioutil");
            return String.format("%1$s\tio.CopyN(ioutil.Discard, reader, %2$d)\n", indent, gap);
        }
        return "";
    }

    private String generateEncodePrimitive(final String varName, final Token token)
    {
        return String.format(
            "\tif err := binary.Write(writer, order, %1$s.%2$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            varName,
            formatPropertyName(token.name()));
    }

    private String generateDecodePrimitive(final String varName, final Token token)
    {
        // FIXME: add range checking
        this.imports.add("fmt");
        final String rangeCheck;
        final String versionCheck;
        final String binaryRead;

        // Decode of a constant is simply assignment
        if (token.isConstantEncoding())
        {
            // if primitiveType="char" this is a character array
            if (token.encoding().primitiveType() == CHAR)
            {
                if (token.encoding().constValue().size() > 1)
                {
                    // constValue is a string
                    return String.format(
                        "\tcopy(%1$s[:], \"%2$s\")\n",
                        varName,
                        token.encoding().constValue());
                }
                else
                {
                    // constValue is a char
                    return String.format(
                        "\t%1$s[0] = %2$s\n",
                        varName,
                        token.encoding().constValue());
                }
            }
            else
            {
                return String.format(
                    "\t%1$s = %2$s\n",
                    varName,
                    generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
            }
        }

        binaryRead = "\tif err := binary.Read(reader, order, &%1$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n";


        if (token.arrayLength() > 1)
        {
            versionCheck = "\tif !%1$sInActingVersion(actingVersion) {\n" +
                "\t\tfor idx := 0; idx < %2$s; idx++ {\n" +
                "\t\t\t%1$s[idx] = %1$sNullValue()\n" +
                "\t\t}\n" +
                "\t\treturn nil\n" +
                "\t}\n";

            rangeCheck = "\tfor idx := 0; idx < %2$s; idx++ {\n" +
                "\t\tif %1$s[idx] < %1$sMinValue() || %1$s[idx] > %1$sMaxValue() {\n" +
                "\t\t\treturn fmt.Errorf(\"Range check failed on %1$s[%%d]\", idx)\n" +
                "\t\t}\n" +
                "\t}\n";
        }
        else
        {
            versionCheck = "\tif !%1$sInActingVersion(actingVersion) {\n" +
                "\t\t%1$s = %1$sNullValue()\n" +
                "\t\treturn nil\n" +
                "\t}\n";

            rangeCheck = "\tif %1$s < %1$sMinValue() || %1$s > %1$sMaxValue() {\n" +
                "\t\treturn fmt.Errorf(\"Range check failed on %1$s\")\n" +
                "\t}\n";
        }

        return String.format(
            versionCheck +
            binaryRead +
            rangeCheck,
            varName,
            token.arrayLength());
    }

    private String generateInitPrimitive(final String varName, final Token token)
    {
        // Decode of a constant is simply assignment
        if (token.isConstantEncoding())
        {
            // if primitiveType="char" this is a character array
            if (token.encoding().primitiveType() == CHAR)
            {
                if (token.encoding().constValue().size() > 1)
                {
                    // constValue is a string
                    return String.format(
                        "\tcopy(%1$s[:], \"%2$s\")\n",
                        varName,
                        token.encoding().constValue());
                }
                else
                {
                    // constValue is a char
                    return String.format(
                        "\t%1$s[0] = %2$s\n",
                        varName,
                        token.encoding().constValue());
                }
            }
            else
            {
                return String.format(
                    "\t%1$s = %2$s\n",
                    varName,
                    generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
            }
        }
        return "";
    }

    // Returns the size of the last Message/Group
    private int generateEncodeDecode(final StringBuilder sb, final String typeName, final List<Token> tokens)
    {
        final char varName = Character.toLowerCase(typeName.charAt(0));
        final StringBuilder encode = new StringBuilder();
        final StringBuilder decode = new StringBuilder();
        final StringBuilder init = new StringBuilder();
        final StringBuilder nested = new StringBuilder();
        int currentOffset = 0;
        int gap = 0;
        generateEncodeHeader(encode, varName, typeName);
        generateDecodeHeader(decode, varName, typeName);
        generateInitHeader(init, varName, typeName);

        for (int i = 0; i < tokens.size(); i++)
        {
            final Token signalToken = tokens.get(i);
            final String propertyName = formatPropertyName(signalToken.name());

            switch (signalToken.signal())
            {
                case BEGIN_ENUM:
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    currentOffset += generateSimpleEncodeDecode(
                        signalToken,
                        typeName,
                        encode, decode, currentOffset);
                    i = i + signalToken.componentTokenCount() - 1;
                    break;
                case BEGIN_FIELD:
                    if (tokens.size() >= i + 1)
                    {
                        currentOffset += generateFieldEncodeDecode(
                            tokens.subList(i, tokens.size() - 1),
                            varName, currentOffset, encode, decode, init);

                        // For encodings we need to move an additional one
                        // token, otherwise we need the count
                        if (tokens.get(i + 1).signal() == Signal.ENCODING)
                        {
                            i += 1;
                        }
                        else
                        {
                            i += signalToken.componentTokenCount() - 1;
                        }
                    }
                    break;
                case ENCODING:
                    gap = signalToken.offset() - currentOffset;
                    encode.append(generateEncodeOffset(gap, ""));
                    decode.append(generateDecodeOffset(gap, ""));
                    currentOffset += signalToken.encodedLength() + gap;

                    // Encode of a constant is a nullop
                    if (!signalToken.isConstantEncoding())
                    {
                        encode.append(generateEncodePrimitive(
                                          Character.toString(varName),
                                          signalToken));
                    }
                    decode.append(generateDecodePrimitive(
                        Character.toString(varName) + "." +  propertyName,
                        signalToken));
                    init.append(generateInitPrimitive(
                        Character.toString(varName) + "." +  propertyName,
                        signalToken));
                    break;
                case BEGIN_GROUP:
                    // generateGroupEncodeDecode can recurse back here
                    // for nested groups which is why we have the offset
                    // calculation
                    gap = generateGroupEncodeDecode(
                        tokens.subList(i, tokens.size() - 1),
                        typeName,
                        encode, decode, init, nested, currentOffset);

                    if (gap > 0)
                    {
                        currentOffset += gap;
                    }

                    // And we can move over this group to the END_GROUP
                    i = i + signalToken.componentTokenCount() - 1;
                    break;
                case END_GROUP:
                    // Close out this group and unwind
                    encode.append("\treturn nil\n}\n");
                    decode.append("\treturn nil\n}\n");
                    init.append("\treturn\n}\n");
                    sb.append(encode);
                    sb.append(decode);
                    sb.append(init);
                    sb.append(nested);
                    return currentOffset; // for gap calculations
                case BEGIN_VAR_DATA:
                    currentOffset += generateVarDataEncodeDecode(
                        tokens.subList(i, tokens.size() - 1),
                        typeName,
                        encode, decode, currentOffset);
                    // And we can move over this group
                    i = i + signalToken.componentTokenCount() - 1;
                    break;
            }
        }

        // You can use blockLength on both messages and groups (handled above)
        // to leave some space (akin to an offset).
        final Token endToken = tokens.get(tokens.size() - 1);
        if (endToken.signal() == Signal.END_MESSAGE)
        {
            gap = endToken.encodedLength() - currentOffset;
            encode.append(generateEncodeOffset(gap, ""));
            decode.append(generateDecodeOffset(gap, ""));
        }

        // Close the Encode/Decode methods
        encode.append("\treturn nil\n}\n");
        decode.append("\treturn nil\n}\n");
        init.append("\treturn\n}\n");

        sb.append(encode);
        sb.append(decode);
        sb.append(init);
        sb.append(nested);

        return currentOffset;
    }

    private void generateEnumEncodeDecode(
        final StringBuilder sb,
        final String enumName,
        final Token token)
    {
        final char varName = Character.toLowerCase(enumName.charAt(0));

        // Encode
        sb.append(String.format(
            "\nfunc (%1$s %2$sEnum) Encode(writer io.Writer, order binary.ByteOrder) (err error) {\n",
            varName,
            enumName));

        sb.append(String.format(
            "\tif err := binary.Write(writer, order, %1$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            varName));

        sb.append("\treturn nil\n}\n");

        // Decode
        sb.append(String.format(
            "\nfunc (%1$s *%2$sEnum) Decode(reader io.Reader, " +
            "order binary.ByteOrder, actingVersion uint16, strict bool) error {\n",
            varName,
            enumName));

        imports.add("reflect");

        sb.append(String.format(
            "\tif err := binary.Read(reader, order, %1$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            varName,
            enumName));

        // We use golang's reflect to range of the values in the struct
        // to check which are legitimate
        sb.append(String.format(
            "\tif strict {\n" +
            "\t\tvalue := reflect.ValueOf(%2$s)\n" +
            "\t\tfor idx := 0; idx < value.NumField(); idx++ {\n" +
            "\t\t\tif *%1$s == value.Field(idx).Interface() {\n" +
            "\t\t\t\treturn nil\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t\t*%1$s = %2$s.NullValue\n" +
            "\t}\n",
            varName,
            enumName));

        sb.append("\treturn nil\n}\n");
    }

    private void generateChoiceEncodeDecode(
        final StringBuilder sb,
        final String choiceName,
        final Token token)
    {
        final char varName = Character.toLowerCase(choiceName.charAt(0));

        // Encode
        sb.append(String.format(
            "\nfunc (%1$s %2$s) Encode(writer io.Writer, order binary.ByteOrder) error {\n",
            varName,
            choiceName));

        sb.append(String.format(
            "\tvar wireval uint%1$d = 0\n" +
            "\tfor k, v := range %2$s {\n" +
            "\t\tif v {\n" +
            "\t\t\twireval |= (1 << uint(k))\n" +
            "\t\t}\n\t}\n" +
            "\treturn binary.Write(writer, order, wireval)\n" +
            "}\n",
            token.encodedLength() * 8,
            varName));

        // Decode
        sb.append(String.format(
            "\nfunc (%1$s *%2$s) Decode(reader io.Reader, order binary.ByteOrder, actingVersion uint16, strict bool) error {\n",
            varName,
            choiceName));

        sb.append(String.format(
            "\tvar wireval uint%1$d\n\n" +
            "\tif err := binary.Read(reader, order, &wireval); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\n" +
            "\tvar idx uint\n" +
            "\tfor idx = 0; idx < %1$d; idx++ {\n" +
            "\t\t%2$s[idx] = (wireval & (1 << idx)) > 0\n" +
            "\t}\n",
            token.encodedLength() * 8,
            varName));

        sb.append("\treturn nil\n}\n");
    }

    private void generateEncodeHeader(
        final StringBuilder sb,
        final char varName,
        final String typeName)
    {
        sb.append(String.format(
            "\nfunc (%1$s %2$s) Encode(writer io.Writer, order binary.ByteOrder) error {\n",
            varName,
            typeName));
    }

    private void generateDecodeHeader(
        final StringBuilder sb,
        final char varName,
        final String typeName)
    {
        sb.append(String.format(
            "\nfunc (%1$s *%2$s) Decode(reader io.Reader, order binary.ByteOrder, actingVersion uint16, strict bool) error {\n",
            varName,
            typeName));
    }

    private void generateInitHeader(
        final StringBuilder sb,
        final char varName,
        final String typeName)
    {
        // Init is a function rather than a method to guarantee uniqueness
        // as a field of a structure may collide
        sb.append(String.format(
            "\nfunc %1$sInit(%2$s *%1$s) {\n",
            typeName,
            varName));
    }

    // Returns how many extra tokens to skip over
    private int generateFieldEncodeDecode(
        final List<Token> tokens,
        final char varName,
        final int currentOffset,
        StringBuilder encode,
        StringBuilder decode,
        StringBuilder init)
    {
        final Token signalToken = tokens.get(0);
        final Token encodingToken = tokens.get(1);
        final String propertyName = formatPropertyName(signalToken.name());

        final String golangType = golangTypeName(encodingToken.encoding().primitiveType());
        int gap = 0; // for offset calculations

        switch (encodingToken.signal())
        {
            case BEGIN_COMPOSITE:
            case BEGIN_ENUM:
            case BEGIN_SET:
                gap = signalToken.offset() - currentOffset;
                encode.append(generateEncodeOffset(gap, ""));
                decode.append(generateDecodeOffset(gap, ""));

                // Encode of a constant is a nullop, decode is assignment
                if (signalToken.isConstantEncoding())
                {
                    decode.append(String.format(
                        "\t%1$s.%2$s = %3$s\n",
                        varName, propertyName, signalToken.encoding().constValue()));
                    init.append(String.format(
                        "\t%1$s.%2$s = %3$s\n",
                        varName, propertyName, signalToken.encoding().constValue()));
                }
                else
                {
                    encode.append(String.format(
                        "\tif err := %1$s.%2$s.Encode(writer, order); err != nil {\n" +
                        "\t\treturn err\n" +
                        "\t}\n",
                        varName, propertyName));

                    decode.append(String.format(
                        "\tif %1$s.%2$sInActingVersion(actingVersion) {\n" +
                        "\t\tif err := %1$s.%2$s.Decode(reader, order, actingVersion, strict); err != nil {\n" +
                        "\t\t\treturn err\n" +
                        "\t\t}\n" +
                        "\t}\n",
                        varName, propertyName));
                }
                break;

            case ENCODING:
                gap = encodingToken.offset() - currentOffset;
                encode.append(generateEncodeOffset(gap, ""));
                decode.append(generateDecodeOffset(gap, ""));

                // Encode of a constant is a nullop
                if (!encodingToken.isConstantEncoding())
                {
                    encode.append(generateEncodePrimitive(
                                      Character.toString(varName),
                                      signalToken));
                }

                decode.append(generateDecodePrimitive(
                    Character.toString(varName) + "." +  propertyName,
                    encodingToken));
                init.append(generateInitPrimitive(
                    Character.toString(varName) + "." +  propertyName,
                    encodingToken));
                break;
        }

        return encodingToken.encodedLength() + gap;
    }

    // returns how much to add to offset
    private int generateSimpleEncodeDecode(
        final Token token,
        final String typeName,
        final StringBuilder encode,
        final StringBuilder decode,
        final int currentOffset)
    {

        final char varName = Character.toLowerCase(typeName.charAt(0));
        final String propertyName = formatPropertyName(token.name());
        final int gap = token.offset() - currentOffset;
        encode.append(generateEncodeOffset(gap, ""));
        decode.append(generateDecodeOffset(gap, ""));

        encode.append(String.format(
            "\tif err := %1$s.%2$s.Encode(writer, order); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            varName, propertyName));

        decode.append(String.format(
            "\tif %1$s.%2$sInActingVersion(actingVersion) {\n" +
            "\t\tif err := %1$s.%2$s.Decode(reader, order, actingVersion, strict); err != nil {\n" +
            "\t\t\treturn err\n" +
            "\t\t}\n" +
            "\t}\n",
            varName, propertyName));

        return token.encodedLength() + gap;
    }

    // returns how much to add to offset
    private int generateVarDataEncodeDecode(
        final List<Token> tokens,
        final String typeName,
        final StringBuilder encode,
        final StringBuilder decode,
        final int currentOffset)
    {
        final Token signalToken = tokens.get(0);
        final char varName = Character.toLowerCase(typeName.charAt(0));
        final String propertyName = formatPropertyName(signalToken.name());
        final int gap = signalToken.offset() - currentOffset;

        encode.append(generateEncodeOffset(gap, ""));
        decode.append(generateDecodeOffset(gap, ""));

        // Write the group header (blocklength and numingroup)
        final String golangTypeForLength = golangTypeName(tokens.get(2).encoding().primitiveType());
        final String golangTypeForData = golangTypeName(tokens.get(3).encoding().primitiveType());

        encode.append(String.format(
            "\tif err := binary.Write(writer, order, %1$s(len(%2$s.%3$s))); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\tif err := binary.Write(writer, order, %2$s.%3$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            golangTypeForLength,
            varName,
            propertyName));

        decode.append(String.format(
            "\n" +
            "\tif !%1$s.%2$sInActingVersion(actingVersion) {\n" +
            "\t\treturn nil\n" +
            "\t}\n",
            varName,
            propertyName));

        // FIXME:performance we might check capacity before make(),
        decode.append(String.format(
            "\tvar %4$sLength %1$s\n" +
            "\tif err := binary.Read(reader, order, &%4$sLength); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\t%3$s.%4$s = make([]%2$s, %4$sLength)\n" +
            "\tif err := binary.Read(reader, order, &%3$s.%4$s); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n",
            golangTypeForLength,
            golangTypeForData,
            varName,
            propertyName));

        if (gap > 0)
        {
            return gap;
        }
        else
        {
            return 0;
        }
    }

    // returns how much to add to offset
    private int generateGroupEncodeDecode(
        final List<Token> tokens,
        final String typeName,
        final StringBuilder encode,
        final StringBuilder decode,
        final StringBuilder init,
        final StringBuilder nested,
        final int currentOffset)
    {
        final char varName = Character.toLowerCase(typeName.charAt(0));
        final Token signalToken = tokens.get(0);
        final String propertyName = formatPropertyName(signalToken.name());
        final String blockLengthType = golangTypeName(tokens.get(2).encoding().primitiveType());
        final String numInGroupType = golangTypeName(tokens.get(3).encoding().primitiveType());

        // Offset handling
        final int offsetGap = signalToken.offset() - currentOffset;
        if (offsetGap > 0)
        {
            encode.append(generateEncodeOffset(offsetGap, ""));
            decode.append(generateDecodeOffset(offsetGap, ""));
        }

        // Recurse for the group's Encode which we do here so we can
        // write any gap in the Group Entry's BlockLength (group offset)
        final int gap = signalToken.encodedLength() -
            generateEncodeDecode(
                nested,
                typeName + toUpperFirstChar(signalToken.name()),
                tokens.subList(5, tokens.size() - 1));

        // Write/Read the group header
        encode.append(String.format(
            "\n\tvar %6$sBlockLength %1$s = %2$d\n" +
            "\tvar %6$sNumInGroup %3$s = %3$s(len(%4$s.%5$s))\n" +
            "\tif err := binary.Write(writer, order, %6$sBlockLength); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\tif err := binary.Write(writer, order, %6$sNumInGroup); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\n",
            blockLengthType,
            signalToken.encodedLength(),
            numInGroupType,
            varName,
            toUpperFirstChar(signalToken.name()),
            propertyName));

        // Write/Read the group itself
        encode.append(String.format(
            "\tfor _, prop := range %1$s.%2$s {\n" +
            "\t\tif err := prop.Encode(writer, order); err != nil {\n" +
            "\t\t\treturn err\n" +
            "\t\t}\n",
            varName,
            toUpperFirstChar(signalToken.name())));

        // Group blocklength handling
        encode.append(generateEncodeOffset(gap, "\t") + "\t}\n");

        decode.append(String.format(
            "\n" +
            "\tif !%1$s.%2$sInActingVersion(actingVersion) {\n" +
            "\t\treturn nil\n" +
            "\t}\n",
            varName,
            propertyName));

        decode.append(String.format(
            "\n\tvar %3$sBlockLength %1$s\n" +
            "\tvar %3$sNumInGroup %2$s\n" +
            "\tif err := binary.Read(reader, order, &%3$sBlockLength); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\tif err := binary.Read(reader, order, &%3$sNumInGroup); err != nil {\n" +
            "\t\treturn err\n" +
            "\t}\n" +
            "\n",
            blockLengthType,
            numInGroupType,
            propertyName));

        decode.append(String.format(
            "\t%1$s.%2$s = make([]%4$s%2$s, %2$sNumInGroup)\n" +
            "\tfor i, _ := range %1$s.%2$s {\n" +
            "\t\tif err := %1$s.%2$s[i].Decode(reader, order, actingVersion, strict); err != nil {\n" +
            "\t\t\treturn err\n" +
            "\t\t}\n",
            varName,
            toUpperFirstChar(signalToken.name()),
            numInGroupType,
            typeName));

        decode.append(generateDecodeOffset(gap, "\t") + "\t}\n");

        if (offsetGap > 0)
        {
            return offsetGap; // We need to know we wrote extra bytes
        }
        else
        {
            return 0;
        }
    }


    // Recursively traverse groups to create the group properties
    private void generateGroupProperties(
        final StringBuilder sb,
        final List<Token> tokens,
        final String prefix)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token token = tokens.get(i);
            if (token.signal() == Signal.BEGIN_GROUP)
            {

                final char varName = Character.toLowerCase(prefix.charAt(0));
                final String propertyName = formatPropertyName(token.name());

                sb.append(String.format(
                    "\nfunc (%1$s %2$s) %3$sId() uint16 {\n" +
                    "\treturn %4$s\n" +
                    "}\n" +
                    "\nfunc (%1$s %2$s) %3$sSinceVersion() uint16 {\n" +
                    "\treturn %5$d\n" +
                    "}\n" +
                    "\nfunc (%1$s %2$s) %3$sInActingVersion(actingVersion uint16) bool {\n" +
                    "\treturn actingVersion >= %1$s.%3$sSinceVersion()\n" +
                    "}\n",
                    varName,
                    prefix,
                    propertyName,
                    token.id(),
                    (long)token.version()));

                // Look inside for nested groups with extra prefix
                generateGroupProperties(
                    sb,
                    tokens.subList(i + 1, i + token.componentTokenCount() - 1),
                    prefix + propertyName);
                i += token.componentTokenCount() - 1;
            }
        }
    }

    private void generateGroups(final StringBuilder sb, final List<Token> tokens, final String prefix)
    {
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            final Token groupToken = tokens.get(i);
            if (groupToken.signal() != Signal.BEGIN_GROUP)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_GROUP: token=" + groupToken);
            }

            // Make a unique Group name by adding our parent
            final String groupName = prefix + formatTypeName(groupToken.name());
            final String golangTypeForNumInGroup = golangTypeName(tokens.get(i + 3).encoding().primitiveType());


            ++i;
            final int groupHeaderTokenCount = tokens.get(i).componentTokenCount();
            i += groupHeaderTokenCount;

            final List<Token> fields = new ArrayList<>();
            i = collectFields(tokens, i, fields);
            sb.append(generateFields(groupName, fields, prefix));

            final List<Token> groups = new ArrayList<>();
            i = collectGroups(tokens, i, groups);
            generateGroups(sb, groups, groupName);

            final List<Token> varData = new ArrayList<>();
            i = collectVarData(tokens, i, varData);
            generateVarData(sb, formatTypeName(groupName), varData, prefix);
        }
    }

    private void generateVarData(
        final StringBuilder sb,
        final String typeName,
        final List<Token> tokens,
        final String prefix)
    {
        for (int i = 0, size = tokens.size(); i < size;)
        {
            final Token token = tokens.get(i);
            if (token.signal() != Signal.BEGIN_VAR_DATA)
            {
                throw new IllegalStateException("tokens must begin with BEGIN_VAR_DATA: token=" + token);
            }

            final String propertyName = toUpperFirstChar(token.name());
            final String characterEncoding = tokens.get(i + 3).encoding().characterEncoding();
            final Token lengthToken = tokens.get(i + 2);
            final int lengthOfLengthField = lengthToken.encodedLength();

            generateFieldMetaAttributeMethod(typeName, sb, token, prefix);

            generateVarDataDescriptors(
                sb, token, typeName, propertyName, characterEncoding, lengthOfLengthField);

            i += token.componentTokenCount();
        }
    }

    private void generateVarDataDescriptors(
        final StringBuilder sb,
        final Token token,
        final String typeName,
        final String propertyName,
        final String characterEncoding,
        final Integer lengthOfLengthField)
    {
        final char varName = Character.toLowerCase(typeName.charAt(0));

        sb.append(String.format(
            "\nfunc (%1$s %2$s) %3$sCharacterEncoding() string {\n" +
             "\treturn \"%4$s\"\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) %3$sSinceVersion() uint16 {\n" +
             "\treturn %5$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) %3$sInActingVersion(actingVersion uint16) bool {\n" +
            "\treturn actingVersion >= %1$s.%3$sSinceVersion()\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) %3$sId() uint16 {\n" +
            "\treturn %6$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) %3$sHeaderLength() uint64 {\n" +
            "\treturn %7$s\n" +
            "}\n",
            varName,
            typeName,
            propertyName,
            characterEncoding,
            (long)token.version(),
            token.id(),
            lengthOfLengthField));
    }

    private void generateChoiceSet(final List<Token> tokens) throws IOException
    {
        final Token token = tokens.get(0);
        final String choiceName = formatTypeName(token.name());
        final StringBuilder sb = new StringBuilder();

        try (Writer out = outputManager.createOutput(choiceName))
        {
            // Initialize the imports
            imports = new TreeSet<String>();
            imports.add("io");
            imports.add("encoding/binary");

            sb.append(generateChoiceDecls(
                choiceName,
                tokens.subList(1, tokens.size() - 1),
                token));

            generateChoiceEncodeDecode(sb, choiceName, token);

            // EncodedLength
            sb.append(String.format(
                          "\nfunc (%1$s %2$s) EncodedLength() int64 {\n" +
                          "\treturn %3$s\n" +
                          "}\n",
                          choiceName,
                          choiceName,
                          token.encodedLength()));

            out.append(generateFileHeader(ir.namespaces(), choiceName));
            out.append(sb);
        }
    }

    private void generateEnum(final List<Token> tokens) throws IOException
    {
        final Token enumToken = tokens.get(0);
        final String enumName = formatTypeName(tokens.get(0).name());
        final char varName = Character.toLowerCase(enumName.charAt(0));

        final StringBuilder sb = new StringBuilder();

        try (Writer out = outputManager.createOutput(enumName))
        {
            // Initialize the imports
            imports = new TreeSet<String>();
            imports.add("io");
            imports.add("encoding/binary");

            sb.append(generateEnumDecls(
                enumName,
                golangTypeName(tokens.get(0).encoding().primitiveType()),
                tokens.subList(1, tokens.size() - 1),
                enumToken));

            generateEnumEncodeDecode(sb, enumName, enumToken);

            // EncodedLength
            sb.append(String.format(
                "\nfunc (%1$s %2$sEnum) EncodedLength() int64 {\n" +
                "\treturn %3$s\n" +
                "}\n",
                varName,
                enumName,
                enumToken.encodedLength()));

            // All of our enumeration values can have a sinceVersion
            for (final Token token : tokens.subList(1, tokens.size() - 1))
            {
                sb.append(String.format(
                    "func (%1$s %2$sEnum) %3$sSinceVersion() uint16 {\n" +
                    "\treturn %4$d\n" +
                    "}\n" +
                    "func (%1$s %2$sEnum) %3$sInActingVersion(actingVersion uint16) bool {\n" +
                    "\treturn actingVersion >= %1$s.%3$sSinceVersion()\n" +
                    "}\n",
                    varName,
                    enumName,
                    token.name(),
                    token.version()));

            }

            out.append(generateFileHeader(ir.namespaces(), enumName));
            out.append(sb);
        }
    }

    private void generateComposite(
        final List<Token> tokens,
        final String namePrefix) throws IOException
    {
        final String compositeName = namePrefix + formatTypeName(tokens.get(0).name());
        final StringBuilder sb = new StringBuilder();

        try (Writer out = outputManager.createOutput(compositeName))
        {
            // Initialize the imports
            imports = new TreeSet<String>();
            imports.add("io");
            imports.add("encoding/binary");

            generateTypeDeclaration(sb, compositeName);
            sb.append(generateTypeBodyComposite(compositeName, tokens.subList(1, tokens.size() - 1)));

            generateEncodeDecode(sb, compositeName, tokens.subList(1, tokens.size() - 1));
            generateEncodedLength(sb, compositeName, tokens.get(0).encodedLength());

            sb.append(generateCompositePropertyElements(compositeName, tokens.subList(1, tokens.size() - 1)));

            out.append(generateFileHeader(ir.namespaces(), compositeName));
            out.append(sb);
        }
    }

    private CharSequence generateEnumDecls(
        final String enumName,
        final String golangType,
        final List<Token> tokens,
        final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();


        // gofmt lines up the types and we don't want it to have to rewrite
        // our generated files. To line things up we need to know the longest
        // string length and then fill with whitespace
        final String nullValue = "NullValue";
        int longest = nullValue.length();
        for (final Token token : tokens)
        {
            longest = Math.max(longest, token.name().length());
        }

        // Enums are modelled as a struct and we export an instance so
        // you can reference known values as expected.
        sb.append(String.format(
             "type %1$sEnum %2$s\n" +
             "type %1$sValues struct {\n",
                      enumName, golangType));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "\t%1$s%2$s%3$sEnum\n",
                token.name(),
                String.format(String.format("%%%ds", longest - token.name().length() + 1), " "),
                enumName));
        }

        // Add the NullValue
        sb.append(String.format(
            "\t%1$s%2$s%3$sEnum\n" +
            "}\n",
            nullValue,
            String.format(String.format("%%%ds", longest - nullValue.length() + 1), " "),
            enumName));

        // And now the Enum Values expressed as a variable
        sb.append(String.format(
             "\nvar %1$s = %1$sValues{",
             enumName));
        for (final Token token : tokens)
        {
            sb.append(
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()) +
                ", ");
        }
        // Add the NullValue and close
        sb.append(encodingToken.encoding().applicableNullValue().toString() +
            "}\n");

        return sb;
    }

    private CharSequence generateChoiceDecls(
        final String choiceName,
        final List<Token> tokens,
        final Token encodingToken)
    {
        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = encodingToken.encoding();

        // gofmt lines up the types and we don't want it to have to rewrite
        // our generated files. To line things up we need to know the longest
        // string length and then fill with whitespace
        int longest = 0;
        for (final Token token : tokens)
        {
            longest = Math.max(longest, token.name().length());
        }

        // A ChoiceSet is modelled as an array of bool of size
        // encodedLength in bits (akin to bits in a bitfield).
        // Choice values are modelled as a struct and we export an
        // instance so you can reference known values by name.
        sb.append(String.format(
            "type %1$s [%2$d]bool\n" +
            "type %1$sChoiceValue uint8\n" +
            "type %1$sChoiceValues struct {\n",
            choiceName, encodingToken.encodedLength() * 8));

        for (final Token token : tokens)
        {
            sb.append(String.format(
                "\t%1$s%2$s%3$sChoiceValue\n",
                toUpperFirstChar(token.name()),
                String.format(String.format("%%%ds", longest - token.name().length() + 1), " "),
                toUpperFirstChar(encodingToken.name()),
                generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString())));
        }

        sb.append("}\n");

        // And now the Values expressed as a variable
        sb.append(String.format(
             "\nvar %1$sChoice = %1$sChoiceValues{",
             choiceName));

        String comma = "";
        for (final Token token : tokens)
        {
            sb.append(comma +
                      generateLiteral(token.encoding().primitiveType(), token.encoding().constValue().toString()));
            comma = ", ";
        }
        sb.append("}\n");

        return sb;
    }
    private static CharSequence generateFileHeader(
        final CharSequence[] namespaces,
        final String typeName)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("// Generated SBE (Simple Binary Encoding) message codec\n\n");
        sb.append(String.format(
            "package %1$s\n" +
            "\n" +
            "import (\n",
            String.join("_", namespaces).toLowerCase().replace('.', '_').replace(' ', '_')));

        for (String s : imports)
        {
            sb.append("\t\"" + s + "\"\n");
        }

        sb.append(")\n\n");
        return sb;
    }

    private static void generateTypeDeclaration(
        final StringBuilder sb,
        final String typeName)
    {
        sb.append(String.format("type %s struct {\n", typeName));
    }

    private void generateTypeBody(
        final StringBuilder sb,
        final String typeName,
        final List<Token> tokens)
    {
        // gofmt lines up the types and we don't want it to have to rewrite
        // our generated files. To line things up we need to know the longest
        // string length and then fill with whitespace
        int longest = 0;
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            switch (token.signal())
            {
                case BEGIN_GROUP:
                    longest = Math.max(longest, propertyName.length());
                    i += token.componentTokenCount() - 1;
                    break;
                case BEGIN_FIELD:
                case BEGIN_VAR_DATA:
                    longest = Math.max(longest, propertyName.length());
                    break;
            }
        }

        final StringBuilder nested = new StringBuilder(); // For nested groups
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token signalToken = tokens.get(i);
            final String propertyName = formatPropertyName(signalToken.name());

            switch (signalToken.signal())
            {
                case BEGIN_FIELD:
                    if (tokens.size() > i + 1)
                    {
                        final Token encodingToken = tokens.get(i + 1);
                        final int arrayLength = encodingToken.arrayLength();
                        switch (encodingToken.signal())
                        {
                            case BEGIN_ENUM:
                                sb.append(
                                    "\t" +
                                    propertyName +
                                    String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                                    ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                                    encodingToken.name() +
                                    "Enum\n");
                                break;

                            case BEGIN_SET:
                                sb.append(
                                    "\t" +
                                    propertyName +
                                    String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                                    ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                                    encodingToken.name() +
                                    "\n");
                                break;

                            default:
                                // If the type is primitive then use the golang naming for it
                                String golangType;
                                golangType = golangTypeName(encodingToken.encoding().primitiveType());
                                if (golangType == null)
                                {
                                    golangType = toUpperFirstChar(encodingToken.name());
                                }
                                // if a primitiveType="char" and presence="constant" then this is actually a character array
                                String arrayspec = "";
                                if (arrayLength > 1)
                                {
                                    arrayspec = "[" + arrayLength + "]";
                                }
                                if (encodingToken.isConstantEncoding() && encodingToken.encoding().primitiveType() == CHAR)
                                {
                                    arrayspec = "[" + encodingToken.encoding().constValue().size() + "]"; // can be 1
                                }
                                sb.append(
                                    "\t" + propertyName +
                                    String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                                    arrayspec +
                                    golangType + "\n");
                                break;
                        }
                        i++;
                    }
                    break;

                case BEGIN_GROUP:
                    sb.append(String.format(
                        "\t%1$s%2$s[]%3$s%1$s\n",
                        toUpperFirstChar(signalToken.name()),
                        String.format(String.format("%%%ds", longest - propertyName.length() + 1), " "),
                        typeName));
                    generateTypeDeclaration(
                        nested,
                        typeName + toUpperFirstChar(signalToken.name()));
                    generateTypeBody(
                        nested,
                        typeName + toUpperFirstChar(signalToken.name()),
                        tokens.subList(i + 1, tokens.size() - 1));
                    i = i + signalToken.componentTokenCount() - 1;
                    break;

                case END_GROUP:
                    // Close the group and unwind
                    sb.append("}\n");
                    sb.append(nested);
                    return;

                case BEGIN_VAR_DATA:
                    sb.append(String.format(
                        "\t%1$s%2$s[]%3$s\n",
                        toUpperFirstChar(signalToken.name()),
                        String.format(String.format("%%%ds", longest - propertyName.length() + 1), " "),

                        golangTypeName(tokens.get(i + 3).encoding().primitiveType())));
                    break;

                default:
                    break;
            }
        }
        sb.append("}\n");
        sb.append(nested);
    }


    private CharSequence generateCompositePropertyElements(
        final String containingTypeName, final List<Token> tokens)
    {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokens.size();)
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            // Write {Min,Max,Null}Value
            switch (token.signal())
            {
                case ENCODING:
                    sb.append(generateMinMaxNull(containingTypeName, propertyName, token));
                    break;

                default:
                    break;
            }

            // SinceVersion/ActingVersion
            switch (token.signal())
            {
                case ENCODING:
                case BEGIN_ENUM:
                case BEGIN_SET:
                case BEGIN_COMPOSITE:
                    sb.append(String.format(
                        "\nfunc (%1$s %2$s) %3$sSinceVersion() uint16 {\n" +
                        "\treturn %4$s\n" +
                        "}\n" +
                        "\nfunc (%1$s %2$s) %3$sInActingVersion(actingVersion uint16) bool {\n" +
                        "\treturn actingVersion >= %1$s.%3$sSinceVersion()\n" +
                        "}\n",
                        Character.toLowerCase(containingTypeName.charAt(0)),
                        containingTypeName,
                        propertyName,
                        (long)token.version()));
                    break;
                default:
                    break;
            }
            i += tokens.get(i).componentTokenCount();
        }

        return sb;
    }

    private CharSequence generateMinMaxNull(
        final String typeName,
        final String propertyName,
        final Token token)
    {
        final StringBuilder sb = new StringBuilder();

        final Encoding encoding = token.encoding();
        final PrimitiveType primitiveType = encoding.primitiveType();
        final String golangTypeName = golangTypeName(primitiveType);
        final CharSequence nullValueString = generateNullValueLiteral(primitiveType, encoding);
        final CharSequence maxValueString = generateMaxValueLiteral(primitiveType, encoding);
        final CharSequence minValueString = generateMinValueLiteral(primitiveType, encoding);

        // MinValue
        sb.append(String.format(
            "\nfunc (%1$s %2$s) %3$sMinValue() %4$s {\n" +
            "\treturn %5$s\n" +
            "}\n",
            Character.toLowerCase(typeName.charAt(0)),
            typeName,
            propertyName,
            golangTypeName,
            minValueString));

        // MaxValue
        sb.append(String.format(
            "\nfunc (%1$s %2$s) %3$sMaxValue() %4$s {\n" +
            "\treturn %5$s\n" +
            "}\n",
            Character.toLowerCase(typeName.charAt(0)),
            typeName,
            propertyName,
            golangTypeName,
            maxValueString));

        // NullValue
        sb.append(String.format(
            "\nfunc (%1$s %2$s) %3$sNullValue() %4$s {\n" +
            "\treturn %5$s\n" +
            "}\n",
            Character.toLowerCase(typeName.charAt(0)),
            typeName,
            propertyName,
            golangTypeName,
            nullValueString));

        return sb;
    }

    private CharSequence generateTypeBodyComposite(
        final String typeName,
        final List<Token> tokens) throws IOException
    {
        final StringBuilder sb = new StringBuilder();


        // gofmt lines up the types and we don't want it to have to rewrite
        // our generated files. To line things up we need to know the longest
        // string length and then fill with whitespace
        int longest = 0;
        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());

            switch (token.signal())
            {
                case BEGIN_GROUP:
                    longest = Math.max(longest, propertyName.length());
                    i += token.componentTokenCount() - 1;
                    break;
                default:
                    longest = Math.max(longest, propertyName.length());
                    break;
            }
        }

        for (int i = 0; i < tokens.size(); i++)
        {
            final Token token = tokens.get(i);
            final String propertyName = formatPropertyName(token.name());
            int arrayLength = token.arrayLength();

            switch (token.signal())
            {
                case ENCODING:
                    // if a primitiveType="char" and presence="constant" then this is actually a character array
                    if (token.isConstantEncoding() && token.encoding().primitiveType() == CHAR)
                    {
                        arrayLength = token.encoding().constValue().size(); // can be 1
                        sb.append("\t" + propertyName +
                            String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                            "[" + arrayLength + "]" +
                            golangTypeName(token.encoding().primitiveType()) + "\n");
                    }
                    else
                    {
                        sb.append("\t" + propertyName +
                            String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                            ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                            golangTypeName(token.encoding().primitiveType()) + "\n");
                    }
                    break;

                case BEGIN_ENUM:
                    sb.append("\t" + propertyName +
                            String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                            ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                            propertyName + "Enum\n");
                    break;

                case BEGIN_SET:
                    sb.append("\t" + propertyName +
                            String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                            ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                            propertyName + "\n");
                    break;

                case BEGIN_COMPOSITE:
                    // recurse
                    generateComposite(tokens.subList(i, tokens.size()), typeName);
                    i = i + token.componentTokenCount() - 2;

                    sb.append("\t" + propertyName +
                            String.format(String.format("%%%ds", longest - propertyName.length() + 1), " ") +
                            ((arrayLength > 1) ? ("[" + arrayLength + "]") : "") +
                            typeName + propertyName + "\n");
                    break;
            }
        }
        sb.append("}\n");
        return sb;
    }

    private CharSequence generateEncodedLength(final StringBuilder sb, final String typeName, final int size)
    {
        sb.append(String.format(
            "\nfunc (%1$s %2$s) EncodedLength() int64 {\n" +
            "\treturn %3$s\n" +
            "}\n",
            Character.toLowerCase(typeName.charAt(0)),
            typeName,
            size));

        return sb;
    }

    private CharSequence generateMessageCode(final String typeName, final List<Token> tokens)
    {
        final Token token = tokens.get(0);
        final String semanticType = token.encoding().semanticType() == null ? "" : token.encoding().semanticType();
        final String blockLengthType = golangTypeName(ir.headerStructure().blockLengthType());
        final String templateIdType = golangTypeName(ir.headerStructure().templateIdType());
        final String schemaIdType = golangTypeName(ir.headerStructure().schemaIdType());
        final String schemaVersionType = golangTypeName(ir.headerStructure().schemaVersionType());
        final StringBuilder sb = new StringBuilder();


        generateEncodeDecode(sb, typeName, tokens);

        sb.append(String.format(
            "\nfunc (%1$s %2$s) SbeBlockLength() (blockLength %3$s) {\n" +
            "\treturn %4$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) SbeTemplateId() (templateId %5$s) {\n" +
            "\treturn %6$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) SbeSchemaId() (schemaId %7$s) {\n" +
            "\treturn %8$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) SbeSchemaVersion() (schemaVersion %9$s) {\n" +
            "\treturn %10$s\n" +
            "}\n" +
            "\nfunc (%1$s %2$s) SbeSemanticType() (semanticType []byte) {\n" +
            "\treturn []byte(\"%11$s\")\n" +
            "}\n",
            Character.toLowerCase(typeName.charAt(0)),
            typeName,
            blockLengthType,
            generateLiteral(ir.headerStructure().blockLengthType(), Integer.toString(token.encodedLength())),
            templateIdType,
            generateLiteral(ir.headerStructure().templateIdType(), Integer.toString(token.id())),
            schemaIdType,
            generateLiteral(ir.headerStructure().schemaIdType(), Integer.toString(ir.id())),
            schemaVersionType,
            generateLiteral(ir.headerStructure().schemaVersionType(), Integer.toString(token.version())),
            semanticType));

        return sb;
    }

    private CharSequence generateFields(final String containingTypeName, final List<Token> tokens, final String prefix)
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
                    "\nfunc (%1$s %2$s) %3$sId() uint16 {\n" +
                    "\treturn %4$s\n" +
                    "}\n" +
                    "\nfunc (%1$s %2$s) %3$sSinceVersion() uint16 {\n" +
                    "\treturn %5$s\n" +
                    "}\n" +
                    "\nfunc (%1$s %2$s) %3$sInActingVersion(actingVersion uint16) bool {\n" +
                    "\treturn actingVersion >= %1$s.%3$sSinceVersion()\n" +
                    "}\n",
                    Character.toLowerCase(containingTypeName.charAt(0)),
                    containingTypeName,
                    propertyName,
                    signalToken.id(),
                    (long)signalToken.version()));

                generateFieldMetaAttributeMethod(containingTypeName, sb, signalToken, prefix);

                switch (encodingToken.signal())
                {
                    case ENCODING:
                        sb.append(generateMinMaxNull(containingTypeName, propertyName, encodingToken));
                        break;

                    case BEGIN_ENUM:
                        break;

                    case BEGIN_SET:
                        break;

                    case BEGIN_COMPOSITE:
                        break;

                    default:
                        break;
                }
            }
        }

        return sb;
    }

    private static void generateFieldMetaAttributeMethod(
        final String containingTypeName,
        final StringBuilder sb,
        final Token token,
        final String prefix)
    {
        final Encoding encoding = token.encoding();
        final String epoch = encoding.epoch() == null ? "" : encoding.epoch();
        final String timeUnit = encoding.timeUnit() == null ? "" : encoding.timeUnit();
        final String semanticType = encoding.semanticType() == null ? "" : encoding.semanticType();
        sb.append(String.format(
            "\nfunc (%1$s %2$s) %3$sMetaAttribute(meta int) string {\n" +
            "\tswitch meta {\n" +
            "\tcase 1:\n" +
            "\t\treturn \"%4$s\"\n" +
            "\tcase 2:\n" +
            "\t\treturn \"%5$s\"\n" +
            "\tcase 3:\n" +
            "\t\treturn \"%6$s\"\n" +
            "\t}\n" +
            "\treturn \"\"\n" +
            "}\n",
            Character.toLowerCase(containingTypeName.charAt(0)),
            containingTypeName,
            toUpperFirstChar(token.name()),
            epoch,
            timeUnit,
            semanticType));
    }

    private CharSequence generateMinValueLiteral(final PrimitiveType primitiveType, final Encoding encoding)
    {
        if (null == encoding.maxValue())
        {
            switch (primitiveType)
            {
                case CHAR:
                    return "byte(32)";
                case INT8:
                    imports.add("math");
                    return "math.MinInt8 + 1";
                case INT16:
                    imports.add("math");
                    return "math.MinInt16 + 1";
                case INT32:
                    imports.add("math");
                    return "math.MinInt32 + 1";
                case INT64:
                    imports.add("math");
                    return "math.MinInt64 + 1";
                case UINT8:
                case UINT16:
                case UINT32:
                case UINT64:
                    return "0";
                case FLOAT:
                    imports.add("math");
                    return "-math.MaxFloat32";
                case DOUBLE:
                    imports.add("math");
                    return "-math.MaxFloat64";

            }
        }

        return generateLiteral(primitiveType, encoding.applicableMinValue().toString());
    }

    private CharSequence generateMaxValueLiteral(final PrimitiveType primitiveType, final Encoding encoding)
    {
        if (null == encoding.maxValue())
        {
            switch (primitiveType)
            {
                case CHAR:
                    return "byte(126)";
                case INT8:
                    imports.add("math");
                    return "math.MaxInt8";
                case INT16:
                    imports.add("math");
                    return "math.MaxInt16";
                case INT32:
                    imports.add("math");
                    return "math.MaxInt32";
                case INT64:
                    imports.add("math");
                    return "math.MaxInt64";
                case UINT8:
                    imports.add("math");
                    return "math.MaxUint8 - 1";
                case UINT16:
                    imports.add("math");
                    return "math.MaxUint16 - 1";
                case UINT32:
                    imports.add("math");
                    return "math.MaxUint32 - 1";
                case UINT64:
                    imports.add("math");
                    return "math.MaxUint64 - 1";
                case FLOAT:
                    imports.add("math");
                    return "math.MaxFloat32";
                case DOUBLE:
                    imports.add("math");
                    return "math.MaxFloat64";
            }
        }

        return generateLiteral(primitiveType, encoding.applicableMaxValue().toString());
    }

    private CharSequence generateNullValueLiteral(final PrimitiveType primitiveType, final Encoding encoding)
    {
        if (null == encoding.nullValue())
        {
            switch (primitiveType)
            {
                case INT8:
                    imports.add("math");
                    return "math.MinInt8";
                case INT16:
                    imports.add("math");
                    return "math.MinInt16";
                case INT32:
                    imports.add("math");
                    return "math.MinInt32";
                case INT64:
                    imports.add("math");
                    return "math.MinInt64";
                case UINT8:
                    imports.add("math");
                    return "math.MaxUint8";
                case UINT16:
                    imports.add("math");
                    return "math.MaxUint16";
                case UINT32:
                    imports.add("math");
                    return "math.MaxUint32";
                case UINT64:
                    imports.add("math");
                    return "math.MaxUint64";
            }
        }

        return generateLiteral(primitiveType, encoding.applicableNullValue().toString());
    }

    private CharSequence generateLiteral(final PrimitiveType type, final String value)
    {
        String literal = "";

        final String castType = golangTypeName(type);
        switch (type)
        {
            case CHAR:
            case UINT8:
            case UINT16:
            case INT8:
            case INT16:
                literal = value;
                break;

            case UINT32:
            case INT32:
                literal = value;
                break;
            case INT64:
            case UINT64:
                literal = castType + "(" + value + ")";
                break;

            case FLOAT:
                literal = "float32(" + (value.endsWith("NaN") ? "math.NaN()" : value) + ")";
                break;

            case DOUBLE:
                literal = value.endsWith("NaN") ? "math.NaN()" : value;
                break;
        }

        return literal;
    }
}
