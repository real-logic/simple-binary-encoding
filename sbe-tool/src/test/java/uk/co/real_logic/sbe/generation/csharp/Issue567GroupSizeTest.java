package uk.co.real_logic.sbe.generation.csharp;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.real_logic.sbe.Tests;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

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
    public void shouldThrowWhenUsingATypeThatIsNotConstrainedToFitInAnIntAsTheGroupSize()
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        assertThrows(IllegalArgumentException.class,
            () -> parse(Tests.getLocalResource("issue567-invalid.xml"), options));
    }

    @Test
    public void shouldGenerateWhenUsingATypeThatIsConstrainedToFitInAnIntAsTheGroupSize() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(Tests.getLocalResource("issue567-valid.xml"), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        final StringWriterOutputManager outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        final CSharpGenerator generator = new CSharpGenerator(ir, outputManager);

        // Act + Assert (no exception)
        generator.generate();
    }
}
