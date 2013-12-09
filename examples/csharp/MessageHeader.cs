/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class MessageHeader : IFixedFlyweight
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
