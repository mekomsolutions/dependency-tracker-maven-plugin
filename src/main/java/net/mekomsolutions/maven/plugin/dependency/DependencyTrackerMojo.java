package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
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
import org.eclipse.aether.impl.ArtifactResolver;

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
	
	@Parameter(property = "compare", defaultValue = "false")
	private Boolean compare;
	
	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;
	
	@Component
	private MavenProjectHelper projectHelper;
	
	@Component
	protected ArtifactResolver artifactResolver;
	
	private static File parentBuildDir;
	
	private static String parentBuildFileName;
	
	private static List<Integer> comparisonResults;
	
	private static Integer moduleCount;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (compare && moduleCount == null) {
				moduleCount = project.getModules().size();
				if (moduleCount > 0) {
					parentBuildDir = buildDirectory;
					parentBuildFileName = buildFileName;
					comparisonResults = new ArrayList<>(project.getModules().size() + 1);
				}
			}
			
			DependencyTracker t = DependencyTracker.createInstance(project, projectHelper, session, artifactResolver,
			    buildFileName, buildDirectory, getLog());
			File remoteReport = null;
			if (compare) {
				remoteReport = t.getRemoteDependencyReport();
			}
			
			File buildReport = t.track();
			
			if (compare) {
				Integer result = t.compare(buildReport, remoteReport);
				if (moduleCount > 0) {
					comparisonResults.add(result);
				}
			}
			
			if (compare && moduleCount > 0 && comparisonResults.size() == moduleCount + 1) {
				//We generate the aggregated report after the last module
				Integer aggregatedResult = t.aggregateDependencyReports(comparisonResults);
				t.saveAggregatedArtifact(parentBuildDir, parentBuildFileName, aggregatedResult);
			}
		}
		catch (Exception e) {
			throw new MojoFailureException("An error occurred while tracking dependencies", e);
		}
	}
	
}
