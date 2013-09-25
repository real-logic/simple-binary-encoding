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

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/**
 * SBE encodedDataType
 */
public class EncodedDataType extends Type
{
    private final PrimitiveType primitiveType;
    private final int length;
    private final boolean varLen;
    private final PrimitiveValue constValue;
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;

    /**
     * Construct a new encodedDataType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EncodedDataType(final Node node)
    {
        super(node); // set the common schema attributes

        /*
         * Grab schema attributes
         * - primitiveType (required)
         * - length (default = 1)
         * - variableLength (default = false)
         * - nullValue (optional)
         * - minValue (optional)
         * - maxValue (optional)
         */
        primitiveType = PrimitiveType.lookup(getAttributeValue(node, "primitiveType"));
        length = Integer.parseInt(getAttributeValue(node, "length", "1"));
        varLen = Boolean.parseBoolean(getAttributeValue(node, "variableLength", "false"));

        // handle constant presence by grabbing child node and parsing it's CDATA based on primitiveType (save it)
        if (this.getPresence() == Presence.CONSTANT)
        {
            if (node.getFirstChild() == null)
            {
                throw new IllegalArgumentException("type has declared presence \"constant\" but XML node has no data");
            }

            constValue = new PrimitiveValue(primitiveType, node.getFirstChild().getNodeValue());
        }
        else
        {
            constValue = null; /* this value is invalid unless presence is constant */
        }

        /*
         * NullValue, MinValue, MaxValue
         * - if the schema overrides the primitives values, then save the values here. Else, we use the PrimitiveType min/max/null
         */

        String minValueStr = getAttributeValueOrNull(node, "minValue");
        if (minValueStr != null)
        {
            minValue = new PrimitiveValue(primitiveType, minValueStr);
        }
        else
        {
            minValue = null; /* this value is invalid unless minValue specified for type */
        }

        String maxValueStr = getAttributeValueOrNull(node, "maxValue");
        if (maxValueStr != null)
        {
            maxValue = new PrimitiveValue(primitiveType, maxValueStr);
        }
        else
        {
            maxValue = null; /* this value is invalid unless maxValue specified for type */
        }

        String nullValueStr = getAttributeValueOrNull(node, "nullValue");
        if (nullValueStr != null)
        {
            // nullValue is mutually exclusive with presence=required or constant
            if (getPresence() != Presence.OPTIONAL)
            {
                throw new IllegalArgumentException("nullValue set, but presence is not optional");
            }

            nullValue = new PrimitiveValue(primitiveType, nullValueStr);
        }
        else
        {
            // TODO: should we check for presence=optional and flag it? No, should default to primitiveType nullValue
            nullValue = null; // this value is invalid unless nullValue specified for type
        }
    }

    /**
     * Construct a new EncodedDataType with direct values. Does not handle constant values.
     *
     * @param name        of the type
     * @param presence    of the type
     * @param description of the type or null
     * @param fixUsage    of the type or null
     * @param primitiveType   of the EncodedDataType
     * @param length      of the EncodedDataType
     * @param varLen      of the EncodedDataType
     */
    public EncodedDataType(final String name,
                           final Presence presence,
                           final String description,
                           final FixUsage fixUsage,
                           final PrimitiveType primitiveType,
                           final int length,
                           final boolean varLen)
    {
        super(name, presence, description, fixUsage);

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
    public int getLength()
    {
        return length;
    }

    /**
     * Return the variableLength attribute of the type
     *
     * @return variableLength boolean of the type
     */
    public boolean getVariableLength()
    {
        return varLen;
    }

    /**
     * Return the primitiveType attribute of the type
     *
     * @return primitiveType attribute of the type
     */
    public PrimitiveType getPrimitiveType()
    {
        return primitiveType;
    }

    /**
     * The size (in octets) of the encoding
     *
     * @return size of the encoding
     */
    public int size()
    {
        if (getPresence() == Presence.CONSTANT)
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
    public PrimitiveValue getConstValue()
        throws IllegalArgumentException
    {
        if (getPresence() != Presence.CONSTANT)
        {
            throw new IllegalArgumentException("type is not of constant presence");
        }

        return constValue;
    }

    /**
     * The minValue of the type
     *
     * @return value of the minValue
     */
    public PrimitiveValue getMinValue()
    {
        return minValue;
    }

    /**
     * The maxValue of the type
     *
     * @return value of the maxValue
     */
    public PrimitiveValue getMaxValue()
    {
        return maxValue;
    }

    /**
     * The nullValue of the type
     *
     * @return value of the nullValue primitiveType or type
     */
    public PrimitiveValue getNullValue()
    {
        return nullValue;
    }
}
