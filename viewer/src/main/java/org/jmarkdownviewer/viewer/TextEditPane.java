package org.jmarkdownviewer.viewer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TextEditPane extends JScrollPane {
    private JTextArea textArea;
    private File currentFile;
    private boolean modified = false;
    private boolean ignoringDocumentChanges = false;
    
    MainFrame parent;

    public TextEditPane(MainFrame parent) {
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        setViewportView(textArea);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Track text changes via DocumentListener
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { markModified(); }
            @Override
            public void removeUpdate(DocumentEvent e) { markModified(); }
            @Override
            public void changedUpdate(DocumentEvent e) { markModified(); }
        });
        
        this.parent = parent;
    }

    private void markModified() {
        if (!ignoringDocumentChanges) {
        	if (modified == false) {
        		modified = true;
        		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "TEXTMODIFIED");
        		this.parent.actionPerformed(e);
        	}
            modified = true;            
        }
    }

    public String getTextContent() {
        return textArea.getText();
    }

    public void setTextContent(String text) {
        ignoringDocumentChanges = true;
        textArea.setText(text);
        ignoringDocumentChanges = false;
        modified = false;
    }

    public void loadFile(File file) throws IOException {
        ignoringDocumentChanges = true;
        this.currentFile = file;
        String content = new String(Files.readAllBytes(file.toPath()));
        textArea.setText(content);
        textArea.setCaretPosition(0);
        modified = false; // Reset modified flag after loading
        ignoringDocumentChanges = false;
    }

    public void saveFile() throws IOException {
        if (currentFile == null) {
            if (parent.doSaveAs());
            	currentFile = parent.getCurrentFile();
            return;
        }
        saveFile(currentFile);
    }

    public void saveFile(File file) throws IOException {
        this.currentFile = file;
        Files.write(file.toPath(), textArea.getText().getBytes());
        modified = false; // Reset modified flag after saving
    }
    
    
    public void doDay() {    	
    	getTextArea().setBackground(Color.WHITE);
    	getTextArea().setForeground(Color.BLACK);
    	getTextArea().setCaretColor(Color.BLACK);
    }
    
    public void doDark() {
    	getTextArea().setBackground(Color.BLACK);
    	getTextArea().setForeground(Color.WHITE);
    	getTextArea().setCaretColor(Color.WHITE);
    }
    
    // getters and setters

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public boolean isModified() {
        return modified;
    }

	public void setModified(boolean modified) {
		this.modified = modified;
	}
        
}