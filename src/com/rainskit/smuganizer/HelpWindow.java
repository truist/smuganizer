package com.rainskit.smuganizer;

import java.awt.BorderLayout;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class HelpWindow extends JFrame {
	private static final String HELP_PATH = "src/intro.html";
	
	public HelpWindow(Main parent) throws FileNotFoundException, IOException {
		super("Smuganizer Help");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JEditorPane helpPane = new JEditorPane();
		helpPane.setContentType("text/html");
		FileReader fileReader = new FileReader(HELP_PATH);
		helpPane.read(fileReader, null);
		fileReader.close();
		helpPane.setEditable(false);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(helpPane));
		
		pack();
		setSize(500, 500);
		setLocationRelativeTo(parent);
		Point parentLocation = parent.getLocation();
		Point currentLocation = this.getLocation();
		setLocation(currentLocation.x, Math.max(currentLocation.y, parentLocation.y + 100));
	}
}
