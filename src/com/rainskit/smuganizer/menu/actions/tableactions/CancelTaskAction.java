package com.rainskit.smuganizer.menu.actions.tableactions;

import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class CancelTaskAction extends TableableAction {
	private static final String ACTION_MAP_KEY = "delete";

	public CancelTaskAction(Smuganizer main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super(main, transferTable, transferManager, "Cancel...", "Canceling...");
		
		transferTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_MAP_KEY);
		transferTable.getActionMap().put(ACTION_MAP_KEY, this);
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
