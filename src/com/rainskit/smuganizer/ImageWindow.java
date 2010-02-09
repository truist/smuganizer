package com.rainskit.smuganizer;

import com.rainskit.smuganizer.smugmugapiwrapper.SmugAPIConstants;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

public class ImageWindow extends JFrame {
	private static final String IMAGE_CARD = "image.card";
	private static final String PROGRESS_CARD = "progress.card";
	private static final String NO_IMAGE_TITLE = "No image";
	private static int RETRY_DELAY = 2000;
	
	private ImageLoader imageLoader;
	private static int lastCallID;
	
	private JPanel displayPanel;
	private JScrollPane displayScrollPane;
	private CardLayout cardLayout;
	private JLabel imageLabel;
	
	public ImageWindow(Smuganizer parent) throws FileNotFoundException, IOException {
		super("Image Viewer");
        setIconImage(parent.getIconImage());
		
		this.imageLoader = new ImageLoader();
		
		imageLabel = new JLabel("");
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		JPanel imagePanel = new JPanel(new GridBagLayout());
		imagePanel.setBackground(Color.DARK_GRAY);
		imagePanel.add(imageLabel);
		
		JPanel progressPanel = new JPanel(new GridBagLayout());
		progressPanel.setBackground(Color.DARK_GRAY);
		progressPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Loading...");
		progressPanel.add(progressBar);
		
		cardLayout = new CardLayout();
		displayPanel = new JPanel(cardLayout);
		displayScrollPane = new JScrollPane(imagePanel);
		displayPanel.add(displayScrollPane, IMAGE_CARD);
		displayPanel.add(progressPanel, PROGRESS_CARD);
		
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(displayPanel);
		pack();
		setSize(700, 500);
		
		setLocationRelativeTo(parent);
		setFocusableWindowState(false);
	}
	
	public void displayImage(TreeableGalleryItem image, HttpClient httpClient) throws IOException {
		++lastCallID;
		if (image != null && image.getPreviewURL() != null) {
			ImageIcon cachedImage = imageLoader.getImage(image);
			if (cachedImage != null) {
				imageLoaded(cachedImage, image.getFullPathLabel(), lastCallID);
			} else {
				clearImage(true);
				imageLoader.addImage(image, lastCallID, httpClient);
			}
		} else {
			clearImage(false);
		}
	}
	
	private void clearImage(boolean showProgressBar) {
		imageLabel.setIcon(null);
		setTitle(NO_IMAGE_TITLE);
		cardLayout.show(displayPanel, (showProgressBar ? PROGRESS_CARD : IMAGE_CARD));
	}

	private void imageLoaded(ImageIcon imageIcon, String title, int callID) {
		if (callID < lastCallID) {
			return;
		}

		Dimension viewportSize = displayScrollPane.getViewport().getExtentSize();
		viewportSize.width -= displayScrollPane.getVerticalScrollBar().getPreferredSize().width;
		viewportSize.height -= displayScrollPane.getHorizontalScrollBar().getPreferredSize().height;
		if (viewportSize.width < imageIcon.getIconWidth() || viewportSize.height < imageIcon.getIconHeight()) {
			imageIcon = resizeImage(imageIcon, viewportSize);
		}

		imageLabel.setIcon(imageIcon);
		cardLayout.show(displayPanel, IMAGE_CARD);
		setTitle(title);
	}
	
	private ImageIcon resizeImage(ImageIcon imageIcon, Dimension newSize) {
		Image image = imageIcon.getImage();
		float imageRatio = ((float)image.getWidth(null)) / image.getHeight(null);
		float windowRatio = ((float)newSize.width) / newSize.height;
		if (imageRatio > windowRatio) {
			return new ImageIcon(image.getScaledInstance(newSize.width, -1, Image.SCALE_FAST));
		} else {
			return new ImageIcon(image.getScaledInstance(-1, newSize.height, Image.SCALE_FAST));
		}
	}

	
	private class ImageLoader implements Runnable {
		private final HashMap<String, SoftReference<ImageIcon>> cachedImageData = new HashMap<String, SoftReference<ImageIcon>>();
		private BlockingDeque<AddImageCall> imageQueue;
		
