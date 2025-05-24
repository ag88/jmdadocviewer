package org.jmarkdownviewer.service.adoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jmarkdownviewer.service.DocService;
import org.jsoup.Jsoup;

public class ADocService implements DocService {
	
	final String[] adocexts = {".adoc", ".asciidoc"};

	public ADocService() {
	}
	
	@Override
	public boolean canHandle(File file) {
		for(String ext: adocexts) {
			if(file.getName().endsWith(ext))
				return true;
		}
		return false;
	}

	@Override
	public String load(File file, JTextComponent textcomp) throws IOException {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		String parent;
		file = file.getCanonicalFile();
		parent = file.getParentFile().getCanonicalPath();
		Attributes attributes = Attributes.builder()
			.experimental(true)
			.attribute("linkcss")
			.build();			
			
		String html = asciidoctor.convertFile(file,	Options.builder()
			.toFile(false).safe(SafeMode.UNSAFE).parse(true)
			.backend("html").standalone(true)
			.attributes(attributes)
			.build());
		if (html != null) {
			try {
				URL url = new URL("file", "", parent);
				// System.out.println(url.toString());
				HTMLDocument doc = (HTMLDocument) textcomp.getDocument();
				doc.setBase(url);
				textcomp.setDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return html;
	}

	@Override
	public String loadRaw(File file) {
		StringBuilder sb = new StringBuilder(1000);
		if (file == null)
			return "";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	@Override
	public void exportHTML(File source, File target) throws IOException {

		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		if (source == null)
			return;

		String html = asciidoctor.convertFile(source, Options.builder()
			.safe(SafeMode.UNSAFE).parse(true).backend("html")
			.standalone(true).toFile(false).build());
		
		if (html == null || html == "")
			return;

		BufferedWriter writer = new BufferedWriter(new FileWriter(target));
		writer.write(html);
		writer.flush();
		writer.close();

	}

	/**
	 * Export text. Note, copies the asciidoc to target file instead
	 *
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void exportText(File source, File target) throws IOException {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		if (source == null)
			return;

		String html = asciidoctor.convertFile(source, Options.builder()
				.safe(SafeMode.UNSAFE).parse(true).backend("html")
				.standalone(true).toFile(false)
				.build());
		
		if (html == null || html == "")
			return;

		String text = Jsoup.parse(html).wholeText();
		BufferedWriter writer = new BufferedWriter(new FileWriter(target));
		writer.write(text);
		writer.flush();
		writer.close();

	}

}
