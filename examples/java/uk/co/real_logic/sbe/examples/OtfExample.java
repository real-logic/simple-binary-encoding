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
package uk.co.real_logic.sbe.examples;

import baseline.Car;
import baseline.MessageHeader;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.*;
import uk.co.real_logic.sbe.otf.OtfGroupSizeDecoder;
import uk.co.real_logic.sbe.otf.OtfMessageDecoder;
import uk.co.real_logic.sbe.otf.OtfHeaderDecoder;
import uk.co.real_logic.sbe.otf.OtfVarDataDecoder;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

import java.io.*;

import java.nio.ByteBuffer;
import java.util.List;

public class OtfExample
{
    private static final MessageHeader MESSAGE_HEADER = new MessageHeader();
    private static final Car CAR = new Car();
    private static final int ACTING_VERSION = 0;
    private static final int MSG_BUFFER_CAPACITY = 4 * 1024;
    private static final int SCHEMA_BUFFER_CAPACITY = 16 * 1024;

    public static void main(final String[] args) throws Exception
    {
        System.out.println("\n*** OTF Example ***\n");

        // Encode up message and schema as if we just got them off the wire.
        final ByteBuffer encodedSchemaBuffer = ByteBuffer.allocateDirect(SCHEMA_BUFFER_CAPACITY);
        encodeSchema(encodedSchemaBuffer);

        final ByteBuffer encodedMsgBuffer = ByteBuffer.allocateDirect(MSG_BUFFER_CAPACITY);
        encodeTestMessage(encodedMsgBuffer);

        // Now lets decode the schema IR so we have IR objects.
        encodedSchemaBuffer.flip();
        final IntermediateRepresentation ir = decodeIr(encodedSchemaBuffer);

        // Now we have IR we can read the message header
        int bufferOffset = 0;
        final DirectBuffer buffer = new DirectBuffer(encodedMsgBuffer);
        final OtfHeaderDecoder headerDecoder = new OtfHeaderDecoder(ir.headerStructure());

        final int templateId = headerDecoder.getTemplateId(buffer, bufferOffset);
        final int actingVersion = headerDecoder.getTemplateVersion(buffer, bufferOffset);
        final int blockLength = headerDecoder.getBlockLength(buffer, bufferOffset);

        bufferOffset += headerDecoder.size();

        // Given the header information we can select the appropriate message template to do the decode.
        // The OTF Java classes are thread safe so the same instances can be reused across multiple threads.
        final OtfGroupSizeDecoder groupSizeDecoder = new OtfGroupSizeDecoder(ir.getType(OtfGroupSizeDecoder.GROUP_SIZE_ENCODING_NAME));
        final OtfVarDataDecoder varDataDecoder = new OtfVarDataDecoder(ir.getType(OtfVarDataDecoder.VAR_DATA_ENCODING_NAME));
        final List<Token> msgTokens = ir.getMessage(templateId);

        final OtfMessageDecoder messageDecoder = new OtfMessageDecoder(groupSizeDecoder, varDataDecoder);
        bufferOffset = messageDecoder.decode(buffer,
                                             bufferOffset,
                                             actingVersion,
                                             blockLength,
                                             msgTokens,
                                             new ExampleTokenListener(new PrintWriter(System.out, true)));

        if (bufferOffset != encodedMsgBuffer.position())
        {
            throw new IllegalStateException("Message not fully decoded");
        }
    }

    private static void encodeSchema(final ByteBuffer buffer)
        throws Exception
    {
        try (final InputStream in = new FileInputStream("examples/resources/TestSchema.xml"))
        {
            final MessageSchema schema = XmlSchemaParser.parse(in);
            final IntermediateRepresentation ir = new IrGenerator().generate(schema);
            new Encoder(buffer, ir).encode();
        }
    }

    private static void encodeTestMessage(final ByteBuffer buffer)
    {
        final DirectBuffer directBuffer = new DirectBuffer(buffer);

        int bufferOffset = 0;
        MESSAGE_HEADER.wrap(directBuffer, bufferOffset, ACTING_VERSION)
                      .blockLength(CAR.blockLength())
                      .templateId(CAR.templateId())
                      .version(CAR.templateVersion());

        bufferOffset += MESSAGE_HEADER.size();

        bufferOffset += ExampleUsingGeneratedStub.encode(CAR, directBuffer, bufferOffset);

        buffer.position(bufferOffset);
    }

    private static IntermediateRepresentation decodeIr(final ByteBuffer buffer)
        throws IOException
    {
        final Decoder decoder = new Decoder(buffer);
        return decoder.decode();
    }
}
