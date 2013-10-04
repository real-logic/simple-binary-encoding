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

        IntermediateRepresentation ir = irg.generate(schema);
        List<Token> tokens = ir.getHeader();

        assertThat(valueOf(tokens.size()), is(valueOf(6)));

        /* assert all elements of node 0 */
        assertThat(tokens.get(0).getSignal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(0).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(tokens.get(0).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(tokens.get(1).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(1).getMetadata().getName(), is("blockLength"));
        assertThat(tokens.get(1).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(tokens.get(1).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(2)));
        assertThat(valueOf(tokens.get(1).getOffset()), is(valueOf(0)));
        assertThat(tokens.get(1).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 2 */
        assertThat(tokens.get(2).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(2).getMetadata().getName(), is("templateId"));
        assertThat(tokens.get(2).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(tokens.get(2).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(2)));
        assertThat(valueOf(tokens.get(2).getOffset()), is(valueOf(2)));
        assertThat(tokens.get(2).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(tokens.get(3).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(3).getMetadata().getName(), is("version"));
        assertThat(tokens.get(3).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(3).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(3).getOffset()), is(valueOf(4)));
        assertThat(tokens.get(3).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 4 */
        assertThat(tokens.get(4).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(4).getMetadata().getName(), is("reserved"));
        assertThat(tokens.get(4).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(4).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(4).getOffset()), is(valueOf(5)));
        assertThat(tokens.get(4).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 5 */
        assertThat(tokens.get(5).getSignal(), is(Signal.END_COMPOSITE));
        assertThat(tokens.get(5).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(tokens.get(5).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(5).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForBasicMessage()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);

        List<Token> tokens = ir.getMessage(50001);

        assertThat(valueOf(tokens.size()), is(valueOf(5)));

        /* assert all elements of node 0 */
        assertThat(tokens.get(0).getSignal(), is(Signal.BEGIN_MESSAGE));
        assertThat(tokens.get(0).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(tokens.get(0).getMetadata().getSchemaId()), is(valueOf(50001L)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(tokens.get(1).getSignal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(1).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(tokens.get(1).getMetadata().getSchemaId()), is(valueOf(40001L)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(tokens.get(2).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(2).getMetadata().getName(), is("uint32"));
        assertThat(tokens.get(2).getPrimitiveType(), is(PrimitiveType.UINT32));
        assertThat(valueOf(tokens.get(2).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(4)));
        assertThat(valueOf(tokens.get(2).getOffset()), is(valueOf(0)));
        assertThat(tokens.get(2).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(tokens.get(3).getSignal(), is(Signal.END_FIELD));
        assertThat(tokens.get(3).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(tokens.get(3).getMetadata().getSchemaId()), is(valueOf(40001L)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(3).getOffset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(tokens.get(4).getSignal(), is(Signal.END_MESSAGE));
        assertThat(tokens.get(4).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(tokens.get(4).getMetadata().getSchemaId()), is(valueOf(50001L)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(4).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthField()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicVariableLengthSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);

        List<Token> tokens = ir.getMessage(1);

        assertThat(valueOf(tokens.size()), is(valueOf(8)));

        assertThat(tokens.get(0).getSignal(), is(Signal.BEGIN_MESSAGE));
        assertThat(tokens.get(0).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(tokens.get(0).getMetadata().getSchemaId()), is(valueOf(1L)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).getOffset()), is(valueOf(0)));

        assertThat(tokens.get(1).getSignal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(1).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(tokens.get(1).getMetadata().getSchemaId()), is(valueOf(1404L)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(1).getOffset()), is(valueOf(0)));

        assertThat(tokens.get(2).getSignal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(2).getMetadata().getName(), is("varDataEncoding"));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(2).getOffset()), is(valueOf(0)));

        assertThat(tokens.get(3).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(3).getMetadata().getName(), is("length"));
        assertThat(tokens.get(3).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(3).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(3).getOffset()), is(valueOf(0)));
        assertThat(tokens.get(3).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        assertThat(tokens.get(4).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(4).getMetadata().getName(), is("varData"));
        assertThat(tokens.get(4).getPrimitiveType(), is(PrimitiveType.CHAR));
        assertThat(valueOf(tokens.get(4).getMetadata().getSchemaId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(-1)));
        assertThat(valueOf(tokens.get(4).getOffset()), is(valueOf(1)));
        assertThat(tokens.get(4).getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        assertThat(tokens.get(5).getSignal(), is(Signal.END_COMPOSITE));
        assertThat(tokens.get(5).getMetadata().getName(), is("varDataEncoding"));
        assertThat(valueOf(tokens.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(5).getOffset()), is(valueOf(0)));

        assertThat(tokens.get(6).getSignal(), is(Signal.END_FIELD));
        assertThat(tokens.get(6).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(tokens.get(6).getMetadata().getSchemaId()), is(valueOf(1404L)));
        assertThat(valueOf(tokens.get(6).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(6).getOffset()), is(valueOf(0)));

        assertThat(tokens.get(7).getSignal(), is(Signal.END_MESSAGE));
        assertThat(tokens.get(7).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(tokens.get(7).getMetadata().getSchemaId()), is(valueOf(1L)));
        assertThat(valueOf(tokens.get(7).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(7).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithRepeatingGroupWithEmbeddedDimentions()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicGroupSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */
        int groupIdx = 4;
        int dimensionsCompIdx = 5;
        int dimensionsBlEncIdx = 6;
        int dimensionsNigEncIdx = 7;

        List<Token> tokens = ir.getMessage(1);

        /* assert on the group token */
        assertThat(tokens.get(groupIdx).getSignal(), is(Signal.BEGIN_GROUP));
        assertThat(tokens.get(groupIdx).getMetadata().getName(), is("Entries"));
        assertThat(valueOf(tokens.get(groupIdx).getMetadata().getSchemaId()), is(valueOf(2L)));

        /* assert on the comp token for dimensions */
        assertThat(tokens.get(dimensionsCompIdx).getSignal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(dimensionsCompIdx).getMetadata().getName(), is("groupSizeEncoding"));

        /* assert on the enc token for dimensions blockLength */
        assertThat(tokens.get(dimensionsBlEncIdx).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(dimensionsBlEncIdx).getMetadata().getName(), is("blockLength"));

        /* assert on the enc token for dimensions numInGroup */
        assertThat(tokens.get(dimensionsNigEncIdx).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(dimensionsNigEncIdx).getMetadata().getName(), is("numInGroup"));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithRepeatingGroupWithEmbeddedDimensionsDefaultDimensionType()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("EmbeddedLengthAndCountFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, 9=field, ... */
        int groupIdx = 4;
        int dimensionsCompIdx = 5;
        int fieldInGroupIdx = 9;

        List<Token> tokens = ir.getMessage(1);

        assertThat(tokens.get(groupIdx).getSignal(), is(Signal.BEGIN_GROUP));
        assertThat(tokens.get(groupIdx).getMetadata().getName(), is("ListOrdGrp"));
        assertThat(valueOf(tokens.get(groupIdx).getMetadata().getSchemaId()), is(valueOf(73L)));

        assertThat(tokens.get(dimensionsCompIdx).getSignal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(dimensionsCompIdx).getMetadata().getName(), is("groupSizeEncoding"));

        assertThat(tokens.get(fieldInGroupIdx).getSignal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(fieldInGroupIdx).getMetadata().getName(), is("ClOrdID"));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthFieldWithEmbeddedLength()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("EmbeddedLengthAndCountFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=field, 5=comp, 6=enc, 7=enc, 8=compend, 9=fieldend */
        int lengthFieldIdx = 4;
        int lengthEncIdx = 6;
        int dataEncIdx = 7;

        List<Token> tokens = ir.getMessage(2);

        /* assert the varDataEncoding field node is formed correctly */
        assertThat(tokens.get(lengthFieldIdx).getSignal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(lengthFieldIdx).getMetadata().getName(), is("EncryptedPassword"));
        assertThat(valueOf(tokens.get(lengthFieldIdx).getMetadata().getSchemaId()), is(valueOf(1402L)));

        /* assert the length node has correct values */
        assertThat(tokens.get(lengthEncIdx).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(lengthEncIdx).getPrimitiveType(), is(PrimitiveType.UINT8));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(tokens.get(dataEncIdx).getSignal(), is(Signal.ENCODING));
        assertThat(tokens.get(dataEncIdx).getPrimitiveType(), is(PrimitiveType.CHAR));
    }
}