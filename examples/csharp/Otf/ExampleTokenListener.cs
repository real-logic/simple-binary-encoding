using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Adaptive.SimpleBinaryEncoding.ir;
using Adaptive.SimpleBinaryEncoding.Otf;
using Encoding = Adaptive.SimpleBinaryEncoding.ir.Encoding;

namespace Adaptive.SimpleBinaryEncoding.Examples.Otf
{
    public class ExampleTokenListener : ITokenListener
    {
        private readonly Stack<string> _namedScope = new Stack<string>();
        private readonly byte[] _tempBuffer = new byte[1024];

        public void OnBeginMessage(Token token)
        {
            _namedScope.Push(token.Name + ".");
        }

        public void OnEndMessage(Token token)
        {
            _namedScope.Pop();
        }

        public void OnEncoding(Token fieldToken, DirectBuffer buffer, int index, Token typeToken, int actingVersion)
        {
            string value = ReadEncodingAsString(buffer, index, typeToken, actingVersion);

            PrintScope();
            Console.WriteLine("{0}={1}", fieldToken.Name, value);
        }

        public void OnEnum(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens, int beginIndex,
            int endIndex, int actingVersion)
        {
            Token typeToken = tokens[beginIndex + 1];
            long encodedValue = ReadEncodingAsLong(buffer, bufferIndex, typeToken, actingVersion);

            string value = null;
            for (int i = beginIndex + 1; i < endIndex; i++)
            {
                // TODO to check..
                if (encodedValue == tokens[i].Encoding.ConstValue.LongValue())
                {
                    value = tokens[i].Name;
                    break;
                }
            }

            PrintScope();
            Console.WriteLine("{0}={1}", fieldToken.Name, value);
        }

        public virtual void OnBitSet(Token fieldToken, DirectBuffer buffer, int bufferIndex, IList<Token> tokens,
            int beginIndex, int endIndex, int actingVersion)
        {
            Token typeToken = tokens[beginIndex + 1];
            long encodedValue = ReadEncodingAsLong(buffer, bufferIndex, typeToken, actingVersion);

            PrintScope();
            Console.Write("{0}:", fieldToken.Name);

            for (int i = beginIndex + 1; i < endIndex; i++)
            {
                Console.Write(" {0}=", tokens[i].Name);

                long bitPosition = tokens[i].Encoding.ConstValue.LongValue();
                bool flag = (encodedValue & (long) Math.Pow(2, bitPosition)) != 0;

                Console.Write(Convert.ToString(flag));
            }

            Console.WriteLine();
        }

        public virtual void OnBeginComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            _namedScope.Push(fieldToken.Name + ".");
        }

        public virtual void OnEndComposite(Token fieldToken, IList<Token> tokens, int fromIndex, int toIndex)
        {
            _namedScope.Pop();
        }

        public virtual void OnBeginGroup(Token token, int groupIndex, int numInGroup)
        {
            _namedScope.Push(token.Name + ".");
        }

        public virtual void OnEndGroup(Token token, int groupIndex, int numInGroup)
        {
            _namedScope.Pop();
        }

        public virtual void OnVarData(Token fieldToken, DirectBuffer buffer, int bufferIndex, int length,
            Token typeToken)
        {
            string value;
            try
            {
                int varDataLength = buffer.GetBytes(bufferIndex, _tempBuffer, 0, length);
                System.Text.Encoding encoding = System.Text.Encoding.GetEncoding(typeToken.Encoding.CharacterEncoding);
                value = encoding.GetString(_tempBuffer, 0, varDataLength);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
                Console.Write(ex.StackTrace);
                return;
            }

            PrintScope();
            Console.WriteLine("{0}={1}", fieldToken.Name, value);
        }

        private static string ReadEncodingAsString(DirectBuffer buffer, int index, Token typeToken, int actingVersion)
        {
            PrimitiveValue constOrNotPresentValue = ConstOrNotPresentValue(typeToken, actingVersion);
            if (null != constOrNotPresentValue)
            {
                return constOrNotPresentValue.ToString();
            }

            var sb = new StringBuilder();
            Encoding encoding = typeToken.Encoding;
            int elementSize = encoding.PrimitiveType.Size;

            for (int i = 0, size = typeToken.ArrayLength; i < size; i++)
            {
                MapEncodingToString(sb, buffer, index + (i*elementSize), encoding);
                sb.Append(", ");
            }

            sb.Length = sb.Length - 2;

            return sb.ToString();
        }

