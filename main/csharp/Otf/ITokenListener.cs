using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.ir;

namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    ///     Callback interface to be implemented by code wanting to decode messages on-the-fly.
    ///     <p />
    ///     If all methods are not required then consider extending <seealso cref="AbstractTokenListener" /> for potential
    ///     performance benefits and simpler code.
    /// </summary>
    public interface ITokenListener
    {
        /// <summary>
        ///     Called on beginning the decoding of a message.
        /// </summary>
        /// <param name="token"> representing the IR for message including meta data. </param>
        void OnBeginMessage(Token token);

        /// <summary>
        ///     Called on end of decoding of a message.
        /// </summary>
        /// <param name="token"> representing the IR for message including meta data. </param>
        void OnEndMessage(Token token);

        /// <summary>
        ///     Primitive encoded type encountered. This can be a root block field or field within a composite or group.
        ///     <p />
        ///     Within a composite the typeToken and fieldToken are the same.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the field of the message root or group. </param>
        /// <param name="buffer"> containing the encoded message. </param>
        /// <param name="bufferIndex"> at which the encoded field begins. </param>
        /// <param name="typeToken"> of the encoded primitive value. </param>
        /// <param name="actingVersion"> of the encoded message for determining validity of extension fields. </param>
        void OnEncoding(Token fieldToken, DirectBuffer buffer, int bufferIndex, Token typeToken, int actingVersion);

        /// <summary>
        ///     Enum encoded type encountered.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the field of the message root or group. </param>
        /// <param name="buffer"> containing the encoded message. </param>
        /// <param name="bufferIndex"> at which the encoded field begins. </param>
        /// <param name="tokens"> describing the message. </param>
        /// <param name="fromIndex"> at which the enum metadata begins. </param>
        /// <param name="toIndex"> at which the enum metadata ends. </param>
        /// <param name="actingVersion"> of the encoded message for determining validity of extension fields. </param>
        void OnEnum(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens, int fromIndex,
            int toIndex, int actingVersion);

        /// <summary>
        ///     BitSet encoded type encountered.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the field of the message root or group. </param>
        /// <param name="buffer"> containing the encoded message. </param>
        /// <param name="bufferIndex"> at which the encoded field begins. </param>
        /// <param name="tokens"> describing the message. </param>
        /// <param name="fromIndex"> at which the bit set metadata begins. </param>
        /// <param name="toIndex"> at which the bit set metadata ends. </param>
        /// <param name="actingVersion"> of the encoded message for determining validity of extension fields. </param>
        void OnBitSet(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens, int fromIndex,
            int toIndex, int actingVersion);

        /// <summary>
        ///     Beginning of Composite encoded type encountered.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the field of the message root or group. </param>
        /// <param name="tokens"> describing the message. </param>
        /// <param name="fromIndex"> at which the composite metadata begins. </param>
        /// <param name="toIndex"> at which the composite metadata ends. </param>
        void OnBeginComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex);

        /// <summary>
        ///     End of Composite encoded type encountered.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the field of the message root or group. </param>
        /// <param name="tokens"> describing the message. </param>
        /// <param name="fromIndex"> at which the composite metadata begins. </param>
        /// <param name="toIndex"> at which the composite metadata ends. </param>
        void OnEndComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex);

        /// <summary>
        ///     Beginning of group encoded type encountered.
        /// </summary>
        /// <param name="token"> describing the group. </param>
        /// <param name="groupIndex"> index for the repeat count of the group. </param>
        /// <param name="numInGroup"> number of times the group will be repeated. </param>
        void OnBeginGroup(Token token, int groupIndex, int numInGroup);

        /// <summary>
        ///     End of group encoded type encountered.
        /// </summary>
        /// <param name="token"> describing the group. </param>
        /// <param name="groupIndex"> index for the repeat count of the group. </param>
        /// <param name="numInGroup"> number of times the group will be repeated. </param>
        void OnEndGroup(Token token, int groupIndex, int numInGroup);

        /// <summary>
        ///     Var data field encountered.
        /// </summary>
        /// <param name="fieldToken"> in the IR representing the var data field. </param>
        /// <param name="buffer"> containing the encoded message. </param>
        /// <param name="bufferIndex"> at which the variable data begins. </param>
        /// <param name="length"> of the variable data in bytes. </param>
        /// <param name="typeToken">
        ///     of the variable data. Specifically needed to determine character encoding of the variable
        ///     data.
        /// </param>
        void OnVarData(Token fieldToken, DirectBuffer buffer, int bufferIndex, int length, Token typeToken);
    }
}