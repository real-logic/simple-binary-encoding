using System;
using System.Linq;
using NUnit.Framework;

namespace Adaptive.SimpleBinaryEncoding.Tests
{
    [TestFixture]
    public class EndianessConverterTests
    {
        [Test]
        public void ApplyShortWithLittleEndianShouldNoOp()
        {
            const short input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyShortWithBigEndianShouldReverseBytes()
        {
            const short input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            short expected = BitConverter.ToInt16(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyUShortWithLittleEndianShouldNoOp()
        {
            const ushort input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyUShortWithBigEndianShouldReverseBytes()
        {
            const ushort input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            ushort expected = BitConverter.ToUInt16(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyIntWithLittleEndianShouldNoOp()
        {
            const int input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyIntWithBigEndianShouldReverseBytes()
        {
            const int input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            int expected = BitConverter.ToInt32(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyUIntWithLittleEndianShouldNoOp()
        {
            const uint input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyUIntWithBigEndianShouldReverseBytes()
        {
            const uint input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            uint expected = BitConverter.ToUInt32(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyULongWithLittleEndianShouldNoOp()
        {
            const ulong input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyULongWithBigEndianShouldReverseBytes()
        {
            const ulong input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);
            
            ulong expected = BitConverter.ToUInt64(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyLongWithLittleEndianShouldNoOp()
        {
            const long input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyLongWithBigEndianShouldReverseBytes()
        {
            const long input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            long expected = BitConverter.ToInt64(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyDoubleWithLittleEndianShouldNoOp()
        {
            const double input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyDoubleWithBigEndianShouldReverseBytes()
        {
            const double input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            double expected = BitConverter.ToDouble(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }

        [Test]
        public void ApplyFloatWithLittleEndianShouldNoOp()
        {
            const float input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.LittleEndian);

            Assert.AreEqual(input, result);
        }

        [Test]
        public void ApplyFloatWithBigEndianShouldReverseBytes()
        {
            const float input = 12;

            var result = EndianessConverter.Apply(input, ByteOrder.BigEndian);

            float expected = BitConverter.ToSingle(BitConverter.GetBytes(input).Reverse().ToArray(), 0);
            Assert.AreEqual(expected, result);
        }
    }
}
