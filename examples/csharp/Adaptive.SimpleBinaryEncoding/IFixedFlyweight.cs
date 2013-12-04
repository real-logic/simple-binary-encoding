namespace Adaptive.SimpleBinaryEncoding
{
    /// <summary>
    ///     Interface for locating a fixed length flyweight over a <see>
    ///         <cref>byte[]</cref>
    ///     </see>
    /// </summary>
    public interface IFixedFlyweight
    {
        /// <summary>
        ///     Reset this flyweight to window over the buffer from a given offset.
        /// </summary>
        /// <param name="buffer">buffer from which to read and write.</param>
        /// <param name="offset">offset at which the flyweight starts.</param>
        /// <param name="actingVersion">actingVersion of the containing template being decoded</param>
        void Wrap(DirectBuffer buffer, int offset, int actingVersion);
    }
}