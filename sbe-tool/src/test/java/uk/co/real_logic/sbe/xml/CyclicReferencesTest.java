/*
 * Copyright 2013-2019 Real Logic Ltd.
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
package uk.co.real_logic.sbe.xml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.real_logic.sbe.TestUtil;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class CyclicReferencesTest
{
    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldTestForCyclicRefs()
        throws Exception
    {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("ref types cannot create circular dependencies");

        final ParserOptions options = ParserOptions.builder().suppressOutput(true).warningsFatal(true).build();

        parse(TestUtil.getLocalResource("cyclic-refs-schema.xml"), options);
    }
}