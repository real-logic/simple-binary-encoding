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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CompilerUtil
{
    public static Class<?> compile(final String className, final Map<String, CharSequence> sources) throws Exception
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
