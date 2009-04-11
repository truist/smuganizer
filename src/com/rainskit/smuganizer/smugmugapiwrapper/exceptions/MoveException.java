package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class MoveException extends SmugException {
	public MoveException(TreeableGalleryItem item, Error error) {
		super("Error moving", item, error);
	}
}
