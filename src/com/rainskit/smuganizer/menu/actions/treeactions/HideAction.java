package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class HideAction extends TreeableAction {
	private static final String HIDE = "Hide";
	private static final String SHOW = "Un-Hide";
	
	public HideAction(TreeMenuManager menuManager, Main main) {
		super(main, menuManager, HIDE, "Changing...");
	}

	@Override
	protected void performAction() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		try {
			for (TreeableGalleryItem each : currentItems) {
				((WriteableTreeableGalleryItem)each).setHidden(getValue(NAME).equals(HIDE));
			}
			DefaultTreeModel model = (DefaultTreeModel)menuManager.getTree().getModel();
			for (TreePath each : menuManager.getTree().getSelectionPaths()) {
				model.nodeChanged((TreeNode)each.getLastPathComponent());
			}
			setEnabled(true);
			putValue(NAME, getValue(NAME).equals(SHOW) ? HIDE : SHOW);
		} catch (SmugException ex) {
			Logger.getLogger(HideAction.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(main, "Error: show/hide failed.", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		if (currentItems.size() == 0) {
			setEnabled(false);
		} else {
			TreeableGalleryItem item = currentItems.get(0);
			boolean currentState = item.isHidden();
			boolean allChangeable = false;
			if (item instanceof WriteableTreeableGalleryItem) {
				allChangeable = ((WriteableTreeableGalleryItem)item).canChangeHiddenStatus(!currentState);
				for (int i = 1; i < currentItems.size(); i++) {
					item = currentItems.get(i);
					if (item instanceof WriteableTreeableGalleryItem) {
						allChangeable &= (currentState == item.isHidden());
						allChangeable &= ((WriteableTreeableGalleryItem)item).canChangeHiddenStatus(!currentState);
					} else {
						allChangeable = false;
						break;
					}
				}
			}
			setEnabled(allChangeable);
			putValue(NAME, currentState ? SHOW : HIDE);
		}
	}

}
