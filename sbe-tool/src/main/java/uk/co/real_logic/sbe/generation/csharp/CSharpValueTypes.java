package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.TargetCodeGenerator;
import uk.co.real_logic.sbe.ir.Ir;

public class CSharpValueTypes implements TargetCodeGenerator
{
    public CodeGenerator newInstance(final Ir ir, final String outputDir)
    {
        return new CSharpStructGenerator(ir, new CSharpNamespaceOutputManager(outputDir, ir.applicableNamespace()));
    }
}
