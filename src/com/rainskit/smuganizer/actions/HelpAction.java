package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;

class HelpAction extends AbstractAction {
	private Main main;

	public HelpAction(Main main) {
		super("Help");
		this.main = main;
	}

	public void actionPerformed(ActionEvent e) {
		main.showHelp();
	}
}
