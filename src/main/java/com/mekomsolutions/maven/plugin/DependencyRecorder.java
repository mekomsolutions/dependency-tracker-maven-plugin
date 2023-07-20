package com.mekomsolutions.maven.plugin;

import static com.mekomsolutions.maven.plugin.Constants.ARTIFACT_SUFFIX;
import static com.mekomsolutions.maven.plugin.Constants.FILENAME_SEPARATOR;
import static com.mekomsolutions.maven.plugin.Constants.OUTPUT_SEPARATOR;

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
 * Helper class that records and generates an artifact containing only declared dependencies along
 * with their hashes, the generated artifact is saved in the build directory
 */
public class DependencyRecorder {
	
	private MavenProject project;
	
	private File buildDirectory;
	
	private Log log;
	
	private DependencyRecorder(MavenProject project, File buildDirectory, Log log) {
		this.project = project;
		this.buildDirectory = buildDirectory;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyRecorder} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param buildDirectory the build directory where to save the generated artifact
	 * @param log {@link Log} instance
	 * @return DependencyRecorder instance
	 */
	protected static DependencyRecorder createInstance(MavenProject project, File buildDirectory, Log log) {
		return new DependencyRecorder(project, buildDirectory, log);
	}
	
	/**
	 * Records the declared dependencies
	 * 
	 * @throws Exception
	 */
	protected void record() throws IOException {
		log.info("Recording project dependencies");
		
		List<String> lines = prepareDependencyArtifact();
		
		log.debug("---------------------- Recorded Dependencies ----------------------");
		
		for (String line : lines) {
			log.debug(line);
		}
		
		log.debug("-------------------------------------------------------------------");
		
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
		
		log.info("Found " + artifacts.size() + " dependencies to record");
		
		List<String> lines = new ArrayList<>();
		for (Artifact a : artifacts) {
			log.debug("Generating sha1 for artifact -> " + a);
			
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
		log.info("Saving recorded dependency details");
		
		String id = project.getArtifactId();
		String version = project.getVersion();
		Utils.writeToFile(new File(buildDirectory, id + FILENAME_SEPARATOR + version + ARTIFACT_SUFFIX), lines);
	}
	
}
