package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask.HandleDuplicate;
import java.io.IOException;
import java.util.List;

public abstract class TreeableGalleryContainer extends TreeableGalleryItem {

	protected TreeableGalleryContainer(TreeableGalleryContainer parent) {
		super(parent);
	}

	public abstract List<? extends TreeableGalleryItem> loadChildren() throws IOException;
	public abstract List<? extends TreeableGalleryItem> getChildren() throws IOException;
	
	public final int getSubAlbumDepth() throws IOException {
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

	public final boolean hasImageChildren() throws IOException {
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

    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    protected String cleanUpName(String newName, HandleDuplicate rename, boolean stripIllegals) throws IOException {
        if (rename == HandleDuplicate.RENAME) {
			int i = -1;
            String baseName = getBaseName(newName);
            String extension = getExtension(newName);
            while (true) {
                newName = baseName + (++i == 0 ? "" : " (" + i + ")") + extension;
                if (!((WriteableTreeableGalleryContainer)this).willChildBeDuplicate(newName, null)) {
                    break;
                }
            }
        }

        if (stripIllegals) {
            for (char e : ILLEGAL_CHARACTERS) {
                newName = newName.replace(e, '-');
            }
        }

        return newName;
    }
	
	protected String getBaseName(String fileName) {
		int lastPeriod = fileName.lastIndexOf('.');
		return (lastPeriod > -1 ? fileName.substring(0, lastPeriod) : fileName);
	}
	
	protected String getExtension(String fileName) {
		int lastPeriod = fileName.lastIndexOf('.');
		return (lastPeriod > -1 ? fileName.substring(lastPeriod) : "");
	}
}
