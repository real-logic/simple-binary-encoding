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
import java.io.IOException;
import java.security.SecureClassLoader;

public class ClassFileManager<M extends JavaFileManager> extends ForwardingJavaFileManager<M>
{
    /**
     * Instance of JavaClassObject that will store the
     * compiled bytecode of our class
     */
    private JavaClassObject classObject;

    /**
     * Will initialize the manager with the specified
     * standard java file manager
     */
    public ClassFileManager(final M standardManager)
    {
        super(standardManager);
    }

    /**
     * Will be used by us to get the class loader for our
     * compiled class. It creates an anonymous class
     * extending the SecureClassLoader which uses the
     * byte code created by the compiler and stored in
     * the JavaClassObject, and returns the Class for it
     */
    public ClassLoader getClassLoader(final Location location)
    {
        return new SecureClassLoader()
        {
            protected Class<?> findClass(String name)
                throws ClassNotFoundException
            {
                final byte[] buffer = classObject.getBytes();
                return super.defineClass(name, classObject.getBytes(), 0, buffer.length);
            }
        };
    }

    /**
     * Gives the compiler an instance of the JavaClassObject
     * so that the compiler can write the byte code into it.
     */
    public JavaFileObject getJavaFileForOutput(final Location location,
                                               final String className,
                                               final JavaFileObject.Kind kind,
                                               final FileObject sibling)
        throws IOException
    {
        classObject = new JavaClassObject(className, kind);
        return classObject;
    }
}
