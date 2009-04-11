package com.rainskit.smuganizer.tree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;

public interface TreeableGalleryItem extends Comparable<TreeableGalleryItem> {
	public static final String CATEGORY = "category";
	public static final String ALBUM = "album";
	public static final String IMAGE = "image";
	public static final String PATH_SEP = "/";

	public String getFullPathLabel();
	public Icon getIcon();
	public URL getPreviewURL() throws MalformedURLException;
	public List<? extends TreeableGalleryItem> loadChildren() throws IOException;

	public String getType();
	public TreeableGalleryItem getParent();
	public boolean canAccept(TreeableGalleryItem childItem, int childIndex);
	public void receiveChild(TreeableGalleryItem childItem, int childIndex);
	
	public String getLabel();
	public boolean canBeRelabeled();
	public void reLabel(String answer);
	public boolean canBeDeleted();
	public void delete();
	public boolean canBeLaunched();
	public void launch() throws IOException, URISyntaxException;
}
