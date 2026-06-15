package net.mekomsolutions.maven.plugin.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtils {
	
	/**
	 * Loads properties from the specified file.
	 *
	 * @param file the file to be loaded into a Properties object
	 * @return a Properties object containing the key-value pairs from the file
	 * @throws IOException
	 */
	public static Properties loadFile(File file) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		return properties;
	}
	
}
