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
package uk.co.real_logic.sbe.generation.java;

import org.junit.Before;
import org.junit.Test;
import uk.co.real_logic.sbe.TestUtil;
import uk.co.real_logic.sbe.generation.OutputManager;
import uk.co.real_logic.sbe.generation.java.util.CharSequenceJavaFileObject;
import uk.co.real_logic.sbe.generation.java.util.ClassFileManager;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;

import javax.tools.*;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class JavaGeneratorTest
{
    private final StringWriter stringWriter = new StringWriter();
    private final StringWriter blackHole = new StringWriter();
    private final OutputManager mockOutputManager = mock(OutputManager.class);
    private IntermediateRepresentation ir;

    @Before
    public void setUp() throws Exception
    {
        MessageSchema schema = parse(TestUtil.getLocalResource("CodeGenerationSchemaTest.xml"));
        IrGenerator irg = new IrGenerator();
        ir = irg.generate(schema);
    }

    @Test
    public void shouldGenerateMessageHeaderStub() throws Exception
    {
        when(mockOutputManager.createOutput(JavaGenerator.MESSAGE_HEADER_VISITOR)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateMessageHeaderStub();

        System.out.println(stringWriter);
    }

    @Test
    public void shouldGenerateUint8EnumStub() throws Exception
    {
        final String className = "Boolean";
        final String fqClassName = ir.getPackageName() + "." + className;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(className)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = compileCode(fqClassName);
        assertNotNull(clazz);

        final Method method = clazz.getDeclaredMethod("lookup", short.class);
        final Object result = method.invoke(null, Short.valueOf((short)1));

        assertThat(result.toString(), is("TRUE"));
    }

    @Test
    public void shouldGenerateCharEnumStub() throws Exception
    {
        final String className = "ModelType";
        final String fqClassName = ir.getPackageName() + "." + className;

        when(mockOutputManager.createOutput(anyString())).thenReturn(blackHole);
        when(mockOutputManager.createOutput(className)).thenReturn(stringWriter);

        final JavaGenerator javaGenerator = new JavaGenerator(ir, mockOutputManager);
        javaGenerator.generateTypeStubs();

        final Class<?> clazz = compileCode(fqClassName);
        assertNotNull(clazz);

        final Method method = clazz.getDeclaredMethod("lookup", byte.class);
        final Object result = method.invoke(null, Byte.valueOf((byte)'B'));

        assertThat(result.toString(), is("B"));
    }

    private Class<?> compileCode(final String className) throws Exception
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final JavaFileManager fileManager = new ClassFileManager<>(compiler.getStandardFileManager(null, null, null));

        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        final JavaCompiler.CompilationTask task =
            compiler.getTask(null, fileManager, diagnostics, null, null,
                             Arrays.asList(new CharSequenceJavaFileObject(className, stringWriter.toString())));

        if (!task.call().booleanValue())
        {
            for (final Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                System.out.println(diagnostic.getCode());
                System.out.println(diagnostic.getKind());
                System.out.println(diagnostic.getPosition());
                System.out.println(diagnostic.getStartPosition());
                System.out.println(diagnostic.getEndPosition());
                System.out.println(diagnostic.getSource());
                System.out.println(diagnostic.getMessage(null));
            }

            return null;
        }

        return fileManager.getClassLoader(null).loadClass(className);
    }
}
