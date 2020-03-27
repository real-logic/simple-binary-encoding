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
package uk.co.real_logic.sbe.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.TestUtil;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class EnumTypeTest
{
    @Test
    public void shouldHandleBinaryEnumType()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"biOp\" encodingType=\"uint8\">" +
            "    <validValue name=\"off\" description=\"switch is off\">0</validValue>" +
            "    <validValue name=\"on\" description=\"switch is on\">1</validValue>" +
            "</enum>" +
            "</types>";

        final Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        final EnumType e = (EnumType)map.get("biOp");

        assertThat(e.name(), is("biOp"));
        assertThat(e.encodingType(), is(PrimitiveType.UINT8));
        assertThat(e.validValues().size(), is(2));
        assertThat(e.getValidValue("on").primitiveValue(), is(PrimitiveValue.parse("1", PrimitiveType.UINT8)));
        assertThat(e.getValidValue("off").primitiveValue(), is(PrimitiveValue.parse("0", PrimitiveType.UINT8)));
    }

    @Test
    public void shouldHandleBooleanEnumType()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"Boolean\" encodingType=\"uint8\" semanticType=\"Boolean\">" +
            "    <validValue name=\"False\">0</validValue>" +
            "    <validValue name=\"True\">1</validValue>" +
            "</enum>" +
            "</types>";

        final Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        final EnumType e = (EnumType)map.get("Boolean");

        assertThat(e.name(), is("Boolean"));
        assertThat(e.encodingType(), is(PrimitiveType.UINT8));
        assertThat(e.validValues().size(), is(2));
        assertThat(e.getValidValue("True").primitiveValue(), is(PrimitiveValue.parse("1", PrimitiveType.UINT8)));
        assertThat(e.getValidValue("False").primitiveValue(), is(PrimitiveValue.parse("0", PrimitiveType.UINT8)));
    }

    @Test
    public void shouldHandleOptionalBooleanEnumType()
        throws Exception
    {
        final String nullValueStr = "255";
        final String testXmlString =
            "<types>" +
            "<enum name=\"optionalBoolean\" encodingType=\"uint8\" presence=\"optional\"" +
            "      nullValue=\"" + nullValueStr + "\" semanticType=\"Boolean\">" +
            "    <validValue name=\"False\">0</validValue>" +
            "    <validValue name=\"True\">1</validValue>" +
            "</enum>" +
            "</types>";

        final Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        final EnumType e = (EnumType)map.get("optionalBoolean");

        assertThat(e.name(), is("optionalBoolean"));
        assertThat(e.encodingType(), is(PrimitiveType.UINT8));
        assertThat(e.validValues().size(), is(2));
        assertThat(e.getValidValue("True").primitiveValue(), is(PrimitiveValue.parse("1", PrimitiveType.UINT8)));
        assertThat(e.getValidValue("False").primitiveValue(), is(PrimitiveValue.parse("0", PrimitiveType.UINT8)));
        assertThat(e.nullValue(), is(PrimitiveValue.parse(nullValueStr, PrimitiveType.UINT8)));
    }

    @Test
    public void shouldHandleEnumTypeList()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"triOp\" encodingType=\"uint8\">" +
            "    <validValue name=\"off\" description=\"switch is off\">0</validValue>" +
            "    <validValue name=\"on\" description=\"switch is on\">1</validValue>" +
            "    <validValue name=\"notKnown\" description=\"switch is unknown\">2</validValue>" +
            "</enum>" +
            "</types>";

        final Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        final EnumType e = (EnumType)map.get("triOp");

        assertThat(e.name(), is("triOp"));
        assertThat(e.encodingType(), is(PrimitiveType.UINT8));

        int foundOn = 0, foundOff = 0, foundNotKnown = 0, count = 0;
        for (final EnumType.ValidValue v : e.validValues())
        {
            switch (v.name())
            {
                case "on":
                    foundOn++;
                    break;

                case "off":
                    foundOff++;
                    break;

                case "notKnown":
                    foundNotKnown++;
                    break;
            }
            count++;
        }

        assertThat(count, is(3));
        assertThat(foundOn, is(1));
        assertThat(foundOff, is(1));
        assertThat(foundNotKnown, is(1));
    }

    @Test
    public void shouldHandleCharEnumEncodingType()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"mixed\" encodingType=\"char\">" +
            "    <validValue name=\"Cee\">C</validValue>" +
            "    <validValue name=\"One\">1</validValue>" +
            "    <validValue name=\"Two\">2</validValue>" +
            "    <validValue name=\"Eee\">E</validValue>" +
            "</enum>" +
            "</types>";

        final Map<String, Type> map = parseTestXmlWithMap("/types/enum", testXmlString);
        final EnumType e = (EnumType)map.get("mixed");

        assertThat(e.encodingType(), is(PrimitiveType.CHAR));
        assertThat(e.getValidValue("Cee").primitiveValue(), is(PrimitiveValue.parse("C", PrimitiveType.CHAR)));
        assertThat(e.getValidValue("One").primitiveValue(), is(PrimitiveValue.parse("1", PrimitiveType.CHAR)));
        assertThat(e.getValidValue("Two").primitiveValue(), is(PrimitiveValue.parse("2", PrimitiveType.CHAR)));
        assertThat(e.getValidValue("Eee").primitiveValue(), is(PrimitiveValue.parse("E", PrimitiveType.CHAR)));
    }

    @Test
    public void shouldThrowExceptionWhenIllegalEncodingTypeSpecified()
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"boolean\" encodingType=\"int64\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        assertThrows(IllegalArgumentException.class, () ->
            parseTestXmlWithMap("/types/enum", testXmlString));
    }

    @Test
    public void shouldThrowExceptionWhenDuplicateValueSpecified()
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"boolean\" encodingType=\"uint8\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"anotherFalse\">0</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        assertThrows(IllegalArgumentException.class, () ->
            parseTestXmlWithMap("/types/enum", testXmlString));
    }

    @Test
    public void shouldThrowExceptionWhenDuplicateNameSpecified()
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"boolean\" encodingType=\"uint8\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"false\">2</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "</types>";

        assertThrows(IllegalArgumentException.class, () ->
            parseTestXmlWithMap("/types/enum", testXmlString));
    }

    @Test
    public void shouldHandleEncodingTypesWithNamedTypes()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "encoding-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertNotNull(fields);

        EnumType type = (EnumType)fields.get(1).type();

        assertThat(type.encodingType(), is(PrimitiveType.CHAR));

        type = (EnumType)fields.get(2).type();

        assertThat(type.encodingType(), is(PrimitiveType.UINT8));
    }

    private static Map<String, Type> parseTestXmlWithMap(final String xPathExpr, final String xml)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new ByteArrayInputStream(xml.getBytes()));
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList list = (NodeList)xPath.compile(xPathExpr).evaluate(document, XPathConstants.NODESET);
        final Map<String, Type> map = new HashMap<>();

        final ParserOptions options = ParserOptions.builder()
            .stopOnError(true)
            .suppressOutput(true)
            .warningsFatal(true)
            .build();

        document.setUserData(XmlSchemaParser.ERROR_HANDLER_KEY, new ErrorHandler(options), null);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            final Type t = new EnumType(list.item(i));
            map.put(t.name(), t);
        }

        return map;
    }
}
