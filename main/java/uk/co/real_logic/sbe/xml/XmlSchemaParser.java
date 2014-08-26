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
import uk.co.real_logic.sbe.util.ValidationUtil;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static uk.co.real_logic.sbe.PrimitiveType.*;
import static uk.co.real_logic.sbe.xml.Presence.REQUIRED;

/**
 * Encapsulate the XML Schema parsing for SBE so that other representations may be
 * used to generate independent representation.
 */
public class XmlSchemaParser
{
    /** Key for storing {@link ErrorHandler} as user data in XML document */
    public static final String ERROR_HANDLER_KEY = "SbeErrorHandler";

    public static final String TYPE_XPATH_EXPR = "/messageSchema/types/type";
    public static final String COMPOSITE_XPATH_EXPR = "/messageSchema/types/composite";
    public static final String ENUM_XPATH_EXPR = "/messageSchema/types/enum";
    public static final String SET_XPATH_EXPR = "/messageSchema/types/set";
    public static final String MESSAGE_SCHEMA_XPATH_EXPR = "/messageSchema";

    public static final String MESSAGE_XPATH_EXPR = "/messageSchema/message";

    /**
     * Validate the document against a given schema. Error will be written to {@link java.lang.System#err}
     *
     * @param xsdFilename schema to validate against.
     * @param in document to be validated.
     * @throws Exception if an error occurs when parsing the document or schema.
     */
    public static void validate(final String xsdFilename, final BufferedInputStream in)
        throws Exception
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        factory.setSchema(schemaFactory.newSchema(new File(xsdFilename)));
        factory.setNamespaceAware(true);

