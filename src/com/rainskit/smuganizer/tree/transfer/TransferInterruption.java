package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;

public abstract class TransferInterruption extends Exception {
	public TransferInterruption(String message) {
		super(message);
	}

	public abstract String getErrorText();

	public abstract TransferErrorDialog.RepairPanel getRepairPanel();
}
