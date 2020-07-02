/*
 * Copyright 2013-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.json;

import org.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Token;
import uk.co.real_logic.sbe.otf.OtfHeaderDecoder;
import uk.co.real_logic.sbe.otf.OtfMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Pretty Print JSON based upon the given Ir.
 */
public class JsonPrinter
{
    private final OtfHeaderDecoder headerDecoder;
    private final Ir ir;

    public JsonPrinter(final Ir ir)
    {
        this.ir = ir;
        headerDecoder = new OtfHeaderDecoder(ir.headerStructure());
    }

    public void print(final ByteBuffer encodedMessage, final StringBuilder output)
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(encodedMessage);
        print(output, buffer, 0);
    }

    public void print(final StringBuilder output, final UnsafeBuffer buffer, final int bufferOffset)
    {
        final int blockLength = headerDecoder.getBlockLength(buffer, bufferOffset);
        final int templateId = headerDecoder.getTemplateId(buffer, bufferOffset);
        final int schemaId = headerDecoder.getSchemaId(buffer, bufferOffset);
        final int actingVersion = headerDecoder.getSchemaVersion(buffer, bufferOffset);

        validateId(schemaId);

        final int messageOffset = bufferOffset + headerDecoder.encodedLength();
        final List<Token> msgTokens = ir.getMessage(templateId);

        OtfMessageDecoder.decode(
            buffer,
            messageOffset,
            actingVersion,
            blockLength,
            msgTokens,
            new JsonTokenListener(output));
    }

    private void validateId(final int schemaId)
    {
        if (schemaId != ir.id())
        {
            throw new IllegalArgumentException("Required schema id " + ir.id() + " but was " + schemaId);
        }
    }

    public String print(final ByteBuffer encodedMessage)
    {
        final StringBuilder sb = new StringBuilder();
        print(encodedMessage, sb);

        return sb.toString();
    }
}
