package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.transfer.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class TransferTable extends JTable {
	public TransferTable(TransferTableModel dataModel) {
		super(dataModel);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		for (int i = 0; i < dataModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(dataModel.getPreferredColumnWidth(i));
		}
		setFillsViewportHeight(true);
		
		TransferTableRenderer renderer = new TransferTableRenderer();
		Enumeration<TableColumn> columns = columnModel.getColumns();
		while (columns.hasMoreElements()) {
			TableColumn each = columns.nextElement();
			each.setCellRenderer(renderer);
		}
	}
	
	public List<AbstractTransferTask> getSelectedItems() {
		ArrayList<AbstractTransferTask> selectedItems = new ArrayList<AbstractTransferTask>();
		for (int row : getSelectedRows()) {
			selectedItems.add(getTaskForRow(row));
		}
		return selectedItems;
	}
	
	private AbstractTransferTask getTaskForRow(int row) {
		return ((TransferTableModel)dataModel).getTaskAtRow(row);
	}

	
	private class TransferTableRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
														boolean isSelected, boolean hasFocus, 
														int row, int column) {
			setForeground(null);
			setToolTipText(null);
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			AbstractTransferTask task = getTaskForRow(row);
			if (task.isInterrupted() || task.isErrored()) {
				setForeground(Color.RED);
				setToolTipText(task.getErrorMessage());
			} else if (task.isActive()) {
				setForeground(Color.GREEN.darker());
			}
			return this;
		}
	}
}
