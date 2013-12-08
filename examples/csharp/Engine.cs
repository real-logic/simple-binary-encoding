/* Generated SBE (Simple Binary Encoding) message codec */

using System;
using Adaptive.SimpleBinaryEncoding;

namespace Uk.Co.Real_logic.Sbe.Examples
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

    public ushort Capacity
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


    public ushort MaxRpm { get { return (ushort)9000; } }

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

    public Engine SetManufacturerCode(byte[] src, int srcOffset)
    {
        const int length = 3;
        if (srcOffset < 0 || srcOffset > (src.Length - length))
        {
            throw new IndexOutOfRangeException("srcOffset out of range for copy: offset=" + srcOffset);
        }

        _buffer.SetBytes(_offset + 3, src, srcOffset, length);
        return this;
    }

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
