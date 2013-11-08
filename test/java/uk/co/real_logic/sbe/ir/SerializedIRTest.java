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

import org.junit.Ignore;
import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.nio.ByteBuffer;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class SerializedIrTest
{
    private static final int CAPACITY = 8192;

    @Test
    public void shouldSerializeIr()
            throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation ir = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, ir);

        serializer.serialize();
    }

    @Test
    public void shouldSerializeThenDeserializeIr()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation serIr = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, serIr);

        serializer.serialize();
        buffer.flip();

        Deserializer deserializer = new Deserializer(buffer);
        IntermediateRepresentation deserIr = deserializer.deserialize();
    }

    @Test @Ignore("will fail on checkPosition at end of buffer")
    public void shouldHandleRightSizedBuffer()
        throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("BasicSchemaFileTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation serIr = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, serIr);

        serializer.serialize();
        buffer.flip();

        ByteBuffer readBuffer = ByteBuffer.allocateDirect(buffer.remaining());
        readBuffer.put(buffer);
        readBuffer.flip();

        Deserializer deserializer = new Deserializer(readBuffer);
        IntermediateRepresentation deserIr = deserializer.deserialize();
    }

    @Test
    public void shouldDeserializeCorrectFrame()
            throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation serIr = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, serIr);

        serializer.serialize();
        buffer.flip();

        Deserializer deserializer = new Deserializer(buffer);
        IntermediateRepresentation deserIr = deserializer.deserialize();

        assertThat(Integer.valueOf(deserIr.version()), is(Integer.valueOf(serIr.version())));
        assertThat(deserIr.packageName(), is(serIr.packageName()));
    }

    private void checkTokenEquality(final Token lhs, final Token rhs)
    {
        assertThat(lhs.name(), is(rhs.name()));
        assertThat(Integer.valueOf(lhs.version()), is(Integer.valueOf(rhs.version())));
        assertThat(Integer.valueOf(lhs.offset()), is(Integer.valueOf(rhs.offset())));
        assertThat(Long.valueOf(lhs.schemaId()), is(Long.valueOf(rhs.schemaId())));
        assertThat(lhs.signal(), is(rhs.signal()));
        assertThat(Integer.valueOf(lhs.size()), is(Integer.valueOf(rhs.size())));

        assertThat(lhs.encoding().byteOrder(), is(rhs.encoding().byteOrder()));
        assertThat(lhs.encoding().primitiveType(), is(rhs.encoding().primitiveType()));
        assertThat(lhs.encoding().constVal(), is(rhs.encoding().constVal()));
        assertThat(lhs.encoding().minVal(), is(rhs.encoding().minVal()));
        assertThat(lhs.encoding().maxVal(), is(rhs.encoding().maxVal()));
        assertThat(lhs.encoding().nullVal(), is(rhs.encoding().nullVal()));
        assertThat(lhs.encoding().characterEncoding(), is(rhs.encoding().characterEncoding()));
    }

    @Test
    public void shouldDeserializeCorrectHeader()
            throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation serIr = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, serIr);

        serializer.serialize();
        buffer.flip();

        Deserializer deserializer = new Deserializer(buffer);
        IntermediateRepresentation deserIr = deserializer.deserialize();

        assertThat(Integer.valueOf(deserIr.header().size()), is(Integer.valueOf(serIr.header().size())));
        for (int i = 0, size = deserIr.header().size(); i < size; i++)
        {
            checkTokenEquality(deserIr.header().get(i), serIr.header().get(i));
        }
    }

    @Test
    public void shouldDeserializeCorrectMessages()
            throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        IntermediateRepresentation serIr = irg.generate(schema);
        ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        Serializer serializer = new Serializer(buffer, serIr);

        serializer.serialize();
        buffer.flip();

        Deserializer deserializer = new Deserializer(buffer);
        IntermediateRepresentation deserIr = deserializer.deserialize();

        assertThat(Integer.valueOf(deserIr.messages().size()), is(Integer.valueOf(serIr.messages().size())));
        for (final List<Token> deserTokenList : deserIr.messages())
        {
            final List<Token> serTokenList = serIr.getMessage(deserTokenList.get(0).schemaId());

            assertThat(Integer.valueOf(deserTokenList.size()), is(Integer.valueOf(serTokenList.size())));
            for (int i = 0, size = deserTokenList.size(); i < size; i++)
            {
                checkTokenEquality(deserTokenList.get(i), serTokenList.get(i));
            }
        }
    }
}
