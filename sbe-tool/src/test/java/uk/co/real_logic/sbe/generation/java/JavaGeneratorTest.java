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
package uk.co.real_logic.sbe.generation.java;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.generation.CompilerUtil;
import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.co.real_logic.sbe.generation.CodeGenerator.MESSAGE_HEADER_DECODER_TYPE;
import static uk.co.real_logic.sbe.generation.java.JavaGenerator.MESSAGE_HEADER_ENCODER_TYPE;
import static uk.co.real_logic.sbe.generation.java.ReflectionUtil.*;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private static final Class<?> BUFFER_CLASS = MutableDirectBuffer.class;
    private static final String BUFFER_NAME = BUFFER_CLASS.getName();
    private static final Class<DirectBuffer> READ_ONLY_BUFFER_CLASS = DirectBuffer.class;
    private static final String READ_ONLY_BUFFER_NAME = READ_ONLY_BUFFER_CLASS.getName();
    private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

    private final StringWriterOutputManager outputManager = new StringWriterOutputManager();
    private final MutableDirectBuffer mockBuffer = mock(MutableDirectBuffer.class);

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
    public void shouldGenerateMessageHeaderStub() throws Exception
    {
        final int bufferOffset = 64;
        final int templateIdOffset = 2;
        final short templateId = (short)7;
        final int blockLength = 32;
        final String fqClassName = ir.applicableNamespace() + "." + MESSAGE_HEADER_ENCODER_TYPE;

        when(mockBuffer.getShort(bufferOffset + templateIdOffset, BYTE_ORDER)).thenReturn(templateId);

        final JavaGenerator generator = generator();
        generator.generateTypeStubs();
        generator.generateMessageHeaderStub();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.getConstructor().newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", BUFFER_CLASS, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset);

        clazz.getDeclaredMethod("blockLength", int.class).invoke(flyweight, blockLength);

        verify(mockBuffer).putShort(bufferOffset, (short)blockLength, BYTE_ORDER);
    }

    @Test
    public void shouldGenerateMessageHeaderDecoderStub() throws Exception
    {
        final int bufferOffset = 64;
        final int templateIdOffset = 2;
        final short templateId = (short)7;
        final String fqClassName = ir.applicableNamespace() + "." + MESSAGE_HEADER_DECODER_TYPE;

        when(mockBuffer.getShort(bufferOffset + templateIdOffset, BYTE_ORDER)).thenReturn(templateId);

        final JavaGenerator generator = generator();
        generator.generateTypeStubs();
        generator.generateMessageHeaderStub();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.getConstructor().newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", READ_ONLY_BUFFER_CLASS, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset);

        final Integer result = (Integer)clazz.getDeclaredMethod("templateId").invoke(flyweight);
        assertThat(result, is((int)templateId));
    }

    @Test
    public void shouldGenerateUint8EnumStub() throws Exception
    {
        final String className = "BooleanType";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object result = clazz.getDeclaredMethod("get", short.class).invoke(null, (short)1);

        assertThat(result.toString(), is("T"));
    }

    @Test
    public void shouldGenerateCharEnumStub() throws Exception
    {
        generateTypeStubs();

        final Class<?> clazz = compileModel();

        final Object result = getByte(clazz, (byte)'B');

        assertThat(result, hasToString("B"));
    }

    @Test
    public void shouldGenerateChoiceSetStub() throws Exception
    {
        final int bufferOffset = 8;
        final byte bitset = (byte)0b0000_0100;
        final String className = "OptionalExtrasDecoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getByte(bufferOffset)).thenReturn(bitset);

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.getConstructor().newInstance();
        final Method method = flyweight.getClass().getDeclaredMethod("wrap", READ_ONLY_BUFFER_CLASS, int.class);
        method.invoke(flyweight, mockBuffer, bufferOffset);

        final Object result = get(flyweight, "cruiseControl");

        assertThat(result, is(Boolean.TRUE));
    }

    @Test
    public void shouldGenerateCompositeEncoder() throws Exception
    {
        final int bufferOffset = 64;
        final int capacityFieldOffset = bufferOffset;
        final int numCylindersOffset = bufferOffset + 2;
        final int expectedEngineCapacity = 2000;
        final int manufacturerCodeOffset = bufferOffset + 3;
        final byte[] manufacturerCode = { 'A', 'B', 'C' };
        final String className = "EngineEncoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getShort(capacityFieldOffset, BYTE_ORDER))
            .thenReturn((short)expectedEngineCapacity);

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.getConstructor().newInstance();
        wrap(bufferOffset, flyweight, mockBuffer, BUFFER_CLASS);

        final short numCylinders = (short)4;
        clazz.getDeclaredMethod("numCylinders", short.class).invoke(flyweight, numCylinders);

        clazz.getDeclaredMethod("putManufacturerCode", byte[].class, int.class)
            .invoke(flyweight, manufacturerCode, 0);

        verify(mockBuffer).putByte(numCylindersOffset, (byte)numCylinders);
        verify(mockBuffer).putBytes(manufacturerCodeOffset, manufacturerCode, 0, manufacturerCode.length);
    }

    @Test
    public void shouldGenerateCompositeDecoder() throws Exception
    {
        final int bufferOffset = 64;
        final int capacityFieldOffset = bufferOffset;
        final int expectedEngineCapacity = 2000;
        final int expectedMaxRpm = 9000;
        final String className = "EngineDecoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        when(mockBuffer.getShort(capacityFieldOffset, BYTE_ORDER))
            .thenReturn((short)expectedEngineCapacity);

        generateTypeStubs();

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        final Object flyweight = clazz.getConstructor().newInstance();
        wrap(bufferOffset, flyweight, mockBuffer, READ_ONLY_BUFFER_CLASS);

        final int capacityResult = getCapacity(flyweight);
        assertThat(capacityResult, is(expectedEngineCapacity));

        final int maxRpmResult = getInt(flyweight, "maxRpm");
        assertThat(maxRpmResult, is(expectedMaxRpm));
    }

    @Test
    public void shouldGenerateBasicMessage() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object msgFlyweight = wrap(buffer, compileCarEncoder().getConstructor().newInstance());

        final Object groupFlyweight = fuelFiguresCount(msgFlyweight, 0);

        assertNotNull(groupFlyweight);

        assertThat(msgFlyweight.toString(), startsWith("[Car]"));
    }

    @Test
    public void shouldGenerateRepeatingGroupDecoder() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = wrap(
            buffer,
            compileCarDecoder().getConstructor().newInstance(),
            getSbeBlockLength(encoder),
            getSbeSchemaVersion(encoder));

        final Integer initialPosition = getLimit(decoder);

        final Object groupFlyweight = getFuelFigures(decoder);
        assertThat(getLimit(decoder), greaterThan(initialPosition));

        assertThat(getCount(groupFlyweight), is(0));
    }

    @Test
    public void shouldGenerateReadOnlyMessage() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        final long expectedSerialNumber = 5L;
        putSerialNumber(encoder, expectedSerialNumber);
        final long serialNumber = getSerialNumber(decoder);
        assertEquals(expectedSerialNumber, serialNumber);
    }

    @Test
    public void shouldGenerateVarDataCodecs() throws Exception
    {
        final String expectedManufacturer = "Ford";
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);

        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        setManufacturer(encoder, expectedManufacturer);

        final String manufacturer = getManufacturer(decoder);

        assertEquals(expectedManufacturer, manufacturer);
    }

    @Test
    public void shouldGenerateCompositeDecoding() throws Exception
    {
        final int expectedEngineCapacity = 2000;
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);

        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        final Object engineEncoder = get(encoder, "engine");
        final Object engineDecoder = get(decoder, "engine");

        setCapacity(engineEncoder, expectedEngineCapacity);
        assertEquals(expectedEngineCapacity, getCapacity(engineDecoder));
    }

    @Test
    public void shouldGenerateBitSetCodecs() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);

        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        final Object extrasEncoder = getExtras(encoder);
        final Object extrasDecoder = getExtras(decoder);

        assertFalse(getCruiseControl(extrasDecoder));
        setCruiseControl(extrasEncoder, true);
        assertTrue(getCruiseControl(extrasDecoder));
    }

    @Test
    public void shouldGenerateEnumCodecs() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        final Class<?> encoderModel = getModelClass(encoder);
        final Object modelB = encoderModel.getEnumConstants()[1];

        set(encoder, "code", encoderModel, modelB);

        assertThat(get(decoder, "code"), hasToString("B"));
    }

    @Test
    public void shouldGenerateGetString() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        set(encoder, "vehicleCode", String.class, "R11");
        assertThat(get(decoder, "vehicleCode"), is("R11"));

        set(encoder, "vehicleCode", String.class, "");
        assertThat(get(decoder, "vehicleCode"), is(""));

        set(encoder, "vehicleCode", String.class, "R11R12");
        assertThat(get(decoder, "vehicleCode"), is("R11R12"));
    }

    @Test
    public void shouldGenerateGetFixedLengthStringUsingAppendable() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        final StringBuilder result = new StringBuilder();
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getDeclaredConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        set(encoder, "vehicleCode", String.class, "R11");
        get(decoder, "vehicleCode", result);
        assertThat(result.toString(), is("R11"));

        result.setLength(0);
        set(encoder, "vehicleCode", String.class, "");
        get(decoder, "vehicleCode", result);
        assertThat(result.toString(), is(""));

        result.setLength(0);
        set(encoder, "vehicleCode", String.class, "R11R12");
        get(decoder, "vehicleCode", result);
        assertThat(result.toString(), is("R11R12"));
    }

    @Test
    public void shouldGenerateGetVariableStringUsingAppendable() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        final StringBuilder result = new StringBuilder();
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getDeclaredConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);
        set(encoder, "color", String.class, "Red");
        get(decoder, "color", result);
        assertThat(result.toString(), is("Red"));

        result.setLength(0);
        set(encoder, "color", String.class, "");
        get(decoder, "color", result);
        assertThat(result.toString(), is(""));

        result.setLength(0);
        set(encoder, "color", String.class, "Red and Blue");
        get(decoder, "color", result);
        assertThat(result.toString(), is("Red and Blue"));
    }

    @Test
    public void shouldGeneratePutCharSequence() throws Exception
    {
        final UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
        generator().generate();

        final Object encoder = wrap(buffer, compileCarEncoder().getConstructor().newInstance());
        final Object decoder = getCarDecoder(buffer, encoder);

        set(encoder, "vehicleCode", CharSequence.class, "R11");
        assertThat(get(decoder, "vehicleCode"), is("R11"));

        set(encoder, "vehicleCode", CharSequence.class, "");
        assertThat(get(decoder, "vehicleCode"), is(""));

        set(encoder, "vehicleCode", CharSequence.class, "R11R12");
        assertThat(get(decoder, "vehicleCode"), is("R11R12"));
    }


    private Class<?> getModelClass(final Object encoder) throws ClassNotFoundException
    {
        final String className = "Model";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        return encoder.getClass().getClassLoader().loadClass(fqClassName);
    }

    private Object getCarDecoder(final UnsafeBuffer buffer, final Object encoder) throws Exception
    {
        final Object decoder = compileCarDecoder().getConstructor().newInstance();

        return wrap(buffer, decoder, getSbeBlockLength(encoder), getSbeSchemaVersion(encoder));
    }

    private static Object wrap(
        final UnsafeBuffer buffer, final Object decoder, final int blockLength, final int version) throws Exception
    {
        return wrap(buffer, decoder, blockLength, version, READ_ONLY_BUFFER_CLASS);
    }

    private static Object wrap(
        final UnsafeBuffer buffer,
        final Object decoder,
        final int blockLength,
        final int version,
        final Class<?> bufferClass) throws Exception
    {
        decoder
            .getClass()
            .getMethod("wrap", bufferClass, int.class, int.class, int.class)
            .invoke(decoder, buffer, 0, blockLength, version);

        return decoder;
    }

    @Test
    public void shouldValidateMissingMutableBufferClass()
    {
        assertThrows(IllegalArgumentException.class, () ->
            new JavaGenerator(ir, "dasdsads", BUFFER_NAME, false, false, false, outputManager));
    }

    @Test
    public void shouldValidateNotImplementedMutableBufferClass()
    {
        assertThrows(IllegalArgumentException.class, () ->
            new JavaGenerator(ir, "java.nio.ByteBuffer", BUFFER_NAME, false, false, false, outputManager));
    }

    @Test
    public void shouldValidateMissingReadOnlyBufferClass()
    {
        assertThrows(IllegalArgumentException.class, () ->
            new JavaGenerator(ir, BUFFER_NAME, "dasdsads", false, false, false, outputManager));
    }

    @Test
    public void shouldValidateNotImplementedReadOnlyBufferClass()
    {
        assertThrows(IllegalArgumentException.class, () ->
            new JavaGenerator(ir, BUFFER_NAME, "java.nio.ByteBuffer", false, false, false, outputManager));
    }

    private Class<?> compileCarEncoder() throws Exception
    {
        final String className = "CarEncoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        return clazz;
    }

    private Class<?> compileCarDecoder() throws Exception
    {
        final String className = "CarDecoder";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> readerClass = compile(fqClassName);
        assertNotNull(readerClass);

        return readerClass;
    }

    private Class<?> compileModel() throws Exception
    {
        final String className = "Model";
        final String fqClassName = ir.applicableNamespace() + "." + className;

        final Class<?> clazz = compile(fqClassName);
        assertNotNull(clazz);

        return clazz;
    }

    private JavaGenerator generator()
    {
        return new JavaGenerator(ir, BUFFER_NAME, READ_ONLY_BUFFER_NAME, false, false, false, outputManager);
    }

    private void generateTypeStubs() throws IOException
    {
        final JavaGenerator javaGenerator = generator();
        javaGenerator.generateTypeStubs();
    }

    private Class<?> compile(final String fqClassName) throws Exception
    {
        final Map<String, CharSequence> sources = outputManager.getSources();
        final Class<?> aClass = CompilerUtil.compileInMemory(fqClassName, sources);
        if (aClass == null)
        {
            System.out.println(sources);
        }

        return aClass;
    }

    private static void wrap(
        final int bufferOffset, final Object flyweight, final MutableDirectBuffer buffer, final Class<?> bufferClass)
        throws Exception
    {
        flyweight
            .getClass()
            .getDeclaredMethod("wrap", bufferClass, int.class)
            .invoke(flyweight, buffer, bufferOffset);
    }

    private static Object wrap(final UnsafeBuffer buffer, final Object encoder) throws Exception
    {
        wrap(0, encoder, buffer, BUFFER_CLASS);

        return encoder;
    }
}
