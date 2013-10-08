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
        List<Token> tokens = ir.header();

        assertThat(valueOf(tokens.size()), is(valueOf(6)));

        /* assert all elements of node 0 */
        assertThat(tokens.get(0).signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(0).name(), is("messageHeader"));
        assertThat(valueOf(tokens.get(0).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).offset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(tokens.get(1).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(1).name(), is("blockLength"));
        assertThat(tokens.get(1).primitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(tokens.get(1).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(2)));
        assertThat(valueOf(tokens.get(1).offset()), is(valueOf(0)));
        assertThat(tokens.get(1).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 2 */
        assertThat(tokens.get(2).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(2).name(), is("templateId"));
        assertThat(tokens.get(2).primitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(tokens.get(2).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(2)));
        assertThat(valueOf(tokens.get(2).offset()), is(valueOf(2)));
        assertThat(tokens.get(2).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(tokens.get(3).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(3).name(), is("version"));
        assertThat(tokens.get(3).primitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(3).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(3).offset()), is(valueOf(4)));
        assertThat(tokens.get(3).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 4 */
        assertThat(tokens.get(4).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(4).name(), is("reserved"));
        assertThat(tokens.get(4).primitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(4).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(4).offset()), is(valueOf(5)));
        assertThat(tokens.get(4).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 5 */
        assertThat(tokens.get(5).signal(), is(Signal.END_COMPOSITE));
        assertThat(tokens.get(5).name(), is("messageHeader"));
        assertThat(valueOf(tokens.get(5).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(5).offset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForBasicMessage()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);

        List<Token> tokens = ir.message(50001);

        assertThat(valueOf(tokens.size()), is(valueOf(5)));

        /* assert all elements of node 0 */
        assertThat(tokens.get(0).signal(), is(Signal.BEGIN_MESSAGE));
        assertThat(tokens.get(0).name(), is("TestMessage50001"));
        assertThat(valueOf(tokens.get(0).schemaId()), is(valueOf(50001L)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).offset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(tokens.get(1).signal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(1).name(), is("Tag40001"));
        assertThat(valueOf(tokens.get(1).schemaId()), is(valueOf(40001L)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(1).offset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(tokens.get(2).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(2).name(), is("uint32"));
        assertThat(tokens.get(2).primitiveType(), is(PrimitiveType.UINT32));
        assertThat(valueOf(tokens.get(2).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(4)));
        assertThat(valueOf(tokens.get(2).offset()), is(valueOf(0)));
        assertThat(tokens.get(2).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        /* assert all elements of node 3 */
        assertThat(tokens.get(3).signal(), is(Signal.END_FIELD));
        assertThat(tokens.get(3).name(), is("Tag40001"));
        assertThat(valueOf(tokens.get(3).schemaId()), is(valueOf(40001L)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(3).offset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(tokens.get(4).signal(), is(Signal.END_MESSAGE));
        assertThat(tokens.get(4).name(), is("TestMessage50001"));
        assertThat(valueOf(tokens.get(4).schemaId()), is(valueOf(50001L)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(4).offset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthField()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicVariableLengthSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);

        List<Token> tokens = ir.message(1);

        assertThat(valueOf(tokens.size()), is(valueOf(8)));

        assertThat(tokens.get(0).signal(), is(Signal.BEGIN_MESSAGE));
        assertThat(tokens.get(0).name(), is("TestMessage1"));
        assertThat(valueOf(tokens.get(0).schemaId()), is(valueOf(1L)));
        assertThat(valueOf(tokens.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(0).offset()), is(valueOf(0)));

        assertThat(tokens.get(1).signal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(1).name(), is("encryptedNewPassword"));
        assertThat(valueOf(tokens.get(1).schemaId()), is(valueOf(1404L)));
        assertThat(valueOf(tokens.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(1).offset()), is(valueOf(0)));

        assertThat(tokens.get(2).signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(2).name(), is("varDataEncoding"));
        assertThat(valueOf(tokens.get(2).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(2).offset()), is(valueOf(0)));

        assertThat(tokens.get(3).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(3).name(), is("length"));
        assertThat(tokens.get(3).primitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(tokens.get(3).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(tokens.get(3).offset()), is(valueOf(0)));
        assertThat(tokens.get(3).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        assertThat(tokens.get(4).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(4).name(), is("varData"));
        assertThat(tokens.get(4).primitiveType(), is(PrimitiveType.CHAR));
        assertThat(valueOf(tokens.get(4).schemaId()), is(valueOf(Token.INVALID_ID)));
        assertThat(valueOf(tokens.get(4).size()), is(valueOf(-1)));
        assertThat(valueOf(tokens.get(4).offset()), is(valueOf(1)));
        assertThat(tokens.get(4).byteOrder(), is(ByteOrder.LITTLE_ENDIAN));

        assertThat(tokens.get(5).signal(), is(Signal.END_COMPOSITE));
        assertThat(tokens.get(5).name(), is("varDataEncoding"));
        assertThat(valueOf(tokens.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(5).offset()), is(valueOf(0)));

        assertThat(tokens.get(6).signal(), is(Signal.END_FIELD));
        assertThat(tokens.get(6).name(), is("encryptedNewPassword"));
        assertThat(valueOf(tokens.get(6).schemaId()), is(valueOf(1404L)));
        assertThat(valueOf(tokens.get(6).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(6).offset()), is(valueOf(0)));

        assertThat(tokens.get(7).signal(), is(Signal.END_MESSAGE));
        assertThat(tokens.get(7).name(), is("TestMessage1"));
        assertThat(valueOf(tokens.get(7).schemaId()), is(valueOf(1L)));
        assertThat(valueOf(tokens.get(7).size()), is(valueOf(0)));
        assertThat(valueOf(tokens.get(7).offset()), is(valueOf(0)));
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

        List<Token> tokens = ir.message(1);

        /* assert on the group token */
        assertThat(tokens.get(groupIdx).signal(), is(Signal.BEGIN_GROUP));
        assertThat(tokens.get(groupIdx).name(), is("Entries"));
        assertThat(valueOf(tokens.get(groupIdx).schemaId()), is(valueOf(2L)));

        /* assert on the comp token for dimensions */
        assertThat(tokens.get(dimensionsCompIdx).signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(dimensionsCompIdx).name(), is("groupSizeEncoding"));

        /* assert on the enc token for dimensions blockLength */
        assertThat(tokens.get(dimensionsBlEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(dimensionsBlEncIdx).name(), is("blockLength"));

        /* assert on the enc token for dimensions numInGroup */
        assertThat(tokens.get(dimensionsNigEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(dimensionsNigEncIdx).name(), is("numInGroup"));
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

        List<Token> tokens = ir.message(1);

        assertThat(tokens.get(groupIdx).signal(), is(Signal.BEGIN_GROUP));
        assertThat(tokens.get(groupIdx).name(), is("ListOrdGrp"));
        assertThat(valueOf(tokens.get(groupIdx).schemaId()), is(valueOf(73L)));

        assertThat(tokens.get(dimensionsCompIdx).signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(tokens.get(dimensionsCompIdx).name(), is("groupSizeEncoding"));

        assertThat(tokens.get(fieldInGroupIdx).signal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(fieldInGroupIdx).name(), is("ClOrdID"));
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

        List<Token> tokens = ir.message(2);

        /* assert the varDataEncoding field node is formed correctly */
        assertThat(tokens.get(lengthFieldIdx).signal(), is(Signal.BEGIN_FIELD));
        assertThat(tokens.get(lengthFieldIdx).name(), is("EncryptedPassword"));
        assertThat(valueOf(tokens.get(lengthFieldIdx).schemaId()), is(valueOf(1402L)));

        /* assert the length node has correct values */
        assertThat(tokens.get(lengthEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(lengthEncIdx).primitiveType(), is(PrimitiveType.UINT8));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(tokens.get(dataEncIdx).signal(), is(Signal.ENCODING));
        assertThat(tokens.get(dataEncIdx).primitiveType(), is(PrimitiveType.CHAR));
    }
}