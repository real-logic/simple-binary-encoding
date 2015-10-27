/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.sbe.json;

import baseline.*;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.IrDecoder;
import uk.co.real_logic.sbe.ir.IrEncoder;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static junit.framework.TestCase.assertEquals;

public class JsonPrinterTest
{
    private static final MessageHeaderEncoder MESSAGE_HEADER = new MessageHeaderEncoder();
    private static final CarEncoder CAR = new CarEncoder();
    private static final int MSG_BUFFER_CAPACITY = 4 * 1024;
    private static final int SCHEMA_BUFFER_CAPACITY = 16 * 1024;

    private static byte[] vehicleCode;
    private static byte[] manufacturerCode;
    private static byte[] make;
    private static byte[] model;

    @BeforeClass
    public static void setupExampleData()
    {
        try
        {
            vehicleCode = "abcdef".getBytes(CarEncoder.vehicleCodeCharacterEncoding());
            manufacturerCode = "123".getBytes(EngineEncoder.manufacturerCodeCharacterEncoding());
            make = "Honda".getBytes(CarEncoder.makeCharacterEncoding());
            model = "Civic VTi".getBytes(CarEncoder.modelCharacterEncoding());
        }
        catch (final UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void exampleMessagePrintedAsJson() throws Exception
    {
        final ByteBuffer encodedSchemaBuffer = ByteBuffer.allocateDirect(SCHEMA_BUFFER_CAPACITY);
        encodeSchema(encodedSchemaBuffer);

        final ByteBuffer encodedMsgBuffer = ByteBuffer.allocateDirect(MSG_BUFFER_CAPACITY);
        encodeTestMessage(encodedMsgBuffer);

        encodedSchemaBuffer.flip();
        final Ir ir = decodeIr(encodedSchemaBuffer);

        final JsonPrinter printer = new JsonPrinter(ir);
        final String result = printer.print(encodedMsgBuffer);
        assertEquals(
            "{\n" +
            "    \"serialNumber\": 1234,\n" +
            "    \"modelYear\": 2013,\n" +
            "    \"someNumbers\": [0, 1, 2, 3, 4],\n" +
            "    \"vehicleCode\": \"abcdef\",\n" +
            "    \"capacity\": 2000,\n" +
            "    \"numCylinders\": 4,\n" +
            "    \"maxRpm\": 9000,\n" +
            "    \"manufacturerCode\": \"123\",\n" +
            "    \"fuel\": Petrol,\n" +
            "    \"fuelFigures\": [\n" +
            "    {\n" +
            "        \"speed\": 30,\n" +
            "        \"mpg\": 35.9\n" +
            "    },\n" +
            "    {\n" +
            "        \"speed\": 55,\n" +
            "        \"mpg\": 49.0\n" +
            "    },\n" +
            "    {\n" +
            "        \"speed\": 75,\n" +
            "        \"mpg\": 40.0\n" +
            "    }],\n" +
            "    \"performanceFigures\": [\n" +
            "    {\n" +
            "        \"octaneRating\": 95,\n" +
            "        \"acceleration\": [\n" +
            "        {\n" +
            "            \"mph\": 30,\n" +
            "            \"seconds\": 4.0\n" +
            "        },\n" +
            "        {\n" +
            "            \"mph\": 60,\n" +
            "            \"seconds\": 7.5\n" +
            "        },\n" +
            "        {\n" +
            "            \"mph\": 100,\n" +
            "            \"seconds\": 12.2\n" +
            "        }]\n" +
            "    },\n" +
            "    {\n" +
            "        \"octaneRating\": 99,\n" +
            "        \"acceleration\": [\n" +
            "        {\n" +
            "            \"mph\": 30,\n" +
            "            \"seconds\": 3.8\n" +
            "        },\n" +
            "        {\n" +
            "            \"mph\": 60,\n" +
            "            \"seconds\": 7.1\n" +
            "        },\n" +
            "        {\n" +
            "            \"mph\": 100,\n" +
            "            \"seconds\": 11.8\n" +
            "        }]\n" +
            "    }],\n" +
            "    \"make\": \"Honda\",\n" +
            "    \"model\": \"Civic VTi\",\n" +
            "    \"activationCode\": \"\"\n" +
            "}",
            result);
    }

    private static void encodeSchema(final ByteBuffer buffer)
        throws Exception
    {
        try (final InputStream in = new FileInputStream("examples/resources/example-schema.xml"))
        {
            final MessageSchema schema = XmlSchemaParser.parse(in, ParserOptions.DEFAULT);
            final Ir ir = new IrGenerator().generate(schema);
            try (final IrEncoder irEncoder = new IrEncoder(buffer, ir))
            {
                irEncoder.encode();
            }
        }
    }

    private static void encodeTestMessage(final ByteBuffer buffer)
    {
        final UnsafeBuffer directBuffer = new UnsafeBuffer(buffer);

        int bufferOffset = 0;
        MESSAGE_HEADER.wrap(directBuffer, bufferOffset)
                      .blockLength(CAR.sbeBlockLength())
                      .templateId(CAR.sbeTemplateId())
                      .schemaId(CAR.sbeSchemaId())
                      .version(CAR.sbeSchemaVersion());

        bufferOffset += MESSAGE_HEADER.encodedLength();

        final int srcOffset = 0;

        CAR.wrap(directBuffer, bufferOffset)
           .serialNumber(1234)
           .modelYear(2013)
           .available(BooleanType.TRUE)
           .code(Model.A)
           .putVehicleCode(vehicleCode, srcOffset);

        for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
        {
            CAR.someNumbers(i, i);
        }

        CAR.extras()
           .clear()
           .cruiseControl(true)
           .sportsPack(true)
           .sunRoof(false);

        CAR.engine()
           .capacity(2000)
           .numCylinders((short)4)
           .putManufacturerCode(manufacturerCode, srcOffset);

        CAR.fuelFiguresCount(3)
           .next().speed(30).mpg(35.9f)
           .next().speed(55).mpg(49.0f)
           .next().speed(75).mpg(40.0f);

        final CarEncoder.PerformanceFiguresEncoder perfFigures = CAR.performanceFiguresCount(2);
        perfFigures.next()
                   .octaneRating((short)95)
                   .accelerationCount(3)
                   .next().mph(30).seconds(4.0f)
                   .next().mph(60).seconds(7.5f)
                   .next().mph(100).seconds(12.2f);
        perfFigures.next()
                   .octaneRating((short)99)
                   .accelerationCount(3)
                   .next().mph(30).seconds(3.8f)
                   .next().mph(60).seconds(7.1f)
                   .next().mph(100).seconds(11.8f);

        CAR.make(new String(make));
        CAR.putModel(model, srcOffset, model.length);

        bufferOffset += CAR.encodedLength();

        buffer.position(bufferOffset);
    }

    private static Ir decodeIr(final ByteBuffer buffer)
        throws IOException
    {
        try (final IrDecoder irDecoder = new IrDecoder(buffer))
        {
            return irDecoder.decode();
        }
    }
}
