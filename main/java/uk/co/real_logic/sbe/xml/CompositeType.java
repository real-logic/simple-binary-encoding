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

/**
 * SBE compositeType.
 * <p>
 * Decimal types can use mantissa and exponent portions as min/max/null.
 * <table>
 *     <thead>
 *         <tr>
 *             <th></th>
 *             <th></th>
 *             <th>Length</th>
 *             <th>Exponent</th>
 *             <th>Min</th>
 *             <th>Max</th>
 *             <th>Null</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>decimal</td>
 *             <td></td>
 *             <td>9</td>
 *             <td>-128 to 127</td>
 *             <td></td>
 *             <td></td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>int64 mantissa</td>
 *             <td></td>
 *             <td></td>
 *             <td>-2^63 + 1</td>
 *             <td>2^64(2^63 - 1)</td>
 *             <td>-2^63</td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>int8 exponent</td>
 *             <td></td>
 *             <td></td>
 *             <td>10^127</td>
 *             <td>10^-128</td>
 *             <td>10^127</td>
 *         </tr>
 *         <tr>
 *             <td>decimal64</td>
 *             <td></td>
 *             <td>8</td>
 *             <td>-128 to 127</td>
 *             <td></td>
 *             <td></td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>int64 mantissa</td>
 *             <td></td>
 *             <td></td>
 *             <td>-2^63 + 1</td>
 *             <td>2^64(2^63 - 1)</td>
 *             <td>-2^63</td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>constant exponent</td>
 *             <td></td>
 *             <td></td>
 *             <td>10^127</td>
 *             <td>10^-128</td>
 *             <td>10^127</td>
 *         </tr>
 *         <tr>
 *             <td>decimal32</td>
 *             <td></td>
 *             <td>4</td>
 *             <td>-128 to 127</td>
 *             <td></td>
 *             <td></td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>int32 mantissa</td>
 *             <td></td>
 *             <td></td>
 *             <td>-2^31 + 1</td>
 *             <td>2^32(2^31 - 1)</td>
 *             <td>-2^63</td>
 *         </tr>
 *         <tr>
 *             <td></td>
 *             <td>constant exponent</td>
 *             <td></td>
 *             <td></td>
 *             <td>10^127</td>
 *             <td>10^-128</td>
 *             <td>10^127</td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public class CompositeType extends Type
{
    private final List<EncodedDataType> compositeList = new ArrayList<>();
    private final Map<String, EncodedDataType> compositeMap = new HashMap<>();

    /**
     * Construct a new compositeType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public CompositeType(final Node node)
        throws XPathExpressionException
    {
        super(node);

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("type").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            EncodedDataType type = new EncodedDataType(list.item(i));

            if (compositeMap.get(type.getName()) != null)
            {
                XmlSchemaParser.handleError(node, "composite already has type name: " + type.getName());
            }
            else
            {
                compositeList.add(type);
                compositeMap.put(type.getName(), type);
            }
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

        for (final EncodedDataType t : compositeList)
        {
            if (t.getVariableLength())
            {
                return Token.VARIABLE_SIZE;
            }

            size += t.size();
        }

        return size;
    }

    /**
     * Return list of the EncodedDataTypes that compose this composite
     *
     * @return {@link List} that holds the types in this composite
     */
    public List<EncodedDataType> getTypeList()
    {
        return compositeList;
    }

    /**
     * Make this composite type, if it has a varData member, variable length
     * by making the EncodedDataType with the name "varData" be variable length.
     */
    public void makeDataFieldCompositeType()
    {
        EncodedDataType edt = compositeMap.get("varData");

        if (edt != null)
        {
            edt.setVariableLength(true);
        }
    }

    /**
     * Check the composite for being a well formed group size encoding. This means
     * that there are the fields "blockLength" and "numInGroup" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedGroupSizeEncoding(final Node node)
    {
        if (compositeMap.get("blockLength") == null)
        {
            XmlSchemaParser.handleError(node, "composite for group size encoding must have \"blockLength\"");
        }
        if (compositeMap.get("numInGroup") == null)
        {
            XmlSchemaParser.handleError(node, "composite for group size encoding must have \"numInGroup\"");
        }
    }

    /**
     * Check the composite for being a well formed variable length data encoding. This means
     * that there are the fields "length" and "varData" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedVariableLengthDataEncoding(final Node node)
    {
        if (compositeMap.get("length") == null)
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding must have \"length\"");
        }
        if (compositeMap.get("varData") == null)
        {
            XmlSchemaParser.handleError(node, "composite for variable length data encoding must have \"varData\"");
        }
    }

    /**
     * Check the composite for being a well formed message header encoding. This means
     * that there are the fields "blockLength", "templateId" and "version" present.
     *
     * @param node of the XML for this composite
     */
    public void checkForWellFormedMessageHeader(final Node node)
    {
        if (compositeMap.get("blockLength") == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"blockLength\"");
        }
        if (compositeMap.get("templateId") == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"templateId\"");
        }
        if (compositeMap.get("version") == null)
        {
            XmlSchemaParser.handleError(node, "composite for message header must have \"version\"");
        }
    }
}
