package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.menu.gui.ExifBox;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;

public class ExifAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "load_exif";
	
	public ExifAction(TreeMenuManager menuManager, Main main, JTree tree) {
		super(main, menuManager, "View EXIF headers", "Loading EXIF headers...");
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
	}

	@Override
	protected void performAction() {
		try {
			new ExifBox(main, menuManager.getCurrentItems().get(0)).setVisible(true);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(main, "Error: " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		setEnabled(currentItems.size() == 1 && ItemType.IMAGE == currentItems.get(0).getType());
	}
}
