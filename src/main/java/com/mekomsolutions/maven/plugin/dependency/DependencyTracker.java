package com.mekomsolutions.maven.plugin.dependency;

import static com.mekomsolutions.maven.plugin.dependency.Constants.ARTIFACT_SUFFIX;
import static com.mekomsolutions.maven.plugin.dependency.Constants.FILE_NAME_SEPARATOR;
import static com.mekomsolutions.maven.plugin.dependency.Constants.OUTPUT_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Helper class that captures and generates an artifact containing only declared dependencies along
 * with their hashes, the generated artifact is saved in the build directory
 */
public class DependencyTracker {
	
	private MavenProject project;
	
	private File buildDirectory;
	
	private Log log;
	
	private DependencyTracker(MavenProject project, File buildDirectory, Log log) {
		this.project = project;
		this.buildDirectory = buildDirectory;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyTracker} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param buildDirectory the build directory where to save the generated artifact
	 * @param log {@link Log} instance
	 * @return DependencyTracker instance
	 */
	protected static DependencyTracker createInstance(MavenProject project, File buildDirectory, Log log) {
		return new DependencyTracker(project, buildDirectory, log);
	}
	
	/**
	 * Tracks the declared dependencies
	 * 
	 * @throws Exception
	 */
	protected void track() throws IOException {
		log.info("Capturing project dependencies");
		
		List<String> lines = prepareDependencyArtifact();
		
		log.debug("---------------------- Tracked Dependencies ----------------------");
		
		for (String line : lines) {
			log.debug(line);
		}
		
		log.debug("------------------------------------------------------------------");
		
		saveDependencyArtifact(lines);
	}
	
	/**
	 * Prepares the dependency details
	 * 
	 * @return artifact contents
	 * @throws IOException
	 */
	protected List<String> prepareDependencyArtifact() throws IOException {
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		
		log.info("Found " + artifacts.size() + " dependencies to track");
		
		List<String> lines = new ArrayList<>();
		for (Artifact a : artifacts) {
			log.debug("Generating sha1 hash for artifact: " + a);
			
			String hash = DigestUtils.sha1Hex(Utils.readFile(a.getFile()));
			lines.add(a.getDependencyConflictId() + OUTPUT_SEPARATOR + hash);
		}
		
		return lines;
	}
	
	/**
	 * Saves the artifact
	 * 
	 * @param lines artifact contents
	 * @throws IOException
	 */
	protected void saveDependencyArtifact(List<String> lines) throws IOException {
		String id = project.getArtifactId();
		String version = project.getVersion();
		File artifactFile = new File(buildDirectory, id + FILE_NAME_SEPARATOR + version + ARTIFACT_SUFFIX);
		
		log.info("Saving dependency tracker artifact to " + artifactFile);
		
		Utils.writeToFile(artifactFile, lines);
	}
	
}
