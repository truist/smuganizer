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
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SmugTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private AsynchronousTransferManager asyncTransferManager;
	
	public SmugTransferHandler(AsynchronousTransferManager asyncTransferManager) {
		this.asyncTransferManager = asyncTransferManager;
	}
	
	//################################
	//#######  EXPORT METHODS  #######
	//################################
	
	@Override
	public int getSourceActions(JComponent source) {
		return ((TransferTree)source).getSourceActions();
	}
	
	@Override
	protected Transferable createTransferable(JComponent source) {
		TransferTree srcTree = (TransferTree)source;
		TreePath[] selectionPaths = srcTree.getSelectionPaths();
		if (selectionPaths.length > 0) {
			return new SmugTransferable(srcTree, (DefaultTreeModel)srcTree.getModel(), selectionPaths);
		} else {
			return null;
		}
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		//This never does anything, because we handle all parts of a "move"
		//operation within the importData() method.  Doing it that way is an
		//optimization with SmugMug, that can support a server-side move,
		//which saves us having to download and re-upload the image, and
		//eliminates the risk that we'd lose metadata.  
		
		//Once we support a "move" between different galleries, this method
		//will be implemented to do the delete on the source gallery
	}
	
	
	
	
	//################################
	//#######  IMPORT METHODS  #######
	//################################
	
	@Override
	public boolean canImport(TransferSupport transferSupport) {
		if (!transferSupport.isDataFlavorSupported(SmugTransferable.transferFlavor)) {
			return false;
		}
		TransferTree.DropLocation location = (TransferTree.DropLocation)transferSupport.getDropLocation();
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
		TransferTree destTree = (TransferTree)transferSupport.getComponent();
		if (!destTree.supportsImport()) {
			return false;
		}
		if (destTree.supportsInsertAtSpecificLocation() && ItemType.IMAGE == itemType) {
			destTree.setDropMode(DropMode.ON_OR_INSERT);
		} else {
			destTree.setDropMode(DropMode.ON);
		}
		
		TreeableGalleryItem destParentItem = (TreeableGalleryItem)((DefaultMutableTreeNode)destParentPath.getLastPathComponent()).getUserObject();
		if (!(destParentItem instanceof WriteableTreeableGalleryContainer)) {
			return false;
		}
		
		TreeableGalleryItem srcItem = (TreeableGalleryItem)((DefaultMutableTreeNode)srcPaths[0].getLastPathComponent()).getUserObject();
		//if we are dragging and dropping in the same tree, then we try to support an in-place move
		if (flavorClass.getTreeModel() == destTree.getModel() && transferSupport.getUserDropAction() == MOVE) {
			return ((WriteableTreeableGalleryContainer)destParentItem).canMoveLocally(srcItem, location.getChildIndex());
		} else {
			return ((WriteableTreeableGalleryContainer)destParentItem).canImport(srcItem);
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
			TransferTree destTree = (TransferTree)transferSupport.getComponent();
			TransferTree.DropLocation destLocation = (TransferTree.DropLocation)transferSupport.getDropLocation();
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
				//if they are dragging and dropping in the same tree, we try to support an in-place move
				if (transferData.getTreeModel() == destTree.getModel() && transferSupport.getUserDropAction() == MOVE) {
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
