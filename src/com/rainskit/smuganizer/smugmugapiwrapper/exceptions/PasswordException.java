package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class PasswordException extends SmugException {
	public PasswordException(TreeableGalleryItem item, Error error) {
		super("Error changing password", item, error);
	}

}
