package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class RenameException extends SmugException {
	public RenameException(TreeableGalleryItem item, String newName, Throwable cause) throws SmugException {
		super("Error renaming", item, "to \"" + newName + "\"", cause);
	}
}
