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

import uk.co.real_logic.sbe.ir.Token;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/**
 * An SBE message containing a list of {@link Field} objects and SBE message attributes.
 *
 * What is difference between {@link Message} and the Intermediate Representation (IR)?
 * <ul>
 * <li>IR is intentionally platform, schema, and language independent.</li>
 * <li>IR is abstract layout and encoding only.</li>
 * <li>IR is a flat representation without cycles or hierarchy.</li>
 * <li>Message is FIX/SBE XML Schema specific.</li>
 * </ul>
 */
public class Message
{
    private static final String FIELD_OR_GROUP_OR_DATA_EXPR = "field|group|data";

    private final int id;
    private final String name;
    private final String description;
    private final int blockLength;
    private final List<Field> fieldList;
    private final String semanticType;
    private final String headerType;
    private final int computedBlockLength;
    private final Map<String, Type> typeByNameMap;

    /**
     * Construct a new message from XML Schema.
     *
     * @param messageNode   from the XML Schema Parsing
     * @param typeByNameMap holding type information for message
     * @throws XPathExpressionException on invalid XPath
     */
    public Message(final Node messageNode, final Map<String, Type> typeByNameMap)
        throws XPathExpressionException
    {
        id = Integer.parseInt(getAttributeValue(messageNode, "id"));                        // required
        name = getAttributeValue(messageNode, "name");                                      // required
        description = getAttributeValueOrNull(messageNode, "description");                  // optional
        blockLength = Integer.parseInt(getAttributeValue(messageNode, "blockLength", "0")); // 0 means not set
        semanticType = getAttributeValueOrNull(messageNode, "semanticType");                // optional
        headerType = getAttributeValue(messageNode, "headerType", "messageHeader");         // has default
        this.typeByNameMap = typeByNameMap;

        fieldList = parseFieldsAndGroups(messageNode);
        computeAndValidateOffsets(messageNode, fieldList, blockLength);

        computedBlockLength = computeMessageRootBlockLength();
        validateBlockLength(messageNode, blockLength, computedBlockLength);
    }

    /**
     * Return the template schemaId of the message
     *
     * @return schemaId of the message
     */
    public int id()
    {
        return id;
    }

    /**
     * Return the name of the message
     *
     * @return name of the message
     */
    public String name()
    {
        return name;
    }

    /**
     * The description of the message (if set) or null
     *
     * @return description set by the message or null
     */
    public String description()
    {
        return description;
    }

    /**
     * The semanticType of the message (if set) or null
     *
     * @return the semanticType of the message (if set) or null
     */
    public String semanticType()
    {
        return semanticType;
    }

    /**
     * Return the list of fields in the message
     *
     * @return {@link java.util.List} of the Field objects in this Message
     */
    public List<Field> fields()
    {
        return fieldList;
    }

    /**
     * Return the size of the {@link Message} in bytes including any padding.
     *
     * @return the size of the {@link Message} in bytes including any padding.
     */
    public int blockLength()
    {
        return blockLength > computedBlockLength ? blockLength : computedBlockLength;
    }

    /**
     * Return the {@link String} representing the {@link Type} for the headerStructure of this message
     *
     * @return the {@link String} representing the {@link Type} for the headerStructure of this message
     */
    public String headerType()
    {
        return headerType;
    }

    private List<Field> parseFieldsAndGroups(final Node node)
        throws XPathExpressionException
    {
        final XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList list = (NodeList)xPath.compile(FIELD_OR_GROUP_OR_DATA_EXPR).evaluate(node, XPathConstants.NODESET);
        boolean groupEncountered = false, dataEncountered = false;

        final List<Field> fieldList = new ArrayList<>();

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field field;

            final String nodeName = list.item(i).getNodeName();
            switch (nodeName)
            {
                case "group":
                    if (dataEncountered)
                    {
                        handleError(node, "group node specified after data node");
                    }

                    field = parseGroupField(list, i);
                    groupEncountered = true;
                    break;

                case "data":
                    field = parseDataField(list, i);
                    dataEncountered = true;
                    break;

                case "field":
                    if (groupEncountered || dataEncountered)
                    {
                        handleError(node, "field node specified after group or data node specified");
                    }

                    field = parseField(list, i);
                    break;

                default:
                    throw new IllegalStateException("Unknown node name: " + nodeName);
            }

            fieldList.add(field);
        }

