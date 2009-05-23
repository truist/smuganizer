package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.Iterator;

class PreviewAction extends TreeableAction {
	private TreeMenuManager menuManager;

	public PreviewAction(TreeMenuManager menuManager, Main main) {
		super("Preview", "Previewing...", main);
		this.menuManager = menuManager;
	}

	@Override
	protected void performAction() {
		main.showImageWindow();
	}

	@Override
	public void updateState(Iterator<TreeableGalleryItem> currentItems) {
		int itemCount = 0;
		while (currentItems.hasNext()) {
			itemCount++;
			if (!TreeableGalleryItem.IMAGE.equals(currentItems.next().getType())) {
				setEnabled(false);
				return;
			}
		}
		setEnabled(itemCount == 1);
	}

}
