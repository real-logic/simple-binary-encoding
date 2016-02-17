/*
 * Copyright 2016 Real Logic Ltd.
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
package uk.co.real_logic.sbe.generation.java;

import org.junit.Test;
import uk.co.real_logic.agrona.DirectBuffer;
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.generation.CompilerUtil;
import uk.co.real_logic.agrona.generation.StringWriterOutputManager;
import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class GenerateFixBinaryTest
{
    private static final Class<?> BUFFER_CLASS = MutableDirectBuffer.class;
    private static final String BUFFER_NAME = BUFFER_CLASS.getName();
    private static final Class<DirectBuffer> READ_ONLY_BUFFER_CLASS = DirectBuffer.class;
    private static final String READ_ONLY_BUFFER_NAME = READ_ONLY_BUFFER_CLASS.getName();

    private final StringWriterOutputManager outputManager = new StringWriterOutputManager();

    @Test
    public void shouldGenerateValidJava() throws Exception
    {
        System.setProperty(SbeTool.KEYWORD_APPEND_TOKEN, "_");

        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(TestUtil.getLocalResource("FixBinary.xml"), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);
        final JavaGenerator generator = new JavaGenerator(ir, BUFFER_NAME, READ_ONLY_BUFFER_NAME, false, outputManager);

        outputManager.setPackageName(ir.applicableNamespace());
        generator.generateMessageHeaderStub();
        generator.generateTypeStubs();
        generator.generate();

        final Map<String, CharSequence> sources = outputManager.getSources();
        final String className = "MDIncrementalRefreshTradeSummary42Decoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> aClass = CompilerUtil.compileInMemory(fqClassName, sources);

        assertNotNull(aClass);
    }
}
