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
 * Interface for locating a variable length flyweight over a {@link DirectBuffer}.
 */
public interface MessageFlyweight
{
    /**
     * Get the root block length for the message type.
     *
     * @return the root block length for the message type.
     */
    int blockLength();

    /**
     * Offset in the underlying buffer at which the message starts.
     *
     * @return offset in the underlying buffer at which the message starts.
     */
    int offset();

    /**
     * Reset the flyweight to a new index in a buffer to overlay a message.
     *
     * @param buffer underlying the message.
     * @param index at which the message body begins.
     */
    void reset(final DirectBuffer buffer, final int index);

    /**
     * The position for the end of the currently access block.
     *
     * @param position for the end of the currently access block.
     */
    void position(final int position);

    /**
     * The position for the end of the currently access block from the message starting offset.
     *
     * @return the position for the end of the currently access block.
     */
    int position();
}
