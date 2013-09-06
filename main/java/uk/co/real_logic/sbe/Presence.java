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
 * Presence attribute for Type
 */
public enum Presence
{
    REQUIRED("required"),
    CONSTANT("constant"),
    OPTIONAL("optional");

    private final String value;

    Presence(final String value)
    {
        this.value = value;
    }

    /**
     * The value as a String of the presence.
     *
     * @return the value as a String
     */
    public String value()
    {
        return value;
    }

    /**
     * Lookup Presence value and return enum
     *
     * @param value of presence to lookup
     * @return the {@link Presence} matching the value
     * @throws IllegalArgumentException if the value is not found
     */
    public static Presence lookup(final String value)
    {
        for (final Presence p : values())
        {
            if (value.equals(p.value))
            {
                return p;
            }
        }

        throw new IllegalArgumentException("No Presence for value: " + value);
    }
}
