package com.rainskit.smuganizer.tree;

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
	protected boolean transferActive;
	protected volatile int transferRecipient;
	protected boolean hasBeenTransferred;
	
	protected TreeableGalleryItem(TreeableGalleryItem parent) {
		this.parent = parent;
	}

	public abstract List<? extends TreeableGalleryItem> loadChildren() throws IOException;
	
	public abstract String getType();
	public TreeableGalleryItem getParent() {
		return parent;
	}
	public abstract void removeChild(TreeableGalleryItem child);
	
	public abstract boolean canMove(TreeableGalleryItem item, int childIndex);
	public abstract void moveItem(TreeableGalleryItem item, int childIndex);
	public abstract boolean canImport(TreeableGalleryItem newItem, int childIndex);
	public abstract TreeableGalleryItem importItem(TreeableGalleryItem newItem, int childIndex) throws IOException;
	
	public final void setTransferActive(boolean active) {
		this.transferActive = active;
		this.hasBeenTransferred |= active;
	}
	public final boolean isTransferActive() {
		return transferActive;
	}
	public final void setTransferRecipient(boolean active) {
		if (active) {
			++transferRecipient;
		} else {
			--transferRecipient;
		}
	}
	public final boolean isTransferRecipient() {
		return (transferRecipient > 0);
	}
	public final void setHasBeenTransferred(boolean hasBeenTransferred) {
		this.hasBeenTransferred = hasBeenTransferred;
	}
	public final boolean hasBeenTransferred() {
		return hasBeenTransferred;
	}
	
	/** The label to show in the Smuganizer tree */
	public abstract String getLabel();
	public abstract String getMetaLabel();
	public abstract boolean canBeRelabeled();
	public abstract void reLabel(String answer);
	public final String getFullPathLabel() {
		return (getParent() != null ? getParent().getFullPathLabel() : "") + PATH_SEP + getLabel();
	}
	/** The file name, for images */
	public abstract String getName();
	/** The caption to show online; should be 'null' unless the caption is different than the filename */
	public abstract String getCaption();
	
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
	
	public abstract URL getDataURL() throws MalformedURLException;
	public abstract URL getPreviewURL() throws MalformedURLException;
	public abstract boolean canBeLaunched();
	public abstract void launch() throws IOException, URISyntaxException;
	
	@Override
	public final String toString() {
		return getLabel() + getMetaLabel();
	}
}
