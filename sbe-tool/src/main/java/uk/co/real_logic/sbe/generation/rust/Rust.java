/*
 * Copyright 2013-2019 Real Logic Ltd.
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
package uk.co.real_logic.sbe.generation.rust;

import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.TargetCodeGenerator;
import uk.co.real_logic.sbe.ir.Ir;

/**
 * This class is present largely to enable the dynamic-loading style pattern
 * of specifying a TargetCodeGenerator Java class name rather than the language
 * name.
 */
public class Rust implements TargetCodeGenerator
{
    public CodeGenerator newInstance(final Ir ir, final String outputDir)
    {
        return defaultRustGenerator(ir, outputDir);
    }

    public static CodeGenerator defaultRustGenerator(final Ir ir, final String outputDir)
    {
        return new RustGenerator(ir, new RustFlatFileOutputManager(outputDir, ir.applicableNamespace()));
    }
}
