package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GalleryImage implements TreeableGalleryItem {
	private static ImageIcon imageIcon = new ImageIcon("lib/images/image.png");
	
	private GalleryAlbum parent;
	private Properties imageDetails;
	private int imageRefNum;
	
	public GalleryImage(GalleryAlbum parent, Properties imageDetails, int imageRefNum) {
		this.parent = parent;
		this.imageDetails = imageDetails;
		this.imageRefNum = imageRefNum;
//		this.title = response.getProperty(Gallery.RESPONSE_IMAGE_TITLE_INDEXED + imageRefNum);
//		this.url = new URL(response.getProperty(Gallery.RESPONSE_BASEURL) + this.name);
	}

	@Override
	public String toString() {
		return getLabel() + (isHidden() ? " (hidden)" : "");
	}

	public String getFullPathLabel() {
		return parent.getFullPathLabel() + PATH_SEP + toString();
	}

	public List<? extends TreeableGalleryItem> loadChildren() {
		return null;
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}

	public Icon getIcon() {
		return imageIcon;
	}

	public URL getPreviewURL() throws MalformedURLException {
		return new URL(imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + imageDetails.getProperty(Gallery.RESPONSE_IMAGE_RESIZED_NAME_INDEXED + imageRefNum));
	}
	
	public String getFullPath() {
		return imageDetails.getProperty(Gallery.RESPONSE_BASEURL) + getName();
	}

	private String getCaption() {
		return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_CAPTION_INDEXED + imageRefNum);
	}

	private String getName() {
		return imageDetails.getProperty(Gallery.RESPONSE_IMAGE_NAME_INDEXED + imageRefNum);
	}

	private boolean isHidden() {
		return Gallery.ARGVAL_YES.equals(imageDetails.getProperty(Gallery.RESPONSE_IMAGE_HIDDEN_INDEXED + imageRefNum));
	}

	public String getLabel() {
		return getCaption();
	}

	public boolean canBeRelabeled() {
		return false;
	}

	public void reLabel(String answer) {
		throw new UnsupportedOperationException("Not supported");
	}

	public boolean canBeDeleted() {
		return false;
	}

	public void delete() {
		throw new UnsupportedOperationException("Not supported");
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URI(getFullPath()));
	}

	public String getType() {
		return IMAGE;
	}

	public TreeableGalleryItem getParent() {
		return parent;
	}

	public boolean canAccept(TreeableGalleryItem childItem, int childIndex) {
		return false;
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
