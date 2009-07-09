package com.rainskit.smuganizer;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.TransferTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TransferTable extends JTable {
	private static final int PROGRESS_PAINT_FREQUENCY = 25;
	
	public TransferTable(TransferTableModel dataModel, boolean multipleSelection) {
		super(dataModel);
		
		if (multipleSelection) {
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		for (int i = 0; i < dataModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setPreferredWidth(dataModel.getPreferredColumnWidth(i));
		}
		setFillsViewportHeight(true);
		
		TransferTableRenderer normalRenderer = new TransferTableRenderer();
		ProgressRenderer progressRenderer = new ProgressRenderer();
		Enumeration<TableColumn> columns = columnModel.getColumns();
		while (columns.hasMoreElements()) {
			TableColumn each = columns.nextElement();
			if (each.getHeaderValue().equals(TransferTableModel.PROGRESS_COLUMN_NAME)) {
				each.setCellRenderer(progressRenderer);
			} else {
				each.setCellRenderer(normalRenderer);
			}
		}
		
		startProgressPainter(TransferTableModel.PROGRESS_COLUMN);
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
	
	private void startProgressPainter(final int progressColumn) {
		Timer timer = new Timer(PROGRESS_PAINT_FREQUENCY, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						for (int row = 0; row < TransferTable.this.getRowCount(); row++) {
							AbstractTransferTask each = getTaskForRow(row);
							if (each.isActive()) {
								AbstractTableModel model = (AbstractTableModel)TransferTable.this.getModel();
								model.fireTableCellUpdated(row, progressColumn);
								break;
							}
						}
					}
				});
			}
		});
		timer.start();
	}
	
	
	private static JComponent modifyRendererForTask(JComponent renderer, AbstractTransferTask task,
													JTable table, boolean isSelected, boolean hasFocus,
													boolean useBrighterBackground) {
		renderer.setFont(table.getFont());
		renderer.setToolTipText(task.getStatusTooltip());
		if (isSelected) {
			renderer.setForeground(table.getSelectionForeground());
			if (useBrighterBackground) {
				renderer.setBackground(table.getSelectionBackground().brighter());
			} else {
				renderer.setBackground(table.getSelectionBackground());
			}
		} else {
			if (task.isInterrupted()) {
				renderer.setForeground(Color.ORANGE.darker());
			}
			else if (task.isErrored()) {
				renderer.setForeground(Color.RED);
			} else if (task.isActive()) {
				renderer.setForeground(Color.GREEN.darker());
			} else {
				renderer.setForeground(table.getForeground());
			}
			renderer.setBackground(table.getBackground());
		}
		if (hasFocus) {
			Border border = null;
			if (isSelected) {
				border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
			}
			if (border == null) {
				border = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			renderer.setBorder(border);
		} else {
			renderer.setBorder(new EmptyBorder(1, 1, 1, 1));
		}
		return renderer;
	}

	
	private class TransferTableRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
														boolean isSelected, boolean hasFocus, 
														int row, int column) {
			setValue(value);
			return modifyRendererForTask(this, getTaskForRow(row), table, isSelected, hasFocus, false);
		}
	}
	
	private class ProgressRenderer implements TableCellRenderer {
		private JProgressBar activeBar;
		private JProgressBar inactiveBar;
		
		public ProgressRenderer() {
			activeBar = new JProgressBar(){
				@Override
				public boolean isDisplayable() {
					return true;	//this makes it actually paint, even though it's not in the component hierarchy
				}
			};
			activeBar.setBorderPainted(true);
			activeBar.setIndeterminate(true);
			activeBar.setStringPainted(true);
			
			inactiveBar = new JProgressBar();
			inactiveBar.setBorderPainted(true);
			inactiveBar.setIndeterminate(false);
			inactiveBar.setStringPainted(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, 
														boolean isSelected, boolean hasFocus, 
														int row, int column) {
			AbstractTransferTask currentTask = getTaskForRow(row);
			JProgressBar progressBar = (currentTask.isActive() ? activeBar : inactiveBar);
			progressBar.setString(value.toString());
			return modifyRendererForTask(progressBar, currentTask, table, isSelected, hasFocus, true);
		}
	}
}



class JProgressBarTest {
	public static void main(String[] args) {
		JFrame frame = new JFrame("ProgressBar test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		JProgressBar testBar = new JProgressBar();
		testBar.setIndeterminate(true);
		testBar.setStringPainted(true);
		testBar.setString("Test");
		frame.getContentPane().add(testBar);
		frame.pack();
		frame.setVisible(true);
	}
}