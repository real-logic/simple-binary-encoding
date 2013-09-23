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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.co.real_logic.sbe.Primitive;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate the XML Schema parsing for SBE so that other representations may be used to generate IR
 */
public class XmlSchemaParser
{
    private static final String typeXPathExpr = "/messageSchema/types/type";
    private static final String compositeXPathExpr = "/messageSchema/types/composite";
    private static final String enumXPathExpr = "/messageSchema/types/enum";
    private static final String setXPathExpr = "/messageSchema/types/set";
    private static final String messageXPathExpr = "/messageSchema/message";
    private static final String messageSchemaXPathExpr = "/messageSchema";

    /**
     * Take an input stream and parse it generating map of template ID to Message objects, types, and schema
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param stream to read schema from
     * @return {@link MessageSchema} object that holds the schema
     */
    public static MessageSchema parseXmlAndGenerateMessageSchema(final InputStream stream)
        throws Exception
    {
        /*
         * We could do the builder by pieces, but ... why?
         * DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         * DocumentBuilder builder = builderFactory.newDocumentBuilder();
         * Document document = builder.parse(stream);
         */
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        XPath xPath = XPathFactory.newInstance().newXPath();

        /* grab all types and populate map of names to Type objects */
        Map<String, Type> typeMap = populateTypeMap(document, xPath);

        /* grab all messages defined and populate map of id to Message objects */
        Map<Long, Message> messageMap = populateMessageMap(document, xPath, typeMap);

        /* grab messageSchema attributes and save maps*/
        final Node node = (Node)xPath.compile(messageSchemaXPathExpr).evaluate(document, XPathConstants.NODE);
        MessageSchema schema = new MessageSchema(node, typeMap, messageMap);

        // TODO: run additional checks and validation on Messages in Message Map

        return schema;
    }

    /*
     * What is difference between Message and the IR?
     * - IR is platform, schema, and language independent. It is abstract layout & metadata only.
     * - Message is FIX/SBE XML Schema specific.
     */
    
    /* TODO: check for messageHeader type and use it for the main header */
    /* TODO: Message needs to hold (in addition to fields): schemaPackage, schemaDescription, schemaVersion, schemaByteOrder, messageHeader */
    
    /*
     * TODO: need a message object to hold sequenced fields. Fields point back to Types. Traversing the fields generates IrNodes
     * - instead of List<IrNode>, need a container, IrContainer
     *   - IrContainer (or IrMessage?)
     *     - is representation of a single message
     *     - has
     *       - List<IrNode> for fields and groups (Elements)
     *       - package, version, description, byteOrder, messageHeader, etc.
     *     - is representation of a single message
     * - each Ir element needs to have its own ByteOrder (optionally) - suggested by Gil
     * - separate functions:
     *   - IrContainer generateIrFromMessage(message) = message has all the associated references to Types, etc.
     *   - IrContainer optimizeForSpace(IrContainer) = generates new IrContainer with optimization
     *   - IrContainer optimizeForDecodeSpeed(IrContainer) = generates new IrContainer with optimization
     *   - IrContainer optimizeForEncodeSpeed(IrContainer) = generates new IrContainer with optimization
     *   - IrContainer optimizeForOnTheFlyDecoder(IrContainer) = generates new IrContainer that uses embedded type fields for On-The-Fly optimization
     *   - String generateFixSbeSchemaFromIr(IrContainer)
     *   - String generateAsn1FromIr(IrContainer)
     *   - String generateGpbFromIr(IrContainer)
     *   - String generateThriftFromIr(IrContainer)
     *
     * Ultra-Meta
     *   - IrContainer parseIrAndGenerateIr(filename) = read in serialized (with SBE) Ir and generate a new internal IrContainer
     */

