using System.Collections.Generic;
using Adaptive.SimpleBinaryEncoding.Util;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Metadata description for a message headerStructure
    /// </summary>
    public class HeaderStructure
    {
        internal const string BlockLength = "blockLength";
        internal const string TemplateId = "templateId";
        internal const string SchemaId = "schemaId";
        internal const string SchemaVersion = "version";

        private readonly IList<Token> _tokens;
        private PrimitiveType _blockLengthType;
        private PrimitiveType _templateIdType;
        private PrimitiveType _schemaIdType;
        private PrimitiveType _schemaVersionType;

        internal HeaderStructure(IList<Token> tokens)
        {
            Verify.NotNull(tokens, "tokens");
            _tokens = tokens;

            CaptureEncodings(tokens);

            Verify.NotNull(_blockLengthType, "blockLengthType");
            Verify.NotNull(_templateIdType, "templateIdType");
            Verify.NotNull(_schemaIdType, "schemaIdType");
            Verify.NotNull(_schemaVersionType, "schemaVersionType");
        }

        private void CaptureEncodings(IList<Token> tokens)
        {
            foreach (Token token in tokens)
            {
                switch (token.Name)
                {
                    case BlockLength:
                        _blockLengthType = token.Encoding.PrimitiveType;
                        break;

                    case TemplateId:
                        _templateIdType = token.Encoding.PrimitiveType;
                        break;

                    case SchemaId:
                        _schemaIdType = token.Encoding.PrimitiveType;
                        break;

                    case SchemaVersion:
                        _schemaVersionType = token.Encoding.PrimitiveType;
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
        /// Underyling type used to store block length information
        /// </summary>
        public PrimitiveType BlockLengthType
        {
            get { return _blockLengthType; }
        }

        /// <summary>
        /// Underyling type used to store template id infomation
        /// </summary>
        public PrimitiveType TemplateIdType
        {
            get { return _templateIdType; }
        }

        /// <summary>
        /// Underyling type used to store schema id information
        /// </summary>
        public PrimitiveType SchemaIdType
        {
            get { return _schemaIdType; }
        }

        /// <summary>
        /// Underyling type used to store schema version information
        /// </summary>
        public PrimitiveType SchemaVersionType
        {
            get { return _schemaVersionType; }
        }
    }
}