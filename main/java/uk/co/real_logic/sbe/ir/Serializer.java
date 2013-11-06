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

import uk.co.real_logic.sbe.generation.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.generated.SerializedFrame;
import uk.co.real_logic.sbe.ir.generated.SerializedToken;
import uk.co.real_logic.sbe.util.Verify;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class Serializer implements Closeable
{
    private static final int CAPACITY = 4096;

    private final FileChannel channel;
    private final ByteBuffer buffer;
    private final DirectBuffer directBuffer;
    private final IntermediateRepresentation ir;
    private final SerializedFrame serializedFrame = new SerializedFrame();
    private final SerializedToken serializedToken = new SerializedToken();

    public Serializer(final String fileName, final IntermediateRepresentation ir)
        throws FileNotFoundException
    {
        channel = new FileOutputStream(fileName).getChannel();
        buffer = ByteBuffer.allocateDirect(CAPACITY);
        directBuffer = new DirectBuffer(buffer);
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

    public int serialize()
        throws IOException
    {
        Verify.notNull(ir, "ir");

        int totalSize = 0;

        buffer.position(0);
        buffer.limit(serializeFrame());
        channel.write(buffer);

        totalSize += serializeTokenList(ir.header());

        for (final List<Token> tokenList : ir.messages())
        {
            totalSize += serializeTokenList(tokenList);
        }
        return totalSize;
    }

    private int serializeTokenList(final List<Token> tokenList)
        throws IOException
    {
        int totalSize = 0;

        for (final Token token : tokenList)
        {
            buffer.position(0);
            int tokenSize = serializeToken(token);
            buffer.limit(tokenSize);
            channel.write(buffer);
            totalSize += tokenSize;
        }

        return totalSize;
    }

    private int serializeFrame()
    {
        serializedFrame.reset(directBuffer, 0)
                       .sbeIrVersion((short)0)
                       .schemaVersion((short)0);

        serializedFrame.putPackageVal(ir.packageName().getBytes(), 0, ir.packageName().getBytes().length);

        return serializedFrame.size();
    }

    private int serializeToken(final Token token)
    {
        final byte[] byteArray = new byte[1024];

        serializedToken.reset(directBuffer, 0)
                       .tokenOffset(token.offset())
                       .tokenSize(token.size())
                       .schemaID((short)token.schemaId())
                       .signal(SerializationUtils.signal(token.signal()))
                       .primitiveType(SerializationUtils.primitiveType(token.encoding().primitiveType()))
                       .byteOrder(SerializationUtils.byteOrder(token.encoding().byteOrder()))
                       .sinceVersion((short)0);

        serializedToken.putName(token.name().getBytes(), 0, token.name().getBytes().length);
        serializedToken.putConstVal(byteArray, 0, SerializationUtils.putVal(byteArray, token.encoding().constVal()));
        serializedToken.putMinVal(byteArray, 0, SerializationUtils.putVal(byteArray, token.encoding().minVal()));
        serializedToken.putMaxVal(byteArray, 0, SerializationUtils.putVal(byteArray, token.encoding().maxVal()));
        serializedToken.putNullVal(byteArray, 0, SerializationUtils.putVal(byteArray, token.encoding().nullVal()));
        serializedToken.putCharacterEncoding(token.encoding().characterEncoding().getBytes(), 0,
                token.encoding().characterEncoding().getBytes().length);

        return serializedToken.size();
    }
}
