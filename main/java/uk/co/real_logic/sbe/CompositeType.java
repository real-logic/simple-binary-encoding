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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * SBE compositeType
 *
 * decimal types can use mantissa and exponent portions as min/max/null.
 *
 *              Length  Exponent     Min                Max              Null
 * decimal      9       -128 to 127  
 *  int64 mantissa                   -2^63 + 1          2^64(2^63 - 1)   -2^63
 *  int8 exponent                    10^127 to 10^-128  10^127
 * decimal64    8       -128 to 127                     
 *  int64 mantissa                   -2^63 + 1          2^64(2^63 - 1)   -2^63
 *  constant exponent                10^127 to 10^-128  10^127
 * decimal32    4       -128 to 127
 *  int32 mantissa                   -2^32 + 1          2^32(2^31 - 1)   -2^31
 *  constant exponent                10^-127            10^127
 */
public class CompositeType extends Type
{
    /**
     * A composite is a sequence of encodedDataTypes, so we have a list of them 
     */
    private final List<EncodedDataType> compositeList;

    /**
     * A composite map that holds all the types within this composite for easy retrieval
     */
    private final Map<String, EncodedDataType> compositeMap;

    /**
     * Construct a new compositeType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public CompositeType(final Node node)
        throws XPathExpressionException
    {
        super(node); // set the common schema attributes

        compositeList = new ArrayList<EncodedDataType>();
        compositeMap = new HashMap<String, EncodedDataType>();

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("type").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            EncodedDataType t = new EncodedDataType(list.item(i));

            if (compositeMap.get(t.getName()) != null)
            {
                throw new IllegalArgumentException("composite already has type name: " + t.getName());
            }

            compositeList.add(t);
            compositeMap.put(t.getName(), t);
        }
    }

    /**
     * Return the EncodedDataType within this composite with the given name
     *
     * @param name of the EncodedDataType to return
     * @return type requested
     */
    public EncodedDataType getType(final String name)
    {
        return compositeMap.get(name);
    }

    /**
     * The size (in octets) of the list of EncodedDataTypes
     *
     * @return size of the compositeType
     */
    public int size()
    {
        int size = 0;

        for (EncodedDataType t : compositeList)
        {
            size += t.size();
        }

        return size;
    }

    /**
     * Return list of the EncodedDataTypes that compose this composite
     *
     * @return {@link java.util.List} that holds the types in this composite
     */
    public List<EncodedDataType> getTypeList()
    {
        return compositeList;
    }
    
}
