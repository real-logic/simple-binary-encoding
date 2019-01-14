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

import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class GroupWithDataTest
{
    @Test
    public void shouldParseSchemaSuccessfully()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();
        final Field entriesGroup = fields.get(1);
        final CompositeType dimensionType = entriesGroup.dimensionType();
        final List<Field> entriesFields = entriesGroup.groupFields();

        assertThat(entriesGroup.name(), is("Entries"));
        assertThat(dimensionType.name(), is("groupSizeEncoding"));

        final Field varDataField = entriesFields.get(2);
        assertThat(varDataField.name(), is("varDataField"));
        assertTrue(varDataField.isVariableLength());
    }
}
