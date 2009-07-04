package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class MixedAlbumException extends RuntimeException {

	public MixedAlbumException(TreeableGalleryItem sourceItem) {
		super("This album (\""+ sourceItem.getFullPathLabel() + "\") has both sub-albums and child images in the same album.\n"
			+ "SmugMug does not support such a configuration.\n"
			+ "Please copy these items over individually.");
	}

}
