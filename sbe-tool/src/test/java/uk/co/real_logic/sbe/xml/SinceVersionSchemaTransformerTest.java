/*
 * Copyright 2013-2022 Real Logic Limited.
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
package uk.co.real_logic.sbe.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.real_logic.sbe.Tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class SinceVersionSchemaTransformerTest
{
    @ParameterizedTest
    @ValueSource(ints = { 0, 4, 5 })
    void shouldFilterAllVersionedFields(final int filteringVersion) throws Exception
    {
        final MessageSchema schema = parse(
            Tests.getLocalResource("since-version-filter-schema.xml"), ParserOptions.DEFAULT);

        final SinceVersionSchemaTransformer sinceVersionSchemaTransformer = new SinceVersionSchemaTransformer(
            filteringVersion);
        final MessageSchema transformedSchema = sinceVersionSchemaTransformer.transform(schema);

        assertEquals(filteringVersion, transformedSchema.version());
        assertTypeSinceVersionLessOrEqualTo(filteringVersion, schema, transformedSchema);
        assertMessageSinceVersionLessOrEqualTo(filteringVersion, schema, transformedSchema);
    }

    private static void assertMessageSinceVersionLessOrEqualTo(
        final int filteringVersion,
        final MessageSchema originalSchema,
        final MessageSchema transformedSchema)
    {
        final ArrayList<Message> transformedMessagesCopy = new ArrayList<>(transformedSchema.messages());

        final Collection<Message> types = originalSchema.messages();
        for (Message message : types)
        {
            if (message.sinceVersion() <= filteringVersion)
            {
                assertNotNull(
                    findAndRemove(transformedMessagesCopy, message),
                    "Message (" + message.name() + ") should be retained");
            }
            else
            {
                assertNull(
                    findAndRemove(transformedMessagesCopy, message),
                    "Message (" + message.name() + ") should be removed");
            }
        }

        assertTrue(transformedMessagesCopy.isEmpty(), "Messages should have been removed: " + transformedMessagesCopy);
    }

    private static void assertTypeSinceVersionLessOrEqualTo(
        final int filteringVersion,
        final MessageSchema originalSchema,
        final MessageSchema transformedSchema)
    {
        final ArrayList<Type> transformedTypesCopy = new ArrayList<>(transformedSchema.types());

        final Collection<Type> types = originalSchema.types();
        for (Type type : types)
        {
            if (type.sinceVersion() <= filteringVersion)
            {
                assertNotNull(
                    findAndRemove(transformedTypesCopy, type),
                    "Type (" + type.name() + ") should be retained");
            }
            else
            {
                assertNull(
                    findAndRemove(transformedTypesCopy, type),
                    "Type (" + type.name() + ") should be removed");
            }
        }

        assertTrue(transformedTypesCopy.isEmpty(), "Types should have been removed: " + transformedTypesCopy);
    }

    private static Type findAndRemove(final ArrayList<Type> transformedTypesCopy, final Type type)
    {
        Type result = null;
        for (final Iterator<Type> it = transformedTypesCopy.iterator(); it.hasNext();)
        {
            final Type transformedType = it.next();
            if (type.name().equals(transformedType.name()))
            {
                result = transformedType;
                it.remove();
            }
        }

        return result;
    }

    private static Message findAndRemove(final ArrayList<Message> transformedMessagesCopy, final Message message)
    {
        Message result = null;
        for (final Iterator<Message> it = transformedMessagesCopy.iterator(); it.hasNext();)
        {
            final Message transformedMessage = it.next();
            if (message.id() == transformedMessage.id())
            {
                result = transformedMessage;
                it.remove();
            }
        }

        return result;
    }
}
