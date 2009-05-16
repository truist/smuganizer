package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.Main;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AsynchronousTransferManager {
	private Main main;
	private ExecutorService transferExecutor;
	
	public AsynchronousTransferManager(Main main) {
		this.main = main;
		this.transferExecutor = Executors.newSingleThreadExecutor();
	}
	
	public void submit(MoveLocal asyncTransferObject) {
		asyncTransferObject.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.err.println("Property change: " + evt.getPropertyName() + " = " + evt.getNewValue());
			}
		});
		transferExecutor.submit(asyncTransferObject, asyncTransferObject);
	}
	
	
	public static class MoveLocal extends SwingWorker<String, String> {
		private DefaultMutableTreeNode srcNode;
		private JTree tree;
		private TreePath destParentPath;
		private DefaultMutableTreeNode destParentNode;
		private int destChildIndex;
		
		public MoveLocal(DefaultMutableTreeNode srcNode,
							JTree tree, 
							TreePath destParentPath, 
							DefaultMutableTreeNode destParentNode,
							int destChildIndex) {
			this.srcNode = srcNode;
			this.tree = tree;
			this.destParentPath = destParentPath;
			this.destParentNode = destParentNode;
			this.destChildIndex = destChildIndex;
		}

		@Override
		protected String doInBackground() throws Exception {
			TreeableGalleryItem destParentItem = (TreeableGalleryItem)destParentNode.getUserObject();
			TreeableGalleryItem srcItem = (TreeableGalleryItem)srcNode.getUserObject();
			publish("Moving " + srcItem.getFullPathLabel() + " to " + destParentItem.getFullPathLabel());
			setProgress(0);
			destParentItem.receiveChild(srcItem, destChildIndex);
			setProgress(90);

			DefaultTreeModel destModel = (DefaultTreeModel)tree.getModel();
			destModel.removeNodeFromParent(srcNode);
			destModel.insertNodeInto(srcNode, destParentNode, destChildIndex);
			tree.makeVisible(destParentPath.pathByAddingChild(srcNode));
//			destTree.scrollRectToVisible(destTree.getPathBounds(newDestPath));
//			destTree.addSelectionPath(newDestPath);
			setProgress(100);
			return null;
		}

		@Override
		protected void process(List<String> chunks) {
			for (String each : chunks) {
				System.err.println(each);
			}
		}

		@Override
		protected void done() {
			System.err.println("Done!");
		}
	}
}
