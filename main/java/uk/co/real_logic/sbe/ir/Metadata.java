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
import uk.co.real_logic.sbe.util.Verify;

/**
 * Metadata describing an {@link Token}
 */
public class Metadata
{
    /** Invalid ID value. */
    public static final long INVALID_ID = -1;

    private final String name;
    private final long schemaId;
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;
    private final PrimitiveValue constValue;

    public Metadata(final String name,
                    final long schemaId,
                    final PrimitiveValue minValue,
                    final PrimitiveValue maxValue,
                    final PrimitiveValue nullValue,
                    final PrimitiveValue constValue)
    {
        Verify.notNull(name, "name");

        this.name = name;
        this.schemaId = schemaId;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.nullValue = nullValue;
        this.constValue = constValue;
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

    public String toString()
    {
        return "Metadata{" +
            "name='" + name + '\'' +
            ", schemaId=" + schemaId +
            ", minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", nullValue=" + nullValue +
            ", constValue=" + constValue +
            '}';
    }

    /**
     * Builder to make {@link Metadata} easier to create.
     */
    public static class Builder
    {
        private String name;
        private long schemaId;
        private PrimitiveValue minValue;
        private PrimitiveValue maxValue;
        private PrimitiveValue nullValue;
        private PrimitiveValue constValue;

        public Builder(final String name)
        {
            this.name = name;
            schemaId = Metadata.INVALID_ID;
            minValue = null;
            maxValue = null;
            nullValue = null;
            constValue = null;
        }

        public Builder schemaId(final long schemaId)
        {
            this.schemaId = schemaId;
            return this;
        }

        public Builder minValue(final PrimitiveValue minValue)
        {
            this.minValue = minValue;
            return this;
        }

        public Builder maxValue(final PrimitiveValue maxValue)
        {
            this.maxValue = maxValue;
            return this;
        }

        public Builder nullValue(final PrimitiveValue nullValue)
        {
            this.nullValue = nullValue;
            return this;
        }

        public Builder constValue(final PrimitiveValue constValue)
        {
            this.constValue = constValue;
            return this;
        }

        public Metadata build()
        {
            return new Metadata(name, schemaId, minValue, maxValue, nullValue, constValue);
        }
    }
}
