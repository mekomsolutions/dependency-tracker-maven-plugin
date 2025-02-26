package net.mekomsolutions.maven.plugin.dependency;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.mekomsolutions.maven.plugin.dependency.Constants.ARTIFACT_SUFFIX;
import static net.mekomsolutions.maven.plugin.dependency.Constants.CLASSIFIER;
import static net.mekomsolutions.maven.plugin.dependency.Constants.COMPARE_ARTIFACT_SUFFIX;
import static net.mekomsolutions.maven.plugin.dependency.Constants.EXTENSION;
import static net.mekomsolutions.maven.plugin.dependency.Constants.OUTPUT_SEPARATOR;
import static net.mekomsolutions.maven.plugin.dependency.DependencyTracker.createInstance;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utils.class, RepositoryUtils.class })
public class DependencyTrackerTest {
	
	private static final String TEST_FILE_NAME = "test-1.0";
	
	private DependencyTracker tracker;
	
	@Mock
	private MavenProject mockProject;
	
	@Mock
	private MavenProjectHelper mockProjectHelper;
	
	@Mock
	private File mockBuildDir;
	
	@Mock
	private Log mockLogger;
	
	@Mock
	private ArtifactRepository mockRemoteRepo;
	
	@Mock
	private ArtifactResolver mockArtifactResolver;
	
	@Mock
	private MavenSession mockSession;
	
