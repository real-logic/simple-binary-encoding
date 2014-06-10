using System;
using System.Collections.Generic;
using System.IO;
using Adaptive.SimpleBinaryEncoding.Ir.Generated;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// <see cref="IrDecoder"/> can be used to read a SBE encoded intermediate representation generated with SBE Tool
    /// </summary>
    public sealed class IrDecoder
    {
        private const int Capacity = 4096;

        private readonly DirectBuffer _directBuffer;
        private readonly FrameCodec _frameCodec = new FrameCodec();
        private readonly long _size;
        private readonly TokenCodec _tokenCodec = new TokenCodec();
        private readonly byte[] _valArray = new byte[Capacity];
        private readonly DirectBuffer _valBuffer;
        private int _irVersion;
        private IList<Token> _irHeader;
        private int _irId;
        private string _irPackageName;
        private string _irNamespaceName = null;
        private int _offset;
        private readonly byte[] _buffer = new byte[1024];
        private string _semanticVersion;

        /// <summary>
        /// Initialize a new instance of <see cref="IrDecoder"/> from a file.
        /// </summary>
        /// <param name="fileName">path to the file containing the SBE encoded IR.</param>
        public IrDecoder(string fileName)
        {
            byte[] buffer = File.ReadAllBytes(fileName);
            _directBuffer = new DirectBuffer(buffer);
            _size = buffer.Length;
            _offset = 0;
            _valBuffer = new DirectBuffer(_valArray);
        }

        /// <summary>
        /// Initialize a new instance of <see cref="IrDecoder"/> from a byte array
        /// </summary>
        /// <param name="buffer">the byte array containg the SBE encoded IR.</param>
        public IrDecoder(byte[] buffer)
        {
            _size = buffer.Length;
            _directBuffer = new DirectBuffer(buffer);
            _offset = 0;
            _valBuffer = new DirectBuffer(_valArray);
        }

        /// <summary>
        /// Decodes the input to <see cref="IntermediateRepresentation"/>
        /// </summary>
        /// <returns></returns>
        public IntermediateRepresentation Decode()
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

            var ir = new IntermediateRepresentation(_irPackageName, _irNamespaceName, _irId, _irVersion, _semanticVersion, _irHeader);

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

            ir.AddMessage(tokens[index].Id, messageTokens);

            return index;
        }

        private void DecodeFrame()
        {
            _frameCodec.WrapForDecode(_directBuffer, _offset, FrameCodec.BlockLength, 0);

            _irId = _frameCodec.IrId;

            if (_frameCodec.IrVersion != 0)
            {
                throw new InvalidOperationException("Unknown SBE version: " + _frameCodec.IrVersion);
            }

            _irVersion = _frameCodec.SchemaVersion;

            var length = _frameCodec.GetPackageName(_buffer, 0, _buffer.Length);
            var encoding = System.Text.Encoding.GetEncoding(FrameCodec.PackageNameCharacterEncoding);
            _irPackageName = encoding.GetString(_buffer, 0, length);

            length = _frameCodec.GetNamespaceName(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(FrameCodec.NamespaceNameCharacterEncoding);
            _irNamespaceName = encoding.GetString(_buffer, 0, length);

            if (string.IsNullOrEmpty(_irNamespaceName))
            {
                _irNamespaceName = null;
            }

            length = _frameCodec.GetSemanticVersion(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(FrameCodec.SemanticVersionCharacterEncoding);
            _semanticVersion = encoding.GetString(_buffer, 0, length);

            if (string.IsNullOrEmpty(_semanticVersion))
            {
                _semanticVersion = null;
            }


            _offset += _frameCodec.Size;
        }

        private Token DecodeToken()
        {
            var tokenBuilder = new Token.Builder();
            var encBuilder = new Encoding.Builder();

            _tokenCodec.WrapForDecode(_directBuffer, _offset, TokenCodec.BlockLength, 0);

            tokenBuilder
                .Offset(_tokenCodec.TokenOffset)
                .Size(_tokenCodec.TokenSize)
                .Id(_tokenCodec.FieldId)
                .Version(_tokenCodec.TokenVersion)
                .Signal(IrUtil.MapSignal(_tokenCodec.Signal));

            PrimitiveType type = IrUtil.MapPrimitiveType(_tokenCodec.PrimitiveType);

            encBuilder
                .PrimitiveType(IrUtil.MapPrimitiveType(_tokenCodec.PrimitiveType))
                .ByteOrder(IrUtil.MapByteOrder(_tokenCodec.ByteOrder))
                .Presence(IrUtil.MapPresence(_tokenCodec.Presence));

            int stringLength = _tokenCodec.GetName(_buffer, 0, _buffer.Length);
            System.Text.Encoding encoding = System.Text.Encoding.GetEncoding(TokenCodec.NameCharacterEncoding);
            string value = encoding.GetString(_buffer, 0, stringLength);
            tokenBuilder.Name(value);

            encBuilder.ConstValue(IrUtil.Get(_valBuffer, type, _tokenCodec.GetConstValue(_valArray, 0, _valArray.Length)));
            encBuilder.MinValue(IrUtil.Get(_valBuffer, type, _tokenCodec.GetMinValue(_valArray, 0, _valArray.Length)));
            encBuilder.MaxValue(IrUtil.Get(_valBuffer, type, _tokenCodec.GetMaxValue(_valArray, 0, _valArray.Length)));
            encBuilder.NullValue(IrUtil.Get(_valBuffer, type, _tokenCodec.GetNullValue(_valArray, 0, _valArray.Length)));

            // character encoding
            stringLength = _tokenCodec.GetCharacterEncoding(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.CharacterEncodingCharacterEncoding);
            value = encoding.GetString(_buffer, 0, stringLength);
            encBuilder.CharacterEncoding(value.Length == 0 ? null : value);

            // epoch
            stringLength = _tokenCodec.GetEpoch(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.EpochCharacterEncoding);
            value = encoding.GetString(_buffer, 0, stringLength);
            encBuilder.Epoch(value.Length == 0 ? null : value);

            // time unit
            stringLength = _tokenCodec.GetTimeUnit(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.TimeUnitCharacterEncoding);
            value = encoding.GetString(_buffer, 0, stringLength);
            encBuilder.TimeUnit(value.Length == 0 ? null : value);

            // semantic type
            stringLength = _tokenCodec.GetSemanticType(_buffer, 0, _buffer.Length);
            encoding = System.Text.Encoding.GetEncoding(TokenCodec.SemanticTypeCharacterEncoding);
            value = encoding.GetString(_buffer, 0, stringLength);
            encBuilder.SemanticType(value.Length == 0 ? null : value);

            _offset += _tokenCodec.Size;

            return tokenBuilder.Encoding(encBuilder.Build()).Build();
        }
    }
}