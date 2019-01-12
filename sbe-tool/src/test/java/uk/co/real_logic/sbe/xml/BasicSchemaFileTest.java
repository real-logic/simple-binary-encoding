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

import java.util.List;

import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class BasicSchemaFileTest
{
    @Test
    public void shouldHandleBasicFile()
        throws Exception
    {
        parse(TestUtil.getLocalResource("basic-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleConstantHeaderField()
        throws Exception
    {
        parse(TestUtil.getLocalResource("basic-schema-constant-header-field.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicFileWithGroup()
        throws Exception
    {
        parse(TestUtil.getLocalResource("basic-group-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicFileWithVariableLengthData()
        throws Exception
    {
        parse(TestUtil.getLocalResource("basic-variable-length-schema.xml"), ParserOptions.DEFAULT);
    }

    @Test
    public void shouldHandleBasicAllTypes()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource("basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();
        assertThat(fields.get(0).name(), is("header"));
        assertThat(fields.get(1).name(), is("EDTField"));
        assertThat(fields.get(2).name(), is("ENUMField"));
        assertThat(fields.get(3).name(), is("SETField"));
        assertThat(fields.get(4).name(), is("int64Field"));
    }
}