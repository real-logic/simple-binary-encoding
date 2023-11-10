using System;
using System.IO;
using Org.SbeTool.Sbe.Dll;
using Uk.Co.Real_logic.Sbe.Properties;

namespace SbePropertyTest {
  static class Test {
    static int Main(string[] args) {
      if (args.Length != 1) {
        Console.WriteLine("Usage: dotnet run -- $BINARY_FILE");
        return 1;
      }
      var binaryFile = args[0];
      var inputBytes = File.ReadAllBytes(binaryFile);
      var buffer = new DirectBuffer(inputBytes);
      var messageHeader = new MessageHeader();
      messageHeader.Wrap(buffer, 0, 0);
      var decoder = new TestMessage();
      decoder.WrapForDecode(buffer, 8, messageHeader.BlockLength, messageHeader.Version);
      var dto = TestMessageDto.DecodeWith(decoder);
      var outputBytes = new byte[inputBytes.Length];
      var outputBuffer = new DirectBuffer(outputBytes);
      var encoder = new TestMessage();
      encoder.WrapForEncodeAndApplyHeader(outputBuffer, 0, new MessageHeader());
      TestMessageDto.EncodeWith(encoder, dto);
      File.WriteAllBytes("output.dat", outputBytes);
      return 0;
    }
  }
}
