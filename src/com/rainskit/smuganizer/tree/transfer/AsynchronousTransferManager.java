package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.AbstractTableModel;

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
	
	private ArrayList<AbstractTransferTask> visibleTasks;
	private ExecutorService transferExecutor;
	
	public AsynchronousTransferManager() {
		this.transferExecutor = Executors.newSingleThreadExecutor();
		this.visibleTasks = new ArrayList<AbstractTransferTask>();
	}

	public void submit(AbstractTransferTask asyncTransferObject) {
		synchronized(visibleTasks) {
			visibleTasks.add(asyncTransferObject);
			fireTableRowsInserted(visibleTasks.size() - 1, visibleTasks.size() - 1);
			asyncTransferObject.addPropertyChangeListener(this);
			transferExecutor.submit(asyncTransferObject, asyncTransferObject);
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		synchronized(visibleTasks) {
			AbstractTransferTask source = (AbstractTransferTask)evt.getSource();
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
		AbstractTransferTask row = visibleTasks.get(rowIndex);
		switch (columnIndex) {
			case PROGRESS_COLUMN:
				return row.getState();
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
}
