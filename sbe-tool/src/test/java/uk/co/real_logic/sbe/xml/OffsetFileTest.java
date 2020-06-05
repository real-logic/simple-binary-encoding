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
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

/*
 * Tests associated with offset and blockLength calculation and validation
 */
public class OffsetFileTest
{
    @Test
    public void shouldHandleAllTypeOffsets() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(1).fields();

        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(8));
        assertThat(fields.get(1).type().encodedLength(), is(20));
        assertThat(fields.get(2).computedOffset(), is(28));
        assertThat(fields.get(2).type().encodedLength(), is(1));
        assertThat(fields.get(3).computedOffset(), is(29));
        assertThat(fields.get(3).type().encodedLength(), is(4));
        assertThat(fields.get(4).computedOffset(), is(33));
        assertThat(fields.get(4).type().encodedLength(), is(8));
    }

    @Test
    public void shouldHandleAllTypeOffsetsSetByXml() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "basic-types-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(2).fields();

        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(8));
        assertThat(fields.get(1).type().encodedLength(), is(20));
        assertThat(fields.get(2).computedOffset(), is(32));
        assertThat(fields.get(2).type().encodedLength(), is(1));
        assertThat(fields.get(3).computedOffset(), is(128));
        assertThat(fields.get(3).type().encodedLength(), is(4));
        assertThat(fields.get(4).computedOffset(), is(136));
        assertThat(fields.get(4).type().encodedLength(), is(8));
    }

    @Test
    public void shouldCalculateGroupOffsetWithNoPaddingFromBlockLength() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final Message msg = schema.getMessage(1);

        assertThat(msg.blockLength(), is(8));

        final List<Field> fields = msg.fields();
        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(8));
        assertNull(fields.get(1).type());

        final List<Field> groupFields = fields.get(1).groupFields();
        assertThat(groupFields.size(), is(2));
        assertThat(groupFields.get(0).computedOffset(), is(0));
        assertThat(groupFields.get(0).type().encodedLength(), is(4));
        assertThat(groupFields.get(1).computedOffset(), is(4));
        assertThat(groupFields.get(1).type().encodedLength(), is(8));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLength() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(2).fields();

        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(64));
        assertNull(fields.get(1).type());
        assertThat(fields.get(1).computedBlockLength(), is(12));

        final List<Field> groupFields = fields.get(1).groupFields();

        assertThat(groupFields.size(), is(2));
        assertThat(groupFields.get(0).computedOffset(), is(0));
        assertThat(groupFields.get(0).type().encodedLength(), is(4));
        assertThat(groupFields.get(1).computedOffset(), is(4));
        assertThat(groupFields.get(1).type().encodedLength(), is(8));
    }

    @Test
    public void shouldCalculateGroupOffsetWithPaddingFromBlockLengthAndGroupBlockLength() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(3).fields();

        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(64));
        assertNull(fields.get(1).type());
        assertThat(fields.get(1).computedBlockLength(), is(16));

        final List<Field> groupFields = fields.get(1).groupFields();

        assertThat(groupFields.size(), is(2));
        assertThat(groupFields.get(0).computedOffset(), is(0));
        assertThat(groupFields.get(0).type().encodedLength(), is(4));
        assertThat(groupFields.get(1).computedOffset(), is(4));
        assertThat(groupFields.get(1).type().encodedLength(), is(8));
    }

    @Test
    public void shouldCalculateDataOffsetWithPaddingFromBlockLength() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "block-length-schema.xml"), ParserOptions.DEFAULT);
        final List<Field> fields = schema.getMessage(4).fields();

        assertThat(fields.get(0).computedOffset(), is(0));
        assertThat(fields.get(0).type().encodedLength(), is(8));
        assertThat(fields.get(1).computedOffset(), is(64));
        assertThat(fields.get(1).type().encodedLength(), is(-1));
    }

    @Test
    public void shouldCalculateCompositeSizeWithOffsetsSpecified() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "composite-offsets-schema.xml"), ParserOptions.DEFAULT);
        final CompositeType header = schema.messageHeader();

        assertThat(header.encodedLength(), is(12));
    }

    @Test
    public void shouldCalculateDimensionSizeWithOffsetsSpecified() throws Exception
    {
        final MessageSchema schema = parse(Tests.getLocalResource(
            "composite-offsets-schema.xml"), ParserOptions.DEFAULT);
        final CompositeType dimensions = schema.getMessage(1).fields().get(0).dimensionType();

        assertThat(dimensions.encodedLength(), is(8));
    }
}
