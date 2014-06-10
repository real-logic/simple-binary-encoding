using System;
using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.ir;

namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    ///     On-the-fly decoder that dynamically decodes messages based on the IR for a schema.
    ///     <p />
    ///     The contents of the messages are structurally decomposed and passed to a <seealso cref="ITokenListener" /> for
    ///     decoding the primitive values.
    ///     <p />
    ///     The design keeps all state on the stack to maximise performance and avoid object allocation. The message decoder
    ///     can be used reused by
    ///     repeatably calling
    ///     <seealso cref="Decode" />
    ///     and is thread safe to be used across multiple threads.
    /// </summary>
    public class OtfMessageDecoder
    {
        private const int GroupDimTypeTokens = 5;
        private const int VarDataTokens = 5;

        /// <summary>
        ///     Decode a message from the provided buffer based on the message schema described with IR
        ///     <seealso cref="Token" />s.
        /// </summary>
        /// <param name="buffer">        containing the encoded message. </param>
        /// <param name="bufferIndex">   at which the message encoding starts in the buffer. </param>
        /// <param name="actingVersion"> of the encoded message for dealing with extension fields. </param>
        /// <param name="blockLength">   of the root message fields. </param>
        /// <param name="msgTokens">     in IR format describing the message structure. </param>
        /// <param name="listener">      to callback for decoding the primitive values as discovered in the structure. </param>
        /// <returns> the index in the underlying buffer after decoding. </returns>
        public static int Decode(DirectBuffer buffer, int bufferIndex, int actingVersion, int blockLength,
            IList<Token> msgTokens, ITokenListener listener)
        {
            int groupsBeginIndex = FindNextOrLimit(msgTokens, 1, msgTokens.Count, Signal.BeginGroup);
            int varDataSearchStart = groupsBeginIndex != msgTokens.Count ? groupsBeginIndex : 1;
            int varDataBeginIndex = FindNextOrLimit(msgTokens, varDataSearchStart, msgTokens.Count, Signal.BeginVarData);

            listener.OnBeginMessage(msgTokens[0]);

            DecodeFields(buffer, bufferIndex, actingVersion, msgTokens, 0, groupsBeginIndex, listener);
            bufferIndex += blockLength;

            bufferIndex = DecodeGroups(buffer, bufferIndex, actingVersion, msgTokens, groupsBeginIndex,
                varDataBeginIndex, listener);

            bufferIndex = DecodeVarData(buffer, bufferIndex, msgTokens, varDataBeginIndex, msgTokens.Count, listener);

            listener.OnEndMessage(msgTokens[msgTokens.Count - 1]);

            return bufferIndex;
        }

        private static void DecodeFields(DirectBuffer buffer, int bufferIndex, int actingVersion, IList<Token> tokens,
            int fromIndex, int toIndex, ITokenListener listener)
        {
            for (int i = fromIndex; i < toIndex; i++)
            {
                if (Signal.BeginField == tokens[i].Signal)
                {
                    i = DecodeField(buffer, bufferIndex, tokens, i, actingVersion, listener);
                }
            }
        }


        private static int DecodeGroups(DirectBuffer buffer, int bufferIndex, int actingVersion, IList<Token> tokens,
            int fromIndex, int toIndex, ITokenListener listener)
        {
            for (int i = fromIndex; i < toIndex; i++)
            {
                Token token = tokens[i];

                if (Signal.BeginGroup == token.Signal)
                {
                    Token blockLengthToken = tokens[i + 2];
                    int blockLength = Util.GetInt(buffer, bufferIndex + blockLengthToken.Offset,
                        blockLengthToken.Encoding.PrimitiveType, blockLengthToken.Encoding.ByteOrder);

                    Token numInGroupToken = tokens[i + 3];
                    int numInGroup = Util.GetInt(buffer, bufferIndex + numInGroupToken.Offset,
                        numInGroupToken.Encoding.PrimitiveType, numInGroupToken.Encoding.ByteOrder);

                    Token dimensionTypeComposite = tokens[i + 1];
                    bufferIndex += dimensionTypeComposite.Size;

                    int beginFieldsIndex = i + GroupDimTypeTokens;
                    int endGroupIndex = FindNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.EndGroup);
                    int nextGroupIndex = FindNextOrLimit(tokens, beginFieldsIndex, toIndex, Signal.BeginGroup);
                    int endOfFieldsIndex = Math.Min(endGroupIndex, nextGroupIndex) - 1;

                    for (int g = 0; g < numInGroup; g++)
                    {
                        listener.OnBeginGroup(token, g, numInGroup);

                        DecodeFields(buffer, bufferIndex, actingVersion, tokens, beginFieldsIndex, endOfFieldsIndex,
                            listener);
                        bufferIndex += blockLength;

                        if (nextGroupIndex < endGroupIndex)
                        {
                            bufferIndex = DecodeGroups(buffer, bufferIndex, actingVersion, tokens, nextGroupIndex,
                                toIndex, listener);
                        }

                        listener.OnEndGroup(token, g, numInGroup);
                    }

                    i = endGroupIndex;
                }
            }

            return bufferIndex;
        }

        private static int DecodeVarData(DirectBuffer buffer, int bufferIndex, IList<Token> tokens, int fromIndex,
            int toIndex, ITokenListener listener)
        {
            for (int i = fromIndex; i < toIndex; i++)
            {
                Token token = tokens[i];

                if (Signal.BeginVarData == token.Signal)
                {
                    Token lengthToken = tokens[i + 2];
                    int length = Util.GetInt(buffer, bufferIndex + lengthToken.Offset,
                        lengthToken.Encoding.PrimitiveType, lengthToken.Encoding.ByteOrder);

                    Token varDataToken = tokens[i + 3];
                    bufferIndex += varDataToken.Offset;

                    listener.OnVarData(token, buffer, bufferIndex, length, varDataToken);

                    bufferIndex += length;
                    i += VarDataTokens;
                }
            }

            return bufferIndex;
        }

        private static int DecodeField(DirectBuffer buffer, int bufferIndex, IList<Token> tokens, int fromIndex,
            int actingVersion, ITokenListener listener)
        {
            int toIndex = FindNextOrLimit(tokens, fromIndex + 1, tokens.Count, Signal.EndField);
            Token fieldToken = tokens[fromIndex];
            Token typeToken = tokens[fromIndex + 1];

            switch (typeToken.Signal)
            {
                case Signal.BeginComposite:
                    DecodeComposite(fieldToken, buffer, bufferIndex + typeToken.Offset, tokens, fromIndex + 1,
                        toIndex - 1, actingVersion, listener);
                    break;

                case Signal.BeginEnum:
                    listener.OnEnum(fieldToken, buffer, bufferIndex + typeToken.Offset, tokens, fromIndex + 1,
                        toIndex - 1, actingVersion);
                    break;

                case Signal.BeginSet:
                    listener.OnBitSet(fieldToken, buffer, bufferIndex + typeToken.Offset, tokens, fromIndex + 1,
                        toIndex - 1, actingVersion);
                    break;

                case Signal.Encoding:
                    listener.OnEncoding(fieldToken, buffer, bufferIndex + typeToken.Offset, typeToken, actingVersion);
                    break;
            }

            return toIndex;
        }

        private static void DecodeComposite(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int fromIndex, int toIndex, int actingVersion, ITokenListener listener)
        {
            listener.OnBeginComposite(fieldToken, tokens, fromIndex, toIndex);

            for (int i = fromIndex + 1; i < toIndex; i++)
            {
                Token token = tokens[i];
                listener.OnEncoding(token, buffer, bufferIndex + token.Offset, token, actingVersion);
            }

            listener.OnEndComposite(fieldToken, tokens, fromIndex, toIndex);
        }

        private static int FindNextOrLimit(IList<Token> tokens, int fromIndex, int limitIndex, Signal signal)
        {
            int i = fromIndex;
            for (; i < limitIndex; i++)
            {
                if (tokens[i].Signal == signal)
                {
                    break;
                }
            }

            return i;
        }
    }
}