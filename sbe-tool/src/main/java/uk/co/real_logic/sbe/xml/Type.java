/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import org.w3c.dom.Node;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/**
 * An SBE type. One of encodedDataType, compositeType, enumType, or setType per the SBE spec.
 */
public abstract class Type
{
    private final String name;
    private final Presence presence;
    private final String description;
    private final int sinceVersion;
    private final int deprecated;
    private final String semanticType;
    private final String referencedName;

    private int offsetAttribute;

    /**
     * Construct a new Type from XML Schema. Called by subclasses to mostly set common fields
     *
     * @param node           from the XML Schema Parsing
     * @param givenName      of this node, if null then the attributed name will be used.
     * @param referencedName of the type when created from a ref in a composite.
     */
    public Type(final Node node, final String givenName, final String referencedName)
    {
        if (null == givenName)
        {
            name = getAttributeValue(node, "name");
        }
        else
        {
            name = givenName;
        }

        this.referencedName = referencedName;

        presence = Presence.get(getAttributeValue(node, "presence", "required"));
        description = getAttributeValueOrNull(node, "description");
        sinceVersion = Integer.parseInt(getAttributeValue(node, "sinceVersion", "0"));
        deprecated = Integer.parseInt(getAttributeValue(node, "deprecated", "0"));
        semanticType = getAttributeValueOrNull(node, "semanticType");
        offsetAttribute = Integer.parseInt(getAttributeValue(node, "offset", "-1"));
    }

    /**
     * Construct a new Type from direct values.
     *
     * @param name         of the type
     * @param presence     of the type
     * @param description  of the type or null
     * @param sinceVersion for the type
     * @param deprecated   version in which this was deprecated.
     * @param semanticType of the type or null
     */
    public Type(
        final String name,
        final Presence presence,
        final String description,
        final int sinceVersion,
        final int deprecated,
        final String semanticType)
    {
        this.name = name;
        this.presence = presence;
        this.description = description;
        this.sinceVersion = sinceVersion;
        this.deprecated = deprecated;
        this.semanticType = semanticType;
        this.offsetAttribute = -1;
        this.referencedName = null;
    }

    /**
     * Return the name of the type
     *
     * @return name of the Type
     */
    public String name()
    {
        return name;
    }

    /**
     * Get the name of the type field is from a reference.
     *
     * @return the name of the type field is from a reference.
     */
    public String referencedName()
    {
        return referencedName;
    }

    /**
     * Return the presence of the type
     *
     * @return presence of the Type
     */
    public Presence presence()
    {
        return presence;
    }

    /**
     * The encodedLength (in octets) of the Type.
     * <p>
     * Overridden by subtypes. This returns 0 by default.
     *
     * @return encodedLength of the type in octets
     */
    public abstract int encodedLength();

    /**
     * The description of the Type (if set) or null
     *
     * @return description set by the type or null
     */
    public String description()
    {
        return description;
    }

    /**
     * The version since this was added to the template.
     *
     * @return version since this was added to the template.
     */
    public int sinceVersion()
    {
        return sinceVersion;
    }

    /**
     * Version in which type was deprecated. Only valid if greater than zero.
     *
     * @return version in which the type was deprecated.
     */
    public int deprecated()
    {
        return deprecated;
    }

    /**
     * The semanticType of the Type
     *
     * @return semanticType of the Type if set or null if not set
     */
    public String semanticType()
    {
        return semanticType;
    }

    public abstract boolean isVariableLength();

    /**
     * Return the offset attribute of the {@link Type} from the schema
     *
     * @return the offset attribute value or -1 to indicate not set by the schema
     */
    public int offsetAttribute()
    {
        return offsetAttribute;
    }

    /**
     * Set the offset attribute of the {@link Type} from the schema
     *
     * @param offsetAttribute to set
     */
    public void offsetAttribute(final int offsetAttribute)
    {
        this.offsetAttribute = offsetAttribute;
    }
}
