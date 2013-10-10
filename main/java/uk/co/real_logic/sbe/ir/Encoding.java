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
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.util.Verify;

import java.nio.ByteOrder;

/**
 * Optional settings that can be associated with {@link Token}s.
 */
public class Encoding
{
    /**
     * Indicates the presence status of a primitive encoded field in a message.
     */
    public static enum Presence
    {
        /** The field presence is required. */
        REQUIRED,

        /** The field presence is optional. */
        OPTIONAL,

        /** The field presence is a constant. */
        CONSTANT
    }

    private final PrimitiveType primitiveType;
    private final ByteOrder byteOrder;
    private final PrimitiveValue minVal;
    private final PrimitiveValue maxVal;
    private final PrimitiveValue nullVal;
    private final PrimitiveValue constVal;

    public Encoding()
    {
        primitiveType = null;
        byteOrder = ByteOrder.LITTLE_ENDIAN;
        minVal = null;
        maxVal = null;
        nullVal = null;
        constVal = null;
    }

    public Encoding(final PrimitiveType primitiveType,
                    final ByteOrder byteOrder,
                    final PrimitiveValue minVal,
                    final PrimitiveValue maxVal,
                    final PrimitiveValue nullVal,
                    final PrimitiveValue constVal)
    {
        Verify.notNull(byteOrder, "byteOrder");

        this.primitiveType = primitiveType;
        this.byteOrder = byteOrder;
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.nullVal = nullVal;
        this.constVal = constVal;
    }

    /**
     * The {@link PrimitiveType} of this encoding.
     *
     * @return the {@link PrimitiveType} of this encoding.
     */
    public PrimitiveType primitiveType()
    {
        return primitiveType;
    }

    /**
     * The {@link ByteOrder} for this encoding.
     *
     * @return the {@link ByteOrder} for this encoding.
     */
    public ByteOrder byteOrder()
    {
        return byteOrder;
    }

    /**
     * The min value for the token or null if not set.
     *
     * @return the minVal for the token or null if not set.
     */
    public PrimitiveValue minVal()
    {
        return minVal;
    }

    /**
     * The max value for the token or null if not set.
     *
     * @return the maxVal for the token or null if not set.
     */
    public PrimitiveValue maxVal()
    {
        return maxVal;
    }

    /**
     * The null value for the token or null if not set.
     *
     * @return the nullVal for the token or null if not set.
     */
    public PrimitiveValue nullVal()
    {
        return nullVal;
    }

    /**
     * The constant value for the token or null if not set.
     *
     * @return the constant value for the token or null if not set.
     */
    public PrimitiveValue constVal()
    {
        return constVal;
    }

    /**
     * Indicates the presence status of a field in a message.
     *
     * @return indicates the presence status of a field in a message.
     */
    public Presence presence()
    {
        if (null != constVal)
        {
            return Presence.CONSTANT;
        }

        if (null != nullVal)
        {
            return Presence.OPTIONAL;
        }

        return Presence.REQUIRED;
    }

    public String toString()
    {
        return "Encoding{" +
            "minVal=" + minVal +
            ", maxVal=" + maxVal +
            ", nullVal=" + nullVal +
            ", constVal=" + constVal +
            '}';
    }

    /**
     * Builder to make {@link Encoding} easier to create.
     */
    public static class Builder
    {
        private PrimitiveType primitiveType;
        private ByteOrder byteOrder;
        private PrimitiveValue minVal;
        private PrimitiveValue maxVal;
        private PrimitiveValue nullVal;
        private PrimitiveValue constVal;

        public Builder()
        {
            primitiveType = null;
            byteOrder = ByteOrder.LITTLE_ENDIAN;
            minVal = null;
            maxVal = null;
            nullVal = null;
            constVal = null;
        }

        public Builder primitiveType(final PrimitiveType primitiveType)
        {
            this.primitiveType = primitiveType;
            return this;
        }

        public Builder byteOrder(final ByteOrder byteOrder)
        {
            this.byteOrder = byteOrder;
            return this;
        }

        public Builder minVal(final PrimitiveValue minValue)
        {
            this.minVal = minValue;
            return this;
        }

        public Builder maxVal(final PrimitiveValue maxValue)
        {
            this.maxVal = maxValue;
            return this;
        }

        public Builder nullVal(final PrimitiveValue nullValue)
        {
            this.nullVal = nullValue;
            return this;
        }

        public Builder constVal(final PrimitiveValue constValue)
        {
            this.constVal = constValue;
            return this;
        }

        public Encoding build()
        {
            return new Encoding(primitiveType, byteOrder, minVal, maxVal, nullVal, constVal);
        }
    }
}
