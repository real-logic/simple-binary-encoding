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
package uk.co.real_logic.sbe.generation.java;

/**
 * Interface for locating a fixed length flyweight over a {@link DirectBuffer}.
 */
public interface FixedFlyweight
{
    /**
     * Wrap this flyweight over a buffer to window it.
     *
     * @param buffer from which to read and write.
     * @param offset at which the flyweight starts.
     * @param actingVersion of the containing template being decoded
     * @return covariant subclass for fluent API support.
     */
    FixedFlyweight wrap(final DirectBuffer buffer, final int offset, final int actingVersion);
}
