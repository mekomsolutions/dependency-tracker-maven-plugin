package net.mekomsolutions.maven.plugin.dependency;

public final class Constants {
	
	public static final String OUTPUT_SEPARATOR = "=";
	
	public static final String FILE_NAME_SEPARATOR = "-";
	
	public static final String CLASSIFIER = "dependencies";
	
	public static final String EXTENSION = "txt";
	
	public static final String ARTIFACT_SUFFIX = FILE_NAME_SEPARATOR + CLASSIFIER + "." + EXTENSION;
	
	public static final String COMPARE_CLASSIFIER = "comparison";
	
	public static final String COMPARE_ARTIFACT_SUFFIX = FILE_NAME_SEPARATOR + COMPARE_CLASSIFIER + "." + EXTENSION;
	
}
