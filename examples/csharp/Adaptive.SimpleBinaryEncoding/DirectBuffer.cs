using System;
using System.Net;
using System.Runtime.InteropServices;

namespace Adaptive.SimpleBinaryEncoding
{
    public unsafe class DirectBuffer : IDisposable
    {
        private bool _disposed;
        private GCHandle _pinnedGCHandle;
        private readonly byte[] _buffer;
        private readonly byte* _pBuffer;

        public DirectBuffer(byte[] byteArray)
        {
            if (byteArray == null) throw new ArgumentNullException("byteArray");

            // pin the buffer so it does not get moved around by GC, this is required since we use pointers
            _pinnedGCHandle = GCHandle.Alloc(byteArray, GCHandleType.Pinned);

            _buffer = byteArray;
            _pBuffer = (byte*) _pinnedGCHandle.AddrOfPinnedObject().ToPointer();
        }

        /// <summary>
        ///  Capacity of the underlying buffer in bytes.
        /// </summary>
        public int Capacity
        {
            get { return _buffer.Length; }
        }

        public byte* BufferPtr { get { return _pBuffer; } }

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
