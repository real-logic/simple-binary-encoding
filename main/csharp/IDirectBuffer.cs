namespace Adaptive.SimpleBinaryEncoding
{
    public interface IDirectBuffer
    {
        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        byte CharGet(int index);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void CharPut(int index, byte value);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        sbyte Int8Get(int index);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void Int8Put(int index, sbyte value);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        short Int16Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int16Put(int index, short value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        int Int32Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int32Put(int index, int value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        long Int64Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int64Put(int index, long value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        byte Uint8Get(int index);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void Uint8Put(int index, byte value);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        ushort Uint16Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint16Put(int index, ushort value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        uint Uint32Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint32Put(int index, uint value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        ulong Uint64Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint64Put(int index, ulong value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        float FloatGet(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void FloatPut(int index, float value, ByteOrder byteOrder);

        /// <summary>
        ///  Get the value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        double DoubleGet(int index, ByteOrder byteOrder);

        /// <summary>
        /// Put a value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void DoublePut(int index, double value, ByteOrder byteOrder);

        /// <summary>
        ///     Get bytes from the underlying buffer into a supplied byte array.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="destination">array into which the bytes will be copied.</param>
        /// <param name="offsetDestination">offset in the supplied buffer to start the copy</param>
        /// <param name="length">length of the supplied buffer to use.</param>
        /// <returns>count of bytes copied.</returns>
        int GetBytes(int index, byte[] destination, int offsetDestination, int length);

        /// <summary>
        ///     Put an array into the underlying buffer.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="src">src    to be copied to the underlying buffer.</param>
        /// <param name="offset">offset in the supplied buffer to begin the copy.</param>
        /// <param name="length">length of the supplied buffer to copy.</param>
        /// <returns>count of bytes copied.</returns>
        int SetBytes(int index, byte[] src, int offset, int length);
    }
}