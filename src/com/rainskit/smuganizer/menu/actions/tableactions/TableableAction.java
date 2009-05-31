package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.menu.TableMenuManager;
import com.rainskit.smuganizer.tree.transfer.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;

public abstract class TableableAction extends AbstractAction {
	protected Main main;
	protected TransferTable table;
	protected AsynchronousTransferManager transferManager;
	private String statusText;
	
	public TableableAction(Main main, TransferTable table, AsynchronousTransferManager transferManager, String name, String statusText) {
		super(name);
		
		this.main = main;
		this.table = table;
		this.transferManager = transferManager;
		this.statusText = statusText;
		
		updateState(table.getSelectedItems());
	}
	
	public final void actionPerformed(ActionEvent e) {
		main.setStatus(statusText);
		try {
			performAction(table.getSelectedItems(), transferManager);
		} finally {
			main.clearStatus();
		}
	}
	
	protected abstract void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager);

	public abstract void updateState(List<AbstractTransferTask> selectedTasks);
}
