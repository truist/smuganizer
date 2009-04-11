package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.AboutBox;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

class AboutAction extends AbstractAction {
	private JFrame parent;

	public AboutAction(JFrame parent) {
		super("About");
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		new AboutBox(parent, true);
	}
}
