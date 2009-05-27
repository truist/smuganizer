package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.menu.gui.HelpWindow;
import com.rainskit.smuganizer.Main;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class HelpAction extends AbstractAction {
	private Main main;

	public HelpAction(Main main) {
		super("Help");
		this.main = main;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			new HelpWindow(main).setVisible(true);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(main, "Error showing help: " + ex.getLocalizedMessage(), "Error loading help", JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(HelpAction.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