        factory.newDocumentBuilder().parse(in);
    }

    /**
     * Take an {@link InputStream} and parse it generating map of template ID to Message objects, types, and schema
     * Input could be from {@link java.io.FileInputStream}, {@link java.io.ByteArrayInputStream}, etc.
     * Exceptions are passed back up for any problems.
     *
     * @param in stream from which schema is read.
     * @param options to be applied during parsing.
     * @return {@link MessageSchema} encoding for the schema.
     * @throws Exception on parsing error.
     */
    public static MessageSchema parse(final InputStream in, final ParserOptions options)
        throws Exception
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        final Document document = factory.newDocumentBuilder().parse(in);
        final XPath xPath = XPathFactory.newInstance().newXPath();

        final ErrorHandler errorHandler = new ErrorHandler(options);
        document.setUserData(ERROR_HANDLER_KEY, errorHandler, null);

        final Map<String, Type> typeByNameMap = findTypes(document, xPath);

        errorHandler.checkIfShouldExit();

        final Map<Long, Message> messageByIdMap = findMessages(document, xPath, typeByNameMap);

        errorHandler.checkIfShouldExit();

        final Node schemaNode = (Node)xPath.compile(MESSAGE_SCHEMA_XPATH_EXPR).evaluate(document, XPathConstants.NODE);
        final MessageSchema messageSchema = new MessageSchema(schemaNode, typeByNameMap, messageByIdMap);

        errorHandler.checkIfShouldExit();

        return messageSchema;
    }

    /**
     * Scan XML for all types (encodedDataType, compositeType, enumType, and setType) and save in map
     *
     * @param document for the XML parsing
     * @param xPath    for XPath expression reuse
     * @return {@link java.util.Map} of name {@link java.lang.String} to Type
     * @throws Exception on parsing error.
     */
    public static Map<String, Type> findTypes(final Document document, final XPath xPath) throws Exception
    {
        final Map<String, Type> typeByNameMap = new HashMap<>();

        // Add primitiveTypes to typeByNameMap - these could be in a static XInclude that is always brought in...
        typeByNameMap.put("char", new EncodedDataType("char", REQUIRED, null, null, CHAR, 1, false));
        typeByNameMap.put("int8", new EncodedDataType("int8", REQUIRED, null, null, INT8, 1, false));
        typeByNameMap.put("int16", new EncodedDataType("int16", REQUIRED, null, null, INT16, 1, false));
        typeByNameMap.put("int32", new EncodedDataType("int32", REQUIRED, null, null, INT32, 1, false));
        typeByNameMap.put("int64", new EncodedDataType("int64", REQUIRED, null, null, INT64, 1, false));
        typeByNameMap.put("uint8", new EncodedDataType("uint8", REQUIRED, null, null, UINT8, 1, false));
        typeByNameMap.put("uint16", new EncodedDataType("uint16", REQUIRED, null, null, UINT16, 1, false));
        typeByNameMap.put("uint32", new EncodedDataType("uint32", REQUIRED, null, null, UINT32, 1, false));
        typeByNameMap.put("uint64", new EncodedDataType("uint64", REQUIRED, null, null, UINT64, 1, false));
        typeByNameMap.put("float", new EncodedDataType("float", REQUIRED, null, null, FLOAT, 1, false));
        typeByNameMap.put("double", new EncodedDataType("double", REQUIRED, null, null, DOUBLE, 1, false));

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
     * Scan XML for all message definitions and save in map
     *
     * @param document      for the XML parsing
     * @param xPath         for XPath expression reuse
     * @param typeByNameMap to use for Type objects
     * @return {@link java.util.Map} of schemaId to Message
     * @throws Exception on parsing error.
     */
    public static Map<Long, Message> findMessages(
        final Document document, final XPath xPath, final Map<String, Type> typeByNameMap)
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
     * Handle an error condition as consequence of parsing.
     *
     * @param node that is the context of the warning.
     * @param msg associated with the error.
     */
    public static void handleError(final Node node, final String msg)
    {
        final ErrorHandler handler = (ErrorHandler)node.getOwnerDocument().getUserData(ERROR_HANDLER_KEY);

        if (handler == null)
        {
            throw new IllegalStateException("ERROR: " + formatLocationInfo(node) + msg);
        }
        else
        {
            handler.error(formatLocationInfo(node) + msg);
        }
    }

    /**
     * Handle a warning condition as a consequence of parsing.
     *
     * @param node as the context for the warning.
     * @param msg associated with the warning.
     */
    public static void handleWarning(final Node node, final String msg)
    {
        final ErrorHandler handler = (ErrorHandler)node.getOwnerDocument().getUserData(ERROR_HANDLER_KEY);

        if (handler == null)
        {
            throw new IllegalStateException("WARNING: " + formatLocationInfo(node) + msg);
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
        final Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

        if (attrNode == null || "".equals(attrNode.getNodeValue()))
        {
            throw new IllegalStateException(
                "Element '" + elementNode.getNodeName() + "' has empty or missing attribute: " + attrName);
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
        final Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

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
        final Node attrNode = elementNode.getAttributes().getNamedItem(attrName);

        if (attrNode == null)
        {
            return null;
        }

        return attrNode.getNodeValue();
    }

    /**
     * Helper function to convert a schema byteOrderName into a {@link ByteOrder}
     *
     * @param byteOrderName specified as a FIX SBE string
     * @return ByteOrder representation
     */
    public static ByteOrder getByteOrder(final String byteOrderName)
    {
        switch (byteOrderName)
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
     * Check name against validity for C++ and Java naming. Warning if not valid.
     *
     * @param node to have the name checked.
     * @param name of the node to be checked.
     */
    public static void checkForValidName(final Node node, final String name)
    {
        if (!ValidationUtil.isSbeCppName(name))
        {
            handleError(node, "name is not valid for C++: " + name);
        }

        if (!ValidationUtil.isSbeJavaName(name))
        {
            handleError(node, "name is not valid for Java: " + name);
        }
    }

    private static void addTypeWithNameCheck(final Map<String, Type> typeByNameMap, final Type type, final Node node)
    {
        if (typeByNameMap.get(type.name()) != null)
        {
            handleWarning(node, "type already exists for name: " + type.name());
        }

        checkForValidName(node, type.name());

        typeByNameMap.put(type.name(), type);
    }

    private static void addMessageWithIdCheck(
        final Map<Long, Message> messageByIdMap, final Message message, final Node node)
    {
        if (messageByIdMap.get(Long.valueOf(message.id())) != null)
        {
            handleError(node, "message template id already exists: " + message.id());
        }

        checkForValidName(node, message.name());

        messageByIdMap.put(Long.valueOf(message.id()), message);
    }

    private static String formatLocationInfo(final Node node)
    {
        final Node parentNode = node.getParentNode();

        return "at " +
            "<" + parentNode.getNodeName() +
            (getAttributeValueOrNull(parentNode, "name") == null ?
                ">" : (" name=\"" + getAttributeValueOrNull(parentNode, "name") + "\"> ")) +
            "<" + node.getNodeName() +
            (getAttributeValueOrNull(node, "name") == null
                ? ">" : (" name=\"" + getAttributeValueOrNull(node, "name") + "\"> "));
    }

    interface NodeFunction
    {
        void execute(Node node) throws XPathExpressionException;
    }

    private static void forEach(final NodeList nodeList, final NodeFunction func) throws Exception
    {
        for (int i = 0, size = nodeList.getLength(); i < size; i++)
        {
            func.execute(nodeList.item(i));
        }
    }
}
