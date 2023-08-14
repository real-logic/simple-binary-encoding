package otf

import "fmt"

type OnBeginMessage func(token Token)
type OnEndMessage func(token Token)
type OnEncoding func(token Token, buffer []byte, actingVersion uint64)
type OnEnum func(token Token, buffer []byte, tokens []Token, fromIndex int, toIndex int, actingVersion uint64)
type OnBitSet func(token Token, buffer []byte, tokens []Token, fromIndex int, toIndex int, actingVersion uint64)
type OnBeginComposite func(token Token, tokens []Token, fromIndex int, toIndex int)
type OnEndComposite func(token Token, tokens []Token, fromIndex int, toIndex int)
type OnGroupHeader func(token Token, numInGroup uint64)
type OnBeginGroup func(token Token, groupIndex uint64, numInGroup uint64)
type OnEndGroup func(token Token, groupIndex uint64, numInGroup uint64)
type OnVarData func(token Token, buffer []byte, length uint64, typeToken Token)

type TokenListener interface {
	OnBeginMessage(token Token)
	OnEndMessage(token Token)
	OnEncoding(fieldToken Token, buffer []byte, typeToken Token, actingVersion uint64)
	OnEnum(fieldToken Token, buffer []byte, tokens []Token, fromIndex int, toIndex int, actingVersion uint64)
	OnBitSet(fieldToken Token, buffer []byte, tokens []Token, fromIndex int, toIndex int, actingVersion uint64)
	OnBeginComposite(fieldToken Token, tokens []Token, fromIndex int, toIndex int)
	OnEndComposite(fieldToken Token, tokens []Token, fromIndex int, toIndex int)
	OnGroupHeader(token Token, numInGroup uint64)
	OnBeginGroup(token Token, groupIndex uint64, numInGroup uint64)
	OnEndGroup(token Token, groupIndex uint64, numInGroup uint64)
	OnVarData(fieldToken Token, buffer []byte, length uint64, typeToken Token)
}

func DecodeComposite(
	fieldToken Token,
	buffer []byte,
	bufferIndex int,
	length uint64,
	tokens []Token,
	tokenIndex int,
	toIndex int,
	actingVersion uint64,
	listener TokenListener) int {
	listener.OnBeginComposite(fieldToken, tokens, tokenIndex, toIndex)
	for i := tokenIndex + 1; i < toIndex; {
		token := tokens[i]
		nextFieldIndex := i + int(token.ComponentTokenCount())
		offset := int(token.Offset())
		switch token.Signal() {
		case SignalBeginComposite:
			DecodeComposite(
				fieldToken,
				buffer,
				bufferIndex+offset,
				length,
				tokens,
				i,
				nextFieldIndex-1,
				actingVersion,
				listener)
		case SignalBeginEnum:
			listener.OnEnum(
				fieldToken,
				buffer[bufferIndex+offset:],
				tokens,
				i,
				nextFieldIndex-1,
				actingVersion)
		case SignalBeginSet:
			listener.OnBitSet(
				fieldToken,
				buffer[bufferIndex+offset:],
				tokens,
				i,
				nextFieldIndex-1,
				actingVersion)
		case SignalEncoding:
			listener.OnEncoding(token, buffer[bufferIndex+offset:], token, actingVersion)
		}
		i += int(token.ComponentTokenCount())
	}
	listener.OnEndComposite(fieldToken, tokens, tokenIndex, toIndex)
	return tokenIndex
}

func DecodeFields(
	buffer []byte,
	bufferIndex int,
	length uint64,
	actingVersion uint64,
	tokens []Token,
	tokenIndex int,
	numTokens int,
	listener TokenListener) int {

	for tokenIndex < numTokens {
		fieldToken := tokens[tokenIndex]
		if SignalBeginField != fieldToken.Signal() {
			break
		}
		nextFieldIndex := tokenIndex + int(fieldToken.ComponentTokenCount())
		tokenIndex++

		typeToken := tokens[tokenIndex]
		offset := bufferIndex + int(typeToken.Offset())
		switch typeToken.Signal() {
		case SignalBeginComposite:
			DecodeComposite(
				fieldToken,
				buffer,
				offset,
				length,
				tokens,
				tokenIndex,
				nextFieldIndex-2,
				actingVersion,
				listener)
		case SignalBeginEnum:
			listener.OnEnum(
				fieldToken,
				buffer[offset:],
				tokens,
				tokenIndex,
				nextFieldIndex-2,
				actingVersion)
		case SignalBeginSet:
			listener.OnBitSet(
				fieldToken,
				buffer[offset:],
				tokens,
				tokenIndex,
				nextFieldIndex-2,
				actingVersion)
		case SignalEncoding:
			listener.OnEncoding(fieldToken, buffer[offset:], typeToken, actingVersion)
		}
		tokenIndex = nextFieldIndex
	}

	return tokenIndex
}

