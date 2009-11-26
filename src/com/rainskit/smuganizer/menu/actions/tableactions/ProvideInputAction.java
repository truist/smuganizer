package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.util.List;

public class ProvideInputAction extends TableableAction {
	public ProvideInputAction(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Provide input...", "Gathering input...");
	}

	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		ShowErrorAction.handleActionDialog(new TransferErrorDialog(main, selectedItems, true), transferManager, selectedItems);
	}

	@Override
	public void updateState(List<AbstractTransferTask> selectedTasks) {
		boolean allInterrupted = !selectedTasks.isEmpty();
		for (AbstractTransferTask each : selectedTasks) {
			allInterrupted &= each.isInterrupted();
		}
		setEnabled(allInterrupted);
	}
}
