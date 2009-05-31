package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.util.List;
import javax.swing.JOptionPane;

public class CancelTaskAction extends TableableAction {

	public CancelTaskAction(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Cancel...", "Canceling...");
	}

	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		String message = "Are you sure you want to cancel " + (selectedItems.size() > 1 ? "these items?" : "this item?");
		int answer = JOptionPane.showConfirmDialog(main, message, "Are you sure?", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			for (AbstractTransferTask each : selectedItems) {
				transferManager.cancel(each);
			}
		}
	}

	@Override
	public void updateState(List<AbstractTransferTask> selectedTasks) {
		setEnabled(!selectedTasks.isEmpty());
	}

}
