/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class VarDataEncoding : IFixedFlyweight
    {
        private DirectBuffer _buffer;
        private int _offset;
        private int _actingVersion;

        public void Wrap(DirectBuffer buffer, int offset, int actingVersion)
        {
            _offset = offset;
            _actingVersion = actingVersion;
            _buffer = buffer;
        }

        public const int Size = -1;

    public byte Length
    {
        get
        {
            return _buffer.Uint8Get(_offset + 0);
        }
        set
        {
            _buffer.Uint8Put(_offset + 0, value);
        }
    }

    }
}
