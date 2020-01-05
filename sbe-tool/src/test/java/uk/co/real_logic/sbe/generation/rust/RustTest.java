package uk.co.real_logic.sbe.generation.rust;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.ir.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RustTest
{
    @TempDir
    File temporaryFolder;

    @Test
    public void nullIRParamShouldTossNPE()
    {
        assertThrows(NullPointerException.class, () -> new Rust().newInstance(null, temporaryFolder.toString()));
    }

    @Test
    public void nullOutputDirParamShouldTossNPE()
    {
        assertThrows(NullPointerException.class, () -> new Rust().newInstance(minimalDummyIr(), null));
    }

    @Test
    public void happyPathRustGeneratorThatThrowsNoExceptions() throws IOException
    {
        final File newFolder = temporaryFolder;
        final CodeGenerator codeGenerator = new Rust().newInstance(minimalDummyIr(), newFolder.toString());
        assertNotNull(codeGenerator);
        codeGenerator.generate();
        newFolder.setWritable(true, false);
    }

    static Ir minimalDummyIr()
    {
        return new Ir("a", "b", 0, 1, null, "2", ByteOrder.BIG_ENDIAN,
            Arrays.asList(
                dummyToken(Signal.ENCODING, HeaderStructure.BLOCK_LENGTH),
                dummyToken(Signal.ENCODING, HeaderStructure.SCHEMA_ID),
                dummyToken(Signal.ENCODING, HeaderStructure.SCHEMA_VERSION),
                dummyToken(Signal.ENCODING, HeaderStructure.TEMPLATE_ID)
            )
        );
    }

    private static Token dummyToken(final Signal signal, final String name)
    {
        return new Token(
            signal,
            name,
            name,
            name,
            0,
            0,
            0,
            0,
            0,
            1,
            new Encoding.Builder().primitiveType(PrimitiveType.INT32).build());
    }
}
