package org.jmarkdownviewer.jmdviewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.node.Node;
import org.jmarkdownviewer.service.DocService;

public class HtmlPane extends JEditorPane {
	
	Logger log = LogManager.getLogger(HtmlPane.class);

	Node document;
	File file;
	DocService docservice;	
	
	final String[] mdexts = {".md", ".markdown", ".mdown", ".mdwn"};
	final String[] adocexts = {".adoc", ".asciidoc"};
	
	public HtmlPane() {
		setEditable(false);
		createPane();
	}

	private void createPane() {
		HTMLEditorKit kit = new HTMLEditorKit();
		setEditorKit(kit);

		// add some styles to the html
		StyleSheet stylesheet = kit.getStyleSheet();
		
		stylesheet.importStyleSheet(App.class.getResource("github.css"));

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
		/*
		sb.append("&amp; ");
		sb.append("<img src=\"");
		sb.append(imgsrcadoc);
		sb.append("\">");
		sb.append("&nbsp; AsciiDoc ");
		*/
		sb.append("Viewer</h1>\n");
		sb.append("<h2>Select a file</h2>\n");
		sb.append("<p>This is some sample text</p>\n");
		sb.append("</body>\n");
		sb.append("</html>");
		
		// create a document, set it on the jeditorpane, then add the html
		Document doc = kit.createDefaultDocument();
		setDocument(doc);
		setText(sb.toString());

	}

	
	public void load(File file) throws IOException {
		
		if(this.docservice != null) {
			String html;
			html = docservice.load(file, this);
			this.file = file;
			setText(html);				
			setCaretPosition(0);
		} else 
			throw new IOException("docservice is null");

		/*
		for(String ext : mdexts) {
			if (file.getName().endsWith(ext)) {
				this.docservice = new MarkDownService();
				this.file = file;
				String html = docservice.load(file, this);				
				setText(html);				
				setCaretPosition(0);
				return;
			}				
		}
		
		for(String ext : adocexts) {
			if (file.getName().endsWith(ext)) {
				this.docservice = new ADocService();				
				this.file = file;
				String html = docservice.load(file, this);
				setText(html);				
				setCaretPosition(0);				
				return;
			}							
		}
		*/
		
	}
	
	public void reload() throws IOException {
		if(this.file != null) {
			load(this.file);
		}
	}
	
	public void HTMLLocalImages(String surl, Image image) {
		try {
			Dictionary cache = (Dictionary) getDocument().getProperty("imageCache");
			if (cache == null) {
				cache = new Hashtable();
				getDocument().putProperty("imageCache", cache);
			}

			URL url = new URL(surl);
			cache.put(url, image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public Image createImage() {
		BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, 100, 50);

		g.setColor(Color.YELLOW);
		g.fillOval(5, 5, 90, 40);
		img.flush();

		return img;
	}

	/*
	public String getMD() {
		MarkdownParser parser = new MarkdownParser(document);
		return parser.getMD();		
	}
	*/
	
	public void setCSS(String cssname) {
		EditorKit kit = getEditorKit();
		StyleSheet stylesheet = ((HTMLEditorKit) kit).getStyleSheet();
		
		stylesheet.importStyleSheet(App.class.getResource(cssname));

	}
	
	/*
	public String getCSS() {
		StringBuilder sb = new StringBuilder(1024);		
		try {
			String line;
			InputStream in = App.class.getResourceAsStream("github.css");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	*/

	public DocService getDocservice() {
		return docservice;
	}

	public void setDocservice(DocService docservice) {
		this.docservice = docservice;
	}
	
	
	public Node getMDocument() {
		return document;
	}

	public void setMDocument(Node document) {
		this.document = document;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}
