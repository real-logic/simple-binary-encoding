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

import uk.co.real_logic.sbe.PrimitiveValue;

/**
 * Metadata describing an {@link Token}
 */
public class Metadata
{
    /** Invalid ID value. */
    public static final long INVALID_ID = Long.MAX_VALUE;

    private final String name;
    private final long id;
    private final long irId;
    private final long xRefIrId;
    private final Token.Signal signal;
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;
    private final PrimitiveValue constValue;
    private final String description;
    private final String fixUsage;

    /**
     * Constructor that uses used from a builder
     *
     * @param builder to use to build the metadata
     */
    public Metadata(final Builder builder)
    {
        name = builder.name;
        id = builder.id;
        irId = builder.irId;
        xRefIrId = builder.xRefIrId;
        signal = builder.signal;
        minValue = builder.minValue;
        maxValue = builder.maxValue;
        nullValue = builder.nullValue;
        constValue = builder.constValue;
        description = builder.description;
        fixUsage = builder.fixUsage;
    }

    /**
     * Return the name of the token
     *
     * @return name of the token
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return the ID of the token assigned by the specification
     *
     * @return ID of the token assigned by the specification
     */
    public long getId()
    {
        return id;
    }

    /**
     * Return the IR ID of the token.
     *
     * @return the IR ID of the token.
     */
    public long getIrId()
    {
        return irId;
    }

    /**
     * Return the cross reference IR ID of the token.
     *
     * @return the cross reference IR ID of the token.
     */
    public long getXRefIrId()
    {
        return xRefIrId;
    }

    /**
     * The Signal the token is raising.
     *
     * @return the Signal the token is raising.
     */
    public Token.Signal getSignal()
    {
        return signal;
    }

    /**
     * Return the minValue for the token or null if not set.
     *
     * @return the minValue for the token or null if not set.
     */
    public PrimitiveValue getMinValue()
    {
        return minValue;
    }

    /**
     * Return the maxValue for the token or null if not set.
     *
     * @return the maxValue for the token or null if not set.
     */
    public PrimitiveValue getMaxValue()
    {
        return maxValue;
    }

    /**
     * Return the nullValue for the token or null if not set.
     *
     * @return the nullValue for the token or null if not set.
     */
    public PrimitiveValue getNullValue()
    {
        return nullValue;
    }

    /**
     * Return the constant value for the token or null if not set.
     *
     * @return the constant value for the token or null if not set.
     */
    public PrimitiveValue getConstValue()
    {
        return constValue;
    }

    /**
     * Return the description for the token or null if not set.
     *
     * @return the description for the token or null if not set.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Return the fixUsage value for the node or null it not set.
     */
    public String getFixUsage()
    {
        return fixUsage;
    }

    /**
     * Builder to make setting {@link Metadata} easier to create.
     */
    public static class Builder
    {
        private String name;
        private long id;
        private long irId;
        private long xRefIrId;
        private Token.Signal signal;
        private PrimitiveValue minValue;
        private PrimitiveValue maxValue;
        private PrimitiveValue nullValue;
        private PrimitiveValue constValue;
        private String description;
        private String fixUsage;

        public Builder(final String name)
        {
            this.name = name;
            id = Metadata.INVALID_ID;
            irId = Metadata.INVALID_ID;
            xRefIrId = Metadata.INVALID_ID;
            this.signal = Token.Signal.NONE;
            minValue = null;
            maxValue = null;
            nullValue = null;
            constValue = null;
            description = null;
            fixUsage = null;
        }

        public void id(final long id)
        {
            this.id = id;
        }

        public void irId(final long irId)
        {
            this.irId = irId;
        }

        public void sRefIrId(final long xRefIrId)
        {
            this.xRefIrId = xRefIrId;
        }

        public void flag(final Token.Signal signal)
        {
            this.signal = signal;
        }

        public void minValue(final PrimitiveValue minValue)
        {
            this.minValue = minValue;
        }

        public void maxValue(final PrimitiveValue maxValue)
        {
            this.maxValue = maxValue;
        }

        public void nullValue(final PrimitiveValue nullValue)
        {
            this.nullValue = nullValue;
        }

        public void constValue(final PrimitiveValue constValue)
        {
            this.constValue = constValue;
        }

        public void description(final String description)
        {
            this.description = description;
        }

        public void fixUsage(final String fixUsage)
        {
            this.fixUsage = fixUsage;
        }
    }
}
