package net.mekomsolutions.maven.plugin.dependency;

import static net.mekomsolutions.maven.plugin.dependency.DependencyTrackerMojo.CTX_KEY_DEPLOY_STATE;
import static net.mekomsolutions.maven.plugin.dependency.DependencyTrackerMojo.DEPLOY_PLUGIN_KEY;
import static net.mekomsolutions.maven.plugin.dependency.DependencyTrackerMojo.MAX_SUPPORTED_VERSION;
import static net.mekomsolutions.maven.plugin.dependency.DependencyTrackerMojo.MIN_SUPPORTED_VERSION;
import static net.mekomsolutions.maven.plugin.dependency.DependencyTrackerMojo.SYSTEM_PROP_SKIP_DEPLOY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.getInternalState;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DependencyTracker.class)
public class DependencyTrackerMojoTest {
	
	private static final String TEST_FILE_NAME = "test-1.0";
	
	@Mock
	private MavenProject mockProject;
	
	@Mock
	private MavenProjectHelper mockProjectHelper;
	
	@Mock
	protected MavenSession mockSession;
	
	@Mock
	private File mockBuildDir;
	
	@Mock
	private Log mockLogger;
	
	@Mock
	private DependencyTracker mockTracker;
	
	@Mock
	private Plugin mockDeployPlugin;
	
	@Mock
	private MavenPluginManager mockPluginManager;
	
	@Mock
	private PluginDescriptor mockDeployPluginDescriptor;
	
