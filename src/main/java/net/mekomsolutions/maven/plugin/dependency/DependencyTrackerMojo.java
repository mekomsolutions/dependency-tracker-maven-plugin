package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
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
	
	private static final String MVN_PLUGIN_GROUP_NAMESPACE = "org.apache.maven.plugins";
	
	protected static final String DEPLOY_PLUGIN_KEY = MVN_PLUGIN_GROUP_NAMESPACE + ":maven-deploy-plugin";
	
	private static final String CTX_KEY_DEPLOY_STATE = MVN_PLUGIN_GROUP_NAMESPACE + ".deploy.DeployMojo.processed";
	
	private static final String DEPLOY_STATE_SKIPPED = "SKIPPED";
	
	private static final String SYSTEM_PROP_SKIP_DEPLOY = "maven.deploy.skip";
	
	protected static final ComparableVersion MIN_SUPPORTED_VERSION = new ComparableVersion("3.0.0");
	
	protected static final ComparableVersion MAX_SUPPORTED_VERSION = new ComparableVersion("3.1.4");
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;
	
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File buildDirectory;
	
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String buildFileName;
	
	@Parameter(defaultValue = "${session}", readonly = true)
	protected MavenSession session;
	
	@Component
	private MavenProjectHelper projectHelper;
	
	@Parameter(property = "compare", defaultValue = "false")
	private boolean compare;
	
	@Parameter(property = "skipDeployIfNoChanges", defaultValue = "false")
	private boolean skipDeployIfNoChanges;
	
	@Component
	protected ArtifactResolver artifactResolver;
	
	@Component
	private MavenPluginManager pluginManager;
	
	private static File parentBuildDir;
	
	private static String parentBuildFileName;
	
	private static Map<String, Integer> projectAndResultMap;
	
	private static Integer moduleCount;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Plugin deployPlugin = project.getPlugin(DEPLOY_PLUGIN_KEY);
		if (compare) {
			ComparableVersion deployPluginVersion = new ComparableVersion(deployPlugin.getVersion());
			if (deployPluginVersion.compareTo(MIN_SUPPORTED_VERSION) < 0
			        || deployPluginVersion.compareTo(MAX_SUPPORTED_VERSION) > 0) {
				String msg = "Dependency tracker plugin's compare goal does not support maven deploy plugin version "
				        + deployPluginVersion + ", supported versions range from " + MIN_SUPPORTED_VERSION + " to "
				        + MAX_SUPPORTED_VERSION;
				throw new MojoFailureException(msg);
			}
		}
		
		try {
			if (compare && moduleCount == null) {
				moduleCount = project.getModules().size();
				projectAndResultMap = new LinkedHashMap<>(moduleCount + 1);
				if (moduleCount > 0) {
					parentBuildDir = buildDirectory;
					parentBuildFileName = buildFileName;
				}
			}
			
			DependencyTracker t = DependencyTracker.createInstance(project, projectHelper, session, artifactResolver,
			    buildFileName, buildDirectory, getLog());
			File remoteReport = null;
			if (compare) {
				remoteReport = t.getRemoteDependencyReport();
			}
			
			File buildReport = t.track();
			final String artifactId = project.getArtifactId();
			if (compare) {
				Integer result = t.compare(buildReport, remoteReport);
				projectAndResultMap.put(project.getArtifactId(), result);
			} else {
				getLog().info("Skipping comparison of dependency reports for " + artifactId);
			}
			
			if (compare && (moduleCount == 0 || moduleCount > 0 && projectAndResultMap.size() == moduleCount + 1)) {
				//We generate the aggregated report after the last module
				Integer aggregatedResult;
				if (moduleCount > 0) {
					aggregatedResult = t.aggregateDependencyReports(projectAndResultMap.values());
					t.saveAggregatedArtifact(parentBuildDir, parentBuildFileName, aggregatedResult);
				} else {
					aggregatedResult = projectAndResultMap.get(artifactId);
				}
				
				if (aggregatedResult == 0 && skipDeployIfNoChanges) {
					session.getUserProperties().put(SYSTEM_PROP_SKIP_DEPLOY, "true");
					PluginDescriptor deployPluginDescriptor = pluginManager.getPluginDescriptor(deployPlugin,
					    project.getRemotePluginRepositories(), session.getRepositorySession());
					for (MavenProject proj : session.getProjects()) {
						skipDeploy(deployPluginDescriptor, proj);
					}
				}
			}
		}
		catch (Exception e) {
			throw new MojoFailureException("An error occurred while tracking dependencies", e);
		}
	}
	
	private void skipDeploy(PluginDescriptor deployPluginDescriptor, MavenProject project) {
		getDeployPluginContext(deployPluginDescriptor, project).put(CTX_KEY_DEPLOY_STATE, DEPLOY_STATE_SKIPPED);
	}
	
	private Map<String, Object> getDeployPluginContext(PluginDescriptor deployPluginDescriptor, MavenProject project) {
		return session.getPluginContext(deployPluginDescriptor, project);
	}
	
}
