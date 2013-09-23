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

import uk.co.real_logic.sbe.PrimitiveType;

import java.nio.ByteOrder;

/**
 * Class to encapsulate an atom of data. This Intermediate Representation (IR)
 * is language, schema, platform independent.
 * <p/>
 * Processing and optimization could be run over a list of IrNodes to perform various functions
 * <ul>
 *     <li>ordering of fields based on size</li>
 *     <li>padding of fields in order to provide expansion room</li>
 *     <li>computing offsets of individual fields</li>
 *     <li>etc.</li>
 * </ul>
 */
public class IrNode
{
    /** Size not determined */
    public static final int VARIABLE_SIZE = -1;

    /** Offset not computed or set */
    public static final int UNKNOWN_OFFSET = -1;

    private final PrimitiveType primitiveType;
    private final int size;
    private final int offset;
    private final Metadata metadata;
    private final ByteOrder byteOrder;

    /**
     * Construct an {@link IrNode} by providing values for all fields.
     *
     * @param primitiveType representing this node.
     * @param size          of the node in bytes.
     * @param offset        within the {@link uk.co.real_logic.sbe.xml.Message}.
     * @param metadata      for the {@link uk.co.real_logic.sbe.xml.Message}.
     * @param byteOrder     for the encoding.
     */
    public IrNode(final PrimitiveType primitiveType,
                  final int size,
                  final int offset,
                  final Metadata metadata,
                  final ByteOrder byteOrder)
    {
        this.primitiveType = primitiveType;
        this.size = size;
        this.offset = offset;
        this.metadata = metadata;
        this.byteOrder = byteOrder;
    }

    /**
     * Construct a default {@link IrNode} based on {@link Metadata} with defaults for other fields.
     *
     * @param metadata for this node.
     */
    public IrNode(final Metadata metadata)
    {
        this.primitiveType = null;
        this.size = 0;
        this.offset = 0;
        this.metadata = metadata;
        this.byteOrder = null;
    }

    public PrimitiveType getPrimitiveType()
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

    public Metadata getMetadata()
    {
        return metadata;
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

    /** Metadata describing an {@link IrNode} */
    public static class Metadata
    {
        /** Invalid ID value */
        public static final long INVALID_ID = Long.MAX_VALUE;

        private final String name;
        private final long id;
        private final long irId;
        private final Flag flag;

        /*
         * constValue
         * nullValue
         * minValue
         * maxValue
         * description (for START/END and others)
         * ENUM_START
         *    ENUM_VALUE
         * ENUM_END
         * SET_START
         *    SET_CHOICE
         * SET_END
         *
         * irId = generated Id field
         * length field (START_STRUCT or normal) has xRefIrId for <data> field (generateIrId for <data> field and save)
         * VAR_START has xRefIrId to length FIELD_START
         * count field (START_STRUCT or normal) has xRefId for <group> field (generateIrId for <group> field and save)
         * GROUP_START has xRefIrId to count FIELD_START
         *
         * GROUP_START - point to field id that holds count
         * GROUP_END -
         */

        public Metadata(final String name,
                        final long id,
                        final long irId,
                        final Flag flag)
        {
            this.name = name;
            this.id = id;
            this.irId = irId;
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
