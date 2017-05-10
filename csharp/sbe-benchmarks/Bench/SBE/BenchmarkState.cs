using Org.SbeTool.Sbe.Dll;
using Uk.Co.Real_logic.Sbe.Benchmarks.Fix;

namespace Org.SbeTool.Sbe.Benchmarks
{
    public class BenchmarkState
    {
        private readonly byte[] _eBuffer = new byte[1024];
        private readonly byte[] _dBuffer = new byte[1024];
        private readonly DirectBuffer _encodeBuffer;
        private readonly DirectBuffer _decodeBuffer;
        private readonly MarketDataIncrementalRefreshTrades _marketData = new MarketDataIncrementalRefreshTrades();
        private readonly MessageHeader _messageHeader = new MessageHeader();

        public int BufferIndex
        {
            get { return 0; }
        }

        public BenchmarkState()
        {
            _encodeBuffer = new DirectBuffer(_eBuffer);
            _decodeBuffer = new DirectBuffer(_dBuffer);
            MarketDataBenchmark.Encode(_messageHeader, _marketData, _decodeBuffer, BufferIndex);
        }

        public DirectBuffer EncodeBuffer
        {
            get { return _encodeBuffer; }
        }

        public DirectBuffer DecodeBuffer
        {
            get { return _decodeBuffer; }
        }

        public MarketDataIncrementalRefreshTrades MarketData
        {
            get { return _marketData; }
        }

        public MessageHeader MessageHeader
        {
            get { return _messageHeader; }
        }
    }
}