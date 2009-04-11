package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.GallerySettings;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GalleryAlbum extends AbstractGalleryTreeable {
	private Gallery gallery;
	
	private GalleryAlbum parent;
	private ArrayList<GalleryAlbum> subAlbums;
	private ArrayList<GalleryImage> images;
	private boolean loaded;
	
	private String urlName;
	private String title;
	private String description;
	private Boolean hidden;
	private int albumRefNum;

	public GalleryAlbum(Gallery gallery, String urlName, String title, String description, int albumRefNum) {
		this.gallery = gallery;
		this.urlName = urlName;
		this.title = title;
		this.description = description;
		this.albumRefNum = albumRefNum;
		
		subAlbums = new ArrayList<GalleryAlbum>();
	}

	void setParent(GalleryAlbum parent) {
		this.parent = parent;
	}

	void addSubAlbum(GalleryAlbum newChild) {
		subAlbums.add(newChild);
	}

	String getUrlName() {
		return urlName;
	}
	
	private List<GalleryImage> getImages() throws IOException {
		if (!loaded) {
			loaded = true;
			this.images = gallery.loadImagesFor(this);
		}
		return images;
	}
	
	public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		if (GallerySettings.getCheckProtectedAlbums()) {
			hidden = Boolean.valueOf(gallery.isAlbumHidden(GalleryAlbum.this));
		}
		
		ArrayList<TreeableGalleryItem> children = new ArrayList<TreeableGalleryItem>();
		children.addAll(subAlbums);
		children.addAll(getImages());
		return children;
	}

	public String getLabel() {
		return title;
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(Gallery.generateUrlFor(getUrlName(), null));
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

	public int compareTo(TreeableGalleryItem other) {
		if (other instanceof GalleryAlbum) {
			return Integer.valueOf(albumRefNum).compareTo(Integer.valueOf(((GalleryAlbum)other).albumRefNum));
		} else {
			return 0;
		}
	}
	public String getMetaLabel() {
		return (isProtected() ? " [protected]" : "");
	}

	@Override
	public boolean isProtected() {
		if (hidden == null) {
			return false;
		} else {
			return hidden.booleanValue();
		}
	}
}
