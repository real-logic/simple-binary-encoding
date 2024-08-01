using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Org.SbeTool.Sbe.Dll;
using Mktdata;

namespace Org.SbeTool.Sbe.Tests
{
  [TestClass]
  public class Issue992Tests
  {
    private const int Offset = 0;
    private DirectBuffer _buffer;
    private MessageHeader _messageHeader;

    [TestInitialize]
    public void SetUp()
    {
      var byteArray = new byte[4096];
      _buffer = new DirectBuffer(byteArray);

      _messageHeader = new MessageHeader();

      _messageHeader.Wrap(_buffer, 0, MessageHeader.SbeSchemaVersion);
      _messageHeader.BlockLength = MDIncrementalRefreshBook46.BlockLength;
      _messageHeader.SchemaId = MDIncrementalRefreshBook46.SchemaId;
      _messageHeader.TemplateId = MDIncrementalRefreshBook46.TemplateId;
      _messageHeader.Version = MDIncrementalRefreshBook46.SchemaVersion;
    }

    [TestMethod]
    public void CheckToStringDoesNotBreakGroupIteration()
    {
      var encoder = new MDIncrementalRefreshBook46()
        .WrapForEncodeAndApplyHeader(_buffer, Offset, _messageHeader);
      encoder.TransactTime = (ulong)new DateTime(2000, 1, 1, 0, 0, 0, DateTimeKind.Utc).Ticks;
      var noMDEntries = encoder.NoMDEntriesCount(2);
      noMDEntries.Next();
      noMDEntries.MDEntryPx.Mantissa = 12;
      noMDEntries.MDEntrySize = 3;
      noMDEntries.Next();
      noMDEntries.MDEntryPx.Mantissa = 15;
      noMDEntries.MDEntrySize = 7;
      encoder.NoOrderIDEntriesCount(0);
      encoder.CheckEncodingIsComplete();

      var expectedString = "[MDIncrementalRefreshBook46](sbeTemplateId=46|sbeSchemaId=1|sbeSchemaVersion=9|sbeBlockLength=11):TransactTime=630822816000000000|MatchEventIndicator={0}|NoMDEntries=[(MDEntryPx=(Mantissa=12|)|MDEntrySize=3|SecurityID=0|RptSeq=0|NumberOfOrders=0|MDPriceLevel=0|MDUpdateAction=New|MDEntryType=NULL_VALUE),(MDEntryPx=(Mantissa=15|)|MDEntrySize=7|SecurityID=0|RptSeq=0|NumberOfOrders=0|MDPriceLevel=0|MDUpdateAction=New|MDEntryType=NULL_VALUE)]|NoOrderIDEntries=[]";

      var decoder = new MDIncrementalRefreshBook46()
        .WrapForDecodeAndApplyHeader(_buffer, Offset, _messageHeader);
      noMDEntries = decoder.NoMDEntries;
      Assert.AreEqual(expectedString, decoder.ToString());
      int counter = 0;
      while (noMDEntries.HasNext)
      {
        Assert.AreEqual(expectedString, decoder.ToString());
        ++counter;
        noMDEntries.Next();
        Assert.AreEqual(expectedString, decoder.ToString());
      }

      Assert.AreEqual(2, counter);
      Assert.AreEqual(expectedString, decoder.ToString());
    }
  }
}
