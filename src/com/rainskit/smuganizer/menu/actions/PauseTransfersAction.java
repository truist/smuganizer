package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class PauseTransfersAction extends AbstractAction {
	private AsynchronousTransferManager transferManager;
	
	public PauseTransfersAction(AsynchronousTransferManager transferManager) {
		super("Pause all transfers");
		this.transferManager = transferManager;
		setSelected(false);
	}

	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}
	
	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		if (selected) {
			transferManager.pause();
		} else {
			transferManager.resume();
		}
	}
}
