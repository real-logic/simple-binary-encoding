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

import java.util.List;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

/**
 * Holder for Field (or Group or Data) information
 */
public class Field
{
    public static final int INVALID_ID = Integer.MAX_VALUE;  // schemaId must only be short, so this is way out of range.

    private final String name;                 // required for field/data & group
    private final String description;          // optional for field/data & group
    private final int id;                      // required for field/data (not present for group)
    private final Type type;                   // required for field/data (not present for group)
    private final int offset;                  // optional for field/data (not present for group)
    private final String semanticType;         // optional for field/data (not present for group?)
    private final Presence presence;           // optional, defaults to required
    private final int blockLength;             // optional for group (not present for field/data)
    private final CompositeType dimensionType; // required for group (not present for field/data)
    private final boolean variableLength;      // true for data (false for field/group)
    private final int sinceVersion;            // optional
    private List<Field> groupFieldList;        // used by group fields as the list of child fields in the group
    private int computedOffset;                // holds the calculated offset of this field from top level <message> or <group>
    private int computedBlockLength;           // used to hold the calculated block length of this group
    private final String epoch;                // optional, epoch from which a timestamps start, defaults to "unix"
    private final String timeUnit;             // optional, defaults to "nanosecond".

    public Field(
        final String name,
        final String description,
        final int id,
        final Type type,
        final int offset,
        final String semanticType,
        final Presence presence,
        final int blockLength,
        final CompositeType dimensionType,
        final boolean variableLength,
        final int sinceVersion,
        final String epoch,
        final String timeUnit)
    {
        this.name = name;
        this.description = description;
        this.id = id;
        this.type = type;
        this.offset = offset;
        this.semanticType = semanticType;
        this.presence = presence;
        this.blockLength = blockLength;
        this.dimensionType = dimensionType;
        this.variableLength = variableLength;
        this.sinceVersion = sinceVersion;
        this.groupFieldList = null;
        this.computedOffset = 0;
        this.computedBlockLength = 0;
        this.epoch = epoch;
        this.timeUnit = timeUnit;
    }

    public void validate(final Node node)
    {
        if (type != null)
        {
            if (semanticType != null && type.semanticType() != null && !semanticType.equals(type.semanticType()))
            {
                handleError(node, "Mismatched semanticType on type and field: " + name);
            }
        }

        checkForValidName(node, name);
    }

    public void groupFields(final List<Field> fields)
    {
        groupFieldList = fields;
    }

    public List<Field> groupFields()
    {
        return groupFieldList;
    }

    public void computedOffset(final int offset)
    {
        computedOffset = offset;
    }

    public int computedOffset()
    {
        return computedOffset;
    }

    public String name()
    {
        return name;
    }

    public String description()
    {
        return description;
    }

    public int id()
    {
        return id;
    }

    public Type type()
    {
        return type;
    }

    public int offset()
    {
        return offset;
    }

    public int blockLength()
    {
        return blockLength;
    }

    public void computedBlockLength(int length)
    {
        computedBlockLength = length;
    }

    public int computedBlockLength()
    {
        return computedBlockLength;
    }

    public String semanticType()
    {
        return semanticType;
    }

    public CompositeType dimensionType()
    {
        return dimensionType;
    }

    public boolean isVariableLength()
    {
        return variableLength;
    }

    public int sinceVersion()
    {
        return sinceVersion;
    }

    public String epoch()
    {
        return epoch;
    }

    public String timeUnit()
    {
        return timeUnit;
    }

    public String toString()
    {
        return "Field{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", id=" + id +
            ", type=" + type +
            ", offset=" + offset +
            ", semanticType='" + semanticType + '\'' +
            ", presence=" + presence +
            ", blockLength=" + blockLength +
            ", dimensionType=" + dimensionType +
            ", variableLength=" + variableLength +
            ", sinceVersion=" + sinceVersion +
            ", groupFieldList=" + groupFieldList +
            ", computedOffset=" + computedOffset +
            ", computedBlockLength=" + computedBlockLength +
            ", epoch='" + epoch + '\'' +
            ", timeUnit=" + timeUnit +
            '}';
    }

    public static class Builder
    {
        private String name;
        private String description;
        private int id = INVALID_ID;
        private Type type;
        private int offset;
        private String semanticType;
        private Presence presence;
        private int blockLength;
        private CompositeType dimensionType;
        private boolean variableLength;
        private int sinceVersion = 0;
        private String epoch;
        private String timeUnit;

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder id(final int id)
        {
            this.id = id;
            return this;
        }

        public Builder type(final Type type)
        {
            this.type = type;
            return this;
        }

        public Builder offset(final int offset)
        {
            this.offset = offset;
            return this;
        }

        public Builder semanticType(final String semanticType)
        {
            this.semanticType = semanticType;
            return this;
        }

        public Builder presence(final Presence presence)
        {
            this.presence = presence;
            return this;
        }

        public Builder blockLength(final int blockLength)
        {
            this.blockLength = blockLength;
            return this;
        }

        public Builder dimensionType(final CompositeType dimensionType)
        {
            this.dimensionType = dimensionType;
            return this;
        }

        public Builder variableLength(final boolean variableLength)
        {
            this.variableLength = variableLength;
            return this;
        }

        public Builder sinceVersion(final int sinceVersion)
        {
            this.sinceVersion = sinceVersion;
            return this;
        }

        public Builder epoch(final String epoch)
        {
            this.epoch = epoch;
            return this;
        }

        public Builder timeUnit(final String timeUnit)
        {
            this.timeUnit = timeUnit;
            return this;
        }

        public Field build()
        {
            return new Field(
                name,
                description,
                id,
                type,
                offset,
                semanticType,
                presence,
                blockLength,
                dimensionType,
                variableLength,
                sinceVersion,
                epoch,
                timeUnit);
        }
    }
}
