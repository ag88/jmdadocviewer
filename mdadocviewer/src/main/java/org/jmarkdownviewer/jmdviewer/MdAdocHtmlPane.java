package org.jmarkdownviewer.jmdviewer;

import java.net.URL;

import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import org.jmarkdownviewer.viewer.HtmlPane;

public class MdAdocHtmlPane extends HtmlPane {	
	
	public MdAdocHtmlPane() {
		super();
	}

	public MdAdocHtmlPane(URL url) {
		super(url);
	}

	@Override
	protected void createPane(URL cssurl) {
		super.createPane(cssurl);
		
		//HTMLEditorKit kit = (HTMLEditorKit) getEditorKit();
		
		String imgsrc = App.class.getResource("markdown.png").toString();
		String imgsrcadoc = App.class.getResource("AsciiDoc-color.png").toString();
		
		// create some simple html as a string
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("<html>\n");
		sb.append("<body>\n");		
		sb.append("<h1>");
		sb.append("<img src=\"");
		sb.append(imgsrc);
		sb.append("\">");
		sb.append("&nbsp; Markdown ");
		sb.append("&amp; ");
		sb.append("<img src=\"");
		sb.append(imgsrcadoc);
		sb.append("\">");
		sb.append("&nbsp; AsciiDoc ");
		sb.append("Viewer</h1>\n");
		sb.append("<h2>Select a file</h2>\n");
		sb.append("<p>This is some sample text</p>\n");
		sb.append("</body>\n");
		sb.append("</html>");
		
		setText(sb.toString());

	}

}
