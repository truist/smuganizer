package com.rainskit.smuganizer.tree.transfer.interruptions;

import com.rainskit.smuganizer.menu.gui.TransferErrorDialog;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public abstract class TransferInterruption extends Exception {
	public TransferInterruption(String message) {
		super(message);
	}

	public abstract String getErrorText();

	public abstract TransferErrorDialog.RepairPanel getRepairPanel();

	protected JTextArea makeMultiLineLabel(String text, Color background) {
		JTextArea label = new JTextArea(text);
		label.setFont(new JLabel().getFont());
		label.setEditable(false);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setBackground(background);
		return label;
	}
}
