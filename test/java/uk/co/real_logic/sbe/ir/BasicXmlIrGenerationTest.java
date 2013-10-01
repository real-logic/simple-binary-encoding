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
package uk.co.real_logic.sbe.ir;

import org.junit.Test;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.nio.ByteOrder;
import java.util.List;

import static java.lang.Integer.valueOf;
import static java.lang.Long.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class BasicXmlIrGenerationTest
{
    @Test
    public void shouldGenerateCorrectIrForMessageHeader()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForHeader(schema);

        assertThat(valueOf(ir.size()), is(valueOf(6)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.BEGIN_COMPOSITE));
        assertThat(ir.get(0).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(0).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(1).getMetadata().getName(), is("blockLength"));
        assertThat(ir.get(1).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(ir.get(1).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));
        assertThat(ir.get(1).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(2).getMetadata().getName(), is("templateId"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(ir.get(2).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(2)));
        assertThat(ir.get(2).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(3).getMetadata().getName(), is("version"));
        assertThat(ir.get(3).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(3).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(4)));
        assertThat(ir.get(3).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(4).getMetadata().getName(), is("reserved"));
        assertThat(ir.get(4).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(4).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(5)));
        assertThat(ir.get(4).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 5 */
        assertThat(ir.get(5).getMetadata().getSignal(), is(Token.Signal.END_COMPOSITE));
        assertThat(ir.get(5).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(5).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(5).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForBasicMessage()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema, 50001);

        assertThat(valueOf(ir.size()), is(valueOf(5)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.BEGIN_MESSAGE));
        assertThat(ir.get(0).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(ir.get(0).getMetadata().getSchemaId()), is(valueOf(50001L)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(ir.get(1).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(1).getMetadata().getSchemaId()), is(valueOf(40001L)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(2).getMetadata().getName(), is("uint32"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT32));
        assertThat(valueOf(ir.get(2).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(4)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(0)));
        assertThat(ir.get(2).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.END_FIELD));
        assertThat(ir.get(3).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(3).getMetadata().getSchemaId()), is(valueOf(40001L)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.END_MESSAGE));
        assertThat(ir.get(4).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(ir.get(4).getMetadata().getSchemaId()), is(valueOf(50001L)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthField()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicVariableLengthSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema, 1);

        assertThat(valueOf(ir.size()), is(valueOf(8)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.BEGIN_MESSAGE));
        assertThat(ir.get(0).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(ir.get(0).getMetadata().getSchemaId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(0).getMetadata().getId()), is(valueOf(0L)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(ir.get(1).getMetadata().getName(), is("EncryptedNewPasswordLen"));
        assertThat(valueOf(ir.get(1).getMetadata().getSchemaId()), is(valueOf(1403L)));
        assertThat(valueOf(ir.get(1).getMetadata().getId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(1).getMetadata().getRefId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(2).getMetadata().getName(), is("length"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(2).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(0)));
        assertThat(ir.get(2).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.END_FIELD));
        assertThat(ir.get(3).getMetadata().getName(), is("EncryptedNewPasswordLen"));
        assertThat(valueOf(ir.get(3).getMetadata().getSchemaId()), is(valueOf(1403L)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(ir.get(4).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(ir.get(4).getMetadata().getSchemaId()), is(valueOf(1404L)));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(4).getMetadata().getRefId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(0)));

        /* assert all elements of node 5 */
        assertThat(ir.get(5).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(5).getMetadata().getName(), is("rawData"));
        assertThat(ir.get(5).getPrimitiveType(), is(PrimitiveType.CHAR));
        assertThat(valueOf(ir.get(5).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(5).size()), is(valueOf(-1)));
        assertThat(valueOf(ir.get(5).getOffset()), is(valueOf(1)));
        assertThat(ir.get(5).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 6 */
        assertThat(ir.get(6).getMetadata().getSignal(), is(Token.Signal.END_FIELD));
        assertThat(ir.get(6).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(ir.get(6).getMetadata().getSchemaId()), is(valueOf(1404L)));
        assertThat(valueOf(ir.get(6).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(6).getOffset()), is(valueOf(0)));

        /* assert all elements of node 7 */
        assertThat(ir.get(7).getMetadata().getSignal(), is(Token.Signal.END_MESSAGE));
        assertThat(ir.get(7).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(ir.get(7).getMetadata().getSchemaId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(7).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(7).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithRepeatingGroup()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicGroupSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema, 1);

        /* assert the NoEntries node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(ir.get(4).getMetadata().getName(), is("NoEntries"));
        assertThat(valueOf(ir.get(4).getMetadata().getSchemaId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(4).getMetadata().getRefId()), is(valueOf(ir.get(7).getMetadata().getId())));
        //assertThat(valueOf(ir.get(4).getMetadata().getRefId()), is(valueOf(2L)));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(7).getMetadata().getSignal(), is(Token.Signal.BEGIN_GROUP));
        assertThat(ir.get(7).getMetadata().getName(), is("Entries"));
        assertThat(valueOf(ir.get(7).getMetadata().getId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(7).getMetadata().getRefId()), is(valueOf(ir.get(4).getMetadata().getId())));
        //assertThat(valueOf(ir.get(7).getMetadata().getRefId()), is(valueOf(1L)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithRepeatingGroupWithEmbeddedDimensions()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("EmbeddedLengthAndCountFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        /* msg, field, enc, field end, field, comp, enc, enc, comp end, field end, group */
        int dimensionsIdx = 4;
        int groupIdx = 10;

        List<Token> ir = irg.generateForMessage(schema, 1);

        /* assert the dimensions node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(dimensionsIdx).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(valueOf(ir.get(dimensionsIdx).getMetadata().getSchemaId()), is(valueOf(73L)));
        assertThat(valueOf(ir.get(dimensionsIdx).getMetadata().getId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(dimensionsIdx).getMetadata().getRefId()), is(valueOf(ir.get(groupIdx).getMetadata().getId())));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(groupIdx).getMetadata().getSignal(), is(Token.Signal.BEGIN_GROUP));
        assertThat(ir.get(groupIdx).getMetadata().getName(), is("ListOrdGrp"));
        assertThat(valueOf(ir.get(groupIdx).getMetadata().getId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(groupIdx).getMetadata().getRefId()), is(valueOf(ir.get(dimensionsIdx).getMetadata().getId())));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthFieldWithEmbeddedLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("EmbeddedLengthAndCountFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        /* msg, field, enc, field end, field, comp, enc, enc, comp end, field end */
        int lengthFieldIdx = 4;
        int lengthEncIdx = 6;
        int dataEncIdx = 7;

        List<Token> ir = irg.generateForMessage(schema, 2);

        /* assert the varDataEncoding field node is formed correctly */
        assertThat(ir.get(lengthFieldIdx).getMetadata().getSignal(), is(Token.Signal.BEGIN_FIELD));
        assertThat(ir.get(lengthFieldIdx).getMetadata().getName(), is("EncryptedPassword"));
        assertThat(valueOf(ir.get(lengthFieldIdx).getMetadata().getSchemaId()), is(valueOf(1402L)));

        /* assert the length node has correct values */
        assertThat(ir.get(lengthEncIdx).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(lengthEncIdx).getPrimitiveType(), is(PrimitiveType.UINT8));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(dataEncIdx).getMetadata().getSignal(), is(Token.Signal.ENCODING));
        assertThat(ir.get(dataEncIdx).getPrimitiveType(), is(PrimitiveType.CHAR));
    }
}