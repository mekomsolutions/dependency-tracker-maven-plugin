package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	
	@Test
	public void execute_shouldGetAndRunTheDependencyTracker() throws Exception {
		PowerMockito.mockStatic(DependencyTracker.class);
		DependencyTrackerMojo mojo = new DependencyTrackerMojo();
		Whitebox.setInternalState(mojo, MavenProject.class, mockProject);
		Whitebox.setInternalState(mojo, MavenProjectHelper.class, mockProjectHelper);
		Whitebox.setInternalState(mojo, File.class, mockBuildDir);
		Whitebox.setInternalState(mojo, "buildFileName", TEST_FILE_NAME);
		mojo = Mockito.spy(mojo);
		Mockito.when(mojo.getLog()).thenReturn(mockLogger);
		Mockito.when(DependencyTracker.createInstance(mockProject, mockProjectHelper, null, null, TEST_FILE_NAME,
		    mockBuildDir, mockLogger)).thenReturn(mockTracker);
		
		mojo.execute();
		
		Mockito.verify(mockTracker).track();
	}
	
}
