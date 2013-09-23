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
 * An SBE message.
 */
public class Message
{
    private final long id;
    private final String name;
    private final String description;
    private final long blockLength;
    private final List<Field> fieldList;
    private final String fixMsgType;

    /**
     * Construct a new message from XML Schema.
     *
     * @param node     from the XML Schema Parsing
     * @param typesMap holding type information for message
     */
    public Message(final Node node, final Map<String, Type> typesMap)
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
        id = Long.parseLong(getXmlAttributeValue(node, "id"));                        // required
        name = getXmlAttributeValue(node, "name");                                    // required
        description = getXmlAttributeValueOrNull(node, "description");                // optional
        blockLength = Long.parseLong(getXmlAttributeValue(node, "blockLength", "0")); // 0 means not set
        fixMsgType = getXmlAttributeValueOrNull(node, "fixMsgType");                  // optional

        fieldList = parseXmlFieldsAndGroups(node, typesMap);
    }

    private static List<Field> parseXmlFieldsAndGroups(final Node node, final Map<String, Type> typesMap)
        throws XPathExpressionException, IllegalArgumentException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("field|group|data").evaluate(node, XPathConstants.NODESET);

        List<Field> fieldList = new ArrayList<Field>();
        Map<String, Field> entryCountFieldMap = new HashMap<String, Field>();  // used for holding entry count fields and matching up
        Map<Integer, Field> lengthFieldMap = new HashMap<Integer, Field>();    // used for holding length fields and matching up

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field f = null;

            if (list.item(i).getNodeName().equals("group"))
            {
                /*
                 * must search for previously parsed field that has groupName = to name (can this map be only visible on the stack?)
                 * must exist as it had to be placed before the group.
                 */

                /* use the Field constructor that is for group (not field) */
                f = new Field(list.item(i), getXmlAttributeValue(list.item(i), "name"));

                Field entryCountField = entryCountFieldMap.get(f.getName());

                if (entryCountField == null)
                {
                    throw new IllegalArgumentException("could not find entry count field for group: " + f.getName());
                }

                f.setEntryCountField(entryCountField);
                entryCountFieldMap.remove(f.getName()); // remove field so that it can't be reused as this level

                f.setGroupFieldList(parseXmlFieldsAndGroups(list.item(i), typesMap)); // recursive call
            }
            else if (list.item(i).getNodeName().equals("field"))
            {
        		/* use the Field constructor that is for field (not group) */
                f = new Field(list.item(i),
                              getXmlAttributeValue(list.item(i), "name"),
                              Integer.parseInt(getXmlAttributeValue(list.item(i), "id")),
                              lookupType(typesMap, getXmlAttributeValue(list.item(i), "type")));

                /* save field for matching up with group if this is an entry count field */
                if (f.getGroupName() != null)
                {
                    entryCountFieldMap.put(f.getGroupName(), f);
                }

                /* save refId for matching up with data if this is a Length field */
                if (f.getRefId() != Field.INVALID_ID)
                {
                    lengthFieldMap.put(new Integer(f.getRefId()), f);
                }
            }
            else if (list.item(i).getNodeName().equals("data"))
            {
        		/* use the Field constructor that is for field (even though this is a data) */
                f = new Field(list.item(i),
                              getXmlAttributeValue(list.item(i), "name"),
                              Integer.parseInt(getXmlAttributeValue(list.item(i), "id")),
                              lookupType(typesMap, getXmlAttributeValue(list.item(i), "type")));

                /* match up with length field */
                Integer lengthFieldRefId = new Integer(f.getId());
                Field lengthField = lengthFieldMap.get(lengthFieldRefId);

                if (lengthField == null)
                {
                    throw new IllegalArgumentException("could not find length field for data field: " + f.getName());
                }

                f.setLengthField(lengthField);
                lengthFieldMap.remove(lengthFieldRefId); // remove field so that it can be reused
            }

            fieldList.add(f);
        }
        /*
         * TODO: if the entryCountMap is not empty, then it means something didn't get matched up... warning?
         * TODO: same for lengthFieldMap
         */
        return fieldList;
    }

    /**
     * static method to encapsulate exception for them type does not exist.
     */
    private static Type lookupType(final Map<String, Type> map, final String name)
        throws IllegalArgumentException
    {
        Type t = map.get(name);

        if (t == null)
        {
            throw new IllegalArgumentException("type does not exist for name: " + name);
        }

        return t;
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
    public List<Field> getFieldList()
    {
        return fieldList;
    }

    /**
     * TODO: provide iterator interface to iterate through all fields and groups seamlessly for IR generation
     */

    /**
     * Class to hold field (or group) information
     */
    public static class Field
    {
        public static final int INVALID_ID = Integer.MAX_VALUE;  // id must only be short, so this is way out of range.
        public static final String INVALID_ID_STRING = Integer.toString(INVALID_ID);

        private final String name;                      // required for field/data & group
        private final String description;               // optional for field/data & group
        private final String groupName;                 // optional for field/date (not present for group)
        private final int id;                           // required for field/data (not present for group)
        private final Type type;                        // required for field/data (not present for group)
        private final long offset;                      // optional for field/data (not present for group)
        private final FixUsage fixUsage;                // optional for field/data (not present for group?)
        private final Presence presence;                // optional for field/data (not present for group)  null means not set
        private final int refId;                        // optional for field (not present for group or data) INVALID_ID means not set
        private final long blockLength;                 // optional for group (not present for field/data)
        private List<Field> groupFieldList;
        private Field entryCountField;                  // used by group fields as the entry count field
        private Field lengthField;                      // used by data fields as the length field

        /**
         * The field constructor
         */
        public Field(final Node node, final String name, final int id, final Type type)
        {
            this.name = name;
            this.description = XmlSchemaParser.getXmlAttributeValueOrNull(node, "description");
            this.groupName = XmlSchemaParser.getXmlAttributeValueOrNull(node, "groupName");
            this.id = id;
            this.type = type;
            this.offset = Long.parseLong(getXmlAttributeValue(node, "offset", "0"));
            this.fixUsage = FixUsage.lookup(XmlSchemaParser.getXmlAttributeValueOrNull(node, "fixUsage"));
            this.presence = Presence.lookup(XmlSchemaParser.getXmlAttributeValueOrNull(node, "presence"));
            this.refId = Integer.parseInt(getXmlAttributeValue(node, "refId", INVALID_ID_STRING));
            this.blockLength = 0;
            this.groupFieldList = null;   // has no meaning if not group
            this.entryCountField = null;  // has no meaning if not group
            this.lengthField = null;      // will be set later

            /* fixUsage must be present or must be on the type. If on both, they must agree. */
            if (this.fixUsage == null && this.type.getFixUsage() == null)
            {
                throw new IllegalArgumentException("Missing fixUsage on type and field: " + this.name);
            }
            else if (this.fixUsage != null && this.type.getFixUsage() != null && this.fixUsage != this.type.getFixUsage())
            {
                throw new IllegalArgumentException("Mismatched fixUsage on type and field: " + this.name);
            }
        }

        /**
         * The group constructor
         */
        public Field(final Node node, final String name)
        {
            this.name = name;
            this.description = XmlSchemaParser.getXmlAttributeValueOrNull(node, "description");
            this.groupName = null;
            this.id = INVALID_ID;
            this.type = null;
            this.offset = 0;
            this.fixUsage = null;
            this.presence = null;
            this.refId = INVALID_ID;
            this.blockLength = Long.parseLong(getXmlAttributeValue(node, "blockLength", "0"));
            this.groupFieldList = null;    // for now. Set later.
            this.entryCountField = null;   // for now. Set later.
            this.lengthField = null;       // has no meaning for group.
        }

        public void setGroupFieldList(final List<Field> list)
        {
            groupFieldList = list;
        }

        public void setEntryCountField(final Field field)
        {
            entryCountField = field;
        }

        public void setLengthField(final Field field)
        {
            lengthField = field;
        }

        public List<Field> getGroupFieldList()
        {
            return groupFieldList;
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
    }
}
