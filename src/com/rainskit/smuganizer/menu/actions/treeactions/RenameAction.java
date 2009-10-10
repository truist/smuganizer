package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.RenameException;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class RenameAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "rename";
	
	public RenameAction(TreeMenuManager menuManager, Main main, JTree tree) {
		super(main, menuManager, "Rename...", "Renaming...");
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
	}

	protected void performAction() throws SmugException {
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

	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		boolean allRenameable = currentItems.size() > 0;
		for (TreeableGalleryItem each : currentItems) {
			allRenameable &= each.canBeRelabeled();
		}
		setEnabled(allRenameable);
	}
}
