package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.TargetCodeGenerator;
import uk.co.real_logic.sbe.ir.Ir;

/**
 * This class is present largely to enable the dynamic-loading style pattern
 * of specifying a TargetCodeGenerator Java class name rather than the language
 * name.
 */
public class Rust implements TargetCodeGenerator
{
    public CodeGenerator newInstance(final Ir ir, final String outputDir)
    {
        return defaultRustGenerator(ir, outputDir);
    }

    public static CodeGenerator defaultRustGenerator(final Ir ir, final String outputDir)
    {
        return new RustGenerator(ir, new RustFlatFileOutputManager(outputDir, ir.applicableNamespace()));
    }
}
