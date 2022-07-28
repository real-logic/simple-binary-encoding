package uk.co.real_logic.sbe.generation.java;

import java.util.Map;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.generation.CompilerUtil;
import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import static org.junit.jupiter.api.Assertions.assertNotNull;


import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class QualifiedYieldTest
{
    private static final Class<?> BUFFER_CLASS = MutableDirectBuffer.class;
    private static final String BUFFER_NAME = BUFFER_CLASS.getName();
    private static final Class<DirectBuffer> READ_ONLY_BUFFER_CLASS = DirectBuffer.class;
    private static final String READ_ONLY_BUFFER_NAME = READ_ONLY_BUFFER_CLASS.getName();

    private final StringWriterOutputManager outputManager = new StringWriterOutputManager();

    @Test
    @EnabledForJreRange(min = JRE.JAVA_17)
    void shouldGenerateValidJava() throws Exception
    {
        System.setProperty(SbeTool.KEYWORD_APPEND_TOKEN, "_");

        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(Tests.getLocalResource("issue910.xml"), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);
        final JavaGenerator generator = new JavaGenerator(
                ir, BUFFER_NAME, READ_ONLY_BUFFER_NAME, false, false, false, outputManager);

        outputManager.setPackageName(ir.applicableNamespace());
        generator.generateMessageHeaderStub();
        generator.generateTypeStubs();
        generator.generate();

        final Map<String, CharSequence> sources = outputManager.getSources();

        {
            final String fqClassName = ir.applicableNamespace() + "." + "Issue910FieldDecoder";
            final Class<?> aClass = CompilerUtil.compileInMemory(fqClassName, sources);
            assertNotNull(aClass);
        }
    }
}
