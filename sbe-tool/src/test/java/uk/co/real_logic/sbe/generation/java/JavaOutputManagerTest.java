package uk.co.real_logic.sbe.generation.java;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class JavaOutputManagerTest
{

    private final String tempDirName = System.getProperty("java.io.tmpdir");

    @Test
    void shouldCreateFileWithinPackage() throws Exception
    {
        final String packageName = "uk.co.real_logic.test";
        final String exampleClassName = "ExampleClassName";

        final JavaOutputManager cut = new JavaOutputManager(tempDirName, packageName);
        final Writer out = cut.createOutput(exampleClassName);
        out.close();

        final String typePackageName = "uk.co.real_logic.common";
        final String typeClassName = "CompositeBigDecimal";
        cut.setPackageName(typePackageName);
        final Writer typeOut = cut.createOutput(typeClassName);
        typeOut.close();

        final String typePackageName2 = "uk.co.real_logic.common2";
        final String typeClassName2 = "CompositeBigInteger";
        cut.setPackageName(typePackageName2);
        final Writer typeOut2 = cut.createOutput(typeClassName2);
        typeOut2.close();

        final String exampleClassName2 = "ExampleClassName2";

        final Writer out2 = cut.createOutput(exampleClassName2);
        out2.close();

        assertFileExists(packageName, exampleClassName);
        assertFileExists(packageName, exampleClassName2);
        assertFileExists(typePackageName, typeClassName);
        assertFileExists(typePackageName2, typeClassName2);
    }

    private void assertFileExists(final String packageName, final String exampleClassName) throws IOException
    {
        final String baseDirName = tempDirName.endsWith("" + File.separatorChar)
            ? tempDirName : tempDirName + File.separatorChar;

        final String fullyQualifiedFilename = baseDirName + packageName.replace('.', File.separatorChar)
            + File.separatorChar + exampleClassName + ".java";

        final Path path = FileSystems.getDefault().getPath(fullyQualifiedFilename);
        final boolean exists = Files.exists(path);
        Files.delete(path);

        assertTrue(exists);
    }

}
