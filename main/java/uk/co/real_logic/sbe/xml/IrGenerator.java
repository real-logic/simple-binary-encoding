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
import uk.co.real_logic.sbe.Primitive;
import uk.co.real_logic.sbe.PrimitiveValue;

import java.nio.ByteOrder;

import java.util.List;
import java.util.ArrayList;

/**
 * Class to hold all the state while generating the IrNode list
 *
 * usage:
 *   irg = new IrGenerator()
 *   irg.generateForMessage(message)
 */
public class IrGenerator
{
    private List<IrNode> irNodeList;
    private int currentOffset;
    private ByteOrder byteOrder;

    public IrGenerator()
    {
	this.irNodeList = new ArrayList<IrNode>();
	this.currentOffset = 0;
    }

    public List<IrNode> generateForMessage(Message msg)
    {
	/* add all the fields */
	addAllFields(msg.getFieldList());

	/* return IR */
	return irNodeList;
    }

    public List<IrNode> generateForHeader(MessageSchema schema)
    {
	byteOrder = schema.getByteOrder();
	Type type = schema.getMessageHeader();

	/* short circuit conditionals would be nice... oh well */
	if (type == null)
	{
	    throw new IllegalArgumentException("messageHeader not defined for messageSchema");
	}
	else if (type.getTypeOfType() != Type.TypeOfType.COMPOSITE)
	{
	    throw new IllegalArgumentException("messageHeader is not composite");
	}

	/* add entire composite */
	add((CompositeType)type, null);

	/* return IR */
	return irNodeList;
    }

    /**
     * The Type version
     */
    private void addStartOrEndNode(final Type type, final IrNode.Flag flag)
    {
	irNodeList.add(new IrNode(new IrNode.MetaData(type.getName(), IrNode.MetaData.INVALID_ID, flag)));
    }

    /**
     * The Field version
     */
    private void addStartOrEndNode(final Message.Field field, final IrNode.Flag flag)
    {
	irNodeList.add(new IrNode(new IrNode.MetaData(field.getName(), field.getId(), flag)));
    }

    private void addAllFields(List<Message.Field> fieldList)
    {
	for (Message.Field field : fieldList)
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
	// add START node
	addStartOrEndNode(type, IrNode.Flag.STRUCT_START);

	/* iterate over EncodedDataTypes */
	for (EncodedDataType edt : type.getTypeList())
	{
	    add(edt, field);
	}

	// add END node
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
	IrNode.MetaData md;
	IrNode node;
	String name = type.getName();
	int id = IrNode.MetaData.INVALID_ID;

	// this might work better as a switch case
	if (type.getPresence() == Presence.REQUIRED)
	{
	    md = new IrNode.MetaData(name, id, IrNode.Flag.NONE);
	    node = new IrNode(type.getPrimitiveType(), type.size(), currentOffset, md, byteOrder);

	    /* add node */
	    irNodeList.add(node);

	    /* update offset */
	    currentOffset += type.size();
	}
	else if (type.getPresence() == Presence.OPTIONAL)
	{
	    // TODO: add nullValue info into MD
	    md = new IrNode.MetaData(name, id, IrNode.Flag.NONE);
	    node = new IrNode(type.getPrimitiveType(), type.size(), currentOffset, md, byteOrder);

	    /* add node */
	    irNodeList.add(node);

	    /* update offset */
	    currentOffset += type.size();
	}
	else if (type.getPresence() == Presence.CONSTANT)
	{
	    // TODO: add constant value info into MD, create new Flag = CONSTANT, etc.
	    md = new IrNode.MetaData(name, id, IrNode.Flag.NONE);
	    node = new IrNode(type.getPrimitiveType(), type.size(), currentOffset, md, byteOrder);

	    /* add node */
	    irNodeList.add(node);
	}

    }

}
