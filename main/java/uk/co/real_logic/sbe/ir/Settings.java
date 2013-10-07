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
 * Settings describing an {@link Token}
 */
public class Settings
{
    private final PrimitiveValue minVal;
    private final PrimitiveValue maxVal;
    private final PrimitiveValue nullVal;
    private final PrimitiveValue constVal;

    public Settings()
    {
        minVal = null;
        maxVal = null;
        nullVal = null;
        constVal = null;
    }

    public Settings(final PrimitiveValue minVal,
                    final PrimitiveValue maxVal,
                    final PrimitiveValue nullVal,
                    final PrimitiveValue constVal)
    {
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.nullVal = nullVal;
        this.constVal = constVal;
    }

    /**
     * Return the minVal for the token or null if not set.
     *
     * @return the minVal for the token or null if not set.
     */
    public PrimitiveValue minVal()
    {
        return minVal;
    }

    /**
     * Return the maxVal for the token or null if not set.
     *
     * @return the maxVal for the token or null if not set.
     */
    public PrimitiveValue maxVal()
    {
        return maxVal;
    }

    /**
     * Return the nullVal for the token or null if not set.
     *
     * @return the nullVal for the token or null if not set.
     */
    public PrimitiveValue nullVal()
    {
        return nullVal;
    }

    /**
     * Return the constant value for the token or null if not set.
     *
     * @return the constant value for the token or null if not set.
     */
    public PrimitiveValue constVal()
    {
        return constVal;
    }

    public String toString()
    {
        return "Settings{" +
            "minVal=" + minVal +
            ", maxVal=" + maxVal +
            ", nullVal=" + nullVal +
            ", constVal=" + constVal +
            '}';
    }

    /**
     * Builder to make {@link Settings} easier to create.
     */
    public static class Builder
    {
        private PrimitiveValue minVal;
        private PrimitiveValue maxVal;
        private PrimitiveValue nullVal;
        private PrimitiveValue constVal;

        public Builder()
        {
            minVal = null;
            maxVal = null;
            nullVal = null;
            constVal = null;
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

        public Settings build()
        {
            return new Settings(minVal, maxVal, nullVal, constVal);
        }
    }
}
