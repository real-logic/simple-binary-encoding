using System;
using System.Net;

namespace Adaptive.SimpleBinaryEncoding
{
    public static class EndianessConverter
    {
        // TODO: we could assume the system is always little endian and have two methods for each (one no-op and another one which reverse, so we do not have branching)

        private static readonly ByteOrder NativeByteOrder = BitConverter.IsLittleEndian
            ? ByteOrder.LittleEndian
            : ByteOrder.BigEndian;

        public static short ApplyInt16(ByteOrder byteOrder, short value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return (short)((value & 0xFFU) << 8 | (value & 0xFF00U) >> 8);
        }

        public static ushort ApplyUint16(ByteOrder byteOrder, ushort value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return (ushort)((value & 0xFFU) << 8 | (value & 0xFF00U) >> 8);
        }

        public static int ApplyInt32(ByteOrder byteOrder, int value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return (int)((value & 0x000000FFU) << 24 | (value & 0x0000FF00U) << 8 |
                   (value & 0x00FF0000U) >> 8 | (value & 0xFF000000U) >> 24);
        }

        public static uint ApplyUint32(ByteOrder byteOrder, uint value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return (value & 0x000000FFU) << 24 | (value & 0x0000FF00U) << 8 |
                   (value & 0x00FF0000U) >> 8 | (value & 0xFF000000U) >> 24;
        }

        public static ulong ApplyUint64(ByteOrder byteOrder, ulong value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return (value & 0x00000000000000FFUL) << 56 | (value & 0x000000000000FF00UL) << 40 |
                    (value & 0x0000000000FF0000UL) << 24 | (value & 0x00000000FF000000UL) << 8 |
                    (value & 0x000000FF00000000UL) >> 8 | (value & 0x0000FF0000000000UL) >> 24 |
                    (value & 0x00FF000000000000UL) >> 40 | (value & 0xFF00000000000000UL) >> 56;
        }

        public static long ApplyInt64(ByteOrder byteOrder, long value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return IPAddress.HostToNetworkOrder(value);
        }

        public static double ApplyDouble(ByteOrder byteOrder, double value)
        {
            if (byteOrder == NativeByteOrder) return value;

            return BitConverter.Int64BitsToDouble(IPAddress.HostToNetworkOrder(BitConverter.DoubleToInt64Bits(value)));
        }

        public unsafe static float ApplyFloat(ByteOrder byteOrder, float value)
        {
            if (byteOrder == NativeByteOrder) return value;

            int valueInt = *(int*) &value;
            int applied = ApplyInt32(byteOrder, valueInt);

            return *(float*) &applied;
        }
    }
}