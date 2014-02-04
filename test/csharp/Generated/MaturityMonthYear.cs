/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.Tests.Generated
{
    public class MaturityMonthYear
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

        public const int Size = 5;

    public const ushort YearNullValue = (ushort)65535;

    public const ushort YearMinValue = (ushort)0;

    public const ushort YearMaxValue = (ushort)65534;

    public ushort Year
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


    public const byte MonthNullValue = (byte)255;

    public const byte MonthMinValue = (byte)0;

    public const byte MonthMaxValue = (byte)254;

    public byte Month
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


    public const byte DayNullValue = (byte)255;

    public const byte DayMinValue = (byte)0;

    public const byte DayMaxValue = (byte)254;

    public byte Day
    {
        get
        {
            return _buffer.Uint8Get(_offset + 3);
        }
        set
        {
            _buffer.Uint8Put(_offset + 3, value);
        }
    }


    public const byte WeekNullValue = (byte)255;

    public const byte WeekMinValue = (byte)0;

    public const byte WeekMaxValue = (byte)254;

    public byte Week
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

    }
}