		private ImageLoader() {
			imageQueue = new LinkedBlockingDeque<AddImageCall>();
			
			new Thread(this).start();
		}
		
		public void addImage(TreeableGalleryItem image, int ID, HttpClient httpClient) throws IOException {
			try {
				ImageIcon cachedImage = getImage(image);
				if (cachedImage != null) {
					imageLoaded(cachedImage, image.getFullPathLabel(), ID);
				} else {
					imageQueue.putFirst(new AddImageCall(image, ID, httpClient));
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		public ImageIcon getImage(TreeableGalleryItem image) throws IOException {
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
					AddImageCall nextCall = imageQueue.takeFirst();
					try {
						//check if someone jumped ahead of us and already cached it, 
						//which is possible because of the pre-emptive queueing
						ImageIcon cachedImage = getImage(nextCall.image);
						if (cachedImage != null) {
							notifyImageLoaded(cachedImage, nextCall.image, nextCall.ID);
						} else {
							loadImage(nextCall);
						}
					} catch (IOException ex) {
						retryIfNeeded(nextCall, ex);
					}
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		private void retryIfNeeded(final AddImageCall nextCall, IOException ex) {
			//sometimes, if the image was just uploaded, SmugMug will error out when we ask for it.
			//other times, the image was just deleted before we got a chance to display it.
			//so we check to see if it still has its parent, as a way to see if it was deleted.
			//if so, we try again - unless the user has already clicked on another image.
			//and we put a small delay in before we try agian, to avoid hammering the server.
			if (nextCall.image.getParent() != null) {
//				Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, "Image failed to load for display", ex);
				if (imageQueue.isEmpty()) {
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(RETRY_DELAY);
								imageQueue.putLast(nextCall);
							} catch (InterruptedException ex1) {
								Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex1);
							}
						}
					}).start();
				}
			}
		}

		private void loadImage(final AddImageCall nextCall) throws IOException {
			byte[] imageData;
			URL imageURL = nextCall.image.getPreviewURL();
			if("file".equals(imageURL.getProtocol())) {
				try {
					imageData = IOUtils.toByteArray(new FileInputStream(new File(imageURL.toURI())));
				} catch (URISyntaxException ex) {
					throw new IOException(ex.getMessage(), ex);
				}
			} else {
				GetMethod getMethod = new GetMethod(imageURL.toExternalForm());
				getMethod.setRequestHeader("User-Agent", SmugAPIConstants.USER_AGENT);
				try {
					int responseCode = nextCall.httpClient.executeMethod(getMethod);
					if (responseCode != HttpStatus.SC_OK) {
						throw new IOException("Received unexpected response code (" + responseCode + ") from server when trying to retrieve URL: " + imageURL.toExternalForm());
					}
					imageData = IOUtils.toByteArray(getMethod.getResponseBodyAsStream());
				} finally {
					getMethod.releaseConnection();
				}
			}
			final ImageIcon imageIcon = new ImageIcon(imageData);
			synchronized (cachedImageData) {
				cachedImageData.put(imageURL.toString(), new SoftReference<ImageIcon>(imageIcon));
			}
			notifyImageLoaded(imageIcon, nextCall.image, nextCall.ID);
		}

		private void notifyImageLoaded(final ImageIcon imageIcon, final TreeableGalleryItem image, final int ID) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						imageLoaded(imageIcon, image.getFullPathLabel(), ID);
					} catch (IOException ex) {
						Logger.getLogger(ImageWindow.class.getName()).log(Level.SEVERE, null, ex);
						imageLoaded(imageIcon, "ERROR: " + ex.getMessage(), ID);
					}
				}
			});
		}
		
		
		private class AddImageCall {
			public final TreeableGalleryItem image;
			public final int ID;
			private HttpClient httpClient;
			
			public AddImageCall(TreeableGalleryItem image, int ID, HttpClient httpClient) {
				this.image = image;
				this.ID = ID;
				this.httpClient = httpClient;
			}
		}
	}
}