    /**
     * Scan XML for all types (encodedDataType, compositeType, enumType, and setType) and save in map
     *
     * @param document for the XML parsing
     * @param xPath    for XPath expression reuse
     * @return {@link java.util.Map} of name {@link java.lang.String} to Type
     */
    public static Map<String, Type> populateTypeMap(final Document document, final XPath xPath)
        throws Exception
    {
        final Map<String, Type> typesMap = new HashMap<String, Type>();

        // add primitiveTypes to typesMap - these could be in a static XInclude that is always brought in...
        typesMap.put("char", new EncodedDataType("char", Presence.REQUIRED, null, null, Primitive.CHAR, 1, false));
        typesMap.put("int8", new EncodedDataType("int8", Presence.REQUIRED, null, null, Primitive.INT8, 1, false));
        typesMap.put("int16", new EncodedDataType("int16", Presence.REQUIRED, null, null, Primitive.INT16, 1, false));
        typesMap.put("int32", new EncodedDataType("int32", Presence.REQUIRED, null, null, Primitive.INT32, 1, false));
        typesMap.put("int64", new EncodedDataType("int64", Presence.REQUIRED, null, null, Primitive.INT64, 1, false));
        typesMap.put("uint8", new EncodedDataType("uint8", Presence.REQUIRED, null, null, Primitive.UINT8, 1, false));
        typesMap.put("uint16", new EncodedDataType("uint16", Presence.REQUIRED, null, null, Primitive.UINT16, 1, false));
        typesMap.put("uint32", new EncodedDataType("uint32", Presence.REQUIRED, null, null, Primitive.UINT32, 1, false));
        typesMap.put("uint64", new EncodedDataType("uint64", Presence.REQUIRED, null, null, Primitive.UINT64, 1, false));

        forEach((NodeList)xPath.compile(typeXPathExpr).evaluate(document, XPathConstants.NODESET),
                new Function()
                {
                    public void execute(final Node node) throws Exception
                    {
                        addTypeWithNameCheck(typesMap, new EncodedDataType(node));
                    }
                });
        
        forEach((NodeList)xPath.compile(compositeXPathExpr).evaluate(document, XPathConstants.NODESET),
                new Function()
                {
                    public void execute(final Node node) throws Exception
                    {
                        addTypeWithNameCheck(typesMap, new CompositeType(node));
                    }
                });

        forEach((NodeList)xPath.compile(enumXPathExpr).evaluate(document, XPathConstants.NODESET),
                new Function()
                {
                    public void execute(final Node node) throws Exception
                    {
                        addTypeWithNameCheck(typesMap, new EnumType(node));
                    }
                });

        forEach((NodeList)xPath.compile(setXPathExpr).evaluate(document, XPathConstants.NODESET),
                new Function()
                {
                    public void execute(final Node node) throws Exception
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
     * Scan XML for all message definitions and save in map
     *
     * @param document for the XML parsing
     * @param xPath    for XPath expression reuse
     * @param typesMap to use for Type objects
     * @return {@link java.util.Map} of id to Message
     */
    public static Map<Long, Message> populateMessageMap(final Document document,
                                                        final XPath xPath,
                                                        final Map<String, Type> typesMap)
        throws Exception
    {
        final Map<Long, Message> map = new HashMap<Long, Message>();

        forEach((NodeList)xPath.compile(messageXPathExpr).evaluate(document, XPathConstants.NODESET),
                new Function()
                {
                    public void execute(final Node node) throws Exception
                    {
                        addMessageWithIdCheck(map, new Message(node, typesMap));
                    }
                });

        return map;
    }

    /**
     * Helper function to add a Message to a map based on id. Checks to make sure id does not exist.
     *
     * @param map     of id to Message objects
     * @param message to be added to map
     */
    private static void addMessageWithIdCheck(Map<Long, Message> map, Message message)
    {
        if (map.get(Long.valueOf(message.getId())) != null)
        {
            throw new IllegalArgumentException("SBE message template id already exists: " + message.getId());
        }

        map.put(Long.valueOf(message.getId()), message);
    }

    /**
     * Helper function that throws an exception when the attribute is not set
     *
     * @param node     that should have the attribute
     * @param attrName that is to be looked up
     * @return value of the attribute
     * @throws IllegalArgumentException if the attribute is not present
     */
    public static String getXmlAttributeValue(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null || n.getNodeValue().equals(""))
        {
            throw new IllegalArgumentException("Element attribute is not present or is empty: " + attrName);
        }

        return n.getNodeValue();
    }

    /**
     * Helper function that uses a default value when value not set
     *
     * @param node     that should have the attribute
     * @param attrName that is to be looked up
     * @param defValue String to return if not set
     * @return value of the attribute or defValue
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
    public static String getXmlAttributeValueOrNull(final Node node, final String attrName)
    {
        Node n = node.getAttributes().getNamedItem(attrName);

        if (n == null)
        {
            return null;
        }

        return n.getNodeValue();
    }

    /**
     * Helper function to convert a schema byteOrder into a {@link ByteOrder}
     *
     * @param order specified as a FIX SBE string
     * @return ByteOrder representation
     */
    public static ByteOrder lookupByteOrder(final String order)
    {
        switch (order)
        {
            case "littleEndian":
                return ByteOrder.LITTLE_ENDIAN;

            case "bigEndian":
                return ByteOrder.BIG_ENDIAN;

            default:
                return ByteOrder.LITTLE_ENDIAN;
        }
    }

    /**
     * Function to be applied to Node objects
     */
    private interface Function
    {
        public void execute(final Node node) throws Exception;
    }

    /**
     * Add compositeType (if any) to Types Map
     *
     * @param list     of Nodes
     * @param function object to execute for each node
     */
    private static void forEach(final NodeList list, final Function function)
        throws Exception
    {
        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            function.execute(list.item(i));
        }
    }
}
