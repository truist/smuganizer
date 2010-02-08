package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.menu.TreeMenuManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public abstract class TreeableAction extends AbstractAction {
	protected Main main;
	protected TreeMenuManager menuManager;
	private String statusText;

	public TreeableAction(Main main, TreeMenuManager menuManager, String name, String statusText) {
		super(name);
		this.main = main;
		this.menuManager = menuManager;
		this.statusText = statusText;
		updateState();
	}

	public final void actionPerformed(ActionEvent e) {
		main.setStatus(statusText);
		try {
			performAction();
		} catch (IOException ex) {
			Logger.getLogger(TreeableAction.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(main, "ERROR: " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		} finally {
			main.clearStatus();
		}
	}
	
	protected abstract void performAction() throws IOException;

	public abstract void updateState();
}
