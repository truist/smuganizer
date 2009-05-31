package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.menu.TreeMenuManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

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
		} finally {
			main.clearStatus();
		}
	}
	
	protected abstract void performAction();

	public abstract void updateState();
}
