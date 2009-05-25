package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public class ImageWindow extends JFrame {
	private static final String IMAGE_CARD = "image.card";
	private static final String PROGRESS_CARD = "progress.card";
	private static final String NO_IMAGE_TITLE = "No image";
	
	private ImageLoader imageLoader;
	private static int lastCallID;
	
	private JPanel displayPanel;
	private CardLayout cardLayout;
	private JLabel imageLabel;
	
	public ImageWindow(Main parent) throws FileNotFoundException, IOException {
		super("Image Viewer");
		
		this.imageLoader = new ImageLoader();
		
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
	
	public void displayImage(TreeableGalleryItem image) throws IOException {
		++lastCallID;
		if (image != null && image.getPreviewURL() != null) {
			ImageIcon cachedImage = imageLoader.getImage(image);
			if (cachedImage != null) {
				imageLoaded(cachedImage, image.getFullPathLabel(), lastCallID);
			} else {
				clearImage(true);
				imageLoader.addImage(image, lastCallID);
			}
		} else {
			clearImage(false);
		}
	}
	
	private void clearImage(boolean showProgressBar) {
		imageLabel.setIcon(null);
		imageLabel.setBorder(null);
		setTitle(NO_IMAGE_TITLE);
		cardLayout.show(displayPanel, (showProgressBar ? PROGRESS_CARD : IMAGE_CARD));
	}

	private void imageLoaded(ImageIcon imageIcon, String title, int callID) {
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
		setTitle(title);
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

	
	private class ImageLoader implements Runnable {
		private HashMap<String, SoftReference<ImageIcon>> cachedImageData;
		private BlockingDeque<AddImageCall> imageQueue;
		
		private ImageLoader() {
			cachedImageData = new HashMap<String, SoftReference<ImageIcon>>();
			imageQueue = new LinkedBlockingDeque<AddImageCall>();
			
			new Thread(this).start();
		}
		
		public void addImage(TreeableGalleryItem image, int ID) throws IOException {
			try {
				ImageIcon cachedImage = getImage(image);
				if (cachedImage != null) {
					imageLoaded(cachedImage, image.getFullPathLabel(), ID);
				} else {
					imageQueue.putFirst(new AddImageCall(image, ID));
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		public ImageIcon getImage(TreeableGalleryItem image) throws MalformedURLException {
			ImageIcon cachedImage = null;
			synchronized(cachedImageData) {
				SoftReference<ImageIcon> softRef = cachedImageData.get(image.getPreviewURL().toString());
				if (softRef != null) {
					cachedImage = softRef.get();
					if (cachedImage == null) {
						cachedImageData.remove(image.getPreviewURL().toString());
					}
				}
			}
			return cachedImage;
		}
		
		public void run() {
			try {
				while (true) {
					try {
						final AddImageCall nextCall = imageQueue.takeFirst();
						//check if someone jumped ahead of us and already cached it, 
						//which is possible because of the pre-emptive queueing
						ImageIcon cachedImage = getImage(nextCall.image);
						if (cachedImage != null) {
							notifyImageLoaded(cachedImage, nextCall.image, nextCall.ID);
						} else {
							InputStream urlStream = nextCall.image.getPreviewURL().openStream();
							byte[] imageData = IOUtils.toByteArray(urlStream);
							final ImageIcon imageIcon = new ImageIcon(imageData);
							synchronized(cachedImageData) {
								cachedImageData.put(nextCall.image.getPreviewURL().toString(), new SoftReference<ImageIcon>(imageIcon));
							}
							urlStream.close();
							notifyImageLoaded(imageIcon, nextCall.image, nextCall.ID);
						}
					} catch (IOException ex) {
						Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		private void notifyImageLoaded(final ImageIcon imageIcon, final TreeableGalleryItem image, final int ID) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					imageLoaded(imageIcon, image.getFullPathLabel(), ID);
				}
			});
		}
		
		
		private class AddImageCall {
			public final TreeableGalleryItem image;
			public final int ID;
			
			public AddImageCall(TreeableGalleryItem image, int ID) {
				this.image = image;
				this.ID = ID;
			}
		}
	}
}