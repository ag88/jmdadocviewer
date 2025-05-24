package org.jmarkdownviewer.jmdviewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jmarkdownviewer.parser.MarkdownParser;

public class App {

	String lastdir;
	String startfile = null;

	private static App m_instance;

	private App() {
		// lastdir = System.getProperty("user.home");
		lastdir = System.getProperty("user.dir");
	}

	public static App getInstance() {
		if (m_instance == null)
			m_instance = new App();
		return m_instance;
	}

	private void run(String[] args) {
		parseargs(args);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainFrame m = new MainFrame();
				m.pack();
				m.setLocationRelativeTo(null);
				m.setVisible(true);
				if (startfile != null) {
					File file = new File(startfile);
					if (file.exists())
						m.openfile(file);
				}
			}
		});

	}

	private void parseargs(String[] args) {
		Options options = new Options();
		Option help = new Option("h", "help", false, "help");
		options.addOption(help);
		options.addOption(
				Option.builder().longOpt("html").desc("convert to html").build());
		options.addOption(
				Option.builder().longOpt("text").desc("convert to txt").build());
		options.addOption(
				Option.builder("o").longOpt("out").hasArg().argName("filename")
				.desc("output to file").build());

		CommandLine line;
		CommandLineParser parser = new DefaultParser();
		try {
			line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar jmdviewer.jar [filename.md]", options);
				System.exit(0);
			}

			if (line.hasOption("html")) {
				if (line.getArgs().length < 1) {
					System.err.println("filename required for html opt");
					System.exit(1);
				}
				String filename = line.getArgs()[0];
				String out = dotohtml(filename);
				if (out != null) {
					if(line.hasOption("out")) {
						String outfilename = line.getOptionValue("out");
						savefile(outfilename, out);
						System.out.println("saved in " + outfilename);
					} else
						System.out.println(out);
				}
				System.exit(0);
			}

			if (line.hasOption("text")) {
				if (line.getArgs().length < 1) {
					System.err.println("filename required for text opt");
					System.exit(1);
				}
				String filename = line.getArgs()[0];
				String out = dototext(filename);
				if (out != null) {
					if(line.hasOption("out")) {
						String outfilename = line.getOptionValue("out");
						savefile(outfilename, out);
						System.out.println("saved in " + outfilename);
					} else
						System.out.println(out);
				}
				System.exit(0);
			}

			args = line.getArgs();
			if (args != null && args.length > 0) {
				setStartfile(args[0]);
			}

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar jmdviewer.jar [filename.md]", options);
			System.exit(1);
		}

	}


	public static void main(String[] args) {
		App app = getInstance();
		app.run(args);
	}

	private String dotohtml(String filename) {
		MarkdownParser parser = new MarkdownParser();
		String rtxt = loadfile(filename);
		if(rtxt == null) return null;
		parser.parse(rtxt);
		return parser.getHTML();
	}

	private String dototext(String filename) {
		MarkdownParser parser = new MarkdownParser();
		String rtxt = loadfile(filename);
		if(rtxt == null) return null;
		parser.parse(rtxt);
		return parser.getText();
	}

	public String loadfile(String filename) {
		StringBuilder sb = new StringBuilder(100);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	public void savefile(String filename, String text) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(text);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLastdir() {
		return lastdir;
	}

	public void setLastdir(String lastdir) {
		this.lastdir = lastdir;
	}

	public String getStartfile() {
		return startfile;
	}

	public void setStartfile(String startfile) {
		this.startfile = startfile;
	}
}
