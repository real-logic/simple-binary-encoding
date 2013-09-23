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

import uk.co.real_logic.sbe.ir.IrNode;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to hold all the state while generating the {@link IrNode} list.
 * <p/>
 * Usage:
 * <code>
 *     <pre>
 *    irg = new IrGenerator();
 *    irg.generateForMessage(message);
 *     </pre>
 * </code>
 */
public class IrGenerator
{
    private final List<IrNode> irNodeList = new ArrayList<IrNode>();
    private int currentOffset = 0;
    private ByteOrder byteOrder;

    public List<IrNode> generateForMessage(final Message msg)
    {
        addStartOrEndNode(msg, IrNode.Flag.MESSAGE_START);

        addAllFields(msg.getFields());

        addStartOrEndNode(msg, IrNode.Flag.MESSAGE_END);

        return irNodeList;
    }

    public List<IrNode> generateForHeader(final MessageSchema schema)
    {
        byteOrder = schema.getByteOrder();
        Type type = schema.getMessageHeader();

        // short circuit conditionals would be nice... oh well
        if (type == null)
        {
            throw new IllegalArgumentException("Message header not defined for messageSchema");
        }
        else if (!(type instanceof CompositeType))
        {
            throw new IllegalArgumentException("Message header is not composite");
        }

        add((CompositeType)type, null);

        return irNodeList;
    }

