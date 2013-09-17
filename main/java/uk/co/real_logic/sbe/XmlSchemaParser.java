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
    /**
     * XPath expression for accessing the type nodes under types
     */
    public static final String typeXPathExpr = "/messageSchema/types/type";

    /**
     * XPath expression for accessing the composite nodes under types
     */
    public static final String compositeXPathExpr = "/messageSchema/types/composite";

    /**
     * XPath expression for accessing the enum nodes under types
     */
    public static final String enumXPathExpr = "/messageSchema/types/enum";

    /**
     * XPath expression for accessing the set nodes under types
     */
    public static final String setXPathExpr = "/messageSchema/types/set";

    /**
     * XPath expression for accessing the message nodes under messageSchema
     */
    public static final String messageXPathExpr = "/messageSchema/message";

    /**
     * XPath expression for accessing the messageSchema root document node
     */
    public static final String messageSchemaXPathExpr = "/messageSchema";

    /**
     * Take an input stream and parse it generating Intermediate Representation.
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param stream to read schema from
     * @return list of Intermediate Representation nodes
     */
    public static List<IrNode> parseAndGenerateIr(final InputStream stream)
        throws Exception
    {
        /* set up XML parsing */
        /*
         * We could do the builder by pieces, but ... why?
         * DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         * DocumentBuilder builder = builderFactory.newDocumentBuilder();
         * Document document = builder.parse(stream);
         */
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        XPath xPath = XPathFactory.newInstance().newXPath();

        /* Grab messageSchema attributes */
        /*
         * package
         * version - optional
         * description - optional
         * byteOrder - bigEndian or littleEndian (default)
         * TODO: save these in the IrNode
         */
        Node messageSchemaNode = (Node)xPath.compile(messageSchemaXPathExpr).evaluate(document, XPathConstants.NODE);

        String pack = getXmlAttributeValue(messageSchemaNode, "package");
        String description = getXmlAttributeValueNullable(messageSchemaNode, "description");
        String version = getXmlAttributeValueNullable(messageSchemaNode, "version");
        String byteOrder = getXmlAttributeValue(messageSchemaNode, "byteOrder", "littleEndian");

        /* grab all types and populate map of names to Type objects */
        Map<String, Type> typesMap = populateTypesMap(document, xPath);


        /* TODO: once all <types> handled, we can move to the actual encoding layout */
        /**
         * TODO: check for messageHeader type and use it for the main header
         */
        /* TODO: grab all <message> elements and handle them - this is where IR is generated */
        /**
         * TODO: need a message object to hold sequenced fields. Fields point back to Types. Traversing the fields generates IrNodes
         */
        return null;
    }

    /**
     * Scan XML for all types (encodedDataType, compositeType, enumType, and setType) and save in map
     *
     * @param document for the XML parsing
     * @param xPath    for XPath expression reuse
     * @return {@link java.util.Map} of name {@link java.lang.String} to Type
     */
    public static Map<String, Type> populateTypesMap(Document document, XPath xPath)
        throws Exception
    {
        final Map<String, Type> typesMap = new HashMap<String, Type>();

        // add primitiveTypes to typesMap - these could be in a static XInclude that is always brought in...
        typesMap.put("char", new EncodedDataType("char", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.CHAR, 1, false));
        typesMap.put("int8", new EncodedDataType("int8", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT8, 1, false));
        typesMap.put("int16", new EncodedDataType("int16", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT16, 1, false));
        typesMap.put("int32", new EncodedDataType("int32", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT32, 1, false));
        typesMap.put("int64", new EncodedDataType("int64", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.INT64, 1, false));
        typesMap.put("uint8", new EncodedDataType("uint8", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT8, 1, false));
        typesMap.put("uint16", new EncodedDataType("uint16", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT16, 1, false));
        typesMap.put("uint32", new EncodedDataType("uint32", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT32, 1, false));
        typesMap.put("uint64", new EncodedDataType("uint64", Presence.REQUIRED, null, FixUsage.NOTSET, Primitive.UINT64, 1, false));

        iterateOverNodeList((NodeList)xPath.compile(typeXPathExpr).evaluate(document, XPathConstants.NODESET),
                            new IteratorCallback() 
                            {
                                @Override
                                public void execute(Node node) throws Exception
                                {
                                    addTypeWithNameCheck(typesMap, new EncodedDataType(node));
                                }
                            });
        
        iterateOverNodeList((NodeList)xPath.compile(compositeXPathExpr).evaluate(document, XPathConstants.NODESET),
                            new IteratorCallback() 
                            {
                                @Override
                                public void execute(Node node) throws Exception
                                {
                                    addTypeWithNameCheck(typesMap, new CompositeType(node));
                                }
                            });

        iterateOverNodeList((NodeList)xPath.compile(enumXPathExpr).evaluate(document, XPathConstants.NODESET),
                            new IteratorCallback() 
                            {
                                @Override
                                public void execute(Node node) throws Exception
                                {
                                    addTypeWithNameCheck(typesMap, new EnumType(node));
                                }
                            });

        iterateOverNodeList((NodeList)xPath.compile(setXPathExpr).evaluate(document, XPathConstants.NODESET),
                            new IteratorCallback()
                            {
                                @Override
                                public void execute(Node node) throws Exception
                                {
                                    addTypeWithNameCheck(typesMap, new SetType(node));
                                }
                            });

        return typesMap;
    }

    /**
     * Helper function to add a Type to a map based on name. Checks to make sure name does not exist.
     *
     * @param map  of names to Type objects
     * @param type to be added to map
     */
    private static void addTypeWithNameCheck(Map<String, Type> map, Type type)
    {
        if (map.get(type.getName()) != null)
        {
            throw new IllegalArgumentException("SBE type already exists: " + type.getName());
        }

        map.put(type.getName(), type);
    }

    /**
     * Helper function that throws an exception when the attribute is not set
     *
     * @param node     that should have the attribute
     * @param attrName that is to be looked up
     * @return value of the attibute
     * @throws IllegalArgumentException if the attribute is not present
     */
    public static String getXmlAttributeValue(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
        {
            throw new IllegalArgumentException("Element attribute is not present: " + attrName);
        }

        return n.getNodeValue();
    }

    /**
     * Helper function that uses a default value when value not set
     *
     * @param node     that should have the attribute
     * @param attrName that is to be looked up
     * @param defValue String to return if not set
     * @return value of the attibute or defValue
     */
    public static String getXmlAttributeValue(final Node node, final String attrName, final String defValue)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
        {
            return defValue;
        }

        return n.getNodeValue();
    }

    /**
     * Helper function that hides the null return from {@link org.w3c.dom.NamedNodeMap#getNamedItem(String)}
     *
     * @param node     that could be null
     * @param attrName that is to be looked up
     * @return null or value of the attribute
     */
    public static String getXmlAttributeValueNullable(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
        {
            return null;
        }

        return n.getNodeValue();
    }

    /**
     * Interface for iterator callback objects
     */
    private interface IteratorCallback
    {
        public void execute(final Node node) throws Exception;
    }

    /**
     * Add compositeType (if any) to Types Map
     *
     * @param list     of Nodes
     * @param callback object to execute for each node
     */
    private static void iterateOverNodeList(NodeList list, IteratorCallback cb)
        throws Exception
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            cb.execute(list.item(i));
        }
    }

    
}
