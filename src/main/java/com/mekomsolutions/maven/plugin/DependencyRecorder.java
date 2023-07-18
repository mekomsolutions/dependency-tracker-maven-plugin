package com.mekomsolutions.maven.plugin;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Helper class that records and generates an artifact containing recorded dependencies along with
 * their hashes, the file us saved in the build directory
 */
public class DependencyRecorder {
	
	private MavenProject project;
	
	private Log log;
	
	private DependencyRecorder(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}
	
	protected static DependencyRecorder createInstance(MavenProject project, Log log) {
		return new DependencyRecorder(project, log);
	}
	
	protected void record() {
		log.info("Recording project dependencies");
		
		Properties record = prepareRecordArtifact();
		
		log.debug("---------------------- Recorded Dependencies ----------------------");
		
		for (Map.Entry entry : record.entrySet()) {
			log.debug(entry.getKey() + "=" + entry.getValue());
		}
		
		log.debug("-------------------------------------------------------------------");
		
		saveRecordArtifact(record);
	}
	
	protected Properties prepareRecordArtifact() {
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		Properties record = new Properties();
		for (Artifact artifact : artifacts) {
			//TODO
		}
		
		return record;
	}
	
	protected void saveRecordArtifact(Properties artifact) {
		log.info("Saving the artifact of recorded dependencies");
		//TODO save
	}
	
}
