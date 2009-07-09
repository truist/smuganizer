package com.rainskit.smuganizer.menu;

import com.rainskit.smuganizer.menu.actions.tableactions.CancelTaskAction;
import com.rainskit.smuganizer.menu.actions.tableactions.ProvideInputAction;
import com.rainskit.smuganizer.menu.actions.tableactions.TableableAction;
import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.menu.actions.tableactions.RetryTaskAction;
import com.rainskit.smuganizer.menu.actions.tableactions.ShowErrorAction;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TableMenuManager implements ListSelectionListener {
	private TransferTable transferTable;
	
	private ArrayList<TableableAction> actions;
	private JPopupMenu popupMenu;
	
	private ProvideInputAction inputAction;
	private ShowErrorAction errorAction;
	
	public TableMenuManager(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		this.transferTable = transferTable;
		
		actions = createActions(main, transferTable, transferManager);
		
		popupMenu = new JPopupMenu();
		for (TableableAction each : actions) {
			if (each != null) {
				popupMenu.add(new JMenuItem(each));
			} else {
				popupMenu.addSeparator();
			}
		}
		
		transferTable.getSelectionModel().addListSelectionListener(this);
		transferTable.addMouseListener(new ClickListener());
	}
	
	public List<? extends AbstractAction> getActions() {
		return actions;
	}
	
	private ArrayList<TableableAction> createActions(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		ArrayList<TableableAction> newActions = new ArrayList<TableableAction>();
		newActions.add(new RetryTaskAction(main, transferTable, transferManager));
		newActions.add(new CancelTaskAction(main, transferTable, transferManager));
		newActions.add(null);
		newActions.add(errorAction = new ShowErrorAction(main, transferTable, transferManager));
		newActions.add(inputAction = new ProvideInputAction(main, transferTable, transferManager));
		return newActions;
	}

	public void valueChanged(ListSelectionEvent e) {
		List<AbstractTransferTask> selectedTasks = transferTable.getSelectedItems();
		for (TableableAction each : actions) {
			if (each != null) {
				each.updateState(selectedTasks);
			}
		}
	}
	
	
	private class ClickListener extends MouseAdapter {
		@Override public void mousePressed(MouseEvent e) { handleEvent(e); }
		@Override public void mouseReleased(MouseEvent e) { handleEvent(e); }
		private void handleEvent(MouseEvent e) {
			if (e.isPopupTrigger()) {
				int row = transferTable.rowAtPoint(e.getPoint());
				if (row > -1) {
					boolean inSelection = false;
					int[] selectedRows = transferTable.getSelectedRows();
					for (int each : selectedRows) {
						inSelection |= (each == row);
					}
					if (!inSelection) {
						transferTable.getSelectionModel().setSelectionInterval(row, row);
					}
					popupMenu.show(transferTable, e.getX(), e.getY());
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (inputAction.isEnabled() && !errorAction.isEnabled()) {
					inputAction.actionPerformed(null);
				} else if (errorAction.isEnabled() && !inputAction.isEnabled()) {
					errorAction.actionPerformed(null);
				}
			}
		}
	}
}
