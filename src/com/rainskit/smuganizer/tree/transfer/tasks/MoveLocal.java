package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.SmugException;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class MoveLocal extends AbstractTransferTask {
	private DefaultMutableTreeNode srcNode;

	public MoveLocal(JTree srcTree,
					DefaultMutableTreeNode srcNode, 
					JTree destTree, 
					TreePath destParentPath, 
					int destChildIndex) {
		super(srcTree, (TreeableGalleryItem)srcNode.getUserObject(), destTree, destParentPath, destChildIndex);
		
		this.srcNode = srcNode;
	}

	protected TreeableGalleryItem doInBackgroundImpl(TransferInterruption previousInterruption) throws TransferInterruption, SmugException {
		destParentItem.moveItem(srcItem, destChildIndex, previousInterruption);
		return null;
	}
	
	public List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem) {
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
		destModel.nodeChanged(srcNode);
		destTree.makeVisible(destParentPath.pathByAddingChild(srcNode));
		return null;
	}

	@Override
	public String getActionString() {
		return "MOVE";
	}
}
