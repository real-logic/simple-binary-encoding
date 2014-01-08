/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Examples.Generated
{
    public class MessageHeader
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

        public const int Size = 6;

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


    public const ushort TemplateIdNullVal = (ushort)65535;

    public const ushort TemplateIdMinVal = (ushort)0;

    public const ushort TemplateIdMaxVal = (ushort)65534;

    public ushort TemplateId
    {
        get
        {
            return _buffer.Uint16GetLittleEndian(_offset + 2);
        }
        set
        {
            _buffer.Uint16PutLittleEndian(_offset + 2, value);
        }
    }


    public const byte VersionNullVal = (byte)255;

    public const byte VersionMinVal = (byte)0;

    public const byte VersionMaxVal = (byte)254;

    public byte Version
    {
        get
        {
            return _buffer.Uint8Get(_offset + 4);
        }
        set
        {
            _buffer.Uint8Put(_offset + 4, value);
        }
    }


    public const byte ReservedNullVal = (byte)255;

    public const byte ReservedMinVal = (byte)0;

    public const byte ReservedMaxVal = (byte)254;

    public byte Reserved
    {
        get
        {
            return _buffer.Uint8Get(_offset + 5);
        }
        set
        {
            _buffer.Uint8Put(_offset + 5, value);
        }
    }

    }
}