	@Before
	public void setup() {
		when(mockProject.getPlugin(DEPLOY_PLUGIN_KEY)).thenReturn(mockDeployPlugin);
		when(mockDeployPlugin.getVersion()).thenReturn("3.1.4");
	}
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildDir", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildFileName", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "moduleCount", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "projectAndResultMap", (Object) null);
	}
	
	@Test
	public void execute_shouldGetAndRunTheDependencyTracker() throws Exception {
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		Whitebox.setInternalState(mojo, "compare", false);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		
		mojo.execute();
		
		Mockito.verify(mockTracker).track();
		Mockito.verify(mockTracker, Mockito.never()).compare(ArgumentMatchers.any(), ArgumentMatchers.any());
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		int moduleCount = getInternalState(DependencyTrackerMojo.class, "moduleCount");
		assertEquals(0, (moduleCount));
		Map<String, Integer> map = getInternalState(DependencyTrackerMojo.class, "projectAndResultMap");
		assertEquals(1, map.size());
	}
	
	@Test
	public void execute_shouldCompareRemoteAndBuildReport() throws Exception {
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		Whitebox.setInternalState(mojo, "compare", true);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		when(mockTracker.getRemoteDependencyReport()).thenReturn(remoteReportFile);
		when(mockTracker.track()).thenReturn(buildReportFile);
		
		mojo.execute();
		
		Mockito.verify(mockTracker).track();
		Mockito.verify(mockTracker).compare(buildReportFile, remoteReportFile);
	}
	
	@Test
	public void execute_shouldCaptureTheParentProjectDetails() throws Exception {
		final String parentArtifactId = "datafilter";
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "projectAndResultMap"));
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		Whitebox.setInternalState(mojo, "compare", true);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		when(mockTracker.getRemoteDependencyReport()).thenReturn(remoteReportFile);
		when(mockTracker.track()).thenReturn(buildReportFile);
		when(mockProject.getModules()).thenReturn(Arrays.asList("api", "web"));
		final Integer comparisonResult = -1;
		when(mockTracker.compare(buildReportFile, remoteReportFile)).thenReturn(comparisonResult);
		when(mockProject.getArtifactId()).thenReturn(parentArtifactId);
		
		mojo.execute();
		
		Assert.assertEquals(mockBuildDir, getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		Assert.assertEquals(TEST_FILE_NAME, getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		Assert.assertEquals(2, (int) getInternalState(DependencyTrackerMojo.class, "moduleCount"));
	}
	
	@Test
	public void execute_shouldNotCaptureTheParentProjectDetailsIfCompareIsNotEnabled() throws Exception {
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "projectAndResultMap"));
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		Whitebox.setInternalState(mojo, "compare", false);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		when(mockTracker.getRemoteDependencyReport()).thenReturn(remoteReportFile);
		when(mockTracker.track()).thenReturn(buildReportFile);
		when(mockProject.getModules()).thenReturn(Arrays.asList("api", "web"));
		
		mojo.execute();
		
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		Assert.assertEquals(2, (int) getInternalState(DependencyTrackerMojo.class, "moduleCount"));
	}
	
	@Test
	public void execute_shouldAggregateComparisonResultsDuringExecutionOfTheLastModule() throws Exception {
		final String artifactId = "web";
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		Whitebox.setInternalState(mojo, "compare", true);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		when(mockTracker.getRemoteDependencyReport()).thenReturn(remoteReportFile);
		when(mockTracker.track()).thenReturn(buildReportFile);
		when(mockProject.getModules()).thenReturn(Arrays.asList("api", artifactId));
		Whitebox.setInternalState(DependencyTrackerMojo.class, "moduleCount", 2);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildDir", mockBuildDir);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildFileName", TEST_FILE_NAME);
		Map<String, Integer> results = new HashMap<>();
		results.put("datafilter", 0);
		results.put("api", 0);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "projectAndResultMap", results);
		when(mockProject.getArtifactId()).thenReturn(artifactId);
		final Integer expectedAggregatedResult = 1;
		when(mockTracker.compare(buildReportFile, remoteReportFile)).thenReturn(1);
		when(mockTracker.aggregateDependencyReports(anyCollection())).thenReturn(expectedAggregatedResult);
		
		mojo.execute();
		
		Mockito.verify(mockTracker).saveAggregatedArtifact(mockBuildDir, TEST_FILE_NAME, 1);
		ArgumentCaptor<Collection> resultsCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(mockTracker).aggregateDependencyReports(resultsCaptor.capture());
		Collection<Integer> actualResults = resultsCaptor.getValue();
		Assert.assertEquals(3, actualResults.size());
		Assert.assertTrue(actualResults.contains(0));
		Assert.assertTrue(actualResults.contains(1));
	}
	
	@Test
	public void execute_shouldFailForAnOlderUnSupportedDeployPluginVersion() throws Exception {
		final String version = "2.8.4";
		DependencyTrackerMojo mojo = Mockito.spy(new DependencyTrackerMojo());
		when(mojo.getLog()).thenReturn(mockLogger);
		when(mockDeployPlugin.getVersion()).thenReturn(version);
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, "compare", true);
		
		MojoFailureException e = Assert.assertThrows(MojoFailureException.class, () -> mojo.execute());
		
		String msg = "Dependency tracker plugin's compare goal does not support maven deploy plugin version " + version
		        + ", supported versions range from " + MIN_SUPPORTED_VERSION + " to " + MAX_SUPPORTED_VERSION;
		Assert.assertEquals(msg, e.getMessage());
	}
	
	@Test
	public void execute_shouldFailForALaterUnSupportedDeployPluginVersion() throws Exception {
		final String version = "3.1.5";
		DependencyTrackerMojo mojo = Mockito.spy(new DependencyTrackerMojo());
		when(mojo.getLog()).thenReturn(mockLogger);
		when(mockDeployPlugin.getVersion()).thenReturn(version);
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, "compare", true);
		
		MojoFailureException e = Assert.assertThrows(MojoFailureException.class, () -> mojo.execute());
		
		String msg = "Dependency tracker plugin's compare goal does not support maven deploy plugin version " + version
		        + ", supported versions range from " + MIN_SUPPORTED_VERSION + " to " + MAX_SUPPORTED_VERSION;
		Assert.assertEquals(msg, e.getMessage());
	}
	
	@Test
	public void execute_shouldSkipDeployIfThereAreNoDependencyChangesAndSkipIsEnabled() throws Exception {
		final String artifactId = "datafilter";
		final Properties userProps = new Properties();
		userProps.setProperty(SYSTEM_PROP_SKIP_DEPLOY, "false");
		final Map<String, Object> deployPluginContext = new HashMap<>();
		deployPluginContext.put(CTX_KEY_DEPLOY_STATE, "TO_BE_DEPLOYED");
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "compare", true);
		Whitebox.setInternalState(mojo, "skipDeployIfNoChanges", true);
		Whitebox.setInternalState(mojo, "session", mockSession);
		Whitebox.setInternalState(mojo, "pluginManager", mockPluginManager);
		mojo = Mockito.spy(mojo);
		when(mojo.getLog()).thenReturn(mockLogger);
		when(DependencyTracker.createInstance(mockProject, mockProjectHelper, mockSession, null, null, mockBuildDir,
		    mockLogger)).thenReturn(mockTracker);
		when(mockTracker.getRemoteDependencyReport()).thenReturn(remoteReportFile);
		when(mockTracker.track()).thenReturn(buildReportFile);
		when(mockProject.getArtifactId()).thenReturn(artifactId);
		when(mockProject.getModules()).thenReturn(Collections.emptyList());
		when(mockTracker.compare(buildReportFile, remoteReportFile)).thenReturn(0);
		when(mockSession.getUserProperties()).thenReturn(userProps);
		when(mockSession.getProjects()).thenReturn(Collections.singletonList(mockProject));
		when(mockPluginManager.getPluginDescriptor(eq(mockDeployPlugin), anyList(), isNull()))
		        .thenReturn(mockDeployPluginDescriptor);
		when(mockSession.getPluginContext(mockDeployPluginDescriptor, mockProject)).thenReturn(deployPluginContext);
		
		mojo.execute();
		
		Assert.assertEquals("true", userProps.get(SYSTEM_PROP_SKIP_DEPLOY));
		Assert.assertEquals("SKIPPED", deployPluginContext.get(CTX_KEY_DEPLOY_STATE));
	}
	
}
