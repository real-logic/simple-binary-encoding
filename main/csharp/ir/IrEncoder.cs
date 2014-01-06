using System;
using System.Collections.Generic;
using Uk.Co.Real_logic.Sbe.Ir.Generated;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    public class IrEncoder : IDisposable
    {

    private const int CAPACITY = 4096;

	private readonly FileChannel Channel;
	private readonly ByteBuffer ResultBuffer;
	private readonly ByteBuffer Buffer;
	private readonly DirectBuffer DirectBuffer;
	private readonly IntermediateRepresentation Ir;
	private readonly FrameCodec FrameCodec = new FrameCodec();
	private readonly TokenCodec TokenCodec = new TokenCodec();
	private readonly sbyte[] ValArray = new sbyte[CAPACITY];
	private readonly DirectBuffer ValBuffer = new DirectBuffer(ValArray);
	private int TotalSize = 0;

	public IrEncoder(string fileName, IntermediateRepresentation ir)
	{
		Channel = (new FileOutputStream(fileName)).Channel;
		ResultBuffer = null;
		Buffer = ByteBuffer.allocateDirect(CAPACITY);
		DirectBuffer = new DirectBuffer(Buffer);
		this.Ir = ir;
	}

	public IrEncoder(ByteBuffer buffer, IntermediateRepresentation ir)
	{
		Channel = null;
		ResultBuffer = buffer;
		this.Buffer = ByteBuffer.allocateDirect(CAPACITY);
		DirectBuffer = new DirectBuffer(this.Buffer);
		this.Ir = ir;
	}

        // TODO dispose pattern
	public void Dispose()
	{
		if (Channel != null)
		{
			Channel.Close();
		}
	}

	public virtual int Encode()
	{
		Verify.notNull(Ir, "ir");

		Write(Buffer, EncodeFrame());

		EncodeTokenList(Ir.headerStructure().tokens());

		foreach (List<Token> tokenList in Ir.messages())
		{
			EncodeTokenList(tokenList);
		}

		return TotalSize;
	}

	private void EncodeTokenList(IList<Token> tokenList)
	{
		foreach (Token token in tokenList)
		{
			Write(Buffer, encodeToken(token));
		}
	}

	private void Write(ByteBuffer buffer, int size)
	{
		buffer.position(0);
		buffer.limit(size);

		if (Channel != null)
		{
			Channel.write(buffer);
		}
		else if (ResultBuffer != null)
		{
			ResultBuffer.put(buffer);
		}

		TotalSize += size;
	}

	private int EncodeFrame()
	{
		FrameCodec.WrapForEncode(DirectBuffer, 0).sbeIrVersion(0).schemaVersion(Ir.version());

		FrameCodec.SetPackageVal(Ir.packageName().Bytes, 0, Ir.packageName().Bytes.length);

		return FrameCodec.size();
	}

    private int EncodeToken(Token token)
    {
        Encoding encoding = token.encoding();
        PrimitiveType type = encoding.primitiveType();

        tokenCodec.wrapForEncode(directBuffer, 0).tokenOffset(token.offset()).tokenSize(token.size()).schemaId(token.schemaId()).tokenVersion(token.version()).signal(mapSignal(token.signal())).primitiveType(mapPrimitiveType(type)).byteOrder(mapByteOrder(encoding.byteOrder())).presence(mapPresence(encoding.presence()));

        sbyte[] nameBytes = token.name().getBytes(TokenCodec.nameCharacterEncoding());
        tokenCodec.putName(nameBytes, 0, nameBytes.Length);

        tokenCodec.putConstVal(valArray, 0, put(valBuffer, encoding.constVal(), type));
        tokenCodec.putMinVal(valArray, 0, put(valBuffer, encoding.minVal(), type));
        tokenCodec.putMaxVal(valArray, 0, put(valBuffer, encoding.maxVal(), type));
        tokenCodec.putNullVal(valArray, 0, put(valBuffer, encoding.nullVal(), type));

        sbyte[] charEncodingBytes = getBytes(encoding.characterEncoding(), TokenCodec.characterEncodingCharacterEncoding());
        tokenCodec.putCharacterEncoding(charEncodingBytes, 0, charEncodingBytes.Length);

        sbyte[] epochBytes = getBytes(encoding.epoch(), TokenCodec.epochCharacterEncoding());
        tokenCodec.putEpoch(epochBytes, 0, epochBytes.Length);

        sbyte[] timeUnitBytes = getBytes(encoding.timeUnit(), TokenCodec.timeUnitCharacterEncoding());
        tokenCodec.putTimeUnit(timeUnitBytes, 0, timeUnitBytes.Length);

        sbyte[] semanticTypeBytes = getBytes(encoding.semanticType(), TokenCodec.semanticTypeCharacterEncoding());
        tokenCodec.putSemanticType(semanticTypeBytes, 0, semanticTypeBytes.Length);

        return tokenCodec.size();
    }
    }
}