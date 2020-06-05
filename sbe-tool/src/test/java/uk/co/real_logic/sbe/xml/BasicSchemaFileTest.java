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

import java.util.List;

import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class BasicSchemaFileTest
{
    @Test
    public void shouldHandleBasicFile()
        throws Exception
    {
        parse(Tests.getLocalResource("basic-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleConstantHeaderField()
        throws Exception
    {
        parse(Tests.getLocalResource("basic-schema-constant-header-field.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicFileWithGroup()
        throws Exception
    {
        parse(Tests.getLocalResource("basic-group-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicFileWithVariableLengthData()
        throws Exception
    {
        parse(Tests.getLocalResource("basic-variable-length-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicAllTypes()
        throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource("basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertThat(fields.get(0).name(), is("header"));
        assertThat(fields.get(1).name(), is("EDTField"));
        assertThat(fields.get(2).name(), is("ENUMField"));
        assertThat(fields.get(3).name(), is("SETField"));
        assertThat(fields.get(4).name(), is("int64Field"));
    }
}