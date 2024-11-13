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
package uk.co.real_logic.sbe.generation.csharp;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

class CSharpGeneratorTest
{
    @Test
    void dtosShouldReferenceTypesInDifferentPackages() throws Exception
    {
        try (InputStream in = Tests.getLocalResource("explicit-package-test-schema.xml"))
        {
            final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
            final MessageSchema schema = parse(in, options);
            final IrGenerator irg = new IrGenerator();
            final Ir ir = irg.generate(schema);
            final StringWriterOutputManager outputManager = new StringWriterOutputManager();
            outputManager.setPackageName(ir.applicableNamespace());

            final CSharpGenerator generator = new CSharpGenerator(ir, outputManager);
            generator.generate();

            final CSharpDtoGenerator dtoGenerator = new CSharpDtoGenerator(ir, outputManager);
            dtoGenerator.generate();

            final java.util.Map<String, CharSequence> sources = outputManager.getSources();
            /*
            sources.forEach((s, cs) ->
            {
                System.out.println("----------------");
                System.out.println("FILE :: " + s);
                System.out.println("----------------");
                System.out.println(cs);
                System.out.println("----------------");
            });
             */

            assertNotNull(sources.get("test.message.schema.TestMessageDto"));
            assertNotNull(sources.get("test.message.schema.MessageHeaderDto"));
            //assertNotNull(sources.get("test.message.schema.common.CarDto"));
            //assertNotNull(sources.get("test.message.schema.common.EngineDto"));
            assertNotNull(sources.get("test.message.schema.CarDto"));
            assertNotNull(sources.get("test.message.schema.EngineDto"));

            /*
            assertNotNull(sources.get("outside.schema.FuelSpecDto"));
            assertNotNull(sources.get("outside.schema.DaysDto"));
            assertNotNull(sources.get("outside.schema.FuelTypeDto"));

            assertNotNull(compile("test.message.schema.TestMessageDto"));
            assertNotNull(compile("test.message.schema.common.CarDto"));
            assertNotNull(compile("outside.schema.FuelSpecDto"));
             */
        }
    }
}
