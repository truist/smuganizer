package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.PasswordException;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

class PasswordAction extends TreeableAction {
	private static final String ADD = "Add password...";
	private static final String REMOVE = "Remove password";
	
	private TreeMenuManager menuManager;
	private Main main;
	
	public PasswordAction(TreeMenuManager menuManager, Main main) {
		super(ADD, "Changing password...", main);
		this.menuManager = menuManager;
		this.main = main;
	}

	@Override
	protected void performAction() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		try {
			String password = null;;
			if (getValue(NAME).equals(ADD)) {
				password = JOptionPane.showInputDialog(main, "Please enter a password", "Enter password", JOptionPane.QUESTION_MESSAGE);
				if (password == null) {
					return;
				}
			} 
			for (TreeableGalleryItem each : currentItems) {
				each.setPassword(password,null);
			}
			
			DefaultTreeModel model = (DefaultTreeModel)menuManager.getTree().getModel();
			for (TreePath each : menuManager.getTree().getSelectionPaths()) {
				model.nodeChanged((TreeNode)each.getLastPathComponent());
			}
			setEnabled(true);
			super.putValue(NAME, getValue(NAME).equals(ADD) ? REMOVE : ADD);
		} catch (PasswordException ex) {
			Logger.getLogger(HideAction.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(main, "Error: password change failed.", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void updateState(Iterator<TreeableGalleryItem> currentItems) {
		if (!currentItems.hasNext()) {
			setEnabled(false);
		} else {
			TreeableGalleryItem item = currentItems.next();
			boolean currentState = item.hasPassword();
			boolean allChangeable = item.canChangePassword(!currentState);
			while (allChangeable && currentItems.hasNext()) {
				item = currentItems.next();
				allChangeable &= (currentState == item.hasPassword());
				allChangeable &= item.canChangePassword(!currentState);
			}
			setEnabled(allChangeable);
			super.putValue(NAME, currentState ? REMOVE : ADD);
		}
	}

}
