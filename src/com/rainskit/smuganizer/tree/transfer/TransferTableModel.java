package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class TransferTableModel extends AbstractTableModel {
	public static final int PROGRESS_COLUMN = 0;
	public static final String PROGRESS_COLUMN_NAME = "Progress";
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
	
	private ArrayList<AbstractTransferTask> transferTasks;
	
	public TransferTableModel() {
		this.transferTasks = new ArrayList<AbstractTransferTask>();
	}

	public TransferTableModel(List<AbstractTransferTask> selectedItems) {
		this();
		transferTasks.addAll(selectedItems);
		fireTableRowsInserted(0, transferTasks.size() - 1);
	}

	public void addTask(AbstractTransferTask transferTask) {
		transferTasks.add(transferTask);
		fireTableRowsInserted(transferTasks.size() - 1, transferTasks.size() - 1);
	}

	public void addTaskAfter(AbstractTransferTask each, AbstractTransferTask parentTask) {
		int parentIndex = transferTasks.indexOf(parentTask);
		transferTasks.add(parentIndex + 1, each);
		fireTableRowsInserted(parentIndex, parentIndex);
	}
	
	public void taskUpdated(AbstractTransferTask task) {
		int row = transferTasks.indexOf(task);
		fireTableRowsUpdated(row, row);
	}

	public void removeTask(AbstractTransferTask task) {
		int row = transferTasks.indexOf(task);
		transferTasks.remove(row);
		fireTableRowsDeleted(row, row);
	}
	
	public AbstractTransferTask getTaskAtRow(int row) {
		return transferTasks.get(row);
	}
	
	public int getRowCount() {
		return transferTasks.size();
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		AbstractTransferTask row = transferTasks.get(rowIndex);
		switch (columnIndex) {
			case PROGRESS_COLUMN:
				return row.getStatus().toString();
			case ACTION_COLUMN:
				return row.getActionString();
			case SOURCE_COLUMN:
				return row.getSourceLabel();
			case DEST_COLUMN:
				return row.getDestLabel();
			default:
				throw new IllegalArgumentException("Invalid column: " + columnIndex);
		}
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
}
