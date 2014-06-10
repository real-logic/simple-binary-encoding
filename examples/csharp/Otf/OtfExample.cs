using System;
using System.Collections.Generic;
using System.IO;
using Adaptive.SimpleBinaryEncoding.Examples.Generated;
using Adaptive.SimpleBinaryEncoding.Examples.generated_stub;
using Adaptive.SimpleBinaryEncoding.ir;
using Adaptive.SimpleBinaryEncoding.Otf;

namespace Adaptive.SimpleBinaryEncoding.Examples.Otf
{
    internal class OtfExample
    {
        private const int ActingVersion = 0;
        private const int MsgBufferCapacity = 4*1024;
        private static readonly MessageHeader MESSAGE_HEADER = new MessageHeader();
        private static readonly Car Car = new Car();

        public static void Main()
        {
            Console.WriteLine("\n*** OTF Example ***\n");

            // Load a schema (serialized IR) as if we just got it off the wire (you can create a serialized IR schema with SbeTool)
            byte[] encodedSchemaBuffer = LoadSchema();

            // Encode up a message as if we just got it off the wire
            var encodedMsgBuffer = new byte[MsgBufferCapacity];
            EncodeTestMessage(encodedMsgBuffer);

            // Now lets decode the schema IR so we have IR objects.
            //encodedSchemaBuffer.flip();
            IntermediateRepresentation ir = DecodeIr(encodedSchemaBuffer);

            // From the IR we can create OTF decoder for message headers.
            var headerDecoder = new OtfHeaderDecoder(ir.HeaderStructure);

            // Now we have IR we can read the message header
            int bufferOffset = 0;
            var buffer = new DirectBuffer(encodedMsgBuffer);

            int templateId = headerDecoder.GetTemplateId(buffer, bufferOffset);
            int schemaId = headerDecoder.GetSchemaId(buffer, bufferOffset);
            int actingVersion = headerDecoder.GetSchemaVersion(buffer, bufferOffset);
            int blockLength = headerDecoder.GetBlockLength(buffer, bufferOffset);

            bufferOffset += headerDecoder.Size;

            // Given the header information we can select the appropriate message template to do the decode.
            // The OTF Java classes are thread safe so the same instances can be reused across multiple threads.

            IList<Token> msgTokens = ir.GetMessage(templateId);

            bufferOffset = OtfMessageDecoder.Decode(buffer, bufferOffset, actingVersion, blockLength, msgTokens,
                new ExampleTokenListener());
        }

        private static byte[] LoadSchema()
        {
            return File.ReadAllBytes(Path.Combine(Environment.CurrentDirectory, "example-schema.sbeir"));
        }

        private static void EncodeTestMessage(byte[] buffer)
        {
            var directBuffer = new DirectBuffer(buffer);

            int bufferOffset = 0;
            MESSAGE_HEADER.Wrap(directBuffer, bufferOffset, ActingVersion);
            MESSAGE_HEADER.BlockLength = Car.BlockLength;
            MESSAGE_HEADER.TemplateId = Car.TemplateId;
            MESSAGE_HEADER.SchemaId = Car.SchemaId;
            MESSAGE_HEADER.Version = Car.Schema_Version;

            bufferOffset += MessageHeader.Size;

            bufferOffset += ExampleUsingGeneratedStub.Encode(Car, directBuffer, bufferOffset);

            //buffer.po (bufferOffset);
        }

        private static IntermediateRepresentation DecodeIr(byte[] buffer)
        {
            var irDecoder = new IrDecoder(buffer);
            return irDecoder.Decode();
        }
    }
}