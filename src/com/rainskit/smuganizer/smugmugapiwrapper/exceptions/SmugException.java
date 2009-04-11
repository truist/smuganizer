package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;

public class SmugException extends RuntimeException {
	public SmugException(String description, Error error) {
		super(description + "; " + error.toString());
	}
	
	public SmugException(String prefix, TreeableGalleryItem item, Error error) {
		this(prefix + " \"" + item.getFullPathLabel() + "\"", error);
	}
	
	public SmugException(String prefix, TreeableGalleryItem item, String suffix, Error error) {
		this(prefix + " \"" + item.getFullPathLabel() + "\" " + suffix, error);
	}
}
