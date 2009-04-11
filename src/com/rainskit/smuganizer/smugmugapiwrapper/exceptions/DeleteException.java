package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class DeleteException extends SmugException {
	public DeleteException(TreeableGalleryItem item, Error error) {
		super("Error deleting", item, error);
	}
}
