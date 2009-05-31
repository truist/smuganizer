package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class CopyRemote extends AbstractTransferTask {

	public CopyRemote(TreeableGalleryItem srcItem, 
						JTree destTree, 
						TreePath destParentPath, 
						int destChildIndex) {
		super(srcItem, destTree, destParentPath, destChildIndex);
	}

	protected TreeableGalleryItem doInBackgroundImpl() throws TransferException, IOException {
		TreeableGalleryItem newItem = destParentItem.importItem(srcItem, destChildIndex);
		newItem.transferStarted(false);
		newItem.transferEnded(false, true);
		return newItem;
	}
	
	protected void cleanUp(TreeableGalleryItem newItem) {
		DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
		DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newItem);
		destModel.insertNodeInto(newNode, destParentNode, destChildIndex);
		destTree.makeVisible(destParentPath.pathByAddingChild(newNode));
	}
}
