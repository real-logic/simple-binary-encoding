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

import org.junit.Assert;
import org.junit.Test;

import static java.lang.Integer.*;
import static java.lang.Boolean.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class EncodedDataTypeTest
{

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
    public void shouldHandleSettingAllAttributes()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType\" presence=\"required\" primitiveType=\"char\" length=\"1\" variableLength=\"false\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        Type t = map.get("testType");
        assertThat(t.getName(), is("testType"));
        assertThat(t.getPresence(), is(Presence.REQUIRED));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(d.getPrimitiveType(), is(Primitive.CHAR));
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(Boolean.FALSE));
    }

    @Test
    public void shouldHandleMultipleTypes()
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

    @Test
    public void shouldSetAppropriateDefaultsWhenNoneSpecified()
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
        assertThat(t.getPresence(), is(Presence.REQUIRED));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(Boolean.FALSE));
    }

    @Test
    public void shouldUseAppropriatePresence()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeDefault\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeRequired\" presence=\"required\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeOptional\" presence=\"optional\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeConstant\" presence=\"constant\" primitiveType=\"char\">A</type>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeDefault").getPresence(), is(Presence.REQUIRED));
        assertThat(map.get("testTypeRequired").getPresence(), is(Presence.REQUIRED));
        assertThat(map.get("testTypeOptional").getPresence(), is(Presence.OPTIONAL));
        assertThat(map.get("testTypeConstant").getPresence(), is(Presence.CONSTANT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownPresenceSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTyeUnknown\" presence=\"XXXXX\" primitiveType=\"char\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoPrimitiveTypeSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testType\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoNameSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type primitiveType=\"char\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldUseAppropriatePrimitiveType()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeChar\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeInt8\" primitiveType=\"int8\"/>" +
            "<type name=\"testTypeInt16\" primitiveType=\"int16\"/>" +
            "<type name=\"testTypeInt32\" primitiveType=\"int32\"/>" +
            "<type name=\"testTypeInt64\" primitiveType=\"int64\"/>" +
            "<type name=\"testTypeUInt8\" primitiveType=\"uint8\"/>" +
            "<type name=\"testTypeUInt16\" primitiveType=\"uint16\"/>" +
            "<type name=\"testTypeUInt32\" primitiveType=\"uint32\"/>" +
            "<type name=\"testTypeUInt64\" primitiveType=\"uint64\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(((EncodedDataType)map.get("testTypeChar")).getPrimitiveType(), is(Primitive.CHAR));
        assertThat(((EncodedDataType)map.get("testTypeInt8")).getPrimitiveType(), is(Primitive.INT8));
        assertThat(((EncodedDataType)map.get("testTypeInt16")).getPrimitiveType(), is(Primitive.INT16));
        assertThat(((EncodedDataType)map.get("testTypeInt32")).getPrimitiveType(), is(Primitive.INT32));
        assertThat(((EncodedDataType)map.get("testTypeInt64")).getPrimitiveType(), is(Primitive.INT64));
        assertThat(((EncodedDataType)map.get("testTypeUInt8")).getPrimitiveType(), is(Primitive.UINT8));
        assertThat(((EncodedDataType)map.get("testTypeUInt16")).getPrimitiveType(), is(Primitive.UINT16));
        assertThat(((EncodedDataType)map.get("testTypeUInt32")).getPrimitiveType(), is(Primitive.UINT32));
        assertThat(((EncodedDataType)map.get("testTypeUInt64")).getPrimitiveType(), is(Primitive.UINT64));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownPrimitiveTypeSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeUnknown\" primitiveType=\"XXXX\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldReturnCorrectSizeForPrimitiveTypes()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeChar\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeInt8\" primitiveType=\"int8\"/>" +
            "<type name=\"testTypeInt16\" primitiveType=\"int16\"/>" +
            "<type name=\"testTypeInt32\" primitiveType=\"int32\"/>" +
            "<type name=\"testTypeInt64\" primitiveType=\"int64\"/>" +
            "<type name=\"testTypeUInt8\" primitiveType=\"uint8\"/>" +
            "<type name=\"testTypeUInt16\" primitiveType=\"uint16\"/>" +
            "<type name=\"testTypeUInt32\" primitiveType=\"uint32\"/>" +
            "<type name=\"testTypeUInt64\" primitiveType=\"uint64\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(valueOf(map.get("testTypeChar").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeInt8").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeInt16").size()), is(valueOf(2)));
        assertThat(valueOf(map.get("testTypeInt32").size()), is(valueOf(4)));
        assertThat(valueOf(map.get("testTypeInt64").size()), is(valueOf(8)));
        assertThat(valueOf(map.get("testTypeUInt8").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeUInt16").size()), is(valueOf(2)));
        assertThat(valueOf(map.get("testTypeUInt32").size()), is(valueOf(4)));
        assertThat(valueOf(map.get("testTypeUInt64").size()), is(valueOf(8)));
    }

    @Test
    public void shouldReturnCorrectDescriptionForType()
        throws Exception
    {
        final String desc = "basic description attribute of a type element";
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeDescription\" primitiveType=\"char\" description=\"" + desc + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeDescription").getDescription(), is(desc));
    }

    @Test
    public void shouldReturnNullOnNoDescriptionSet()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeNoDescription\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        String description = map.get("testTypeNoDescription").getDescription();
        Assert.assertNull(description);
    }

    @Test
    public void shouldReturnCorrectFixUsageForType()
        throws Exception
    {
        final String fixUsage = "char";
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeFIX\" primitiveType=\"char\" fixUsage=\"" + fixUsage + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeFIX").getFixUsage(), is(FixUsage.CHAR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownFixUsageSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeUnknown\" primitiveType=\"char\" fixUsage=\"XXXX\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldReturnFixUsageNOTSETWhenNotSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" + 
            "<type name=\"testTypeFIX\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeFIX").getFixUsage(), is(FixUsage.NOTSET));
    }        

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenConstantPresenceButNoDataSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testTypePresenceConst\" primitiveType=\"char\" presence=\"constant\">" +
            "</type>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldReturnCorrectPresenceConstantWhenSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testTypePresenceConst\" primitiveType=\"char\" presence=\"constant\">" +
            "F" +
            "</type>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypePresenceConst")).getConstantValue()), is(new PrimitiveValue(Primitive.CHAR, "F")));
    }

    @Test
    public void shouldReturnDefaultMinValueWhenSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testTypeDefaultCharMinValue\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeDefaultInt8MinValue\" primitiveType=\"int8\"/>" +
            "<type name=\"testTypeDefaultInt16MinValue\" primitiveType=\"int16\"/>" +
            "<type name=\"testTypeDefaultInt32MinValue\" primitiveType=\"int32\"/>" +
            "<type name=\"testTypeDefaultInt64MinValue\" primitiveType=\"int64\"/>" +
            "<type name=\"testTypeDefaultUInt8MinValue\" primitiveType=\"uint8\"/>" +
            "<type name=\"testTypeDefaultUInt16MinValue\" primitiveType=\"uint16\"/>" +
            "<type name=\"testTypeDefaultUInt32MinValue\" primitiveType=\"uint32\"/>" +
            "<type name=\"testTypeDefaultUInt64MinValue\" primitiveType=\"uint64\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeDefaultCharMinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_CHAR)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt8MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_INT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt16MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_INT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt32MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_INT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt64MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_INT64)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt8MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_UINT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt16MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_UINT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt32MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_UINT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt64MinValue")).getMinValue()), is(new PrimitiveValue(PrimitiveValue.MIN_VALUE_UINT64)));
    }

    @Test
    public void shouldReturnDefaultMaxValueWhenSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testTypeDefaultCharMaxValue\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeDefaultInt8MaxValue\" primitiveType=\"int8\"/>" +
            "<type name=\"testTypeDefaultInt16MaxValue\" primitiveType=\"int16\"/>" +
            "<type name=\"testTypeDefaultInt32MaxValue\" primitiveType=\"int32\"/>" +
            "<type name=\"testTypeDefaultInt64MaxValue\" primitiveType=\"int64\"/>" +
            "<type name=\"testTypeDefaultUInt8MaxValue\" primitiveType=\"uint8\"/>" +
            "<type name=\"testTypeDefaultUInt16MaxValue\" primitiveType=\"uint16\"/>" +
            "<type name=\"testTypeDefaultUInt32MaxValue\" primitiveType=\"uint32\"/>" +
            "<type name=\"testTypeDefaultUInt64MaxValue\" primitiveType=\"uint64\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeDefaultCharMaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_CHAR)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt8MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_INT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt16MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_INT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt32MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_INT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt64MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_INT64)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt8MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_UINT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt16MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_UINT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt32MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_UINT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt64MaxValue")).getMaxValue()), is(new PrimitiveValue(PrimitiveValue.MAX_VALUE_UINT64)));
    }

    @Test
    public void shouldReturnDefaultNullValueWhenSpecified()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testTypeDefaultCharNullValue\" primitiveType=\"char\"/>" +
            "<type name=\"testTypeDefaultInt8NullValue\" primitiveType=\"int8\"/>" +
            "<type name=\"testTypeDefaultInt16NullValue\" primitiveType=\"int16\"/>" +
            "<type name=\"testTypeDefaultInt32NullValue\" primitiveType=\"int32\"/>" +
            "<type name=\"testTypeDefaultInt64NullValue\" primitiveType=\"int64\"/>" +
            "<type name=\"testTypeDefaultUInt8NullValue\" primitiveType=\"uint8\"/>" +
            "<type name=\"testTypeDefaultUInt16NullValue\" primitiveType=\"uint16\"/>" +
            "<type name=\"testTypeDefaultUInt32NullValue\" primitiveType=\"uint32\"/>" +
            "<type name=\"testTypeDefaultUInt64NullValue\" primitiveType=\"uint64\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeDefaultCharNullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_CHAR)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt8NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_INT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt16NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_INT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt32NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_INT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultInt64NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_INT64)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt8NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_UINT8)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt16NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_UINT16)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt32NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_UINT32)));
        assertThat((((EncodedDataType)map.get("testTypeDefaultUInt64NullValue")).getNullValue()),
                   is(new PrimitiveValue(PrimitiveValue.NULL_VALUE_UINT64)));
    }

    @Test
    public void shouldReturnCorrectMinValueWhenSpecified()
        throws Exception
    {
        final String minVal = Long.toString(10);
        final String testXmlString = "<types>" +
            "<type name=\"testTypeInt8MinValue\" primitiveType=\"int8\" minValue=\"" + minVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8MinValue")).getMinValue()), is(new PrimitiveValue(Primitive.INT8, minVal)));
    }

    @Test
    public void shouldReturnCorrectMaxValueWhenSpecified()
        throws Exception
    {
        final String maxVal = Long.toString(10);
        final String testXmlString = "<types>" +
            "<type name=\"testTypeInt8MaxValue\" primitiveType=\"int8\" maxValue=\"" + maxVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8MaxValue")).getMaxValue()), is(new PrimitiveValue(Primitive.INT8, maxVal)));
    }

    @Test
    public void shouldReturnCorrectNullValueWhenSpecified()
        throws Exception
    {
        final String nullVal = Long.toString(10);
        final String testXmlString = "<types>" +
            "<type name=\"testTypeInt8NullValue\" primitiveType=\"int8\" presence=\"optional\" nullValue=\"" + nullVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8NullValue")).getNullValue()), is(new PrimitiveValue(Primitive.INT8, nullVal)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNullValueWithPresenceRequired()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testType\" primitiveType=\"int8\" presence=\"required\" nullValue=\"10\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNullValueWithPresenceConstant()
        throws Exception
    {
        final String testXmlString = "<types>" +
            "<type name=\"testType\" primitiveType=\"int8\" presence=\"constant\" nullValue=\"10\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }
}
