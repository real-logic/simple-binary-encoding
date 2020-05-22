/*
 * Copyright 2013-2020 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation;

import org.agrona.generation.PackageOutputManager;
import uk.co.real_logic.sbe.generation.c.CGenerator;
import uk.co.real_logic.sbe.generation.c.COutputManager;
import uk.co.real_logic.sbe.generation.cpp.CppGenerator;
import uk.co.real_logic.sbe.generation.cpp.NamespaceOutputManager;
import uk.co.real_logic.sbe.generation.golang.GolangGenerator;
import uk.co.real_logic.sbe.generation.golang.GolangOutputManager;
import uk.co.real_logic.sbe.generation.java.JavaGenerator;
import uk.co.real_logic.sbe.ir.Ir;

import static uk.co.real_logic.sbe.SbeTool.*;

/**
 * Loader for {@link CodeGenerator}s which target a language. This provide convenient short names rather than the
 * fully qualified class name of the generator.
 */
public enum TargetCodeGeneratorLoader implements TargetCodeGenerator
{
    /**
     * Generates codecs for the Java 8 programming language.
     */
    JAVA()
    {
        public CodeGenerator newInstance(final Ir ir, final String outputDir)
        {
            return new JavaGenerator(
                ir,
                System.getProperty(JAVA_ENCODING_BUFFER_TYPE, JAVA_DEFAULT_ENCODING_BUFFER_TYPE),
                System.getProperty(JAVA_DECODING_BUFFER_TYPE, JAVA_DEFAULT_DECODING_BUFFER_TYPE),
                Boolean.getBoolean(JAVA_GROUP_ORDER_ANNOTATION),
                Boolean.getBoolean(JAVA_GENERATE_INTERFACES),
                Boolean.getBoolean(DECODE_UNKNOWN_ENUM_VALUES),
                new PackageOutputManager(outputDir, ir.applicableNamespace()));
        }
    },

    /**
     * Generates codecs for the C11 programming language.
     */
    C()
    {
        public CodeGenerator newInstance(final Ir ir, final String outputDir)
        {
            return new CGenerator(ir, new COutputManager(outputDir, ir.applicableNamespace()));
        }
    },

    /**
     * Generates codecs for the C++11 programming language with some conditional includes for C++14 and C++17.
     */
    CPP()
    {
        public CodeGenerator newInstance(final Ir ir, final String outputDir)
        {
            return new CppGenerator(ir, new NamespaceOutputManager(outputDir, ir.applicableNamespace()));
        }
    },

    /**
     * Generates codecs for the Go programming language.
     */
    GOLANG()
    {
        public CodeGenerator newInstance(final Ir ir, final String outputDir)
        {
            return new GolangGenerator(ir, new GolangOutputManager(outputDir, ir.applicableNamespace()));
        }
    };

    /**
     * Do a case insensitive lookup of a target language for code generation.
     *
     * @param name of the target language to lookup.
     * @return the {@link TargetCodeGenerator} for the given language name.
     */
    public static TargetCodeGenerator get(final String name)
    {
        for (final TargetCodeGeneratorLoader target : values())
        {
            if (target.name().equalsIgnoreCase(name))
            {
                return target;
            }
        }

        try
        {
            return (TargetCodeGenerator)Class.forName(name).getConstructor().newInstance();
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException("No code generator for name: " + name, ex);
        }
    }
}
