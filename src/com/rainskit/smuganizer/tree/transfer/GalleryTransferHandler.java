package com.rainskit.smuganizer.tree.transfer;

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class GalleryTransferHandler extends TransferHandler {
	@Override
	public int getSourceActions(JComponent source) {
		return COPY;
	}
	
	@Override
	protected Transferable createTransferable(JComponent source) {
		JTree srcTree = (JTree)source;
		TreePath[] selectionPaths = srcTree.getSelectionPaths();
		if (selectionPaths.length > 0) {
			return new SmugTransferable(srcTree, (DefaultTreeModel)srcTree.getModel(), selectionPaths);
		} else {
			return null;
		}
	}
}
