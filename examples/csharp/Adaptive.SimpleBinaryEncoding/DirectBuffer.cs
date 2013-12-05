using System;
using System.Runtime.InteropServices;

namespace Adaptive.SimpleBinaryEncoding
{
    public unsafe class DirectBuffer : IDisposable, IDirectBuffer
    {
        private readonly byte[] _buffer;
        private readonly byte* _pBuffer;
        private bool _disposed;
        private GCHandle _pinnedGCHandle;
        // assumes .NET runs only on little endian systems
        private const ByteOrder NativeByteOrder = ByteOrder.LittleEndian;

        public DirectBuffer(byte[] byteArray)
        {
            if (byteArray == null) throw new ArgumentNullException("byteArray");

            // pin the buffer so it does not get moved around by GC, this is required since we use pointers
            _pinnedGCHandle = GCHandle.Alloc(byteArray, GCHandleType.Pinned);

            _buffer = byteArray;
            _pBuffer = (byte*) _pinnedGCHandle.AddrOfPinnedObject().ToPointer();
        }

        /// <summary>
        /// Check that a given position is within the capacity of the buffer
        /// </summary>
        /// <param name="position">position access is required to.</param>
        public void CheckPosition(int position)
        {
            if (position > _buffer.Length)
            {
                 throw new IndexOutOfRangeException(string.Format("position={0} is beyond capacity={1}", position, _buffer.Length));
            }
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte CharGet(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void CharPut(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public sbyte Int8Get(int index)
        {
            return *(sbyte*)(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int8Put(int index, sbyte value)
        {
            *(sbyte*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public short Int16Get(int index, ByteOrder byteOrder)
        {
            var data = *(short*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyInt16(byteOrder, data);
            }
            
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Int16Put(int index, short value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyInt16(byteOrder, value);
            }

            *(short*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public int Int32Get(int index, ByteOrder byteOrder)
        {
            var data = *(int*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyInt32(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Int32Put(int index, int value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyInt32(byteOrder, value);
            }

            *(int*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public long Int64Get(int index, ByteOrder byteOrder)
        {
            var data = *(long*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyInt64(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Int64Put(int index, long value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyInt64(byteOrder, value);
            }

            *(long*)(_pBuffer + index) = value;
        }


        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte Uint8Get(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint8Put(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public ushort Uint16Get(int index, ByteOrder byteOrder)
        {
            var data = *(ushort*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyUint16(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint16Put(int index, ushort value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyUint16(byteOrder, value);
            }

            *(ushort*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public uint Uint32Get(int index, ByteOrder byteOrder)
        {
            var data = *(uint*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyUint32(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint32Put(int index, uint value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyUint32(byteOrder, value);
            }

            *(uint*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public ulong Uint64Get(int index, ByteOrder byteOrder)
        {
            var data = *(ulong*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyUint64(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint64Put(int index, ulong value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyUint64(byteOrder, value);
            }

            *(ulong*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public float FloatGet(int index, ByteOrder byteOrder)
        {
            var data = *(float*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyFloat(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void FloatPut(int index, float value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyFloat(byteOrder, value);
            }

            *(float*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public double DoubleGet(int index, ByteOrder byteOrder)
        {
            var data = *(double*)(_pBuffer + index);
            if (byteOrder != NativeByteOrder)
            {
                data = EndianessConverter.ApplyDouble(byteOrder, data);
            }

            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void DoublePut(int index, double value, ByteOrder byteOrder)
        {
            if (byteOrder != NativeByteOrder)
            {
                value = EndianessConverter.ApplyDouble(byteOrder, value);
            }

            *(double*)(_pBuffer + index) = value;
        }


        /// <summary>
        ///     Get bytes from the underlying buffer into a supplied byte array.
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
        ///     Put an array into the underlying buffer.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="src">src    to be copied to the underlying buffer.</param>
        /// <param name="offset">offset in the supplied buffer to begin the copy.</param>
        /// <param name="length">length of the supplied buffer to copy.</param>
        /// <returns>count of bytes copied.</returns>
        public int SetBytes(int index, byte[] src, int offset, int length)
        {
            int count = Math.Min(length, _buffer.Length - index);
            Buffer.BlockCopy(src, offset, _buffer, index, count);

            return count;
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        ~DirectBuffer()
        {
            Dispose(false);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (_disposed)
                return;

            _pinnedGCHandle.Free();
            _disposed = true;
        }
    }
}