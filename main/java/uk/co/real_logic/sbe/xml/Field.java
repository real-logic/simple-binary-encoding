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

    private final String name;          // required for field/data & group
    private final String description;   // optional for field/data & group
    private final String groupName;     // optional for field/date (not present for group)
    private final int id;               // required for field/data (not present for group)
    private final Type type;            // required for field/data (not present for group)
    private final int offset;           // optional for field/data (not present for group)
    private final String semanticType;  // optional for field/data (not present for group?)
    private final Presence presence;    // optional for field/data (not present for group)  null means not set
    private final int refId;            // optional for field (not present for group or data) INVALID_ID means not set
    private final int blockLength;      // optional for group (not present for field/data)
    private final String dimensionType; // required for group (not present for field/data) - has default
    private List<Field> groupFieldList; // used by group fields as the list of child fields in the group
    private Field entryCountField;      // used by group fields as the entry count field
    private Field lengthField;          // used by data fields as the length field
    private Field groupField;           // used by entry count fields as the group field
    private Field dataField;            // used by length fields as the data field
    private long irId = INVALID_ID;     // used to identify this field by an IR ID
    private long irRefId = INVALID_ID;  // used to identify an associated field by an IR ID
    private int calculatedOffset;       // used to hold the calculated offset of this field from top level <message> or <group>
    private int calculatedBlockLength;  // used to hold the calculated block length of this group

    /** Builder constructor */
    public Field(final String name,
                 final String description,
                 final String groupName,
                 final int id,
                 final Type type,
                 final int offset,
                 final String semanticType,
                 final Presence presence,
                 final int refId,
                 final int blockLength,
                 final String dimensionType)
    {
        this.name = name;
        this.description = description;
        this.groupName = groupName;
        this.id = id;
        this.type = type;
        this.offset = offset;
        this.semanticType = semanticType;
        this.presence = presence;
        this.refId = refId;
        this.blockLength = blockLength;
        this.dimensionType = dimensionType;
        this.groupFieldList = null;
        this.entryCountField = null;
        this.lengthField = null;
        this.groupField = null;
        this.dataField = null;
        this.calculatedOffset = 0;
        this.calculatedBlockLength = 0;
    }

    public void validate(final Node node)
    {
        if (type != null)
        {
            // fixUsage must be present or must be on the type. If on both, they must agree.
            if (semanticType == null && type.getSemanticType() == null)
            {
                handleError(node, "Missing semanticType/fixUsage on type and field: " + name);
            }
            else if (semanticType != null && type.getSemanticType() != null && !semanticType.equals(type.getSemanticType()))
            {
                handleError(node, "Mismatched semanticType/fixUsage on type and field: " + name);
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

    public void setEntryCountField(final Field field)
    {
        entryCountField = field;
    }

    public Field getEntryCountField()
    {
        return entryCountField;
    }

    public void setLengthField(final Field field)
    {
        lengthField = field;
    }

    public Field getLengthField()
    {
        return lengthField;
    }

    public void setGroupField(final Field field)
    {
        groupField = field;
    }

    public Field getGroupField()
    {
        return groupField;
    }

    public void setDataField(final Field field)
    {
        dataField = field;
    }

    public Field getDataField()
    {
        return dataField;
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

    public String getGroupName()
    {
        return groupName;
    }

    public int getId()
    {
        return id;
    }

    public int getRefId()
    {
        return refId;
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

    public void setIrId(final long id)
    {
        irId = id;
    }

    public long getIrId()
    {
        return irId;
    }

    public void setIrRefId(final long id)
    {
        irRefId = id;
    }

    public long getIrRefId()
    {
        return irRefId;
    }

    public String getSemanticType()
    {
        return semanticType;
    }

    public String getDimensionType()
    {
        return dimensionType;
    }

    public String toString()
    {
        return "Field{" +
            "name=" + name +
            ", description=" + description +
            ", groupName=" + groupName +
            ", id=" + id +
            ", type=" + type +
            ", offset=" + offset +
            ", semanticType=" + semanticType +
            ", presence=" + presence +
            ", refId=" + refId +
            ", blockLength=" + blockLength +
            ", groupFieldList=" + groupFieldList +
            ", entryCountField=" + entryCountField +
            ", lengthField=" + lengthField +
            ", groupField=" + groupField +
            ", dataField=" + dataField +
            ", irId=" + irId +
            ", irRefId =" + irRefId +
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
        private int refId;
        private int blockLength;
        private String dimensionType;

        public Builder(final String name)
        {
            this.name = name;
            description = null;
            groupName = null;
            id = INVALID_ID;
            type = null;
            offset = 0;
            semanticType = null;
            presence = null;
            refId = INVALID_ID;
            blockLength = 0;
            dimensionType = null;
        }

        public void description(final String description)
        {
            this.description = description;
        }

        public void groupName(final String groupName)
        {
            this.groupName = groupName;
        }

        public void id(final int id)
        {
            this.id = id;
        }

        public void type(final Type type)
        {
            this.type = type;
        }

        public void offset(final int offset)
        {
            this.offset = offset;
        }

        public void semanticType(final String semanticType)
        {
            this.semanticType = semanticType;
        }

        public void presence(final Presence presence)
        {
            this.presence = presence;
        }

        public void refId(final int refId)
        {
            this.refId = refId;
        }

        public void blockLength(final int blockLength)
        {
            this.blockLength = blockLength;
        }

        public void dimensionType(final String dimensionType)
        {
            this.dimensionType = dimensionType;
        }

        public Field build()
        {
            return new Field(name, description, groupName, id, type, offset, semanticType, presence, refId, blockLength, dimensionType);
        }
    }
}
