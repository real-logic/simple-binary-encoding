/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package uk.co.real_logic.sbe.xml;

import org.junit.Assert;
import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;

import java.util.List;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

/*
 * Tests associated with offset and blockLength calculation and validation
 */
public class OffsetFileTest
{
    @Test
    public void shouldHandleAllTypeOffsets()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicTypesSchemaFileTest.xml"));
        List<Field> fields = schema.getMessage(1).getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getType().size()), is(valueOf(20)));
        assertThat(valueOf(fields.get(2).getCalculatedOffset()), is(valueOf(26)));
        assertThat(valueOf(fields.get(2).getType().size()), is(valueOf(1)));
        assertThat(valueOf(fields.get(3).getCalculatedOffset()), is(valueOf(27)));
        assertThat(valueOf(fields.get(3).getType().size()), is(valueOf(4)));
        assertThat(valueOf(fields.get(4).getCalculatedOffset()), is(valueOf(31)));
        assertThat(valueOf(fields.get(4).getType().size()), is(valueOf(8)));
    }

    @Test
    public void shouldHandleAllTypeOffsetsSetByXML()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicTypesSchemaFileTest.xml"));
        List<Field> fields = schema.getMessage(2).getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).getType().size()), is(valueOf(20)));
        assertThat(valueOf(fields.get(2).getCalculatedOffset()), is(valueOf(32)));
        assertThat(valueOf(fields.get(2).getType().size()), is(valueOf(1)));
        assertThat(valueOf(fields.get(3).getCalculatedOffset()), is(valueOf(128)));
        assertThat(valueOf(fields.get(3).getType().size()), is(valueOf(4)));
        assertThat(valueOf(fields.get(4).getCalculatedOffset()), is(valueOf(136)));
        assertThat(valueOf(fields.get(4).getType().size()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithNoPaddingFromBlockLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BlockLengthFileTest.xml"));
        Message msg = schema.getMessage(1);
        assertThat(valueOf(msg.getBlockLength()), is(valueOf(6)));
        List<Field> fields = msg.getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(6)));
        Assert.assertNull(fields.get(1).getType());
        List<Field> groupFields = fields.get(1).getGroupFields();
        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).getType().size()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getCalculatedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getType().size()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BlockLengthFileTest.xml"));
        List<Field> fields = schema.getMessage(2).getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(64)));
        Assert.assertNull(fields.get(1).getType());
        assertThat(valueOf(fields.get(1).getCalculatedBlockLength()), is(valueOf(12)));
        List<Field> groupFields = fields.get(1).getGroupFields();
        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).getType().size()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getCalculatedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getType().size()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLengthAndGroupBlockLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BlockLengthFileTest.xml"));
        List<Field> fields = schema.getMessage(3).getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(64)));
        Assert.assertNull(fields.get(1).getType());
        assertThat(valueOf(fields.get(1).getCalculatedBlockLength()), is(valueOf(16)));
        List<Field> groupFields = fields.get(1).getGroupFields();
        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).getType().size()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getCalculatedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).getType().size()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateDataOffsetWithPaddingFromBlockLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BlockLengthFileTest.xml"));
        List<Field> fields = schema.getMessage(4).getFields();
        assertThat(valueOf(fields.get(0).getCalculatedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).getType().size()), is(valueOf(6)));
        assertThat(valueOf(fields.get(1).getCalculatedOffset()), is(valueOf(64)));
        assertThat(valueOf(fields.get(1).getType().size()), is(valueOf(-1)));
    }
}