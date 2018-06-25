package uk.co.real_logic.sbe.generation.csharp;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

import org.agrona.generation.StringWriterOutputManager;
import org.junit.Test;

import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

public class Issue567GroupSizeTest
{
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenUsingATypeThatIsNotConstrainedToFitInAnIntAsTheGroupSize() throws Exception
    {
        // Arrange
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
        // Arrange
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
