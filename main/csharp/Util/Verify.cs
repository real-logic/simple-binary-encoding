using System;
using System.Collections;
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
using System.Collections.Generic;

namespace Adaptive.SimpleBinaryEncoding.Util
{

    /// <summary>
    /// Various verification checks to be applied in code.
    /// </summary>
    public class Verify
    {
        /// <summary>
        /// Verify that a reference is not null.
        /// </summary>
        /// <param name="reference"> to be verified not null. </param>
        /// <param name="name"> of the reference to be verified. </param>
        public static void NotNull(object reference, string name)
        {
            if (null == reference)
            {
                throw new NullReferenceException(name + " must not be null");
            }
        }

        /// <summary>
        /// Verify that a map contains and entry for a given key.
        /// </summary>
        /// <param name="map"> to be checked. </param>
        /// <param name="key"> to get by. </param>
        /// <param name="name"> of entry. </param>
        public static void Present<T,V>(IDictionary<T, V> map, T key, string name)
        {
            if (null == map[key])
            {
                throw new InvalidOperationException(name + " not found in map for key: " + key);
            }
        }
    }
}