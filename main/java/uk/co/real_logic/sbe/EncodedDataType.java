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

import org.w3c.dom.Node;

/**
 * SBE encodedDataType
 */
public class EncodedDataType extends Type
{
    /**
     * The primitiveType this Type encodes as
     */
    private final Primitive primitive;

    /**
     * Number of elements of the primitive type
     */
    private final int length;

    /**
     * variable length or not
     */
    private final boolean varLen;

    /**
     * value of constant if used
     */
    private final int constValue;

    /**
     * Construct a new encodedDataType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EncodedDataType(final Node node)
    {
        super(node); // set the common schema attributes

        /**
         * Grab schema attributes
         * - primitiveType (required)
         * - length (default = 1)
         * - variableLength (default = false)
         *
         * TODO:
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

            this.constValue = Primitive.parseConstValue2Int(this.primitive, node.getFirstChild().getNodeValue());
        }
        else
        {
            this.constValue = 0; /** this value is invalid unless presence is constant */
        }

        // TODO: handle nullValue (mutually exclusive with presence of required and optional), minValue, and maxValue
        /**
         * NullValue, MinValue, MaxValue
         * - if the schema overrides the primitives values, then it sets a flag and fills the value for null/min/max
         */
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
        super(name, presence, description, fixUsage);
        this.primitive = primitive;
        this.length = length;
        this.varLen = varLen;
        this.constValue = 0;
        // TODO: add nullValue, minValue, maxValue
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
        return primitive.size();
    }

    /**
     * The constant value of the type (if )
     *
     * @return value of the constant for this type
     */
    public int getConstantValue()
        throws IllegalArgumentException
    {
        if (getPresence() != Presence.CONSTANT)
        {
            throw new IllegalArgumentException("type is not of constant presence");
        }

        return constValue;
    }
}
