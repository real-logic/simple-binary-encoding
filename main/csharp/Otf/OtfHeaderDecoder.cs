using Adaptive.SimpleBinaryEncoding.ir;

namespace Adaptive.SimpleBinaryEncoding.Otf
{
    /// <summary>
    /// Used to decode a message header while doing on-the-fly decoding of a message stream.
    ///
    /// Meta data is cached to improve the performance of decoding headers.
    ///
    /// This class is thread safe.
    /// </summary>
    public class OtfHeaderDecoder
    {
        private readonly int _size;
        private readonly int _blockLengthOffset;
        private readonly int _templateIdOffset;
        private readonly int _schemaIdOffset;
        private readonly int _schemaVersionOffset;

        private readonly PrimitiveType _blockLengthType;
        private readonly PrimitiveType _templateIdType;
        private readonly PrimitiveType _schemaIdType;
        private readonly PrimitiveType _schemaVersionType;

        private readonly ByteOrder _blockLengthByteOrder;
        private readonly ByteOrder _templateIdByteOrder;
        private readonly ByteOrder _schemaIdByteOrder;
        private readonly ByteOrder _schemaVersionByteOrder;

        /// <summary>
        /// Read the message header structure and cache the meta data for finding the key fields for decoding messages.
        /// </summary>
        /// <param name="headerStructure"> for the meta data describing the message header. </param>
        public OtfHeaderDecoder(HeaderStructure headerStructure)
        {
            _size = headerStructure.Tokens[0].Size;

            foreach (Token token in headerStructure.Tokens)
            {
                switch (token.Name)
                {
                    case HeaderStructure.BlockLength:
                        _blockLengthOffset = token.Offset;
                        _blockLengthType = token.Encoding.PrimitiveType;
                        _blockLengthByteOrder = token.Encoding.ByteOrder;
                        break;

                    case HeaderStructure.TemplateId:
                        _templateIdOffset = token.Offset;
                        _templateIdType = token.Encoding.PrimitiveType;
                        _templateIdByteOrder = token.Encoding.ByteOrder;
                        break;

                    case HeaderStructure.SchemaId:
                        _schemaIdOffset = token.Offset;
                        _schemaIdType = token.Encoding.PrimitiveType;
                        _schemaIdByteOrder = token.Encoding.ByteOrder;
                        break;

                    case HeaderStructure.SchemaVersion:
                        _schemaVersionOffset = token.Offset;
                        _schemaVersionType = token.Encoding.PrimitiveType;
                        _schemaVersionByteOrder = token.Encoding.ByteOrder;
                        break;
                }
            }
        }

        /// <summary>
        /// The size of the message header in bytes.
        /// </summary>
        /// <value>the size of the message header in bytes.</value>
        public int Size
        {
            get { return _size; }
        }

        /// <summary>
        /// Get the template id from the message header.
        /// </summary>
        /// <param name="buffer"> from which to read the value. </param>
        /// <param name="bufferOffset"> in the buffer at which the message header begins. </param>
        /// <returns> the value of the template id. </returns>
        public int GetTemplateId(DirectBuffer buffer, int bufferOffset)
        {
            return Util.GetInt(buffer, bufferOffset + _templateIdOffset, _templateIdType, _templateIdByteOrder);
        }


        /// <summary>
        /// Get the schema id number from the message header.
        /// </summary>
        /// <param name="buffer">buffer from which to read the value.</param>
        /// <param name="bufferOffset">bufferOffset in the buffer at which the message header begins.</param>
        /// <returns>the value of the schema id number.</returns>
        public int GetSchemaId(DirectBuffer buffer, int bufferOffset)
        {
            return Util.GetInt(buffer, bufferOffset + _schemaIdOffset, _schemaIdType, _schemaIdByteOrder);
        }

        /// <summary>
        /// Get the schema version number from the message header.
        /// </summary>
        /// <param name="buffer"> from which to read the value. </param>
        /// <param name="bufferOffset"> in the buffer at which the message header begins. </param>
        /// <returns> the value of the schema version number. </returns>
        public virtual int GetSchemaVersion(DirectBuffer buffer, int bufferOffset)
        {
            return Util.GetInt(buffer, bufferOffset + _schemaVersionOffset, _schemaVersionType, _schemaVersionByteOrder);
        }

        /// <summary>
        /// Get the block length of the root block in the message.
        /// </summary>
        /// <param name="buffer"> from which to read the value. </param>
        /// <param name="bufferOffset"> in the buffer at which the message header begins. </param>
        /// <returns> the length of the root block in the coming message. </returns>
        public virtual int GetBlockLength(DirectBuffer buffer, int bufferOffset)
        {
            return Util.GetInt(buffer, bufferOffset + _blockLengthOffset, _blockLengthType, _blockLengthByteOrder);
        }
    }
}