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
package uk.co.real_logic.sbe;

/**
 * Class to encapsulate a field within an SBE message. This Intermediate Representation (IR)
 * is language neutral.
 * <p/>
 * Processing and optimization is run over a list of IRNodes to perform various functions
 * - ordering of fields based on size
 * - padding of fields in order to provide expansion room
 * - computing offsets of individual fields
 * - etc.
 */
public class IrNode
{
    /** constants */

    /** Size not determined */
    private final static int INVALID_SIZE = -1;

    /** Offset not computed or set */
    private final static int INVALID_OFFSET = -1;

    /** Type of field */
    private final Type type;

    /** Size of field */
    private int size;

    /** Offset of field from start of message */
    private int offset;

    /**
     * Construct an IrNode
     */
    public IrNode(final Type type)
    {
        this.type = type;
        this.size = type.size();
        this.offset = INVALID_OFFSET;
    }
}
