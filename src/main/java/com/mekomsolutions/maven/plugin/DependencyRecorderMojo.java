package com.mekomsolutions.maven.plugin;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Goal which records project dependency details excluding transitive dependencies and writes them
 * to a file as an artifact located in the build directory.
 */
@Mojo(name = "record", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class DependencyRecorderMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			DependencyRecorder.createInstance(project, getLog()).record();
		}
		catch (IOException e) {
			throw new MojoFailureException("An error occurred while recording dependencies", e);
		}
	}
	
}
