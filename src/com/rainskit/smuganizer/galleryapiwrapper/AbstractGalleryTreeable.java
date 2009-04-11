package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public abstract class AbstractGalleryTreeable extends TreeableGalleryItem {

	@Override
	public boolean canChangeHiddenStatus(boolean newState) {
		return false;
	}

	@Override
	public final void setHidden(boolean hidden) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public final boolean canAccept(TreeableGalleryItem childItem, int childIndex) {
		return false;
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

	public final void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
