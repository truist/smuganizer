package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.tasks.MoveLocal;
import com.rainskit.smuganizer.tree.transfer.tasks.CopyRemote;
import com.rainskit.smuganizer.tree.*;
import com.rainskit.smuganizer.tree.TreeableGalleryItem.ItemType;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
		SmugTransferable.FlavorClass flavorClass = retrieveData(transferSupport.getTransferable());
		if (flavorClass == null) {
			return false;
		}
		TreePath[] srcPaths = flavorClass.getTreePaths();
		ItemType itemType = checkThatAllPathsHaveSameType(srcPaths);
		if (itemType == null) {
			return false;
		}
		JTree destTree = (JTree)transferSupport.getComponent();
		destTree.setDropMode(ItemType.IMAGE == itemType ? DropMode.ON_OR_INSERT : DropMode.ON);
		
		TreeableGalleryItem destParentItem = (TreeableGalleryItem)((DefaultMutableTreeNode)destParentPath.getLastPathComponent()).getUserObject();
		TreeableGalleryItem srcItem = (TreeableGalleryItem)((DefaultMutableTreeNode)srcPaths[0].getLastPathComponent()).getUserObject();
		if (flavorClass.getTreeModel() == destTree.getModel()) {
			return destParentItem.canMove(srcItem, location.getChildIndex());
		} else {
			return destParentItem.canImport(srcItem);
		}
	}
	
	private ItemType checkThatAllPathsHaveSameType(TreePath[] paths) {
		ItemType firstType = null;
		for (TreePath eachPath : paths) {
			DefaultMutableTreeNode eachNode = (DefaultMutableTreeNode)eachPath.getLastPathComponent();
			TreeableGalleryItem eachItem = (TreeableGalleryItem)eachNode.getUserObject();
			ItemType eachType = eachItem.getType();
			if (firstType == null) {
				firstType = eachType;
			} else if (firstType != eachType) {
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
			SmugTransferable.FlavorClass transferData = retrieveData(transferSupport.getTransferable());
			
			TreePath[] treePaths = transferData.getTreePaths();
			for (TreePath srcPath : treePaths) {
				DefaultMutableTreeNode srcNode = (DefaultMutableTreeNode)srcPath.getLastPathComponent();
				AbstractTransferTask task = null;
				if (transferData.getTreeModel() == destTree.getModel()) {
					task = new MoveLocal(transferData.getSourceTree(), srcNode, destTree, destParentPath, destChildIndex++);
				} else {
					task = new CopyRemote(transferData.getSourceTree(), (TreeableGalleryItem)srcNode.getUserObject(), destTree, destParentPath, destChildIndex++);
				}
				task.addStatusListener(new TreeUpdatingStatusListener(transferData.getTreeModel(), srcNode, (DefaultTreeModel)destTree.getModel(), destParentNode));
				asyncTransferManager.submit(task);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private SmugTransferable.FlavorClass retrieveData(Transferable transferable) {
		try {
			return (SmugTransferable.FlavorClass)transferable.getTransferData(SmugTransferable.transferFlavor);
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
		JTree srcTree = (JTree)source;
		TreePath[] selectionPaths = srcTree.getSelectionPaths();
		if (selectionPaths.length > 0) {
			return new SmugTransferable(srcTree, (DefaultTreeModel)srcTree.getModel(), selectionPaths);
		} else {
			return null;
		}
	}
	
	
	private class TreeUpdatingStatusListener implements StatusListener {
		private DefaultTreeModel srcModel;
		private DefaultMutableTreeNode srcNode;
		private DefaultTreeModel destModel;
		private DefaultMutableTreeNode destParentNode;
		
		public TreeUpdatingStatusListener(DefaultTreeModel srcModel, DefaultMutableTreeNode srcNode,
											DefaultTreeModel destModel, DefaultMutableTreeNode destParentNode) {
			this.srcModel = srcModel;
			this.srcNode = srcNode;
			this.destModel = destModel;
			this.destParentNode = destParentNode;
			statusChanged(null);
		}
		
		public void statusChanged(AbstractTransferTask task) {
			srcModel.nodeChanged(srcNode);
			destModel.nodeChanged(destParentNode);
		}
	}
}
