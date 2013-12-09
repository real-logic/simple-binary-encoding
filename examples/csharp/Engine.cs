/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Baseline
{
    public class Engine : IFixedFlyweight
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

    public const ushort CapacityNullVal = (ushort)65535;

    public const ushort CapacityMinVal = (ushort)0;

    public const ushort CapacityMaxVal = (ushort)65534;

    public ushort Capacity
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


    public const byte NumCylindersNullVal = (byte)255;

    public const byte NumCylindersMinVal = (byte)0;

    public const byte NumCylindersMaxVal = (byte)254;

    public byte NumCylinders
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


    public const ushort MaxRpmNullVal = (ushort)65535;

    public const ushort MaxRpmMinVal = (ushort)0;

    public const ushort MaxRpmMaxVal = (ushort)65534;

    public ushort MaxRpm { get { return (ushort)9000; } }

    public const byte ManufacturerCodeNullVal = (byte)0;

    public const byte ManufacturerCodeMinVal = (byte)32;

    public const byte ManufacturerCodeMaxVal = (byte)126;

    public const int ManufacturerCodeLength  = 3;

    public byte GetManufacturerCode(int index)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        return _buffer.CharGet(_offset + 3 + (index * 1));
    }

    public void SetManufacturerCode(int index, byte value)
    {
        if (index < 0 || index >= 3)
        {
            throw new IndexOutOfRangeException("index out of range: index=" + index);
        }

        _buffer.CharPut(_offset + 3 + (index * 1), value);
    }

    public const string ManufacturerCodeCharacterEncoding = "UTF-8";

    public int GetManufacturerCode(byte[] dst, int dstOffset)
    {
        const int length = 3;
        if (dstOffset < 0 || dstOffset > (dst.Length - length))
        {
            throw new IndexOutOfRangeException("dstOffset out of range for copy: offset=" + dstOffset);
        }

        _buffer.GetBytes(_offset + 3, dst, dstOffset, length);
        return length;
    }

    public void SetManufacturerCode(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 3, src, srcOffset, length);
    }

    public const byte FuelNullVal = (byte)0;

    public const byte FuelMinVal = (byte)32;

    public const byte FuelMaxVal = (byte)126;

    private static readonly byte[] _fuelValue = {80, 101, 116, 114, 111, 108};

    public const int FuelLength = 6;
    public byte Fuel(int index)
    {
        return _fuelValue[index];
    }

    public int GetFuel(byte[] dst, int offset, int length)
    {
        int bytesCopied = Math.Min(length, 6);
        Array.Copy(_fuelValue, 0, dst, offset, bytesCopied);
        return bytesCopied;
    }
    }
}
