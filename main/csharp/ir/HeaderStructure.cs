using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.Util;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Metadata description for a message headerStructure
    /// </summary>
    public class HeaderStructure
    {
        public const string TemplateId = "templateId";
        public const string TemplateVersion = "version";
        public const string BlockLength = "blockLength";

        private readonly IList<Token> _tokens;
        private PrimitiveType _templateIdType;
        private PrimitiveType _templateVersionType;
        private PrimitiveType _blockLengthType;

        public HeaderStructure(IList<Token> tokens)
        {
            Verify.NotNull(tokens, "tokens");
            _tokens = tokens;

            CaptureEncodings(tokens);

            Verify.NotNull(_templateIdType, "templateIdType");
            Verify.NotNull(_templateVersionType, "templateVersionType");
            Verify.NotNull(_blockLengthType, "blockLengthType");
        }

        private void CaptureEncodings(IList<Token> tokens)
        {
            foreach (Token token in tokens)
            {
                switch (token.Name)
                {
                    case TemplateId:
                        _templateIdType = token.Encoding.PrimitiveType;
                        break;

                    case TemplateVersion:
                        _templateVersionType = token.Encoding.PrimitiveType;
                        break;

                    case BlockLength:
                        _blockLengthType = token.Encoding.PrimitiveType;
                        break;
                }
            }
        }

        public IList<Token> Tokens
        {
            get { return _tokens; }
        }

        public PrimitiveType TemplateIdType
        {
            get { return _templateIdType; }
        }

        public PrimitiveType TemplateVersionType
        {
            get { return _templateVersionType; }
        }

        public PrimitiveType BlockLengthType
        {
            get { return _blockLengthType; }
        }
    }
}