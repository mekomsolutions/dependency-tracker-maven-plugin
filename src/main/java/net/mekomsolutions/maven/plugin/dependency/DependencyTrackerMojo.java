package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Tracks project dependency details excluding transitive dependencies and writes them to a file as
 * an artifact in the build directory.
 */
@Mojo(name = "track", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class DependencyTrackerMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File buildDirectory;
	
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String buildFileName;
	
	@Component
	private MavenProjectHelper projectHelper;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			DependencyTracker.createInstance(project, projectHelper, buildFileName, buildDirectory, getLog()).track();
		}
		catch (IOException e) {
			throw new MojoFailureException("An error occurred while tracking dependencies", e);
		}
	}
	
}
