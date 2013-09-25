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
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.io.InputStream;
import java.util.List;

import static java.lang.Integer.valueOf;
import static java.lang.Long.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.*;

public class BasicXmlIrGenerationTest
{
    @Test
    public void shouldGenerateCorrectIrForMessageHeader()
        throws Exception
    {
        MessageSchema schema = parse(getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForHeader(schema);

        assertThat(valueOf(ir.size()), is(valueOf(6)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.COMPOSITE_START));
        assertThat(ir.get(0).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(0).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(1).getMetadata().getName(), is("blockLength"));
        assertThat(ir.get(1).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(ir.get(1).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(2).getMetadata().getName(), is("templateId"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(valueOf(ir.get(2).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(2)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(2)));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(3).getMetadata().getName(), is("version"));
        assertThat(ir.get(3).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(3).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(4)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(4).getMetadata().getName(), is("reserved"));
        assertThat(ir.get(4).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(5)));

        /* assert all elements of node 5 */
        assertThat(ir.get(5).getMetadata().getSignal(), is(Token.Signal.COMPOSITE_END));
        assertThat(ir.get(5).getMetadata().getName(), is("messageHeader"));
        assertThat(valueOf(ir.get(5).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(5).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(5).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForBasicMessage()
        throws Exception
    {
        MessageSchema schema = parse(getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema.getMessage(50001));

        assertThat(valueOf(ir.size()), is(valueOf(5)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.MESSAGE_START));
        assertThat(ir.get(0).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(ir.get(0).getMetadata().getId()), is(valueOf(50001L)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.FIELD_START));
        assertThat(ir.get(1).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(1).getMetadata().getId()), is(valueOf(40001L)));
        assertThat(ir.get(1).getMetadata().getFixUsage(), is("int"));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(2).getMetadata().getName(), is("uint32"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT32));
        assertThat(valueOf(ir.get(2).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(4)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(0)));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.FIELD_END));
        assertThat(ir.get(3).getMetadata().getName(), is("Tag40001"));
        assertThat(valueOf(ir.get(3).getMetadata().getId()), is(valueOf(40001L)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.MESSAGE_END));
        assertThat(ir.get(4).getMetadata().getName(), is("TestMessage50001"));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(50001L)));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithVariableLengthField()
        throws Exception
    {
        MessageSchema schema = parse(getLocalResource("BasicVariableLengthSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema.getMessage(1));

        assertThat(valueOf(ir.size()), is(valueOf(8)));

        /* assert all elements of node 0 */
        assertThat(ir.get(0).getMetadata().getSignal(), is(Token.Signal.MESSAGE_START));
        assertThat(ir.get(0).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(ir.get(0).getMetadata().getId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(0).getMetadata().getIrId()), is(valueOf(0L)));
        assertThat(valueOf(ir.get(0).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(0).getOffset()), is(valueOf(0)));

        /* assert all elements of node 1 */
        assertThat(ir.get(1).getMetadata().getSignal(), is(Token.Signal.FIELD_START));
        assertThat(ir.get(1).getMetadata().getName(), is("EncryptedNewPasswordLen"));
        assertThat(valueOf(ir.get(1).getMetadata().getId()), is(valueOf(1403L)));
        assertThat(valueOf(ir.get(1).getMetadata().getIrId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(1).getMetadata().getXRefIrId()), is(valueOf(2L)));
        assertThat(ir.get(1).getMetadata().getFixUsage(), is("Length"));
        assertThat(valueOf(ir.get(1).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(1).getOffset()), is(valueOf(0)));

        /* assert all elements of node 2 */
        assertThat(ir.get(2).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(2).getMetadata().getName(), is("length"));
        assertThat(ir.get(2).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(valueOf(ir.get(2).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(2).size()), is(valueOf(1)));
        assertThat(valueOf(ir.get(2).getOffset()), is(valueOf(0)));

        /* assert all elements of node 3 */
        assertThat(ir.get(3).getMetadata().getSignal(), is(Token.Signal.FIELD_END));
        assertThat(ir.get(3).getMetadata().getName(), is("EncryptedNewPasswordLen"));
        assertThat(valueOf(ir.get(3).getMetadata().getId()), is(valueOf(1403L)));
        assertThat(valueOf(ir.get(3).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(3).getOffset()), is(valueOf(0)));

        /* assert all elements of node 4 */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.FIELD_START));
        assertThat(ir.get(4).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(1404L)));
        assertThat(valueOf(ir.get(4).getMetadata().getIrId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(4).getMetadata().getXRefIrId()), is(valueOf(1L)));
        assertThat(ir.get(4).getMetadata().getFixUsage(), is("data"));
        assertThat(valueOf(ir.get(4).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(4).getOffset()), is(valueOf(0)));

        /* assert all elements of node 5 */
        assertThat(ir.get(5).getMetadata().getSignal(), is(Token.Signal.NONE));
        assertThat(ir.get(5).getMetadata().getName(), is("rawData"));
        assertThat(ir.get(5).getPrimitiveType(), is(PrimitiveType.CHAR));
        assertThat(valueOf(ir.get(5).getMetadata().getId()), is(valueOf(Metadata.INVALID_ID)));
        assertThat(valueOf(ir.get(5).size()), is(valueOf(-1)));
        assertThat(valueOf(ir.get(5).getOffset()), is(valueOf(1)));

        /* assert all elements of node 6 */
        assertThat(ir.get(6).getMetadata().getSignal(), is(Token.Signal.FIELD_END));
        assertThat(ir.get(6).getMetadata().getName(), is("EncryptedNewPassword"));
        assertThat(valueOf(ir.get(6).getMetadata().getId()), is(valueOf(1404L)));
        assertThat(valueOf(ir.get(6).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(6).getOffset()), is(valueOf(0)));

        /* assert all elements of node 7 */
        assertThat(ir.get(7).getMetadata().getSignal(), is(Token.Signal.MESSAGE_END));
        assertThat(ir.get(7).getMetadata().getName(), is("TestMessage1"));
        assertThat(valueOf(ir.get(7).getMetadata().getId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(7).size()), is(valueOf(0)));
        assertThat(valueOf(ir.get(7).getOffset()), is(valueOf(0)));
    }

