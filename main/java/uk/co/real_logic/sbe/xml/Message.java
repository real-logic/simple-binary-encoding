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
 * An SBE message containing a list of {@link Message.Field} objects and SBE message attributes.
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
    private final String fixMsgType;
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
        /*
         * message
         * - name (required) - unique?
         *
         * field
         * - name (required) - unique within message?
         * - schemaId (required) - unique within message?
         *
         * group
         * - name (required) - unique within message? field for num entries must precede it!
         */
        id = Long.parseLong(getAttributeValue(messageNode, "id"));                          // required
        name = getAttributeValue(messageNode, "name");                                      // required
        description = getAttributeValueOrNull(messageNode, "description");                  // optional
        blockLength = Integer.parseInt(getAttributeValue(messageNode, "blockLength", "0")); // 0 means not set
        fixMsgType = getAttributeValueOrNull(messageNode, "fixMsgType");                    // optional

        fieldList = parseFieldsAndGroups(messageNode, typeByNameMap);

        validateBlockLength(messageNode, blockLength, calculateAndValidateOffsets(messageNode, fieldList));
    }

    private List<Field> parseFieldsAndGroups(final Node node, final Map<String, Type> typeByNameMap)
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
                    field.setGroupFieldList(parseFieldsAndGroups(list.item(i), typeByNameMap)); // recursive call

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

                    field = new Field(list.item(i),
                                      getAttributeValue(list.item(i), "name"),
                                      Integer.parseInt(getAttributeValue(list.item(i), "id")),
                                      fieldType);

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
    private Field parseGroupNode(final Node node, Map<String, Field> entryCountFieldMap)
    {
        Field field = new Field(node, getAttributeValue(node, "name"));
        Field entryCountField = entryCountFieldMap.get(field.getName());

        if (entryCountField == null)
        {
            handleError(node, "could not find entry count field for group: " + field.getName());
        }
        else
        {
            field.setEntryCountField(entryCountField);
            entryCountField.setGroupField(field);

            field.setIrId(irIdCursor++);
            field.setIrRefId(entryCountField.getIrId());
            entryCountField.setIrRefId(field.getIrId());

            entryCountFieldMap.remove(field.getName()); // remove field so that it can't be reused as this level
        }
        return field;
    }

    /**
     * parse and handle creating a Field that represents a variable length field
     */
    private Field parseDataNode(final Node node, Map<Integer, Field> lengthFieldMap, Type type)
    {
        Field field = new Field(node,
                                getAttributeValue(node, "name"),
                                Integer.parseInt(getAttributeValue(node, "id")),
                                type);

        Field lengthField = lengthFieldMap.get(Integer.valueOf(field.getId()));

        if (lengthField == null)
        {
            handleError(node, "could not find length field for data field: " + field.getName());
        }
        else
        {
            field.setLengthField(lengthField);
            lengthField.setDataField(field);

            field.setIrId(irIdCursor++);
            field.setIrRefId(lengthField.getIrId());
            lengthField.setIrRefId(field.getIrId());

            lengthFieldMap.remove(Integer.valueOf(field.getId())); // remove field so that it can be reused
        }
        return field;
    }

    /**
     * Calculate and validate the offsets of the fields in the list. Will set the fields calculatedOffset.
     * Will validate the blockLength of the fields encompassing &lt;message&gt; or &lt;group&gt;. Will recurse
     * into repeated groups.
     *
     * @param fields to iterate over
     * @return the total size of the list or {@link Token#VARIABLE_SIZE} if the size will vary
     */
    public int calculateAndValidateOffsets(final Node node, List<Field> fields)
    {
        int currOffset = 0;

        for (Field field : fields)
        {
            /* check if specified offset is ok or not */
            if (field.getOffset() > 0 && field.getOffset() < currOffset)
            {
                handleError(node, "specified offset is too small for field name: " + field.getName());
            }

            /* if offset specified, then use it (since it was checked before) */
            if (field.getOffset() > 0 && Token.VARIABLE_SIZE != currOffset)
            {
                currOffset = field.getOffset();  // reset current offset to the one requested by the field specifcation
            }

            /* save the fields current offset (even for <group> elements!) */
            field.setCalculatedOffset(currOffset);

            /* if this field is a <group> then recurse into it */
            if (field.getGroupFieldList() != null)
            {
                int calculatedBlockLength = calculateAndValidateOffsets(node, field.getGroupFieldList());

                /* validate the <group> blockLength, if set */
                validateBlockLength(node, field.getBlockLength(), calculatedBlockLength);

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
     * The fixMsgType of the message (if set) or null
     */
    public String getFixMsgType()
    {
        return fixMsgType;
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
        return blockLength;
    }

    private void validateBlockLength(final Node node, final long specifiedBlockLength, final long calculatedBlockLength)
    {
        if (0 < specifiedBlockLength && calculatedBlockLength > specifiedBlockLength)
        {
            handleError(node, "specified blockLength is too small");
        }
    }

    /*
     * TODO: probably should break this class out into its own main class
     */

    /** Holder for Field (or Group) information */
    public static class Field
    {
        public static final int INVALID_ID = Integer.MAX_VALUE;  // schemaId must only be short, so this is way out of range.
        public static final String INVALID_ID_STRING = Integer.toString(INVALID_ID);

        private final String name;          // required for field/data & group
        private final String description;   // optional for field/data & group
        private final String groupName;     // optional for field/date (not present for group)
        private final int id;               // required for field/data (not present for group)
        private final Type type;            // required for field/data (not present for group)
        private final int offset;           // optional for field/data (not present for group)
        private final String fixUsage;      // optional for field/data (not present for group?)
        private final Presence presence;    // optional for field/data (not present for group)  null means not set
        private final int refId;            // optional for field (not present for group or data) INVALID_ID means not set
        private final int blockLength;      // optional for group (not present for field/data)
        private List<Field> groupFieldList;
        private Field entryCountField;      // used by group fields as the entry count field
        private Field lengthField;          // used by data fields as the length field
        private Field groupField;           // used by entry count fields as the group field
        private Field dataField;            // used by length fields as the data field
        private long irId = INVALID_ID;     // used to identify this field by an IR ID
        private long irRefId = INVALID_ID;  // used to identify an associated field by an IR ID
        private int calculatedOffset;       // used to hold the calculated offset of this field from top level <message> or <group>

        /** The field constructor */
        public Field(final Node node, final String name, final int id, final Type type)
        {
            this.name = name;
            this.description = getAttributeValueOrNull(node, "description");
            this.groupName = getAttributeValueOrNull(node, "groupName");
            this.id = id;
            this.type = type;
            this.offset = Integer.parseInt(getAttributeValue(node, "offset", "0"));
            this.fixUsage = getAttributeValueOrNull(node, "fixUsage");
            this.presence = Presence.lookup(getAttributeValueOrNull(node, "presence"));
            this.refId = Integer.parseInt(getAttributeValue(node, "refId", INVALID_ID_STRING));
            this.blockLength = 0;
            this.groupFieldList = null;   // has no meaning if not group
            this.entryCountField = null;  // has no meaning if not group
            this.lengthField = null;      // will be set later
            this.groupField = null;       // will be set later
            this.dataField = null;        // will be set later
            this.calculatedOffset = 0;

            if (type != null)
            {
                // fixUsage must be present or must be on the type. If on both, they must agree.
                if (fixUsage == null && type.getFixUsage() == null)
                {
                    handleError(node, "Missing fixUsage on type and field: " + name);
                }
                else if (fixUsage != null && type.getFixUsage() != null && !fixUsage.equals(type.getFixUsage()))
                {
                    handleError(node, "Mismatched fixUsage on type and field: " + name);
                }
            }
        }

        /** The group constructor */
        public Field(final Node node, final String name)
        {
            this.name = name;
            this.description = XmlSchemaParser.getAttributeValueOrNull(node, "description");
            this.groupName = null;
            this.id = INVALID_ID;
            this.type = null;
            this.offset = 0;
            this.fixUsage = null;
            this.presence = null;
            this.refId = INVALID_ID;
            this.blockLength = Integer.parseInt(getAttributeValue(node, "blockLength", "0"));
            this.groupFieldList = null;    // for now. Set later.
            this.entryCountField = null;   // for now. Set later.
            this.lengthField = null;       // has no meaning for group.
            this.groupField = null;        // has no meaning
            this.dataField = null;         // has no meaning
            this.calculatedOffset = 0;
        }

        public void setGroupFieldList(final List<Field> list)
        {
            groupFieldList = list;
        }

        public List<Field> getGroupFieldList()
        {
            return groupFieldList;
        }

        public void setEntryCountField(final Field field)
        {
            entryCountField = field;
        }

        public Field getEntryCountField()
        {
            return entryCountField;
        }

        public void setLengthField(final Field field)
        {
            lengthField = field;
        }

        public Field getLengthField()
        {
            return lengthField;
        }

        public void setGroupField(final Field field)
        {
            groupField = field;
        }

        public Field getGroupField()
        {
            return groupField;
        }

        public void setDataField(final Field field)
        {
            dataField = field;
        }

        public Field getDataField()
        {
            return dataField;
        }

        public void setCalculatedOffset(final int offset)
        {
            calculatedOffset = offset;
        }

        public int getCalculatedOffset()
        {
            return calculatedOffset;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getGroupName()
        {
            return groupName;
        }

        public int getId()
        {
            return id;
        }

        public int getRefId()
        {
            return refId;
        }

        public Type getType()
        {
            return type;
        }

        public int getOffset()
        {
            return offset;
        }

        public int getBlockLength()
        {
            return blockLength;
        }

        public void setIrId(final long id)
        {
            irId = id;
        }

        public long getIrId()
        {
            return irId;
        }

        public void setIrRefId(final long id)
        {
            irRefId = id;
        }

        public long getIrRefId()
        {
            return irRefId;
        }

        public String getFixUsage()
        {
            return fixUsage;
        }

        public String toString()
        {
            return "Field{" +
                "name=" + name +
                ", description=" + description +
                ", groupName=" + groupName +
                ", id=" + id +
                ", type=" + type +
                ", offset=" + offset +
                ", fixUsage=" + fixUsage +
                ", presence=" + presence +
                ", refId=" + refId +
                ", blockLength=" + blockLength +
                ", groupFieldList=" + groupFieldList +
                ", entryCountField=" + entryCountField +
                ", lengthField=" + lengthField +
                ", groupField=" + groupField +
                ", dataField=" + dataField +
                ", irId=" + irId +
                ", irRefId =" + irRefId +
                ", calculatedOffset=" + calculatedOffset +
                '}';
        }
    }
}
