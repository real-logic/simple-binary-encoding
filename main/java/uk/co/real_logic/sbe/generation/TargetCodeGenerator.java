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
package uk.co.real_logic.sbe.generation;

import uk.co.real_logic.sbe.generation.cpp98.Cpp98Generator;
import uk.co.real_logic.sbe.generation.csharp.CSharpGenerator;
import uk.co.real_logic.sbe.generation.csharp.CSharpNamespaceOutputManager;
import uk.co.real_logic.sbe.generation.cpp98.NamespaceOutputManager;
import uk.co.real_logic.sbe.generation.java.JavaGenerator;
import uk.co.real_logic.sbe.generation.java.PackageOutputManager;
import uk.co.real_logic.sbe.ir.Ir;

import java.io.IOException;

/**
 * Target a code generator for a given language.
 */
public enum TargetCodeGenerator
{
    JAVA()
        {
            public CodeGenerator newInstance(final Ir ir, final String outputDir)
                throws IOException
            {
                return new JavaGenerator(ir, new PackageOutputManager(outputDir, ir.applicableNamespace()));
            }
        },

    CPP98()
        {
            public CodeGenerator newInstance(final Ir ir, final String outputDir)
                throws IOException
            {
                return new Cpp98Generator(ir, new NamespaceOutputManager(outputDir, ir.applicableNamespace()));
            }
        },

    CSHARP()
        {
            public CodeGenerator newInstance(final Ir ir, final String outputDir)
                throws IOException
            {
                return new CSharpGenerator(ir, new CSharpNamespaceOutputManager(outputDir, ir.applicableNamespace()));
            }
        };


    /**
     * Get a new {@link CodeGenerator} for the given target language.
     *
     * @param ir        describing the message schemas from which code should generated.
     * @param outputDir to which the generated code with be written.
     * @return a new instance of a {@link CodeGenerator} for the given target language.
     * @throws IOException if an error occurs when dealing with the output directory.
     */
    public abstract CodeGenerator newInstance(final Ir ir, final String outputDir) throws IOException;

    /**
     * Do a case insensitive lookup of a target language for code generation.
     *
     * @param name of the target language to lookup.
     * @return the {@link TargetCodeGenerator} for the given language name.
     */
    public static TargetCodeGenerator get(final String name)
    {
        for (final TargetCodeGenerator target : values())
        {
            if (name.equalsIgnoreCase(target.name()))
            {
                return target;
            }
        }

        throw new IllegalArgumentException("No code generator for name: " + name);
    }
}
