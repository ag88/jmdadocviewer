package org.jmarkdownviewer.jmdviewer.parser;

import java.io.File;
import java.net.MalformedURLException;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Link;

public class FileImageVisitor extends AbstractVisitor {

	
	String parent;
	
	public FileImageVisitor(String parent) {
		this.parent = parent;
	}
	
	@Override
	public void visit(Image image) {
		if(!image.getDestination().startsWith("http")) {
			try {
				String name = new File(parent,image.getDestination())
					.toURI().toURL().toExternalForm();
				image.setDestination(name);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		visitChildren(image);
	}
	
	@Override
	public void visit(Link link) {
		if(!link.getDestination().startsWith("http")) {
			try {
				String name = new File(parent,link.getDestination())
					.toURI().toURL().toExternalForm();
				link.setDestination(name);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		visitChildren(link);
	}


}
