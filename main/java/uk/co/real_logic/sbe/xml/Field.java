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
    public static final String INVALID_ID_STRING = Integer.toString(INVALID_ID);

    private final String name;                 // required for field/data & group
    private final String description;          // optional for field/data & group
    private final int id;                      // required for field/data (not present for group)
    private final Type type;                   // required for field/data (not present for group)
    private final int offset;                  // optional for field/data (not present for group)
    private final String semanticType;         // optional for field/data (not present for group?)
    private final Presence presence;           // optional for field/data (not present for group)  null means not set
    private final int blockLength;             // optional for group (not present for field/data)
    private final CompositeType dimensionType; // required for group (not present for field/data)
    private final boolean variableLength;      // true for data (false for field/group)
    private List<Field> groupFieldList;        // used by group fields as the list of child fields in the group
    private int calculatedOffset;              // used to hold the calculated offset of this field from top level <message> or <group>
    private int calculatedBlockLength;         // used to hold the calculated block length of this group

    /** Builder constructor */
    public Field(final String name,
                 final String description,
                 final String groupName,
                 final int id,
                 final Type type,
                 final int offset,
                 final String semanticType,
                 final Presence presence,
                 final int blockLength,
                 final CompositeType dimensionType,
                 final boolean variableLength)
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
        this.groupFieldList = null;
        this.calculatedOffset = 0;
        this.calculatedBlockLength = 0;
    }

    public void validate(final Node node)
    {
        if (type != null)
        {
            // must be present or must be on the type. If on both, they must agree.
            if (semanticType == null && type.getSemanticType() == null)
            {
                handleError(node, "Missing semanticType on type and field: " + name);
            }
            else if (semanticType != null && type.getSemanticType() != null && !semanticType.equals(type.getSemanticType()))
            {
                handleError(node, "Mismatched semanticType on type and field: " + name);
            }
        }
    }

    public void setGroupFields(final List<Field> list)
    {
        groupFieldList = list;
    }

    public List<Field> getGroupFields()
    {
        return groupFieldList;
    }

    public void setCalculatedOffset(final int offset)
    {
        calculatedOffset = offset;
    }

    public int getCalculatedOffset()
    {
        return calculatedOffset;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getId()
    {
        return id;
    }

    public Type getType()
    {
        return type;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getBlockLength()
    {
        return blockLength;
    }

    public void setCalculatedBlockLength(int length)
    {
        calculatedBlockLength = length;
    }

    public int getCalculatedBlockLength()
    {
        return calculatedBlockLength;
    }

    public String getSemanticType()
    {
        return semanticType;
    }

    public CompositeType getDimensionType()
    {
        return dimensionType;
    }

    public boolean getVariableLength()
    {
        return variableLength;
    }

    public String toString()
    {
        return "Field{" +
            "name=" + name +
            ", description=" + description +
            ", id=" + id +
            ", type=" + type +
            ", offset=" + offset +
            ", semanticType=" + semanticType +
            ", presence=" + presence +
            ", blockLength=" + blockLength +
            ", groupFieldList=" + groupFieldList +
            ", calculatedOffset=" + calculatedOffset +
            '}';
    }

    public static class Builder
    {
        private final String name;
        private String description;
        private String groupName;
        private int id;
        private Type type;
        private int offset;
        private String semanticType;
        private Presence presence;
        private int blockLength;
        private CompositeType dimensionType;
        private boolean variableLength;

        public Builder(final String name)
        {
            this.name = name;
            description = null;
            id = INVALID_ID;
            type = null;
            offset = 0;
            semanticType = null;
            presence = null;
            blockLength = 0;
            dimensionType = null;
            variableLength = false;
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

        public Field build()
        {
            return new Field(name, description, groupName, id, type, offset,
                             semanticType, presence, blockLength, dimensionType, variableLength);
        }
    }
}
