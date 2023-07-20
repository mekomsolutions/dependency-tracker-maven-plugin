package com.mekomsolutions.maven.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

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
	 * @throws Exception
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
	 * @throws IOException
	 */
	protected void saveRecordArtifact(Properties record) throws IOException {
		log.info("Saving the artifact containing recorded dependencies");
		
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		record.store(o, "");
		FileUtils.fileWrite(new File(buildDirectory, "dependency-record.txt"), UTF_8.name(), o.toString(UTF_8.name()));
	}
	
}
