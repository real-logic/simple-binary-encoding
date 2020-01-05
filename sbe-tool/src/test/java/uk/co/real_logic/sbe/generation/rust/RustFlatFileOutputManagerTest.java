package uk.co.real_logic.sbe.generation.rust;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RustFlatFileOutputManagerTest
{
    @TempDir
    File tempDir;

    static final String PACKAGE_NAME = "uk.co.real_logic.test";
    static final String EXAMPLE_CLASS_NAME = "ExampleClassName";

    @Test
    public void shouldCreateFileUponConstruction() throws Exception
    {
        final String tempDirName = tempDir.getAbsolutePath();
        final RustFlatFileOutputManager om = new RustFlatFileOutputManager(tempDirName, PACKAGE_NAME);

        final String expectedFullyQualifiedFilename = getExpectedFullFileName(tempDirName);
        final Path path = FileSystems.getDefault().getPath(expectedFullyQualifiedFilename);
        assertTrue(Files.exists(path));

        final String initialContents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertTrue(initialContents.contains("Generated code"));

        final String arbitraryInput = "\narbitrary\n";
        assertFalse(initialContents.contains(arbitraryInput));

        try (Writer out = om.createOutput(EXAMPLE_CLASS_NAME))
        {
            out.write(arbitraryInput);
        }

        final String postOutputContents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertTrue(postOutputContents.contains("Generated code"));
        assertTrue(postOutputContents.contains(arbitraryInput));
    }

    @Test
    public void nullDirectoryParamThrowsNPE()
    {
        assertThrows(NullPointerException.class, () -> new RustFlatFileOutputManager(null, PACKAGE_NAME));
    }

    @Test
    public void nullPackageParamThrowsNpe()
    {
        assertThrows(NullPointerException.class,
            () -> new RustFlatFileOutputManager(tempDir.getAbsolutePath(), null));
    }

    private static String getExpectedFullFileName(final String tempDirName)
    {
        return (tempDirName.endsWith("" + File.separatorChar) ? tempDirName : tempDirName + File.separatorChar) +
            PACKAGE_NAME.replace('.', '_') + ".rs";
    }
}
