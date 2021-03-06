package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.menu.gui.SetPasswordDialog;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class PasswordAction extends TreeableAction {
	private static final String ADD = "Add password...";
	private static final String REMOVE = "Remove password";
	
	public PasswordAction(TreeMenuManager menuManager, Smuganizer main) {
		super(main, menuManager, ADD, "Changing password...");
	}

	@Override
	protected void performAction() throws IOException {
		try {
			String password = null;
			String hint = null;
			if (getValue(NAME).equals(ADD)) {
				SetPasswordDialog passwordDialog = new SetPasswordDialog(main);
				passwordDialog.setVisible(true);
				if (passwordDialog.closedWithOk()) {
					password = passwordDialog.getPassword();
					hint = passwordDialog.getHint();
				} else {
					return;
				}
			} 
			for (TreeableGalleryItem each : menuManager.getCurrentItems()) {
				((WriteableTreeableGalleryItem)each).setPassword(password, hint);
			}
			
			DefaultTreeModel model = (DefaultTreeModel)menuManager.getTree().getModel();
			for (TreePath each : menuManager.getTree().getSelectionPaths()) {
				model.nodeChanged((TreeNode)each.getLastPathComponent());
			}
			setEnabled(true);
			super.putValue(NAME, getValue(NAME).equals(ADD) ? REMOVE : ADD);
		} catch (IOException ex) {
			Logger.getLogger(HideAction.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(main, "Error: password change failed.", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void updateState() {
		ArrayList<TreeableGalleryItem> currentItems = menuManager.getCurrentItems();
		if (currentItems.size() == 0) {
			setEnabled(false);
		} else {
			try {
				TreeableGalleryItem item = currentItems.get(0);
				boolean currentState = item.hasPassword();
				boolean allChangeable = false;
				if (item instanceof WriteableTreeableGalleryItem) {
					allChangeable = ((WriteableTreeableGalleryItem)item).canChangePassword(!currentState);
					for (int i = 1; i < currentItems.size(); i++) {
						item = currentItems.get(i);
						if (item instanceof WriteableTreeableGalleryItem) {
							allChangeable &= (currentState == item.hasPassword());
							allChangeable &= ((WriteableTreeableGalleryItem)item).canChangePassword(!currentState);
						} else {
							allChangeable = false;
							break;
						}
					}
				}
				setEnabled(allChangeable);
				super.putValue(NAME, currentState ? REMOVE : ADD);
			} catch (IOException ex) {
				Logger.getLogger(PasswordAction.class.getName()).log(Level.SEVERE, "Unable to determine menu state", ex);
				setEnabled(false);
			}
		}
	}

}
