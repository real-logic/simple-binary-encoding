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
package uk.co.real_logic.sbe.generation.java.util;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CompilerUtil
{
    private static final String TEMP_DIR_NAME = System.getProperty("java.io.tmpdir");

    public static Class<?> compileInMemory(final String className, final Map<String, CharSequence> sources)
        throws Exception
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaFileManager fileManager = new ClassFileManager<>(compiler.getStandardFileManager(null, null, null));
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, wrap(sources));

        if (!task.call().booleanValue())
        {
            for (final Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                System.err.println(diagnostic.getCode());
                System.err.println(diagnostic.getKind());
                System.err.println(diagnostic.getPosition());
                System.err.println(diagnostic.getStartPosition());
                System.err.println(diagnostic.getEndPosition());
                System.err.println(diagnostic.getSource());
                System.err.println(diagnostic.getMessage(null));
            }

            return null;
        }

        return fileManager.getClassLoader(null).loadClass(className);
    }

    public static Class<?> compileOnDisk(final String className, final Map<String, CharSequence> sources)
        throws Exception
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        List<String> options = new ArrayList<>();
        options.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path") + File.pathSeparator + TEMP_DIR_NAME));

        final Collection<File> files = persist(sources);
        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
        final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        if (!task.call().booleanValue())
        {
            for (final Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                System.err.println(diagnostic.getCode());
                System.err.println(diagnostic.getKind());
                System.err.println(diagnostic.getPosition());
                System.err.println(diagnostic.getStartPosition());
                System.err.println(diagnostic.getEndPosition());
                System.err.println(diagnostic.getSource());
                System.err.println(diagnostic.getMessage(null));
            }

            return null;
        }

        final Class<?> clazz = fileManager.getClassLoader(null).loadClass(className);

        fileManager.close();

        return clazz;
    }

    private static Collection<File> persist(final Map<String, CharSequence> sources)
        throws Exception
    {
        final Collection<File> files = new ArrayList<>(sources.size());
        for (final Map.Entry<String, CharSequence> entry : sources.entrySet())
        {
            final String fqClassName = entry.getKey();
            String className = fqClassName;
            Path path = Paths.get(TEMP_DIR_NAME);

            final int indexOfLastDot = fqClassName.lastIndexOf('.');
            if (indexOfLastDot != -1)
            {
                className = fqClassName.substring(indexOfLastDot + 1, fqClassName.length());

                path = Paths.get(TEMP_DIR_NAME + File.separatorChar +
                                 fqClassName.substring(0, indexOfLastDot).replace('.', File.separatorChar));
                Files.createDirectories(path);
            }

            final File file = new File(path.toString(), className + ".java");
            files.add(file);

            try (final FileWriter out = new FileWriter(file))
            {
                out.append(entry.getValue());
                out.flush();
            }
        }

        return files;
    }

    private static Collection<CharSequenceJavaFileObject> wrap(final Map<String, CharSequence> sources)
    {
        final Collection<CharSequenceJavaFileObject> collection = new ArrayList<>(sources.size());
        for (final Map.Entry<String, CharSequence> entry : sources.entrySet())
        {
            collection.add(new CharSequenceJavaFileObject(entry.getKey(), entry.getValue()));
        }

        return collection;
    }
}
