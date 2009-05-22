package com.rainskit.smuganizer.tree.transfer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class MoveLocal extends AbstractTransferTask {

	public MoveLocal(DefaultMutableTreeNode srcNode, 
					JTree destTree, 
					TreePath destParentPath, 
					DefaultMutableTreeNode destParentNode, 
					int destChildIndex) {
		super(srcNode, destTree, destParentPath, destParentNode, destChildIndex);
	}

	@Override
	protected String doInBackground() throws Exception {
		Logger.getLogger(MoveLocal.class.getName()).log(Level.INFO, "Moving " + srcItem.getFullPathLabel() + " to " + destParentItem.getFullPathLabel() + " at position " + destChildIndex);
		setProgress(0);
		destParentItem.moveItem(srcItem, destChildIndex);
		setProgress(90);
		//check to see if we are just changing an image's location within it's parent,
		//and fix up the index var to work correctly with the tree model
		if (srcNode.getParent() == destParentNode && destChildIndex > destParentNode.getIndex(srcNode)) {
			destChildIndex--;
		}
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
				destModel.removeNodeFromParent(srcNode);
				destModel.insertNodeInto(srcNode, destParentNode, destChildIndex);
				destTree.makeVisible(destParentPath.pathByAddingChild(srcNode));
			}
		});
		setProgress(100);
		return null;
	}
}
