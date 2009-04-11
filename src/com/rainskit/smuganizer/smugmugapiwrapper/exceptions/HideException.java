package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class HideException extends SmugException {
	public HideException(TreeableGalleryItem item, Error error) {
		super("Error hiding", item, error);
	}
}
