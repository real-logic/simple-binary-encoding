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
package uk.co.real_logic.sbe.generation.cpp;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class CppGeneratorTest
{
    private final StringWriterOutputManager outputManager = new StringWriterOutputManager();
    private Ir ir;

    @BeforeEach
    public void setUp() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(Tests.getLocalResource("code-generation-schema.xml"), options);
        final IrGenerator irg = new IrGenerator();
        ir = irg.generate(schema);

        outputManager.clear();
        outputManager.setPackageName(ir.applicableNamespace());
    }

    @Test
    public void shouldUseGeneratedLiteralForConstantOneWhenGeneratingBitsetCode() throws Exception
    {
        final CppGenerator generator = new CppGenerator(ir, outputManager);
        generator.generate();

        final String source = outputManager.getSource("code.generation.test.OptionalExtras").toString();
        assertFalse(source.contains("1u << "));
        assertTrue(source.contains("static_cast<std::uint8_t>(1) << "));
    }
}
