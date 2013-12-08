/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Uk.Co.Real_logic.Sbe.Examples
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
            return _buffer.Uint16Get(_offset + 0, ByteOrder.LittleEndian);
        }
        set
        {
            _buffer.Uint16Put(_offset + 0, value, ByteOrder.LittleEndian);
        }
    }


    public ushort TemplateId
    {
        get
        {
            return _buffer.Uint16Get(_offset + 2, ByteOrder.LittleEndian);
        }
        set
        {
            _buffer.Uint16Put(_offset + 2, value, ByteOrder.LittleEndian);
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
