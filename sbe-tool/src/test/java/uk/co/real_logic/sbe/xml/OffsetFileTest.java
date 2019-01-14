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

/*
 * Tests associated with offset and blockLength calculation and validation
 */
public class OffsetFileTest
{
    @Test
    public void shouldHandleAllTypeOffsets()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).type().encodedLength()), is(valueOf(20)));
        assertThat(valueOf(fields.get(2).computedOffset()), is(valueOf(28)));
        assertThat(valueOf(fields.get(2).type().encodedLength()), is(valueOf(1)));
        assertThat(valueOf(fields.get(3).computedOffset()), is(valueOf(29)));
        assertThat(valueOf(fields.get(3).type().encodedLength()), is(valueOf(4)));
        assertThat(valueOf(fields.get(4).computedOffset()), is(valueOf(33)));
        assertThat(valueOf(fields.get(4).type().encodedLength()), is(valueOf(8)));
    }

    @Test
    public void shouldHandleAllTypeOffsetsSetByXML()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(2).fields();

        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).type().encodedLength()), is(valueOf(20)));
        assertThat(valueOf(fields.get(2).computedOffset()), is(valueOf(32)));
        assertThat(valueOf(fields.get(2).type().encodedLength()), is(valueOf(1)));
        assertThat(valueOf(fields.get(3).computedOffset()), is(valueOf(128)));
        assertThat(valueOf(fields.get(3).type().encodedLength()), is(valueOf(4)));
        assertThat(valueOf(fields.get(4).computedOffset()), is(valueOf(136)));
        assertThat(valueOf(fields.get(4).type().encodedLength()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithNoPaddingFromBlockLength()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final Message msg = schema.getMessage(1);

        assertThat(valueOf(msg.blockLength()), is(valueOf(8)));

        final List<Field> fields = msg.fields();
        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(8)));
        Assert.assertNull(fields.get(1).type());

        final List<Field> groupFields = fields.get(1).groupFields();
        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).type().encodedLength()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).computedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).type().encodedLength()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLength()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(2).fields();

        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(64)));
        Assert.assertNull(fields.get(1).type());
        assertThat(valueOf(fields.get(1).computedBlockLength()), is(valueOf(12)));

        final List<Field> groupFields = fields.get(1).groupFields();

        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).type().encodedLength()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).computedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).type().encodedLength()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLengthAndGroupBlockLength()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(3).fields();

        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(64)));
        Assert.assertNull(fields.get(1).type());
        assertThat(valueOf(fields.get(1).computedBlockLength()), is(valueOf(16)));

        final List<Field> groupFields = fields.get(1).groupFields();

        assertThat(valueOf(groupFields.size()), is(valueOf(2)));
        assertThat(valueOf(groupFields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(groupFields.get(0).type().encodedLength()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).computedOffset()), is(valueOf(4)));
        assertThat(valueOf(groupFields.get(1).type().encodedLength()), is(valueOf(8)));
    }

    @Test
    public void shouldCalculateDataOffsetWithPaddingFromBlockLength()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(4).fields();

        assertThat(valueOf(fields.get(0).computedOffset()), is(valueOf(0)));
        assertThat(valueOf(fields.get(0).type().encodedLength()), is(valueOf(8)));
        assertThat(valueOf(fields.get(1).computedOffset()), is(valueOf(64)));
        assertThat(valueOf(fields.get(1).type().encodedLength()), is(valueOf(-1)));
    }

    @Test
    public void shouldCalculateCompositeSizeWithOffsetsSpecified()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "composite-offsets-schema.xml"), ParserOptions.DEFAULT);
        final CompositeType header = schema.messageHeader();

        assertThat(valueOf(header.encodedLength()), is(valueOf(12)));
    }

    @Test
    public void shouldCalculateDimensionSizeWithOffsetsSpecified()
        throws Exception
    {
        final MessageSchema schema = parse(TestUtil.getLocalResource(
            "composite-offsets-schema.xml"), ParserOptions.DEFAULT);
        final CompositeType dimensions = schema.getMessage(1).fields().get(0).dimensionType();

        assertThat(valueOf(dimensions.encodedLength()), is(valueOf(8)));
    }
}