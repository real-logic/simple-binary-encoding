using System;
using System.Collections.Generic;
using System.IO;
using Uk.Co.Real_logic.Sbe.Ir.Generated;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    public class IrDecoder
    {
        private const int Capacity = 4096;

        private readonly DirectBuffer _directBuffer;
        private readonly FrameCodec _frameCodec = new FrameCodec();
        private readonly long _size;
        private readonly TokenCodec _tokenCodec = new TokenCodec();
        private readonly byte[] _valArray = new byte[Capacity];
        private readonly DirectBuffer _valBuffer;
        private int IrVersion;
        private IList<Token> _irHeader;
        private string _irPackageName;
        private int _offset;

        public IrDecoder(string fileName)
        {
            byte[] buffer = File.ReadAllBytes(fileName);
            _directBuffer = new DirectBuffer(buffer);
            _size = buffer.Length;
            _offset = 0;
            _valBuffer = new DirectBuffer(_valArray);
        }

        public IrDecoder(byte[] buffer)
        {
            _size = buffer.Length;
            _directBuffer = new DirectBuffer(buffer);
            _offset = 0;
            _valBuffer = new DirectBuffer(_valArray);
        }

        public virtual IntermediateRepresentation Decode()
        {
            DecodeFrame();

            IList<Token> tokens = new List<Token>();
            while (_offset < _size)
            {
                tokens.Add(DecodeToken());
            }

            int i = 0, size = tokens.Count;

            if (tokens[0].Signal == Signal.BeginComposite)
            {
                i = CaptureHeader(tokens, 0);
            }

            var ir = new IntermediateRepresentation(_irPackageName, _irHeader, IrVersion);

            for (; i < size; i++)
            {
                if (tokens[i].Signal == Signal.BeginMessage)
                {
                    i = CaptureMessage(tokens, i, ir);
                }
            }

            return ir;
        }

        private int CaptureHeader(IList<Token> tokens, int index)
        {
            IList<Token> headerTokens = new List<Token>();

            Token token = tokens[index];
            headerTokens.Add(token);
            do
            {
                token = tokens[++index];
                headerTokens.Add(token);
            } while (Signal.EndComposite != token.Signal);

            _irHeader = headerTokens;

            return index;
        }

        private int CaptureMessage(IList<Token> tokens, int index, IntermediateRepresentation ir)
        {
            IList<Token> messageTokens = new List<Token>();

            Token token = tokens[index];
            messageTokens.Add(token);
            do
            {
                token = tokens[++index];
                messageTokens.Add(token);
            } while (Signal.EndMessage != token.Signal);

            ir.AddMessage(tokens[index].SchemaId, messageTokens);

            return index;
        }

        private void DecodeFrame()
        {
            _frameCodec.WrapForDecode(_directBuffer, _offset, FrameCodec.BlockLength, 0);

            if (_frameCodec.SbeIrVersion != 0)
            {
                throw new InvalidOperationException("Unknown SBE version: " + _frameCodec.SbeIrVersion);
            }

            var buffer = new byte[1024];

            IrVersion = _frameCodec.SchemaVersion;

            int length = _frameCodec.GetPackageVal(buffer, 0, buffer.Length);
            _irPackageName = System.Text.Encoding.UTF8.GetString(buffer, 0, length);

            _offset += _frameCodec.Size;
        }

        private Token DecodeToken()
        {
            var tokenBuilder = new Token.Builder();
            var encBuilder = new Encoding.Builder();

            var buffer = new byte[1024];

            _tokenCodec.WrapForDecode(_directBuffer, _offset, TokenCodec.BlockLength, 0);

            tokenBuilder
                .Offset(_tokenCodec.TokenOffset)
                .Size(_tokenCodec.TokenSize)
                .SchemaId(_tokenCodec.SchemaId)
                .Version(_tokenCodec.TokenVersion)
                .Signal(IrUtil.MapSignal(_tokenCodec.Signal));

            PrimitiveType type = IrUtil.MapPrimitiveType(_tokenCodec.PrimitiveType);

            encBuilder
                .PrimitiveType(IrUtil.MapPrimitiveType(_tokenCodec.PrimitiveType))
                .ByteOrder(IrUtil.MapByteOrder(_tokenCodec.ByteOrder))
                .Presence(IrUtil.MapPresence(_tokenCodec.Presence));

            int stringLength = _tokenCodec.GetName(buffer, 0, buffer.Length);
            System.Text.Encoding encoding = System.Text.Encoding.GetEncoding(TokenCodec.NameCharacterEncoding);
            string value = encoding.GetString(buffer, 0, stringLength);
            tokenBuilder.Name(value);

            encBuilder.ConstVal(IrUtil.Get(_valBuffer, type, _tokenCodec.GetConstVal(_valArray, 0, _valArray.Length)));
            encBuilder.MinVal(IrUtil.Get(_valBuffer, type, _tokenCodec.GetMinVal(_valArray, 0, _valArray.Length)));
            encBuilder.MaxVal(IrUtil.Get(_valBuffer, type, _tokenCodec.GetMaxVal(_valArray, 0, _valArray.Length)));
            encBuilder.NullVal(IrUtil.Get(_valBuffer, type, _tokenCodec.GetNullVal(_valArray, 0, _valArray.Length)));

            // character encoding
            stringLength = _tokenCodec.GetCharacterEncoding(buffer, 0, buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.CharacterEncodingCharacterEncoding);
            value = encoding.GetString(buffer, 0, stringLength);
            encBuilder.CharacterEncoding(value.Length == 0 ? null : value);

            // epoch
            stringLength = _tokenCodec.GetEpoch(buffer, 0, buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.EpochCharacterEncoding);
            value = encoding.GetString(buffer, 0, stringLength);
            encBuilder.Epoch(value.Length == 0 ? null : value);

            // time unit
            stringLength = _tokenCodec.GetTimeUnit(buffer, 0, buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.TimeUnitCharacterEncoding);
            value = encoding.GetString(buffer, 0, stringLength);
            encBuilder.TimeUnit(value.Length == 0 ? null : value);

            // semantic type
            stringLength = _tokenCodec.GetSemanticType(buffer, 0, buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.SemanticTypeCharacterEncoding);
            value = encoding.GetString(buffer, 0, stringLength);
            encBuilder.SemanticType(value.Length == 0 ? null : value);

            _offset += _tokenCodec.Size;

            return tokenBuilder.Encoding(encBuilder.Build()).Build();
        }
    }
}