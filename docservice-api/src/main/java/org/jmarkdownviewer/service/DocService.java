package org.jmarkdownviewer.service;

import java.io.File;
import java.io.IOException;

import javax.swing.text.JTextComponent;

public interface DocService {

	public boolean canHandle(File file);
	
	public String load(File file, JTextComponent textcomp) throws IOException;
	
	public String loadRaw(File file);
	
	public void exportHTML(File source, File target) throws IOException;
	
	public void exportText(File source, File target) throws IOException;
}
