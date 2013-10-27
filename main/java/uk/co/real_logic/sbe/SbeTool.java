/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
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
package uk.co.real_logic.sbe;

import uk.co.real_logic.sbe.generation.CodeGenerator;
import uk.co.real_logic.sbe.generation.TargetCodeGenerator;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * A tool for running the SBE parser, validator, and code generator.
 * <p/>
 * Usage:
 * <code>
 *     <pre>
 *     $ java -jar sbe.jar &lt;filename.xml&gt;
 *     $ java -jar sbe.jar -Doption=value &lt;filename.xml&gt;
 *     </pre>
 * </code>
 * <p/>
 * Properties
 * <ul>
 *     <li><code>sbe.validation.xsd</code>: Use XSD to validate or not.</li>
 *     <li><code>sbe.generate</code>: Generate or not.</li>
 *     <li><code>sbe.target.language</code>: Target language for code generation, defaults to Java.</li>
 *     <li><code>sbe.output.dir</code>: Target directory for code generation, defaults to current directory.</li>
 * </ul>
 */
public class SbeTool
{
    /** Boolean system property to control throwing exceptions on all errors */
    public static final String VALIDATION_STOP_ON_ERROR = "sbe.validation.stop.on.error";

    /** Boolean system property to control whether to consider warnings fatal and treat them as errors */
    public static final String VALIDATION_WARNINGS_FATAL = "sbe.validation.warnings.fatal";

    /** System property to hold XSD to validate message specification against */
    public static final String VALIDATION_XSD = "sbe.validation.xsd";

    /** Boolean system property to control suppressing output on all errors and warnings */
    public static final String VALIDATION_SUPPRESS_OUTPUT = "sbe.validation.suppress.output";

    /** Target language for generated code. */
    public static final String TARGET_LANGUAGE = "sbe.target.language";

    /** Output directory for generated code */
    public static final String OUTPUT_DIR = "sbe.output.dir";

    /**
     * Main entry point for the SBE Tool.
     *
     * @param args command line arguments. A single filename is expected.
     * @throws Exception if an error occurs during process of the message schema.
     */
    public static void main(final String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.format("Usage: %s <filename>\n", SbeTool.class.getName());
        }

        final String messageSchemaFileName = args[0];
        final String outputDirName = System.getProperty(OUTPUT_DIR, ".");
        final String targetCodeGenerator = System.getProperty(TARGET_LANGUAGE, "Java");

        generate(messageSchemaFileName, outputDirName, targetCodeGenerator);
    }

    /**
     * Generate SBE encoding and decoding stubs for a target language.
     *
     * @param messageSchemaFileName file containing encoding specification for the messages.
     * @param outputDirName directory into which code will be generated.
     * @param targetLanguage for the generated code.
     * @throws Exception if an error occurs while generating the code.
     */
    public static void generate(final String messageSchemaFileName,
                                final String outputDirName,
                                final String targetLanguage)
        throws Exception
    {
        final TargetCodeGenerator targetCodeGenerator = TargetCodeGenerator.get(targetLanguage);

        try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(messageSchemaFileName)))
        {
            final MessageSchema schema = XmlSchemaParser.parse(in);
            final IntermediateRepresentation ir = new IrGenerator().generate(schema);
            final CodeGenerator codeGenerator = targetCodeGenerator.newInstance(ir, outputDirName);

            codeGenerator.generate();
        }
    }
}
