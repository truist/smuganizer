package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GalleryAlbum implements TreeableGalleryItem {
	private static ImageIcon albumIcon = new ImageIcon("lib/images/camera.png");
	
	private Gallery gallery;
	
	private GalleryAlbum parent;
	private ArrayList<GalleryAlbum> subAlbums;
	private ArrayList<GalleryImage> images;
	private boolean loaded;
	
	private String urlName;
	private String title;
	private String description;
	private boolean hidden;

	public GalleryAlbum(Gallery gallery, String urlName, String title, String description) {
		this.gallery = gallery;
		this.urlName = urlName;
		this.title = title;
		this.description = description;
		subAlbums = new ArrayList<GalleryAlbum>();
	}

	void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	void setParent(GalleryAlbum parent) {
		this.parent = parent;
	}

	void addSubAlbum(GalleryAlbum newChild) {
		subAlbums.add(newChild);
	}

	public String getUrlName() {
		return urlName;
	}
	
	public List<GalleryAlbum> getSubAlbums() {
		return subAlbums;
	}

	public List<GalleryImage> getImages() throws IOException {
		if (!loaded) {
			loaded = true;
			this.images = gallery.loadImagesFor(this);
		}
		return images;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public String getFullPathLabel() {
		return (parent != null ? parent.getFullPathLabel() : "") + PATH_SEP + toString();
	}

	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		ArrayList<TreeableGalleryItem> children = new ArrayList<TreeableGalleryItem>();
		children.addAll(getSubAlbums());
		children.addAll(getImages());
		return children;
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}

	public Icon getIcon() {
		return albumIcon;
	}

	public String getLabel() {
		return title;
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
		return false;
	}

	public void launch() throws IOException, URISyntaxException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}

	public String getType() {
		return ALBUM;
	}

	public TreeableGalleryItem getParent() {
		return (parent == null ? gallery : parent);
	}

	public boolean canAccept(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
