package com.mekomsolutions.maven.plugin;

import java.io.File;
import java.nio.file.Files;

/**
 * Contains plugin utilities
 */
public class Utils {
	
	/**
	 * Reads the contents of the specified file
	 *
	 * @param file the file to read
	 * @return the read bytes
	 * @throws Exception
	 */
	public static byte[] readFile(File file) throws Exception {
		return Files.readAllBytes(file.toPath());
	}
	
}
