package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
}
