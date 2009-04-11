package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class RenameException extends SmugException {
	public RenameException(TreeableGalleryItem item, String newName, Error error) {
		super("Error renaming", item, "to \"" + newName + "\"", error);
	}
}
