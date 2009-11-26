package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.util.List;

public class ShowErrorAction extends TableableAction {
	public ShowErrorAction(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Show error...", "Showing error...");
	}

	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		handleActionDialog(new TransferErrorDialog(main, selectedItems, false), transferManager, selectedItems);
	}
	
	public static void handleActionDialog(TransferErrorDialog dialog, 
											AsynchronousTransferManager transferManager,
											List<AbstractTransferTask> selectedItems) {
		dialog.setVisible(true);
		if (dialog.shouldRetryTasks()) {
			transferManager.retry(selectedItems);
		}
	}

	@Override
	public void updateState(List<AbstractTransferTask> selectedTasks) {
		boolean allErrored = !selectedTasks.isEmpty();
		for (AbstractTransferTask each : selectedTasks) {
			allErrored &= each.isErrored();
		}
		setEnabled(allErrored);
	}
}
