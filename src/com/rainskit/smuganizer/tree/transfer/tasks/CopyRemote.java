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
	public CopyRemote(JTree srcTree,
						TreeableGalleryItem srcItem, 
						JTree destTree, 
						TreePath destParentPath, 
						int destChildIndex) {
		super(srcTree, srcItem, destTree, destParentPath, destChildIndex);
	}

	protected TreeableGalleryItem doInBackgroundImpl(TransferInterruption previousInterruption) throws TransferInterruption, IOException {
		if (destParentItem.canImport(srcItem)) {
			TreeableGalleryItem newItem = destParentItem.importItem(srcItem, previousInterruption);
			newItem.transferStarted(false);
			newItem.transferEnded(false, true);
			return newItem;
		} else {
			throw new IllegalStateException("Error: it is not possible to import \"" 
				+ srcItem.getFullPathLabel() 
				+ "\" into \"" 
				+ destParentItem.getFullPathLabel()
				+ "\"");
		}
	}
	
	public List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem) {
		DefaultTreeModel destModel = (DefaultTreeModel)destTree.getModel();
		DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
		DefaultMutableTreeNode newNode = checkIfItemIsAlreadyInSubTree(newItem, destParentNode);
		if (newNode == null) {
			newNode = new DefaultMutableTreeNode(newItem);
			destModel.insertNodeInto(newNode, destParentNode, Math.min(destChildIndex, destParentNode.getChildCount()));
		}
		destTree.makeVisible(destParentPath.pathByAddingChild(newNode));

		ArrayList<AbstractTransferTask> followUpTasks = new ArrayList<AbstractTransferTask>();
		if (destParentItem.canMove(newItem, destChildIndex)) {
			followUpTasks.add(new MoveLocal(destTree, newNode, destTree, destParentPath, destChildIndex));
		}
		if (srcItem.isProtected() && !newItem.isProtected()) {
			followUpTasks.add(new ProtectItem(destTree, newItem, destTree, newNode));
		}
		
		List<? extends TreeableGalleryItem> children = srcItem.getChildren();
		if (children != null) {
			int childIndex = 0;
			for (TreeableGalleryItem childItem : children) {
				followUpTasks.add(new CopyRemote(srcTree, childItem, destTree, new TreePath(newNode.getPath()), childIndex++));
			}
		}
		
		return followUpTasks;
	}

	private DefaultMutableTreeNode checkIfItemIsAlreadyInSubTree(TreeableGalleryItem newItem, DefaultMutableTreeNode destParentNode) {
		for (int i = 0; i < destParentNode.getChildCount(); i++) {
			DefaultMutableTreeNode each = (DefaultMutableTreeNode)destParentNode.getChildAt(i);
			if (each.getUserObject() == newItem) {
				return each;
			}
		}
		return null;
	}

	@Override
	public String getActionString() {
		return "COPY";
	}
}
