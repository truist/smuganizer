package com.rainskit.smuganizer.tree.transfer;

import com.rainskit.smuganizer.tree.transfer.tasks.AbstractTransferTask;

public interface StatusListener {
	public void statusChanged(AbstractTransferTask task);
}
