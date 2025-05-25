package org.jmarkdownviewer.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.text.MessageFormat;

import org.jmarkdownviewer.viewer.HtmlPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

public class HtmlPaneTest {

	@Test
	public void htmlPaneTest(TestReporter reporter) {
		reporter.publishEntry("htmlPaneTest()");
		
		HtmlPane htmlpane = new HtmlPane();
		
		File file = new File(getClass().getResource("/test.md").getFile());
		
		try {
			htmlpane.load(file);
		} catch (Exception e) {
			String msg = MessageFormat.format("unable to load file {0} : {1}", file.getName(), e.getMessage());
			fail(msg);
		}
		// short delay
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		try {
			htmlpane.reload();
		} catch (Exception e) {
			String msg = MessageFormat.format("unable to reload file {0} : {1}", file.getName(), e.getMessage());
			fail(msg);
		}
	}
	
}
