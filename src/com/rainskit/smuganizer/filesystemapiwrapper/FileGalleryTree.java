package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.tree.*;
import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import java.io.IOException;

public class FileGalleryTree extends TransferTree {
	
	public FileGalleryTree(Smuganizer main) {
		super(main, null);
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
	public boolean supportsImport() {
		return true;
	}

	@Override
	public boolean supportsInsertAtSpecificLocation() {
		return false;
	}
}
