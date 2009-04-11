package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.Main;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;

class LaunchAction extends TreeableAction {
	private TreeMenuManager menuManager;

	public LaunchAction(TreeMenuManager menuManager, Main main) {
		super("View in web browser", "Launching...", main);
		this.menuManager = menuManager;
	}

	protected void performAction() {
		for (TreeableGalleryItem each : menuManager.getCurrentItems()) {
			try {
				each.launch();
			} catch (IOException ex) {
				Logger.getLogger(LaunchAction.class.getName()).log(Level.SEVERE, null, ex);
			} catch (URISyntaxException ex) {
				Logger.getLogger(LaunchAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	@Override
	public void updateState(Iterator<TreeableGalleryItem> items) {
		boolean anyLaunchable = false;
		while (items.hasNext()) {
			anyLaunchable |= items.next().canBeLaunched();
		}
		setEnabled(anyLaunchable && Desktop.isDesktopSupported());
	}
}
