package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyUtils.class)
public class UtilsTest {
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(PropertyUtils.class);
	}
	
	@Test
	public void getStringResult_shouldReturnsNoChangesDetected() {
		Utils utils = new Utils();
		String result = utils.getStringResult(0);
		Assert.assertEquals("no dependency changes detected", result);
	}
	
	@Test
	public void getStringResult_shouldReturnsNoReportFoundsString() {
		Utils utils = new Utils();
		String result = utils.getStringResult(-1);
		Assert.assertEquals("no existing remote dependency report found", result);
	}
	
	@Test
	public void getStringResult_shouldReturnsChangesDetected() {
		Utils utils = new Utils();
		String result = utils.getStringResult(1);
		Assert.assertEquals("dependency changes detected", result);
	}
	
	@Test
	public void createDependencyDiff_shouldReturnCorrectAddedDependencies() throws IOException {
		File buildReport = Mockito.mock(File.class);
		File remoteReport = Mockito.mock(File.class);
		Properties buildProps = new Properties();
		buildProps.setProperty("servlet-api", "3.0.0");
		buildProps.setProperty("spring", "2.0.0");
		buildProps.setProperty("commons-api", "1.0.0");
		Properties remoteProps = new Properties();
		remoteProps.setProperty("commons-api", "1.0.0");
		Mockito.when(PropertyUtils.loadFile(buildReport)).thenReturn(buildProps);
		Mockito.when(PropertyUtils.loadFile(remoteReport)).thenReturn(remoteProps);
		
		DependencyDiff result = Utils.createDependencyDiff(buildReport, remoteReport);
		
		Assert.assertEquals(2, result.getAdded().size());
		Assert.assertTrue(result.getAdded().contains("servlet-api"));
		Assert.assertTrue(result.getAdded().contains("spring"));
	}
	
	@Test
	public void createDependencyDiff_shouldReturnCorrectRemovedDependencies() throws IOException {
		File buildReport = Mockito.mock(File.class);
		File remoteReport = Mockito.mock(File.class);
		Properties buildProps = new Properties();
		buildProps.setProperty("commons-api", "1.0.0");
		Properties remoteProps = new Properties();
		remoteProps.setProperty("servlet-api", "3.0.0");
		remoteProps.setProperty("spring", "2.0.0");
		remoteProps.setProperty("commons-api", "1.0.0");
		Mockito.when(PropertyUtils.loadFile(buildReport)).thenReturn(buildProps);
		Mockito.when(PropertyUtils.loadFile(remoteReport)).thenReturn(remoteProps);
		
		DependencyDiff result = Utils.createDependencyDiff(buildReport, remoteReport);
		
		Assert.assertEquals(2, result.getRemoved().size());
		Assert.assertTrue(result.getRemoved().contains("servlet-api"));
		Assert.assertTrue(result.getRemoved().contains("spring"));
	}
	
	@Test
	public void createDependencyDiff_shouldReturnCorrectModifiedDependencies() throws IOException {
		File buildReport = Mockito.mock(File.class);
		File remoteReport = Mockito.mock(File.class);
		String servletApi = "servlet-api";
		String spring = "spring";
		Properties buildProps = new Properties();
		buildProps.setProperty("commons-api", "1.0.0");
		buildProps.setProperty(servletApi, "3.0.1");
		buildProps.setProperty(spring, "2.0.1");
		Properties remoteProps = new Properties();
		remoteProps.setProperty("commons-api", "1.0.0");
		remoteProps.setProperty(servletApi, "3.0.0");
		remoteProps.setProperty(spring, "2.0.0");
		Mockito.when(PropertyUtils.loadFile(buildReport)).thenReturn(buildProps);
		Mockito.when(PropertyUtils.loadFile(remoteReport)).thenReturn(remoteProps);
		
		DependencyDiff result = Utils.createDependencyDiff(buildReport, remoteReport);
		
		Assert.assertEquals(2, result.getModified().size());
		Assert.assertEquals(result.getModified().get(servletApi), Arrays.asList("3.0.0", "3.0.1"));
		Assert.assertEquals(result.getModified().get(spring), Arrays.asList("2.0.0", "2.0.1"));
	}
	
}
