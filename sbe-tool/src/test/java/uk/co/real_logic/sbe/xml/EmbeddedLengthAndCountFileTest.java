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

import org.junit.Assert;
import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;

import java.util.List;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class EmbeddedLengthAndCountFileTest
{
    @Test
    public void shouldHandleEmbeddedCountForGroup()
        throws Exception
    {
        final MessageSchema schema = parse(
            TestUtil.getLocalResource("embedded-length-and-count-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertThat(fields.get(1).name(), is("ListOrdGrp"));
        assertThat(valueOf(fields.get(1).id()), is(valueOf(73)));
        Assert.assertNotNull(fields.get(1).dimensionType());

        final List<Field> groupFields = fields.get(1).groupFields();
        Assert.assertNotNull(groupFields);
    }

    @Test
    public void shouldHandleEmbeddedLengthForData()
        throws Exception
    {
        parse(TestUtil.getLocalResource("embedded-length-and-count-schema.xml"), ParserOptions.DEFAULT);
        /* should parse correctly */
    }
}