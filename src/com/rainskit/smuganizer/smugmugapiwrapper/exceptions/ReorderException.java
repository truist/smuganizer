package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class ReorderException extends SmugException {
	public ReorderException(TreeableGalleryItem item, Error error) {
		super("Error changing the position of", item, error);
	}
}
