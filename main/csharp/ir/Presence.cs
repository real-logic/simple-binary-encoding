namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Indicates the presence status of a primitive encoded field in a message.
    /// </summary>
    public enum Presence
    {
        /// <summary>
        /// The field presence is required. 
        /// </summary>
        Required,

        /// <summary>
        /// The field presence is optional. 
        /// </summary>
        Optional,

        /// <summary>
        /// The field presence is a constant. 
        /// </summary>
        Constant
    }
}