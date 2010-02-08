package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.ModifiedItemAttributes;
import java.io.IOException;

public interface WriteableTreeableGalleryContainer extends WriteableTreeableGalleryItem {
	
	public boolean canMoveLocally(TreeableGalleryItem childItem, int childIndex) throws IOException;
	public void moveItemLocally(TreeableGalleryItem childItem, int childIndex, ModifiedItemAttributes modifiedItemAttributes) throws IOException;

	public boolean canImport(TreeableGalleryItem newItem) throws IOException;
	public boolean willChildBeDuplicate(String fileName, String caption) throws IOException;
	public boolean allowsDuplicateChildren();
	public TreeableGalleryItem importItem(TreeableGalleryItem newItem, ModifiedItemAttributes modifiedAttributes) throws IOException;
	
	public void childRemoved(TreeableGalleryItem child);
}
