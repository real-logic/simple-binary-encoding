/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.sbe.json;

import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.codec.java.CodecUtil;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.otf.TokenListener;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static uk.co.real_logic.sbe.PrimitiveType.CHAR;

public class JsonTokenListener implements TokenListener
{

    private final byte[] tempBuffer = new byte[1024];
    private final StringBuilder output;
    private int indentation = 0;

    public JsonTokenListener(final StringBuilder output)
    {
        this.output = output;
    }

    public void onBeginMessage(Token token)
    {
        startObject();
    }

    public void onEndMessage(Token token)
    {
        endObject();
    }

    public void onEncoding(Token fieldToken, DirectBuffer buffer, int bufferIndex, Token typeToken, int actingVersion)
    {
        property(fieldToken);
        appendEncodingAsString(buffer, bufferIndex, typeToken, actingVersion);
        next();
    }

    public void onEnum(
        Token fieldToken,
        DirectBuffer buffer,
        int bufferIndex,
        List<Token> tokens,
        int fromIndex,
        int toIndex,
        int actingVersion)
    {

    }

    public void onBitSet(
        Token fieldToken,
        DirectBuffer buffer,
        int bufferIndex,
        List<Token> tokens,
        int fromIndex,
        int toIndex,
        int actingVersion)
    {

    }

    public void onBeginComposite(Token fieldToken, List<Token> tokens, int fromIndex, int toIndex)
    {

    }

    public void onEndComposite(Token fieldToken, List<Token> tokens, int fromIndex, int toIndex)
    {

    }

    public void onGroupHeader(Token token, int numInGroup)
    {
        property(token);
        append("[\n");
    }

    public void onBeginGroup(Token token, int groupIndex, int numInGroup)
    {
        startObject();
    }

    public void onEndGroup(Token token, int groupIndex, int numInGroup)
    {
        endObject();
        if (isLastGroup(groupIndex, numInGroup))
        {
            backup();
            append("],\n");
        }
    }

    public void onVarData(Token fieldToken, DirectBuffer buffer, int bufferIndex, int length, Token typeToken)
    {
        try
        {
            property(fieldToken);
            doubleQuote();

            buffer.getBytes(bufferIndex, tempBuffer, 0, length);
            append(new String(tempBuffer, 0, length, typeToken.encoding().characterEncoding()));

            doubleQuote();
            next();
        }
        catch (final UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            return;
        }
    }

    private boolean isLastGroup(int groupIndex, int numInGroup)
    {
        return groupIndex == numInGroup - 1;
    }

    private void next()
    {
        append(",\n");
    }

    private void property(Token token)
    {
        indent();
        doubleQuote();
        append(token.name());
        append("\": ");
    }

    private void appendEncodingAsString(
            final DirectBuffer buffer, final int index, final Token typeToken, final int actingVersion)
    {
        final PrimitiveValue constOrNotPresentValue = constOrNotPresentValue(typeToken, actingVersion);
        if (null != constOrNotPresentValue)
        {
            append(constOrNotPresentValue.toString());
            return;
        }

        final Encoding encoding = typeToken.encoding();
        final int elementSize = encoding.primitiveType().size();

        final int size = typeToken.arrayLength();
        if (size > 1 && encoding.primitiveType() == CHAR)
        {
            doubleQuote();
            for (int i = 0; i < size; i++)
            {
                output.append((char) CodecUtil.charGet(buffer, index + (i * elementSize)));
            }
            doubleQuote();
        }
        else
        {
            if (size > 1)
            {
                append("[");
            }

            for (int i = 0; i < size; i++)
            {
                mapEncodingToString(output, buffer, index + (i * elementSize), encoding);
                append(", ");
            }

            backup();
            if (size > 1)
            {
                append("]");
            }
        }
    }

    private void backup()
    {
        output.setLength(output.length() - 2);
    }

    private static PrimitiveValue constOrNotPresentValue(final Token token, final int actingVersion)
    {
        final Encoding encoding = token.encoding();
        if (token.isConstantEncoding())
        {
            return encoding.constValue();
        }
        else if (token.isOptionalEncoding())
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
                sb.append('"').append((char) CodecUtil.charGet(buffer, index)).append('"');
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

    private void indent()
    {
        for (int i = 0; i < indentation; i++)
        {
            append("    ");
        }
    }

    private void doubleQuote()
    {
        append("\"");
    }

    private void startObject()
    {
        indent();
        append("{\n");
        indentation++;
    }

    private void endObject()
    {
        backup();
        append("\n");
        indentation--;
        indent();
        append("}");
        if (indentation > 0)
        {
            next();
        }
    }

    private void append(final CharSequence value)
    {
        output.append(value);
    }

}
