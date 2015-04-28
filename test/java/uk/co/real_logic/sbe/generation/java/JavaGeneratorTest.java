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
import uk.co.real_logic.agrona.DirectBuffer;
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
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.MESSAGE_HEADER_TYPE;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private static final Class<?> BUFFER_CLASS = MutableDirectBuffer.class;
    private static final String MUTABLE_BUFFER_NAME = BUFFER_CLASS.getName();
    private static final Class<DirectBuffer> READ_ONLY_BUFFER_CLASS = DirectBuffer.class;
    private static final String READ_ONLY_BUFFER_NAME = READ_ONLY_BUFFER_CLASS.getName();
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
        final short templateId = (short) 7;
        final int actingVersion = 0;
        final int blockLength = 32;
        final String fqClassName = ir.applicableNamespace() + "." + MESSAGE_HEADER_TYPE;

        when(mockBuffer.getShort(bufferOffset + templateIdOffset, BYTE_ORDER)).thenReturn(templateId);

        generator().generateMessageHeaderStub();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_CLASS, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final Integer result = (Integer) clazz.getDeclaredMethod("templateId").invoke(flyweight);
        assertThat(result, is((int) templateId));

        clazz.getDeclaredMethod("blockLength", int.class).invoke(flyweight, blockLength);

        verify(mockBuffer).putShort(bufferOffset, (short) blockLength, BYTE_ORDER);
    }

    @Test
    public void shouldGenerateUint8EnumStub() throws Exception
    {
        final String className = "BooleanType";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("get", short.class).invoke(null, (short) 1);

        assertThat(result.toString(), is("TRUE"));
    }

    @Test
    public void shouldGenerateCharEnumStub() throws Exception
    {
        final String className = "Model";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("get", byte.class).invoke(null, (byte) 'B');

        assertThat(result.toString(), is("B"));
    }

    @Test
    public void shouldGenerateChoiceSetStub() throws Exception
    {
        final int bufferOffset = 8;
        final int actingVersion = 0;
        final byte bitset = (byte) 0b0000_0100;
        final String className = "OptionalExtras";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getByte(bufferOffset)).thenReturn(bitset);

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_CLASS, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final Object result = clazz.getDeclaredMethod("cruiseControl").invoke(flyweight);

        assertThat((Boolean) result, is(Boolean.TRUE));
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
            .thenReturn((short) expectedEngineCapacity);

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_CLASS, int.class, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset, actingVersion);

        final int capacityResult = (Integer) clazz.getDeclaredMethod("capacity").invoke(flyweight);
        assertThat(capacityResult, is(expectedEngineCapacity));

        final int maxRpmResult = (Integer) clazz.getDeclaredMethod("maxRpm").invoke(flyweight);
        assertThat(maxRpmResult, is(expectedMaxRpm));

        final short numCylinders = (short) 4;
        clazz.getDeclaredMethod("numCylinders", short.class).invoke(flyweight, numCylinders);

        clazz.getDeclaredMethod("putManufacturerCode", byte[].class, int.class)
            .invoke(flyweight, manufacturerCode, 0);

        verify(mockBuffer).putByte(numCylindersOffset, (byte) numCylinders);
        verify(mockBuffer).putBytes(manufacturerCodeOffset, manufacturerCode, 0, manufacturerCode.length);
    }

    @Test
    public void shouldGenerateBasicMessage() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();
        final Class<?> clazz = compileCar();

        final Object msgFlyweight = clazz.newInstance();
        wrapForEncode(buffer, msgFlyweight);

        final Integer initialPosition = limit(msgFlyweight);

        final Object groupFlyweight = clazz.getDeclaredMethod("fuelFigures").invoke(msgFlyweight);
        assertThat(limit(msgFlyweight), greaterThan(initialPosition));

        final Integer count = (Integer) groupFlyweight.getClass().getDeclaredMethod("count").invoke(groupFlyweight);
        assertThat(count, is(0));
    }

    @Test
    public void shouldGenerateReadOnlyMessage() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Class<?> readerClass = compileReadOnlyCar();
        final Class<?> writerClass = compileCar();

        final Object writer = writerClass.newInstance();
        wrapForEncode(buffer, writer);

        final Object reader = readerClass.newInstance();
        wrapForDecode(buffer, readerClass, reader);

        final long expectedSerialNumber = 5L;
        serialNumber(writerClass, writer, expectedSerialNumber);
        final long serialNumber = serialNumber(readerClass, reader);
        assertEquals(expectedSerialNumber, serialNumber);
    }

    private void wrapForDecode(final UnsafeBuffer buffer,
                               final Class<?> readerClass,
                               final Object reader) throws Exception
    {
        readerClass.getMethod("wrapForDecode", READ_ONLY_BUFFER_CLASS, int.class, int.class, int.class)
                   .invoke(reader, buffer, 0, 0, 0);
    }

    private Class<?> compileReadOnlyCar() throws Exception
    {
        final String className = "ReadOnlyCar";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> readerClass = compile(fqClassName);
        assertNotNull(readerClass);
        return readerClass;
    }

    private void serialNumber(final Class<?> writerClass,
                              final Object writer,
                              final long serial) throws Exception
    {
        writerClass.getMethod("serialNumber", long.class).invoke(writer, serial);
    }

    private long serialNumber(final Class<?> readerClass,
                              final Object reader) throws Exception
    {
        return (long) readerClass.getMethod("serialNumber").invoke(reader);
    }

    private Integer limit(final Object msgFlyweight) throws Exception
    {
        return (Integer) msgFlyweight.getClass().getMethod("limit").invoke(msgFlyweight);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateMissingMutableBufferClass() throws IOException
    {
        new JavaGenerator(ir, "dasdsads", MUTABLE_BUFFER_NAME, outputManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateNotImplementedMutableBufferClass() throws IOException
    {
        new JavaGenerator(ir, "java.nio.ByteBuffer", MUTABLE_BUFFER_NAME, outputManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateMissingReadOnlyBufferClass() throws IOException
    {
        new JavaGenerator(ir, MUTABLE_BUFFER_NAME, "dasdsads", outputManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateNotImplementedReadOnlyBufferClass() throws IOException
    {
        new JavaGenerator(ir, MUTABLE_BUFFER_NAME, "java.nio.ByteBuffer", outputManager);
    }

    private Class<?> compileCar() throws Exception
    {
        final String className = "Car";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);
        return clazz;
    }

    private void wrapForEncode(final UnsafeBuffer buffer,
                               final Object msgFlyweight) throws Exception
    {
        msgFlyweight.getClass()
            .getDeclaredMethod("wrapForEncode", BUFFER_CLASS, int.class)
            .invoke(msgFlyweight, buffer, 0);
    }

    private JavaGenerator generator() throws IOException
    {
        return new JavaGenerator(ir, MUTABLE_BUFFER_NAME, READ_ONLY_BUFFER_NAME, outputManager);
    }

    private void generateTypeStubs() throws IOException
    {
        final JavaGenerator javaGenerator = generator();
        javaGenerator.generateTypeStubs();
    }

    private Class<?> compile(final String fqClassName) throws Exception
    {
        final Map<String, CharSequence> sources = outputManager.getSources();
        System.out.println(sources);
        return CompilerUtil.compileInMemory(fqClassName, sources);
    }

}
