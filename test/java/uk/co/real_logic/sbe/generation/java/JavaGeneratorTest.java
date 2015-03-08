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
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.agrona.generation.CompilerUtil;
import uk.co.real_logic.agrona.generation.StringWriterOutputManager;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.MESSAGE_HEADER_TYPE;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private static final Class<?> BUFFER_IMPLEMENTATION = MutableDirectBuffer.class;
    private static final String BUFFER_IMPLEMENTATION_NAME = BUFFER_IMPLEMENTATION.getName();
    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private final StringWriterOutputManager outputManager = new StringWriterOutputManager();
    private final MutableDirectBuffer mockBuffer = mock(MutableDirectBuffer.class);

    private Ir ir;

    @Before
    public void setUp() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(TestUtil.getLocalResource("code-generation-schema.xml"), options);
        final IrGenerator irg = new IrGenerator();
        ir = irg.generate(schema);

        outputManager.clear();
        outputManager.setPackageName(ir.applicableNamespace());
    }

    @Test
    public void shouldGenerateMessageHeaderStub() throws Exception
    {
        final int bufferOffset = 64;
        final int templateIdOffset = 2;
        final short templateId = (short)7;
        final int actingVersion = 0;
        final int blockLength = 32;
        final String fqClassName = ir.applicableNamespace() + "." + MESSAGE_HEADER_TYPE;

        when(mockBuffer.getShort(bufferOffset + templateIdOffset, BYTE_ORDER)).thenReturn(templateId);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generateMessageHeaderStub();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_IMPLEMENTATION, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final Integer result = (Integer)clazz.getDeclaredMethod("templateId").invoke(flyweight);
        assertThat(result, is((int)templateId));

        clazz.getDeclaredMethod("blockLength", int.class).invoke(flyweight, blockLength);

        verify(mockBuffer).putShort(bufferOffset, (short)blockLength, BYTE_ORDER);
    }

    @Test
    public void shouldGenerateUint8EnumStub() throws Exception
    {
        final String className = "BooleanType";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("get", short.class).invoke(null, (short)1);

        assertThat(result.toString(), is("TRUE"));
    }

    @Test
    public void shouldGenerateCharEnumStub() throws Exception
    {
        final String className = "Model";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("get", byte.class).invoke(null, (byte)'B');

        assertThat(result.toString(), is("B"));
    }

    @Test
    public void shouldGenerateChoiceSetStub() throws Exception
    {
        final int bufferOffset = 8;
        final int actingVersion = 0;
        final byte bitset = (byte)0b0000_0100;
        final String className = "OptionalExtras";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getByte(bufferOffset)).thenReturn(bitset);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_IMPLEMENTATION, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final Object result = clazz.getDeclaredMethod("cruiseControl").invoke(flyweight);

        assertThat((Boolean)result, is(Boolean.TRUE));
    }

    @Test
    public void shouldGenerateCompositeStub() throws Exception
    {
        final int actingVersion = 0;
        final int bufferOffset = 64;
        final int capacityFieldOffset = bufferOffset;
        final int numCylindersOffset = bufferOffset + 2;
        final int expectedEngineCapacity = 2000;
        final int expectedMaxRpm = 9000;
        final int manufacturerCodeOffset = bufferOffset + 3;
        final byte[] manufacturerCode = {'A', 'B', 'C'};
        final String className = "Engine";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getShort(capacityFieldOffset, BYTE_ORDER))
            .thenReturn((short)expectedEngineCapacity);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_IMPLEMENTATION, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final int capacityResult = (Integer)clazz.getDeclaredMethod("capacity").invoke(flyweight);
        assertThat(capacityResult, is(expectedEngineCapacity));

        final int maxRpmResult = (Integer)clazz.getDeclaredMethod("maxRpm").invoke(flyweight);
        assertThat(maxRpmResult, is(expectedMaxRpm));

        final short numCylinders = (short)4;
        clazz.getDeclaredMethod("numCylinders", short.class).invoke(flyweight, numCylinders);

        clazz.getDeclaredMethod("putManufacturerCode", byte[].class, int.class)
            .invoke(flyweight, manufacturerCode, 0);

        verify(mockBuffer).putByte(numCylindersOffset, (byte)numCylinders);
        verify(mockBuffer).putBytes(manufacturerCodeOffset, manufacturerCode, 0, manufacturerCode.length);
    }

    @Test
    public void shouldGenerateBasicMessage() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        final String className = "Car";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final JavaGenerator javaGenerator = new JavaGenerator(ir, BUFFER_IMPLEMENTATION_NAME, outputManager);
        javaGenerator.generate();

        final Class<?> clazz = CompilerUtil.compileInMemory(fqClassName, outputManager.getSources());
        assertNotNull(clazz);

        final Object msgFlyweight = clazz.newInstance();
        msgFlyweight.getClass()
            .getDeclaredMethod("wrapForEncode", BUFFER_IMPLEMENTATION, int.class)
            .invoke(msgFlyweight, buffer, 0);

        final Integer initialPosition = (Integer)msgFlyweight.getClass().getDeclaredMethod("limit").invoke(msgFlyweight);

        final Object groupFlyweight = clazz.getDeclaredMethod("fuelFigures").invoke(msgFlyweight);
        assertThat(
            (Integer)msgFlyweight.getClass().getDeclaredMethod("limit").invoke(msgFlyweight), greaterThan(initialPosition));

        final Integer count = (Integer)groupFlyweight.getClass().getDeclaredMethod("count").invoke(groupFlyweight);
        assertThat(count, is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateMissingBufferClass() throws IOException
    {
        new JavaGenerator(ir, "dasdsads", outputManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateNotImplementedBufferClass() throws IOException
    {
        new JavaGenerator(ir, "java.nio.ByteBuffer", outputManager);
    }
}
