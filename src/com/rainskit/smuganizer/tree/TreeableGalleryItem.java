package com.rainskit.smuganizer.tree;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TreeableGalleryItem implements Comparable<TreeableGalleryItem> {
	public enum ItemType { ROOT, CATEGORY, ALBUM, IMAGE }
	
	public static final String PATH_SEP = "/";
	
	protected TreeableGalleryContainer parent;
	
	public TreeableGalleryItem(TreeableGalleryContainer parent) {
		this.parent = parent;
	}

	public abstract ItemType getType();
	
	public final TreeableGalleryContainer getParent() {
		return parent;
	}
	public void setParent(TreeableGalleryContainer newParent) throws IOException {
		this.parent = newParent;
	}
	
	public abstract URL getPreviewURL() throws IOException;
	
	/** The label to show in the Smuganizer tree */
	public abstract String getLabel() throws IOException;
	/** If the file is hidden or password-protected, return something here 
	 * to indicate so.  The string you return here will be appended to the
	 * Label, in the tree.
	 */
	public abstract String getMetaLabel() throws IOException;
	public final String getFullPathLabel() throws IOException {
		return (getParent() != null ? getParent().getFullPathLabel() : "") + PATH_SEP + getLabel();
	}
	/** The file name, for images */
	public abstract String getFileName() throws IOException;
	/** Name to use when generating URLs that point to this file */
	public abstract String getURLName() throws IOException;
	/** The caption to show online; should be 'null' unless the caption is different than the filename */
	public abstract String getCaption() throws IOException;
	/** If the item has a description (larger block of text), return it here */
	public abstract String getDescription() throws IOException;
	
	public final boolean isProtected() throws IOException {
		return isHidden() || hasPassword();
	}
	public final boolean isParentProtected() throws IOException {
		if (parent != null) {
			return parent.isProtected() || parent.isParentProtected();
		} else {
			return false;
		}
	}
	
	public abstract boolean isHidden() throws IOException;
	
	public abstract boolean hasPassword() throws IOException;
	
	public abstract URL getDataURL() throws IOException;
	public abstract boolean canBeLaunched() throws IOException;
	public abstract void launch() throws IOException, URISyntaxException;
	
	@Override
	public final String toString() {
		try {
			return getLabel() + getMetaLabel();
		} catch (IOException ex) {
			Logger.getLogger(TreeableGalleryItem.class.getName()).log(Level.SEVERE, null, ex);
			return "ERROR: " + ex.getMessage();
		}
	}
	
	
	
	
	private int sending;
	private volatile int receiving;
	private boolean hasBeenSent;
	public final void transferStarted(boolean recipient) {
		if (recipient) {
			++receiving;
		} else {
			++sending;
		}
	}
	public final void transferEnded(boolean recipient, boolean succeeded) {
		if (recipient) {
			--receiving;
		} else {
			--sending;
			hasBeenSent = succeeded;
		}
	}
	public final boolean isSending() {
		return (sending > 0);
	}
	public final boolean isReceiving() {
		return (receiving > 0);
	}
	public final boolean hasBeenSent() {
		return hasBeenSent;
	}
}
