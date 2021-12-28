/*
 * Copyright 2013-2021 Real Logic Limited.
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.xml.sax.InputSource;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_17)
public class Issue567GroupSizeTest
{
    public static final String ERR_MSG =
        "WARNING: at <sbe:message name=\"issue567\"> <group name=\"group\"> \"numInGroup\" should be UINT8 or UINT16";

    private final PrintStream mockErr = mock(PrintStream.class);
    private PrintStream err;

    @BeforeEach
    public void before()
    {
        err = System.err;
        System.setErr(mockErr);
    }

    @AfterEach
    public void after()
    {
        System.setErr(err);
        verify(mockErr).println(ERR_MSG);
    }

    @Test
    public void shouldThrowWhenUsingATypeThatIsNotConstrainedToFitInAnIntAsTheGroupSize() throws IOException
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final InputStream in = Tests.getLocalResource("issue567-invalid.xml");
        final InputSource is = new InputSource(new InputStreamReader(in, StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () -> parse(is, options));
    }

    @Test
    public void shouldGenerateWhenUsingATypeThatIsConstrainedToFitInAnIntAsTheGroupSize() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final InputStream in = Tests.getLocalResource("issue567-valid.xml");
        final InputSource is = new InputSource(new InputStreamReader(in, StandardCharsets.UTF_8));

        final MessageSchema schema = parse(is, options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        final StringWriterOutputManager outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        final CSharpGenerator generator = new CSharpGenerator(ir, outputManager);

        // Act + Assert (no exception)
        generator.generate();
    }
}
