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
import org.apache.commons.lang.StringEscapeUtils;

public class GalleryAlbum extends AbstractGalleryTreeable {
	private Gallery gallery;
	
	private ArrayList<GalleryAlbum> subAlbums;
	private ArrayList<GalleryImage> images;
	private boolean loaded;
	
	private String urlName;
	private String title;
	private String description;
	private Boolean albumProtected;
	private int albumRefNum;

	public GalleryAlbum(Gallery gallery, String urlName, String title, String description, int albumRefNum) {
		super(null);
		
		this.gallery = gallery;
		this.urlName = urlName;
		this.title = StringEscapeUtils.unescapeHtml(title);
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

	public String getName() {
		return urlName;
	}
	
	public String getCaption() {
		return null;
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
			albumProtected = Boolean.valueOf(gallery.isAlbumProtected(GalleryAlbum.this));
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
		Desktop.getDesktop().browse(Gallery.generateUrlFor(getName(), null));
	}

	public ItemType getType() {
		return ItemType.ALBUM;
	}

	@Override
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
		return (hasPassword() ? " [protected]" : "");
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean hasPassword() {
		if (albumProtected == null) {
			return false;
		} else {
			return albumProtected.booleanValue();
		}
	}

	@Override
	public URL getDataURL() throws MalformedURLException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}
}
