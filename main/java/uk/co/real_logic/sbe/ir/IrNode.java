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
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.Primitive;

import java.nio.ByteOrder;

/**
 * Class to encapsulate an atom of data. This Intermediate Representation (IR)
 * is language, schema, platform independent.
 * <p>
 * Processing and optimization could be run over a list of IrNodes to perform various functions
 * - ordering of fields based on size
 * - padding of fields in order to provide expansion room
 * - computing offsets of individual fields
 * - etc.
 */
public class IrNode
{
    /** Size not determined */
    public static final int VARIABLE_SIZE = -1;

    /** Offset not computed or set */
    public static final int UNKNOWN_OFFSET = -1;

    /** how to encode field */
    private final Primitive primitiveType;

    /** Size of field */
    private final int size;

    /** Offset of field from start of buffer */
    private final int offset;

    /** Meta Data associated with node */
    private final MetaData metaData;

    /** byteOrder of the data */
    private final ByteOrder byteOrder;

    /**
     * Construct an IrNode
     */
    public IrNode(final Primitive primitiveType, 
                  final int size, 
                  final int offset, 
                  final MetaData metaData,
                  final ByteOrder byteOrder)
    {
        this.primitiveType = primitiveType;
        this.size = size;
        this.offset = offset;
        this.metaData = metaData;
        this.byteOrder = byteOrder;
    }

    public IrNode(final MetaData metaData)
    {
        this.primitiveType = null;
        this.size = 0;
        this.offset = 0;
        this.metaData = metaData;
        this.byteOrder = null;
    }

    public Primitive getPrimitive()
    {
        return primitiveType;
    }

    public int size()
    {
        return size;
    }

    public int getOffset()
    {
        return offset;
    }

    public MetaData getMetaData()
    {
        return metaData;
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    public enum Flag
    {
        MESSAGE_START,
        MESSAGE_END,
        STRUCT_START,
        STRUCT_END,
        FIELD_START,
        FIELD_END,
        GROUP_START,
        GROUP_END,
        NONE
    }

    /**
     * class to encapsulate IrNode metadata
     */
    public static class MetaData
    {
        public static final long INVALID_ID = Long.MAX_VALUE;

        private final String name;
        private final long id;
        private final Flag flag;
        
        public MetaData(final String name, final long id, final Flag flag)
        {
            this.name = name;
            this.id = id;
            this.flag = flag;
        }

        public String getName()
        {
            return name;
        }

        public long getId()
        {
            return id;
        }

        public Flag getFlag()
        {
            return flag;
        }
    }
}
