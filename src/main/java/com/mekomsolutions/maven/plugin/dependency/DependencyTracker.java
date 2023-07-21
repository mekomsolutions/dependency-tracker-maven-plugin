package com.mekomsolutions.maven.plugin.dependency;

import static com.mekomsolutions.maven.plugin.dependency.Constants.ARTIFACT_SUFFIX;
import static com.mekomsolutions.maven.plugin.dependency.Constants.OUTPUT_SEPARATOR;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	
	private String buildFileName;
	
	private File buildDirectory;
	
	private Log log;
	
	private DependencyTracker(MavenProject project, String buildFileName, File buildDirectory, Log log) {
		this.project = project;
		this.buildFileName = buildFileName;
		this.buildDirectory = buildDirectory;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyTracker} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param buildFileName the name of the project build file
	 * @param buildDirectory the build directory where to save the generated artifact
	 * @param log {@link Log} instance
	 * @return DependencyTracker instance
	 */
	protected static DependencyTracker createInstance(MavenProject project, String buildFileName, File buildDirectory,
	        Log log) {
		
		return new DependencyTracker(project, buildFileName, buildDirectory, log);
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
		
		lines.forEach(line -> log.debug(line));
		
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
		
		Map<String, String> keyAndHash = new TreeMap<>();
		for (Artifact a : artifacts) {
			log.debug("Generating sha1 hash for artifact: " + a);
			
			String hash = DigestUtils.sha1Hex(Utils.readFile(a.getFile()));
			keyAndHash.put(a.getDependencyConflictId(), hash);
		}
		
		return keyAndHash.entrySet().stream().map(e -> e.getKey() + OUTPUT_SEPARATOR + e.getValue()).collect(toList());
	}
	
	/**
	 * Saves the artifact
	 * 
	 * @param lines artifact contents
	 * @throws IOException
	 */
	protected void saveDependencyArtifact(List<String> lines) throws IOException {
		File artifactFile = new File(buildDirectory, buildFileName + ARTIFACT_SUFFIX);
		
		log.info("Saving dependency tracker artifact to " + artifactFile);
		
		Utils.writeToFile(artifactFile, lines);
	}
	
}
