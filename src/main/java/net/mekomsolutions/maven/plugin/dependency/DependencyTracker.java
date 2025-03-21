package net.mekomsolutions.maven.plugin.dependency;

import static java.util.stream.Collectors.toList;
import static net.mekomsolutions.maven.plugin.dependency.Constants.AGGREGATED_ARTIFACT_SUFFIX;
import static net.mekomsolutions.maven.plugin.dependency.Constants.ARTIFACT_SUFFIX;
import static net.mekomsolutions.maven.plugin.dependency.Constants.COMPARE_ARTIFACT_SUFFIX;
import static net.mekomsolutions.maven.plugin.dependency.Constants.OUTPUT_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Helper class that captures and generates an artifact containing only declared dependencies along
 * with their hashes, the generated artifact is saved in the build directory
 */
public class DependencyTracker {
	
	private MavenProject project;
	
	private MavenProjectHelper projectHelper;
	
	private ArtifactResolver artifactResolver;
	
	private MavenSession session;
	
	private String buildFileName;
	
	private File buildDirectory;
	
	private Log log;
	
	private DependencyTracker(MavenProject project, MavenProjectHelper projectHelper, MavenSession session,
	    ArtifactResolver artifactResolver, String buildFileName, File buildDirectory, Log log) {
		this.project = project;
		this.projectHelper = projectHelper;
		this.artifactResolver = artifactResolver;
		this.session = session;
		this.buildFileName = buildFileName;
		this.buildDirectory = buildDirectory;
		this.log = log;
	}
	
	/**
	 * Creates a {@link DependencyTracker} instance
	 * 
	 * @param project {@link MavenProject} instance
	 * @param projectHelper {@link MavenProjectHelper} instance
	 * @param session {@link MavenSession} instance
	 * @param buildFileName the name of the project build file
	 * @param buildDir the build directory where to save the generated artifact
	 * @param log {@link Log} instance
	 * @return DependencyTracker instance
	 */
	protected static DependencyTracker createInstance(MavenProject project, MavenProjectHelper projectHelper,
	        MavenSession session, ArtifactResolver artifactResolver, String buildFileName, File buildDir, Log log) {
		return new DependencyTracker(project, projectHelper, session, artifactResolver, buildFileName, buildDir, log);
	}
	
	/**
	 * Tracks the declared dependencies
	 * 
	 * @return The generated dependency report file
	 * @throws Exception
	 */
	protected File track() throws IOException {
		log.info("Capturing project dependencies");
		
		List<String> lines = prepareDependencyArtifact();
		
		log.debug("---------------------- Tracked Dependencies ----------------------");
		
		lines.forEach(line -> log.debug(line));
		
		log.debug("------------------------------------------------------------------");
		
		if ("pom".equals(project.getPackaging()) && !buildDirectory.exists()) {
			//It's common for projects with pom packaging to have no generated artifacts so the build directory 
			//won't exist therefore we need to create it if necessary.
			log.debug("Creating build directory: " + buildDirectory.getAbsolutePath());
			buildDirectory.mkdir();
		}
		
		return saveDependencyArtifact(lines);
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
			
			keyAndHash.put(a.getId(), DigestUtils.sha1Hex(Utils.readFile(a.getFile())));
		}
		
		return keyAndHash.entrySet().stream().map(e -> e.getKey() + OUTPUT_SEPARATOR + e.getValue()).collect(toList());
	}
	
	/**
	 * Saves the dependency artifact to the build directory.
	 * 
	 * @param lines artifact contents
	 * @return generated dependency artifact file
	 * @throws IOException
	 */
	protected File saveDependencyArtifact(List<String> lines) throws IOException {
		File artifactFile = Utils.instantiateFile(buildDirectory, buildFileName + ARTIFACT_SUFFIX);
		
		log.info("Saving dependency tracker artifact to " + artifactFile);
		
		Utils.writeToFile(artifactFile, lines);
		
		log.info("Attaching dependency tracker artifact");
		
		projectHelper.attachArtifact(project, Constants.EXT, Constants.CLASSIFIER, artifactFile);
		return artifactFile;
	}
	
	/**
	 * Fetches the dependency remote from the remote repository defined in the distribution management
	 * section of a project's POM file.
	 *
	 * @return the downloaded dependency report file.
	 * @throws Exception
	 */
	protected File getRemoteDependencyReport() {
		org.eclipse.aether.artifact.Artifact ea = new DefaultArtifact(project.getGroupId(), project.getArtifactId(),
		        Constants.CLASSIFIER, Constants.EXT, project.getVersion());
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
	protected Integer compare(File buildReport, File remoteReport) throws Exception {
		log.info("Comparing project dependency reports");
		int result = -1;
		if (remoteReport != null) {
			byte[] buildContent = Utils.readFile(buildReport);
			byte[] remoteContent = Utils.readFile(remoteReport);
			result = Arrays.equals(buildContent, remoteContent) ? 0 : 1;
		}
		
		saveComparisonArtifact(result);
		return result;
	}
	
	/**
	 * Saves the artifact containing the comparison result to the build directory.
	 *
	 * @param result the result to write to the artifact file.
	 * @throws IOException
	 */
	protected void saveComparisonArtifact(Integer result) throws IOException {
		File artifactFile = Utils.instantiateFile(buildDirectory, buildFileName + COMPARE_ARTIFACT_SUFFIX);
		
		log.info("Saving dependency comparison result artifact to " + artifactFile);
		
		Utils.writeBytesToFile(artifactFile, result.toString().getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Aggregates the specified list of results into a single result.
	 * 
	 * @param results list of results to aggregate
	 * @return the aggregated result
	 */
	protected Integer aggregateDependencyReports(List<Integer> results) {
		log.info("Aggregating " + results.size() + " dependency comparison results");
		
		Integer result;
		Set<Integer> uniqueResults = results.stream().collect(Collectors.toSet());
		if (uniqueResults.size() == 1 && uniqueResults.iterator().next() == 0) {
			result = 0;
		} else if (uniqueResults.contains(1)) {
			result = 1;
		} else {
			result = -1;
		}
		
		return result;
	}
	
	/**
	 * Saves the artifact containing the aggregated comparison result to the parent project's build
	 * directory.
	 *
	 * @param parentBuildDir the build directory of the parent project
	 * @param parentBuildFileName the build file name of the parent project
	 * @param result the result to write to the artifact file.
	 * @throws IOException
	 */
	protected void saveAggregatedArtifact(File parentBuildDir, String parentBuildFileName, Integer result)
	        throws IOException {
		File artifactFile = Utils.instantiateFile(parentBuildDir, parentBuildFileName + AGGREGATED_ARTIFACT_SUFFIX);
		
		log.info("Saving aggregated dependency comparison result artifact to " + artifactFile);
		
		Utils.writeBytesToFile(artifactFile, result.toString().getBytes(StandardCharsets.UTF_8));
	}
	
}
