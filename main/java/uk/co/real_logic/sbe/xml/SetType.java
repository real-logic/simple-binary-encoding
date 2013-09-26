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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/** SBE setType */
public class SetType extends Type
{
    private final PrimitiveType encodingType;
    private final Map<PrimitiveValue, Choice> choiceByPrimitiveValueMap = new HashMap<>();
    private final Map<String, Choice> choiceByNameMap = new HashMap<>();

    /**
     * Construct a new SetType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public SetType(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        super(node);

        encodingType = PrimitiveType.lookup(getAttributeValue(node, "encodingType"));
        if (encodingType != PrimitiveType.UINT8 && encodingType != PrimitiveType.UINT16 &&
            encodingType != PrimitiveType.UINT32 && encodingType != PrimitiveType.UINT64)
        {
            throw new IllegalArgumentException("Unknown encodingType " + encodingType);
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("choice").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Choice c = new Choice(list.item(i), encodingType);

            if (choiceByPrimitiveValueMap.get(c.getPrimitiveValue()) != null)
            {
                throw new IllegalArgumentException("Choice value already exists: " + c.getPrimitiveValue());
            }

            if (choiceByNameMap.get(c.getName()) != null)
            {
                throw new IllegalArgumentException("Choice already exists for name: " + c.getName());
            }

            choiceByPrimitiveValueMap.put(c.getPrimitiveValue(), c);
            choiceByNameMap.put(c.getName(), c);
        }
    }

    public PrimitiveType getEncodingType()
    {
        return encodingType;
    }

    /**
     * The size (in octets) of the encodingType
     *
     * @return size of the encodingType
     */
    public int size()
    {
        return encodingType.size();
    }

    public Choice getChoice(final PrimitiveValue value)
    {
        return choiceByPrimitiveValueMap.get(value);
    }

    public Choice getChoice(final String name)
    {
        return choiceByNameMap.get(name);
    }

    public Collection<Choice> getChoices()
    {
        return choiceByNameMap.values();
    }

    /** Holder for valid values for EnumType */
    public static class Choice
    {
        private final String name;
        private final String description;
        private final PrimitiveValue value;

        /**
         * Construct a Choice given the XML node and the encodingType
         *
         * @param node         that contains the validValue
         * @param encodingType for the enum
         */
        public Choice(final Node node, final PrimitiveType encodingType)
        {
            name = getAttributeValue(node, "name");
            description = getAttributeValueOrNull(node, "description");
            value = PrimitiveValue.parse(node.getFirstChild().getNodeValue(), encodingType);

            // choice values are bit positions (0, 1, 2, 3, 4, etc.) from LSB to MSB
            if (value.longValue() >= (encodingType.size() * 8))
            {
                throw new IllegalArgumentException("Choice value out of bounds: " + value.longValue());
            }
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
