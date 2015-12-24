/*
 * Copyright 2014 - 2015 Real Logic Ltd.
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
import uk.co.real_logic.sbe.ir.Token;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.xpath.XPathConstants.NODESET;
import static uk.co.real_logic.sbe.PrimitiveType.isUnsigned;

/**
 * SBE compositeType.
 */
public class CompositeType extends Type
{
    public static final String COMPOSITE_TYPE = "composite";
    public static final String SUB_TYPES_EXP = "type|enum|set|composite|ref";

    private final Map<String, Type> containedTypeByNameMap = new LinkedHashMap<>();
    private final int sinceVersion;
    private final int offsetAttribute;

    /**
     * Construct a new compositeType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     * @throws XPathExpressionException if the XPath is invalid.
     */
    public CompositeType(final Node node) throws XPathExpressionException
    {
        super(node);

        sinceVersion = Integer.parseInt(XmlSchemaParser.getAttributeValue(node, "sinceVersion", "0"));
        offsetAttribute = Integer.parseInt(XmlSchemaParser.getAttributeValue(node, "offset", "-1"));
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList list = (NodeList)xPath.compile(SUB_TYPES_EXP).evaluate(node, NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            final Node subTypeNode = list.item(i);
            final String nodeName = subTypeNode.getNodeName();

            switch (nodeName)
            {
                case "type":
                    final EncodedDataType encodedDataType = new EncodedDataType(subTypeNode);

                    if (containedTypeByNameMap.put(encodedDataType.name(), encodedDataType) != null)
                    {
                        XmlSchemaParser.handleError(node, "composite already contains type named: " + encodedDataType.name());
                    }
                    break;

                case "enum":
                    final EnumType enumType = new EnumType(subTypeNode);

                    if (containedTypeByNameMap.put(enumType.name(), enumType) != null)
                    {
                        XmlSchemaParser.handleError(node, "composite already contains type named: " + enumType.name());
                    }
                    break;

                case "set":
                    final SetType setType = new SetType(subTypeNode);

                    if (containedTypeByNameMap.put(setType.name(), setType) != null)
                    {
                        XmlSchemaParser.handleError(node, "composite already contains type named: " + setType.name());
                    }
                    break;

                case "composite":
                    final CompositeType compositeType = new CompositeType(subTypeNode);

                    if (containedTypeByNameMap.put(compositeType.name(), compositeType) != null)
                    {
                        XmlSchemaParser.handleError(node, "composite already contains type named: " + compositeType.name());
                    }
                    break;

                case "ref":
                    XmlSchemaParser.handleError(node, "\"ref\" not yet supported");
                    break;

                default:
                    throw new IllegalStateException("Unknown node name: " + nodeName);
            }
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
     * Return the sinceVersion value of the {@link CompositeType}
     *
     * @return the sinceVersion of the {@link CompositeType}
     */
    public int sinceVersion()
    {
        return sinceVersion;
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
        else if (numInGroupType.primitiveType() != PrimitiveType.UINT8 && numInGroupType.primitiveType() != PrimitiveType.UINT16)
        {
            XmlSchemaParser.handleWarning(node, "\"numInGroup\" should be UINT8 or UINT16");
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
        else if (!isUnsigned(lengthType.primitiveType()))
        {
            XmlSchemaParser.handleError(node, "\"length\" must be unsigned type");
        }
        else if (lengthType.primitiveType() != PrimitiveType.UINT8 && lengthType.primitiveType() != PrimitiveType.UINT16)
        {
            XmlSchemaParser.handleWarning(node, "\"length\" should be UINT8 or UINT16");
        }

        if (containedTypeByNameMap.get("varData") == null)
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding must have \"varData\"");
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
        else if (blockLengthType.primitiveType() != PrimitiveType.UINT16)
        {
            XmlSchemaParser.handleWarning(node, "\"blockLength\" should be UINT16");
        }

        if (templateIdType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"templateId\"");
        }
        else if (templateIdType.primitiveType() != PrimitiveType.UINT16)
        {
            XmlSchemaParser.handleError(node, "\"templateId\" must be UINT16");
        }

        if (schemaIdType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"schemaId\"");
        }
        else if (schemaIdType.primitiveType() != PrimitiveType.UINT16)
        {
            XmlSchemaParser.handleError(node, "\"schemaId\" must be UINT16");
        }

        if (versionType == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"version\"");
        }
        else if (versionType.primitiveType() != PrimitiveType.UINT16)
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

    public int offsetAttribute()
    {
        return offsetAttribute;
    }
}
