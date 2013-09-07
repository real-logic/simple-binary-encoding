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
package uk.co.real_logic.sbe;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

import org.junit.Assert;
import org.junit.Test;

import static java.lang.Integer.*;
import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EncodedDataTypeTest
{
    /**
     * These tests do not use the XSD. So, some attributes should be set by EncodedDataType
     * - length
     * - variableLength
     */

    /**
     * Grab type nodes, parse them, and populate map for those types.
     *
     * @param xPathExpr for type nodes in XML
     * @param xml string to parse
     * @return map of name to EncodedDataType nodes
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
            Type t = new EncodedDataType(list.item(i));
            map.put(t.getName(), t);
        }
        return map;
    }

    @Test
    public void basicSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType\" presence=\"required\" primitiveType=\"char\" length=\"1\" variableLength=\"false\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        Type t = map.get("testType");
        assertThat(t.getName(), is("testType"));
        assertThat(t.getPresence().toString(), is(Presence.REQUIRED.toString()));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(d.getPrimitiveType().toString(), is(Primitive.CHAR.toString()));
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(valueOf(Boolean.FALSE)));
    }

    @Test
    public void defaultAttributesSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        assertThat(map.get("testType").getName(), is("testType"));
        // assert defaults for length, variableLength and presence
        Type t = map.get("testType");
        assertThat(t.getPresence().toString(), is(Presence.REQUIRED.toString()));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(valueOf(Boolean.FALSE)));
    }

    @Test
    public void presenceAttributeSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeDefault\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeRequired\" presence=\"required\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeOptional\" presence=\"optional\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeConstant\" presence=\"constant\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeDefault").getPresence().toString(), is(Presence.REQUIRED.toString()));
        assertThat(map.get("testTypeRequired").getPresence().toString(), is(Presence.REQUIRED.toString()));
        assertThat(map.get("testTypeOptional").getPresence().toString(), is(Presence.OPTIONAL.toString()));
        assertThat(map.get("testTypeConstant").getPresence().toString(), is(Presence.CONSTANT.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownPresenceAttributeSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTyeUnknown\" presence=\"XXXXX\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noPrimitiveTypeAttributeSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noNameAttributeSingleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void basicMultipleTypeTest()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType1\" presence=\"required\" primitiveType=\"char\" length=\"1\" variableLength=\"false\"/>" +
            "<type name=\"testType2\" presence=\"required\" primitiveType=\"int8\" length=\"1\" variableLength=\"false\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        assertThat(valueOf(map.size()), is(valueOf(2)));
        assertThat(map.get("testType1").getName(), is("testType1"));
        assertThat(map.get("testType2").getName(), is("testType2"));
    }

    /**
     * Tests for:
     * - primitiveType adding themselves correctly
     * - correct sizing set for Type
     * - description being saved
     * - fixUsage being saved
     */
}
