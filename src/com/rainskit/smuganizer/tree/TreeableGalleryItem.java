package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.SmugAlbum;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public abstract class TreeableGalleryItem implements Comparable<TreeableGalleryItem> {
	public static final String CATEGORY = "category";
	public static final String ALBUM = "album";
	public static final String IMAGE = "image";
	public static final String PATH_SEP = "/";
	
	protected TreeableGalleryItem parent;
	
	protected TreeableGalleryItem(TreeableGalleryItem parent) {
		this.parent = parent;
	}

	public abstract List<? extends TreeableGalleryItem> loadChildren() throws IOException;
	
	public abstract String getType();
	public TreeableGalleryItem getParent() {
		return parent;
	}
	public abstract void removeChild(TreeableGalleryItem child);
	
	public abstract boolean canAccept(TreeableGalleryItem childItem, int childIndex);
	public abstract void receiveChild(TreeableGalleryItem childItem, int childIndex);
	
	public abstract String getLabel();
	public abstract String getMetaLabel();
	public abstract boolean canBeRelabeled();
	public abstract void reLabel(String answer);
	
	public abstract boolean canBeDeleted();
	public abstract void delete();
	
	public final boolean isProtected() {
		return isHidden() || hasPassword();
	}
	
	public final boolean isParentProtected() {
		TreeableGalleryItem parent = getParent();
		if (parent != null) {
			return parent.isProtected() || parent.isParentProtected();
		} else {
			return false;
		}
	}
	
	public abstract boolean isHidden();
	public abstract boolean canChangeHiddenStatus(boolean newState);
	public abstract void setHidden(boolean hidden);
	
	public abstract boolean hasPassword();
	public abstract boolean canChangePassword(boolean newState);
	public abstract void setPassword(String password, String passwordHint);
	
	public abstract URL getPreviewURL() throws MalformedURLException;
	public abstract boolean canBeLaunched();
	public abstract void launch() throws IOException, URISyntaxException;
	
	public final String getFullPathLabel() {
		return (getParent() != null ? getParent().getFullPathLabel() : "") + PATH_SEP + getLabel();
	}

	@Override
	public final String toString() {
		return getLabel() + getMetaLabel();
	}
}
