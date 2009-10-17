package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
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
	public void setParent(TreeableGalleryContainer newParent) throws SmugException {
		this.parent = newParent;
	}
	
	public abstract URL getPreviewURL() throws IOException;
	
	/** The label to show in the Smuganizer tree */
	public abstract String getLabel() throws SmugException;
	/** If the file is hidden or password-protected, return something here 
	 * to indicate so.  The string you return here will be appended to the
	 * Label, in the tree.
	 */
	public abstract String getMetaLabel();
	public final String getFullPathLabel() throws SmugException {
		return (getParent() != null ? getParent().getFullPathLabel() : "") + PATH_SEP + getLabel();
	}
	/** The file name, for images */
	public abstract String getFileName();
	/** Name to use when generating URLs that point to this file */
	public abstract String getURLName();
	/** The caption to show online; should be 'null' unless the caption is different than the filename */
	public abstract String getCaption();
	/** If the item has a description (larger block of text), return it here */
	public abstract String getDescription();
	
	public final boolean isProtected() {
		return isHidden() || hasPassword();
	}
	public final boolean isParentProtected() {
		if (parent != null) {
			return parent.isProtected() || parent.isParentProtected();
		} else {
			return false;
		}
	}
	
	public abstract boolean isHidden();
	
	public abstract boolean hasPassword();
	
	public abstract URL getDataURL() throws IOException;
	public abstract boolean canBeLaunched();
	public abstract void launch() throws IOException, URISyntaxException;
	
	@Override
	public final String toString() {
		try {
			return getLabel() + getMetaLabel();
		} catch (SmugException ex) {
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
