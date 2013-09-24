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

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.ir.IrNode;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold all the state while generating the {@link IrNode} list.
 * <p/>
 * Usage:
 * <code>
 *     <pre>
 *    irg = new IrGenerator();
 *    list = irg.generateForMessage(message);
 *     </pre>
 * </code>
 */
public class IrGenerator
{
    private final List<IrNode> irNodeList = new ArrayList<>();
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
        CompositeType type = schema.getMessageHeader();

        add(type, null);

        return irNodeList;
    }

    private void addStartOrEndNode(final Message msg, final IrNode.Flag flag)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(msg.getName());

        builder.setId(msg.getId());
        builder.setIrId(0);
        builder.setFlag(flag);
        builder.setFixUsage(msg.getFixMsgType());
        builder.setDescription(msg.getDescription());
        irNodeList.add(new IrNode(new IrNode.Metadata(builder)));
    }

    private void addStartOrEndNode(final Type type, final IrNode.Flag flag)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(type.getName());

        builder.setFlag(flag);

        if (type.getFixUsage() != null)
        {
            builder.setFixUsage(type.getFixUsage().getName());
        }

        builder.setDescription(type.getDescription());
        irNodeList.add(new IrNode(new IrNode.Metadata(builder)));
    }

    private void addStartOrEndNode(final Message.Field field, final IrNode.Flag flag)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(field.getName());

        if (field.getEntryCountField() != null)
        {
            /* a group field */
            builder.setXRefIrId(field.getEntryCountField().getIrId());
        }
        else if (field.getLengthField() != null)
        {
            /* a data field */
            builder.setXRefIrId(field.getLengthField().getIrId());
        }
        else if (field.getGroupField() != null)
        {
            /* an entry count field for a group field */
            builder.setXRefIrId(field.getGroupField().getIrId());
        }
        else if (field.getDataField() != null)
        {
            /* a length field for a data field */
            builder.setXRefIrId(field.getDataField().getIrId());
        }

        builder.setId(field.getId());
        builder.setIrId(field.getIrId());
        builder.setFlag(flag);
        builder.setDescription(field.getDescription());

        if (field.getFixUsage() != null)
        {
            builder.setFixUsage(field.getFixUsage().getName());
        }

        irNodeList.add(new IrNode(new IrNode.Metadata(builder)));
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

    /*
     * generate IrNodes for composite types
     */
    private void add(final CompositeType type, final Message.Field field)
    {
        addStartOrEndNode(type, IrNode.Flag.COMPOSITE_START);

        for (final EncodedDataType edt : type.getTypeList())
        {
            add(edt, field);
        }

        addStartOrEndNode(type, IrNode.Flag.COMPOSITE_END);
    }

    /*
     * generate IrNodes for enumerated types
     */
    private void add(final EnumType type, final Message.Field field)
    {
        PrimitiveValue nullValue = null;
        PrimitiveType encodingType = type.getEncodingType();
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(encodingType.primitiveName());

        addStartOrEndNode(type, IrNode.Flag.ENUM_START);

        if (type.getPresence() == Presence.OPTIONAL)
        {
            builder.setNullValue(encodingType.nullValue());
        }

        irNodeList.add(new IrNode(encodingType, encodingType.size(), currentOffset, byteOrder, new IrNode.Metadata(builder)));

        for (final EnumType.ValidValue v : type.getValidValues())
        {
            add(v);
        }

        addStartOrEndNode(type, IrNode.Flag.ENUM_END);

        currentOffset += encodingType.size();
    }

    /*
     * Generate IrNode for ValidValue of EnumType
     */
    private void add(final EnumType.ValidValue value)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(value.getName());

        builder.setFlag(IrNode.Flag.ENUM_VALUE);
        builder.setConstValue(value.getPrimitiveValue());
        builder.setDescription(value.getDescription());
        irNodeList.add(new IrNode(new IrNode.Metadata(builder)));
    }

    /*
     * Generate IrNodes for bitset types
     */
    private void add(final SetType type, final Message.Field field)
    {
        PrimitiveType encodingType = type.getEncodingType();
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(encodingType.primitiveName());

        addStartOrEndNode(type, IrNode.Flag.SET_START);

        irNodeList.add(new IrNode(encodingType, encodingType.size(), currentOffset, byteOrder, new IrNode.Metadata(builder)));

        /* loop over values and add each as an IrNode */
        for (final SetType.Choice choice : type.getChoices())
        {
            add(choice);
        }

        addStartOrEndNode(type, IrNode.Flag.SET_END);

        currentOffset += encodingType.size();
    }

    /*
     * generate IrNode for Choice of SetType
     */
    private void add(final SetType.Choice value)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(value.getName());

        builder.setFlag(IrNode.Flag.SET_CHOICE);
        builder.setConstValue(value.getPrimitiveValue());
        builder.setDescription(value.getDescription());
        irNodeList.add(new IrNode(new IrNode.Metadata(builder)));
    }

    /*
     * generate IrNode for encoded types
     */
    private void add(final EncodedDataType type, final Message.Field field)
    {
        IrNode.Metadata.Builder builder = new IrNode.Metadata.Builder(type.getName());

        // this might work better as a switch case
        if (type.getPresence() == Presence.REQUIRED)
        {
            builder.setMinValue(type.getMinValue());
            builder.setMaxValue(type.getMaxValue());
        }
        else if (type.getPresence() == Presence.OPTIONAL)
        {
            builder.setMinValue(type.getMinValue());
            builder.setMaxValue(type.getMaxValue());
            builder.setNullValue(type.getNullValue());
        }
        else if (type.getPresence() == Presence.CONSTANT)
        {
            builder.setConstValue(type.getConstantValue());
        }

        irNodeList.add(new IrNode(type.getPrimitiveType(), type.size(), currentOffset, byteOrder, new IrNode.Metadata(builder)));

        currentOffset += type.size();
    }
}
