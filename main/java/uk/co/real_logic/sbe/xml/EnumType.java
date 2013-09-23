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
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SBE enumType
 */
public class EnumType extends Type
{
    private final PrimitiveType encodingType;
    private final PrimitiveValue nullValue;
    private final Map<PrimitiveValue, ValidValue> validValueByPrimitiveValueMap = new HashMap<PrimitiveValue, ValidValue>();
    private final Map<String, ValidValue> validValueByNameMap = new HashMap<String, ValidValue>();

    /**
     * Construct a new enumType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EnumType(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        super(node, TypeOfType.ENUM); // set the common schema attributes

        /**
         * grab attributes from schema
         * - encodingType (required) - must be either 'char' or 'int8' according to spec
         * - nullValue (optional with presence=optional)
         */
        encodingType = PrimitiveType.lookup(XmlSchemaParser.getAttributeValue(node, "encodingType"));
        if (encodingType != PrimitiveType.CHAR && encodingType != PrimitiveType.UINT8)
        {
            throw new IllegalArgumentException("unknown encodingType " + encodingType);
        }

        String nullValueStr = XmlSchemaParser.getAttributeValueOrNull(node, "nullValue");
        if (nullValueStr != null)
        {
            // nullValue is mutually exclusive with presence=required or constant
            if (this.getPresence() != Presence.OPTIONAL)
            {
                throw new IllegalArgumentException("nullValue set, but presence is not optional");
            }

            nullValue = new PrimitiveValue(this.encodingType, nullValueStr);
        }
        else
        {
            nullValue = null;
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("validValue").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            ValidValue v = new ValidValue(list.item(i), encodingType);

            if (validValueByPrimitiveValueMap.get(v.getPrimitiveValue()) != null)
            {
                throw new IllegalArgumentException("validValue already exists for value: " + v.getPrimitiveValue());
            }

            if (validValueByNameMap.get(v.getName()) != null)
            {
                throw new IllegalArgumentException("validValue already exists for name: " + v.getName());
            }

            validValueByPrimitiveValueMap.put(v.getPrimitiveValue(), v);
            validValueByNameMap.put(v.getName(), v);
        }
    }

    public PrimitiveType getEncodingType()
    {
        return encodingType;
    }

    public ValidValue getValidValue(final PrimitiveValue value)
    {
        return validValueByPrimitiveValueMap.get(value);
    }

    public ValidValue getValidValue(final String name)
    {
        return validValueByNameMap.get(name);
    }

    /**
     * The nullValue of the type
     *
     * @return value of the nullValue primitive or type
     */
    public PrimitiveValue getNullValue()
    {
        if (nullValue == null)
        {
            return encodingType.nullValue();
        }

        return nullValue;
    }

    /**
     * TODO: can iterate like (Map.Entry<String, ValidValue> entry : EnumType.getValidValueSet()) with this
     */
    public Set<Map.Entry<String, ValidValue>> getValidValueSet()
    {
        return validValueByNameMap.entrySet();
    }

    /**
     * Class to hold valid values for EnumType
     */
    public static class ValidValue
    {
        private final String name;
        private final String description;
        private final PrimitiveValue value;
        private final PrimitiveType encodingType;

        /**
         * Construct a ValidValue given the XML node and the encodingType.
         *
         * @param node         that contains the validValue
         * @param encodingType for the enum
         */
        public ValidValue(final Node node, final PrimitiveType encodingType)
        {
            /*
             * attrs: name(required), description(optional)
             * value: the value of the validValue
             */
            this.encodingType = encodingType;
            name = XmlSchemaParser.getAttributeValue(node, "name");
            description = XmlSchemaParser.getAttributeValueOrNull(node, "description");
            value = new PrimitiveValue(encodingType, node.getFirstChild().getNodeValue());
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
