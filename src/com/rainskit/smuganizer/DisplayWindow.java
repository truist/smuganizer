package com.rainskit.smuganizer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public class DisplayWindow extends JFrame {
	private static final String IMAGE_CARD = "image.card";
	private static final String PROGRESS_CARD = "progress.card";
	private static final String HELP_CARD = "help.card";
	
	private ImageLoader imageLoader;
	private static int lastCallID;
	
	private JPanel displayPanel;
	private CardLayout cardLayout;
	private JLabel imageLabel;
	
	public DisplayWindow(Main parent) throws FileNotFoundException, IOException {
		super("Image Viewer");
		
		this.imageLoader = new ImageLoader();
		
		JEditorPane helpPane = new JEditorPane();
		helpPane.setContentType("text/html");
		FileReader fileReader = new FileReader("src/intro.html");
		helpPane.read(fileReader, null);
		fileReader.close();
		helpPane.setEditable(false);
		
		imageLabel = new JLabel("");
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		JPanel imagePanel = new JPanel(new GridBagLayout());
		imagePanel.setBackground(Color.DARK_GRAY);
		imagePanel.add(imageLabel);
		
		JPanel progressPanel = new JPanel(new GridBagLayout());
		progressPanel.setBackground(Color.DARK_GRAY);
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Loading...");
		progressPanel.add(progressBar);
		
		cardLayout = new CardLayout();
		displayPanel = new JPanel(cardLayout);
		displayPanel.add(new JScrollPane(helpPane), HELP_CARD);
		displayPanel.add(new JScrollPane(imagePanel), IMAGE_CARD);
		displayPanel.add(progressPanel, PROGRESS_CARD);
		
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
	
	public void displayImage(URL previewURL) throws IOException {
		++lastCallID;
		if (previewURL != null) {
			ImageIcon cachedImage = imageLoader.getImage(previewURL);
			if (cachedImage != null) {
				imageLoaded(cachedImage, lastCallID);
			} else {
				clearImage(true);
				imageLoader.addImage(previewURL, lastCallID);
			}
			reShowIfNeeded();
		} else {
			clearImage(false);
		}
	}
	
	private void clearImage(boolean showProgressBar) {
		imageLabel.setIcon(null);
		imageLabel.setBorder(null);
		cardLayout.show(displayPanel, (showProgressBar ? PROGRESS_CARD : IMAGE_CARD));
	}

	private void imageLoaded(ImageIcon imageIcon, int callID) {
		if (callID < lastCallID) {
			return;
		}
		
		Dimension screenSize = findSmallestScreenSize();
		Dimension preferredSize = new Dimension(Math.max(imageIcon.getIconWidth() + 20, getWidth()),
												Math.max(imageIcon.getIconHeight() + 40, getHeight()));
		if (preferredSize.width < screenSize.width && preferredSize.height < screenSize.height) {
			setSize(preferredSize);
		}
		
		imageLabel.setIcon(imageIcon);
		imageLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		cardLayout.show(displayPanel, IMAGE_CARD);
	}
	
	private Dimension findSmallestScreenSize() {
		Dimension size = new Dimension(0, 0);
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice screen : graphicsEnvironment.getScreenDevices()) {
			Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
			if (size.width == 0 || size.width > screenBounds.width) {
				size.width = screenBounds.width;
			}
			if (size.height == 0 || size.height > screenBounds.height) {
				size.height = screenBounds.height;
			}
		}
		return size;
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
	
	
	private class ImageLoader implements Runnable {
		private HashMap<String, SoftReference<ImageIcon>> cachedImageData;
		private BlockingDeque<AddImageCall> imageQueue;
		
		private ImageLoader() {
			cachedImageData = new HashMap<String, SoftReference<ImageIcon>>();
			imageQueue = new LinkedBlockingDeque<AddImageCall>();
			
			new Thread(this).start();
		}
		
		public void addImage(URL imageLocation, int ID) throws IOException {
			try {
				ImageIcon cachedImage = getImage(imageLocation);
				if (cachedImage != null) {
					imageLoaded(cachedImage, ID);
				} else {
					System.err.println("Adding to queue: " + imageLocation.toExternalForm());
					imageQueue.putFirst(new AddImageCall(imageLocation, ID));
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(DisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		public ImageIcon getImage(URL imageLocation) {
			ImageIcon cachedImage = null;
			synchronized(cachedImageData) {
				SoftReference<ImageIcon> softRef = cachedImageData.get(imageLocation.toString());
				if (softRef != null) {
					cachedImage = softRef.get();
					if (cachedImage == null) {
						System.err.println("Cache miss");
						cachedImageData.remove(imageLocation.toString());
					}
				}
			}
			return cachedImage;
		}
		
		public void run() {
			try {
				while (true) {
					final AddImageCall nextCall = imageQueue.takeFirst();
					//check if someone jumped ahead of us and already cached it, 
					//which is possible because of the pre-emptive queueing
					ImageIcon cachedImage = getImage(nextCall.imageLocation);
					if (cachedImage != null) {
						notifyImageLoaded(cachedImage, nextCall.ID);
					} else {
						try {
							System.err.println("Loading: " + nextCall.imageLocation.toExternalForm());
							InputStream urlStream = nextCall.imageLocation.openStream();
							byte[] imageData = IOUtils.toByteArray(urlStream);
							final ImageIcon imageIcon = new ImageIcon(imageData);
							synchronized(cachedImageData) {
								cachedImageData.put(nextCall.imageLocation.toString(), new SoftReference<ImageIcon>(imageIcon));
							}
							urlStream.close();
							notifyImageLoaded(imageIcon, nextCall.ID);
							System.err.println("Done loading");
						} catch (IOException ex) {
							Logger.getLogger(DisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(DisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		private void notifyImageLoaded(final ImageIcon imageIcon, final int ID) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					imageLoaded(imageIcon, ID);
				}
			});
		}
		
		
		private class AddImageCall {
			public final URL imageLocation;
			public final int ID;
			
			public AddImageCall(URL imageLocation, int ID) {
				this.imageLocation = imageLocation;
				this.ID = ID;
			}
		}
	}
}
