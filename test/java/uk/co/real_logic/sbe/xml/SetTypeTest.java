/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package uk.co.real_logic.sbe.xml;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.real_logic.sbe.Primitive;
import uk.co.real_logic.sbe.PrimitiveValue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SetTypeTest
{

    /**
     * Grab type nodes, parse them, and populate map for those types.
     *
     * @param xPathExpr for type nodes in XML
     * @param xml       string to parse
     * @return map of name to SetType nodes
     */
    private static Map<String, Type> parseTestXmlWithMap(final String xPathExpr, final String xml)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(xPathExpr).evaluate(document, XPathConstants.NODESET);
        Map<String, Type> map = new HashMap<String, Type>();

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Type t = new SetType(list.item(i));
            map.put(t.getName(), t);
        }
        return map;
    }

    @Test
    public void shouldHandleBinarySetType()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"biOp\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\" description=\"Bit 0\">0</choice>" +
            "    <choice name=\"Bit1\" description=\"Bit 1\">1</choice>" +
            "</set>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/set", testXmlString);
        SetType e = (SetType)map.get("biOp");
        assertThat(e.getName(), is("biOp"));
        assertThat(e.getEncodingType(), is(Primitive.UINT8));
        assertThat(valueOf(e.getChoiceSet().size()), is(valueOf(2)));
        assertThat(e.getChoice("Bit1").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "1")));
        assertThat(e.getChoice("Bit0").getPrimitiveValue(), is(new PrimitiveValue(Primitive.UINT8, "0")));
    }

    @Test
    public void shouldHandleSetTypeList()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"listed\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"Bit1\">1</choice>" +
            "    <choice name=\"Bit2\">2</choice>" +
            "    <choice name=\"Bit3\">3</choice>" +
            "</set>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/set", testXmlString);
        SetType e = (SetType)map.get("listed");
        assertThat(e.getEncodingType(), is(Primitive.UINT8));

        int foundBit0 = 0, foundBit1 = 0, foundBit2 = 0, foundBit3 = 0, count = 0;
        for (Map.Entry<String, SetType.Choice> entry : e.getChoiceSet())
        {
            if (entry.getKey().equals("Bit0"))
            {
                foundBit0++;
            }
            else if (entry.getKey().equals("Bit1"))
            {
                foundBit1++;
            }
            else if (entry.getKey().equals("Bit2"))
            {
                foundBit2++;
            }
            else if (entry.getKey().equals("Bit3"))
            {
                foundBit3++;
            }
            count++;
        }
        assertThat(valueOf(count), is(valueOf(4)));
        assertThat(valueOf(foundBit0), is(valueOf(1)));
        assertThat(valueOf(foundBit1), is(valueOf(1)));
        assertThat(valueOf(foundBit2), is(valueOf(1)));
        assertThat(valueOf(foundBit3), is(valueOf(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenIllegalEncodingTypeSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"biOp\" encodingType=\"char\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"Bit1\">1</choice>" +
            "</set>" +
            "</types>";

        parseTestXmlWithMap("/types/set", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDuplicateValueSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"biOp\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"AnotherBit0\">0</choice>" +
            "</set>" +
            "</types>";

        parseTestXmlWithMap("/types/set", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenDuplicateNameSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"biOp\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"Bit0\">1</choice>" +
            "</set>" +
            "</types>";

        parseTestXmlWithMap("/types/set", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenValueOutOfBoundsSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<set name=\"biOp\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"Bit100\">100</choice>" +
            "</set>" +
            "</types>";

        parseTestXmlWithMap("/types/set", testXmlString);
    }
}
