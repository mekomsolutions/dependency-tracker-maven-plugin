package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.impl.ArtifactResolver;

/**
 * Compares project dependencies with those of a previous excluding transitive ones and writes them
 * to a file as an artifact in the build directory.
 */
@Mojo(name = "compare", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class DependencyComparisonMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File buildDirectory;
	
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String buildFileName;
	
	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;
	
	@Component
	protected ArtifactResolver artifactResolver;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			DependencyComparator.createInstance(project, artifactResolver, session, buildFileName, buildDirectory, getLog())
			        .compare(null, null);
		}
		catch (Exception e) {
			throw new MojoFailureException("An error occurred while comparing dependency reports", e);
		}
	}
	
}
