package net.mekomsolutions.maven.plugin.dependency;

import static java.util.stream.Collectors.toMap;
import static net.mekomsolutions.maven.plugin.dependency.Constants.KEY_SEPARATOR_DOLLAR;
import static net.mekomsolutions.maven.plugin.dependency.Constants.SEPARATOR_COLON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;

/**
 * Contains plugin utilities
 */
public class Utils {
	
	/**
	 * Reads the contents of the specified file
	 *
	 * @param file the file to read
	 * @return the read bytes
	 * @throws IOException
	 */
	public static byte[] readFile(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	
	/**
	 * Writes lines of data to the specified file
	 *
	 * @param file the file to write to
	 * @param lines the lines of data to write
	 * @throws IOException
	 */
	public static void writeToFile(File file, List<String> lines) throws IOException {
		Files.write(file.toPath(), lines);
	}
	
	/**
	 * Writes bytes to the specified file
	 *
	 * @param file the file to write to
	 * @param bytes the bytes to write
	 * @throws IOException
	 */
	public static void writeBytesToFile(File file, byte[] bytes) throws IOException {
		Files.write(file.toPath(), bytes);
	}
	
	/**
	 * Creates a File instance with the specified parent and name
	 * 
	 * @param parent parent directory
	 * @param fileName file name
	 * @return File object
	 */
	public static File instantiateFile(File parent, String fileName) {
		return new File(parent, fileName);
	}
	
	/**
	 * Returns a message based on the provided result value.
	 *
	 * @param result an integer representing the result type, where: - 0 indicates no dependency changes
	 *            detected - -1 indicates no existing remote dependency report found - any other value
	 *            indicates dependency changes detected
	 * @return a string message indicating the corresponding result
	 */
	public static String getStringResult(int result) {
		String str;
		if (result == 0) {
			str = "no dependency changes detected";
		} else if (result == -1) {
			str = "no existing remote dependency report found";
		} else {
			str = "dependency changes detected";
		}
		
		return str;
	}
	
	/**
	 * Prints the differences between dependencies from a build report file and a remote report file,
	 * including additions, removals, and modifications. It logs the details categorized by these
	 * changes.
	 *
	 * @param buildReport the file containing the build's dependency report
	 * @param remoteReport the file containing the remote dependency report for comparison
	 * @param log the logging object used to output the dependency change details
	 * @throws IOException
	 */
	public static void printDependencyDiff(File buildReport, File remoteReport, Log log) throws IOException {
		DependencyDiff diff = createDependencyDiff(buildReport, remoteReport);
		log.info("Dependency Changes:");
		if (!diff.getAdded().isEmpty()) {
			log.info(" Added: (" + diff.getAdded().size() + ")");
			diff.getAdded().forEach(a -> log.info("  - " + a));
		}
		
		if (!diff.getRemoved().isEmpty()) {
			log.info(" Removed: (" + diff.getRemoved().size() + ")");
			diff.getRemoved().forEach(r -> log.info("  - " + r));
		}
		
		if (!diff.getModified().isEmpty()) {
			Map<String, List<String>> modified = diff.getModified();
			log.info(" Modified: (" + modified.size() + ")");
			modified.entrySet().stream().forEach(e -> {
				log.info("  - " + e.getKey() + " was " + e.getValue().stream().collect(Collectors.joining(" now ")));
			});
		}
	}
	
	/**
	 * Creates a DependencyDiff object by comparing two dependency report files, identifying added,
	 * removed, and modified dependencies.
	 *
	 * @param buildReport the file containing the build's dependency report
	 * @param remoteReport the file containing the remote dependency report for comparison
	 * @return a DependencyDiff object containing the detected changes in dependencies
	 * @throws IOException
	 */
	public static DependencyDiff createDependencyDiff(File buildReport, File remoteReport) throws IOException {
		Properties buildProps = PropertyUtils.loadFile(buildReport);
		Properties remoteProps = PropertyUtils.loadFile(remoteReport);
		final List<String> added = buildProps.keySet().stream().filter(key -> !remoteProps.containsKey(key))
		        .map(k -> getArtifactId(k)).collect(Collectors.toList());
		final List<String> removed = remoteProps.keySet().stream().filter(key -> !buildProps.containsKey(key))
		        .map(k -> getArtifactId(k)).collect(Collectors.toList());
		final Map<String, List<String>> modified = buildProps.keySet().stream().filter(remoteProps::containsKey)
		        .filter(key -> !buildProps.get(key).equals(remoteProps.get(key))).collect(toMap(k -> getArtifactId(k),
		            k -> Arrays.asList(remoteProps.get(k).toString(), buildProps.get(k).toString())));
		
		return new DependencyDiff(added, removed, modified);
	}
	
	private static String getArtifactId(Object key) {
		return key.toString().replace(KEY_SEPARATOR_DOLLAR, SEPARATOR_COLON);
	}
	
}
