package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TreeableGalleryItem implements Comparable<TreeableGalleryItem> {

	public enum ItemType { ROOT, CATEGORY, ALBUM, IMAGE }
	
	public static final String PATH_SEP = "/";
	
	protected TreeableGalleryItem parent;
	private int sending;
	private volatile int receiving;
	private boolean hasBeenSent;
	
	protected TreeableGalleryItem(TreeableGalleryItem parent) {
		this.parent = parent;
	}

	public abstract List<? extends TreeableGalleryItem> loadChildren() throws IOException;
	public abstract List<? extends TreeableGalleryItem> getChildren();
	
	public abstract ItemType getType();
	public TreeableGalleryItem getParent() {
		return parent;
	}
	public void setParent(TreeableGalleryItem newParent) throws SmugException {
		this.parent = newParent;
	}
	public abstract void childRemoved(TreeableGalleryItem child);
	
	public abstract boolean canMoveLocally(TreeableGalleryItem newChild, int childIndex);
	public abstract void moveItemLocally(TreeableGalleryItem newChild, int childIndex, TransferInterruption previousInterruption) throws SmugException;
	public abstract boolean canImport(TreeableGalleryItem newItem);
	public abstract TreeableGalleryItem importItem(TreeableGalleryItem newItem, TransferInterruption previousInterruption) throws IOException, TransferInterruption;
	
	public final int getSubAlbumDepth() {
		int subDepth = 0;
		boolean hasChildAlbums = false;
		List<? extends TreeableGalleryItem> children = getChildren();
		if (children != null) {
			for (TreeableGalleryItem eachChild : children) {
				if (eachChild.getType() == ItemType.ALBUM) {
					hasChildAlbums = true;
					subDepth = Math.max(subDepth, eachChild.getSubAlbumDepth());
				}
			}
		}
		return (hasChildAlbums ? 1 : 0) + subDepth;
	}

	public final boolean hasImageChildren() {
		List<? extends TreeableGalleryItem> children = getChildren();
		if (children != null) {
			for (TreeableGalleryItem eachChild : children) {
				if (ItemType.IMAGE == eachChild.getType()) {
					return true;
				}
			}
		}
		return false;
	}

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
	
	/** The label to show in the Smuganizer tree */
	public abstract String getLabel() throws SmugException;
	/** If the file is hidden or password-protected, return something here 
	 * to indicate so.  The string you return here will be appended to the
	 * Label, in the tree.
	 */
	public abstract String getMetaLabel();
	public abstract boolean canBeRelabeled();
	public abstract void reLabel(String answer) throws SmugException;
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
	
	public abstract boolean canBeDeleted();
	public abstract void delete() throws SmugException;
	
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
	public abstract void setHidden(boolean hidden) throws SmugException;
	
	public abstract boolean hasPassword();
	public abstract boolean canChangePassword(boolean newState);
	public abstract void setPassword(String password, String passwordHint) throws SmugException;
	
	public abstract URL getDataURL() throws IOException;
	public abstract URL getPreviewURL() throws IOException;
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
}
