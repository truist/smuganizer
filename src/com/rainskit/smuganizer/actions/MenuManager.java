package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.*;
import com.rainskit.smuganizer.tree.GalleryTree;
import com.rainskit.smuganizer.tree.SmugTree;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTree;

public class MenuManager {
	private JMenuBar menuBar;
	
	public MenuManager(Main main, SmugTree smugTree, GalleryTree galleryTree) {
		menuBar = new JMenuBar();
		
		menuBar.add(makeTreeMenu(main, galleryTree, "Gallery", new ConnectGalleryAction(main), createGallerySettingsMenu()));
		menuBar.add(makeTreeMenu(main, smugTree, "SmugMug", new ConnectSmugMugAction(main), createSmugMugSettingsMenu()));
		
		menuBar.add(createHelpMenu(main));
	}

	private JMenu makeTreeMenu(Main main, JTree tree, String title, Action connectAction, JMenu settings) {
		JMenu menu = new JMenu(title);
		menu.add(connectAction);
		if (settings != null) {
			menu.add(settings);
		}
		menu.addSeparator();
		TreeMenuManager manager = new TreeMenuManager(main, tree);
		for (TreeableAction each : manager.getActions()) {
			if (each != null) {
				menu.add(new JMenuItem(each));
			} else {
				menu.addSeparator();
			}
		}
		return menu;
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
		return settingsMenu;
	}
	
	private JMenu createHelpMenu(Main main) {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new HelpAction(main));
		helpMenu.add(new AboutAction(main));
		return helpMenu;
	}
	
	public JMenuBar getMenuBar() {
		return menuBar;
	}
}
