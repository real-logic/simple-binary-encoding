using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.ir;

namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    ///     Abstract <seealso cref="ITokenListener" /> that can be extended when not all callback methods are required.
    ///     <p />
    ///     By extending this class their is a possibility for the optimizer to elide unused methods otherwise requiring
    ///     polymorphic dispatch.
    /// </summary>
    public class AbstractTokenListener : ITokenListener
    {
        public virtual void OnBeginMessage(Token token)
        {
            // no op
        }

        public virtual void OnEndMessage(Token token)
        {
            // no op
        }

        public virtual void OnEncoding(Token fieldToken, DirectBuffer buffer, int bufferIndex, Token typeToken,
            int actingVersion)
        {
            // no op
        }

        public virtual void OnEnum(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int fromIndex, int toIndex, int actingVersion)
        {
            // no op
        }

        public virtual void OnBitSet(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int fromIndex, int toIndex, int actingVersion)
        {
            // no op
        }

        public virtual void OnBeginComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            // no op
        }

        public virtual void OnEndComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            // no op
        }

        public virtual void OnBeginGroup(Token token, int groupIndex, int numInGroup)
        {
            // no op
        }

        public virtual void OnEndGroup(Token token, int groupIndex, int numInGroup)
        {
            // no op
        }

        public virtual void OnVarData(Token fieldToken, DirectBuffer buffer, int bufferIndex, int length,
            Token typeToken)
        {
            // no op
        }
    }
}