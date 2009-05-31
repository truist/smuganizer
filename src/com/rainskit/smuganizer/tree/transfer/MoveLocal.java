package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class MoveLocal extends AbstractTransferTask {
	private DefaultMutableTreeNode srcNode;

	public MoveLocal(DefaultMutableTreeNode srcNode, 
					JTree destTree, 
					TreePath destParentPath, 
					int destChildIndex) {
		super((TreeableGalleryItem)srcNode.getUserObject(), destTree, destParentPath, destChildIndex);
		
		this.srcNode = srcNode;
	}

	protected TreeableGalleryItem doInBackgroundImpl() throws TransferException {
		destParentItem.moveItem(srcItem, destChildIndex);
		return srcItem;
	}
	
	protected void cleanUp(TreeableGalleryItem newItem) {
		//check to see if we are just changing an image's location within it's parent,
		//and fix up the index var to work correctly with the tree model
		DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
		int destChildOffset = 0;
		if (srcNode.getParent() == destParentNode && destChildIndex > destParentNode.getIndex(srcNode)) {
			destChildOffset = -1;
		}
		DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
		destModel.removeNodeFromParent(srcNode);
		destModel.insertNodeInto(srcNode, destParentNode, destChildIndex + destChildOffset);
		destTree.makeVisible(destParentPath.pathByAddingChild(srcNode));
	}
}
