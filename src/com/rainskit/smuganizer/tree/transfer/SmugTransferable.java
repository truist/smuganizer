package com.rainskit.smuganizer.tree.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SmugTransferable implements Transferable {
	public static DataFlavor transferFlavor = new DataFlavor(FlavorClass.class, "tree/smugtree");
	
	private JTree sourceTree;
	private DefaultTreeModel treeModel;
	private TreePath[] selectionPaths;
	
	public SmugTransferable(JTree sourceTree, DefaultTreeModel treeModel, TreePath[] selectionPaths) {
		this.sourceTree = sourceTree;
		this.treeModel = treeModel;
		this.selectionPaths = selectionPaths;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {transferFlavor};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (transferFlavor.getClass().isInstance(flavor));
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return new FlavorClass();
	}
	
	
	public class FlavorClass {
		public JTree getSourceTree() {
			return sourceTree;
		}
		
		public DefaultTreeModel getTreeModel() {
			return treeModel;
		}
		
		public TreePath[] getTreePaths() {
			return selectionPaths;
		}
	}
}
