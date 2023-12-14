package otf

import "fmt"

type OftHeaderDecoder struct {
	encodedLength          int32
	blockLengthOffset      int32
	templateIdOffset       int32
	schemaIdOffset         int32
	schemaVersionOffset    int32
	blockLengthType        PrimitiveType
	templateIdType         PrimitiveType
	schemaIdType           PrimitiveType
	schemaVersionType      PrimitiveType
	blockLengthByteOrder   ByteOrder
	templateIdByteOrder    ByteOrder
	schemaIdByteOrder      ByteOrder
	schemaVersionByteOrder ByteOrder
}

func NewOtfHeaderDecoder(tokens []Token) (*OftHeaderDecoder, error) {
	decoder := &OftHeaderDecoder{
		encodedLength: tokens[0].EncodedLength(),
	}

	var blockLengthToken,
		templateIdToken,
		schemaIdToken,
		versionToken *Token

	for i := range tokens {
		token := tokens[i]
		name := token.Name()
		switch name {
		case "blockLength":
			blockLengthToken = &token
		case "templateId":
			templateIdToken = &token
		case "schemaId":
			schemaIdToken = &token
		case "version":
			versionToken = &token
		}
	}

	if blockLengthToken == nil {
		return nil, fmt.Errorf("blockLength token not found")
	}

	decoder.blockLengthOffset = blockLengthToken.Offset()
	encoding := blockLengthToken.Encoding()
	decoder.blockLengthType = encoding.PrimitiveType()
	decoder.blockLengthByteOrder = encoding.ByteOrder()

	if templateIdToken == nil {
		return nil, fmt.Errorf("templateId token not found")
	}

	decoder.templateIdOffset = templateIdToken.Offset()
	templateIdEncoding := templateIdToken.Encoding()
	decoder.templateIdType = templateIdEncoding.PrimitiveType()
	decoder.templateIdByteOrder = templateIdEncoding.ByteOrder()

	if schemaIdToken == nil {
		return nil, fmt.Errorf("schemaId token not found")
	}

	decoder.schemaIdOffset = schemaIdToken.Offset()
	schemaIdEncoding := schemaIdToken.Encoding()
	decoder.schemaIdType = schemaIdEncoding.PrimitiveType()
	decoder.schemaIdByteOrder = schemaIdEncoding.ByteOrder()

	if versionToken == nil {
		return nil, fmt.Errorf("version token not found")
	}

	decoder.schemaVersionOffset = versionToken.Offset()
	versionEncoding := versionToken.Encoding()
	decoder.schemaVersionType = versionEncoding.PrimitiveType()
	decoder.schemaVersionByteOrder = versionEncoding.ByteOrder()

	return decoder, nil
}

func (o *OftHeaderDecoder) EncodedLength() int32 {
	return o.encodedLength
}

// All elements must be unsigned integers according to Specification

func (o *OftHeaderDecoder) TemplateId(headerBuffer []byte) (uint64, error) {
	return GetAsUInt(o.templateIdType, o.templateIdByteOrder, headerBuffer[o.templateIdOffset:])
}

func (o *OftHeaderDecoder) SchemaId(headerBuffer []byte) (uint64, error) {
	return GetAsUInt(o.schemaIdType, o.schemaIdByteOrder, headerBuffer[o.schemaIdOffset:])
}

func (o *OftHeaderDecoder) SchemaVersion(headerBuffer []byte) (uint64, error) {
	return GetAsUInt(o.schemaVersionType, o.schemaVersionByteOrder, headerBuffer[o.schemaVersionOffset:])
}

func (o *OftHeaderDecoder) BlockLength(headerBuffer []byte) (uint64, error) {
	return GetAsUInt(o.blockLengthType, o.blockLengthByteOrder, headerBuffer[o.blockLengthOffset:])
}
