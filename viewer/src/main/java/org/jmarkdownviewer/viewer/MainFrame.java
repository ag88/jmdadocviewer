package org.jmarkdownviewer.viewer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jmarkdownviewer.parser.MarkdownParser;
import org.jmarkdownviewer.service.DocService;
import org.jmarkdownviewer.viewer.service.DocServiceLoader;

import com.vaadin.open.Open;

public class MainFrame extends JFrame implements ActionListener, HyperlinkListener, WindowListener {
	
	Logger log = LogManager.getLogger(MainFrame.class);

    private JTabbedPane tabbedPane;
    private TextEditPane textEditPane;
    private HtmlPane htmlpane;
    private JLabel mlMsg;
	URL stylesheet;
	
	JButton bnDark;
	JButton bnDay;
	ButtonGroup gr1;
	JRadioButtonMenuItem rbmday;
	JRadioButtonMenuItem rbmdark;
	private File currentFile;
	private boolean isReloadingFromEditor = false;

	public MainFrame(HtmlPane htmlpane) throws Exception {
		super();
		String title = (String) Env.getInstance().get("title");
		if (title != null)
			setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.htmlpane = htmlpane;
		createGui();
	}

	public MainFrame(HtmlPane htmlpane, boolean create) throws Exception {
		super();
		String title = (String) Env.getInstance().get("title");
		if (title != null)
			setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.htmlpane = htmlpane;
		if (create)
			createGui();
	}

