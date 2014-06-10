/* Generated SBE (Simple Binary Encoding) message codec */

#pragma warning disable 1591 // disable warning on missing comments
using System;
using Adaptive.SimpleBinaryEncoding;

namespace Adaptive.SimpleBinaryEncoding.PerfTests.Bench.SBE.FIX
{
    public sealed partial class IntQty32
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

        public const int Size = 4;

    public const int MantissaNullValue = -2147483648;

    public const int MantissaMinValue = -2147483647;

    public const int MantissaMaxValue = 2147483647;

    public int Mantissa
    {
        get
        {
            return _buffer.Int32GetLittleEndian(_offset + 0);
        }
        set
        {
            _buffer.Int32PutLittleEndian(_offset + 0, value);
        }
    }


    public const sbyte ExponentNullValue = (sbyte)-128;

    public const sbyte ExponentMinValue = (sbyte)-127;

    public const sbyte ExponentMaxValue = (sbyte)127;

    public sbyte Exponent { get { return (sbyte)0; } }
    }
}
