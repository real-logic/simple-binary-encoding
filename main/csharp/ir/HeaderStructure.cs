using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.Util;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Metadata description for a message headerStructure
    /// </summary>
    public class HeaderStructure
    {
        internal const string TemplateId = "templateId";
        internal const string TemplateVersion = "version";
        internal const string BlockLength = "blockLength";

        private readonly IList<Token> _tokens;
        private PrimitiveType _templateIdType;
        private PrimitiveType _templateVersionType;
        private PrimitiveType _blockLengthType;

        internal HeaderStructure(IList<Token> tokens)
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

        /// <summary>
        /// List of <see cref="Token"/> associated with this <see cref="HeaderStructure"/>
        /// </summary>
        public IList<Token> Tokens
        {
            get { return _tokens; }
        }

        /// <summary>
        /// Underyling type used to store template id infomation
        /// </summary>
        public PrimitiveType TemplateIdType
        {
            get { return _templateIdType; }
        }

        /// <summary>
        /// Underyling type used to store template version information
        /// </summary>
        public PrimitiveType TemplateVersionType
        {
            get { return _templateVersionType; }
        }

        /// <summary>
        /// Underyling type used to store block length information
        /// </summary>
        public PrimitiveType BlockLengthType
        {
            get { return _blockLengthType; }
        }
    }
}