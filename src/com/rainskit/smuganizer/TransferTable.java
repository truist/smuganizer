package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TransferTable extends JTable {
	public TransferTable(AsynchronousTransferManager dataModel) {
		super(dataModel);
		
//		columnModel.getColumn(AsynchronousTransferManager.PROGRESS_COLUMN).setCellRenderer(new ProgressCellRenderer());
		for (int i = 0; i < dataModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(dataModel.getPreferredColumnWidth(i));
		}
		setFillsViewportHeight(true);
	}
	
	
//	private class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
//		public ProgressCellRenderer() {
//			super(0, 100);
//			setStringPainted(true);
//			setBorderPainted(false);
//			setBackground(TransferTable.this.getBackground());
//		}
//
//		public Component getTableCellRendererComponent(JTable table, Object value, 
//														boolean isSelected, boolean hasFocus, 
//														int row, int column) {
//			setValue(((Integer)value).intValue());
//			return this;
//		}
//	}
}
