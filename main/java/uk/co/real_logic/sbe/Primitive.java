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
 * Primitive type	Description	                        Length (octets)   Null        Min          Max
 * char             character                               1             0           0x20         0x7E
 * int8	            Signed byte	                            1             -128        -127         127
 * uint8	        Unsigned byte / single-byte character	1             255         0            254
 * int16	        16-bit signed integer	                2             -32768      -32767       32767
 * uint16	        16-bit unsigned integer	                2             65535       0            65534
 * int32	        32-bit signed integer	                4             2^31        -2^31 + 1    2^31 - 1
 * uint32	        32-bit unsigned integer	                4             2^32 - 1    0            2^32 - 2
 * int64	        64-bit signed integer	                8             2^63        -2^63 + 1    2^63 - 1
 * uint64	        64-bit unsigned integer	                8             2^64 - 1    0            2^64 - 2
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

    /**
     * Parse constant value string and return int representation
     *
     * @param primitive that this is supposed to be
     * @param value of the constant expressed as a String
     * @return int representation of the constant
     * @throws IllegalArgumentException if parsing not known for type
     */
    public static int parseConstValue2Int(final Primitive primitive, final String value)
    {
        switch (primitive)
        {
        case CHAR:
            if (value.length() > 1)
                throw new IllegalArgumentException("constant char value malformed");

            return value.getBytes()[0];
        case INT8:
        case INT16:
        case INT32:
        case INT64:
        case UINT8:
        case UINT16:
        case UINT32:
        case UINT64:
            // TODO: not entirely adequate, but then again, Java doesn't have unsigned 64-bit ints...
            return Integer.parseInt(value);
        default:
            throw new IllegalArgumentException("Do not know how to parse this primitive type for constant value");
        }
    }
}
