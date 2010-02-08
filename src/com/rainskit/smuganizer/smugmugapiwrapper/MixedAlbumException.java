package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;

public class MixedAlbumException extends IOException {

	public MixedAlbumException(TreeableGalleryItem sourceItem) throws IOException {
		super("This album (\""+ sourceItem.getFullPathLabel() + "\") has both sub-albums and child images in the same album.\n"
			+ "SmugMug does not support such a configuration.\n"
			+ "Please copy these items over individually.");
	}

}
