package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.DeleteException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

class DeleteAction extends TreeableAction {
	private TreeMenuManager menuManager;
	private Main main;

	public DeleteAction(TreeMenuManager menuManager, Main main) {
		super("Delete...", "Deleting...", main);
		this.menuManager = menuManager;
		this.main = main;
	}

	protected void performAction() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		String itemLabel = (currentItems.size() > 1 ? "these items" : "\"" + currentItems.get(0).getLabel() + "\"");
		int answer = JOptionPane.showConfirmDialog(main, 
						"Are you sure you want to delete " + itemLabel + "?", 
						"Confirm delete", 
						JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			try {
				for (TreeableGalleryItem each : currentItems) {
					each.delete();
				}
				TreePath[] selectionPaths = menuManager.getTree().getSelectionPaths();
				for (TreePath each : selectionPaths) {
					((DefaultTreeModel)menuManager.getTree().getModel()).removeNodeFromParent((MutableTreeNode)each.getLastPathComponent());
				}
				menuManager.getTree().setSelectionPath(selectionPaths[0].getParentPath());
			} catch (DeleteException ex) {
				Logger.getLogger(DeleteAction.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(main, "Error: delete failed.  (Note: standard SmugMug categories cannot be deleted.)", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public void updateState(Iterator<TreeableGalleryItem> currentItems) {
		boolean allDeletable = currentItems.hasNext();
		while (currentItems.hasNext()) {
			allDeletable &= currentItems.next().canBeDeleted();
		}
		setEnabled(allDeletable);
	}
}
