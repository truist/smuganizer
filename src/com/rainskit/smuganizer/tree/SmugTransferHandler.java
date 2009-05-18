package com.rainskit.smuganizer.tree;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class SmugTransferHandler extends TransferHandler {
	private AsynchronousTransferManager asyncTransferManager;
	
	public SmugTransferHandler(AsynchronousTransferManager asyncTransferManager) {
		this.asyncTransferManager = asyncTransferManager;
	}
	
	//################################
	//#######  IMPORT METHODS  #######
	//################################
	
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
		TreePath[] selectionPaths = retrieveData(transferSupport.getTransferable());
		if (selectionPaths == null) {
			return false;
		}
		String itemType = checkThatAllPathsHaveSameType(selectionPaths);
		if (itemType == null) {
			return false;
		}
		((JTree)transferSupport.getComponent()).setDropMode(TreeableGalleryItem.IMAGE.equals(itemType) ? DropMode.ON_OR_INSERT : DropMode.ON);
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parentPath.getLastPathComponent();
		TreeableGalleryItem parentItem = (TreeableGalleryItem)parentNode.getUserObject();
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)selectionPaths[0].getLastPathComponent();
		return parentItem.canAccept((TreeableGalleryItem)childNode.getUserObject(), location.getChildIndex());
	}
	
	private String checkThatAllPathsHaveSameType(TreePath[] paths) {
		String firstType = null;
		for (TreePath eachPath : paths) {
			DefaultMutableTreeNode eachNode = (DefaultMutableTreeNode)eachPath.getLastPathComponent();
			TreeableGalleryItem eachItem = (TreeableGalleryItem)eachNode.getUserObject();
			String eachType = eachItem.getType();
			if (firstType == null) {
				firstType = eachType;
			} else if (!firstType.equals(eachType)) {
				return null;
			}
		}
		return firstType;
	}

	@Override
	public boolean importData(TransferSupport transferSupport) {
		if (canImport(transferSupport)) {	//have to check, because "paste" doesn't call canImport()
			JTree destTree = (JTree)transferSupport.getComponent();
			JTree.DropLocation destLocation = (JTree.DropLocation)transferSupport.getDropLocation();
			TreePath destParentPath = destLocation.getPath();
			DefaultMutableTreeNode destParentNode = (DefaultMutableTreeNode)destParentPath.getLastPathComponent();
			int destChildIndex = destLocation.getChildIndex();
			if (destChildIndex == -1) {
				destChildIndex = destParentNode.getChildCount();
			}
			TreePath[] transferData = retrieveData(transferSupport.getTransferable());
			destTree.clearSelection();
			for (TreePath srcPath : transferData) {
				DefaultMutableTreeNode srcNode = (DefaultMutableTreeNode)srcPath.getLastPathComponent();
				asyncTransferManager.submit(new AsynchronousTransferManager.MoveLocal(srcNode, 
												destTree, 
												destParentPath, 
												destParentNode, 
												destChildIndex++));
			}
			return true;
		} else {
			return false;
		}
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
	
	
	//################################
	//#######  EXPORT METHODS  #######
	//################################
	
	@Override
	public int getSourceActions(JComponent source) {
		return MOVE;
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
