package com.rainskit.smuganizer.menu;

import com.rainskit.smuganizer.menu.actions.treeactions.TreeableAction;
import com.rainskit.smuganizer.menu.actions.SettingsSortCategoryAction;
import com.rainskit.smuganizer.menu.actions.AboutAction;
import com.rainskit.smuganizer.menu.actions.CleanCaptionsAction;
import com.rainskit.smuganizer.menu.actions.CheckForProtectedAlbumsAction;
import com.rainskit.smuganizer.menu.actions.ConnectGalleryAction;
import com.rainskit.smuganizer.menu.actions.SettingsSortAction;
import com.rainskit.smuganizer.menu.actions.ConnectSmugMugAction;
import com.rainskit.smuganizer.menu.actions.HelpAction;
import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.menu.actions.AlwaysIgnoreFileCaptionAction;
import com.rainskit.smuganizer.menu.actions.AlwaysRemoveFileCaptionAction;
import com.rainskit.smuganizer.tree.GalleryTree;
import com.rainskit.smuganizer.tree.SmugTree;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTree;

public class SmugMenu extends JMenuBar {
	public SmugMenu(Main main, SmugTree smugTree, GalleryTree galleryTree, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super();
		
		add(makeTreeMenu(main, galleryTree, "Gallery", new ConnectGalleryAction(main), createGallerySettingsMenu()));
		add(makeTreeMenu(main, smugTree, "SmugMug", new ConnectSmugMugAction(main), createSmugMugSettingsMenu()));
		
		add(makeTableMenu(main, transferTable, transferManager));
		
		add(createHelpMenu(main));
	}

	private JMenu makeTreeMenu(Main main, JTree tree, String title, Action connectAction, JMenu settings) {
		JMenu menu = new JMenu(title);
		menu.add(connectAction);
		if (settings != null) {
			menu.add(settings);
		}
		menu.addSeparator();
		TreeMenuManager manager = new TreeMenuManager(main, tree);
		addActionsToMenu(manager.getActions(), menu);
		return menu;
	}
	
	private JMenu makeTableMenu(Main main, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		JMenu menu = new JMenu("Transfer");
		
		JMenu settingsMenu = new JMenu("Settings");
		MaxOneGroup group = new MaxOneGroup();
		JMenuItem alwaysRemove = new JCheckBoxMenuItem(new AlwaysRemoveFileCaptionAction());
		group.add(alwaysRemove);
		settingsMenu.add(alwaysRemove);
		JMenuItem alwaysIgnore = new JCheckBoxMenuItem(new AlwaysIgnoreFileCaptionAction());
		group.add(alwaysIgnore);
		settingsMenu.add(alwaysIgnore);
		
		menu.add(settingsMenu);
		menu.addSeparator();
		
		TableMenuManager manager = new TableMenuManager(main, transferTable, transferManager);
		addActionsToMenu(manager.getActions(), menu);
		return menu;
	}
	
	private void addActionsToMenu(List<? extends AbstractAction> actions, JMenu menu) {
		for (AbstractAction each : actions) {
			if (each != null) {
				menu.add(new JMenuItem(each));
			} else {
				menu.addSeparator();
			}
		}
	}

	private JMenu createSmugMugSettingsMenu() {
		JMenu settingsMenu = new JMenu("Settings");
		SettingsSortCategoryAction sortAlbumsAction = new SettingsSortCategoryAction();
		SettingsSortAction sortAction = new SettingsSortAction(sortAlbumsAction);
		settingsMenu.add(new JCheckBoxMenuItem(sortAction));
		settingsMenu.add(new JCheckBoxMenuItem(sortAlbumsAction));
		return settingsMenu;
	}
	
	private JMenu createGallerySettingsMenu() {
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.add(new JCheckBoxMenuItem(new CheckForProtectedAlbumsAction()));
		settingsMenu.add(new JCheckBoxMenuItem(new CleanCaptionsAction()));
		return settingsMenu;
	}
	
	private JMenu createHelpMenu(Main main) {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new HelpAction(main));
		helpMenu.add(new AboutAction(main));
		return helpMenu;
	}
	
	
	private class MaxOneGroup implements ItemListener {
		private ArrayList<AbstractButton> buttons;
		
		public MaxOneGroup() {
			this.buttons = new ArrayList<AbstractButton>();
		}

		private void add(AbstractButton button) {
			button.addItemListener(this);
			buttons.add(button);
		}

		public void itemStateChanged(ItemEvent ie) {
			if (ItemEvent.SELECTED == ie.getStateChange()) {
				Object item = ie.getItem();
				for (AbstractButton each : buttons) {
					if (each != item) {
//						each.setSelected(false);
						if (each.isSelected()) {
							ButtonModel model = each.getModel();
							model.setArmed(true);
							model.setPressed(true);
							model.setPressed(false);
							model.setArmed(false);
						}
					}
				}
			}
		}
	}
}
