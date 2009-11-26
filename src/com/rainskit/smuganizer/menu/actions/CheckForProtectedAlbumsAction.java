package com.rainskit.smuganizer.menu.actions;

import com.rainskit.smuganizer.settings.GallerySettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

public class CheckForProtectedAlbumsAction extends AbstractAction {
	public CheckForProtectedAlbumsAction() {
		super("Check if albums are public or protected (slows down the initial load)");
		setSelected(GallerySettings.getCheckProtectedAlbums());
	}
	
	public void actionPerformed(ActionEvent e) {
		setSelected(((JMenuItem)e.getSource()).isSelected());
	}
	
	void setSelected(boolean selected) {
		putValue(SELECTED_KEY, Boolean.valueOf(selected));
		GallerySettings.setCheckProtectedAlbums(selected);
	}
}
