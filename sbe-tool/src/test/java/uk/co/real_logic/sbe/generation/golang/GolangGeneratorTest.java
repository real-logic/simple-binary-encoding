/*
 * Copyright 2013-2024 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation.golang;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

class GolangGeneratorTest
{
    @Test
    void shouldUseUpperCaseTypeNamesWhenReferencingEnumValues() throws Exception
    {
        try (InputStream in = Tests.getLocalResource("value-ref-with-lower-case-enum.xml"))
        {
            final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
            final MessageSchema schema = parse(in, options);
            final IrGenerator irg = new IrGenerator();
            final Ir ir = irg.generate(schema);
            final StringWriterOutputManager outputManager = new StringWriterOutputManager();
            outputManager.setPackageName(ir.applicableNamespace());

            final GolangGenerator generator = new GolangGenerator(ir, outputManager);
            generator.generate();

            final String engineTypeSource = outputManager.getSource("issue505.EngineType").toString();
            assertThat(engineTypeSource, allOf(
                containsString("type EngineTypeEnum uint8"),
                containsString("type EngineTypeValues struct {\n" +
                "\tGas       EngineTypeEnum\n" +
                "\tNullValue EngineTypeEnum\n}")));
            final String messageSource = outputManager.getSource("issue505.SomeMessage").toString();
            assertThat(messageSource, containsString("type SomeMessage struct {\n\tEngineType EngineTypeEnum\n}"));
        }
    }

    @Test
    void shouldUseUpperCaseTypeNamesWhenReferencingBitSet() throws Exception
    {
        try (InputStream in = Tests.getLocalResource("message-with-lower-case-bitset.xml"))
        {
            final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
            final MessageSchema schema = parse(in, options);
            final IrGenerator irg = new IrGenerator();
            final Ir ir = irg.generate(schema);
            final StringWriterOutputManager outputManager = new StringWriterOutputManager();
            outputManager.setPackageName(ir.applicableNamespace());

            final GolangGenerator generator = new GolangGenerator(ir, outputManager);
            generator.generate();

            final String eventTypeSource = outputManager.getSource("test.EventType").toString();
            assertThat(eventTypeSource,
                containsString("type EventType [8]bool\n" +
                "type EventTypeChoiceValue uint8\n" +
                "type EventTypeChoiceValues struct {\n" +
                "\tA     EventTypeChoiceValue\n" +
                "\tBb    EventTypeChoiceValue\n" +
                "\tCcc   EventTypeChoiceValue\n" +
                "\tD     EventTypeChoiceValue\n" +
                "\tEeeee EventTypeChoiceValue\n}\n\n" +
                "var EventTypeChoice = EventTypeChoiceValues{0, 1, 2, 3, 4}"));
            final String messageSource = outputManager.getSource("test.SomeMessage").toString();
            assertThat(messageSource, containsString("type SomeMessage struct {\n\tMyEvent EventType\n}"));
        }
    }
}