    private void addStartOrEndNode(final Message msg, final IrNode.Flag flag)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(msg.getName(), msg.getId(), 0,
                                                      IrNode.Metadata.INVALID_ID, flag, msg.getDescription())));
    }

    private void addStartOrEndNode(final Type type, final IrNode.Flag flag)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(type.getName(), IrNode.Metadata.INVALID_ID, IrNode.Metadata.INVALID_ID,
                                                      IrNode.Metadata.INVALID_ID, flag, type.getDescription())));
    }

    private void addStartOrEndNode(final Message.Field field, final IrNode.Flag flag)
    {
        long xRefIrId = IrNode.Metadata.INVALID_ID;

        if (field.getEntryCountField() != null)
        {
            /* a group field */
            xRefIrId = field.getEntryCountField().getIrId();
        }
        else if (field.getLengthField() != null)
        {
            /* a data field */
            xRefIrId = field.getLengthField().getIrId();
        }
        else if (field.getGroupField() != null)
        {
            /* an entry count field for a group field */
            xRefIrId = field.getGroupField().getIrId();
        }
        else if (field.getDataField() != null)
        {
            /* a length field for a data field */
            xRefIrId = field.getDataField().getIrId();
        }

        irNodeList.add(new IrNode(new IrNode.Metadata(field.getName(), field.getId(), field.getIrId(),
                                                      xRefIrId, flag, field.getDescription())));
    }

    private void addAllFields(final List<Message.Field> fieldList)
    {
        for (final Message.Field field : fieldList)
        {
            if (field.getType() == null)
            {
                addStartOrEndNode(field, IrNode.Flag.GROUP_START);
                // add all the fields in the group
                addAllFields(field.getGroupFieldList());
                addStartOrEndNode(field, IrNode.Flag.GROUP_END);
            }
            else if (field.getType() instanceof EncodedDataType)
            {
                addStartOrEndNode(field, IrNode.Flag.FIELD_START);
                add((EncodedDataType)field.getType(), field);
                addStartOrEndNode(field, IrNode.Flag.FIELD_END);
            }
            else if (field.getType() instanceof CompositeType)
            {
                addStartOrEndNode(field, IrNode.Flag.FIELD_START);
                add((CompositeType)field.getType(), field);
                addStartOrEndNode(field, IrNode.Flag.FIELD_END);
            }
            else if (field.getType() instanceof EnumType)
            {
                addStartOrEndNode(field, IrNode.Flag.FIELD_START);
                add((EnumType)field.getType(), field);
                addStartOrEndNode(field, IrNode.Flag.FIELD_END);
            }
            else if (field.getType() instanceof SetType)
            {
                addStartOrEndNode(field, IrNode.Flag.FIELD_START);
                add((SetType)field.getType(), field);
                addStartOrEndNode(field, IrNode.Flag.FIELD_END);
            }
        }
    }

    private void add(final CompositeType type, final Message.Field field)
    {
        addStartOrEndNode(type, IrNode.Flag.STRUCT_START);

        for (EncodedDataType edt : type.getTypeList())
        {
            add(edt, field);
        }

        addStartOrEndNode(type, IrNode.Flag.STRUCT_END);
    }

    /*
     * generate IrNodes for enumerated types
     */
    private void add(final EnumType type, final Message.Field field)
    {
        PrimitiveValue nullValue = null;
        PrimitiveType encodingType = type.getEncodingType();

        addStartOrEndNode(type, IrNode.Flag.ENUM_START);

        /*
         * If presence is optional, then use nullValue specified. If not specified, then use encodingType null value
         */
        if (type.getPresence() == Presence.OPTIONAL)
        {
            nullValue = type.getNullValue();

            if (nullValue == null)
            {
                nullValue = encodingType.nullValue();
            }
        }

        IrNode.Metadata md = new IrNode.Metadata(encodingType.primitiveName(), null, null, nullValue);

        irNodeList.add(new IrNode(encodingType, encodingType.size(), currentOffset, byteOrder, md));

        /* loop over values and add each as an IrNode */
        for (Map.Entry<String, EnumType.ValidValue> entry : type.getValidValueSet())
        {
            add(entry.getValue());
        }

        addStartOrEndNode(type, IrNode.Flag.ENUM_END);

        currentOffset += encodingType.size();
    }

    /*
     * generate IrNode for ValidValue of EnumType
     */
    private void add(final EnumType.ValidValue value)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(value.getName(), value.getDescription(), value.getPrimitiveValue() , IrNode.Flag.ENUM_VALUE)));
    }

    /*
     * generate IrNodes for bitset types
     */
    private void add(final SetType type, final Message.Field field)
    {
        PrimitiveType encodingType = type.getEncodingType();

        addStartOrEndNode(type, IrNode.Flag.SET_START);

        IrNode.Metadata md = new IrNode.Metadata(encodingType.primitiveName(), null, null);

        irNodeList.add(new IrNode(encodingType, encodingType.size(), currentOffset, byteOrder, md));

        /* loop over values and add each as an IrNode */
        for (Map.Entry<String, SetType.Choice> entry : type.getChoiceSet())
        {
            add(entry.getValue());
        }

        addStartOrEndNode(type, IrNode.Flag.SET_END);

        currentOffset += encodingType.size();
    }

    /*
     * generate IrNode for Choice of SetType
     */
    private void add(final SetType.Choice value)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(value.getName(), value.getDescription(), value.getPrimitiveValue() , IrNode.Flag.SET_CHOICE)));
    }

    /*
     * generate IrNode for encoded types
     */
    private void add(final EncodedDataType type, final Message.Field field)
    {
        IrNode.Metadata md = null;

        // this might work better as a switch case
        if (type.getPresence() == Presence.REQUIRED)
        {
            md = new IrNode.Metadata(type.getName(), type.getMinValue(), type.getMaxValue());
        }
        else if (type.getPresence() == Presence.OPTIONAL)
        {
            md = new IrNode.Metadata(type.getName(), type.getMinValue(), type.getMaxValue(), type.getNullValue());
        }
        else if (type.getPresence() == Presence.CONSTANT)
        {
            md = new IrNode.Metadata(type.getName(), type.getConstantValue());
        }

        /* create and add the IrNode itself */
        irNodeList.add(new IrNode(type.getPrimitiveType(), type.size(), currentOffset, byteOrder, md));

        /* update the offset */
        currentOffset += type.size();
    }
}
