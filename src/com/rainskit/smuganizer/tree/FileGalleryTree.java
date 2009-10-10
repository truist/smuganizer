package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import java.io.IOException;

public class FileGalleryTree extends TransferTree {
	
	public FileGalleryTree(Main main) {
		super(main);
	}

	@Override
	public void loadTree(TreeableGalleryItem root) throws IOException {
		loadTreeImpl(root, false);
	}

	@Override
	public int getSourceActions() {
		return SmugTransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean canImport() {
		return true;
	}

	@Override
	public boolean canInsertAtSpecificLocation() {
		return false;
	}
}
