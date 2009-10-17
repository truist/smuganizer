package com.rainskit.smuganizer.tree;

import java.io.IOException;
import java.util.List;

public abstract class TreeableGalleryContainer extends TreeableGalleryItem {

	protected TreeableGalleryContainer(TreeableGalleryContainer parent) {
		super(parent);
	}

	public abstract List<? extends TreeableGalleryItem> loadChildren() throws IOException;
	public abstract List<? extends TreeableGalleryItem> getChildren();
	
	public final int getSubAlbumDepth() {
		int subDepth = 0;
		boolean hasChildAlbums = false;
		List<? extends TreeableGalleryItem> children = getChildren();
		if (children != null) {
			for (TreeableGalleryItem eachChild : children) {
				if (eachChild.getType() == ItemType.ALBUM) {
					hasChildAlbums = true;
					subDepth = Math.max(subDepth, ((TreeableGalleryContainer)eachChild).getSubAlbumDepth());
				}
			}
		}
		return (hasChildAlbums ? 1 : 0) + subDepth;
	}

	public final boolean hasImageChildren() {
		List<? extends TreeableGalleryItem> children = getChildren();
		if (children != null) {
			for (TreeableGalleryItem eachChild : children) {
				if (ItemType.IMAGE == eachChild.getType()) {
					return true;
				}
			}
		}
		return false;
	}

	protected String constructFileName(String fileName, String caption, boolean rename) {
		String constructedName;
		if (caption == null) {
			constructedName = fileName;
		} else {
			constructedName = caption + getExtension(fileName);
		}
		if (rename) {
			int i = 0;
			String possibleFileName;
			do {
				possibleFileName = getBaseName(constructedName) + " (" + ++i + ")" + getExtension(constructedName);
			} while (((WriteableTreeableGalleryContainer)this).willChildBeDuplicate(possibleFileName, null));
			return possibleFileName;
		} else {
			return constructedName;
		}
	}
	
	private String getBaseName(String fileName) {
		int lastPeriod = fileName.lastIndexOf('.');
		return (lastPeriod > -1 ? fileName.substring(0, lastPeriod) : "");
	}
	
	private String getExtension(String fileName) {
		int lastPeriod = fileName.lastIndexOf('.');
		return (lastPeriod > -1 ? fileName.substring(lastPeriod) : "");
	}
}
