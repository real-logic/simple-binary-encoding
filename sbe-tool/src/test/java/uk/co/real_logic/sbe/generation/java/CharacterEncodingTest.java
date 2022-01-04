/*
 * Copyright 2013-2021 Real Logic Limited.
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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static uk.co.real_logic.sbe.generation.java.JavaUtil.STD_CHARSETS;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class CharacterEncodingTest
{
    private static final String XML_SCHEMA;

    static
    {
        final StringBuilder buffer =
            new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
            .append("<sbe:messageSchema xmlns:sbe=\"http://fixprotocol.io/2016/sbe\"\n")
            .append("                   package=\"code.generation.test\"\n")
            .append("                   id=\"6\"\n")
            .append("                   version=\"0\"\n")
            .append("                   semanticVersion=\"5.2\"\n")
            .append("                   description=\"Example schema\"\n")
            .append("                   byteOrder=\"littleEndian\">\n")
            .append("    <types>\n")
            .append("        <composite name=\"messageHeader\" ")
            .append("description=\"Message identifiers and length of message root\">\n")
            .append("            <type name=\"blockLength\" primitiveType=\"uint16\"/>\n")
            .append("            <type name=\"templateId\" primitiveType=\"uint16\"/>\n")
            .append("            <type name=\"schemaId\" primitiveType=\"uint16\"/>\n")
            .append("            <type name=\"version\" primitiveType=\"uint16\"/>\n")
            .append("        </composite>\n");

        final HashSet<String> asciiCharsetNames = new HashSet<>();
        asciiCharsetNames.add(US_ASCII.name());
        asciiCharsetNames.addAll(US_ASCII.aliases());

        int i = 0;
        for (final String alias : STD_CHARSETS.keySet())
        {
            buffer.append("        <type name=\"type_fixed_")
                .append(i)
                .append("\" primitiveType=\"char\" semanticType=\"String\" length=\"3\" characterEncoding=\"")
                .append(alias)
                .append("\"/>\n");
            buffer.append("        <composite name=\"type_var_")
                .append(i)
                .append("\">\n")
                .append("            <type name=\"length\" primitiveType=\"uint32\" maxValue=\"1073741824\"/>\n")
                .append("            <type name=\"varData\" primitiveType=\"uint8\" length=\"0\" characterEncoding=\"")
                .append(alias)
                .append("\"/>\n")
                .append("        </composite>\n");
            i++;
        }
        buffer.append("        <type name=\"type_fixed_custom\" primitiveType=\"char\" semanticType=\"String\"")
            .append(" length=\"2\"")
            .append(" characterEncoding=\"custom-encoding\"/>\n");
        buffer.append("        <composite name=\"type_var_custom\">\n")
            .append("            <type name=\"length\" primitiveType=\"uint32\" maxValue=\"1073741824\"/>\n")
            .append("            <type name=\"varData\" primitiveType=\"uint8\" length=\"0\"")
            .append(" characterEncoding=\"custom-encoding\"/>\n        </composite>\n");

        buffer.append("    </types>\n")
            .append("    <sbe:message name=\"EncodingTest\" id=\"1\" description=\"Multiple encodings\">\n");
        i = 0;
        for (int j = 0, size = STD_CHARSETS.size(); j < size; j++)
        {
            buffer.append("        <field name=\"f_").append(i).append("\" id=\"").append(i)
                .append("\"  type=\"type_fixed_").append(i).append("\"/>\n");
            i++;
        }
        buffer.append("<field name=\"f_custom\" id=\"").append(i++).append("\" type=\"type_fixed_custom\"/>");

        for (int j = 0, size = STD_CHARSETS.size(); j < size; j++)
        {
            buffer.append("        <data name=\"var_").append(i).append("\" id=\"").append(i)
                .append("\"  type=\"type_var_").append(j).append("\"/>\n");
            i++;
        }
        buffer.append("<data name=\"var_custom\" id=\"").append(i).append("\" type=\"type_var_custom\"/>");

        buffer.append("    </sbe:message>\n")
            .append("</sbe:messageSchema>");
        XML_SCHEMA = buffer.toString();
    }

    @Test
    public void shouldUseStandardCharsetsForWellKnowEncodings() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema =
            parse(new ByteArrayInputStream(XML_SCHEMA.getBytes(UTF_8)), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        final StringWriterOutputManager outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        final JavaGenerator generator = new JavaGenerator(
            ir, MutableDirectBuffer.class.getName(), DirectBuffer.class.getName(), false, false, false, outputManager);

        generator.generate();
        final Map<String, CharSequence> sources = outputManager.getSources();
        final String encoderSources = sources.get("code.generation.test.EncodingTestEncoder").toString();
        final String decoderSources = sources.get("code.generation.test.EncodingTestDecoder").toString();
        verifyCharacterEncodingMethods(encoderSources);
        verifyCharacterEncodingMethods(decoderSources);
    }

    private void verifyCharacterEncodingMethods(final String code)
    {
        int i = 0;
        for (final String charset : STD_CHARSETS.values())
        {
            assertContainsCharacterEncodingMethod(
                "f_" + (i++), "java.nio.charset.StandardCharsets." + charset + ".name()", code);
        }
        assertContainsCharacterEncodingMethod("f_custom", "\"CUSTOM-ENCODING\"", code);
    }

    private static void assertContainsCharacterEncodingMethod(
        final String fieldName, final String expectedEncoding, final String sources)
    {
        final String expectedOne =
            "    public static String " + fieldName + "CharacterEncoding()\n" +
            "    {\n        return " + expectedEncoding + ";\n    }";
        assertThat(sources, containsString(expectedOne));
    }
}
