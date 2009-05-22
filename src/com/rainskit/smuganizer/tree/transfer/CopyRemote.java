package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class CopyRemote extends AbstractTransferTask {

	public CopyRemote(DefaultMutableTreeNode srcNode, 
						JTree destTree, 
						TreePath destParentPath, 
						DefaultMutableTreeNode destParentNode, 
						int destChildIndex) {
		super(srcNode, destTree, destParentPath, destParentNode, destChildIndex);
	}

	@Override
	protected String doInBackground() throws Exception {
		Logger.getLogger(CopyRemote.class.getName()).log(Level.INFO, "Copying " + srcItem.getFullPathLabel() + " to " + destParentItem.getFullPathLabel() + " at position " + destChildIndex);
		setProgress(0);
		final TreeableGalleryItem newItem = destParentItem.importItem(srcItem, destChildIndex);
		setProgress(90);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newItem);
				destModel.insertNodeInto(newNode, destParentNode, destChildIndex);
				destTree.makeVisible(destParentPath.pathByAddingChild(newNode));
			}
		});
		setProgress(100);
		return null;
	}
}
