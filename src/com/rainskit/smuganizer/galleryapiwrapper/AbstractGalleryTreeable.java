package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.TransferInterruption;

public abstract class AbstractGalleryTreeable extends TreeableGalleryItem {
	public AbstractGalleryTreeable(TreeableGalleryItem parent) {
		super(parent);
	}

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return false;
	}

	@Override
	public final void setHidden(boolean hidden) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean canChangePassword(boolean newState) {
		return false;
	}

	@Override
	public void setPassword(String password, String passwordHint) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public final boolean canMove(TreeableGalleryItem childItem, int childIndex) {
		return false;
	}

	public final void moveItem(TreeableGalleryItem childItem, int childIndex, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean canImport(TreeableGalleryItem newItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, int childIndex, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public final boolean canBeDeleted() {
		return false;
	}

	public final boolean canBeRelabeled() {
		return false;
	}

	public final void delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public final void reLabel(String answer) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeChild(TreeableGalleryItem child) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
