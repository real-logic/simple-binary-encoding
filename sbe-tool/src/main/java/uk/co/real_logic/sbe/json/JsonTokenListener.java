/*
 * Copyright 2013-2017 Real Logic Ltd.
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

import org.agrona.DirectBuffer;
import uk.co.real_logic.sbe.PrimitiveValue;
import uk.co.real_logic.sbe.ir.Encoding;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.otf.TokenListener;
import uk.co.real_logic.sbe.otf.Types;

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

    public void onBeginMessage(final Token token)
    {
        startObject();
    }

    public void onEndMessage(final Token token)
    {
        endObject();
    }

    public void onEncoding(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final Token typeToken,
        final int actingVersion)
    {
        property(fieldToken);
        appendEncodingAsString(buffer, bufferIndex, typeToken, actingVersion);
        next();
    }

    public void onEnum(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final int actingVersion)
    {
    }

    public void onBitSet(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final List<Token> tokens,
        final int fromIndex,
        final int toIndex,
        final int actingVersion)
    {
    }

    public void onBeginComposite(
        final Token fieldToken, final List<Token> tokens, final int fromIndex, final int toIndex)
    {
    }

    public void onEndComposite(
        final Token fieldToken, final List<Token> tokens, final int fromIndex, final int toIndex)
    {
    }

    public void onGroupHeader(final Token token, final int numInGroup)
    {
        property(token);
        output.append("[\n");
    }

    public void onBeginGroup(final Token token, final int groupIndex, final int numInGroup)
    {
        startObject();
    }

    public void onEndGroup(final Token token, final int groupIndex, final int numInGroup)
    {
        endObject();
        if (isLastGroup(groupIndex, numInGroup))
        {
            backup();
            output.append("],\n");
        }
    }

    public void onVarData(
        final Token fieldToken,
        final DirectBuffer buffer,
        final int bufferIndex,
        final int length,
        final Token typeToken)
    {
        try
        {
            property(fieldToken);
            doubleQuote();

            buffer.getBytes(bufferIndex, tempBuffer, 0, length);
            output.append(new String(tempBuffer, 0, length, typeToken.encoding().characterEncoding()));

            doubleQuote();
            next();
        }
        catch (final UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
    }

    private static boolean isLastGroup(final int groupIndex, final int numInGroup)
    {
        return groupIndex == numInGroup - 1;
    }

    private void next()
    {
        output.append(",\n");
    }

    private void property(final Token token)
    {
        indent();
        doubleQuote();
        output.append(token.name());
        output.append("\": ");
    }

    private void appendEncodingAsString(
        final DirectBuffer buffer, final int index, final Token typeToken, final int actingVersion)
    {
        final PrimitiveValue constOrNotPresentValue = constOrNotPresentValue(typeToken, actingVersion);
        if (null != constOrNotPresentValue)
        {
            output.append(constOrNotPresentValue.toString());
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
                output.append((char)buffer.getByte(index + (i * elementSize)));
            }

            doubleQuote();
        }
        else
        {
            if (size > 1)
            {
                output.append('[');
            }

            for (int i = 0; i < size; i++)
            {
                Types.appendAsString(output, buffer, index + (i * elementSize), encoding);
                output.append(", ");
            }

            backup();
            if (size > 1)
            {
                output.append(']');
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
        else if (token.isOptionalEncoding() && actingVersion < token.version())
        {
            return encoding.applicableNullValue();
        }

        return null;
    }

    private void indent()
    {
        for (int i = 0; i < indentation; i++)
        {
            output.append("    ");
        }
    }

    private void doubleQuote()
    {
        output.append('\"');
    }

    private void startObject()
    {
        indent();
        output.append("{\n");
        indentation++;
    }

    private void endObject()
    {
        backup();
        output.append('\n');
        indentation--;
        indent();
        output.append('}');

        if (indentation > 0)
        {
            next();
        }
    }
}
