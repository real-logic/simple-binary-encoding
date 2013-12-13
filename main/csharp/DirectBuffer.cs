using System;
using System.Runtime.InteropServices;

namespace Adaptive.SimpleBinaryEncoding
{
    /// <summary>
    /// Provides access to read and write simple data types to and from a byte array in the SBE format.
    /// </summary>
    public sealed unsafe class DirectBuffer : IDisposable
    {
        private readonly byte[] _buffer;
        private readonly byte* _pBuffer;
        private bool _disposed;
        private GCHandle _pinnedGCHandle;

        /// <summary>
        /// Constructs a <see cref="DirectBuffer"/>
        /// </summary>
        /// <param name="byteArray">The byte array that will act as the backing buffer.</param>
        public DirectBuffer(byte[] byteArray)
        {
            if (byteArray == null) throw new ArgumentNullException("byteArray");

            // pin the buffer so it does not get moved around by GC, this is required since we use pointers
            _pinnedGCHandle = GCHandle.Alloc(byteArray, GCHandleType.Pinned);

            _buffer = byteArray;
            _pBuffer = (byte*) _pinnedGCHandle.AddrOfPinnedObject().ToPointer();
        }

        /// <summary>
        /// Assert that a given position is within the capacity of the buffer.
        /// </summary>
        /// <param name="position">The position (index) that access is required to.</param>
        public void CheckPosition(int position)
        {
            if (position >= _buffer.Length)
            {
                 throw new IndexOutOfRangeException(string.Format("position={0} is beyond capacity={1}", position, _buffer.Length));
            }
        }

