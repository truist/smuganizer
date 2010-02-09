package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.Smuganizer;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class ViewLogAction extends AbstractAction {
	private Smuganizer main;

	public ViewLogAction(Smuganizer main) {
		super("View log");
		this.main = main;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Desktop.getDesktop().open(Main.getLogFile());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(main, "Error showing log: " + ex.getLocalizedMessage(), "Error showing log", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(ViewLogAction.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
