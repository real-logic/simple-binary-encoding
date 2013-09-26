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
    public static final long INVALID_ID = -1;

    private final String name;
    private final long schemaId;
    private final long id;
    private final long refId;
    private final Token.Signal signal;
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;
    private final PrimitiveValue constValue;
    private final String description;

    public Metadata(final String name,
                    final long schemaId,
                    final long id,
                    final long refId,
                    final Token.Signal signal,
                    final PrimitiveValue minValue,
                    final PrimitiveValue maxValue,
                    final PrimitiveValue nullValue,
                    final PrimitiveValue constValue,
                    final String description)
    {
        this.name = name;
        this.schemaId = schemaId;
        this.id = id;
        this.refId = refId;
        this.signal = signal;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.nullValue = nullValue;
        this.constValue = constValue;
        this.description = description;
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
    public long getSchemaId()
    {
        return schemaId;
    }

    /**
     * Return the ID of the token.
     *
     * @return the ID of the token.
     */
    public long getId()
    {
        return id;
    }

    /**
     * Return the cross reference ID of the token.
     *
     * @return the cross reference ID of the token.
     */
    public long getRefId()
    {
        return refId;
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

    public String toString()
    {
        return "Metadata{" +
            "name='" + name + '\'' +
            ", schemaId=" + schemaId +
            ", id=" + id +
            ", refId=" + refId +
            ", signal=" + signal +
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", nullValue=" + nullValue +
            ", constValue=" + constValue +
            ", description='" + description + '\'' +
            '}';
    }

    /**
     * Builder to make {@link Metadata} easier to create.
     */
    public static class Builder
    {
        private String name;
        private long schemaId;
        private long id;
        private long refId;
        private Token.Signal signal;
        private PrimitiveValue minValue;
        private PrimitiveValue maxValue;
        private PrimitiveValue nullValue;
        private PrimitiveValue constValue;
        private String description;

        public Builder(final String name)
        {
            this.name = name;
            schemaId = Metadata.INVALID_ID;
            id = Metadata.INVALID_ID;
            refId = Metadata.INVALID_ID;
            signal = Token.Signal.ENCODING;
            minValue = null;
            maxValue = null;
            nullValue = null;
            constValue = null;
            description = null;
        }

        public void schemaId(final long schemaId)
        {
            this.schemaId = schemaId;
        }

        public void id(final long id)
        {
            this.id = id;
        }

        public void refId(final long refId)
        {
            this.refId = refId;
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

        public Metadata build()
        {
            return new Metadata(name, schemaId, id, refId, signal, minValue, maxValue, nullValue, constValue, description);
        }
    }
}