    @Test
    public void shouldGenerateCorrectIrForMessageWithRepeatingGroup()
        throws Exception
    {
        MessageSchema schema = parse(getLocalResource("BasicGroupSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();

        List<Token> ir = irg.generateForMessage(schema.getMessage(1));

        /* assert the NoEntries node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(4).getMetadata().getSignal(), is(Token.Signal.FIELD_START));
        assertThat(ir.get(4).getMetadata().getName(), is("NoEntries"));
        assertThat(valueOf(ir.get(4).getMetadata().getId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(4).getMetadata().getIrId()), is(valueOf(1L)));
        assertThat(valueOf(ir.get(4).getMetadata().getXRefIrId()), is(valueOf(2L)));
        assertThat(ir.get(4).getMetadata().getFixUsage(), is("NumInGroup"));

        /* assert the group node has the right IrId and xRefIrId, etc. */
        assertThat(ir.get(7).getMetadata().getSignal(), is(Token.Signal.GROUP_START));
        assertThat(ir.get(7).getMetadata().getName(), is("Entries"));
        assertThat(valueOf(ir.get(7).getMetadata().getIrId()), is(valueOf(2L)));
        assertThat(valueOf(ir.get(7).getMetadata().getXRefIrId()), is(valueOf(1L)));
    }

    private static InputStream getLocalResource(final String name)
    {
        InputStream in = BasicXmlIrGenerationTest.class.getClassLoader().getResourceAsStream(name);

        if (in == null)
        {
            throw new RuntimeException("could not find " + name);
        }

        return in;
    }
}