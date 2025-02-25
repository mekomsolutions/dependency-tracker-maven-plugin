package net.mekomsolutions.maven.plugin.dependency;

import static net.mekomsolutions.maven.plugin.dependency.Constants.COMPARE_ARTIFACT_SUFFIX;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.impl.MetadataResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * TODO Add javadocs
 */
public class DependencyComparator {
	
	private MavenProject project;
	
	private ArtifactResolver artifactResolver;
	
	private MavenSession session;
	
	private String buildFileName;
	
	private File buildDirectory;
	
	private Log log;
	
	private DependencyComparator(MavenProject project, ArtifactResolver artifactResolver, MavenSession session,
	    String buildFileName, File buildDirectory, Log log) {
		this.project = project;
		this.artifactResolver = artifactResolver;
		this.session = session;
		this.buildFileName = buildFileName;
		this.buildDirectory = buildDirectory;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyComparator} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param project {@link MetadataResolver} instance
	 * @param project {@link MavenSession} instance
	 * @param buildFileName the name of the project build file
	 * @param buildDirectory the build directory where to save the generated artifact
	 * @param log {@link Log} instance
	 * @return DependencyTracker instance
	 */
	protected static DependencyComparator createInstance(MavenProject project, ArtifactResolver artifactResolver,
	        MavenSession session, String buildFileName, File buildDirectory, Log log) {
		return new DependencyComparator(project, artifactResolver, session, buildFileName, buildDirectory, log);
	}
	
	/**
	 * Compares the specified dependency artifacts reports and generates a report artifact containing
	 * the comparison result. If the reports match, the result of the comparison artifact is 0, if
	 * changes are detected the result is 1 otherwise -1, where -1 implies there was no existing
	 * previous report that was found in the remote repository, this typically happens upon the first
	 * build of the project.
	 * 
	 * @param buildReport report file generated during the current build.
	 * @param remoteReport the report file from the remote repo
	 * @throws Exception
	 */
	protected void compare(File buildReport, File remoteReport) throws Exception {
		log.info("Comparing project dependency reports");
		int result = -1;
		if (remoteReport != null) {
			byte[] buildContent = Utils.readFile(buildReport);
			byte[] remoteContent = Utils.readFile(remoteReport);
			result = Arrays.equals(buildContent, remoteContent) ? 0 : 1;
		}
		
		saveComparisonArtifact(result);
	}
	
	protected File getRemoteDependencyReport() throws Exception {
		org.eclipse.aether.artifact.Artifact ea = new DefaultArtifact(project.getGroupId(), project.getArtifactId(),
		        Constants.CLASSIFIER, Constants.EXTENSION, project.getVersion());
		ArtifactRepository remoteRepo = project.getDistributionManagementArtifactRepository();
		RemoteRepository remoteAetherRepo = RepositoryUtils.toRepo(remoteRepo);
		ArtifactRequest artifactReq = new ArtifactRequest(ea, Collections.singletonList(remoteAetherRepo), null);
		try {
			ArtifactResult artifactRes = artifactResolver.resolveArtifact(session.getRepositorySession(), artifactReq);
			return artifactRes.getArtifact().getFile();
		}
		catch (ArtifactResolutionException e) {
			log.info("No remote dependency report found");
			return null;
		}
	}
	
	/**
	 * Saves the artifact containing the comparison result.
	 * 
	 * @param result the result to write to the artifact file.
	 * @throws IOException
	 */
	protected void saveComparisonArtifact(Integer result) throws IOException {
		File artifactFile = Utils.instantiateFile(buildDirectory, buildFileName + COMPARE_ARTIFACT_SUFFIX);
		
		log.info("Saving dependency comparison result artifact to " + artifactFile);
		
		Utils.writeBytesToFile(artifactFile, result.toString().getBytes(StandardCharsets.UTF_8));
	}
	
}
