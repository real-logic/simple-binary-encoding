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
package uk.co.real_logic.sbe.generation;

import java.io.IOException;

/**
 * Abstraction for code generators to implement.
 */
public interface CodeGenerator
{
    /**
     * Generate the stub for reading message headers.
     */
    void generateMessageHeaderStub() throws IOException;

    /**
     * Generate the the stubs for handling types.
     *
     * @throws IOException if an error is encountered when writing the output.
     */
    void generateTypeStubs() throws IOException;

    /**
     * Generate the stubs for handling the messages.
     *
     * @throws IOException if an error is encountered when writing the output.
     */
    void generateMessageStubs() throws IOException;
}
