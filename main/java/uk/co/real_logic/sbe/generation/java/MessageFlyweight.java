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
     * Reset state in preparation for decoding a message.
     *
     * @param buffer from which to read.
     * @param index at which the message body begins.
     */
    void resetForDecode(final DirectBuffer buffer, final int index);

    /**
     * Reset state in preparation for encoding a message.
     *
     * @param buffer into which will writes will occur.
     * @param index at which the message body should begin.
     */
    void resetForEncode(final DirectBuffer buffer, final int index);
}
