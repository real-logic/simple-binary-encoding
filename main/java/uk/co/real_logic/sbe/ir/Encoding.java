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
 * Optional encoding settings that can be associated with {@link Token}s.
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
    private final Presence presence;
    private final ByteOrder byteOrder;
    private final PrimitiveValue minVal;
    private final PrimitiveValue maxVal;
    private final PrimitiveValue nullVal;
    private final PrimitiveValue constVal;
    private final String characterEncoding;
    private final String epoch;
    private final String timeUnit;
    private final String semanticType;

    public Encoding()
    {
        primitiveType = null;
        presence = Presence.REQUIRED;
        byteOrder = ByteOrder.LITTLE_ENDIAN;
        minVal = null;
        maxVal = null;
        nullVal = null;
        constVal = null;
        characterEncoding = "";
        epoch = null;
        timeUnit = null;
        semanticType = null;
    }

    public Encoding(final PrimitiveType primitiveType,
                    final Presence presence,
                    final ByteOrder byteOrder,
                    final PrimitiveValue minVal,
                    final PrimitiveValue maxVal,
                    final PrimitiveValue nullVal,
                    final PrimitiveValue constVal,
                    final String characterEncoding,
                    final String epoch,
                    final String timeUnit,
                    final String semanticType)
    {
        Verify.notNull(presence, "presence");
        Verify.notNull(byteOrder, "byteOrder");

        this.primitiveType = primitiveType;
        this.presence = presence;
        this.byteOrder = byteOrder;
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.nullVal = nullVal;
        this.constVal = constVal;
        this.characterEncoding = characterEncoding;
        this.epoch = epoch;
        this.timeUnit = timeUnit;
        this.semanticType = semanticType;
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
        return  presence;
    }

    /**
     * The most applicable null value for the encoded type.
     *
     * @return most applicable null value for the encoded type.
     */
    public PrimitiveValue applicableNullVal()
    {
        if (null != nullVal)
        {
            return nullVal;
        }

        return primitiveType.nullVal();
    }

    /**
     * The most applicable min value for the encoded type.
     *
     * @return most applicable min value for the encoded type.
     */
    public PrimitiveValue applicableMinVal()
    {
        if (null != minVal)
        {
            return minVal;
        }

        return primitiveType.minVal();
    }


    /**
     * The most applicable max value for the encoded type.
     *
     * @return most applicable max value for the encoded type.
     */
    public PrimitiveValue applicableMaxVal()
    {
        if (null != maxVal)
        {
            return maxVal;
        }

        return primitiveType.maxVal();
    }

    /**
     * The character encoding for the token or null if not set.
     *
     * @return the character encoding for the token or null if not set.
     */
    public String characterEncoding()
    {
        return characterEncoding;
    }

    /**
     * The epoch from which a timestamp is offset. The default is "unix".
     *
     * @return the epoch from which a timestamp is offset.
     */
    public String epoch()
    {
        return epoch;
    }

    /**
     * The time unit of the timestamp.
     *
     * @return the time unit of the timestamp.
     */
    public String timeUnit()
    {
        return timeUnit;
    }

    /**
     * The semantic type of an encoding which can have relevance to the application layer.
     *
     * @return semantic type of an encoding which can have relevance to the application layer.
     */
    public String semanticType()
    {
        return semanticType;
    }

    public String toString()
    {
        return "Encoding{" +
            "primitiveType=" + primitiveType +
            ", presence=" + presence +
            ", byteOrder=" + byteOrder +
            ", minVal=" + minVal +
            ", maxVal=" + maxVal +
            ", nullVal=" + nullVal +
            ", constVal=" + constVal +
            ", characterEncoding='" + characterEncoding + '\'' +
            ", epoch='" + epoch + '\'' +
            ", timeUnit=" + timeUnit +
            ", semanticType='" + semanticType + '\'' +
            '}';
    }

    /**
     * Builder to make {@link Encoding} easier to create.
     */
    public static class Builder
    {
        private PrimitiveType primitiveType = null;
        private Presence presence = Presence.REQUIRED;
        private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
        private PrimitiveValue minVal = null;
        private PrimitiveValue maxVal = null;
        private PrimitiveValue nullVal = null;
        private PrimitiveValue constVal = null;
        private String characterEncoding = "";
        private String epoch = null;
        private String timeUnit = null;
        private String semanticType = null;

        public Builder primitiveType(final PrimitiveType primitiveType)
        {
            this.primitiveType = primitiveType;
            return this;
        }

        public Builder presence(final Presence presence)
        {
            this.presence = presence;
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

        public Builder characterEncoding(final String characterEncoding)
        {
            this.characterEncoding = characterEncoding;
            return this;
        }

        public Builder epoch(final String epoch)
        {
            this.epoch = epoch;
            return this;
        }

        public Builder timeUnit(final String timeUnit)
        {
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder semanticType(final String semanticType)
        {
            this.semanticType = semanticType;
            return this;
        }

        public Encoding build()
        {
            return new Encoding(primitiveType,
                                presence,
                                byteOrder,
                                minVal,
                                maxVal,
                                nullVal,
                                constVal,
                                characterEncoding,
                                epoch,
                                timeUnit,
                                semanticType);
        }
    }
}
