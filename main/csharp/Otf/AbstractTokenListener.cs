using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.ir;

namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    ///     Abstract <seealso cref="ITokenListener" /> that can be extended when not all callback methods are required.
    ///     <p />
    ///     By extending this class their is a possibility for the optimizer to elide unused methods otherwise requiring polymorphic dispatch.
    /// For usage see: https://github.com/real-logic/simple-binary-encoding/blob/master/examples/csharp/Otf/ExampleTokenListener.cs
    /// </summary>
    public class AbstractTokenListener : ITokenListener
    {
        /// <summary>
        /// Callback raised when the OTF decoder encounters the begining of a message
        /// </summary>
        /// <param name="token">the corresponding token</param>
        public virtual void OnBeginMessage(Token token)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters the end of a message
        /// </summary>
        /// <param name="token">the corresponding token</param>
        public virtual void OnEndMessage(Token token)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters an encoding
        /// </summary>
        public virtual void OnEncoding(Token fieldToken, DirectBuffer buffer, int bufferIndex, Token typeToken, int actingVersion)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters an enum
        /// </summary>
        public virtual void OnEnum(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int fromIndex, int toIndex, int actingVersion)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters a bit set
        /// </summary>
        public virtual void OnBitSet(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int fromIndex, int toIndex, int actingVersion)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters the beginning of a composite
        /// </summary>
        public virtual void OnBeginComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters the end of a composite
        /// </summary>
        public virtual void OnEndComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters the beginning of a group
        /// </summary>
        public virtual void OnBeginGroup(Token token, int groupIndex, int numInGroup)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters the end of a group
        /// </summary>
        public virtual void OnEndGroup(Token token, int groupIndex, int numInGroup)
        {
            // no op
        }

        /// <summary>
        /// Callback raised when the OTF decoder encounters a variable length data
        /// </summary>
        public virtual void OnVarData(Token fieldToken, DirectBuffer buffer, int bufferIndex, int length,
            Token typeToken)
        {
            // no op
        }
    }
}