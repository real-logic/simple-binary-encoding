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
    private final String characterEncoding;
    private final int sinceVersion;
    private final int offsetAttribute;
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
        characterEncoding = getAttributeValue(node, "characterEncoding", "UTF-8");
        sinceVersion = Integer.parseInt(getAttributeValue(node, "sinceVersion", "0"));
        offsetAttribute = Integer.parseInt(getAttributeValue(node, "offset", "-1"));

        if (presence() == Presence.CONSTANT)
        {
            if (node.getFirstChild() == null)
            {
                handleError(node, "type has declared presence as \"constant\" but XML node has no data");
                constValue = null;
            }
            else
            {
                final String nodeValue = node.getFirstChild().getNodeValue();
                if (PrimitiveType.CHAR == primitiveType)
                {
                    if (nodeValue.length() == 1)
                    {
                        constValue = PrimitiveValue.parse(nodeValue, primitiveType);
                    }
                    else
                    {
                        constValue = PrimitiveValue.parse(nodeValue, primitiveType, nodeValue.length(), characterEncoding);
                    }
                }
                else
                {
                    constValue = PrimitiveValue.parse(nodeValue, primitiveType);
                }
            }
        }
        else
        {
            constValue = null;
        }

        final String minValStr = getAttributeValueOrNull(node, "minValue");
        minValue = minValStr != null ? PrimitiveValue.parse(minValStr, primitiveType) : null;

        final String maxValStr = getAttributeValueOrNull(node, "maxValue");
        maxValue = maxValStr != null ? PrimitiveValue.parse(maxValStr, primitiveType) : null;

        final String nullValStr = getAttributeValueOrNull(node, "nullValue");
        if (nullValStr != null)
        {
            if (presence() != Presence.OPTIONAL)
            {
                handleWarning(node, "nullValue set, but presence is not optional");
            }

            nullValue = PrimitiveValue.parse(nullValStr, primitiveType);
        }
        else
        {
            nullValue = null;
        }
    }

    /**
     * Construct a new EncodedDataType with direct values. Does not handle constant values.
     *
     * @param name          of the type
     * @param presence      of the type
     * @param description   of the type or null
     * @param semanticType  of the type or null
     * @param primitiveType of the EncodedDataType
     * @param length        of the EncodedDataType
     * @param varLen        of the EncodedDataType
     */
    public EncodedDataType(
        final String name,
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
        characterEncoding = null;
        sinceVersion = 0;
        offsetAttribute = -1;
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

        return primitiveType.size() * length;
    }

    /**
     * The constant value of the type if specified
     *
     * @return value of the constant for this type
     */
    public PrimitiveValue constVal()
        throws IllegalArgumentException
    {
        if (presence() != Presence.CONSTANT)
        {
            throw new IllegalStateException("type is not of constant presence");
        }

        return constValue;
    }

    /**
     * The minValue of the type
     *
     * @return value of the minValue
     */
    public PrimitiveValue minValue()
    {
        return minValue;
    }

    /**
     * The maxValue of the type
     *
     * @return value of the maxValue
     */
    public PrimitiveValue maxValue()
    {
        return maxValue;
    }

    /**
     * The nullValue of the type
     *
     * @return value of the nullValue primitiveType or type
     */
    public PrimitiveValue nullValue()
    {
        return nullValue;
    }

    /**
     * The character encoding of the type
     *
     * @return value representing the encoding
     */
    public String characterEncoding()
    {
        return characterEncoding;
    }

    /**
     * Return the sinceVersion of the {@link EncodedDataType}
     *
     * @return the sinceVersion value of the {@link EncodedDataType}
     */
    public int sinceVersion()
    {
        return sinceVersion;
    }

    /**
     * Return the offset attribute of the {@link EncodedDataType} from the schema
     *
     * @return the offset attribute value or -1 to indicate not set by the schema
     */
    public int offsetAttribute()
    {
        return offsetAttribute;
    }
}
