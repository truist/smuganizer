package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import java.io.File;

public class FileGallery extends DirectoryAlbum {
	
	public FileGallery(File rootDir) {
		super(null, rootDir);
	}

	@Override
	public ItemType getType() {
		return ItemType.ROOT;
	}

	@Override
	public boolean canMoveLocally(TreeableGalleryItem item, int childIndex) {
		return false;
	}

	@Override
	public void moveItemLocally(TreeableGalleryItem item, int childIndex, TransferInterruption previousInterruption) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean canBeDeleted() {
		return false;
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Not supported.");
	}
}
