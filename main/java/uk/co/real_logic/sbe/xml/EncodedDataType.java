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
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;

import org.w3c.dom.Node;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.handleError;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.handleWarning;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/**
 * SBE encodedDataType
 */
public class EncodedDataType extends Type
{
    private final PrimitiveType primitiveType;
    private final int length;
    private final PrimitiveValue constValue;
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;
    private boolean varLen;

    /**
     * Construct a new encodedDataType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EncodedDataType(final Node node)
    {
        super(node);

        primitiveType = PrimitiveType.get(getAttributeValue(node, "primitiveType"));
        length = Integer.parseInt(getAttributeValue(node, "length", "1"));
        varLen = Boolean.parseBoolean(getAttributeValue(node, "variableLength", "false"));

        // handle constant presence by grabbing child node and parsing it's CDATA based on primitiveType (save it)
        if (this.presence() == Presence.CONSTANT)
        {
            if (node.getFirstChild() == null)
            {
                handleError(node, "type has declared presence \"constant\" but XML node has no data");
                constValue = null;
            }
            else
            {
                constValue = PrimitiveValue.parse(node.getFirstChild().getNodeValue(), primitiveType);
            }
        }
        else
        {
            constValue = null;
        }

        String minValueStr = getAttributeValueOrNull(node, "minVal");
        if (minValueStr != null)
        {
            minValue = PrimitiveValue.parse(minValueStr, primitiveType);
        }
        else
        {
            minValue = null;
        }

        String maxValueStr = getAttributeValueOrNull(node, "maxVal");
        if (maxValueStr != null)
        {
            maxValue = PrimitiveValue.parse(maxValueStr, primitiveType);
        }
        else
        {
            maxValue = null;
        }

        String nullValueStr = getAttributeValueOrNull(node, "nullVal");
        if (nullValueStr != null)
        {
            if (presence() != Presence.OPTIONAL)
            {
                handleWarning(node, "nullVal set, but presence is not optional");
            }

            nullValue = PrimitiveValue.parse(nullValueStr, primitiveType);
        }
        else
        {
            // TODO: should we check for presence=optional and flag it? No, should default to primitiveType nullVal
            nullValue = null; // this value is invalid unless nullVal specified for type
        }
    }

    /**
     * Construct a new EncodedDataType with direct values. Does not handle constant values.
     *
     * @param name        of the type
     * @param presence    of the type
     * @param description of the type or null
     * @param semanticType    of the type or null
     * @param primitiveType   of the EncodedDataType
     * @param length      of the EncodedDataType
     * @param varLen      of the EncodedDataType
     */
    public EncodedDataType(final String name,
                           final Presence presence,
                           final String description,
                           final String semanticType,
                           final PrimitiveType primitiveType,
                           final int length,
                           final boolean varLen)
    {
        super(name, presence, description, semanticType);

        this.primitiveType = primitiveType;
        this.length = length;
        this.varLen = varLen;
        this.constValue = null;
        this.minValue = null;
        this.maxValue = null;
        this.nullValue = null;
    }

    /**
     * Return the length attribute of the type
     *
     * @return length attribute of the type
     */
    public int length()
    {
        return length;
    }

    /**
     * Return the variableLength attribute of the type
     *
     * @return variableLength boolean of the type
     */
    public boolean isVariableLength()
    {
        return varLen;
    }

    public void variableLength(final boolean variableLength)
    {
        this.varLen = variableLength;
    }

    /**
     * Return the primitiveType attribute of the type
     *
     * @return primitiveType attribute of the type
     */
    public PrimitiveType primitiveType()
    {
        return primitiveType;
    }

    /**
     * The size (in octets) of the encoding as length of the primitiveType times its count.
     *
     * @return size of the encoding
     */
    public int size()
    {
        if (presence() == Presence.CONSTANT)
        {
            return 0;
        }

        if (varLen)
        {
            return Token.VARIABLE_SIZE;
        }

        return (primitiveType.size() * length);
    }

    /**
     * The constant value of the type if specified
     *
     * @return value of the constant for this type
     */
    public PrimitiveValue constValue()
        throws IllegalArgumentException
    {
        if (presence() != Presence.CONSTANT)
        {
            throw new IllegalStateException("type is not of constant presence");
        }

        return constValue;
    }

    /**
     * The minVal of the type
     *
     * @return value of the minVal
     */
    public PrimitiveValue minValue()
    {
        return minValue;
    }

    /**
     * The maxVal of the type
     *
     * @return value of the maxVal
     */
    public PrimitiveValue maxValue()
    {
        return maxValue;
    }

    /**
     * The nullVal of the type
     *
     * @return value of the nullVal primitiveType or type
     */
    public PrimitiveValue nullValue()
    {
        return nullValue;
    }
}
