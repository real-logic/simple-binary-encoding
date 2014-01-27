package uk.co.real_logic.sbe.maven.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import uk.co.real_logic.sbe.SbeTool;
import uk.co.real_logic.sbe.ir.Ir;
import uk.co.real_logic.sbe.ir.IrDecoder;
import uk.co.real_logic.sbe.ir.IrEncoder;
import uk.co.real_logic.sbe.xml.IrGenerator;

@Mojo(name = "run", defaultPhase = LifecyclePhase.CLEAN)
public class SbeMavenPluginMojo extends AbstractMojo {

	@Component
	private MavenProject project;
	
	@Parameter(alias = SbeTool.TARGET_NAMESPACE, required = false)
	private String targetNamespace;

	@Parameter(alias = SbeTool.SHOULD_GENERATE, required = false)
	private boolean shouldGenerate = true;

	@Parameter(alias = SbeTool.OUTPUT_DIR, required = false)
	private String outputDir = "target/generated-sources/sbe";

	@Parameter(alias = SbeTool.TARGET_LANGUAGE, required = false)
	private String targetLanguage = "Java";

	@Parameter(alias = SbeTool.ENCODED_IR_FILENAME, required = false)
	private String encodedIrFilename;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Simple Binary Encoding Maven Plugin.");
		getLog().info("resources: " + resources);
		try {
			executeCore();
		} catch (Exception e) {
			throw new MojoExecutionException("", e);
		}

	}

	private void executeCore() throws Exception {
		getLog().info("SBE Plugin base directory: " + project.getBasedir());
		final File absoluteOutput = new File(project.getBasedir(), outputDir);
		getLog().info("SBE Plugin output directory: " + absoluteOutput);
		
		
		for (String resourceName : resources) {
			File resourceFile = new File(project.getBasedir(), resourceName);
			Ir ir = null;
			if (resourceFile.getName().endsWith(".xml")) {
				ir = new IrGenerator().generate(SbeTool.parseSchema(resourceFile.getAbsolutePath()), targetNamespace);
			} else if (resourceFile.getName().endsWith(".sbeir")) {
				ir = new IrDecoder(resourceFile.getAbsolutePath()).decode();
			} else {
				getLog().info("File format not supported.");
			}

			if (shouldGenerate) {
				SbeTool.generate(ir, absoluteOutput.getAbsolutePath(), targetLanguage);
			}

			if (encodedIrFilename != null) {
				final File fullPath = new File(absoluteOutput, encodedIrFilename);
				try (IrEncoder irEncoder = new IrEncoder(fullPath.getAbsolutePath(), ir)) {
					irEncoder.encode();
				}
			}
		}
		
		project.addCompileSourceRoot(outputDir);
	}

	@Parameter(alias = "resources", required = true)
	private List<String> resources;

}
