package net.mekomsolutions.maven.plugin.dependency;

public final class Constants {
	
	public static final String OUTPUT_SEPARATOR = "=";
	
	public static final String FILE_NAME_SEPARATOR = "-";
	
	public static final String CLASSIFIER = "dependencies";
	
	public static final String EXT = "txt";
	
	public static final String ARTIFACT_SUFFIX = FILE_NAME_SEPARATOR + CLASSIFIER + "." + EXT;
	
	public static final String COMPARE_CLASSIFIER = "comparison";
	
	public static final String AGGREGATED_CLASSIFIER = "comparison-all";
	
	public static final String COMPARE_ARTIFACT_SUFFIX = FILE_NAME_SEPARATOR + COMPARE_CLASSIFIER + "." + EXT;
	
	public static final String AGGREGATED_ARTIFACT_SUFFIX = FILE_NAME_SEPARATOR + AGGREGATED_CLASSIFIER + "." + EXT;
	
}
