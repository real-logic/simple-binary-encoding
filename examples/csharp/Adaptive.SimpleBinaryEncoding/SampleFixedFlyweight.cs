namespace Adaptive.SimpleBinaryEncoding
{
    public unsafe class SampleFixedFlyweight : IFixedFlyweight
    {
        private byte* _pBuffer;
        private int _offset;
        private int _actingVersion;

        public IFixedFlyweight Reset(DirectBuffer buffer, int offset, int actingVersion)
        {
            _pBuffer = buffer.BufferPtr;
            _offset = offset;
            _actingVersion = actingVersion;
            return this;
        }

        public int Size { get { return 12; } }
    }
}