        return fieldList;
    }

    private Field parseGroupField(final NodeList nodeList, final int nodeIndex) throws XPathExpressionException
    {
        final String dimensionTypeName = getAttributeValue(nodeList.item(nodeIndex), "dimensionType", "groupSizeEncoding");
        Type dimensionType = typeByNameMap.get(dimensionTypeName);
        if (dimensionType == null)
        {
            handleError(nodeList.item(nodeIndex), "could not find dimensionType: " + dimensionTypeName);
        }
        else if (!(dimensionType instanceof CompositeType))
        {
            handleError(nodeList.item(nodeIndex), "dimensionType should be a composite type: " + dimensionTypeName);
            dimensionType = null;
        }
        else
        {
            ((CompositeType)dimensionType).checkForWellFormedGroupSizeEncoding(nodeList.item(nodeIndex));
        }

        final Field field = new Field.Builder()
            .name(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .blockLength(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "blockLength", "0")))
            .sinceVersion(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "sinceVersion", "0")))
            .dimensionType((CompositeType)dimensionType)
            .build();

        XmlSchemaParser.checkForValidName(nodeList.item(nodeIndex), field.name());

        field.groupFields(parseFieldsAndGroups(nodeList.item(nodeIndex))); // recursive call

        return field;
    }

    private Field parseField(final NodeList nodeList, final int nodeIndex)
    {
        final String typeName = getAttributeValue(nodeList.item(nodeIndex), "type");
        final Type fieldType = typeByNameMap.get(typeName);
        if (fieldType == null)
        {
            handleError(nodeList.item(nodeIndex), "could not find type: " + typeName);
        }

        final Field field = new Field.Builder()
            .name(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .offset(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "offset", "0")))
            .semanticType(getAttributeValueOrNull(nodeList.item(nodeIndex), "semanticType"))
            .presence(Presence.get(getAttributeValue(nodeList.item(nodeIndex), "presence", "required")))
            .sinceVersion(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "sinceVersion", "0")))
            .epoch(getAttributeValue(nodeList.item(nodeIndex), "epoch", "unix"))
            .timeUnit(getAttributeValue(nodeList.item(nodeIndex), "timeUnit", "nanosecond"))
            .type(fieldType)
            .build();

        field.validate(nodeList.item(nodeIndex));

        return field;
    }

    private Field parseDataField(final NodeList nodeList, final int nodeIndex)
    {
        final String typeName = getAttributeValue(nodeList.item(nodeIndex), "type");
        final Type fieldType = typeByNameMap.get(typeName);
        if (fieldType == null)
        {
            handleError(nodeList.item(nodeIndex), "could not find type: " + typeName);
        }
        else if (!(fieldType instanceof CompositeType))
        {
            handleError(nodeList.item(nodeIndex), "data type is not composite type: " + typeName);
        }
        else
        {
            ((CompositeType)fieldType).checkForWellFormedVariableLengthDataEncoding(nodeList.item(nodeIndex));
            ((CompositeType)fieldType).makeDataFieldCompositeType();
        }

        final Field field = new Field.Builder()
            .name(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .offset(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "offset", "0")))
            .semanticType(getAttributeValueOrNull(nodeList.item(nodeIndex), "semanticType"))
            .presence(Presence.get(getAttributeValue(nodeList.item(nodeIndex), "presence", "required")))
            .sinceVersion(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "sinceVersion", "0")))
            .epoch(getAttributeValue(nodeList.item(nodeIndex), "epoch", "unix"))
            .timeUnit(getAttributeValue(nodeList.item(nodeIndex), "timeUnit", "nanosecond"))
            .type(fieldType)
            .variableLength(true)
            .build();

        field.validate(nodeList.item(nodeIndex));

        return field;
    }

    /*
     * Compute and validate the offsets of the fields in the list and will set the fields computedOffset.
     * Will validate the blockLength of the fields encompassing &lt;message&gt; or &lt;group&gt; and recursively descend
     * into repeated groups.
     */
    private int computeAndValidateOffsets(final Node node, final List<Field> fields, final int blockLength)
    {
        boolean variableSizedBlock = false;
        int offset = 0;

        for (final Field field : fields)
        {
            if (0 != field.offset() && field.offset() < offset)
            {
                handleError(node, "Offset provides insufficient space at field: " + field.name());
            }

            if (Token.VARIABLE_SIZE != offset)
            {
                if (0 != field.offset())
                {
                    offset = field.offset();
                }
                else if (null != field.dimensionType() && 0 != blockLength)
                {
                    offset = blockLength;
                }
                else if (field.isVariableLength() && 0 != blockLength)
                {
                    offset = blockLength;
                }
            }

            field.computedOffset(variableSizedBlock ? Token.VARIABLE_SIZE : offset);

            if (null != field.groupFields())
            {
                final int groupBlockLength = computeAndValidateOffsets(node, field.groupFields(), 0);

                validateBlockLength(node, field.blockLength(), groupBlockLength);
                field.computedBlockLength(Math.max(field.blockLength(), groupBlockLength));

                variableSizedBlock = true;
            }
            else if (null != field.type())
            {
                int size = field.type().size();

                if (Token.VARIABLE_SIZE == size)
                {
                    variableSizedBlock = true;
                }

                if (!variableSizedBlock)
                {
                    offset += size;
                }
            }
        }

        return offset;
    }

    private int computeMessageRootBlockLength()
    {
        int blockLength = 0;

        for (final Field field : fieldList)
        {
            if (field.groupFields() != null)
            {
                return blockLength;
            }
            else if (field.type() != null)
            {
                final int fieldSize = field.type().size();

                if (Token.VARIABLE_SIZE == fieldSize)
                {
                    return blockLength;
                }

                blockLength = field.computedOffset() + fieldSize;
            }
        }

        return blockLength;
    }

    private void validateBlockLength(final Node node, final long specifiedBlockLength, final long computedBlockLength)
    {
        if (0 != specifiedBlockLength && computedBlockLength > specifiedBlockLength)
        {
            handleError(
                node,
                "specified blockLength provides insufficient space " + computedBlockLength + " > " + specifiedBlockLength);
        }
    }
}
