using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;
using Microsoft.VisualStudio.TextTemplating.VSHost;

namespace Adaptive.SimpleBinaryEncoding.CodeGen
{
    // Note: the class name is used as the name of the Custom Tool from the end-user's perspective.
    [ComVisible(true)]
    [Guid("5C42F48F-2747-4166-9745-92B594342C99")]
    [ProvideObject(typeof(SbeSingleFileGenerator))]
    [ProvideCodeGeneratorExtension("SbeSingleFileGenerator", ".sbe.xml")]
    [CodeGeneratorRegistration(typeof(SbeSingleFileGenerator), "SbeSingleFileGenerator", VsContextGuids.VsContextGuidVcsProject, GeneratesDesignTimeSource = true)]
    public class SbeSingleFileGenerator : CustomToolBase
    {
        protected override string DefaultExtension()
        {
            return ".cs";
        }

        protected override byte[] Generate(string inputFilePath, string inputFileContents, string defaultNamespace, IVsGeneratorProgress progressCallback)
        {
            var localDir = Path.GetDirectoryName(typeof(SbeSingleFileGenerator).Assembly.Location);
            var outputDir = Path.Combine(Path.GetTempPath(), Path.GetRandomFileName());
            Directory.CreateDirectory(outputDir);

            var process = new Process
                {
                    StartInfo =
                        {
                            FileName = "java.exe",
                            Arguments = String.Format("-Dsbe.target.language=csharp -Dsbe.target.namespace={0} -Dsbe.output.dir=\"{1}\" -jar sbe.jar \"{2}\"", defaultNamespace, outputDir, inputFilePath),
                            UseShellExecute = false,
                            CreateNoWindow = true,
                            WorkingDirectory = localDir,
                            RedirectStandardInput = true,
                            RedirectStandardOutput = true,
                            RedirectStandardError = true,
                            StandardOutputEncoding = Encoding.UTF8,
                        },
                };

            process.Start();
            try
            {
                process.StandardInput.Write(inputFileContents);

                var stdOut = process.StandardOutput.ReadToEnd();
                var stdErr = process.StandardError.ReadToEnd();

                using (var outputStream = new MemoryStream())
                using (var outWriter = new StreamWriter(outputStream))
                {
                    outWriter.WriteLine("using System;");
                    outWriter.WriteLine("using Adaptive.SimpleBinaryEncoding;");
                    foreach (var outputFile in Directory.GetFiles(outputDir, "*.cs"))
                    {
                        outWriter.WriteLine("//");
                        outWriter.WriteLine("// {0}", Path.GetFileName(outputFile));
                        outWriter.WriteLine("//");

                        outWriter.Write(
                            File.ReadAllText(outputFile)
                                .Replace("using System;", String.Empty)
                                .Replace("using Adaptive.SimpleBinaryEncoding;", String.Empty)
                        );
                        
                        outWriter.WriteLine();
                        outWriter.WriteLine();
                        outWriter.Flush();
                    }
                    return outputStream.ToArray();
                }

            }
            finally
            {
                process.Close();
                Directory.Delete(outputDir, true);
            }
        }
    }
}