        /// <summary>
        /// Gets the <see cref="byte"/> value at a given index.
        /// </summary>
        /// <param name="index">index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte CharGet(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="byte"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void CharPut(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="sbyte"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public sbyte Int8Get(int index)
        {
            return *(sbyte*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="sbyte"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int8Put(int index, sbyte value)
        {
            *(sbyte*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="byte"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte Uint8Get(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="byte"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint8Put(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        #region Big Endian

        /// <summary>
        /// Gets the <see cref="short"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public short Int16GetBigEndian(int index)
        {
            var data = *(short*) (_pBuffer + index);
            data = EndianessConverter.ApplyInt16(ByteOrder.BigEndian, data);
            return data;
        }

        /// <summary>
        /// Writes a <see cref="short"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int16PutBigEndian(int index, short value)
        {
           value = EndianessConverter.ApplyInt16(ByteOrder.BigEndian, value);

            *(short*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="int"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public int Int32GetBigEndian(int index)
        {
            var data = *(int*) (_pBuffer + index);
            data = EndianessConverter.ApplyInt32(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="int"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int32PutBigEndian(int index, int value)
        {
            value = EndianessConverter.ApplyInt32(ByteOrder.BigEndian, value);

            *(int*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="long"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public long Int64GetBigEndian(int index)
        {
            var data = *(long*) (_pBuffer + index);
           data = EndianessConverter.ApplyInt64(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="long"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int64PutBigEndian(int index, long value)
        {
           value = EndianessConverter.ApplyInt64(ByteOrder.BigEndian, value);

            *(long*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="ushort"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public ushort Uint16GetBigEndian(int index)
        {
            var data = *(ushort*) (_pBuffer + index);
            data = EndianessConverter.ApplyUint16(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="ushort"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint16PutBigEndian(int index, ushort value)
        {
            value = EndianessConverter.ApplyUint16(ByteOrder.BigEndian, value);

            *(ushort*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="uint"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public uint Uint32GetBigEndian(int index)
        {
            var data = *(uint*) (_pBuffer + index);
            data = EndianessConverter.ApplyUint32(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="uint"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint32PutBigEndian(int index, uint value)
        {
           value = EndianessConverter.ApplyUint32(ByteOrder.BigEndian, value);

            *(uint*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="ulong"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public ulong Uint64GetBigEndian(int index)
        {
            var data = *(ulong*) (_pBuffer + index);
            data = EndianessConverter.ApplyUint64(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="ulong"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint64PutBigEndian(int index, ulong value)
        {
           value = EndianessConverter.ApplyUint64(ByteOrder.BigEndian, value);

            *(ulong*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="float"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public float FloatGetBigEndian(int index)
        {
            var data = *(float*) (_pBuffer + index);
            data = EndianessConverter.ApplyFloat(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="float"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void FloatPutBigEndian(int index, float value)
        {
           value = EndianessConverter.ApplyFloat(ByteOrder.BigEndian, value);

            *(float*) (_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="double"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public double DoubleGetBigEndian(int index)
        {
            var data = *(double*) (_pBuffer + index);
            data = EndianessConverter.ApplyDouble(ByteOrder.BigEndian, data);

            return data;
        }

        /// <summary>
        /// Writes a <see cref="double"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void DoublePutBigEndian(int index, double value)
        {
            value = EndianessConverter.ApplyDouble(ByteOrder.BigEndian, value);

            *(double*) (_pBuffer + index) = value;
        }

        #endregion

        #region Little Endian

        /// <summary>
        /// Gets the <see cref="short"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public short Int16GetLittleEndian(int index)
        {
            return *(short*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="short"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int16PutLittleEndian(int index, short value)
        {
            *(short*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="int"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public int Int32GetLittleEndian(int index)
        {
            return *(int*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="int"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int32PutLittleEndian(int index, int value)
        {
            *(int*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="long"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public long Int64GetLittleEndian(int index)
        {
            return *(long*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="long"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int64PutLittleEndian(int index, long value)
        {
            *(long*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="ushort"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public ushort Uint16GetLittleEndian(int index)
        {
            return *(ushort*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="ushort"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint16PutLittleEndian(int index, ushort value)
        {
            *(ushort*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="uint"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public uint Uint32GetLittleEndian(int index)
        {
            return *(uint*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="uint"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint32PutLittleEndian(int index, uint value)
        {
            *(uint*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="ulong"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public ulong Uint64GetLittleEndian(int index)
        {
            return *(ulong*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="ulong"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint64PutLittleEndian(int index, ulong value)
        {
            *(ulong*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="float"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public float FloatGetLittleEndian(int index)
        {
            return *(float*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="float"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void FloatPutLittleEndian(int index, float value)
        {
            *(float*)(_pBuffer + index) = value;
        }

        /// <summary>
        /// Gets the <see cref="double"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public double DoubleGetLittleEndian(int index)
        {
            return *(double*)(_pBuffer + index);
        }

        /// <summary>
        /// Writes a <see cref="double"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void DoublePutLittleEndian(int index, double value)
        {
            *(double*)(_pBuffer + index) = value;
        }

        #endregion

        /// <summary>
        /// Copies a range of bytes from the underlying into a supplied byte array.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="destination">array into which the bytes will be copied.</param>
        /// <param name="offsetDestination">offset in the supplied buffer to start the copy</param>
        /// <param name="length">length of the supplied buffer to use.</param>
        /// <returns>count of bytes copied.</returns>
        public int GetBytes(int index, byte[] destination, int offsetDestination, int length)
        {
            int count = Math.Min(length, _buffer.Length - index);
            Buffer.BlockCopy(_buffer, index, destination, offsetDestination, count);
            return count;
        }

        /// <summary>
        /// Writes a byte array into the underlying buffer.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="src">source byte array to be copied to the underlying buffer.</param>
        /// <param name="offset">offset in the supplied buffer to begin the copy.</param>
        /// <param name="length">length of the supplied buffer to copy.</param>
        /// <returns>count of bytes copied.</returns>
        public int SetBytes(int index, byte[] src, int offset, int length)
        {
            int count = Math.Min(length, _buffer.Length - index);
            Buffer.BlockCopy(src, offset, _buffer, index, count);

            return count;
        }


        /// <summary>
        /// Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources.
        /// </summary>
        /// <filterpriority>2</filterpriority>
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        ~DirectBuffer()
        {
            Dispose(false);
        }

        private void Dispose(bool disposing)
        {
            if (_disposed)
                return;

            _pinnedGCHandle.Free();
            _disposed = true;
        }
    }
}