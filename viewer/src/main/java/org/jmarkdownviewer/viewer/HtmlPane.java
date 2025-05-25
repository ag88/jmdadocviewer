package org.jmarkdownviewer.viewer;

import java.awt.Color;
import java.awt.Desktop;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.StyleSheet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.Message;
import org.commonmark.node.Node;
import org.jmarkdownviewer.service.DocService;
import org.jmarkdownviewer.viewer.service.DocServiceLoader;

import com.vaadin.open.Open;

public class HtmlPane extends JEditorPane implements HyperlinkListener {
	
	Logger log = LogManager.getLogger(HtmlPane.class);

	File file;
	DocService docservice;	
	
	final String[] mdexts = {".md", ".markdown", ".mdown", ".mdwn"};
	final String[] adocexts = {".adoc", ".asciidoc"};
	
	public HtmlPane() {
		setEditable(false);
		URL url = getClass().getResource("github.css");
		createPane(url);
	}
	
	public HtmlPane(URL url) {
		setEditable(false);
		createPane(url);
	}
	
	
	protected void createPane(URL cssurl) {
		HTMLEditorKit kit = new HTMLEditorKit();
		setEditorKit(kit);

		// add some styles to the html
		StyleSheet stylesheet = kit.getStyleSheet();		
		stylesheet.importStyleSheet(cssurl);

		// create a document, set it on the jeditorpane, then add the html
		Document doc = kit.createDefaultDocument();
		setDocument(doc);

		/*
		// create some simple html as a string
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("<html>\n");
		sb.append("<body>\n");		
		sb.append("<h1>hello world!</h1>");
		sb.append("</body>\n");
		sb.append("</html>");
				
		setText(sb.toString());
		*/

		addHyperlinkListener(this);
	}
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        	if(Desktop.isDesktopSupported()) {
        		Open.open(e.getURL().toString());
        		/*
        	    try {        	    	
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
				*/
        	}
         }		
	}
	
	public void load(File file) throws Exception {
		Marker marker = MarkerManager.getMarker("load()");
				
		try {
			DocService service = DocServiceLoader.getInstance().getProviderForFile(file);
			if(service == null) {
				Message msg = log.getMessageFactory().newMessage(
						"unable to create docservice for {}", file.getName());
				log.error(marker, msg.getFormattedMessage());				
				throw new Exception(msg.getFormattedMessage());
			}
			this.docservice = service;
		} catch (Exception e) {
			Message msg = log.getMessageFactory().newMessage(
				"unable to create docservice for {}", file.getName());
			log.error(marker, msg.getFormattedMessage());				
			throw new Exception(msg.getFormattedMessage());
		}

		String html;
		try {
			html = docservice.load(file, this);
		} catch (IOException e) {
			Message msg = log.getMessageFactory().newMessage(
					"unable to load file {}", file.getName());
			log.error(marker, msg.getFormattedMessage());				
			throw new Exception(msg.getFormattedMessage());
		}
		this.file = file;
		setText(html);				
		setCaretPosition(0);			
		
	}
	
	public void reload() throws Exception {
		Marker marker = MarkerManager.getMarker("reload()");
		if(this.file != null) {
			try {
				load(this.file);
			} catch (Exception e) {
				Message msg = log.getMessageFactory().newMessage(
						"{}: unable to reload file {}: {}", marker.getName(), file.getName(), e.getMessage());				
				throw new Exception(msg.getFormattedMessage());
			}
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
	
	public void setStyleSheet(URL url) {
		EditorKit kit = getEditorKit();
		StyleSheet stylesheet = ((HTMLEditorKit) kit).getStyleSheet();
		
		stylesheet.importStyleSheet(url);

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
	
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}