	@Mock
	private RepositorySystemSession mockRepoSysSession;
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(Utils.class);
		PowerMockito.mockStatic(RepositoryUtils.class);
		tracker = createInstance(mockProject, mockProjectHelper, mockSession, mockArtifactResolver, TEST_FILE_NAME,
		    mockBuildDir, mockLogger);
	}
	
	@Test
	public void track_shouldPrepareDependenceDetailsAndSaveTheArtifact() throws Exception {
		tracker = Mockito.spy(tracker);
		List<String> testDependencies = Arrays.asList("dependency1=hash1", "dependency2=hash2");
		AtomicInteger preparedInvocations = new AtomicInteger();
		Mockito.doAnswer(invocation -> {
			preparedInvocations.incrementAndGet();
			return testDependencies;
		}).when(tracker).prepareDependencyArtifact();
		
		AtomicInteger saveInvocations = new AtomicInteger();
		Mockito.doAnswer(invocation -> {
			saveInvocations.incrementAndGet();
			return null;
		}).when(tracker).saveDependencyArtifact(testDependencies);
		
		tracker.track();
		
		assertEquals(1, preparedInvocations.get());
		assertEquals(1, saveInvocations.get());
	}
	
	@Test
	public void prepareDependencyArtifact_shouldGetTheProjectDependenciesSortedAlphabeticallyByKey() throws Exception {
		final String groupId1 = "groupId-1";
		final String artifactId1 = "artifactId-1";
		final String version1 = "version-1";
		final String type1 = "type-1";
		final String data1 = "data-1";
		final String groupId2 = "groupId-2";
		final String artifactId2 = "artifactId-2";
		final String version2 = "version-2";
		final String type2 = "type-2";
		final String data2 = "data-2";
		final String groupId3 = "agroupId-3";
		final String artifactId3 = "artifactId-3";
		final String version3 = "version-3";
		final String type3 = "type-3";
		final String data3 = "data-3";
		final File mockDependencyFile1 = Mockito.mock(File.class);
		final File mockDependencyFile2 = Mockito.mock(File.class);
		final File mockDependencyFile3 = Mockito.mock(File.class);
		Artifact a1 = new DefaultArtifact(groupId1, artifactId1, version1, null, type1, "", null);
		a1.setFile(mockDependencyFile1);
		Artifact a2 = new DefaultArtifact(groupId2, artifactId2, version2, "test", type2, "tests", null);
		a2.setFile(mockDependencyFile2);
		Artifact a3 = new DefaultArtifact(groupId3, artifactId3, version3, null, type3, "", null);
		a3.setFile(mockDependencyFile3);
		Set<Artifact> artifacts = new HashSet<>();
		artifacts.add(a1);
		artifacts.add(a2);
		artifacts.add(a3);
		when(mockProject.getDependencyArtifacts()).thenReturn(artifacts);
		when(Utils.readFile(mockDependencyFile1)).thenReturn(data1.getBytes(UTF_8));
		when(Utils.readFile(mockDependencyFile2)).thenReturn(data2.getBytes(UTF_8));
		when(Utils.readFile(mockDependencyFile3)).thenReturn(data3.getBytes(UTF_8));
		
		List<String> lines = tracker.prepareDependencyArtifact();
		
		assertEquals(3, lines.size());
		assertEquals(a3.getId() + OUTPUT_SEPARATOR + sha1Hex(data3.getBytes(UTF_8)), lines.get(0));
		assertEquals(a1.getId() + OUTPUT_SEPARATOR + sha1Hex(data1.getBytes(UTF_8)), lines.get(1));
		assertEquals(a2.getId() + OUTPUT_SEPARATOR + sha1Hex(data2.getBytes(UTF_8)), lines.get(2));
	}
	
	@Test
	public void saveDependencyArtifact_shouldSaveTheDependencyArtifactToTheBuildDirectory() throws Exception {
		final File artifactFile = Mockito.mock(File.class);
		List<String> testDependencies = Arrays.asList("dependency1=hash1", "dependency2=hash2");
		when(Utils.instantiateFile(mockBuildDir, TEST_FILE_NAME + ARTIFACT_SUFFIX)).thenReturn(artifactFile);
		
		tracker.saveDependencyArtifact(testDependencies);
		
		PowerMockito.verifyStatic(Utils.class);
		Utils.writeToFile(artifactFile, testDependencies);
		Mockito.verify(mockProjectHelper).attachArtifact(mockProject, EXTENSION, CLASSIFIER, artifactFile);
	}
	
	@Test
	public void getRemoteDependencyReport_shouldDownloadTheReportFromTheRemoteRepo() throws Exception {
		final String groupId = "group-id";
		final String artifactId = "artifact-id";
		final String version = "1.0.7-SNAPSHOT";
		when(mockProject.getGroupId()).thenReturn(groupId);
		when(mockProject.getArtifactId()).thenReturn(artifactId);
		when(mockProject.getVersion()).thenReturn(version);
		final File artifactFile = Mockito.mock(File.class);
		final org.eclipse.aether.artifact.Artifact artifact = Mockito.mock(org.eclipse.aether.artifact.Artifact.class);
		when(artifact.getFile()).thenReturn(artifactFile);
		ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest());
		artifactResult.setArtifact(artifact);
		RemoteRepository remoteAetherRepo = new RemoteRepository.Builder(null, null, null).build();
		when(mockProject.getDistributionManagementArtifactRepository()).thenReturn(mockRemoteRepo);
		when(RepositoryUtils.toRepo(mockRemoteRepo)).thenReturn(remoteAetherRepo);
		when(mockSession.getRepositorySession()).thenReturn(mockRepoSysSession);
		when(mockArtifactResolver.resolveArtifact(eq(mockRepoSysSession), any(ArtifactRequest.class)))
		        .thenReturn(artifactResult);
		
		assertEquals(artifactFile, tracker.getRemoteDependencyReport());
		
		ArgumentCaptor<ArtifactRequest> reqArgCaptor = ArgumentCaptor.forClass(ArtifactRequest.class);
		Mockito.verify(mockArtifactResolver).resolveArtifact(eq(mockRepoSysSession), reqArgCaptor.capture());
		ArtifactRequest actualRequest = reqArgCaptor.getValue();
		org.eclipse.aether.artifact.Artifact aetherArtifact = actualRequest.getArtifact();
		assertEquals(groupId, aetherArtifact.getGroupId());
		assertEquals(artifactId, aetherArtifact.getArtifactId());
		assertEquals(Constants.CLASSIFIER, aetherArtifact.getClassifier());
		assertEquals(Constants.EXTENSION, aetherArtifact.getExtension());
		assertEquals(version, aetherArtifact.getVersion());
		assertEquals(Collections.singletonList(remoteAetherRepo), actualRequest.getRepositories());
	}
	
	@Test
	public void getRemoteDependencyReport_shouldReturnNullIfReportDoesNotExistInTheRemoteRepo() throws Exception {
		RemoteRepository remoteAetherRepo = new RemoteRepository.Builder(null, null, null).build();
		when(mockProject.getDistributionManagementArtifactRepository()).thenReturn(mockRemoteRepo);
		when(RepositoryUtils.toRepo(mockRemoteRepo)).thenReturn(remoteAetherRepo);
		when(mockSession.getRepositorySession()).thenReturn(mockRepoSysSession);
		when(mockArtifactResolver.resolveArtifact(eq(mockRepoSysSession), any(ArtifactRequest.class)))
		        .thenThrow(new ArtifactResolutionException(Collections.emptyList()));
		assertNull(tracker.getRemoteDependencyReport());
		Mockito.verify(mockLogger).info("No remote dependency report found");
	}
	
	@Test
	public void compare_shouldCompareAndSaveReportForMatchingDependencyReports() throws Exception {
		final byte[] reportContents = "dependency1=hash1".getBytes(UTF_8);
		final File buildReport = Mockito.mock(File.class);
		final File remoteReport = Mockito.mock(File.class);
		when(Utils.readFile(buildReport)).thenReturn(reportContents);
		when(Utils.readFile(remoteReport)).thenReturn(reportContents);
		tracker = Mockito.spy(tracker);
		AtomicInteger saveInvocations = new AtomicInteger();
		Mockito.doAnswer(invocation -> {
			saveInvocations.incrementAndGet();
			return null;
		}).when(tracker).saveComparisonArtifact(0);
		
		tracker.compare(buildReport, remoteReport);
		
		assertEquals(1, saveInvocations.get());
	}
	
	@Test
	public void compare_shouldCompareAndSaveReportForDependencyReportsThatDoNotMatch() throws Exception {
		final File buildReport = Mockito.mock(File.class);
		final File remoteReport = Mockito.mock(File.class);
		when(Utils.readFile(buildReport)).thenReturn("dependency1=hash1".getBytes(UTF_8));
		when(Utils.readFile(remoteReport)).thenReturn("dependency1=hash2".getBytes(UTF_8));
		tracker = Mockito.spy(tracker);
		AtomicInteger saveInvocations = new AtomicInteger();
		Mockito.doAnswer(invocation -> {
			saveInvocations.incrementAndGet();
			return null;
		}).when(tracker).saveComparisonArtifact(1);
		
		tracker.compare(buildReport, remoteReport);
		
		assertEquals(1, saveInvocations.get());
	}
	
	@Test
	public void compare_shouldCompareAndSaveReportWhenTheReportDoesNotExistInTheRemoteRepo() throws Exception {
		tracker = Mockito.spy(tracker);
		AtomicInteger saveInvocations = new AtomicInteger();
		Mockito.doAnswer(invocation -> {
			saveInvocations.incrementAndGet();
			return null;
		}).when(tracker).saveComparisonArtifact(-1);
		
		tracker.compare(Mockito.mock(File.class), null);
		
		assertEquals(1, saveInvocations.get());
	}
	
	@Test
	public void saveComparisonArtifact_shouldSaveTheComparisonArtifactToTheBuildDirectory() throws Exception {
		final File artifactFile = Mockito.mock(File.class);
		final Integer result = 1;
		when(Utils.instantiateFile(mockBuildDir, TEST_FILE_NAME + COMPARE_ARTIFACT_SUFFIX)).thenReturn(artifactFile);
		
		tracker.saveComparisonArtifact(result);
		
		PowerMockito.verifyStatic(Utils.class);
		Utils.writeBytesToFile(artifactFile, result.toString().getBytes(UTF_8));
	}
	
}
