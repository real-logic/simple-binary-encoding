/*
 * Copyright 2013-2019 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.generation.rust;

import org.agrona.Verify;
import org.agrona.generation.OutputManager;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import static java.io.File.separatorChar;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;

public class RustFlatFileOutputManager implements OutputManager
{
    private final File outputFile;

    RustFlatFileOutputManager(final String baseDirName, final String packageName)
    {
        Verify.notNull(baseDirName, "baseDirName");
        Verify.notNull(packageName, "packageName");

        final String outputDirName = baseDirName.endsWith("" + separatorChar) ?
            baseDirName : baseDirName + separatorChar;
        final File outputDir = new File(outputDirName);
        final boolean outputDirAvailable = outputDir.exists() || outputDir.mkdirs();
        if (!outputDirAvailable)
        {
            throw new IllegalStateException("Unable to create directory: " + outputDirName);
        }
        this.outputFile = new File(outputDirName + packageName.replace('.', '_') + ".rs");

        try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), UTF_8))
        {
            writer.append("/// Generated code for SBE package ")
                .append(packageName)
                .append("\n\n");
        }
        catch (final IOException ex)
        {
            throw new IllegalStateException("Unable to write header for : " + outputDirName, ex);
        }
    }

    public Writer createOutput(final String name) throws IOException
    {
        // Note the deliberate lack of a "CREATE" or "CREATE_NEW" option in order to
        // prevent writing to a file that has not been properly initialized
        final Writer writer = Files.newBufferedWriter(outputFile.toPath(), UTF_8, WRITE, APPEND);

        writer.append("\n/// ")
            .append(name)
            .append("\n");

        return writer;
    }
}
