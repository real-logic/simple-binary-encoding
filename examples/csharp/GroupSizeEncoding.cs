/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class GroupSizeEncoding
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

        public const int Size = 3;

    public const ushort BlockLengthNullVal = (ushort)65535;

    public const ushort BlockLengthMinVal = (ushort)0;

    public const ushort BlockLengthMaxVal = (ushort)65534;

    public ushort BlockLength
    {
        get
        {
            return _buffer.Uint16GetLittleEndian(_offset + 0);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 0, value);
        }
    }


    public const byte NumInGroupNullVal = (byte)255;

    public const byte NumInGroupMinVal = (byte)0;

    public const byte NumInGroupMaxVal = (byte)254;

    public byte NumInGroup
    {
        get
        {
            return _buffer.Uint8Get(_offset + 2);
        }
        set
        {
            _buffer.Uint8Put(_offset + 2, value);
        }
    }

    }
}
