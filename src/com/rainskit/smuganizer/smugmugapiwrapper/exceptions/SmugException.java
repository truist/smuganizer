package com.rainskit.smuganizer.smugmugapiwrapper.exceptions;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;

public class SmugException extends IOException {
	public SmugException(String description, Throwable cause) {
		super(description, cause);
	}
	
	public SmugException(String prefix, TreeableGalleryItem item, Throwable cause) throws SmugException {
		this(prefix + " \"" + item.getFullPathLabel() + "\"", cause);
	}
	
	public SmugException(String prefix, TreeableGalleryItem item, String suffix, Throwable cause) throws SmugException {
		this(prefix + " \"" + item.getFullPathLabel() + "\" " + suffix, cause);
	}
	
	public static RuntimeException convertError(com.kallasoft.smugmug.api.json.AbstractResponse.Error error) {
		return new RuntimeException(error.getMessage() + " {" + error.getCode().toString() + "|");
	}
}
