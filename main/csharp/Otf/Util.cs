namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    /// Utility functions to help with on-the-fly decoding.
    /// </summary>
    public class Util
    {
        /// <summary>
        /// Get an integer value from a buffer at a given index.
        /// </summary>
        /// <param name="buffer"> from which to read. </param>
        /// <param name="bufferIndex"> at which he integer should be read. </param>
        /// <param name="type"> of the integer encoded in the buffer. </param>
        /// <param name="byteOrder"> of the integer in the buffer. </param>
        /// <returns> the value of the encoded integer. </returns>
        internal static int GetInt(DirectBuffer buffer, int bufferIndex, PrimitiveType type, ByteOrder byteOrder)
        {
            switch (type.Type)
            {
                case SbePrimitiveType.Int8:
                    return buffer.Int8Get(bufferIndex);

                case SbePrimitiveType.UInt8:
                    return buffer.Uint8Get(bufferIndex);

                case SbePrimitiveType.Int16:
                    return byteOrder == ByteOrder.LittleEndian ? buffer.Int16GetLittleEndian(bufferIndex) : buffer.Int16GetBigEndian(bufferIndex);

                case SbePrimitiveType.UInt16:
                    return byteOrder == ByteOrder.LittleEndian ? buffer.Uint16GetLittleEndian(bufferIndex) : buffer.Uint16GetBigEndian(bufferIndex);

                case SbePrimitiveType.Int32:
                    return byteOrder == ByteOrder.LittleEndian ? buffer.Int32GetLittleEndian(bufferIndex) : buffer.Int32GetBigEndian(bufferIndex);

                default:
                    throw new System.ArgumentException("Unsupported type: " + type);
            }
        }
    }
}
