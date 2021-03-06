package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class DeleteAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "delete";
	
	public DeleteAction(TreeMenuManager menuManager, Smuganizer main, JTree tree) {
		super(main, menuManager, "Delete...", "Deleting...");
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
	}

	protected void performAction() throws IOException {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		String itemLabel = (currentItems.size() > 1 ? "these items" : "\"" + currentItems.get(0).getLabel() + "\"");
		int answer = JOptionPane.showConfirmDialog(main, 
						"Are you sure you want to delete " + itemLabel + "?", 
						"Confirm delete", 
						JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			try {
				for (TreeableGalleryItem each : currentItems) {
					((WriteableTreeableGalleryItem)each).delete();
				}
				TreePath[] selectionPaths = menuManager.getTree().getSelectionPaths();
				for (TreePath each : selectionPaths) {
					((DefaultTreeModel)menuManager.getTree().getModel()).removeNodeFromParent((MutableTreeNode)each.getLastPathComponent());
				}
				menuManager.getTree().setSelectionPath(selectionPaths[0].getParentPath());
			} catch (IOException ex) {
				Logger.getLogger(DeleteAction.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(main, "Error: delete failed.", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		boolean allDeletable = currentItems.size() > 0;
		try {
			for (TreeableGalleryItem each : currentItems) {
				allDeletable &= (each instanceof WriteableTreeableGalleryItem && ((WriteableTreeableGalleryItem) each).canBeDeleted());
			}
		} catch (IOException ex) {
			Logger.getLogger(DeleteAction.class.getName()).log(Level.SEVERE, null, ex);
			allDeletable = false;
		}
		setEnabled(allDeletable);
	}
}
