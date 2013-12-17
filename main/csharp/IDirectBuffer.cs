namespace Adaptive.SimpleBinaryEncoding
{
    /// <summary>
    /// Provides methods to access simple data types by index from an SBE buffer.
    /// </summary>
    public interface IDirectBuffer
    {
        /// <summary>
        /// Get the <c>byte</c> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        byte CharGet(int index);

        /// <summary>
        /// Put a <c>char</c> value into a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void CharPut(int index, byte value);

        /// <summary>
        /// Gets the <see cref="sbyte"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        sbyte Int8Get(int index);

        /// <summary>
        /// Writes a <see cref="sbyte"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void Int8Put(int index, sbyte value);

        /// <summary>
        /// Gets the <see cref="short"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        short Int16Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="short"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int16Put(int index, short value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="int"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        int Int32Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes an <see cref="int"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int32Put(int index, int value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="long"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        long Int64Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="long"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Int64Put(int index, long value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="byte"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <returns>the value at a given index.</returns>
        byte Uint8Get(int index);

        /// <summary>
        /// writes a <see cref="byte"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        void Uint8Put(int index, byte value);

        /// <summary>
        /// Gets the <see cref="ushort"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        ushort Uint16Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="ushort"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint16Put(int index, ushort value, ByteOrder byteOrder);

        /// <summary>
        ///  Gets the <see cref="uint"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        uint Uint32Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="uint"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint32Put(int index, uint value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="ulong"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        ulong Uint64Get(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="ulong"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void Uint64Put(int index, ulong value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="float"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        float FloatGet(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="float"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void FloatPut(int index, float value, ByteOrder byteOrder);

        /// <summary>
        /// Gets the <see cref="double"/> value at a given index.
        /// </summary>
        /// <param name="index"> index in bytes from which to get.</param>
        /// <param name="byteOrder">byte order of the value to be read.</param>
        /// <returns>the value at a given index.</returns>
        double DoubleGet(int index, ByteOrder byteOrder);

        /// <summary>
        /// Writes a <see cref="double"/> value to a given index.
        /// </summary>
        /// <param name="index">index in bytes for where to put.</param>
        /// <param name="value">value to be written</param>
        /// <param name="byteOrder">byte order of the value when written</param>
        void DoublePut(int index, double value, ByteOrder byteOrder);

        /// <summary>
        /// Copies a range of bytes from the underlying into a supplied byte array.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="destination">array into which the bytes will be copied.</param>
        /// <param name="offsetDestination">offset in the supplied buffer to start the copy</param>
        /// <param name="length">length of the supplied buffer to use.</param>
        /// <returns>count of bytes copied.</returns>
        int GetBytes(int index, byte[] destination, int offsetDestination, int length);

        /// <summary>
        /// Writes a byte array into the underlying buffer.
        /// </summary>
        /// <param name="index">index  in the underlying buffer to start from.</param>
        /// <param name="src">source byte array to be copied to the underlying buffer.</param>
        /// <param name="offset">offset in the supplied buffer to begin the copy.</param>
        /// <param name="length">length of the supplied buffer to copy.</param>
        /// <returns>count of bytes copied.</returns>
        int SetBytes(int index, byte[] src, int offset, int length);
    }
}