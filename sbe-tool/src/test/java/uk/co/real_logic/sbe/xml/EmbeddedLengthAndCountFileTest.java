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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class EmbeddedLengthAndCountFileTest
{
    @Test
    public void shouldHandleEmbeddedCountForGroup() throws Exception
    {
        final MessageSchema schema = parse(
            Tests.getLocalResource("embedded-length-and-count-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertThat(fields.get(1).name(), is("ListOrdGrp"));
        assertThat(fields.get(1).id(), is(73));
        assertNotNull(fields.get(1).dimensionType());

        final List<Field> groupFields = fields.get(1).groupFields();
        assertNotNull(groupFields);
    }

    @Test
    public void shouldHandleEmbeddedLengthForData() throws Exception
    {
        parse(Tests.getLocalResource("embedded-length-and-count-schema.xml"), ParserOptions.DEFAULT);
    }
}
