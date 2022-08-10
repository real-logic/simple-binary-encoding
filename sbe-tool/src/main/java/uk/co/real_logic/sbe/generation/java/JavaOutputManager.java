/*
 * Copyright 2013-2022 Real Logic Limited.
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
package uk.co.real_logic.sbe.generation.java;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import org.agrona.collections.Object2NullableObjectHashMap;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.generation.PackageOutputManager;
import uk.co.real_logic.sbe.generation.MultiPackageOutputManager;

/**
 * Implementation of {@link MultiPackageOutputManager} for Java.
 */
public class JavaOutputManager implements MultiPackageOutputManager
{
    private final String baseDirName;
    private final PackageOutputManager basePackageOutputManager;
    private PackageOutputManager actingPackageOutputManager;
    private final Object2ObjectHashMap<String, PackageOutputManager> outputManagerCache
        = new Object2NullableObjectHashMap<>();

    /**
     * Constructor.
     * @param baseDirName the target directory
     * @param packageName the initial package name
     */
    public JavaOutputManager(final String baseDirName, final String packageName)
    {
        basePackageOutputManager = new PackageOutputManager(baseDirName, packageName);
        actingPackageOutputManager = basePackageOutputManager;
        this.baseDirName = baseDirName;
    }

    @Override
    public void setPackageName(final String packageName)
    {
        actingPackageOutputManager = outputManagerCache.get(packageName);
        if (actingPackageOutputManager == null)
        {
            actingPackageOutputManager = new PackageOutputManager(baseDirName, packageName);
            outputManagerCache.put(packageName, actingPackageOutputManager);
        }
    }

    private void resetPackage()
    {
        actingPackageOutputManager = basePackageOutputManager;
    }

    @Override
    public Writer createOutput(final String name) throws IOException
    {
        return new FilterWriter(actingPackageOutputManager.createOutput(name))
        {
            public void close() throws IOException
            {
                super.close();
                resetPackage();
            }
        };
    }
}
