package net.mekomsolutions.maven.plugin.dependency;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
	
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
	
}
