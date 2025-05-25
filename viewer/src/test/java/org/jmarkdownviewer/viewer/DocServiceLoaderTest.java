package org.jmarkdownviewer.viewer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.jmarkdownviewer.service.DocService;
import org.jmarkdownviewer.viewer.service.DocServiceLoader;

/**
 * Unit test for simple App.
 */
public class DocServiceLoaderTest 
{
	
	@Test
	public void providersTest(TestReporter reporter) {
		reporter.publishEntry("providersTest()");
		
		List<DocService> services = DocServiceLoader.getInstance().providers();
		boolean found = false;
		for(DocService service : services) {
			if (service.getClass().getCanonicalName()
				.equals("org.jmarkdownviewer.service.md.MarkDownService")) {
				found = true;
				break;
			}
		}
		assertTrue(found);
		
	}
	
	@Test
	public void providerTest(TestReporter reporter) {
		reporter.publishEntry("providerTest()");
				
		DocService service = DocServiceLoader.getInstance().provider();
		assertTrue(service.getClass().getCanonicalName()
				.equals("org.jmarkdownviewer.service.md.MarkDownService"));		
		
	}

	@Test
	public void getProviderForFileTest(TestReporter reporter) {
		reporter.publishEntry("getProviderForFileTest()");

		File file = new File(getClass().getResource("/test.md").getFile());				
		
		DocService service = DocServiceLoader.getInstance().getProviderForFile(file);
		assertTrue(service.getClass().getCanonicalName()
				.equals("org.jmarkdownviewer.service.md.MarkDownService"));		
		
	}
}
