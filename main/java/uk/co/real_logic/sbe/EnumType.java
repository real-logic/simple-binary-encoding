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
import java.util.HashMap;
import java.util.Set;

/**
 * SBE enumType
 */
public class EnumType extends Type
{
    private final Primitive encodingType;
    private final PrimitiveValue nullValue;
    private final Map<PrimitiveValue, ValidValue> valueMap;
    private final Map<String, ValidValue> nameMap;

    /**
     * Construct a new enumType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EnumType(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        super(node); // set the common schema attributes

        /**
         * grab attributes from schema
         * - encodingType (required) - must be either 'char' or 'int8' according to spec
         * - nullValue (optional with presence=optional)
         */
        this.encodingType = Primitive.lookup(XmlSchemaParser.getXmlAttributeValue(node, "encodingType"));
        if (this.encodingType != Primitive.CHAR && this.encodingType != Primitive.INT8)
        {
            throw new IllegalArgumentException("unknown encodingType " + this.encodingType);
        }

        String nullValueStr = XmlSchemaParser.getXmlAttributeValueNullable(node, "nullValue");
        if (nullValueStr != null)
        {
            // nullValue is mutually exclusive with presence=required or constant
            if (this.getPresence() != Presence.OPTIONAL)
            {
                throw new IllegalArgumentException("nullValue set, but presence is not optional");
            }

            this.nullValue = new PrimitiveValue(this.encodingType, nullValueStr);
        }
        else
        {
            this.nullValue = null;
        }

        this.valueMap = new HashMap<PrimitiveValue, ValidValue>();
        this.nameMap = new HashMap<String, ValidValue>();

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("validValue").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            ValidValue v = new ValidValue(list.item(i), encodingType);

            if (this.valueMap.get(v.getPrimitiveValue()) != null)
            {
                throw new IllegalArgumentException("validValue already exists for value: " + v.getPrimitiveValue().toString());
            }

            if (this.nameMap.get(v.getName()) != null)
            {
                throw new IllegalArgumentException("validValue already exists for name: " + v.getName());
            }

            this.valueMap.put(v.getPrimitiveValue(), v);
            this.nameMap.put(v.getName(), v);
        }
    }

    public Primitive getEncodingType()
    {
        return encodingType;
    }

    public ValidValue getValidValue(final PrimitiveValue value)
    {
        return valueMap.get(value);
    }

    public ValidValue getValidValue(final String name)
    {
        return nameMap.get(name);
    }

    /**
     * TODO: can iteraate like (Map.Entry<String, ValidValue> entry : EnumType.getValidValueSet()) with this
     */
    public Set<Map.Entry<String, ValidValue>> getValidValueSet()
    {
        return nameMap.entrySet();
    }

    /**
     * Class to hold valid values for EnumType
     */
    public class ValidValue
    {
        private final String name;
        private final String description;
        private final PrimitiveValue value;
        private final Primitive encodingType;

        /**
         * Construct a ValidValue given the XML node and the encodingType
         *
         * @param node         that contains the validValue
         * @param encodingType for the enum
         */
        public ValidValue(final Node node, final Primitive encodingType)
        {
            /**
             * attrs: name(required), description(optional)
             * value: the value of the validValue
             */
            this.encodingType = encodingType;
            this.name = XmlSchemaParser.getXmlAttributeValue(node, "name");
            this.description = XmlSchemaParser.getXmlAttributeValueNullable(node, "description");
            this.value = new PrimitiveValue(encodingType, node.getFirstChild().getNodeValue());
        }

        public PrimitiveValue getPrimitiveValue()
        {
            return value;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
