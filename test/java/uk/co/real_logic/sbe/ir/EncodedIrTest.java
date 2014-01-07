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
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.nio.ByteBuffer;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class EncodedIrTest
{
    private static final int CAPACITY = 8192;

    @Test
    public void shouldEncodeIr()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
    }

    @Test
    public void shouldEncodeThenDecodeIr()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
        buffer.flip();

        IrDecoder decoder = new IrDecoder(buffer);
        IntermediateRepresentation decodedIr = decoder.decode();
    }

    @Test
    public void shouldHandleRightSizedBuffer()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
        buffer.flip();

        ByteBuffer readBuffer = ByteBuffer.allocateDirect(buffer.remaining());
        readBuffer.put(buffer);
        readBuffer.flip();

        IrDecoder irDecoder = new IrDecoder(readBuffer);
        IntermediateRepresentation decodedIr = irDecoder.decode();
    }

    @Test
    public void shouldDecodeCorrectFrame()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
        buffer.flip();

        IrDecoder irDecoder = new IrDecoder(buffer);
        IntermediateRepresentation decodedIr = irDecoder.decode();

        assertThat(Integer.valueOf(decodedIr.version()), is(Integer.valueOf(ir.version())));
        assertThat(decodedIr.packageName(), is(ir.packageName()));
        assertThat(decodedIr.namespaceName(), is(ir.namespaceName()));
    }

    private void assertEqual(final Token lhs, final Token rhs)
    {
        assertThat(lhs.name(), is(rhs.name()));
        assertThat(Integer.valueOf(lhs.version()), is(Integer.valueOf(rhs.version())));
        assertThat(Integer.valueOf(lhs.offset()), is(Integer.valueOf(rhs.offset())));
        assertThat(Long.valueOf(lhs.schemaId()), is(Long.valueOf(rhs.schemaId())));
        assertThat(lhs.signal(), is(rhs.signal()));
        assertThat(Integer.valueOf(lhs.size()), is(Integer.valueOf(rhs.size())));

        assertThat(lhs.encoding().byteOrder(), is(rhs.encoding().byteOrder()));
        assertThat(lhs.encoding().primitiveType(), is(rhs.encoding().primitiveType()));
        assertThat(lhs.encoding().presence(), is(rhs.encoding().presence()));
        assertThat(lhs.encoding().constVal(), is(rhs.encoding().constVal()));
        assertThat(lhs.encoding().minVal(), is(rhs.encoding().minVal()));
        assertThat(lhs.encoding().maxVal(), is(rhs.encoding().maxVal()));
        assertThat(lhs.encoding().nullVal(), is(rhs.encoding().nullVal()));
        assertThat(lhs.encoding().characterEncoding(), is(rhs.encoding().characterEncoding()));
        assertThat(lhs.encoding().epoch(), is(rhs.encoding().epoch()));
        assertThat(lhs.encoding().timeUnit(), is(rhs.encoding().timeUnit()));
        assertThat(lhs.encoding().semanticType(), is(rhs.encoding().semanticType()));
    }

    @Test
    public void shouldDecodeCorrectHeader()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
        buffer.flip();

        final IrDecoder irDecoder = new IrDecoder(buffer);
        final IntermediateRepresentation decodedIr = irDecoder.decode();
        final List<Token> tokens = decodedIr.headerStructure().tokens();

        assertThat(Integer.valueOf(tokens.size()), is(Integer.valueOf(ir.headerStructure().tokens().size())));
        for (int i = 0, size = tokens.size(); i < size; i++)
        {
            assertEqual(tokens.get(i), ir.headerStructure().tokens().get(i));
        }
    }

    @Test
    public void shouldDecodeCorrectMessages()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        IrEncoder irEncoder = new IrEncoder(buffer, ir);

        irEncoder.encode();
        buffer.flip();

        IrDecoder irDecoder = new IrDecoder(buffer);
        IntermediateRepresentation decodedIr = irDecoder.decode();

        assertThat(Integer.valueOf(decodedIr.messages().size()), is(Integer.valueOf(ir.messages().size())));
        for (final List<Token> decodedTokenList : decodedIr.messages())
        {
            final List<Token> tokens = ir.getMessage(decodedTokenList.get(0).schemaId());

            assertThat(Integer.valueOf(decodedTokenList.size()), is(Integer.valueOf(tokens.size())));
            for (int i = 0, size = decodedTokenList.size(); i < size; i++)
            {
                assertEqual(decodedTokenList.get(i), tokens.get(i));
            }
        }
    }
}
