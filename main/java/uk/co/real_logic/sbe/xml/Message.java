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
 * <p/>
 * What is difference between {@link Message} and the Intermediate Representation (IR)?
 * <ul>
 * <li>IR is intentionally platform, schema, and language independent.</li>
 * <li>IR is abstract layout and options only.</li>
 * <li>IR is a flat representation without cycles or hierarchy.</li>
 * <li>Message is FIX/SBE XML Schema specific.</li>
 * </ul>
 */
public class Message
{
    private static final String FIELD_OR_GROUP_OR_DATA_EXPR = "field|group|data";

    private final long id;
    private final String name;
    private final String description;
    private final int blockLength;
    private final List<Field> fieldList;
    private final String semanticType;
    private final String headerType;
    private final int calculatedBlockLength;
    private final Map<String, Type> typeByNameMap;

    /**
     * Construct a new message from XML Schema.
     *
     * @param messageNode   from the XML Schema Parsing
     * @param typeByNameMap holding type information for message
     */
    public Message(final Node messageNode, final Map<String, Type> typeByNameMap)
        throws XPathExpressionException, IllegalArgumentException
    {
        id = Long.parseLong(getAttributeValue(messageNode, "id"));                          // required
        name = getAttributeValue(messageNode, "name");                                      // required
        description = getAttributeValueOrNull(messageNode, "description");                  // optional
        blockLength = Integer.parseInt(getAttributeValue(messageNode, "blockLength", "0")); // 0 means not set
        semanticType = getAttributeValueOrNull(messageNode, "semanticType");                // optional
        headerType = getAttributeValue(messageNode, "headerType", "messageHeader");         // has default
        this.typeByNameMap = typeByNameMap;

        fieldList = parseFieldsAndGroups(messageNode);

        calculateAndValidateOffsets(messageNode, fieldList, blockLength);

        calculatedBlockLength = calculateMessageBlockLength();

        validateBlockLength(messageNode, blockLength, calculatedBlockLength);
    }

    private List<Field> parseFieldsAndGroups(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(FIELD_OR_GROUP_OR_DATA_EXPR).evaluate(node, XPathConstants.NODESET);
        int numGroupEncountered = 0, numDataEncountered = 0;

        List<Field> fieldList = new ArrayList<>();

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field field;

            final String nodeName = list.item(i).getNodeName();
            switch (nodeName)
            {
                case "group":
                    if (numDataEncountered > 0)
                    {
                        handleError(node, "group specified after data specified");
                    }

                    field = parseGroupField(list, i);

                    numGroupEncountered++;
                    break;

                case "data":
                    field = parseDataField(list, i);

                    numDataEncountered++;
                    break;

                case "field":
                    if (numGroupEncountered > 0 || numDataEncountered > 0)
                    {
                        handleError(node, "field specified after group or data specified");
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

    private Field parseGroupField(final NodeList nodeList,
                                  final int nodeIndex) throws XPathExpressionException
    {
        final String dimensionTypeName = getAttributeValue(nodeList.item(nodeIndex), "dimensionType", "groupSizeEncoding");
        final Type dimensionType = typeByNameMap.get(dimensionTypeName);
        if (dimensionType == null)
        {
            handleError(nodeList.item(nodeIndex), "could not find dimensionType: " + dimensionTypeName);
        }
        else if (!(dimensionType instanceof CompositeType))
        {
            handleError(nodeList.item(nodeIndex), "dimensionType is not composite type: " + dimensionTypeName);
        }
        else
        {
            ((CompositeType)dimensionType).checkForWellFormedGroupSizeEncoding(nodeList.item(nodeIndex));
        }

        final Field field = new Field.Builder(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .blockLength(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "blockLength", "0")))
            .dimensionType((CompositeType)dimensionType)
            .build();

        field.setGroupFields(parseFieldsAndGroups(nodeList.item(nodeIndex))); // recursive call

        return field;
    }

    private Field parseField(final NodeList nodeList,
                             final int nodeIndex)
    {
        final String typeName = getAttributeValue(nodeList.item(nodeIndex), "type");
        final Type fieldType = typeByNameMap.get(typeName);
        if (fieldType == null)
        {
            handleError(nodeList.item(nodeIndex), "could not find type: " + typeName);
        }

        Field field = new Field.Builder(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .offset(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "offset", "0")))
            .semanticType(getAttributeValueOrNull(nodeList.item(nodeIndex), "semanticType"))
            .presence(Presence.lookup(getAttributeValueOrNull(nodeList.item(nodeIndex), "presence")))
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

        final Field field = new Field.Builder(getAttributeValue(nodeList.item(nodeIndex), "name"))
            .description(getAttributeValueOrNull(nodeList.item(nodeIndex), "description"))
            .id(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "id")))
            .offset(Integer.parseInt(getAttributeValue(nodeList.item(nodeIndex), "offset", "0")))
            .semanticType(getAttributeValueOrNull(nodeList.item(nodeIndex), "semanticType"))
            .presence(Presence.lookup(getAttributeValueOrNull(nodeList.item(nodeIndex), "presence")))
            .type(fieldType)
            .variableLength(true)
            .build();

