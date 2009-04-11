package com.rainskit.smuganizer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DisplayWindow extends JFrame {
	private static final String IMAGE_CARD = "image.card";
	private static final String HELP_CARD = "help.card";
	
	private CardLayout cardLayout;
	private JPanel displayPanel;
	private JLabel imageLabel;
	
	public DisplayWindow(Main parent) throws FileNotFoundException, IOException {
		super("Image Viewer");
		
		JEditorPane helpPane = new JEditorPane();
		helpPane.setContentType("text/html");
		FileReader fileReader = new FileReader("src/intro.html");
		helpPane.read(fileReader, null);
		fileReader.close();
		helpPane.setEditable(false);
		
		imageLabel = new JLabel("");
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		JPanel imagePanel = new JPanel(new GridBagLayout());
		imagePanel.setBackground(Color.WHITE);
		imagePanel.add(imageLabel);
		
		cardLayout = new CardLayout();
		displayPanel = new JPanel(cardLayout);
		displayPanel.setBackground(Color.WHITE);
		displayPanel.add(new JScrollPane(helpPane), HELP_CARD);
		displayPanel.add(new JScrollPane(imagePanel), IMAGE_CARD);
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(displayPanel);
		pack();
		setSize(500, 500);
		setLocationRelativeTo(parent);
		Point parentLocation = parent.getLocation();
		Point currentLocation = this.getLocation();
		setLocation(currentLocation.x, Math.max(currentLocation.y, parentLocation.y + 100));
	}

	public void displayImage(URL previewURL) {
		if (previewURL != null) {
			imageLabel.setIcon(new ImageIcon(previewURL));
			imageLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
			cardLayout.show(displayPanel, IMAGE_CARD);
			reShowIfNeeded();
		} else {
			imageLabel.setIcon(null);
			imageLabel.setBorder(null);
		}
	}

	public void showHelp() {
		cardLayout.show(displayPanel, HELP_CARD);
		reShowIfNeeded();
	}
	
	public void reShowIfNeeded() {
		if (!isVisible()) {
			setVisible(true);
		}
	}
}
