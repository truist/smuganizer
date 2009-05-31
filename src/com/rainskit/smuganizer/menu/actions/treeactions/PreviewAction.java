package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.menu.TreeMenuManager;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.KeyStroke;

public class PreviewAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "preview";
	
	public PreviewAction(TreeMenuManager menuManager, Main main, JTree tree) {
		super(main, menuManager, "Preview", "Previewing...");
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
	}

	@Override
	protected void performAction() {
		main.showImageWindow();
	}

	@Override
	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		setEnabled(currentItems.size() == 1 && ItemType.IMAGE == currentItems.get(0).getType());
	}
}
