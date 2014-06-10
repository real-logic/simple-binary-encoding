/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Ir.Generated
{
    public sealed partial class VarDataEncoding
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

    public const byte LengthNullValue = (byte)255;

    public const byte LengthMinValue = (byte)0;

    public const byte LengthMaxValue = (byte)254;

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


    public const byte VarDataNullValue = (byte)255;

    public const byte VarDataMinValue = (byte)0;

    public const byte VarDataMaxValue = (byte)254;
    }
}
