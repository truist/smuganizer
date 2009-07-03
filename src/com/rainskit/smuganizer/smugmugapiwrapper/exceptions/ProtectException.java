package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class ProtectException extends SmugException {
	public ProtectException(TreeableGalleryItem item, Error error) {
		super("Unable to protect item", item, error);
	}
}
