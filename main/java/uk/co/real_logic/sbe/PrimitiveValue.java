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
 * Class used to encapsulate values for primitives. Used for nullValue, minValue, maxValue, and constants
 * <p/>
 * <table>
 *     <thead>
 *         <tr>
 *             <th>PrimitiveType</th>
 *             <th>Null</th>
 *             <th>Min</th>
 *             <th>Max</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>char</td>
 *             <td>0</td>
 *             <td>0x20</td>
 *             <td>0x7E</td>
 *         </tr>
 *         <tr>
 *             <td>int8</td>
 *             <td>-128</td>
 *             <td>-127</td>
 *             <td>127</td>
 *         </tr>
 *         <tr>
 *             <td>uint8</td>
 *             <td>255</td>
 *             <td>0</td>
 *             <td>254</td>
 *         </tr>
 *         <tr>
 *             <td>int16</td>
 *             <td>-32768</td>
 *             <td>-32767</td>
 *             <td>32767</td>
 *         </tr>
 *         <tr>
 *             <td>uint16</td>
 *             <td>65535</td>
 *             <td>0</td>
 *             <td>65534</td>
 *         </tr>
 *         <tr>
 *             <td>int32</td>
 *             <td>2^31</td>
 *             <td>-2^31 + 1</td>
 *             <td>2^31 - 1</td>
 *         </tr>
 *         <tr>
 *             <td>uint32</td>
 *             <td>2^32 - 1</td>
 *             <td>0</td>
 *             <td>2^32 - 2</td>
 *         </tr>
 *         <tr>
 *             <td>int64</td>
 *             <td>2^63</td>
 *             <td>-2^63 + 1</td>
 *             <td>2^63 - 1</td>
 *         </tr>
 *         <tr>
 *             <td>uint64</td>
 *             <td>2^64 - 1</td>
 *             <td>0</td>
 *             <td>2^64 - 2</td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public class PrimitiveValue
{
    /** Representation of value is a Java long */
    public static final int LONG_VALUE_REPRESENTATION = 0x1;

    /** Representation of value is a Java double */
    public static final int DOUBLE_VALUE_REPRESENTATION = 0x2;

    public static final long MIN_VALUE_CHAR = 0x20;
    public static final long MAX_VALUE_CHAR = 0x7E;
    public static final long NULL_VALUE_CHAR = 0;

    public static final long MIN_VALUE_INT8 = -127;
    public static final long MAX_VALUE_INT8 = 127;
    public static final long NULL_VALUE_INT8 = -128;

    public static final long MIN_VALUE_UINT8 = 0;
    public static final long MAX_VALUE_UINT8 = 254;
    public static final long NULL_VALUE_UINT8 = 255;

    public static final long MIN_VALUE_INT16 = -32767;
    public static final long MAX_VALUE_INT16 = 32767;
    public static final long NULL_VALUE_INT16 = -32768;

    public static final long MIN_VALUE_UINT16 = 0;
    public static final long MAX_VALUE_UINT16 = 65534;
    public static final long NULL_VALUE_UINT16 = 65535;

    public static final long MIN_VALUE_INT32 = -2147483647;
    public static final long MAX_VALUE_INT32 = 2147483647;
    public static final long NULL_VALUE_INT32 = -2147483648;

    public static final long MIN_VALUE_UINT32 = 0;
    public static final long MAX_VALUE_UINT32 = 0xFFFFFFFD;
    public static final long NULL_VALUE_UINT32 = 0xFFFFFFFE;

    public static final long MIN_VALUE_INT64 = Long.MIN_VALUE + 1;  // -2^63 + 1
    public static final long MAX_VALUE_INT64 = Long.MAX_VALUE;      //  2^63 - 1  (SBE spec says -2^63 - 1)
    public static final long NULL_VALUE_INT64 = Long.MIN_VALUE;     // -2^63

    public static final long MIN_VALUE_UINT64 = 0;
    public static final long MAX_VALUE_UINT64 = Long.MAX_VALUE;  // TODO: placeholder for now (replace with BigInteger?)
    public static final long NULL_VALUE_UINT64 = Long.MIN_VALUE; // TODO: placeholder for now (replace with BigInteger?)

    public static final float MIN_VALUE_FLOAT = Float.MIN_VALUE;
    public static final float MAX_VALUE_FLOAT = Float.MAX_VALUE;
    public static final float NULL_VALUE_FLOAT = Float.NaN;         // TODO: can NOT be used as a normal equality check

    public static final double MIN_VALUE_DOUBLE = Double.MIN_VALUE;
    public static final double MAX_VALUE_DOUBLE = Double.MAX_VALUE;
    public static final double NULL_VALUE_DOUBLE = Double.NaN;      // TODO: can NOT be used as a normal equality check

    private final long longValue;
    private final double doubleValue;
    private final int representation;

    /**
     * Construct and fill in value as a long
     *
     * @param value in long format
     */
    public PrimitiveValue(final long value)
    {
        longValue = value;
        doubleValue = 0.0;
        representation = LONG_VALUE_REPRESENTATION;
    }

    public PrimitiveValue(final double value)
    {
        longValue = 0;
        doubleValue = value;
        representation = DOUBLE_VALUE_REPRESENTATION;
    }

    /**
     * Parse constant value string and set representation based on type
     *
     * @param primitiveType that this is supposed to be
     * @param value     expressed as a String
     * @throws IllegalArgumentException if parsing not known for type
     */
    public PrimitiveValue(final PrimitiveType primitiveType, final String value)
    {
        switch (primitiveType)
        {
            case CHAR:
                if (value.length() > 1)
                {
                    throw new IllegalArgumentException("constant char value malformed");
                }
                longValue = value.getBytes()[0];
                doubleValue = 0.0;
                representation = LONG_VALUE_REPRESENTATION;
                break;

            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                /*
                 * TODO: not entirely adequate, but then again, Java doesn't have unsigned 64-bit integers...
                 */
                longValue = Long.parseLong(value);
                doubleValue = 0.0;
                representation = LONG_VALUE_REPRESENTATION;
                break;

            case FLOAT:
            case DOUBLE:
                longValue = 0;
                doubleValue = Double.parseDouble(value);
                representation = DOUBLE_VALUE_REPRESENTATION;
                break;

            default:
                throw new IllegalArgumentException("Do not know how to parse this primitiveType type for constant value");
        }
    }

    /**
     * Return long value for this PrimitiveValue
     *
     * @return value expressed as a long
     * @throws IllegalArgumentException if not a long value representation
     */
    public long longValue()
    {
        if (representation != LONG_VALUE_REPRESENTATION)
        {
            throw new IllegalArgumentException("PrimitiveValue is not a long representation");
        }

        return longValue;
    }

    /**
     * Return string representation of this object
     *
     * @return String representing object value
     * @throws IllegalArgumentException if unknown representation
     */
    public String toString()
    {
        switch (representation)
        {
            case LONG_VALUE_REPRESENTATION:
                return Long.toString(longValue);

            case DOUBLE_VALUE_REPRESENTATION:
                return Double.toString(doubleValue);

            default:
                throw new IllegalArgumentException("Unknown PrimitiveValue representation");
        }
    }

    /**
     * Determine if two values are equivalent.
     * TODO: does not work for NaN for float and double.
     *
     * @param value to compare this value with
     * @return equivalence of values
     */
    public boolean equals(final Object value)
    {
        /* should only be used for long value representation at the moment */
        if (null != value && value instanceof PrimitiveValue)
        {
            PrimitiveValue rhs = (PrimitiveValue)value;

            if (representation == rhs.representation &&
                longValue == rhs.longValue)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Return hashCode for value. This is the underlying representations hashCode for the value
     *
     * @return int value of the hashCode
     */
    public int hashCode()
    {
        if (representation != LONG_VALUE_REPRESENTATION)
        {
            throw new IllegalArgumentException("PrimitiveValue is not a long representation");
        }

        return (int)(longValue ^ (longValue >>> 32));
    }
}
