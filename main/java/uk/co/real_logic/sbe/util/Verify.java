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
package uk.co.real_logic.sbe.util;

import java.util.Map;

/**
 * Various verification checks to be applied in code.
 */
public class Verify
{
    /**
     * Verify that a reference is not null.
     *
     * @param ref to be verified not null.
     * @param name of the reference to be verified.
     */
    public static void notNull(final Object ref, final String name)
    {
        if (null == ref)
        {
            throw new NullPointerException(name + " must not be null");
        }
    }

    /**
     * Verify that a map contains and entry for a given key.
     *
     * @param map to be checked.
     * @param key to get by.
     * @param name of entry.
     * @throws NullPointerException if map or key is null
     * @throws IllegalStateException if the entry does not exist.
     */
    public static void present(final Map<?, ?> map, final Object key, final String name)
    {
        if (null == map.get(key))
        {
            throw new IllegalStateException(name + " not found in map for key: " + key);
        }
    }
}