func DecodeData(
	buffer []byte,
	bufferIndex int,
	length uint64,
	tokens []Token,
	tokenIndex int,
	numTokens int,
	actingVersion uint64,
	listener TokenListener) int {
	for tokenIndex < numTokens {
		token := tokens[tokenIndex]
		if SignalBeginVarData != token.Signal() {
			break
		}
		isPresent := token.TokenVersion() <= int32(actingVersion)
		lengthToken := tokens[tokenIndex+2]
		dataToken := tokens[tokenIndex+3]
		if (uint64(bufferIndex) + uint64(dataToken.Offset())) > length {
			panic("length too short for data length field")
		}
		dataLength := uint64(0)
		if isPresent {
			lengthTokenEncoding := lengthToken.Encoding()

			var err error
			dataLength, err = lengthTokenEncoding.GetAsUInt(buffer[bufferIndex+int(lengthToken.Offset()):])
			if err != nil {
				panic(fmt.Errorf("invalid length encoding %s", err))
			}
			bufferIndex += int(dataToken.Offset())
		}

		if (uint64(bufferIndex) + dataLength) > length {
			panic("length too short for data field")
		}
		listener.OnVarData(token, buffer[bufferIndex:], dataLength, dataToken)

		bufferIndex += int(dataLength)
		tokenIndex += int(token.ComponentTokenCount())
	}
	return bufferIndex
}

func DecodeGroups(
	buffer []byte,
	bufferIndex int,
	length uint64,
	actingVersion uint64,
	tokens []Token,
	tokenIndex int,
	numTokens int,
	listener TokenListener) (int, int) {
	for tokenIndex < numTokens {
		token := tokens[tokenIndex]
		if SignalBeginGroup != token.Signal() {
			break
		}
		isPresent := token.TokenVersion() <= int32(actingVersion)
		dimensionsTypeComposite := tokens[tokenIndex+1]
		dimensionsLength := int(dimensionsTypeComposite.EncodedLength())
		if (uint64(bufferIndex) + uint64(dimensionsLength)) > length {
			panic("length too short for dimensions composite")
		}

		blockLengthToken := tokens[tokenIndex+2]
		numInGroupToken := tokens[tokenIndex+3]
		blockLength := uint64(0)
		numInGroup := uint64(0)
		if isPresent {
			blockLengthTokenEncoding := blockLengthToken.Encoding()
			var err error
			blockLength, err = blockLengthTokenEncoding.GetAsUInt(buffer[bufferIndex+int(blockLengthToken.Offset()):])
			if err != nil {
				panic(fmt.Errorf("invalid block length encoding %s", err))
			}
			numInGroupTokenEncoding := numInGroupToken.Encoding()
			numInGroup, err = numInGroupTokenEncoding.GetAsUInt(buffer[bufferIndex+int(numInGroupToken.Offset()):])
			if err != nil {
				panic(fmt.Errorf("invalid num in group encoding %s", err))
			}
			bufferIndex += dimensionsLength
		}

		beginFieldsIndex := tokenIndex + int(dimensionsTypeComposite.ComponentTokenCount()) + 1
		listener.OnGroupHeader(token, numInGroup)
		for i := uint64(0); i < numInGroup; i++ {
			listener.OnBeginGroup(token, i, numInGroup)
			if (uint64(bufferIndex) + blockLength) > length {
				panic("length too short for group blockLength")
			}
			afterFieldsIndex := DecodeFields(
				buffer,
				bufferIndex,
				length,
				actingVersion,
				tokens,
				beginFieldsIndex,
				numTokens,
				listener)
			bufferIndex += int(blockLength)
			groupIndex, groupNumTokens := DecodeGroups(
				buffer,
				bufferIndex,
				length,
				actingVersion,
				tokens,
				afterFieldsIndex,
				numTokens,
				listener)
			bufferIndex = DecodeData(
				buffer,
				groupIndex,
				length,
				tokens,
				groupNumTokens,
				numTokens,
				actingVersion,
				listener,
			)
			listener.OnEndGroup(token, i, numInGroup)
		}
		tokenIndex += int(token.ComponentTokenCount())
	}
	return bufferIndex, tokenIndex
}

func Decode(
	buffer []byte,
	actingVersion uint64,
	blockLength uint64,
	tokens []Token,
	listener TokenListener) int {
	listener.OnBeginMessage(tokens[0])
	length := uint64(len(buffer))
	if length < blockLength {
		panic("length too short for message blockLength")
	}
	numTokens := len(tokens)
	tokenIndex := DecodeFields(
		buffer,
		0,
		length,
		actingVersion,
		tokens,
		1,
		numTokens,
		listener,
	)
	bufferIndex := int(blockLength)
	groupIndex, groupNumTokens := DecodeGroups(
		buffer,
		bufferIndex,
		length,
		actingVersion,
		tokens,
		tokenIndex,
		numTokens,
		listener,
	)
	bufferIndex = DecodeData(
		buffer,
		groupIndex,
		length,
		tokens,
		groupNumTokens,
		numTokens,
		actingVersion,
		listener,
	)
	listener.OnEndMessage(tokens[numTokens-1])
	return bufferIndex
}
