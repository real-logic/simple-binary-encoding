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
 * primitiveTypes
 * <p/>
 * Primitive type	Description	                        Length (octets)
 * char             character                               1
 * int8	            Signed byte	                        1
 * uint8	        Unsigned byte / single-byte character	1
 * int16	        16-bit signed integer	                2
 * uint16	        16-bit unsigned integer	                2
 * int32	        32-bit signed integer	                4
 * uint32	        32-bit unsigned integer	                4
 * int64	        64-bit signed integer	                8
 * uint64	        64-bit unsigned integer	                8
 */
public enum Primitive
{
    CHAR("char", 1),
    INT8("int8", 1),
    INT16("int16", 2),
    INT32("int32", 4),
    INT64("int64", 8),
    UINT8("uint8", 1),
    UINT16("uint16", 2),
    UINT32("uint32", 4),
    UINT64("uint64", 8);

    private final String name;
    private final int size;

    Primitive(final String name, final int size)
    {
        this.name = name;
        this.size = size;
    }

    /**
     * The name of the primitive type as a String
     *
     * @return the name as a String
     */
    public String primitiveName()
    {
        return name;
    }

    /**
     * The size of the primitive type in octets
     *
     * @return size (in octets) of the primitive type
     */
    public int size()
    {
        return size;
    }

    /**
     * Lookup name of Primitive and return enum
     *
     * @param value of primitiveType to lookup
     * @return the {@link Primitive} matching the name
     * @throws IllegalArgumentException if name not found
     */
    public static Primitive lookup(final String value)
    {
        for (final Primitive p : values())
        {
            if (value.equals(p.name))
            {
                return p;
            }
        }

        throw new IllegalArgumentException("No PrimitiveType for value: " + value);
    }
}
