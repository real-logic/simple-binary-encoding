using System;
using System.Runtime.InteropServices;
using NUnit.Framework;

namespace Adaptive.SimpleBinaryEncoding.Tests
{
    [TestFixture]
    public unsafe class DirectBufferTests
    {
        private byte[] _buffer;
        private DirectBuffer _directBuffer;
        private byte* _pBuffer;

        [SetUp]
        public void SetUp()
        {
            _buffer = new Byte[16];
            _directBuffer = new DirectBuffer(_buffer);
            var handle = GCHandle.Alloc(_buffer, GCHandleType.Pinned);

            _pBuffer = (byte*)handle.AddrOfPinnedObject().ToPointer();
        }

        [Test]
        public void CheckPositionShouldNotThrowWhenPositionIsInRange()
        {
            _directBuffer.CheckLimit(_buffer.Length);
        }

        [Test]
        [ExpectedException(typeof(IndexOutOfRangeException))]
        public void CheckPositionShouldThrowWhenPositionIsNotInRange()
        {
            _directBuffer.CheckLimit(_buffer.Length + 1);
        }

        [Test]
        public void ConstructFromNativeBuffer()
        {
            var managedBuffer = new Byte[16];
            var handle = GCHandle.Alloc(managedBuffer, GCHandleType.Pinned);
            var unmanagedBuffer = (byte*) handle.AddrOfPinnedObject().ToPointer();

            const int value = 5;
            const int index = 0;
            
            using (var directBufferFromUnmanagedbuffer = new DirectBuffer(unmanagedBuffer, managedBuffer.Length))
            {
                directBufferFromUnmanagedbuffer.Int64PutLittleEndian(index, value);
                Assert.AreEqual(value, *(long*) (unmanagedBuffer + index));
            }
        }

        [Test]
        public void Recycle()
        {
            var directBuffer = new DirectBuffer();
            var firstBuffer = new Byte[16];
            directBuffer.Wrap(firstBuffer);

            directBuffer.Int64PutLittleEndian(0, 1);
            Assert.AreEqual(1, BitConverter.ToInt64(firstBuffer, 0));

            var secondBuffer = new byte[16];
            var secondBufferHandle = GCHandle.Alloc(secondBuffer, GCHandleType.Pinned);
            var secondUnmanagedBuffer = (byte*)secondBufferHandle.AddrOfPinnedObject().ToPointer();
            directBuffer.Wrap(secondUnmanagedBuffer, 16);
            directBuffer.Int64PutLittleEndian(0, 2);
            Assert.AreEqual(2, BitConverter.ToInt64(secondBuffer, 0));

            directBuffer.Dispose();
        }

        [Test]
        public void Reallocate()
        {
            const int initialBufferSize = 8;
            var initialBuffer = new Byte[initialBufferSize];
            
            const int biggerBufferSize = 100;
            var biggerBuffer = new byte[biggerBufferSize];
            
            var reallocableBuffer = new DirectBuffer(initialBuffer, 
                (existingSize, requestedSize) =>
                {
                    Assert.AreEqual(initialBufferSize, existingSize);
                    Assert.AreEqual(16, requestedSize);
                    return biggerBuffer;
                });
            
            reallocableBuffer.CheckLimit(8);
            reallocableBuffer.Int64PutLittleEndian(0, 1);
            Assert.AreEqual(initialBufferSize, reallocableBuffer.Capacity);

            reallocableBuffer.CheckLimit(16);
            reallocableBuffer.Int64PutLittleEndian(8, 2);
            Assert.AreEqual(biggerBufferSize, reallocableBuffer.Capacity);

            Assert.AreEqual(1, BitConverter.ToInt64(biggerBuffer, 0));
            Assert.AreEqual(2, BitConverter.ToInt64(biggerBuffer, 8));
        }

        [Test]
        public void ReallocateFailure()
        {
            const int initialBufferSize = 8;
            var initialBuffer = new Byte[initialBufferSize];
            var reallocableBuffer = new DirectBuffer(initialBuffer, (existingSize, requestedSize) =>
            {
                Assert.AreEqual(initialBufferSize, existingSize);
                Assert.AreEqual(16, requestedSize);
                return null;
            });

            reallocableBuffer.CheckLimit(8);
            reallocableBuffer.Int64PutLittleEndian(0, 1);
            Assert.AreEqual(initialBufferSize, reallocableBuffer.Capacity);
            Assert.Throws<IndexOutOfRangeException>(() => reallocableBuffer.CheckLimit(16));
        }