	public void createGui() throws Exception {
		setPreferredSize(new Dimension(1280, 960));

		JMenuBar menubar = new JMenuBar();
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic(KeyEvent.VK_F);
		menubar.add(mFile);
		mFile.add(addmenuitem("Open", "OPEN", KeyEvent.VK_O));
		JMenuItem msave = addmenuitem("Save", "SAVE", KeyEvent.VK_S);
		msave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        mFile.add(msave);
        mFile.add(addmenuitem("Save As", "SAVE_AS", KeyEvent.VK_A));
        mFile.addSeparator();
		mFile.add(addmenuitem("Reload", "RELOAD", KeyEvent.VK_R));
		mFile.add(addmenuitem("Export", "EXPORT", KeyEvent.VK_E));
		mFile.add(addmenuitem("Print", "PRINT", KeyEvent.VK_P));
        mFile.addSeparator();
		mFile.add(addmenuitem("About", "ABOUT", KeyEvent.VK_A));
		mFile.add(addmenuitem("Exit", "EXIT", KeyEvent.VK_X));

		JMenu mView = new JMenu("View");
		mView.setMnemonic(KeyEvent.VK_V);
		menubar.add(mView);
        mView.add(addmenuitem("Update Preview", "UPDATE_PREVIEW", KeyEvent.VK_U));
        mView.add(addmenuitem("Editor", "SHOW_EDITOR", KeyEvent.VK_E));
        mView.add(addmenuitem("Preview", "SHOW_PREVIEW", KeyEvent.VK_P));
		mView.addSeparator();
		gr1 = new ButtonGroup();
		rbmday = new JRadioButtonMenuItem("Normal Mode", true);
		rbmday.setActionCommand("DAY");
		rbmday.setMnemonic(KeyEvent.VK_N);
		rbmday.addActionListener(this);
		gr1.add(rbmday);
		mView.add(rbmday);
		rbmdark = new JRadioButtonMenuItem("Dark Mode", false);
		rbmdark.setActionCommand("DARK");
		rbmdark.setMnemonic(KeyEvent.VK_D);
		rbmdark.addActionListener(this);
		gr1.add(rbmdark);
		mView.add(rbmdark);

		setJMenuBar(menubar);
		getContentPane().setLayout(new BorderLayout());

		JToolBar toolbar = new JToolBar();
		toolbar.add(makeNavigationButton("Open24.gif", "OPEN", "Open", "Open"));
        toolbar.add(makeNavigationButton("Save24.gif", "SAVE", "Save", "Save"));
        toolbar.add(makeNavigationButton("SaveAs24.gif", "SAVE_AS", "Save As", "Save As"));
		toolbar.add(makeNavigationButton("Refresh24.gif", "RELOAD", "Reload", "Reload"));

		bnDark = makeNavigationButton("moon.png", "DARK", "Dark", "Dark");
		toolbar.add(bnDark);
		bnDay = makeNavigationButton("sun.png", "DAY", "Day", "Day");
		toolbar.add(bnDay);
		bnDay.setVisible(false);
        //toolbar.add(makeNavigationButton("Refresh24.gif", "UPDATE_PREVIEW", "Update Preview", "Update Preview"));
		add(toolbar, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        /*
         * htmlpane is passed in the constructor
		if (stylesheet == null)
			htmlpane = new MdAdocHtmlPane();
		else
			htmlpane = new MdAdocHtmlPane(stylesheet);
		*/
        if (htmlpane == null)
        	throw new Exception("HtmlPane is NULL");

        htmlpane.addHyperlinkListener(this);
        tabbedPane.addTab("Preview", new JScrollPane(htmlpane));

        // Editor tab (TextEditPane)
        textEditPane = new TextEditPane(this);
        tabbedPane.addTab("Editor", textEditPane);
        
        // Tab switch listener: update preview when switching to Preview tab
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == 0 && !isReloadingFromEditor) {
                    updatePreviewFromEditor();
                }
            }
        });

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        mlMsg = new JLabel();
        updateStatusMsg("Ready");
        getContentPane().add(mlMsg, BorderLayout.SOUTH);
        
        addWindowListener(this);
        
    }

	protected JButton makeNavigationButton(String imageName, String actionCommand,
			String toolTipText, String altText) {

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);

		setIcon(button, imageName, altText);

		return button;
	}

	protected void setIcon(JButton button, String imageName, String altText) {
		// Look for the image.
		String imgLocation = "icons/" + imageName;
		URL imageURL = MainFrame.class.getResource(imgLocation);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}
	}

	protected JMenuItem addmenuitem(String label, String cmd, int keyevent) {
		JMenuItem item = new JMenuItem(label);
		item.setMnemonic(keyevent);
		item.setActionCommand(cmd);
		item.addActionListener(this);
		return item;
	}

    private void doopen() {
        JFileChooser chooser = new JFileChooser(Env.getInstance().getLastdir());
        FileFilter filter = new FileNameExtensionFilter("MarkDown file", "md", "markdown", "mdown", "mdwn");        
        chooser.addChoosableFileFilter(filter);
        filter = new FileNameExtensionFilter("AsciiDoc file", "adoc", "asciidoc");
        chooser.addChoosableFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {            
            openfile(chooser.getSelectedFile());
            Env.getInstance().setLastdir(chooser.getSelectedFile().getParent());
        }
    }

	public void openfile(File file) {
		try {
			this.currentFile = file;
			htmlpane.load(file);
            textEditPane.loadFile(file);
            updateStatusMsg("Opened: " + file.getName());
		} catch (Exception e) {
			String msg = MessageFormat.format("unable load file {0}: {1}",
					file.getName(), e.getMessage());
			JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void doReload() {
		if (textEditPane.isModified()) {
			int ret = JOptionPane.showConfirmDialog(this, "Text is modified, reloading will discard the changes",
				"Text is modified", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return;			
		}
		try {
			htmlpane.reload();
            if (currentFile != null) {
                try {
                    textEditPane.loadFile(currentFile);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error reloading file: " + ex.getMessage(), 
                            "Reload Error", JOptionPane.ERROR_MESSAGE);
                }
            }
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this, e1.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
		}		
	}
	
    public void dosave() {
        // If no current file, trigger Save As
        if (currentFile == null) {
            doSaveAs();
            return;
        }
        try {
            textEditPane.saveFile();
            updateStatusMsg("Saved: " + currentFile.getName());
            htmlpane.reload();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), 
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean doSaveAs() {
        JFileChooser chooser = new JFileChooser();
        if (currentFile != null) {
            chooser.setCurrentDirectory(currentFile.getParentFile());
        } else {
            chooser.setCurrentDirectory(new File(Env.getInstance().getLastdir()));
        }
        
        // Add file filters
        FileFilter mdFilter = new FileNameExtensionFilter("Markdown files", "md", "markdown", "mdown", "mdwn");
        chooser.addChoosableFileFilter(mdFilter);
        FileFilter adocFilter = new FileNameExtensionFilter("AsciiDoc files", "adoc", "asciidoc");
        chooser.addChoosableFileFilter(adocFilter);
        chooser.setAcceptAllFileFilterUsed(false);
        
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            
            // Append extension if missing
            FileFilter selectedFilter = chooser.getFileFilter();
            if (selectedFilter instanceof FileNameExtensionFilter) {
                String[] extensions = ((FileNameExtensionFilter) selectedFilter).getExtensions();
                boolean hasValidExtension = false;
                for (String ext : extensions) {
                    if (selectedFile.getName().toLowerCase().endsWith("." + ext.toLowerCase())) {
                        hasValidExtension = true;
                        break;
                    }
                }
                if (!hasValidExtension && extensions.length > 0) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + "." + extensions[0]);
                }
            }
            
            // Check for overwrite
            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "File " + selectedFile.getName() + " exists. Overwrite?",
                    "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.OK_OPTION) return false;
            }
            
            try {
                textEditPane.saveFile(selectedFile);
                htmlpane.reload();
                Env.getInstance().setLastdir(selectedFile.getParent());
                updateStatusMsg("Saved as: " + selectedFile.getName());
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else 
        	return false;
    }

	String selextension;

	private void doexport() {
		JFileChooser chooser = new JFileChooser(Env.getInstance().getLastdir());
		FileFilter filter = new FileNameExtensionFilter("HTML file", "htm", "html");
		chooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("Plain text file", "txt");
		chooser.addChoosableFileFilter(filter);
		chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY,
				new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				FileFilter filter = (FileFilter) evt.getNewValue();

				selextension = ((FileNameExtensionFilter) filter).getExtensions()[0];

			}
		});

		int ret = chooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.getName().indexOf(".") == -1 && selextension == null) {
				String msg = "Select an appropriate extension,"
						+ " Only HTML or TXT is supported for now";
				JOptionPane.showMessageDialog(this, msg, "Invalid file type",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (file.getName().indexOf(".") == -1 && selextension != "") {
				file = new File(file.getAbsolutePath()
						.concat(".").concat(selextension));
			}
			if (file.exists()) {
				String msg = "File " + file.getName() + " exists ! Overwrite?";
				ret = JOptionPane.showConfirmDialog(this, msg, "Overwrite warning",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (ret != JOptionPane.OK_OPTION)
					return;
			}
			if (file.getName().toLowerCase().endsWith("htm") ||
				file.getName().toLowerCase().endsWith("html")) {
				DocService docservice = htmlpane.getDocservice();
				if (docservice == null)
					return;
				try {
					docservice.exportHTML(htmlpane.getFile(), file);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
				}

			} else if (file.getName().toLowerCase().endsWith("txt")) {
				DocService docservice = htmlpane.getDocservice();
				if (docservice == null)
					return;
				try {
					docservice.exportText(htmlpane.getFile(), file);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
				}
			}

		}

	}

    private void updatePreviewFromEditor() {
        if (currentFile == null) {
            //JOptionPane.showMessageDialog(this, "No file is currently open.", 
            //        "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            isReloadingFromEditor = true;
            String editorContent = textEditPane.getTextContent();
            
            if (currentFile.getName().endsWith(".md") || 
                currentFile.getName().endsWith(".markdown")) {
                Files.write(currentFile.toPath(), editorContent.getBytes());
                htmlpane.reload();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Live preview update is currently only supported for Markdown files.", 
                        "Feature Limitation", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating preview: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            isReloadingFromEditor = false;
        }
    }

    private void doDay() {
    	Marker marker = MarkerManager.getMarker("doDay()");    			
		// note this is from app
		URL url = (URL) Env.getInstance().get("dayCss");
		if (url == null) {
			log.error(marker, "Env: url for day CSS is null");
			return;
		}			
		if (stylesheet != null)
			url = stylesheet;
		htmlpane.setStyleSheet(url);
		try {
			htmlpane.reload();
		} catch (Exception e2) {
			log.error(e2.getMessage());
		}
		textEditPane.doDay();
		bnDark.setVisible(true);
		bnDay.setVisible(false);
		gr1.clearSelection();
		rbmday.setSelected(true);		
    }
    
    private void doDark() {
    	Marker marker = MarkerManager.getMarker("doDark()");
		// note this is from app
		URL url = (URL) Env.getInstance().get("darkCss");
		if (url == null) {
			log.error(marker, "Env: url for dark CSS is null");
			return;
		}
		htmlpane.setStyleSheet(url);
		try {
			htmlpane.reload();
		} catch (Exception e3) {
			log.error(e3.getMessage());
		}
		textEditPane.doDark();
		bnDark.setVisible(false);
		bnDay.setVisible(true);
		gr1.clearSelection();
		rbmdark.setSelected(true);
    }
    
	private void doprint() {
		try {
			boolean done = htmlpane.print();
			if (done) {
				JOptionPane.showMessageDialog(this, "Printing is done");
			} else {
				JOptionPane.showMessageDialog(this, "Error while printing");
			}
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"Error while printing", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void doabout() {
		Marker marker = MarkerManager.getMarker("doabout()");
		
		StringBuilder sb = new StringBuilder(1024);
		try {
			URL url = (URL) Env.getInstance().get("aboutMD");
			if (url == null) {
				log.error(marker, "Env: url for About.md is null");
				return;
			}			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(url.openStream()));
			String l;
			while ((l = reader.readLine()) != null) {
				sb.append(l);
				sb.append("\n");
			}
			reader.close();
		} catch (IOException e) {
			log.error(marker, e.getMessage());
		}
		MarkdownParser parser = new MarkdownParser();
		parser.parse(sb.toString());
		Class appClazz = (Class) Env.getInstance().get("appClass");
		if (appClazz != null) {
			parser.updatejarimages(appClazz);
		} else {
			log.error(marker, "Env: appClass is null");
		}
		String html = parser.getHTML();
		if (html != null && html != "") {
			htmlpane.setText(html);
			htmlpane.setCaretPosition(0);
		}
	}

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if(Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        }        
    }
    
    public void updateStatusMsg(String msg) {
    	StringBuilder sb = new StringBuilder(20);
    	if(textEditPane.isModified())
    		sb.append("* ");
    	sb.append(msg);
    	mlMsg.setText(sb.toString());    	
    }
    
    public void updateStatusMsg(String msg, boolean preserve) {
    	StringBuilder sb = new StringBuilder(20);
    	if(textEditPane.isModified())
    		sb.append("* ");
    	sb.append(msg);
    	if (preserve) {
    		String currtext = mlMsg.getText();
    		if (currtext.startsWith("* "))
    			currtext = currtext.substring(2);
    		sb.append(" ");
    		sb.append(currtext);
    	}    		
    	mlMsg.setText(sb.toString());    	
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		Marker marker = MarkerManager.getMarker("actionPerformed()");
		String cmd = e.getActionCommand();
		if (cmd.equals("OPEN")) {
			doopen();
        } else if(cmd.equals("SAVE")) {
            dosave();
        } else if(cmd.equals("SAVE_AS")) {
            doSaveAs();
        } else if (cmd.equals("RELOAD")) {
        	doReload();
		} else if (cmd.equals("ABOUT")) {
			doabout();
		} else if (cmd.equals("EXPORT")) {
			doexport();
		} else if (cmd.equals("DAY")) {
			doDay();
		} else if (cmd.equals("DARK")) {
			doDark();
		} else if(cmd.equals("UPDATE_PREVIEW")) {
			updatePreviewFromEditor();
		} else if(cmd.equals("SHOW_EDITOR")) {
            tabbedPane.setSelectedIndex(1);
        } else if(cmd.equals("SHOW_PREVIEW")) {
            tabbedPane.setSelectedIndex(0);
        } else if(cmd.equals("TEXTMODIFIED")) {
        	updateStatusMsg("", false);
		} else if (cmd.equals("PRINT")) {
			doprint();
		} else if (cmd.equals("EXIT")) {
			dispose();
			System.exit(0);
		}
	}

	public URL getStylesheet() {
		return stylesheet;
	}

	public void setStylesheet(URL stylesheet) {
		this.stylesheet = stylesheet;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

	private static final long serialVersionUID = 3354572268511291816L;

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (textEditPane.isModified()) {			
			int ret = JOptionPane.showConfirmDialog(this, "text is modified, Save?", "text modified", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (ret == JOptionPane.YES_OPTION) {
				if (textEditPane.getCurrentFile() == null) {
					doSaveAs();
				} else {
					dosave();
				}
			}			
		}		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
