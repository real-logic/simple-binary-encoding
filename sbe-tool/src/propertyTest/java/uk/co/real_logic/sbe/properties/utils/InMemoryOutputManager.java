/*
 * Copyright 2013-2024 Real Logic Limited.
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

package uk.co.real_logic.sbe.properties.utils;

import org.agrona.generation.DynamicPackageOutputManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import javax.tools.*;

/**
 * An implementation of {@link DynamicPackageOutputManager} that stores generated source code in memory and compiles it
 * on demand.
 */
public class InMemoryOutputManager implements DynamicPackageOutputManager
{
    private final String packageName;
    private final Map<String, InMemoryJavaFileObject> sourceFiles = new HashMap<>();
    private String packageNameOverride;

    public InMemoryOutputManager(final String packageName)
    {
        this.packageName = packageName;
    }

    @Override
    public Writer createOutput(final String name)
    {
        return new InMemoryWriter(name);
    }

    @Override
    public void setPackageName(final String packageName)
    {
        packageNameOverride = packageName;
    }

    /**
     * Compile the generated sources and return a {@link URLClassLoader} that can be used to load the generated classes.
     *
     * @return a {@link URLClassLoader} that can be used to load the generated classes
     */
    public URLClassLoader compileGeneratedSources()
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        final InMemoryFileManager fileManager = new InMemoryFileManager(standardFileManager);
        final JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            null,
            null,
            null,
            sourceFiles.values()
        );

        if (!task.call())
        {
            throw new IllegalStateException("Compilation failed");
        }

        final GeneratedCodeLoader classLoader = new GeneratedCodeLoader(getClass().getClassLoader());
        classLoader.defineClasses(fileManager);
        return classLoader;
    }

    public void dumpSources(final StringBuilder builder)
    {
        builder.append(System.lineSeparator()).append("Generated sources file count: ").append(sourceFiles.size())
            .append(System.lineSeparator());

        sourceFiles.forEach((qualifiedName, file) ->
        {
            builder.append(System.lineSeparator()).append("Source file: ").append(qualifiedName)
                .append(System.lineSeparator()).append(file.sourceCode)
                .append(System.lineSeparator());
        });
    }

    class InMemoryWriter extends StringWriter
    {
        private final String name;

        InMemoryWriter(final String name)
        {
            this.name = name;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            final String actingPackageName = packageNameOverride == null ? packageName : packageNameOverride;
            packageNameOverride = null;

            final String qualifiedName = actingPackageName + "." + name;
            final InMemoryJavaFileObject sourceFile =
                new InMemoryJavaFileObject(qualifiedName, getBuffer().toString());

            final InMemoryJavaFileObject existingFile = sourceFiles.putIfAbsent(qualifiedName, sourceFile);

            if (existingFile != null && !Objects.equals(existingFile.sourceCode, sourceFile.sourceCode))
            {
                throw new IllegalStateException("Duplicate (but different) class: " + qualifiedName);
            }
        }
    }

    static class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>
    {
        private final List<InMemoryJavaFileObject> outputFiles = new ArrayList<>();

        InMemoryFileManager(final StandardJavaFileManager fileManager)
        {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
            final Location location,
            final String className,
            final JavaFileObject.Kind kind,
            final FileObject sibling)
        {
            final InMemoryJavaFileObject outputFile = new InMemoryJavaFileObject(className, kind);
            outputFiles.add(outputFile);
            return outputFile;
        }

        public Collection<InMemoryJavaFileObject> outputFiles()
        {
            return outputFiles;
        }
    }

    static class InMemoryJavaFileObject extends SimpleJavaFileObject
    {
        private final String sourceCode;
        private final ByteArrayOutputStream outputStream;

        InMemoryJavaFileObject(final String className, final String sourceCode)
        {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = sourceCode;
            this.outputStream = new ByteArrayOutputStream();
        }

        InMemoryJavaFileObject(final String className, final Kind kind)
        {
            super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
            this.sourceCode = null;
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors)
        {
            return sourceCode;
        }

        @Override
        public ByteArrayOutputStream openOutputStream()
        {
            return outputStream;
        }

        public byte[] getClassBytes()
        {
            return outputStream.toByteArray();
        }
    }

    static class GeneratedCodeLoader extends URLClassLoader
    {
        GeneratedCodeLoader(final ClassLoader parent)
        {
            super(new URL[0], parent);
        }

        void defineClasses(final InMemoryFileManager fileManager)
        {
            fileManager.outputFiles().forEach(file ->
            {
                final byte[] classBytes = file.getClassBytes();
                super.defineClass(null, classBytes, 0, classBytes.length);
            });
        }
    }
}
