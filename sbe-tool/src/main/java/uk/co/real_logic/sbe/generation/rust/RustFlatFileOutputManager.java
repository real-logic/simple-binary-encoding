package uk.co.real_logic.sbe.generation.rust;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static java.io.File.separatorChar;

public class RustFlatFileOutputManager implements OutputManager
{
    private final File outputFile;

    RustFlatFileOutputManager(final String baseDirectoryName, final String packageName)
    {
        Verify.notNull(baseDirectoryName, "baseDirectoryName");
        Verify.notNull(packageName, "packageName");

        final String outputDirName = (baseDirectoryName.endsWith("" + separatorChar) ? baseDirectoryName :
            baseDirectoryName + separatorChar);
        final File outputDir = new File(outputDirName);
        final boolean outputDirAvailable = outputDir.exists() || outputDir.mkdirs();
        if (!outputDirAvailable)
        {
            throw new IllegalStateException("Unable to create directory: " + outputDirName);
        }
        this.outputFile = new File(outputDirName + packageName.replace('.', '_') + ".rs");

        // Initialize the contents of the output file with a minimal header
        try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8))
        {
            writer.append("/// Generated code for SBE package ")
                .append(packageName)
                .append("\n\n");
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Unable to write header for : " + outputDirName);
        }
    }

    @Override
    public Writer createOutput(final String name) throws IOException
    {
        // Note the deliberate lack of a "CREATE" or "CREATE_NEW" option in order to
        // prevent writing to a file that has not been properly initialized
        final Writer writer = Files.newBufferedWriter(outputFile.toPath(),
            StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        writer.append("\n/// ")
            .append(name)
            .append("\n");
        return writer;
    }
}
