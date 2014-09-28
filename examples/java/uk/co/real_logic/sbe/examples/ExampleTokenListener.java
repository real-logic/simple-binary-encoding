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
package uk.co.real_logic.sbe.examples;

import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.codec.java.CodecUtil;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.otf.TokenListener;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class ExampleTokenListener implements TokenListener
{
    private final PrintWriter out;
    private final Deque<String> namedScope = new ArrayDeque<>();
    private final byte[] tempBuffer = new byte[1024];

    public ExampleTokenListener(final PrintWriter out)
    {
        this.out = out;
    }

    public void onBeginMessage(final Token token)
    {
        namedScope.push(token.name() + ".");
    }

    public void onEndMessage(final Token token)
    {
        namedScope.pop();
    }

    public void onEncoding(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int index,
        final Token typeToken,
        final int actingVersion)
    {
        final CharSequence value = readEncodingAsString(buffer, index, typeToken, actingVersion);

        printScope();
        out.append(fieldToken.name())
           .append('=')
           .append(value)
           .println();
    }

    public void onEnum(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int beginIndex,
        final int endIndex,
        final int actingVersion)
    {
        final Token typeToken = tokens.get(beginIndex + 1);
        final long encodedValue = readEncodingAsLong(buffer, bufferIndex, typeToken, actingVersion);

        String value = null;
        for (int i = beginIndex + 1; i < endIndex; i++)
        {
            if (encodedValue == tokens.get(i).encoding().constValue().longValue())
            {
                value = tokens.get(i).name();
                break;
            }
        }

        printScope();
        out.append(fieldToken.name())
           .append('=')
           .append(value)
           .println();
    }

    public void onBitSet(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int beginIndex,
        final int endIndex,
        final int actingVersion)
    {
        final Token typeToken = tokens.get(beginIndex + 1);
        final long encodedValue = readEncodingAsLong(buffer, bufferIndex, typeToken, actingVersion);

        printScope();
        out.append(fieldToken.name()).append(':');

        for (int i = beginIndex + 1; i < endIndex; i++)
        {
            out.append(' ').append(tokens.get(i).name()).append('=');

            final long bitPosition = tokens.get(i).encoding().constValue().longValue();
            final boolean flag = (encodedValue & (1L << bitPosition)) != 0;

            out.append(Boolean.toString(flag));
        }

        out.println();
    }

    public void onBeginComposite(final Token fieldToken, final List<Token> tokens, final int fromIndex, final int toIndex)
    {
        namedScope.push(fieldToken.name() + ".");
    }

    public void onEndComposite(final Token fieldToken, final List<Token> tokens, final int fromIndex, final int toIndex)
    {
        namedScope.pop();
    }

    public void onGroupHeader(final Token token, final int numInGroup)
    {
        printScope();
        out.append(token.name())
           .append(" Group Header : numInGroup=")
           .append(Integer.toString(numInGroup))
           .println();
    }

    public void onBeginGroup(final Token token, final int groupIndex, final int numInGroup)
    {
        namedScope.push(token.name() + ".");
    }

    public void onEndGroup(final Token token, final int groupIndex, final int numInGroup)
    {
        namedScope.pop();
    }

    public void onVarData(
        final Token fieldToken, final DirectBuffer buffer, final int bufferIndex, final int length, final Token typeToken)
    {
        final String value;
        try
        {
            value = new String(
                tempBuffer, 0, buffer.getBytes(bufferIndex, tempBuffer, 0, length), typeToken.encoding().characterEncoding());
        }
        catch (final UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return;
        }

        printScope();
        out.append(fieldToken.name())
           .append('=')
           .append(value)
           .println();
    }

    private static CharSequence readEncodingAsString(
        final DirectBuffer buffer, final int index, final Token typeToken, final int actingVersion)
    {
        final PrimitiveValue constOrNotPresentValue = constOrNotPresentValue(typeToken, actingVersion);
        if (null != constOrNotPresentValue)
        {
            return constOrNotPresentValue.toString();
        }

        final StringBuilder sb = new StringBuilder();
        final Encoding encoding = typeToken.encoding();
        final int elementSize = encoding.primitiveType().size();

        for (int i = 0, size = typeToken.arrayLength(); i < size; i++)
        {
            mapEncodingToString(sb, buffer, index + (i * elementSize), encoding);
            sb.append(", ");
        }

        sb.setLength(sb.length() - 2);

        return sb;
    }

    private long readEncodingAsLong(
        final DirectBuffer buffer, final int bufferIndex, final Token typeToken, final int actingVersion)
    {
        final PrimitiveValue constOrNotPresentValue = constOrNotPresentValue(typeToken, actingVersion);
        if (null != constOrNotPresentValue)
        {
            return constOrNotPresentValue.longValue();
        }

        return getLong(buffer, bufferIndex, typeToken.encoding());
    }

    private static PrimitiveValue constOrNotPresentValue(final Token token, final int actingVersion)
    {
        final Encoding encoding = token.encoding();
        if (Encoding.Presence.CONSTANT == encoding.presence())
        {
            return encoding.constValue();
        }
        else if (Encoding.Presence.OPTIONAL == encoding.presence())
        {
            if (actingVersion < token.version())
            {
                return encoding.applicableNullValue();
            }
        }

        return null;
    }

    private static void mapEncodingToString(
        final StringBuilder sb, final DirectBuffer buffer, final int index, final Encoding encoding)
    {
        switch (encoding.primitiveType())
        {
            case CHAR:
                sb.append('\'').append((char)CodecUtil.charGet(buffer, index)).append('\'');
                break;

            case INT8:
                sb.append(CodecUtil.int8Get(buffer, index));
                break;

            case INT16:
                sb.append(CodecUtil.int16Get(buffer, index, encoding.byteOrder()));
                break;

            case INT32:
                sb.append(CodecUtil.int32Get(buffer, index, encoding.byteOrder()));
                break;

            case INT64:
                sb.append(CodecUtil.int64Get(buffer, index, encoding.byteOrder()));
                break;

            case UINT8:
                sb.append(CodecUtil.uint8Get(buffer, index));
                break;

            case UINT16:
                sb.append(CodecUtil.uint16Get(buffer, index, encoding.byteOrder()));
                break;

            case UINT32:
                sb.append(CodecUtil.uint32Get(buffer, index, encoding.byteOrder()));
                break;

            case UINT64:
                sb.append(CodecUtil.uint64Get(buffer, index, encoding.byteOrder()));
                break;

            case FLOAT:
                sb.append(CodecUtil.floatGet(buffer, index, encoding.byteOrder()));
                break;

            case DOUBLE:
                sb.append(CodecUtil.doubleGet(buffer, index, encoding.byteOrder()));
                break;
        }
    }

    private static long getLong(final DirectBuffer buffer, final int index, final Encoding encoding)
    {
        switch (encoding.primitiveType())
        {
            case CHAR:
                return CodecUtil.charGet(buffer, index);

            case INT8:
                return CodecUtil.int8Get(buffer, index);

            case INT16:
                return CodecUtil.int16Get(buffer, index, encoding.byteOrder());

            case INT32:
                return CodecUtil.int32Get(buffer, index, encoding.byteOrder());

            case INT64:
                return CodecUtil.int64Get(buffer, index, encoding.byteOrder());

            case UINT8:
                return CodecUtil.uint8Get(buffer, index);

            case UINT16:
                return CodecUtil.uint16Get(buffer, index, encoding.byteOrder());

            case UINT32:
                return CodecUtil.uint32Get(buffer, index, encoding.byteOrder());

            case UINT64:
                return CodecUtil.uint64Get(buffer, index, encoding.byteOrder());

            default:
                throw new IllegalArgumentException("Unsupported type for long: " + encoding.primitiveType());
        }
    }

    private void printScope()
    {
        final Iterator<String> i = namedScope.descendingIterator();
        while (i.hasNext())
        {
            out.print(i.next());
        }
    }
}
