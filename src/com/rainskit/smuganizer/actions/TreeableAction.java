package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;

abstract class TreeableAction extends AbstractAction {
	private String statusText;
	protected Main main;

	public TreeableAction(String name, String statusText, Main main) {
		super(name);
		this.statusText = statusText;
		this.main = main;
		updateState(new ArrayList<TreeableGalleryItem>().iterator());
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

	public abstract void updateState(Iterator<TreeableGalleryItem> currentItems);
}
