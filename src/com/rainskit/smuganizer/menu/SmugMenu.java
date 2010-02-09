package com.rainskit.smuganizer.menu;

import com.rainskit.smuganizer.tree.transfer.TransferTable;
import com.rainskit.smuganizer.menu.actions.SettingsSortCategoryAction;
import com.rainskit.smuganizer.menu.actions.AboutAction;
import com.rainskit.smuganizer.menu.actions.CleanCaptionsAction;
import com.rainskit.smuganizer.menu.actions.CheckForProtectedAlbumsAction;
import com.rainskit.smuganizer.menu.actions.SettingsSortAction;
import com.rainskit.smuganizer.menu.actions.HelpAction;
import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.menu.actions.AlwaysIgnoreFileCaptionAction;
import com.rainskit.smuganizer.menu.actions.AlwaysRemoveFileCaptionAction;
import com.rainskit.smuganizer.menu.actions.PauseTransfersAction;
import com.rainskit.smuganizer.menu.actions.PreserveCaptionsAction;
import com.rainskit.smuganizer.galleryapiwrapper.GalleryTree;
import com.rainskit.smuganizer.menu.actions.LogContentAction;
import com.rainskit.smuganizer.menu.actions.LogHeadersAction;
import com.rainskit.smuganizer.menu.actions.ViewLogAction;
import com.rainskit.smuganizer.smugmugapiwrapper.SmugTree;
import com.rainskit.smuganizer.tree.transfer.AsynchronousTransferManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class SmugMenu extends JMenuBar {
	public SmugMenu(Smuganizer main, SmugTree smugTree, GalleryTree galleryTree, TransferTable transferTable, AsynchronousTransferManager transferManager) {
		super();
		
		JMenu menu = new JMenu("Settings");
		menu.add(createGallerySettingsMenu());
		menu.add(createSmugMugSettingsMenu());
        menu.add(createFileSettingsMenu());
		menu.add(createTransferSettingsMenu());
		menu.addSeparator();
		menu.add(new JCheckBoxMenuItem(new PauseTransfersAction(transferManager)));
		add(menu);

		add(createLogMenu(main));

		add(createHelpMenu(main));
	}
	
	private JMenu createGallerySettingsMenu() {
		JMenu settingsMenu = new JMenu("Gallery");
		settingsMenu.add(new JCheckBoxMenuItem(new CheckForProtectedAlbumsAction()));
		settingsMenu.add(new JCheckBoxMenuItem(new CleanCaptionsAction()));
		return settingsMenu;
	}
	
	private JMenu createSmugMugSettingsMenu() {
		JMenu settingsMenu = new JMenu("SmugMug");
		SettingsSortCategoryAction sortAlbumsAction = new SettingsSortCategoryAction();
		SettingsSortAction sortAction = new SettingsSortAction(sortAlbumsAction);
		settingsMenu.add(new JCheckBoxMenuItem(sortAction));
		settingsMenu.add(new JCheckBoxMenuItem(sortAlbumsAction));
		return settingsMenu;
	}
	
	private JMenu createFileSettingsMenu() {
		JMenu settingsMenu = new JMenu("Local Computer");
		settingsMenu.add(new JCheckBoxMenuItem(new PreserveCaptionsAction()));
		return settingsMenu;
	}

	private JMenu createTransferSettingsMenu() {
		JMenu settingsMenu = new JMenu("Transfer");
		MaxOneGroup group = new MaxOneGroup();
		JMenuItem alwaysRemove = new JCheckBoxMenuItem(new AlwaysRemoveFileCaptionAction());
		group.add(alwaysRemove);
		settingsMenu.add(alwaysRemove);
		JMenuItem alwaysIgnore = new JCheckBoxMenuItem(new AlwaysIgnoreFileCaptionAction());
		group.add(alwaysIgnore);
		settingsMenu.add(alwaysIgnore);
		return settingsMenu;
	}

	private JMenu createLogMenu(Smuganizer main) {
		JMenu logMenu = new JMenu("Log");
		logMenu.add(new ViewLogAction(main));
		MaxOneGroup group = new MaxOneGroup();
		JMenuItem logHeaders = new JCheckBoxMenuItem(new LogHeadersAction());
		group.add(logHeaders);
		logMenu.add(logHeaders);
		JMenuItem logContent = new JCheckBoxMenuItem(new LogContentAction());
		group.add(logContent);
		logMenu.add(logContent);
		return logMenu;
	}

	private JMenu createHelpMenu(Smuganizer main) {
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