        private long ReadEncodingAsLong(DirectBuffer buffer, int bufferIndex, Token typeToken, int actingVersion)
        {
            PrimitiveValue constOrNotPresentValue = ConstOrNotPresentValue(typeToken, actingVersion);
            if (null != constOrNotPresentValue)
            {
                return constOrNotPresentValue.LongValue();
            }

            return GetLong(buffer, bufferIndex, typeToken.Encoding);
        }

        private static PrimitiveValue ConstOrNotPresentValue(Token token, int actingVersion)
        {
            Encoding encoding = token.Encoding;
            if (encoding.Presence == Presence.Constant)
            {
                return encoding.ConstValue;
            }

            if (Presence.Optional == encoding.Presence)
            {
                if (actingVersion < token.Version)
                {
                    return encoding.ApplicableNullVal;
                }
            }

            return null;
        }

        //TODO big endian
        private static void MapEncodingToString(StringBuilder sb, DirectBuffer buffer, int index, Encoding encoding)
        {
            switch (encoding.PrimitiveType.Type)
            {
                case SbePrimitiveType.Char:
                    sb.Append('\'').Append(buffer.CharGet(index)).Append('\'');
                    break;

                case SbePrimitiveType.Int8:
                    sb.Append(buffer.Int8Get(index));
                    break;

                case SbePrimitiveType.Int16:
                    sb.Append(buffer.Int16GetLittleEndian(index));
                    break;

                case SbePrimitiveType.Int32:
                    sb.Append(buffer.Int32GetLittleEndian(index));
                    break;

                case SbePrimitiveType.Int64:
                    sb.Append(buffer.Int64GetLittleEndian(index));
                    break;

                case SbePrimitiveType.UInt8:
                    sb.Append(buffer.Uint8Get(index));
                    break;

                case SbePrimitiveType.UInt16:
                    sb.Append(buffer.Uint16GetLittleEndian(index));
                    break;

                case SbePrimitiveType.UInt32:
                    sb.Append(buffer.Uint32GetLittleEndian(index));
                    break;

                case SbePrimitiveType.UInt64:
                    sb.Append(buffer.Uint64GetLittleEndian(index));
                    break;

                case SbePrimitiveType.Float:
                    sb.Append(buffer.FloatGetLittleEndian(index));
                    break;

                case SbePrimitiveType.Double:
                    sb.Append(buffer.DoubleGetLittleEndian(index));
                    break;
            }
        }

        private static long GetLong(DirectBuffer buffer, int index, Encoding encoding)
        {
            switch (encoding.PrimitiveType.Type)
            {
                case SbePrimitiveType.Char:
                    return buffer.CharGet(index);

                case SbePrimitiveType.Int8:
                    return buffer.Int8Get(index);

                case SbePrimitiveType.Int16:
                    return buffer.Int16GetLittleEndian(index);

                case SbePrimitiveType.Int32:
                    return buffer.Int32GetLittleEndian(index);

                case SbePrimitiveType.Int64:
                    return buffer.Int64GetLittleEndian(index);

                case SbePrimitiveType.UInt8:
                    return buffer.Uint8Get(index);

                case SbePrimitiveType.UInt16:
                    return buffer.Uint16GetLittleEndian(index);

                case SbePrimitiveType.UInt32:
                    return buffer.Uint32GetLittleEndian(index);

                case SbePrimitiveType.UInt64:
                    return (long) buffer.Uint64GetLittleEndian(index); // TODO this is incorrect

                default:
                    throw new ArgumentException("Unsupported type for long: " + encoding.PrimitiveType);
            }
        }

        private void PrintScope()
        {
            foreach (string item in _namedScope.Reverse())
            {
                Console.Write(item);
            }
        }
    }
}