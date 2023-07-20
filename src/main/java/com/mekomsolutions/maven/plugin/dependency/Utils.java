package com.mekomsolutions.maven.plugin.dependency;

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
	 * Writes data to the specified file
	 *
	 * @param file the file to write to
	 * @param lines the lines of data to write
	 * @throws IOException
	 */
	public static void writeToFile(File file, List<String> lines) throws IOException {
		Files.write(file.toPath(), lines);
	}
	
}
