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

import uk.co.real_logic.sbe.generation.OutputManager;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class StringWriterOutputManager implements OutputManager
{
    private String packageName;
    private final Map<String, StringWriter> sourceFileByName = new HashMap<>();

    public Writer createOutput(final String name) throws IOException
    {
        final StringWriter stringWriter = new StringWriter();
        sourceFileByName.put(packageName + "." + name, stringWriter);

        return stringWriter;
    }

    public void setPackageName(final String packageName)
    {
        this.packageName = packageName;
    }

    public CharSequence getSource(final String name)
    {
        return sourceFileByName.get(name).toString();
    }

    public Map<String, CharSequence> getSources()
    {
        final Map<String, CharSequence> sources = new HashMap<>();
        for (final Map.Entry<String, StringWriter> entry : sourceFileByName.entrySet())
        {
            sources.put(entry.getKey(), entry.getValue().toString());
        }

        return sources;
    }
}
