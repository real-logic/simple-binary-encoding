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
import uk.co.real_logic.sbe.Primitive;
import uk.co.real_logic.sbe.PrimitiveValue;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/** SBE setType */
public class SetType extends Type
{
    private final Primitive encodingType;
    private final Map<PrimitiveValue, Choice> choiceMap;
    private final Map<String, Choice> nameMap;

    /**
     * Construct a new SetType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public SetType(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        super(node, TypeOfType.SET); // set the common schema attributes

        /*
         * grab attributes from schema
         * - encodingType (required) - must be either uint8, uint16, uint32, or uint64
         */
        encodingType = Primitive.lookup(getAttributeValue(node, "encodingType"));
        if (encodingType != Primitive.UINT8 && encodingType != Primitive.UINT16 &&
            encodingType != Primitive.UINT32 && encodingType != Primitive.UINT64)
        {
            throw new IllegalArgumentException("unknown encodingType " + this.encodingType);
        }

        choiceMap = new HashMap<PrimitiveValue, Choice>();
        nameMap = new HashMap<String, Choice>();

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("choice").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Choice c = new Choice(list.item(i), encodingType);

            if (choiceMap.get(c.getPrimitiveValue()) != null)
            {
                throw new IllegalArgumentException("choice value already exists: " + c.getPrimitiveValue());
            }

            if (nameMap.get(c.getName()) != null)
            {
                throw new IllegalArgumentException("choice already exists for name: " + c.getName());
            }

            choiceMap.put(c.getPrimitiveValue(), c);
            nameMap.put(c.getName(), c);
        }
    }

    public Primitive getEncodingType()
    {
        return encodingType;
    }

    public Choice getChoice(final PrimitiveValue value)
    {
        return choiceMap.get(value);
    }

    public Choice getChoice(final String name)
    {
        return nameMap.get(name);
    }

    /*
     * TODO: can iterate like (Map.Entry<String, Choice> entry : EnumType.getChoiceSet()) with this
     */
    public Set<Map.Entry<String, Choice>> getChoiceSet()
    {
        return nameMap.entrySet();
    }

    /** Class to hold valid values for EnumType */
    public static class Choice
    {
        private final String name;
        private final String description;
        private final PrimitiveValue value;
        private final Primitive encodingType;

        /**
         * Construct a Choice given the XML node and the encodingType
         *
         * @param node         that contains the validValue
         * @param encodingType for the enum
         */
        public Choice(final Node node, final Primitive encodingType)
        {
            this.encodingType = encodingType;
            name = getAttributeValue(node, "name");
            description = getAttributeValueOrNull(node, "description");
            value = new PrimitiveValue(encodingType, node.getFirstChild().getNodeValue());

            // choice values are bit positions (0, 1, 2, 3, 4, etc.) from LSB to MSB
            if (value.longValue() >= (encodingType.size() * 8))
            {
                throw new IllegalArgumentException("Choice value out of bounds: " + this.value.longValue());
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
