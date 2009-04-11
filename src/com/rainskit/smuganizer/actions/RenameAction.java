package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class RenameAction extends TreeableAction {
	private TreeMenuManager menuManager;
	private Main main;

	public RenameAction(TreeMenuManager menuManager, Main main) {
		super("Rename...", "Renaming...", main);
		this.menuManager = menuManager;
		this.main = main;
	}

	protected void performAction() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		String prompt;
		String defaultAnswer;
		if (currentItems.size() > 1) {
			prompt = "these " + currentItems.size() + " items";
			defaultAnswer = "";
		} else {
			prompt = "\"" + currentItems.get(0).getLabel() + "\"";
			defaultAnswer = currentItems.get(0).getLabel();
		}
		String answer = (String)JOptionPane.showInputDialog(main, 
									"Rename " + prompt + " to:", 
									"New title / caption?", 
									JOptionPane.QUESTION_MESSAGE, 
									null, 
									null, 
									defaultAnswer);
		if (answer != null) {
			try {
				for (TreeableGalleryItem each : currentItems) {
					each.reLabel(answer);
				}
			} catch (RenameException ex) {
				Logger.getLogger(RenameAction.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(main, "Error: rename failed.  (Note: standard SmugMug categories cannot be renamed.)", "Error", JOptionPane.WARNING_MESSAGE);
			}
			DefaultTreeModel model = (DefaultTreeModel)menuManager.getTree().getModel();
			for (TreePath each : menuManager.getTree().getSelectionPaths()) {
				model.nodeChanged((TreeNode)each.getLastPathComponent());
			}
		}
	}

	public void updateState(Iterator<TreeableGalleryItem> currentItems) {
		boolean allRenameable = currentItems.hasNext();
		while (currentItems.hasNext()) {
			allRenameable &= currentItems.next().canBeRelabeled();
		}
		setEnabled(allRenameable);
	}
}