        field.validate(nodeList.item(nodeIndex));

        return field;
    }

    /**
     * Calculate and validate the offsets of the fields in the list. Will set the fields calculatedOffset.
     * Will validate the blockLength of the fields encompassing &lt;message&gt; or &lt;group&gt;. Will recurse
     * into repeated groups.
     *
     * @param fields to iterate over
     * @param blockLength of the surrounding element or 0 for not set
     * @return the total size of the list or {@link Token#VARIABLE_SIZE} if the size will vary
     */
    public int calculateAndValidateOffsets(final Node node, final List<Field> fields, final int blockLength)
    {
        int currOffset = 0;

        for (final Field field : fields)
        {
            if (field.getOffset() > 0 && field.getOffset() < currOffset)
            {
                handleError(node, "Specified offset is too small for field name: " + field.getName());
            }

            if (Token.VARIABLE_SIZE != currOffset)
            {
                /* if offset specified, then use it (since it was checked before) */
                if (field.getOffset() > 0)
                {
                    currOffset = field.getOffset();  // reset current offset to the one requested by the field specification
                }
                else if (field.getDimensionType() != null && blockLength > 0)
                {
                    currOffset = blockLength;        // reset current offset to the blockLength specified
                }
                else if (field.getVariableLength() && blockLength > 0)
                {
                    currOffset = blockLength;        // reset current offset to the blockLength specified
                }
            }

            /* save the fields current offset (even for <group> elements!) */
            field.setCalculatedOffset(currOffset);

            /* if this field is a <group> then recurse into it */
            if (field.getGroupFields() != null)
            {
                /* 0 blockLength as group blockLength is different */
                int calculatedBlockLength = calculateAndValidateOffsets(node, field.getGroupFields(), 0);

                /* validate the <group> blockLength, if set */
                validateBlockLength(node, field.getBlockLength(), calculatedBlockLength);

                if (field.getBlockLength() > calculatedBlockLength)
                {
                    field.setCalculatedBlockLength(field.getBlockLength());
                }
                else
                {
                    field.setCalculatedBlockLength(calculatedBlockLength);
                }

                /*
                 * After a <group> element, the offset and total size will be varying
                 * TODO: these could be DEPENDENT_SIZE such that they depend on the entry count field, etc.
                 */
                currOffset = Token.VARIABLE_SIZE;
            }
            else if (field.getType() != null) // will be <field> or <data>
            {
                int calculatedSize = field.getType().size();

                if (Token.VARIABLE_SIZE == calculatedSize || Token.VARIABLE_SIZE == currOffset)
                {
                    currOffset = Token.VARIABLE_SIZE;
                }
                else
                {
                    currOffset += calculatedSize;
                }
            }
        }

        return currOffset;
    }

    /**
     * Calculate and return the blockLength for a message. Which is the length until the first
     * variable length field or repeating group. This must be run after the offsets are calculated.
     *
     * @return calculated blockLength for the message
     */
    private int calculateMessageBlockLength()
    {
        int currLength = 0;

        for (final Field field : fieldList)
        {
            if (field.getGroupFields() != null)
            {
                return field.getCalculatedOffset();
            }
            else if (field.getType() != null) // will be <field> or <data>
            {
                int calculatedSize = field.getType().size();

                if (Token.VARIABLE_SIZE == calculatedSize)
                {
                    return currLength;
                }

                currLength = field.getCalculatedOffset() + calculatedSize;
            }
        }

        return currLength;
    }

    /**
     * Return the template schemaId of the message
     *
     * @return schemaId of the message
     */
    public long getId()
    {
        return id;
    }

    /**
     * Return the name of the message
     *
     * @return name of the message
     */
    public String getName()
    {
        return name;
    }

    /**
     * The description of the message (if set) or null
     *
     * @return description set by the message or null
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * The semanticType of the message (if set) or null
     */
    public String getSemanticType()
    {
        return semanticType;
    }

    /**
     * Return the list of fields in the message
     *
     * @return {@link java.util.List} of the Field objects in this Message
     */
    public List<Field> getFields()
    {
        return fieldList;
    }

    /**
     * Return the size of the {@link Message} in bytes including any padding.
     *
     * @return the size of the {@link Message} in bytes including any padding.
     */
    public int getBlockLength()
    {
        return (blockLength > calculatedBlockLength ? blockLength : calculatedBlockLength);
    }

    /**
     * Return the {@link String} representing the {@link Type} for the header of this message
     */
    public String getHeaderType()
    {
        return headerType;
    }

    private void validateBlockLength(final Node node, final long specifiedBlockLength, final long calculatedBlockLength)
    {
        if (0 < specifiedBlockLength && calculatedBlockLength > specifiedBlockLength)
        {
            handleError(node, "specified blockLength is too small");
        }
    }
}
