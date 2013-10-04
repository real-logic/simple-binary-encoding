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

import org.junit.Test;

import java.nio.ByteBuffer;

public class JavaUtilTest
{
    private final DirectBuffer buffer = new DirectBuffer(ByteBuffer.allocate(32));

    @Test
    public void shouldHandleUnsignedTypes()
    {
        buffer.putByte(0, (byte)(127 & 0xFF));
        buffer.putByte(1, (byte)(128 & 0xFF));
        buffer.putByte(2, (byte)(254 & 0xFF));
    }
}
