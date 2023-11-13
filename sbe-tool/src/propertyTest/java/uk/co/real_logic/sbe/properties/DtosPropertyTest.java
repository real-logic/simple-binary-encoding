/*
 * Copyright 2013-2023 Real Logic Limited.
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

package uk.co.real_logic.sbe.properties;

import net.jqwik.api.*;
import uk.co.real_logic.sbe.generation.cpp.CppDtoGenerator;
import uk.co.real_logic.sbe.generation.cpp.CppGenerator;
import uk.co.real_logic.sbe.generation.cpp.NamespaceOutputManager;
import uk.co.real_logic.sbe.generation.csharp.CSharpDtoGenerator;
import uk.co.real_logic.sbe.generation.csharp.CSharpGenerator;
import uk.co.real_logic.sbe.generation.csharp.CSharpNamespaceOutputManager;
import uk.co.real_logic.sbe.properties.arbitraries.SbeArbitraries;
import org.agrona.IoUtil;
import org.agrona.io.DirectBufferInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@SuppressWarnings("ReadWriteStringCanBeUsed")
public class DtosPropertyTest
{
    private static final String DOTNET_EXECUTABLE = System.getProperty("sbe.tests.dotnet.executable", "dotnet");
    private static final String SBE_DLL =
        System.getProperty("sbe.dll", "csharp/sbe-dll/bin/Release/netstandard2.0/SBE.dll");
    private static final String CPP_EXECUTABLE = System.getProperty("sbe.tests.cpp.executable", "g++");
    private static final boolean KEEP_DIR_ON_FAILURE = Boolean.parseBoolean(
        System.getProperty("sbe.tests.keep.dir.on.failure", "true"));

    @Property
    void csharpDtoEncodeShouldBeTheInverseOfDtoDecode(
        @ForAll("encodedMessage") final SbeArbitraries.EncodedMessage encodedMessage
    ) throws IOException, InterruptedException
    {
        final Path tempDir = Files.createTempDirectory("sbe-csharp-dto-test");
        boolean success = false;

        try
        {
            final CSharpNamespaceOutputManager outputManager = new CSharpNamespaceOutputManager(
                tempDir.toString(),
                "SbePropertyTest"
            );

            try
            {
                new CSharpGenerator(encodedMessage.ir(), outputManager)
                    .generate();
                new CSharpDtoGenerator(encodedMessage.ir(), outputManager)
                    .generate();
            }
            catch (final Exception generationException)
            {
                throw new AssertionError(
                    "Code generation failed.\n\n" +
                        "DIR:" + tempDir + "\n\n" +
                        "SCHEMA:\n" + encodedMessage.schema(),
                    generationException);
            }

            copyResourceToFile("/CSharpDtosPropertyTest/SbePropertyTest.csproj", tempDir);
            copyResourceToFile("/CSharpDtosPropertyTest/Program.cs", tempDir);

            writeInputFile(encodedMessage, tempDir);

            execute(encodedMessage.schema(), tempDir, "test",
                DOTNET_EXECUTABLE, "run",
                "--property:SBE_DLL=" + SBE_DLL,
                "--", "input.dat");

            final byte[] inputBytes = new byte[encodedMessage.length()];
            encodedMessage.buffer().getBytes(0, inputBytes);
            final byte[] outputBytes = Files.readAllBytes(tempDir.resolve("output.dat"));
            if (!Arrays.equals(inputBytes, outputBytes))
            {
                throw new AssertionError(
                    "Input and output files differ\n\n" +
                        "DIR:" + tempDir + "\n\n" +
                        "SCHEMA:\n" + encodedMessage.schema());
            }
            success = true;
        }
        finally
        {
            if (!KEEP_DIR_ON_FAILURE || success)
            {
                IoUtil.delete(tempDir.toFile(), true);
            }
            else
            {
                Files.write(
                    tempDir.resolve("schema.xml"),
                    encodedMessage.schema().getBytes(StandardCharsets.UTF_8));

                Files.write(
                    tempDir.resolve("encoding.log"),
                    encodedMessage.encodingLog().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Property(shrinking = ShrinkingMode.OFF)
    void cppDtoEncodeShouldBeTheInverseOfDtoDecode(
        @ForAll("encodedMessage") final SbeArbitraries.EncodedMessage encodedMessage
    ) throws IOException, InterruptedException
    {
        final Path tempDir = Files.createTempDirectory("sbe-cpp-dto-test");
        boolean success = false;

        try
        {
            final NamespaceOutputManager outputManager = new NamespaceOutputManager(
                tempDir.toString(),
                "sbe_property_test"
            );

            try
            {
                new CppGenerator(encodedMessage.ir(), true, outputManager)
                    .generate();
                new CppDtoGenerator(encodedMessage.ir(), outputManager)
                    .generate();
            }
            catch (final Exception generationException)
            {
                throw new AssertionError(
                    "Code generation failed.\n\nSCHEMA:\n" + encodedMessage.schema(),
                    generationException);
            }

            copyResourceToFile("/CppDtosPropertyTest/main.cpp", tempDir);

            writeInputFile(encodedMessage, tempDir);

            execute(encodedMessage.schema(), tempDir, "compile",
                CPP_EXECUTABLE, "--std", "c++17", "-o", "round-trip-test", "main.cpp");

            execute(encodedMessage.schema(), tempDir, "test",
                tempDir.resolve("round-trip-test").toString(), "input.dat");

            final byte[] inputBytes = new byte[encodedMessage.length()];
            encodedMessage.buffer().getBytes(0, inputBytes);
            final byte[] outputBytes = Files.readAllBytes(tempDir.resolve("output.dat"));
            if (!Arrays.equals(inputBytes, outputBytes))
            {
                throw new AssertionError(
                    "Input and output files differ\n\n" +
                        "SCHEMA:\n" + encodedMessage.schema());
            }
            success = true;
        }
        finally
        {
            if (!KEEP_DIR_ON_FAILURE || success)
            {
                IoUtil.delete(tempDir.toFile(), true);
            }
            else
            {
                Files.write(
                    tempDir.resolve("schema.xml"),
                    encodedMessage.schema().getBytes(StandardCharsets.UTF_8));

                Files.write(
                    tempDir.resolve("encoding.log"),
                    encodedMessage.encodingLog().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static void writeInputFile(
        final SbeArbitraries.EncodedMessage encodedMessage,
        final Path tempDir) throws IOException
    {
        try (
            DirectBufferInputStream inputStream = new DirectBufferInputStream(
                encodedMessage.buffer(),
                0,
                encodedMessage.length()
            );
            OutputStream outputStream = Files.newOutputStream(tempDir.resolve("input.dat")))
        {
            final byte[] buffer = new byte[2048];
            int read;
            while ((read = inputStream.read(buffer, 0, buffer.length)) >= 0)
            {
                outputStream.write(buffer, 0, read);
            }
        }
    }

    private static void execute(
        final String schema,
        final Path tempDir,
        final String name,
        final String... args) throws InterruptedException, IOException
    {
        final Path stdout = tempDir.resolve(name + "_stdout.txt");
        final Path stderr = tempDir.resolve(name + "_stderr.txt");
        final ProcessBuilder processBuilder = new ProcessBuilder(args)
            .directory(tempDir.toFile())
            .redirectOutput(stdout.toFile())
            .redirectError(stderr.toFile());

        final Process process = processBuilder.start();

        if (0 != process.waitFor())
        {
            throw new AssertionError(
                "Process failed with exit code: " + process.exitValue() + "\n\n" +
                    "DIR:" + tempDir + "\n\n" +
                    "STDOUT:\n" + new String(Files.readAllBytes(stdout)) + "\n\n" +
                    "STDERR:\n" + new String(Files.readAllBytes(stderr)) + "\n\n" +
                    "SCHEMA:\n" + schema);
        }

        final byte[] errorBytes = Files.readAllBytes(stderr);
        if (errorBytes.length != 0)
        {
            throw new AssertionError(
                "Process wrote to stderr.\n\n" +
                    "DIR:" + tempDir + "\n\n" +
                    "STDOUT:\n" + new String(Files.readAllBytes(stdout)) + "\n\n" +
                    "STDERR:\n" + new String(errorBytes) + "\n\n" +
                    "SCHEMA:\n" + schema + "\n\n"
            );
        }
    }

    @Provide
    Arbitrary<SbeArbitraries.EncodedMessage> encodedMessage()
    {
        final SbeArbitraries.CharGenerationMode mode =
            SbeArbitraries.CharGenerationMode.JSON_PRINTER_COMPATIBLE;
        return SbeArbitraries.encodedMessage(mode);
    }

    private static void copyResourceToFile(
        final String resourcePath,
        final Path outputDir)
    {
        try (InputStream inputStream = DtosPropertyTest.class.getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IOException("Resource not found: " + resourcePath);
            }

            final int resourceNameIndex = resourcePath.lastIndexOf('/') + 1;
            final String resourceName = resourcePath.substring(resourceNameIndex);
            final Path outputFilePath = outputDir.resolve(resourceName);
            Files.copy(inputStream, outputFilePath);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
