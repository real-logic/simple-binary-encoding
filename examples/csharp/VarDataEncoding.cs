/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class VarDataEncoding
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

    public const byte LengthNullVal = (byte)255;

    public const byte LengthMinVal = (byte)0;

    public const byte LengthMaxVal = (byte)254;

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


    public const byte VarDataNullVal = (byte)255;

    public const byte VarDataMinVal = (byte)0;

    public const byte VarDataMaxVal = (byte)254;
    }
}
