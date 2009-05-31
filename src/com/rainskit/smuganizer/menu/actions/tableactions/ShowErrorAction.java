package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.util.List;
import javax.swing.JOptionPane;

public class ShowErrorAction extends TableableAction {
	public ShowErrorAction(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Resolve error...", "Resolving error...");
	}

	@Override
	protected void performAction(List<AbstractTransferTask> selectedItems, AsynchronousTransferManager transferManager) {
		JOptionPane.showMessageDialog(main, "It worked!");
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
