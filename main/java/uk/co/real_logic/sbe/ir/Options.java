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
 * Optional settings that can be associated with {@link Token}s.
 */
public class Options
{
    private final PrimitiveValue minVal;
    private final PrimitiveValue maxVal;
    private final PrimitiveValue nullVal;
    private final PrimitiveValue constVal;

    public Options()
    {
        minVal = null;
        maxVal = null;
        nullVal = null;
        constVal = null;
    }

    public Options(final PrimitiveValue minVal,
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
        return "Options{" +
            "minVal=" + minVal +
            ", maxVal=" + maxVal +
            ", nullVal=" + nullVal +
            ", constVal=" + constVal +
            '}';
    }

    /**
     * Builder to make {@link Options} easier to create.
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

        public Options build()
        {
            return new Options(minVal, maxVal, nullVal, constVal);
        }
    }
}
