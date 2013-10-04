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
 * Constraints describing an {@link Token}
 */
public class Constraints
{
    private final PrimitiveValue minValue;
    private final PrimitiveValue maxValue;
    private final PrimitiveValue nullValue;
    private final PrimitiveValue constValue;

    public Constraints()
    {
        minValue = null;
        maxValue = null;
        nullValue = null;
        constValue = null;
    }

    public Constraints(final PrimitiveValue minValue,
                       final PrimitiveValue maxValue,
                       final PrimitiveValue nullValue,
                       final PrimitiveValue constValue)
    {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.nullValue = nullValue;
        this.constValue = constValue;
    }

    /**
     * Return the minValue for the token or null if not set.
     *
     * @return the minValue for the token or null if not set.
     */
    public PrimitiveValue minValue()
    {
        return minValue;
    }

    /**
     * Return the maxValue for the token or null if not set.
     *
     * @return the maxValue for the token or null if not set.
     */
    public PrimitiveValue maxValue()
    {
        return maxValue;
    }

    /**
     * Return the nullValue for the token or null if not set.
     *
     * @return the nullValue for the token or null if not set.
     */
    public PrimitiveValue nullValue()
    {
        return nullValue;
    }

    /**
     * Return the constant value for the token or null if not set.
     *
     * @return the constant value for the token or null if not set.
     */
    public PrimitiveValue constValue()
    {
        return constValue;
    }

    public String toString()
    {
        return "Constraints{" +
            "minValue=" + minValue +
            ", maxValue=" + maxValue +
            ", nullValue=" + nullValue +
            ", constValue=" + constValue +
            '}';
    }

    /**
     * Builder to make {@link Constraints} easier to create.
     */
    public static class Builder
    {
        private PrimitiveValue minValue;
        private PrimitiveValue maxValue;
        private PrimitiveValue nullValue;
        private PrimitiveValue constValue;

        public Builder()
        {
            minValue = null;
            maxValue = null;
            nullValue = null;
            constValue = null;
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

        public Constraints build()
        {
            return new Constraints(minValue, maxValue, nullValue, constValue);
        }
    }
}
