package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.KeyStroke;

class PreviewAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "preview";
	
	private TreeMenuManager menuManager;

	public PreviewAction(TreeMenuManager menuManager, Main main, JTree tree) {
		super("Preview", "Previewing...", main);
		this.menuManager = menuManager;
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
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
