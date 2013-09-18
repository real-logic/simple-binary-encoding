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
package uk.co.real_logic.sbe;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * An SBE message.
 */
public class Message
{
    private final Long id;
    private final String name;
    private final String description;
    private final long blockLength;
    private final List<Field> fieldList;

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
        this.id = new Long(XmlSchemaParser.getXmlAttributeValue(node, "id"));                              // required
        this.name = XmlSchemaParser.getXmlAttributeValue(node, "name");                                    // required
        this.description = XmlSchemaParser.getXmlAttributeValueNullable(node, "description");              // optional
        this.blockLength = Long.parseLong(XmlSchemaParser.getXmlAttributeValue(node, "blockLength", "0")); // 0 means not set
        this.fieldList = parseXmlFieldsAndGroups(node, typesMap);
    }

    private static List<Field> parseXmlFieldsAndGroups(final Node node, final Map<String, Type> typesMap)
        throws XPathExpressionException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("field|group").evaluate(node, XPathConstants.NODESET);

        List<Field> fieldList = new ArrayList<Field>();

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Field f = null;

            if (list.item(i).getNodeName().equals("group"))
            {
                /*
                 * TODO: must search for previously parsed field that has groupName = to name (can this map be only visible on the stack?)
                 */
                /* use the Field constructor that is for group (not field) */
                f = new Field(list.item(i),
                              XmlSchemaParser.getXmlAttributeValue(list.item(i), "name"));
                f.setGroupFieldList(parseXmlFieldsAndGroups(list.item(i), typesMap)); // recursive call
            }
            else if (list.item(i).getNodeName().equals("field"))
            {
        		/* use the Field constructor that is for field (not group) */
                f = new Field(list.item(i),
                              XmlSchemaParser.getXmlAttributeValue(list.item(i), "name"),
                              Integer.parseInt(XmlSchemaParser.getXmlAttributeValue(list.item(i), "id")),
                              lookupType(typesMap, XmlSchemaParser.getXmlAttributeValue(list.item(i), "type")));
            }

            fieldList.add(f);
        }

        return fieldList;
    }

    /**
     * static method to encapsulate exception for them type does not exist.
     */
    private static Type lookupType(final Map<String, Type> map, final String name)
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
    public Long getId()
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
     * TODO: provide iterator interface to iterate through all fields and groups seamlessly for IR generation
     */

    /**
     * Class to hold field (or group) information
     */
    public static class Field
    {
        /*
             * field
             * - name (required) - unique within message?
             * - id (required) - unique within message?
             * - type (required)
             * - refId (optional)
             * - description (optional)
             * - offset (optional)
             * - groupName (optional)
             *
             * group
             * - name (required) - unique within message? field for num entries must precede it!
             * - description (optional)
             * - blockLength (optional)
         */
        public static final int INVALID_ID = Integer.MAX_VALUE;  // id must only be short, so this is way out of range.

        private final String name;                      // required for field & group
        private final String description;               // optional for field & group
        private final int id;                           // required for field (not present for group)
        private final Type type;                        // required for field (not present for group)
        private final long offset;                      // optional for field (not present for group)
        private final long blockLength;                 // optional for group (not present for field)
        private List<Field> groupFieldList;

        /**
         * The field constructor
         */
        public Field(final Node node, final String name, final int id, final Type type)
        {
            this.name = name;
            this.description = XmlSchemaParser.getXmlAttributeValueNullable(node, "description");
            this.id = id;
            this.type = type;
            this.offset = Long.parseLong(XmlSchemaParser.getXmlAttributeValue(node, "offset", "0"));
            this.blockLength = 0;
            this.groupFieldList = null; // has no meaning if not group
            // TODO: fixUsage must be present or must be on the type
        }

        /**
         * The group constructor
         */
        public Field(final Node node, final String name)
        {
            this.name = name;
            this.description = XmlSchemaParser.getXmlAttributeValueNullable(node, "description");
            this.id = INVALID_ID;
            this.type = null;
            this.offset = 0;
            this.blockLength = Long.parseLong(XmlSchemaParser.getXmlAttributeValue(node, "blockLength", "0"));
            this.groupFieldList = null; // for now. Set later.
        }

        public void setGroupFieldList(final List<Field> list)
        {
            groupFieldList = list;
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

        public int getId()
        {
            return id;
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
