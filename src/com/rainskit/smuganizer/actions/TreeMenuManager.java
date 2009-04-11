package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

class TreeMenuManager implements TreeSelectionListener {
	private JTree tree;
	private ArrayList<TreeableAction> actions;
	private JPopupMenu popupMenu;
	private ArrayList<TreeableGalleryItem> currentItems;

	public TreeMenuManager(Main main, JTree tree) {
		this.tree = tree;
		this.actions = createActions(main);
		
		popupMenu = new JPopupMenu();
		for (TreeableAction each : actions) {
			if (each != null) {
				popupMenu.add(new JMenuItem(each));
			} else {
				popupMenu.addSeparator();
			}
		}
		
		currentItems = new ArrayList<TreeableGalleryItem>();
		
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new RightClickListener());
	}
	
	public List<TreeableAction> getActions() {
		return actions;
	}
	
	private ArrayList<TreeableAction> createActions(Main main) {
		ArrayList<TreeableAction> newActions = new ArrayList<TreeableAction>();
		newActions.add(new RenameAction(this, main));
		newActions.add(new DeleteAction(this, main));
		newActions.add(null);
		newActions.add(new LaunchAction(this, main));
		return newActions;
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
				each.updateState(currentItems.iterator());
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
					popupMenu.show(tree, e.getX(), e.getY());
				}
			}
		}
	}
}
