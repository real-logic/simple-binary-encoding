package uk.co.real_logic.sbe.generation.csharp;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.Signal;
import uk.co.real_logic.sbe.ir.Token;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CSharpStructGenerator implements CodeGenerator
{
    private static final String INDENT = "    ";

    private final Ir ir;
    private final OutputManager outputManager;

    public CSharpStructGenerator(final Ir ir, final OutputManager outputManager)
    {
        Verify.notNull(ir, "ir");
        Verify.notNull(outputManager, "outputManager");

        this.ir = ir;
        this.outputManager = outputManager;
    }

    @Override
    public void generate() throws IOException
    {
        writeComposite(ir.headerStructure().tokens());
    }

    public void writeComposite(final List<Token> tokens) throws IOException
    {
        final String compositeName = CSharpUtil.formatClassName(tokens.get(0).applicableTypeName());
        final String structName = compositeName + "Value";

        try (Writer out = outputManager.createOutput(structName))
        {
            out.append(generateFileHeader(ir.packageName()));
            out.append(generateStructDeclaration(structName, compositeName));
            out.append(generateStructConstructor(structName, tokens, INDENT + INDENT));
            out.append(generateStructMembers(tokens, INDENT + INDENT));
            out.append(INDENT + "}\n");
            out.append("}\n");
        }
    }

    private CharSequence generateFileHeader(final String packageName)
    {
        return String.format(
                "/* Generated SBE (Simple Binary Encoding) message codec */\n\n" +
                "using System;\n" +
                "using System.Runtime.InteropServices;\n\n" +
                "namespace %s\n" +
                "{\n", packageName);
    }

    private CharSequence generateStructDeclaration(final String structName, final String compositeName)
    {
        return String.format(
            INDENT + "[StructLayout(LayoutKind.Explicit, Size = %s.Size)]\n" +
            INDENT + "public struct %s\n" +
            INDENT + "{\n",
            compositeName, structName);
    }

    private CharSequence generateStructConstructor(final String structName, final List<Token> tokens,
        final String baseIndent)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(baseIndent);
        sb.append("public ");
        sb.append(structName);
        sb.append("(");

        boolean firstTokenDone = false;

        for (final Token token : tokens)
        {
            switch (token.signal())
            {
                case ENCODING:
                    if (firstTokenDone)
                    {
                        sb.append(",");
                    }
                    sb.append("\n");
                    sb.append(baseIndent + INDENT + CSharpUtil.cSharpTypeName(token.encoding().primitiveType()));
                    sb.append(" ");
                    sb.append(token.name());
                    firstTokenDone = true;
                    break;
            }
        }

        sb.append(")\n");
        sb.append(baseIndent + "{\n"); // start of constructor body

        // Generate field assignments
        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                sb.append(String.format(baseIndent + INDENT + "%s = %s;\n",
                    CSharpUtil.toUpperFirstChar(token.name()), token.name()));
            }
        }

        sb.append(baseIndent + "}\n"); // end of constructor

        return sb;
    }

    private CharSequence generateStructMembers(final List<Token> tokens, final String baseIndent)
    {
        final StringBuilder sb = new StringBuilder();

        for (final Token token : tokens)
        {
            if (token.signal() == Signal.ENCODING)
            {
                sb.append(String.format("\n" + baseIndent + "[FieldOffset(%s)]\n", token.offset()));
                sb.append(String.format(baseIndent + "public readonly %s %s;\n",
                    CSharpUtil.cSharpTypeName(token.encoding().primitiveType()),
                    CSharpUtil.toUpperFirstChar(token.name())));
            }
        }

        return sb;
    }

}
