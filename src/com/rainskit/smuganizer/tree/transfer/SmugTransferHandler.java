package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.*;
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
		TreePath destParentPath = location.getPath();
		if (destParentPath == null) {
			return false;
		}
		TreePath[] srcPaths = retrieveData(transferSupport.getTransferable());
		if (srcPaths == null) {
			return false;
		}
		String itemType = checkThatAllPathsHaveSameType(srcPaths);
		if (itemType == null) {
			return false;
		}
		((JTree)transferSupport.getComponent()).setDropMode(TreeableGalleryItem.IMAGE.equals(itemType) ? DropMode.ON_OR_INSERT : DropMode.ON);
		
		TreeableGalleryItem destParentItem = (TreeableGalleryItem)((DefaultMutableTreeNode)destParentPath.getLastPathComponent()).getUserObject();
		TreeableGalleryItem srcItem = (TreeableGalleryItem)((DefaultMutableTreeNode)srcPaths[0].getLastPathComponent()).getUserObject();
		if (pathsAreFromSameTree(destParentPath, srcPaths[0])) {
			return destParentItem.canMove(srcItem, location.getChildIndex());
		} else {
			return destParentItem.canImport(srcItem, location.getChildIndex());
		}
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
			for (TreePath srcPath : transferData) {
				DefaultMutableTreeNode srcNode = (DefaultMutableTreeNode)srcPath.getLastPathComponent();
				if (pathsAreFromSameTree(srcPath, destParentPath)) {
					asyncTransferManager.submit(new MoveLocal(srcNode, destTree, destParentPath, destParentNode, destChildIndex++));
				} else {
					asyncTransferManager.submit(new CopyRemote(srcNode, destTree, destParentPath, destParentNode, destChildIndex++));
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean pathsAreFromSameTree(TreePath left, TreePath right) {
		return left.getPathComponent(0).equals(right.getPathComponent(0));
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
}