        #region Byte

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        public void ShouldPutByte(byte value, int index)
        {
            _directBuffer.CharPut(index, value);

            Assert.AreEqual(value, _buffer[index]);
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        public void ShouldGetByte(byte value, int index)
        {
            _buffer[index] = value;

            var result = _directBuffer.CharGet(index);

            Assert.AreEqual(value, result);
        }

        #endregion

        #region Int8

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt8(sbyte value, int index)
        {
            _directBuffer.Int8Put(index, value);

            Assert.AreEqual(value, *(sbyte*) (_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt8(sbyte value, int index)
        {
            _buffer[index] = *(byte*) &value;

            var result = _directBuffer.Int8Get(index);

            Assert.AreEqual(value, result);
        }

        #endregion

        #region Int16

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt16LittleEndian(short value, int index)
        {
            _directBuffer.Int16PutLittleEndian(index, value);

            Assert.AreEqual(value, *(short*) (_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt16BigEndian(short value, int index)
        {
            _directBuffer.Int16PutBigEndian(index, value);

            var expected = EndianessConverter.ApplyInt16(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, *(short*) (_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt16LittleEndian(short value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 2);

            var result = _directBuffer.Int16GetLittleEndian(index);

            Assert.AreEqual(value, result);
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt16BigEndian(short value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 2);

            var result = _directBuffer.Int16GetBigEndian(index);

            var expected = EndianessConverter.ApplyInt16(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, result);
        }

        #endregion

        #region Int32

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt32LittleEndian(int value, int index)
        {
            _directBuffer.Int32PutLittleEndian(index, value);

            Assert.AreEqual(value, *(int*)(_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt32BigEndian(int value, int index)
        {
            _directBuffer.Int32PutBigEndian(index, value);

            var expected = EndianessConverter.ApplyInt32(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, *(int*)(_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt32LittleEndian(int value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 4);

            var result = _directBuffer.Int32GetLittleEndian(index);

            Assert.AreEqual(value, result);
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt32BigEndian(int value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 4);

            var result = _directBuffer.Int32GetBigEndian(index);

            var expected = EndianessConverter.ApplyInt32(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, result);
        }

        #endregion

        #region Int64

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt64LittleEndian(int value, int index)
        {
            _directBuffer.Int64PutLittleEndian(index, value);

            Assert.AreEqual(value, *(long*)(_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldPutInt64BigEndian(long value, int index)
        {
            _directBuffer.Int64PutBigEndian(index, value);

            var expected = EndianessConverter.ApplyInt64(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, *(long*)(_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt64LittleEndian(long value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 8);

            var result = _directBuffer.Int64GetLittleEndian(index);

            Assert.AreEqual(value, result);
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        [TestCase(-5, 8)]
        public void ShouldGetInt64BigEndian(long value, int index)
        {
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 8);

            var result = _directBuffer.Int64GetBigEndian(index);
            var expected = EndianessConverter.ApplyInt64(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, result);
        }

        #endregion

        #region UInt8

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        public void ShouldPutUInt8(byte value, int index)
        {
            _directBuffer.Uint8Put(index, value);

            Assert.AreEqual(value, *(_pBuffer + index));
        }

        [TestCase(5, 0)]
        [TestCase(5, 8)]
        public void ShouldGetUInt8(byte value, int index)
        {
            _buffer[index] = *&value;

            var result = _directBuffer.Uint8Get(index);

            Assert.AreEqual(value, result);
        }

        #endregion

        #region UInt16

        [Test]
        public void ShouldPutUInt16LittleEndian()
        {
            const ushort value = 5;
            const int index = 0;
            _directBuffer.Uint16PutLittleEndian(index, value);

            Assert.AreEqual(value, *(ushort*)(_pBuffer + index));
        }

        [Test]
        public void ShouldPutUInt16BigEndian()
        {
            const ushort value = 5;
            const int index = 0;
            _directBuffer.Uint16PutBigEndian(index, value);

            var expected = EndianessConverter.ApplyUint16(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, *(ushort*)(_pBuffer + index));
        }

        [Test]
        public void ShouldGetUInt16LittleEndian()
        {
            const ushort value = 5;
            const int index = 0;
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 2);

            var result = _directBuffer.Uint16GetLittleEndian(index);

            Assert.AreEqual(value, result);
        }

       [Test]
        public void ShouldGetUInt16BigEndian()
        {
            const ushort value = 5;
            const int index = 0;
            var bytes = BitConverter.GetBytes(value);
            Array.Copy(bytes, 0, _buffer, index, 2);

            var result = _directBuffer.Uint16GetBigEndian(index);

            var expected = EndianessConverter.ApplyUint16(ByteOrder.BigEndian, value);
            Assert.AreEqual(expected, result);
        }

        #endregion

        #region UInt32

       [Test]
       public void ShouldPutUInt32LittleEndian()
       {
           const uint value = 5;
           const int index = 0;
           _directBuffer.Uint32PutLittleEndian(index, value);

           Assert.AreEqual(value, *(uint*)(_pBuffer + index));
       }

       [Test]
       public void ShouldPutUInt32BigEndian()
       {
           const uint value = 5;
           const int index = 0;
           _directBuffer.Uint32PutBigEndian(index, value);

           var expected = EndianessConverter.ApplyUint32(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, *(uint*)(_pBuffer + index));
       }

       [Test]
       public void ShouldGetUInt32LittleEndian()
       {
           const uint value = 5;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 4);

           var result = _directBuffer.Uint32GetLittleEndian(index);

           Assert.AreEqual(value, result);
       }

       [Test]
       public void ShouldGetUInt32BigEndian()
       {
           const uint value = 5;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 4);

           var result = _directBuffer.Uint32GetBigEndian(index);

           var expected = EndianessConverter.ApplyUint32(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, result);
       }

       #endregion

        #region UInt64

       [Test]
       public void ShouldPutUInt64LittleEndian()
       {
           const ulong value = ulong.MaxValue - 1;
           const int index = 0;
           _directBuffer.Uint64PutLittleEndian(index, value);

           Assert.AreEqual(value, *(ulong*)(_pBuffer + index));
       }

       [Test]
       public void ShouldPutUInt64BigEndian()
       {
           const ulong value = ulong.MaxValue - 1;
           const int index = 0;
           _directBuffer.Uint64PutBigEndian(index, value);

           var expected = EndianessConverter.ApplyUint64(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, *(ulong*)(_pBuffer + index));
       }

       [Test]
       public void ShouldGetUInt64LittleEndian()
       {
           const ulong value = ulong.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 8);

           var result = _directBuffer.Uint64GetLittleEndian(index);

           Assert.AreEqual(value, result);
       }

       [Test]
       public void ShouldGetUInt64BigEndian()
       {
           const ulong value = ulong.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 8);

           var result = _directBuffer.Uint64GetBigEndian(index);

           var expected = EndianessConverter.ApplyUint64(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, result);
       }

       #endregion

        #region Float

       [Test]
       public void ShouldPutFloatLittleEndian()
       {
           const float value = float.MaxValue - 1;
           const int index = 0;
           _directBuffer.FloatPutLittleEndian(index, value);

           Assert.AreEqual(value, *(float*)(_pBuffer + index));
       }

       [Test]
       public void ShouldPutFloatBigEndian()
       {
           const float value = float.MaxValue - 1;
           const int index = 0;
           _directBuffer.FloatPutBigEndian(index, value);

           var expected = EndianessConverter.ApplyFloat(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, *(float*)(_pBuffer + index));
       }

       [Test]
       public void ShouldGetFloatLittleEndian()
       {
           const float value = float.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 4);

           var result = _directBuffer.FloatGetLittleEndian(index);

           Assert.AreEqual(value, result);
       }

       [Test]
       public void ShouldGetFloatBigEndian()
       {
           const float value = float.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 4);

           var result = _directBuffer.FloatGetBigEndian(index);

           var expected = EndianessConverter.ApplyFloat(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, result);
       }

       #endregion

        #region Double

       [Test]
       public void ShouldPutDoubleLittleEndian()
       {
           const double value = double.MaxValue - 1;
           const int index = 0;
           _directBuffer.DoublePutLittleEndian(index, value);

           Assert.AreEqual(value, *(double*)(_pBuffer + index));
       }

       [Test]
       public void ShouldPutDoubleBigEndian()
       {
           const double value = double.MaxValue - 1;
           const int index = 0;
           _directBuffer.DoublePutBigEndian(index, value);

           var expected = EndianessConverter.ApplyDouble(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, *(double*)(_pBuffer + index));
       }

       [Test]
       public void ShouldGetDoubleLittleEndian()
       {
           const double value = double.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 8);

           var result = _directBuffer.DoubleGetLittleEndian(index);

           Assert.AreEqual(value, result);
       }

       [Test]
       public void ShouldGetDoubleBigEndian()
       {
           const double value = double.MaxValue - 1;
           const int index = 0;
           var bytes = BitConverter.GetBytes(value);
           Array.Copy(bytes, 0, _buffer, index, 8);

           var result = _directBuffer.DoubleGetBigEndian(index);

           var expected = EndianessConverter.ApplyDouble(ByteOrder.BigEndian, value);
           Assert.AreEqual(expected, result);
       }

       #endregion
    }
}