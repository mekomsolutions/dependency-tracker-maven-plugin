package net.mekomsolutions.maven.plugin.dependency;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.getInternalState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	private File mockBuildDir;
	
	@Mock
	private Log mockLogger;
	
	@Mock
	private DependencyTracker mockTracker;
	
	@After
	public void tearDown() {
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildDir", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildFileName", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "moduleCount", (Object) null);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "comparisonResults", (Object) null);
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
		assertNull(getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "comparisonResults"));
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
		final File remoteReportFile = Mockito.mock(File.class);
		final File buildReportFile = Mockito.mock(File.class);
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "comparisonResults"));
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
		
		mojo.execute();
		
		Assert.assertEquals(mockBuildDir, getInternalState(DependencyTrackerMojo.class, "parentBuildDir"));
		Assert.assertEquals(TEST_FILE_NAME, getInternalState(DependencyTrackerMojo.class, "parentBuildFileName"));
		Assert.assertEquals(2, (int) getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		final List<Integer> comparisonResults = getInternalState(DependencyTrackerMojo.class, "comparisonResults");
		Assert.assertNotNull(comparisonResults);
		Assert.assertEquals(1, comparisonResults.size());
		Assert.assertEquals(comparisonResult, comparisonResults.get(0));
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
		assertNull(getInternalState(DependencyTrackerMojo.class, "comparisonResults"));
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
		assertNull(getInternalState(DependencyTrackerMojo.class, "moduleCount"));
		assertNull(getInternalState(DependencyTrackerMojo.class, "comparisonResults"));
	}
	
	@Test
	public void execute_shouldAggregateComparisonResultsDuringExecutionOfTheLastModule() throws Exception {
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
		when(mockProject.getModules()).thenReturn(Arrays.asList("api", "web"));
		when(mockTracker.compare(buildReportFile, remoteReportFile)).thenReturn(-1);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "moduleCount", 2);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildDir", mockBuildDir);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "parentBuildFileName", TEST_FILE_NAME);
		List<Integer> results = new ArrayList<>();
		results.add(1);
		results.add(0);
		Whitebox.setInternalState(DependencyTrackerMojo.class, "comparisonResults", results);
		final Integer expectedAggregatedResult = 1;
		when(mockTracker.compare(buildReportFile, remoteReportFile)).thenReturn(expectedAggregatedResult);
		when(mockTracker.aggregateDependencyReports(Arrays.asList(1, 0, expectedAggregatedResult)))
		        .thenReturn(expectedAggregatedResult);
		
		mojo.execute();
		
		Mockito.verify(mockTracker).saveAggregatedArtifact(mockBuildDir, TEST_FILE_NAME, expectedAggregatedResult);
	}
	
}
