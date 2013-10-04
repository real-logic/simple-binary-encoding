/*
 * Copyright 2013 Real Logic Ltd.
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

import org.junit.Before;
import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private final OutputManager outputManager = mock(OutputManager.class);
    private IntermediateRepresentation ir;

    @Before
    public void setUp() throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();

        ir = irg.generate(schema);
    }

    @Test
    public void shouldGenerateMessageHeaderStub() throws Exception
    {
        final StringWriter stringWriter = new StringWriter();
        when(outputManager.createOutput(JavaGenerator.MESSAGE_HEADER_VISITOR)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, outputManager);

        javaGenerator.generateMessageHeaderStub();

        System.out.println("stringWriter = " + stringWriter);
    }
}
