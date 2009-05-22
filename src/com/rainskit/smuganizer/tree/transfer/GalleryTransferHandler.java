package com.rainskit.smuganizer.tree.transfer;

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

public class GalleryTransferHandler extends TransferHandler {
	@Override
	public int getSourceActions(JComponent source) {
		return COPY;
	}
	
	@Override
	protected Transferable createTransferable(JComponent source) {
		TreePath[] selectionPaths = ((JTree)source).getSelectionPaths();
		if (selectionPaths.length > 0) {
			return new SmugTransferable(selectionPaths);
		} else {
			return null;
		}
	}

//	@Override
//	protected void exportDone(JComponent source, Transferable transferable, int action) {
//		super.exportDone(source, transferable, action);
//	}
}
