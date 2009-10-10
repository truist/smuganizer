package com.rainskit.smuganizer.menu;

import com.rainskit.smuganizer.menu.actions.treeactions.ExifAction;
import com.rainskit.smuganizer.menu.actions.treeactions.TreeableAction;
import com.rainskit.smuganizer.menu.actions.treeactions.LaunchAction;
import com.rainskit.smuganizer.menu.actions.treeactions.PasswordAction;
import com.rainskit.smuganizer.menu.actions.treeactions.PreviewAction;
import com.rainskit.smuganizer.menu.actions.treeactions.HideAction;
import com.rainskit.smuganizer.menu.actions.treeactions.RenameAction;
import com.rainskit.smuganizer.menu.actions.treeactions.DeleteAction;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeMenuManager implements TreeSelectionListener {
	private JTree tree;
	private ArrayList<TreeableAction> actions;
	private ArrayList<TreeableGalleryItem> currentItems;

	public TreeMenuManager(Main main, JTree tree) {
		this.tree = tree;
		
		currentItems = new ArrayList<TreeableGalleryItem>();
		actions = createActions(main, tree);
		
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new RightClickListener());
	}
	
	private ArrayList<TreeableAction> createActions(Main main, JTree tree) {
		ArrayList<TreeableAction> newActions = new ArrayList<TreeableAction>();
		newActions.add(new PreviewAction(this, main, tree));
		newActions.add(new LaunchAction(this, main, tree));
		newActions.add(new ExifAction(this, main, tree));
		newActions.add(null);
		newActions.add(new RenameAction(this, main, tree));
		newActions.add(new DeleteAction(this, main, tree));
		newActions.add(null);
		newActions.add(new HideAction(this, main));
		newActions.add(new PasswordAction(this, main));
		return newActions;
	}

	private void showPopup(JTree tree, int x, int y) {
		boolean lastAddedWasSeparator = false;
		JPopupMenu popupMenu = new JPopupMenu();
		for (TreeableAction each : actions) {
			if (each != null) {
				if (each.isEnabled()) {
					popupMenu.add(new JMenuItem(each));
					lastAddedWasSeparator = false;
				}
			} else if (!lastAddedWasSeparator) {
				popupMenu.addSeparator();
				lastAddedWasSeparator = true;
			}
		}
		if (lastAddedWasSeparator) {
			popupMenu.remove(popupMenu.getComponentCount() - 1);
		}
		
		popupMenu.show(tree, x, y);
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath[] changedPaths = e.getPaths();
		for (TreePath eachPath : changedPaths) {
			DefaultMutableTreeNode eachNode = ((DefaultMutableTreeNode)eachPath.getLastPathComponent());
			if (eachNode != null && eachNode.getUserObject() instanceof TreeableGalleryItem) {
				TreeableGalleryItem eachItem = (TreeableGalleryItem)eachNode.getUserObject();
				if (e.isAddedPath(eachPath)) {
					addNewCurrentItem(eachItem);
				} else {
					removeCurrentItem(eachItem);
				}
			}
		}
	}
	
	private void addNewCurrentItem(TreeableGalleryItem newItem) {
		currentItems.add(newItem);
		updateActionState();
	}
	
	private void removeCurrentItem(TreeableGalleryItem oldItem) {
		currentItems.remove(oldItem);
		updateActionState();
	}
	
	private void updateActionState() {
		for (TreeableAction each : actions) {
			if (each != null) {
				each.updateState();
			}
		}
	}
	
	public ArrayList<TreeableGalleryItem> getCurrentItems() {
		return currentItems;
	}
	
	public JTree getTree() {
		return tree;
	}
	
	
	private class RightClickListener extends MouseAdapter {
		@Override public void mousePressed(MouseEvent e) { handleEvent(e); }
		@Override public void mouseReleased(MouseEvent e) { handleEvent(e); }
		private void handleEvent(MouseEvent e) {
			if (e.isPopupTrigger()) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null && tree.getPathBounds(path).contains(e.getPoint())) {
					boolean clickedOnSelection = false;
					TreePath[] selectedPaths = tree.getSelectionPaths();
					if (selectedPaths != null) {
						for (TreePath each : selectedPaths) {
							if (path.equals(each)) {
								clickedOnSelection = true;
							}
						}
					}
					if (!clickedOnSelection) {
						tree.setSelectionPath(path);
					}
					showPopup(tree, e.getX(), e.getY());
				}
			}
		}
	}
}
