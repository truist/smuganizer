package com.rainskit.smuganizer.tree.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.tree.TreePath;

public class SmugTransferable implements Transferable {
	public static DataFlavor transferFlavor = new DataFlavor(FlavorClass.class, "tree/smugtree");
	private TreePath[] selectionPaths;
	
	public SmugTransferable(TreePath[] selectionPaths) {
		this.selectionPaths = selectionPaths;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {transferFlavor};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (transferFlavor == flavor);
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return new FlavorClass();
	}
	
	
	public class FlavorClass {
		public TreePath[] getTreePaths() {
			return selectionPaths;
		}
	}
}
