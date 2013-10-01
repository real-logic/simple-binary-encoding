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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/**
 * An SBE message containing a list of {@link Field} objects and SBE message attributes.
 * <p/>
 * What is difference between {@link Message} and the Intermediate Representation (IR)?
 * <ul>
 * <li>IR is intentionally platform, schema, and language independent.</li>
 * <li>IR is abstract layout and metadata only.</li>
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
    private long irIdCursor = 1;

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
        semanticType = getMultiNamedAttributeValueOrNull(messageNode,
                                                         new String[]{"semanticType", "fixMsgType"});  // optional
        headerType = getAttributeValue(messageNode, "headerType", "messageHeader");         // has default
        this.typeByNameMap = typeByNameMap;

        fieldList = parseFieldsAndGroups(messageNode);

        calculatedBlockLength = calculateAndValidateOffsets(messageNode, fieldList, blockLength);

        validateBlockLength(messageNode, blockLength, calculatedBlockLength);
    }

    private List<Field> parseFieldsAndGroups(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(FIELD_OR_GROUP_OR_DATA_EXPR).evaluate(node, XPathConstants.NODESET);
        int numGroupEncountered = 0, numDataEncountered = 0;

        List<Field> fieldList = new ArrayList<>();
        Map<String, Field> entryCountFieldMap = new HashMap<>();  // used for holding entry count fields and matching up
        Map<Integer, Field> lengthFieldMap = new HashMap<>();    // used for holding length fields and matching up

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field field;
            Type fieldType;

            final String nodeName = list.item(i).getNodeName();
            switch (nodeName)
            {
                case "group":
                    if (numDataEncountered > 0)
                    {
                        handleError(node, "group specified after data specified");
                    }

                    field = parseGroupNode(list.item(i), entryCountFieldMap);

                    /* if we have a dangling dimension Field, then add it here to fieldList. */
                    if (field.getId() != Field.INVALID_ID)
                    {
                        fieldList.add(field.getEntryCountField());
                    }

                    field.setGroupFields(parseFieldsAndGroups(list.item(i))); // recursive call

                    numGroupEncountered++;
                    break;

                case "data":
                    fieldType = typeByNameMap.get(getAttributeValue(list.item(i), "type"));
                    if (fieldType == null)
                    {
                        handleError(list.item(i), "could not find type");
                    }

                    field = parseDataNode(list.item(i), lengthFieldMap, fieldType);

                    numDataEncountered++;
                    break;

                case "field":
                    if (numGroupEncountered > 0 || numDataEncountered > 0)
                    {
                        handleError(node, "field specified after group or data specified");
                    }

                    fieldType = typeByNameMap.get(getAttributeValue(list.item(i), "type"));
                    if (fieldType == null)
                    {
                        handleError(list.item(i), "could not find type");
                    }

                    field = parseFieldNode(list.item(i), fieldType);

                    if (field.getGroupName() != null)
                    {
                        entryCountFieldMap.put(field.getGroupName(), field);
                        field.setIrId(irIdCursor++);
                    }

                    if (field.getRefId() != Field.INVALID_ID)
                    {
                        lengthFieldMap.put(Integer.valueOf(field.getRefId()), field);
                        field.setIrId(irIdCursor++);
                    }
                    break;

                default:
                    throw new IllegalStateException("Unknown node name: " + nodeName);
            }
            fieldList.add(field);
        }

        if (entryCountFieldMap.size() > 0)
        {
            handleWarning(node, "entry count field or fields not matched");
        }
        if (lengthFieldMap.size() > 0)
        {
            handleWarning(node, "length field or fields not matched");
        }

        return fieldList;
    }

    /**
     * parse and handle creating a Field that represents a repeating group
     */
    private Field parseGroupNode(final Node node, final Map<String, Field> entryCountFieldMap)
    {
        Field field = new Field.Builder(getAttributeValue(node, "name"))
            .description(getAttributeValueOrNull(node, "description"))
            .id(Integer.parseInt(getAttributeValue(node, "id", Field.INVALID_ID_STRING)))
            .blockLength(Integer.parseInt(getAttributeValue(node, "blockLength", "0")))
            .dimensionType(XmlSchemaParser.getAttributeValue(node, "dimensionType", "groupSizeEncoding"))
            .build();

        Field entryCountField = entryCountFieldMap.get(field.getName());

        if (entryCountField != null)                   /* separate field for entry count/dimensions */
        {
            /* wire up the cross references */
            field.setEntryCountField(entryCountField);
            entryCountField.setGroupField(field);

            field.setIrId(irIdCursor++);
            field.setIrRefId(entryCountField.getIrId());
            entryCountField.setIrRefId(field.getIrId());

            entryCountFieldMap.remove(field.getName()); // remove field so that it can't be reused at this level
        }
        else if (field.getId() != Field.INVALID_ID)    /* built-in field for entry count/dimensions */
        {
            /* group has id set. Which signifies an embedded dimension set */
            Field.Builder entryCountFieldBuilder = new Field.Builder("dimension" + field.getId());

            entryCountFieldBuilder.id(field.getId());

            Type type = typeByNameMap.get(field.getDimensionType());
            if (type == null)
            {
                handleError(node, "could not find dimensionType: " + field.getDimensionType());
            }
            entryCountFieldBuilder.type(type);

            entryCountField = entryCountFieldBuilder.build();

            field.setEntryCountField(entryCountField);
            entryCountField.setGroupField(field);

            entryCountField.setIrId(irIdCursor++);
            field.setIrId(irIdCursor++);
            field.setIrRefId(entryCountField.getIrId());
            entryCountField.setIrRefId(field.getIrId());
        }
        else
        {
            handleError(node, "could not find entry count field for group: " + field.getName());
        }
        return field;
    }

    /**
     * parse and handle creating a Field that represents a variable length field
     */
    private Field parseDataNode(final Node node, final Map<Integer, Field> lengthFieldMap, final Type type)
    {
        Field field = new Field.Builder(getAttributeValue(node, "name"))
            .description(getAttributeValueOrNull(node, "description"))
            .id(Integer.parseInt(getAttributeValue(node, "id")))
            .type(type)
            .offset(Integer.parseInt(getAttributeValue(node, "offset", "0")))
            .semanticType(getMultiNamedAttributeValueOrNull(node, new String[] {"semanticType", "fixUsage"}))
            .presence(Presence.lookup(getAttributeValueOrNull(node, "presence")))
            .build();

        field.validate(node);

        Field lengthField = lengthFieldMap.get(Integer.valueOf(field.getId()));

        if (lengthField != null) /* separate explicit field for length */
        {
            field.setLengthField(lengthField);
            lengthField.setDataField(field);

            field.setIrId(irIdCursor++);
            field.setIrRefId(lengthField.getIrId());
            lengthField.setIrRefId(field.getIrId());

            lengthFieldMap.remove(Integer.valueOf(field.getId())); // remove field so that it can be reused
        }
        else if (type instanceof CompositeType) /* embedded length in composite */
        {
            /* encoded types inside for "length" and "varData". We just let them go on and generate IR as normal. */
        }
        else
        {
            handleError(node, "could not find length field for data field: " + field.getName());
        }
        return field;
    }

    /**
     * parse and handle creating a Field that represents a field
     */
    private Field parseFieldNode(final Node node, final Type type)
    {
        Field field = new Field.Builder(getAttributeValue(node, "name"))
            .description(getAttributeValueOrNull(node, "description"))
            .groupName(getAttributeValueOrNull(node, "groupName"))
            .id(Integer.parseInt(getAttributeValue(node, "id")))
            .type(type)
            .offset(Integer.parseInt(getAttributeValue(node, "offset", "0")))
            .semanticType(getMultiNamedAttributeValueOrNull(node, new String[] {"semanticType", "fixUsage"}))
            .presence(Presence.lookup(getAttributeValueOrNull(node, "presence")))
            .refId(Integer.parseInt(getAttributeValue(node, "refId", Field.INVALID_ID_STRING)))
            .build();

        field.validate(node);

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
            /* check if specified offset is ok or not */
            if (field.getOffset() > 0 && field.getOffset() < currOffset)
            {
                handleError(node, "specified offset is too small for field name: " + field.getName());
            }

            if (Token.VARIABLE_SIZE != currOffset)
            {
                /* if offset specified, then use it (since it was checked before) */
                if (field.getOffset() > 0)
                {
                    currOffset = field.getOffset();  // reset current offset to the one requested by the field specification
                }
                else if (field.getEntryCountField() != null && blockLength > 0)
                {
                    currOffset = blockLength;        // reset current offset to the blockLength specified
                }
                else if (field.getLengthField() != null && blockLength > 0)
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
    public long getBlockLength()
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
