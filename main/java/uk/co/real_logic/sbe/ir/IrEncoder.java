/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.ir;

import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.generated.FrameCodec;
import uk.co.real_logic.sbe.ir.generated.TokenCodec;
import uk.co.real_logic.sbe.util.Verify;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import static uk.co.real_logic.sbe.ir.IrUtil.*;

public class IrEncoder implements Closeable
{
    private static final int CAPACITY = 4096;

    private final FileChannel channel;
    private final ByteBuffer resultBuffer;
    private final ByteBuffer buffer;
    private final DirectBuffer directBuffer;
    private final Ir ir;
    private final FrameCodec frameCodec = new FrameCodec();
    private final TokenCodec tokenCodec = new TokenCodec();
    private final byte[] valArray = new byte[CAPACITY];
    private final DirectBuffer valBuffer = new DirectBuffer(valArray);
    private int totalSize = 0;

    public IrEncoder(final String fileName, final Ir ir)
        throws FileNotFoundException
    {
        channel = new FileOutputStream(fileName).getChannel();
        resultBuffer = null;
        buffer = ByteBuffer.allocateDirect(CAPACITY);
        directBuffer = new DirectBuffer(buffer);
        this.ir = ir;
    }

    public IrEncoder(final ByteBuffer buffer, final Ir ir)
    {
        channel = null;
        resultBuffer = buffer;
        this.buffer = ByteBuffer.allocateDirect(CAPACITY);
        directBuffer = new DirectBuffer(this.buffer);
        this.ir = ir;
    }

    public void close()
        throws IOException
    {
        if (channel != null)
        {
            channel.close();
        }
    }

    public int encode()
        throws IOException
    {
        Verify.notNull(ir, "ir");

        write(buffer, encodeFrame());

        encodeTokenList(ir.headerStructure().tokens());

        for (final List<Token> tokenList : ir.messages())
        {
            encodeTokenList(tokenList);
        }

        return totalSize;
    }

    private void encodeTokenList(final List<Token> tokenList)
        throws IOException
    {
        for (final Token token : tokenList)
        {
            write(buffer, encodeToken(token));
        }
    }

    private void write(final ByteBuffer buffer, final int size)
        throws IOException
    {
        buffer.position(0);
        buffer.limit(size);

        if (channel != null)
        {
            channel.write(buffer);
        }
        else if (resultBuffer != null)
        {
            resultBuffer.put(buffer);
        }

        totalSize += size;
    }

    private int encodeFrame()
        throws UnsupportedEncodingException
    {
        frameCodec.wrapForEncode(directBuffer, 0)
                  .irId(ir.id())
                  .irVersion(0)
                  .schemaVersion(ir.version());

        final byte[] packageBytes = ir.packageName().getBytes(FrameCodec.packageNameCharacterEncoding());
        frameCodec.putPackageName(packageBytes, 0, packageBytes.length);

        final byte[] namespaceBytes = getBytes(ir.namespaceName(), FrameCodec.namespaceNameCharacterEncoding());
        frameCodec.putNamespaceName(namespaceBytes, 0, namespaceBytes.length);

        final byte[] semanticVersionBytes = getBytes(ir.semanticVersion(), FrameCodec.semanticVersionCharacterEncoding());
        frameCodec.putSemanticVersion(semanticVersionBytes, 0, semanticVersionBytes.length);

        return frameCodec.size();
    }

    private int encodeToken(final Token token)
        throws UnsupportedEncodingException
    {
        final Encoding encoding = token.encoding();
        final PrimitiveType type = encoding.primitiveType();

        tokenCodec.wrapForEncode(directBuffer, 0)
                  .tokenOffset(token.offset())
                  .tokenSize(token.size())
                  .fieldId(token.id())
                  .tokenVersion(token.version())
                  .signal(mapSignal(token.signal()))
                  .primitiveType(mapPrimitiveType(type))
                  .byteOrder(mapByteOrder(encoding.byteOrder()))
                  .presence(mapPresence(encoding.presence()));

        final byte[] nameBytes = token.name().getBytes(TokenCodec.nameCharacterEncoding());
        tokenCodec.putName(nameBytes, 0, nameBytes.length);

        tokenCodec.putConstValue(valArray, 0, put(valBuffer, encoding.constValue(), type));
        tokenCodec.putMinValue(valArray, 0, put(valBuffer, encoding.minValue(), type));
        tokenCodec.putMaxValue(valArray, 0, put(valBuffer, encoding.maxValue(), type));
        tokenCodec.putNullValue(valArray, 0, put(valBuffer, encoding.nullValue(), type));

        final byte[] charEncodingBytes = getBytes(encoding.characterEncoding(), TokenCodec.characterEncodingCharacterEncoding());
        tokenCodec.putCharacterEncoding(charEncodingBytes, 0, charEncodingBytes.length);

        final byte[] epochBytes = getBytes(encoding.epoch(), TokenCodec.epochCharacterEncoding());
        tokenCodec.putEpoch(epochBytes, 0, epochBytes.length);

        final byte[] timeUnitBytes = getBytes(encoding.timeUnit(), TokenCodec.timeUnitCharacterEncoding());
        tokenCodec.putTimeUnit(timeUnitBytes, 0, timeUnitBytes.length);

        final byte[] semanticTypeBytes = getBytes(encoding.semanticType(), TokenCodec.semanticTypeCharacterEncoding());
        tokenCodec.putSemanticType(semanticTypeBytes, 0, semanticTypeBytes.length);

        return tokenCodec.size();
    }
}
