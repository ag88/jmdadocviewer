package org.jmarkdownviewer.jmdviewer.parser;

import java.util.List;

import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.CustomNode;

public class YAMLVisitor extends YamlFrontMatterVisitor {

	StringBuilder sb;
	
	public YAMLVisitor() {
		sb = new StringBuilder(1024);
	}

	@Override
	public void visit(CustomNode customNode) {
		super.visit(customNode);
		if (customNode instanceof YamlFrontMatterNode) {
			YamlFrontMatterNode node = (YamlFrontMatterNode) customNode;
			sb.append(node.getKey() + ":\n");
			List<String> values = node.getValues();
			for(String v : values) {
				sb.append("  " + v + "\n");
			}
		}
		visitChildren(customNode);
	}

	String getYAML() {
		return sb.toString();
	}
}
