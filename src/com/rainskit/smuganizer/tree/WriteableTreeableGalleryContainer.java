package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.ModifiedItemAttributes;

public interface WriteableTreeableGalleryContainer extends WriteableTreeableGalleryItem {
	
	public boolean canMoveLocally(TreeableGalleryItem childItem, int childIndex);
	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws SmugException;

	public boolean canImport(TreeableGalleryItem newItem);
	public boolean willChildBeDuplicate(String fileName, String caption);
	public boolean allowsDuplicateChildren();
	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedAttributes) throws SmugException;
	
	public void childRemoved(TreeableGalleryItem child);
}
