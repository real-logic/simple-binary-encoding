using System;
using System.Runtime.InteropServices;

namespace Adaptive.SimpleBinaryEncoding
{
    public sealed unsafe class DirectBuffer2 : IDisposable
    {
        private readonly byte[] _buffer;
        private readonly byte* _pBuffer;
        private bool _disposed;
        private GCHandle _pinnedGCHandle;
        // assumes .NET runs only on little endian systems
        private const ByteOrder NativeByteOrder = ByteOrder.LittleEndian;

        public DirectBuffer2(byte[] byteArray)
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
            if (position >= _buffer.Length)
            {
                 throw new IndexOutOfRangeException(string.Format("position={0} is beyond capacity={1}", position, _buffer.Length));
            }
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte CharGetLittle(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void CharPutLittle(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public sbyte Int8GetLittle(int index)
        {
            return *(sbyte*)(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int8PutLittle(int index, sbyte value)
        {
            *(sbyte*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public short Int16GetLittle(int index)
        {
            return *(short*)(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Int16PutLittle(int index, short value)
        {
            *(short*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public int Int32GetLittle(int index, ByteOrder byteOrder)
        {
            return *(int*)(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Int32PutLittle(int index, int value, ByteOrder byteOrder)
        {
            *(int*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public long Int64GetLittle(int index, ByteOrder byteOrder)
        {
            return *(long*)(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Int64PutLittle(int index, long value, ByteOrder byteOrder)
        {
            *(long*)(_pBuffer + index) = value;
        }


        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        public byte Uint8GetLittle(int index)
        {
            return *(_pBuffer + index);
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        public void Uint8PutLittle(int index, byte value)
        {
            *(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public ushort Uint16GetLittle(int index, ByteOrder byteOrder)
        {
            var data = *(ushort*)(_pBuffer + index);
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint16PutLittle(int index, ushort value, ByteOrder byteOrder)
        {
            *(ushort*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public uint Uint32GetLittle(int index, ByteOrder byteOrder)
        {
            var data = *(uint*)(_pBuffer + index);
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint32PutLittle(int index, uint value, ByteOrder byteOrder)
        {
            *(uint*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public ulong Uint64GetLittle(int index, ByteOrder byteOrder)
        {
            var data = *(ulong*)(_pBuffer + index);
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void Uint64PutLittle(int index, ulong value, ByteOrder byteOrder)
        {
            *(ulong*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public float FloatGetLittle(int index, ByteOrder byteOrder)
        {
            var data = *(float*)(_pBuffer + index);
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void FloatPutLittle(int index, float value, ByteOrder byteOrder)
        {
            *(float*)(_pBuffer + index) = value;
        }

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        public double DoubleGetLittle(int index, ByteOrder byteOrder)
        {
            var data = *(double*)(_pBuffer + index);
            return data;
        }

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        public void DoublePutLittle(int index, double value, ByteOrder byteOrder)
        {
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

        ~DirectBuffer2()
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