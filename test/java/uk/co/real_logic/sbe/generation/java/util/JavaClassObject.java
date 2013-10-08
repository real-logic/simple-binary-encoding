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

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class JavaClassObject extends SimpleJavaFileObject
{
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public JavaClassObject(final String className, final Kind kind)
    {
        super(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes()
    {
        return baos.toByteArray();
    }

    public OutputStream openOutputStream() throws IOException
    {
        return baos;
    }
}
