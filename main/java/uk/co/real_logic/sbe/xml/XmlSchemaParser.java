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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.co.real_logic.sbe.PrimitiveType;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate the XML Schema parsing for SBE so that other representations may be
 * used to generate independent representation.
 */
public class XmlSchemaParser
{
    public static final String XML_ERROR_HANDLER_KEY = "SBEErrorHandler";

    private static final String TYPE_XPATH_EXPR = "/messageSchema/types/type";
    private static final String COMPOSITE_XPATH_EXPR = "/messageSchema/types/composite";
    private static final String ENUM_XPATH_EXPR = "/messageSchema/types/enum";
    private static final String SET_XPATH_EXPR = "/messageSchema/types/set";
    private static final String MESSAGE_XPATH_EXPR = "/messageSchema/message";
    private static final String MESSAGE_SCHEMA_XPATH_EXPR = "/messageSchema";

    /**
     * Take an {@link InputStream} and parse it generating map of template ID to Message objects, types, and schema
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param in stream from which schema is read.
     * @return {@link MessageSchema} options for the schema.
     */
    public static MessageSchema parse(final InputStream in)
        throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setXIncludeAware(true);
        factory.setNamespaceAware(true);

        String xsdFilename = System.getProperty(SbeTool.SBE_VALIDATE_XSD);
        if (xsdFilename != null)
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setSchema(schemaFactory.newSchema(new File(xsdFilename)));
        }

        Document document = factory.newDocumentBuilder().parse(in);
        XPath xPath = XPathFactory.newInstance().newXPath();

        /* saving the error handling state in the XML DOM tree. */
        ErrorHandler errorHandler = new ErrorHandler();
        document.setUserData(XML_ERROR_HANDLER_KEY, errorHandler, null);

        Map<String, Type> typeByNameMap = findTypes(document, xPath);

        errorHandler.checkIfShouldExit();

        Map<Long, Message> messageByIdMap = findMessages(document, xPath, typeByNameMap);

        errorHandler.checkIfShouldExit();

        final Node schemaNode = (Node)xPath.compile(MESSAGE_SCHEMA_XPATH_EXPR).evaluate(document, XPathConstants.NODE);
        MessageSchema messageSchema = new MessageSchema(schemaNode, typeByNameMap, messageByIdMap);

        errorHandler.checkIfShouldExit();

        // TODO: run additional checks and validation on Messages in Message Map

        return messageSchema;
    }

    /**
     * Scan XML for all types (encodedDataType, compositeType, enumType, and setType) and save in map
     *
     * @param document for the XML parsing
     * @param xPath    for XPath expression reuse
     * @return {@link java.util.Map} of name {@link java.lang.String} to Type
     */
    public static Map<String, Type> findTypes(final Document document, final XPath xPath)
        throws Exception
    {
        final Map<String, Type> typeByNameMap = new HashMap<>();

        // Add primitiveTypes to typeByNameMap - these could be in a static XInclude that is always brought in...
        typeByNameMap.put("char", new EncodedDataType("char", Presence.REQUIRED, null, null, PrimitiveType.CHAR, 1, false));
        typeByNameMap.put("int8", new EncodedDataType("int8", Presence.REQUIRED, null, null, PrimitiveType.INT8, 1, false));
        typeByNameMap.put("int16", new EncodedDataType("int16", Presence.REQUIRED, null, null, PrimitiveType.INT16, 1, false));
        typeByNameMap.put("int32", new EncodedDataType("int32", Presence.REQUIRED, null, null, PrimitiveType.INT32, 1, false));
        typeByNameMap.put("int64", new EncodedDataType("int64", Presence.REQUIRED, null, null, PrimitiveType.INT64, 1, false));
        typeByNameMap.put("uint8", new EncodedDataType("uint8", Presence.REQUIRED, null, null, PrimitiveType.UINT8, 1, false));
        typeByNameMap.put("uint16", new EncodedDataType("uint16", Presence.REQUIRED, null, null, PrimitiveType.UINT16, 1, false));
        typeByNameMap.put("uint32", new EncodedDataType("uint32", Presence.REQUIRED, null, null, PrimitiveType.UINT32, 1, false));
        typeByNameMap.put("uint64", new EncodedDataType("uint64", Presence.REQUIRED, null, null, PrimitiveType.UINT64, 1, false));

        forEach((NodeList)xPath.compile(TYPE_XPATH_EXPR).evaluate(document, XPathConstants.NODESET),
                new NodeFunction()
                {
                    public void execute(final Node node) throws XPathExpressionException
                    {
                        addTypeWithNameCheck(typeByNameMap, new EncodedDataType(node), node);
                    }
                });

        forEach((NodeList)xPath.compile(COMPOSITE_XPATH_EXPR).evaluate(document, XPathConstants.NODESET),
                new NodeFunction()
                {
                    public void execute(final Node node) throws XPathExpressionException
                    {
                        addTypeWithNameCheck(typeByNameMap, new CompositeType(node), node);
                    }
                });

        forEach((NodeList)xPath.compile(ENUM_XPATH_EXPR).evaluate(document, XPathConstants.NODESET),
                new NodeFunction()
                {
                    public void execute(final Node node) throws XPathExpressionException
                    {
                        addTypeWithNameCheck(typeByNameMap, new EnumType(node), node);
                    }
                });

        forEach((NodeList)xPath.compile(SET_XPATH_EXPR).evaluate(document, XPathConstants.NODESET),
                new NodeFunction()
                {
                    public void execute(final Node node) throws XPathExpressionException
                    {
                        addTypeWithNameCheck(typeByNameMap, new SetType(node), node);
                    }
                });

        return typeByNameMap;
    }

    /**
     * Helper function to add a Type to a typeByNameMap based on name. Checks to make sure name does not exist.
     *
     * @param typeByNameMap of names to Type objects
     * @param type          to be added to typeByNameMap
     * @param node          for the type
     */
    private static void addTypeWithNameCheck(final Map<String, Type> typeByNameMap, final Type type, final Node node)
    {
        if (typeByNameMap.get(type.getName()) != null)
        {
            handleWarning(node, "type already exists for name: " + type.getName());
        }

        typeByNameMap.put(type.getName(), type);
    }

    /**
     * Scan XML for all message definitions and save in map
     *
     * @param document      for the XML parsing
     * @param xPath         for XPath expression reuse
     * @param typeByNameMap to use for Type objects
     * @return {@link java.util.Map} of schemaId to Message
     */
    public static Map<Long, Message> findMessages(final Document document,
                                                  final XPath xPath,
                                                  final Map<String, Type> typeByNameMap)
        throws Exception
    {
        final Map<Long, Message> messageByIdMap = new HashMap<>();

        forEach((NodeList)xPath.compile(MESSAGE_XPATH_EXPR).evaluate(document, XPathConstants.NODESET),
                new NodeFunction()
                {
                    public void execute(final Node node) throws XPathExpressionException
                    {
                        addMessageWithIdCheck(messageByIdMap, new Message(node, typeByNameMap), node);
                    }
                });

        return messageByIdMap;
    }

    /**
     * Helper function to add a Message to a messageByIdMap based on schemaId. Checks to make sure schemaId does not exist.
     *
     * @param messageByIdMap of schemaId to Message objects
     * @param message        to be added to messageByIdMap
     * @param node           for the message
     */
    private static void addMessageWithIdCheck(final Map<Long, Message> messageByIdMap, final Message message, final Node node)
    {
        if (messageByIdMap.get(Long.valueOf(message.getId())) != null)
        {
            handleError(node, "message template id already exists: " + message.getId());
        }

        messageByIdMap.put(Long.valueOf(message.getId()), message);
    }

    private static String formatLocationInfo(final Node node)
    {
        Node parentNode = node.getParentNode();

        return "at " +
            "<" + parentNode.getNodeName() +
            (getAttributeValueOrNull(parentNode, "name") == null ? ">" : (" name=\"" + getAttributeValueOrNull(parentNode, "name") + "\"> ")) +
            "<" + node.getNodeName() +
            (getAttributeValueOrNull(node, "name") == null ? ">" : (" name=\"" + getAttributeValueOrNull(node, "name") + "\"> "));
    }

    /** Handle an error condition as consequence of parsing. */
    public static void handleError(final Node node, final String msg)
    {
        ErrorHandler handler = (ErrorHandler)node.getOwnerDocument().getUserData(XML_ERROR_HANDLER_KEY);

        if (handler == null)
        {
            throw new IllegalArgumentException("ERROR: " + formatLocationInfo(node) + msg);
        }
        else
        {
            handler.error(formatLocationInfo(node) + msg);
        }
    }

    /** Handle a warning condition as a consequence of parsing. */
    public static void handleWarning(final Node node, final String msg)
    {
        ErrorHandler handler = (ErrorHandler)node.getOwnerDocument().getUserData(XML_ERROR_HANDLER_KEY);

        if (handler == null)
        {
            throw new IllegalArgumentException("WARNING: " + formatLocationInfo(node) + msg);
        }
        else
        {
            handler.warning(formatLocationInfo(node) + msg);
        }
    }

    /**
     * Helper function that throws an exception when the attribute is not set.
     *
     * @param elementNode that should have the attribute
     * @param attrName    that is to be looked up
     * @return value of the attribute
     * @throws IllegalArgumentException if the attribute is not present
     */
    public static String getAttributeValue(final Node elementNode, final String attrName)
    {
        Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

        if (attrNode == null || "".equals(attrNode.getNodeValue()))
        {
            throw new IllegalArgumentException("Element attribute is not present or is empty: " + attrName);
        }

        return attrNode.getNodeValue();
    }

    /**
     * Helper function that uses a default value when value not set.
     *
     * @param elementNode that should have the attribute
     * @param attrName    that is to be looked up
     * @param defValue    String to return if not set
     * @return value of the attribute or defValue
     */
    public static String getAttributeValue(final Node elementNode, final String attrName, final String defValue)
    {
        Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

        if (attrNode == null)
        {
            return defValue;
        }

        return attrNode.getNodeValue();
    }

    /**
     * Helper function that hides the null return from {@link org.w3c.dom.NamedNodeMap#getNamedItem(String)}
     *
     * @param elementNode that could be null
     * @param attrName    that is to be looked up
     * @return null or value of the attribute
     */
    public static String getAttributeValueOrNull(final Node elementNode, final String attrName)
    {
        Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

        if (attrNode == null)
        {
            return null;
        }

        return attrNode.getNodeValue();
    }

    /**
     * Helper function to convert a schema byteOrder into a {@link ByteOrder}
     *
     * @param byteOrder specified as a FIX SBE string
     * @return ByteOrder representation
     */
    public static ByteOrder lookupByteOrder(final String byteOrder)
    {
        switch (byteOrder)
        {
            case "littleEndian":
                return ByteOrder.LITTLE_ENDIAN;

            case "bigEndian":
                return ByteOrder.BIG_ENDIAN;

            default:
                return ByteOrder.LITTLE_ENDIAN;
        }
    }

    private interface NodeFunction
    {
        void execute(final Node node) throws XPathExpressionException;
    }

    private static void forEach(final NodeList nodeList, final NodeFunction func)
        throws Exception
    {
        for (int i = 0, size = nodeList.getLength(); i < size; i++)
        {
            func.execute(nodeList.item(i));
        }
    }
}
