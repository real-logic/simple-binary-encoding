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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold all the state while generating the IrNode list
 * <p/>
 * usage:
 * irg = new IrGenerator()
 * irg.generateForMessage(message)
 */
public class IrGenerator
{
    private final List<IrNode> irNodeList = new ArrayList<IrNode>();
    private int currentOffset = 0;
    private ByteOrder byteOrder;
    private long irIdCursor = 1;

    public List<IrNode> generateForMessage(final Message msg)
    {
        addStartOrEndNode(msg, IrNode.Flag.MESSAGE_START);

        addAllFields(msg.getFieldList());

        addStartOrEndNode(msg, IrNode.Flag.MESSAGE_END);

        return irNodeList;
    }

    public List<IrNode> generateForHeader(final MessageSchema schema)
    {
        byteOrder = schema.getByteOrder();
        Type type = schema.getMessageHeader();

	    /* short circuit conditionals would be nice... oh well */
        if (type == null)
        {
            throw new IllegalArgumentException("Message header not defined for messageSchema");
        }
        else if (type.getTypeOfType() != Type.TypeOfType.COMPOSITE)
        {
            throw new IllegalArgumentException("Message header is not composite");
        }

        add((CompositeType)type, null);

        return irNodeList;
    }

    private long generateIrId()
    {
        return irIdCursor++;
    }

    /**
     * The Message version
     */
    private void addStartOrEndNode(final Message msg, final IrNode.Flag flag)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(msg.getName(), msg.getId(), generateIrId(), flag)));
    }

    /**
     * The Type version
     */
    private void addStartOrEndNode(final Type type, final IrNode.Flag flag)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(type.getName(), IrNode.Metadata.INVALID_ID, generateIrId(), flag)));
    }

    /**
     * The Field version
     */
    private void addStartOrEndNode(final Message.Field field, final IrNode.Flag flag)
    {
        irNodeList.add(new IrNode(new IrNode.Metadata(field.getName(), field.getId(), generateIrId(), flag)));
    }

    private void addAllFields(final List<Message.Field> fieldList)
    {
        for (final Message.Field field : fieldList)
        {
            if (field.getType() == null)
            {
                // TODO: group item START/END, MD, etc. Tying back to count field, etc.

		        /* add all the fields in the group */
                addAllFields(field.getGroupFieldList());
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

    // TODO: EnumType version
    private void add(final EnumType type, final Message.Field field)
    {

    }

    // TODO: SetType version
    private void add(final SetType type, final Message.Field field)
    {

    }

    private void add(final EncodedDataType type, final Message.Field field)
    {
        String name = type.getName();
        long id = IrNode.Metadata.INVALID_ID;

        // this might work better as a switch case
        if (type.getPresence() == Presence.REQUIRED)
        {
            irNodeList.add(new IrNode(type.getPrimitiveType(), type.size(), currentOffset,
                                      new IrNode.Metadata(name, id, IrNode.Metadata.INVALID_ID, IrNode.Flag.NONE), byteOrder));
            currentOffset += type.size();
        }
        else if (type.getPresence() == Presence.OPTIONAL)
        {
            // TODO: add nullValue info into MD

            irNodeList.add(new IrNode(type.getPrimitiveType(), type.size(), currentOffset,
                                      new IrNode.Metadata(name, id, IrNode.Metadata.INVALID_ID, IrNode.Flag.NONE), byteOrder));
            currentOffset += type.size();
        }
        else if (type.getPresence() == Presence.CONSTANT)
        {
            // TODO: add constant value info into MD, create new Flag = CONSTANT, etc.

            irNodeList.add(new IrNode(type.getPrimitiveType(), type.size(), currentOffset,
                                      new IrNode.Metadata(name, id, IrNode.Metadata.INVALID_ID, IrNode.Flag.NONE), byteOrder));

            // TODO: What about offset update? Constants are not sent. So, no size and no offset impact.
        }
    }
}
