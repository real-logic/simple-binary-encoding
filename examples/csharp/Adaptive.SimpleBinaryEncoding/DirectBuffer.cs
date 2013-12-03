using System;
using System.Runtime.InteropServices;

namespace Adaptive.SimpleBinaryEncoding
{
    public unsafe class DirectBuffer : IDisposable
    {
        private readonly byte[] _buffer;
        private readonly byte* _pBuffer;
        private bool _disposed;
        private GCHandle _pinnedGCHandle;

        public DirectBuffer(byte[] byteArray)
        {
            if (byteArray == null) throw new ArgumentNullException("byteArray");

            // pin the buffer so it does not get moved around by GC, this is required since we use pointers
            _pinnedGCHandle = GCHandle.Alloc(byteArray, GCHandleType.Pinned);

            _buffer = byteArray;
            _pBuffer = (byte*) _pinnedGCHandle.AddrOfPinnedObject().ToPointer();
        }

        /// <summary>
        ///     Capacity of the underlying buffer in bytes.
        /// </summary>
        public int Capacity
        {
            get { return _buffer.Length; }
        }

        public byte* BufferPtr
        {
            get { return _pBuffer; }
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
    }
}