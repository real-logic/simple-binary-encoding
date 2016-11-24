/*
 * Copyright 2014 - 2016 Real Logic Ltd.
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.ir.Token;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.xpath.XPathConstants.NODESET;
import static uk.co.real_logic.sbe.PrimitiveType.*;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/**
 * SBE compositeType.
 */
public class CompositeType extends Type
{
    public static final String COMPOSITE_TYPE = "composite";
    public static final String SUB_TYPES_EXP = "type|enum|set|composite|ref";

    private final List<String> compositesPath = new ArrayList<>();
    private final Map<String, Type> containedTypeByNameMap = new LinkedHashMap<>();

    public CompositeType(final Node node) throws XPathExpressionException
    {
        this(node, null, new ArrayList<>());
    }

    /**
     * Construct a new compositeType from XML Schema.
     *
     * @param node           from the XML Schema Parsing
     * @param givenName      for this node.
     * @param compositesPath with the path of composites that represents the levels of composition.
     * @throws XPathExpressionException if the XPath is invalid.
     */
    public CompositeType(final Node node, final String givenName, final List<String> compositesPath)
        throws XPathExpressionException
    {
        super(node, givenName);

        this.compositesPath.addAll(compositesPath);
        this.compositesPath.add(getAttributeValue(node, "name"));

        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList list = (NodeList)xPath.compile(SUB_TYPES_EXP).evaluate(node, NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            final Node subTypeNode = list.item(i);
            final String subTypeName = XmlSchemaParser.getAttributeValue(subTypeNode, "name");

            processType(subTypeNode, subTypeName, null);
        }

        checkForValidOffsets(node);
    }

    /**
     * Return the EncodedDataType within this composite with the given name
     *
     * @param name of the EncodedDataType to return
     * @return type requested
     */
    public Type getType(final String name)
    {
        return containedTypeByNameMap.get(name);
    }

    /**
     * The encodedLength (in octets) of the list of EncodedDataTypes
     *
     * @return encodedLength of the compositeType
     */
    public int encodedLength()
    {
        int offset = 0;

        for (final Type t : containedTypeByNameMap.values())
        {
            if (t.isVariableLength())
            {
                return Token.VARIABLE_LENGTH;
            }

            if (t.offsetAttribute() != -1)
            {
                offset = t.offsetAttribute();
            }

            offset += t.encodedLength();
        }

        return offset;
    }

    /**
     * Return list of the Type that compose this composite
     *
     * @return {@link List} that holds the types in this composite
     */
    public List<Type> getTypeList()
    {

        return new ArrayList<>(containedTypeByNameMap.values());
    }

    /**
     * Make this composite type, if it has a varData member, variable length
     * by making the EncodedDataType with the name "varData" be variable length.
     */
    public void makeDataFieldCompositeType()
    {
        final EncodedDataType edt = (EncodedDataType)containedTypeByNameMap.get("varData");
        if (edt != null)
        {
            edt.variableLength(true);
        }
    }

