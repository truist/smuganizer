package com.rainskit.smuganizer.filesystemapiwrapper;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.File;

public abstract class AbstractFileGalleryItem extends TreeableGalleryItem {
	protected File myFile;
	
	public AbstractFileGalleryItem(AbstractFileGalleryItem parent) {
		super(parent);
	}
	
	protected boolean moveSelfTo(File newLocation) {
		if (myFile.renameTo(newLocation)) {
			myFile = newLocation;
			return true;
		} else {
			return false;
		}
	}
}
