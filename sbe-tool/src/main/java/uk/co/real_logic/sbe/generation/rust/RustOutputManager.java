/*
 * Copyright 2013-2021 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static java.io.File.separatorChar;
import static uk.co.real_logic.sbe.generation.rust.RustUtil.toLowerSnakeCase;

/**
 * {@link OutputManager} for managing the creation of Rust source files as the target of code generation.
 * The character encoding for the {@link java.io.Writer} is UTF-8.
 */
public class RustOutputManager implements OutputManager
{
    private final File rootDir;
    private final File srcDir;

    /**
     * Create a new {@link OutputManager} for generating rust source files into a given module.
     * @param baseDirName for the generated source code.
     * @param packageName for the generated source code relative to the baseDirName.
     */
    public RustOutputManager(final String baseDirName, final String packageName)
    {
        Verify.notNull(baseDirName, "baseDirName");
        Verify.notNull(packageName, "packageName");

        String dirName = baseDirName.endsWith("" + separatorChar) ? baseDirName : baseDirName + separatorChar;
        dirName += packageName.replaceAll("\\.", "_").toLowerCase() + separatorChar;
        final String libDirName = dirName;
        rootDir = new File(libDirName);

        final String srcDirName = libDirName + separatorChar + "src";

        srcDir = new File(srcDirName);
        if (!srcDir.exists() && !srcDir.mkdirs())
        {
            throw new IllegalStateException("Unable to create directory: " + srcDirName);
        }
    }

    /**
     * Create a new output which will be a rust source file in the given module.
     * <p>
     * The {@link java.io.Writer} should be closed once the caller has finished with it. The Writer is
     * buffered for efficient IO operations.
     *
     * @param name the name of the rust struct.
     * @return a {@link java.io.Writer} to which the source code should be written.
     * @throws IOException if an issue occurs when creating the file.
     */
    @Override public Writer createOutput(final String name) throws IOException
    {
        final String fileName = toLowerSnakeCase(name) + ".rs";
        final File targetFile = new File(srcDir, fileName);
        return Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8);
    }

    /**
     *
     * @return a {@link java.io.Writer} to which the crate definition should be written.
     * @throws IOException if an issue occurs when creating the file.
     */
    public Writer createCargoToml() throws IOException
    {
        final String fileName = "Cargo.toml";
        final File targetFile = new File(rootDir, fileName);
        return Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8);
    }

}
