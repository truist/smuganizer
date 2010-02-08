package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.tree.*;
import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.tree.transfer.SmugTransferHandler;
import java.io.IOException;

public class GalleryTree extends TransferTree {

	public GalleryTree(Main main) {
		super(main, Gallery.loginHttpClient);
	}
	
	public void loadTree(TreeableGalleryItem root) throws IOException {
		loadTreeImpl(root, true);
	}

	@Override
	public int getSourceActions() {
		return SmugTransferHandler.COPY;
	}

	@Override
	public boolean supportsImport() {
		return false;
	}

	@Override
	public boolean supportsInsertAtSpecificLocation() {
		return false;
	}
}
