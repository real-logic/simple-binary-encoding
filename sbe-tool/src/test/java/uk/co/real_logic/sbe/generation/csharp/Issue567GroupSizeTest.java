package uk.co.real_logic.sbe.generation.csharp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.*;

import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.io.PrintStream;

public class Issue567GroupSizeTest
{
    public static final String ERR_MSG =
        "WARNING: at <sbe:message name=\"issue567\"> <group name=\"group\"> \"numInGroup\" should be UINT8 or UINT16";

    private PrintStream err;
    private PrintStream mockErr = mock(PrintStream.class);

    @Before
    public void before()
    {
        err = System.err;
        System.setErr(mockErr);
    }

    @After
    public void after()
    {
        System.setErr(err);
        verify(mockErr).println(ERR_MSG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenUsingATypeThatIsNotConstrainedToFitInAnIntAsTheGroupSize() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(TestUtil.getLocalResource("issue567-invalid.xml"), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        final StringWriterOutputManager outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        final CSharpGenerator generator = new CSharpGenerator(ir, outputManager);

        // Act + Assert (exception thrown)
        generator.generate();
    }

    @Test
    public void shouldGenerateWhenUsingATypeThatIsConstrainedToFitInAnIntAsTheGroupSize() throws Exception
    {
        final ParserOptions options = ParserOptions.builder().stopOnError(true).build();
        final MessageSchema schema = parse(TestUtil.getLocalResource("issue567-valid.xml"), options);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);

        final StringWriterOutputManager outputManager = new StringWriterOutputManager();
        outputManager.setPackageName(ir.applicableNamespace());
        final CSharpGenerator generator = new CSharpGenerator(ir, outputManager);

        // Act + Assert (no exception)
        generator.generate();
    }
}
