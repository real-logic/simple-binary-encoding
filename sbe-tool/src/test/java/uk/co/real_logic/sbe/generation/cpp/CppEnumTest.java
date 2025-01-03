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
package uk.co.real_logic.sbe.generation.cpp;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

import static org.junit.jupiter.api.Assertions.fail;

class CppEnumTest
{
    private StringWriterOutputManager outputManager;
    private CppGenerator generator;

    private void setupGenerator(final InputStream in) throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(in, options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        generator = new CppGenerator(ir, false, outputManager);
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    void shouldFailOnKeywordEnumValues() throws Exception
    {
        System.clearProperty(SbeTool.KEYWORD_APPEND_TOKEN);

        try (InputStream in = Tests.getLocalResource("issue1007.xml"))
        {
            setupGenerator(in);

            try
            {
                generator.generate();
            }
            catch (final IllegalStateException exception)
            {
                assertEquals(
                    "Invalid property name='false' please correct the schema or consider setting system property: sbe.keyword.append.token",
                    exception.getMessage());
                return;
            }

            fail("expected IllegalStateException");
            final String source = outputManager.getSources().toString();

            System.err.println(source);
        }
    }

    @Test
    void shouldAddSuffixToEnumValues() throws Exception
    {
        System.setProperty(SbeTool.KEYWORD_APPEND_TOKEN, "_");

        try (InputStream in = Tests.getLocalResource("issue1007.xml"))
        {
            setupGenerator(in);

            generator.generate();
            final String sources = outputManager.getSources().toString();

            assertThat(sources, containsString("false_ = static"));
            assertThat(sources, containsString("true_ = static"));
            assertThat(sources, containsString("return false_;"));
            assertThat(sources, containsString("return true_;"));
            assertThat(sources, containsString("case false_:"));
            assertThat(sources, containsString("case true_:"));
        }
    }
}
