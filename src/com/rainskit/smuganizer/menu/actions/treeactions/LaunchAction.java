package com.rainskit.smuganizer.menu.actions.treeactions;

import com.rainskit.smuganizer.menu.*;
import com.rainskit.smuganizer.Smuganizer;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.KeyStroke;

public class LaunchAction extends TreeableAction {
	private static final String ACTION_MAP_KEY = "launch";
	
	public LaunchAction(TreeMenuManager menuManager, Smuganizer main, JTree tree) {
		super(main, menuManager, "Open", "Launching...");
		
		tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), ACTION_MAP_KEY);
		tree.getActionMap().put(ACTION_MAP_KEY, this);
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
	public void updateState() {
		boolean anyLaunchable = false;
		try {
			for (TreeableGalleryItem each : menuManager.getCurrentItems()) {
					anyLaunchable |= each.canBeLaunched();
			}
		} catch (IOException ex) {
			Logger.getLogger(LaunchAction.class.getName()).log(Level.SEVERE, null, ex);
			anyLaunchable = false;
		}
		setEnabled(anyLaunchable && Desktop.isDesktopSupported());
	}
}
