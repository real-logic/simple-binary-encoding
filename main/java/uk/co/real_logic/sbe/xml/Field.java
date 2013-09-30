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
    private List<Field> groupFieldList;
    private Field entryCountField;      // used by group fields as the entry count field
    private Field lengthField;          // used by data fields as the length field
    private Field groupField;           // used by entry count fields as the group field
    private Field dataField;            // used by length fields as the data field
    private long irId = INVALID_ID;     // used to identify this field by an IR ID
    private long irRefId = INVALID_ID;  // used to identify an associated field by an IR ID
    private int calculatedOffset;       // used to hold the calculated offset of this field from top level <message> or <group>
    private int calculatedBlockLength;  // used to hold the calculated block length of this group

    /** The field constructor */
    public Field(final Node node, final String name, final int id, final Type type)
    {
        this.name = name;
        this.description = getAttributeValueOrNull(node, "description");
        this.groupName = getAttributeValueOrNull(node, "groupName");
        this.id = id;
        this.type = type;
        this.offset = Integer.parseInt(getAttributeValue(node, "offset", "0"));
        this.semanticType = getMultiNamedAttributeValueOrNull(node, new String[] {"semanticType", "fixUsage"});
        this.presence = Presence.lookup(getAttributeValueOrNull(node, "presence"));
        this.refId = Integer.parseInt(getAttributeValue(node, "refId", INVALID_ID_STRING));
        this.blockLength = 0;
        this.dimensionType = null;
        this.groupFieldList = null;   // has no meaning if not group
        this.entryCountField = null;  // has no meaning if not group
        this.lengthField = null;      // will be set later
        this.groupField = null;       // will be set later
        this.dataField = null;        // will be set later
        this.calculatedOffset = 0;
        this.calculatedBlockLength = 0;

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

    /** The group constructor */
    public Field(final Node node, final String name)
    {
        this.name = name;
        this.description = XmlSchemaParser.getAttributeValueOrNull(node, "description");
        this.groupName = null;
        this.id = Integer.parseInt(getAttributeValue(node, "id", INVALID_ID_STRING));
        this.type = null;
        this.offset = 0;
        this.semanticType = null;
        this.presence = null;
        this.refId = INVALID_ID;
        this.blockLength = Integer.parseInt(getAttributeValue(node, "blockLength", "0"));
        this.dimensionType = XmlSchemaParser.getAttributeValue(node, "dimensionType", "groupSizeEncoding");
        this.groupFieldList = null;    // for now. Set later.
        this.entryCountField = null;   // for now. Set later.
        this.lengthField = null;       // has no meaning for group.
        this.groupField = null;        // has no meaning
        this.dataField = null;         // has no meaning
        this.calculatedOffset = 0;
        this.calculatedBlockLength = 0;
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
}
