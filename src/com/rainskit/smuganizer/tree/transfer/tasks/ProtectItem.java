package com.rainskit.smuganizer.tree.transfer.tasks;

import com.rainskit.smuganizer.tree.transfer.interruptions.TransferInterruption;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import com.rainskit.smuganizer.tree.WriteableTreeableGalleryItem;
import com.rainskit.smuganizer.tree.transfer.interruptions.InterruptionSet;
import com.rainskit.smuganizer.tree.transfer.interruptions.PasswordRequiredInterruption;
import java.io.IOException;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProtectItem extends AbstractTransferTask {
	private DefaultMutableTreeNode node;
	
	public ProtectItem(JTree srcTree, TreeableGalleryItem item, JTree destTree, DefaultMutableTreeNode node) {
		super(srcTree, item, destTree, null, -1);
		this.node = node;
	}

	@Override
	protected TreeableGalleryItem doInBackgroundImpl(InterruptionSet previousInterruptions) throws TransferInterruption, IOException {
		WriteableTreeableGalleryItem writeableSrcItem = (WriteableTreeableGalleryItem)srcItem;
		if (writeableSrcItem.canChangeHiddenStatus(true)) {
			writeableSrcItem.setHidden(true);
		} else if (writeableSrcItem.canChangePassword(true)) {
			if (previousInterruptions.hasInterruption(PasswordRequiredInterruption.class)) {
				PasswordRequiredInterruption passwordInterruption = (PasswordRequiredInterruption)previousInterruptions.getInterruption(PasswordRequiredInterruption.class);
				writeableSrcItem.setPassword(passwordInterruption.getPassword(), passwordInterruption.getHint());
			} else {
				throw new PasswordRequiredInterruption(srcItem);
			}
		} else {
			throw new IOException("Unable to protect this item (even though its source was protected) " + srcItem.getFullPathLabel(), null);
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
