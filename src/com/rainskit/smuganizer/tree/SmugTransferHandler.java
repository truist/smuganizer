package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SmugTransferHandler extends TransferHandler {
	private Main main;
	
	public SmugTransferHandler(Main main) {
		this.main = main;
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}
	
	@Override
	protected Transferable createTransferable(JComponent source) {
		JTree tree = (JTree)source;
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if (selectionPaths.length == 0) {
			return null;
		}
		String firstType = null;
		for (TreePath eachPath : selectionPaths) {
			DefaultMutableTreeNode eachNode = (DefaultMutableTreeNode)eachPath.getLastPathComponent();
			TreeableGalleryItem eachItem = (TreeableGalleryItem)eachNode.getUserObject();
			String eachType = eachItem.getType();
			if (firstType == null) {
				firstType = eachType;
			} else if (!firstType.equals(eachType)) {
				return null;
			}
		}
		tree.setDropMode(TreeableGalleryItem.IMAGE.equals(firstType) ? DropMode.INSERT : DropMode.ON);
		return new SmugTransferable(selectionPaths);
	}

	@Override
	public boolean canImport(TransferSupport transferSupport) {
		if (!transferSupport.isDataFlavorSupported(SmugTransferable.transferFlavor)) {
			return false;
		}
		JTree.DropLocation location = (JTree.DropLocation)transferSupport.getDropLocation();
		TreePath parentPath = location.getPath();
		if (parentPath == null) {
			return false;
		}
		TreePath[] data = retrieveData(transferSupport.getTransferable());
		if (data == null) {
			return false;
		}
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parentPath.getLastPathComponent();
		TreeableGalleryItem parentItem = (TreeableGalleryItem)parentNode.getUserObject();
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)data[0].getLastPathComponent();
		return parentItem.canAccept((TreeableGalleryItem)childNode.getUserObject(), location.getChildIndex());
	}

	@Override
	public boolean importData(TransferSupport transferSupport) {
		main.setStatus("Moving...");
		try {
			JTree tree = (JTree)transferSupport.getComponent();
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();

			JTree.DropLocation location = (JTree.DropLocation)transferSupport.getDropLocation();
			TreePath parentPath = location.getPath();
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parentPath.getLastPathComponent();
			TreeableGalleryItem parentItem = (TreeableGalleryItem)parentNode.getUserObject();

			int childIndex = location.getChildIndex();
			if (childIndex == -1) {
				childIndex = treeModel.getChildCount(parentNode);
			}

			for (TreePath eachPath : retrieveData(transferSupport.getTransferable())) {
				DefaultMutableTreeNode eachNode = (DefaultMutableTreeNode)eachPath.getLastPathComponent();
				TreeableGalleryItem childItem = (TreeableGalleryItem)eachNode.getUserObject();
				try {
					parentItem.receiveChild(childItem, childIndex);
				} catch (RuntimeException se) {
					JOptionPane.showMessageDialog(main, "Oh no!  Something went wrong: " + se.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					Logger.getLogger(SmugTransferHandler.class.getName()).log(Level.SEVERE, "Error transfering data", se);
					throw se;
				}
				if (eachNode.getParent() == parentNode && childIndex > parentNode.getIndex(eachNode)) { 
					childIndex--;
				}
				treeModel.removeNodeFromParent(eachNode);
				treeModel.insertNodeInto(eachNode, parentNode, childIndex++);
				TreePath newPath = parentPath.pathByAddingChild(eachNode);
				tree.makeVisible(newPath);
				tree.scrollRectToVisible(tree.getPathBounds(newPath));
				tree.addSelectionPath(newPath);
			}
		} finally {
			main.clearStatus();
		}

		return true;
	}
	
	private TreePath[] retrieveData(Transferable transferable) {
		try {
			return ((SmugTransferable.FlavorClass)transferable.getTransferData(SmugTransferable.transferFlavor)).getTreePaths();
		} catch (UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	protected void exportDone(JComponent source, Transferable transferable, int action) {
		super.exportDone(source, transferable, action);
	}
}
