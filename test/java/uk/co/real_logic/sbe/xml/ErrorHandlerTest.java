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

import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.TestUtil;

import org.junit.Test;
import org.w3c.dom.Document;
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
import java.util.Map;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class ErrorHandlerTest
{
    @Test
    public void shouldNotExitOnTypeErrorsAndWarnings()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "<enum name=\"NullBoolean\" encodingType=\"uint8\" nullVal=\"255\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "<enum name=\"DupNameBoolean\" encodingType=\"uint8\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"anotherFalse\">0</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "<enum name=\"DupValBoolean\" encodingType=\"uint8\" semanticType=\"Boolean\">" +
            "    <validValue name=\"false\">0</validValue>" +
            "    <validValue name=\"false\">2</validValue>" +
            "    <validValue name=\"true\">1</validValue>" +
            "</enum>" +
            "<set name=\"DupValueSet\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"AnotherBit0\">0</choice>" +
            "</set>" +
            "<set name=\"DupNameSet\" encodingType=\"uint8\">" +
            "    <choice name=\"Bit0\">0</choice>" +
            "    <choice name=\"Bit0\">1</choice>" +
            "</set>" +
            "<composite name=\"decimal\">" +
            "    <type name=\"mantissa\" primitiveType=\"int64\"/>" +
            "    <type name=\"mantissa\" primitiveType=\"int64\"/>" +
            "    <type name=\"exponent\" primitiveType=\"int8\"/>" +
            "</composite>" +
            "<type name=\"ConstButNoValue\" primitiveType=\"char\" presence=\"constant\"></type>" +
            "<type name=\"NullButNotOptional\" primitiveType=\"int8\" presence=\"required\" nullVal=\"10\"/>" +
            "</types>";

        Map<String, Type> map = new HashMap<>();
        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "false");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL, "false");
        ErrorHandler handler = new ErrorHandler();

        parseTestXmlAddToMap(map, "/types/composite", testXmlString, handler);
        parseTestXmlAddToMap(map, "/types/type", testXmlString, handler);
        parseTestXmlAddToMap(map, "/types/enum", testXmlString, handler);
        parseTestXmlAddToMap(map, "/types/set", testXmlString, handler);

        assertThat(valueOf(handler.errorCount()), is(valueOf(3)));
        assertThat(valueOf(handler.warningCount()), is(valueOf(19)));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldExitAfterTypes()
        throws Exception
    {
        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "false");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL, "false");

        try
        {
            parse(TestUtil.getLocalResource("ErrorHandlerTypesTest.xml"));
        }
        catch (IllegalArgumentException shouldHaveOnly2Errors)
        {
            assertThat(shouldHaveOnly2Errors.getMessage(), is("had 2 errors"));

            throw shouldHaveOnly2Errors;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldExitAfterTypesWhenDupTypesDefined()
        throws Exception
    {
        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "false");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL, "true");

        try
        {
            parse(TestUtil.getLocalResource("ErrorHandlerTypesDupTest.xml"));
        }
        catch (IllegalArgumentException shouldHaveOnly1Warning)
        {
            assertThat(shouldHaveOnly1Warning.getMessage(), is("had 1 warnings"));

            throw shouldHaveOnly1Warning;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldExitAfterMessageWhenDupMessageIdsDefined()
        throws Exception
    {
        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "false");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL, "true");

        try
        {
            parse(TestUtil.getLocalResource("ErrorHandlerDupMessageTest.xml"));
        }
        catch (IllegalArgumentException shouldHaveOnly1Error)
        {
            assertThat(shouldHaveOnly1Error.getMessage(), is("had 1 errors"));

            throw shouldHaveOnly1Error;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldExitAfterMessage()
        throws Exception
    {
        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "false");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_WARNINGS_FATAL, "true");

        try
        {
            parse(TestUtil.getLocalResource("ErrorHandlerMessageTest.xml"));
        }
        catch (IllegalArgumentException shouldHaveOnly12Errors)
        {
            assertThat(shouldHaveOnly12Errors.getMessage(), is("had 12 errors"));

            throw shouldHaveOnly12Errors;
        }
    }

    /*
     * TODO:
     * left over entry count and length field (warning)
     * dup field id? (not currently tracked)
     */

    private static void parseTestXmlAddToMap(final Map<String, Type> map,
                                             final String xPathExpr,
                                             final String xml,
                                             final ErrorHandler handler)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(xPathExpr).evaluate(document, XPathConstants.NODESET);

        document.setUserData(XmlSchemaParser.XML_ERROR_HANDLER_KEY, handler, null);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Type type = null;

            if (xPathExpr.endsWith("enum"))
            {
                type = new EnumType(list.item(i));
            }
            else if (xPathExpr.endsWith("set"))
            {
                type = new SetType(list.item(i));
            }
            else if (xPathExpr.endsWith("type"))
            {
                type = new EncodedDataType(list.item(i));
            }
            else if (xPathExpr.endsWith("composite"))
            {
                type = new CompositeType(list.item(i));
            }

            if (type != null)
            {
                map.put(type.name(), type);
            }
        }
    }
}
