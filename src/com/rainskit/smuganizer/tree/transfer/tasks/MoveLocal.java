package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.tree.TransferTree;
import com.rainskit.smuganizer.tree.transfer.interruptions.InterruptionSet;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
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

	protected TreeableGalleryItem doInBackgroundImpl(InterruptionSet previousInterruptions) throws TransferInterruption, IOException {
		ModifiedItemAttributes finalAttributes = new ModifiedItemAttributes();
		finalAttributes = handleDuplicates(finalAttributes, previousInterruptions);
		destParentItem.moveItemLocally(srcItem, destChildIndex, finalAttributes);
		srcItem.transferStarted(false);
		srcItem.transferEnded(false, true);
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
        if (destChildIndex > destParentNode.getChildCount()) {  //can happen if intermediate transfers were cancelled
            destChildIndex = destParentNode.getChildCount();
            destChildOffset = 0;
        }
		destModel.insertNodeInto(srcNode, destParentNode, destChildIndex + destChildOffset);
		TransferTree.sortTree(destParentNode, false);
		destModel.nodeStructureChanged(destParentNode);
		destTree.makeVisible(destParentPath.pathByAddingChild(srcNode));
		return null;
	}

	@Override
	public String getActionString() {
		return "MOVE";
	}
}
