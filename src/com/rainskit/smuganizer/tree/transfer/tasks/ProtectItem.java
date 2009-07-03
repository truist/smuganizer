package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.smugmugapiwrapper.exceptions.ProtectException;
import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.PasswordRequiredInterruption;
import java.io.IOException;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProtectItem extends AbstractTransferTask {
	private DefaultMutableTreeNode node;
	
	public ProtectItem(TreeableGalleryItem item, JTree tree, DefaultMutableTreeNode node) {
		super(item, tree, null, -1);
		this.node = node;
	}

	@Override
	protected TreeableGalleryItem doInBackgroundImpl(TransferInterruption previousInterruption) throws TransferInterruption, IOException {
		if (srcItem.canChangeHiddenStatus(true)) {
			srcItem.setHidden(true);
		} else if (srcItem.canChangePassword(true)) {
			if (previousInterruption != null && previousInterruption instanceof PasswordRequiredInterruption) {
				srcItem.setPassword(((PasswordRequiredInterruption)previousInterruption).getPassword(), null);
			} else {
				throw new PasswordRequiredInterruption(srcItem);
			}
		} else {
			throw new ProtectException(srcItem, null);
		}
		return null;
	}

	@Override
	public List<AbstractTransferTask> cleanUp(TreeableGalleryItem newItem) {
		((DefaultTreeModel)destTree.getModel()).nodeChanged(node);
		return null;
	}

	@Override
	public String getActionString() {
		return "PROTECT";
	}
}
