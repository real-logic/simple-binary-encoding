/*
 * Copyright 2013 Real Logic Ltd.
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
package uk.co.real_logic.sbe.generation.csharp;

import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.util.Verify;

import java.io.*;

/**
 * {@link uk.co.real_logic.sbe.generation.OutputManager} for managing the creation of C# source files
 * as the target of code generation.
 *
 * The character encoding for the {@link java.io.Writer} is UTF-8.
 */
public class CSharpNamespaceOutputManager implements OutputManager
{
    private final File outputDir;

    /**
     * Create a new {@link uk.co.real_logic.sbe.generation.OutputManager} for generating C# source
     * files into a given package.
     *
     * @param baseDirectoryName for the generated source code.
     * @param packageName for the generated source code relative to the baseDirectoryName.
     * @throws IOException if an error occurs during output
     */
    public CSharpNamespaceOutputManager(final String baseDirectoryName, final String packageName) throws IOException
    {
        Verify.notNull(baseDirectoryName, "baseDirectoryName");
        Verify.notNull(packageName, "packageName");

        // TODO ODE I'm generating the files without directory structure, that's probably OK for csharp
        outputDir = new File(baseDirectoryName);
        if (!outputDir.exists())
        {
            if (!outputDir.mkdirs())
            {
                throw new IllegalStateException("Unable to create directory: " + baseDirectoryName);
            }
        }
    }

    /**
     * Create a new output which will be a C# source file in the given package.
     *
     * The {@link java.io.Writer} should be closed once the caller has finished with it. The Writer is
     * buffer for efficient IO operations.
     *
     * @param name the name of the C# class.
     * @return a {@link java.io.Writer} to which the source code should be written.
     */
    public Writer createOutput(final String name) throws IOException
    {
        final File targetFile = new File(outputDir, name + ".cs");
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8"));
    }
}
