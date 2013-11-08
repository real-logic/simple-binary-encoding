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

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class SerializedIRTest
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

    @Test @Ignore
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
}
