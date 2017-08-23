package uk.co.real_logic.sbe.generation.rust;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class RustFlatFileOutputManagerTest
{
    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    static final String PACKAGE_NAME = "uk.co.real_logic.test";
    static final String EXAMPLE_CLASS_NAME = "ExampleClassName";

    @Test
    public void shouldCreateFileUponConstruction() throws Exception
    {
        final File tempDir = folderRule.getRoot();
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

    @Test(expected = NullPointerException.class)
    public void nullDirectoryParamThrowsNPE()
    {
        new RustFlatFileOutputManager(null, PACKAGE_NAME);
    }

    @Test(expected = NullPointerException.class)
    public void nullPackageParamThrowsNPE()
    {
        new RustFlatFileOutputManager(folderRule.getRoot().getAbsolutePath(), null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfTargetDirNotWritable() throws IOException, InterruptedException
    {
        final File tempDir = folderRule.newFolder();
        assertTrue(tempDir.setReadOnly());
        final String tempDirName = tempDir.getAbsolutePath();
        try
        {
            new RustFlatFileOutputManager(tempDirName, PACKAGE_NAME);
        }
        catch (final IllegalStateException e)
        {
            int waitCount = 0;
            boolean writable;
            do
            {
                writable = tempDir.setWritable(true);
                Thread.sleep(100);
                waitCount += 1;
            }
            while (!writable && waitCount < 10);
            throw e;
        }
        fail("should be unreachable");
    }

    private static String getExpectedFullFileName(final String tempDirName)
    {
        return (tempDirName.endsWith("" + File.separatorChar) ? tempDirName : tempDirName + File.separatorChar) +
            PACKAGE_NAME.replace('.', '_') + ".rs";
    }

}
