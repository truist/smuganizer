package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.HideException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class HideAction extends TreeableAction {
	private static final String HIDE = "Hide";
	private static final String SHOW = "Show";
	
	private TreeMenuManager menuManager;
	private Main main;

	public HideAction(TreeMenuManager menuManager, Main main) {
		super(HIDE, "Changing...", main);
		
		this.menuManager = menuManager;
		this.main = main;
	}

	@Override
	protected void performAction() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		try {
			for (TreeableGalleryItem each : currentItems) {
				each.setHidden(getValue(NAME).equals(HIDE));
			}
			DefaultTreeModel model = (DefaultTreeModel)menuManager.getTree().getModel();
			for (TreePath each : menuManager.getTree().getSelectionPaths()) {
				model.nodeChanged((TreeNode)each.getLastPathComponent());
			}
			setEnabled(true);
			super.putValue(NAME, getValue(NAME).equals(SHOW) ? HIDE : SHOW);
		} catch (HideException ex) {
			Logger.getLogger(HideAction.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(main, "Error: show/hide failed.", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void updateState(Iterator<TreeableGalleryItem> currentItems) {
		if (!currentItems.hasNext()) {
			setEnabled(false);
		} else {
			TreeableGalleryItem item = currentItems.next();
			boolean currentState = item.isHidden();
			boolean allChangeable = item.canChangeHiddenStatus(!currentState);
			while (allChangeable && currentItems.hasNext()) {
				item = currentItems.next();
				allChangeable &= (currentState == item.isHidden());
				allChangeable &= item.canChangeHiddenStatus(!currentState);
			}
			setEnabled(allChangeable);
			super.putValue(NAME, currentState ? SHOW : HIDE);
		}
	}

}
