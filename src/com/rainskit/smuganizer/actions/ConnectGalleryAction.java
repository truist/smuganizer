package com.rainskit.smuganizer.actions;

import com.rainskit.smuganizer.GalleryLoginDialog;
import com.rainskit.smuganizer.GallerySettings;
import com.rainskit.smuganizer.Main;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class ConnectGalleryAction extends AbstractAction {
	private Main main;
	
	public ConnectGalleryAction(Main main) {
		super("Connect...");
		
		this.main = main;
	}

	public void actionPerformed(ActionEvent e) {
		GallerySettings gallerySettings = new GallerySettings();
		GalleryLoginDialog settingsDialog = new GalleryLoginDialog(main, gallerySettings);
		
		settingsDialog.setVisible(true);
		if (!settingsDialog.wasClosedWithOK()) {
			return;
		}
		main.loadGalleryTree(gallerySettings);
	}

}
