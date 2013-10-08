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
import uk.co.real_logic.sbe.generation.java.util.CompilerUtil;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.MESSAGE_HEADER_VISITOR;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private final StringWriter stringWriter = new StringWriter();
    private final StringWriter blackHole = new StringWriter();
    private final OutputManager mockOutputManager = mock(OutputManager.class);
    private final DirectBuffer mockBuffer = mock(DirectBuffer.class);

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
        final int bufferOffset = 64;
        final int templateIdOffset = 2;
        final Short templateId = Short.valueOf((short)7);
        final Integer blockLength = Integer.valueOf(32);
        final String fqClassName = ir.getPackageName() + "." + MESSAGE_HEADER_VISITOR;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(MESSAGE_HEADER_VISITOR)).thenReturn(stringWriter);
        when(Short.valueOf(mockBuffer.getShort(bufferOffset + templateIdOffset))).thenReturn(templateId);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateMessageHeaderStub();

        final Class<?> clazz = CompilerUtil.compileCode(fqClassName, stringWriter.toString());
        assertNotNull(clazz);

        final DirectBufferFlyweight flyweight = (DirectBufferFlyweight)clazz.newInstance();
        flyweight.reset(mockBuffer, bufferOffset);

        final Integer result = (Integer)clazz.getDeclaredMethod("templateId").invoke(flyweight);
        assertThat(result, is(Integer.valueOf(templateId.intValue())));

        clazz.getDeclaredMethod("blockLength", int.class).invoke(flyweight, blockLength);

        verify(mockBuffer).putShort(bufferOffset, blockLength.shortValue());
    }

    @Test
    public void shouldGenerateUint8EnumStub() throws Exception
    {
        final String className = "Boolean";
        final String fqClassName = ir.getPackageName() + "." + className;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(className)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileCode(fqClassName, stringWriter.toString());
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("lookup", short.class).invoke(null, Short.valueOf((short)1));

        assertThat(result.toString(), is("TRUE"));
    }

    @Test
    public void shouldGenerateCharEnumStub() throws Exception
    {
        final String className = "ModelType";
        final String fqClassName = ir.getPackageName() + "." + className;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(className)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileCode(fqClassName, stringWriter.toString());
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("lookup", byte.class).invoke(null, Byte.valueOf((byte)'B'));

        assertThat(result.toString(), is("B"));
    }

    @Test
    public void shouldGenerateChoiceSetStub() throws Exception
    {
        final int bufferOffset = 8;
        final Byte bitset = Byte.valueOf((byte)0b0000_0100);
        final String className = "OptionalExtras";
        final String fqClassName = ir.getPackageName() + "." + className;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(className)).thenReturn(stringWriter);
        when(Byte.valueOf(mockBuffer.getByte(bufferOffset))).thenReturn(bitset);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileCode(fqClassName, stringWriter.toString());
        assertNotNull(clazz);

        final DirectBufferFlyweight flyweight = (DirectBufferFlyweight)clazz.newInstance();
        flyweight.reset(mockBuffer, bufferOffset);

        final Object result = clazz.getDeclaredMethod("cruiseControl").invoke(flyweight);

        assertThat((Boolean)result, is(Boolean.TRUE));
    }
}
