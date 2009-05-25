package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.SmugMugLoginDialog;
import com.rainskit.smuganizer.SmugMugSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ConnectSmugMugAction extends AbstractAction {
	private Main main;
	
	public ConnectSmugMugAction(Main main) {
		super("Connect...");
		this.main = main;
	}

	public void actionPerformed(ActionEvent e) {
		SmugMugLoginDialog settingsDialog = new SmugMugLoginDialog(main);
		settingsDialog.setVisible(true);
		if (settingsDialog.wasClosedWithOK()) {
			main.loadSmugTree();
		}
	}
}
