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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulate the XML Schema parsing for SBE so that other representations may be used to generate IR
 */
public class XmlSchemaParser
{
    /** XPath expression for accessing the type nodes under types */
    public static final String typeXPathExpr = "/messageSchema/types/type";

    /** XPath expression for accessing the composite nodes under types */
    public static final String compositeXPathExpr = "/messageSchema/types/composite";

    /** XPath expression for accessing the enum nodes under types */
    public static final String enumXPathExpr = "/messageSchema/types/enum";

    /** XPath expression for accessing the set nodes under types */
    public static final String setXPathExpr = "/messageSchema/types/set";

    /** XPath expression for accessing the message nodes under messageSchema */
    public static final String messageXPathExpr = "/messageSchema/message";

    /**
     * Take an input stream and parse it generating Intermediate Representation.
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param stream to read schema from
     * @return list of Intermediate Representation nodes
     */
    public static List<IrNode> parseAndGenerateIr(final InputStream stream)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        /** set up XML parsing */
        /**
         * We could do the builder by pieces, but ... why?
         * DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         * DocumentBuilder builder = builderFactory.newDocumentBuilder();
         * Document document = builder.parse(stream);
         */
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        XPath xPath = XPathFactory.newInstance().newXPath();

        /** init types table/map for lookup by <field> elements */
        Map<String, Type> typesMap = new HashMap<String, Type>();

        // add primitiveTypes to typesMap - these could be in a static XInclude that is always brought in...
        // TODO: add in the minValue, maxValue, and nullValue settings to these
        typesMap.put("char", new EncodedDataType("char", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.CHAR, 1, false));
        typesMap.put("int8", new EncodedDataType("int8", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT8, 1, false));
        typesMap.put("int16", new EncodedDataType("int16", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT16, 1, false));
        typesMap.put("int32", new EncodedDataType("int32", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT32, 1, false));
        typesMap.put("int64", new EncodedDataType("int64", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT64, 1, false));
        typesMap.put("uint8", new EncodedDataType("uint8", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT8, 1, false));
        typesMap.put("uint16", new EncodedDataType("uint16", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT16, 1, false));
        typesMap.put("uint32", new EncodedDataType("uint32", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT32, 1, false));
        typesMap.put("uint64", new EncodedDataType("uint64", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT64, 1, false));

        /** grab all "type" types (encodedDataType) and add to types table */
        addEncodedDataTypes(typesMap, (NodeList)xPath.compile(typeXPathExpr).evaluate(document, XPathConstants.NODESET));
        /** grab all "composite" types (compositeType) and add to types table */
        addCompositeTypes(typesMap, (NodeList)xPath.compile(compositeXPathExpr).evaluate(document, XPathConstants.NODESET));
        /** grab all "enum" types (enumType) and add to types table */
        addEnumTypes(typesMap, (NodeList)xPath.compile(enumXPathExpr).evaluate(document, XPathConstants.NODESET));
        /** grab all "set" types (setType) and add to types table */
        addSetTypes(typesMap, (NodeList)xPath.compile(setXPathExpr).evaluate(document, XPathConstants.NODESET));

        /** TODO: once all <types> handled, we can move to the actual encoding layout */

        /** TODO: grab all <message> elements and handle them - this is where IR is generated */
        return null;
    }

    /**
     * Add encodedDataType (if any) to Types Map
     *
     * @param map  of types
     * @param list of Nodes
     */
    private static void addEncodedDataTypes(Map<String, Type> map, NodeList list)
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Type t = new EncodedDataType(list.item(i));
            map.put(t.getName(), t);
        }
    }

    /**
     * Add compositeType (if any) to Types Map
     *
     * @param map  of types
     * @param list of Nodes
     */
    private static void addCompositeTypes(Map<String, Type> map, NodeList list)
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            // TODO: may need to pass in map to constructor so it can look up the types
            // System.out.println(list.item(i).getFirstChild().getNodeValue());
        }
    }

    /**
     * Add enumType (if any) to Types Map
     *
     * @param map  of types
     * @param list of Nodes
     */
    private static void addEnumTypes(Map<String, Type> map, NodeList list)
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            // System.out.println(list.item(i).getFirstChild().getNodeValue());
        }
    }

    /**
     * Add setType (if any) to Types Map
     *
     * @param map  of types
     * @param list of Nodes
     */
    private static void addSetTypes(Map<String, Type> map, NodeList list)
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            // System.out.println(list.item(i).getFirstChild().getNodeValue());
        }
    }

    /**
     * Helper function that throws an exception when the attribute is not set
     *
     * @param node that should have the attribute
     * @param attrName that is to be looked up
     * @return value of the attibute
     * @throws IllegalArgumentException if the attribute is not present
     */
    public static String getXmlAttributeValue(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
            throw new IllegalArgumentException("Element attribute is not present: " + attrName);
        return n.getNodeValue();
    }

    /**
     * Helper function that uses a default value when value not set
     *
     * @param node that should have the attribute
     * @param attrName that is to be looked up
     * @param defValue String to return if not set
     * @return value of the attibute or defValue
     */
    public static String getXmlAttributeValue(final Node node, final String attrName, final String defValue)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
            return defValue;
        return n.getNodeValue();
    }

    /**
     * Helper function that hides the null return from {@link org.w3c.dom.Node.getNamedItem()}
     *
     * @param node that could be null
     * @param attrName that is to be looked up
     * @return null or value of the attribute
     */
    public static String getXmlAttributeValueNullable(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
            return null;
        return n.getNodeValue();
    }
}
