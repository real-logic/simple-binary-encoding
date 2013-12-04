namespace Adaptive.SimpleBinaryEncoding
{
    /// <summary>
    ///     Interface for locating a variable length flyweight over a<see>
    ///         <cref>byte[]</cref>
    ///     </see>
    /// </summary>
    public interface IMessageFlyweight
    {
        /// <summary>
        ///     Offset in the underlying buffer at which the message starts.
        /// </summary>
        int Offset { get; }

        /// <summary>
        ///     The position for the first byte of the next block in the buffer.
        /// </summary>
        int Position { get; set; }

        /// <summary>
        ///     Reset the flyweight to a new index in a buffer to overlay a message for encoding.
        /// </summary>
        /// <param name="buffer">buffer underlying the message.</param>
        /// <param name="offset">offset at which the message body begins.</param>
        void WrapForEncode(DirectBuffer buffer, int offset);

        /// <summary>
        ///     Reset the flyweight to a new index in a buffer to overlay a message for decoding.
        /// </summary>
        /// <param name="buffer">buffer underlying the message.</param>
        /// <param name="offset">offset at which the message body begins.</param>
        /// <param name="actingBlockLength">actingBlockLength to be used when decoding the message.</param>
        /// <param name="actingVersion">actingVersion of the template to be used with decoding the message.</param>
        void WrapForDecode(DirectBuffer buffer, int offset, int actingBlockLength, int actingVersion);
    }
}