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

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/**
 * An SBE type. One of encodedDataType, compositeType, enumType, or setType per the SBE spec.
 */
public abstract class Type
{
    /** Default presence attribute for Types */
    public static final String DEFAULT_PRESENCE = "required";

    private final String name;
    private final Presence presence;
    private final String description;
    private final String semanticType;

    /**
     * Construct a new Type from XML Schema. Called by subclasses to mostly set common fields
     *
     * @param node from the XML Schema Parsing
     */
    public Type(final Node node)
    {
        name = getAttributeValue(node, "name");
        presence = Presence.get(getAttributeValue(node, "presence", "required"));
        description = getAttributeValueOrNull(node, "description");
        semanticType = getAttributeValueOrNull(node, "semanticType");
    }

    /**
     * Construct a new Type from direct values.
     *
     * @param name of the type
     * @param presence of the type
     * @param description of the type or null
     * @param semanticType of the type or null
     */
    public Type(final String name, final Presence presence, final String description, final String semanticType)
    {
        this.name = name;
        this.presence = presence;
        this.description = description;
        this.semanticType = semanticType;
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
     * Return the presence of the type
     *
     * @return presence of the Type
     */
    public Presence presence()
    {
        return presence;
    }

    /**
     * The size (in octets) of the Type.
     *
     * Overridden by subtypes. This returns 0 by default.
     *
     * @return size of the type in octets
     */
    public abstract int size();

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
     * The semanticType of the Type
     *
     * @return semanticType of the Type if set or null if not set
     */
    public String semanticType()
    {
        return semanticType;
    }
}
