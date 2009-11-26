package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class ShowInTreeAction extends TableableAction {
	public ShowInTreeAction(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Show in tree", "Showing...");
	}
	
	@Override
	public void updateState(List<AbstractTransferTask> selectedTasks) {
		setEnabled(selectedTasks.size() > 0);
	}
	
	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		AbstractTransferTask task = selectedItems.get(0);
		task.getSourceTree().clearSelection();
		task.getDestTree().clearSelection();
		for (AbstractTransferTask each : selectedItems) {
			showNode(findNodeForItem(each.getSourceItem(), each.getSourceTree()), each.getSourceTree());
			showNode(findNodeForItem(each.getDestParentItem(), each.getDestTree()), each.getDestTree());
		}
	}

	private DefaultMutableTreeNode findNodeForItem(TreeableGalleryItem item, JTree tree) {
		if (item != null && tree != null) {
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
			return matchNodeToItem(rootNode, item);
		} else {
			return null;
		}
	}

	private DefaultMutableTreeNode matchNodeToItem(DefaultMutableTreeNode node, TreeableGalleryItem item) {
		if (node.getUserObject() == item) {
			return node;
		} else {
			Enumeration children = node.children();
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode match = matchNodeToItem((DefaultMutableTreeNode)children.nextElement(), item);
				if (match != null) {
					return match;
				}
			}
			return null;
		}
	}

	private void showNode(DefaultMutableTreeNode node, JTree tree) {
		if (node != null && tree != null) {
			TreePath path = new TreePath(node.getPath());
			tree.makeVisible(path);
			tree.addSelectionPath(path);
			tree.scrollPathToVisible(path);
		}
	}
}
