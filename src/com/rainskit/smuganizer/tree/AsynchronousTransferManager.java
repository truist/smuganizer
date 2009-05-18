package com.rainskit.smuganizer.tree;

import com.rainskit.smuganizer.Main;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AsynchronousTransferManager extends AbstractTableModel implements PropertyChangeListener {
	public static final int PROGRESS_COLUMN = 0;
	private static final String PROGRESS_COLUMN_NAME = "Progress";
	private static final int PROGRESS_COLUMN_WIDTH = 100;
	public static final int ACTION_COLUMN = 1;
	private static final String ACTION_COLUMN_NAME = "Action";
	private static final int ACTION_COLUMN_WIDTH = 100;
	public static final int SOURCE_COLUMN = 2;
	private static final String SOURCE_COLUMN_NAME = "Source";
	private static final int SOURCE_COLUMN_WIDTH = 400;
	public static final int DEST_COLUMN = 3;
	private static final String DEST_COLUMN_NAME = "Destination";
	private static final int DEST_COLUMN_WIDTH = 400;
	public static final int COLUMN_COUNT = 4;
	
	private ArrayList<MoveLocal> visibleTasks;
	private ExecutorService transferExecutor;
	
	public AsynchronousTransferManager() {
		this.transferExecutor = Executors.newSingleThreadExecutor();
		this.visibleTasks = new ArrayList<MoveLocal>();
	}

	public void submit(MoveLocal asyncTransferObject) {
		synchronized(visibleTasks) {
			visibleTasks.add(asyncTransferObject);
			fireTableRowsInserted(visibleTasks.size() - 1, visibleTasks.size() - 1);
			asyncTransferObject.addPropertyChangeListener(this);
			transferExecutor.submit(asyncTransferObject, asyncTransferObject);
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		synchronized(visibleTasks) {
			MoveLocal source = (MoveLocal)evt.getSource();
			int row = visibleTasks.indexOf(source);
			if (StateValue.DONE == evt.getNewValue()) {
				visibleTasks.remove(row);
				fireTableRowsDeleted(row, row);
			} else {
				fireTableCellUpdated(visibleTasks.indexOf(source), 0);
			}
		}
	}

	public int getRowCount() {
		return visibleTasks.size();
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case PROGRESS_COLUMN:
				return PROGRESS_COLUMN_NAME;
			case ACTION_COLUMN:
				return ACTION_COLUMN_NAME;
			case SOURCE_COLUMN:
				return SOURCE_COLUMN_NAME;
			case DEST_COLUMN:
				return DEST_COLUMN_NAME;
			default:
				throw new IllegalArgumentException("Invalid column: " + column);
		}
	}

	public int getPreferredColumnWidth(int column) {
		switch (column) {
			case PROGRESS_COLUMN:
				return PROGRESS_COLUMN_WIDTH;
			case ACTION_COLUMN:
				return ACTION_COLUMN_WIDTH;
			case SOURCE_COLUMN:
				return SOURCE_COLUMN_WIDTH;
			case DEST_COLUMN:
				return DEST_COLUMN_WIDTH;
			default:
				throw new IllegalArgumentException("Invalid column: " + column);
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		MoveLocal row = visibleTasks.get(rowIndex);
		switch (columnIndex) {
			case PROGRESS_COLUMN:
				return row.getProgress();
			case ACTION_COLUMN:
				return "MOVE";
			case SOURCE_COLUMN:
				return ((TreeableGalleryItem)row.srcNode.getUserObject()).getFullPathLabel();
			case DEST_COLUMN:
				return ((TreeableGalleryItem)row.destParentNode.getUserObject()).getFullPathLabel();
			default:
				throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
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
			TreeableGalleryItem srcItem = (TreeableGalleryItem)srcNode.getUserObject();
			TreeableGalleryItem destParentItem = (TreeableGalleryItem)destParentNode.getUserObject();
			Logger.getLogger(AsynchronousTransferManager.class.getName()).log(Level.INFO, 
				"Moving " + srcItem.getFullPathLabel() + " to " + destParentItem.getFullPathLabel() + " at position " + destChildIndex);
			setProgress(0);
			destParentItem.receiveChild(srcItem, destChildIndex);
			setProgress(90);
			//check to see if we are just changing an image's location within it's parent,
			//and fix up the index var to work correctly with the tree model
			if (srcNode.getParent() == destParentNode && destChildIndex > destParentNode.getIndex(srcNode)) { 
				destChildIndex--;
			}
			DefaultTreeModel destModel = (DefaultTreeModel)tree.getModel();
			destModel.removeNodeFromParent(srcNode);
			destModel.insertNodeInto(srcNode, destParentNode, destChildIndex);
			tree.makeVisible(destParentPath.pathByAddingChild(srcNode));
//			destTree.scrollRectToVisible(destTree.getPathBounds(newDestPath));
//			destTree.addSelectionPath(newDestPath);
			setProgress(100);
			return null;
		}
	}
}
