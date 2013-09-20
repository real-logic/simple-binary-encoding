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

    public IrGenerator()
    {
	this.irNodeList = new ArrayList<IrNode>();
	this.currentOffset = 0;
    }

    public List<IrNode> generateForMessage(Message msg)
    {
	// TODO: iterate over fields calling add()
	return null;
    }

    public List<IrNode> generateForHeader(MessageSchema msg)
    {
	// TODO: messageHeader type is passed to add
	return null;
    }

    /*
     * Probably need full Field instead of just fieldID for these
     */

    private void add(Type type, int fieldID)
    {
	// TODO: figure out which type and cast it calling add()
    }

    private void add(EncodedDataType type, int fieldId)
    {
	// TODO: lowest level. This is what creates IrNodes and adds to the IrNodeList
    }

    private void add(CompositeType type, int fieldId)
    {
	// TODO: call add(type) for all primitive in the composite
    }

}
