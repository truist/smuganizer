package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public abstract class AbstractTransferTask extends SwingWorker<String, String> {
	protected DefaultMutableTreeNode srcNode;
	protected TreeableGalleryItem srcItem;
	protected JTree destTree;
	protected TreePath destParentPath;
	protected DefaultMutableTreeNode destParentNode;
	protected TreeableGalleryItem destParentItem;
	protected int destChildIndex;

	public AbstractTransferTask(DefaultMutableTreeNode srcNode, 
								JTree tree, 
								TreePath destParentPath, 
								DefaultMutableTreeNode destParentNode, 
								int destChildIndex) {
		super();
		this.srcNode = srcNode;
		this.srcItem = ((TreeableGalleryItem) srcNode.getUserObject());
		this.destTree = tree;
		this.destParentPath = destParentPath;
		this.destParentNode = destParentNode;
		this.destParentItem = (TreeableGalleryItem) destParentNode.getUserObject();
		this.destChildIndex = destChildIndex;

		switchTransferStatus(true);
	}

	@Override
	protected void done() {
		switchTransferStatus(false);
	}

	protected void switchTransferStatus(boolean newStatus) {
		srcItem.setTransferActive(newStatus);
		destParentItem.setTransferRecipient(newStatus);
		((DefaultTreeModel)destTree.getModel()).nodeChanged(srcNode);
		((DefaultTreeModel)destTree.getModel()).nodeChanged(destParentNode);
	}
}
