package net.mekomsolutions.maven.plugin.dependency;

import java.util.List;
import java.util.Map;

/**
 * Represents the differences between two dependencies reports i.e. dependencies that have been
 * added, removed, or modified.
 */
public class DependencyDiff {
	
	private List<String> added;
	
	private List<String> removed;
	
	private Map<String, List<String>> Modified;
	
	public DependencyDiff(List<String> added, List<String> removed, Map<String, List<String>> modified) {
		this.added = added;
		this.removed = removed;
		Modified = modified;
	}
	
	/**
	 * Gets the added
	 *
	 * @return the added
	 */
	public List<String> getAdded() {
		return added;
	}
	
	/**
	 * Gets the removed
	 *
	 * @return the removed
	 */
	public List<String> getRemoved() {
		return removed;
	}
	
	/**
	 * Gets the Modified
	 *
	 * @return the Modified
	 */
	public Map<String, List<String>> getModified() {
		return Modified;
	}
	
}
