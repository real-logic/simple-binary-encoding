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
    private final long blockLength;
    private final List<Field> fieldList;
    private final String fixMsgType;
    private int irIdCursor = 1;

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
         * - id (required) - unique within message?
         *
         * group
         * - name (required) - unique within message? field for num entries must precede it!
         */
        id = Long.parseLong(getAttributeValue(messageNode, "id"));                        // required
        name = getAttributeValue(messageNode, "name");                                    // required
        description = getAttributeValueOrNull(messageNode, "description");                // optional
        blockLength = Long.parseLong(getAttributeValue(messageNode, "blockLength", "0")); // 0 means not set
        fixMsgType = getAttributeValueOrNull(messageNode, "fixMsgType");                  // optional

        fieldList = parseFieldsAndGroups(messageNode, typeByNameMap);
    }

    private List<Field> parseFieldsAndGroups(final Node node, final Map<String, Type> typeByNameMap)
        throws XPathExpressionException, IllegalArgumentException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(FIELD_OR_GROUP_OR_DATA_EXPR).evaluate(node, XPathConstants.NODESET);

        List<Field> fieldList = new ArrayList<Field>();
        Map<String, Field> entryCountFieldMap = new HashMap<>();  // used for holding entry count fields and matching up
        Map<Integer, Field> lengthFieldMap = new HashMap<>();    // used for holding length fields and matching up

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field field = null;

            final String nodeName = list.item(i).getNodeName();
            switch (nodeName)
            {
                case "group":
                    /*
                     * must search for previously parsed field that has groupName = to name
                     * (can this map be only visible on the stack?)
                     * must exist as it had to be placed before the group.
                     */
                    field = new Field(list.item(i), getAttributeValue(list.item(i), "name"));
                    Field entryCountField = entryCountFieldMap.get(field.getName());

                    if (entryCountField == null)
                    {
                        throw new IllegalArgumentException("could not find entry count field for group: " + field.getName());
                    }

                    field.setEntryCountField(entryCountField);
                    entryCountField.setGroupField(field);

                    field.setIrId(irIdCursor++);
                    field.setXRefIrId(entryCountField.getIrId());
                    entryCountField.setXRefIrId(field.getIrId());

                    entryCountFieldMap.remove(field.getName()); // remove field so that it can't be reused as this level

                    field.setGroupFieldList(parseFieldsAndGroups(list.item(i), typeByNameMap)); // recursive call
                    break;

                case "field":
                    field = new Field(list.item(i),
                                      getAttributeValue(list.item(i), "name"),
                                      Integer.parseInt(getAttributeValue(list.item(i), "id")),
                                      lookupType(typeByNameMap, getAttributeValue(list.item(i), "type")));

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

                case "data":
                    field = new Field(list.item(i),
                                      getAttributeValue(list.item(i), "name"),
                                      Integer.parseInt(getAttributeValue(list.item(i), "id")),
                                      lookupType(typeByNameMap, getAttributeValue(list.item(i), "type")));

                    Field lengthField = lengthFieldMap.get(Integer.valueOf(field.getId()));
                    if (lengthField == null)
                    {
                        throw new IllegalArgumentException("could not find length field for data field: " + field.getName());
                    }

                    field.setLengthField(lengthField);
                    lengthField.setDataField(field);

                    field.setIrId(irIdCursor++);
                    field.setXRefIrId(lengthField.getIrId());
                    lengthField.setXRefIrId(field.getIrId());

                    lengthFieldMap.remove(Integer.valueOf(field.getId())); // remove field so that it can be reused
                    break;

                default:
                    throw new IllegalStateException("Unknown node name: " + nodeName);
            }

            fieldList.add(field);
        }
        /*
         * TODO: if the entryCountMap is not empty, then it means something didn't get matched up... warning?
         * TODO: same for lengthFieldMap
         */
        return fieldList;
    }

    /** static method to encapsulate exception for them type does not exist. */
    private static Type lookupType(final Map<String, Type> typeByNameMap, final String name)
        throws IllegalArgumentException
    {
        Type type = typeByNameMap.get(name);
        if (type == null)
        {
            throw new IllegalArgumentException("Type does not exist for name: " + name);
        }

        return type;
    }

    /**
     * Return the template id of the message
     *
     * @return id of the message
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
     * Return the list of fields in the message
     *
     * @return {@link java.util.List} of the Field objects in this Message
     */
    public List<Field> getFields()
    {
        return fieldList;
    }

    /** Class to hold field (or group) information */
    public static class Field
    {
        public static final int INVALID_ID = Integer.MAX_VALUE;  // id must only be short, so this is way out of range.
        public static final String INVALID_ID_STRING = Integer.toString(INVALID_ID);

        private final String name;          // required for field/data & group
        private final String description;   // optional for field/data & group
        private final String groupName;     // optional for field/date (not present for group)
        private final int id;               // required for field/data (not present for group)
        private final Type type;            // required for field/data (not present for group)
        private final long offset;          // optional for field/data (not present for group)
        private final FixUsage fixUsage;    // optional for field/data (not present for group?)
        private final Presence presence;    // optional for field/data (not present for group)  null means not set
        private final int refId;            // optional for field (not present for group or data) INVALID_ID means not set
        private final long blockLength;     // optional for group (not present for field/data)
        private List<Field> groupFieldList;
        private Field entryCountField;      // used by group fields as the entry count field
        private Field lengthField;          // used by data fields as the length field
        private Field groupField;           // used by entry count fields as the group field
        private Field dataField;            // used by length fields as the data field
        private int irId = INVALID_ID;      // used to identify this field by an IR ID
        private int xRefIrId = INVALID_ID;  // used to identify an associated field by an IR ID

        /** The field constructor */
        public Field(final Node node, final String name, final int id, final Type type)
        {
            this.name = name;
            this.description = XmlSchemaParser.getAttributeValueOrNull(node, "description");
            this.groupName = XmlSchemaParser.getAttributeValueOrNull(node, "groupName");
            this.id = id;
            this.type = type;
            this.offset = Long.parseLong(getAttributeValue(node, "offset", "0"));
            this.fixUsage = FixUsage.lookup(XmlSchemaParser.getAttributeValueOrNull(node, "fixUsage"));
            this.presence = Presence.lookup(XmlSchemaParser.getAttributeValueOrNull(node, "presence"));
            this.refId = Integer.parseInt(getAttributeValue(node, "refId", INVALID_ID_STRING));
            this.blockLength = 0;
            this.groupFieldList = null;   // has no meaning if not group
            this.entryCountField = null;  // has no meaning if not group
            this.lengthField = null;      // will be set later
            this.groupField = null;       // will be set later
            this.dataField = null;        // will be set later

            // fixUsage must be present or must be on the type. If on both, they must agree.
            if (fixUsage == null && type.getFixUsage() == null)
            {
                throw new IllegalArgumentException("Missing fixUsage on type and field: " + name);
            }
            else if (fixUsage != null && type.getFixUsage() != null && fixUsage != type.getFixUsage())
            {
                throw new IllegalArgumentException("Mismatched fixUsage on type and field: " + name);
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
            this.blockLength = Long.parseLong(getAttributeValue(node, "blockLength", "0"));
            this.groupFieldList = null;    // for now. Set later.
            this.entryCountField = null;   // for now. Set later.
            this.lengthField = null;       // has no meaning for group.
            this.groupField = null;        // has no meaning
            this.dataField = null;         // has no meaning
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

        public long getOffset()
        {
            return offset;
        }

        public long getBlockLength()
        {
            return blockLength;
        }

        public void setIrId(final int id)
        {
            irId = id;
        }

        public int getIrId()
        {
            return irId;
        }

        public void setXRefIrId(final int id)
        {
            xRefIrId = id;
        }

        public int getXRefIrId()
        {
            return xRefIrId;
        }
    }
}
