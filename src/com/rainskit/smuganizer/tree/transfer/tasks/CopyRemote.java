package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
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

	protected TreeableGalleryItem doInBackgroundImpl(TransferInterruption previousInterruption) throws TransferInterruption, IOException {
		TreeableGalleryItem newItem = destParentItem.importItem(srcItem, previousInterruption);
		newItem.transferStarted(false);
		newItem.transferEnded(false, true);
		return newItem;
	}
	
	public List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem) {
		DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
		DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newItem);
		destModel.insertNodeInto(newNode, destParentNode, destChildIndex);
		destTree.makeVisible(destParentPath.pathByAddingChild(newNode));
		
		ArrayList<AbstractTransferTask> followUpTasks = new ArrayList<AbstractTransferTask>();
		followUpTasks.add(new MoveLocal(newNode, destTree, destParentPath, destChildIndex));
		if (srcItem.isProtected()) {
			followUpTasks.add(new ProtectItem(newItem, destTree, newNode));
		}
		return followUpTasks;
	}

	@Override
	public String getActionString() {
		return "COPY";
	}
}
