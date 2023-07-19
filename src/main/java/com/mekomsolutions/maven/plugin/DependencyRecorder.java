package com.mekomsolutions.maven.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Helper class that records and generates an artifact containing only declared dependencies along
 * with their hashes, the generated artifact is saved in the build directory
 */
public class DependencyRecorder {
	
	private MavenProject project;
	
	private Log log;
	
	private DependencyRecorder(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyRecorder} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param log {@link Log} instance
	 * @return DependencyRecorder instance
	 */
	protected static DependencyRecorder createInstance(MavenProject project, Log log) {
		return new DependencyRecorder(project, log);
	}
	
	/**
	 * Records the declared dependencies
	 */
	protected void record() throws Exception {
		log.info("Recording project dependencies");
		
		Properties record = prepareRecordArtifact();
		
		log.debug("---------------------- Recorded Dependencies ----------------------");
		
		for (Map.Entry entry : record.entrySet()) {
			log.debug(entry.getKey() + "=" + entry.getValue());
		}
		
		log.debug("-------------------------------------------------------------------");
		
		saveRecordArtifact(record);
	}
	
	/**
	 * Prepares the dependency record artifact
	 * 
	 * @return record as properties
	 */
	protected Properties prepareRecordArtifact() throws Exception {
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		
		log.info("Found " + artifacts.size() + " dependencies to record");
		
		Properties record = new Properties();
		for (Artifact a : artifacts) {
			log.debug("Generating sha1 for artifact -> " + a);
			
			byte[] artifactData = Utils.readFile(a.getFile());
			byte[] hashData = MessageDigest.getInstance("SHA-1").digest(artifactData);
			record.setProperty(a.getDependencyConflictId(), new String(Base64.getEncoder().encode(hashData), UTF_8));
		}
		
		return record;
	}
	
	/**
	 * Saves the record artifact
	 * 
	 * @param record the record artifact to save
	 */
	protected void saveRecordArtifact(Properties record) {
		log.info("Saving the artifact containing recorded dependencies");
		//TODO save
	}
	
}
