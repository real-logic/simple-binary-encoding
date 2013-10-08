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
 * Interface for setting locating a flyweight over a {@link DirectBuffer}.
 */
public interface DirectBufferFlyweight
{
    /**
     * Reset this flyweight to window over the buffer from a given offset.
     *
     * @param buffer from which to read and write.
     * @param offset at which the flyweight starts.
     */
    void reset(final DirectBuffer buffer, final int offset);
}
