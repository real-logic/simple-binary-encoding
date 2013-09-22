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

import uk.co.real_logic.sbe.Primitive;
import uk.co.real_logic.sbe.PrimitiveValue;

import org.w3c.dom.Node;

/**
 * SBE encodedDataType
 */
public class EncodedDataType extends Type
{
    private final Primitive primitive;
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
        super(node, TypeOfType.ENCODEDDATA); // set the common schema attributes

        /**
         * Grab schema attributes
         * - primitiveType (required)
         * - length (default = 1)
         * - variableLength (default = false)
         * - nullValue (optional)
         * - minValue (optional)
         * - maxValue (optional)
         */
        this.primitive = Primitive.lookup(XmlSchemaParser.getXmlAttributeValue(node, "primitiveType"));
        this.length = Integer.parseInt(XmlSchemaParser.getXmlAttributeValue(node, "length", "1"));
        this.varLen = Boolean.parseBoolean(XmlSchemaParser.getXmlAttributeValue(node, "variableLength", "false"));

        // handle constant presence by grabbing child node and parsing it's CDATA based on primitive (save it)
        if (this.getPresence() == Presence.CONSTANT)
        {
            if (node.getFirstChild() == null)
            {
                throw new IllegalArgumentException("type has declared presence \"constant\" but XML node has no data");
            }

            this.constValue = new PrimitiveValue(this.primitive, node.getFirstChild().getNodeValue());
        }
        else
        {
            this.constValue = null; /* this value is invalid unless presence is constant */
        }

        /**
         * NullValue, MinValue, MaxValue
         * - if the schema overrides the primitives values, then save the values here. Else, we use the Primitive min/max/null
         */

        String minValueStr = XmlSchemaParser.getXmlAttributeValueOrNull(node, "minValue");
        if (minValueStr != null)
        {
            this.minValue = new PrimitiveValue(this.primitive, minValueStr);
        }
        else
        {
            this.minValue = null; /* this value is invalid unless minValue specified for type */
        }

        String maxValueStr = XmlSchemaParser.getXmlAttributeValueOrNull(node, "maxValue");
        if (maxValueStr != null)
        {
            this.maxValue = new PrimitiveValue(this.primitive, maxValueStr);
        }
        else
        {
            this.maxValue = null; /* this value is invalid unless maxValue specified for type */
        }

        String nullValueStr = XmlSchemaParser.getXmlAttributeValueOrNull(node, "nullValue");
        if (nullValueStr != null)
        {
            // nullValue is mutually exclusive with presence=required or constant
            if (this.getPresence() != Presence.OPTIONAL)
            {
                throw new IllegalArgumentException("nullValue set, but presence is not optional");
            }

            this.nullValue = new PrimitiveValue(this.primitive, nullValueStr);
        }
        else
        {
            // TODO: should we check for presence=optional and flag it? No, should default to primitive nullValue
            this.nullValue = null; /* this value is invalid unless nullValue specified for type */
        }
    }

    /**
     * Construct a new EncodedDataType with direct values. Does not handle constant values.
     *
     * @param name        of the type
     * @param presence    of the type
     * @param description of the type or null
     * @param fixUsage    of the type or null
     * @param primitive   of the EncodedDataType
     * @param length      of the EncodedDataType
     * @param varLen      of the EncodedDataType
     */
    public EncodedDataType(final String name,
                           final Presence presence,
                           final String description,
                           final FixUsage fixUsage,
                           final Primitive primitive,
                           final int length,
                           final boolean varLen)
    {
        super(name, presence, description, fixUsage, TypeOfType.ENCODEDDATA);
        this.primitive = primitive;
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
    public Primitive getPrimitiveType()
    {
        return primitive;
    }

    /**
     * The size (in octets) of the primitiveType
     *
     * @return size of the primitiveType
     */
    public int size()
    {
        if (getPresence() == Presence.CONSTANT)
        {
            return 0;
        }

        return primitive.size();
    }

    /**
     * The constant value of the type if specified
     *
     * @return value of the constant for this type
     */
    public PrimitiveValue getConstantValue()
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
     * @return value of the minValue primitive or type
     */
    public PrimitiveValue getMinValue()
    {
        if (minValue == null)
        {
            return primitive.minValue();
        }

        return minValue;
    }

    /**
     * The maxValue of the type
     *
     * @return value of the maxValue primitive or type
     */
    public PrimitiveValue getMaxValue()
    {
        if (maxValue == null)
        {
            return primitive.maxValue();
        }

        return maxValue;
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
            return primitive.nullValue();
        }

        return nullValue;
    }
}