    /**
     * Check the composite for being a well formed group encodedLength encoding. This means
     * that there are the fields "blockLength" and "numInGroup" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedGroupSizeEncoding(final Node node)
    {
        final EncodedDataType blockLengthType = (EncodedDataType)containedTypeByNameMap.get("blockLength");
        final EncodedDataType numInGroupType = (EncodedDataType)containedTypeByNameMap.get("numInGroup");

        if (blockLengthType == null)
        {
            XmlSchemaParser.handleError(node, "composite for group encodedLength encoding must have \"blockLength\"");
        }
        else if (!isUnsigned(blockLengthType.primitiveType()))
        {
            XmlSchemaParser.handleError(node, "\"blockLength\" must be unsigned type");
        }

        if (numInGroupType == null)
        {
            XmlSchemaParser.handleError(node, "composite for group encodedLength encoding must have \"numInGroup\"");
        }
        else if (!isUnsigned(numInGroupType.primitiveType()))
        {
            XmlSchemaParser.handleError(node, "\"numInGroup\" must be unsigned type");
        }
        else if (numInGroupType.primitiveType() != UINT8 && numInGroupType.primitiveType() != UINT16)
        {
            XmlSchemaParser.handleWarning(node, "\"numInGroup\" should be UINT8 or UINT16");
        }
        else
        {
            final PrimitiveValue maxValue = numInGroupType.maxValue();
            validateMaxValue(node, numInGroupType.primitiveType(), maxValue);

            final PrimitiveValue minValue = numInGroupType.minValue();
            if (null != minValue)
            {
                final long max = maxValue != null ?
                    maxValue.longValue() : numInGroupType.primitiveType().maxValue().longValue();

                if (minValue.longValue() > max)
                {
                    XmlSchemaParser.handleError(node, String.format(
                        "\"numInGroup\" minValue=%s greater than maxValue=%d", minValue, max));
                }
            }
        }
    }

    /**
     * Check the composite for being a well formed variable length data encoding. This means
     * that there are the fields "length" and "varData" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedVariableLengthDataEncoding(final Node node)
    {
        final EncodedDataType lengthType = (EncodedDataType)containedTypeByNameMap.get("length");

        if (lengthType == null)
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding must have \"length\"");
        }
        else
        {
            final PrimitiveType primitiveType = lengthType.primitiveType();
            if (!isUnsigned(primitiveType))
            {
                XmlSchemaParser.handleError(node, "\"length\" must be unsigned type");
            }
            else if (primitiveType != UINT8 && primitiveType != UINT16 && primitiveType != UINT32)
            {
                XmlSchemaParser.handleWarning(node, "\"length\" should be UINT8, UINT16, or UINT32");
            }

            validateMaxValue(node, primitiveType, lengthType.maxValue());
        }

        if ("optional".equals(getAttributeValueOrNull(node, "presence")))
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding cannot have presence=\"optional\"");
        }

        if (containedTypeByNameMap.get("varData") == null)
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding must have \"varData\"");
        }
    }

    private static void validateMaxValue(final Node node, final PrimitiveType primitiveType, final PrimitiveValue value)
    {
        if (null != value)
        {
            final long longValue = value.longValue();
            final long allowedValue = primitiveType.maxValue().longValue();
            if (longValue > allowedValue)
            {
                XmlSchemaParser.handleError(node, String.format(
                    "maxValue greater than allowed for type: maxValue=%d allowed=%d", longValue, allowedValue));
            }

            final long maxInt = INT32.maxValue().longValue();
            if (primitiveType == UINT32 && longValue > maxInt)
            {
                XmlSchemaParser.handleError(node, String.format(
                    "maxValue greater than allowed for type: maxValue=%d allowed=%d", longValue, maxInt));
            }
        }
        else if (primitiveType == UINT32)
        {
            final long maxInt = INT32.maxValue().longValue();
            XmlSchemaParser.handleError(node, String.format(
                "maxValue must be set for varData UINT32 type: max value allowed=%d", maxInt));
        }
    }

    /**
     * Check the composite for being a well formed message headerStructure encoding. This means
     * that there are the fields "blockLength", "templateId" and "version" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedMessageHeader(final Node node)
    {
        final EncodedDataType blockLengthType = (EncodedDataType)containedTypeByNameMap.get("blockLength");
        final EncodedDataType templateIdType = (EncodedDataType)containedTypeByNameMap.get("templateId");
        final EncodedDataType schemaIdType = (EncodedDataType)containedTypeByNameMap.get("schemaId");
        final EncodedDataType versionType = (EncodedDataType)containedTypeByNameMap.get("version");

        if (blockLengthType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"blockLength\"");
        }
        else if (!isUnsigned(blockLengthType.primitiveType()))
        {
            XmlSchemaParser.handleError(node, "\"blockLength\" must be unsigned");
        }
        else if (blockLengthType.primitiveType() != UINT16)
        {
            XmlSchemaParser.handleWarning(node, "\"blockLength\" should be UINT16");
        }

        if (templateIdType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"templateId\"");
        }
        else if (templateIdType.primitiveType() != UINT16)
        {
            XmlSchemaParser.handleError(node, "\"templateId\" must be UINT16");
        }

        if (schemaIdType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"schemaId\"");
        }
        else if (schemaIdType.primitiveType() != UINT16)
        {
            XmlSchemaParser.handleError(node, "\"schemaId\" must be UINT16");
        }

        if (versionType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"version\"");
        }
        else if (versionType.primitiveType() != UINT16)
        {
            XmlSchemaParser.handleError(node, "\"version\" must be UINT16");
        }
    }

    /**
     * Check the composite for any specified offsets and validate they are correctly specified.
     *
     * @param node of the XML for this composite
     */
    public void checkForValidOffsets(final Node node)
    {
        int offset = 0;

        for (final Type edt : containedTypeByNameMap.values())
        {
            final int offsetAttribute = edt.offsetAttribute();

            if (-1 != offsetAttribute)
            {
                if (offsetAttribute < offset)
                {
                    XmlSchemaParser.handleError(
                        node,
                        String.format("composite element \"%s\" has incorrect offset specified", edt.name()));
                }

                offset = offsetAttribute;
            }

            offset += edt.encodedLength();
        }
    }

    public boolean isVariableLength()
    {
        return false;
    }

    private Type processType(final Node subTypeNode, final String subTypeName, final String givenName)
        throws XPathExpressionException
    {
        final String nodeName = subTypeNode.getNodeName();
        Type type = null;

        switch (nodeName)
        {
            case "type":
                type = addType(subTypeNode, subTypeName, new EncodedDataType(subTypeNode, givenName));
                break;

            case "enum":
                type = addType(subTypeNode, subTypeName, new EnumType(subTypeNode, givenName));
                break;

            case "set":
                type = addType(subTypeNode, subTypeName, new SetType(subTypeNode, givenName));
                break;

            case "composite":
                type = addType(subTypeNode, subTypeName, new CompositeType(subTypeNode, givenName, compositesPath));
                break;

            case "ref":
            {
                final XPath xPath = XPathFactory.newInstance().newXPath();

                final String refName = XmlSchemaParser.getAttributeValue(subTypeNode, "name");
                final String refType = XmlSchemaParser.getAttributeValue(subTypeNode, "type");
                final int refOffset = Integer.parseInt(XmlSchemaParser.getAttributeValue(subTypeNode, "offset", "-1"));
                final Node refTypeNode = (Node)xPath.compile("/messageSchema/types/*[@name='" + refType + "']")
                    .evaluate(subTypeNode.getOwnerDocument(), XPathConstants.NODE);

                if (refTypeNode == null)
                {
                    XmlSchemaParser.handleError(subTypeNode, "ref type not found: " + refType);
                }
                else
                {
                    if (compositesPath.contains(refType))
                    {
                        XmlSchemaParser.handleError(refTypeNode, "ref types cannot create circular dependencies.");
                        throw new IllegalStateException("ref types cannot create circular dependencies");
                    }

                    type = processType(refTypeNode, refName, refName);

                    if (-1 != refOffset)
                    {
                        type.offsetAttribute(refOffset);
                    }
                }

                break;
            }

            default:
                throw new IllegalStateException("Unknown node type: name=" + nodeName);
        }

        return type;
    }

    private Type addType(final Node subTypeNode, final String name, final Type type)
    {
        if (containedTypeByNameMap.put(name, type) != null)
        {
            XmlSchemaParser.handleError(subTypeNode, "composite already contains a type named: " + name);
        }

        return type;
    }
}
