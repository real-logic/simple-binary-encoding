/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class CyclicReferencesTest
{
    @Test
    public void shouldTestForCyclicRefs() throws Exception
    {
        try
        {
            final ParserOptions options = ParserOptions.builder().suppressOutput(true).warningsFatal(true).build();

            parse(Tests.getLocalResource("cyclic-refs-schema.xml"), options);
        }
        catch (final IllegalStateException ex)
        {
            assertEquals(ex.getMessage(), "ref types cannot create circular dependencies");
            return;
        }

        fail("Expected IllegalStateException");
    }
}
