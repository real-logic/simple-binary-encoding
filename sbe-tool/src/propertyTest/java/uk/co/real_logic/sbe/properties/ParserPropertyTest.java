/*
 * Copyright 2013-2025 Real Logic Limited.
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
package uk.co.real_logic.sbe.properties;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import uk.co.real_logic.sbe.properties.arbitraries.SbeArbitraries;
import uk.co.real_logic.sbe.properties.schema.MessageSchema;
import uk.co.real_logic.sbe.properties.schema.TestXmlSchemaWriter;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

class ParserPropertyTest
{
    @Property
    void shouldParseAnyValidSchema(@ForAll("schemas") final MessageSchema schema) throws Exception
    {
        final String xml = TestXmlSchemaWriter.writeString(schema);
        try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
        {
            parse(in, ParserOptions.DEFAULT);
        }
    }

    @Provide
    Arbitrary<MessageSchema> schemas()
    {
        return SbeArbitraries.messageSchema();
    }
}
