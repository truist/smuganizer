package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.util.List;

public class RetryTaskAction extends TableableAction {

	public RetryTaskAction(Smuganizer main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Retry", "Retrying...");
	}

	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		transferManager.retry(selectedItems);
	}

	@Override
	public void updateState(List<AbstractTransferTask> selectedTasks) {
		boolean allErrored = (!selectedTasks.isEmpty());
		for (AbstractTransferTask each : selectedTasks) {
			allErrored &= each.isErrored();
		}
		setEnabled(allErrored);
	}
}
